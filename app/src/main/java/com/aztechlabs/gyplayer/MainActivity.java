package com.aztechlabs.gyplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vansuita.gaussianblur.GaussianBlur;

import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;

import static com.aztechlabs.gyplayer.SongPlayer.ACTION_NEXT;


public class MainActivity extends AppCompatActivity {

    AppCompatImageView playlist, btnPrev, btnPlay, btnNext, btnFav, btnShuffle, btnLoop;
    ImageView albumArt;
    TextView title, artist;
    AppCompatImageView back;
    private SongPlayer player;
    boolean serviceBound = false;
    MediaMetadataRetriever metaRetriver;
    LinearLayout rootLyt;
    SeekBar seekBar;
    Handler mHandler ;
    LecteurPrefModel lecteur;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        metaRetriver = new MediaMetadataRetriever();

        Realm.init(this);
        Realm realm = Realm.getDefaultInstance();
        lecteur = realm.where(LecteurPrefModel.class).equalTo("id", 1).findFirst();

        playlist = findViewById(R.id.playlist);
        btnPrev = findViewById(R.id.btn_prev);
        btnPlay = findViewById(R.id.btn_play);
        btnNext = findViewById(R.id.btn_next);
        btnFav = findViewById(R.id.btn_fav);
        btnShuffle = findViewById(R.id.btn_shuffle);
        btnLoop = findViewById(R.id.btn_loop);
        albumArt = findViewById(R.id.album_art);
        title = findViewById(R.id.title);
        artist = findViewById(R.id.artiste);
        rootLyt = findViewById(R.id.rootlyt);
        seekBar = findViewById(R.id.seekbar);
        mHandler = new Handler();
        seekBar.setProgress(0);

        if (lecteur.isShuffle()){
            btnShuffle.setColorFilter(ContextCompat.getColor(this, R.color.color_tint), android.graphics.PorterDuff.Mode.SRC_IN);

        }
        if (lecteur.isLoop()){
            btnLoop.setColorFilter(ContextCompat.getColor(this, R.color.color_tint), android.graphics.PorterDuff.Mode.SRC_IN);

        }

        btnLoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lecteur.isLoop()){
                    btnLoop.setColorFilter(null);
                    realm.beginTransaction();
                    lecteur.setIsLoop(false);
                    realm.copyToRealmOrUpdate(lecteur);
                    realm.commitTransaction();
                }else{
                    btnLoop.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_tint), android.graphics.PorterDuff.Mode.SRC_IN);
                    realm.beginTransaction();
                    lecteur.setIsLoop(true);
                    realm.copyToRealmOrUpdate(lecteur);
                    realm.commitTransaction();
                    lecteur = realm.where(LecteurPrefModel.class).equalTo("id", 1).findFirst();
                }
            }
        });


        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lecteur.isShuffle()){
                    btnShuffle.setColorFilter(null);

                    realm.beginTransaction();
                    lecteur.setShuffle(false);
                    realm.copyToRealmOrUpdate(lecteur);
                    realm.commitTransaction();
                }else{
                    btnShuffle.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_tint), android.graphics.PorterDuff.Mode.SRC_IN);

                    realm.beginTransaction();
                    lecteur.setShuffle(true);
                    realm.copyToRealmOrUpdate(lecteur);
                    realm.commitTransaction();
                }
            }
        });

        playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SongList.class));
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player.mediaPlayer.isPlaying()){
                    player.mediaPlayer.pause();
                }else {
                    if (lecteur.getLastPlayedUri() != null ||lecteur.getLastPlayedUri() != ""){
                        player.mediaFile = lecteur.getLastPlayedUri();

                    }
                    player.mediaPlayer.start();
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNextSong();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent= new Intent(this, SongPlayer.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        serviceBound=true;
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }


    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onBindingDied(ComponentName name) {
            serviceBound = false;
            btnPlay.setImageResource(R.drawable.ic_play);
        }

        @Override
        public void onNullBinding(ComponentName name) {
            serviceBound = false;
            btnPlay.setImageResource(R.drawable.ic_play);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SongPlayer.LocalBinder binder = (SongPlayer.LocalBinder) service;
            player = binder.getService();
            if (player.mediaPlayer != null && player.mediaPlayer.isPlaying()){
                serviceBound = true;
                btnPlay.setImageResource(R.drawable.ic_pause);
                updateInfo();
            }


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            btnPlay.setImageResource(R.drawable.ic_play);
        }
    };

    public void updateInfo(){
        if (serviceBound){
            metaRetriver.setDataSource(player.mediaFile);
            title.setText(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            artist.setText(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            Bitmap thumbnail = null;
            // Update the current
            byte [] data = metaRetriver.getEmbeddedPicture();
            thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length);

            if (thumbnail != null){
                albumArt.setImageBitmap(thumbnail);
                Bitmap blurredBitmap = GaussianBlur.with(MainActivity.this).render(thumbnail);
                BitmapDrawable ob = new BitmapDrawable(getResources(), blurredBitmap);
                rootLyt.setBackground(ob);
            }

            seekBar.setMax(player.mediaPlayer.getDuration());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    if (player.mediaPlayer != null) {
                        int mCurrentPosition = player.mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(mCurrentPosition, true);
                    }
                    mHandler.postDelayed(this, 1000);
                }
            };
            myRunnable.run();
        }else {

        }

    }

    public void playNextSong() {
        //SongModel son = listSons.get(audioIndex);


            Intent broadcastIntent = new Intent(ACTION_NEXT);
            //broadcastIntent.putExtra("media", path);
            sendBroadcast(broadcastIntent);


    }


}