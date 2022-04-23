package com.haidoan.android.seminar_group1_audio_demo;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    TextView songNameTextView;
    TextView audioDurationTextView;
    TextView audioCurrentTextView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }


        audioDurationTextView = findViewById(R.id.audio_duration_textview);
        songNameTextView = findViewById(R.id.song_name_textview);
        audioCurrentTextView = findViewById(R.id.audio_current_textview);

        Button playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            }
        });

        Button pauseButton = findViewById(R.id.pause_button);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.pause();
            }
        });


        Button forwardButton = findViewById(R.id.forward_button);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentAudioPosition = mediaPlayer.getCurrentPosition();
                int audioDuration = mediaPlayer.getDuration();
                if (mediaPlayer.isPlaying() && currentAudioPosition <= audioDuration) {
                    currentAudioPosition += 3000;
                    mediaPlayer.seekTo(currentAudioPosition);
                    audioCurrentTextView.setText(currentAudioPosition + "");
                }

            }
        });

        Button rewindButton = findViewById(R.id.rewind_button);
        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentAudioPosition = mediaPlayer.getCurrentPosition();
                if (mediaPlayer.isPlaying() && currentAudioPosition >= 3000) {
                    currentAudioPosition -= 3000;

                    mediaPlayer.seekTo(currentAudioPosition);
                    audioCurrentTextView.setText(currentAudioPosition + "");
                }
            }
        });

// mediaPlayer = MediaPlayer.create(this, R.raw.julius_marx_julius_marx_vices);
//        if (getFilesFromGallery() == null) mediaPlayer = null;
//        else
//            mediaPlayer = MediaPlayer.create(this, getFilesFromGallery());

        //mediaPlayer = getFilesFromGallery() == null ? null : MediaPlayer.create(this, getFilesFromGallery())
        // ;
        Uri filePath = Uri.fromFile(
                new File(getFilesDir().getPath() + "julius_marx_julius_marx_vices.mp3"));

        mediaPlayer = MediaPlayer.create(this, filePath);

        if (mediaPlayer != null)
            audioDurationTextView.setText(mediaPlayer.getDuration() + "");
        else
            audioDurationTextView.setText(getFilesDir().getPath());

    }

    private Uri getFilesFromGallery() {
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                //MediaStore.Audio.Media.SIZE
        };
        String selection = null;
        //MediaStore.Audio.Media.DISPLAY_NAME  == ?";
        String[] selectionArgs = null;
        //new String[]{
//                String.valueOf(TimeUnit.MILLISECONDS.convert(
//                    5, TimeUnit.MINUTES))
        //"julius_marx_julius_marx_vices.mp3"
        //  };

        String sortOrder = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";

        try (Cursor cursor = getApplicationContext().getContentResolver().query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            // int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);

            if (cursor.getCount() < 1) {
                Log.d("Error"
                        , "Cannot load audio file from gallery");
                return null;
            }
            while (cursor.moveToNext()) {
                // Get values of columns for a given Audio.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColumn);
                //int size = cursor.getInt(sizeColumn);

                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                songNameTextView.setText(name);
                // Stores column values and the contentUri in a local object
                // that represents the media file.
                return contentUri;
            }
        }
        return null;
    }
}