package com.example.reproductormusica.LoginRegister;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.example.reproductormusica.DB.DB;
import com.example.reproductormusica.FB.FBUsuarios;
import com.example.reproductormusica.Modelos.Usuario;
import com.example.reproductormusica.Reproductor.ReproductorActivity;
import com.example.reproductormusica.databinding.ActivityLoginBinding;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private Usuario usuario;
    private DB db;
    private FBUsuarios fb_usuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        db = new DB(this);
        fb_usuarios = new FBUsuarios();


        // Entrar directamente si isLoggedIn esta activado
        SharedPreferences sh = getSharedPreferences("Login", Context.MODE_PRIVATE);
                boolean login = sh.getBoolean("isLoggedIn", false);
                if (login){
                    //Iniciar actividad de reproductor
                    Intent intent = new Intent(LoginActivity.this, ReproductorActivity.class);
                    startActivity(intent);
                    //Cerrar actividad
                    finish();
                }

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Verificar formulario login
                if(verificarFormulario()){
                    fb_usuarios.downloadUsuario(binding.txtNameLogin.getText().toString(), new FBUsuarios.OnUsuarioDownloadedListener() {
                        @Override
                        public void onUsuarioDownloaded(Usuario usuarioDescargado) {
                            //Comprobar si la contraseña del usuario es igual a la del formulario
                            if (usuarioDescargado.getPassword().equals(cifrar(binding.txtPasswLogin.getText().toString()))){
                                //Iniciar actividad de reproductor
                                Intent intent = new Intent(LoginActivity.this, ReproductorActivity.class);
                                startActivity(intent);

                                //Guardar que has logeado
                                SharedPreferences.Editor editor = sh.edit();
                                editor.putBoolean("isLoggedIn", true);
                                editor.putString("User", usuarioDescargado.getUserName());
                                editor.apply();

                                if (!db.checkUserExist(usuarioDescargado.getUserName())){
                                    db.addUser(usuarioDescargado);
                                }

                                //Cerrar actividad
                                finish();

                                //Mostrar error si la contraseña es incorrecta
                            }else{
                                binding.passwd.setError("El usuario o contraseña son incorrectos");
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            //Obtener usuario
                            usuario = db.getUsuario(binding.txtNameLogin.getText().toString());
                            //Mostrar error si el usuario no existe
                            if (usuario == null){
                                binding.userName.setError("El usuario o contraseña son incorrectos");

                                //Si el usuario existe
                            }else{
                                //Comprobar si la contraseña del usuario es igual a la del formulario
                                if (usuario.getPassword().equals(cifrar(binding.txtPasswLogin.getText().toString()))){
                                    //Iniciar actividad de reproductor
                                    Intent intent = new Intent(LoginActivity.this, ReproductorActivity.class);
                                    startActivity(intent);

                                    //Guardar que has logeado
                                    SharedPreferences.Editor editor = sh.edit();
                                    editor.putBoolean("isLoggedIn", true);
                                    editor.putString("User", usuario.getUserName());
                                    editor.apply();

                                    //Cerrar actividad
                                    finish();

                                    //Mostrar error si la contraseña es incorrecta
                                }else{
                                    binding.passwd.setError("El usuario o contraseña son incorrectos");
                                }
                            }
                        }
                    });


                }
            }
        });

        binding.btnIrRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Abrir actividad de registro
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }

    private boolean verificarFormulario(){

        boolean correcto = true;

        //Comprobar que los campos no esten vacios
        if (binding.txtNameLogin.getText().toString().isEmpty()){
            binding.userName.setError("Field can not be empty");
            correcto = false;
        }
        if (binding.txtPasswLogin.getText().toString().isEmpty()){
            binding.passwd.setError("Field can not be empty");
            correcto = false;
        }

        return correcto;
    }


    // Cifrar contraseña
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
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


}