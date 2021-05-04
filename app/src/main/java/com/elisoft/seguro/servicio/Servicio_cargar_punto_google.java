package com.elisoft.seguro.servicio;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;


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
import com.elisoft.seguro.notificaciones.SharedPrefManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;



public class Servicio_cargar_punto_google extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener ,
        ActivityCompat.OnRequestPermissionsResultCallback{
    /**
     * The Location listener.
     */
//    LocationListener locationListener;
    /**
     * The Location manager.
     */
//    LocationManager locationManager;
//    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int PETICION_PERMISO_LOCALIZACION = 101;
    private GoogleApiClient googleApiClient = null;
    private LocationRequest locationRequest = null;
    //    private LocationSettingsRequest locationSettingsRequest;
    private Location currentLocation;

    private static String LOCATION = "location";


//----- ITEM DE SERVICIOS


    private static final String TAG = Servicio_cargar_punto_google.class.getSimpleName();
    private final static String FOREGROUND_CHANNEL_ID = "foreground_channel_id";
    private NotificationManager mNotificationManager;
    private Handler handler;
    private int count = 0;
    private static int stateService = Constants.STATE_SERVICE.NOT_CONNECTED;
    String fecha = "";
    Suceso suceso;
    double latitud_a;
    double longitud_a;
    int numero = 0;
    int rotacion = 0;
    double altura = 0;

    int id_star = 0;
    boolean sw_subiendo;

    private RequestQueue queue = null;
    private RequestQueue queue_notificacion = null;
    String conexion_internet="";



    MyReceiver mConnectivityReceiver;
    IntentFilter mConnectivityIntentFilter;


    @Override
    public void onCreate() {
        /*configurar el cliente de google*/
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        /*conectar al servicio*/
        Log.w(TAG, "onCreate: Conectando el google client");
        googleApiClient.connect();


        //NOTIFICACION
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        stateService = Constants.STATE_SERVICE.NOT_CONNECTED;



        //VERIFICAR CONEXION A INTERNET
        mConnectivityIntentFilter = new IntentFilter();
        mConnectivityIntentFilter.addAction(Intent.ACTION_MANAGE_NETWORK_USAGE);

        mConnectivityReceiver = new MyReceiver();

    }

    @Override
    public void onDestroy() {


        /*remover las actualizaciones*/
        //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);

        /*desconectar del servicio*/

        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }

        //verificar si tiene conexion a internet
        unregisterReceiver(mConnectivityReceiver);
        //ESTADO DE LA NOTIFICACION
        stateService = Constants.STATE_SERVICE.NOT_CONNECTED;

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }


        //    mp.start();
        super.onStart(intent, startId);
        id_star = startId;
        Log.e("Google", "Service Started cargarpunto.. " + startId);


        sw_subiendo = false;

        // if user starts the service
        switch (intent.getAction()) {
            case Constants.ACTION.START_ACTION:
                Log.d(TAG, "Received user starts foreground intent");
                startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());

                break;
            case Constants.ACTION.STOP_ACTION:
                stopForeground(true);
                stopSelf();
                break;
            default:
                stopForeground(true);
                stopSelf();
        }

        //VERIFICAR SI TIENE CONEXION A INTERNET
        registerReceiver(mConnectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        //registerReceiver(mConnectivityReceiver, mConnectivityIntentFilter);
        //return START_NOT_STICKY;
        return START_REDELIVER_INTENT;
    }


    @SuppressLint("WrongConstant")
    private Notification prepareNotification() {
        // handle build version above android oreo
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                mNotificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {
            CharSequence name = getString(R.string.text_name_notification);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID, name, importance);
            channel.enableVibration(false);
            mNotificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, Principal.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // if min sdk goes below honeycomb
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }*/

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // make a stop intent
        Intent stopIntent = new Intent(this, Servicio_cargar_punto_google.class);
        stopIntent.setAction(Constants.ACTION.STOP_ACTION);
        PendingIntent pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        //remoteViews.setOnClickPendingIntent(R.id.btn_stop, pendingStopIntent);
        remoteViews.setTextViewText(R.id.tv_fecha, fecha);

        // if it is connected
        switch (stateService) {
            case Constants.STATE_SERVICE.NOT_CONNECTED:
                remoteViews.setTextViewText(R.id.tv_estado, "Preparando la conexión. . ."+conexion_internet);
                remoteViews.setImageViewResource(R.id.im_estado, R.drawable.ic_advertencia);
                break;
            case Constants.STATE_SERVICE.CONNECTED:
                remoteViews.setTextViewText(R.id.tv_estado, "En linea."+conexion_internet);
                remoteViews.setImageViewResource(R.id.im_estado, R.drawable.ic_ok);
                break;
            case Constants.STATE_SERVICE.GPS_INACTIVO:
                remoteViews.setTextViewText(R.id.tv_estado, "GPS inactivo."+conexion_internet);
                remoteViews.setImageViewResource(R.id.im_estado, R.drawable.ic_advertencia);
                break;
            case Constants.STATE_SERVICE.SIN_INTERNET:
                remoteViews.setTextViewText(R.id.tv_estado, "Sin conexión a internet . ."+conexion_internet);
                remoteViews.setImageViewResource(R.id.im_estado, R.drawable.ic_advertencia);
                break;
            default:
                remoteViews.setTextViewText(R.id.tv_estado, "En espera."+conexion_internet);
                remoteViews.setImageViewResource(R.id.im_estado, R.drawable.ic_advertencia);
                break;
        }



        // notification builder
        NotificationCompat.Builder notificationBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID);
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }
        notificationBuilder
                .setContent(remoteViews)
                .setSmallIcon(R.drawable.ic_notificacion_logo)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        return notificationBuilder.build();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.w(TAG, "onConnected: Conectado");
        /*conectar a la libreria de google*/


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ubicacion","Sin permiso de ubicación");
        }
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);


        if (currentLocation != null){
            Log.e("Google Api",currentLocation.getLatitude()+","+currentLocation.getLongitude());
        }else{
            Log.e("ubicacion","Sin ubicación");
        }

        /*iniciar las peticiones*/
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(7000)
                .setFastestInterval(7000);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Log.w(TAG, "onConnectionSuspended: Desconectado");;
        } else if (i == CAUSE_NETWORK_LOST) {
            Log.w(TAG, "onConnectionSuspended: Sin Red" );
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed: Coneccion fallida: " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.w(TAG, "onLocationChanged: " + location.toString() );
        currentLocation = location;



        //OBTENER UBICACION


        //  if (isConnectingToInternet(getApplicationContext())) {
        if(true){
            //ENVIANDO UBICACION
            stateService = Constants.STATE_SERVICE.CONNECTED;
            startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());


            if (sw_subiendo == false) {

                try {
                    float precision = location.getAccuracy();
                    float bearing = location.getBearing();


// si esta en camino hace un pedido.. el id_carrera se lo va a colocar con un -1
             /*       SharedPreferences prefe=getSharedPreferences("ultimo_pedido",Context.MODE_PRIVATE);
                        int estado=Integer.parseInt(prefe.getString("estado","0"));
                        String id_pedido=prefe.getString("id_pedido","");
                        */
                    // Bucle de simulación de pedido cuando tiene estado del pedido=0  o sino con un estado=1 cuando tiene carreras...

                    SharedPreferences prefe = getSharedPreferences("perfil_conductor", Context.MODE_PRIVATE);
                    String id = prefe.getString("ci", "");
                    String placa = prefe.getString("placa", "");

                    int estado_perfil = 0;
                    try {
                        estado_perfil = Integer.parseInt(prefe.getString("estado", "0"));
                    } catch (Exception e) {
                        estado_perfil = 0;
                    }


                    //verificamos el estado del motista estaactivo o inactivo.
                    //verificamos el estado del motista estaactivo o inactivo.
                    if (estado_perfil == 0) {
                        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                        Log.e("servicio google", "eliminado.." + id_star);
                    }


                    double latitud = location.getLatitude();
                    double longitud = location.getLongitude();
                    latitud_a = latitud;
                    longitud_a = longitud;
                    altura=location.getAltitude();
                    if (location.hasBearing()) {
                        rotacion = Math.round(bearing);
                    }
                    //servicio para cargar puntos..




                    if (id.equals("") == false && latitud != 0 && longitud != 0  ) {

                        try {
                            SharedPreferences punto_2 = getSharedPreferences("mi ubicacion_2", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = punto_2.edit();
                            editor.putString("latitud", String.valueOf(latitud_a));
                            editor.putString("longitud", String.valueOf(longitud_a));
                            editor.putString("altura", String.valueOf(altura));
                            editor.putString("rotacion", String.valueOf(rotacion));
                            editor.commit();


                            SharedPreferences punto = getSharedPreferences("mi ubicacion", Context.MODE_PRIVATE);
                            double latitud_fin = Double.parseDouble(punto.getString("latitud", "0"));
                            double longitud_fin = Double.parseDouble(punto.getString("longitud", "0"));
                            int rotacion_fin = Integer.parseInt(punto.getString("rotacion","0"));
                            double altura_fin = Double.parseDouble(punto.getString("altura","0"));
                            double distancia = getDistancia(latitud, longitud, latitud_fin, longitud_fin);

                            Log.d("latitude", "(" + location.getLatitude() + "," + location.getLongitude() + "),Precision:" + precision + ". Rotacion:" + rotacion + ". Distancia:" + distancia + ". start:" + id_star);

                            SharedPreferences ped_2 = getSharedPreferences("ultimo_pedido_conductor", MODE_PRIVATE);
                            int id_pedido = 0;
                            try {
                                id_pedido = Integer.parseInt(ped_2.getString("id_pedido", ""));
                            } catch (Exception e) {
                                id_pedido = 0;
                            }
                            if (id_pedido == 0) {
                                precision = 0;
                            }



                            try {
                                if (distancia >= 3 && precision < 30) {

                                    int id_carrera = Integer.parseInt(ped_2.getString("id_carrera", ""));
                                    numero = Integer.parseInt(ped_2.getString("numero", "1"));

                                    if (ped_2.getString("id_pedido", "").equals("") == false && ped_2.getString("id_pedido", "0").equals("0") == false && ped_2.getString("estado", "").equals("1") == true) {
                                        //hilo_traking.execute(getString(R.string.servidor) + "frmTaxi.php?opcion=set_ubicacion_punto_carrera", "2", id, String.valueOf(latitud), String.valueOf(longitud), placa, String.valueOf(id_carrera), String.valueOf(numero), String.valueOf(id_pedido), String.valueOf(distancia));

                                        servicio_ubicacion_punto_carrera(id,
                                                String.valueOf(latitud),
                                                String.valueOf(longitud),
                                                placa,
                                                String.valueOf(id_carrera),
                                                String.valueOf(numero),
                                                String.valueOf(id_pedido),
                                                String.valueOf(distancia));
                                    } else {
                                        //hilo_traking.execute(getString(R.string.servidor) + "frmTaxi.php?opcion=set_ubicacion_punto", "1", id, String.valueOf(latitud), String.valueOf(longitud), placa);// parametro que recibe el doinbackground
                                        servicio_ubicacion_punto(
                                                id,
                                                String.valueOf(latitud),
                                                String.valueOf(longitud),
                                                placa);
                                    }
                                } else   {
                                    if (ped_2.getString("id_pedido", "").equals("") == false && ped_2.getString("id_pedido", "0").equals("0") == false && ped_2.getString("estado", "").equals("1") == true) {
                                        servicio_ubicacion_punto(
                                                id,
                                                String.valueOf(latitud),
                                                String.valueOf(longitud),
                                                placa);
                                    } else {
                                        //hilo_traking.execute(getString(R.string.servidor) + "frmTaxi.php?opcion=set_ubicacion_punto", "1", id, String.valueOf(latitud), String.valueOf(longitud), placa);// parametro que recibe el doinbackground

                                        servicio_ubicacion_punto(
                                                id,
                                                String.valueOf(latitud),
                                                String.valueOf(longitud),
                                                placa);
                                    }
                                }
                            } catch (Exception e) {
                                try {


                                    //  hilo_traking.execute(getString(R.string.servidor) + "frmTaxi.php?opcion=set_ubicacion_punto", "1", id, String.valueOf(latitud), String.valueOf(longitud), placa);// parametro que recibe el doinbackground
                                    servicio_ubicacion_punto(
                                            id,
                                            String.valueOf(latitud),
                                            String.valueOf(longitud),
                                            placa);

                                } catch (Exception a) {
                                    a.printStackTrace();
                                    sw_subiendo = false;
                                    //reconectando("←←← ▼ →→→");
                                }
                            }

                            //envia una notificacion al pasajero cuando esta a 50 metros de deistancia de donde pidio el Taxi

                            SharedPreferences ped = getSharedPreferences("ultimo_pedido_conductor", MODE_PRIVATE);
                            if (ped.getString("id_pedido", "").equals("") == false) {
                                SharedPreferences punto_taxi = getSharedPreferences("mi ubicacion", MODE_PRIVATE);
                                double lat_taxi = Double.parseDouble(punto_taxi.getString("latitud", "0"));
                                double lon_taxi = Double.parseDouble(punto_taxi.getString("longitud", "0"));
                                double lat_pedido = Double.parseDouble(ped.getString("latitud", "0"));
                                double lon_pedido = Double.parseDouble(ped.getString("longitud", "0"));
                                double distancia_cerca = getDistancia(lat_taxi, lon_taxi, lat_pedido, lon_pedido);
                                int notificacion_cerca = ped.getInt("notificacion_cerca", 0);
                                int notificacion_llego = ped.getInt("notificacion_llego", 0);

                                if (distancia_cerca <= 500 && notificacion_cerca == 0) {
                                    SharedPreferences pedido = getSharedPreferences("ultimo_pedido_conductor", MODE_PRIVATE);
                                    SharedPreferences.Editor editor2 = pedido.edit();
                                    editor2.putInt("notificacion_cerca", 1);
                                    editor2.commit();

                                    String id_pedido_1 = ped.getString("id_pedido", "");
                                    servicio_estoy_cerca(id_pedido_1);
                                } else if (distancia_cerca <= 50 && notificacion_llego == 0) {
                                    SharedPreferences pedido = getSharedPreferences("ultimo_pedido_conductor", MODE_PRIVATE);
                                    SharedPreferences.Editor editor3 = pedido.edit();
                                    editor3.putInt("notificacion_llego", 1);
                                    editor3.commit();

                                    String id_pedido_1 = ped.getString("id_pedido", "");
                                    servicio_notificacion_llego(id_pedido_1);
                                }

                            }

                        } catch (Exception e) {
                            SharedPreferences punto = getSharedPreferences("mi ubicacion", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = punto.edit();
                            editor.putString("latitud", "0");
                            editor.putString("longitud", "0");
                            editor.putString("altura", "0");
                            editor.putString("rotacion", "0");
                            editor.commit();

                            //reconectando("←←← ▲ →→→");
                            sw_subiendo = false;
                        }


                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    sw_subiendo = false;
                    e.printStackTrace();
                    //reconectando("Localizando - ►►►");
                }
            }

        }else{
            sw_subiendo=false;
            // SIN CONEXION A INTERNET
            stateService = Constants.STATE_SERVICE.SIN_INTERNET;
            startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());
            //reconectando("Sin acceso a internet");
        }





    }




//conexion de internet

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();
            onNetworkChange(ni);
        }
        // constructor
        public MyReceiver(){
            Log.w("My receive","Creado");
        }
    };


    private void onNetworkChange(NetworkInfo networkInfo) {
        if (networkInfo != null) {
            if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                // CONNECTED
               // conexion_internet="1";
                sw_subiendo=false;
                startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());
            } else {
                // DISCONNECTED"
               // conexion_internet="0";
                sw_subiendo=true;

                //ERROR AL CONECTAR CON EL SERVIDOR
                Date date = new Date();
                DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd 'de' MMMM");
                fecha=hourdateFormat.format(date);

                stateService = Constants.STATE_SERVICE.NOT_CONNECTED;
                startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());


            }
        }else{
            // DISCONNECTED"
           // conexion_internet="01";
            sw_subiendo=true;

            Date date = new Date();
            DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd 'de' MMMM");
            fecha=hourdateFormat.format(date);

            stateService = Constants.STATE_SERVICE.NOT_CONNECTED;
            startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());

        }
    }







    private void servicio_notificacion_llego(String id_pedido_1)  {

        try {


            String token= SharedPrefManager.getInstance(this).getDeviceToken();

            JSONObject jsonParam= new JSONObject();
            jsonParam.put("id_pedido",id_pedido_1);
            jsonParam.put("rotacion", String.valueOf(rotacion));

            jsonParam.put("token", token);
            String url=getString(R.string.servidor) + "frmPedido.php?opcion=notificacion_llego_el_taxi";
            if (queue_notificacion == null) {
                queue_notificacion = Volley.newRequestQueue(this);
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
                                    //final
                                    //se envio la notificacion... al pasajero
                                    SharedPreferences pedido=getSharedPreferences("ultimo_pedido_conductor",MODE_PRIVATE);
                                    SharedPreferences.Editor editor=pedido.edit();
                                    editor.putInt("notificacion_llego",1);
                                    editor.commit();
                                }
                                else
                                {
                                    //final
                                    SharedPreferences pedido=getSharedPreferences("ultimo_pedido_conductor",MODE_PRIVATE);
                                    SharedPreferences.Editor editor=pedido.edit();
                                    editor.putInt("notificacion_llego",0);
                                    editor.commit();
                                }
                                //final

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

            myRequest.setRetryPolicy(new DefaultRetryPolicy(7000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue_notificacion.add(myRequest);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void servicio_estoy_cerca(String id_pedido_1)  {
        try {

            final String latitud_h;
            final String longitud_h;
            final String rotacion_h;


// set_ubicacion_punto  ----- cargar punto de ubicacion....

            String token= SharedPrefManager.getInstance(this).getDeviceToken();

            JSONObject jsonParam= new JSONObject();
            jsonParam.put("id_pedido",id_pedido_1);

            jsonParam.put("token", token);
            String url=getString(R.string.servidor) + "frmPedido.php?opcion=estoy_cerca";
            if (queue_notificacion == null) {
                queue_notificacion = Volley.newRequestQueue(this);
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
                                    //final
                                    //se envio la notificacion... al pasajero
                                    SharedPreferences pedido=getSharedPreferences("ultimo_pedido_conductor",MODE_PRIVATE);
                                    SharedPreferences.Editor editor=pedido.edit();
                                    editor.putInt("notificacion_cerca",1);
                                    editor.commit();
                                }
                                else
                                {
                                    //final
                                    //se envio la notificacion... al pasajero
                                    SharedPreferences pedido=getSharedPreferences("ultimo_pedido_conductor",MODE_PRIVATE);
                                    SharedPreferences.Editor editor=pedido.edit();
                                    editor.putInt("notificacion_cerca",0);
                                    editor.commit();
                                }
                                //final

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


            myRequest.setRetryPolicy(new DefaultRetryPolicy(7000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue_notificacion.add(myRequest);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void servicio_ubicacion_punto(
            String id_conductor,
            String latitud,
            String longitud,
            String placa) {
        try {

            final String latitud_h;
            final String longitud_h;
            final String rotacion_h;

            sw_subiendo = true;
// set_ubicacion_punto  ----- cargar punto de ubicacion....

            String token= SharedPrefManager.getInstance(this).getDeviceToken();

            JSONObject jsonParam= new JSONObject();
            jsonParam.put("ci", id_conductor);
            jsonParam.put("latitud", latitud);
            jsonParam.put("longitud", longitud);
            jsonParam.put("placa", placa);
            jsonParam.put("rotacion", String.valueOf(rotacion));
            latitud_h=latitud;
            longitud_h=longitud;
            rotacion_h= String.valueOf(rotacion);

            jsonParam.put("token", token);
            String url=getString(R.string.servidor) + "frmTaxi.php?opcion=set_ubicacion_punto";


            if (queue == null) {
                queue = Volley.newRequestQueue(this);
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
                                    //final
                                    SharedPreferences punto=getSharedPreferences("mi ubicacion", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor=punto.edit();
                                    editor.putString("latitud",latitud_h);
                                    editor.putString("longitud",longitud_h);
                                    editor.putString("rotacion",rotacion_h);
                                    editor.commit();

                                    Log.w("Servicio cargar punto","Se cargo la ubicacion al servidor ("+latitud_a+","+longitud_a+")");
                                    //reconectando("ESTOY LIBRE ☺");

                                    //OBTENER FECHA DE LA ULTIMA CONEXION
                                    Date date = new Date();
                                    DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd 'de' MMMM");
                                    fecha=hourdateFormat.format(date);

                                    stateService = Constants.STATE_SERVICE.CONNECTED;
                                    startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());


                                }
                                else
                                {
                                    //final
                                    //reconectando("ESTOY LIBRE ☻");
                                }
                                //final
                                sw_subiendo=false;
                            } catch (JSONException e) {


                                e.printStackTrace();
                                sw_subiendo = false;
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    sw_subiendo = false;

                    //ERROR AL CONECTAR CON EL SERVIDOR
                    Date date = new Date();
                    DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd 'de' MMMM");
                    fecha=hourdateFormat.format(date);

                    stateService = Constants.STATE_SERVICE.NOT_CONNECTED;
                    startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());


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


            myRequest.setRetryPolicy(new DefaultRetryPolicy(7000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(myRequest);


        } catch (Exception e) {
            e.printStackTrace();
            sw_subiendo = false;
        }
    }

    private void servicio_ubicacion_punto_carrera(
            String id_conductor,
            String latitud,
            String longitud,
            String placa,
            String id_carrera,
            String numero,
            String id_pedido,
            String distancia) {

        try {

            final String latitud_h;
            final String longitud_h;
            final String rotacion_h;

            sw_subiendo = true;
// set_ubicacion_punto  ----- cargar punto de ubicacion....

            String token= SharedPrefManager.getInstance(this).getDeviceToken();

            JSONObject jsonParam= new JSONObject();
            jsonParam.put("ci", id_conductor);
            jsonParam.put("latitud", latitud);
            jsonParam.put("longitud", longitud);
            jsonParam.put("placa", placa);
            jsonParam.put("id_carrera", id_carrera);
            jsonParam.put("numero", numero);
            jsonParam.put("id_pedido", id_pedido);
            jsonParam.put("distancia", distancia);
            jsonParam.put("rotacion", String.valueOf(rotacion));
            latitud_h=latitud;
            longitud_h=longitud;
            rotacion_h= String.valueOf(rotacion);

            jsonParam.put("token", token);
            String url=getString(R.string.servidor) + "frmTaxi.php?opcion=set_ubicacion_punto_carrera";
            if (queue == null) {
                queue = Volley.newRequestQueue(this);
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

                                    try {
                                        SharedPreferences ped = getSharedPreferences("ultimo_pedido_conductor", MODE_PRIVATE);
                                        int numero_carrera = Integer.parseInt(ped.getString("numero", "1"));
                                        numero_carrera++;
                                        SharedPreferences.Editor editar = ped.edit();
                                        editar.putString("numero", String.valueOf(numero_carrera));
                                        editar.commit();
                                    }catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }

                                    //final
                                    SharedPreferences punto=getSharedPreferences("mi ubicacion", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor=punto.edit();
                                    editor.putString("latitud",latitud_h);
                                    editor.putString("longitud",longitud_h);
                                    editor.putString("rotacion",rotacion_h);
                                    editor.commit();

                                    Log.w("Servicio cargar punto","Se cargo la ubicacion al servidor ("+latitud_a+","+longitud_a+")");

                                    //reconectando("EN SERVICIO - ►");

                                }
                                else
                                {
                                    try {
                                        SharedPreferences ped = getSharedPreferences("ultimo_pedido_conductor", MODE_PRIVATE);
                                        int numero_carrera = Integer.parseInt(ped.getString("numero", "1"));
                                        numero_carrera++;
                                        SharedPreferences.Editor editar = ped.edit();
                                        editar.putString("numero", String.valueOf(numero_carrera));
                                        editar.commit();
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    //reconectando("EN SERVICIO ☻");
                                }
                                //final
                                sw_subiendo=false;
                            } catch (JSONException e) {

                                sw_subiendo = false;
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    sw_subiendo = false;

                    //ERROR AL CONECTAR CON EL SERVIDOR
                    Date date = new Date();
                    DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd 'de' MMMM");
                    fecha=hourdateFormat.format(date);

                    stateService = Constants.STATE_SERVICE.NOT_CONNECTED;
                    startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());

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

            myRequest.setRetryPolicy(new DefaultRetryPolicy(7000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


            queue.add(myRequest);


        } catch (Exception e) {
            e.printStackTrace();
            sw_subiendo = false;
        }

    }



    public static boolean isConnectingToInternet(Context _context) {
        ConnectivityManager connectivity = (ConnectivityManager) _context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    public  double getDistancia(double lat_a,double lon_a, double lat_b, double lon_b){
        long  Radius = 6371000;
        double dLat = Math.toRadians(lat_b-lat_a);
        double dLon = Math.toRadians(lon_b-lon_a);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) * Math.sin(dLon /2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double distancia=(Radius * c);
        return  distancia;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PETICION_PERMISO_LOCALIZACION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Permiso concedido

                @SuppressWarnings("MissingPermission")
                Location lastLocation =
                        LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            } else {


                Log.e("permiso ssegundo plano", "Permiso denegado");
            }
        }
    }





}
