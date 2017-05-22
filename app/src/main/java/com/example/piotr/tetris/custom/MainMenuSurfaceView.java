package com.example.piotr.tetris.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.SurfaceView;
import com.example.piotr.tetris.Board;
import com.example.piotr.tetris.GameFragment;
import com.example.piotr.tetris.PaintsContainer;
import com.example.piotr.tetris.R;


public class MainMenuSurfaceView extends SurfaceView{

    private Board board;


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

    public void setFieldsAndPaints(Board board){
        this.board = board;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (board != null) {
            float fieldWidth = (float) getWidth() / board.getColumns();
            for (int i = 0; i < board.getColumns(); ++i) {
                for (int j = 0; j < board.getRows(); ++j) {
                    canvas.drawRect((float) i * fieldWidth + 1.0f,
                            (float) j * fieldWidth + 1.0f,
                            (i + 1.0f) * fieldWidth - 2.0f,
                            (j + 1.0f) * fieldWidth - 2.0f,
                            board.getFieldPaint(i, j));
                }
            }
        }
        canvas.drawColor(ContextCompat.getColor(getContext(), R.color.mainMenuBackground));
    }
}
