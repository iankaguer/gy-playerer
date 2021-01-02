package com.aztechlabs.gyplayer;

import android.app.SearchManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.realm.Realm;

import static android.content.Context.SENSOR_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;
import static java.lang.Character.getType;

//Fragment de la liste des sons
public class FragList extends Fragment {
    List<SongModel> listSons;
    Realm realm;
    LecteurPrefModel lecteur;
    TextView lartist, ltitre;
    SeekBar lseekBar;
    SongListAdapter sAdapter;
    AppCompatImageView searchbtn, backbtn, reloadBtn;
    private static final int STANDARD_APPBAR = 0;
    private static final int SEARCH_APPBAR = 1;
    private int mAppBarState;
    LinearLayout appBarStandard, appBarSearch;
    EditText editTextSearch;
    RecyclerView recyclerV;
    ProgressBar progressb;
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.listfragment, container, false);

        //initUI();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Realm.init(getContext());
    
        lartist = view.findViewById(R.id.lartist);
        ltitre = view.findViewById(R.id.ltitre);
        lseekBar = view.findViewById(R.id.lseekbar);
        searchbtn = view.findViewById(R.id.searchbtn);
        backbtn = view.findViewById(R.id.backbtn);
        appBarSearch = view.findViewById(R.id.barlytsearch);
        appBarStandard = view.findViewById(R.id.barlytstandard);
        editTextSearch = view.findViewById(R.id.edittextsearch);
        reloadBtn = view.findViewById(R.id.reloadbtn);
        progressb = view.findViewById(R.id.progressb);
        
        
    
        ((LecteurActivity)getActivity()).letSeek(lseekBar);
        ((LecteurActivity)getActivity()).updateData(ltitre, lartist);

        recyclerV = view.findViewById(R.id.recyclerv);
        realm = Realm.getDefaultInstance();
        listSons = realm.where(SongModel.class).findAll();
        lecteur = realm.where(LecteurPrefModel.class).equalTo("id", 1).findFirst();
    
        setAppBarState(STANDARD_APPBAR);

        sAdapter = new SongListAdapter(getContext(), listSons);
        recyclerV.setAdapter(sAdapter);
        recyclerV.setLayoutManager(new LinearLayoutManager(getContext()));
    
        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleToolBarState();
            }
        });
    
        reloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressb.setVisibility(View.VISIBLE);
                try {
                    new SearchSong(getContext(), progressb).execute().get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                    
                }finally {
                    listSons = realm.where(SongModel.class).findAll();
                    recyclerV.setLayoutManager(new LinearLayoutManager(getActivity()));
                    sAdapter = new SongListAdapter(getContext(), listSons);
                    recyclerV.setAdapter(sAdapter);
                    sAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Liste Mise à jour", Toast.LENGTH_SHORT).show();
                    progressb.setVisibility(View.GONE);
                }
            }
        });
    
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleToolBarState();
            }
        });
        

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
    
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        
            }
    
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                charSequence = charSequence.toString().toLowerCase();
    
                final List<SongModel> filteredList = new ArrayList<>();
    
                for (int number = 0; number < listSons.size(); number++) {
        
                    final String name = listSons.get(number).getName();
                    final String artist = listSons.get(number).getArtist();
        
        
        
                    if (name.toLowerCase().contains(charSequence)||artist.toLowerCase().contains(charSequence)) {
            
                        filteredList.add(listSons.get(number));
                    }
        
        
                    //
        
                }
                recyclerV.setLayoutManager(new LinearLayoutManager(getActivity()));
                sAdapter = new SongListAdapter(getContext(), filteredList);
                recyclerV.setAdapter(sAdapter);
                sAdapter.notifyDataSetChanged();
            
            }
    
            @Override
            public void afterTextChanged(Editable editable) {
        
            }
        });
       
    }
    
    @Override
    public void onResume() {
        super.onResume();
        setAppBarState(STANDARD_APPBAR);
    }
    
    
    private void toggleToolBarState() {
        if (mAppBarState == STANDARD_APPBAR) {
            setAppBarState(SEARCH_APPBAR);
        } else {
            setAppBarState(STANDARD_APPBAR);
        }
    }
    
    private void setAppBarState(int state) {
        
        
        mAppBarState = state;
        if (mAppBarState == STANDARD_APPBAR) {
            appBarSearch.setVisibility(View.GONE);
            appBarStandard.setVisibility(View.VISIBLE);
            
            View view = getView();
            InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                im.hideSoftInputFromWindow(view.getWindowToken(), 0); // make keyboard hide
            } catch (NullPointerException e) {
                Log.d("TAG", "setAppbarState Exception: " + e.getMessage());
            }
        } else if (mAppBarState == SEARCH_APPBAR) {
            appBarStandard.setVisibility(View.GONE);
            appBarSearch.setVisibility(View.VISIBLE);
            InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            im.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0); // make keyboard popup
    
        }
    }
    
}