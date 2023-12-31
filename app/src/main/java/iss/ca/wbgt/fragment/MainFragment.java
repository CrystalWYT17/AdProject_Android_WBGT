package iss.ca.wbgt.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import iss.ca.wbgt.service.ApiService;
import iss.ca.wbgt.model.ForecastDay;
import iss.ca.wbgt.service.LocationService;
import iss.ca.wbgt.util.MyLineChart;
import iss.ca.wbgt.R;
import iss.ca.wbgt.adapter.RecyclerAdapter;
import iss.ca.wbgt.viewModel.StationDataViewModel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    //for line chart
    private LineChart lineChart;
    private ArrayList<Entry> lineEntries = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView serverTextView;

    //for recyclerview
    private RecyclerView recyclerView;
    private List<ForecastDay> forecastDayList = new ArrayList<ForecastDay>();
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerAdapter recyclerAdapter;
    private LinearLayoutManager horizontalLayout;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private StationDataViewModel viewModel;

    private Map<String,List<String>> dayForecast = new HashMap<>();
    private Map<Integer, List<Double>> xHoursForecast = new HashMap<>();
    private LocationService locationService = new LocationService();

    public MainFragment() {
        // Required empty public constructor
    }

    public void setXHoursForecast(Map<Integer, List<Double>> hoursForecast){
        this.xHoursForecast = hoursForecast;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
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
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        // set station name and wbgt value
        TextView txtStationName = rootView.findViewById(R.id.station);
        TextView txtWbgtValue = rootView.findViewById(R.id.wbgt_value);

        // get view model
        viewModel = new ViewModelProvider(this).get(StationDataViewModel.class);

        viewModel.getStateData().observe(getViewLifecycleOwner(),s -> {
            if(s.getStationName() != null && s.getWbgtValue() != null){
                txtStationName.setText(s.getStationName());
                txtWbgtValue.setText(s.getWbgtValue());
            }
        });

        SharedPreferences shr = getActivity().getSharedPreferences("wbgt_main_fragment", Context.MODE_PRIVATE);
        String currentStationName = shr.getString("currentStationName","");
        String currentWbgt = shr.getString("currentWbgt","");
        String dayForecastString = shr.getString("dayForecast","");

        String stationId = locationService.getCurrentLocation(getContext());

        if( currentWbgt.isEmpty() && currentStationName.isEmpty()){

            txtStationName.setText("Choa Chu Kang Station");
            txtWbgtValue.setText("33");
        }
        else{
            txtStationName.setText(currentStationName);
            txtWbgtValue.setText(String.valueOf(Math.round(Float.parseFloat(currentWbgt))));
        }
        if(dayForecastString.isEmpty()){
            getDayForecastStaticData();
        }
        else{
            convertStringToMap(dayForecastString);
            getDataForXDaysForecast();
        }

        //linechart
        lineChart = (LineChart) rootView.findViewById(R.id.lineChart);
        progressBar = (ProgressBar) rootView.findViewById(R.id.loadingBar);
        serverTextView = (TextView) rootView.findViewById(R.id.serverErrorTxt);
        lineChart.setVisibility(View.GONE);

        ApiService service = new ApiService();

        CompletableFuture<Map<Integer, List<Double>>> chartData = CompletableFuture.supplyAsync(() -> {
            return service.getXHourForecastMultiStation(stationId);
        });

        chartData.thenAccept(forecastData -> {
            xHoursForecast = forecastData;
            lineEntries.clear();
            getEntries();

            if(lineEntries == null || lineEntries.isEmpty()){
                getActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    lineChart.setVisibility(View.GONE);
                    serverTextView.setVisibility(View.VISIBLE);
                });

            }else {
                getActivity().runOnUiThread(() -> {
                    MyLineChart newLineChart = new MyLineChart(lineChart, lineEntries);
                    progressBar.setVisibility(View.GONE);
                    serverTextView.setVisibility(View.GONE);
                    lineChart.setVisibility(View.VISIBLE);
                    newLineChart.drawLineChart();
                });
            }

        });

        //refresh
        SwipeRefreshLayout refreshLayout = rootView.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                final String currentStationId = stationId;
                //make api call and refresh data
                ApiService service = new ApiService();

                CompletableFuture<Map<Integer, List<Double>>> chartData = CompletableFuture.supplyAsync(() -> {
                    return service.getXHourForecastMultiStation(currentStationId);
                });

                chartData.thenAccept(forecastData -> {
                    xHoursForecast = forecastData;
                    lineEntries.clear();
                    getEntries();

                    getActivity().runOnUiThread(() -> {
                        MyLineChart newLineChart = new MyLineChart(lineChart, lineEntries);
                        System.out.println("done refreshing");
                        refreshLayout.setRefreshing(false);
                        newLineChart.drawLineChart();
                    });
                });

                locationService.getNearestStation(getContext());
            }
        });


        //recyclerView
        recyclerView = (RecyclerView) rootView.findViewById(R.id.xDaysForecast);
        layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
