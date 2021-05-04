package com.elisoft.seguro.servicio;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.elisoft.seguro.R;
import com.elisoft.seguro.Suceso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Servicio_tarifa extends Service {
    Suceso suceso;
    int cantidad=0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        cantidad=0;
        get_tarifa();
    }

    public Servicio_tarifa() {
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service destruido de Tarifa", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // comenzar el servicio con el taxista....
    public class Servicio extends AsyncTask<String,Integer,String> {
        String stiempo="0",starifa="0",saltura="0",sdistancia="0";

        @Override
        protected String doInBackground(String... params) {

            String cadena = params[0];
            URL url = null;  // url donde queremos obtener informacion
            String devuelve = "500";


            if (params[1] == "1") {
                try {
                    HttpURLConnection urlConn;

                    url = new URL(cadena);
                    urlConn = (HttpURLConnection) url.openConnection();
                    urlConn.setDoInput(true);
                    urlConn.setDoOutput(true);
                    urlConn.setUseCaches(false);
                    urlConn.setRequestProperty("Content-Type", "application/json");
                    urlConn.setRequestProperty("Accept", "application/json");
                    urlConn.connect();

                    //se crea el objeto JSON
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("id_pedido", params[2]);
                    jsonParam.put("ci", params[3]);
                    jsonParam.put("placa", params[4]);
                    jsonParam.put("id_carrera", params[5]);


                    //Envio los prametro por metodo post
                    OutputStream os = urlConn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(jsonParam.toString());
                    writer.flush();
                    writer.close();

                    int respuesta = urlConn.getResponseCode();

                    StringBuilder result = new StringBuilder();

                    if (respuesta == HttpURLConnection.HTTP_OK) {

                        String line;
                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                        while ((line = br.readLine()) != null) {
                            result.append(line);
                        }

                        SystemClock.sleep(950);
                        JSONObject respuestaJSON = new JSONObject(result.toString());//Creo un JSONObject a partir del
                        suceso=new Suceso(respuestaJSON.getString("suceso"),respuestaJSON.getString("mensaje"));

                        if (suceso.getSuceso().equals("1")) {
                            try{
                                JSONArray usu=respuestaJSON.getJSONArray("carrera");
                                stiempo=respuestaJSON.getString("tiempo");
                                saltura=respuestaJSON.getString("altura");
                                sdistancia=respuestaJSON.getString("distancia");
                                starifa=respuestaJSON.getString("monto");
                                devuelve="1";
                            }catch (Exception e)
                            {
                                Log.e("Error",e.toString());
                                devuelve="2";
                            }
                        } else  {
                            devuelve = "2";
                        }

                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return devuelve;
        }


        @Override
        protected void onPreExecute() {



        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (suceso.getSuceso().equals("1")) {
                Intent i=new Intent("tarifa");
                i.putExtra("distancia",sdistancia);
                i.putExtra("tiempo",stiempo);
                i.putExtra("tarifa",starifa);
                sendBroadcast(i);

                if(cantidad<5){
                    cantidad++;
                }
            }else  if (suceso.getSuceso().equals("2")) {
            }else{

            }
            get_tarifa();




        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
        }


    }

    public void get_tarifa(){


        SharedPreferences ped_2=getSharedPreferences("ultimo_pedido_conductor",MODE_PRIVATE);
        SharedPreferences  prefe = getSharedPreferences("perfil_conductor", Context.MODE_PRIVATE);
        String id_conductor=prefe.getString("ci", "");
        String id_placa=prefe.getString("placa", "");
        int id_pedido=0;
        int id_carrera= 0;
        if(cantidad>3){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            id_pedido=Integer.parseInt(ped_2.getString("id_pedido",""));
            id_carrera = Integer.parseInt(ped_2.getString("id_carrera", ""));
            Servicio hilo_tarifa= new Servicio();
            hilo_tarifa.execute(getString(R.string.servidor) + "frmCarrera.php?opcion=get_carrera_por_id", "1", String.valueOf(id_pedido), String.valueOf(id_conductor), String.valueOf(id_placa), String.valueOf(id_carrera));// parametro que recibe el doinbackground



        }catch (Exception e)
        {
            id_pedido=0;
            id_carrera=0;
            onDestroy();
        }






    }

}
