package edu.massey.matchygame;

import android.graphics.Interpolator;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import edu.massey.matchygame.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // keys for storing state
    private static final String NUM_MATCHED_KEY = "numMatched";
    private static final String SCORE_KEY = "score";
    private static final String LAST_INDEX_KEY = "lastIndex";

    private static final String TILE_TURNED_STATE_KEY = "tileTurnedState";

    private static final String TILE_DRAWABLE_STATE_KEY = "tileDrawableState";
    ActivityMainBinding mainBinding;
    // last tile clicked
    private ImageView lastTile = null;
    // array of tiles set using view binding
    private ImageView[] tiles;
    // array of drawable ids
    private int[] drawables = {
            R.drawable.baseline_account_circle_24,
            R.drawable.baseline_auto_awesome_24,
            R.drawable.baseline_bluetooth_24,
            R.drawable.baseline_build_24,
            R.drawable.baseline_cable_24,
            R.drawable.baseline_camera_alt_24,
            R.drawable.baseline_dataset_24,
            R.drawable.baseline_battery_charging_full_24,
    };
    // keep track of how many pairs matched
    private int numMatched = 0;
    // keep track of score
    private int score = 0;

    // inner class for storing tileState
    class TileState {
        public int resId;
        public boolean turned;
        TileState(int r, boolean t) {
            resId = r;
            turned = t;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        initTiles();
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
        numMatched = inState.getInt(NUM_MATCHED_KEY, 0);
        score = inState.getInt(SCORE_KEY, 0);
        int lastIndex = inState.getInt(LAST_INDEX_KEY, -1);
        boolean[] tileTurnedState = inState.getBooleanArray(TILE_TURNED_STATE_KEY);
        int[] tileDrawableState = inState.getIntArray(TILE_DRAWABLE_STATE_KEY);
        for (int i = 0; i < 16; i++) {
            tiles[i].setTag(new TileState(tileDrawableState[i], tileTurnedState[i]));
            if (tileTurnedState[i]) {
                tiles[i].setImageResource(tileDrawableState[i]);
            } else {
                tiles[i].setImageDrawable(null);
            }
        }
        if (lastIndex != -1) lastTile = tiles[lastIndex];
        else lastTile = null;
    }


    private void initTiles() {
        tiles = new ImageView[]{
                mainBinding.button11, mainBinding.button12, mainBinding.button13, mainBinding.button14,
                mainBinding.button21, mainBinding.button22, mainBinding.button23, mainBinding.button24,
                mainBinding.button31, mainBinding.button32, mainBinding.button33, mainBinding.button34,
                mainBinding.button41, mainBinding.button42, mainBinding.button43, mainBinding.button44
        };
        for (int i = 0; i < 16; i++) {
            tiles[i].setOnClickListener(view -> tileClick((ImageView) view));
        }
    }

    private void clearTileStates() {
        for (ImageView tile : tiles) {
            tile.setImageDrawable(null);
            tile.setTag(new TileState(0, false));
        }
    }

    private void setTileImages() {
        for (int i = 0; i < 8; i++) { // iterate over all 8 numbers
            for (int j = 0; j < 2; j++) { // generate the same number for two tiles
                int x;
                TileState t;
                // keep generating a random tile index
                do {
                    x = (int) (Math.random() * 16);
                    t = (TileState) tiles[x].getTag();
                }
                // as long as the tag resource id isn't 0 - i.e. keep searching if non-blank tile
                while (t.resId != 0);
                // set the resource id of the tag
                t.resId = drawables[i];
            }
        }
    }

    private void initApp() {
        // initialize fields
        score = 0;
        lastTile = null;
        numMatched = 0;
        // clear the buttons' text and values
        clearTileStates();
        // set the button values
        setTileImages();
        // set the score text
        showScore();
    }

    private void tileClick(ImageView t) {
        // get the computed resource id of the tile
        int value = ((TileState) t.getTag()).resId;
        // if the tile shows an icon already, do nothing
        if (t.getDrawable() != null) return;
        // show the tile's icon
        showTile(t, true);
        // if first of a pair
        if (lastTile == null) {
            // set current tile as last tile
            lastTile = t;
        }
        // otherwise, it's part of a pair
        else {
            // if a match is found
            if (((TileState) lastTile.getTag()).resId == value) {
                numMatched++;
                score += 10;
                lastTile = null;
            } else {
                showTile(lastTile, false);
                // set current tile as last tile
                lastTile = t;
            }
        }
        showScore();
    }

    private void showTile(final ImageView tile, final boolean turned) {
        final TileState ts = (TileState) tile.getTag();
        final float from, to;
        // if changing back to blank tile
        if (!turned) {
            // change from front to back
            from = 0;
            to = 180;
        } else {
            // change from back to front
            from = 180;
            to = 360;
        }
        // set rotation on y-axis to either front or back
        tile.setRotationY(from);
        // rotate halfway...
        tile.animate().rotationY(from + to / 2.0f).setDuration(250).withEndAction(() -> {
            // then either set the icon or reset it
            if (turned) tile.setImageResource(ts.resId);
            else tile.setImageDrawable(null);
            ts.turned = turned;
            // and rotate all the way
            tile.animate().rotationY(to).setDuration(250).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(() -> {
                tile.setRotationY(0);
            });
        });
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
        // need to store tile state as two separate arrays
        boolean[] tileTurnedState = new boolean[16];
        int[] tileDrawableState = new int[16];
        for (int i = 0; i < tiles.length; i++) {
            TileState ts = (TileState) tiles[i].getTag();
            tileTurnedState[i] = ts.turned;
            tileDrawableState[i] = ts.resId;
            if (lastTile == tiles[i]) {
                lastIndex = i;
            }
        }
        outState.putInt(LAST_INDEX_KEY, lastIndex);
        outState.putBooleanArray(TILE_TURNED_STATE_KEY, tileTurnedState);
        outState.putIntArray(TILE_DRAWABLE_STATE_KEY, tileDrawableState);
        super.onSaveInstanceState(outState);
    }
}