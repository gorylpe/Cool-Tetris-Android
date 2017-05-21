package com.example.piotr.tetris;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.piotr.tetris.custom.GameSurfaceView;
import com.example.piotr.tetris.custom.NextBlockPreviewSurfaceView;

import java.util.*;


public class GameFragment extends Fragment implements Runnable {

    private Random random;

    private final long frameTimeInMs = 10;

    private volatile int score;
    private volatile int level;

    private boolean notEnded;
    private static final Object pauseLock = new Object();
    private boolean paused;

    private OnStateChangeListener listener;
    private PaintsContainer paintsContainer;
    private Handler uiHandler;
    private GameSurfaceView gameSurfaceView;
    private NextBlockPreviewSurfaceView nextBlockPreviewSurfaceView;

    private enum BlockMoveState {
        GENERATE_BLOCK,
        MOVE_BLOCK
    }

    public static final Object blockMoveLock = new Object();
    private boolean canBlockMakeMove;
    private BlockMoveState nextMove;
    private final float blockMoveStartDelay = 500.0f;
    private final float blockMoveStopDelay = 100.0f;
    private final float levelConst = (blockMoveStartDelay - blockMoveStopDelay) / 9;
    private final float blockMoveDelayDelta = -0.02f;
    private float blockMoveDelay;
    private float blockMoveTimeAfterLastMove;
    private final float blockMoveAcceleratedDelay = blockMoveStopDelay / 2;
    private boolean isAccelerated;


    private int currentBlock[][];
    private int currentBlockPosition[];
    private int currentBlockRotation;
    private int currentBlockNumber[];
    private int nextBlock[][];
    private final int nextBlockPosition[] = {4,1};
    private final int nextBlockRotation = 0;
    private int nextBlockNumber[];

    public GameFragment() {
        random = new Random();

        notEnded = true;
        paused = false;

        currentBlock = new int[4][2];
        for(int i = 0; i < currentBlock.length; ++i){
            for(int j = 0; j < currentBlock[i].length; ++j){
                currentBlock[i][j] = -1;
            }
        }
        currentBlockNumber = new int[1];
        currentBlockPosition = new int[2];

        nextBlock = new int[4][2];
        nextBlockNumber = new int[1];
        nextBlockNumber[0] = random.nextInt(blocks.length);
        for(int i = 0; i < nextBlock.length; ++i){
            for(int j = 0; j < nextBlock[i].length; ++j){
                nextBlock[i][j] = blocks[nextBlockNumber[0]][0][i][j];
            }
        }

        nextMove = BlockMoveState.GENERATE_BLOCK;
        canBlockMakeMove = true;

        isAccelerated = false;
        blockMoveDelay = blockMoveStartDelay;
        blockMoveTimeAfterLastMove = 0.0f;

        score = 0;
        level = 11 - (int)(blockMoveStartDelay / levelConst);
    }

    public void saveToBundle(Bundle outState){
        outState.putInt("score", score);
        outState.putInt("level", level);
        outState.putFloat("blockMoveDelay", blockMoveDelay);
        outState.putSerializable("nextMove", nextMove);
        outState.putSerializable("currentBlock", currentBlock);
        outState.putSerializable("currentBlockNumber", currentBlockNumber);
        Log.d("level save", Integer.toString(currentBlockNumber[0]));
        outState.putSerializable("currentBlockPosition", currentBlockPosition);
        outState.putInt("currentBlockOrientation", currentBlockRotation);
        outState.putSerializable("nextBlock", nextBlock);
        outState.putSerializable("nextBlockNumber", nextBlockNumber);
    }

    public void restoreFromBundle(Bundle savedInstanceState) {
        score = savedInstanceState.getInt("score");
        redrawScore();
        level = savedInstanceState.getInt("level");
        redrawLevel();
        blockMoveDelay = savedInstanceState.getFloat("blockMoveDelay");
        nextMove = (BlockMoveState)savedInstanceState.getSerializable("nextMove");

        int[][] newCurrentBlock = (int[][])savedInstanceState.getSerializable("currentBlock");
        for(int i = 0; i < currentBlock.length; ++i){
            for(int j = 0; j < currentBlock[i].length; ++j)
                currentBlock[i][j] = newCurrentBlock[i][j];
        }
        currentBlockNumber[0] = ((int[])savedInstanceState.getSerializable("currentBlockNumber"))[0];
        Log.d("level restore", Integer.toString(currentBlockNumber[0]));
        currentBlockRotation = savedInstanceState.getInt("currentBlockRotation");
        int[] newCurrentBlockPosition = (int[])savedInstanceState.getSerializable("currentBlockPosition");
        for(int i = 0; i < currentBlockPosition.length; ++i){
            currentBlockPosition[i] = newCurrentBlockPosition[i];
        }
        redrawFields();
        int[][] newNextBlock = (int[][])savedInstanceState.getSerializable("nextBlock");
        for(int i = 0; i < nextBlock.length; ++i){
            for(int j = 0; j < nextBlock[i].length; ++j)
                nextBlock[i][j] = newNextBlock[i][j];
        }
        nextBlockNumber[0] = ((int[])savedInstanceState.getSerializable("nextBlockNumber"))[0];
        redrawNextBlock();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_game, container, false);
        gameSurfaceView = (GameSurfaceView)view.findViewById(R.id.game_surface_view);
        gameSurfaceView.setFieldsCurrentBlockAndPaints(fields, currentBlock, currentBlockPosition, currentBlockNumber, paintsContainer);
        nextBlockPreviewSurfaceView = (NextBlockPreviewSurfaceView)view.findViewById(R.id.next_block);
        nextBlockPreviewSurfaceView.setNextBlockAndPaints(nextBlock, nextBlockNumber, paintsContainer);
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


