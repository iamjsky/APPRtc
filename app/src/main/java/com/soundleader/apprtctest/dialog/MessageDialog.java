package com.soundleader.apprtctest.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.soundleader.apprtctest.R;

import java.util.HashMap;

public class MessageDialog extends Dialog implements View.OnClickListener {

    public static final String EXTRA_CODE_RESULT = "RESULT";

    public static final int CODE_CONFIRM  = 1;
    public static final int CODE_CANCEL  = 2;

    TextView _titleView;
    TextView _msgView;

    String _titleStr;
    String _msgStr;

    Button _confirmBtn;
    Button _cancelBtn;
    View _devider;

    DialogButtonCallback _callback;

    boolean _hasCancel;

    public MessageDialog(@NonNull Context context, DialogButtonCallback callback) {
        super(context);
        _titleStr = "확인";
        _msgStr = "";
        _callback = callback;
        _hasCancel = true;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_message);
        setCancelable(false);

        _titleView = findViewById(R.id.text_title);
        _msgView = findViewById(R.id.text_msg);
        _confirmBtn = findViewById(R.id.btn_confirm);
        _confirmBtn.setOnClickListener(this);
        _cancelBtn = findViewById(R.id.btn_cancel);
        _cancelBtn.setOnClickListener(this);
        _devider = findViewById(R.id.view_devider);
    }



    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();


        setComponent();
    }

    public void show(String title, String msg, boolean hasCancel){
        _titleStr = title;
        _msgStr = msg;
        _hasCancel = hasCancel;
        show();
    }

    @Override
    public void show() {
        super.show();
    }

    private void setComponent(){
        _titleView.setText(_titleStr);
        _msgView.setText(_msgStr);

        if(_hasCancel){
            _devider.setVisibility(View.VISIBLE);
            _cancelBtn.setVisibility(View.VISIBLE);
        }else{
            _devider.setVisibility(View.GONE);
            _cancelBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        HashMap<String, Object> result = new HashMap<>();
        switch (v.getId()){
            case R.id.btn_cancel:
                result.put(EXTRA_CODE_RESULT, CODE_CANCEL);
                break;
            case R.id.btn_confirm:
                result.put(EXTRA_CODE_RESULT, CODE_CONFIRM);
                break;
        }
        if(_callback != null){
            _callback.onDialogCallback(result);
        }
        dismiss();
    }
}
