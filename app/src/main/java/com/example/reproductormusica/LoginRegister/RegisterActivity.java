package com.example.reproductormusica.LoginRegister;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.reproductormusica.DB.DB;
import com.example.reproductormusica.FB.FBUsuarios;
import com.example.reproductormusica.Modelos.Usuario;
import com.example.reproductormusica.databinding.ActivityRegisterBinding;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private Usuario usuario;
    private DB db;
    private FBUsuarios fb_usuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        db = new DB(this);
        fb_usuarios = new FBUsuarios();

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_passwd = binding.txtPasswRegister.getText().toString();
                String txt_passwdConf = binding.txtPasswConfRegister.getText().toString();

                //Verificar formulario
                if (verificarFormulario()) {
                    //Crear usuario
                    String userName = binding.txtNameRegister.getText().toString();
                    String password = binding.txtPasswRegister.getText().toString();
                    String email = binding.txtEmailRegister.getText().toString();
                    usuario = new Usuario(userName, cifrar(password), email);

                    // Obtener una instancia de ConnectivityManager
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                    // Verificar el estado de la conexión de red
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        // La aplicación tiene conexión a Internet

                        fb_usuarios.checkUsuarioExistence(userName, new FBUsuarios.OnUsuarioExistenceListener() {
                            @Override
                            public void onUsuarioExists(boolean exists) {
                                if (exists) {
                                    binding.userName.setError("El usuario ya existe");
                                } else {
                                    // El usuario no existe, proceder con la creación
                                    fb_usuarios.uploadUsuario(usuario, new FBUsuarios.OnUsuarioUploadListener() {
                                        @Override
                                        public void onUsuarioUploaded() {
                                            // Añadir usuario a la base de datos local
                                            db.addUser(usuario);
                                        }

                                        @Override
                                        public void onFailure(String error) {
                                            // Tratar el error en la subida del usuario a Firebase
                                        }
                                    });
                                    // Cierra la actividad actual así vuelvo a la anterior
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(String error) {
                                // Tratar el error en la verificación de existencia del usuario
                            }
                        });

                    } else {
                        // La aplicación no tiene conexión a Internet
                        Toast.makeText(getApplicationContext(), "No hay conexión a Internet", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        binding.btnIrLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cierra la actividad actual asi vuelvo a la anterior
                finish();
            }
        });

    }

    private boolean verificarFormulario() {

        boolean correcto = true;

        //Comprobar que los campos no esten vacios
        if (binding.txtNameRegister.getText().toString().isEmpty()) {
            binding.userName.setError("Field can not be empty");
            correcto = false;
        }
        if (binding.txtEmailRegister.getText().toString().isEmpty()) {
            binding.email.setError("Field can not be empty");
            correcto = false;
        }
        if (binding.txtPasswRegister.getText().toString().isEmpty()) {
            binding.passwd.setError("Field can not be empty");
            correcto = false;

            //Comprobar que la contraseña cumpla los requisitos
        } else if (!verificarPassword(binding.txtPasswRegister.getText().toString())) {
            binding.passwd.setError("Contraseña inválida. Debe entre 8 y máximo 20 caracteres, un número, una letra minúscula, una letra mayúscula.");
            correcto = false;

            //Comprobar que la contraseña y su confirmacion sean iguales
        } else if (!binding.txtPasswRegister.getText().toString().equals(binding.txtPasswConfRegister.getText().toString())) {
            binding.passwd.setError("Las contraseñas no coinciden");
            binding.passwdConf.setError("Las contraseñas no coinciden");
            correcto = false;
        }
        if (binding.txtPasswConfRegister.getText().toString().isEmpty()) {
            binding.passwdConf.setError("Field can not be empty");
            correcto = false;
        }

        return correcto;
    }

    //Comprueba que la contraseña cumpla los requisitos
    private boolean verificarPassword(String passwd) {

        String regex = "^(?=.*[0-9])" //Un numero
                + "(?=.*[a-z])(?=.*[A-Z])"//Una minuscula y mayuscula
                + "(?=\\S+$).{8,20}$";//No permitir espacios en blanco y longitud entre 8 y 20

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(passwd);

        return m.matches();
    }

    // Cifra la contraseña
    public static String cifrar(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);

            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}