package com.soundleader.apprtctest.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.soundleader.apprtctest.R;

import org.webrtc.SurfaceViewRenderer;

@SuppressLint("ValidFragment")
public class FragmentSmallVideo extends Fragment {

    View _view;
    SurfaceViewRenderer _renderer;

    @SuppressLint("ValidFragment")
    public FragmentSmallVideo(Context context){
        _view = LayoutInflater.from(context).inflate(R.layout.view_video_small, null);
        _renderer = _view.findViewById(R.id.smallscreen_video_view);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return _view;
    }

    public SurfaceViewRenderer getSmallRenderer(){
        return _renderer;
    }
}
