package com.aztechlabs.gyplayer;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class LecteurPrefModel extends RealmObject {
    @PrimaryKey
    private long id;
    private boolean isLoop; //READ_ALL, READ_ONE
    private int lastPlayedPosition;
    private String lastPlayedUri;
    private boolean isShuffle;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isLoop() {
        return isLoop;
    }

    public void setIsLoop(boolean isLoop) {
        this.isLoop = isLoop;
    }

    public int getLastPlayedPosition() {
        return lastPlayedPosition;
    }

    public void setLastPlayedPosition(int lastPlayedPosition) {
        this.lastPlayedPosition = lastPlayedPosition;
    }

    public String getLastPlayedUri() {
        return lastPlayedUri;
    }

    public void setLastPlayedUri(String lastPlayedUri) {
        this.lastPlayedUri = lastPlayedUri;
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public void setShuffle(boolean shuffle) {
        isShuffle = shuffle;
    }

    public static void initLecteur(Context ctx){
        Realm.init(ctx);
        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();
        LecteurPrefModel lecteur = new LecteurPrefModel();
        lecteur.setId(1);
        lecteur.setIsLoop(true);
        lecteur.setLastPlayedPosition(0);
        lecteur.setLastPlayedUri("");
        lecteur.setShuffle(false);

        realm.copyToRealmOrUpdate(lecteur);
        realm.commitTransaction();

    }
}
