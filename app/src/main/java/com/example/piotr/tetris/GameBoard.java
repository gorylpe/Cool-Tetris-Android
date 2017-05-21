package com.example.piotr.tetris;

import android.content.Context;
import android.graphics.Paint;

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

        private final Object moveLock = new Object();

        public Block(BlockCoords coords, int x, int y){
            this.coords = coords;
            this.x = x;
            this.y = y;
            rotation = 0;
        }

        public void rotateRight(){
            synchronized (moveLock) {
                if (rotation == coords.getRotationsNumber() - 1) {
                    rotation = 0;
                } else {
                    rotation++;
                }
            }
        }

        public void rotateLeft(){
            synchronized (moveLock) {
                if (rotation == 0) {
                    rotation = coords.getRotationsNumber() - 1;
                } else {
                    rotation--;
                }
            }
        }

        public void moveDown(){
            synchronized (moveLock) {
                y++;
            }
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
    }

    private Block currentBlock;
    private Block nextBlock;

    public GameBoard(Context context, int xSize, int ySize) {
        super(context, xSize, ySize);
    }

    public boolean checkIfCanMoveDownCurrentBlock() {
        synchronized (currentBlock.moveLock) {
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
}
