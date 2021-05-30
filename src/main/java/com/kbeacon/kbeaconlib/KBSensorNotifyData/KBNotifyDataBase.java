package com.kbeacon.kbeaconlib.KBSensorNotifyData;

import com.kbeacon.kbeaconlib.KBeacon;

public abstract class KBNotifyDataBase {
    abstract public int getSensorDataType();

    abstract public void parseSensorDataResponse(final KBeacon beacon, byte[] sensorDataRsp);

}
