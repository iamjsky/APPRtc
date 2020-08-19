package com.soundleader.apprtctest.usb;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.soundleader.apprtctest.R;

import java.util.ArrayList;
import java.util.List;

public class USBTestActivity extends AppCompatActivity implements MIDIReceiver.ReceiveCallback, MIDISelector.ConnectCallback{

    String TAG = "USB_A_L";

    USBManager _manager;
    List<String> datas;

    ArrayAdapter<String> adapter;

    Handler _handler = new Handler();

    ListView _listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_usb);
        datas = new ArrayList();
        _listView = findViewById(R.id.list);
        adapter = new ArrayAdapter<String>(USBTestActivity.this, android.R.layout.simple_list_item_1, datas);
        _listView.setAdapter(adapter);



        _manager = USBManager.getInstance(USBTestActivity.this,this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _manager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _manager.onPause();
    }

    public void onStart(View v){
        _manager.startConnectProcess();
    }

    public void sendTest(View v){
        _manager.sendNoteOnTest();
    }

    public void onStop(View v){
        _manager.disconnect();
    }

    private void addData(String value){
        datas.add(value);
        if(datas.size() > 100){
            datas.remove(0);
        }
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onDataReceive(final String msg) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
//                String  data = "[USB][DATA] : "+msg;
//                Log.d(TAG,"[USB][DATA] : "+msg);
//                addData(data);
            }
        });
    }

    @Override
    public void onMsgReceive(String msg) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"[USB][MSG] : "+msg);
                String  data = "[USB][MSG] : "+msg;
                addData(data);
            }
        });
    }

    @Override
    public void onConnectResult(int msg) {
        _handler.post(new Runnable() {
            @Override
            public void run() {
//                Log.d(TAG,"[USB][CONN] : "+msg);
//                String  data = "[USB][CONN] : "+msg;
//                addData(data);
            }
        });
    }

    @Override
    public void onReturnedMsg(final String msg) {

    }
}
