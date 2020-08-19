package com.soundleader.apprtctest.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.soundleader.apprtctest.R;

public class ProgressDialog extends Dialog {

    Handler _handler = new Handler();

    Runnable removeProcess = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };

    public ProgressDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_progress);
        setCancelable(false);
    }


    public ProgressDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        setContentView(R.layout.dialog_progress);
    }


    public void show(int delay) {
        show();
        _handler.postDelayed(removeProcess, delay);
    }

    @Override
    public void onBackPressed() {

    }
}
