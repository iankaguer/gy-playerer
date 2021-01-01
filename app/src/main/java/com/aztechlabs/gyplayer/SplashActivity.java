package com.aztechlabs.gyplayer;

import android.Manifest;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.airbnb.lottie.LottieAnimationView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
//Activité introduction
public class SplashActivity extends AppCompatActivity {
    int PERMISSIONWSTO = 1101;
    int PERMISSIONRSTO = 1102;
    int PERMISSIONPHONE = 1103;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ProgressBar progress = findViewById(R.id.progress);
        progress.setProgress(1, true);
        //Animation du logo
        LottieAnimationView lottieAnimationView = (LottieAnimationView) findViewById(R.id.animation);
        lottieAnimationView.setAnimation("gyplayer.json");
        lottieAnimationView.loop(false); lottieAnimationView.playAnimation();
    
        new Timer().schedule(new TimerTask(){
            public void run() {
                //Verification des autorisations
                //Lecture de contenu
                if (ActivityCompat.checkSelfPermission( SplashActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SplashActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONRSTO);
                }
                //ecriture de contenu
                if (ActivityCompat.checkSelfPermission( SplashActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SplashActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONWSTO);
                }
                //Etat du telephone de contenu : Pour controler le volume du son lors des appels
                if (ActivityCompat.checkSelfPermission( SplashActivity.this,
                        Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SplashActivity.this,
                            new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONPHONE);
                }
    
                //initiation de la base de données et check & initiation de la dernière lecture
                Realm.init(SplashActivity.this);
                Realm realm = Realm.getDefaultInstance();
                LecteurPrefModel lecteur = realm.where(LecteurPrefModel.class).equalTo("id", 1).findFirst();
                if (lecteur == null){
                    LecteurPrefModel.initLecteur(SplashActivity.this);
                }
                progress.setProgress(10, true);
    
                //Mise à jour de la liste des sons et lancement de l'activité principale
                try {
                    new SearchSong(SplashActivity.this, progress).execute().get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    progress.setProgress(100, true);
                    new Timer().schedule(new TimerTask(){
                        public void run() {
                            startActivity(new Intent(SplashActivity.this, LecteurActivity.class));
                        }
                    }, 500);
                }
            
            }
        }, 3100);


        

    }

    //Requete de destruction de la base de données
    public void realmMigration(){
        Realm.init(getApplicationContext());
        Realm.deleteRealm(Realm.getDefaultConfiguration());
    }
}