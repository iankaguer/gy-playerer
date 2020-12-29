package com.aztechlabs.gyplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import io.realm.Realm;

public class SongList extends AppCompatActivity {
    public static final String _PLAY_NEW_SONG = "com.aztechlabs.gyplayer.PlayNewAudio";
    AppCompatImageView back;
    private SongPlayer player;
    boolean serviceBound = false;
    List<SongModel> listSons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);
        Realm.init(this);

        RecyclerView recyclerV = findViewById(R.id.recyclerv);
        Realm realm = Realm.getDefaultInstance();
        listSons = realm.where(SongModel.class).findAll();

        SongListAdapter sAdapter = new SongListAdapter(SongList.this, listSons, player, serviceBound);
        recyclerV.setAdapter(sAdapter);
        recyclerV.setLayoutManager(new LinearLayoutManager(this));

        recyclerV.addOnItemTouchListener(new CustomTouchListener(this, new onItemClickListener() {
            @Override
            public void onClick(View view, int index) {
                //playAudio(index);
                //Log.e("gp log", "gp le son => "+listSons.get(index));
                SongModel son = listSons.get(index);

                    MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
                    metaRetriver.setDataSource(son.getUri());
                //String artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

                playAudio(son.getUri());

            }
        }));


        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SongList.this, MainActivity.class));
            }
        });
    }

    public void onBackPressed(){
        super.onBackPressed();
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }

    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SongPlayer.LocalBinder binder = (SongPlayer.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    public void playAudio(String path) {
        //SongModel son = listSons.get(audioIndex);
        if (!serviceBound) {

            Intent playerIntent = new Intent(this, SongPlayer.class);
            playerIntent.putExtra("media", path);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(_PLAY_NEW_SONG);
            broadcastIntent.putExtra("media", path);
            sendBroadcast(broadcastIntent);
        }

    }


}