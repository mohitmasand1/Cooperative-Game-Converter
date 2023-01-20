package com.example.argon;

import static android.graphics.Color.BLACK;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.argon.Model.GameConfig;
import com.example.argon.Model.GameConfigManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

/**
 * @since 11-28-22
 * Displays how many times each achievement is earned using a bar chart
 * */
public class AchievementStatsActivity extends AppCompatActivity {

    public static final int NUM_ACHIEVEMENTS = 10;
    public final int ANIMAL = 0;
    public final int FRUIT = -1;
    private int position;
    private static final String CONFIG_POSITION = "config position";
    private static final String barDataSetLabel = "Levels";
    private GameConfigManager gameConfigManager;
    private GameConfig config;
    private BarChart achievementBarChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement_stats);

        gameConfigManager = GameConfigManager.getInstance();
        extractDataFromIntent();
        config = gameConfigManager.getGameConfig(position);

        setBackground();
        getSupportActionBar().setTitle(getString(R.string.title_achievement_bar_graph, config.getName()));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setUpBarGraph();
        setUpXAxis();
        setUpYAxis();
    }

    public static Intent makeIntent(Context context, int position){
        Intent intent = new Intent(context, AchievementStatsActivity.class);
        intent.putExtra(CONFIG_POSITION,position);
        return intent;
    }

    private void extractDataFromIntent() {
        Intent intent = getIntent();
        position = intent.getIntExtra(CONFIG_POSITION, 0);
    }

    // Handles Toolbar button selections
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @SuppressLint({"ResourceAsColor", "ResourceType"})
    public void setBackground() {
        int color = gameConfigManager.getThemeColor();
        getWindow().getDecorView().setBackground(ContextCompat.getDrawable(this, color));
    }


    private void setUpYAxis() {
        achievementBarChart.getAxisRight().setEnabled(false);
        YAxis yAxis = achievementBarChart.getAxisLeft();

        // Setup lines
        yAxis.setDrawGridLines(false);

        yAxis.setDrawZeroLine(true);
        yAxis.setZeroLineWidth(2f);
        yAxis.setZeroLineColor(BLACK);

        yAxis.setAxisMinimum(0);
        yAxis.setAxisLineWidth(2f);
        yAxis.setAxisLineColor(BLACK);

        yAxis.setGranularity(1.0f); // min interval between y axis values is 1
        yAxis.setTextSize(18f);
        achievementBarChart.animateY(1200);
    }

    private void setUpXAxis() {
        ArrayList<String> xAxisLabel = new ArrayList<>();
        int theme = gameConfigManager.getThemeSelected();
        // Set up labels
        for(int i = 0; i < NUM_ACHIEVEMENTS; i++) {
            if(theme == FRUIT) {
                xAxisLabel.add(config.getAchievementFruits()[i]);
            } else if(theme == ANIMAL) {
                xAxisLabel.add(config.getAchievementAnimals()[i]);
            } else { // theme == PLANET
                xAxisLabel.add(config.getAchievementPlanets()[i]);
            }
        }
        XAxis xAxis = achievementBarChart.getXAxis();

        // Format lines
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);

        // Format labels
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabel));
        xAxis.setGranularity(1.0f);
        xAxis.setLabelCount(10);
        xAxis.setLabelRotationAngle(270);
        xAxis.setTextSize(18f);
    }

    private void setUpBarGraph() {
        achievementBarChart = findViewById(R.id.achievementBarChart);
        // Set up each bar
        ArrayList<BarEntry> levels = new ArrayList<>();
        for(int i = 0; i < NUM_ACHIEVEMENTS; i++){
            levels.add(new BarEntry( i, config.getAchievementTime(i)));
        }

        // On click listener for each bar
        achievementBarChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = achievementBarChart.getBarData().getDataSetForEntry(e).getEntryIndex((BarEntry) e);
                int theme = gameConfigManager.getThemeSelected();
                int image;
                String achievement;
                // Set achievement name and image
                if (theme == FRUIT) {
                    achievement = config.getAchievementFruits()[index];
                    image = ConfigActivity.achievementImagesMap.get(achievement);
                } else if (theme == ANIMAL) {
                    achievement = config.getAchievementAnimals()[index];
                    image = ConfigActivity.achievementImagesMapAnimals.get(achievement);
                } else { // theme == PLANET
                    achievement = config.getAchievementPlanets()[index];
                    image = ConfigActivity.achievementImagesMapPlanets.get(achievement);
                }

                // Build alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(AchievementStatsActivity.this);
                builder.setTitle(achievement);
                builder.setIcon(image);
                builder.setMessage(getString(R.string.achievement_times_earned,config.getAchievementTime(index)));
                builder.create();
                builder.show();
            }
            @Override
            public void onNothingSelected() { }
        });

        // Set up legend, text and colour of bars
        BarDataSet barDataSet = new BarDataSet(levels, barDataSetLabel);
        barDataSet.setColors(new int[]{
                R.color.red,
                R.color.dark_orange,
                R.color.yellow,
                R.color.lime,
                R.color.green,
                R.color.teal,
                R.color.cobalt_blue,
                R.color.navy_blue,
                R.color.purple,
                R.color.magenta},
                this);
        barDataSet.setValueTextColor(BLACK);
        barDataSet.setValueTextSize(18f);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(1f); // Set bar width so that there is no gap

        // Convert float to int and display value as string
        barData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return Integer.toString((int)value);
            }
        });

        // Disable description and legend
        achievementBarChart.getDescription().setEnabled(false);
        achievementBarChart.getLegend().setEnabled(false);
        achievementBarChart.setData(barData);
    }
}