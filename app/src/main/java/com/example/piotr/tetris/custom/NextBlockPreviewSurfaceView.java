package com.example.piotr.tetris.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceView;
import com.example.piotr.tetris.Block;
import com.example.piotr.tetris.GameBoard;


public class NextBlockPreviewSurfaceView extends SurfaceView{

    private GameBoard board;


    public NextBlockPreviewSurfaceView(Context context) {
        super(context);
        initialize();
    }

    public NextBlockPreviewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public NextBlockPreviewSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public NextBlockPreviewSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize(){
        setWillNotDraw(false);
    }


    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = MeasureSpec.getSize(widthMeasureSpec);

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
    }

    public void setBoard(GameBoard board){
        this.board = board;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (board != null) {
            Block block = board.getNextBlock();
            if(block != null) {
                canvas.drawColor(board.getBackgroundPaint().getColor());
                float fieldWidth = (float) getWidth() / 5;
                float fieldHeight = (float) getHeight() / 5;

                int[][] localCoords = block.getLocalCoords();
                for (int[] coords : localCoords) {
                    float x = (float)(coords[0] + 2.0f);
                    float y = (float)(coords[1] + 1.0f);
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
