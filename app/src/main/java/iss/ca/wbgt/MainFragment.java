package iss.ca.wbgt;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    MyLineChart myLineChart;

    //for recyclerview
    private RecyclerView recyclerView;
    private List<ForecastDay> forecastDayList = new ArrayList<ForecastDay>();
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerAdapter recyclerAdapter;
    private LinearLayoutManager horizontalLayout;
    private View childView;
    private int recyclerViewItemPosition;
    ;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String stationName;
    private String wbgtValue;

    private Map<String,List<String>> dayForecast = new HashMap<>();

    public MainFragment() {
        // Required empty public constructor
    }

    public void setDayForecast(Map<String, List<String>> dayForecast) {
        this.dayForecast = dayForecast;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public void setWbgtValue(String wbgtValue) {
        this.wbgtValue = wbgtValue;
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

        //refresh
        SwipeRefreshLayout refreshLayout = rootView.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //make api call and refresh data
                System.out.println("done refreshing");
                refreshLayout.setRefreshing(false);
            }
        });

        // set station name and wbgt value
        TextView txtStationName = rootView.findViewById(R.id.station);
        TextView txtWbgtValue = rootView.findViewById(R.id.wbgt_value);
        txtStationName.setText(stationName);
        txtWbgtValue.setText(String.valueOf(wbgtValue));

        //linechart
        lineChart = (LineChart) rootView.findViewById(R.id.lineChart);
        getEntries();
        myLineChart = new MyLineChart(lineChart, lineEntries);
        myLineChart.drawLineChart();

        //recyclerView
        recyclerView = (RecyclerView) rootView.findViewById(R.id.xDaysForecast);
        layoutManager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(layoutManager);
        getDataForXDaysForecast();
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

    private void getDataForXDaysForecast(){

        DayOfWeek currentDay = LocalDate.now().getDayOfWeek();
        List<String> dayNames = new ArrayList<>(Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"));
        int currentIndx = dayNames.indexOf(currentDay.getDisplayName(TextStyle.FULL, Locale.ENGLISH));

        for(int i =0; i<dayNames.size() ; i++){
            int index = (currentIndx + i) % dayNames.size();
            String day = dayNames.get(index);
            if(dayForecast.containsKey(day.toUpperCase())){
                String dayName;
                if(day.equalsIgnoreCase(String.valueOf(currentDay))){
                    dayName = "Today";
                }
                else{
                    dayName = dayNames.get(index);
                }
                List<String> minMaxWbgt = dayForecast.get(String.valueOf(currentDay));
                Double minVal = Double.parseDouble(minMaxWbgt.get(0));
                Double maxVal = Double.parseDouble(minMaxWbgt.get(1));
                ForecastDay forecastDay = new ForecastDay(dayName, String.valueOf(Math.round(minVal)), String.valueOf(Math.round(maxVal)));
                forecastDayList.add(forecastDay);

            }
        }

//        for(Map.Entry<String, List<String>> dayF: dayForecast.entrySet()){
//            Log.i("data","dayF: "+dayF.getKey()+"value: "+dayF.getValue());
//        }
//        forecastDayList.add(new ForecastDay("Today", "35", "25"));
//        forecastDayList.add(new ForecastDay("Mon", "35", "25"));
//        forecastDayList.add(new ForecastDay("Tue", "35", "25"));
//        forecastDayList.add(new ForecastDay("Wed", "35", "25"));
//        forecastDayList.add(new ForecastDay("Thu", "35", "25"));
//        forecastDayList.add(new ForecastDay("Fri", "35", "25"));
//        forecastDayList.add(new ForecastDay("Sat", "35", "25"));
    }


}