package com.kbeacon.kbeaconlib.KBCfgPackage;

import com.kbeacon.kbeaconlib.KBException;
import com.kbeacon.kbeaconlib.KBAdvPackage.KBAdvType;

import java.util.ArrayList;
import java.util.HashMap;

public class KBCfgCommon extends KBCfgBase{
    public final static int  KB_CAPIBILITY_KEY = 0x1;
    public final static int  KB_CAPIBILITY_BEEP =  0x2;
    public final static int  KB_CAPIBILITY_ACC = 0x4;
    public final static int  KB_CAPIBILITY_TEMP = 0x8;
    public final static int  KB_CAPIBILITY_HUMIDITY = 0x10;
    public final static int  KB_CAPIBILITY_EDDY = 0x20;
    public final static int MAX_NAME_LENGTH = 18;

    public final static int MIN_TLM_INTERVAL = 2;
    public final static int MAX_TLM_INTERVAL = 255;

    public final static int ADV_FLAGS_CONNECTABLE = 0x1;
    public final static int ADV_FLAGS_AUTO_POWER_ON = 0x4;

    public final static String  JSON_FIELD_BEACON_MODLE = "modle";
    public final static String  JSON_FIELD_BEACON_VER = "ver";
    public final static String  JSON_FIELD_BEACON_HVER = "hver";
    public final static String  JSON_FIELD_DEV_NAME = "devName";
    public final static String  JSON_FIELD_TLM_ADV_INTERVAL = "tlmItvl";
    public final static String  JSON_FIELD_ADV_PERIOD = "advPrd";
    public final static String  JSON_FIELD_TX_PWR = "txPwr";
    public final static String  JSON_FIELD_MIN_TX_PWR = "minPwr";
    public final static String  JSON_FIELD_MAX_TX_PWR = "maxPwr";
    public final static String  JSON_FIELD_BASIC_CAPIBILITY = "bCap";
    public final static String  JSON_FIELD_TRIG_CAPIBILITY = "trCap";
    public final static String  JSON_FIELD_PWD = "pwd";
    public final static String  JSON_FIELD_MEA_PWR = "meaPwr";
    public final static String  JSON_FIELD_BEACON_TYPE = "type";
    public final static String  JSON_FIELD_ADV_FLAG = "advFlag";

    //basic capiblity
    private Integer basicCapibility;

    private Integer trigCapibility;

    private Integer maxTxPower;

    private Integer minTxPower;

    private String model;

    private String version;

    private String hversion;

    private String advTypeString;

    ////////////////////can be configruation able///////////////////////
    private Integer txPower;

    private Integer refPower1Meters;

    private Integer tlmAdvInterval;

    private Float advPeriod;

    private String password;

    private String name;

    private Integer advType; //beacon type (iBeacon, Eddy TLM/UID/ etc.,)

    private Integer autoAdvAfterPowerOn; //beacon automatic start advertisement after powen on

    private Integer advConnectable; //is beacon can be connectable

    //adv type
    private Integer mAdvFlag;


    //basic capibility
    public Integer getBasicCapability()
    {
        return basicCapibility;
    }

    //is the device support iBeacon
    public boolean isSupportIBeacon()
    {
        int nAdvCapibility = (basicCapibility >> 8);
        return ((nAdvCapibility & KBAdvType.KBAdvTypeIBeacon) > 0);
    }

    //is the device support URL
    public boolean isSupportEddyURL()
    {
        int nAdvCapibility = (basicCapibility >> 8);
        return ((nAdvCapibility & KBAdvType.KBAdvTypeEddyURL) > 0);
    }

    //is the device support TLM
    public boolean isSupportEddyTLM()
    {
        int nAdvCapibility = (basicCapibility >> 8);
        return ((nAdvCapibility & KBAdvType.KBAdvTypeEddyTLM) > 0);
    }

    //is the device support UID
    public boolean isSupportEddyUID()
    {
        int nAdvCapibility = (basicCapibility >> 8);
        return ((nAdvCapibility & KBAdvType.KBAdvTypeEddyUID) > 0);
    }

    //support kb sensor
    public boolean isSupportKBSensor()
    {
        int nAdvCapibility = (basicCapibility >> 8);
        return ((nAdvCapibility & KBAdvType.KBAdvTypeSensor) > 0);
    }

