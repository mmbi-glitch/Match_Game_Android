package edu.massey.matchygame;

import android.animation.TimeInterpolator;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import edu.massey.matchygame.databinding.ActivityMainBinding;
import edu.massey.matchygame.databinding.TileBinding;

public class MainActivity extends AppCompatActivity {

    // ----------------- data fields ------------------ //

    // tag for use by Log
    private static final String TAG = "MatchingGame";

    // keys for storing state
    private static final String NUM_MATCHED_KEY = "numMatched";
    private static final String SCORE_KEY = "score";
    private static final String LAST_INDEX_KEY = "lastIndex";
    private static final String TILE_TURNED_STATE_KEY = "tileTurnedState";
    private static final String TILE_VALUE_STATE_KEY = "tileDrawableState";
    // statics for storing grid view stuff
    private static final int NUM_COLS = 4;
    private static final int NUM_TILES = 32;
    // array of drawable ids
    private final int[] drawables = {
            R.drawable.baseline_account_circle_24,
            R.drawable.baseline_auto_awesome_24,
            R.drawable.baseline_bluetooth_24,
            R.drawable.baseline_build_24,
            R.drawable.baseline_cable_24,
            R.drawable.baseline_camera_alt_24,
            R.drawable.baseline_dataset_24,
            R.drawable.baseline_battery_charging_full_24,
    };
    // global interpolator
    private final TimeInterpolator flipInterpolator = new AccelerateDecelerateInterpolator();
    // array of tiles set using view binding
//    private ImageView[] tiles;
    // last tile clicked
    private final ImageView lastTile = null;
    // view bindings
    ActivityMainBinding mainBinding;
    TileBinding tileBinding;
    private GridView tileGrid;
    private TileAdapter tileAdapter;
    // index of last tile clicked
    private int lastTileIndex = -1;
    // array that stores index of drawables behind a tile
    private int[] tileValue = new int[NUM_TILES];
    // array that keeps track of a tile's turned state
    private boolean[] tileTurned = new boolean[NUM_TILES];

    // keep track of how many pairs matched
    private int numMatched = 0;
    // keep track of score
    private int score = 0;

