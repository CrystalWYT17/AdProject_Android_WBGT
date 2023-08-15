package iss.ca.wbgt;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StationFragment extends Fragment implements AdapterView.OnItemSelectedListener, OnMapReadyCallback {

    private List<String> stationNameList;
    private List<Station> stationList;
    private LinkedHashMap<Integer, List<Double>> xHoursForecast = new LinkedHashMap<>();
    private MapView mMapView;
    private TextView serverTextView;
    //linechart
    private LineChart lineChart;
    private List<String> labels = new ArrayList<>();
    private ProgressBar loadingBar;
    private StationDataViewModel viewModel;
    private ArrayList<Entry> lineEntries = new ArrayList<>();
    private String stationId;

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
        loadingBar = (ProgressBar) rootView.findViewById(R.id.loadingBar);
        serverTextView = (TextView) rootView.findViewById(R.id.serverError);

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
        loadingBar.setVisibility(View.VISIBLE);
        ApiService service = new ApiService();

        CompletableFuture<LinkedHashMap<Integer, List<Double>>> chartData = CompletableFuture.supplyAsync(() -> {
            return service.getXHourForecastMultiStation(stationList.get(position).getId());
        });

        chartData.thenAccept(forecastData -> {
            xHoursForecast = forecastData;
            lineEntries.clear();
            getEntries();

            if(lineEntries == null || lineEntries.isEmpty()){
                getActivity().runOnUiThread(() -> {
                    loadingBar.setVisibility(View.GONE);
                    lineChart.setVisibility(View.GONE);
                    serverTextView.setVisibility(View.VISIBLE);
                });

            }else {
                getActivity().runOnUiThread(() -> {
                    MyLineChart newLineChart = new MyLineChart(lineChart, lineEntries);
                    loadingBar.setVisibility(View.GONE);
                    serverTextView.setVisibility(View.GONE);
                    lineChart.setVisibility(View.VISIBLE);
                    newLineChart.drawLineChart();
                });
            }

        });
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
        for(Station station: stationList){
            int zoomLevel = 10;
            LatLng markerLocation = new LatLng(station.getLatitude(),station.getLongitude());
            map.addMarker(new MarkerOptions().position(markerLocation).title(station.getName()));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLocation, zoomLevel));
        }
        map.getUiSettings().setZoomControlsEnabled(true);
    }

    private void getEntries(){
        LinkedHashMap<Integer, Double> entries = new LinkedHashMap<>();
        xHoursForecast.forEach((key, value)->{
            Double average = value.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .getAsDouble();
            entries.put(key, average);
        });
        //manipulating hour data for linechart
        //before end of today
        List<Integer> keys = new ArrayList<>(entries.keySet());
        for(Integer key:keys){
            lineEntries.add(new Entry(key, entries.get(key).floatValue()));
            entries.remove(key);
            if(key == 23){
                break;
            }
        }
        //for tomorrow
        for(Integer key: entries.keySet()){
            lineEntries.add(new Entry(key+24, entries.get(key).floatValue()));
        }
    }
    private List<Station> getStationList(){
        List<Station> stationList = new ArrayList<>();
        stationList.add(new Station("S117", "Ban Yan", 103.679, 1.256));
        stationList.add(new Station("S116", "West Coast", 103.754, 1.281));
        stationList.add(new Station("S50", "Clementi", 103.7768, 1.3337));
        stationList.add(new Station("S60", "Sentosa", 103.8279, 1.25));
        stationList.add(new Station("S107","East Coast Parkway",103.9625,1.3135));
        stationList.add(new Station("S43","Kim Chuan Street",103.8878,1.3399));
        stationList.add(new Station("S111","Scotts Road",103.8365,1.31055));
        stationList.add(new Station("S115","Tuas South Avenue 3",103.61843,1.29377));
        stationList.add(new Station("S109","Ang Mo Kio Avenue 5",103.8492,1.3764));
        stationList.add(new Station("S121","Choa Chu Kang Road",103.72244,1.37288));
        stationList.add(new Station("S104","Woodlands Avenue 9",103.78538,1.44387));
        stationList.add(new Station("S24","Upper Changi Road North",103.9826,1.3678));
        stationList.add(new Station("S44","Nanyang Avenue",103.68166,1.34583));
        return stationList;
    }

    private void initializeStationNameList(List<Station> stationList){
        stationNameList = new ArrayList<String>();
        for (Station station: stationList){
            stationNameList.add(station.getName());
        }
    }
}