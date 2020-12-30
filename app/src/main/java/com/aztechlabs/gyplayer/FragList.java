package com.aztechlabs.gyplayer;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.realm.Realm;

//Fragment de la liste des sons
public class FragList extends Fragment {
    List<SongModel> listSons;
    Realm realm;
    LecteurPrefModel lecteur;
    TextView lartist, ltitre;
    SeekBar lseekBar;
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.listfragment, container, false);

        //initUI();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Realm.init(getContext());
    
        lartist = view.findViewById(R.id.lartist);
        ltitre = view.findViewById(R.id.ltitre);
        lseekBar = view.findViewById(R.id.lseekbar);
        
    
        ((LecteurActivity)getActivity()).letSeek(lseekBar);
        ((LecteurActivity)getActivity()).updateData(ltitre, lartist);

        RecyclerView recyclerV = view.findViewById(R.id.recyclerv);
        realm = Realm.getDefaultInstance();
        listSons = realm.where(SongModel.class).findAll();
        lecteur = realm.where(LecteurPrefModel.class).equalTo("id", 1).findFirst();

        SongListAdapter sAdapter = new SongListAdapter(getContext(), listSons);
        recyclerV.setAdapter(sAdapter);
        recyclerV.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerV.addOnItemTouchListener(new CustomTouchListener(getContext(), new onItemClickListener() {
            @Override
            public void onClick(View view, int index) {
                //playAudio(index);
                //Log.e("gp log", "gp le son => "+listSons.get(index));
                SongModel son = listSons.get(index);

                MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
                metaRetriver.setDataSource(son.getUri());
                //String artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                ((LecteurActivity)getActivity()).playAudio(son.getUri());
                //playAudio(son.getUri());

            }
        }));
       
    }
}