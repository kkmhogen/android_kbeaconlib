package com.kbeacon.kbeaconlib.KBSensorNotifyData;

import com.kbeacon.kbeaconlib.KBUtility;
import com.kbeacon.kbeaconlib.KBeacon;

public class KBHumidityNotifyData extends KBNotifyDataBase{


    public long eventUTCTime;
    private float temperature;
    private float humidity;

    private static final String LOG_TAG = "KBTHNotifyData";
    private static final int DEFAULT_MESSAGE_LEN = 9;

    public int getSensorDataType()
    {
        return KBNotifyDataType.NTF_DATA_TYPE_HUMIDITY;
    }

    public long getEventUTCTime()
    {
        return eventUTCTime;
    }

    public float getHumidity() {
        return humidity;
    }

    public float getTemperature() {
        return temperature;
    }

    public void parseSensorDataResponse(final KBeacon beacon, byte[] sensorDataNtf)
    {
        int nIndex = 1;

        if (sensorDataNtf == null || sensorDataNtf.length < DEFAULT_MESSAGE_LEN)
        {
            return;
        }

        //nearby utc time
        int nUtcTime =  KBUtility.htonint(sensorDataNtf[nIndex++],
                sensorDataNtf[nIndex++],
                sensorDataNtf[nIndex++],
                sensorDataNtf[nIndex++]);
        eventUTCTime = (nUtcTime & 0xFFFFFFFF);

        temperature = KBUtility.signedBytes2Float(sensorDataNtf[nIndex], sensorDataNtf[nIndex+1]);
        nIndex += 2;

        humidity = KBUtility.signedBytes2Float(sensorDataNtf[nIndex], sensorDataNtf[nIndex+1]);
        nIndex += 2;
    }
}
