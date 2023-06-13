package com.example.reproductormusica.ListasReproduccion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import com.example.reproductormusica.FB.FBFiles;
import com.example.reproductormusica.FB.FBListasReproduccion;
import com.example.reproductormusica.Modelos.CancionModel;
import com.example.reproductormusica.Modelos.ListaReproduccionModel;
import com.example.reproductormusica.R;
import com.example.reproductormusica.databinding.ActivityNewListBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NewListActivity extends AppCompatActivity {

    private ActivityNewListBinding binding;
    private String imgName;
    private Uri uriImg;
    private DB db;
    private String path;
    private String imgPath;
    private String user;
    private FBListasReproduccion fb_listas;
    private FBFiles fb_files;
    private NetworkInfo networkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        getSupportActionBar().setTitle("");

        // Iniciar base de datos
        db = new DB(this);
        fb_listas = new FBListasReproduccion();
        fb_files = new FBFiles();

        // Guardar nombre usuario
        SharedPreferences sh = getSharedPreferences("Login", Context.MODE_PRIVATE);
        user = sh.getString("User", "");

        binding.selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 2);
            }
        });

        binding.newList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = binding.txtListName.getText().toString();
                // Obtener una instancia de ConnectivityManager
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                // Verificar el estado de la conexión de red
                networkInfo = connectivityManager.getActiveNetworkInfo();
                // Comprobar conexion a internet
                if (networkInfo != null && networkInfo.isConnected()) {
                    if (!titulo.isEmpty()) {
                        try {
                            ListaReproduccionModel lista;
                            CancionModel song = new CancionModel();
                            if (uriImg != null) {
                                copyFileToInternalStorage(getApplicationContext(), uriImg, user + titulo.replace(".", ""), "/Portadas");
                                db.addLista(user, titulo, imgPath);
                                lista = new ListaReproduccionModel(titulo, imgPath, user, song);
                                fb_files.upload(imgPath, "");
                            } else {
                                db.addLista(user, titulo, "");
                                lista = new ListaReproduccionModel(titulo, "", user, song);
                            }

                            fb_listas.guardarListaReproduccion(lista);
                            // Abrir la actividad Reproductor
                            Intent intent = new Intent(NewListActivity.this, ListaReproduccionActivity.class);
                            startActivity(intent);
                            // Desactivar la animación de deslizamiento
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("ERROR", e.toString());
                        }
                    }else {
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

        if (requestCode == 2 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImg = data.getData();
            String path = uriImg.getPath();
            imgName = new File(path).getName();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriImg);
                ImageView imageView = findViewById(R.id.imagen_cancion2);
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

    public void onBackPressed() {
        // Abrir la actividad Reproductor
        Intent intent = new Intent(NewListActivity.this, ListaReproduccionActivity.class);
        startActivity(intent);

        // Desactivar la animación de deslizamiento
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}