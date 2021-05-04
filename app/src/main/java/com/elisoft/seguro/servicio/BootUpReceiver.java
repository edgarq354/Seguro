package com.elisoft.seguro.servicio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        // Start our BootUpService class once boot up has completed
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {


/*
            SharedPreferences pedido=context.getSharedPreferences("ultimo_pedido_conductor",MODE_PRIVATE);
            int id_pedido=0;
            try{
                id_pedido=Integer.parseInt(pedido.getString("id_pedido","0"));
            }catch (Exception e){
                id_pedido=0;
            }
            if(id_pedido>0)
            {

                Intent pushIntent=new Intent(context,Servicio_cargar_punto_volley.class);
                pushIntent.setAction(Constants.ACTION.START_ACTION);
                context.startService(pushIntent);
            }else
            {
                SharedPreferences  prefe = context.getSharedPreferences("perfil_conductor", Context.MODE_PRIVATE);

                if(prefe.getString("estado","0").equals("1"))
                {
                    Intent pushIntent=new Intent(context,Servicio_cargar_punto_volley.class);
                    pushIntent.setAction(Constants.ACTION.START_ACTION);
                    context.startService(pushIntent);
                }
            }
            */



            BootUpService.enqueueWork(context, new Intent());
        }
    }



}
