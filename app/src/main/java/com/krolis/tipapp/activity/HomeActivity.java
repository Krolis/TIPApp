package com.krolis.tipapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.krolis.tipapp.service.CheckGradesEventReceiver;
import com.krolis.tipapp.dao.GradeMockProvider;
import com.krolis.tipapp.dao.GradeOnlineProvider;
import com.krolis.tipapp.dao.GradeProvider;
import com.krolis.tipapp.adapter.SectionsPagerAdapter;

/**
 * Created by Krolis on 2016-06-11.
 */
public class HomeActivity extends AppCompatActivity {

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    boolean isDoubleClicked;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if(GradeOnlineProvider.getInstance().isActiveOnlineSession()){
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), GradeOnlineProvider.getInstance());
        }else{
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), GradeMockProvider.getInstance());
        }
        mViewPager = (ViewPager) findViewById(R.id.mainViewPager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(SectionsPagerAdapter.POSITION_LIST);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!GradeProvider.isActiveSession()){
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                break;
            case R.id.action_logout:
                CheckGradesEventReceiver.stopMonitoring(getApplicationContext());
                GradeProvider.logoutTask(HomeActivity.this);
                break;
        }
        return true;
    }


    @Override
    public void onBackPressed() {

        if(mViewPager.getCurrentItem()==SectionsPagerAdapter.POSITION_INFO){
            mViewPager.setCurrentItem(SectionsPagerAdapter.POSITION_LIST);
            return;
        }

        if (isDoubleClicked) {
           // GradeProvider.logoutTask(HomeActivity.this);
            super.onBackPressed();
            return;
        }

        this.isDoubleClicked = true;
        Toast.makeText(this, "Naciśnij jeszcze raz aby wyjść", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isDoubleClicked= false;
            }
        }, 1000);
    }

}