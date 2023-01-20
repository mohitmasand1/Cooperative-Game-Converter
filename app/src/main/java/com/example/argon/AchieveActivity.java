package com.example.argon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.argon.Model.GameConfig;
import com.example.argon.Model.GameConfigManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * @since 11-06-22
 * Activity class displaying achievement levels for a game play */

public class AchieveActivity extends AppCompatActivity {

    String[] difficulties = {"Easy", "Normal", "Hard"};
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterDiff;
    GameConfigManager configs;
    ArrayList<String> levels = new ArrayList<>();
    ArrayAdapter<String> adapter;
    ListView list;

    boolean inputted = false;
    private static final double EASY = 0.75;
    private static final double NORMAL = 1.00;
    private static final double HARD = 1.25;
    private static final String LIST_POSITION = "list position";
    private int position, clicked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achieve);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.achieve_activity_title);

        configs = GameConfigManager.getInstance();
        extractDataFromIntent();

        setLevelsToZero();
        setUpDifficultyDropDownMenu();
        populateListView();
        setUpPlayerInputButton();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setBackground();
    }

    // Shows toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.achievement_toolbar, menu);
        return true;
    }

    public static Intent makeIntent(Context context, int position){
        Intent intent = new Intent(context,AchieveActivity.class);
        intent.putExtra(LIST_POSITION,position);
        return intent;
    }

    private void extractDataFromIntent() {
        Intent intent = getIntent();
        position = intent.getIntExtra(LIST_POSITION, 0);
    }

    @SuppressLint({"ResourceAsColor", "ResourceType"})
    public void setBackground() {
        int color = configs.getThemeColor();
        getWindow().getDecorView().setBackground(ContextCompat.getDrawable(this, color));
    }

    // Handles Toolbar button selections
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.achievementStatsButton) {
            Intent intent = AchievementStatsActivity.makeIntent(AchieveActivity.this, position);
            startActivity(intent);
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void setUpDifficultyDropDownMenu() {
        autoCompleteTextView = findViewById(R.id.auto_complete_txt);
        adapterDiff = new ArrayAdapter<>(this, R.layout.list_item_difficulty, difficulties);
        autoCompleteTextView.setAdapter(adapterDiff);
        autoCompleteTextView.setText(adapterDiff.getItem(1).toString(), false);
        clicked = 1;
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                clicked = i;
            }
        });
    }

    private void setUpPlayerInputButton() {
        EditText input = findViewById(R.id.etNumPlayers);
        Button btn = findViewById(R.id.buttonNumPlayers);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int enter = Integer.parseInt(input.getText().toString());
                    if (clicked == 0) {
                        updateAchievementsList(enter, EASY);
                    } else if (clicked == 1) {
                        updateAchievementsList(enter, NORMAL);
                    } else if (clicked == 2) {
                        updateAchievementsList(enter, HARD);
                    }
                    adapter.notifyDataSetChanged();
                }
                catch(NumberFormatException e) {
                    Toast.makeText(AchieveActivity.this, R.string.please_enter_players_and_diff, Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void populateListView() {
        adapter = new MyListAdapter(levels);
        list = findViewById(R.id.listAchievements);
        list.setAdapter(adapter);
    }

    // Sets theme names
    private class MyListAdapter extends ArrayAdapter<String> {
        public MyListAdapter(ArrayList<String> levels) {
            super(AchieveActivity.this, R.layout.item_list_achievement, levels);
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.item_list_achievement, parent, false);
            }

            ImageView imageView = itemView.findViewById(R.id.imageViewAchieveIcon);

            setCorrectImageForAchievement(imageView, position);

            TextView name = itemView.findViewById(R.id.tvAchieveLevel);
            if (GameConfigManager.getInstance().getThemeSelected() == -1) {
                name.setText(configs.getGameConfig(AchieveActivity.this.position).getAchievementFruits()[9 - position]);
            } else if (GameConfigManager.getInstance().getThemeSelected() == 0) {
                name.setText(configs.getGameConfig(AchieveActivity.this.position).getAchievementAnimals()[9 - position]);
            } else if (GameConfigManager.getInstance().getThemeSelected() == 1) {
                name.setText(configs.getGameConfig(AchieveActivity.this.position).getAchievementPlanets()[9 - position]);
            }

            TextView level = itemView.findViewById(R.id.tvAchieveLevelScore);
            if (inputted) {
                level.setText(getString(R.string.achievement_level_score, levels.get(position)));
            } else {
                level.setText("");
            }

            TextView timesEarned = itemView.findViewById(R.id.timesAchieveEarnedTV);
            timesEarned.setText(configs.getGameConfig(AchieveActivity.this.position).getAchievementTime(9 - position) + "");

            return itemView;
        }
    }

    private void setCorrectImageForAchievement(ImageView imageView, int position) {
        if (GameConfigManager.getInstance().getThemeSelected() == -1) {
            Map<String, Integer> levels = ConfigActivity.achievementImagesMap;
            imageView.setImageResource(levels.get(GameConfig.achievementFruits[9 - position]));
        } else if (GameConfigManager.getInstance().getThemeSelected() == 0) {
            Map<String, Integer> levels = ConfigActivity.achievementImagesMapAnimals;
            imageView.setImageResource(levels.get(GameConfig.achievementAnimals[9 - position]));
        } else if (GameConfigManager.getInstance().getThemeSelected() == 1) {
            Map<String, Integer> levels = ConfigActivity.achievementImagesMapPlanets;
            imageView.setImageResource(levels.get(GameConfig.achievementPlanets[9 - position]));
        }
    }

    private void updateAchievementsList(int players, double difficulty) {
        levels.clear();
        int[] lvls = configs.getGameConfig(position).calculateAchievementMins(players, difficulty);
        for (int i: lvls) {
            levels.add(String.valueOf(i));
        }
        Collections.reverse(levels);
        inputted = true;
    }

    private void setLevelsToZero() {
        for (int i = 0; i < 10; i++) {
            levels.add("0");
        }
    }
}