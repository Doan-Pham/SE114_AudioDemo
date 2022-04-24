package com.haidoan.android.seminar_group1_audio_demo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer = null;

    TextView audioNameTextView;
    TextView audioDurationTextView;
    TextView audioCurrentTextView;

    Button loadButton;
    Button playButton;
    Button pauseButton;
    Button forwardButton;
    Button rewindButton;
    Button audioCurrentButton;
    Button stopButton;
    Button saveButton;

    ActivityResultLauncher<String> pickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null)
                        mediaPlayer = MediaPlayer.create(getApplicationContext(), result);
                }
            });

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

        audioDurationTextView = findViewById(R.id.audio_duration_textview);
        audioNameTextView = findViewById(R.id.audio_name_textview);
        audioCurrentTextView = findViewById(R.id.audio_current_textview);

        loadButton = findViewById(R.id.load_button);
        loadButton.setOnClickListener(view -> {
            //Load audio from resource
            mediaPlayer = MediaPlayer.create(this, R.raw.song2);

            //Load audio from gallery
            pickerLauncher.launch("audio/*");

            //Load audio from file
//            File filePath = new File(
//                    getFilesDir().getPath() + "/" + "inputDirectory" + "/" + "song2.mp3");
//            Uri filePathAsUri = Uri.fromFile(filePath);
//            mediaPlayer = MediaPlayer.create(this, filePathAsUri);

            if (mediaPlayer != null) {
                audioNameTextView.setText("Load audio successful");
                audioCurrentTextView.setText("00:00");
                audioDurationTextView.setText(convertDurationToAudioTime(mediaPlayer.getDuration()));
            } else {
                Toast.makeText(this, "Failed to load audio", Toast.LENGTH_SHORT).show();
            }
        });

        playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(view -> {
            if (mediaPlayer != null) {
                mediaPlayer.start();
                audioDurationTextView.setText(
                        convertDurationToAudioTime(mediaPlayer.getDuration()));
            }
        });

        pauseButton = findViewById(R.id.pause_button);
        pauseButton.setOnClickListener(view -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying())
                mediaPlayer.pause();
        });

        forwardButton = findViewById(R.id.forward_button);
        forwardButton.setOnClickListener(view -> {
            if (mediaPlayer != null) {
                int currentAudioPosition = mediaPlayer.getCurrentPosition();
                int audioDuration = mediaPlayer.getDuration();
                if (mediaPlayer.isPlaying() && currentAudioPosition <= audioDuration - 3000) {
                    currentAudioPosition += 3000;
                    mediaPlayer.seekTo(currentAudioPosition);
                    audioCurrentTextView.setText(convertDurationToAudioTime(currentAudioPosition));
                }
            }
        });

        rewindButton = findViewById(R.id.rewind_button);
        rewindButton.setOnClickListener(view -> {
            if (mediaPlayer != null) {
                int currentAudioPosition = mediaPlayer.getCurrentPosition();
                if (mediaPlayer.isPlaying() && currentAudioPosition >= 3000) {
                    currentAudioPosition -= 3000;
                    mediaPlayer.seekTo(currentAudioPosition);
                    audioCurrentTextView.setText(convertDurationToAudioTime(currentAudioPosition));
                }
            }
        });

        audioCurrentButton = findViewById(R.id.audio_current_button);
        audioCurrentButton.setOnClickListener(view -> {
            if (mediaPlayer != null)
                audioCurrentTextView.setText(
                        convertDurationToAudioTime(mediaPlayer.getCurrentPosition()));
        });

        stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(view -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                audioNameTextView.setText("No name");
                audioCurrentTextView.setText("-01:00");
                audioDurationTextView.setText("-01:00");
            }
        });

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(view -> {
            //saveAudioToGallery();
            saveAudioToFile();
        });
    }

    private void saveAudioToGallery() {

        ContentResolver resolver = getApplicationContext().getContentResolver();

        Uri audioCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            audioCollection =
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            audioCollection =
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String newAudioName = "gallerySavedSong2.mp3";


        ContentValues newSongDetails = new ContentValues();
        newSongDetails.put(MediaStore.Audio.Media.DISPLAY_NAME, newAudioName);
        newSongDetails.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");

        //This is needed in API < Android Q (API 29)
//        String externalMusicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();
//        newSongDetails.put(MediaStore.Audio.Media.DATA
//                , externalMusicDirectory + "/" + newAudioName);

        Uri newAudioUri = resolver.insert(audioCollection, newSongDetails);

        //Create a buffer for transferring data between InputStream and OutputStream
        // The "i" variable is for storing result from reading InputStream
        int i = 0;
        int bufferSize = 512;
        byte[] buffer = new byte[bufferSize];

        try {
            //Create an InputStream from resource
            InputStream inputStream = getResources().openRawResource(R.raw.song2);

            //Create an OutputStream with ContentResolver
            OutputStream outputStream = resolver.openOutputStream(newAudioUri, "w");

            //Read from InputStream into buffer then write to OutputStream
            while ((i = inputStream.read(buffer)) != -1)
                outputStream.write(buffer, 0, i);

            inputStream.close();
            outputStream.close();

            Toast.makeText(this, "Audio saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save audio", Toast.LENGTH_SHORT).show();
        }

    }

    private void saveAudioToFile() {
        // Initialize the input file path
        File inputDirectory = new File(
                getFilesDir() + "/" + "inputDirectory" + "/");
        if (!inputDirectory.exists()) {
            inputDirectory.mkdir();
        }
        File inputAudioFile = new File(inputDirectory + "/" + "song2.mp3");

        FileInputStream inputStream = null;


        //Initialize the output file path for saving audio to
        String newAudioName = "filedSaveSong2.mp3";
        File outputDirectory = new File(
                getFilesDir() + "/" + "outputDirectory" + "/");
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }
        File outputAudioFile = new File(outputDirectory + "/" + newAudioName);

        //Create a buffer for transferring data between InputStream and OutputStream
        // The "i" variable is for storing result from reading InputStream
        int i = 0;
        int bufferSize = 512;
        byte[] buffer = new byte[bufferSize];

        try {
            // Create an InputStream from file path
            inputStream = new FileInputStream(inputAudioFile);

            // Create an OutputStream from file path
            FileOutputStream outputStream = new FileOutputStream(outputAudioFile);

            while ((i = inputStream.read(buffer)) != -1)
                outputStream.write(buffer, 0, i);

            outputStream.close();
            inputStream.close();

            Toast.makeText(this, "Audio saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save audio", Toast.LENGTH_SHORT).show();
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

    private String convertDurationToAudioTime(int duration) {
        return String.format(
                Locale.US,
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration)
        );
    }
}