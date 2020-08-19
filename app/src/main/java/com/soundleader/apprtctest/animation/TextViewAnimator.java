package com.soundleader.apprtctest.animation;

import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

public class TextViewAnimator {

    final String TAG = "TEXTAnim_L::";

    private static final int TYPE_SIZE = 1;

    private static final int PROCESS_LARGER = 1;
    private static final int PROCESS_SMALLER = 0;
    private static final int PROCESS_NONE = -1;

    Handler _handler = new Handler();

    TextView _target;

    float _minSize;
    float _maxSize;

    int _type;
    int _currentProcess;

    float _sizeInterval = 0.5f;

    int _delay = 25;

    private TextViewAnimator(){
        _type = 0;
    }

    public static TextViewAnimator getSizeChanger(float defaultSizeChangeVal, TextView target){
        float defaultVal = target.getTextSize();
        TextViewAnimator animator = new TextViewAnimator();
        animator._minSize = defaultVal - defaultSizeChangeVal;
        animator._maxSize = defaultVal + defaultSizeChangeVal;

        if(animator._minSize < 10){
            animator._maxSize += (10 - animator._minSize);
            animator._minSize = 10;
        }

        animator._target = target;
        animator._type = TYPE_SIZE;
        return animator;
    }


    public void start(){
        if(_type == TYPE_SIZE){
            float currentSize = _target.getTextSize();
//            Log.d(TAG," current size : "+currentSize);
            if(currentSize > _minSize){
                _currentProcess = PROCESS_SMALLER;
            }else if(currentSize < _maxSize){
                _currentProcess = PROCESS_LARGER;
            }else{
                _currentProcess = PROCESS_NONE;
            }

            _handler.postDelayed(sizeChange, _delay);
        }
    }

    Runnable sizeChange = new Runnable() {
        @Override
        public void run() {
            try{
                float currentSize = _target.getTextSize();
//                Log.d(TAG," current size : "+currentSize + " max : "+_maxSize +"  min : "+_minSize);
                if(_currentProcess == PROCESS_LARGER){

                    float temp = currentSize + _sizeInterval;
                    if(temp >= _maxSize){
                        temp = _maxSize;
                        _currentProcess = PROCESS_SMALLER;
                    }
//                    Log.d(TAG, " LARGE set size : "+temp);
                    _target.setTextSize(TypedValue.COMPLEX_UNIT_PX,temp);
                }else if(_currentProcess == PROCESS_SMALLER){
                    float temp = currentSize - _sizeInterval;
                    if(temp <= _minSize){
                        temp = _minSize;
                        _currentProcess = PROCESS_LARGER;
                    }
//                    Log.d(TAG, " SMALL set size : "+temp);
                    _target.setTextSize(TypedValue.COMPLEX_UNIT_PX,temp);
                }

                if(currentSize != PROCESS_NONE){
                    _handler.postDelayed(sizeChange, _delay);
                }
            }catch (Exception e){}
        }
    };




}
