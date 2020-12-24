package com.aztechlabs.gyplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import io.realm.Realm;
import io.realm.RealmResults;

public class SongList extends AppCompatActivity {

    AppCompatImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        RecyclerView recyclerV = findViewById(R.id.recyclerv);
        Realm realm = Realm.getDefaultInstance();
        RealmResults listSons = realm.where(SongModel.class).findAll();

        SongListAdapter sAdapter = new SongListAdapter(SongList.this, listSons);
        recyclerV.setAdapter(sAdapter);
        recyclerV.setLayoutManager(new LinearLayoutManager(this));



        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SongList.this, MainActivity.class));
            }
        });
    }
}