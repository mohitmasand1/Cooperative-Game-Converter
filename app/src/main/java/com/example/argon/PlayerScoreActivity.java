
package com.example.argon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.argon.Model.GameConfig;
import com.example.argon.Model.GameConfigManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @since 11-21-22
 * Activity class for recording individual player scores in a gameplay.
 * Active while creating or editing a game play and display the individual scores according to
 * number of players. */

public class PlayerScoreActivity extends AppCompatActivity {
    private static final String GAME_LIST_POSITION = "game list position";
    private static final int EMPTY = -1;
    private static final int MAX_CHARS = 7;
    private GameConfigManager gameConfigManager;
    private static final String NUM_PLAYERS = "number of players";
    private static final String LIST_POSITION = "list position";
    private static final String DIFFICULTY = "difficulty";
    private static final String ACTIVITY_TYPE = "activity type";
    private static final String ADD_GAMEPLAY = "add gameplay";
    private static final String EDIT_GAMEPLAY = "edit gameplay";
    private static final String GAME_CONFIG_MANAGER = "game config manager";
    private static final String GAME_CONFIG_PREF = "game config pref";
    private final String ADD_PLAYER = "add player";
    private final String MINUS_PLAYER = "minus player";
    private final String[] themes = {"Fruit", "Animal", "Planet"};
    private final int MIN_PLAYERS = 1;
    private final List<Integer> savedScores = new ArrayList<>();

