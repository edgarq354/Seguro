package com.elisoft.seguro.uso_datos;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

/**
 * Created by ELIO on 27/02/2018.
 */

public class XXXX extends AccessibilityService {

    public static String TAG = "XXXX";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent");
        String text = event.getText().toString();

        if (event.getClassName().equals("android.app.AlertDialog")) {
            performGlobalAction(GLOBAL_ACTION_BACK);

            Log.e(TAG, text);
            Intent intent = new Intent("com.times.ussd.action.REFRESH");
            intent.putExtra("message", text);
            Toast.makeText(this,text, Toast.LENGTH_LONG).show();
            //Globals.setTEXT(text);

            SharedPreferences prefe = getSharedPreferences("recarga", Context.MODE_PRIVATE);


            Intent servicio_recarga=new Intent(this, Servicio_recargar_actualizado.class);
            servicio_recarga.putExtra("id_recarga",prefe.getString("id_recarga",""));
            servicio_recarga.putExtra("mensaje_empresa",text);
            servicio_recarga.putExtra("estado","RECARGADO");
            servicio_recarga.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(servicio_recarga);
        }

    }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.packageNames = new String[]{"com.android.phone"};
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);
    }


}
