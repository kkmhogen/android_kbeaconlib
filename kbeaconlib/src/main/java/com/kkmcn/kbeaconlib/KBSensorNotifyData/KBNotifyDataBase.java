package com.kkmcn.kbeaconlib.KBSensorNotifyData;

import com.kkmcn.kbeaconlib.KBeacon;

public abstract class KBNotifyDataBase {
    abstract public int getSensorDataType();

    abstract public void parseSensorDataResponse(final KBeacon beacon, byte[] sensorDataRsp);

}
