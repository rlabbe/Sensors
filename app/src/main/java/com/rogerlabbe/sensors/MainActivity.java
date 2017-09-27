package com.rogerlabbe.sensors;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static SensorManager sensorService;
    private Sensor mAccelerometer;
    private long lastUpdate;
    public EditText editAx;
    public EditText editAy;
    public EditText editAz;
    public EditText editText;
    private FileOutputStream csvFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorService = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();
        mAccelerometer = sensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccelerometer != null) {
            sensorService.registerListener(mySensorEventListener, mAccelerometer,
                    SensorManager.SENSOR_DELAY_UI);
            Log.i("Compass MainActivity", "Registerered for ACCELEROMETER Sensor");
            Toast.makeText(this, "ACCELEROMETER Sensor found",
                    Toast.LENGTH_LONG).show();
        } else {
            Log.e("Compass MainActivity", "Registerered for ACCELEROMETER Sensor");
            Toast.makeText(this, "ACCELEROMETER Sensor not found",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        editText = (EditText) findViewById(R.id.editText);
        editAx = (EditText) findViewById(R.id.editText2);
        editAy = (EditText) findViewById(R.id.editText3);
        editAz = (EditText) findViewById(R.id.editText4);
        editText.setText("nada!");

        File path = this.getFilesDir();
        File file = new File(path, "sensors.csv");


        try {
            csvFile = openFileOutput("sensors.csv", Context.MODE_APPEND);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        };

        try {
            csvFile.write("time, ax, ay,az".getBytes());
            csvFile.flush();
            csvFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the user taps the Send button
     */
    public void sendMessage(View view) {
        Toast.makeText(this, "pushy",
                Toast.LENGTH_LONG).show();
    }


    private SensorEventListener mySensorEventListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];
            long time = event.timestamp;
            editText.setText(String.valueOf(time));
            editAx.setText("x: " + String.valueOf(ax));
            editAy.setText("y: " + String.valueOf(ay));
            editAz.setText("z: " + String.valueOf(az));
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAccelerometer != null) {
            sensorService.unregisterListener(mySensorEventListener);
        }
    }
}
