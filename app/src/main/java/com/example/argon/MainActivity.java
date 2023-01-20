// main activity used as the starting screen which displays all current game configurations and allows users
// to add more or interact with currently existing ones
package com.example.argon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import com.example.argon.Model.GameConfig;
import com.example.argon.Model.GameConfigManager;
import com.google.gson.Gson;

import java.util.ArrayList;
/**
 * @since 11-06-22
 * Activity class for main activity screen which shows the all the game configurations in a listview */

public class MainActivity extends AppCompatActivity {
    public static final int LIST_DIVIDER = 20;
    public static final int FRUIT_THEME = -1;
    public static final int ANIMAL_THEME = 0;
    public static final int PLANET_THEME = 1;
    public static final String GAME_CONFIG_MANAGER = "game config manager";
    public static final String GAME_CONFIG_PREF = "game config pref";
    public static final String VIEW_CONFIG = "view config";
    public static final String ADD_CONFIG = "add config";
    private GameConfigManager gameConfigManager;

    ArrayAdapter<GameConfig> adapter;
    Toolbar toolbar;
    ListView list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.game_configurations);

        gameConfigManager = getManagerFromSharedPref(this);
        GameConfigManager.setInstance(gameConfigManager);
        gameConfigManager = GameConfigManager.getInstance();
        adapter = new MyAdapter((ArrayList<GameConfig>) gameConfigManager.getAllConfigs());
        setUpAddConfigButton();
        registerClickCallback();
        populateListView();
    }
    @Override
    protected void onStart() {
        super.onStart();
        adapter.notifyDataSetChanged();
        gameConfigManager.getAllConfigs().forEach(GameConfig::updateAchieveEarnedBasedOnTheme);
        saveManagerIntoSharedPref(gameConfigManager);
        setBackground();
        setUpAddConfigButton();
        setListHeight();
    }


    @SuppressLint({"ResourceAsColor", "ResourceType"})
    private void setBackground() {
        if (gameConfigManager.getThemeSelected() == PLANET_THEME) {
            getWindow().getDecorView().setBackground(ContextCompat.getDrawable(this, R.drawable.blackchecker));
            gameConfigManager.setThemeColor(R.drawable.blackchecker);
        } else if (gameConfigManager.getThemeSelected() == FRUIT_THEME) {
            getWindow().getDecorView().setBackground(ContextCompat.getDrawable(this, R.drawable.pinkchecker));
            gameConfigManager.setThemeColor(R.drawable.pinkchecker);
        } else if (gameConfigManager.getThemeSelected() == ANIMAL_THEME) {
            getWindow().getDecorView().setBackground(ContextCompat.getDrawable(this, R.drawable.bluechecker));
            gameConfigManager.setThemeColor(R.drawable.bluechecker);
        }
        list.setDivider(new ColorDrawable(android.R.color.transparent));
        list.setDividerHeight(LIST_DIVIDER);
    }

    private void setListHeight() {
        ViewGroup.LayoutParams params = list.getLayoutParams();
        if (gameConfigManager.getSize() == 0) {
            emptyStateGameConfigAlertDialog();
            params.height = 2000;
        } else {
            params.height = 0;
        }
        list.setLayoutParams(params);
        list.requestLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_themes, menu);
        return true;
    }

    @SuppressLint({"ShowToast", "NonConstantResourceId"})
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                Intent intent = AboutActivity.makeIntent(MainActivity.this);
                startActivity(intent);
                break;
            case R.id.fruit_theme_option:
                Toast.makeText(MainActivity.this, getString(R.string.applied_theme, getString(R.string.fruit)), Toast.LENGTH_LONG).show();
                gameConfigManager.setThemeSelected(FRUIT_THEME);
                break;
            case R.id.animal_theme_option:
                Toast.makeText(MainActivity.this, getString(R.string.applied_theme, getString(R.string.animal)), Toast.LENGTH_LONG).show();
                gameConfigManager.setThemeSelected(ANIMAL_THEME);
                break;
            case R.id.planet_theme_option:
                Toast.makeText(MainActivity.this, getString(R.string.applied_theme, getString(R.string.planet)), Toast.LENGTH_LONG).show();
                gameConfigManager.setThemeSelected(PLANET_THEME);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        setBackground();
        setUpAddConfigButton();
        saveManagerIntoSharedPref(gameConfigManager);
        gameConfigManager.getAllConfigs().forEach(GameConfig::updateAchieveEarnedBasedOnTheme);

        return true;
    }

    // get saved game from shared preferences
    public static GameConfigManager getManagerFromSharedPref(Context context){
        SharedPreferences prefs = context.getSharedPreferences(GAME_CONFIG_PREF,MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(GAME_CONFIG_MANAGER,"");
        GameConfigManager configManager =  gson.fromJson(json,GameConfigManager.class);
        return configManager;
    }

    private void saveManagerIntoSharedPref(GameConfigManager gameConfigManager){
        SharedPreferences prefs = this.getSharedPreferences(GAME_CONFIG_PREF,MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(gameConfigManager);
        editor.putString(GAME_CONFIG_MANAGER,json);
        editor.apply();
    }


    // repopulate listview with gameConfigManager contents everytime this activity is started
    private void populateListView(){
        adapter = new MyAdapter((ArrayList<GameConfig>) gameConfigManager.getAllConfigs());
        // configure list view
        list = findViewById(R.id.listViewConfigs);
        setListHeight();
        //empty state
        View emptyView = getLayoutInflater().inflate(R.layout.config_list_emptystate, null);
        addContentView(emptyView, list.getLayoutParams());
        list.setEmptyView(emptyView);

        list.setAdapter(adapter);

    }

    private class MyAdapter extends ArrayAdapter<GameConfig> {
        public MyAdapter(ArrayList<GameConfig> games) {
            super(MainActivity.this, R.layout.item_list_main_menu, games);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.item_list_main_menu, parent, false);
            }

            TextView name = itemView.findViewById(R.id.tvConfigName);
            name.setText(gameConfigManager.getGameConfig(position).getName());

            TextView size = itemView.findViewById(R.id.tvConfigSize);
            size.setText(getString(R.string.menu_games_played,gameConfigManager.getGameConfig(position).getAllGamesPlayed().size()));

            ImageView image = itemView.findViewById(R.id.config_image_Iv);
            Bitmap taken = gameConfigManager.getGameConfig(position).getBoardImg();
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), taken);
            final float corner =  (float) taken.getWidth() * 0.12f;
            roundedBitmapDrawable.setCornerRadius(corner);
            image.setImageDrawable(roundedBitmapDrawable);

            return itemView;
        }
    }

    // Create an alert dialog when there are no game configs added
    private void emptyStateGameConfigAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_menu_empty_title)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .setMessage(R.string.alert_menu_empty_message)
                .setIcon(R.drawable.android_img)
                .show();
    }

    // click on a specific game to edit it
    private void registerClickCallback(){
        ListView list = findViewById(R.id.listViewConfigs);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = ConfigActivity.makeIntent(MainActivity.this, VIEW_CONFIG,position);
                startActivity(intent);
            }
        });
    }

    //Press image button to add game config
    private void setUpAddConfigButton(){
        ImageButton addConfigButton = findViewById(R.id.addConfigButton);
        // Set button to current theme
        switch(gameConfigManager.getThemeSelected()) {
            case ANIMAL_THEME:
                addConfigButton.setImageResource(R.drawable.animal_button_image);
                addConfigButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
                break;
            case PLANET_THEME:
                addConfigButton.setImageResource(R.drawable.planet_button_image);
                addConfigButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
                break;
            default: // FRUIT_THEME:
                addConfigButton.setImageResource(R.drawable.fruit_button_image);
                addConfigButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.pink)));
                break;
        }
        addConfigButton.setOnClickListener(v -> {
            playThemeSound();
            Intent intent = GameActivity.makeIntent(MainActivity.this, ADD_CONFIG,0,0);
            startActivity(intent);
        });
    }

    // Play a sound related to the theme when button is pressed
    private void playThemeSound() {
        switch(gameConfigManager.getThemeSelected()) {
            case ANIMAL_THEME:
                MediaPlayer.create(this,R.raw.animal_sound).start();
                break;
            case PLANET_THEME:
                MediaPlayer.create(this,R.raw.planet_sound).start();
                break;
            default: // FRUIT_THEME
                MediaPlayer.create(this,R.raw.fruit_sound).start();
        }
    }
}