    private void redrawFields(){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if(gameSurfaceView != null)
                    gameSurfaceView.invalidate();
            }
        });
    }

    @Override
    public void run() {
        while(notEnded){
            long lastTime = System.currentTimeMillis();

            switch(nextMove){
                case GENERATE_BLOCK: {
                    int blockNumber = random.nextInt(blocks.length);
                    if(checkIfThereIsPlaceForNewBlock(blockNumber)){
                        putNewBlock(blockNumber);
                        nextMove = BlockMoveState.MOVE_BLOCK;
                    } else {
                        onGameEnd();
                    }
                    break;
                }
                case MOVE_BLOCK: {
                    synchronized (blockMoveLock){
                        if(isAccelerated){
                            if (blockMoveTimeAfterLastMove > blockMoveAcceleratedDelay) {
                                blockMoveTimeAfterLastMove = 0.0f;
                                moveBlockDown();
                            }
                        } else {
                            if (blockMoveTimeAfterLastMove > blockMoveDelay) {
                                blockMoveTimeAfterLastMove = 0.0f;
                                moveBlockDown();
                            }
                        }
                    }
                    break;
                }
            }

            redrawFields();

            blockMoveTimeAfterLastMove += (float)frameTimeInMs;
            blockMoveDelay += blockMoveDelayDelta;
            if(blockMoveDelay <= blockMoveStopDelay)
                blockMoveDelay = blockMoveStopDelay;

            int newLevel = 11 - (int)(blockMoveDelay / levelConst);
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

    private boolean checkIfThereIsPlaceForNewBlock(int blockNumber){
        boolean isFree = true;
        for(int i = 0; i < blocks[blockNumber][0].length; ++i){
            int x = blocks[blockNumber][0][i][0] + nextBlockPosition[0];
            int y = blocks[blockNumber][0][i][1] + nextBlockPosition[1];
            try{
                if(fields[x][y] != Field.EMPTY){
                    isFree = false;
                    break;
                }
            } catch(ArrayIndexOutOfBoundsException e){}
        }
        return isFree;
    }

    private void redrawNextBlock(){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                nextBlockPreviewSurfaceView.invalidate();
            }
        });
    }

    private void putNewBlock(int newBlockNumber){
        synchronized (blockMoveLock) {
            currentBlockNumber[0] = nextBlockNumber[0];
            currentBlockRotation = 0;
            currentBlockPosition[0] = nextBlockPosition[0];
            currentBlockPosition[1] = nextBlockPosition[1];
            for (int i = 0; i < nextBlock.length; ++i) {
                currentBlock[i][0] = nextBlock[i][0];
                currentBlock[i][1] = nextBlock[i][1];
            }
            nextBlockNumber[0] = newBlockNumber;
            for (int i = 0; i < blocks[newBlockNumber][nextBlockRotation].length; ++i) {
                nextBlock[i][0] = blocks[newBlockNumber][nextBlockRotation][i][0];
                nextBlock[i][1] = blocks[newBlockNumber][nextBlockRotation][i][1];
            }
            redrawNextBlock();
        }
    }

    private void moveBlockDown(){
        synchronized (blockMoveLock) {
            boolean isFree = true;
            for (int i = 0; i < currentBlock.length; ++i) {
                int x = currentBlock[i][0] + currentBlockPosition[0];
                int y = currentBlock[i][1] + currentBlockPosition[1];
                if (y >= fields[0].length - 1) {
                    isFree = false;
                    break;
                } else {
                    if (fields[x][y + 1] != Field.EMPTY) {
                        isFree = false;
                        break;
                    }
                }
            }
            if(isFree){
                ++currentBlockPosition[1];
            } else {
                nextMove = BlockMoveState.GENERATE_BLOCK;
                placeBlock();
                checkLinesToRemove();
                removeCurrentBlock();
            }
        }
    }

    private void placeBlock() {
        for (int i = 0; i < currentBlock.length; ++i) {
            int x = currentBlock[i][0] + currentBlockPosition[0];
            int y = currentBlock[i][1] + currentBlockPosition[1];
            fields[x][y] = Field.getFromValue(currentBlockNumber[0]);
        }
    }

    private void checkLinesToRemove(){
        ArrayList<Integer> linesToCheck = new ArrayList<Integer>();
        linesToCheck.ensureCapacity(blocks[0].length);
        for (int i = 0; i < currentBlock.length; ++i){
            int y = currentBlock[i][1] + currentBlockPosition[1];
            boolean contains = false;
            for(int j = 0; j < linesToCheck.size(); ++j){
                if(linesToCheck.get(j).equals(y)){
                    contains = true;
                }
            }
            if(!contains)
                linesToCheck.add(y);
        }

        Collections.sort(linesToCheck);

        ArrayList<Integer> linesToDelete = new ArrayList<Integer>();
        linesToCheck.ensureCapacity(blocks[0][0].length);

        for(int i = 0; i < linesToCheck.size(); ++i){
            int y = linesToCheck.get(i);
            boolean toDelete = true;
            for(int k = 0; k < fields.length; ++k){
                if(fields[k][y] == Field.EMPTY){
                    toDelete = false;
                    break;
                }
            }
            if(toDelete){
                linesToDelete.add(y);
            }
        }

        if(linesToDelete.size() > 0){
            for(int i = 0; i < linesToDelete.size(); ++i){
                int y = linesToDelete.get(i);
                for(int j = y; j > 0; --j){
                    for(int k = 0; k < fields.length; ++k){
                        fields[k][j] = fields[k][j - 1];
                    }
                }
            }

            addScore(linesToDelete.size());
        }
    }

    private void removeCurrentBlock(){
        currentBlockPosition[1] = -1;
    }

    private void redrawScore(){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                ((TextView)getView().findViewById(R.id.score)).setText(Integer.toString(score));
            }
        });
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

    private void redrawLevel(){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                ((TextView)getView().findViewById(R.id.level)).setText(Integer.toString(level));
            }
        });
    }

    private void setLevel(int newLevel){
        level = newLevel;
        redrawLevel();
    }

    public void moveLeft() {
        if(nextMove == BlockMoveState.MOVE_BLOCK) {
            synchronized (blockMoveLock) {
                boolean isAvailable = true;
                for (int i = 0; i < currentBlock.length; ++i) {
                    int x = currentBlock[i][0] + currentBlockPosition[0];
                    int y = currentBlock[i][1] + currentBlockPosition[1];
                    if (x <= 0) {
                        isAvailable = false;
                        break;
                    } else {
                        if (fields[x - 1][y] != Field.EMPTY) {
                            isAvailable = false;
                            break;
                        }
                    }
                }
                if (isAvailable) {
                    --currentBlockPosition[0];
                }
            }
        }
    }

    public void moveRight() {
        synchronized (blockMoveLock) {
            if(nextMove == BlockMoveState.MOVE_BLOCK) {
                boolean isAvailable = true;
                for (int i = 0; i < currentBlock.length; ++i) {
                    int x = currentBlock[i][0] + currentBlockPosition[0];
                    int y = currentBlock[i][1] + currentBlockPosition[1];
                    if (x >= fields.length - 1) {
                        isAvailable = false;
                        break;
                    } else {
                        if (fields[x + 1][y] != Field.EMPTY) {
                            isAvailable = false;
                            break;
                        }
                    }
                }
                if (isAvailable) {
                    ++currentBlockPosition[0];
                }
            }
        }
    }



    public void rotate() {
        synchronized (blockMoveLock) {
            if(nextMove == BlockMoveState.MOVE_BLOCK) {
                int nextRotation = currentBlockRotation - 1;
                if(nextRotation < 0)
                    nextRotation += blocks[currentBlockNumber[0]].length;
                switch(checkIfThereIsPlaceForRotation(nextRotation)){
                    case IN_PLACE:
                        for (int i = 0; i < currentBlock.length; ++i) {
                            currentBlock[i][0] = blocks[currentBlockNumber[0]][nextRotation][i][0];
                            currentBlock[i][1] = blocks[currentBlockNumber[0]][nextRotation][i][1];
                        }
                        currentBlockRotation = nextRotation;
                        break;
                    case MOVE_LEFT:
                        for (int i = 0; i < currentBlock.length; ++i) {
                            currentBlock[i][0] = blocks[currentBlockNumber[0]][nextRotation][i][0] - 1;
                            currentBlock[i][1] = blocks[currentBlockNumber[0]][nextRotation][i][1];
                        }
                        currentBlockRotation = nextRotation;
                        break;
                    case MOVE_RIGHT:
                        for (int i = 0; i < currentBlock.length; ++i) {
                            currentBlock[i][0] = blocks[currentBlockNumber[0]][nextRotation][i][0] + 1;
                            currentBlock[i][1] = blocks[currentBlockNumber[0]][nextRotation][i][1];
                        }
                        currentBlockRotation = nextRotation;
                        break;
                }
            }
        }
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
        if(context instanceof PaintsContainer){
            paintsContainer = (PaintsContainer)context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PaintsContainer");
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
