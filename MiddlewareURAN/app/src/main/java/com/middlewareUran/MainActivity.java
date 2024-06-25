package com.middlewareUran;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.middlewareUran.Objects.Spots;
import com.middlewareUran.Objects.Station;
import com.middlewareUran.Objects.StationLog;
import com.middlewareUran.Objects.Tags;
import com.middlewareUran.Objects.TagsAdapter;
import com.middlewareUran.database.ConnectionHelperDb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Button botonComenzar,button_options;
    TextView txtStatus;
    TextView txt_selected_galera;
    ListView listView_tags;
    Spinner spinnerPowerAntenna,spinner_area;
    Constantes constantes = new Constantes();
    Context context = MainActivity.this;
    ArrayList<String> spotsList = new ArrayList<>();
    Spots spotSelected = null;
    ArrayList<Spots> arrayListSpots = new ArrayList<>();
    static ArrayList<Tags> arrayListTags = new ArrayList<>();
    static ArrayList<String> arrayepc = new ArrayList<>();
    static ArrayList<Station> arrayListStations = new ArrayList<>();

static TagsAdapter tagsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Setear variable spinner de power antenna
        spinnerPowerAntenna = (Spinner)findViewById(R.id.spinnerPowerAntenna);

        //Variable de puerto para servidor TCP.
        botonComenzar = (Button) findViewById(R.id.botonComenzar);
        button_options = (Button) findViewById(R.id.button_options);

        txtStatus = (TextView) findViewById(R.id.txtStatus);
        listView_tags = (ListView) findViewById(R.id.listview_tags);
        tagsAdapter = new TagsAdapter(context,arrayListTags);
        listView_tags.setAdapter(tagsAdapter);



        txt_selected_galera = (TextView) findViewById(R.id.textView_selected_galera);


        //Buscar dispositivo Chainway
        if(Build.MANUFACTURER.equals("CHAINWAY")) {
            Log.d("ServicioAPI", "Manufacturer : " + Build.MANUFACTURER);
            Log.d("ServicioAPI", "Model : " + Build.MODEL);
            txtStatus.setTextColor(Color.parseColor("#00e40a"));
            txtStatus.setText("Dispositivo encontrado : " + Build.MODEL);
        } else {
            txtStatus.setTextColor(Color.RED);
            txtStatus.setText("El dispositivo encontrado no es compatible.");
            botonComenzar.setEnabled(false);
        }
        if(!foregroundServiceRunning()) {
            Log.d("MiddleWare", "El servicio no esta corriendo.");

        } else {
            Log.d("MiddleWare", "El servicio esta corriendo.");
            botonComenzar.setText("PARAR SERVICIO");
        }
        button_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDialogOptions();
            }
        });
        botonComenzar.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (spotSelected != null ) {

                    if (botonComenzar.getText().equals("COMENZAR SERVICIO")) {
                        //Seteando puerto para servidor TCP
                        //Comenzando Middleware
                        Toast.makeText(MainActivity.this, "Comenzando servicio", Toast.LENGTH_SHORT).show();
                        botonComenzar.setText("PARAR SERVICIO");
                        Intent comienzaIntent = new Intent(MainActivity.this, ServicioAPI.class);
                        comienzaIntent.putExtra("powerAntenna", Integer.parseInt(spinnerPowerAntenna.getSelectedItem().toString()));
                        comienzaIntent.setAction("comenzarMiddleWare");
                        startForegroundService(comienzaIntent);
                    } else {
                        //Parando middleware
                        Toast.makeText(MainActivity.this, "Parando servicio", Toast.LENGTH_SHORT).show();
                        botonComenzar.setText("COMENZAR SERVICIO");
                        Intent pararIntent = new Intent(MainActivity.this, ServicioAPI.class);
                        pararIntent.setAction("pararMiddleware");
                        startForegroundService(pararIntent);
                    }
                }else{
                    validationInfo();

                }
            }
        });




        validationInfo();
         //updateInfoDataBase();
      //  initRFID_ServiceApi();
    }
    public void downloadInfoDataBase(){
        String url = constantes.getUrlApi()+"/updateInfoDataBase";
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        JSONObject jsonBody = new JSONObject();
        JsonObjectRequest jsObjectRequest = new JsonObjectRequest(Request.Method.GET, url,jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse( JSONObject response) {
                        updateDataBase(response);
                        queue.stop();
                    }//onResponse

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                queue.stop();
                registryErrorToDataBase(error.toString());
                Toast.makeText(MainActivity.this,"error", Toast.LENGTH_LONG).show();
            }
        }){
        };

        queue.add(jsObjectRequest);
    }

    public void updateDataBase(JSONObject info){
        try {
            JSONArray jsonArray_spots = info.getJSONArray("spots");
            JSONArray jsonArray_stations = info.getJSONArray("stations");
            JSONArray jsonArray_stationsLog = info.getJSONArray("stationsLog");
            JSONArray jsonArray_tags = info.getJSONArray("tags");

            updateSpotsToDataBase(jsonArray_spots);
            updateStationsToDataBase(jsonArray_stations);
            updateStationsLogToDataBase(jsonArray_stationsLog);
            updateTagsToDataBase(jsonArray_tags);


        } catch (JSONException e) {
            registryErrorToDataBase(String.valueOf(e));
            throw new RuntimeException(e);
        }


    }
    public void  initRFID_ServiceApi(){
        if(spotSelected != null) {
            Toast.makeText(MainActivity.this, "Comenzando servicio", Toast.LENGTH_SHORT).show();
            botonComenzar.setText("PARAR SERVICIO");
            Intent comienzaIntent = new Intent(MainActivity.this, ServicioAPI.class);
            comienzaIntent.putExtra("powerAntenna", Integer.parseInt(spinnerPowerAntenna.getSelectedItem().toString()));
            comienzaIntent.setAction("comenzarMiddleWare");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(comienzaIntent);
            }
        }else{
            Toast.makeText(context,"error selecciona una Galera",Toast.LENGTH_SHORT).show();
        }
    }




    public boolean foregroundServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(ServicioAPI.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void ShowDialogOptions(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.txt_options)
                .setItems(R.array.opciones_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                        if(which == 0){//descargar informacion
                            downloadPositions();
                        }

                        if(which == 1){//descargar informacion
                            downloadInfoDataBase();
                        }

                        if(which == 2){//descargar informacion
                            showAlertSelectedSpot();
                        }

                        if(which == 3){//descargar informacion
                            downloadInfoDataBase();

                        }




                    }
                });
        builder.show();
    }

    private void downloadPositions() {
        String url = constantes.getUrlApi()+"/updateInfoDataBase";
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        JSONObject jsonBody = new JSONObject();
        JsonObjectRequest jsObjectRequest = new JsonObjectRequest(Request.Method.GET, url,jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse( JSONObject response) {
                        try {
                            updateStationsToDataBase(response.getJSONArray("stations"));
                            updateStationsLogToDataBase(response.getJSONArray("stationsLog"));
                        } catch (JSONException e) {
                            registryErrorToDataBase(e.getMessage());
                            throw new RuntimeException(e);
                        }

                        queue.stop();
                    }//onResponse

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                queue.stop();
                registryErrorToDataBase(error.toString());
                Toast.makeText(MainActivity.this,"error", Toast.LENGTH_LONG).show();
            }
        }){
        };

        queue.add(jsObjectRequest);
    }




    public void registryErrorToDataBase(String error){
        final String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        final String time = new SimpleDateFormat("HH:mm").format(new Date());
        ConnectionHelperDb connection = new ConnectionHelperDb(MainActivity.this,"database",null,1);
        SQLiteDatabase db = connection.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("error",error);
        values.put("date",date+' '+time);
        db.insert("errors","idRegistry",values);

        db.close();
        connection.close();
    }

    public void updateSpotsToDataBase(JSONArray spots){
        ConnectionHelperDb connection = new ConnectionHelperDb(MainActivity.this,"database",null,1);
        SQLiteDatabase db = connection.getWritableDatabase();
        try {
            String SQL_UPDATE_SPOTS = "UPDATE spots SET status = 'inactivo', selected = 'inactivo'";
            db.execSQL(SQL_UPDATE_SPOTS);
            spotSelected = null;
            txt_selected_galera.setText("");


            for (int i = 0; i < spots.length(); i++) {
                String id  =  spots.getJSONObject(i).getString("id");
                String code  =  spots.getJSONObject(i).getString("code");
                String name  =  spots.getJSONObject(i).getString("name");
                ContentValues values = new ContentValues();
                values.put("id", id);
                values.put("code", code);
                values.put("name", name);
                values.put("status", "activo");
                values.put("selected", "inactivo");
                // Insertar la nueva fila
                db.insert("spots", null, values);
            }


        } catch (Exception e) {
            registryErrorToDataBase(e.getMessage());
        } finally {
            // Cerrar la base de datos
            db.close();
            connection.close();
        }
        validationInfo();
    }

    public void updateStationsToDataBase(JSONArray stations){
        ConnectionHelperDb connection = new ConnectionHelperDb(MainActivity.this,"database",null,1);
        SQLiteDatabase db = connection.getWritableDatabase();
        try {
            String SQL_UPDATE_STATIONS = "DELETE FROM stations" ;
            db.execSQL(SQL_UPDATE_STATIONS);

            for (int i = 0; i < stations.length(); i++) {
                String id  =  stations.getJSONObject(i).getString("id");
                String idSpot  =  stations.getJSONObject(i).getString("idSpot");
                String code  =  stations.getJSONObject(i).getString("code");

                ContentValues values = new ContentValues();
                values.put("id", id);
                values.put("idSpot", idSpot);
                values.put("code", code);
                // Insertar la nueva fila
                db.insert("stations", null, values);
            }

        } catch (Exception e) {
            registryErrorToDataBase(e.getMessage());
        } finally {
            // Cerrar la base de datos
            db.close();
            connection.close();
        }
    }

    public void updateStationsLogToDataBase(JSONArray stationsLog){
        ConnectionHelperDb connection = new ConnectionHelperDb(MainActivity.this,"database",null,1);
        SQLiteDatabase db = connection.getWritableDatabase();

        try {
            String SQL_UPDATE_STATIONS_LOG = "UPDATE stations_log SET status = 'inactivo' WHERE status = 'activo'";
            db.execSQL(SQL_UPDATE_STATIONS_LOG);

            for (int i = 0; i < stationsLog.length(); i++) {
                String id  =  stationsLog.getJSONObject(i).getString("id");
                String idSpot  =  stationsLog.getJSONObject(i).getString("idSpot");
                String codeSpot  =  stationsLog.getJSONObject(i).getString("codeSpot");
                String nameSpot  =  stationsLog.getJSONObject(i).getString("nameSpot");
                String idStation  =  stationsLog.getJSONObject(i).getString("idStation");
                String codeStation  =  stationsLog.getJSONObject(i).getString("codeStation");
                String idStaff  =  stationsLog.getJSONObject(i).getString("idStaff");
                String codeStaff  =  stationsLog.getJSONObject(i).getString("codeStaff");
                String nameStaff  =  stationsLog.getJSONObject(i).getString("nameStaff");
                String start_at  =  stationsLog.getJSONObject(i).getString("start_at");



                ContentValues values = new ContentValues();
                values.put("id", id);
                values.put("idSpot", idSpot);
                values.put("codeSpot", codeSpot);
                values.put("nameSpot", nameSpot);
                values.put("idStation", idStation);
                values.put("codeStation", codeStation);
                values.put("idStaff", idStaff);
                values.put("codeStaff", codeStaff);
                values.put("nameStaff", nameStaff);
                values.put("start_at", start_at);
                values.put("status", "activo");
                // Insertar la nueva fila
                db.insert("stations_log", null, values);
            }
            if(spotSelected != null) {
                showStations(spotSelected);
            }
        } catch (Exception e) {
            registryErrorToDataBase(e.getMessage());
        } finally {
            // Cerrar la base de datos
            db.close();
            connection.close();
        }
    }

    public void updateTagsToDataBase(JSONArray tags){
        ConnectionHelperDb connection = new ConnectionHelperDb(MainActivity.this,"database",null,1);
    SQLiteDatabase db = connection.getWritableDatabase();

        try {
        String SQL_DELETE_TAGS = "DELETE FROM tags";
        db.execSQL(SQL_DELETE_TAGS);

        for (int i = 0; i < tags.length(); i++) {
            String id  =  tags.getJSONObject(i).getString("id");
            String epc  =  tags.getJSONObject(i).getString("epc");
            String idStation  =  tags.getJSONObject(i).getString("idStation");

            ContentValues values = new ContentValues();
            values.put("id", id);
            values.put("epc", epc);
            values.put("idStation", idStation);

            // Insertar la nueva fila
            db.insert("tags", null, values);
        }

    } catch (Exception e) {
        registryErrorToDataBase(e.getMessage());
    } finally {
        // Cerrar la base de datos
        db.close();
        connection.close();
    }
}

    public void showAlertSelectedSpot(){
        spotsList.clear();
        ConnectionHelperDb connection = new ConnectionHelperDb(MainActivity.this, "database", null, 1);
        SQLiteDatabase db = connection.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM spots WHERE status='activo'", null);
            arrayListSpots.clear();
            spotsList.clear();
            if (cursor.moveToFirst()) {
                do {
                    spotsList.add(cursor.getString(3)); // Asumiendo que la primera columna es el nombre
                    Spots spot  = new Spots(cursor.getInt(0),cursor.getString(1),cursor.getString(2),
                                                cursor.getString(3),cursor.getString(4),cursor.getString(5));
                    arrayListSpots.add(spot);
                } while (cursor.moveToNext());
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Elige una galera");

            // Convertir ArrayList en un array para usar en AlertDialog
                        final String[] spotsArray = spotsList.toArray(new String[0]);

                        builder.setItems(spotsArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'which' es el índice del ítem seleccionado
                                Toast.makeText(MainActivity.this, "Seleccionaste: " + spotsArray[which], Toast.LENGTH_SHORT).show();
                                registrySpotSlected(arrayListSpots.get(which));
                            }
                        });

            // Crear y mostrar el AlertDialog
                        AlertDialog dialog = builder.create();
                        dialog.show();



        } catch (Exception e) {
            Log.e("Database Error", "Error while reading database", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
            connection.close();
        }
    }


    public void validationInfo(){
        //validamois si hay una galera
        ConnectionHelperDb connection = new ConnectionHelperDb(MainActivity.this, "database", null, 1);
        SQLiteDatabase db = connection.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM spots WHERE status='activo'", null);

            if(cursor.getCount() > 0 ){//hay spots activos

                if (cursor.moveToFirst()) {//Buscar el activo
                    do {
                        if(cursor.getString(5).equals("activo")) {
                             spotSelected =  new Spots(cursor.getInt(0),cursor.getString(1),cursor.getString(2),
                                                            cursor.getString(3),cursor.getString(4),cursor.getString(5));
                           // txt_selected_galera.setText(spotSelected.getName());
                            showStations(spotSelected);
                        }
                    } while (cursor.moveToNext());
                }
                if (spotSelected == null) {
                    showAlertSelectedSpot();
                }else{//si ya tiene un spot seleccionado entonces validamos que alla un turno iniciado
                   Cursor cursorTurno = null;
                   cursorTurno = db.rawQuery("SELECT * FROM turnos WHERE estado = 'activo'",null);
                    if(cursorTurno.getCount() > 0 ) {//hay un turno activo
                        if (cursorTurno.moveToFirst()) {
                                  updateCountTagsToStations();
                        }
                    }else{
                        showAlertInitTurno();
                    }
                }
            }else{//descargar la informacion
                downloadInfoDataBase();
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
    }

    private void updateCountTagsToStations() {
        ConnectionHelperDb connection = new ConnectionHelperDb(MainActivity.this, "database", null, 1);
        SQLiteDatabase db = connection.getReadableDatabase();
        for (int i = 0; i < arrayListStations.size(); i++) {



        }
    }

    public void showAlertInitTurno(){
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Iniciar turno"); // Título del diálogo
    builder.setMessage("¿Estás seguro de que quieres iniciar un turno?"); // Mensaje del diálogo

// Botón positivo
    builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Acción para el botón positivo
            Toast.makeText(getApplicationContext(), "Iniciando turno", Toast.LENGTH_LONG).show();
            initTurno();
        }
    });

