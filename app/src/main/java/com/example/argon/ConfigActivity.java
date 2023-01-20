// Config activity used for adding/viewing game plays in a configuration
// also used to see achievement levels for a configuration and to edit the configuration

package com.example.argon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.example.argon.Model.GameConfigManager;
import com.example.argon.Model.GamePlay;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 11-06-22
 * Activity class displaying configuration activity. Contains methods pertaining to the
 * game play UI to display editing, deleting, and listing of game plays in a configuration*/

public class ConfigActivity extends AppCompatActivity {
    private static final String ACTIVITY_TYPE = "activity type";
    private static final String LIST_POSITION = "list position";
    private static final String GAME_CONFIG_MANAGER = "game config manager";
    private static final String GAME_CONFIG_PREF = "game config pref";
    private static final String ADD_GAMEPLAY = "add gameplay";
    private static final String EDIT_CONFIG = "edit config";
    private static final String EDIT_GAMEPLAY = "edit gameplay";
    private GameConfigManager gameConfigManager;
    private static int position;
    private String activityType;
    private List<GamePlay> gamePlays;
    private Toolbar toolbar;
    private ArrayAdapter<GamePlay> adapter;
    ListView list;

    // associate achievement level with achievement image
    public static final Map<String, Integer> achievementImagesMap = new HashMap<String, Integer>(){
        {
            put("Blasphemous Bananas", R.drawable.banana);
            put("Pompous Plums", R.drawable.plum);
            put("Terrible Tomatoes", R.drawable.tomato);
            put("Mediocre Mangoes", R.drawable.mango);
            put("Mid Melons", R.drawable.melon);
            put("Swaggy Strawberries", R.drawable.strawberry);
            put("Powerful Persimmons", R.drawable.persimmon);
            put("Epic Eggplants", R.drawable.eggplant);
            put("Great Grapes", R.drawable.grapes);
            put("Awesome Apples", R.drawable.apple);
        }
    };

    public static final Map<String, Integer> achievementImagesMapAnimals = new HashMap<>() {
        {
            put("Bad Bears", R.drawable.bear);
            put("Pompous Pigs", R.drawable.pig);
            put("Terrible Tigers", R.drawable.tiger);
            put("Mediocre Men", R.drawable.man);
            put("Mid Monkeys", R.drawable.monkey);
            put("Swaggy Snake", R.drawable.snake);
            put("Powerful Pandas", R.drawable.panda);
            put("Epic Elephants", R.drawable.elephant);
            put("Great Giraffes", R.drawable.giraffe);
            put("Awesome Anteaters", R.drawable.anteater);
        }
    };

