package iss.ca.wbgt;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private LineChart lineChart;
    private LineData lineData;
    private LineDataSet lineDataSet;
    private ArrayList<Entry> lineEntries = new ArrayList<>();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MainFragment() {
        // Required empty public constructor
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
        lineChart = (LineChart) rootView.findViewById(R.id.lineChart);
        getEntries();
        lineDataSet = new LineDataSet(lineEntries, "WBGT");
        lineData = new LineData(lineDataSet);
        lineDataSet.setColor(Color.GREEN);
        lineDataSet.setLineWidth(2f);
        lineDataSet.setDrawValues(false);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setValueTextSize(12f);

        //customize Axis
        XAxis xAxis = lineChart.getXAxis();
        YAxis yAxis = lineChart.getAxisLeft();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxis.setDrawGridLines(false);
        //lineChart.getAxisRight().setDrawGridLines(false);
        lineChart.getAxisRight().setEnabled(false);
        xAxis.setGranularity(1f);

        //lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setData(lineData);
        lineChart.invalidate();
        lineChart.animateX(1000);
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
}