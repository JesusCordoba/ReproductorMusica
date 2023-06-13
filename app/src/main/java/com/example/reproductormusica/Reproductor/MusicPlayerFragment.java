package com.example.reproductormusica.Reproductor;


import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.reproductormusica.Modelos.CancionModel;
import com.example.reproductormusica.Modelos.MyMediaPlayer;
import com.example.reproductormusica.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class MusicPlayerFragment extends Fragment {

    private TextView titleTv;
    private ImageView pausePlay,nextBtn,previousBtn;
    private ArrayList<CancionModel> songsList;
    private CancionModel currentSong;
    private MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    private View view;
    private Context context;
    int x=0;

    public MusicPlayerFragment(Context context) {
        // Required empty public constructor
        this.context = context;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_music_player_mini, container, false);

        titleTv = view.findViewById(R.id.song_title);
        pausePlay = view.findViewById(R.id.pause_play);
        nextBtn = view.findViewById(R.id.next);
        previousBtn = view.findViewById(R.id.previous);
        // Obtener los argumentos pasados al fragmento
        Bundle bundle = getArguments();
        if (bundle != null) {
            songsList = (ArrayList<CancionModel>) bundle.getSerializable("LIST");
            // Realizar las operaciones necesarias con la lista de canciones
            // ...
        }

        //setResourcesWithMusic();
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songsList != null) {
                    playNextSong();
                }
            }
        });
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songsList != null) {
                    playPreviousSong();
                }
            }
        });
        pausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songsList != null){
                    pausePlay();
                }

            }
        });

        titleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songsList != null){
                    Intent intent = new Intent(context, MusicPlayerActivity.class);
                    intent.putExtra("LIST", songsList);
                    intent.putExtra("CURRENT_POSITION", MyMediaPlayer.currentIndex);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer!=null){

                    if(mediaPlayer.isPlaying()){
                        pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                        songsList = MyMediaPlayer.getSongsList();
                        setResourcesWithMusic();
                    }else{
                        pausePlay.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                    }

                }
                new Handler().postDelayed(this,100);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    void setResourcesWithMusic(){
        currentSong = songsList.get(MyMediaPlayer.currentIndex);

        titleTv.setText(currentSong.getTitle());

    }

    private void playMusic(){
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playNextSong(){
        if(MyMediaPlayer.currentIndex== songsList.size()-1)
            return;
        MyMediaPlayer.currentIndex +=1;
        mediaPlayer.reset();
        setResourcesWithMusic();
        playMusic();
    }

    private void playPreviousSong(){
        if(MyMediaPlayer.currentIndex== 0)
            return;
        MyMediaPlayer.currentIndex -=1;
        mediaPlayer.reset();
        setResourcesWithMusic();
        playMusic();
    }

    private void pausePlay(){
        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();
    }


}

