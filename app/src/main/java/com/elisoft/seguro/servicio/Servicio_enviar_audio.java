package com.elisoft.seguro.servicio;

import android.app.IntentService;
import android.content.Intent;

import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Map;


/**
 * Created by ELIO on 12/01/2018.
 */

public class Servicio_enviar_audio extends IntentService {
   String  url="";
   int id_usuario=0;
   int id_administrador=0;
   String id_conductor="0";
   String titulo,mensaje,audio, tipo,canal;


    public Servicio_enviar_audio() {
        super("Servicio_enviar_audio");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

                Bundle bundle=intent.getExtras();
                id_usuario=bundle.getInt("id_usuario");
                id_administrador=bundle.getInt("id_administrador");
                id_conductor=bundle.getString("id_conductor");
                titulo=bundle.getString("titulo");
                mensaje=bundle.getString("mensaje");
                audio=bundle.getString("audio");
                tipo=bundle.getString("tipo");
                canal=bundle.getString("canal");
                url=bundle.getString("url");
                handleActionRun();

        }
    }

    /**
     * Maneja la acción de ejecución del servicio
     */
    private void handleActionRun() {
        try {


            servicio_enviar_audio(url,
                    String.valueOf(id_usuario),
                    String.valueOf(id_administrador),
                    String.valueOf(id_conductor),
                    titulo,
                    mensaje,
                    audio,
                    tipo,
                    canal
            );

            // Quitar de primer plano
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void servicio_enviar_audio(String url,
            String id_usuario,
            String id_administrador,
            String id_conductor,
            String titulo,
            String mensaje,
            String audio,
            String tipo,
            String canal) {

        try {
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("id_usuario", id_usuario);
            jsonParam.put("id_administrador", id_administrador);
            jsonParam.put("id_conductor", id_conductor);
            jsonParam.put("titulo", titulo);
            jsonParam.put("mensaje", mensaje);
            jsonParam.put("audio", audio);
            jsonParam.put("tipo", tipo);
            jsonParam.put("canal", canal);


            RequestQueue queue = Volley.newRequestQueue(this);

            JsonObjectRequest myRequest= new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonParam,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject respuestaJSON) {

                            try {
                                String dato=respuestaJSON.getString("suceso")+respuestaJSON.getString("mensaje");

                            } catch (JSONException e) {
                                e.printStackTrace();
                                //  mensaje_error("Falla en tu conexión a Internet.");
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    // mensaje_error("Falla en tu conexión a Internet.");
                }
            }
            ){
                public Map<String,String> getHeaders() throws AuthFailureError {
                    Map<String,String> parametros= new HashMap<>();
                    parametros.put("content-type","application/json; charset=utf-8");
                    parametros.put("Authorization","apikey 849442df8f0536d66de700a73ebca-us17");
                    parametros.put("Accept", "application/json");

                    return  parametros;
                }
            };

            queue.add(myRequest);
        } catch (Exception e) {
        }

    }







}

