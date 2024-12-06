package com.melihcandemir.artbookjava;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.navigation.NavigationView;
import com.melihcandemir.artbookjava.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ArtAdapter.ArtAdapterCallback {
    ActivityMainBinding binding;
    ArrayList<Art> artArrayList;
    ArtAdapter artAdapter;
    SQLiteDatabase sqLiteDatabase;
    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setLogo(R.drawable.ic_artbook);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                return true;
            } else if (item.getItemId() == R.id.nav_about) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                return true;
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            SpannableString spanString = new SpannableString(Objects.requireNonNull(menuItem.getTitle()).toString());
            spanString.setSpan(new AbsoluteSizeSpan(20, true), 0, spanString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            menuItem.setTitle(spanString);
        }


        sqLiteDatabase = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);

        artArrayList = new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        artAdapter = new ArtAdapter(artArrayList, this);
        binding.recyclerView.setAdapter(artAdapter);
        getData();

        binding.buttonAddArtwork.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ArtActivity.class);
            intent.putExtra("info", "new");
            startActivity(intent);
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void getData() {
        try {
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM arts", null);
            int nameIx = cursor.getColumnIndex("artname");
            int idIx = cursor.getColumnIndex("id");
            int imageIx = cursor.getColumnIndex("image");

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIx);
                int id = cursor.getInt(idIx);
                byte[] image = cursor.getBlob(imageIx);
                Art art = new Art(name, id, image);
                artArrayList.add(art);
            }

            artAdapter.notifyDataSetChanged();
            cursor.close();

        } catch (Exception e) {
            Log.e("getData", e+"");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.art_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.add_art) {
            Intent intent = new Intent(this, ArtActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        } else if (item.getItemId() == R.id.about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
            sqLiteDatabase.close();
        }
    }

    @Override
    public void onArtDelete(int artId, int position) {
        new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                .setTitle("Delete Artwork")
                .setMessage("Are you sure you want to delete this artwork?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteArtFromDatabase(artId);
                    artArrayList.remove(position);
                    artAdapter.notifyItemRemoved(position);
                    artAdapter.notifyItemRangeChanged(position, artArrayList.size());
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void deleteArtFromDatabase(int artId) {
        try {
            sqLiteDatabase.delete("arts", "id = ?", new String[]{String.valueOf(artId)});
            Toast.makeText(this, "The artwork was deleted!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("deleteArtFromDatabase",e+"");
        }
    }
}