// Botón negativo
    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Acción para el botón negativo
            dialog.dismiss();
           // Toast.makeText(getApplicationContext(), "Has cancelado la acción", Toast.LENGTH_SHORT).show();
        }
    });

// Mostrar el diálogo
    AlertDialog dialog = builder.create();
    dialog.show();
}

public void showAlertEndTurno(){
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Terminat turno"); // Título del diálogo
    builder.setMessage("¿Estás seguro de que quieres terminar el turno?"); // Mensaje del diálogo

// Botón positivo
    builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Acción para el botón positivo
            Toast.makeText(getApplicationContext(), "Terminando turno", Toast.LENGTH_LONG).show();
        }
    });

// Botón negativo
    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Acción para el botón negativo
            dialog.dismiss();
            // Toast.makeText(getApplicationContext(), "Has cancelado la acción", Toast.LENGTH_SHORT).show();
        }
    });

// Mostrar el diálogo
    AlertDialog dialog = builder.create();
    dialog.show();
}

public void initTurno(){
    final String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
    final String time = new SimpleDateFormat("HH:mm").format(new Date());
    ConnectionHelperDb connection = new ConnectionHelperDb(MainActivity.this, "database", null, 1);
    SQLiteDatabase db = connection.getWritableDatabase();
    String codeTurno = "1";
    Cursor cursor = db.rawQuery("SELECT * FROM turnos WHERE dateStart ='"+date+"' ORDER BY id DESC LIMIT 1",null);
    if (cursor.moveToFirst()){
        int newCode = Integer.parseInt(cursor.getString(8)) + 1;
        codeTurno = String.valueOf(newCode);
    }


    ContentValues values = new ContentValues();

    values.put("idSpot", spotSelected.getId());
    values.put("codeSpot", spotSelected.getCode());
    values.put("nameSpot", spotSelected.getName());
    values.put("dateStart", date);
    values.put("timeStart", time);
    values.put("dateEnd", "");
    values.put("timeEnd", "");
    values.put("codeTurno", codeTurno);
    values.put("estado", "activo");
    // Insertar el nuevo turno
    db.insert("turnos", null, values);
    validationInfo();
}




    public void registrySpotSlected(Spots spot){
        ConnectionHelperDb connection = new ConnectionHelperDb(MainActivity.this, "database", null, 1);
        SQLiteDatabase db = connection.getReadableDatabase();
        Cursor cursor = null;

        try {
            String SQL_UPDATE_SPOTS = "UPDATE spots SET selected = 'inactivo'";
            db.execSQL(SQL_UPDATE_SPOTS);
            txt_selected_galera.setText("");

            String SQL_UPDATE_SPOTS_SELECTED = "UPDATE spots  SET selected = 'activo' WHERE idRegistry = "+spot.getIdRegistry()+" ";
            db.execSQL(SQL_UPDATE_SPOTS_SELECTED);

        } catch (Exception e) {
            registryErrorToDataBase(e.getMessage());
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            db.close();
            connection.close();
            validationInfo();
        }
    }

    public void showStations(Spots spot){
        ConnectionHelperDb connection = new ConnectionHelperDb(MainActivity.this, "database", null, 1);
        SQLiteDatabase db = connection.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM stations WHERE idSpot='"+spot.getId()+"' ", null);
            arrayListStations.clear();


        } catch (Exception e) {
            Log.e("Database Error", "Error while reading database", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
           // db.close();
            //connection.close();
            Toast.makeText(context,"listo",Toast.LENGTH_SHORT).show();
        }
    }
    static boolean update = false;
    public static void showOnListView(String epc,String antena,String count){
        update = false;
        int index = 0;
        for (int i = 0; i < arrayListTags.size(); i++) {
            if(epc.equals(arrayListTags.get(i).getEpc())){
                update = true;
                index = i;
            }
        }

        if (!update){//se inserta en la

                String chofer = "";
                String placa = "";

            switch (epc) {
                case "E2806995000050038065B264":
                    chofer = "Juan Pérez";
                    placa = "ABC-1234";
                    break;
                case "E28069950000500380657264":
                    chofer = "María López";
                    placa = "XYZ-5678";
                    break;
                case "E28069950000400380657A64":
                    chofer = "Carlos Sánchez";
                    placa = "DEF-2345";
                    break;
                case "E28069950000500380653664":
                    chofer = "Ana Ramírez";
                    placa = "LMN-6789";
                    break;
                case "E28069950000400380653E64":
                    chofer = "Luis Hernández";
                    placa = "GHI-3456";
                    break;
                case "E28069950000400380657664":
                    chofer = "José Torres";
                    placa = "PQR-7890";
                    break;
                case "E2806995000050038065FA64":
                    chofer = "Laura García";
                    placa = "JKL-4567";
                    break;
                case "E28069950000500380657E64":
                    chofer = "Pedro Martínez";
                    placa = "STU-8901";
                    break;
                case "E28069950000400380653264":
                    chofer = "Carmen Gutiérrez";
                    placa = "MNO-5678";
                    break;
                case "E28069950000500380653A64":
                    chofer = "Francisco Díaz";
                    placa = "VWX-1234";
                    break;
                case "E28069950000400311B359BB":
                    chofer = "Silvia Fernández";
                    placa = "RST-6789";
                    break;
                default:
                    chofer = "No registrado";
                    placa = "No registrado";
            }

            Tags tag = new Tags(epc,antena,count,chofer,placa);
            arrayListTags.add(tag);


        }

        if (update){// se actualiza
            Tags tag = arrayListTags.get(index);
            tag.setAntena(antena);
            tag.setConut(count);


        }

        tagsAdapter.notifyDataSetChanged();
    }
    public static void funcionupdateButton(String idStation,String epc){

        for (int i = 0; i < arrayListStations.size() ; i++) {
            if(arrayListStations.get(i).getId().equals(idStation)){
             Button btn = arrayListStations.get(i).getButton();
                btn.setText("");
                String txt_btn = arrayListStations.get(i).getCode();
                if(arrayListStations.get(i).getStationLog() != null){
                    txt_btn = txt_btn+"\n"+arrayListStations.get(i).getStationLog().getNameStaff();

                }

                arrayListStations.get(i).getArrayTags().add(epc);
                txt_btn = txt_btn+"\n"+"tags: "+arrayListStations.get(i).getArrayTags().size();

                btn.setText(txt_btn);

            }
        }

    }

}