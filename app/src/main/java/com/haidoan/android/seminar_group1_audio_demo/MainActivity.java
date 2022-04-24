package com.haidoan.android.seminar_group1_audio_demo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    MediaRecorder mediaRecorder;

    TextView audioNameTextView;
    TextView audioDurationTextView;
    TextView audioCurrentTextView;

    Button playButton;
    Button pauseButton;
    Button forwardButton;
    Button rewindButton;
    Button audioCurrentButton;
    Button stopButton;
    Button recordButton;
    Button stopRecordButton;

    ParcelFileDescriptor filePathForSaving;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        while (!checkAndRequestPermissions()) {
            checkAndRequestPermissions();
        }

        audioDurationTextView = findViewById(R.id.audio_duration_textview);
        audioNameTextView = findViewById(R.id.song_name_textview);
        audioCurrentTextView = findViewById(R.id.audio_current_textview);


        //Load audio from resource
        mediaPlayer = MediaPlayer.create(this, R.raw.song2);

        //Load audio from gallery
        Uri audioUri = getAudioFromGallery();
        mediaPlayer = MediaPlayer.create(this, audioUri);

        //Load audio from file
//            File filePath = new File(
//                    getFilesDir().getPath() + File.separator + "song2.mp3");
//            Uri filePathAsUri = Uri.fromFile(filePath);
//            mediaPlayer = MediaPlayer.create(this, filePathAsUri);

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
            if (mediaPlayer != null)
                mediaPlayer.stop();
        });
    }

    private Uri getAudioFromGallery() {

        //The collection to query in
        Uri audioCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            audioCollection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            audioCollection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        //Columns to query for
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
        };
        //Selection condition
        String selection = MediaStore.Audio.Media.DISPLAY_NAME + "== ?";

        //Arguments for selection condition
        String[] selectionArgs = {"24-04-2022-01-27-10.mp3"};

        String sortOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";

        try (Cursor cursor = getApplicationContext().getContentResolver().query(
                audioCollection,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            //Get indexes of data columns
            int idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

            if (cursor.getCount() > 1) {
                Log.d("Error"
                        , "Cannot load audio file from gallery or file doesn't exist");
                return null;
            }
            while (cursor.moveToNext()) {

                //Get data
                long id = cursor.getLong(idColumnIndex);
                String name = cursor.getString(nameColumnIndex);
                int duration = cursor.getInt(durationColumnIndex);

                //Get the Uri for the audio file returned by the query
                Uri audioUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

                audioNameTextView.setText(name);
                audioDurationTextView.setText(convertDurationToAudioTime(duration));
                return audioUri;
            }
        }
        return null;
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

        String newAudioName = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", Locale.getDefault())
                .format(System.currentTimeMillis()) + ".mp3";

        String externalMusicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();

        ContentValues newSongDetails = new ContentValues();
        newSongDetails.put(MediaStore.Audio.Media.DISPLAY_NAME, newAudioName);
        newSongDetails.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");
        newSongDetails.put(MediaStore.Audio.Media.DATA
                , externalMusicDirectory + File.separator + newAudioName);

        Uri newAudioUri = resolver.insert(audioCollection, newSongDetails);

        try {
            filePathForSaving = resolver.openFileDescriptor(newAudioUri, "w");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveFileToGallery() {

        ContentResolver resolver = getApplicationContext().getContentResolver();

        Uri audioCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            audioCollection =
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            audioCollection =
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String newAudioName = "test1.mp3";

        String externalMusicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();

        ContentValues newSongDetails = new ContentValues();
        newSongDetails.put(MediaStore.Audio.Media.DISPLAY_NAME, newAudioName);
        newSongDetails.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");
        newSongDetails.put(MediaStore.Audio.Media.DATA
                , externalMusicDirectory + File.separator + newAudioName);

        Uri newAudioUri = resolver.insert(audioCollection, newSongDetails);

        int i = 0;
        int bufferSize = 512;
        byte[] buffer = new byte[bufferSize];

        InputStream inputStream = getResources().openRawResource(R.raw.song2);
        //FileOutputStream outputStream = new FileOutputStream();
        try {
            OutputStream outputStream = resolver.openOutputStream(newAudioUri, "w");

            while ((i = inputStream.read(buffer)) != -1)
                outputStream.write(buffer, 0, i);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkAndRequestPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                + (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                + (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))))
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            } else {
                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
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