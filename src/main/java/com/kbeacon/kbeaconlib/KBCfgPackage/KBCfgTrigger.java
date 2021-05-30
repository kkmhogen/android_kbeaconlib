package com.kbeacon.kbeaconlib.KBCfgPackage;

import com.kbeacon.kbeaconlib.KBAdvPackage.KBAdvType;
import com.kbeacon.kbeaconlib.KBException;

import java.util.HashMap;

public class KBCfgTrigger extends KBCfgBase {
    //trigger type
    public static final int KBTriggerTypeMotion = 0x1;   //motion trigger
    public static final int KBTriggerTypeButton = 0x2;   //push button trigger
    public static final int KBTriggerTypeNearby = 0x4;   //nearby trigger
    public static final int KBTriggerTypeHumidity = 0x8;   //humidity trigger

    //action option
    public static final int KBTriggerActionOff = 0x0;    //disable trigger
    public static final int KBTriggerActionAdv = 0x1;    //start advertisement when trigger event happened
    public static final int KBTriggerActionAlert = 0x2;  //start beep led flash when trigger event happened
    public static final int KBTriggerActionRecord = 0x4;
    public static final int KBTriggerActionVibration = 0x8;
    public static final int KBTriggerActionRptApp = 0x10;

    //push button trigger para
    public static final int KBTriggerBtnHold = 0x1;
    public static final int KBTriggerBtnSingleClick = 0x2;
    public static final int KBTriggerBtnDoubleClick = 0x4;
    public static final int KBTriggerBtnTripleClick = 0x8;

    //motion trigger sensitive
    public static final int KBTriggerMotionDefaultSensitive = 0x2;   //default motion sensitive

    //trigger advertisement mode
    public static final int KBTriggerAdvOnlyMode = 0x0;    //only advertisement when trigger event happened
    public static final int KBTriggerAdv2AliveMode = 0x1;  //always advertisement
    public static final float KBTriggerAdvIntervalDefault = 400;  //default trigger advertisement interval

    public static final String JSON_FIELD_TRIGGER_TYPE = "trType";
    public static final String JSON_FIELD_TRIGGER_ACTION = "trAct";
    public static final String JSON_FIELD_TRIGGER_PARA = "trPara";
    public static final String JSON_FIELD_TRIGGER_ADV_MODE = "trAMode";
    public static final String JSON_FIELD_TRIGGER_ADV_TYPE = "trAType";
    public static final String JSON_FIELD_TRIGGER_ADV_TIME = "trATm";
    public static final String JSON_FIELD_TRIGGER_ADV_INTERVAL = "trAPrd";

    protected Integer triggerType;

    protected Integer triggerAction;

    protected Integer triggerPara;

    protected Integer triggerAdvMode;

    protected Integer triggerAdvType;

    protected Integer triggerAdvTime;

    //Beacon alive advertisement interval
    protected Float triggerAdvInterval;

    public Integer getTriggerType()
    {
        return triggerType;
    }

    public Integer getTriggerAction()
    {
        return triggerAction;
    }

    public Integer getTriggerPara()
    {
        return triggerPara;
    }

    public Integer getTriggerAdvMode()
    {
        return triggerAdvMode;
    }

    public Integer getTriggerAdvType()
    {
        return triggerAdvType;
    }

    public Integer getTriggerAdvTime()
    {
        return triggerAdvTime;
    }

    public Float getTriggerAdvInterval()
    {
        return triggerAdvInterval;
    }

    public int cfgParaType()
    {
        return KBCfgType.KBConfigTypeTrigger;
    }


    public void setTriggerAction(Integer triggerAction) throws KBException{
        int nTriggerActionMask = KBTriggerActionOff | KBTriggerActionAdv | KBTriggerActionAlert | KBTriggerActionRecord | KBTriggerActionVibration | KBTriggerActionRptApp;
        if (triggerAction == 0 || (triggerAction & nTriggerActionMask) > 0) {
            this.triggerAction = triggerAction;
        }else{
            throw new KBException(KBException.KBEvtCfgInputInvalid, "trigger action invalid");
        }
    }

    public void setTriggerType(Integer triggerType){
        this.triggerType = triggerType;
    }

    public void setTriggerPara(Integer triggerPara) {
        this.triggerPara = triggerPara;
    }

    public void setTriggerAdvMode(Integer triggerAdvMode) throws KBException{
        if (triggerAdvMode == KBTriggerAdvOnlyMode
        || triggerAdvMode == KBTriggerAdv2AliveMode) {
            this.triggerAdvMode = triggerAdvMode;
        }else{
            throw new KBException(KBException.KBEvtCfgInputInvalid, "adv type invalid");
        }
    }

