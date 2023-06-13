package com.example.reproductormusica.Modelos;


public class ListaReproduccionModel {

    private String lst_name;
    private String lst_img;
    private String userName;
    private CancionModel lst_canciones;

    public ListaReproduccionModel(){

    }
    public ListaReproduccionModel(String lst_name, String lst_img, String userName) {
        this.lst_name = lst_name;
        this.lst_img = lst_img;
        this.userName = userName;

    }

    public ListaReproduccionModel(String lst_name, String lst_img, String userName, CancionModel lst_canciones) {
        this.lst_name = lst_name;
        this.lst_img = lst_img;
        this.userName = userName;
        this.lst_canciones = lst_canciones;
    }

    public String getLst_name() {
        return lst_name;
    }

    public void setLst_name(String lst_name) {
        this.lst_name = lst_name;
    }

    public String getLst_img() {
        return lst_img;
    }

    public void setLst_img(String lst_img) {
        this.lst_img = lst_img;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public CancionModel getLst_canciones() {
        return lst_canciones;
    }

    public void setLst_canciones(CancionModel lst_canciones) {
        this.lst_canciones = lst_canciones;
    }


}
