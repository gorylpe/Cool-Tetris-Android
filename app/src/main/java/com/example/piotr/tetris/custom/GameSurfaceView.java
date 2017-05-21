package com.example.piotr.tetris.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import com.example.piotr.tetris.GameFragment;
import com.example.piotr.tetris.PaintsContainer;

public class GameSurfaceView extends SurfaceView {

    private Paint[] paints;
    private Paint backgroundPaint;
    private Paint borderPaint;

    private GameFragment.Field fields[][] = null;
    private int currentBlock[][] = null;
    private int currentBlockNumber[] = null;
    private int currentBlockPosition[] = null;

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


    public void setFieldsCurrentBlockAndPaints(GameFragment.Field[][] fields, int[][] currentBlock, int[] currentBlockPosition, int[] currentBlockNumber, PaintsContainer paintsContainer){
        this.fields = fields;
        this.paints = paintsContainer.getBlockPaints();
        this.backgroundPaint = paintsContainer.getBackgroundPaint();
        this.borderPaint = paintsContainer.getBorderPaint();
        this.currentBlock = currentBlock;
        this.currentBlockNumber = currentBlockNumber;
        this.currentBlockPosition = currentBlockPosition;
    }

    @Override
    protected void onDraw(Canvas canvas){
        if(fields != null){
            canvas.drawColor(borderPaint.getColor());
            canvas.drawRect(1.0f, 1.0f, (float)getWidth() - 2.0f, (float)getHeight() - 2.0f, backgroundPaint);

            float fieldWidth = (float)getWidth() / fields.length;
            float fieldHeight = (float)getHeight() / fields[0].length;
            for(int i = 0; i < fields.length; ++i){
                for(int j = 0; j < fields[i].length; ++j){
                    if(fields[i][j] != GameFragment.Field.EMPTY)
                        canvas.drawRect((float)i * fieldWidth + 1.0f,
                                        (float)j * fieldHeight + 1.0f,
                                        (i + 1.0f) * fieldWidth - 2.0f,
                                        (j + 1.0f) * fieldHeight - 2.0f,
                                        paints[fields[i][j].getValue()]);
                }
            }
            synchronized (GameFragment.blockMoveLock) {
                if (currentBlockPosition[1] != -1) {
                    for (int i = 0; i < currentBlock.length; ++i) {
                        float x = (float)(currentBlock[i][0] + currentBlockPosition[0]);
                        float y = (float)(currentBlock[i][1] + currentBlockPosition[1]);
                        canvas.drawRect(x * fieldWidth + 1.0f,
                                y * fieldHeight + 1.0f,
                                (x + 1.0f) * fieldWidth - 2.0f,
                                (y + 1.0f) * fieldHeight - 2.0f,
                                paints[currentBlockNumber[0]]);
                    }
                }
            }
        }
    }
}
