package com.aztechlabs.gyplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.vansuita.gaussianblur.GaussianBlur;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//fragment du lecteur de music
public class FragLecteur extends Fragment {

    AppCompatImageView  btnPrev, btnPlay, btnNext, btnFav, btnShuffle, btnLoop;
    ImageView albumArt;
    TextView title, artist;
    LinearLayout rootLyt;
    SeekBar seekBar;
    Handler mHandler ;
    Realm realm;
    SongPlayer player;
    LecteurPrefModel lecteur;
    String mediaF;
    MediaMetadataRetriever metaRetriver;
    List<SongModel> listSons;
    int audioIndex = 0;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.lectfragment, container, false);
        return view;
       

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Realm.init(getContext());
        realm = Realm.getDefaultInstance();
        lecteur = realm.where(LecteurPrefModel.class).equalTo("id", 1).findFirst();
        player = ((LecteurActivity)getActivity()).player;
        
        metaRetriver = new MediaMetadataRetriever();
        
        btnPrev = view.findViewById(R.id.btn_prev);
        btnPlay = view.findViewById(R.id.btn_play);
        btnNext = view.findViewById(R.id.btn_next);
        btnFav = view.findViewById(R.id.btn_fav);
        btnShuffle = view.findViewById(R.id.btn_shuffle);
        btnLoop = view.findViewById(R.id.btn_loop);
        albumArt = view.findViewById(R.id.album_art);
        title = view.findViewById(R.id.title);
        artist = view.findViewById(R.id.artiste);
        rootLyt = view.findViewById(R.id.rootlyt);
        seekBar = view.findViewById(R.id.seekbar);
        mHandler = new Handler();
        seekBar.setProgress(0);
    
        //updateInfo(player.mediaFile);
    
        
        
    
        listSons = ((LecteurActivity)getActivity()).listSons;
        ((LecteurActivity)getActivity()).updateData(rootLyt, albumArt);
        ((LecteurActivity)getActivity()).letSeek(seekBar);
        
        mediaF = lecteur.getLastPlayedUri();
        justUpdateThere(mediaF);
        
        
        ((LecteurActivity)getActivity()).updateData(title, artist);
        if (((LecteurActivity)getActivity()).serviceBound){
            if (player.mediaPlayer.isPlaying()){
                btnPlay.setBackgroundResource(R.drawable.ic_pause);
            }
        }
        if (lecteur.isShuffle()){
            btnShuffle.setColorFilter(ContextCompat.getColor(getContext(), R.color.color_tint), android.graphics.PorterDuff.Mode.SRC_IN);

        }
        if (lecteur.isLoop()){
            btnLoop.setColorFilter(ContextCompat.getColor(getContext(), R.color.color_tint), android.graphics.PorterDuff.Mode.SRC_IN);

        }
        
        
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lecteur = realm.where(LecteurPrefModel.class).equalTo("id", 1).findFirst();
                
                if (((LecteurActivity)getActivity()).serviceBound) {
                    ((LecteurActivity) getActivity()).player.skipToNext();
                }else {
                    if (lecteur.isShuffle()){
                        SongModel nextSong = listSons.get(new Random().nextInt(listSons.size()));
                        mediaF = nextSong.getUri();
                        Log.e("index1 isShuffle", mediaF);
                    }else {
                        mediaF = lecteur.getLastPlayedUri();
                        
                        for (int i=0; i<listSons.size(); i++){
                            Log.e("index booucle", audioIndex+"");
                            if (listSons.get(i).getUri() == mediaF){
                                audioIndex = i;
                                Log.e("index trouvé", audioIndex+"");
                            }
                        }
                        audioIndex = audioIndex +1;

                        if (audioIndex == listSons.size()){
                            audioIndex = 0;
                            mediaF = listSons.get(audioIndex).getUri();
                            
                        }else {
                            Log.e("index1 media", audioIndex +" "+ listSons.size());
                            mediaF = listSons.get(audioIndex).getUri();
                            
                        }
                    }
                    //Log.e("index2 media avant enregistrement", mediaF);
                    realm.beginTransaction();
                    lecteur.setLastPlayedUri(mediaF);
                    realm.copyToRealmOrUpdate(lecteur);
                    realm.commitTransaction();
                    justUpdateThere(mediaF);
                    
                }
            }
        });
    
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lecteur = realm.where(LecteurPrefModel.class).equalTo("id", 1).findFirst();
            
                if (((LecteurActivity)getActivity()).serviceBound) {
                    ((LecteurActivity) getActivity()).player.skipToPrev();
                }else {
                    if (lecteur.isShuffle()){
                        SongModel nextSong = listSons.get(new Random().nextInt(listSons.size()));
                        mediaF = nextSong.getUri();
                        Log.e("index1 isShuffle", mediaF);
                    }else {
                        mediaF = lecteur.getLastPlayedUri();
                    
                        for (int i=0; i<listSons.size(); i++){
                            if (listSons.get(i).getUri() == mediaF){
                                audioIndex = i;
                            }
                        }
                        audioIndex = audioIndex -1;
                    
                        if (audioIndex < 0){
                            audioIndex = listSons.size()-1;
                            mediaF = listSons.get(audioIndex).getUri();
                        }else {
                            mediaF = listSons.get(audioIndex).getUri();
                        }
                    }
                    //Log.e("index2 media avant enregistrement", mediaF);
                    realm.beginTransaction();
                    lecteur.setLastPlayedUri(mediaF);
                    realm.copyToRealmOrUpdate(lecteur);
                    realm.commitTransaction();
                    justUpdateThere(mediaF);
                
                }
            }
        });

        btnLoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lecteur = realm.where(LecteurPrefModel.class).equalTo("id", 1).findFirst();
                if (lecteur.isLoop()){
                    btnLoop.setColorFilter(null);
                    realm.beginTransaction();
                    lecteur.setIsLoop(false);
                    realm.copyToRealmOrUpdate(lecteur);
                    realm.commitTransaction();
                }else{
                    btnLoop.setColorFilter(ContextCompat.getColor(getContext(), R.color.color_tint), android.graphics.PorterDuff.Mode.SRC_IN);
                    realm.beginTransaction();
                    lecteur.setIsLoop(true);
                    realm.copyToRealmOrUpdate(lecteur);
                    realm.commitTransaction();
                }
            }
        });


        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lecteur = realm.where(LecteurPrefModel.class).equalTo("id", 1).findFirst();
                if (lecteur.isShuffle()){
                    btnShuffle.setColorFilter(null);

                    realm.beginTransaction();
                    lecteur.setShuffle(false);
                    realm.copyToRealmOrUpdate(lecteur);
                    realm.commitTransaction();
                }else{
                    btnShuffle.setColorFilter(ContextCompat.getColor(getContext(), R.color.color_tint), android.graphics.PorterDuff.Mode.SRC_IN);
                    realm.beginTransaction();
                    lecteur.setShuffle(true);
                    realm.copyToRealmOrUpdate(lecteur);
                    realm.commitTransaction();
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((LecteurActivity)getActivity()).serviceBound){
                    if (((LecteurActivity)getActivity()).player.mediaPlayer.isPlaying()){
                        ((LecteurActivity)getActivity()).player.mediaPlayer.pause();
                        btnPlay.setImageResource(R.drawable.ic_play);
                    }else {
                        ((LecteurActivity)getActivity()).player.mediaPlayer.start();
                        btnPlay.setImageResource(R.drawable.ic_pause);
                    }

                }else {
                     
                        ((LecteurActivity)getActivity()).playAudio(mediaF);
                }

            }
        });

       
        
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (((LecteurActivity)getActivity()).serviceBound){
        
        }else {
            justUpdateThere(mediaF);
        }
        
        
        
        
    }
    
    private void justUpdateThere( String path){
        if (path!=null || path != ""){
            metaRetriver.setDataSource(path);
            seekBar.setMax(Integer.parseInt(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
            artist.setText(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            title.setText(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            //title.setText(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            //title.setText(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
    
            // Update the current
            byte [] data = metaRetriver.getEmbeddedPicture();
            if(data !=null){
                Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
                if (b != null){
                    albumArt.setImageBitmap(b);
                    Bitmap blurredBitmap = GaussianBlur.with(getContext()).render(b);
                    BitmapDrawable ob = new BitmapDrawable(getResources(), blurredBitmap);
                    rootLyt.setBackground(ob);
                }
            }
        }
        
        
        
    }
    
    
    
}


