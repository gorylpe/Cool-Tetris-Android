package com.example.piotr.tetris;

/**
 * Created by Piotr on 21.05.2017.
 */
public class Board {

    public enum Field{
        CYAN(0),
        BLUE(1),
        ORANGE(2),
        YELLOW(3),
        GREEN(4),
        PURPLE(5),
        RED(6),
        EMPTY(7);

        private final int value;
        Field(int value){
            this.value = value;
        }

        public final int getValue(){
            return value;
        }
        public static Field getFromValue(int value){
            switch(value){
                case 0:
                    return Field.CYAN;
                case 1:
                    return Field.BLUE;
                case 2:
                    return Field.ORANGE;
                case 3:
                    return Field.YELLOW;
                case 4:
                    return Field.GREEN;
                case 5:
                    return Field.PURPLE;
                case 6:
                    return Field.RED;
            }
            return Field.EMPTY;
        }
    }

    private Field[][] fields;

    public Board(int xSize, int ySize){
        fields = new Field[xSize][ySize];
        for(int i = 0; i < fields.length; ++i){
            for(int j = 0; j < fields[i].length; ++j){
                fields[i][j] = Field.EMPTY;
            }
        }
    }
}
