package com.krolis.tipapp.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.krolis.tipapp.model.Grade;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Krolis on 2016-06-21.
 */
public class GradesBaseAdapter<T> extends BaseAdapter {
    Context context;
    List<Grade> grades;
    LayoutInflater layoutInflater;

    public GradesBaseAdapter(Context context, List<Grade> grades) {
        this.context = context;
        this.grades = grades;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return grades.size();
    }

    @Override
    public Object getItem(int position) {
        if(position>=0 && position<grades.size())
            return grades.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView= layoutInflater.inflate(R.layout.grade_item, null);

        TextView subjectName = (TextView) convertView.findViewById(R.id.subject_name_tv);
        TextView subjectGrade = (TextView) convertView.findViewById(R.id.subject_grade_tv);

        subjectGrade.setText("");
        Grade g = (Grade) getItem(position);
        subjectName.setText(g.getSubject() + ":");
        if(g.getNotes().isEmpty()){
            subjectGrade.setText("\t\tbrak oceny");
        }else{

            Iterator it = g.getNotes().entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry e = (Map.Entry) it.next();

                subjectGrade.append("\t\tocena: ");
                subjectGrade.append(Html.fromHtml("<b>" + e.getKey() + "</b>"));
                subjectGrade.append(" data: ");
                subjectGrade.append((String) e.getValue());

                if (it.hasNext())
                    subjectGrade.append(System.getProperty("line.separator"));
            }
        }
        return convertView;
    }
}
