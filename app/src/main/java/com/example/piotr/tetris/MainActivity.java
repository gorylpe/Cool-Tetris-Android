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

public class MainActivity extends AppCompatActivity{

    private static final int REQUEST_PLAY_GAME = 0;
    private final long delay = 60;

    private Random random;
    private Handler uiHandler;
    private MainMenuSurfaceView mainMenuSurfaceView;

    private Timer timer;

    private Board board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        random = new Random();

        uiHandler = new Handler(getMainLooper());

        board = new Board(this, 20, 60);

        for(int i = 0; i < board.getColumns(); ++i){
            for(int j = 0; j < board.getRows(); ++j){
                board.setField(i, j, Board.Field.get(random.nextDouble() < 0.2 ? random.nextInt(Board.Field.getSize()) : 0));
            }
        }

        setContentView(R.layout.activity_main);

        mainMenuSurfaceView = ((MainMenuSurfaceView)findViewById(R.id.main_menu_surfaceview));
        mainMenuSurfaceView.setFieldsAndPaints(board);
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
        for(int i = 0; i < board.getColumns(); ++i){
            for(int j =  board.getRows() - 1; j > 0 ; --j){
                board.setField(i, j, board.getField(i, j - 1));
            }
        }
    }

    private void generateNewFields(){
        for(int i = 0; i < board.getColumns(); ++i){
            board.setField(i, 0, Board.Field.get(random.nextDouble() < 0.2 ? random.nextInt(Board.Field.getSize()) : 0));
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
}
