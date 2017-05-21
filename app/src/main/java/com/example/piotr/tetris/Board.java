package com.example.piotr.tetris;


import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Board {

    private Paint[] paints;
    private Paint backgroundPaint;
    private Paint borderPaint;

    public enum Field{
        EMPTY(0),
        CYAN(1),
        BLUE(2),
        ORANGE(3),
        YELLOW(4),
        GREEN(5),
        PURPLE(6),
        RED(7);

        private final int value;
        Field(int value){
            this.value = value;
        }

        public final int getValue(){
            return value;
        }

        private static final List<Field> values = Collections.unmodifiableList(Arrays.asList(Field.values()));
        public static Field get(int i){
            return values.get(i);
        }
    }

    private Field[][] fields;

    public Board(Context context, int xSize, int ySize){
        fields = new Field[xSize][ySize];
        for(int i = 0; i < fields.length; ++i){
            for(int j = 0; j < fields[i].length; ++j){
                fields[i][j] = Field.EMPTY;
            }
        }

        int[] colorsId = context.getResources().getIntArray(R.array.tetris_colors);
        paints = new Paint[8];
        for(int i = 0; i < paints.length; ++i){
            paints[i] = new Paint();
            paints[i].setColor(colorsId[i]);
        }

        backgroundPaint = new Paint();
        backgroundPaint.setColor(R.color.background);

        borderPaint = new Paint();
        borderPaint.setColor(R.color.border);
    }

    public Paint getPaint(int num){
        return paints[num];
    }

    public Paint getBackgroundPaint() {
        return backgroundPaint;
    }

    public Paint getBorderPaint(){
        return borderPaint;
    }

    public int getColumns(){
        return fields.length;
    }

    public int getRows(){
        return fields[0].length;
    }

    public Field getField(int x, int y){
        return fields[x][y];
    }

    public void setField(int x, int y, Field field){
        fields[x][y] = field;
    }

    public void saveToBundle(Bundle outState){
        outState.putSerializable("fields", fields);
    }

    public void restoreFromBundle(Bundle savedInstanceState){
        Field[][] newFields = (Field[][])savedInstanceState.getSerializable("fields");
        for(int i = 0; i < fields.length; ++i){
            for(int j = 0; j < fields[i].length; ++j)
                fields[i][j] = newFields[i][j];
        }
    }
}
