package com.aztechlabs.gyplayer;

import android.annotation.SuppressLint;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.gauravk.bubblenavigation.BubbleNavigationLinearView;
import com.vansuita.gaussianblur.GaussianBlur;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
///Activité principale
public class LecteurActivity extends AppCompatActivity {
 
  ViewPager viewPager;
  BubbleNavigationLinearView equal_navigation_bar;
    public static final String _PLAY_NEW_SONG = "com.aztechlabs.gyplayer.PlayNewAudio";
    AppCompatImageView back;
    public SongPlayer player;
    boolean serviceBound = false;
    List<SongModel> listSons;
    Realm realm;
    LecteurPrefModel lecteur;
    MediaMetadataRetriever metaRetriver;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_lecteur);
      metaRetriver = new MediaMetadataRetriever();
    
      Realm.init(this);
    
      realm = Realm.getDefaultInstance();
      listSons = realm.where(SongModel.class).findAll();
      lecteur = realm.where(LecteurPrefModel.class).equalTo("id", 1).findFirst();

    viewPager = findViewById(R.id.containers);
    equal_navigation_bar = findViewById(R.id.equal_navigation_bar);
   initUI();

  }
    
 //si on press "BACK" sortir de l'app
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
    //arret du service
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            //unbindService(serviceConnection);
            //service is active
            if (player !=null){
                player.stopSelf();
            }
            
        }
    }
    //conncetion au service
    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SongPlayer.LocalBinder binder = (SongPlayer.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
            Log.e("lets go", "were connected");
            
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            realm.beginTransaction();
            lecteur.setLastPlayedUri(player.mediaFile);
            lecteur.setLastPlayedPosition(player.mediaPlayer.getCurrentPosition());
            realm.copyToRealmOrUpdate(lecteur);
            realm.commitTransaction();
            
            serviceBound = false;
        }
    };
  
    
    //lecture de sons :  si aucun son en lecture envoyer au service sinon lui faire parvenir via le broadcast
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
    //fonction qui suit la postion de lecture actuelle et met a jour le seekbar
    public void letSeek(SeekBar seekBar){
        Handler mHandler = new Handler();
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                
                if (serviceBound){
                    seekBar.setMax(player.mediaPlayer.getDuration());
                    int mCurrentPosition = player.mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(mCurrentPosition, true);
                }
                
                mHandler.postDelayed(this, 1000);
            }
        };
        myRunnable.run();
    }
    //mise a jour de la couverture de l'album en fonction de la lecture
    public void updateData(LinearLayout rootLyt, ImageView albumArt){
        Handler mHandler = new Handler();
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Log.e("runnnnn", "running");
            
                if (serviceBound){
    
                    metaRetriver.setDataSource(player.mediaFile);
                    // Update the current
                    byte [] data = metaRetriver.getEmbeddedPicture();
                    Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
                    if (b != null){
                        albumArt.setImageBitmap(b);
                        Bitmap blurredBitmap = GaussianBlur.with(LecteurActivity.this).render(b);
                        BitmapDrawable ob = new BitmapDrawable(getResources(), blurredBitmap);
                        rootLyt.setBackground(ob);
                    }
                
                }
            
                mHandler.postDelayed(this, 1000);
            }
        };
        myRunnable.run();
        
        
    }
    //Mise a jour de titre et nom d'artiste
    public void updateData(TextView title, TextView artist){
        Handler mHandler = new Handler();
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                
                if (serviceBound){
                    metaRetriver.setDataSource(player.mediaFile);
                    title.setText(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                    artist.setText(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                }
                
                mHandler.postDelayed(this, 1000);
            }
        };
        myRunnable.run();
        
        
    }
   
//liaison des fragments a l'activité
 @SuppressLint("ClickableViewAccessibility")
 public void initUI(){
  ArrayList<Fragment> fragList = new  ArrayList<Fragment>();
  fragList.add(new FragLecteur());
  fragList.add(new FragList());
  FragmentManager fragmentManager = getSupportFragmentManager();
  FragmentPagerAdapter fpAdapter = new FragmentPagerAdapter(fragmentManager) {
   @Override
   public Fragment getItem(int position) {
    if (position >= 0 && position < fragList.size()){
     return fragList.get(position);
    }

    return fragList.get(0);
   }

   @Override
   public int getCount() {
    return fragList.size();
   }
  };
  viewPager.setAdapter(fpAdapter);
  viewPager.setOnTouchListener(new View.OnTouchListener() {
   @Override
   public boolean onTouch(View v, MotionEvent event) {
    v.animate();
    return true;
   }
  });
  equal_navigation_bar.setNavigationChangeListener((view, position) -> viewPager.setCurrentItem(position,true));



  //change the initial activate element
  int newInitialPosition = 1;
  equal_navigation_bar.setCurrentActiveItem(newInitialPosition);
  viewPager.setCurrentItem(newInitialPosition, false);

 }
}
