package edu.massey.matchygame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

import edu.massey.matchygame.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;
    // last button clicked
    private Button lastButton = null;
    // array of buttons set using button ids
    private Button[] buttons;
    // keep track of how many pairs matched
    private int numMatched = 0;
    // keep track of score
    private int score = 0;

    // keys for storing state
    private static final String NUM_MATCHED_KEY = "numMatched";
    private static final String SCORE_KEY = "score";
    private static final String LAST_INDEX_KEY = "lastIndex";
    private static final String BUTTON_STATE_KEY = "buttonState";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        initButtons();
        // call restore state after initializing the app
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            initApp();
        }
        mainBinding.restartButton.setOnClickListener(v -> initApp());
    }

    private void restoreState(Bundle inState) {
        // restore all the values using the saved bundle
        // don't forget to include default values
        numMatched = inState.getInt(NUM_MATCHED_KEY,0);
        score = inState.getInt(SCORE_KEY,0);
        int lastIndex = inState.getInt(LAST_INDEX_KEY,-1);
        String[] buttonState = inState.getStringArray(BUTTON_STATE_KEY);
        for(int i = 0; i < 16; i++) {
            buttons[i].setFreezesText(true);
            buttons[i].setTag(R.string.bValue, buttonState[i]);
        }
        if (lastIndex!=-1) lastButton = buttons[lastIndex];
        else lastButton = null;
    }


    private void initButtons() {
        buttons = new Button[]{
                mainBinding.button11, mainBinding.button12, mainBinding.button13, mainBinding.button14,
                mainBinding.button21, mainBinding.button22, mainBinding.button23, mainBinding.button24,
                mainBinding.button31, mainBinding.button32, mainBinding.button33, mainBinding.button34,
                mainBinding.button41, mainBinding.button42, mainBinding.button43, mainBinding.button44
        };
        for (int i = 0; i < 16; i++) {
            buttons[i].setFreezesText(true);
            buttons[i].setOnClickListener(view -> buttonClick((Button) view));
        }
    }

    private void clearButtonTextAndValues() {
        for (int i = 0; i < buttons.length; i++) {
                buttons[i].setText("");
                buttons[i].setTag(R.string.bValue, "");
        }
    }

    private void setButtonValues() {
        for (int i = 1; i < 9; i++) { // iterate over all 8 numbers
            for (int j = 0; j < 2; j++) { // generate the same number for two tiles
                int x;
                // keep generating a random tile
                do {
                    x = (int) (Math.random() * 16);
                }
                // as long as it's value is already set - in other words, we want to find a blank tile
                while (!"".equals(buttons[x].getTag(R.string.bValue)));
                buttons[x].setTag(R.string.bValue,"" + i);
            }
        }
    }

    private void initApp() {
        // initialize fields
        score = 0;
        lastButton = null;
        numMatched = 0;
        // clear the buttons' text and values
        clearButtonTextAndValues();
        // set the button values
        setButtonValues();
        // set the score text
        showScore();
    }

    private void buttonClick(Button b) {
        // get the computed value of the tile
        String value = (String) b.getTag(R.string.bValue);
        // if the tile shows a number already, do nothing
        if (!b.getText().equals("")) return;
        // show the tile's number
        b.setText(value);
        // if first of a pair
        if (lastButton == null) {
            // set current tile as last button
            lastButton = b;
        }
        // otherwise, it's part of a pair
        else {
            // if a match is found
            if (lastButton.getText().equals(value)) {
                numMatched++;
                score+=10;
                lastButton = null;
            } else {
                lastButton.setText("");
                // set current tile as last index
                lastButton = b;
            }
        }
        showScore();

    }

    private void showScore() {
        if (numMatched == 8) {
            mainBinding.scoreView.setText(getText(R.string.completed) + " " + score);
        } else {
            mainBinding.scoreView.setText(getText(R.string.score) + " " + score);
        }
    }

    // function called when the activity needs to maintain state
    // two situations where this should happen
    // 1. user temp navigates away from activity (e.g. receives call)
    // 2. config change (e.g. phone is rotated)
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // save all necessary state using key-value pairs
        outState.putInt(NUM_MATCHED_KEY, numMatched);
        outState.putInt(SCORE_KEY, score);
        int lastIndex = -1;
        // need to store button state as a string array
        String[] buttonState = new String[16];
        for (int i = 0; i < buttons.length; i++) {
            buttonState[i] = (String) buttons[i].getTag(R.string.bValue);
            if (lastButton == buttons[i]) {
                lastIndex = i;
            }
        }
        outState.putInt(LAST_INDEX_KEY, lastIndex);
        outState.putStringArray(BUTTON_STATE_KEY, buttonState);
        super.onSaveInstanceState(outState);
    }
}