    public void setTriggerAdvType(Integer triggerAdvType) throws KBException {
        if (triggerAdvType == KBAdvType.KBAdvTypeEddyTLM
                || triggerAdvType == KBAdvType.KBAdvTypeEddyUID
                || triggerAdvType == KBAdvType.KBAdvTypeEddyURL
                || triggerAdvType == KBAdvType.KBAdvTypeSensor
                || triggerAdvType == KBAdvType.KBAdvTypeIBeacon)
        {
            this.triggerAdvType = triggerAdvType;
        }else{
            throw new KBException(KBException.KBEvtCfgInputInvalid, "adv type invalid");
        }
    }
    public void setTriggerAdvTime(Integer triggerAdvTime) throws KBException{
        if (triggerAdvTime >= 10) {
            this.triggerAdvTime = triggerAdvTime;
        }else{
            throw new KBException(KBException.KBEvtCfgInputInvalid, "adv time invalid");
        }
    }

    public void setTriggerAdvInterval(Float triggerAdvInterval) throws KBException{
        if (triggerAdvInterval <= 10000 && triggerAdvInterval >= 100) {
            this.triggerAdvInterval = triggerAdvInterval;
        }else{
            throw new KBException(KBException.KBEvtCfgInputInvalid, "adv time interval invalid");
        }
    }

    public int updateConfig(HashMap<String, Object> dicts)
    {
        int nUpdateParaNum = 0;
        Integer nTempValue = null;

        nTempValue = (Integer)dicts.get(JSON_FIELD_TRIGGER_TYPE);
        if (nTempValue != null)
        {
            triggerType = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_TRIGGER_ACTION);
        if (nTempValue != null)
        {
            triggerAction = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_TRIGGER_PARA);
        if (nTempValue != null)
        {
            triggerPara = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_TRIGGER_ADV_MODE);
        if (nTempValue != null)
        {
            triggerAdvMode = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_TRIGGER_ADV_TYPE);
        if (nTempValue != null)
        {
            triggerAdvType = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_TRIGGER_ADV_TIME);
        if (nTempValue != null)
        {
            triggerAdvTime = nTempValue;
            nUpdateParaNum++;
        }

        Float nTempFloat = parseFloat(dicts.get(JSON_FIELD_TRIGGER_ADV_INTERVAL));
        if (nTempFloat != null) {
                triggerAdvInterval = nTempFloat;
                nUpdateParaNum++;
        }

        return nUpdateParaNum;
    }

    public Long parseLong(Object nData)
    {
        Long parseData = null;
        if (nData != null) {
            if (nData instanceof Integer) {
                parseData = ((Integer) nData).longValue();
            } else if (nData instanceof Long) {
                parseData = (Long)nData;
            }
        }

        return parseData;
    }

    public Float parseFloat(Object oPeriodData)
    {
        Float parseData = null;
        if (oPeriodData != null) {
            if (oPeriodData instanceof Float) {
                parseData = (Float) oPeriodData;
            } else if (oPeriodData instanceof Double) {
                Double nPeriodFlt = (Double) oPeriodData;
                parseData = (float) nPeriodFlt.doubleValue();
            } else if (oPeriodData instanceof Integer) {
                Integer nPeriodInt = (Integer) oPeriodData;
                parseData = (float) nPeriodInt;
            }
        }

        return parseData;
    }

    public HashMap<String, Object> toDictionary()
    {
        HashMap<String, Object>cfgDicts = new HashMap<String, Object>(4);
        if (triggerType != null)
        {
            cfgDicts.put(JSON_FIELD_TRIGGER_TYPE, triggerType);
        }

        if (triggerAction != null)
        {
            cfgDicts.put(JSON_FIELD_TRIGGER_ACTION, triggerAction);
        }

        if (triggerPara != null)
        {
            cfgDicts.put(JSON_FIELD_TRIGGER_PARA, triggerPara);
        }

        if (triggerAdvMode != null)
        {
            cfgDicts.put(JSON_FIELD_TRIGGER_ADV_MODE, triggerAdvMode);
        }

        if (triggerAdvType != null)
        {
            cfgDicts.put(JSON_FIELD_TRIGGER_ADV_TYPE, triggerAdvType);
        }

        if (triggerAdvTime != null)
        {
            cfgDicts.put(JSON_FIELD_TRIGGER_ADV_TIME, triggerAdvTime);
        }

        if (triggerAdvInterval != null)
        {
            cfgDicts.put(JSON_FIELD_TRIGGER_ADV_INTERVAL, triggerAdvInterval);
        }

        return cfgDicts;
    }

}
