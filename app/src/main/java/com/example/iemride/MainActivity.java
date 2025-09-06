package com.example.iemride;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class MainActivity extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        playerView = findViewById(R.id.videoView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Initialize the player
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // Build the media item from the raw resource
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.iemridevd;
        Uri uri = Uri.parse(videoPath);
        MediaItem mediaItem = MediaItem.fromUri(uri);

        // Set the media item and prepare the player
        player.setMediaItem(mediaItem);
        player.prepare();

        // Set the player to loop the video indefinitely
        player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);

        player.play(); // Start playback automatically
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }
    public void login (View v){
        Intent i = new Intent(MainActivity.this,Loginactivity.class);
        startActivity(i);
    }
}