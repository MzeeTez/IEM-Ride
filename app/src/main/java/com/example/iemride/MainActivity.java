package com.example.iemride;

import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;

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

        videoView = findViewById(R.id.videoView);
        // Build the video URI from the raw resource
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.iemridevd;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);

        // Set a listener to loop the video
        videoView.setOnCompletionListener(mediaPlayer -> {
            videoView.start(); // Restart the video when it completes
        });

        videoView.start(); // Start the video
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume video playback when the activity comes to the foreground
        videoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause video playback when the activity is not in the foreground
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }
}