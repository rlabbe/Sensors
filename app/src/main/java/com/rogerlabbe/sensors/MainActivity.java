package com.rogerlabbe.sensors;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static SensorManager sensorService;
    private Sensor mAccelerometer;
    private long lastUpdate;
    public TextView editAx;
    public TextView editAy;
    public TextView editAz;
    public TextView editText;
    private FileOutputStream csvStream;
    private String csvFilePath;
    boolean running = false;
    Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;

        // Make sure we are allowed to write to storage.
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        sensorService = (SensorManager) getSystemService(SENSOR_SERVICE);

        editText = (TextView) findViewById(R.id.editText);
        editAx = (TextView) findViewById(R.id.editText2);
        editAy = (TextView) findViewById(R.id.editText3);
        editAz = (TextView) findViewById(R.id.editText4);
        playButton =  (Button)findViewById(R.id.button);

        playButton.setText("Record");
        editText.setText("time: ");
        editAx.setText("x: ");
        editAy.setText("y: ");
        editAz.setText("z: ");

        // open and close to force new file to be written with header. Pressing button just
        // appends to this file
        OpenFile(false);
        CloseFile();
    }

    public void OpenFile(boolean append) {
        File csvFile = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS), "sensors.csv");

        // save file name so we can scan for changes when file is closed to allow Windows to
        // see the file.
        csvFilePath = csvFile.getAbsolutePath();

        try {
            csvStream = new FileOutputStream(csvFile, append);
            if (!append)
                csvStream.write("time, ax, ay, az\n".getBytes());
        } catch (IOException e) {
            Toast.makeText(this, "exception", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    public void CloseFile() {
        if (csvStream != null) {
            try {
                csvStream.close();
                // If we don't scan for changes Windows never 'sees' the file via USB.
                MediaScannerConnection.scanFile (this, new String[] { csvFilePath }, null, null);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** handle buttons */
    public void sendMessage(View view) {
        switch (view.getId()) {
            case R.id.button:
                startStop();
                break;
            case R.id.button2:
                CloseFile();
                finish();
                break;
        }
    }

    private void startStop() {
        if (!running) {
            lastUpdate = System.currentTimeMillis();
            mAccelerometer = sensorService.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            if (mAccelerometer != null) {
                OpenFile(true);
                sensorService.registerListener(mySensorEventListener, mAccelerometer,
                        SensorManager.SENSOR_DELAY_FASTEST);
                Log.i("Compass MainActivity", "Registerered for ACCELEROMETER Sensor");
                running = true;
                playButton.setText("Recording");
            } else {
                Log.e("Compass MainActivity", "Registerered for ACCELEROMETER Sensor");
                Toast.makeText(this, "ACCELEROMETER Sensor not found", Toast.LENGTH_LONG).show();
            }
        } else {
            running = false;
            sensorService.unregisterListener(mySensorEventListener, mAccelerometer);
            Toast.makeText(this, "Recording Stopped", Toast.LENGTH_LONG).show();
            CloseFile();
            playButton.setText("Record");
        }
    }


    private SensorEventListener mySensorEventListener = new SensorEventListener() {

        int count = 0;
        long firsttime = 0;

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];
            long time = event.timestamp;
            if (firsttime == 0)
                firsttime = time;
            time -= firsttime;
            count += 1;
            if (count >= 10) {
                editText.setText(String.format("Time: %.4f", time / 1.e9));
                editAx.setText(String.format("x   %.4f", ax));
                editAy.setText(String.format("y   %.4f", ay));
                editAz.setText(String.format("z   %.4f", az));
                count = 0;
            }

            try {
                // 9 decimals is lossless for float
                String s = String.format("%d, %.9f, %.9f, %.9f\n", time, ax, ay, az);
                csvStream.write(s.getBytes());
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
