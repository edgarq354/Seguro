package com.elisoft.seguro.servicio;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.elisoft.seguro.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;


/**
 * Created by ELIO on 12/01/2018.
 */

public class Servicio_descargar_imagen_perfil extends IntentService {
    String id_usuario="0";



    public Servicio_descargar_imagen_perfil() {
        super("Servicio_descargar_imagen_perfil");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

                Bundle bundle=intent.getExtras();
                id_usuario=bundle.getString("id_conductor");
                handleActionRun();

        }
    }

    /**
     * Maneja la acción de ejecución del servicio
     */
    private void handleActionRun() {
        try {

            getImage(String.valueOf(id_usuario));
            // Quitar de primer plano
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void getImage(String id)//
    {
        /*
        class GetImage extends AsyncTask<String,Void,Bitmap> {



            public GetImage() {

            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);

                if( bitmap!=null)
                {
                    guardar_en_memoria(bitmap);
                }


            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Bitmap doInBackground(String... strings) {
                String url = getString(R.string.servidor)+"frmTaxi.php?opcion=get_imagen&id_conductor="+strings[0];//hace consulta ala Bd para recurar la imagen

                Bitmap mIcon =null;
                try {
                    InputStream in = new java.net.URL(url).openStream();
                    mIcon = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                }
                return mIcon;
            }
        }

        GetImage gi = new GetImage();
        gi.execute(id);
        */



        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable(){
            @Override
            public void run() {
                String  url=  getString(R.string.servidor_web)+"public/Imagen_Conductor/Perfil-"+id_usuario+".png";
                Picasso.with(getApplicationContext()).load(url).into(new Target() {

                    @Override
                    public void onPrepareLoad(Drawable arg0) {
                    }

                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom arg1) {
                        if( bitmap!=null)
                        {
                            guardar_en_memoria(bitmap);
                        }
                    }

                    @Override
                    public void onBitmapFailed(Drawable arg0) {
                    }
                });

            }
        });


        /*
        Picasso.with(this).load(url).transform(new Transformation() {
            @Override
            public Bitmap transform(Bitmap bitmap) {
                if( bitmap!=null)
                {
                    guardar_en_memoria(bitmap);
                }
                return null;
            }

            @Override
            public String key() {
                return null;
            }
        });

*/
    }

    private void guardar_en_memoria(Bitmap bitmapImage)
    {

        if(bitmapImage!=null) {

            try {
                File file=null;
                FileOutputStream fos = null;
                String APP_DIRECTORY = getString(R.string.nombre_carpeta)+"/";//nombre de directorio
                String MEDIA_DIRECTORY = APP_DIRECTORY + "Imagen";//nombre de la carpeta
                file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
                File mypath = new File(file, id_usuario+ "_perfil.jpg");//nombre del archivo imagen

                boolean isDirectoryCreated = file.exists();//pregunto si esxiste el directorio creado
                if (!isDirectoryCreated)
                    isDirectoryCreated = file.mkdirs();

                if (isDirectoryCreated) {
                    fos = new FileOutputStream(mypath);
                    // Use the compress method on the BitMap object to write image to the OutputStream
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }





}

