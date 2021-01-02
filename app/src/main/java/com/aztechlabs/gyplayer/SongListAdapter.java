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
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.RealmResults;
//Adapter de la list des sons
public class SongListAdapter extends RecyclerView.Adapter<SongListHolder> {
    List<SongModel> songs;
    Context ctx;
    List<SongModel> songsFiltered;

    public SongListAdapter(Context context , List<SongModel> songs) {
        this.songs = songs;
        this.ctx=context;
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
