package com.aztechlabs.gyplayer;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

//Model ou table d'enregistrement de la liste des sons
public class SongModel extends RealmObject {
    @PrimaryKey
    private long id;
    private String name;
    private String uri;
    private String artist;
    private boolean isfavorite;

    public SongModel() {
        isfavorite = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isIsfavorite() {
        return isfavorite;
    }

    public void setIsfavorite(boolean isfavorite) {
        this.isfavorite = isfavorite;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public static boolean iSInDBsong(Context ctx, String name, String path){
        Realm.init(ctx);
        Realm realm = Realm.getDefaultInstance();
        SongModel songModel = realm.where(SongModel.class)
                .equalTo("name", name)
                .equalTo("uri", path)
                .findFirst();
        if(songModel != null){
            return true;
        }
        return false;
    }
}
