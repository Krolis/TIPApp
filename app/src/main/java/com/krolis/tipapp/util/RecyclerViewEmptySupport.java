package com.krolis.tipapp.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by Krolis on 2016-06-14.
 * Nie wiem jak zachowa się strona, czy jest wgl możliwość braku jakichkolwiek rubryk z ocenami.
 * W przypadku pierwszego uruchomienia i braku internetu lista faktycznie może być pusta.*/


public class RecyclerViewEmptySupport extends RecyclerView {
    private View emptyView;
    private Button btn;

    private AdapterDataObserver emptyObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            Adapter<?> adapter =  getAdapter();
            Log.d("XD", "adapter != null && emptyView != null");
            if(adapter != null && emptyView != null) {
                Log.d("XD", adapter.getItemCount() + " intem count");
                if(adapter.getItemCount() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                    RecyclerViewEmptySupport.this.setVisibility(View.GONE);
                }
                else {
                    emptyView.setVisibility(View.GONE);
                    RecyclerViewEmptySupport.this.setVisibility(View.VISIBLE);
                }
            }

        }
    };

    public RecyclerViewEmptySupport(Context context) {
        super(context);
    }

    public RecyclerViewEmptySupport(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewEmptySupport(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        Log.d("XD","SETADAPTEER");
        if(adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
        }

        emptyObserver.onChanged();
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        btn = (Button) emptyView.findViewById(R.id.grade_rec_view_empty_button);
        Log.d("XD", "SETEMPTYVIEW");
    }

    public void setEmptyText(String text){
        Log.d("XD","SETTEXT");

        if(btn!=null){
            btn.setText(text);
        }
    }


    public void setEmptyOnClickListener(OnClickListener emptyOnClickListener) {
        btn.setOnClickListener(emptyOnClickListener);
        btn.setClickable(false);
    }

    public void setEmptyClickable(boolean isClickable) {
        btn.setClickable(isClickable);
    }
}

