package com.example.piotr.tetris.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import com.example.piotr.tetris.*;

public class GameSurfaceView extends SurfaceView {

    private GameBoard board = null;

    public GameSurfaceView(Context context) {
        super(context);
        initialize();
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public GameSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public GameSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(heightMeasureSpec) / 2;

        setMeasuredDimension(width, MeasureSpec.getSize(heightMeasureSpec));
    }

    private void initialize(){
        setWillNotDraw(false);
    }


    public void setBoard(GameBoard board){
        this.board = board;
    }

    @Override
    protected void onDraw(Canvas canvas){
        if(board != null){
            canvas.drawColor(board.getBorderPaint().getColor());
            canvas.drawRect(1.0f, 1.0f, (float)getWidth() - 2.0f, (float)getHeight() - 2.0f, board.getBackgroundPaint());

            float fieldWidth = (float)getWidth() / board.getColumns();
            float fieldHeight = (float)getHeight() / board.getRows();
            for(int i = 0; i < board.getColumns(); ++i){
                for(int j = 0; j < board.getRows(); ++j){
                    if(board.getField(i, j) != Board.Field.EMPTY)
                        canvas.drawRect((float)i * fieldWidth + 1.0f,
                                        (float)j * fieldHeight + 1.0f,
                                        (i + 1.0f) * fieldWidth - 2.0f,
                                        (j + 1.0f) * fieldHeight - 2.0f,
                                        board.getFieldPaint(i, j));
                }
            }

            Block block = board.getCurrentBlock();
            if (block != null) {
                int[][] localCoords = block.getLocalCoords();
                for (int[] coords : localCoords) {
                    float x = (float)(coords[0] + block.getX());
                    float y = (float)(coords[1] + block.getY());
                    canvas.drawRect(x * fieldWidth + 1.0f,
                            y * fieldHeight + 1.0f,
                            (x + 1.0f) * fieldWidth - 2.0f,
                            (y + 1.0f) * fieldHeight - 2.0f,
                            board.getBlockPaint(block));
                }
            }
        }
    }
}
