package com.soundleader.apprtctest.dialog;


import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.midi.MidiDeviceInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.soundleader.apprtctest.R;
import com.soundleader.apprtctest.usb.MidiInfo;


import java.util.ArrayList;

public class DialogList extends Dialog {

    Context mContext;

    ListView mListView;
    ArrayList<MidiInfo> mDeviceList;
    ListAdapter mListAdapter;

    AdapterView.OnItemClickListener mCallback;

    public DialogList(Context context) {
        super(context);
        mContext = context;
        mDeviceList = new ArrayList<>();
    }

    public DialogList(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        mDeviceList = new ArrayList<>();
    }

    protected DialogList(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
        mDeviceList = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_selecotr);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mListView = findViewById(R.id.list);
        mListAdapter = new ListAdapter();
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mDeviceList.size() != 0) {
                    mCallback.onItemClick(adapterView, view, i, l);
                }
            }
        });
        findViewById(R.id.cancel).setOnClickListener(onCancel);
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Point pt = new Point();
        getWindow().getWindowManager().getDefaultDisplay().getSize(pt);

        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(pt);

        int height = pt.y;
        ViewGroup.LayoutParams param = mListView.getLayoutParams();
        param.height = height / 2;
        mListView.setLayoutParams(param);

    }
    /**
     * public methdos
     */
    // data set
    public void clear(){
        mDeviceList.clear();

    }
    public void setData(ArrayList<MidiInfo> datas) {
        mDeviceList = datas;
        if (mListAdapter != null && mListView != null){
            mListView.post(new Runnable() {
                @Override
                public void run() {
                    mListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener callback) {
        mCallback = callback;
    }

    public MidiInfo getMidiDevice(int pos) {
        return mDeviceList.get(pos);
    }

    public void showDialog() {
        show();
    }


    /**
     * Events
     */
    View.OnClickListener onCancel = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dismiss();
        }
    };


    /**
     * List Adapter
     */
    class ListAdapter extends BaseAdapter {
        @Override
        public int getCount() {

            if (mDeviceList.size() != 0)
                return mDeviceList.size();

            return 1;
        }

        @Override
        public Object getItem(int i) {
            return mDeviceList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_selector, null);
            if (mDeviceList.size() == 0) {
                ((TextView) view.findViewById(R.id.title)).setText("연결된 디바이스가 없습니다.");
            } else {
                MidiInfo data = (MidiInfo) getItem(i);
                ((TextView) view.findViewById(R.id.title)).setText(data.getInfo().getProperties().getString(MidiDeviceInfo.PROPERTY_NAME));
            }
            return view;
        }
    }
}
