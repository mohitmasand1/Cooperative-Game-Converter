//game activity used as a screen for adding/editing game plays or game configurations
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
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.example.argon.Model.GameConfig;
import com.example.argon.Model.GameConfigManager;
import com.example.argon.Model.GamePlay;
import com.google.gson.Gson;

/**
 * @since 11-06-22
 * Activity class displaying game activity. Contains methods on displaying UI for
 * when add gameplay, edit game config, and add game config buttons are clicked */

public class GameActivity extends AppCompatActivity {
    private static final double EASY = 0.75;
    private static final double NORMAL = 1.00;
    private static final double HARD = 1.25;
    private static final String EDIT_CONFIG = "edit config";
    private static final String ADD_GAMEPLAY = "add gameplay";
    public static final String ADD_CONFIG = "add config";
    public static final String ACTIVITY_TYPE = "activity type";
    public static final String LIST_POSITION = "list position";
    private static final int PLAYER_SCORE_ACTIVITY_BACK = 0;
    private static final String EDIT_GAMEPLAY = "edit gameplay";
    private static final String GAME_LIST_POSITION = "game list position";
    private GameConfigManager gameConfigManager;
    private EditText etGameName, etNumPlayers,etBadScore,etGreatScore;
    private TextView poorScoreText,goodScoreText,numOfPlayersText,gameConfigurationText, difficultyText;
    private Button completeActionButton;
    Toolbar toolbar;
    private String gameName, activityType;
    private RadioGroup difficultyRadioGroup;
    private int numPlayers,badScore,greatScore,position,positionGame;
    public static final int CAMERA_REQ = 200;
    public static Bitmap imgTaken, firstImg;
    public static ImageView memory;
    private boolean added = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        extractDataFromIntent();
        gameConfigManager = getManagerFromSharedPref(this);

        toolbar = findViewById(R.id.toolbarEditGame);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Add back button

