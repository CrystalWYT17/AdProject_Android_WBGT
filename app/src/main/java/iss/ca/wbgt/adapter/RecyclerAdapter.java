package iss.ca.wbgt.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import iss.ca.wbgt.R;
import iss.ca.wbgt.model.ForecastDay;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemView> {

    List<ForecastDay> forecastDayList;
    public RecyclerAdapter(List<ForecastDay> forecastList){
        this.forecastDayList = forecastList;
    }

    @NonNull
    @Override
    public ItemView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler, parent, false);
        return new ItemView(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemView holder, int position) {
        holder.dayTxt.setText(forecastDayList.get(position).getDay());
        holder.valueHigh.setText(forecastDayList.get(position).getHighValue());
        holder.valueLow.setText(forecastDayList.get(position).getLowValue());
    }

    @Override
    public int getItemCount() {
        return forecastDayList.size();
    }

    public class ItemView extends RecyclerView.ViewHolder{
        TextView dayTxt;
        TextView highTxt;
        TextView valueHigh;
        TextView lowTxt;
        TextView valueLow;

        public ItemView(@NonNull View itemView) {
            super(itemView);
            dayTxt = (TextView) itemView.findViewById(R.id.day);
            highTxt = (TextView) itemView.findViewById(R.id.highText);
            valueHigh = (TextView) itemView.findViewById(R.id.value_high);
            lowTxt = (TextView) itemView.findViewById(R.id.lowText);
            valueLow = (TextView) itemView.findViewById(R.id.value_low);
        }
    }
}
