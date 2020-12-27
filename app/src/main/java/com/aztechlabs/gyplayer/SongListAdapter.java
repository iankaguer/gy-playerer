package com.aztechlabs.gyplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import io.realm.RealmResults;

public class SongListAdapter extends RecyclerView.Adapter<SongListHolder> {
    List<SongModel> songs;
    Context ctx;
    private SongPlayer player;
    boolean serviceBound;

    public SongListAdapter(Context context , List<SongModel> songs, SongPlayer player, boolean serviceBound) {
        this.songs = songs;
        this.ctx=context;
        this.serviceBound=serviceBound;
        this.player = player;
    }

    @NonNull
    @Override
    public SongListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(ctx);

        // Inflate the custom layout
        View itemSon = inflater.inflate(R.layout.item_song, parent, false);

        // Return a new holder instance
        SongListHolder viewHolder = new SongListHolder(itemSon);
        return viewHolder;
    }

   // @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onBindViewHolder(@NonNull SongListHolder holder, int position) {
        //Log.e("lesSons", songs+"");
        SongModel song = songs.get(position);



            /*MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(ctx, Uri.parse(song.getUri()));
            String ss = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            byte [] data = mmr.getEmbeddedPicture();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            //Bitmap bitmap = BitmapFactory.decodeResource(ctx.getResources(), mmr.getPrimaryImage());
            //Bitmap bitmap = mmr.getPrimaryImage();
            mmr.release();*/
            Bitmap bitmap = null;
            try {
                bitmap = retrieveAlbumArt(song.getUri());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }


        holder.titre.setText(song.getName());
            if (song.isIsfavorite()){
                holder.isFav.setImageResource(R.drawable.ic_fav);
            }
            if (bitmap !=null){
                holder.albumcover.setImageBitmap(bitmap);
            }

            holder.artist.setText(song.getArtist());




    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public ServiceConnection servConnection = new ServiceConnection() {
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

    public static Bitmap retrieveAlbumArt(String audioPath) throws Throwable {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(audioPath, new HashMap<String, String>());

            byte [] data = mediaMetadataRetriever.getEmbeddedPicture();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.getMessage());

        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }


}