        completeActionButton = findViewById(R.id.completeActivityButton);
        gameConfigManager = gameConfigManager.getInstance();
        initializeScreen();
        setPreSetBoardImage();
        setupAddButton();
        setupScreen();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setBackground();
    }

    public static Intent makeIntent(Context context,String type, int position,int positionGame){
        Intent intent = new Intent(context,GameActivity.class);
        intent.putExtra(ACTIVITY_TYPE,type);
        intent.putExtra(LIST_POSITION,position);
        intent.putExtra(GAME_LIST_POSITION,positionGame);
        return intent;
    }

    private void extractDataFromIntent() {
        Intent intent = getIntent();
        activityType = intent.getStringExtra(ACTIVITY_TYPE);
        position = intent.getIntExtra(LIST_POSITION,0);
        positionGame = intent.getIntExtra(GAME_LIST_POSITION,0);
    }

    @SuppressLint({"ResourceAsColor", "ResourceType"})
    public void setBackground() {
        int color = gameConfigManager.getThemeColor();
        getWindow().getDecorView().setBackground(ContextCompat.getDrawable(this, color));
    }

    public static GameConfigManager getManagerFromSharedPref(Context context){
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.GAME_CONFIG_PREF,MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(MainActivity.GAME_CONFIG_MANAGER,"");
        GameConfigManager configManager =  gson.fromJson(json,GameConfigManager.class);
        return configManager;
    }

    private void saveManagerIntoSharedPref(GameConfigManager gameConfigManager){
        SharedPreferences prefs = this.getSharedPreferences(MainActivity.GAME_CONFIG_PREF,MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(gameConfigManager);
        editor.putString(MainActivity.GAME_CONFIG_MANAGER,json);
        editor.apply();
    }

    // Shows toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_gameplay_toolbar, menu);
        extractDataFromIntent();
        if (activityType.equals(ADD_GAMEPLAY)) {
            menu.getItem(0).setVisible(false);
        }
        return true;
    }

    // Handles Toolbar button selections
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.action_photo){
            generateAndShowMemoryHandler();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void setPreSetBoardImage() {
        if (activityType.equals(ADD_CONFIG)){
            if (!added) {
                firstImg = BitmapFactory.decodeResource(getResources(), R.drawable.generic_game);
            }
        }
    }

    // Alert dialog for changing the memory image
    @SuppressLint({"ResourceType", "SetTextI18n"})
    private void generateAndShowMemoryHandler() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflate = LayoutInflater.from(this);
        View view = inflate.inflate(R.layout.view_memory_alert, null);

        TextView title = view.findViewById(R.id.edit_memory_titleTv);
        Button retake = view.findViewById(R.id.reTakeMemoryBtn);
        if (added) {
            retake.setText(getString(R.string.retake_img));
        } else {
            retake.setText(getString(R.string.take_img));
        }

        memory = view.findViewById(R.id.IV_memory);
        if (activityType.equals(EDIT_GAMEPLAY)) {
            retake.setText(getString(R.string.retake_img));
            Bitmap img = gameConfigManager.getGameConfig(position).getGamePlay(positionGame).getMemory();
            memory.setImageBitmap(img);
            title.setText(getString(R.string.memory_img));
        } else if (activityType.equals(ADD_CONFIG)){
            memory.setImageBitmap(firstImg);
            title.setText(getString(R.string.set_board_img));
        } else if (activityType.equals(EDIT_CONFIG)) {
            Bitmap img = gameConfigManager.getGameConfig(position).getBoardImg();
            memory.setImageBitmap(img);
            title.setText(getString(R.string.your_board_img));
        }

        retake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        builder.setCancelable(false)
                .setView(view)
                .setNeutralButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        builder.show();
    }

    private void setupScreen(){
        if (activityType.equals(EDIT_CONFIG)) {
            toolbar.setTitle(getString(R.string.edit_config_title));

            etGameName.setVisibility(View.VISIBLE);
            etBadScore.setVisibility(View.VISIBLE);
            etGreatScore.setVisibility(View.VISIBLE);

            etNumPlayers.setVisibility(View.INVISIBLE);

            GameConfig config = gameConfigManager.getGameConfig(position);

            etGameName.setText(String.valueOf(config.getName()));
            etBadScore.setText(String.valueOf(config.getPoorScore()));
            etGreatScore.setText(String.valueOf(config.getGreatScore()));

            completeActionButton.setText(R.string.finish_editing);

            gameConfigurationText.setText(R.string.game_configuration_name);

            poorScoreText.setVisibility(View.VISIBLE);
            goodScoreText.setVisibility(View.VISIBLE);
            numOfPlayersText.setVisibility(View.INVISIBLE);

            difficultyText.setVisibility(View.INVISIBLE);
            difficultyRadioGroup.setVisibility(View.INVISIBLE);

        } else if (activityType.equals(ADD_GAMEPLAY) || activityType.equals(EDIT_GAMEPLAY)) {
            if (activityType.equals(ADD_GAMEPLAY)) {
                toolbar.setTitle(getString(R.string.add_gameplay_title));
                completeActionButton.setText(R.string.add_gameplay);

            } else {
                toolbar.setTitle(getString(R.string.edit_gameplay_title));
                completeActionButton.setText(R.string.edit_gameplay_button);
                GamePlay curGamePlay = gameConfigManager.getGameConfig(position).getGamePlay(positionGame);
                setUpRadioButtonDifficulty(curGamePlay);
                int curGamePlayNumPlayers = curGamePlay.getNumPlayers();
                if (curGamePlayNumPlayers != 0){
                    etNumPlayers.setText(String.valueOf(curGamePlayNumPlayers));
                }
            }
            etGameName.setVisibility(View.INVISIBLE);
            etBadScore.setVisibility(View.INVISIBLE);
            etGreatScore.setVisibility(View.INVISIBLE);
            etNumPlayers.setVisibility(View.VISIBLE);

            poorScoreText.setVisibility(View.INVISIBLE);
            goodScoreText.setVisibility(View.INVISIBLE);

            gameConfigurationText.setText(gameConfigManager.getGameConfig(position).getName());

            numOfPlayersText.setVisibility(View.VISIBLE);

            difficultyText.setVisibility(View.VISIBLE);
            difficultyRadioGroup.setVisibility(View.VISIBLE);

        } else {
            toolbar.setTitle(getString(R.string.add_configuration_title));

            etGameName.setVisibility(View.VISIBLE);
            etBadScore.setVisibility(View.VISIBLE);
            etGreatScore.setVisibility(View.VISIBLE);

            etNumPlayers.setVisibility(View.INVISIBLE);

            completeActionButton.setText(R.string.add_configuration);

            gameConfigurationText.setText(R.string.game_configuration_name);

            poorScoreText.setVisibility(View.VISIBLE);
            goodScoreText.setVisibility(View.VISIBLE);
            numOfPlayersText.setVisibility(View.INVISIBLE);

            difficultyText.setVisibility(View.INVISIBLE);
            difficultyRadioGroup.setVisibility(View.INVISIBLE);
        }
    }

    private boolean allInputsFull(EditText one, EditText two, EditText three) {
        return (!one.getText().toString().isEmpty() &&
                !two.getText().toString().isEmpty() &&
                !three.getText().toString().isEmpty());
    }

    private static int textToInt(TextView text){
        return Integer.parseInt(text.getText().toString());
    }

    private void addGame(){
        // Displays UI for when adding a configuration
        if (activityType.equals(ADD_CONFIG) && allInputsFull(etGameName, etBadScore, etGreatScore)) {
            badScore = textToInt(etBadScore);
            greatScore = textToInt(etGreatScore);
            if (badScore >= greatScore) {
                Toast.makeText(GameActivity.this, R.string.invalid_score_inputs, Toast.LENGTH_LONG).show();
            } else {
                gameName = etGameName.getText().toString();
                gameConfigManager.addGameConfig(gameName, badScore, greatScore);
                int size = gameConfigManager.getAllConfigs().size();
                gameConfigManager.getGameConfig(size - 1).setBoardImg(firstImg);

                Toast.makeText(GameActivity.this, R.string.config_added, Toast.LENGTH_SHORT).show();
                finish();
            }
            // Displays UI for when adding a gameplay
        } else if (activityType.equals(ADD_GAMEPLAY) && !etNumPlayers.getText().toString().isEmpty()) {
            numPlayers = textToInt(etNumPlayers);
            if (numPlayers == 0) {
                Toast.makeText(GameActivity.this, R.string.ADD_ZERO_PLAYERS_ERROR, Toast.LENGTH_LONG).show();
            } else {
                double difficulty = getDifficultyFromRadioButtons();
                int size = gameConfigManager.getGameConfig(position).getAllGamesPlayed().size();
                Intent intent = PlayerScoreActivity.makeIntent(GameActivity.this,numPlayers,position,size,difficulty,ADD_GAMEPLAY);
                startActivityForResult(intent, PLAYER_SCORE_ACTIVITY_BACK);
            }
            // Displays UI for when editing a gameplay
        } else if (activityType.equals(EDIT_GAMEPLAY) && !etNumPlayers.getText().toString().isEmpty()){
            numPlayers = textToInt(etNumPlayers);
            if (numPlayers == 0) {
                Toast.makeText(GameActivity.this, R.string.ADD_ZERO_PLAYERS_ERROR, Toast.LENGTH_LONG).show();
            } else {
                double difficulty = getDifficultyFromRadioButtons();
                Intent intent = PlayerScoreActivity.makeIntent(GameActivity.this,numPlayers,position,positionGame,difficulty,EDIT_GAMEPLAY);
                startActivityForResult(intent, PLAYER_SCORE_ACTIVITY_BACK);
            }
        }
        // Displays UI for when editing a configuration
        else if (activityType.equals(EDIT_CONFIG) && allInputsFull(etGameName, etBadScore, etGreatScore)) {
            badScore = textToInt(etBadScore);
            greatScore = textToInt(etGreatScore);
            if (badScore >= greatScore) {
                Toast.makeText(GameActivity.this, R.string.invalid_score_inputs, Toast.LENGTH_LONG).show();
            } else {
                gameName = etGameName.getText().toString();
                gameConfigManager.editGameConfigName(position, gameName);
                gameConfigManager.editGameConfigGreatScore(position, greatScore);
                gameConfigManager.editGameConfigPoorScore(position, badScore);
                gameConfigManager.getGameConfig(position).updateAllGamePlaysAchievementEarned();
                Toast.makeText(GameActivity.this, R.string.config_edited, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(GameActivity.this, R.string.incomplete_inputs, Toast.LENGTH_LONG).show();
        }
    }

    // This method is called when the second activity finishes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check that it is the SecondActivity with an OK result
        if (requestCode == PLAYER_SCORE_ACTIVITY_BACK) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(GameActivity.this, getString(R.string.reenter_params), Toast.LENGTH_SHORT).show();
            }
            else{
                finish();
            }
        }
        else if (requestCode == CAMERA_REQ && resultCode == Activity.RESULT_OK) {
            if (activityType.equals(EDIT_GAMEPLAY)) {
                imgTaken = (Bitmap) data.getExtras().get("data");
                memory.setImageBitmap(imgTaken);
                MediaStore.Images.Media.insertImage(getContentResolver(), GameActivity.imgTaken, getString(R.string.gameplay_img_save_name, positionGame), "");  // Saves the image.
                gameConfigManager.getGameConfig(position).getGamePlay(positionGame).setMemory(imgTaken);
                saveManagerIntoSharedPref(gameConfigManager);
            } else if (activityType.equals(ADD_CONFIG) || activityType.equals(EDIT_CONFIG)) {
                added = true;
                imgTaken = (Bitmap) data.getExtras().get("data");
                firstImg = imgTaken;
                memory.setImageBitmap(imgTaken);
                if (activityType.equals(EDIT_CONFIG)) {
                    gameConfigManager.getGameConfig(position).setBoardImg(imgTaken);
                }
                saveManagerIntoSharedPref(gameConfigManager);
            }
        }
    }

    private void setupAddButton(){
        Button addGameBtn = findViewById(R.id.completeActivityButton);
        addGameBtn.setOnClickListener(v -> {
            addGame();
        });
    }

    private void initializeScreen(){
        poorScoreText = findViewById(R.id.poorScoreText);
        goodScoreText = findViewById(R.id.goodScoreText);
        numOfPlayersText = findViewById(R.id.numPlayersText);
        gameConfigurationText = findViewById(R.id.gameConfigurationText);
        difficultyText = findViewById(R.id.difficultyText);

        difficultyRadioGroup = findViewById(R.id.difficultyRadioGroup);

        etGameName = findViewById(R.id.gameName);
        etNumPlayers = findViewById(R.id.numPlayers);
        etBadScore = findViewById(R.id.poorScore);
        etGreatScore= findViewById(R.id.goodScore);

    }

    private double getDifficultyFromRadioButtons(){
        RadioButton[] radioButtonList = new RadioButton[]{
                findViewById(R.id.easyRadioButton),
                findViewById(R.id.normalRadioButton),
                findViewById(R.id.hardRadioButton)
        };

        if(radioButtonList[0].isChecked()){
            return EASY;
        } else if (radioButtonList[2].isChecked()){
            return HARD;
        } else{
            return NORMAL;
        }
    }

    private void setUpRadioButtonDifficulty(GamePlay curGameplay){
        RadioButton[] radioButtonList = new RadioButton[]{
                findViewById(R.id.easyRadioButton),
                findViewById(R.id.normalRadioButton),
                findViewById(R.id.hardRadioButton)
        };

        if(curGameplay.getDifficulty() == EASY){
            radioButtonList[0].setChecked(true);
        } else if (curGameplay.getDifficulty() == HARD){
            radioButtonList[2].setChecked(true);
        } else{
            radioButtonList[1].setChecked(true);
        }
    }

    private void launchCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PlayerScoreActivity.CAMERA_PERM_CODE);
        } else {
            Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camera, CAMERA_REQ);
        }
    }

}