    private double difficulty;
    private int position,positionOfGame, numPlayers, difference;
    private List<Integer> playerScores = new ArrayList<>();
    private String activityType;
    public static final int CAMERA_PERM_CODE = 101, CAMERA_REQ = 100;
    private boolean fromCamera = false;
    private MyAdapter adapter;
    private ListView list;
    private TextView level, nextLevel;
    private ImageView achievement, cameraPic;
    private AnimationSet animationSet;
    private Bitmap imgTaken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_score);

        gameConfigManager = GameConfigManager.getInstance();
        extractDataFromIntent();
        populateListView();
        setTitle(getString(R.string.enter_player_score_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUpAddPlayerButton();
        setUpMinusPlayerButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setBackground();

        if (fromCamera) {
            generateAndShowAlertBox();
        }
    }

    public static Intent makeIntent(Context context, int numPlayers, int position,int positionOfGame,double difficulty,String activityType){
        Intent intent = new Intent(context,PlayerScoreActivity.class);
        intent.putExtra(NUM_PLAYERS, numPlayers);
        intent.putExtra(LIST_POSITION,position);
        intent.putExtra(GAME_LIST_POSITION,positionOfGame);
        intent.putExtra(DIFFICULTY,difficulty);
        intent.putExtra(ACTIVITY_TYPE,activityType);
        return intent;
    }

    private void extractDataFromIntent() {
        Intent intent = getIntent();
        numPlayers = intent.getIntExtra(NUM_PLAYERS,0);
        position = intent.getIntExtra(LIST_POSITION,0);
        difficulty = intent.getDoubleExtra(DIFFICULTY,0);
        positionOfGame = intent.getIntExtra(GAME_LIST_POSITION,0);
        activityType = intent.getStringExtra(ACTIVITY_TYPE);
    }

    @SuppressLint({"ResourceAsColor", "ResourceType"})
    public void setBackground() {
        int color = gameConfigManager.getThemeColor();
        getWindow().getDecorView().setBackground(ContextCompat.getDrawable(this, color));
    }

    // Shows toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.player_score_toolbar, menu);
        return true;
    }

    // Handles Toolbar button selections
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                intent.putExtra("keyName", "pressed back");
                setResult(RESULT_OK, intent);
                finish();
                onBackPressed();
                break;
            case R.id.action_submit:
                int combinedScore = getCombinedScore();
                if (combinedScore < 0){
                    Toast.makeText(PlayerScoreActivity.this, R.string.toast_fill_all_scores, Toast.LENGTH_LONG).show();
                } else {
                    hideSoftKeyboard(PlayerScoreActivity.this);
                    savedScores.clear();
                    // launch camera if activity is adding gameplay
                    if (activityType.equals(ADD_GAMEPLAY)) {
                        launchCamera();
                        gameConfigManager.getGameConfig(position).addGamePlay(numPlayers, combinedScore, difficulty, playerScores);
                        fromCamera = true;
                    } else {
                        gameConfigManager.getGameConfig(position).editPlayerScoresAndAchieve(positionOfGame,playerScores);
                        gameConfigManager.getGameConfig(position).editGameDifficultyAndAchieve(positionOfGame,difficulty);
                        generateAndShowAlertBox();
                    }
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    // adapter for the list of edit texts in this activity
    public class MyAdapter extends BaseAdapter {
        private ArrayList<ListItem> myItems;
        private Context context;

        public MyAdapter(Context context) {
            this.context = context;
            myItems = new ArrayList<ListItem>();
            // fill the list accordingly when the activity is launched, if adding creating empty list
            // if editing create list with values of last game
            if (activityType.equals(ADD_GAMEPLAY)) {
                try {
                    for (int i = 0; i < numPlayers; i++) {
                        ListItem listItem = new ListItem();
                        listItem.caption = "";
                        myItems.add(listItem);
                        savedScores.add(EMPTY);
                    }
                    notifyDataSetChanged();
                } catch (NullPointerException npe) {
                    Toast.makeText(PlayerScoreActivity.this, getString(R.string.null_ptr_exception), Toast.LENGTH_LONG).show();
                }
            } else {
                try {
                    playerScores = gameConfigManager.getGameConfig(position).getGamePlay(positionOfGame).getPlayerScores();
                    for (int i = 0; i < numPlayers; i++) {
                        if (i < playerScores.size()) {
                            ListItem listItem = new ListItem();
                            savedScores.add(playerScores.get(i));
                            listItem.caption = String.valueOf(playerScores.get(i));
                            myItems.add(listItem);
                        } else {
                            ListItem listItem = new ListItem();
                            listItem.caption = "";
                            myItems.add(listItem);
                            savedScores.add(EMPTY);
                        }
                    }
                    notifyDataSetChanged();
                } catch (NullPointerException npe) {
                    Toast.makeText(PlayerScoreActivity.this, getString(R.string.null_ptr_exception), Toast.LENGTH_LONG).show();
                }
            }
        }

        // functions used to interact with adapter from outside
        public int getCount() {
            return myItems.size();
        }

        public Object getItem(int position) {
            return myItems.get(position);
        }

        public long getItemId(int position) {
            return position;
        }


        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.the_individual_scores, null, false);
                holder.etCaption = (EditText) convertView
                        .findViewById(R.id.etPlayerScore);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if(position < myItems.size()){
                holder.etCaption.setText(myItems.get(position).caption);
            }
            holder.etCaption.setTag(position);
            holder.etCaption.setFilters(new InputFilter[] {
                    new InputFilter.LengthFilter(MAX_CHARS)
            });


            // text watcher to update saved scores
            holder.etCaption.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(position < myItems.size()){
                        // Deal with empty edit text
                        if (myItems.get(position).caption.equals("")){
                            savedScores.set(position, EMPTY);
                        } else {
                            savedScores.set(position, Integer.valueOf(myItems.get(position).caption));
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            // focus change listener so the values in the list are not lost when scrolling
            holder.etCaption.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus){
                        final int position = (Integer)v.getTag();
                        final EditText Caption = (EditText) v;
                        myItems.get(position).caption = Caption.getText().toString();
                    }
                }
            });
            return convertView;
        }
    }

    //objects used within adapter
    class ViewHolder {
        EditText etCaption;
    }

    class ListItem {
        String caption;
    }


    private void populateListView(){
        adapter = new MyAdapter(this);
        // configure list view
        list = findViewById(R.id.addGameplayListView);
        list.setAdapter(adapter);
    }

    private void updateListView(String action){
        if (Objects.equals(action, ADD_PLAYER)){
            ListItem listItem = new ListItem();
            if (adapter.myItems.size() < savedScores.size()){
                // Saved edit text was empty
                if (savedScores.get(adapter.myItems.size()) == EMPTY) {
                    listItem.caption = getString(R.string.zero);
                } else {
                    listItem.caption = String.valueOf(savedScores.get(adapter.myItems.size()));
                }
            } else {
                listItem.caption = getString(R.string.zero);
                savedScores.add(0);
            }
            adapter.notifyDataSetChanged();
            adapter.myItems.add(listItem);
        }

        if (Objects.equals(action, MINUS_PLAYER)){
            try {
                int size = adapter.myItems.size();
                // change focus to safely remove the edit text
                findViewById(R.id.addGameplayListView).requestFocus();
                adapter.myItems.remove(size - 1);
                adapter.notifyDataSetChanged();
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    // Returns name of the next achievement to the earned one
    private String getNextAchieve(String achieved, String[] achieves) {
        for (int i = 0; i < achieves.length - 1; i++) {
            if (achieves[i].equals(achieved)) {
                return achieves[i+1];
            }
        }
        return achieves[achieves.length - 1];
    }

    // Returns the difference in earned achievement and the next achievement
    private int getDifferenceInAchieve() {
        int difference = 0;
        int scoreAchieved = gameConfigManager.getGameConfig(position).getAllGamesPlayed().get(positionOfGame).getCombScore();
        int numPlayers = gameConfigManager.getGameConfig(position).getAllGamesPlayed().get(positionOfGame).getNumPlayers();
        double difficulty = gameConfigManager.getGameConfig(position).getAllGamesPlayed().get(positionOfGame).getDifficulty();
        for (int a: gameConfigManager.getGameConfig(position).calculateAchievementMins(numPlayers, difficulty)) {
            if (a > scoreAchieved) {
                return a - scoreAchieved;
            }
        }
        return difference;
    }

    // Hides keyboard
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    // Creates animations for achievement page
    private void createAnimation() {
        Animation alphaAnim = AnimationUtils.loadAnimation(this, R.anim.alpha_anim);
        Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce_anim);
        animationSet = new AnimationSet(false);

        animationSet.addAnimation(alphaAnim);
        animationSet.addAnimation(bounceAnim);
    }

    // Updates and saves the theme after it is changed from achievement page
    @SuppressLint({"SetTextI18n", "StringFormatMatches"})
    private void updateAllUIAfterThemeChange(int themeNum, int boardNum, Map<String, Integer> newTheme, String[] newNames) {
        Toast.makeText(PlayerScoreActivity.this, getString(R.string.applies_theme), Toast.LENGTH_SHORT).show();
        gameConfigManager.setThemeSelected(themeNum);
        gameConfigManager.setThemeColor(boardNum);
        gameConfigManager.getAllConfigs().forEach(GameConfig::updateAchieveEarnedBasedOnTheme);
        String achieved = gameConfigManager.getGameConfig(position).getAllGamesPlayed().get(positionOfGame).getAchieveEarned();
        String next = getNextAchieve(achieved, newNames);
        level.setText(getString(R.string.level_achieved, achieved));
        achievement.setImageResource(newTheme.get(achieved));
        nextLevel.setText(getString(R.string.points_away, difference, next));
    }

    private void setUpAddPlayerButton(){
        Button button = findViewById(R.id.addPlayerScoreButton);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                numPlayers++;
                updateListView(ADD_PLAYER);
            }
        });
    }

    private void setUpMinusPlayerButton(){
        Button button = findViewById(R.id.minusPlayerScoreButton);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (numPlayers > MIN_PLAYERS){
                    numPlayers--;
                    updateListView(MINUS_PLAYER);
                } else {
                    // Toast message since number of players has to be at least 1
                    Toast.makeText(PlayerScoreActivity.this, PlayerScoreActivity.this.getString(R.string.ADD_ZERO_PLAYERS_ERROR), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    // Spinner selection handler for changing themes from achievement page
    private void setSpinnerClickHandler(View view) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, themes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner themeSpinner = view.findViewById(R.id.themeSpinner);
        themeSpinner.setAdapter(adapter);
        final int curTheme = gameConfigManager.getThemeSelected();
        themeSpinner.setSelection(curTheme + 1);

        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        String[] newNamesFruit = gameConfigManager.getGameConfig(position).getAchievementFruits();
                        updateAllUIAfterThemeChange(MainActivity.FRUIT_THEME, R.drawable.pinkchecker, ConfigActivity.achievementImagesMap, newNamesFruit);
                        break;
                    case 1:
                        String[] newNamesAnimal = gameConfigManager.getGameConfig(position).getAchievementAnimals();
                        updateAllUIAfterThemeChange(MainActivity.ANIMAL_THEME, R.drawable.bluechecker, ConfigActivity.achievementImagesMapAnimals, newNamesAnimal);
                        break;
                    case 2:
                        String[] newNamesPlanet = gameConfigManager.getGameConfig(position).getAchievementPlanets();
                        updateAllUIAfterThemeChange(MainActivity.PLANET_THEME, R.drawable.blackchecker, ConfigActivity.achievementImagesMapPlanets, newNamesPlanet);
                        break;
                }
                saveManagerIntoSharedPref(gameConfigManager);
                setBackground();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    // Replay button for animation on achievement page
    private void setReplayAnimationButton(View view) {
        ImageButton replay = view.findViewById(R.id.replayAnimBtn);
        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                achievement.startAnimation(animationSet);
                MediaPlayer.create(PlayerScoreActivity.this, R.raw.achievement_sound).start();
            }
        });
    }

    @SuppressLint({"SetTextI18n", "StringFormatMatches"})
    private void generateAndShowAlertBox() {
        String achieved = gameConfigManager.getGameConfig(position).getAllGamesPlayed().get(positionOfGame).getAchieveEarned();
        String next = "";
        difference = getDifferenceInAchieve();

        // Gets name of next achievement relative to the theme
        int id = -1;
        if (gameConfigManager.getThemeSelected() == MainActivity.FRUIT_THEME) {
            id = ConfigActivity.achievementImagesMap.get(achieved);
            next = getNextAchieve(achieved, gameConfigManager.getGameConfig(position).getAchievementFruits());
        } else if (gameConfigManager.getThemeSelected() == MainActivity.ANIMAL_THEME){
            id = ConfigActivity.achievementImagesMapAnimals.get(achieved);
            next = getNextAchieve(achieved, gameConfigManager.getGameConfig(position).getAchievementAnimals());
        } else if (gameConfigManager.getThemeSelected() == MainActivity.PLANET_THEME){
            id = ConfigActivity.achievementImagesMapPlanets.get(achieved);
            next = getNextAchieve(achieved, gameConfigManager.getGameConfig(position).getAchievementPlanets());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflate = LayoutInflater.from(this);
        View view = inflate.inflate(R.layout.add_gameplay_alert, null);

        nextLevel = view.findViewById(R.id.nextAchievedTV);
        nextLevel.setText(getString(R.string.points_away,difference, next));

        level = view.findViewById(R.id.textViewDialogGamePlay);
        level.setText(getString(R.string.level_achieved, achieved));

        achievement = view.findViewById(R.id.imageViewGamePlay);
        achievement.setImageResource(id);

        cameraPic = view.findViewById(R.id.cameraGamePlayAlertIV);
        if (activityType.equals(ADD_GAMEPLAY)) {
            cameraPic.setImageResource(android.R.drawable.ic_menu_camera);
            try {  // For API < 31:
                cameraPic.setImageBitmap(Bitmap.createScaledBitmap(imgTaken, 400, 600, false));
                gameConfigManager.getGameConfig(position).getGamePlay(positionOfGame).setMemory(imgTaken);
            } catch (NullPointerException e) {}
            saveManagerIntoSharedPref(gameConfigManager); // Saves data after photo taken

        } else if (activityType.equals(EDIT_GAMEPLAY)){
            cameraPic.setVisibility(View.GONE);
            TextView captured = view.findViewById(R.id.memoryCapturedTv);
            captured.setVisibility(View.GONE);
            View divider = view.findViewById(R.id.alert_divider);
            divider.setVisibility(View.GONE);
        }

        createAnimation();
        setSpinnerClickHandler(view);
        setReplayAnimationButton(view);

        builder.setCancelable(false)
                .setView(view)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });

        builder.show();
        achievement.startAnimation(animationSet);
        // Add sound
        MediaPlayer.create(this, R.raw.achievement_sound).start();

    }



    // Launches camera app given permission is granted
    private void launchCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camera, CAMERA_REQ);
        }
    }

    private int getCombinedScore(){
        int combinedScore = 0;
        if (activityType.equals(ADD_GAMEPLAY)) {
            try {
                for (int i = 0; i < numPlayers; i++) {
                    Object obj = list.getAdapter().getItem(i);
                    ListItem listItem = (ListItem)obj;
                    int value = Integer.parseInt(listItem.caption);
                    combinedScore += value;
                    playerScores.add(value);
                }
                return combinedScore;
            } catch (NumberFormatException exception) {
                playerScores.clear();
                return -1;
            }

            // If editing a gameplay, gets combined score based on current values in editText
        } else {
            try {
                int i;
                for (i = 0; i < numPlayers; i++) {
                    Object obj = list.getAdapter().getItem(i);
                    ListItem listItem = (ListItem)obj;
                    int value = Integer.parseInt(listItem.caption);
                    if (i<playerScores.size()){
                        playerScores.set(i,value);
                    } else {
                        playerScores.add(value);
                    }
                    combinedScore += value;
                }
                for (int j=i;i<playerScores.size();j++){
                    playerScores.remove(playerScores.size() - 1);
                }
                return combinedScore;
            } catch (NumberFormatException exception) {
                playerScores.clear();
                return -1;
            }
        }
    }

    // Handles user's answer to grant permission for camera app
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.camera_granted), Toast.LENGTH_SHORT).show();
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera, CAMERA_REQ);
            } else {
                Toast.makeText(this, getString(R.string.request_camera_perm), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Saves the image taken by the camera as soon as user clicks check icon
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQ && resultCode == Activity.RESULT_OK) {
            imgTaken = (Bitmap) data.getExtras().get("data");
            try {
                cameraPic.setImageBitmap(Bitmap.createScaledBitmap(imgTaken, 400, 600, false));
            } catch (NullPointerException e) {}
            MediaStore.Images.Media.insertImage(getContentResolver(), imgTaken, getString(R.string.gameplay_img_save_name, positionOfGame) , "");  // Saves the image.
            gameConfigManager.getGameConfig(position).getGamePlay(positionOfGame).setMemory(imgTaken);
            saveManagerIntoSharedPref(gameConfigManager);
        }
    }

    private void saveManagerIntoSharedPref(GameConfigManager gameConfigManager){
        SharedPreferences prefs = this.getSharedPreferences(GAME_CONFIG_PREF,MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(gameConfigManager);
        editor.putString(GAME_CONFIG_MANAGER,json);
        editor.apply();
    }

}