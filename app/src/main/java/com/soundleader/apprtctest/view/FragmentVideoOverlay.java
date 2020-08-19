package com.soundleader.apprtctest.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.soundleader.apprtctest.R;

import info.androidhive.fontawesome.FontDrawable;

public class FragmentVideoOverlay extends Fragment {

    ImageButton _disconnect;
    View _fragmentView;
    ProgressBar _progress;
    TextView _titleTxt;
    TextView _teacherTxt;
    TextView _otherTxt;
//    Button _fullBtn;
    ImageButton _smallBtn;
    ImageButton _micBtn;
    ImageButton _connectBtn;
    ImageButton btn_camera_reverse_lr, btn_camera_reverse_ud;

    LinearLayout _containerSmall;
    LinearLayout _containerMic;
    LinearLayout _containerConnect;

    ImageView _viewIndicator;

    int currentVisible = View.GONE;

    View.OnClickListener onCancelClicked;

    Handler _handler = new Handler();

    Runnable _visibleTimeout = new Runnable() {
        @Override
        public void run() {
            toggleFragmentWithAnim();
        }
    };

    String _title = "";
    String _teacher = "";
    String _other = "";

    boolean isProgress = false;
    boolean isUSBConnected = false;
    int controllerVisible = View.GONE;
    View.OnClickListener fullControllCallback;
    View.OnClickListener smallControllCallback;
    View.OnClickListener micControllCallback;
    View.OnClickListener usbControllCallback;
    View.OnClickListener cameraLRControllCallback;
    View.OnClickListener cameraUDControllCallback;

    public FragmentVideoOverlay(){

    }

    @SuppressLint("ValidFragment")
    public FragmentVideoOverlay(View.OnClickListener onCancelClicked){
        this.onCancelClicked = onCancelClicked;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_video_overlay, container, false);

        _disconnect = view.findViewById(R.id.btn_disconnect);
        _disconnect.setOnClickListener(onCancelClicked);
        _progress = view.findViewById(R.id.view_progress);
        _viewIndicator = view.findViewById(R.id.view_indicator);
        _fragmentView = view;
        _fragmentView.setVisibility(currentVisible);

        _titleTxt = view.findViewById(R.id.text_title);
        _teacherTxt = view.findViewById(R.id.text_teacher);
        _otherTxt = view.findViewById(R.id.text_other);
        _smallBtn = view.findViewById(R.id.btn_small_control);
        _micBtn = view.findViewById(R.id.btn_mic_control);
        _connectBtn = view.findViewById(R.id.btn_usb_control);
        _containerSmall = view.findViewById(R.id.container_remote_view);
        _containerSmall.setVisibility(controllerVisible);
        _containerMic = view.findViewById(R.id.container_mic_view);
        _containerMic.setVisibility(controllerVisible);
        _containerConnect = view.findViewById(R.id.container_usb_view);
        btn_camera_reverse_lr = view.findViewById(R.id.btn_camera_reverse_lr);
        btn_camera_reverse_ud = view.findViewById(R.id.btn_camera_reverse_ud);

        _smallBtn.setOnClickListener(smallControllCallback);
        _micBtn.setOnClickListener(micControllCallback);
        _connectBtn.setOnClickListener(usbControllCallback);
        btn_camera_reverse_lr.setOnClickListener(cameraLRControllCallback);
        btn_camera_reverse_ud.setOnClickListener(cameraUDControllCallback);

        _titleTxt.setText("Title : "+_title);
        _teacherTxt.setText("강사 : "+_teacher);
        _otherTxt.setText(_other);

