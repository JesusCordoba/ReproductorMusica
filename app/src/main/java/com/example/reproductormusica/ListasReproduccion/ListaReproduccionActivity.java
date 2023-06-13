package com.example.reproductormusica.ListasReproduccion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.reproductormusica.DB.DB;
import com.example.reproductormusica.FB.FBListasReproduccion;
import com.example.reproductormusica.Modelos.ListaReproduccionModel;
import com.example.reproductormusica.R;
import com.example.reproductormusica.adapters.ListaReproduccionAdapter;
import com.example.reproductormusica.databinding.ActivityListaReproduccionBinding;

import java.util.ArrayList;

public class ListaReproduccionActivity extends AppCompatActivity {

    private ActivityListaReproduccionBinding binding;
    private DB db;
    private RecyclerView recyclerView;
    private ListaReproduccionAdapter adapter;
    private ArrayList<ListaReproduccionModel> lista;
    private String user;
    private FBListasReproduccion fb_listas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListaReproduccionBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        getSupportActionBar().setTitle("");

        // Guarda el nombre de usuario
        SharedPreferences sh = getSharedPreferences("Login", Context.MODE_PRIVATE);
        user = sh.getString("User", "");

        fb_listas = new FBListasReproduccion();
        //Iniciar variables
        db = new DB(this);

        lista = new ArrayList<ListaReproduccionModel>();
        recyclerView = findViewById(R.id.recycler_list_view);

        adapter = new ListaReproduccionAdapter(lista, this);

        // Listar listas de reproduccion
        ArrayList<ListaReproduccionModel> listaReproduccion = db.listarlistas(user);
        lista.addAll(listaReproduccion);

        // Añadir canciones a recyclerview
        if (lista.size() != 0) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            recyclerView.setAdapter(adapter);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        // Ocultar una opción del menú por su ID
        menu.findItem(R.id.cerrar_sesion).setVisible(false);
        menu.findItem(R.id.editar_lista_reproduccion).setVisible(false);
        menu.findItem(R.id.eliminar_lista_reproduccion).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nueva_lista_reproduccion:
                // Obtener una instancia de ConnectivityManager
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                // Verificar el estado de la conexión de red
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                // Comprobar conexion a internet
                if (networkInfo != null && networkInfo.isConnected()) {
                    Intent intent = new Intent(ListaReproduccionActivity.this, NewListActivity.class);
                    startActivity(intent);
                    // Desactivar la animación de deslizamiento
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                } else {
                    // La aplicación no tiene conexión a Internet
                    Toast.makeText(getApplicationContext(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recyclerView != null) {
            recyclerView.setAdapter(adapter);
        }
    }

}