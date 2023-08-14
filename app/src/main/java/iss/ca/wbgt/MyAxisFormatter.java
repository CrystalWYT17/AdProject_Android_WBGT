package iss.ca.wbgt;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class MyAxisFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        String res;
        if (value <= 23){
            res = String.valueOf(Math.round(value))+":00";
        }else {
            value = value -24;
            res = String.valueOf(Math.round(value))+":00";
        }
        return res;
    }
}
