package com.kbeacon.kbeaconlib.KBSensorNotifyData;
import com.kbeacon.kbeaconlib.KBeacon;

public class KBNotifyButtonEvtData extends KBNotifyDataBase{
    public final static int BTN_LONG_CLICK_EVT = 0x2;
    public final static int BTN_SINGLE_CLICK_EVT = 0x3;
    public final static int BTN_DOUBLE_CLICK_EVT = 0x4;
    public final static int BTN_THREE_CLICK_EVT = 0x5;

    public int keyNtfEvent = 0;

    public int getSensorDataType()
    {
        return KBNotifyDataType.NTF_DATA_TYPE_BUTTON_EVT;
    }

    public void parseSensorDataResponse(final KBeacon beacon, byte[] sensorDataNtf)
    {
        if (sensorDataNtf == null || sensorDataNtf.length < 2)
        {
            return;
        }
        if (sensorDataNtf[0] != KBNotifyDataType.NTF_DATA_TYPE_BUTTON_EVT)
        {
            return;
        }

        keyNtfEvent = sensorDataNtf[1];
    }
}
