package com.melihcandemir.artbookjava;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.melihcandemir.artbookjava.databinding.ActivityArtBinding;

import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {
    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            String artAdd = getString(R.string.artAdd);
            actionBar.setTitle(artAdd);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_backpress);

            actionBar.setLogo(R.drawable.ic_addart);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(ArtActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });


        registerLauncher();

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        assert info != null;
        if (info.equals("new")) {
            clearFields();
            binding.saveButton.setVisibility(View.VISIBLE);
        } else {
            int artId = intent.getIntExtra("artId",0);
            binding.saveButton.setVisibility(View.INVISIBLE);
            binding.clearButton.setVisibility(View.INVISIBLE);
            binding.artImageView.setEnabled(false);
            binding.nameText.setEnabled(false);
            binding.artistText.setEnabled(false);
            binding.yearText.setEnabled(false);

            try {

                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", new String[]{String.valueOf(artId)});
                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()) {
                    binding.nameText.setText(cursor.getString(artNameIx));
                    binding.artistText.setText(cursor.getString(painterNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    binding.artImageView.setImageBitmap(bitmap);
                }

                cursor.close();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clear_area(View view) {
        if (binding.nameText.getText().toString().isEmpty()
                && binding.artistText.getText().toString().isEmpty()
                && binding.yearText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Fields are already empty!", Toast.LENGTH_LONG).show();
        } else {
            new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                    .setTitle("Clear Form")
                    .setMessage("Are you sure you want to clear all fields?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        clearFields();
                        Toast.makeText(this, "Fields have been cleared!", Toast.LENGTH_LONG).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    private void clearFields() {
        binding.nameText.setText("");
        binding.artistText.setText("");
        binding.yearText.setText("");
        binding.artImageView.setImageResource(R.drawable.selectimage);
    }

    /* public void update_artwork(View view) {
        // Mevcut sanat eseri bilgilerini al
        String name = binding.nameText.getText().toString();
        String artistName = binding.artistText.getText().toString();
        String year = binding.yearText.getText().toString();

        // Güncellenebilir mi kontrolü (örneğin boş bırakılmamalı)
        if (name.isEmpty() || artistName.isEmpty() || year.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_LONG).show();
            return;
        }

        if (selectedImage == null) {
            Toast.makeText(this, "Please choose a image!", Toast.LENGTH_LONG).show();
            return;
        }

        // Görseli küçült
        Bitmap smallImage = makeSmallerImage(selectedImage, 300);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {
            // Sanat eserini güncellemek için SQL sorgusu
            String sql = "UPDATE arts SET artname = ?, paintername = ?, year = ?, image = ? WHERE id = ?";
            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindString(1, name);
            statement.bindString(2, artistName);
            statement.bindString(3, year);
            statement.bindBlob(4, byteArray);
            statement.bindString(5, String.valueOf(artId));  // Mevcut sanat eseri ID'si

            // Sorguyu çalıştır
            statement.execute();
            Toast.makeText(this, "Artwork updated!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }

        // Ana sayfaya geri dön
        Intent intent = new Intent(ArtActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    } */

    @Override
    public boolean onSupportNavigateUp() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        Intent intent = new Intent(ArtActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        return true;
    }

    public void save(View view) {
        String name = binding.nameText.getText().toString();
        String artistName = binding.artistText.getText().toString();
        String year = binding.yearText.getText().toString();

        if (name.isEmpty() || artistName.isEmpty() || year.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_LONG).show();
            return;
        }

        if (selectedImage == null) {
            Toast.makeText(this, "Please choose a image!", Toast.LENGTH_LONG).show();
            return;
        }

        Bitmap smallImage = makeSmallerImage(selectedImage, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {
            database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY, artname VARCHAR, paintername VARCHAR, year VARCHAR, image BLOB)");

            String sqlString = "INSERT INTO arts (artname, paintername,year, image) VALUES(?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1, name);
            sqLiteStatement.bindString(2, artistName);
            sqLiteStatement.bindString(3, year);
            sqLiteStatement.bindBlob(4, byteArray);
            sqLiteStatement.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(ArtActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            // landscape image
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            // portrait image
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width,height,true);
    }

    public void selectImage(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 33+ -> READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "Permission needed for open to gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();
                } else {
                    // request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
                // request permission
            } else {
                // gallery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        } else {
            // Android 32- -> READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, "Permission needed for open to gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                } else {
                    // request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
                // request permission
            } else {
                // gallery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }
    }

    private void registerLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
            if (o.getResultCode() == RESULT_OK) {
                Intent intentFromResult = o.getData();
                if (intentFromResult != null) {
                    Uri imageData = intentFromResult.getData();
                    //binding.artImageView.setImageURI(imageData);

                    try {
                        ImageDecoder.Source source = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            assert imageData != null;
                            source = ImageDecoder.createSource(getContentResolver(), imageData);
                        } else {
                            selectedImage = MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(), imageData);
                            binding.artImageView.setImageBitmap(selectedImage);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            selectedImage = ImageDecoder.decodeBitmap(source);
                        }
                        binding.artImageView.setImageBitmap(selectedImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                if (o) {
                    // permission granted
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);

                } else {
                    // permission denied
                    Toast.makeText(ArtActivity.this, "Permission needed!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}