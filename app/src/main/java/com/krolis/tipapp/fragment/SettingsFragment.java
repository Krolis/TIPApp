package com.krolis.tipapp.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.krolis.tipapp.service.CheckGradesEventReceiver;
import com.krolis.tipapp.dao.GradeProvider;
import com.krolis.tipapp.TIPApplication;

/**
 * Created by Krolis on 2016-07-04.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(TIPApplication.PREFS_NAME);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(GradeProvider.isActiveSession() &&
                (key.equals(getString(R.string.pref_sync_key))||key.equals(getString(R.string.pref_sync_time_key))))
            CheckGradesEventReceiver.startMonitoring(getActivity());
    }
}
