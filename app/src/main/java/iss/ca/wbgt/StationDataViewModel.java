package iss.ca.wbgt;

import java.util.List;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StationDataViewModel extends ViewModel {

    private final MutableLiveData<UserCurrentData> stateData = new MutableLiveData<>(new UserCurrentData());

    public MutableLiveData<UserCurrentData> getStateData() {
        return stateData;
    }

    public void setUserCurrentData(UserCurrentData currentData){
        stateData.setValue(currentData);
    }

}
