package com.example.piotr.tetris;

import android.content.Context;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.Collections;

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

        public int[][] getCoords(int rotation){
            return block[rotation];
        }

        public int getRotationsNumber(){
            return block.length;
        }
    }

    public class Block{
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

        public int[][] getRotatedLocalCoords(int rotation){
            return coords.getCoords(rotation);
        }

        public Field getFieldType(){
            return Field.getFromValue(coords.getValue());
        }
    }

    private final Object moveLock = new Object();

    private Block currentBlock;
    private Block nextBlock;

    public GameBoard(Context context, int xSize, int ySize) {
        super(context, xSize, ySize);
    }

    public boolean checkIfCanMoveDownCurrentBlock() {
        synchronized (moveLock) {
            boolean isFree = true;
            int[][] localCoords = currentBlock.getLocalCoords();
            for (int i = 0; i < localCoords.length; ++i) {
                int x = localCoords[i][0] + currentBlock.getX();
                int y = localCoords[i][1] + currentBlock.getY();

                if (y >= getRows() - 1) {
                    isFree = false;
                    break;
                } else {
                    if (getField(x, y + 1) != Field.EMPTY) {
                        isFree = false;
                        break;
                    }
                }
            }
            return isFree;
        }
    }

    public void moveCurrentBlock(){
        synchronized (moveLock){
            currentBlock.moveDown();
        }
    }

    public void placeCurrentBlock(){
        synchronized (moveLock){
            int[][] localCoords = currentBlock.getLocalCoords();
            for(int i = 0; i < localCoords.length; ++i){
                int x = localCoords[i][0] + currentBlock.getX();
                int y = localCoords[i][1] + currentBlock.getY();

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
            boolean isAvailable = true;
            int[][] localCoords = currentBlock.getLocalCoords();
            for (int i = 0; i < localCoords.length; ++i) {
                int x = localCoords[i][0] + currentBlock.getX();
                int y = localCoords[i][1] + currentBlock.getY();
                if (x <= 0) {
                    isAvailable = false;
                    break;
                } else {
                    if (getField(x - 1, y) != Field.EMPTY) {
                        isAvailable = false;
                        break;
                    }
                }
            }
            if (isAvailable) {
                currentBlock.moveLeft();
            }
        }
    }

    public void moveRightCurrentBlock() {
        synchronized (moveLock) {
            boolean isAvailable = true;
            int[][] localCoords = currentBlock.getLocalCoords();
            for (int i = 0; i < localCoords.length; ++i) {
                int x = localCoords[i][0] + currentBlock.getX();
                int y = localCoords[i][1] + currentBlock.getY();
                if (x >= getColumns() - 1) {
                    isAvailable = false;
                    break;
                } else {
                    if (getField(x + 1, y) != Field.EMPTY) {
                        isAvailable = false;
                        break;
                    }
                }
            }
            if (isAvailable) {
                currentBlock.moveRight();
            }
        }
    }

    private enum RotationType{
        IN_PLACE,
        MOVE_LEFT,
        MOVE_RIGHT,
        NOT_POSSIBLE
    }

    private RotationType checkIfThereIsPlaceForRotation(int rotation){
        RotationType rotationType = RotationType.IN_PLACE;
        int[][] blockToCheck = blocks[currentBlockNumber[0]][rotation];
        for (int i = 0; i < blockToCheck.length; ++i) {
            int x = blockToCheck[i][0] + currentBlockPosition[0];
            int y = blockToCheck[i][1] + currentBlockPosition[1];
            if(x < 0 || x >= fields.length || y < 0 || y >= fields[0].length){
                rotationType = RotationType.NOT_POSSIBLE;
                break;
            } else {
                if(fields[x][y] != Field.EMPTY){
                    rotationType = RotationType.NOT_POSSIBLE;
                    break;
                }
            }
        }
        //check move left
        if(rotationType == RotationType.NOT_POSSIBLE){
            rotationType = RotationType.MOVE_LEFT;
            for (int i = 0; i < blockToCheck.length; ++i) {
                int x = blockToCheck[i][0] + currentBlockPosition[0] - 1;
                int y = blockToCheck[i][1] + currentBlockPosition[1];
                if(x < 0 || x >= fields.length || y < 0 || y >= fields[0].length){
                    rotationType = RotationType.NOT_POSSIBLE;
                    break;
                } else {
                    if(fields[x][y] != Field.EMPTY){
                        rotationType = RotationType.NOT_POSSIBLE;
                        break;
                    }
                }
            }
        }
        //check move right
        if(rotationType == RotationType.NOT_POSSIBLE){
            rotationType = RotationType.MOVE_RIGHT;
            for (int i = 0; i < blockToCheck.length; ++i) {
                int x = blockToCheck[i][0] + currentBlockPosition[0] + 1;
                int y = blockToCheck[i][1] + currentBlockPosition[1];
                if(x < 0 || x >= fields.length || y < 0 || y >= fields[0].length){
                    rotationType = RotationType.NOT_POSSIBLE;
                    break;
                } else {
                    if(fields[x][y] != Field.EMPTY){
                        rotationType = RotationType.NOT_POSSIBLE;
                        break;
                    }
                }
            }
        }
        return rotationType;
    }
}
