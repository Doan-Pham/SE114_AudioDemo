package com.haidoan.android.seminar_group1_audio_demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer = null;

    TextView audioNameTextView;

    Button loadButton;
    Button playButton;
    Button pauseButton;
    Button stopButton;

    ActivityResultLauncher<String> pickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        mediaPlayer = MediaPlayer.create(getApplicationContext(), result);

                        if (mediaPlayer != null) {
                            audioNameTextView.setText("Load audio successful");
                        } else {
                            Toast.makeText(getApplicationContext()
                                    , "Failed to load audio", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

        audioNameTextView = findViewById(R.id.audio_name_textview);

        loadButton = findViewById(R.id.load_button);
        loadButton.setOnClickListener(view -> {
//            loadAudioFromResource();
            loadAudioFromGallery();
//            loadAudioFromFile();
        });

        playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(view -> {
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        });

        pauseButton = findViewById(R.id.pause_button);
        pauseButton.setOnClickListener(view -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying())
                mediaPlayer.pause();
        });

        stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(view -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                audioNameTextView.setText("No name");
            }
        });

    }

    private void loadAudioFromResource() {
        //Load audio from resource
        mediaPlayer = MediaPlayer.create(this, R.raw.song2);
    }

    private void loadAudioFromGallery() {
        pickerLauncher.launch("audio/*");
    }

    private void loadAudioFromFile() {

        //Initialize the input file path

        // getFilesDir() returns the app's private directory, it usually looks like
        // data/data/package_name/....
        File filePath = new File(getFilesDir().getPath()
                + "/" + "inputDirectory" + "/" + "song2.mp3");

        Uri filePathAsUri = Uri.fromFile(filePath);
        mediaPlayer = MediaPlayer.create(this, filePathAsUri);

        if (mediaPlayer != null) {
            audioNameTextView.setText("Load audio successful");
        } else {
            Toast.makeText(this, "Failed to load audio", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkAndRequestPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                + (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)))
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
        return false;
    }
}