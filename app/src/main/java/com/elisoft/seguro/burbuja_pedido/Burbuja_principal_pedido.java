package com.elisoft.seguro.burbuja_pedido;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.elisoft.seguro.Principal;
import com.elisoft.seguro.R;
import com.elisoft.seguro.Suceso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Burbuja_principal_pedido extends Service {
    private WindowManager mWindowManager;
    private View mChatHeadView;

    ImageView closeButton;
    ImageView im_punto_pasajero;
    ImageView im_punto_empresa;
    ImageView im_lo_tengo;
    ImageView im_finalizar;


    public Burbuja_principal_pedido() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mChatHeadView = LayoutInflater.from(this).inflate(R.layout.burbuja_principal_pedido, null);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        params.x = 0;
        params.y = 100;

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mChatHeadView, params);

        closeButton = (ImageView) mChatHeadView.findViewById(R.id.close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });

        im_punto_pasajero = (ImageView) mChatHeadView.findViewById(R.id.im_punto_pasajero);
        im_punto_empresa = (ImageView) mChatHeadView.findViewById(R.id.im_punto_empresa);
        im_lo_tengo = (ImageView) mChatHeadView.findViewById(R.id.im_lo_tengo);
        im_finalizar = (ImageView) mChatHeadView.findViewById(R.id.im_finalizar);


        verificar_estado_pedido();


        im_lo_tengo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences spedido = getSharedPreferences("ultimo_pedido_conductor", Context.MODE_PRIVATE);
                SharedPreferences perfil=getSharedPreferences("perfil_conductor",MODE_PRIVATE);
                SharedPreferences punto = getSharedPreferences("mi ubicacion", Context.MODE_PRIVATE);
                double latitud_fin = Double.parseDouble(punto.getString("latitud", "0"));
                double longitud_fin = Double.parseDouble(punto.getString("longitud", "0"));
                double altura_fin = Double.parseDouble(punto.getString("altura", "0"));

                servicio_ya_lo_tengo(
                        spedido.getString("id_pedido",""),
                        String.valueOf(latitud_fin),
                        String.valueOf(longitud_fin),
                        String.valueOf(altura_fin),
                        perfil.getString("ci",""),
                        perfil.getString("placa",""),
                        spedido.getString("id_usuario",""),
                        "");
            }
        });
        im_punto_pasajero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Uri uri = Uri.parse("http://maps.google.com/maps?saddr=&daddr=
               -17.766251,-63.165336");
                if (URLUtil.isValidUrl(uri.toString())) {
                    Intent intent=new Intent(Intent.ACTION_VIEW,uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity( intent);
                    }
                    */
                //buscamos una ruta para el motista     SOLO CO ACCESO A INTERNET
            marcar_ruta_pasajero();





            }
        });

        im_finalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent = new Intent(Burbuja_principal_pedido.this, Principal.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    stopSelf();
            }
        });

        final ImageView chatHeadImage = (ImageView) mChatHeadView.findViewById(R.id.chat_head_profile_iv);

        chatHeadImage.setOnTouchListener(new View.OnTouchListener() {
            private int lastAction;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private static final int MAX_CLICK_DURATION = 200;
            private long startClickTime;


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startClickTime = Calendar.getInstance().getTimeInMillis();
                        initialX = params.x;
                        initialY = params.y;

                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        lastAction = event.getAction();

                        return true;
                    case MotionEvent.ACTION_UP:

                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        if (clickDuration < MAX_CLICK_DURATION) {
                            Intent intent = new Intent(Burbuja_principal_pedido.this, Principal.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                            stopSelf();
                        }
                        lastAction = event.getAction();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        mWindowManager.updateViewLayout(mChatHeadView, params);
                        lastAction = event.getAction();
                        return true;
                }
                return false;
            }
        });

    }

    private void marcar_ruta_pasajero() {
        SharedPreferences prefe1=getSharedPreferences("ultimo_pedido_conductor", MODE_PRIVATE);
        double lat= Double.parseDouble(prefe1.getString("latitud",""));
        double lon= Double.parseDouble(prefe1.getString("longitud",""));
        if(prefe1.getString("id_pedido","").equals("")==false) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+lat+","+lon);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatHeadView != null) mWindowManager.removeView(mChatHeadView);
    }

    public void verificar_estado_pedido()
    {
        SharedPreferences pedido=getSharedPreferences("ultimo_pedido_conductor",MODE_PRIVATE);
        int id_pedido=0,id_carrera=0;
        try{
            id_pedido= Integer.parseInt(pedido.getString("id_pedido","0"));
            try{
                id_carrera= Integer.parseInt(pedido.getString("id_carrera","0"));
            }catch (Exception ee)
            {
                id_carrera=0;
            }

            if(id_pedido==0 && id_carrera==0)
            {
                //no tiene pedido
                vista_boton(false,false);
            }else if(id_carrera==0)
            {
                //tiene pedido
                vista_boton(true,false);
            }else{
                //tiene carrera en curso.
                vista_boton(false,true);
            }

        }catch (Exception e){
            e.printStackTrace();
            vista_boton(false,false);
        }
    }
    public void vista_boton(boolean enPedido, boolean enCarrera)
    {
        LinearLayout.LayoutParams cero = new LinearLayout.LayoutParams(0, 0);
        LinearLayout.LayoutParams normal = new LinearLayout.LayoutParams(150, 150);
        im_finalizar.setLayoutParams(cero);
        im_punto_empresa.setLayoutParams(cero);
        im_punto_pasajero.setLayoutParams(cero);
        im_lo_tengo.setLayoutParams(cero);

        if(enPedido){
            marcar_ruta_empresa();
            im_punto_empresa.setLayoutParams(normal);
            im_lo_tengo.setLayoutParams(normal);
        }else if(enCarrera){
            marcar_ruta_pasajero();
            im_punto_pasajero.setLayoutParams(normal);
            im_finalizar.setLayoutParams(normal);
        }
    }

    private void servicio_ya_lo_tengo(String id_pedido,
                                           String latitud,
                                           String longitud,
                                           String altura,
                                           String ci,
                                           String placa,
                                           String id_usuario,
                                           String direccion) {
        try {



            JSONObject jsonParam= new JSONObject();
            jsonParam.put("id_pedido", id_pedido);
            jsonParam.put("latitud", latitud);
            jsonParam.put("longitud", longitud);
            jsonParam.put("altura", altura);
            jsonParam.put("ci", ci);
            jsonParam.put("placa", placa);
            jsonParam.put("id_usuario", id_usuario);
            jsonParam.put("direccion", direccion);
            String url=getString(R.string.servidor) + "frmCarrera.php?opcion=comenzar_carrera";
            RequestQueue queue = Volley.newRequestQueue(this);


            JsonObjectRequest myRequest= new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonParam,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject respuestaJSON) {

                            Suceso suceso;

                            try {


                                suceso= new Suceso(respuestaJSON.getString("suceso"),respuestaJSON.getString("mensaje"));

                                if (suceso.getSuceso().equals("1")) {


                                    SharedPreferences pedido=getSharedPreferences("ultimo_pedido_conductor",MODE_PRIVATE);
                                    SharedPreferences.Editor editor=pedido.edit();
                                    editor.putString("id_carrera","1");
                                    editor.putString("estado","1");
                                    editor.putString("numero","1");
                                    editor.commit();
//////////////////-----------------------------------------------------------------------------------

                                    //final
                                    verificar_estado_pedido();

                                }else
                                {

                                }
                            } catch (JSONException e) {


                                e.printStackTrace();

                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

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


            myRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(myRequest);


        } catch (Exception e) {

        }
    }


    private void marcar_ruta_empresa() {
        SharedPreferences punto=getSharedPreferences("ultimo_pedido_conductor", Context.MODE_PRIVATE);
        double lat_empresa=Double.parseDouble(punto.getString("latitud_lugar","0"));
        double lon_empresa=Double.parseDouble(punto.getString("longitud_lugar","0"));


        if(punto.getString("id_pedido","").equals("")==false) {
            if(lat_empresa==0||lon_empresa==0)
            {
                Intent intent = new Intent(Burbuja_principal_pedido.this, Principal.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                stopSelf();
            }else{
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+lat_empresa+","+lon_empresa);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);}
        }
    }

}
