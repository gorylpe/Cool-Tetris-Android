package com.example.piotr.tetris;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity implements  GameFragment.OnStateChangeListener,
                                                                GameControlsFragment.OnBlockControlListener,
                                                                PaintsContainer

{

    private Paint[] paints;
    private Paint backgroundPaint;
    private Paint borderPaint;

    enum GameState{
        NOT_STARTED,
        PLAYING,
        PAUSED
    }

    private GameState gameState;
    private GameFragment gameFragment;
    private Thread gameThread;

    private AlertDialog dialog;

    private Timer moveLeftTimer;
    private Timer moveRightTimer;
    private final long moveDelay = 100;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        paints = new Paint[8];
        for(int i = 0; i < paints.length; ++i){
            paints[i] = new Paint();
        }
        paints[0].setColor(ContextCompat.getColor(this, R.color.cyan));
        paints[1].setColor(ContextCompat.getColor(this, R.color.blue));
        paints[2].setColor(ContextCompat.getColor(this, R.color.orange));
        paints[3].setColor(ContextCompat.getColor(this, R.color.yellow));
        paints[4].setColor(ContextCompat.getColor(this, R.color.green));
        paints[5].setColor(ContextCompat.getColor(this, R.color.purple));
        paints[6].setColor(ContextCompat.getColor(this, R.color.red));
        paints[7].setColor(ContextCompat.getColor(this, R.color.background));

        backgroundPaint = new Paint();
        backgroundPaint.setColor(ContextCompat.getColor(this, R.color.background));

        borderPaint = new Paint();
        borderPaint.setColor(ContextCompat.getColor(this, R.color.border));
        //must be before inflating xml

        moveLeftTimer = new Timer();
        moveRightTimer = new Timer();

        setContentView(R.layout.activity_game);

        gameFragment = (GameFragment)getFragmentManager().findFragmentById(R.id.fragment_game);
        if(savedInstanceState != null) {
            onPause();
            gameFragment.restoreFromBundle(savedInstanceState);
        }else{
            this.newGame();
        }
        gameThread = new Thread(gameFragment);
        gameThread.start();
    }

    public void newGame(){
        gameState = GameState.NOT_STARTED;
        gameFragment.pause();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        gameFragment.end();
        try {
            gameThread.join();
        }catch (InterruptedException e){}
        gameFragment.saveToBundle(bundle);
    }

    @Override
    public void onPause() {
        super.onPause();
        gameState = GameState.PAUSED;
        gameFragment.pause();
    }

    @Override
    public void onResume(){
        super.onResume();
        switch(gameState){
            case PAUSED:
            {
                if(dialog != null)
                    dialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle(R.string.paused_dialog);
                builder.setMessage(R.string.paused_dialog_desc);

                final GameFragment gameFragment = (GameFragment)getFragmentManager().findFragmentById(R.id.fragment_game);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        gameFragment.resume();
                        gameState = GameState.PLAYING;
                        dialog.dismiss();
                    }
                });

                dialog = builder.create();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                break;
            }
            case NOT_STARTED:
            {
                if(dialog != null)
                    dialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle(R.string.not_started_dialog);
                builder.setMessage(R.string.not_started_dialog_desc);

                final GameFragment gameFragment = (GameFragment)getFragmentManager().findFragmentById(R.id.fragment_game);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        gameFragment.resume();
                        gameState = GameState.PLAYING;
                        dialog.dismiss();
                    }
                });

                dialog = builder.create();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                break;
            }
        }
    }

    @Override
    public Paint[] getBlockPaints() {
        return paints;
    }

    @Override
    public Paint getBackgroundPaint() {
        return backgroundPaint;
    }

    @Override
    public Paint getBorderPaint(){
        return borderPaint;
    }

    @Override
    public void onGameEnd(int score) {
        Intent intent = new Intent();
        intent.putExtra("score", score);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onPressLeft() {
        moveLeftTimer = new Timer();
        moveLeftTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(gameFragment != null)
                    gameFragment.moveLeft();
            }
        }, 0, moveDelay);
    }

    @Override
    public void onReleaseLeft(){
        moveLeftTimer.cancel();
        moveLeftTimer.purge();
    }

    @Override
    public void onPressRight() {
        moveRightTimer = new Timer();
        moveRightTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(gameFragment != null)
                    gameFragment.moveRight();
            }
        }, 0, moveDelay);
    }

    @Override
    public void onReleaseRight(){
        moveRightTimer.cancel();
        moveRightTimer.purge();
    }

    @Override
    public void onPressRotate() {
        if(gameFragment != null)
            gameFragment.rotate();
    }

    @Override
    public void onPressAccelerate() {
        if(gameFragment != null)
            gameFragment.setAccelerated();
    }

    @Override
    public void onReleaseAccelerate() {
        if(gameFragment != null)
            gameFragment.unsetAccelerated();
    }
}
