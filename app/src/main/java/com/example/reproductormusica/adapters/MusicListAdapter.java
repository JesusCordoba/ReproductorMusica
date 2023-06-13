package com.example.reproductormusica.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.reproductormusica.DB.DB;
import com.example.reproductormusica.FB.FBFiles;
import com.example.reproductormusica.Reproductor.EditSongActivity;
import com.example.reproductormusica.FB.FBCanciones;
import com.example.reproductormusica.FB.FBListasReproduccion;
import com.example.reproductormusica.Modelos.CancionModel;
import com.example.reproductormusica.Modelos.ListaReproduccionModel;
import com.example.reproductormusica.Modelos.MyMediaPlayer;
import com.example.reproductormusica.Reproductor.MusicPlayerActivity;
import com.example.reproductormusica.R;
import com.example.reproductormusica.Reproductor.NewSongActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder> {

    private ArrayList<CancionModel> songsList;
    private ArrayList<CancionModel> songsListOrignal;
    private Context context;
    private DB db;
    private View rootView;
    private String user;
    private String list;
    private String list_img;
    private FBListasReproduccion fb_listas;
    private FBCanciones fb_canciones;
    private FBFiles fb_files;
    private int selectedItemPosition = -1;
    private String type;

    public MusicListAdapter(ArrayList<CancionModel> songsList, Context context, View rootView, String list, String list_img, String type) {
        this.songsList = songsList;
        this.context = context;
        this.rootView = rootView;
        this.list = list;
        this.list_img = list_img;
        this.type = type;
        songsListOrignal = songsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        db = new DB(context);
        fb_listas = new FBListasReproduccion();
        fb_canciones = new FBCanciones();
        fb_files = new FBFiles();
        return new MusicListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MusicListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        // Cancion de la lista
        CancionModel songData = songsList.get(position);
        // Añadir titulo
        holder.titleTextView.setText(songData.getTitle());

        // Guarda el nombre de usuario
        SharedPreferences sh = context.getSharedPreferences("Login", Context.MODE_PRIVATE);
        user = sh.getString("User", "");

        // Añadir imagen
        if (songData.getImg() != null) {
            File imgFile = new File(songData.getImg());
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.iconImageView.setImageBitmap(myBitmap);
            } else {
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.music_icon);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
                holder.iconImageView.setImageDrawable(bitmapDrawable);
            }
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.music_icon);
            BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
            holder.iconImageView.setImageDrawable(bitmapDrawable);
        }

        // Añadir menu de opciones de editar y eliminar
        holder.iconOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, holder.iconOption);
                popupMenu.inflate(R.menu.menu_music_option);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                        switch (item.getItemId()) {
                            case R.id.edit_item:
                                // Comprobar conexion a internet
                                if (networkInfo != null && networkInfo.isConnected()) {
                                    // Abre la actividad de editar cancion
                                    Intent intent = new Intent(context, EditSongActivity.class);
                                    intent.putExtra("cancion", songData);
                                    intent.putExtra("list_title", list);
                                    intent.putExtra("list_img", list_img);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    ((Activity) context).startActivityForResult(intent, 10);

                                    // Finaliza la actividad
                                    if (context instanceof Activity) {
                                        ((Activity) context).finish();
                                    }
                                } else {
                                    // La aplicación no tiene conexión a Internet
                                    Toast.makeText(context, "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            case R.id.delete_item:
                                // Comprobar conexion a internet
                                if (networkInfo != null && networkInfo.isConnected()) {
                                    // Dialogo que elimina un elemento
                                    showDeleteDialog(holder.itemView.getRootView().getContext(), songData, position);
                                } else {
                                    // La aplicación no tiene conexión a Internet
                                    Toast.makeText(context, "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            case R.id.agregaralista:
                                // Comprobar conexion a internet
                                if (networkInfo != null && networkInfo.isConnected()) {
                                    showListaReproduccionDialog(user, songData);
                                } else {
                                    // La aplicación no tiene conexión a Internet
                                    Toast.makeText(context, "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            case R.id.agregarCancion:
                                // Comprobar conexion a internet
                                if (networkInfo != null && networkInfo.isConnected()) {
                                    // Abre la actividad de editar cancion
                                    Intent intent2 = new Intent(context, NewSongActivity.class);
                                    intent2.putExtra("cancion", songData);
                                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    ((Activity) context).startActivity(intent2);
                                    ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                                    // Finaliza la actividad
                                    if (context instanceof Activity) {
                                        ((Activity) context).finish();
                                    }
                                } else {
                                    // La aplicación no tiene conexión a Internet
                                    Toast.makeText(context, "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            default:
                                return false;
                        }
                    }


                });
                if (list != "") {
                    MenuItem menuItemAgregaralista = popupMenu.getMenu().findItem(R.id.agregaralista);
                    menuItemAgregaralista.setVisible(false); // Establece el MenuItem como invisible
                    MenuItem menuItemAgregaraCancion = popupMenu.getMenu().findItem(R.id.agregarCancion);
                    menuItemAgregaraCancion.setVisible(false); // Establece el MenuItem como invisible
                }
                if (type == "local") {
                    MenuItem menuItemEdititem = popupMenu.getMenu().findItem(R.id.edit_item);
                    menuItemEdititem.setVisible(false); // Establece el MenuItem como invisible
                    MenuItem menuItemDeleteItem = popupMenu.getMenu().findItem(R.id.delete_item);
                    menuItemDeleteItem.setVisible(false); // Establece el MenuItem como invisible
                    MenuItem menuItemAgregaralista = popupMenu.getMenu().findItem(R.id.agregaralista);
                    menuItemAgregaralista.setVisible(false); // Establece el MenuItem como invisible
                }
                if (type == "account") {
                    MenuItem menuItemAgregaraCancion = popupMenu.getMenu().findItem(R.id.agregarCancion);
                    menuItemAgregaraCancion.setVisible(false); // Establece el MenuItem como invisible
                }
                popupMenu.show();
            }
        });


        // Iniciar cancion al seleccionar una cancion de la lista
        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (selectedItemPosition == position) {
                    // El elemento actualmente seleccionado es el mismo, no reiniciar la canción
                    Intent intent = new Intent(context, MusicPlayerActivity.class);
                    intent.putExtra("LIST", songsList);
                    intent.putExtra("CURRENT_POSITION", selectedItemPosition);
                    intent.putExtra("renaudar", true);
                    int progress = MyMediaPlayer.getInstance().getCurrentPosition();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("progress", progress);

                    // Establece las animaciones de transición personalizadas
                    if (context instanceof Activity) {
                        Activity activity = (Activity) context;
                        activity.startActivity(intent);
                        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }

                } else {
                    // El elemento actualmente seleccionado es diferente, actualizar la posición seleccionada
                    selectedItemPosition = position;
                    Intent intent = new Intent(context, MusicPlayerActivity.class);
                    intent.putExtra("LIST", songsList);
                    intent.putExtra("CURRENT_POSITION", selectedItemPosition);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    // Establece las animaciones de transición personalizadas
                    if (context instanceof Activity) {
                        Activity activity = (Activity) context;
                        activity.startActivity(intent);
                        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }

                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView iconImageView;
        Button iconOption;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.music_title_text);
            iconImageView = itemView.findViewById(R.id.icon_view);
            iconOption = itemView.findViewById(R.id.button2);
        }
    }

    public void search(String search) {
        ArrayList<CancionModel> newList = new ArrayList<>();
        search = search.toLowerCase();
        //Recorre la lista de canciones buscando las que cumplan el filtro
        for (CancionModel song : songsListOrignal) {
            String titulo = song.getTitle().toLowerCase();
            String artista = "";
            if (song.getArtista() != null) {
                artista = song.getArtista().toLowerCase();
            }
            String genero = "";
            if (song.getGenero() != null) {
                genero = song.getGenero().toLowerCase();
            }

            if (titulo.contains(search) || artista.contains(search) || genero.contains(search)) {
                newList.add(song);
            }
        }
        songsList = newList;
        notifyDataSetChanged();
    }

    public void randomSongList() {
        Collections.shuffle(songsList);
        songsListOrignal = songsList;
        notifyDataSetChanged();
    }


    private void showDeleteDialog(Context context, CancionModel songData, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmar borrado");
        builder.setMessage("¿Estás seguro de que deseas borrar esta canción?");

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
                if (list != "") {
                    //db_lista.deleteCancionLista(songData.getTitle(), list);
                    db.deleteCancionLista(songData.getTitle(), list, user);
                } else {
                    // Borra una cancion
                    db.deleteCancion(user, songData.getTitle());
                    //db_lista.deleteCancionListas(songData.getTitle());
                    db.deleteCancionListas(songData.getTitle(), user);
                    //if (!db.verificarImagenDuplicada(songData.getImg()) && !db_lista.verificarImagenListaDuplicada(songData.getImg())){
                    if (!db.verificarImagenDuplicada(songData.getImg(), user) && !db.verificarImagenListaDuplicada(songData.getImg(), user)) {
                        fb_files.delete(songData.getImg(), "");
                        File imagen_anterior = new File(songData.getImg());

                        imagen_anterior.delete();
                    }

                    // Verificar si alguien utiliza la cancion y borrarla
                    if (!db.verificarPath(songData.getPath())) {
                        File eliminar_cancion = new File(songData.getPath());
                        eliminar_cancion.delete();
                    }



                    fb_canciones.deleteCancionByTitulo(user, songData.getTitle());

                    fb_canciones.verificarPath(songData.getPath(), existePath -> {
                        if (!existePath) {
                            fb_files.delete(songData.getPath(), "Musica");
                        }
                    });

                    fb_listas.deleteListaByTituloCancion(user, songData.getTitle());
                }

                dialog.dismiss(); // Cierra el diálogo después de realizar la acción
                songsList.remove(position);
                songsListOrignal = songsList;
                notifyItemRemoved(position);
            }
        });

        // Crea y muestra el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showListaReproduccionDialog(String user, CancionModel cancion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Seleccionar lista de reproducción");

        // Obtener la lista de nombres de listas de reproducción desde la base de datos
        ArrayList<String> nombresListas = db.obtenerNombresListas(user);
        ArrayList<ListaReproduccionModel> listas_usuario = db.listarlistas(user);

        // Convertir el ArrayList en un arreglo de Strings
        String[] arrayNombresListas = nombresListas.toArray(new String[nombresListas.size()]);

        // Crear una vista para el diálogo
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_list_scrollable, null);
        builder.setView(dialogView);

        // Obtener la referencia del ListView en la vista del diálogo
        ListView listView = dialogView.findViewById(R.id.listView);

        // Crear un ArrayAdapter para el ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, arrayNombresListas);

        // Establecer el adaptador en el ListView
        listView.setAdapter(adapter);

        // Establecer un listener para el evento de clic en el ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String listaSeleccionada = arrayNombresListas[position];
                // Aquí puedes llamar al método de la base de datos para agregar la canción a la lista seleccionada
                //db.agregarCancionALista(listaSeleccionada, tituloCancion);
                db.addCancionLista(user, listaSeleccionada, cancion);
                List<CancionModel> listacancion = new ArrayList<CancionModel>();
                CancionModel agregarCancion = cancion;
                agregarCancion.setUserName(user);
                listacancion.add(agregarCancion);
                ListaReproduccionModel listaagregar = new ListaReproduccionModel(listas_usuario.get(position).getLst_name(), listas_usuario.get(position).getLst_img(), listas_usuario.get(position).getUserName(), cancion);

                fb_listas.guardarListaReproduccion(listaagregar);
                Toast.makeText(context, "Canción agregada a la lista: " + listaSeleccionada, Toast.LENGTH_SHORT).show();
            }
        });

        // Mostrar el diálogo
        builder.show();
    }
}
