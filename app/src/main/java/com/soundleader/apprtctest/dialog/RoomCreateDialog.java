package com.soundleader.apprtctest.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.soundleader.apprtctest.R;

import java.util.HashMap;

public class RoomCreateDialog extends Dialog {
    public static String EXTRA_CODE = "CODE";
    public static String EXTRA_NAME = "NAME";

    public static final int DIALOG_RESULT_CODE_CONFIRM = 1;
    public static final int DIALOG_RESULT_CODE_CANCEL = 0;

    private HashMap<String, Object> _result;
    private DialogButtonCallback _callback;

    private Button _confirmView;
    private Button _cancelView;

    private EditText _editView;

    Context _context;

    public RoomCreateDialog(@NonNull Context context) {
        super(context);
        _context = context;
        setContentView(R.layout.dialog_room_create);
        _confirmView = findViewById(R.id.dialog_confirm);
        _confirmView.setOnClickListener(onConfirm);
        _cancelView = findViewById(R.id.dialog_cancel);
        _cancelView.setOnClickListener(onCancel);
        _editView = findViewById(R.id.dialog_room_name);
        _result = new HashMap<>();
    }

    public void setCallback(DialogButtonCallback callback){
        _callback = callback;
    }

    View.OnClickListener onConfirm = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String name = _editView.getText().toString();
            if(name.length() < 2){
                Toast.makeText(_context, "방 제목을 2자 이상 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            _result.put(EXTRA_CODE, DIALOG_RESULT_CODE_CONFIRM);
            _result.put(EXTRA_NAME, name);
            dismiss();
            call();
        }
    };

    View.OnClickListener onCancel = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            _result.put(EXTRA_CODE, DIALOG_RESULT_CODE_CANCEL);
            dismiss();
            call();
        }
    };


    private void call(){
        if(_callback != null){
            _callback.onDialogCallback(_result);
        }
    }


}