    //is support button
    public boolean isSupportButton()
    {
        return ((basicCapibility & 0x1) > 0);
    }

    //is support beep
    public boolean isSupportBeep()
    {
        return ((basicCapibility & 0x2) > 0);
    }

    //is support acc sensor
    public boolean isSupportAccSensor()
    {
        return ((basicCapibility & 0x4) > 0);
    }

    //is support humidity sensor
    public boolean isSupportHumiditySensor()
    {
        return ((basicCapibility & 0x8) > 0);
    }

    //is support DM distancing
    public boolean isSupportEnhancedProximity()
    {
        return ((basicCapibility & 0x10) > 0);
    }

    //is support DM distancing
    public boolean isSupportDM()
    {
        return ((basicCapibility & 0x20) > 0);
    }

    public Integer getTrigCapability()
    {
        return trigCapibility;
    }

    public Integer getMaxTxPower()
    {
        return maxTxPower;
    }

    public Integer getMinTxPower()
    {
        return minTxPower;
    }

    public Integer getTxPower()
    {
        return txPower;
    }

    public Integer getRefPower1Meters()
    {
        return refPower1Meters;
    }

    public Float getAdvPeriod()
    {
        return advPeriod;
    }

    public Integer getTLMAdvInterval()
    {
        //compitable old version beacon
        if (tlmAdvInterval == null)
        {
            return 8;
        }
        return tlmAdvInterval;
    }

    public Integer getAdvType()
    {
        return advType;
    }

    public Integer getAutoAdvAfterPowerOn()
    {
        return autoAdvAfterPowerOn;
    }

    public Integer getAdvConnectable()
    {
        return advConnectable;
    }

    public String getModel()
    {
        return model;
    }

    public String getVersion()
    {
        return version;
    }

    public String getHardwareVersion()
    {
        return hversion;
    }

    public String getAdvTypeString()
    {
        return advTypeString;
    }

    public String getName()
    {
        return name;
    }

    public KBCfgCommon()
    {
    }

    public ArrayList<Integer> getSupportedAdvTypeArray()
    {
        ArrayList<Integer> advTypeArray = new ArrayList<>(4);
        advTypeArray.add(KBAdvType.KBAdvTypeIBeacon);
        advTypeArray.add(KBAdvType.KBAdvTypeEddyURL);
        advTypeArray.add(KBAdvType.KBAdvTypeEddyUID);
        advTypeArray.add(KBAdvType.KBAdvTypeEddyTLM);

        if (getSupportedSensorArray().size() > 0) {
            advTypeArray.add(KBAdvType.KBAdvTypeSensor);
        }

        return advTypeArray;
    }

    public ArrayList<Integer> getSupportedSensorArray()
    {
        ArrayList<Integer> sensorArray = new ArrayList<>(3);
        if (isSupportHumiditySensor()){
            sensorArray.add(KB_CAPIBILITY_HUMIDITY);
        }
        if (isSupportAccSensor()){
            sensorArray.add(KB_CAPIBILITY_ACC);
        }

        return sensorArray;
    }


    public int cfgParaType()
    {
        return KBCfgType.KBConfigTypeCommon;
    }

