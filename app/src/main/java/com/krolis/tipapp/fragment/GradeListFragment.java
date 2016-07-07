package com.krolis.tipapp.fragment;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.krolis.tipapp.dao.GradeProvider;
import com.krolis.tipapp.util.NoActiveSessionOnServer;
import com.krolis.tipapp.util.NoInternetConnection;
import com.krolis.tipapp.util.RecyclerViewEmptySupport;
import com.krolis.tipapp.adapter.SemesterAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Krolis on 2016-06-14.*/


public class GradeListFragment extends Fragment {
    RecyclerView mRecyclerView;
    private SemesterAdapter mSemesterAdapter;
    List<String> semesters;
    LinearLayoutManager mLayoutManager;
    GradeProvider provider;


    public GradeListFragment() {
        super();
    }

    public static GradeListFragment newInstance(GradeProvider provider) {
        GradeListFragment fragment = new GradeListFragment();
        fragment.setProvider(provider);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_grade_list, container, false);
        mRecyclerView = (RecyclerViewEmptySupport) rootView.findViewById(R.id.grade_rec_view);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        semesters = new ArrayList<>();
        mSemesterAdapter = new SemesterAdapter(semesters, getContext(),provider);
        View gradeRecViewEmpty = rootView.findViewById(R.id.grade_rec_view_empty);
        ((RecyclerViewEmptySupport)mRecyclerView).setEmptyView(gradeRecViewEmpty);
        ((RecyclerViewEmptySupport)mRecyclerView).setEmptyOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadSemestersTask().execute();
            }
        });
        ((RecyclerViewEmptySupport)mRecyclerView).setEmptyClickable(false);

        mRecyclerView.setAdapter(mSemesterAdapter);
        mRecyclerView.setHasFixedSize(true);


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        new DownloadSemestersTask().execute();
    }

    public void setProvider(GradeProvider provider) {
        this.provider = provider;
    }

    class DownloadSemestersTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((RecyclerViewEmptySupport)mRecyclerView).setEmptyText("Pobieram dane");
            ((RecyclerViewEmptySupport)mRecyclerView).setEmptyClickable(false);
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            try{
                return provider.getSemesters();
            }catch(IOException e){
                e.printStackTrace();
                //// TODO: 2016-06-14  check net conn
                return new LinkedList<String>();
            }catch (NoInternetConnection e){
                return null;
            }catch (NoActiveSessionOnServer e){
                GradeProvider.logoutTask(getContext());
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> result) {
            if(result != null){
                semesters.addAll(result);
                mSemesterAdapter.notifyDataSetChanged();
            }else{
                ((RecyclerViewEmptySupport)mRecyclerView).setEmptyText("Brak połączenia z internetem :(\nnaciśnij aby odświeżyć.");
                ((RecyclerViewEmptySupport)mRecyclerView).setEmptyClickable(true);
            }


        }
    }
}
