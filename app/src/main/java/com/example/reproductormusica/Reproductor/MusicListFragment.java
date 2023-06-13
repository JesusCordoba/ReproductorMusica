package com.example.reproductormusica.Reproductor;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.reproductormusica.DB.DB;
import com.example.reproductormusica.Modelos.CancionModel;
import com.example.reproductormusica.R;
import com.example.reproductormusica.adapters.MusicListAdapter;

import java.util.ArrayList;

public class MusicListFragment extends Fragment {

    public MusicListFragment() {
        // Required empty public constructor
    }

    public static MusicListFragment newInstance(String usuario) {
        MusicListFragment fragment = new MusicListFragment();
        user = usuario;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private View view;
    private ArrayList<CancionModel> songsList = new ArrayList<>();
    static String user;
    private RecyclerView recyclerView;
    static MusicListAdapter adapter;
    private DB db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_music_list, container, false);

        // Iniciar base de datos
        db = new DB(getContext());

        // Crear adaptador
        recyclerView = view.findViewById(R.id.recycler_account);
        View rootView = view.findViewById(android.R.id.content);
        adapter = new MusicListAdapter(songsList,getContext(), rootView, "", "", "account");

        // Añade canciones a la lista
        setSongs();

        if(songsList.size()!=0){
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
        }

        return view;
    }
    public void setSongs(){
        ArrayList<CancionModel> lista = db.listarcanciones(user);
        for (CancionModel cancion : lista) {
            songsList.add(cancion);
        }
    }


    public void randomList(){
        if (adapter != null){
            adapter.randomSongList();
        }
    }
    public void searchList(String searchText){
        if (adapter!= null){
            adapter.search(searchText);
        }
    }
}