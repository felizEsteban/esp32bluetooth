package com.example.proyecto;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<String> deviceAddress=new MutableLiveData<>();
    private MutableLiveData<String> deviceName=new MutableLiveData<>();
    private MutableLiveData<Integer> temp=new MutableLiveData<>();
    private MutableLiveData<Integer> hum=new MutableLiveData<>();
    private MutableLiveData<String> dataIn=new MutableLiveData<>();
    private MutableLiveData<String> dataOut=new MutableLiveData<>();

    public SharedViewModel() {
        this.deviceName.setValue("no name");
        this.deviceAddress.setValue("no address");
        this.temp.setValue(0);
        this.hum.setValue(0);
        this.dataIn.setValue("no data in");
        this.dataOut.setValue("no data out");
    }

    public MutableLiveData<String> getDeviceAddress() {return deviceAddress; }
    public void setDeviceAddress(String deviceAddress) {this.deviceAddress.setValue(deviceAddress);   }
    public MutableLiveData<String> getDeviceName() { return deviceName;   }
    public void setDeviceName(String deviceName) {this.deviceName.setValue(deviceName);    }
    public MutableLiveData<Integer> getTemp() {return temp;    }
    public void setTemp(Integer temp) { this.temp.setValue(temp);   }
    public MutableLiveData<Integer> getHum() { return hum;    }
    public void setHum(MutableLiveData<Integer> hum) { this.hum = hum;   }
    public MutableLiveData<String> getDataIn() {    return dataIn;   }
    public void setDataIn(String dataIn) { this.dataIn.setValue(dataIn);    }
    public MutableLiveData<String> getDataOut() {return dataOut;  }
    public void setDataOut(String dataOut) { this.dataOut.setValue(dataOut); }
}
