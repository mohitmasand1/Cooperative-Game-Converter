package com.example.argon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;

import android.text.method.LinkMovementMethod;

import android.view.MenuItem;
import android.widget.TextView;

import com.example.argon.Model.GameConfigManager;

/**
 * @since 11-05-22
 * About Activity for providing the information of the group members,
 * explaining the features of the app, and crediting the sources
 * used for the app
 * */

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);
        setupHyperLinksSFX();
        setupHyperLinksIMGs();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Add back button
        setBackground();
    }

    public static Intent makeIntent(Context context){
        return new Intent(context, AboutActivity.class);
    }

    // Handles Toolbar button selections
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    // set up hyperlinks for sound resources
    private void setupHyperLinksSFX(){
        TextView twinkHLTextView = findViewById(R.id.twinkHLTextView);
        twinkHLTextView.setMovementMethod(LinkMovementMethod.getInstance());

        TextView sheepHLTextView = findViewById(R.id.sheepHLTextView);
        sheepHLTextView.setMovementMethod(LinkMovementMethod.getInstance());

        TextView munchHLTextView = findViewById(R.id.munchHLTextView);
        munchHLTextView.setMovementMethod(LinkMovementMethod.getInstance());

        TextView fanfareHLTextView = findViewById(R.id.fanfareHLTextView);
        fanfareHLTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // set up hyperlinks for image resources
    private void setupHyperLinksIMGs(){
       TextView achievePlumHLTextView = findViewById(R.id.achievePlumHLTextView);
       achievePlumHLTextView.setMovementMethod(LinkMovementMethod.getInstance());

       TextView checkedPinkHLTextView = findViewById(R.id.checkedPinkHLTextView);
       checkedPinkHLTextView.setMovementMethod(LinkMovementMethod.getInstance());

       TextView checkedGreyHLTextView = findViewById(R.id.checkedGreyHLTextView);
       checkedGreyHLTextView.setMovementMethod(LinkMovementMethod.getInstance());

       TextView achieveEmojiHLTextView = findViewById(R.id.achieveEmojiHLTextView);
       achieveEmojiHLTextView.setMovementMethod(LinkMovementMethod.getInstance());

       TextView achieveAppleHLTextView = findViewById(R.id.achieveAppleHLTextView);
       achieveAppleHLTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // set up background
    @SuppressLint({"ResourceAsColor", "ResourceType"})
    private void setBackground() {
        GameConfigManager gameConfigManager = GameConfigManager.getInstance();
        int color = gameConfigManager.getThemeColor();
        getWindow().getDecorView().setBackground(ContextCompat.getDrawable(this, color));
    }

}