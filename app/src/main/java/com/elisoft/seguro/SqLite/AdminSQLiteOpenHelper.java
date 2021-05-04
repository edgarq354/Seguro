package com.elisoft.seguro.SqLite;

/**
 * Created by elisoft on 07-11-16.
 */import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {

    public AdminSQLiteOpenHelper(Context context, String nombre, CursorFactory factory, int version) {
        super(context, nombre, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table chat(" +
                "id integer default 0," +
                "titulo text," +
                "mensaje text," +
                "fecha text," +
                "hora text," +
                "id_usuario text," +
                "id_conductor text,"+
                "estado integer default 0," +
                "yo integer default 0," +
                "tipo text ," +
                "audio text ," +
                "primary key(id,id_usuario,id_conductor))");

        db.execSQL("create table audio(" +
                "id integer primary key default 0," +
                "titulo text," +
                "mensaje text," +
                "tipo text," +
                "canal text," +
                "audio text," +
                "fecha text," +
                "hora text," +
                "id_usuario text," +
                "id_conductor text,"+
                "id_administrador text,"+
                "estado integer default 0," +
                "yo integer default 0," +
                "visto integer default 0)");


        db.execSQL("create table direccion(" +
                    "id integer," +
                    "detalle text," +
                    "latitud text," +
                    "longitud text," +
                    "id_empresa integer," +
                    "id_usuario integer,"+
                    "nombre text)");

        db.execSQL("create table pedido_usuario(" +
                "id integer, " +
                "id_taxi integer," +
                "fecha_pedido text," +
                "latitud text," +
                "longitud text," +
                "nombre text," +
                "apellido text," +
                "celular text ," +
                "marca text," +
                "placa text," +
                "indicacion text,"+
                "descripcion text,"+
                "estado_pedido integer)");

        db.execSQL("create table pedido_taxi(" +
                "id integer, " +
                "id_usuario integer," +
                "fecha_pedido TIMESTAMP," +
                "latitud text," +
                "longitud text," +
                "nombre text," +
                "apellido text," +
                "celular text ," +
                "indicacion text,"+
                "descripcion text,"+
                "estado_pedido integer," +
                "monto_total text," +
                "clase_vehiculo integer," +
                "calificacion_conductor," +
                "calificacion_vehiculo" +
                ")");


        //cuarga los puntos de recorrido de los pedidos...
        db.execSQL("create table puntos_pedido(" +
                "id_pedido integer," +
                "latitud text," +
                "longitud text, " +
                "fecha timestamp default CURRENT_TIMESTAMP," +
                "primary key(id_pedido,latitud,longitud)" +
                ")");
        db.execSQL("create table notificacion(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT not null," +
                "titulo text," +
                "mensaje text," +
                "cliente text," +
                "id_pedido text," +
                "nombre text,"+
                "latitud text," +
                "longitud text," +
                "tipo text," +
                "fecha text," +
                "hora text," +
                "leido integer default 0," +
                "indicacion text)");

        db.execSQL("create table carrera(" +
                "id integer," +
                "latitud_inicio text," +
                "longitud_inicio text," +
                "latitud_fin text ," +
                "longitud_fin text ," +
                "distancia text," +
                "tiempo text," +
                "fecha_inicio text," +
                "fecha_fin text," +
                "id_pedido integer," +
                "id_usuario integer," +
                "monto text," +
                "ruta text," +
                "direccion_inicio text," +
                "direccion_fin text" +
                ")");
        db.execSQL("create table carrera_casual(" +
                "id integer," +
                "latitud_inicio text," +
                "longitud_inicio text," +
                "latitud_fin text," +
                "longitud_fin text ," +
                "distancia text," +
                "tiempo text," +
                "fecha_inicio text," +
                "fecha_fin text," +
                "id_pedido integer," +
                "id_usuario integer," +
                "monto text," +
                "ruta text," +
                "direccion_inicio text," +
                "direccion_fin text" +
                ")");
        db.execSQL("create table foto(" +
                "id integer not null," +
                "id_fotocopia integer not null," +
                "direccion text," +
                "tipo text," +
                "primary key(id,id_fotocopia))");



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int versionAnte, int versionNue) {

        db.execSQL("drop table if exists chat");
        db.execSQL("create table chat(" +
                "id integer default 0," +
                "titulo text," +
                "mensaje text," +
                "fecha text," +
                "hora text," +
                "id_usuario text," +
                "id_conductor text,"+
                "estado integer default 0," +
                "yo integer default 0," +
                "tipo text ," +
                "audio text ," +
                "primary key(id,id_usuario,id_conductor))");

        db.execSQL("drop table if exists audio");
        db.execSQL("create table audio(" +
                "id integer primary key default 0," +
                "titulo text," +
                "mensaje text," +
                "tipo text," +
                "canal text," +
                "audio text," +
                "fecha text," +
                "hora text," +
                "id_usuario text," +
                "id_conductor text,"+
                "id_administrador text,"+
                "estado integer default 0," +
                "yo integer default 0," +
                "visto integer default 0)");

        db.execSQL("drop table if exists direccion");
        db.execSQL("create table direccion(id integer,detalle text,latitud text,longitud text,id_empresa text,id_usuario text,nombre text)");
        db.execSQL("drop table if exists pedido_usuario");
        db.execSQL("create table pedido_usuario(" +
                "id integer, " +
                "id_taxi integer," +
                "fecha_pedido text," +
                "latitud text," +
                "longitud text," +
                "nombre text," +
                "apellido text," +
                "celular text ," +
                "marca text," +
                "placa text," +
                "indicacion text,"+
                "descripcion text,"+
                "estado_pedido integer)");

        db.execSQL("drop table if exists pedido_taxi");
        db.execSQL("create table pedido_taxi(" +
                "id integer, " +
                "id_usuario integer," +
                "fecha_pedido TIMESTAMP," +
                "latitud text," +
                "longitud text," +
                "nombre text," +
                "apellido text," +
                "celular text ," +
                "indicacion text,"+
                "descripcion text,"+
                "estado_pedido integer," +
                "monto_total text," +
                "clase_vehiculo integer," +
                "calificacion_conductor," +
                "calificacion_vehiculo" +
                ")");

        //cuarga los puntos de recorrido de los pedidos...
        db.execSQL("drop table if exists puntos_pedido");
        db.execSQL("create table puntos_pedido(" +
                "id_pedido integer," +
                "latitud text," +
                "longitud text, " +
                "fecha timestamp default CURRENT_TIMESTAMP," +
                "primary key(id_pedido,latitud,longitud)" +
                ")");
        db.execSQL("drop table if exists notificacion");
        db.execSQL("create table notificacion(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT not null," +
                "titulo text," +
                "mensaje text," +
                "cliente text," +
                "id_pedido text," +
                "nombre text,"+
                "latitud text," +
                "longitud text," +
                "tipo text," +
                "fecha text," +
                "hora text," +
                "leido integer default 0," +
                "indicacion  text)");
        db.execSQL("drop table if exists carrera");
        db.execSQL("create table carrera(" +
                "id integer," +
                "latitud_inicio text," +
                "longitud_inicio text," +
                "latitud_fin text ," +
                "longitud_fin text ," +
                "distancia text," +
                "tiempo text," +
                "fecha_inicio text," +
                "fecha_fin text," +
                "id_pedido integer," +
                "id_usuario integer," +
                "monto text," +
                "ruta text," +
                "direccion_inicio text," +
                "direccion_fin text" +
                ")");
        db.execSQL("drop table if exists carrera_casual");
        db.execSQL("create table carrera_casual(" +
                "id integer," +
                "latitud_inicio text," +
                "longitud_inicio text," +
                "latitud_fin text ," +
                "longitud_fin text ," +
                "distancia text," +
                "tiempo text," +
                "fecha_inicio text," +
                "fecha_fin text," +
                "id_pedido integer," +
                "id_usuario integer," +
                "monto text," +
                "ruta text," +
                "direccion_inicio text," +
                "direccion_fin text" +
                ")");
        db.execSQL("drop table if exists foto");
        db.execSQL("create table foto(" +
                "id integer not null," +
                "id_fotocopia integer not null," +
                "direccion text," +
                "tipo text," +
                "primary key(id,id_fotocopia))");


    }
}

