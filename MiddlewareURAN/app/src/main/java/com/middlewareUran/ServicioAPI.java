package com.middlewareUran;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.middlewareUran.Objects.Cages;
import com.middlewareUran.Objects.ItemReadingLog;
import com.middlewareUran.Objects.Spots;
import com.middlewareUran.Objects.Station;
import com.middlewareUran.database.ConnectionHelperDb;
import com.rscja.deviceapi.RFIDWithUHFA4;
import com.rscja.deviceapi.RFIDWithUHFA8;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.AntennaState;
import com.rscja.deviceapi.entity.UHFTAGInfo;
import com.rscja.deviceapi.enums.AntennaEnum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ServicioAPI extends Service {


    String reader = "Terminado";
    boolean enviadoCorrecto = true;
    //Variabl de powerAntenna
    int powerAntenna;
    int timeAntenna;
    boolean loopFlag = false;
    //Variable de tiempo de espera
    int TIME_WAIT_3_MINUTE_IN_MILISEGUNDOS = 20000;
    int THREE_MINUTE_IN_SECONDS = 20;

    //Variables de audio
    public boolean isBuzzer=true;
    private AudioManager audioManager;
    private float volumenRatio;
    private SoundPool soundPool;
    HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    Constantes constantes = new Constantes();
    public RFIDWithUHFA4 mReaderChainway;
    Boolean activo, comandoInicio,comandoTermino = false, comenzarScan = false;
    UHFTAGInfo uhftagInfo;
    List<String> listaEPC = new ArrayList<>();
    ArrayList<HashMap<String, String>> tagList = new ArrayList<HashMap<String, String>>();
    int tagCount, indiceIndividual;
    //Webhook
    RequestQueue rQueue;
    JSONObject jsonObject;
    ArrayList<ItemReadingLog> itemReadingLogs_array = new ArrayList<>();
    ArrayList<ItemReadingLog> itemReadingLogs_noRepeat = new ArrayList<>();
    ArrayList<String> items_noRepeat = new ArrayList<>();
    ArrayList<ArrayList<ItemReadingLog>> works_array = new ArrayList<>();
    Spots spotSelected = null;
    Cages cageSelected = null;
    String idCageSelected = "";
   String  idSpotSelected = "";
    String idTurno = "";
    String codeTurno = "";
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {///este metodo envia lee los tags rfid
            if(msg.what==1) {
                UHFTAGInfo info = (UHFTAGInfo) msg.obj;
                //   addDataToList(mergeTidEpc(info.getTid(), info.getEPC()), info.getRssi(), info.getAnt());
                // if(enviadoCorrecto){
                //  SendDataToServer(info.getEPC(), String.valueOf(info.getCount()), info.getRssi(), info.getAnt());

                if (info.getEPC() != "") {
                    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    final String time = new SimpleDateFormat("HH:mm").format(new Date());


                        ConnectionHelperDb connection = new ConnectionHelperDb(ServicioAPI.this, "database", null, 1);
                        SQLiteDatabase db = connection.getReadableDatabase();
                        SQLiteDatabase db_wrie = connection.getWritableDatabase();
                        Cursor cursor = null;

                        try {
                            cursor = db.rawQuery("SELECT * FROM tags WHERE epc='"+info.getEPC()+"'", null);
                            Log.e("sql epc","SELECT * FROM tags WHERE epc='"+info.getEPC()+"'");
                            if (cursor.moveToFirst()) {// si existe en la bd
                                MainActivity.showOnListView(info.getEPC(),info.getAnt(),String.valueOf(info.getCount()));
                                SendDataToServer(info.getEPC(), String.valueOf(info.getCount()), info.getRssi(), info.getAnt());
                                playSound();
                            }
                        } catch (Exception e) {
                            Log.e("Database Error", "Error while reading database", e);
                        } finally {
                            if (cursor != null && !cursor.isClosed()) {
                                cursor.close();
                            }
                            db_wrie.close();
                            db.close();
                            connection.close();

                        }



                    //} else {
                    //  Toast.makeText(ServicioAPI.this,"no",Toast.LENGTH_SHORT).show();
                    //}
                    //  playSound();
                    led();
                    //  Toast.makeText(ServicioAPI.this,info.getEPC(),Toast.LENGTH_SHORT).show();
                }
                } else {
                    //setTotalTime();
                    //    Toast.makeText(ServicioAPI.this,"error handle",Toast.LENGTH_SHORT).show();
                }

        }
    };

    public ServicioAPI() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Obtener powerAntenna y timeAntenna de MainActivity
        Bundle extras = intent.getExtras();
        if (extras != null) {
            powerAntenna = extras.getInt("powerAntenna");
            timeAntenna = extras.getInt("timeAntenna");
            //reader = extras.getString("lectora");
            //The key argument here must match that used in the other activity
        }
        idCageSelected   =  getSpotSelected();
     
     //   getTurnoAcual();

        initSound();
        Thread thread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (activo) {
                            String fecha = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault()).format(new Date());
                            Log.d("MiddleWare", "El servicio esta activo : " + fecha);
                           // connectionToServer();


                            comenzarScan = true;
                            comandoInicio = false;
                           Date  dateInicio = new Date();
                                while(comenzarScan){
                                    Date dateAhora = new Date();
                                // Adding 10 mins using Date constructor.
                             //   Log.d("facha1", dateInicio.toString());
                              //  Log.d("facha2", dateAhora.toString());
                               // Log.d("facharesta", String.valueOf(dateAhora.getTime() - dateInicio.getTime()));
                                long diff = dateAhora.getTime() - dateInicio.getTime();
                                int Segundos = (int) (diff/1000);
                                  //  Log.d("egundos", String.valueOf(Segundos));
                                    if(Segundos > THREE_MINUTE_IN_SECONDS){
                                 //   Log.d("segundosPaso10", String.valueOf(Segundos));
                                  //  comenzarScan = false;
                                 //   connectionToServer();

                                       // SendDataToServerLog();
                                    dateInicio = new Date();
                             //       Log.e("nuevafecha",dateInicio.toString());
                                    continue;

                                //    connectionToServer();
                                }

                                if(!comandoInicio) {

                                   List<AntennaState> list=new ArrayList<>();
                                    list.add(new AntennaState(AntennaEnum.ANT1,true));
                                    list.add(new AntennaState(AntennaEnum.ANT2,true));
                                    list.add(new AntennaState(AntennaEnum.ANT3,true));
                                    list.add(new AntennaState(AntennaEnum.ANT4,true));

                                    if (mReaderChainway.setANT(list)) {
                                    } else {
                                    }

                                    if (mReaderChainway.setPower(powerAntenna)) {
                                        Log.d("ServicioAPI", "PowerAntena: " + powerAntenna);

                                    } else {
                                        comandoInicio = false;
                                    }
                                    Log.d("ServicioAPI", "Sin comando de inicio");
                                    if (mReaderChainway.startInventoryTag()) {
                                        comandoInicio = true;
                                        loopFlag = true;
                                  //      Toast.makeText(ServicioAPI.this,"primer hilo",Toast.LENGTH_SHORT).show();
                                        new TagThread().start();
                                    } else {
                                        Log.d("ServicioAPI", "Error startInventory");

                                    }


                                }

                              //  if(!comandoInicio) {

                                //    Log.d("ServicioAPI", "Pasando a comando de inicio");
                                //    if (mReaderChainway.startInventoryTag()) {
                                 //       comandoInicio = true;
                                 //       loopFlag = true;
                                   //     Toast.makeText(ServicioAPI.this,"SEGUNDO hilo",Toast.LENGTH_SHORT).show();

                                //        new TagThread().start();
                               //     } else {
                               //         Log.d("ServicioAPI", "Error startInventory");

                                 //   }
                               /*     uhftagInfo = mReaderChainway.readTagFromBuffer();
                                    Toast.makeText(ServicioAPI.this,"werwwefwee",Toast.LENGTH_SHORT).show();
                                    if (uhftagInfo != null) {
                                     /*   playSound();
                                        led();
                                        if (!listaEPC.contains(uhftagInfo.getEPC())) {
                                            listaEPC.add(uhftagInfo.getEPC());
                                            SendDataToServer(uhftagInfo.getEPC(), String.valueOf(uhftagInfo.getCount()),uhftagInfo.getRssi(), uhftagInfo.getAnt());
                                            Log.d("ServicioAPI", "epc :" + uhftagInfo.getEPC());
                                        }
                                    }else{
                                        //playSound();
                                    }*/
                                //}

                            }
                            try {
                                //Tiempo de espera de 10 segundos
                                Thread.sleep(TIME_WAIT_3_MINUTE_IN_MILISEGUNDOS);
                            } catch (InterruptedException e) {
                               // e.printStackTrace();
                                Log.e("error tiempo",e.toString());
                            }
                        }
                    }
                }
        );
        //Comienza el servicio de middleware
        if(intent.getAction().equals("comenzarMiddleWare")) {
            //Setear comenzar scan a false
            comenzarScan = true;

            //Abriendo dispositivo de lectura chainway
            try {
                mReaderChainway = RFIDWithUHFA4.getInstance();
            } catch (Exception ex) {
                Log.d("ServicioAPI", "Error : " + ex);
                Toast.makeText(ServicioAPI.this,"error"+ex,Toast.LENGTH_LONG).show();
            }
            if (mReaderChainway != null) {
                new InitTask().execute();
            }
            activo = true;
            Log.d("Middleware", "ServicioApi, onStartCommand, comenzar");
            final String CHANNELID = "Foreground Service ID";
            NotificationChannel channel = new NotificationChannel(
                    CHANNELID,
                    CHANNELID,
                    NotificationManager.IMPORTANCE_HIGH
            );

            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                    .setContentText("Middleware Activo")
                    .setContentTitle("Servicio Habilitado")
                    .setSmallIcon(R.drawable.ic_launcher_background);
            thread.start();
            startForeground(1001, notification.build());
            //Se para el servicio de middleware
        } else if(intent.getAction().equals("pararMiddleware")) {
            activo = false;
            comenzarScan = false;
            loopFlag = false;
            Log.d("Middleware", "ServicioApi, onStartCommand, parar startId: " + startId);
            //stopForeground(true);
            stopSelfResult(startId);
            stopSelfResult(1001);
            //Cerrando dispositivo de lectura chainway
            Log.d("ServicioAPI", "Cerrando dispositivo chainway");
            if(mReaderChainway!=null){
                if (mReaderChainway.stopInventory()) {


                    Log.d("ServicioAPI", "stopInventory exitoso");
                } else {
                    Log.d("ServicioAPI", "Error en stopInventory");
                }

               // mReaderChainway.free();
            }
            //Seteando a activo false

        }
        //return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void getTurnoAcual() {

        ConnectionHelperDb connection = new ConnectionHelperDb(ServicioAPI.this, "database", null, 1);
        SQLiteDatabase db = connection.getReadableDatabase();
        Cursor cursorTurno = null;
        cursorTurno = db.rawQuery("SELECT * FROM turnos WHERE estado = 'activo'", null);
        if (cursorTurno.getCount() > 0) {//hay un turno activo
            if (cursorTurno.moveToFirst()) {
                idTurno = String.valueOf(cursorTurno.getInt(0));
                codeTurno = cursorTurno.getString(8);
            }
        }
    }

    private String getSpotSelected() {
        String id ="";
        ConnectionHelperDb connection = new ConnectionHelperDb(ServicioAPI.this, "database", null, 1);
        SQLiteDatabase db = connection.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM cages WHERE status='activo' AND selected='activo'", null);

                if (cursor.moveToFirst()) {//Buscar el activo
                            cageSelected =  new Cages(cursor.getInt(0),cursor.getString(1),cursor.getString(2),
                                    cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7));
                           id = cageSelected.getId();
                }


        } catch (Exception e) {
            Log.e("Database Error", "Error while reading database", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
            connection.close();
        }
        return id;
    }


    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            // Reiniciar servicio de lectora
           // mReaderChainway.free();
            return mReaderChainway.init();
        }
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!result) {
                Toast.makeText(ServicioAPI.this, "Inicio de lector RFID falló", Toast.LENGTH_SHORT).show();
                Log.d("chainway", "Inicio de RFID falló");
            } else {
                Log.d("chainway", "Inicio de RFID éxitoso");
            }
        }
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    //Ping to webserver
    public void connectionToServer() {

        String url = constantes.getUrlApi()+"/reader/"+reader;
        RequestQueue queue = Volley.newRequestQueue(ServicioAPI.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(ServicioAPI.this, response, Toast.LENGTH_SHORT).show();

                        if (response.equals("true")) {
                            Log.d("connectionToServer", "Respuesta exitosa");


                            /* comenzarScan = true;
                            if (BtInventory.getText().equals("Start")) {
                                readTag();
                            }
                            */
                        }else{
                            comenzarScan = false;
                            if(mReaderChainway!=null){
                                if (mReaderChainway.stopInventory()) {
                                    loopFlag = false;
                                    Log.d("ServicioAPI", "stopInventory exitoso");
                                } else {
                                    Log.d("ServicioAPI", "Error en stopInventory");
                                }

                                //mReaderChainway.free();
                            }
                        }
                        queue.stop();

                      //  Toast.makeText(ServicioAPI.this,response,Toast.LENGTH_SHORT).show();
                        //playSound();

                    }
                }, new Response.ErrorListener() {

            public void onErrorResponse(VolleyError error) {
                Log.e("Error respuesta", error.toString());

            }
        });
        queue.add(stringRequest);
    }

    class TagThread extends Thread {
        public void run() {
            UHFTAGInfo uhftagInfo;
            Message msg;
            long time=0;
            while (loopFlag) {
                uhftagInfo = mReaderChainway.readTagFromBuffer();
                if (uhftagInfo != null) {
                    try {
                        msg = handler.obtainMessage();
                        msg.obj = uhftagInfo;
                        msg.what=1;
                        handler.sendMessage(msg);
                        sleep(timeAntenna * 60000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                   // Toast.makeText(ServicioAPI.this,"asd"+uhftagInfo.getEPC(),Toast.LENGTH_SHORT).show();
                 //   Log.d("epc:",uhftagInfo.getEPC());

                }


            }
        }
    }

    private String mergeTidEpc(String tid, String epc) {
        if (!TextUtils.isEmpty(tid) && !tid.equals("0000000000000000") && !tid.equals("000000000000000000000000")) {
            return "TID:" + tid + "\nEPC:" + epc;
        } else {
            return epc;
        }
    }


    public void SendDataToServer(final String epcAndTid, final String valueOf, final String rssi, final String ant) {
        final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        final String time = new SimpleDateFormat("HH:mm").format(new Date());
        enviadoCorrecto = false;
        String url = constantes.getUrlApi()+"/sendData/antena/"+epcAndTid+"/"+valueOf+"/"+rssi+"/"+ant+"/"+reader+"/"+date+"/"+time;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                       // Toast.makeText(ServicioAPI.this,response,Toast.LENGTH_SHORT).show();
                        // insrtRegistryToLog(epcAndTid,valueOf,rssi,ant,"Proceso","enviado",date,time);
                        enviadoCorrecto = true;
                        queue.stop();

                    }
                }, new Response.ErrorListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("errorr",error.toString());
                Toast.makeText(ServicioAPI.this,error.toString(),Toast.LENGTH_SHORT).show();
               //   insrtRegistryToLog(epcAndTid,valueOf,rssi,ant,"Proceso","no enviado",date,time);
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(3000),
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(stringRequest);

    }

    public void SendDataToServerLog(){
        ConnectionHelperDb connection = new ConnectionHelperDb(ServicioAPI.this,"database",null,1);
        SQLiteDatabase db = connection.getReadableDatabase();
        Cursor cursorGetNoEnviado = null;
        cursorGetNoEnviado = db.rawQuery("SELECT  * FROM logs WHERE estado='no enviado' ORDER BY id ASC LIMIT 1000", null) ;

        if(cursorGetNoEnviado.getCount() > 0){// si hya reegistros pendientes
            RequestQueue queue = Volley.newRequestQueue(ServicioAPI.this);

            JSONArray jsonArray = new JSONArray();

            try {
                int idColumnIndex = cursorGetNoEnviado.getColumnIndex("id");
                int epcColumnIndex = cursorGetNoEnviado.getColumnIndex("epc");
                int idStationColumnIndex = cursorGetNoEnviado.getColumnIndex("idStation");
                int dateColumnIndex = cursorGetNoEnviado.getColumnIndex("date");
                int timeColumnIndex = cursorGetNoEnviado.getColumnIndex("time");
                int estadoColumnIndex = cursorGetNoEnviado.getColumnIndex("estado");
                int codeTurnoColumnIndex = cursorGetNoEnviado.getColumnIndex("codeTurno");
                int idTurnoColumnIndex = cursorGetNoEnviado.getColumnIndex("idTurno");

                while (cursorGetNoEnviado.moveToNext()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", cursorGetNoEnviado.getInt(idColumnIndex));
                    jsonObject.put("epc", cursorGetNoEnviado.getString(epcColumnIndex));
                    jsonObject.put("idStation", cursorGetNoEnviado.getString(idStationColumnIndex));
                    jsonObject.put("date", cursorGetNoEnviado.getString(dateColumnIndex));
                    jsonObject.put("time", cursorGetNoEnviado.getString(timeColumnIndex));
                    jsonObject.put("estado", cursorGetNoEnviado.getString(estadoColumnIndex));
                    jsonObject.put("codeTurno", cursorGetNoEnviado.getString(codeTurnoColumnIndex));
                    jsonObject.put("idTurno", cursorGetNoEnviado.getString(idTurnoColumnIndex));

                    jsonArray.put(jsonObject);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }




            Log.e("mJSONArray",jsonArray.toString());

            String url = constantes.urlApi+"/saveLogs/"+idSpotSelected;
            // Request a string response from the provided URL.
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, url,jsonArray,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            queue.stop();
                            //sendImagesToServer(response.optString("response"));
                         registerToEstatusEnviado(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(ServicioAPI.this, error.toString(), Toast.LENGTH_SHORT).show();
                    Log.d("error",error.toString());
                   queue.stop();

                }

            }) ;

            queue.add(jsonArrayRequest);

        }
    }

    public void registerToEstatusEnviado(JSONArray jsonArray)  {
        ConnectionHelperDb connection = new ConnectionHelperDb(ServicioAPI.this,"database",null,1);
        SQLiteDatabase db = connection.getWritableDatabase();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                Log.e("consulta"," UPDATE  logs SET estado = 'enviado'  WHERE id = "+jsonArray.getJSONObject(i).get("id"));


            try{
                db.execSQL("UPDATE  logs SET estado = 'enviado'  WHERE id = "+jsonArray.getJSONObject(i).getInt("id"));
            } catch (Exception e) {
                registryErrorToDataBase(e.getMessage());
            } finally {
                // Cerrar la base de datos
                db.close();
                connection.close();
            }


            } catch (JSONException e) {
                registryErrorToDataBase(e.getMessage());

                throw new RuntimeException(e);
            }
        }
        db.close();
        connection.close();
    }
    public void registryErrorToDataBase(String error){
        final String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        final String time = new SimpleDateFormat("HH:mm").format(new Date());
        ConnectionHelperDb connection = new ConnectionHelperDb(ServicioAPI.this,"database",null,1);
        SQLiteDatabase db = connection.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("error",error);
        values.put("date",date+' '+time);
        db.insert("errors","idRegistry",values);

        db.close();
        connection.close();
    }

    public void insrtRegistryToLog(final String epcAndTid, final String valueOf, final String rssi, final String ant,final String reader,final String estado,final String date,String time){

        ConnectionHelperDb connection = new ConnectionHelperDb(ServicioAPI.this,"database",null,1);
        SQLiteDatabase db = connection.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("epc",epcAndTid);
        values.put("count",valueOf);
        values.put("rssi", rssi);
        values.put("ant", ant);
        values.put("reader", reader);

        values.put("date", date);
        values.put("time", time);
        values.put("estado", estado);
        db.insert("logs","id",values);
    }

    public void EnviarNoEnviados() {


        final ConnectionHelperDb connection1 = new ConnectionHelperDb(ServicioAPI.this, "database", null, 1);

        SQLiteDatabase db = connection1.getReadableDatabase();
        String consulta = "SELECT * FROM logs WHERE estado = 'no enviado' ";
        Cursor cursor = db.rawQuery(consulta, null);
        while (cursor.moveToNext()) {
            final String id = cursor.getString(0);
            final String epcAndTid = cursor.getString(1);
            final String valueOf = cursor.getString(2);
            final String rssi = cursor.getString(3);
            final String ant = cursor.getString(4);
            final  String reader = cursor.getString(5);
            final String date = cursor.getString(6);
            final String time = cursor.getString(7);

            String url = constantes.getUrlApi()+"/sendData/antena/"+epcAndTid+"/"+valueOf+"/"+rssi+"/"+ant+"/"+reader+"/"+date+"/"+time;
            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            SQLiteDatabase db = connection1.getWritableDatabase();
                            String consulta = "UPDATE  logs SET estado = 'enviado'  WHERE id = "+id ;
                            db.rawQuery(consulta, null);
                        }
                    }, new Response.ErrorListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onErrorResponse(VolleyError error) {
                    SQLiteDatabase db = connection1.getWritableDatabase();
                    String consulupdate = "UPDATE logs SET estado = 'no enviado'  WHERE id = "+id ;
                    db.rawQuery(consulupdate, null);

                }
            });

// Add the request to the RequestQueue.
            queue.add(stringRequest);
        }
    }

    public void playSound() {
        if(isBuzzer) {
            mReaderChainway.buzzer();
        }
    }
    public void led(){
        mReaderChainway.led();
    }


    //Iniciar sonido
    private void initSound() {
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
        soundMap.put(2, soundPool.load(this, R.raw.serror, 1));
        audioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
    }






}

