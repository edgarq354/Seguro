package com.elisoft.seguro;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class Permisos extends AppCompatActivity implements View.OnClickListener {
    Button bt_siguiente;
    AlertDialog alerta_formulario_accesibilidad = null;


   static final int PERMISOS_BASICOS=1;
    int PERMISOS_DE_ACCESIBILIDAD=2;
    int PERMISOS_ACCESO_AL_USO=3;
    int PERMISOS_SUPERPOSICION=4;
    int PERMISOS_DESACTIVAR_NOTIFICACION=5;
    int PERMISOS_DESACTIVAR_OPTIMIZACION_BATERIA=6;

    Switch sw_permisos_basicos;
    TextView tv_permisos_basicos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permisos);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sw_permisos_basicos=findViewById(R.id.sw_permisos_basicos);
        tv_permisos_basicos=findViewById(R.id.tv_permisos_basicos);


        bt_siguiente=findViewById(R.id.bt_siguiente);

        bt_siguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Permisos.this,Login.class));
            }
        });

        sw_permisos_basicos.setOnClickListener(this);

    }



    public void  abrir_servicio_de_accesibilidad()
    {

        try {
            // get prompts.xml view
            LayoutInflater layoutInflater = LayoutInflater.from(Permisos.this);
            View promptView = layoutInflater.inflate(R.layout.permiso_accesibilidad, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Permisos.this);
            alertDialogBuilder.setView(promptView);
            alertDialogBuilder.setTitle("Para activar el servicio de accesibilidad");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
            alertDialogBuilder.show();



        }catch (Exception eeee)
        {

        }

    }

    public void habilitar_permisos_basicos()
    {

        String[] SMS_PERMISSIONS1 = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_PHONE_STATE,

                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,

                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_PHONE_NUMBERS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.MANAGE_OWN_CALLS,
                Manifest.permission.CALL_PHONE,

                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,

                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION };


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            SMS_PERMISSIONS1 = new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.READ_PHONE_STATE,

                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS,

                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.MANAGE_OWN_CALLS,
                    Manifest.permission.CALL_PHONE,

                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,

                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};
        }



        ActivityCompat.requestPermissions(Permisos.this,
                SMS_PERMISSIONS1,
                PERMISOS_BASICOS);


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        int per=0;
        switch (requestCode) {
            case PERMISOS_BASICOS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 ) {
                    for (int i=0;i<grantResults.length;i++){
                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                            per++;
                        }
                    }

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    //INCOMPLETO
                    this.sw_permisos_basicos.setChecked(false);

                }

                if(per<grantResults.length){
                    //INCOMPLETO
                    this.sw_permisos_basicos.setChecked(false);
                }else
                {
                    //COMPLETO
                    this.sw_permisos_basicos.setChecked(true);
                    this.tv_permisos_basicos.setVisibility(View.INVISIBLE);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
            default:
               return;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sw_permisos_basicos:
                habilitar_permisos_basicos();
                break;
        }
    }
}