        if(isProgress)
            _progress.setVisibility(View.VISIBLE);
        else
            _progress.setVisibility(View.GONE);
        return view;
    }

    public void setInfos(String title, String teacher){
        _title = title;
        _teacher = teacher;
        if(_titleTxt != null){
            _titleTxt.setText("Title : "+_title);
            _teacherTxt.setText("강사 : "+_teacher);
        }
    }

    public void setMicEnabled(boolean val){
        if(val){
            _micBtn.setImageResource(R.drawable.icon_block_mic);
        }else{
            _micBtn.setImageResource(R.drawable.icon_enable_mic);
        }
    }
    public void setCameraLRControllListener(View.OnClickListener callback){
        cameraLRControllCallback = callback;
        if(btn_camera_reverse_lr != null){
            btn_camera_reverse_lr.setOnClickListener(cameraLRControllCallback);
        }
    }
    public void setCameraUDControllListener(View.OnClickListener callback){
        cameraUDControllCallback = callback;
        if(btn_camera_reverse_ud != null){
            btn_camera_reverse_ud.setOnClickListener(cameraUDControllCallback);
        }
    }
    public void setMicControllListener(View.OnClickListener callback){
        micControllCallback = callback;
        if(_micBtn != null){
            _micBtn.setOnClickListener(smallControllCallback);
        }
    }

    public void setUsbControllListener(View.OnClickListener callback){
        usbControllCallback = callback;
        if(_connectBtn != null)
            _connectBtn.setOnClickListener(usbControllCallback);
    }

    public void setOther(String other){
        _other = other;
        if(_otherTxt != null){
            _otherTxt.setText(_other);
        }
    }

    public void setUpdatime(long uptime){
        if(_titleTxt != null){
            _titleTxt.setText("Title : "+_title + " "+uptime);
//            _otherTxt.setText(_other);
        }
    }

    public void setSmallControllerView(int value){

        controllerVisible = value;
        if(_containerSmall != null)
            _containerSmall.setVisibility(controllerVisible);
        if(_containerMic != null)
            _containerMic.setVisibility(controllerVisible);
    }

    public int getSmallControllerVisiblity(){
        return _containerSmall.getVisibility();
    }

    public void setSmallControllListener(View.OnClickListener callback){
        smallControllCallback = callback;
        if(_smallBtn != null){
            _smallBtn.setOnClickListener(smallControllCallback);
        }
    }

    public void showProgress(){
        if(_fragmentView != null){
            _progress.post(new Runnable() {
                @Override
                public void run() {
                    _progress.setVisibility(View.VISIBLE);
                    _disconnect.setVisibility(View.GONE);
                    Animation animation = new AlphaAnimation(0, 1);
                    animation.setDuration(300);
                    _fragmentView.setVisibility(View.VISIBLE);
                    _fragmentView.setAnimation(animation);
                }
            });
        }
        isProgress = true;
    }

    public void hideProgress(){
        _progress.post(new Runnable() {
            @Override
            public void run() {
                Animation animation = new AlphaAnimation(0, 1);
                animation.setDuration(300);
                _fragmentView.setVisibility(View.VISIBLE);
                _fragmentView.setAnimation(animation);
                _progress.setVisibility(View.GONE);
            }
        });
        isProgress = false;
    }

    public void showWithAnim(){
        if(_fragmentView != null){
            //            _progress.setVisibility(View.GONE);
            _disconnect.setVisibility(View.VISIBLE);
            Animation animation = new AlphaAnimation(0, 1);
            animation.setDuration(500);
            _fragmentView.setVisibility(View.VISIBLE);
            _fragmentView.setAnimation(animation);
        }
        currentVisible = View.VISIBLE;
        _handler.postDelayed(_visibleTimeout, 8000);
    }

    public void hideWithAnim(){
        if(isProgress){
            return;
        }

        _handler.removeCallbacks(_visibleTimeout);
        if(_fragmentView != null){
            Animation animation = new AlphaAnimation(1, 0);
            animation.setDuration(500);
            _fragmentView.setVisibility(View.GONE);
            _fragmentView.setAnimation(animation);
        }
        currentVisible = View.GONE;
    }

    public void toggleFragmentWithAnim(){
        if(currentVisible == View.VISIBLE){
            hideWithAnim();
        }else{
            showWithAnim();
        }
    }

    public void showFragment(){
        if(_fragmentView != null){
            _fragmentView.setVisibility(View.VISIBLE);
        }
        currentVisible = View.VISIBLE;
    }

    public void hideFragment(){
        if(_fragmentView != null){
            _fragmentView.setVisibility(View.GONE);
        }
        currentVisible = View.GONE;
    }



    public void changeIndicator(boolean isConnected){
        FontDrawable drawable;
        isUSBConnected = isConnected;
        if(isConnected){
            drawable  = new FontDrawable(getContext(), R.string.fa_check_circle, true, false);
            drawable.setTextColor(ContextCompat.getColor(getContext(), R.color.primary2));
            _containerConnect.setVisibility(View.GONE);
        }else{
            drawable  = new FontDrawable(getContext(), R.string.fa_times_circle, true, false);
            drawable.setTextColor(ContextCompat.getColor(getContext(), R.color.colorOther2));
            _containerConnect.setVisibility(View.VISIBLE);
        }
        _viewIndicator.setImageDrawable(drawable);

    }


}
