package com.example.directionapi;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.directionapi.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private LatLng Origin;
    private ArrayList<LatLng> Destination = new ArrayList<LatLng>();

    private ArrayList<LatLng> polyline = new ArrayList<LatLng>();


    int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    double lat = 0;
    double lng = 0;
    String commandStr = LocationManager.NETWORK_PROVIDER;
    LocationManager locationManager;
    //設定監聽移動距離做更新(單位:m)
    long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    //設定秒數做更新(單位:ms)
    long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    private Polyline direction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Get_Location();

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
        Set_Listener();
    }

    public void Get_Location() {
        //取得APP對手機服務的權限(GPS)
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_ACCESS_COARSE_LOCATION);
            Get_Location();
        }
    }

    public void Set_Listener() {
        //設定監聽如果有新位置時所做的事情
        LocationListener locationListenerGPS = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String msg = "New Latitude: " + latitude + "New Longitude: " + longitude;
                System.out.println(msg);
            }
        };
        //取得系統服務(GPS)
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //設定更新速度與距離
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //return;
            Get_Location();
        }
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
        //如果沒移動(或是沒有網路)取得先前位置，如果沒有上面刷新，就會是手機最後定位的位置
        Location location = locationManager.getLastKnownLocation(commandStr);
        //取得經緯度
        lat = location.getLatitude();
        lng = location.getLongitude();
        //利用LatLng Lib 設定目前位置
        LatLng HOME = new LatLng(lat, lng);
        Origin = HOME;
        //新增Marker在Maps裡面，並命名為目前位置
        mMap.addMarker(new MarkerOptions().position(HOME).title("目前位置"));
        //移動相機至HOME點，並置放至最中間，然後縮放level=15
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HOME, 15));
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latlng) {
                remove_direction_line();
                System.out.println(latlng);
                Destination.add(latlng);
                mMap.addMarker(new MarkerOptions().position(latlng).title("點選位置"));
                My_Direction my_direction = new My_Direction(Origin, Destination, "AIzaSyBm6kC5U0Y_k3lfmggPRurC0C3o3wiUlA0");
                my_direction.SearchDirection(new My_Direction.onDataReadyCallback() {
                    @Override
                    public void onDataReady(ArrayList<LatLng> data) {
                        polyline = data;
                        Draw_Map(polyline);
                    }
                });
            }
        });
    }
    private void Draw_Map(ArrayList<LatLng> Points){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                PolylineOptions polylineOptions = new PolylineOptions();
                for(int i=0; i< Points.size();i++){
                     polylineOptions.add(Points.get(i));
                }
                polylineOptions.color(MapsActivity.this.getResources().getColor(R.color.route_color));
                polylineOptions.width(9f);
                direction = mMap.addPolyline(polylineOptions);
            }
        });
    }
    private void remove_direction_line() {
        if(direction!=null){
            direction.remove();
        }
    }
}