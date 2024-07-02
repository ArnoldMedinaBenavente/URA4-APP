package com.middlewareUran.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.Nullable;

public class ConnectionHelperDb extends SQLiteOpenHelper {
    final String CREATE_TABLE_LOGS = "CREATE TABLE logs (id INTEGER PRIMARY KEY, epc TEXT,idStation TEXT,date TEXT,time TEXT,estado TEXT,codeTurno TEXT,  idTurno TEXT)";
    final String CREATE_TABLE_TURNOS = "CREATE TABLE turnos (id INTEGER PRIMARY KEY, idSpot TEXT, codeSpot TEXT, nameSpot TEXT,dateStart TEXT,timeStart TEXT," +
            "dateEnd TEXT,timeEnd,codeTurno TEXT,estado TEXT)";
    final String CREATE_TABLE_SPOTS = "CREATE TABLE spots (idRegistry INTEGER PRIMARY KEY, id TEXT,code TEXT," +
            "name TEXT,status TEXT,selected TEXT)";
    final String CREATE_TABLE_STATIONS = "CREATE TABLE stations (idRegistry INTEGER PRIMARY KEY, id TEXT, idSpot TEXT,code TEXT)";
    final String CREATE_TABLE_STATIONS_LOG = "CREATE TABLE stations_log (idRegistry INTEGER PRIMARY KEY, id TEXT, idSpot TEXT,codeSpot TEXT," +
            "nameSpot TEXT,idStation TEXT,codeStation TEXT,idStaff TEXT,codeStaff TEXT,nameStaff TEXT,start_at TEXT,end_at TEXT," +
            "status TEXT)";

    final String CREATE_TABLE_ERRORS = "CREATE TABLE errors (idRegistry INTEGER PRIMARY KEY, error TEXT,date TEXT)";
    final String CREATE_TABLE_TAGS = "CREATE TABLE tags (idRegistry INTEGER PRIMARY KEY, id TEXT,epc TEXT, idStation TEXT)";

    final String CREATE_TABLE_CAGES = "CREATE TABLE cages (idRegistry INTEGER PRIMARY KEY, id TEXT,id_customer TEXT,code TEXT," +
            "manager TEXT,status TEXT,selected TEXT, name_customer TEXT)";

    public ConnectionHelperDb(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LOGS);
        db.execSQL(CREATE_TABLE_TURNOS);
        db.execSQL(CREATE_TABLE_SPOTS);
        db.execSQL(CREATE_TABLE_STATIONS);
        db.execSQL(CREATE_TABLE_STATIONS_LOG);
        db.execSQL(CREATE_TABLE_ERRORS);
        db.execSQL(CREATE_TABLE_TAGS);
        db.execSQL(CREATE_TABLE_CAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
