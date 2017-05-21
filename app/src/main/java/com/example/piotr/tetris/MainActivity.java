package com.example.piotr.tetris;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import com.example.piotr.tetris.custom.MainMenuSurfaceView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements PaintsContainer {

    public static final int REQUEST_PLAY_GAME = 0;
    private final long delay = 60;

    private Paint[] paints;
    private Paint backgroundPaint;

    private Random random;
    private Handler uiHandler;
    private MainMenuSurfaceView mainMenuSurfaceView;

    private Timer timer;

    int[][] fields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        random = new Random();

        uiHandler = new Handler(getMainLooper());

        paints = new Paint[8];
        for(int i = 0; i < paints.length; ++i){
            paints[i] = new Paint();
        }
        paints[0].setColor(ContextCompat.getColor(this, R.color.cyan));
        paints[1].setColor(ContextCompat.getColor(this, R.color.blue));
        paints[2].setColor(ContextCompat.getColor(this, R.color.orange));
        paints[3].setColor(ContextCompat.getColor(this, R.color.yellow));
        paints[4].setColor(ContextCompat.getColor(this, R.color.green));
        paints[5].setColor(ContextCompat.getColor(this, R.color.purple));
        paints[6].setColor(ContextCompat.getColor(this, R.color.red));
        paints[7].setColor(ContextCompat.getColor(this, R.color.background));

        backgroundPaint = new Paint();
        backgroundPaint.setColor(ContextCompat.getColor(this, R.color.background));

        fields = new int[20][60];
        for(int i = 0; i < fields.length; ++i){
            for(int j = 0; j < fields[i].length; ++j){
                fields[i][j] = random.nextDouble() < 0.2 ? random.nextInt(paints.length) : 7;
            }
        }

        setContentView(R.layout.activity_main);

        mainMenuSurfaceView = ((MainMenuSurfaceView)findViewById(R.id.main_menu_surfaceview));
        mainMenuSurfaceView.setFieldsAndPaints(fields, this);
    }

    public void startGame(View view){
        Intent intent = new Intent(this, GameActivity.class);
        startActivityForResult(intent, REQUEST_PLAY_GAME);
    }

    public void leaderboards(View view){
        Intent intent = new Intent(this, LeaderboardsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_PLAY_GAME){
            if(resultCode == RESULT_OK){
                int score = data.getIntExtra("score", 0);
                Intent intent = new Intent(this, LeaderboardsActivity.class);
                intent.putExtra("score", score);
                startActivity(intent);
            }
        }
    }

    private void redraw(){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mainMenuSurfaceView != null) {
                    mainMenuSurfaceView.invalidate();
                }
            }
        });
    }

    private void moveFields(){
        for(int i = 0; i < fields.length; ++i){
            for(int j = fields[i].length - 1; j > 0; --j){
                fields[i][j] = fields[i][j-1];
            }
        }
    }

    private void generateNewFields(){
        for(int i = 0; i < fields.length; ++i){
            fields[i][0] = random.nextDouble() < 0.15 ? random.nextInt(paints.length) : 7; //empty
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                moveFields();
                generateNewFields();
                redraw();
            }
        }, delay, delay);
    }

    @Override
    public void onPause(){
        timer.cancel();
        timer.purge();
        super.onPause();
    }

    @Override
    public Paint[] getBlockPaints() {
        return paints;
    }

    @Override
    public Paint getBackgroundPaint() {
        return backgroundPaint;
    }

    @Override
    public Paint getBorderPaint() {
        return null;
    }
}
