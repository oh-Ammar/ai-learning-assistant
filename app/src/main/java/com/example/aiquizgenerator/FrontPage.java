package com.example.aiquizgenerator;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

public class FrontPage extends AppCompatActivity {
    RelativeLayout rootLayout;
    ImageView blurImageView;
    Button docToText, docSummarizer;
    Animation buttonClick;
    AnimationDrawable backgroundAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_front_page);

        // Initialize UI elements
        rootLayout = findViewById(R.id.rootLayout);
        docToText = findViewById(R.id.documentToText);
        docSummarizer = findViewById(R.id.summarizeDocument);
        blurImageView = findViewById(R.id.blurImageView);

        // Load button click animation
        buttonClick = AnimationUtils.loadAnimation(this, R.anim.button_click);

        // Set the background animation
        rootLayout.setBackgroundResource(R.drawable.animation_list);
        backgroundAnimation = (AnimationDrawable) rootLayout.getBackground();
        backgroundAnimation.setEnterFadeDuration(1000);
        backgroundAnimation.setExitFadeDuration(1000);
        backgroundAnimation.start();

        // Set click listeners with animations
        docToText.setOnClickListener(v -> {
            v.startAnimation(buttonClick);
            transitionToActivity(DocumentToText.class);
        });

        docSummarizer.setOnClickListener(v -> {
            v.startAnimation(buttonClick);
            transitionToActivity(DocumentSummarizer.class);
        });
    }

    // Method to transition between activities with custom animations
    private void transitionToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(FrontPage.this, targetActivity);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}