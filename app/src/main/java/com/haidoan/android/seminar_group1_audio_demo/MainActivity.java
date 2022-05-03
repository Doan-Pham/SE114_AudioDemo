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

public class MainActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer = null;

    TextView audioNameTextView;

    Button loadButton;
    Button playButton;
    Button pauseButton;
    Button stopButton;
    Button saveButton;

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

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(view -> {
            saveAudioToGallery();
            //saveAudioToFile();
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

    private void saveAudioToGallery() {

        //ContentResolver is a class to interact with ContentProvider - a class that manages access
        //to a central repository of data
        ContentResolver resolver = getApplicationContext().getContentResolver();

        //MediaStore is an API for applications to interact with a device's media
        //URI (Uniform Resource Identifier) is a string of characters that identifies a logical
        //or physical resource

        Uri audioCollectionUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            audioCollectionUri =
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            audioCollectionUri =
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String newAudioName = "gallerySavedSong2.mp3";

        //ContentValues is used to store a set of values that the ContentResolver can process.
        ContentValues newSongDetails = new ContentValues();
        newSongDetails.put(MediaStore.Audio.Media.DISPLAY_NAME, newAudioName);
        newSongDetails.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");

        //This is needed in API < Android Q (API 29)
//        String externalMusicDirectory = Environment.getExternalStoragePublicDirectory
//        (Environment.DIRECTORY_MUSIC).toString();

//        newSongDetails.put(MediaStore.Audio.Media.DATA
//                , externalMusicDirectory + "/" + newAudioName);

        Uri newAudioUri = resolver.insert(audioCollectionUri, newSongDetails);

        // Create a buffer for transferring data between InputStream and OutputStream
        // The "i" variable is for storing the number of bytes read from InputStream
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


        //Initialize the output file path for saving audio to
        String newAudioName = "filedSaveSong2.mp3";
        File outputDirectory = new File(
                getFilesDir() + "/" + "outputDirectory" + "/");
        if (!outputDirectory.exists()) {
            outputDirectory.mkdir();
        }
        File outputAudioFile = new File(outputDirectory + "/" + newAudioName);

        //Create a buffer for transferring data between InputStream and OutputStream
        // The "i" variable is for storing the number of bytes read from InputStream
        int i = 0;
        int bufferSize = 512;
        byte[] buffer = new byte[bufferSize];

        try {
            // Create an InputStream from file path
            FileInputStream inputStream = new FileInputStream(inputAudioFile);

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
}