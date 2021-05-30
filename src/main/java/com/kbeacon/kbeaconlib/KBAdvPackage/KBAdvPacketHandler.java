package com.kbeacon.kbeaconlib.KBAdvPackage;

import android.bluetooth.le.ScanRecord;
import android.util.Log;

import com.kbeacon.kbeaconlib.KBUtility;

import java.util.HashMap;


public class KBAdvPacketHandler {
    private static final int APPLE_MANUFACTURE_ID = 0x004C;
    private static final int MIN_EDDY_URL_ADV_LEN = 3;
    private static final int MIN_EDDY_UID_ADV_LEN = 18;
    private static final int MIN_EDDY_TLM_ADV_LEN = 14;
    private static final int MIN_IBEACON_ADV_LEN = 0x17;
    private static final int MIN_SENSOR_ADV_LEN = 3;

    private static final String LOG_TAG = "KBAdvPacketHandler";

    private Integer batteryPercent;

    private int filterAdvType;

    public Integer getBatteryPercent()
    {
        return batteryPercent;
    }

    private HashMap<String, KBAdvPacketBase> mAdvPackets;

    static private HashMap<String, Class> kbAdvPacketTypeObjects;

    static{
        kbAdvPacketTypeObjects = new HashMap<>(5);
        kbAdvPacketTypeObjects.put(String.valueOf(KBAdvType.KBAdvTypeSensor), KBAdvPacketSensor.class);
        kbAdvPacketTypeObjects.put(String.valueOf(KBAdvType.KBAdvTypeEddyURL), KBAdvPacketEddyURL.class);
        kbAdvPacketTypeObjects.put(String.valueOf(KBAdvType.KBAdvTypeEddyTLM), KBAdvPacketEddyTLM.class);
        kbAdvPacketTypeObjects.put(String.valueOf(KBAdvType.KBAdvTypeEddyUID), KBAdvPacketEddyUID.class);
        kbAdvPacketTypeObjects.put(String.valueOf(KBAdvType.KBAdvTypeIBeacon), KBAdvPacketIBeacon.class);
    }

    public KBAdvPacketHandler()
    {
        mAdvPackets = new HashMap<>(5);
    }

    public KBAdvPacketBase[] advPackets()
    {
        KBAdvPacketBase[] advArrays = new KBAdvPacketBase[mAdvPackets.size()];
        mAdvPackets.values().toArray(advArrays);

        return advArrays;
    }

    public void setAdvTypeFilter(int filterAdvType) {
        this.filterAdvType = filterAdvType;
    }

    public KBAdvPacketBase getAdvPacket(int nAdvType)
    {
        return mAdvPackets.get(String.valueOf(nAdvType));
    }

    public boolean parseAdvPacket(ScanRecord record, int rssi, String name) {
        int nAdvType = KBAdvType.KBAdvTypeInvalid;
        byte[] beaconData = null;
        boolean bParseDataRslt = false;

        if (record.getManufacturerSpecificData() != null) {
            beaconData = record.getManufacturerSpecificData(APPLE_MANUFACTURE_ID);
            if (beaconData != null && beaconData.length == MIN_IBEACON_ADV_LEN) {
                nAdvType = KBAdvType.KBAdvTypeIBeacon;
            }
        }
        if (record.getServiceData() != null) {
            byte[] eddyData = record.getServiceData(KBUtility.PARCE_UUID_EDDYSTONE);
            if (eddyData != null) {
                beaconData = eddyData;
                if (eddyData[0] == 0x10 && eddyData.length >= MIN_EDDY_URL_ADV_LEN) {
                    nAdvType = KBAdvType.KBAdvTypeEddyURL;
                } else if (eddyData[0] == 0x0 && eddyData.length >= MIN_EDDY_UID_ADV_LEN) {
                    nAdvType = KBAdvType.KBAdvTypeEddyUID;
                } else if (eddyData[0] == 0x20 && eddyData.length >= MIN_EDDY_TLM_ADV_LEN) {
                    nAdvType = KBAdvType.KBAdvTypeEddyTLM;
                } else if (eddyData[0] == 0x21 && eddyData.length >= MIN_SENSOR_ADV_LEN) {
                    nAdvType = KBAdvType.KBAdvTypeSensor;
                }else{
                    nAdvType = KBAdvType.KBAdvTypeInvalid;
                }
            }
        }
        if ((filterAdvType & nAdvType) == 0)
        {
            return false;
        }

        byte[] byExtenData = record.getServiceData(KBUtility.PARCE_UUID_EXT_DATA);
        if (byExtenData != null && byExtenData.length > 2) {
            batteryPercent = (int) (byExtenData[0] & 0xFF);
            if (batteryPercent > 100){
                batteryPercent = 100;
            }
        }

        if (nAdvType != KBAdvType.KBAdvTypeInvalid) {
            String strAdvTypeKey = String.valueOf(nAdvType);
            KBAdvPacketBase advPacket = mAdvPackets.get(strAdvTypeKey);
            boolean bNewObj = false;
            if (advPacket == null) {
                Class classNewObj = kbAdvPacketTypeObjects.get(strAdvTypeKey);
                try {
                    if (classNewObj != null) {
                        advPacket = (KBAdvPacketBase) classNewObj.newInstance();
                    }
                } catch (Exception excpt) {
                    excpt.printStackTrace();
                    Log.e(LOG_TAG, "create adv packet class failed");
                    return false;
                }
                bNewObj = true;
            }

            if (advPacket != null && advPacket.parseAdvPacket(beaconData)) {
                advPacket.updateBasicInfo(rssi);
                if (bNewObj) {
                    mAdvPackets.put(strAdvTypeKey, advPacket);
                }
                bParseDataRslt = true;
            }
        }

        return bParseDataRslt;
    }
}
