package iss.ca.wbgt.viewModel;

import java.util.ArrayList;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import iss.ca.wbgt.model.UserCurrentData;

import com.github.mikephil.charting.data.Entry;

public class StationDataViewModel extends ViewModel {

    private final MutableLiveData<UserCurrentData> stateData = new MutableLiveData<>(new UserCurrentData());
    private MutableLiveData<ArrayList<Entry>> lineEntries = new MutableLiveData<>();
    private MutableLiveData<String> separateStationId = new MutableLiveData<>();

    public MutableLiveData<UserCurrentData> getStateData() {
        return stateData;
    }
    public MutableLiveData<ArrayList<Entry>> getLineEntries(){return lineEntries;}
    public MutableLiveData<String> getSeparateStationId(){return separateStationId;}

    public void setUserCurrentData(UserCurrentData currentData){
        stateData.setValue(currentData);
    }
    public void setLineEntries(ArrayList<Entry> entries){lineEntries.setValue(entries);}
    public void setSeparateStationId(String stationId){separateStationId.setValue(stationId);}

}