//        getDataForXDaysForecast();
        recyclerAdapter = new RecyclerAdapter(forecastDayList);
        horizontalLayout = new LinearLayoutManager(
                getActivity(),
                LinearLayoutManager.HORIZONTAL,
                false);
        recyclerView.setLayoutManager(horizontalLayout);
        recyclerView.setAdapter(recyclerAdapter);

        return rootView;
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

    private void getDataForXDaysForecast(){

        DayOfWeek currentDay = LocalDate.now().getDayOfWeek();
        List<String> dayNames = new ArrayList<>(Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"));
        int currentIndx = dayNames.indexOf(currentDay.getDisplayName(TextStyle.FULL, Locale.ENGLISH));

        for(int i =0; i<dayNames.size() ; i++){
            int index = (currentIndx + i) % dayNames.size();
            String day = dayNames.get(index);
            if(dayForecast.containsKey(day.toUpperCase())){
                String dayName;
                String tmp = null;
                List<String> minMaxWbgt;
                if(day.equalsIgnoreCase(String.valueOf(currentDay))){
                    dayName = "Today";
                }
                else{
                    dayName = dayNames.get(index).substring(0,3);
                    tmp = dayNames.get(index).toUpperCase();
                }
                if(tmp == null){
                    minMaxWbgt = dayForecast.get(String.valueOf(currentDay));
                }
                else {
                    minMaxWbgt = dayForecast.get(tmp);
                }

                Double minVal = Double.parseDouble(minMaxWbgt.get(0));
                Double maxVal = Double.parseDouble(minMaxWbgt.get(1));
                ForecastDay forecastDay = new ForecastDay(dayName, String.valueOf(Math.round(maxVal)), String.valueOf(Math.round(minVal)));
                forecastDayList.add(forecastDay);

            }
        }
    }

    public void convertStringToMap(String dayForecastString){
        String[] eachDayList = dayForecastString.split("/");
        String[] dayName;
        String[] minMaxVal;

        for(String eachDay: eachDayList){
            List<String> lowHigh = new ArrayList<>();
            dayName = eachDay.split(",");
            minMaxVal = dayName[1].split("\\|");
            lowHigh.add(minMaxVal[0]);
            lowHigh.add(minMaxVal[1]);
            dayForecast.put(dayName[0],lowHigh);
        }
    }

    private void getDayForecastStaticData(){
        forecastDayList.add(new ForecastDay("Today", "35", "25"));
        forecastDayList.add(new ForecastDay("Mon", "35", "25"));
        forecastDayList.add(new ForecastDay("Tue", "35", "25"));
        forecastDayList.add(new ForecastDay("Wed", "35", "25"));
        forecastDayList.add(new ForecastDay("Thu", "35", "25"));
        forecastDayList.add(new ForecastDay("Fri", "35", "25"));
        forecastDayList.add(new ForecastDay("Sat", "35", "25"));
    }
}