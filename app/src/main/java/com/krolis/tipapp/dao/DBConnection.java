package com.krolis.tipapp.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.krolis.tipapp.model.Grade;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Krolis on 2016-07-02.
 */
public class DBConnection extends SQLiteOpenHelper {
    private static int DB_VEERSION = 1;

    private static final String COLUMN_GRADE_ID = "GRADE_ID";
    private static final String COLUMN_GRADE_SUBJECT = "GRADE_SUBJECT";
    private static final String COLUMN_GRADE_NOTE = "GRADE_NOTE";
    private static final String COLUMN_GRADE_NOTE1 = "GRADE_NOTE1";
    private static final String COLUMN_GRADE_NOTE2 = "GRADE_NOTE2";
    private static final String COLUMN_GRADE_NOTE3 = "GRADE_NOTE3";
    private static final String COLUMN_GRADE_NOTE4 = "GRADE_NOTE4";
    private static final String SQL_SEMESTER_COLUMN = " (" +
                    COLUMN_GRADE_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_GRADE_SUBJECT + " TEXT, " +
                    COLUMN_GRADE_NOTE1 + " TEXT, " +
                    COLUMN_GRADE_NOTE2 + " TEXT, " +
                    COLUMN_GRADE_NOTE3 + " TEXT, " +
                    COLUMN_GRADE_NOTE4 + " TEXT" +
                    " )";


    public DBConnection(Context context, String name) {
        super(context, name, null, DB_VEERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    private String createSemesterTableSyntax(String sem) {
        String tempSem = sem.replace('/','_');
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS '");
        sb.append(tempSem);
        sb.append('\'');
        sb.append(SQL_SEMESTER_COLUMN);
        return sb.toString();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



    public boolean setupSemesterTables(List<String> semesters) {
        if(getSemesterTables()!=null){
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        for (String sem : semesters) {
            db.execSQL(createSemesterTableSyntax(sem));
        }
        return true;
    }

    public List<String> getSemesterTables(){
        List<String> result = new LinkedList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if(cursor.moveToFirst() && cursor.moveToNext()){
            do{
                result.add(cursor.getString(0).replace('_','/'));
            }while (cursor.moveToNext());
        }else{
            return null;
        }
        return result;
    }

    public List<Grade> getGrades(String semester) {
        List<Grade> result = new LinkedList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try{
            cursor = db.query(false, '\'' +semester.replace('/', '_') + '\'', null, null, null, null, null, null, null);
        }catch (SQLiteException e){
            if(cursor!=null)
                cursor.close();
            return null;
        }

        if(cursor.moveToFirst())
            do{
                int subjectColumn = cursor.getColumnIndex(COLUMN_GRADE_SUBJECT);
                int note1Column = cursor.getColumnIndex(COLUMN_GRADE_NOTE1);
                Map<String,String> notes = new HashMap<>();

                for(int i =0;i<4;i++){
                    String noteAndDate = cursor.getString(note1Column + i);
                    if(noteAndDate!= null && !noteAndDate.trim().isEmpty()) {
                        String temp2[] = noteAndDate.split(" ");
                        if (temp2.length >= 2)
                            notes.put(temp2[0].trim(), temp2[1].trim());
                    }
                }

                result.add(new Grade(cursor.getString(subjectColumn),notes));
            }while (cursor.moveToNext());
        cursor.close();
        return result;
    }

    public void insertGrade(String semester, Grade grade) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        semester = '\''+semester.replace('/','_')+'\'';

        values.put(COLUMN_GRADE_SUBJECT, grade.getSubject());
        Map<String, String> notes = grade.getNotes();
        int i =1;
        for(Map.Entry note: notes.entrySet()){
            values.put(COLUMN_GRADE_NOTE + i, note.getKey() + " " + note.getValue());
            i++;
        }

        Cursor cursor = db.query(false,semester,null,COLUMN_GRADE_SUBJECT+" = '"+grade.getSubject()+'\'',null,null,null,null,null);
        if(cursor.moveToFirst()){
            db.update(semester,values,COLUMN_GRADE_SUBJECT+" = '"+grade.getSubject()+'\'',null);
        }else{
            db.insert(semester,null,values);
        }


    }
}
