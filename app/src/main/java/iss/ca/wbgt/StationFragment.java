package iss.ca.wbgt;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StationFragment extends Fragment implements AdapterView.OnItemSelectedListener, OnMapReadyCallback {

    private List<String> stationNameList;
    private List<Station> stationList;
    private MapView mMapView;
    //linechart
    private LineChart lineChart;
    private ArrayList<Entry> lineEntries = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StationFragment newInstance(String param1, String param2) {
        StationFragment fragment = new StationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_station, container, false);
        Spinner spinner = (Spinner) rootView.findViewById(R.id.dropdown);
        lineChart = (LineChart) rootView.findViewById(R.id.lineChart);
        getEntries();
        MyLineChart newLineChart = new MyLineChart(lineChart, lineEntries);
        newLineChart.drawLineChart();

        //initialize station data
        stationList = getStationList();
        initializeStationNameList(stationList);

        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, stationNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY);
        //MapsInitializer.initialize(requireContext());

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Toast.makeText(getContext(), stations[position], Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //LatLng markerLocation = new LatLng(1.25,103.8279);
//        map.addMarker(new MarkerOptions().position(new LatLng(1.25,103.8279)).title("Marker"));
//        int zoomLevel = 12;
//        map.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, zoomLevel));
//        map.getUiSettings().setZoomControlsEnabled(true);

        for(Station station: stationList){
            int zoomLevel = 10;
            LatLng markerLocation = new LatLng(station.getLatitude(),station.getLongitude());
            map.addMarker(new MarkerOptions().position(markerLocation).title(station.getName()));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, zoomLevel));
        }
        map.getUiSettings().setZoomControlsEnabled(true);
    }

    private void getEntries(){
        lineEntries.add(new Entry(0f, 5f));
        lineEntries.add(new Entry(1f, 8f));
        lineEntries.add(new Entry(2f, 6f));
        lineEntries.add(new Entry(3f, 8f));
        lineEntries.add(new Entry(4f, 6f));
        lineEntries.add(new Entry(5f, 4f));
        lineEntries.add(new Entry(6f, 5f));
        lineEntries.add(new Entry(7f, 4f));
        lineEntries.add(new Entry(8f, 6f));
        lineEntries.add(new Entry(9f, 7f));
        lineEntries.add(new Entry(10f, 10f));
        lineEntries.add(new Entry(11f, 12f));
        lineEntries.add(new Entry(12f, 10f));
        lineEntries.add(new Entry(13f, 4f));
        lineEntries.add(new Entry(14f, 10f));
        lineEntries.add(new Entry(15f, 8f));
        lineEntries.add(new Entry(16f, 5f));
        lineEntries.add(new Entry(17f, 4f));
        lineEntries.add(new Entry(18f, 3f));
        lineEntries.add(new Entry(19f, 4f));
        lineEntries.add(new Entry(20f, 6f));
        lineEntries.add(new Entry(21f, 8f));
        lineEntries.add(new Entry(22f, 6f));
        lineEntries.add(new Entry(23f, 4f));
    }
    private List<Station> getStationList(){
        List<Station> stationList = new ArrayList<>();
        stationList.add(new Station("S117", "Ban Yan", 103.679, 1.256));
        stationList.add(new Station("S116", "West Coast", 103.754, 1.281));
        stationList.add(new Station("S50", "Clementi", 103.7768, 1.3337));
        stationList.add(new Station("S60", "Sentosa", 103.8279, 1.25));
        stationList.add(new Station("S107","East Coast Parkway",103.9625,1.3135));
        stationList.add(new Station("S43","Kim Chuan Street",103.8878,1.3399));
        return stationList;
    }

    private void initializeStationNameList(List<Station> stationList){
        stationNameList = new ArrayList<String>();
        for (Station station: stationList){
            stationNameList.add(station.getName());
        }
    }
}