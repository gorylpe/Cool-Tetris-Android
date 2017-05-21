package com.example.piotr.tetris;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.piotr.tetris.custom.GameSurfaceView;
import com.example.piotr.tetris.custom.NextBlockPreviewSurfaceView;

import java.util.*;


public class GameFragment extends Fragment implements Runnable {

    private final long frameTimeInMs = 10;

    private OnStateChangeListener listener;
    private Handler uiHandler;
    private GameSurfaceView gameSurfaceView;
    private NextBlockPreviewSurfaceView nextBlockPreviewSurfaceView;

    private volatile int score;
    private volatile int level;
    private final int maxLevel = 10;

    private boolean notEnded;
    private static final Object pauseLock = new Object();
    private boolean paused;


    private enum NextMoveState {
        GENERATE_BLOCK,
        MOVE_BLOCK
    }

    public static final Object blockMoveLock = new Object();
    private NextMoveState nextMove;

    private final float blockMoveStartDelay = 500.0f;
    private final float blockMoveStopDelay = 100.0f;
    private final float levelConst = (blockMoveStartDelay - blockMoveStopDelay) / 9;
    private final float blockMoveDelayDelta = -0.02f;
    private float blockMoveDelay;
    private float blockMoveTimeAfterLastMove;
    private final float blockMoveAcceleratedDelay = blockMoveStopDelay / 2;
    private boolean isAccelerated;

    private GameBoard board;

    public GameFragment() {

        notEnded = true;
        paused = false;

        board = new GameBoard(getActivity(), 10, 20);

        nextMove = NextMoveState.GENERATE_BLOCK;

        isAccelerated = false;
        blockMoveDelay = blockMoveStartDelay;
        blockMoveTimeAfterLastMove = 0.0f;

        score = 0;
        level = 0;
    }

    public void saveToBundle(Bundle outState){
        outState.putInt("score", score);
        outState.putInt("level", level);
        outState.putFloat("blockMoveDelay", blockMoveDelay);
        outState.putSerializable("nextMove", nextMove);
        board.saveToBundle(outState);
    }

    public void restoreFromBundle(Bundle savedInstanceState) {
        score = savedInstanceState.getInt("score");
        redrawScore();

        level = savedInstanceState.getInt("level");
        redrawLevel();

        blockMoveDelay = savedInstanceState.getFloat("blockMoveDelay");
        nextMove = (NextMoveState)savedInstanceState.getSerializable("nextMove");
        board.restoreFromBundle(savedInstanceState);
        redrawBoard();
        redrawNextBlock();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_game, container, false);
        gameSurfaceView = (GameSurfaceView)view.findViewById(R.id.game_surface_view);
        gameSurfaceView.setBoard(board);
        nextBlockPreviewSurfaceView = (NextBlockPreviewSurfaceView)view.findViewById(R.id.next_block);
        nextBlockPreviewSurfaceView.setBoard(board);
        return view;
    }

    private void onGameEnd(){
        listener.onGameEnd(score);
        end();
    }

    public void end(){
        resume();
        notEnded = false;
    }


    private void redrawBoard(){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if(gameSurfaceView != null)
                    gameSurfaceView.invalidate();
            }
        });
    }

    private void redrawNextBlock(){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                nextBlockPreviewSurfaceView.invalidate();
            }
        });
    }

    private void redrawScore(){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                ((TextView)getView().findViewById(R.id.score)).setText(Integer.toString(score));
            }
        });
    }

    private void redrawLevel(){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                ((TextView)getView().findViewById(R.id.level)).setText(Integer.toString(level));
            }
        });
    }

    @Override
    public void run() {
        while(notEnded){
            long lastTime = System.currentTimeMillis();

            switch(nextMove){
                case GENERATE_BLOCK: {
                    if(board.generateNewBlock()){
                        nextMove = NextMoveState.MOVE_BLOCK;
                    } else {
                        onGameEnd();
                    }
                    break;
                }
                case MOVE_BLOCK: {
                    if(isAccelerated){
                        if (blockMoveTimeAfterLastMove > blockMoveAcceleratedDelay) {
                            blockMoveTimeAfterLastMove = 0.0f;
                            board.moveDownCurrentBlock();
                        }
                    } else {
                        if (blockMoveTimeAfterLastMove > blockMoveDelay) {
                            blockMoveTimeAfterLastMove = 0.0f;
                            board.moveDownCurrentBlock();
                        }
                    }
                    break;
                }
            }

            redrawBoard();

            blockMoveTimeAfterLastMove += (float)frameTimeInMs;
            blockMoveDelay += blockMoveDelayDelta;
            if(blockMoveDelay <= blockMoveStopDelay)
                blockMoveDelay = blockMoveStopDelay;

            int newLevel = maxLevel + 1 - (int)(blockMoveDelay / levelConst);
            if(newLevel != level)
                setLevel(newLevel);

            try{
                Thread.sleep(frameTimeInMs - (System.currentTimeMillis() - lastTime));
            } catch(InterruptedException | IllegalArgumentException e){}

            synchronized (pauseLock){
                while(paused){
                    try{
                        pauseLock.wait();
                    } catch(InterruptedException e){}
                }
            }
        }
    }

    public void pause(){
        synchronized (pauseLock){
            paused = true;
        }
    }

    public void resume(){
        synchronized (pauseLock){
            paused = false;
            blockMoveTimeAfterLastMove = 0.0f;
            pauseLock.notifyAll();
        }
    }

    private void addScore(int scoreChange){
        switch(scoreChange){
            case 1:
                scoreChange = (level + 1) * 40;
                break;
            case 2:
                scoreChange = (level + 1) * 100;
                break;
            case 3:
                scoreChange = (level + 1) * 300;
                break;
            case 4:
                scoreChange = (level + 1) * 1200;
                break;
        }
        score += scoreChange;
        redrawScore();
    }

    private void setLevel(int newLevel){
        level = newLevel;
        redrawLevel();
    }

    public void rotate() {
        board.rotateLeftCurrentBlock();
    }

    public void moveLeft() {
        board.moveLeftCurrentBlock();
    }

    public void moveRight() {
        board.moveRightCurrentBlock();
    }

    public void setAccelerated(){
        isAccelerated = true;
    }

    public void unsetAccelerated(){
        isAccelerated = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mutualOnAttach(context);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mutualOnAttach(activity);
    }

    private void mutualOnAttach(Context context){
        uiHandler = new Handler(context.getMainLooper());
        if (context instanceof OnStateChangeListener) {
            listener = (OnStateChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnStateChangeListener {
        void onGameEnd(int score);
    }
}
