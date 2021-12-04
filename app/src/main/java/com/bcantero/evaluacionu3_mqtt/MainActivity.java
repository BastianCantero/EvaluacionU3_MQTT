package com.bcantero.evaluacionu3_mqtt;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView lbl_sensorName_value, lbl_sensorValue_value, lbl_sensorType_value, lbl_sensorLocation_value, lbl_sensorDate_value, lbl_sensorObservation_value;

    private Button btnSave, btnDiscard;

    private static final String serverUri = "tcp://test.mosquitto.org:1883";
    private static final String userName = "bastian";
    private static final String password = "1235";
    private static final String appName = "app1";
    private static final String clientId = "marcelo";

    private static final String TAG = "MQTT Client 01";

    private MqttAndroidClient mqttAndroidClient;

    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;

    private ListView listView;
    private List<String> stringList;
    private ArrayAdapter<String> arrayAdapter;

    private FirebaseFunctions mFunctions;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference dbReference;
    private EditText etNomUsurio, etEmail, etContrasena;

    private ListView sensor_listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        builder =  new AlertDialog.Builder(MainActivity.this);
        //builder.setTitle("¿Eliminar registro?");

        lbl_sensorName_value = (TextView) findViewById(R.id.lbl_sensorName_value);
        lbl_sensorValue_value = (TextView) findViewById(R.id.lbl_sensorValue_value);
        lbl_sensorType_value = (TextView) findViewById(R.id.lbl_sensorType_value);
        lbl_sensorLocation_value = (TextView) findViewById(R.id.lbl_sensorLocation_value);
        lbl_sensorDate_value = (TextView) findViewById(R.id.lbl_sensorDate_value);
        lbl_sensorObservation_value = (TextView) findViewById(R.id.lbl_sensorObservation_value);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);

        btnDiscard = (Button) findViewById(R.id.btnDiscard);
        btnDiscard.setOnClickListener(this);

        initFirebaseDB();

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    Log.d(TAG, "Reconectado a : " + serverURI);
                } else {
                    Log.d(TAG, "Conectado a: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d(TAG, "Se ha perdido la conexón!.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(TAG, "Mensaje recibido:" + message.toString());

                JSONObject jsonObject = new JSONObject(message.toString());

                lbl_sensorName_value.setText(jsonObject.getString("nombre_sensor"));
                lbl_sensorType_value.setText(jsonObject.getString("tipo"));
                lbl_sensorValue_value.setText(jsonObject.getString("valor"));
                lbl_sensorLocation_value.setText(jsonObject.getString("ubicacion"));
                lbl_sensorDate_value.setText(jsonObject.getString("fecha-hora"));
                lbl_sensorObservation_value.setText(jsonObject.getString("observ"));

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(userName);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "Conectado a: " + serverUri);

                    try {
                        asyncActionToken.getSessionPresent();
                        Log.d(TAG, "Topicos: " + asyncActionToken.getTopics().toString());
                    } catch (Exception e) {
                        String message = e.getMessage();
                        Log.d(TAG, "Error el mensaje es nulo! " + String.valueOf(message == null));
                    }

                    Toast.makeText(MainActivity.this, "Conectado a mosquitto!", Toast.LENGTH_SHORT).show();

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);

                    try {
                        mqttAndroidClient.subscribe("sensor/sensores", 2);
                        Toast.makeText(MainActivity.this, "Subscrito a test01", Toast.LENGTH_SHORT).show();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    Log.d(TAG, "Falla al conectar a: " + serverUri);

                }
            });
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSave:

                saveSensorModel();

                break;

            case R.id.btnDiscard:

                discard();

                break;
        }

    }

    private void saveSensorModel(){


        if (!(lbl_sensorName_value.getText().equals("") || lbl_sensorValue_value.getText().equals("") || lbl_sensorType_value.getText().equals("") || lbl_sensorLocation_value.getText().equals("") || lbl_sensorDate_value.getText().equals("") || lbl_sensorObservation_value.getText().equals(""))) {

            builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    try {

                        SensorModel sensorModel =  new SensorModel(
                                UUID.randomUUID().toString(),
                                lbl_sensorName_value.getText().toString(),
                                lbl_sensorType_value.getText().toString(),
                                lbl_sensorValue_value.getText().toString(),
                                lbl_sensorLocation_value.getText().toString(),
                                lbl_sensorDate_value.getText().toString(),
                                lbl_sensorObservation_value.getText().toString()

                        );

                        dbReference.child("sensor").child(sensorModel.getId_Sensor()).setValue(sensorModel);
                        cleanLbl();
                        Toast.makeText(getApplicationContext(), "Registro guardo con exito", Toast.LENGTH_LONG).show();

                    }catch (Exception e){

                        Toast.makeText(getApplicationContext(), "Error al guardar el registro", Toast.LENGTH_LONG).show();

                    }

                }
            });

            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //alertDialog.closeOptionsMenu();
                }
            });

            builder.setTitle("¿Guardar registro?");
            alertDialog = builder.create();
            alertDialog.show();


        }else{

            Toast.makeText(getApplicationContext(), "No hay registros", Toast.LENGTH_LONG).show();

        }


    }

    private void discard(){
        if (!(lbl_sensorName_value.getText().equals("") || lbl_sensorValue_value.getText().equals("") || lbl_sensorType_value.getText().equals("") || lbl_sensorLocation_value.getText().equals("") || lbl_sensorDate_value.getText().equals("") || lbl_sensorObservation_value.getText().equals(""))) {

            builder.setPositiveButton("Descartar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    cleanLbl();

                }
            });

            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //alertDialog.closeOptionsMenu();
                }
            });

            builder.setTitle("¿Descartar registro?");
            alertDialog = builder.create();
            alertDialog.show();


        }else{

            Toast.makeText(getApplicationContext(), "No hay registros para descartar", Toast.LENGTH_LONG).show();

        }
    }

    private void cleanLbl(){
        lbl_sensorName_value.setText("");
        lbl_sensorValue_value.setText("");
        lbl_sensorType_value.setText("");
        lbl_sensorLocation_value.setText("");
        lbl_sensorDate_value.setText("");
        lbl_sensorObservation_value.setText("");
    }

    private void initFirebaseDB() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        dbReference = firebaseDatabase.getReference();
    }

}