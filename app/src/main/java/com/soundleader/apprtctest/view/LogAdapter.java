package com.soundleader.apprtctest.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.soundleader.apprtctest.R;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.Holder> {

    List<String> _data;

    public LogAdapter(List<String> data){
        _data = data;
    }

    public void setData(List<String> data){
        _data = data;
        notifyDataSetChanged();

    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_log_list_item, parent, false);
        Holder vh = new Holder(v);
        return vh;


    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int i) {
        holder.logView.setText(_data.get(i));
    }

    @Override
    public int getItemCount() {
        return _data.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView logView;

        public Holder(View v) {
            super(v);
            logView = v.findViewById(R.id.text_log);
        }
    }


}
