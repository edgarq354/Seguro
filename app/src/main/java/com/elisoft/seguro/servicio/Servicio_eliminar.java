package com.elisoft.seguro.servicio;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.elisoft.seguro.Constants;
import com.elisoft.seguro.R;
import com.elisoft.seguro.SqLite.AdminSQLiteOpenHelper;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class Servicio_eliminar extends IntentService {
    String sid_pedido;

    private static final String TAG = Servicio_eliminar.class.getSimpleName();


    public Servicio_eliminar() {
        super("Servicio_eliminar");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            sid_pedido=intent.getExtras().getString("id_pedido","");
            if (Constants.ACTION_RUN_ISERVICE.equals(action)) {
                handleActionRun();
            }
        }
    }

    /**
     * Maneja la acción de ejecución del servicio
     */
    private void handleActionRun() {
        try {


            // Bucle de simulación de pedido cuando tiene estado del pedido=0
            Thread.sleep(300000);

            // Bucle de simulación cuando tiene carreras... carrera es cuando tiene el estado=1

            eliminar_ultimo_pedido();
            // Quitar de primer plano
            stopForeground(true);
            // si nuestro estado esta en 2 o mayor .. quiere decir que no nuestro pedido se finalizo o sino se cancelo... sin nninguna carrera...
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Se elimino la ultima solicitud...", Toast.LENGTH_SHORT).show();

        // Emisión para avisar que se terminó el servicio
        Intent localIntent = new Intent(Constants.ACTION_PROGRESS_EXIT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    public void eliminar_ultimo_pedido()
    {
        try {
            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, getString(R.string.nombre_sql), null, Integer.parseInt(getString(R.string.version_sql)));

            SQLiteDatabase bd = admin.getWritableDatabase();
            bd.execSQL("DELETE FROM pedido_taxi WHERE id>="+sid_pedido);
            bd.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }



}

