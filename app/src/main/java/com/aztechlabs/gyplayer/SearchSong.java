package com.aztechlabs.gyplayer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ProgressBar;

import java.util.ArrayList;

import io.realm.Realm;

//Tache asynchrone de la mise à jour des sons (et leurs enregistrements dans la base)
public class SearchSong extends AsyncTask<Void, Integer, Void> {
    private Context ctx;
    private ProgressBar progres;
    private int progresStatus = 0;

    public SearchSong (Context context, ProgressBar progres){
        this.ctx = context;
        this.progres = progres;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] selection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST
        };
        

        Cursor cr = ctx.getContentResolver().query(uri, selection, null, null, null);
        //Log.e("song uril", listPath+"");
        if (cr != null){
            while (cr.moveToNext()){
                publishProgress(cr.getColumnCount());
                String title = cr.getString(0);
                String chemin = cr.getString(1);
                String artist = cr.getString(2);

                /*Log.e("curseur titre => ", title);
                Log.e("curseur chemin=> ", chemin);*/

                if (!SongModel.iSInDBsong(ctx, title, chemin)){
                    Realm.init(ctx);
                    
                    Realm realm= Realm.getDefaultInstance();
                    Number maxId = realm.where(SongModel.class).max("id");
                    int nextId = (maxId == null) ? 1 : maxId.intValue() + 1;
                    SongModel song = new SongModel();

                    realm.beginTransaction();

                    song.setId(nextId);
                    song.setName(title);
                    song.setArtist(artist);
                    song.setUri(chemin);

                    realm.copyToRealmOrUpdate(song);
                    realm.commitTransaction();
                }

            }
        }
        return null;
    }

    @Override
    public void onPostExecute(Void result) {


    }


    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
       progres.setProgress(values[0], true);
    }
    
   
    
}
