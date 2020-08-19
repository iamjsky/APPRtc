package com.soundleader.apprtctest.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.soundleader.apprtctest.R;
import com.soundleader.apprtctest.beans.RoomData;
import com.soundleader.apprtctest.utils.ImageLoader;

import java.util.List;

import info.androidhive.fontawesome.FontDrawable;

public class AdapterRoomList extends RecyclerView.Adapter<AdapterRoomList.CHolder> {
    final String TAG = "ADAPTER_R_L_::";
    private List<RoomData> mDataset;

    static AdapterView.OnItemClickListener _callback;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class CHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView titleView;
        public TextView idView;
        public TextView useridView;
        public ImageView profileView;
        public ImageView indicator;
        public CHolder(View v) {
            super(v);
            indicator = v.findViewById(R.id.view_indicator);
            titleView = v.findViewById(R.id.text_title);
            useridView = v.findViewById(R.id.text_userid);
            useridView .setClipToOutline(true);
            profileView = v.findViewById(R.id.img_profile);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition() ;
                    if (pos != RecyclerView.NO_POSITION && _callback != null) {
                        _callback.onItemClick(null, v, pos, getItemId());
                    }
                }
            });
        }
    }

    public Context _context;



    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterRoomList(Context context,List<RoomData> myDataset, AdapterView.OnItemClickListener callback) {
        _context = context;
        mDataset = myDataset;
        _callback = callback;
    }

    public void setData(List<RoomData> myDataset){
        mDataset = myDataset;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AdapterRoomList.CHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        // create a new view
        Log.d(TAG, "onCreateViewHolder ");
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_room_list, parent, false);
        CHolder vh = new CHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(CHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder pos : " + position);
        RoomData data = mDataset.get(position);
        holder.titleView.setText(data.getTitle());
        holder.useridView.setText("강사 : "+data.getUserId());
        int color ;
        FontDrawable drawable;
        if(data.getStatus() == 0){
            drawable  = new FontDrawable(_context, R.string.fa_check_circle, true, false);
            drawable.setTextColor(ContextCompat.getColor(_context, R.color.primary2));
        }else{
            drawable  = new FontDrawable(_context, R.string.fa_times_circle, true, false);
            drawable.setTextColor(ContextCompat.getColor(_context, R.color.colorOther2));
        }
        holder.indicator.setImageDrawable(drawable);
        new ImageLoader( ).execute( holder.profileView, data.getProfileImg());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
