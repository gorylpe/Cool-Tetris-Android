package com.example.piotr.tetris;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Piotr on 21.05.2017.
 */
public class GameBoard extends Board {

    public enum BlockCoords {
        I(1, new int[][][]{{{-2,0},{-1,0},{0,0},{1,0}} , {{0,-1},{0,0},{0,1},{0,2}}}),
        J(2, new int[][][]{{{-1,0},{0,0},{1,0},{1,1}} , {{-1,1},{0,1},{0,0},{0,-1}} , {{-1,-1},{-1,0},{0,0},{1,0}} , {{1,-1},{0,-1},{0,0},{0,1}}}),
        L(3, new int[][][]{{{-1,1},{-1,0},{0,0},{1,0}}, {{-1,-1},{0,-1},{0,0},{0,1}}, {{-1,0},{0,0},{1,0},{1,-1}} , {{0,-1},{0,0},{0,1},{1,1}}}),
        O(4, new int[][][]{{{0,0},{1,0},{0,1},{1,1}}}),
        S(5, new int[][][]{{{-1,1},{0,1},{0,0},{1,0}} , {{-1,-1},{-1,0},{0,0},{0,1}}}),
        T(6, new int[][][]{{{-1,0},{0,0},{1,0},{0,1}} , {{-1,0},{0,0},{0,-1},{0,1}} , {{-1,0},{0,0},{1,0},{0,-1}} , {{0,0},{0,-1},{0,1},{1,0}}}),
        Z(7, new int[][][]{{{-1,0},{0,0},{0,1},{1,1}} , {{-1,1},{-1,0},{0,0},{0,-1}}});

        private final int block[][][];
        private final int value;

        BlockCoords(int value, int[][][] block){
            this.block = block;
            this.value = value;
        }

        public int getValue(){
            return value;
        }

        private static final List<BlockCoords> values = Collections.unmodifiableList(Arrays.asList(BlockCoords.values()));
        public static BlockCoords get(int i){
            return values.get(i);
        }

        public static int getSize(){
            return values.size();
        }

        public int[][] getCoords(int rotation){
            return block[rotation];
        }

        public int getRotationsNumber(){
            return block.length;
        }
    }

    public class Block implements Serializable{
        private BlockCoords coords;
        private int rotation;
        private int x;
        private int y;

        public Block(BlockCoords coords, int x, int y){
            this.coords = coords;
            this.x = x;
            this.y = y;
            rotation = 0;
        }

        public void rotateRight(){
            if (rotation == coords.getRotationsNumber() - 1) {
                rotation = 0;
            } else {
                rotation++;
            }
        }

        public void rotateLeft(){
            if (rotation == 0) {
                rotation = coords.getRotationsNumber() - 1;
            } else {
                rotation--;
            }
        }

        public void moveUp(){
            y--;
        }


        public void moveDown(){
            y++;
        }

        public void moveLeft(){
            x--;
        }

        public void moveRight(){
            x++;
        }

        public Paint getPaint(){
            return GameBoard.this.getPaint(coords.getValue());
        }

        public int getX(){
            return x;
        }

        public int getY(){
            return y;
        }

        public int[][] getLocalCoords(){
            return coords.getCoords(rotation);
        }

        public Field getFieldType(){
            return Field.get(coords.getValue());
        }
    }

    private Random random;

    private final Object moveLock = new Object();

    private Block currentBlock;
    private Block nextBlock;

    private final int startX = 4;
    private final int startY = 1;

    public GameBoard(Context context, int xSize, int ySize) {
        super(context, xSize, ySize);

        random = new Random();
        nextBlock = new Block(BlockCoords.get(random.nextInt(BlockCoords.getSize())), startX, startY);
        generateNewBlock();
    }

    @Override
    public void saveToBundle(Bundle outState){
        super.saveToBundle(outState);
        outState.putSerializable("currentBlock", currentBlock);
        outState.putSerializable("nextBlock", nextBlock);
    }

    @Override
    public void restoreFromBundle(Bundle savedInstanceState){
        super.restoreFromBundle(savedInstanceState);
        currentBlock = (Block)savedInstanceState.getSerializable("currentBlock");
        nextBlock = (Block)savedInstanceState.getSerializable("nextBlock");
    }

    private enum MoveType{
        CREATE_BLOCK,
        MOVE_DOWN,
        MOVE_LEFT,
        MOVE_RIGHT,
        ROTATE_IN_PLACE,
        ROTATE_WITH_MOVE_LEFT,
        ROTATE_WITH_MOVE_RIGHT
    }

