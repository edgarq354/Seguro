package com.elisoft.seguro.servicio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.elisoft.seguro.Constants;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ELIO on 05/01/2018.
 */

public class InicioMovilReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent){



        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //context.startActivity(new Intent(context,Menu_taxi.class));

            Toast.makeText(context,"REINICIO EL CELUAR",Toast.LENGTH_LONG).show();

            SharedPreferences pedido=context.getSharedPreferences("ultimo_pedido_conductor",MODE_PRIVATE);
            int id_pedido=0;
            try{
                id_pedido=Integer.parseInt(pedido.getString("id_pedido","0"));
            }catch (Exception e){
                id_pedido=0;
            }
            if(id_pedido>0)
            {

                Intent pushIntent=new Intent(context,Servicio_cargar_punto_google.class);
                pushIntent.setAction(Constants.ACTION.START_ACTION);
                context.startService(pushIntent);
            }else
            {

                if(get_estado(context)==1)
                {
                    Intent pushIntent=new Intent(context,Servicio_cargar_punto_google.class);
                    pushIntent.setAction(Constants.ACTION.START_ACTION);
                    context.startService(pushIntent);
                }
            }

        }


    }


    public int get_estado(Context context)
    {
        SharedPreferences  prefe = context.getSharedPreferences("perfil_conductor", Context.MODE_PRIVATE);
        int estado_perfil=0;
        try{
            estado_perfil=Integer.parseInt(prefe.getString("estado","0"));
        }catch (Exception e)
        {
            estado_perfil=0;
        }
        return estado_perfil;
    }


}
