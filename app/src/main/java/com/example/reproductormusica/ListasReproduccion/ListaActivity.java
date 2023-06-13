package com.example.reproductormusica.ListasReproduccion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.reproductormusica.DB.DB;
import com.example.reproductormusica.FB.FBListasReproduccion;
import com.example.reproductormusica.Modelos.CancionModel;
import com.example.reproductormusica.adapters.MusicListAdapter;
import com.example.reproductormusica.R;
import com.example.reproductormusica.databinding.ActivityListaBinding;

import java.io.File;
import java.util.ArrayList;

public class ListaActivity extends AppCompatActivity {

    private ActivityListaBinding binding;
    private RecyclerView recyclerView;
    private MusicListAdapter adapter;
    private ArrayList<CancionModel> songsList = new ArrayList<>();
    private String user;
    private DB db;
    private String titulo;
    private String img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListaBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        getSupportActionBar().setTitle("");

        // Guarda el nombre de usuario
        SharedPreferences sh = getSharedPreferences("Login", Context.MODE_PRIVATE);
        user = sh.getString("User", "");

        db = new DB(this);
        titulo = getIntent().getStringExtra("list_title");
        img = getIntent().getStringExtra("list_img");

        binding.lstTitle.setText(titulo);

        // Añadir imagen
        if (img != null) {
            File imgFile = new File(img);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                binding.imageView.setImageBitmap(myBitmap);
            }
        }

        recyclerView = findViewById(R.id.recycler_view_lista_canciones);
        // Crear adaptador
        View rootView = findViewById(android.R.id.content);
        adapter = new MusicListAdapter(songsList, this, rootView, titulo, img, "list");

        ArrayList<CancionModel> lista = db.listarCanciones(user, titulo);
        for (CancionModel cancion : lista) {
            songsList.add(cancion);
        }
        if (songsList.size() != 0) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        // Ocultar una opción del menú por su ID
        menu.findItem(R.id.nueva_lista_reproduccion).setVisible(false);
        menu.findItem(R.id.cerrar_sesion).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Obtener una instancia de ConnectivityManager
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Verificar el estado de la conexión de red
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        switch (item.getItemId()) {
            case R.id.editar_lista_reproduccion:
                // Comprobar conexion a internet
                if (networkInfo != null && networkInfo.isConnected()) {
                    // Abre la actividad de editar cancion
                    Intent intent = new Intent(getApplicationContext(), EditListActivity.class);
                    intent.putExtra("name", titulo);
                    intent.putExtra("img", img);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                    // Desactivar la animación de deslizamiento
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                } else {
                    // La aplicación no tiene conexión a Internet
                    Toast.makeText(getApplicationContext(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.eliminar_lista_reproduccion:
                // Comprobar conexion a internet
                if (networkInfo != null && networkInfo.isConnected()) {
                    showDeleteDialog(ListaActivity.this, titulo);
                } else {
                    // La aplicación no tiene conexión a Internet
                    Toast.makeText(getApplicationContext(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeleteDialog(Context context, String titulo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmar borrado");
        builder.setMessage("¿Estás seguro de que deseas borrar esta lista?");

        // Opción "Cancelar"
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Cierra el diálogo sin realizar ninguna acción
            }
        });

        // Opción "Borrar"
        builder.setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.deleteList(titulo, user);
                FBListasReproduccion fb = new FBListasReproduccion();
                fb.deleteListaByTitulo(user,titulo);
                dialog.dismiss(); // Cierra el diálogo después de realizar la acción
                Intent intent = new Intent(ListaActivity.this, ListaReproduccionActivity.class);
                startActivity(intent);

                // Desactivar la animación de deslizamiento
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();

            }
        });

        // Crea y muestra el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onBackPressed() {
        // Abrir la actividad Reproductor
        Intent intent = new Intent(ListaActivity.this, ListaReproduccionActivity.class);
        startActivity(intent);

        // Desactivar la animación de deslizamiento
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}