    public static final Map<String, Integer> achievementImagesMapPlanets = new HashMap<>() {
        {
            put("Poopy Pluto", R.drawable.pluto);
            put("Nerdy Neptune", R.drawable.neptune);
            put("Ugly Uranus", R.drawable.uranus);
            put("Sad Saturn", R.drawable.saturn);
            put("Junky Jupiter", R.drawable.jupiter);
            put("Magical Mars", R.drawable.mars);
            put("Epic Earth", R.drawable.earth);
            put("Vital Venus", R.drawable.venus);
            put("Marvelous Mercury", R.drawable.mercury);
            put("Super Sun", R.drawable.sun);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        extractDataFromIntent();
        gameConfigManager = GameConfigManager.getInstance();
        toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.no_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Add back button
        setUpAddGameButton();
        setUpEditGameButton();
        registerClickCallback();
        populateListView();
    }

    @Override
    protected void onStart(){
        super.onStart();
        adapter.notifyDataSetChanged();
        toolbar.setTitle(getString(R.string.config_activity_title,gameConfigManager.getGameConfig(position).getName()));
        saveManagerIntoSharedPref(gameConfigManager);
        setBackground();
        setListHeight();
    }

    public static Intent makeIntent(Context context, String type, int position){
        Intent intent = new Intent(context,ConfigActivity.class);
        intent.putExtra(ACTIVITY_TYPE,type);
        intent.putExtra(LIST_POSITION,position);
        return intent;
    }

    private void extractDataFromIntent() {
        Intent intent = getIntent();
        activityType = intent.getStringExtra(ACTIVITY_TYPE);
        position = intent.getIntExtra(LIST_POSITION,0);
    }

    @SuppressLint({"ResourceAsColor", "ResourceType"})
    public void setBackground() {
        int color = gameConfigManager.getThemeColor();
        getWindow().getDecorView().setBackground(ContextCompat.getDrawable(this, color));
        list.setDivider(new ColorDrawable(android.R.color.transparent));
        list.setDividerHeight(MainActivity.LIST_DIVIDER);
    }

    private void setListHeight() {
        ViewGroup.LayoutParams params = list.getLayoutParams();
        if (gameConfigManager.getGameConfig(position).getAllGamesPlayed().size() == 0) {
            params.height = 2000;
        } else {
            params.height = 0;
        }
        list.setLayoutParams(params);
        list.requestLayout();
    }

    private void saveManagerIntoSharedPref(GameConfigManager gameConfigManager){
        SharedPreferences prefs = this.getSharedPreferences(GAME_CONFIG_PREF,MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(gameConfigManager);
        editor.putString(GAME_CONFIG_MANAGER,json);
        editor.apply();
    }


    private void populateListView(){
        gamePlays = gameConfigManager.getGameConfig(position).getAllGamesPlayed();
        adapter = new gamePlayListAdapter();

        // Configure list view
        list = findViewById(R.id.listViewGamePlays);
        setListHeight();
        View emptyView = getLayoutInflater().inflate(R.layout.gameplay_list_emptystate, null);
        addContentView(emptyView, list.getLayoutParams());
        list.setEmptyView(emptyView);
        list.setAdapter(adapter);

        try{
            // Empty state
            if (gamePlays.size() == 0) {
                setupEmptyGPMsg();
            }
            else {
                adapter.notifyDataSetChanged();
            }
        }catch(NullPointerException npe){
            Toast.makeText(ConfigActivity.this, getString(R.string.null_ptr_exception), Toast.LENGTH_LONG).show();
        }
    }

    // click on a specific gameplay to edit it
    private void registerClickCallback(){
        ListView list = findViewById(R.id.listViewGamePlays);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int positionGamePlayList, long id) {
                Intent intent = GameActivity.makeIntent(ConfigActivity.this,EDIT_GAMEPLAY,position,positionGamePlayList);
                startActivity(intent);
            }
        });
    }

    private void setUpAddGameButton(){
        Button btn = findViewById(R.id.addGameplayBtn);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = GameActivity.makeIntent(ConfigActivity.this, ADD_GAMEPLAY,position,0);
                startActivity(intent);

            }
        });
    }

    private void setUpEditGameButton(){
        Button btn = findViewById(R.id.editGameConfigBtn);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = GameActivity.makeIntent(ConfigActivity.this, EDIT_CONFIG,position,0);
                startActivity(intent);
            }
        });
    }


    // Delete alert box, handles yes and no options
    private void setUpDeleteConfigButton() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirm_delete,gameConfigManager.getGameConfig(position).getName()));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
            gameConfigManager.deleteGameConfig(position);
            saveManagerIntoSharedPref(gameConfigManager);
            finish();
        });
        builder.setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog alertDelete = builder.create();
        alertDelete.show();
    }


    // shows toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.config_toolbar, menu);
        return true;
    }

    // Handles Toolbar button selections
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_delete:
                setUpDeleteConfigButton();
                break;
            case R.id.action_achieve:
                Intent intent = AchieveActivity.makeIntent(ConfigActivity.this, position);
                startActivity(intent);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void setupEmptyGPMsg(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.no_games_played_title)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .setMessage(R.string.no_games_played)
                .setIcon(R.drawable.android_img)
                .show();
    }

    private class gamePlayListAdapter extends ArrayAdapter<GamePlay> {
        // Array adapter constructor
        public gamePlayListAdapter(){
            super(ConfigActivity.this, R.layout.gameplay_list_layout, gamePlays);
        }

        // Setup info about a gameplay
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View gamePlayView = convertView;

            // Make sure there is a view to work with
            if(gamePlayView == null){
                gamePlayView = getLayoutInflater().inflate(R.layout.gameplay_list_layout,parent,false);
            }
            // Get current gameplay
            GamePlay currGamePlay = gamePlays.get(position);

            /* Set all the views */

            // Date
            TextView dateTV = gamePlayView.findViewById(R.id.dateTextView);
            dateTV.setText(currGamePlay.getTimeCreated());

            // Num players
            TextView numPlayersTV = gamePlayView.findViewById(R.id.numPlayersTextView);
            numPlayersTV.setText(getString(R.string.config_num_players,currGamePlay.getNumPlayers()));

            // Combined score
            TextView combinedScoreTV = gamePlayView.findViewById(R.id.combinedScoreTextView);
            combinedScoreTV.setText(getString(R.string.config_combined_score, currGamePlay.getCombScore()));

            // Achievement levels
            TextView achievementLevelTV = gamePlayView.findViewById(R.id.achievementLevelTextView);
            achievementLevelTV.setText(currGamePlay.getAchieveEarned());

            // Difficulty
            TextView difficultyTV = gamePlayView.findViewById(R.id.difficultyTextView);
            difficultyTV.setText(getString(R.string.config_difficulty, currGamePlay.getDifficultyAsString()));

            // Picture that goes with the achievement level
            ImageView achievementImage = gamePlayView.findViewById(R.id.achievementImageView);
            String achievement = currGamePlay.getAchieveEarned();

            try {
                if (gameConfigManager.getThemeSelected() == MainActivity.FRUIT_THEME) {
                    achievementImage.setImageResource(achievementImagesMap.get(achievement));
                } else if (gameConfigManager.getThemeSelected() == MainActivity.ANIMAL_THEME) {
                    achievementImage.setImageResource(achievementImagesMapAnimals.get(achievement));
                } else if (gameConfigManager.getThemeSelected() == MainActivity.PLANET_THEME) {
                    achievementImage.setImageResource(achievementImagesMapPlanets.get(achievement));
                }
            } catch (NullPointerException ignored) {

            }


            return gamePlayView;
        }
    }



}