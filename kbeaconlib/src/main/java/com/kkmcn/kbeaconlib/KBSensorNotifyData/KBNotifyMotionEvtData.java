package com.kkmcn.kbeaconlib.KBSensorNotifyData;
import com.kkmcn.kbeaconlib.KBeacon;

public class KBNotifyMotionEvtData extends KBNotifyDataBase {
    public int motionNtfEvent = 0;

    public int getSensorDataType()
    {
        return KBNotifyDataType.NTF_DATA_TYPE_MOTION_EVT;
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

        motionNtfEvent = sensorDataNtf[1];
    }
}