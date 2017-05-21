package com.example.piotr.tetris.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import com.example.piotr.tetris.LeaderboardsActivity;
import com.example.piotr.tetris.R;

import java.util.ArrayList;

public class ScoresAdapter extends BaseAdapter {

    private ArrayList<LeaderboardsActivity.Entry> entries;
    private Context context;

    public ScoresAdapter(ArrayList<LeaderboardsActivity.Entry> entries, Context context){
        this.entries = entries;
        this.context = context;
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        TextView textView;
        if(convertView == null){
            textView = new TextView(context);
            textView.setLayoutParams(new GridView.LayoutParams(viewGroup.getMeasuredWidth(), viewGroup.getMeasuredHeight() / entries.size()));
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(8,8,8,8);
            textView.setTextColor(ContextCompat.getColor(context, R.color.white));
            textView.setTextSize(context.getResources().getDimension(R.dimen.leaderboards_score_size) * (float)viewGroup.getHeight() / 1707.0f);
        } else {
            textView = (TextView) convertView;
        }
        SpannableString spannableString = new SpannableString(entries.get(i).getName() + "\n" + Integer.toString(entries.get(i).getScore()));
        int nameStart = 0;
        int nameEnd = entries.get(i).getName().length();
        int scoreStart = entries.get(i).getName().length() + 1;
        int scoreEnd = scoreStart +  Integer.toString(entries.get(i).getScore()).length();
        int[] tetrisColors = context.getResources().getIntArray(R.array.tetris_colors);
        spannableString.setSpan(new ForegroundColorSpan(i >= tetrisColors.length ? tetrisColors[i % tetrisColors.length] : tetrisColors[i]), nameStart, nameEnd, 0);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), scoreStart, scoreEnd, 0);
        spannableString.setSpan(new RelativeSizeSpan(1.5f), scoreStart, scoreEnd, 0);
        textView.setText(spannableString);

        if(i == entries.size() - 1)
            viewGroup.invalidate();

        return textView;
    }
}