    public boolean checkIfMoveCurrentBlockIsPossible(MoveType moveType) {
        switch(moveType){
            case MOVE_DOWN:
                currentBlock.moveDown();
                break;
            case MOVE_LEFT:
                currentBlock.moveLeft();
                break;
            case MOVE_RIGHT:
                currentBlock.moveRight();
                break;
            case ROTATE_IN_PLACE:
                currentBlock.rotateLeft();
                break;
            case ROTATE_WITH_MOVE_LEFT:
                currentBlock.rotateLeft();
                currentBlock.moveLeft();
                break;
            case ROTATE_WITH_MOVE_RIGHT:
                currentBlock.rotateLeft();
                currentBlock.moveRight();
                break;
        }

        boolean isFree = true;
        int[][] localCoords = currentBlock.getLocalCoords();
        for (int i = 0; i < localCoords.length; ++i) {
            int x = localCoords[i][0] + currentBlock.getX();
            int y = localCoords[i][1] + currentBlock.getY();

            if (x < 0 || x >= getColumns() ||  y < 0 || y >= getRows()) {
                isFree = false;
                break;
            } else {
                if (getField(x, y) != Field.EMPTY) {
                    isFree = false;
                    break;
                }
            }
        }

        switch(moveType){
            case MOVE_DOWN:
                currentBlock.moveUp();
                break;
            case MOVE_LEFT:
                currentBlock.moveRight();
                break;
            case MOVE_RIGHT:
                currentBlock.moveLeft();
                break;
            case ROTATE_IN_PLACE:
                currentBlock.rotateRight();
                break;
            case ROTATE_WITH_MOVE_LEFT:
                currentBlock.rotateRight();
                currentBlock.moveRight();
                break;
            case ROTATE_WITH_MOVE_RIGHT:
                currentBlock.rotateRight();
                currentBlock.moveLeft();
                break;
        }

        return isFree;
    }

    public boolean generateNewBlock(){
        currentBlock = nextBlock;
        if(!checkIfMoveCurrentBlockIsPossible(MoveType.CREATE_BLOCK))
            return false;

        nextBlock = new Block(BlockCoords.get(random.nextInt(BlockCoords.getSize())), startX, startY);

        return true;
    }

    public boolean moveDownCurrentBlock(){
        boolean result;
        synchronized (moveLock){
            result = checkIfMoveCurrentBlockIsPossible(MoveType.MOVE_DOWN);
            if(result)
                currentBlock.moveDown();
        }
        return result;
    }

    public void placeCurrentBlock(){
        synchronized (moveLock){
            int[][] localCoords = currentBlock.getLocalCoords();
            for(int[] fieldCoords : localCoords){
                int x = fieldCoords[0] + currentBlock.getX();
                int y = fieldCoords[1] + currentBlock.getY();

                setField(x, y, currentBlock.getFieldType());
            }
        }
    }

    public int checkAndRemoveFullLines(){
        ArrayList<Integer> linesToRemove = new ArrayList<Integer>();
        linesToRemove.ensureCapacity(getRows());
        for (int y = 0; y < getRows(); ++y){
            boolean toRemove = true;
            for(int x = 0; x < getColumns(); ++x){
                if(getField(x, y) == Field.EMPTY){
                    toRemove = false;
                    break;
                }
            }
            if(toRemove){
                linesToRemove.add(y);
            }
        }

        int removedLines = 0;

        if(linesToRemove.size() > 0){
            for(int line : linesToRemove){
                for(int y = line; y > 0; --y){
                    for(int x = 0; x < getColumns(); ++x){
                        setField(x, y, getField(x, y - 1));
                    }
                }
            }

            removedLines++;
        }

        return removedLines;
    }

    public void removeCurrentBlock(){
        currentBlock = null;
    }

    public void moveLeftCurrentBlock() {
        synchronized (moveLock) {
            if (checkIfMoveCurrentBlockIsPossible(MoveType.MOVE_LEFT)) {
                currentBlock.moveLeft();
            }
        }
    }

    public void moveRightCurrentBlock() {
        synchronized (moveLock) {
            if (checkIfMoveCurrentBlockIsPossible(MoveType.MOVE_RIGHT)) {
                currentBlock.moveRight();
            }
        }
    }

    public void rotateLeftCurrentBlock() {
        synchronized (moveLock) {
            if(checkIfMoveCurrentBlockIsPossible(MoveType.ROTATE_IN_PLACE)){

                currentBlock.rotateLeft();

            } else if(checkIfMoveCurrentBlockIsPossible(MoveType.ROTATE_WITH_MOVE_LEFT)){

                currentBlock.rotateLeft();
                currentBlock.moveLeft();

            } else if(checkIfMoveCurrentBlockIsPossible(MoveType.ROTATE_WITH_MOVE_RIGHT)){

                currentBlock.rotateLeft();
                currentBlock.moveRight();
            }
        }
    }
}
