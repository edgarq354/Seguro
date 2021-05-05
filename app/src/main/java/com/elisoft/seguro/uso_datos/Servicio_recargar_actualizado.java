package com.elisoft.seguro.uso_datos;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.elisoft.seguro.Constants;
import com.elisoft.seguro.R;
import com.elisoft.seguro.Suceso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class Servicio_recargar_actualizado extends IntentService {
    Suceso suceso;
    String numero="";
    String codigo="";
    String monto="";
    String id_recarga="";
    String empresa="";
    String mensaje_empresa="";
    String estado="";
    int operador=0;
    private static final String TAG = Servicio_recargar_actualizado.class.getSimpleName();
    RequestQueue queue=null;



    public Servicio_recargar_actualizado() {
        super("Servicio_recargar");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            //  operador=Integer.parseInt(intent.getStringExtra("operador"));
         //   numero=intent.getStringExtra("numero");
         //   monto=intent.getStringExtra("monto");
         //   codigo=intent.getStringExtra("codigo");
            id_recarga=intent.getStringExtra("id_recarga");
         //   empresa=intent.getStringExtra("empresa");
            mensaje_empresa=intent.getStringExtra("mensaje_empresa");
            estado=intent.getStringExtra("estado");


            handleActionRun();
            if (Constants.ACTION_RUN_ISERVICE.equals(action)) {


            }
        }
    }

    /**
     * Maneja la acción de ejecución del servicio
     */
    private void handleActionRun() {


        servicio_recarga_actualizar();
    }

    private void servicio_recarga_actualizar() {

        try {

            JSONObject jsonParam= new JSONObject();
            jsonParam.put("id_recarga", id_recarga);
            jsonParam.put("mensaje_empresa", mensaje_empresa);
            jsonParam.put("estado", estado);

            String url=getString(R.string.servidor) + "frmRecarga.php?opcion=actualizar_recarga";
            if (queue == null) {
                queue = Volley.newRequestQueue(this);
                Log.e("volley","Setting a new request queue");
            }


            JsonObjectRequest myRequest= new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonParam,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject respuestaJSON) {
                            try {
                                suceso= new Suceso(respuestaJSON.getString("suceso"),respuestaJSON.getString("mensaje"));

                                if (suceso.getSuceso().equals("1")) {
                                  //  monto_total=respuestaJSON.getString("monto_total");
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }
            ){
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> parametros= new HashMap<>();
                    parametros.put("content-type","application/json; charset=utf-8");
                    parametros.put("Authorization","apikey 849442df8f0536d66de700a73ebca-us17");
                    parametros.put("Accept", "application/json");

                    return  parametros;
                }
            };


            // TIEMPO DE ESPERA
            myRequest.setRetryPolicy(new DefaultRetryPolicy(6000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(myRequest);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {










        // Emisión para avisar que se terminó el servicio
        //Intent localIntent = new Intent(Constants.ACTION_PROGRESS_EXIT);
       // LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }




    //VERIFICAR SI ESTA CON CONEXION WIFI
    protected Boolean conectadoWifi(){
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (info != null) {
                return info.isConnected();
            }
        }
        return false;
    }
    //VERIFICAR SI ESTA CON CONEXION DE DATOS
    protected Boolean conectadoRedMovil(){
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (info != null) {
                return info.isConnected();
            }
        }
        return false;
    }

    protected Boolean estaConectado(){
        if(conectadoWifi()){
            return true;
        }else{
            return conectadoRedMovil();
        }
    }

    private void dailNumber(String USSD) {
        //startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + USSD)));
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        //callIntent.putExtra("simSlot", 0);
        // callIntent.putExtra("com.android.phone.extra.slot", operador);
        callIntent.putExtra("simSlot", operador); //For sim 1
        callIntent.setData(Uri.parse("tel:" + USSD));
        startActivity(callIntent);
    }
}