    // ------------------ inner classes ---------------------------- //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        setSupportActionBar(mainBinding.toolbar);
        initTiles();
        // call restore state after initializing the app
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            initApp();
        }
    }

    private void restoreState(Bundle inState) {
        // restore all the values using the saved bundle
        // don't forget to include default values
        numMatched = inState.getInt(NUM_MATCHED_KEY, 0);
        score = inState.getInt(SCORE_KEY, 0);
        lastTileIndex = inState.getInt(LAST_INDEX_KEY, -1);
        tileTurned = inState.getBooleanArray(TILE_TURNED_STATE_KEY);
        tileValue = inState.getIntArray(TILE_VALUE_STATE_KEY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions, menu);
        return super.onCreateOptionsMenu(menu);
    }


    // ------------------ overridden methods -------------------- //

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_restart) {
            // more advanced dialog that survives rotations
            new SureDialog().show(getSupportFragmentManager(), null);
            return true;
        }
        if (item.getItemId() == R.id.action_help) {
            Snackbar.make(mainBinding.getRoot(), "Help coming soon!", Snackbar.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initApp() {
        // initialize fields
        score = 0;
        lastTileIndex = -1;
        numMatched = 0;
        // clear tile states
        clearTileState();
        // set the tile images
        setTileImages();
        // tell the tile adapted that data has changed
        tileAdapter.notifyDataSetInvalidated();
        // set the score text
        showScore();
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
        outState.putInt(LAST_INDEX_KEY, lastTileIndex);
        outState.putBooleanArray(TILE_TURNED_STATE_KEY, tileTurned);
        outState.putIntArray(TILE_VALUE_STATE_KEY, tileValue);
        super.onSaveInstanceState(outState);
    }

    private void clearTileState() {
        for (int i = 0; i < NUM_TILES; i++) {
            tileValue[i] = -1;
            tileTurned[i] = false;
        }
    }

    private void setTileImages() {
        // iterate over all 8 images
        for (int i = 0; i < 8; i++) {
            // choose some unused tiles and set their values to i
            for (int j = 0; j < NUM_TILES / 8; j++) {
                int x;
                // keep generating a random tile index
                // as long as it's a non-blank tile
                do x = (int) (Math.random() * NUM_TILES);
                while (tileValue[x] != -1);
                // set the tile value equal to an index for the drawables
                tileValue[x] = i;
            }
        }
    }

    // ---------------------- helper methods ---------------------- //

    private void initTiles() {
        tileGrid = mainBinding.gridView;
        tileGrid.setNumColumns(NUM_COLS);
        tileAdapter = new TileAdapter();
        tileGrid.setAdapter(tileAdapter);
        tileGrid.setOnItemClickListener((parent, view, position, id) -> tileClick(position));
    }

    private void tileClick(int position) {
        // if tile is already turned, do nothing
        if (tileTurned[position]) return;
        // if it's the first of a pair
        if (lastTileIndex == -1) {
            // turn around just this tile - show its drawable
            lastTileIndex = position;
            showTile(position, tileValue[position], false, 0);
        } else {
            // if it matches the previous tile
            if (tileValue[lastTileIndex] == tileValue[position]) {
                // keep them both turned around - i.e. show their drawables
                showTile(position, tileValue[position], false, 0);
                numMatched++;
                score += 10;
                lastTileIndex = -1;
            }
            // else, if doesn't match
            else {
                // turn the current one around to show its drawable
                // for this, you may want to use turnBack = true,
                // this means if the drawable is off the screen
                // while animating, it will flip back
                showTile(position, tileValue[position], true, 0);
                // and turn the previous one back - hide its drawable
                showTile(lastTileIndex, -1, false, 0);
                // this
                lastTileIndex = -1;
            }
        }
        showScore();
    }

    // turn image-view at 'position' round to show drawable 'tile' or blank if 'tile' is -1
    // if 'turnBack' is true, turn the tile around again after a 400ms delay
    private void showTile(final int position, final int tile, final boolean turnBack, int delay) {
        final int from, to;
        final ViewHolder vh;
        // get the view for the tile if it's visible
        View layout = tileGrid.getChildAt(position - tileGrid.getFirstVisiblePosition());
        // if the view is null --- i.e. off the screen
        if (layout == null) {
            // make sure it's turned back
            tileTurned[position] = false;
            return;
        }
        // get its view holder from tag
        vh = (ViewHolder) layout.getTag();
        // rotation animation start and end values
        if (!tileTurned[position]) {
            from = -180;
            to = 0;
        } else {
            from = 0;
            to = -180;
        }
        // record if it's turned or not
        tileTurned[position] = tile != -1;
        // when animating, check the position first - otherwise, may run on wrong view
        if (vh.position != position) {
            if (turnBack) tileTurned[position] = false;
            return;
        }
        // if we are turning it back, make it visible first.
        if (tile == -1) {
            vh.image.setImageResource(drawables[tileValue[position]]);
        } else {
            vh.image.setImageDrawable(null);
        }
        // set initial rotation
        vh.image.setRotationY(from);
        // rotate half way
        vh.image.animate().rotationY((from + to) / 2f).setDuration(200).setInterpolator(flipInterpolator).setStartDelay(delay).withEndAction(() -> {
            // after rotating halfway, change the tile drawable
            if (vh.position != position) { // check position again
                if (turnBack)
                    tileTurned[position] = false;
                return;
            }
            // if the image view is not turned around, set its drawable to null
            if (tile == -1) vh.image.setImageDrawable(null);
                // otherwise, make its drawable visible
            else vh.image.setImageResource(drawables[tile]);
            // then rotate the rest of the way
            vh.image.animate().rotationY(to).setStartDelay(0).setInterpolator(flipInterpolator).setDuration(200).withEndAction(() -> {
                // if turnBack is set, flip it back
                if (turnBack) showTile(position, -1, false, 400);
            });
        });
    }

    private void showScore() {
        String scoreText = getText(R.string.app_name) + " ";
        if (numMatched == NUM_TILES / 2) {
            scoreText += getText(R.string.completed).toString() + " " + score;
        } else {
            scoreText += getText(R.string.score).toString() + " " + score;
        }
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setTitle(scoreText);
    }

    // to make dialog not disappear during a rotation, need to create a new class
    // that extends from DialogFragment
    public static class SureDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            MainActivity activity = (MainActivity) getActivity();
            return new AlertDialog.Builder(Objects.requireNonNull(activity))
                    .setMessage(R.string.sure)
                    .setPositiveButton("Yes", (di, i) -> activity.initApp())
                    .setNegativeButton("No", null)
                    .create();
        }
    }

    public class ViewHolder {
        public int position;
        public ImageView image;
    }

    // inner class for tile adapter
    public class TileAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return NUM_TILES;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            // if view not recycled
            if (convertView == null) {
                // inflate layout from xml
                tileBinding = TileBinding.inflate(getLayoutInflater());
                // set convert view will be predefined linear layout
                convertView = tileBinding.getRoot();
                // create new view holder
                vh = new ViewHolder();
                vh.image = tileBinding.tileButton;
                // and set the tag
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            // set the size of the view to be square
            convertView.setMinimumHeight(tileGrid.getWidth() / tileGrid.getNumColumns());
            // make sure image view is not rotated
            vh.image.setRotationY(0);
            // if it's turned over, show the icon, otherwise not
            if (tileTurned[position]) vh.image.setImageResource(drawables[tileValue[position]]);
            else vh.image.setImageDrawable(null);
            // set the position
            vh.position = position;

            return convertView;
        }
    }
}
