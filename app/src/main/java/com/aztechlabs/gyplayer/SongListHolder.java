package com.aztechlabs.gyplayer;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SongListHolder extends RecyclerView.ViewHolder {

    LinearLayout lytitem;
    ImageView albumcover, isFav;
    TextView titre, artist;
    public SongListHolder(@NonNull View itemView) {
        super(itemView);
        lytitem = itemView.findViewById(R.id.lytitem);
        albumcover = itemView.findViewById(R.id.albumcover);
        isFav = itemView.findViewById(R.id.isfav);
        titre = itemView.findViewById(R.id.titre);
        artist = itemView.findViewById(R.id.artist);
    }
}
