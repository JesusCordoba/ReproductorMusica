package com.example.reproductormusica.DB;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.reproductormusica.Modelos.CancionModel;
import com.example.reproductormusica.Modelos.ListaReproduccionModel;
import com.example.reproductormusica.Modelos.Usuario;

import java.util.ArrayList;

public class DB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "db_ReproductorMusica";
    private static final int DATABASE_VERSION = 50;

    private static final String DATABASE_TABLE_CANCIONES = "tbl_Canciones";
    private static final String DATABASE_TABLE_LISTASREP = "tbl_ListasReproduccion";
    private static final String DATABASE_TABLE_USUARIOS = "tbl_Usuarios";

    public DB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE if not exists " + DATABASE_TABLE_CANCIONES + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "userName TEXT,"
                + "path TEXT,"
                + "img TEXT,"
                + "titulo TEXT,"
                + "artista TEXT,"
                + "genero TEXT,"
                + "duracion TEXT)");

        db.execSQL("CREATE TABLE if not exists " + DATABASE_TABLE_LISTASREP + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "lst_name TEXT,"
                + "lst_img TEXT,"
                + "userName TEXT,"
                + "path TEXT,"
                + "img TEXT,"
                + "titulo TEXT,"
                + "artista TEXT,"
                + "genero TEXT,"
                + "duracion TEXT)");

        db.execSQL("CREATE TABLE if not exists " + DATABASE_TABLE_USUARIOS + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "userName TEXT unique,"
                + "email TEXT,"
                + "password TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_CANCIONES);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_LISTASREP);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_USUARIOS);
        onCreate(db);
    }

    /////////////////////////////////////////////////////////
    /*                      Canciones                      */
    /////////////////////////////////////////////////////////
    public void addCancion(CancionModel cancion, String usuario) {
        if (!verificarCancion(cancion.getTitle(), usuario)) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("userName", usuario);
            contentValues.put("path", cancion.getPath());
            contentValues.put("img", cancion.getImg());
            contentValues.put("titulo", cancion.getTitle());
            contentValues.put("artista", cancion.getArtista());
            contentValues.put("genero", cancion.getGenero());
            contentValues.put("duracion", cancion.getDuration());
            db.insert(DATABASE_TABLE_CANCIONES, null, contentValues);
            db.close();
        }
    }

    public ArrayList<CancionModel> listarcanciones(String user) {
        ArrayList<CancionModel> songList = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + DATABASE_TABLE_CANCIONES + " where " + "userName" + " LIKE \'%" + user + "%\'", null);
        if (cursor.getCount() != 0) {
            if (cursor.moveToFirst()) {
                do {

                    @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex("path"));
                    @SuppressLint("Range") String img = cursor.getString(cursor.getColumnIndex("img"));
                    @SuppressLint("Range") String titulo = cursor.getString(cursor.getColumnIndex("titulo"));
                    @SuppressLint("Range") String artista = cursor.getString(cursor.getColumnIndex("artista"));
                    @SuppressLint("Range") String genero = cursor.getString(cursor.getColumnIndex("genero"));
                    @SuppressLint("Range") String duracion = cursor.getString(cursor.getColumnIndex("duracion"));
                    CancionModel cancion = new CancionModel(path, img, titulo, artista, genero, duracion);
                    songList.add(cancion);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        db.close();
        return songList;
    }

    public void updateCancion(String userName, CancionModel cancion, String nombre_original) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("titulo", cancion.getTitle());
        values.put("artista", cancion.getArtista());
        values.put("genero", cancion.getGenero());
        values.put("img", cancion.getImg());

        String whereClause = "userName = ? AND titulo = ?";
        String[] whereArgs = {userName, nombre_original};
        db.update(DATABASE_TABLE_CANCIONES, values, whereClause, whereArgs);
        db.close();
    }


    public void deleteCancion(String userName, String titulo) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = "userName = ? AND titulo = ?";
        String[] whereArgs = {userName, titulo};
        db.delete(DATABASE_TABLE_CANCIONES, whereClause, whereArgs);
        db.close();
    }


    public boolean verificarImagenDuplicada(String img, String userName) {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + DATABASE_TABLE_CANCIONES + " WHERE img = ? AND userName = ?";
        String[] selectionArgs = {img, userName};

        Cursor cursor = db.rawQuery(query, selectionArgs);

        boolean hayDuplicados = false;
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            hayDuplicados = count > 1;
        }

        cursor.close();
        db.close();

        return hayDuplicados;
    }


    public boolean verificarCancion(String titulo, String userName) {
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = {"id"};
        String selection = "titulo = ? AND userName = ?";
        String[] selectionArgs = {titulo, userName};

        Cursor cursor = db.query(DATABASE_TABLE_CANCIONES, columns, selection, selectionArgs, null, null, null);

        boolean existeCancion = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return existeCancion;
    }

    public boolean verificarPath(String path) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {"path"};
        String selection = "path = ?";
        String[] selectionArgs = {path};

        Cursor cursor = db.query(DATABASE_TABLE_CANCIONES, projection, selection, selectionArgs, null, null, null);
        boolean existePath = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return existePath;
    }



    /////////////////////////////////////////////////////////
    /*                   Listas reproduccion               */
    /////////////////////////////////////////////////////////
    public void addLista(String userName, String lst_name, String img) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("lst_name", lst_name);
        contentValues.put("lst_img", img);
        contentValues.put("userName", userName);
        db.insert(DATABASE_TABLE_LISTASREP, null, contentValues);
        db.close();
    }


    public void addCancionLista(String userName, String lst_name, CancionModel cancion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("lst_name", lst_name);
        contentValues.put("userName", userName);
        contentValues.put("path", cancion.getPath());
        contentValues.put("img", cancion.getImg());
        contentValues.put("titulo", cancion.getTitle());
        contentValues.put("artista", cancion.getArtista());
        contentValues.put("genero", cancion.getGenero());
        contentValues.put("duracion", cancion.getDuration());
        db.insert(DATABASE_TABLE_LISTASREP, null, contentValues);
        db.close();
    }

    public void agregarLista(String userName, ListaReproduccionModel lista, CancionModel cancion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("lst_name", lista.getLst_name());
        contentValues.put("lst_img", lista.getLst_img());
        contentValues.put("userName", userName);

        if (cancion != null) {
            contentValues.put("path", cancion.getPath());
            contentValues.put("img", cancion.getImg());
            contentValues.put("titulo", cancion.getTitle());
            contentValues.put("artista", cancion.getArtista());
            contentValues.put("genero", cancion.getGenero());
            contentValues.put("duracion", cancion.getDuration());
        }

        db.insert(DATABASE_TABLE_LISTASREP, null, contentValues);
        db.close();
    }

    public ArrayList<String> obtenerNombresListas(String user) {
        ArrayList<String> nombresListas = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try {
            // Consulta para obtener todos los nombres de listas de reproducción
            String query = "SELECT " + "lst_name" + " FROM " + DATABASE_TABLE_LISTASREP + " where path IS NULL AND " + "userName" + " LIKE \'%" + user + "%\'";
            cursor = db.rawQuery(query, null);

            // Verificar si hay resultados
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Obtener el nombre de lista de reproducción de cada fila y agregarlo a la lista
                    @SuppressLint("Range") String nombreLista = cursor.getString(cursor.getColumnIndex("lst_name"));
                    nombresListas.add(nombreLista);
                } while (cursor.moveToNext());
            }
        } finally {
            // Cerrar el cursor
            if (cursor != null) {
                cursor.close();
            }
        }

        // Devolver la lista de nombres de listas de reproducción
        return nombresListas;
    }

    public ArrayList<ListaReproduccionModel> listarlistas(String user) {
        ArrayList<ListaReproduccionModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + DATABASE_TABLE_LISTASREP + " where path IS NULL AND " + "userName" + " LIKE \'%" + user + "%\'", null);
        if (cursor.getCount() != 0) {
            if (cursor.moveToFirst()) {
                do {

                    @SuppressLint("Range") String lst_name = cursor.getString(cursor.getColumnIndex("lst_name"));
                    @SuppressLint("Range") String lst_img = cursor.getString(cursor.getColumnIndex("lst_img"));
                    @SuppressLint("Range") String userName = cursor.getString(cursor.getColumnIndex("userName"));
                    ListaReproduccionModel lista = new ListaReproduccionModel(lst_name, lst_img, userName);
                    list.add(lista);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        //db.close();
        return list;
    }

    public ArrayList<ListaReproduccionModel> todaslaslistas(String user) {
        ArrayList<ListaReproduccionModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + DATABASE_TABLE_LISTASREP + " where " + "userName" + " LIKE \'%" + user + "%\'", null);
        if (cursor.getCount() != 0) {
            if (cursor.moveToFirst()) {
                do {

                    @SuppressLint("Range") String lst_name = cursor.getString(cursor.getColumnIndex("lst_name"));
                    @SuppressLint("Range") String lst_img = cursor.getString(cursor.getColumnIndex("lst_img"));
                    @SuppressLint("Range") String userName = cursor.getString(cursor.getColumnIndex("userName"));
                    ListaReproduccionModel lista = new ListaReproduccionModel(lst_name, lst_img, userName);
                    list.add(lista);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        //db.close();
        return list;
    }

    public ArrayList<CancionModel> listarCanciones(String user, String lst_nombre) {
        ArrayList<CancionModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + DATABASE_TABLE_LISTASREP + " where path IS NOT NULL AND " + "userName" + " LIKE \'%" + user + "%\' AND lst_name LIKE \'%" + lst_nombre + "%\' ", null);
        if (cursor.getCount() != 0) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex("path"));
                    @SuppressLint("Range") String img = cursor.getString(cursor.getColumnIndex("img"));
                    @SuppressLint("Range") String titulo = cursor.getString(cursor.getColumnIndex("titulo"));
                    @SuppressLint("Range") String artista = cursor.getString(cursor.getColumnIndex("artista"));
                    @SuppressLint("Range") String genero = cursor.getString(cursor.getColumnIndex("genero"));
                    @SuppressLint("Range") String duracion = cursor.getString(cursor.getColumnIndex("duracion"));
                    CancionModel cancion = new CancionModel(path, img, titulo, artista, genero, duracion);
                    list.add(cancion);
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
        db.close();
        return list;
    }

    public void deleteCancionLista(String titulo, String lstNombre, String userName) {
        SQLiteDatabase db = getWritableDatabase();
        // Definir la cláusula WHERE para filtrar los registros a eliminar
        String whereClause = "titulo = ? AND lst_name = ? AND userName = ?";
        String[] whereArgs = {titulo, lstNombre, userName};
        // Eliminar los registros que coincidan con los valores proporcionados
        db.delete(DATABASE_TABLE_LISTASREP, whereClause, whereArgs);
        // Cerrar la conexión a la base de datos
        db.close();
    }

    public void deleteCancionListas(String titulo, String userName) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = "titulo = ? AND userName = ?";
        String[] whereArgs = {titulo, userName};
        db.delete(DATABASE_TABLE_LISTASREP, whereClause, whereArgs);
        db.close();
    }

    public void updateCancionLista(CancionModel cancion, String nombre_original, String userName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("titulo", cancion.getTitle());
        values.put("artista", cancion.getArtista());
        values.put("genero", cancion.getGenero());
        values.put("img", cancion.getImg());

        String whereClause = "titulo = ? AND userName = ?";
        String[] whereArgs = {nombre_original, userName};
        db.update(DATABASE_TABLE_LISTASREP, values, whereClause, whereArgs);
        db.close();
    }


    public void updateList(String lst_name_original, String lst_name, String lst_image, String userName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("lst_name", lst_name);
        values.put("lst_img", lst_image);
        values.put("userName", userName);

        String whereClause = "lst_name = ?";
        String[] whereArgs = {String.valueOf(lst_name_original)};
        db.update(DATABASE_TABLE_LISTASREP, values, whereClause, whereArgs);
        db.close();
    }

    public void deleteList(String lstName, String userName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "lst_name = ? AND userName = ?";
        String[] whereArgs = {lstName, userName};
        db.delete(DATABASE_TABLE_LISTASREP, whereClause, whereArgs);
        db.close();
    }


    public boolean verificarImagenListaDuplicada(String img, String userName) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + DATABASE_TABLE_LISTASREP + " WHERE img = ? AND userName = ?";
        String[] selectionArgs = {img, userName};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        boolean hayDuplicados = false;
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            hayDuplicados = count > 1;
        }
        cursor.close();
        return hayDuplicados;
    }


    /////////////////////////////////////////////////////////
    /*                        Usuarios                     */
    /////////////////////////////////////////////////////////
    public void addUser(Usuario usuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("userName", usuario.getUserName());
        contentValues.put("email", usuario.getEmail());
        contentValues.put("password", usuario.getPassword());
        db.insert(DATABASE_TABLE_USUARIOS, null, contentValues);
        db.close();
    }

    public Usuario getUsuario(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Usuario usuario = null;

        try {
            String query = "SELECT * FROM " + DATABASE_TABLE_USUARIOS + " WHERE " + "userName" + "=?";
            cursor = db.rawQuery(query, new String[]{username});
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex("password"));
                @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex("email"));
                usuario = new Usuario(username, password, email);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting user: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            //db.close();
        }

        return usuario;
    }

    public void eliminarUsuario(String username) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.delete(DATABASE_TABLE_USUARIOS, "userName" + "=?", new String[]{username});
        } catch (Exception e) {
            Log.e(TAG, "Error while deleting user: " + e.getMessage());
        } finally {
            db.close();
        }
    }


    public boolean checkUserExist(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exist = false;

        try {
            String query = "SELECT * FROM " + DATABASE_TABLE_USUARIOS + " WHERE " + "userName" + "=?";
            cursor = db.rawQuery(query, new String[]{username});
            exist = cursor.moveToFirst();
        } catch (Exception e) {
            Log.e(TAG, "Error while checking user existence: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return exist;
    }

}
