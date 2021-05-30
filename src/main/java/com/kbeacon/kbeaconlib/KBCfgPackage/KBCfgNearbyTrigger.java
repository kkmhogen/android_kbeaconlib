package com.kbeacon.kbeaconlib.KBCfgPackage;

import android.util.Log;
import com.kbeacon.kbeaconlib.KBAdvPackage.KBAdvType;
import com.kbeacon.kbeaconlib.KBException;
import com.kbeacon.kbeaconlib.UTCTime;

import java.util.HashMap;

public class KBCfgNearbyTrigger extends KBCfgTrigger {
    private static final String LOG_TAG = "KBCfgNearbyTrigger";
    public static final int ALM_DISTANCE_NEAY = 13;
    public static final int ALM_DISTANCE_MIDDLE = 20;
    public static final int ALM_DISTANCE_FARWAY = 35;
    public static final int NB_RANGE_MODE_RSSI = 0;
    public static final int NB_RANGE_MODE_DM = 1;

    public static final String JSON_FIELD_NEARBY_SCAN_INTERVAL = "nSTvl";
    public static final String JSON_FIELD_NEARBY_SCAN_WINDOW = "nSWin";
    public static final String JSON_FIELD_NEARBY_ADV_TX_PWR = "nAdPwr";
    public static final String JSON_FIELD_NEARBY_ADV_INTERVAL = "nAdTvl";
    public static final String JSON_FIELD_NEARBY_ALM_INTERVAL = "nAlTvl";
    public static final String NEARBY_ALM_WINDOW = "nAlWin";
    public static final String JSON_FIELD_NEARBY_ALM_FACTORY = "nAlFt";
    public static final String JSON_FIELD_NEARBY_ALM_DISTANCE = "nAlDs";
    public static final String JSON_FIELD_NEARBY_SLEEP_TIME = "nSTm";
    public static final String JSON_FIELD_NEARBY_GROUP_ID = "nbGid";
    public static final String JSON_FIELD_NEARBY_RANGE_MODE = "nbMode";

    private Float nbScanInterval;

    private Float nbScanWindow;

    private Integer nbAdvTxPower;

    private Float nbAdvInterval;

    private Integer nbALmInterval;

    private Integer nbALmDuration;

    private Integer nbAlmFactory;

    private Integer nbAlmDistance;

    private Long nbSleepTime;

    private Integer nbRangeMode;

    private Integer nbGroupID;

    public static final int KBTriggerAliveAdvOnlyMode = 0x3;  //always advertisement
    public static final int KBTriggerNoAdvNoAliveAdvMode = 0x4;  //always advertisement

    public void setNbGroupID(Integer nGroupID) throws KBException{
        if (nGroupID >= 0 && nGroupID <= 4095) {
            this.nbGroupID = nGroupID;
        }else{
            throw new KBException(KBException.KBEvtCfgInputInvalid, "nearby group ID invalid");
        }
    }

    public Integer getNbGroupID()
    {
        return nbGroupID;
    }

    public void setNbRangeMode(Integer nRangeMode) throws KBException{
        if (nRangeMode == NB_RANGE_MODE_DM || nRangeMode == NB_RANGE_MODE_RSSI) {
            this.nbRangeMode =  nRangeMode;
        }else{
            throw new KBException(KBException.KBEvtCfgInputInvalid, "nearby range mode invalid");
        }
    }

    public Integer getNbRangeMode()
    {
        return nbRangeMode;
    }


    public void setNbScanInterval(Float nbScanInterval) throws KBException{
        if (nbScanInterval <= 10000 && nbScanInterval >= 20) {
            this.nbScanInterval = nbScanInterval;
        }else{
            throw new KBException(KBException.KBEvtCfgInputInvalid, "nearby scan interval invalid");
        }
    }

    public Integer getNbAlmDistance() {
        return nbAlmDistance;
    }

    public void setNbScanWindow(Float nbScanWindow) throws KBException
    {
        if (nbScanWindow <= 10000 && nbScanWindow >= 20) {
            this.nbScanWindow = nbScanWindow;
        }else{
            throw new KBException(KBException.KBEvtCfgInputInvalid, "nearby scan window invalid");
        }
    }

    public void setNbAdvTxPower(Integer nbAdvTxPower)
    {
        this.nbAdvTxPower = nbAdvTxPower;
    }

    public void setNbAdvInterval(Float nbAdvInterval) throws KBException
    {
        if (nbAdvInterval <= 10000 && nbAdvInterval >= 20) {
            this.nbAdvInterval = nbAdvInterval;
        }else{
            throw new KBException(KBException.KBEvtCfgInputInvalid, "nearby adv interval invalid");
        }
    }

