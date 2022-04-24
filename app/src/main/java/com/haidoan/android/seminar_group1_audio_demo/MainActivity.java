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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    MediaRecorder mediaRecorder;

    TextView songNameTextView;
    TextView audioDurationTextView;
    TextView audioCurrentTextView;

    Button playButton;
    Button pauseButton;
    Button forwardButton;
    Button rewindButton;
    Button recordButton;
    Button stopRecordButton;

    ParcelFileDescriptor filePathForSaving;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

        audioDurationTextView = findViewById(R.id.audio_duration_textview);
        songNameTextView = findViewById(R.id.song_name_textview);
        audioCurrentTextView = findViewById(R.id.audio_current_textview);

        playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            }
        });

        pauseButton = findViewById(R.id.pause_button);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null && mediaPlayer.isPlaying())
                    mediaPlayer.pause();
            }
        });

        forwardButton = findViewById(R.id.forward_button);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer == null) return;

                int currentAudioPosition = mediaPlayer.getCurrentPosition();
                int audioDuration = mediaPlayer.getDuration();
                if (mediaPlayer.isPlaying() && currentAudioPosition <= audioDuration - 3000) {
                    currentAudioPosition += 3000;
                    mediaPlayer.seekTo(currentAudioPosition);
                    audioCurrentTextView.setText(currentAudioPosition + "");
                }
            }
        });

        rewindButton = findViewById(R.id.rewind_button);
        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer == null) return;

                int currentAudioPosition = mediaPlayer.getCurrentPosition();
                if (mediaPlayer.isPlaying() && currentAudioPosition >= 3000) {
                    currentAudioPosition -= 3000;
                    mediaPlayer.seekTo(currentAudioPosition);
                    audioCurrentTextView.setText(currentAudioPosition + "");
                }
            }
        });

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFileToGallery();
            }
        });

        recordButton = findViewById(R.id.record_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
                if (filePathForSaving != null) {
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    mediaRecorder.setOutputFile(filePathForSaving.getFileDescriptor());
                    mediaRecorder.setAudioChannels(1);
                    try {
                        mediaRecorder.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaRecorder.start();
                }
            }
        });

        stopRecordButton = findViewById(R.id.stop_record_button);
        stopRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaRecorder != null) {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mediaRecorder = null;
                }
            }
        });

// mediaPlayer = MediaPlayer.create(this, R.raw.julius_marx_julius_marx_vices);
//        if (getFilesFromGallery() == null) mediaPlayer = null;
//        else
//            mediaPlayer = MediaPlayer.create(this, getFilesFromGallery());

        mediaPlayer = getFilesFromGallery() == null ? null : MediaPlayer.create(this, getFilesFromGallery());
        // ;
        Uri filePath = Uri.fromFile(
                new File(getFilesDir().getPath() + File.separator + "julius_marx_julius_marx_vices.mp3"));

        //mediaPlayer = MediaPlayer.create(this, filePath);

        if (mediaPlayer != null) {
            audioDurationTextView.setText(
                    mediaPlayer.getDuration() + "");
            songNameTextView.setText(
                    "julius_marx_julius_marx_vices.mp3");
        } else
            audioDurationTextView.setText(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath());
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
        String selection = MediaStore.Audio.Media.DISPLAY_NAME + "== ?";
        //MediaStore.Audio.Media.DISPLAY_NAME  == ?";
        String[] selectionArgs = {"new song.mp3"};
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

    private void saveFileToGallery() {
        // Add a specific media item.
        ContentResolver resolver = getApplicationContext()
                .getContentResolver();

// Find all audio files on the primary external storage device.
        Uri audioCollection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            audioCollection = MediaStore.Audio.Media
                    .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            audioCollection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        MediaRecorder recorder = new MediaRecorder();
// Publish a new song.
        String newSongName = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", Locale.US).format(System.currentTimeMillis()) + ".mp3";
        //newSongName = "new song.mp3";
        ContentValues newSongDetails = new ContentValues();
        newSongDetails.put(MediaStore.Audio.Media.DISPLAY_NAME,
                newSongName);
        newSongDetails.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();
        //getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        newSongDetails.put(MediaStore.Audio.Media.DATA, directory + File.separator + newSongName);
        Uri uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, newSongDetails);
// Keeps a handle to the new song's URI in case we need to modify it
// later.
        try {
            filePathForSaving = resolver.openFileDescriptor(uri, "w");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean checkAndRequestPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                + (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                + (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))))
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        return false;
    }


}