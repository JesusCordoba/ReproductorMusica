package com.example.reproductormusica.FB;

import android.util.Log;

import com.example.reproductormusica.Modelos.CancionModel;
import com.example.reproductormusica.Modelos.ListaReproduccionModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FBListasReproduccion {
    private FirebaseFirestore db;
    private CollectionReference listasCollection;

    public FBListasReproduccion() {
        // Obtener una instancia de Firestore
        db = FirebaseFirestore.getInstance();
        listasCollection = db.collection("ListasReproduccion");
    }

    public void guardarListaReproduccion(ListaReproduccionModel lista) {
        // Guardar la lista de reproducción en la colección "ListasReproduccion" en Firestore
        listasCollection.add(lista)
                .addOnSuccessListener(documentReference -> {
                    // Éxito al guardar la lista de reproducción
                    String listaId = documentReference.getId();
                    // Realizar acciones adicionales si es necesario
                })
                .addOnFailureListener(e -> {
                    // Error al guardar la lista de reproducción
                    // Manejar el error de acuerdo a tus necesidades
                });
    }

    public void descargarListasReproduccion(String userName, OnListasReproduccionDownloadedListener listener) {
        listasCollection
                .whereEqualTo("userName", userName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ListaReproduccionModel> listasReproduccion = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ListaReproduccionModel lista = documentSnapshot.toObject(ListaReproduccionModel.class);
                        listasReproduccion.add(lista);
                    }

                    // Pasar el resultado al listener
                    if (listener != null) {
                        listener.onListasReproduccionDownloaded(listasReproduccion);
                    }
                })
                .addOnFailureListener(e -> {
                    // Error al descargar las listas de reproducción
                    // Manejar el error de acuerdo a tus necesidades
                    Log.e("DOWNLOAD_PLAYLISTS", "Error al descargar listas de reproducción: " + e.getMessage());
                });
    }


    public interface OnListasReproduccionDownloadedListener {
        void onListasReproduccionDownloaded(List<ListaReproduccionModel> listasReproduccion);
    }

    public void updateListaByTitulo(String userName, String lst_name_original, String lst_name, String lst_img) {
        listasCollection
                .whereEqualTo("userName", userName)
                .whereEqualTo("lst_name", lst_name_original)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String listaId = documentSnapshot.getId();
                        DocumentReference listaRef = listasCollection.document(listaId);

                        listaRef.update(
                                        "lst_name", lst_name,
                                        "lst_img", lst_img
                                )
                                .addOnSuccessListener(aVoid -> {
                                    // Éxito al actualizar la lista
                                    // Realizar acciones adicionales si es necesario
                                })
                                .addOnFailureListener(e -> {
                                    // Error al actualizar la lista
                                    // Manejar el error de acuerdo a tus necesidades
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Error al buscar la lista por título
                    // Manejar el error de acuerdo a tus necesidades
                });
    }


    public void updateListaByTituloCancion(String userName, String titulo, CancionModel nuevaCancion) {
        // Construir la consulta para buscar la lista de reproducción por el título de una canción
        listasCollection
                .whereEqualTo("userName", userName)
                .whereEqualTo("lst_canciones.title", titulo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        // Obtener el ID del documento de la lista de reproducción
                        String listaId = documentSnapshot.getId();

                        // Actualizar el campo de canciones de la lista de reproducción
                        listasCollection
                                .document(listaId)
                                .update("lst_canciones", nuevaCancion)
                                .addOnSuccessListener(aVoid -> {
                                    // Éxito al actualizar la canción de la lista de reproducción
                                    Log.d("UPDATE_SONG_LIST", "TRUE");
                                    // Realizar acciones adicionales si es necesario
                                })
                                .addOnFailureListener(e -> {
                                    // Error al actualizar la canción de la lista de reproducción
                                    Log.e("UPDATE_SONG_LIST", "FALSE");
                                    // Manejar el error de acuerdo a tus necesidades
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Error al buscar la lista de reproducción por título de canción
                    Log.e("UPDATE_SONG_LIST", "NOT FOUND");
                    // Manejar el error de acuerdo a tus necesidades
                });
    }

    public void deleteListaByTituloCancion(String userName, String tituloCancion) {
        listasCollection
                .whereEqualTo("userName", userName)
                .whereEqualTo("lst_canciones.title", tituloCancion)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String listaId = documentSnapshot.getId();
                        DocumentReference listaRef = listasCollection.document(listaId);

                        listaRef.delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Éxito al eliminar la canción
                                    // Realizar acciones adicionales si es necesario
                                    Log.d("FBLista DELETE", "Eliminado: " + tituloCancion);
                                })
                                .addOnFailureListener(e -> {
                                    // Error al eliminar la canción
                                    // Manejar el error de acuerdo a tus necesidades
                                    Log.e("FBLista DELETE", "No se pudo eliminar: " + tituloCancion);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Error al buscar la lista de reproducción por título de canción
                    // Manejar el error de acuerdo a tus necesidades
                    Log.e("FBLista DELETE", "No se encuentra: " + tituloCancion);
                });
    }


    public void deleteListaByTitulo(String userName, String tituloLista) {
        listasCollection
                .whereEqualTo("userName", userName)
                .whereEqualTo("lst_name", tituloLista)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String listaId = documentSnapshot.getId();
                        DocumentReference listaRef = listasCollection.document(listaId);

                        listaRef.delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Éxito al eliminar la lista de reproducción
                                    // Realizar acciones adicionales si es necesario
                                })
                                .addOnFailureListener(e -> {
                                    // Error al eliminar la lista de reproducción
                                    // Manejar el error de acuerdo a tus necesidades
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Error al buscar la lista de reproducción por título
                    // Manejar el error de acuerdo a tus necesidades
                });
    }



}

