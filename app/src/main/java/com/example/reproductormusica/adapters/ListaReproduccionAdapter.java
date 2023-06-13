package com.example.reproductormusica.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.reproductormusica.DB.DB;
import com.example.reproductormusica.ListasReproduccion.ListaActivity;
import com.example.reproductormusica.Modelos.ListaReproduccionModel;
import com.example.reproductormusica.R;

import java.io.File;
import java.util.ArrayList;

public class ListaReproduccionAdapter extends RecyclerView.Adapter<ListaReproduccionAdapter.ViewHolder> {

    private ArrayList<ListaReproduccionModel> lista;
    private Context context;
    private DB db;

    public ListaReproduccionAdapter(ArrayList<ListaReproduccionModel> lista, Context context) {
        this.lista = lista;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_list_item,parent,false);
        db = new DB(context);
        return new ListaReproduccionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListaReproduccionAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ListaReproduccionModel listData = lista.get(position);
        // Añadir titulo
        holder.titleTextView.setText(listData.getLst_name());

        // Añadir imagen
        if (listData.getLst_img() != null){
            File imgFile = new  File(listData.getLst_img());
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.iconImageView.setImageBitmap(myBitmap);
            }
        }

        // Iniciar cancion al seleccionar una cancion de la lista
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ListaActivity.class);
                String titulo = lista.get(position).getLst_name();
                String img = lista.get(position).getLst_img();
                intent.putExtra("list_title", titulo);
                intent.putExtra("list_img", img);
                context.startActivity(intent);

                // Finaliza la actividad
                if (context instanceof Activity) {
                    // Desactivar la animación de deslizamiento
                    ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    ((Activity) context).finish();
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView titleTextView;
        ImageView iconImageView;
        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.list_title_text);
            iconImageView = itemView.findViewById(R.id.list_img);
        }
    }

}
