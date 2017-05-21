package com.example.piotr.tetris.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceView;
import com.example.piotr.tetris.GameFragment;
import com.example.piotr.tetris.PaintsContainer;
import com.example.piotr.tetris.R;

/**
 * Created by Piotr on 21.05.2017.
 */
public class MainMenuSurfaceView extends SurfaceView{

    private int[][] fields;
    private Paint[] paints;
    private Paint backgroundPaint;


    public MainMenuSurfaceView(Context context) {
        super(context);
        initialize();
    }

    public MainMenuSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public MainMenuSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public MainMenuSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize(){
        setWillNotDraw(false);
    }

    public void setFieldsAndPaints(int[][] fields, PaintsContainer paintsContainer){
        this.fields = fields;
        this.paints = paintsContainer.getBlockPaints();
        this.backgroundPaint = paintsContainer.getBackgroundPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (fields != null) {
            float fieldWidth = (float) getWidth() / fields.length;
            for (int i = 0; i < fields.length; ++i) {
                for (int j = 0; j < fields[i].length; ++j) {
                    canvas.drawRect((float) i * fieldWidth + 1.0f,
                            (float) j * fieldWidth + 1.0f,
                            (i + 1.0f) * fieldWidth - 2.0f,
                            (j + 1.0f) * fieldWidth - 2.0f,
                            paints[fields[i][j]]);
                }
            }
        }
        canvas.drawColor(R.color.mainMenuBackground);
    }
}
