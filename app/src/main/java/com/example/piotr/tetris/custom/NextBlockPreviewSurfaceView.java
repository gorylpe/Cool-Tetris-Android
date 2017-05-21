package com.example.piotr.tetris.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.SurfaceView;
import com.example.piotr.tetris.PaintsContainer;
import com.example.piotr.tetris.R;


public class NextBlockPreviewSurfaceView extends SurfaceView{

    private Paint[] paints;
    private Paint backgroundPaint;

    private int[][] nextBlock = null;
    private int[] nextBlockNumber = null;


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

    public void setBoard(int[][] nextBlock, int[] nextBlockNumber, PaintsContainer paintsContainer){
        this.nextBlock = nextBlock;
        this.nextBlockNumber = nextBlockNumber;
        this.paints = paintsContainer.getBlockPaints();
        this.backgroundPaint = paintsContainer.getBackgroundPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (nextBlock != null) {
            canvas.drawColor(backgroundPaint.getColor());
            float fieldWidth = (float) getWidth() / 5;
            float fieldHeight = (float) getHeight() / 5;
            for (int i = 0; i < nextBlock.length; ++i) {
                float x = (float)nextBlock[i][0] + 2.0f;
                float y = (float)nextBlock[i][1] + 1.0f;
                canvas.drawRect(x * fieldWidth + 1.0f,
                        y * fieldHeight + 1.0f,
                        (x + 1.0f) * fieldWidth - 2.0f,
                        (y + 1.0f) * fieldHeight - 2.0f,
                        paints[nextBlockNumber[0]]);
            }
        }
    }

}
