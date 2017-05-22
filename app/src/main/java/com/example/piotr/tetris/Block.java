package com.example.piotr.tetris;

import android.graphics.Paint;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Block implements Serializable {

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

        private static final List<BlockCoords> values = Arrays.asList(BlockCoords.values());
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

    private BlockCoords coords;
    private Integer rotation;
    private Integer x;
    private Integer y;

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

    public int getValue(){
        return coords.getValue();
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

    public Board.Field getFieldType(){
        return Board.Field.get(coords.getValue());
    }
}
