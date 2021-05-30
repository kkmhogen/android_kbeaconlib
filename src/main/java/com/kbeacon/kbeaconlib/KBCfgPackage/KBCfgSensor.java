package com.kbeacon.kbeaconlib.KBCfgPackage;

import com.kbeacon.kbeaconlib.KBException;

import java.util.HashMap;

public class KBCfgSensor extends KBCfgBase{

    public static final String JSON_FIELD_SENSOR_TYPE = "sensor";

    public static final String SENSOR_TYPE_ACC_POSITION = "Acc";
    public static final String SENSOR_TYPE_HUMIDITY_2_TEMP = "Humidity";

    public static final String JSON_SENSOR_TYPE_HT_MEASURE_INTERVAL = "msItvl";
    public static final String JSON_SENSOR_TYPE_HT_TEMP_CHANGE_THD = "tsThd";
    public static final String JSON_SENSOR_TYPE_HT_HUMIDITY_CHANGE_THD = "hsThd";


    public static final int KBSensorTypeDisable = 0x0;
    public static final int KBSensorTypeAcc = 0x1;
    public static final int KBSensorTypeHumidity = 0x2;

    //configruation
    private Integer sensorType;

    //measure interval
    private Integer sensorHtMeasureInterval;

    //temperature interval
    private Integer temperatureChangeThreshold;

    //humidity interval
    private Integer humidityChangeThreshold;

    public int cfgParaType()
    {
        return KBCfgType.KBConfigTypeSensor;
    }

    public void setSensorType(Integer nSensorType) throws KBException
    {
        if ((nSensorType != KBSensorTypeDisable)
                && (nSensorType & KBSensorTypeAcc) == 0
                && (nSensorType & KBSensorTypeHumidity) == 0)
        {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "sensor type invalid");
        }
        else
        {
            sensorType = nSensorType;
        }
    }

    public Integer getSensorHtMeasureInterval()
    {
        return sensorHtMeasureInterval;
    }

    public  Integer getTemperatureChangeThreshold()
    {
        return temperatureChangeThreshold;
    }

    public Integer getHumidityChangeThreshold()
    {
        return humidityChangeThreshold;
    }

    public void setSensorHtMeasureInterval(Integer nMeasureInterval)
    {
        sensorHtMeasureInterval = nMeasureInterval;
    }

    public void setTemperatureChangeThreshold(Integer nSaveThd)
    {
        temperatureChangeThreshold = nSaveThd;
    }

    public void setHumidityChangeThreshold(Integer nSaveThd)
    {
        humidityChangeThreshold = nSaveThd;
    }

    public Integer getSensorType()
    {
        return sensorType;
    }

    public static String getSensorTypeString(Integer nSensorType)
    {
        String strSensorType = "";

        if ((nSensorType & 0x1) > 0)
        {
            strSensorType = strSensorType + SENSOR_TYPE_ACC_POSITION + "|";
        }

        if ((nSensorType & 0x2) > 0)
        {
            strSensorType = strSensorType + SENSOR_TYPE_HUMIDITY_2_TEMP + "|";
        }

        if (strSensorType.length() == 0)
        {
            return "none";
        }
        else
        {
            return strSensorType;
        }
    }

    public int updateConfig(HashMap<String,Object>dicts)
    {
        int nUpdateConfigNum = 0;
        Object obj;

        obj = dicts.get(JSON_FIELD_SENSOR_TYPE);
        if (obj != null)
        {
            sensorType = (Integer) obj;
            nUpdateConfigNum++;
        }

        obj = dicts.get(JSON_SENSOR_TYPE_HT_MEASURE_INTERVAL);
        if (obj != null)
        {
            sensorHtMeasureInterval = (Integer) obj;
            nUpdateConfigNum++;
        }

        obj = dicts.get(JSON_SENSOR_TYPE_HT_TEMP_CHANGE_THD);
        if (obj != null)
        {
            temperatureChangeThreshold = (Integer) obj;
            nUpdateConfigNum++;
        }

        obj = dicts.get(JSON_SENSOR_TYPE_HT_HUMIDITY_CHANGE_THD);
        if (obj != null)
        {
            humidityChangeThreshold = (Integer) obj;
            nUpdateConfigNum++;
        }

        return nUpdateConfigNum;
    }

    public HashMap<String, Object> toDictionary()
    {
        HashMap<String, Object> configDicts = new HashMap<>(2);

        if (sensorType != null)
        {
            configDicts.put(JSON_FIELD_SENSOR_TYPE, sensorType);
        }

        if (sensorHtMeasureInterval != null)
        {
            configDicts.put(JSON_SENSOR_TYPE_HT_MEASURE_INTERVAL, sensorHtMeasureInterval);
        }

        if (temperatureChangeThreshold != null)
        {
            configDicts.put(JSON_SENSOR_TYPE_HT_TEMP_CHANGE_THD, temperatureChangeThreshold);
        }

        if (humidityChangeThreshold != null)
        {
            configDicts.put(JSON_SENSOR_TYPE_HT_HUMIDITY_CHANGE_THD, humidityChangeThreshold);
        }

        return configDicts;
    }
}
