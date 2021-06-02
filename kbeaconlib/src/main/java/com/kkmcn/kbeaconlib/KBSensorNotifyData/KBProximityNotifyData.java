package com.kkmcn.kbeaconlib.KBSensorNotifyData;

import com.kkmcn.kbeaconlib.KBUtility;
import com.kkmcn.kbeaconlib.KBeacon;


public class KBProximityNotifyData extends KBNotifyDataBase {

    public long nearbyEventUTCTime;
    public String nearbyDeviceMac;
    public int nearbyDeviceMajorID;
    public int nearbyDeviceMinorID;
    public int nearbySeconds;
    public int nearbyDistance;

    private static final String LOG_TAG = "KBProximityNotifyData";
    private static final int DEFAULT_MESSAGE_LEN = 15;

    public int getSensorDataType()
    {
        return KBNotifyDataType.NTF_DATA_TYPE_PROXIMITY;
    }

    public void parseSensorDataResponse(final KBeacon beacon, byte[] sensorDataNtf)
    {
        int nIndex = 1;

        if (sensorDataNtf == null || sensorDataNtf.length < DEFAULT_MESSAGE_LEN)
        {
            return;
        }

        //nearby utc time
        int nEventTime =  KBUtility.htonint(sensorDataNtf[nIndex++],
                sensorDataNtf[nIndex++],
                sensorDataNtf[nIndex++],
                sensorDataNtf[nIndex++]);
        nearbyEventUTCTime = (nEventTime & 0xFFFFFFFF);

        //mac address;
        byte[] byMacAddress = new byte[4];
        byMacAddress[0] = sensorDataNtf[nIndex++];
        byMacAddress[1] = sensorDataNtf[nIndex++];
        byMacAddress[2] = sensorDataNtf[nIndex++];
        byMacAddress[3] = sensorDataNtf[nIndex++];
        String strMacPrefex = beacon.getMac().substring(0, 6);
        String strMacTail = KBUtility.bytesToHexString(byMacAddress);
        if (strMacTail != null) {
            strMacTail = strMacTail.toUpperCase();
            nearbyDeviceMac = strMacPrefex +
                    ":" + strMacTail.substring(0, 2) +
                    ":" + strMacTail.substring(2, 4) +
                    ":" + strMacTail.substring(4, 6) +
                    ":" + strMacTail.substring(6, 8);
        }

        //major id
        nearbyDeviceMajorID = KBUtility.htonshort(sensorDataNtf[nIndex++], sensorDataNtf[nIndex++]);

        //minor id
        nearbyDeviceMinorID = KBUtility.htonshort(sensorDataNtf[nIndex++], sensorDataNtf[nIndex++]);

        //nearby time
        nearbySeconds = sensorDataNtf[nIndex++];

        //distance
        nearbyDistance = sensorDataNtf[nIndex];
    }
}
