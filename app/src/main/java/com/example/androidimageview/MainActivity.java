package com.example.androidimageview;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.nex3z.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    private Spinner albumSpinner;
    private FlowLayout flowLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);

        flowLayout = findViewById(R.id.flowLayout);
        albumSpinner = findViewById(R.id.albumSpinner);


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    READ_EXTERNAL_STORAGE_REQUEST_CODE);
        } else {
            loadAlbums();
        }

        albumSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedAlbum = parent.getItemAtPosition(position).toString();
                loadImagesFromAlbum(selectedAlbum);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAlbums();
            } else {

            }
        }
    }

    private void loadAlbums() {
        List<String> albumList = new ArrayList<>();
        HashSet<String> albumSet = new HashSet<>();

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        String sortOrder = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC";

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                if (!albumSet.contains(album)) {
                    albumSet.add(album);
                    albumList.add(album);
                }
            }
            cursor.close();
        }

        Log.d("Tag", albumList.toString());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, albumList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        albumSpinner.setAdapter(adapter);
    }

    private void loadImagesFromAlbum(String albumName) {
        List<String> imageList = new ArrayList<>();

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.DATA};
        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?";
        String[] selectionArgs = {albumName};
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                imageList.add(imagePath);
            }
            cursor.close();
        }

        displayImages(imageList);
    }

    private void displayImages(List<String> imageList) {
        flowLayout.removeAllViews();

        for (String imagePath : imageList) {
            ImageView imageView = new ImageView(this);

            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
            layoutParams.width = 300;
            layoutParams.height = 300;
            layoutParams.setMargins(10, 10, 10, 10);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            imageView.setLayoutParams(layoutParams);

            Glide.with(this)
                    .load(imagePath)
                    .placeholder(R.drawable.progress_drawable)
                    .into(imageView);

            imageView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                intent.putExtra("imagePath", imagePath);
                startActivity(intent);
            });

            flowLayout.addView(imageView);
        }
    }
}