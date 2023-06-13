package com.example.reproductormusica.FB;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class FBFiles {

    private static final String STORAGE_PATH = "reproductormusica/";
    private final StorageReference storageReference;

    public FBFiles() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference().child(STORAGE_PATH);
    }

    public void upload(String filePath, String fileType) {
        File file = new File(filePath);
        Uri fileUri = Uri.fromFile(file);
        StorageReference fileRef = storageReference.child(getFilePath(file.getName(), fileType));

        UploadTask uploadTask = fileRef.putFile(fileUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // La carga se completó exitosamente
                // Puedes obtener la URL de descarga del archivo subido
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String downloadUrl = uri.toString();
                        Log.d("FBFiles", "URL de descarga: " + downloadUrl);
                        // Aquí puedes manejar la URL de descarga del archivo subido
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // La carga falló
                if (e instanceof StorageException) {
                    StorageException storageException = (StorageException) e;
                    int errorCode = storageException.getErrorCode();
                    String errorMessage = storageException.getMessage();
                    // Aquí puedes manejar el error de carga
                }
            }
        });
    }

    public void download(String path, String fileType) {

        File destinationFile = new File(path);
        Uri destinationUri = Uri.fromFile(destinationFile);

        StorageReference fileRef = storageReference.child(getFilePath(destinationFile.getName(), fileType));

        fileRef.getFile(destinationUri).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // La descarga se completó exitosamente
                // Aquí puedes manejar la lógica posterior a la descarga
                Log.d("FIRESTORE_DOWNLOAD", "Descargado: " + getFilePath(destinationFile.getName(), fileType));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // La descarga falló
                // Aquí puedes manejar el error de descarga
                Log.e("FIRESTORE_DOWNLOAD", "Error al descargar: " + getFilePath(destinationFile.getName(), fileType));
            }
        });
    }

    public void delete(String fileName, String fileType) {
        StorageReference fileRef = storageReference.child(getFilePath(fileName, fileType));

        fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // La eliminación se completó exitosamente
                // Aquí puedes manejar la lógica posterior a la eliminación
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // La eliminación falló
                // Aquí puedes manejar el error de eliminación
            }
        });
    }

    private String getFilePath(String fileName, String fileType) {
        String filePath;
        if (fileType.equalsIgnoreCase("Musica")) {
            filePath = "musica/" + fileName;
        } else {
            filePath = "imagenes/" + fileName;
        }
        return filePath;
    }

}
