package com.krolis.tipapp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.test.RenamingDelegatingContext;

import com.krolis.tipapp.dao.DBConnection;
import com.krolis.tipapp.dao.GradeMockProvider;
import com.krolis.tipapp.dao.GradeOnlineProvider;
import com.krolis.tipapp.dao.GradeProvider;
import com.krolis.tipapp.model.Grade;
import com.krolis.tipapp.util.NoActiveSessionOnServer;
import com.krolis.tipapp.util.NoInternetConnection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestApp {
    GradeProvider gradeProvider;
    Context context;
    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        gradeProvider = GradeOnlineProvider.getInstance();
        context = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
    }

    @Test
    public void shouldLoginWrong() throws NoInternetConnection,IOException {
        if(GradeProvider.isOnline())
            assertEquals(GradeProvider.LOGIN_WRONG, gradeProvider.login("11111", "test"));
        else{
            thrown.expect(NoInternetConnection.class);
            gradeProvider.login("11111", "test");
        }
    }

    @Test
    public void getSemestersThrowsOrNull() throws NoActiveSessionOnServer, NoInternetConnection,IOException{
        if(GradeProvider.isOnline()){
            assertEquals(null, gradeProvider.getSemesters());
        }else{
            thrown.expect(NoInternetConnection.class);
            gradeProvider.getSemesters();
        }
    }

    @Test
    public void getActiveProvider() throws NoInternetConnection,IOException{
        GradeProvider tempProvider = GradeProvider.getActiveProvider();
        assertEquals(null, tempProvider);
    }

    @Test
    public void dbConnection(){
        GradeMockProvider gradeMockProvider = GradeMockProvider.getInstance();
        gradeMockProvider.login(GradeMockProvider.MOCK_LOGIN, GradeMockProvider.MOCK_PASSWORD);

        DBConnection db = new DBConnection(context,GradeMockProvider.MOCK_LOGIN);
        assertEquals(null, db.getSemesterTables());
        assertEquals(null, db.getGrades(""));
        List<String> semesters = gradeMockProvider.getSemesters();
        db.setupSemesterTables(semesters);

        for(String sem: semesters){
            for(Grade grade: gradeMockProvider.getGrades(sem)){
                db.insertGrade(sem,grade);
            }
            assertEquals(gradeMockProvider.getGrades(sem),db.getGrades(sem));
        }

    }

}