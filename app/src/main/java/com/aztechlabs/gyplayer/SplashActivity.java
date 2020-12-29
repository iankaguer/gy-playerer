package com.aztechlabs.gyplayer;

import android.Manifest;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class SplashActivity extends AppCompatActivity {
    int PERMISSIONWSTO = 1101;
    int PERMISSIONRSTO = 1102;
    int PERMISSIONPHONE = 1103;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ProgressBar progress = findViewById(R.id.progress);


        //realmMigration();
        if (ActivityCompat.checkSelfPermission( SplashActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONRSTO);
        }

        if (ActivityCompat.checkSelfPermission( SplashActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONWSTO);
        }

        if (ActivityCompat.checkSelfPermission( SplashActivity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONPHONE);
        }

        Realm.init(this);
        Realm realm = Realm.getDefaultInstance();
        LecteurPrefModel lecteur = realm.where(LecteurPrefModel.class).equalTo("id", 1).findFirst();
        if (lecteur == null){
            LecteurPrefModel.initLecteur(this);
        }

        try {
            new SearchSong(SplashActivity.this, progress).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }finally {
            new Timer().schedule(new TimerTask(){
                public void run() {
                    startActivity(new Intent(SplashActivity.this, SongList.class));
                }
            }, 2500);
        }






    }

    public void realmMigration(){
        Realm.init(getApplicationContext());
        Realm.deleteRealm(Realm.getDefaultConfiguration());
    }
}