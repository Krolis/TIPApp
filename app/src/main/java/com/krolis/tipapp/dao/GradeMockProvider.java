package com.krolis.tipapp.dao;

import com.krolis.tipapp.model.Grade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Krolis on 2016-06-22.
 */
public class GradeMockProvider extends GradeProvider {
    public static final String MOCK_LOGIN = "55555";
    public static final String MOCK_PASSWORD = "mock";
    private static GradeMockProvider mInstance = null;
    private static Object sync = new Object();
    private static  List<String> semesters;
    private boolean isActive = false;

    private GradeMockProvider(){  }

    public static GradeMockProvider getInstance(){
        if(mInstance == null){
            synchronized (sync){
                if(mInstance == null)
                    mInstance = new GradeMockProvider();
            }
        }
        return mInstance;
    }

    @Override
    public int login(String login, String pass){

        if(login.equals(MOCK_LOGIN) && pass.equals(MOCK_PASSWORD)){
            isActive=true;
            return LOGIN_OK;
        }
        return LOGIN_WRONG;
    }

    @Override
    public List<Grade> getGrades(String semester){
        List<Grade> result = new ArrayList<>();

        Map temp = new HashMap<String, String>();
        temp.put("5.0","22-22-2222");
        result.add(new Grade("nazwa przedmiotu", temp));
        return result;
    }

    @Override
    public List<String> getSemesters() {
        List<String> semesters = new ArrayList<>(4);
        semesters.add("pierwszy");
        semesters.add("drugi");
        semesters.add("trzeci");
        semesters.add("czwarty");
        return semesters;
    }

    @Override
    public int logout(){

        isActive=false;
        return GradeProvider.LOGIN_OK;
    }


    public boolean isActiveMockSession() {
        return isActive;
    }
}
