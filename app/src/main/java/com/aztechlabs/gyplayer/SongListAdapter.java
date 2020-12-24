package com.aztechlabs.gyplayer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListHolder> {
    List<SongModel> songs;
    Context ctx;

    public SongListAdapter(Context context ,List<SongModel> songs) {
        songs = songs;
        ctx=context;
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

    @Override
    public void onBindViewHolder(@NonNull SongListHolder holder, int position) {
        SongModel song = songs.get(position);
        File fichierSon = new File(song.getUri());
        if(fichierSon.exists()){
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(song.getUri());
            byte [] data = mmr.getEmbeddedPicture();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);


            holder.titre.setText(song.getName());
            if (song.isIsfavorite()){
                holder.isFav.setImageResource(R.drawable.ic_fav);
            }
            holder.albumcover.setImageBitmap(bitmap);
            holder.artist.setText(song.getArtist());
        }

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
}
