package com.elisoft.seguro.servicio;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import android.widget.Toast;

import com.elisoft.seguro.Constants;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;


public class BootUpService extends JobIntentService {

    // You can assign any number to your job id
    final static int job_id = 95;
    Context mcontext;

    public static void enqueueWork(Context context, Intent intent) {

        enqueueWork(context, BootUpService.class, job_id, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // Any code you need to run on boot up can be placed here.
        // This is just a sample Toast message to show our BroadcastReceiver works.
        // We'll be using a Handler to run our Toast since it needs to run on the UI thread. Otherwise you don't need it.
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                verificar_estado_servicio();

            }
        });
    }

    public void verificar_estado_servicio(){
        SharedPreferences pedido=getSharedPreferences("ultimo_pedido_conductor",MODE_PRIVATE);
        int id_pedido=0;
        try{
            id_pedido=Integer.parseInt(pedido.getString("id_pedido","0"));
        }catch (Exception e){
            id_pedido=0;
        }
        if(id_pedido>0)
        {

            ejecutar_servicio_volley();
        }else
        {
            SharedPreferences  prefe = getSharedPreferences("perfil_conductor", Context.MODE_PRIVATE);

            if(prefe.getString("estado","0").equals("1"))
            {
                ejecutar_servicio_volley();
            }
        }

    }

    public void ejecutar_servicio_volley()
    {
        Toast.makeText(getApplicationContext(), "RECONECTADO CORRECTAMENTE", Toast.LENGTH_LONG).show();
        try{
            // startActivity(new Intent(BootUpService.this, Menu_taxi.class));
            Intent pushIntent=new Intent(BootUpService.this,Servicio_cargar_punto_google.class);
            pushIntent.setAction(Constants.ACTION.START_ACTION);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                startForegroundService(pushIntent);
            } else {
                startService(pushIntent);
            }
        }catch (Exception ee)
        {
            Toast.makeText(getApplicationContext(), ee.toString(), Toast.LENGTH_LONG).show();
        }
    }

}
