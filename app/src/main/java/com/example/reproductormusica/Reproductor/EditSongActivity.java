package com.example.reproductormusica.Reproductor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.reproductormusica.DB.DB;
import com.example.reproductormusica.FB.FBCanciones;
import com.example.reproductormusica.FB.FBFiles;
import com.example.reproductormusica.FB.FBListasReproduccion;
import com.example.reproductormusica.ListasReproduccion.ListaActivity;
import com.example.reproductormusica.Modelos.CancionModel;
import com.example.reproductormusica.R;
import com.example.reproductormusica.databinding.ActivityEditSongBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class EditSongActivity extends AppCompatActivity {

    private ActivityEditSongBinding binding;
    private RecyclerView recyclerView;
    private String nombre_orignal;
    private CancionModel cancion;
    private DB db;
    private Uri uriSong;
    private Uri uriImg;
    private String songName;
    private String imgName;
    private String path;
    private String imgPath;
    private FBCanciones fb_canciones;//Añadido
    private FBListasReproduccion fb_listas;
    private FBFiles fb_files;
    private String img_original;
    private String user;
    private String list_title;
    private String list_img;
    private NetworkInfo networkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditSongBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        getSupportActionBar().setTitle("");

        // Guarda el nombre de usuario
        SharedPreferences sh = getSharedPreferences("Login", Context.MODE_PRIVATE);
        user = sh.getString("User", "");

        fb_canciones = new FBCanciones();//Añadido
        fb_listas = new FBListasReproduccion();
        fb_files = new FBFiles();
        recyclerView = findViewById(R.id.recycler_view);
        db = new DB(this);
        Intent intent = getIntent();
        cancion = (CancionModel) intent.getSerializableExtra("cancion");
        list_title = intent.getStringExtra("list_title");
        list_img = intent.getStringExtra("list_img");

        nombre_orignal = cancion.getTitle();
        img_original = cancion.getImg();

        binding.path.setText(cancion.getPath());
        binding.txtMusicTitulo.setText(cancion.getTitle());
        binding.txtMusicArtista.setText(cancion.getArtista());
        binding.txtMusicGenero.setText(cancion.getGenero());

        File imgFile = new File(cancion.getImg());
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            binding.imagenCancion.setImageBitmap(myBitmap);
        }

        binding.selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 2);
            }
        });

        binding.editSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener una instancia de ConnectivityManager
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                // Verificar el estado de la conexión de red
                networkInfo = connectivityManager.getActiveNetworkInfo();
                // Comprobar conexion a internet
                if (networkInfo != null && networkInfo.isConnected()) {
                    cancion.setTitle(binding.txtMusicTitulo.getText().toString());
                    cancion.setArtista(binding.txtMusicArtista.getText().toString());
                    cancion.setGenero(binding.txtMusicGenero.getText().toString());
                    if (!binding.txtMusicTitulo.getText().toString().isEmpty()) {
                        try {

                            if (uriImg != null) {
                                copyFileToInternalStorage(getApplicationContext(), uriImg, user + binding.txtMusicTitulo.getText().toString().replace(".", ""), "/Portadas");
                                cancion.setImg(imgPath);
                                fb_files.upload(cancion.getImg(), "");
                                if (!db.verificarImagenDuplicada(img_original, user) && !db.verificarImagenListaDuplicada(img_original, user)) {
                                    File imagen_anterior = new File(img_original);
                                    imagen_anterior.delete();
                                    fb_files.delete(img_original, "");
                                }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        db.updateCancion(user,cancion, nombre_orignal);
                        fb_canciones.updateCancionByTitulo(user,nombre_orignal, cancion.getTitle(), cancion.getArtista(), cancion.getGenero(), cancion.getDuration(), cancion.getImg());
                        db.updateCancionLista(cancion, nombre_orignal, user);
                        List<CancionModel> lista_Canciones = new ArrayList<>();
                        cancion.setUserName(user);
                        lista_Canciones.add(cancion);
                        fb_listas.updateListaByTituloCancion(user,nombre_orignal, cancion);

                        if (list_title.isEmpty()) {
                            // Abrir la actividad Reproductor
                            Intent intent = new Intent(EditSongActivity.this, ReproductorActivity.class);
                            startActivity(intent);
                        } else {
                            // Abrir la actividad Lista
                            Intent intent = new Intent(EditSongActivity.this, ListaActivity.class);
                            intent.putExtra("list_title", list_title);
                            intent.putExtra("list_img", list_img);
                            startActivity(intent);
                        }

                        // Desactivar la animación de deslizamiento
                        overridePendingTransition(0, 0);
                        finish();
                    } else {
                        // La aplicación no tiene conexión a Internet
                        Toast.makeText(getApplicationContext(), "No hay titulo", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // La aplicación no tiene conexión a Internet
                    Toast.makeText(getApplicationContext(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriSong = data.getData();

            uriSong.getPath();
            binding.path.setText("Path: " + uriSong.getPath());

            String path = uriSong.getPath();
            songName = new File(path).getName();
            binding.txtMusicTitulo.setText(songName);

        }

        if (requestCode == 2 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImg = data.getData();
            String path = uriImg.getPath();
            imgName = new File(path).getName();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriImg);
                ImageView imageView = findViewById(R.id.imagen_cancion);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void copyFileToInternalStorage(Context context, Uri uri, String fileName, String directory) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            File directoryFile = Environment.getExternalStoragePublicDirectory("ReproductorMusica/" + directory);
            if (!directoryFile.exists()) {
                if (directoryFile.mkdirs()) {
                    Toast.makeText(getApplicationContext(), "Carpeta creada : " + directoryFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Carpeta existente : " + directoryFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
            File file = new File(directoryFile, fileName);
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            if (directory == "/Musica") {
                path = file.getPath();
            } else {
                imgPath = file.getPath();
            }

            outputStream.close();
            inputStream.close();
        }
    }

    @Override
    public void onBackPressed() {
        if (list_title.isEmpty()) {
            // Abrir la actividad Reproductor
            Intent intent = new Intent(EditSongActivity.this, ReproductorActivity.class);
            startActivity(intent);
        } else {
            // Abrir la actividad Lista
            Intent intent = new Intent(EditSongActivity.this, ListaActivity.class);
            intent.putExtra("list_title", list_title);
            intent.putExtra("list_img", list_img);
            startActivity(intent);
        }

        // Desactivar la animación de deslizamiento
        overridePendingTransition(0, 0);
        finish();
    }

}