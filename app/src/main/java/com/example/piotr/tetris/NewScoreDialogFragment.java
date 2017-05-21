package com.example.piotr.tetris;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NewScoreDialogFragment extends DialogFragment {

    public static final String SCORE = "score";
    private int score;

    public void DialogFragment(){}

    public static NewScoreDialogFragment newInstance(int score){
        NewScoreDialogFragment fragment = new NewScoreDialogFragment();
        Bundle args = new Bundle();
        args.putInt(SCORE, score);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        score = getArguments().getInt(SCORE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_add_score, null);
        final TextView scoreTextView = (TextView)view.findViewById(R.id.score);
        scoreTextView.setText(Integer.toString(score));

        builder.setView(view).
                setPositiveButton(R.string.ok, null). //create button in onResume to prevent dismiss on click
                setTitle(R.string.new_score_title);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        final EditText editText = (EditText)view.findViewById(R.id.name);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(editText.getText().toString().equals("")){
                            editText.setHint(R.string.enter_name_invalid_hint);
                        } else {
                            if(getActivity() instanceof NewNameListener) {
                                NewNameListener listener = (NewNameListener) getActivity();
                                listener.addNewScore(editText.getText().toString(), score);
                                dismiss();
                            }
                        }
                    }
                });
            }
        });
        return dialog;
    }

    @Override
    public void onPause(){
        super.onPause();
        Dialog dialog = getDialog();
        if(dialog != null)
            dialog.dismiss();
    }

    public interface NewNameListener{
        void addNewScore(String name, int score);
    }
}
