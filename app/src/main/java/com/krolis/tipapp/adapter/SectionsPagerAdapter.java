package com.krolis.tipapp.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.krolis.tipapp.dao.GradeProvider;
import com.krolis.tipapp.fragment.GradeListFragment;
import com.krolis.tipapp.fragment.InfoFragment;

/**
 * Created by Krolis on 2016-06-14.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private static final int TABS_COUNT = 2;
    public static final int POSITION_LIST = 0;
    public static final int POSITION_INFO = 1;
    GradeProvider provider;

    public SectionsPagerAdapter(FragmentManager fm, GradeProvider provider) {
        super(fm);
        this.provider = provider;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment result;
        switch(position){
            case POSITION_INFO:
                result = new InfoFragment();
                break;
            default:
                result = GradeListFragment.newInstance(provider);
        }
        return result;
    }

    @Override
    public int getCount() {
        return TABS_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String result;
        switch(position){
            case POSITION_INFO:
                result =  "Informacje: ";
                break;
            default:
                result = "Twoje wyniki: ";
        }
        return result;
    }
}
