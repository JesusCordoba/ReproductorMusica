package com.example.reproductormusica.Reproductor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.reproductormusica.DB.DB;
import com.example.reproductormusica.FB.FBCanciones;
import com.example.reproductormusica.FB.FBFiles;
import com.example.reproductormusica.Modelos.CancionModel;
import com.example.reproductormusica.R;
import com.example.reproductormusica.databinding.ActivityNewSongBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NewSongActivity extends AppCompatActivity {

    private ActivityNewSongBinding binding;
    private Context context;
    private Uri uriSong;
    private Uri uriImg;
    private String songName;
    private String imgName;
    private String path;
    private DB db;
    private String user;
    private String imgPath;
    private FBCanciones fb_canciones;
    private FBFiles fb_files;
    private CancionModel cancion;
    private NetworkInfo networkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewSongBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        getSupportActionBar().setTitle("");
        context = getApplicationContext();
        fb_canciones = new FBCanciones();
        fb_files = new FBFiles();

        // Iniciar base de datos
        db = new DB(this);
        // Guardar nombre usuario
        SharedPreferences sh = getSharedPreferences("Login", Context.MODE_PRIVATE);
        user = sh.getString("User", "");

        Intent intent = getIntent();
        cancion = (CancionModel) intent.getSerializableExtra("cancion");
        if (cancion != null) {
            path = cancion.getPath();
            songName = new File(path).getName();
            binding.txtMusicTitulo.setText(songName);
        } else {
            binding.txtMusicTitulo.setVisibility(View.GONE);
            binding.txtMusicArtista.setVisibility(View.GONE);
            binding.txtMusicGenero.setVisibility(View.GONE);
            binding.selectImageButton.setVisibility(View.GONE);
            binding.newSong.setVisibility(View.GONE);
        }

        binding.selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select Audio File"), 1);
            }
        });

        binding.selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 2);
            }
        });

        binding.newSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener una instancia de ConnectivityManager
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                // Verificar el estado de la conexión de red
                networkInfo = connectivityManager.getActiveNetworkInfo();
                // Comprobar conexion a internet
                if (networkInfo != null && networkInfo.isConnected()) {

                    String titulo = binding.txtMusicTitulo.getText().toString();
                    String artista = binding.txtMusicArtista.getText().toString();
                    String genero = binding.txtMusicGenero.getText().toString();
                    if (!titulo.isEmpty()) {
                        try {
                            if (cancion != null) {
                                copyFileToInternalStorage2(getApplicationContext(), cancion.getPath(), titulo, "/Musica");
                                cancion.setPath(imgPath);
                            } else {
                                copyFileToInternalStorage(getApplicationContext(), uriSong, titulo, "/Musica");
                            }

                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(path);
                            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            CancionModel cancion;
                            if (uriImg != null) {
                                copyFileToInternalStorage(getApplicationContext(), uriImg, user + titulo.replace(".", ""), "/Portadas");
                                cancion = new CancionModel(path, imgPath, titulo, artista, genero, duration);
                                fb_canciones.subirCancion(user, path, imgPath, titulo, artista, genero, duration);
                            } else {
                                cancion = new CancionModel(path, "", titulo, artista, genero, duration);
                                fb_canciones.subirCancion(user, path, "", titulo, artista, genero, duration);
                            }

                            db.addCancion(cancion, user);

                            fb_files.upload(cancion.getPath(), "Musica");
                            if (cancion.getImg() != null) {
                                fb_files.upload(cancion.getImg(), "");
                            }

                            Intent intent = new Intent(NewSongActivity.this, ReproductorActivity.class);
                            startActivity(intent);
                            // Desactivar la animación de deslizamiento
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("ERROR", e.toString());
                        }
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
            // Mostrar elementos de la vista
            binding.txtMusicTitulo.setVisibility(View.VISIBLE);
            binding.txtMusicArtista.setVisibility(View.VISIBLE);
            binding.txtMusicGenero.setVisibility(View.VISIBLE);
            binding.selectImageButton.setVisibility(View.VISIBLE);
            binding.newSong.setVisibility(View.VISIBLE);

            uriSong = data.getData();
            uriSong.getPath();
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

    public void copyFileToInternalStorage2(Context context, String oldpath, String fileName, String directory) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

            File sourceFile = new File(oldpath);
            InputStream inputStream = new FileInputStream(sourceFile);

            File directoryFile = Environment.getExternalStoragePublicDirectory("ReproductorMusica/" + directory);
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

    public void onBackPressed() {
        // Abrir la actividad Reproductor
        Intent intent = new Intent(NewSongActivity.this, ReproductorActivity.class);
        startActivity(intent);

        // Desactivar la animación de deslizamiento
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

}