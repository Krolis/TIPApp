package com.krolis.tipapp.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.krolis.tipapp.util.NoActiveSessionOnServer;
import com.krolis.tipapp.util.NoInternetConnection;
import com.krolis.tipapp.dao.GradeProvider;
import com.krolis.tipapp.model.Grade;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Krolis on 2016-06-14.
 */

public class SemesterAdapter extends RecyclerView.Adapter<SemesterAdapter.SemViewHolder> {
    List<String> semesters;
    SparseArray<List<Grade>> gradesSparseArray;
    Context context;
    GradeProvider provider;

    public SemesterAdapter(List<String> semesters, Context context, GradeProvider provider) {
        this.semesters = semesters;
        gradesSparseArray = new SparseArray<List<Grade>>();
        this.context = context;
        this.provider=provider;
    }

    @Override
    public SemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.semester_item, parent, false);

        SemViewHolder vh = new SemViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(SemViewHolder holder, final int position) {
        String temp[] = semesters.get(position).split("/");
        if(temp.length>1)
            holder.titleText.setText("Rok " + temp[0] + " semestr " + (temp[1].charAt(temp[1].length() - 1) == 'Z' ? "zimowy" : "letni"));
        else
            holder.titleText.setText(semesters.get(position));

        holder.emptyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SemesterAdapter.this.notifyItemChanged(position);
            }
        });

        if(gradesSparseArray.get(position)==null ){
            new DownloadGrades(holder, position).execute();
        }else{
            fillGrades(holder, gradesSparseArray.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if(semesters!=null)
         return semesters.size();
        else
            return 0;
    }

    class SemViewHolder extends RecyclerView.ViewHolder{
        TextView titleText;
        ListView listView;
        TextView avgText;
        Button emptyButton;

        public SemViewHolder(View itemView) {
            super(itemView);
            listView = (ListView) itemView.findViewById(R.id.list);
            titleText = (TextView) itemView.findViewById(R.id.title_text);
            avgText = (TextView) itemView.findViewById(R.id.avg_text);
            View emptyView = itemView.findViewById(R.id.list_view_empty);
            listView.setEmptyView(emptyView);
            emptyButton = (Button) emptyView.findViewById(R.id.list_view_empty_button);
         }
    }

    private class DownloadGrades extends AsyncTask<Void ,Void, List<Grade>> {
        SemViewHolder holder;
        int position;

        public DownloadGrades(SemViewHolder holder, int position){
            this.holder = holder;
            if(position>=0 && position<semesters.size())
                this.position =position;
            else
                this.position = 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            holder.emptyButton.setText(R.string.empty_download_data);
            holder.emptyButton.setClickable(false);
        }

        @Override
        protected List<Grade> doInBackground(Void... params) {
            List<Grade> result;
            try{
                result = provider.getGrades(semesters.get(position));
            }catch (IOException e){
                e.printStackTrace();
                result = new LinkedList<Grade>();
            }catch (NoInternetConnection e){
                result = null;
            }catch (NoActiveSessionOnServer e){
                GradeProvider.logoutTask(context);
                result = null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<Grade> grades) {
            if(grades!=null){
                gradesSparseArray.put(position,grades);
                fillGrades(holder, grades);
            }else {
                holder.emptyButton.setClickable(true);
                holder.emptyButton.setText(R.string.empty_tap_to_refresh);
                Toast.makeText(context, R.string.toast_no_internet_conn,Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void fillGrades(SemViewHolder holder, List<Grade> grades) {
        BaseAdapter adapter = new GradesBaseAdapter<Grade>(context, grades);
        holder.listView.setAdapter(adapter);

        holder.listView.addHeaderView(new View(context));
        holder.listView.addFooterView(new View(context));

        double sum = 0.0;
        int n =0;
        for(Grade g: grades){
            if(getFinalGrade(g)!=-1){
                sum+=getFinalGrade(g);
                n++;
            }
        }

        if(n>0){
            DecimalFormat df = new DecimalFormat("#.00");
            holder.avgText.setText("średnia: ");
            holder.avgText.append(Html.fromHtml("<b>" + df.format(sum/n) + "</b>"));
        }

    }

    public static double getFinalGrade(Grade g){
        double result = -1;

        for(String n: g.getNotes().keySet()){
            try{
                double temp = Double.parseDouble(n.replace(",",".").replace(" ", ""));
                if(temp > result)
                    result=temp;
            }catch (NumberFormatException e){
                if(!n.isEmpty()){
                    if (n.charAt(0) =='X')
                        result = 2.0;
                    else if(n.charAt(0) == 'Z')
                        result = -1;
                }

            }
        }
        return result;
    }
}
