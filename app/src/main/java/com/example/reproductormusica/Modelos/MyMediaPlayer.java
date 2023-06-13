package com.example.reproductormusica.Modelos;

import android.media.MediaPlayer;

import java.util.ArrayList;

public class MyMediaPlayer {

    static MediaPlayer instance;
    public static int currentIndex = -1;
    public static ArrayList<CancionModel> songsList;

    public static MediaPlayer getInstance(){
        if(instance == null){
            instance = new MediaPlayer();
        }
        return instance;
    }

    public static ArrayList<CancionModel> getSongsList() {
        return songsList;
    }

    public static void setSongsList(ArrayList<CancionModel> songsList) {
        MyMediaPlayer.songsList = songsList;
    }
}
