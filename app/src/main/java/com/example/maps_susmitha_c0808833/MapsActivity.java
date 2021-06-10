package com.example.maps_susmitha_c0808833;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;

import android.content.Context;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;

import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.maps_susmitha_c0808833.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    LocationManager locationManager;
    LocationListener locationListener;
    LatLng userLocation;

    public static final String TAG = "MapsActivity";
    public static final int REQUEST_CODE = 1;

    List<Marker> latLngArrayListPolygon = new ArrayList<Marker>();
    ArrayList<Double> distancesFromMidPointsOfPolygonEdges = new ArrayList<>();
    List<String> allCityNames = new ArrayList<String>();
    List<Integer> doesCityExist = new ArrayList<Integer>();

    Polygon shape;
    Polyline line;
    private static final int POLYGON_SIDES = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        int count = 0;
        do {
            latLngArrayListPolygon.add(null);
            allCityNames.add("");
            doesCityExist.add(-1);
            count++;
        } while (count < POLYGON_SIDES);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                setHomeLocation(location);
            }
        };

        if (!isGrantedLocationPermission()) {
            requestForLocationPermission();
        } else {
            updateLatestLocation();
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                for (int i = 0; i < latLngArrayListPolygon.size(); i++) {
                    if (latLngArrayListPolygon.get(i) != null) {
                        LatLng latLng1 = latLng;
                        LatLng latLng2 = latLngArrayListPolygon.get(i).getPosition();

                        double distance =  mesaureLengthBetweenPoints(latLng1, latLng2);
                        if (3.0 > distance) {
                            if (doesCityExist.get(0) != -1 && doesCityExist.get(1) != -1 && doesCityExist.get(2) != -1 && doesCityExist.get(3) != -1) {
                                shape.remove();
                                shape = null;
                            }
                            doesCityExist.set(i, -1);
                            allCityNames.set(i, "");
                            latLngArrayListPolygon.get(i).remove();
                            latLngArrayListPolygon.set(i, null);
                            return;
                        }
                    }
                }

                if (shape != null) {
                    for (int i = 0; i < latLngArrayListPolygon.size(); i++) {
                        if (latLngArrayListPolygon.get(i) != null) {
                            latLngArrayListPolygon.get(i).remove();
                            latLngArrayListPolygon.set(i, null);
                        }
                    }
                    latLngArrayListPolygon.clear();
                    latLngArrayListPolygon.add(null);
                    latLngArrayListPolygon.add(null);
                    latLngArrayListPolygon.add(null);
                    latLngArrayListPolygon.add(null);

                    allCityNames.clear();
                    allCityNames.add("");
                    allCityNames.add("");
                    allCityNames.add("");
                    allCityNames.add("");

                    doesCityExist.clear();
                    doesCityExist.add(-1);
                    doesCityExist.add(-1);
                    doesCityExist.add(-1);
                    doesCityExist.add(-1);

                    shape.remove();
                    shape = null;
                }

                String latestCity = "";
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addressList != null && addressList.size() > 0 && addressList.get(0).getLocality() != null) {
                        latestCity = addressList.get(0).getLocality();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (latestCity.equals("")) {
                    return;
                }

                Boolean exist = false;
                for (String city : allCityNames) {
                    if (city.equalsIgnoreCase(latestCity)) {
                        exist = true;
                        return;
                    }
                }

                if (!exist) {
                    if (allCityNames.get(0).equals("")) {
                        allCityNames.set(0, latestCity);
                    } else if (allCityNames.get(1).equals("")) {
                        allCityNames.set(1, latestCity);
                    } else if (allCityNames.get(2).equals("")) {
                        allCityNames.set(2, latestCity);
                    } else if (allCityNames.get(3).equals("")) {
                        allCityNames.set(3, latestCity);
                    }
                }

                if (shape != null) {
                    shape.remove();
                    shape = null;
                }

                adjustPolygonWithRespectTo(latLng);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                String markerAddress = " ";
                Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        // street name
                        if (addressList.get(0).getThoroughfare() != null)
                            markerAddress += addressList.get(0).getThoroughfare() + ",";
                        if (addressList.get(0).getLocality() != null)
                            markerAddress += addressList.get(0).getLocality() + " ";
                        if (addressList.get(0).getPostalCode() != null)
                            markerAddress += addressList.get(0).getPostalCode() + " ";
                        if (addressList.get(0).getAdminArea() != null)
                            markerAddress += addressList.get(0).getAdminArea();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(), markerAddress, Toast.LENGTH_LONG).show();
                return false;
            }
        });

        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(@NonNull Polygon polygon) {

                LatLng latLng1 = latLngArrayListPolygon.get(0).getPosition();
                LatLng latLng2 = latLngArrayListPolygon.get(1).getPosition();
                LatLng latLng3 = latLngArrayListPolygon.get(2).getPosition();
                LatLng latLng4 = latLngArrayListPolygon.get(3).getPosition();

                double length1 = mesaureLengthBetweenPoints(latLng1, latLng2);
                double length2 = mesaureLengthBetweenPoints(latLng2, latLng3);
                double length3 = mesaureLengthBetweenPoints(latLng3, latLng4);
                double length4 = mesaureLengthBetweenPoints(latLng4, latLng1);

                double totalLength = Double.parseDouble(String.format("%.2f", length1 + length2 + length3 + length4));

                Toast.makeText(getBaseContext(), "A-B-C-D: " + totalLength + "Mile", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setHomeLocation(Location location) {
        userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions marker = new MarkerOptions().position(userLocation).title("Your location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).snippet("Toronto");

        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
    }

    private void adjustPolygonWithRespectTo(LatLng point) {
        double minDistance = 0;

        if (latLngArrayListPolygon.get(0) != null && latLngArrayListPolygon.get(1) != null && latLngArrayListPolygon.get(2) != null) {
            distancesFromMidPointsOfPolygonEdges.clear();

            for (int i = 0; i < latLngArrayListPolygon.size(); i++) {
                if (latLngArrayListPolygon.get(i) != null && latLngArrayListPolygon.get(i + 1) != null) {
                    ArrayList<LatLng> list = new ArrayList<>();

                    if (i == (latLngArrayListPolygon.size() - 1)) {
                        list.add(latLngArrayListPolygon.get(latLngArrayListPolygon.size() - 1).getPosition());
                        list.add(latLngArrayListPolygon.get(0).getPosition());
                    } else {
                        list.add((latLngArrayListPolygon.get(i).getPosition()));
                        list.add((latLngArrayListPolygon.get(i + 1).getPosition()));
                    }

                    LatLng midPoint = findMidPoint(list);

                    Location startPoint = new Location("");
                    startPoint.setLatitude(point.latitude);
                    startPoint.setLongitude(point.longitude);

                    Location endPoint = new Location("");
                    endPoint.setLatitude(midPoint.latitude);
                    endPoint.setLongitude(midPoint.longitude);

                    double distance = startPoint.distanceTo(endPoint);

                    distancesFromMidPointsOfPolygonEdges.add(distance);

                    if (i == 0) {
                        minDistance = distance;
                    } else {

                        if (distance < minDistance) {
                            minDistance = distance;
                        }
                    }
                }
            }

            int position = minIndex(distancesFromMidPointsOfPolygonEdges);

            int shiftByNumber = (latLngArrayListPolygon.size() - position - 1);

            if (shiftByNumber != latLngArrayListPolygon.size()) {
                latLngArrayListPolygon = rotate(latLngArrayListPolygon, shiftByNumber);
            }
        }

        Location currentLocation = new Location("currentLocation");
        currentLocation.setLatitude(userLocation.latitude);
        currentLocation.setLongitude(userLocation.longitude);

        Location markerLocation = new Location("markerLocation");
        markerLocation.setLatitude(point.latitude);
        markerLocation.setLongitude(point.longitude);

        double distance = Double.parseDouble(String.format("%.2f", currentLocation.distanceTo(markerLocation) / 1610));

        MarkerOptions newMarker = new MarkerOptions().position(point).snippet(String.valueOf(distance)+ "Mile");

        if (doesCityExist.get(0) == -1) {
            newMarker.title("A");
            doesCityExist.set(0, 1);
        } else if (doesCityExist.get(1) == -1) {
            newMarker.title("B");
            doesCityExist.set(1, 1);
        } else if (doesCityExist.get(2) == -1) {
            newMarker.title("C");
            doesCityExist.set(2, 1);
        } else if (doesCityExist.get(3) == -1) {
            newMarker.title("D");
            doesCityExist.set(3, 1);
        }

        if (latLngArrayListPolygon.get(0) == null) {
            latLngArrayListPolygon.set(0, mMap.addMarker(newMarker));
            doesCityExist.set(0, 1);
        } else if (latLngArrayListPolygon.get(1) == null) {
            latLngArrayListPolygon.set(1, mMap.addMarker(newMarker));
            doesCityExist.set(1, 1);
        } else if (latLngArrayListPolygon.get(2) == null) {
            latLngArrayListPolygon.set(2, mMap.addMarker(newMarker));
            doesCityExist.set(2, 1);
        } else if (latLngArrayListPolygon.get(3) == null) {
            latLngArrayListPolygon.set(3, mMap.addMarker(newMarker));
            doesCityExist.set(3, 1);
        }

        PolygonOptions polygonOptions = null;
        for (int i = 0; i < latLngArrayListPolygon.size(); i++) {
            if (latLngArrayListPolygon.get(i) != null) {
                if (i == 0)
                    polygonOptions = new PolygonOptions().add(latLngArrayListPolygon.get(0).getPosition());
                else
                    polygonOptions.add(latLngArrayListPolygon.get(i).getPosition());
            }
        }

        polygonOptions.strokeColor(Color.RED);
        polygonOptions.strokeWidth(10f);
        polygonOptions.fillColor(0x5900AA00);

        if (doesCityExist.get(0) != -1 && doesCityExist.get(1) != -1 && doesCityExist.get(2) != -1 && doesCityExist.get(3) != -1) {
            shape = mMap.addPolygon(polygonOptions);
        }

        if (shape != null) {
            shape.setClickable(true);
        }
    }

    public static int minIndex(ArrayList<Double> list) {
        return list.indexOf(Collections.min(list));
    }

    public static <T> List<T> rotate(List<T> aL, int shift) {
        if (aL.size() == 0)
            return aL;

        T element = null;
        for (int i = 0; i < shift; i++) {
            // remove last element, add it to front of the ArrayList
            element = aL.remove(aL.size() - 1);
            aL.add(0, element);
        }

        return aL;
    }

    private LatLng findMidPoint(List<LatLng> points) {
        double latitude = 0;
        double longitude = 0;
        int n = points.size();

        for (LatLng point : points) {
            latitude += point.latitude;
            longitude += point.longitude;
        }

        return new LatLng(latitude / n, longitude / n);
    }


    public double mesaureLengthBetweenPoints(LatLng latLng, LatLng latLng1) {
        Location point1 = new Location("point1");
        point1.setLatitude(latLng.latitude);
        point1.setLongitude(latLng.longitude);

        Location point2 = new Location("point2");
        point2.setLatitude(latLng1.latitude);
        point2.setLongitude(latLng1.longitude);

        double distance = Double.parseDouble(String.format("%.2f", point2.distanceTo(point1) / 1610));

        return distance;
    }

    private boolean isGrantedLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestForLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private void updateLatestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 7500, 0, locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 7500, 0, locationListener);
        }
    }
}