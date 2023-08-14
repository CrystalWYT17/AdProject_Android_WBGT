package iss.ca.wbgt;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class MyLineChart {
    private LineChart lineChart;
    private LineData lineData;
    private LineDataSet lineDataSet;
    private ArrayList<Entry> lineEntries = new ArrayList<>();

    public MyLineChart(LineChart lineChart, ArrayList<Entry> lineEntries){
        this.lineChart = lineChart;
        this.lineEntries = lineEntries;
    }

    public void setData(ArrayList<Entry> lineEntries){
        this.lineEntries = lineEntries;
    }

    public void drawLineChart(){
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
        xAxis.setValueFormatter(new MyAxisFormatter());
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
    }
}
