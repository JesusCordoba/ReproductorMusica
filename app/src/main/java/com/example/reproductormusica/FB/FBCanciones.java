package com.example.reproductormusica.FB;

import com.example.reproductormusica.Modelos.CancionModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
public class FBCanciones {
    private FirebaseFirestore db;
    private CollectionReference cancionesCollection;

    public FBCanciones() {
        // Obtener una instancia de Firestore
        db = FirebaseFirestore.getInstance();
        cancionesCollection = db.collection("Canciones");
    }

    public void subirCancion(String userName, String path, String img, String titulo, String artista, String genero, String duracion) {
        // Crear un nuevo objeto Cancion
        CancionModel cancion = new CancionModel(userName, path, img, titulo, artista, genero, duracion);

        // Subir la canción a la colección "Canciones" en Firestore
        cancionesCollection.add(cancion)
                .addOnSuccessListener(documentReference -> {
                    // Éxito al subir la canción
                    String cancionId = documentReference.getId();
                    // Realizar acciones adicionales si es necesario
                })
                .addOnFailureListener(e -> {
                    // Error al subir la canción
                    // Manejar el error de acuerdo a tus necesidades
                });
    }

    public void descargarCanciones(String userName, OnCancionesDownloadedListener listener) {
        cancionesCollection
                .whereEqualTo("userName", userName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CancionModel> canciones = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        CancionModel cancion = documentSnapshot.toObject(CancionModel.class);
                        canciones.add(cancion);
                    }

                    // Pasar el resultado al listener
                    if (listener != null) {
                        listener.onCancionesDownloaded(canciones);
                    }
                })
                .addOnFailureListener(e -> {
                    // Error al descargar las canciones
                    // Manejar el error de acuerdo a tus necesidades
                    System.out.println("Error al descargar canciones: " + e.getMessage());
                });
    }

    public interface OnCancionesDownloadedListener {
        void onCancionesDownloaded(List<CancionModel> canciones);
    }

    public void updateCancionByTitulo(String userName, String titulo, String nuevoTitulo, String artista, String genero, String duracion, String img) {
        cancionesCollection
                .whereEqualTo("userName", userName)
                .whereEqualTo("title", titulo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String cancionId = documentSnapshot.getId();
                        DocumentReference cancionRef = cancionesCollection.document(cancionId);

                        cancionRef.update(
                                        "title", nuevoTitulo,
                                        "artista", artista,
                                        "genero", genero,
                                        "duration", duracion,
                                        "img", img
                                )
                                .addOnSuccessListener(aVoid -> {
                                    // Éxito al actualizar la canción
                                    // Realizar acciones adicionales si es necesario
                                })
                                .addOnFailureListener(e -> {
                                    // Error al actualizar la canción
                                    // Manejar el error de acuerdo a tus necesidades
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Error al buscar la canción por título
                    // Manejar el error de acuerdo a tus necesidades
                });
    }


    public void deleteCancionByTitulo(String userName, String titulo) {
        cancionesCollection
                .whereEqualTo("userName", userName)
                .whereEqualTo("title", titulo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String cancionId = documentSnapshot.getId();
                        DocumentReference cancionRef = cancionesCollection.document(cancionId);

                        cancionRef.delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Éxito al eliminar la canción
                                    // Realizar acciones adicionales si es necesario
                                })
                                .addOnFailureListener(e -> {
                                    // Error al eliminar la canción
                                    // Manejar el error de acuerdo a tus necesidades
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Error al buscar la canción por título
                    // Manejar el error de acuerdo a tus necesidades
                });
    }

    public void verificarPath(String path, OnVerificarPathListener listener) {
        cancionesCollection
                .whereEqualTo("path", path)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean existePath = !queryDocumentSnapshots.isEmpty();
                    if (listener != null) {
                        listener.onVerificarPath(existePath);
                    }
                })
                .addOnFailureListener(e -> {
                    // Error al buscar la canción por path
                    // Manejar el error de acuerdo a tus necesidades
                });
    }

    public interface OnVerificarPathListener {
        void onVerificarPath(boolean existePath);
    }



}
