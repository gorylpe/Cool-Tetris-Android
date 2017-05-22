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

public class GameActivity extends AppCompatActivity implements  GameFragment.OnStateChangeListener, GameControlsFragment.OnBlockControlListener {

    enum GameState{
        NOT_STARTED,
        PLAYING,
        PAUSED
    }

    private GameState gameState;
    private GameFragment gameFragment;
    private Thread gameThread;

    private GameBoard gameBoard;

    private AlertDialog dialog;

    private Timer moveLeftTimer;
    private Timer moveRightTimer;
    private final long moveDelay = 100;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Log.d("oncreate", "d");

        //must be before inflating xml

        moveLeftTimer = new Timer();
        moveRightTimer = new Timer();

        setContentView(R.layout.activity_game);

        gameBoard = new GameBoard(this, 10, 20);

        gameFragment = (GameFragment)getFragmentManager().findFragmentById(R.id.fragment_game);
        gameFragment.setBoard(gameBoard);
        if(savedInstanceState != null) {
            onPause();
            gameBoard.restoreFromBundle(savedInstanceState);
            gameFragment.restoreFromBundle(savedInstanceState);
        }else{
            this.newGame();
        }
    }

    public void newGame(){
        gameState = GameState.NOT_STARTED;
        gameFragment.pause();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        gameFragment.saveToBundle(bundle);
        gameBoard.saveToBundle(bundle);
    }

    @Override
    public void onPause() {
        super.onPause();
        gameState = GameState.PAUSED;
        gameFragment.pause();
        gameFragment.end();
        try {
            gameThread.join();
        }catch (InterruptedException e){}
    }

    @Override
    public void onResume(){
        super.onResume();
        gameThread = new Thread(gameFragment);
        gameThread.start();
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
                if(gameBoard != null)
                    gameBoard.moveLeftCurrentBlock();
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
                if(gameBoard != null)
                    gameBoard.moveRightCurrentBlock();
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
        if(gameBoard != null)
            gameBoard.rotateLeftCurrentBlock();
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
