package com.aztechlabs.gyplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import info.abdolahi.CircularMusicProgressBar;

public class MainActivity extends AppCompatActivity {

    AppCompatImageView playlist, btnPrev, btnPlay, btnNext, btnFav, btnShuffle, btnReadAll;
    CircularMusicProgressBar albumArt;
    TextView title, artist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playlist = findViewById(R.id.playlist);
        btnPrev = findViewById(R.id.btn_prev);
        btnPlay = findViewById(R.id.btn_play);
        btnNext = findViewById(R.id.btn_next);
        btnFav = findViewById(R.id.btn_fav);
        btnShuffle = findViewById(R.id.btn_shuffle);
        btnReadAll = findViewById(R.id.btn_readall);
        albumArt = findViewById(R.id.album_art);
        title = findViewById(R.id.title);
        artist = findViewById(R.id.artiste);

        playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SongList.class));
            }
        });


        Log.e("logerror2", getMediaController().getMetadata() +"");

    }

    public void onBackPressed(){
        super.onBackPressed();
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);

    }
}