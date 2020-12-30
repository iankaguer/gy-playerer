package com.aztechlabs.gyplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
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

import java.util.List;
import java.util.Random;

import io.realm.Realm;

//fragment du lecteur de music
public class FragLecteur extends Fragment {

    AppCompatImageView playlist, btnPrev, btnPlay, btnNext, btnFav, btnShuffle, btnLoop;
    ImageView albumArt;
    TextView title, artist;
    LinearLayout rootLyt;
    SeekBar seekBar;
    Handler mHandler ;
    public static final String PLAY_AUCHOIX = "com.aztechlabs.gyplayer.PlayAuChoix";
    Realm realm;
    SongPlayer player;
    LecteurPrefModel lecteur;
    String mediaF;
    MediaMetadataRetriever metaRetriver;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.lectfragment, container, false);
        

        

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
        mediaF = lecteur.getLastPlayedUri();

        ((LecteurActivity)getActivity()).updateData(rootLyt, albumArt);
        ((LecteurActivity)getActivity()).letSeek(seekBar);
        ((LecteurActivity)getActivity()).updateData(title, artist);
        
        if (lecteur.isShuffle()){
            btnShuffle.setColorFilter(ContextCompat.getColor(getContext(), R.color.color_tint), android.graphics.PorterDuff.Mode.SRC_IN);

        }
        if (lecteur.isLoop()){
            btnLoop.setColorFilter(ContextCompat.getColor(getContext(), R.color.color_tint), android.graphics.PorterDuff.Mode.SRC_IN);

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
                    btnLoop.setColorFilter(ContextCompat.getColor(getContext(), R.color.color_tint), android.graphics.PorterDuff.Mode.SRC_IN);
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
                    if (mediaF != null){
                        ((LecteurActivity)getActivity()).playAudio(mediaF);
                        btnPlay.setImageResource(R.drawable.ic_pause);
                        metaRetriver.setDataSource(mediaF);
                        seekBar.setMax(Integer.parseInt(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
                        ((LecteurActivity)getActivity()).letSeek(seekBar);

                    }else {
                        List<SongModel> listSons = realm.where(SongModel.class).findAll();
                        SongModel son = listSons.get(new Random().nextInt(listSons.size()));
                        mediaF = son.getUri();
                        ((LecteurActivity)getActivity()).playAudio(mediaF);
                        
                    }
                  

                }



                //////////////////////////////////


            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //playNextSong();
            }
        });
        
    }
    
    @Override
    public void onResume() {
        
        super.onResume();
        
        
    }



    
    
}