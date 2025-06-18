package com.example.mcdonaldqueuegame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenu extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button startButton = findViewById(R.id.startButton);
        Button highScoresButton = findViewById(R.id.highScoresButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the game activity
                Intent intent = new Intent(MainMenu.this, MainActivity.class); // Replace MainActivity with your game activity
                startActivity(intent);
            }
        });

        highScoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the high scores activity
//                Intent intent = new Intent(MainMenuActivity.this, HighScoresActivity.class); // Replace HighScoresActivity with your high scores activity
//                startActivity(intent);
            }
        });
    }
}