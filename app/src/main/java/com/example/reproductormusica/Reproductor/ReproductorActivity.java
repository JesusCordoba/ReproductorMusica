package com.example.reproductormusica.Reproductor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.reproductormusica.DB.DB;
import com.example.reproductormusica.FB.FBCanciones;
import com.example.reproductormusica.FB.FBFiles;
import com.example.reproductormusica.FB.FBListasReproduccion;
import com.example.reproductormusica.ListasReproduccion.ListaReproduccionActivity;
import com.example.reproductormusica.LoginRegister.LoginActivity;
import com.example.reproductormusica.Modelos.CancionModel;
import com.example.reproductormusica.Modelos.ListaReproduccionModel;
import com.example.reproductormusica.R;
import com.example.reproductormusica.databinding.ActivityReproductorBinding;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class ReproductorActivity extends AppCompatActivity {

    private ActivityReproductorBinding binding;
    private ArrayList<CancionModel> songsList = new ArrayList<>();
    private String user;
    private DB db;
    private FBCanciones fb_canciones;
    private FBListasReproduccion fb_listas;
    private FBFiles fb_files;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private List<CancionModel> cancionesDescargadas;
    private List<ListaReproduccionModel> listasDescargadas;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MusicListFragment mlf;
    private MusicListLocalFragment mlfl;
    boolean descargado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReproductorBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        getSupportActionBar().setTitle("");

        // Guarda el nombre de usuario
        SharedPreferences sh = getSharedPreferences("Login", Context.MODE_PRIVATE);
        user = sh.getString("User", "");
        // Iniciar base de datos
        db = new DB(this);
        fb_canciones = new FBCanciones();
        fb_listas = new FBListasReproduccion();
        fb_files = new FBFiles();
        // TAB layout
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        //Iniciar fragmentos
        mlf = new MusicListFragment();
        mlfl = new MusicListLocalFragment();

        // Añadir MusicPlayerFragment para controlar la musica desde la actividad
        ConstraintLayout constraintLayout = findViewById(R.id.ly_mini_reproductor);
        MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment(getApplicationContext());
        getSupportFragmentManager().beginTransaction()
                .add(constraintLayout.getId(), musicPlayerFragment)
                .commit();

        // Comprueba permisos
        if(checkPermission() == false){
            requestPermission();
            return;
        }


        binding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            // Filtrar canciones
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Mezcla la lista que se muestra en el tablayout
                int tab_position = tabLayout.getSelectedTabPosition();
                if (tab_position == 0) {
                    mlf.searchList(s.toString());
                }else{
                    mlfl.searchList(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Añadir cancion
        binding.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener una instancia de ConnectivityManager
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                // Verificar el estado de la conexión de red
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                // Comprobar conexion a internet
                if (networkInfo != null && networkInfo.isConnected()) {
                    Intent intent = new Intent(ReproductorActivity.this, NewSongActivity.class);
                    startActivity(intent);
                    // Añadir animacion
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }else {
                    // La aplicación no tiene conexión a Internet
                    Toast.makeText(getApplicationContext(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Poner cambiar el orden de la lista
        binding.random.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mezcla la lista que se muestra en el tablayout
                int tab_position = tabLayout.getSelectedTabPosition();
                if (tab_position == 0) {
                    mlf.randomList();
                }else{
                    mlfl.randomList();
                }
            }
        });

        //Acceder a las listas de reproduccion
        binding.listasReproduccion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReproductorActivity.this, ListaReproduccionActivity.class);
                startActivity(intent);
            }
        });

        binding.sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener una instancia de ConnectivityManager
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                // Verificar el estado de la conexión de red
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                // Comprobar conexion a internet
                if (networkInfo != null && networkInfo.isConnected()) {
                    fb_canciones.descargarCanciones(user, new FBCanciones.OnCancionesDownloadedListener() {
                        @Override
                        public void onCancionesDownloaded(List<CancionModel> canciones) {

                            cancionesDescargadas = canciones;
                            if (cancionesDescargadas != null) {
                                // Descargar canciones de la base de datos
                                ActivityCompat.requestPermissions(ReproductorActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                            }

                        }
                    });
                    fb_listas.descargarListasReproduccion(user, new FBListasReproduccion.OnListasReproduccionDownloadedListener() {
                        @Override
                        public void onListasReproduccionDownloaded(List<ListaReproduccionModel> listasReproduccion) {
                            listasDescargadas = listasReproduccion;
                            if (listasDescargadas.size() == 0) {
                                Log.d("DESCARGAR LISTAS", "LISTA VACIA");
                            }
                            if (listasDescargadas != null) {
                                agregarListas();
                            }
                        }
                    });
                }else {
                    // La aplicación no tiene conexión a Internet
                    Toast.makeText(getApplicationContext(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
                }

            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        // Ocultar una opción del menú por su ID
        menu.findItem(R.id.nueva_lista_reproduccion).setVisible(false);
        menu.findItem(R.id.editar_lista_reproduccion).setVisible(false);
        menu.findItem(R.id.eliminar_lista_reproduccion).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cerrar_sesion:
                //Iniciar actividad de login
                Intent intent = new Intent(ReproductorActivity.this, LoginActivity.class);
                startActivity(intent);
                SharedPreferences sh = getSharedPreferences("Login", Context.MODE_PRIVATE);
                //Modificar isLoggedIn para no iniciar sesion despues de cerrar
                SharedPreferences.Editor editor = sh.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.apply();
                //Cerrar actividad
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(ReproductorActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if(result == PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            return false;
        }
    }

    void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(ReproductorActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){
            Toast.makeText(ReproductorActivity.this,"READ PERMISSION IS REQUIRED,PLEASE ALLOW FROM SETTTINGS",Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(ReproductorActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},123);
        }else
            ActivityCompat.requestPermissions(ReproductorActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},123);
    }

    private boolean existeCancionConPath(List<CancionModel> lista, String path) {
        for (CancionModel cancion : lista) {
            if (cancion.getPath() != null && cancion.getPath().equals(path)) {
                // La canción con el mismo path se encuentra en la lista
                return true;
            }
        }
        return false;
    }

    private boolean existeLista(List<ListaReproduccionModel> listas, String name) {
        for (ListaReproduccionModel item : listas) {
            if (item.getLst_name() != null && item.getLst_name().equals(name)) {
                // La canción con el mismo path se encuentra en la lista
                return true;
            }
        }
        return false;
    }


    public void descargarCanciones(CancionModel cancion){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
            fb_files.download(cancion.getPath(), "Musica");
            fb_files.download(cancion.getImg(), "");
        }

        db.addCancion(cancion, user);
        songsList.add(0,cancion);
    }

    public void agregarListas(){
        List<ListaReproduccionModel> listas_db = db.todaslaslistas(user);
        for (ListaReproduccionModel lista : listasDescargadas) {
            if (!existeLista(listas_db, lista.getLst_name())) {
                CancionModel lista_Canciones = lista.getLst_canciones();
                // Descargar listas si no esta vacio
                if (lista.getLst_canciones()!=null){
                    Log.d("DESCARGAR LISTA", lista.getLst_name() + " : " + lista_Canciones.getTitle());
                    db.agregarLista(user, lista, lista_Canciones);
                    fb_files.download(lista.getLst_img(), "");
                }else{
                    Log.d("DESCARGAR LISTA", lista.getLst_name());
                    fb_files.download(lista.getLst_img(), "");
                    db.addLista(lista.getUserName(), lista.getLst_name(), lista.getLst_img());
                }

                Log.d("DESCARGAR LISTA", lista.getLst_name());
            }else{
                Log.d("NO DESCARGAR LISTA", lista.getLst_name());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int canciones_descargadas = 0;
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Los permisos han sido otorgados, realizar la descarga de canciones
                for (CancionModel cancion : cancionesDescargadas) {
                    // Actualizar actividad si se descargan canciones
                    boolean todasDescargadas = canciones_descargadas == cancionesDescargadas.size()-1;
                    if (descargado == true && todasDescargadas == true){
                        Intent intent = new Intent(ReproductorActivity.this, ReproductorActivity.class);
                        startActivity(intent);
                        // Desactivar la animación de deslizamiento
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }
                    canciones_descargadas++;

                    // Descargar cancion
                    if (!existeCancionConPath(songsList, cancion.getPath())) {
                    //if (!db.verificarCancion(cancion.getTitle())) {
                        Log.d("DESCARGAR", cancion.getTitle());
                        descargarCanciones(cancion);
                        descargado = true;
                    }else{
                        // Log para ver las canciones que no se descargan
                        Log.d("NO DESCARGAR", cancion.getTitle());

                    }
                }

            } else {
                // Los permisos no han sido otorgados
                Log.e("PERMISOS DESCARGAR", "ERROR PERMISOS DE ESCRITURA");
            }
        }
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso otorgado, puedes realizar la lógica correspondiente
                Intent intent = new Intent(ReproductorActivity.this, ReproductorActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            } else {
                // Permiso denegado, cerrar aplicacion
                finish();

                // Llamada a System.exit(0) para cerrar completamente la aplicación
                System.exit(0);
            }
        }

    }

    // Configurar tablayout
    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            // Retorna el fragmento correspondiente a la posición

            if (position == 0) {
                return mlf.newInstance(user);

            } else {
                return mlfl.newInstance(user);
            }
        }

        @Override
        public int getCount() {
            // Retorna la cantidad total de pestañas
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            // Retorna el título de cada pestaña
            if (position == 0) {
                return "Account";
            } else {
                return "Local";
            }
        }
    }
}