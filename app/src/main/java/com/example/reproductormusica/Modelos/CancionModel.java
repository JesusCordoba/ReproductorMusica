package com.example.reproductormusica.Modelos;

import java.io.Serializable;

public class CancionModel implements Serializable {
    private String path;
    private String img;
    private String title;
    private String artista;
    private String genero;
    private String duration;
    private String userName;


    public CancionModel() {
    }

    public CancionModel(String path, String title, String duration) {
        this.path = path;
        this.title = title;
        this.duration = duration;
    }

    public CancionModel(String path, String img, String title, String artista, String genero, String duration) {
        this.path = path;
        this.img = img;
        this.title = title;
        this.artista = artista;
        this.genero = genero;
        this.duration = duration;
    }

    public CancionModel(String userName,String path, String img, String title, String artista, String genero, String duration) {
        this.userName = userName;
        this.path = path;
        this.img = img;
        this.title = title;
        this.artista = artista;
        this.genero = genero;
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