    //uint is 100ms
    public void setNbALmInterval(Integer nbALmInterval) throws KBException
    {
        Integer nDeviceAlmItvl = nbALmInterval / 100;
        if (nDeviceAlmItvl > 0 && nDeviceAlmItvl < 200)
        {
            this.nbALmInterval = nDeviceAlmItvl;
        }
        else
        {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "nearby alarm interval invalid");
        }
    }

    public Integer getNbAlmInterval() {
        return nbALmInterval* 100;
    }

    //uint is 10ms
    public void setNbALmDuration(Integer nbALmDuration) throws KBException
    {
        Integer nDeviceAlmDuration = nbALmDuration / 10;
        if (nDeviceAlmDuration > 0 && nDeviceAlmDuration < 200)
        {
            this.nbALmDuration = nDeviceAlmDuration;
        }
        else
        {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "nearby alarm interval invalid");
        }
    }

    public Integer getNbALmDuration() {
        return nbALmDuration * 10;
    }

    public void setNbAlmDistance(Integer nbAlmDistance) {
        this.nbAlmDistance = nbAlmDistance;
    }

    public void setNbAlmFactory(Integer nbAlmFactory) throws KBException
    {
        if (nbAlmFactory > 30 || nbAlmFactory < 10)
        {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "nearby alarm interval invalid");
        }
        this.nbAlmFactory = nbAlmFactory;
    }

    public void setNbSleepTime(KBCfgSleepTime sleepTime) throws KBException
    {
        if (sleepTime.mSleepStartHour > 24 || sleepTime.mSleepStartMinute > 59 ||
                sleepTime.mSleepEndHour > 24 || sleepTime.mSleepEndMinute > 59)
        {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "nearby alarm interval invalid");
        }

        long nSleepTime = 0;
        if (!(sleepTime.mSleepEndHour == sleepTime.mSleepStartHour && sleepTime.mSleepEndMinute == sleepTime.mSleepStartMinute))
        {
            UTCTime utcStart = UTCTime.getUTCFromLocalTime((int) sleepTime.mSleepStartHour, (int) sleepTime.mSleepStartMinute, 0);
            nSleepTime = (byte) utcStart.mHours;
            nSleepTime = (nSleepTime << 8);
            nSleepTime += (byte) utcStart.mMinutes;
            nSleepTime = (nSleepTime << 8);

            UTCTime utcStop = UTCTime.getUTCFromLocalTime((int) sleepTime.mSleepEndHour, (int) sleepTime.mSleepEndMinute, 0);
            nSleepTime += (byte) utcStop.mHours;
            nSleepTime = (nSleepTime << 8);
            nSleepTime += (byte) utcStop.mMinutes;

            Log.v(LOG_TAG, String.format("set sleep time to:%d,sh:%d, sm:%d, eh:%d, em:%d", nSleepTime, utcStart.mHours,
                    utcStart.mMinutes, utcStop.mHours, utcStop.mMinutes));
        }

        this.nbSleepTime = nSleepTime;
    }

    public boolean isNBSleepEnable()
    {
        int sleepStart = (int)((this.nbSleepTime >> 16) & 0xFFFF);
        int sleepEnd = (int)(this.nbSleepTime & 0xFFFF);
        if (sleepStart == sleepEnd)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public Integer getNbAdvTxPower() {
        return nbAdvTxPower;
    }


    public KBCfgSleepTime getNBSleepTime()
    {
        KBCfgSleepTime sleepTime = new KBCfgSleepTime();
        sleepTime.mSleepStartHour = (byte)((this.nbSleepTime >> 24) & 0xFF);
        sleepTime.mSleepStartMinute = (byte)((this.nbSleepTime >> 16) & 0xFF);
        sleepTime.mSleepEndHour = (byte)((this.nbSleepTime >> 8) & 0xFF);
        sleepTime.mSleepEndMinute = (byte)(this.nbSleepTime & 0xFF);

        UTCTime localStart = UTCTime.getLocalTimeFromUTC(sleepTime.mSleepStartHour, sleepTime.mSleepStartMinute, 0);
        UTCTime localEnd = UTCTime.getLocalTimeFromUTC(sleepTime.mSleepEndHour, sleepTime.mSleepEndMinute, 0);
        sleepTime.mSleepStartHour = (byte)localStart.mHours;
        sleepTime.mSleepStartMinute = (byte)localStart.mMinutes;
        sleepTime.mSleepEndHour = (byte)localEnd.mHours;
        sleepTime.mSleepEndMinute = (byte)localEnd.mMinutes;

        return sleepTime;
    }

    public void setTriggerAdvMode(Integer triggerAdvMode) throws KBException{
        if (triggerAdvMode == KBTriggerNoAdvNoAliveAdvMode
                || triggerAdvMode == KBTriggerAliveAdvOnlyMode) {
            this.triggerAdvMode = triggerAdvMode;
        }else{
            throw new KBException(KBException.KBEvtCfgInputInvalid, "adv type invalid");
        }
    }

    public void setTriggerAdvType(Integer triggerAdvType) throws KBException {
        if (triggerAdvType == KBAdvType.KBAdvTypeIBeacon) {
            this.triggerAdvType = triggerAdvType;
        } else {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "adv type invalid");
        }
    }

    public int updateConfig(HashMap<String, Object> dicts)
    {
        int nUpdateParaNum = super.updateConfig(dicts);

        Integer nTempValue = null;
        Float fTempValue = null;

        fTempValue = parseFloat(dicts.get(JSON_FIELD_NEARBY_SCAN_INTERVAL));
        if (fTempValue != null)
        {
            nbScanInterval = fTempValue;
            nUpdateParaNum++;
        }

        fTempValue = parseFloat(dicts.get(JSON_FIELD_NEARBY_SCAN_WINDOW));
        if (fTempValue != null)
        {
            nbScanWindow = fTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_NEARBY_ADV_TX_PWR);
        if (nTempValue != null)
        {
            nbAdvTxPower = nTempValue;
            nUpdateParaNum++;
        }

        fTempValue = parseFloat(dicts.get(JSON_FIELD_NEARBY_ADV_INTERVAL));
        if (fTempValue != null)
        {
            nbAdvInterval = fTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_NEARBY_ALM_INTERVAL);
        if (nTempValue != null)
        {
            nbALmInterval = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(NEARBY_ALM_WINDOW);
        if (nTempValue != null)
        {
            nbALmDuration = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_NEARBY_ALM_FACTORY);
        if (nTempValue != null)
        {
            nbAlmFactory = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_NEARBY_ALM_DISTANCE);
        if (nTempValue != null)
        {
            nbAlmDistance = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_NEARBY_RANGE_MODE);
        if (nTempValue != null)
        {
            nbRangeMode = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_NEARBY_GROUP_ID);
        if (nTempValue != null)
        {
            nbGroupID = nTempValue;
            nUpdateParaNum++;
        }

        Long lnTempValue = parseLong(dicts.get(JSON_FIELD_NEARBY_SLEEP_TIME));
        if (lnTempValue != null)
        {
            nbSleepTime = lnTempValue;
            nUpdateParaNum++;
        }

        return nUpdateParaNum;
    }

    public HashMap<String, Object> toDictionary()
    {
        HashMap<String, Object>cfgDicts = super.toDictionary();

        if (nbScanInterval != null)
        {
            cfgDicts.put(JSON_FIELD_NEARBY_SCAN_INTERVAL, nbScanInterval);
        }

        if (nbScanWindow != null)
        {
            cfgDicts.put(JSON_FIELD_NEARBY_SCAN_WINDOW, nbScanWindow);
        }

        if (nbAdvTxPower != null)
        {
            cfgDicts.put(JSON_FIELD_NEARBY_ADV_TX_PWR, nbAdvTxPower);
        }

        if (nbAdvInterval != null)
        {
            cfgDicts.put(JSON_FIELD_NEARBY_ADV_INTERVAL, nbAdvInterval);
        }

        if (nbALmInterval != null)
        {
            cfgDicts.put(JSON_FIELD_NEARBY_ALM_INTERVAL, nbALmInterval);
        }

        if (nbALmDuration != null)
        {
            cfgDicts.put(NEARBY_ALM_WINDOW, nbALmDuration);
        }

        if (nbAlmFactory != null)
        {
            cfgDicts.put(JSON_FIELD_NEARBY_ALM_FACTORY, nbAlmFactory);
        }

        if (nbAlmDistance != null)
        {
            cfgDicts.put(JSON_FIELD_NEARBY_ALM_DISTANCE, nbAlmDistance);
        }

        if (nbGroupID != null)
        {
            cfgDicts.put(JSON_FIELD_NEARBY_GROUP_ID, nbGroupID);
        }

        if (nbRangeMode != null)
        {
            cfgDicts.put(JSON_FIELD_NEARBY_RANGE_MODE, nbRangeMode);
        }

        if (nbSleepTime != null)
        {
            cfgDicts.put(JSON_FIELD_NEARBY_SLEEP_TIME, nbSleepTime);
        }

        return cfgDicts;
    }
}
