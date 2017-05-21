package com.example.piotr.tetris;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridView;
import android.widget.TextView;
import com.example.piotr.tetris.custom.ScoresAdapter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class LeaderboardsActivity extends AppCompatActivity implements NewScoreDialogFragment.NewNameListener {

    private static final String NEW_SCORE_DIALOG = "new_score_dialog";
    private static final String TOO_LOW_SCORE_DIALOG = "too_low_score_dialog";

    private GridView scoresGrid;
    private ArrayList<Entry> entries;

    public static class Entry implements Comparable<Entry>, Serializable{
        private String name;
        private int score;

        public Entry(String name, int score){
            this.name = name;
            this.score = score;
        }

        public int getScore(){
            return score;
        }

        public String getName(){
            return name;
        }

        @Override
        public int compareTo(Entry entry) {
            return -Integer.valueOf(score).compareTo(entry.getScore());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboards);

        entries = new ArrayList<>();
        loadEntries();

        TextView leaderboardsView = (TextView)findViewById(R.id.leaderboardsView);

        scoresGrid = (GridView)findViewById(R.id.scoresGrid);

        //adapter calculated height depends on scoresGrid height
        scoresGrid.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scoresGrid.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                scoresGrid.setAdapter(new ScoresAdapter(entries, LeaderboardsActivity.this));
            }
        });
        //cant scroll
        scoresGrid.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return motionEvent.getAction() == MotionEvent.ACTION_MOVE;
            }
        });

        //create only once
        if(savedInstanceState == null){
            Intent intent = getIntent();
            if(intent != null) {
                if (intent.hasExtra("score")) {
                    int score = intent.getIntExtra("score", 0);
                    int lastScore = entries.get(entries.size() - 1).getScore();
                    if (score > lastScore) {
                        NewScoreDialogFragment fragment = NewScoreDialogFragment.newInstance(score);
                        fragment.show(getFragmentManager(), NEW_SCORE_DIALOG);
                    } else {
                        TooLowScoreDialogFragment fragment = new TooLowScoreDialogFragment();
                        fragment.show(getFragmentManager(), TOO_LOW_SCORE_DIALOG);
                    }
                }
            }
        }
    }

    public static class TooLowScoreDialogFragment extends DialogFragment{
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.too_low_score_title);
            builder.setMessage(R.string.too_low_score_desc);
            return builder.create();
        }
    }

    private void saveEntries(){
        try{
            FileOutputStream fos = this.openFileOutput(this.getString(R.string.filename), Context.MODE_PRIVATE);
            ObjectOutputStream of = new ObjectOutputStream(fos);
            of.writeObject(entries);
            of.flush();
            of.close();
            fos.close();
            Log.d("Save to file:", this.toString());
        } catch(Exception e){
            Log.e("InternalStorageSave", e.getMessage());
        }
    }

    private void loadEntries(){
        try{
            FileInputStream fis = this.openFileInput(this.getString(R.string.filename));
            ObjectInputStream oi = new ObjectInputStream(fis);
            Object read = oi.readObject();
            if(read instanceof ArrayList){
                entries = (ArrayList<Entry>)read;
                Log.d("Load from file:", this.toString() + ":" + Integer.toString(entries.size()));
            }
            if(entries.size() < 10)
                createNewEntries();
            fis.close();
        } catch(FileNotFoundException e){
            createNewEntries();
        } catch(IOException | ClassNotFoundException e){
            Log.e("InternalStorageLoad", e.getMessage());
        }
    }

    private void createNewEntries(){
        entries = new ArrayList<>(10);
        for(int i = 10; i > 0; --i){
            entries.add(new Entry("GAME MASTER " + i, i * 1000));
        }
    }

    @Override
    public void addNewScore(String name, int score) {
        entries.set(entries.size() - 1, new Entry(name, score));
        Collections.sort(entries);
        saveEntries();
        scoresGrid.setAdapter(new ScoresAdapter(entries, LeaderboardsActivity.this));
    }
}