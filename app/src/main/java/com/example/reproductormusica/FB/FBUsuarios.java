package com.example.reproductormusica.FB;

import android.util.Log;

import com.example.reproductormusica.Modelos.Usuario;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FBUsuarios {

    private final String COLLECTION_NAME = "Usuarios";
    private final String TAG = "FB_USUARIOS";
    private FirebaseFirestore db;
    private CollectionReference usuariosCollection;

    public FBUsuarios() {
        db = FirebaseFirestore.getInstance();
        usuariosCollection = db.collection(COLLECTION_NAME);
    }

    public void uploadUsuario(Usuario usuario, OnUsuarioUploadListener listener) {
        DocumentReference document = usuariosCollection.document(usuario.getUserName());
        document.set(usuario)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onUsuarioUploaded();
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                    Log.e(TAG, "Error al cargar el usuario: " + e.getMessage());
                });
    }

    public interface OnUsuarioUploadListener {
        void onUsuarioUploaded();
        void onFailure(String error);
    }


    public void downloadUsuario(String userName, OnUsuarioDownloadedListener listener) {
        DocumentReference document = usuariosCollection.document(userName);
        document.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Usuario usuario = documentSnapshot.toObject(Usuario.class);
                        if (listener != null) {
                            listener.onUsuarioDownloaded(usuario);
                        }
                    } else {
                        if (listener != null) {
                            listener.onFailure("Usuario no encontrado");
                        }
                        Log.e(TAG, "Usuario no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                    Log.e(TAG, "Error al descargar el usuario: " + e.getMessage());
                });
    }

    public interface OnUsuarioDownloadedListener {
        void onUsuarioDownloaded(Usuario usuario);
        void onFailure(String error);
    }

    public void checkUsuarioExistence(String userName, OnUsuarioExistenceListener listener) {
        DocumentReference document = usuariosCollection.document(userName);
        document.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if (listener != null) {
                            listener.onUsuarioExists(true);
                        }
                    } else {
                        if (listener != null) {
                            listener.onUsuarioExists(false);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                    Log.e(TAG, "Error al comprobar la existencia del usuario: " + e.getMessage());
                });
    }

    public interface OnUsuarioExistenceListener {
        void onUsuarioExists(boolean exists);
        void onFailure(String error);
    }
}

