package edu.massey.matchygame;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

import edu.massey.matchygame.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mainBinding;
    // index of last button clicked
    private int lastButtonIndex = -1;
    // array of buttons set using button ids
    private Button[] buttons;
    // keep track of how many pairs matched
    private int numMatched = 0;
    // keep track of score
    private int score = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        initButtons();
        initApp();
        mainBinding.restartButton.setOnClickListener(v -> initApp());
    }

    private void initButtons() {
        buttons = new Button[]{
                mainBinding.button11, mainBinding.button12, mainBinding.button13, mainBinding.button14,
                mainBinding.button21, mainBinding.button22, mainBinding.button23, mainBinding.button24,
                mainBinding.button31, mainBinding.button32, mainBinding.button33, mainBinding.button34,
                mainBinding.button41, mainBinding.button42, mainBinding.button43, mainBinding.button44
        };
        for (int i = 0; i < 16; i++) {
            buttons[i].setOnClickListener(view -> buttonClick((Button) view));
        }
    }

    private void clearButtonTextAndValues() {
        for (int i = 0; i < buttons.length; i++) {
                buttons[i].setText("");
                buttons[i].setTag(R.string.bValue, "");
                buttons[i].setTag(R.integer.bIndex, i);
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
        lastButtonIndex = -1;
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
        if (lastButtonIndex == -1) {
            // set current tile as last index
            lastButtonIndex = (int) b.getTag(R.integer.bIndex);
        }
        // otherwise, it's part of a pair
        else {
            // if a match is found
            if (buttons[lastButtonIndex].getText().equals(value)) {
                numMatched++;
                score+=10;
                lastButtonIndex = -1;
            } else {
                buttons[lastButtonIndex].setText("");
                // set current tile as last index
                lastButtonIndex = (int) b.getTag(R.integer.bIndex);
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

}