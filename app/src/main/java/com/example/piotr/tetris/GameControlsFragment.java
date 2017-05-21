package com.example.piotr.tetris;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class GameControlsFragment extends Fragment {

    private OnBlockControlListener listener;

    public GameControlsFragment(){

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_game_controls, container, false);

        Button buttonLeft = (Button)view.findViewById(R.id.buttonLeft);
        Button buttonRight = (Button)view.findViewById(R.id.buttonRight);
        Button buttonA = (Button)view.findViewById(R.id.buttonA);
        Button buttonB = (Button)view.findViewById(R.id.buttonB);

        buttonLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN){
                    listener.onPressLeft();
                } else if(motionEvent.getActionMasked() == MotionEvent.ACTION_UP){
                    listener.onReleaseLeft();
                }
                return true;
            }
        });

        buttonRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN){
                    listener.onPressRight();
                } else if(motionEvent.getActionMasked() == MotionEvent.ACTION_UP){
                    listener.onReleaseRight();
                }
                return true;
            }
        });

        buttonA.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN){
                    listener.onPressRotate();
                }
                return true;
            }
        });

        buttonB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN){
                    listener.onPressAccelerate();
                } else if(motionEvent.getActionMasked() == MotionEvent.ACTION_UP){
                    listener.onReleaseAccelerate();
                }
                return true;
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mutualOnAttach(context);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mutualOnAttach(activity);
    }

    private void mutualOnAttach(Context context) {
        if (context instanceof OnBlockControlListener) {
            listener = (OnBlockControlListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBlockControlListener");
        }
    }

    @Override
    public void onDetach(){
        super.onDetach();
        listener = null;
    }

    public interface OnBlockControlListener{
        void onPressLeft();
        void onReleaseLeft();
        void onPressRight();
        void onReleaseRight();
        void onPressRotate();
        void onPressAccelerate();
        void onReleaseAccelerate();
    }
}
