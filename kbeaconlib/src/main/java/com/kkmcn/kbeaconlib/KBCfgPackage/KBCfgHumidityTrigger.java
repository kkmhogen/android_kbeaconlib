package com.kkmcn.kbeaconlib.KBCfgPackage;

import com.kkmcn.kbeaconlib.KBException;

import java.util.HashMap;

public class KBCfgHumidityTrigger extends KBCfgTrigger{
    private static final String LOG_TAG = "KBCfgHumidityTrigger";

    public static final int KBTriggerHTParaMaskTemperatureAbove = 0x1;
    public static final int KBTriggerHTParaMaskTemperatureBelow = 0x2;
    public static final int KBTriggerHTParaMaskHumidityAbove = 0x4;
    public static final int KBTriggerHTParaMaskHumidityBelow = 0x8;
    public static final int KBTriggerHTParaMaskRpt2App = 0x10;

    public static final int KBTriggerConditionDefaultTemperatureAbove = 60;
    public static final int KBTriggerConditionDefaultTemperatureBelow = -10;
    public static final int KBTriggerConditionDefaultHumidityAbove = 80;
    public static final int KBTriggerConditionDefaultHumidityBelow = 20;

    public static final String JSON_FIELD_TRIGGER_HUMIDITY_PARA_MASK = "htMsk";
    public static final String JSON_FIELD_TRIGGER_TEMPERATURE_ABOVE = "tpAbv";
    public static final String JSON_FIELD_TRIGGER_TEMPERATURE_BELOW = "tpBlw";
    public static final String JSON_FIELD_TRIGGER_HUMIDITY_ABOVE = "htAbv";
    public static final String JSON_FIELD_TRIGGER_HUMIDITY_BELOW = "htBlw";

    private Integer triggerHtParaMask;

    private Integer triggerTemperatureAbove;

    private Integer triggerTemperatureBelow;

    private Integer triggerHumidityAbove;

    private Integer triggerHumidityBelow;

    public Integer getTriggerHtParaMask() {
        return triggerHtParaMask;
    }

    public Integer getTriggerTemperatureAbove() {
        return triggerTemperatureAbove;
    }

    public Integer getTriggerTemperatureBelow() {
        return triggerTemperatureBelow;
    }

    public Integer getTriggerHumidityAbove() {
        return triggerHumidityAbove;
    }

    public Integer getTriggerHumidityBelow() {
        return triggerHumidityBelow;
    }

    public void setTriggerHtParaMask(Integer maskValue) {
        this.triggerHtParaMask = maskValue;
    }

    public void setTriggerTemperatureAbove(Integer triggerThd) throws KBException{
        if (triggerThd > 1000 || triggerThd < -50)
        {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "temperature above threshold is invalid");
        }

        this.triggerTemperatureAbove = triggerThd;
    }

    public void setTriggerTemperatureBelow(Integer triggerThd) throws KBException{
        if (triggerThd > 1000 || triggerThd < -50)
        {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "temperature below threshold is invalid");
        }

        this.triggerTemperatureBelow = triggerThd;
    }

    public void setTriggerHumidityAbove(Integer triggerThd) throws KBException{
        if (triggerThd > 100 || triggerThd < 0)
        {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "humidity above threshold is invalid");
        }

        this.triggerHumidityAbove = triggerThd;
    }

    public void setTriggerHumidityBelow(Integer triggerThd) throws KBException{
        if (triggerThd > 100 || triggerThd < 0)
        {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "humidity above threshold is invalid");
        }

        this.triggerHumidityBelow = triggerThd;
    }

    public int updateConfig(HashMap<String, Object> dicts)
    {
        int nUpdateParaNum = super.updateConfig(dicts);
        Integer nTempValue = null;

        nTempValue = (Integer)dicts.get(JSON_FIELD_TRIGGER_HUMIDITY_PARA_MASK);
        if (nTempValue != null)
        {
            triggerHtParaMask = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_TRIGGER_TEMPERATURE_ABOVE);
        if (nTempValue != null)
        {
            triggerTemperatureAbove = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_TRIGGER_TEMPERATURE_BELOW);
        if (nTempValue != null)
        {
            triggerTemperatureBelow = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_TRIGGER_HUMIDITY_ABOVE);
        if (nTempValue != null)
        {
            triggerHumidityAbove = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_TRIGGER_HUMIDITY_BELOW);
        if (nTempValue != null)
        {
            triggerHumidityBelow = nTempValue;
            nUpdateParaNum++;
        }

        return nUpdateParaNum;
    }

    public HashMap<String, Object> toDictionary()
    {
        HashMap<String, Object>cfgDicts = super.toDictionary();

        if (triggerHtParaMask != null)
        {
            cfgDicts.put(JSON_FIELD_TRIGGER_HUMIDITY_PARA_MASK, triggerHtParaMask);
        }

        if (triggerTemperatureAbove != null)
        {
            cfgDicts.put(JSON_FIELD_TRIGGER_TEMPERATURE_ABOVE, triggerTemperatureAbove);
        }

        if (triggerTemperatureBelow != null)
        {
            cfgDicts.put(JSON_FIELD_TRIGGER_TEMPERATURE_BELOW, triggerTemperatureBelow);
        }

        if (triggerHumidityAbove != null)
        {
            cfgDicts.put(JSON_FIELD_TRIGGER_HUMIDITY_ABOVE, triggerHumidityAbove);
        }

        if (triggerHumidityBelow != null)
        {
            cfgDicts.put(JSON_FIELD_TRIGGER_HUMIDITY_BELOW, triggerHumidityBelow);
        }

        return cfgDicts;
    }
}