    //update KBeacon tx power
    public void setTxPower(Integer nTxPower) throws KBException
    {
        if (nTxPower >= -40 && nTxPower <= 5) {
            txPower = nTxPower;
        } else {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "invalid tx power data");
        }
    }

    public void setTLMAdvInterval(Integer nTLMInterval) throws KBException
    {
        if (nTLMInterval >= MIN_TLM_INTERVAL && nTLMInterval <= MAX_TLM_INTERVAL) {
            tlmAdvInterval = nTLMInterval;
        } else {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "invalid tlm interval data");
        }
    }

    public void setRefPower1Meters( Integer nRefPower1Meters) throws  KBException {
        if (nRefPower1Meters < -10 && nRefPower1Meters > -100) {
            refPower1Meters = nRefPower1Meters;
        } else {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "referance power invalid");
        }
    }

    public void setAdvPeriod(Float nAdvPeriod) throws KBException
    {
        if ((nAdvPeriod <= 10000.0 && nAdvPeriod >= 100.0)
                || nAdvPeriod == 0.0) {
            advPeriod = nAdvPeriod;
        } else {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "adv period invalid");
        }
    }

    public void setPassword(String strPwd) throws KBException {
        if (strPwd.length() >= 8 && strPwd.length() <= 16) {
            password = strPwd;
        } else {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "password length invalid");
        }
    }

    public void setName(String strName) throws KBException {
        if (strName.length() <= MAX_NAME_LENGTH) {
            name = strName;
        } else {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "name length invalid");
        }
    }

    public void setAdvType(Integer nAdvType) throws  KBException {
        if ((nAdvType & KBAdvType.KBAdvTypeSensor) == 0
                && (nAdvType & KBAdvType.KBAdvTypeEddyUID) == 0
                && (nAdvType & KBAdvType.KBAdvTypeIBeacon) == 0
                && (nAdvType & KBAdvType.KBAdvTypeEddyTLM) == 0
                && (nAdvType & KBAdvType.KBAdvTypeEddyURL) == 0) {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "adv type invalid");
        } else {
            advType = nAdvType;
        }
    }

    public void setAutoAdvAfterPowerOn(Integer nAutoAdvAfterPowerOn) throws KBException {
        if (nAutoAdvAfterPowerOn == 0
                || nAutoAdvAfterPowerOn == 1) {
            autoAdvAfterPowerOn = nAutoAdvAfterPowerOn;

            if (mAdvFlag == null){
                mAdvFlag = 0;
            }

            if (autoAdvAfterPowerOn > 0) {
                mAdvFlag = mAdvFlag | ADV_FLAGS_AUTO_POWER_ON;
            } else {
                mAdvFlag = mAdvFlag & ((~ADV_FLAGS_AUTO_POWER_ON) & 0xFF);
            }
        } else {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "advertisement type invalid");
        }
    }

    public void setAdvFlags(int nAdvFlags)
    {
        mAdvFlag = nAdvFlags;
        advConnectable = ((mAdvFlag & ADV_FLAGS_CONNECTABLE) > 0) ? 0x1 : 0x0;
        autoAdvAfterPowerOn = ((mAdvFlag & ADV_FLAGS_AUTO_POWER_ON) > 0) ? 0x1 : 0x0;
    }

    public Integer getAdvFlags()
    {
        return mAdvFlag;
    }

    public void setAdvConnectable(Integer nAdvConnectable) throws KBException {
        if (nAdvConnectable == 0
                || nAdvConnectable == 1) {
            advConnectable = nAdvConnectable;

            if (mAdvFlag == null){
                mAdvFlag = 0;
            }
            if (nAdvConnectable > 0) {
                mAdvFlag = mAdvFlag | ADV_FLAGS_CONNECTABLE;
            } else {
                mAdvFlag = mAdvFlag & ((~ADV_FLAGS_CONNECTABLE) & 0xFF);
            }
        } else {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "advertisement connectable invalid");
        }
    }

    public int updateConfig(HashMap<String, Object> dicts) {
        String strTempValue;
        int nUpdateParaNum = 0;

        strTempValue = (String) dicts.get(JSON_FIELD_BEACON_MODLE);
        if (strTempValue != null) {
            model = strTempValue;
            nUpdateParaNum++;
        }

        strTempValue = (String) dicts.get(JSON_FIELD_BEACON_VER);
        if (strTempValue != null) {
            version = strTempValue;
            nUpdateParaNum++;
        }

        strTempValue = (String) dicts.get(JSON_FIELD_BEACON_HVER);
        if (strTempValue != null) {
            hversion = strTempValue;
            nUpdateParaNum++;
        }

        Integer nTempValue = (Integer) dicts.get(JSON_FIELD_MAX_TX_PWR);
        if (nTempValue != null) {
            maxTxPower = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer) dicts.get(JSON_FIELD_MIN_TX_PWR);
        if (nTempValue != null) {
            minTxPower = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer) dicts.get(JSON_FIELD_BASIC_CAPIBILITY);
        if (nTempValue != null) {
            basicCapibility = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer) dicts.get(JSON_FIELD_TRIG_CAPIBILITY);
        if (nTempValue != null) {
            trigCapibility = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer) dicts.get(JSON_FIELD_TLM_ADV_INTERVAL);
        if (nTempValue != null) {
            tlmAdvInterval = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer) dicts.get(JSON_FIELD_ADV_FLAG);
        if (nTempValue != null) {
            mAdvFlag = nTempValue;
            advConnectable = ((mAdvFlag & 0x1) > 0) ? 0x1 : 0x0;
            nUpdateParaNum++;

            autoAdvAfterPowerOn = ((mAdvFlag & 0x4) > 0) ? 0x1 : 0x0;
        }

        Object oPeriodData = dicts.get(JSON_FIELD_ADV_PERIOD);
        if (oPeriodData != null) {
            if (oPeriodData instanceof Float){
                advPeriod = (Float) oPeriodData;
                nUpdateParaNum++;
            }
            else if (oPeriodData instanceof Double) {
                Double nPeriodFlt = (Double)oPeriodData;
                advPeriod = (float)nPeriodFlt.doubleValue();
                nUpdateParaNum++;
            }
            else if (oPeriodData instanceof Integer) {
                Integer nPeriodInt = (Integer)oPeriodData;
                advPeriod = (float)nPeriodInt;
                nUpdateParaNum++;
            }
        }

        strTempValue = (String) dicts.get(JSON_FIELD_DEV_NAME);
        if (strTempValue != null) {
            name = strTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer) dicts.get(JSON_FIELD_TX_PWR);
        if (nTempValue != null) {
            txPower = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer) dicts.get(JSON_FIELD_MEA_PWR);
        if (nTempValue != null) {
            refPower1Meters = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer) dicts.get(JSON_FIELD_BEACON_TYPE);
        if (nTempValue != null) {
            advType = nTempValue;
            nUpdateParaNum++;
        }

        return nUpdateParaNum;
    }

    public HashMap<String, Object> toDictionary()
    {
        HashMap<String, Object> configDicts = new HashMap<>(10);

        if (advType != null) {
            configDicts.put(JSON_FIELD_BEACON_TYPE, advType);
        }

        if (name != null) {
            configDicts.put(JSON_FIELD_DEV_NAME, name);
        }

        if (advPeriod != null) {
            configDicts.put(JSON_FIELD_ADV_PERIOD, advPeriod);
        }

        if (txPower != null) {
            configDicts.put(JSON_FIELD_TX_PWR, txPower);
        }

        if (refPower1Meters != null) {
            configDicts.put(JSON_FIELD_MEA_PWR, refPower1Meters);
        }

        if (tlmAdvInterval != null) {
            configDicts.put(JSON_FIELD_TLM_ADV_INTERVAL, tlmAdvInterval);
        }

        if (mAdvFlag != null)
        {
            configDicts.put(JSON_FIELD_ADV_FLAG, mAdvFlag);
        }

        //password
        if (password != null && password.length() >= 8 && password.length() <= 16) {
            configDicts.put(JSON_FIELD_PWD, password);
        }


        return configDicts;
    }

    public static String getAdvTypeString(int beaconAdvType) {
        int advType = beaconAdvType;

        ArrayList<String> nsBeaconTypeArray = new ArrayList<>(10);

        if ((advType & KBAdvType.KBAdvTypeIBeacon) > 0) {
            nsBeaconTypeArray.add(KBAdvType.KBAdvTypeIBeaconString);
        }
        if ((advType & KBAdvType.KBAdvTypeEddyURL) > 0) {
            nsBeaconTypeArray.add(KBAdvType.KBAdvTypeEddyURLString);
        }

        if ((advType & KBAdvType.KBAdvTypeEddyUID) > 0) {
            nsBeaconTypeArray.add(KBAdvType.KBAdvTypeEddyUIDString);
        }

        if ((advType & KBAdvType.KBAdvTypeEddyTLM) > 0) {
            nsBeaconTypeArray.add(KBAdvType.KBAdvTypeEddyTLMString);
        }

        if ((advType & KBAdvType.KBAdvTypeSensor) > 0) {
            nsBeaconTypeArray.add(KBAdvType.KBAdvTypeSensorString);
        }

        String strBeaconString = (nsBeaconTypeArray.size() > 0) ? nsBeaconTypeArray.get(0) : "null";

        for (int i = 1; i < nsBeaconTypeArray.size(); i++) {
            strBeaconString = strBeaconString +  ";" + nsBeaconTypeArray.get(i);
        }

        return strBeaconString;
    }
}
