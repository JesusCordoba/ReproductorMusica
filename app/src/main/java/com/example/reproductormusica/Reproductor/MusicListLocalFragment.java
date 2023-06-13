package com.example.reproductormusica.Reproductor;


import android.database.Cursor;
import android.os.Bundle;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.reproductormusica.DB.DB;
import com.example.reproductormusica.Modelos.CancionModel;
import com.example.reproductormusica.R;
import com.example.reproductormusica.adapters.MusicListAdapter;

import java.io.File;
import java.util.ArrayList;


public class MusicListLocalFragment extends Fragment {

    public MusicListLocalFragment() {
        // Required empty public constructor
    }

    public static MusicListLocalFragment newInstance(String usuario) {
        MusicListLocalFragment fragment = new MusicListLocalFragment();
        user = usuario;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
    private ArrayList<CancionModel> songsList = new ArrayList<>();
    static String user;
    private RecyclerView recyclerView;
    static MusicListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music_list_local, container, false);

        recyclerView = view.findViewById(R.id.recycler_local);
        // Crear adaptador
        View rootView = view.findViewById(android.R.id.content);
        adapter = new MusicListAdapter(songsList,getContext(), rootView, "", "", "local");

        // Añade canciones a la lista
        setSongs();


        if (songsList.size() == 0) {

        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
        }

        return view;
    }
    public void setSongs(){

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
                MediaStore.Audio.Media.DATA + " NOT LIKE ?";
        String[] selectionArgs = new String[]{"%ReproductorMusica/Musica%"};

        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        Cursor cursor = getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );

        while (cursor.moveToNext()) {
            CancionModel songData = new CancionModel(cursor.getString(1), cursor.getString(0), cursor.getString(2));
            if (new File(songData.getPath()).exists()) {
                songsList.add(songData);
            }
        }



    }
    public void randomList(){
        if (adapter!= null){
            adapter.randomSongList();
        }
    }

    public void searchList(String searchText){
        if (adapter!= null){
            adapter.search(searchText);
        }
    }
}