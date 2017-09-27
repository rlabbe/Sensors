package com.rogerlabbe.sensors;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
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

        int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "sensors.txt");
        try {
            csvFile = new FileOutputStream(file);
            csvFile.write("time, ax, ay,az\n".getBytes());
        } catch (IOException e) {
            Toast.makeText(this, "exception",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        sensorService = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();
        mAccelerometer = sensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccelerometer != null) {
            sensorService.registerListener(mySensorEventListener, mAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
            Log.i("Compass MainActivity", "Registerered for ACCELEROMETER Sensor");
            //Toast.makeText(this, "ACCELEROMETER Sensor found",
             //       Toast.LENGTH_LONG).show();
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


        /*try {
            csvFile = openFileOutput("sensors.csv", Context.MODE_APPEND | Context.MODE_WORLD_WRITEABLE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        };

        try {
            csvFile.write("time, ax, ay,az".getBytes());
            csvFile.flush();
            csvFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * Called when the user taps the Send button
     */
    public void sendMessage(View view) {
        Toast.makeText(this, "pushy",
                Toast.LENGTH_LONG).show();
    }


    private SensorEventListener mySensorEventListener = new SensorEventListener() {

        int count = 0;

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];
            long time = event.timestamp;
            count += 1;
            if (count > 10) {
                editText.setText(String.valueOf(time));
                editAx.setText("x: " + String.valueOf(ax));
                editAy.setText("y: " + String.valueOf(ay));
                editAz.setText("z: " + String.valueOf(az));
                count = 0;
            }

            try {
                String s = String.format("%d %f %f %f\n", time, ax, ay, az);
                csvFile.write(s.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
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
