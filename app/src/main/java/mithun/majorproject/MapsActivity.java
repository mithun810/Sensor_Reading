package mithun.majorproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;

import static java.lang.Math.sqrt;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap;
    GPSTracker gps,gps1;
    DatabaseReference rootRef;
    DatabaseReference ref;
    DatabaseReference cref;
    double latitude=-35;
    double longitude=118;
    private Sensor mySensor,mySensor2 , mySensor3;
    private SensorManager SM;
    private Circle circle;
    String user="Ranganath";
    //private Firebase mRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        rootRef = FirebaseDatabase.getInstance().getReference();
        ref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://majorproject-1cebe.firebaseio.com/");
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);
        //SM1 = (SensorManager)getSystemService(SENSOR_SERVICE);

        mySensor2 = SM.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mySensor =  SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mySensor3 = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //  SM.registerListener(this , mySensor, SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(this , mySensor2, SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(this, mySensor,SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(this , mySensor3, SensorManager.SENSOR_DELAY_NORMAL);
    }

/**Sensor*/
@Override
public void onSensorChanged(SensorEvent sensorEvent) {


    Sensor sens = sensorEvent.sensor;
    double effacce =  0.0;
    if(sens.getType() == Sensor.TYPE_ACCELEROMETER) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];



        if (x > y && x > z) {
            effacce= (float) sqrt(y * y + z * z);

        } else if (y > x && y > z) {
            effacce= (float) sqrt(x * x + z * z);
        } else {
            effacce= (float) sqrt(x * x + y * y);
        }

    }

    if(sens.getType() == Sensor.TYPE_MAGNETIC_FIELD){

        float magx =sensorEvent.values[0];
        float magy = sensorEvent.values[1];
        //float z = sensorEvent.values[2];
        cref=ref.child(user).child("Magnetic-X");
        cref.setValue(magx);
        cref=ref.child(user).child("Magnetic-Y");
        cref.setValue(magy);
    }


    if(sens.getType() == Sensor.TYPE_GYROSCOPE){
        float gyrx = sensorEvent.values[0];
        float gyry = sensorEvent.values[1];
        float gyrz =sensorEvent.values[2];
        cref=ref.child(user).child("Gyroscope-X");
        cref.setValue(gyrx);
        cref=ref.child(user).child("Gyroscope-Y");
        cref.setValue(gyry);
        cref=ref.child(user).child("Gyroscope-Z");
        cref.setValue(gyrz);

    }

    gps1 = new GPSTracker(MapsActivity.this);
    if(gps1.canGetLocation()) {

        latitude = gps1.getLatitude();
        longitude = gps1.getLongitude();

    } else {
        // Can't get location.
        // GPS or network is not enabled.
        // Ask user to enable GPS/network in settings.
        gps1.showSettingsAlert();
    }
    cref=ref.child(user).child("location");
    cref.setValue(latitude+","+longitude);
    cref=ref.child(user).child("Acceleration");
    cref.setValue(effacce);
}

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        gps = new GPSTracker(MapsActivity.this);


        //mRef = new Firebase("https://majorproject-1cebe.firebaseio.com/");
        if(gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            gps.showSettingsAlert();
        }
        circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(latitude, longitude))
                .radius(1000)
                .strokeWidth(10)
                .strokeColor(Color.GREEN)
                .fillColor(Color.argb(128, 255, 0, 0))
                .clickable(true));

        googleMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {

            @Override
            public void onCircleClick(Circle circle) {
                // Flip the r, g and b components of the circle's
                // stroke color.
                int strokeColor = circle.getStrokeColor() ^ 0x00ffffff;
                circle.setStrokeColor(strokeColor);
            }
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(latitude, longitude);
        //Firebase mRefChild =mRef.child("GPS");
        //mRefChild.setValue(latitude+","+longitude);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Mithun's here !!"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
/*    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


        Sensor sens = sensorEvent.sensor;

        if(sens.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            float acceleration = (float) 0.0;


            if (x > y && x > z) {
                acceleration= (float) sqrt(y * y + z * z);

            } else if (y > x && y > z) {
                acceleration= (float) sqrt(x * x + z * z);
            } else {
                acceleration= (float) sqrt(x * x + y * y);
            }

        }

        if(sens.getType() == Sensor.TYPE_MAGNETIC_FIELD){

            float magx =sensorEvent.values[0];
            float magy = sensorEvent.values[1];
            //float z = sensorEvent.values[2];
        }


        if(sens.getType() == Sensor.TYPE_GYROSCOPE){
            float gyrx = sensorEvent.values[0];
            float gyry = sensorEvent.values[1];
            float gyrz =sensorEvent.values[2];

        }

        gps1 = new GPSTracker(MapsActivity.this);
        if(gps1.canGetLocation()) {

            latitude = gps1.getLatitude();
            longitude = gps1.getLongitude();

        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            gps1.showSettingsAlert();
        }
        cref=ref.child(latitude+","+longitude);
        cref.setValue("Acceleration");
    }*/