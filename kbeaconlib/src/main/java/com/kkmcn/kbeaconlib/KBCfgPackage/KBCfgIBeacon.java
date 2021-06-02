package com.kkmcn.kbeaconlib.KBCfgPackage;

import com.kkmcn.kbeaconlib.KBException;
import com.kkmcn.kbeaconlib.KBUtility;

import java.util.HashMap;

public class KBCfgIBeacon extends KBCfgBase{
    public final static String JSON_FIELD_IBEACON_UUID  = "uuid";
    public final static String JSON_FIELD_IBEACON_MAJORID  = "majorID";
    public final static String JSON_FIELD_IBEACON_MINORID = "minorID";

    private Integer majorID;

    private Integer minorID;

    private String uuid;

    public int cfgParaType()
    {
        return KBCfgType.KBConfigTypeIBeacon;
    }

    public String getUuid()
    {
        return uuid;
    }

    public Integer getMajorID()
    {
        return majorID;
    }

    public Integer getMinorID()
    {
        return minorID;
    }


    public void setMajorID(Integer nMajorID) throws KBException
    {
        if (nMajorID >= 0 && nMajorID <= 65535)
        {
            majorID = nMajorID;
        }
        else
        {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "majorID invalid");
        }
    }

    public void setMinorID(Integer nMinorID) throws KBException
    {
        if (nMinorID >= 0 && nMinorID <= 65535)
        {
            minorID = nMinorID;
        }
        else
        {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "minorID invalid");
        }
    }

    public void  setUuid(String strUuid) throws KBException
    {
        if (KBUtility.isUUIDString(strUuid) )
        {
            uuid = strUuid;
        }
        else
        {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "uuid invalid");
        }
    }

    public int updateConfig(HashMap<String, Object> dicts)
    {
        int nUpdateParaNum = 0;

        String strTempValue = (String)dicts.get(JSON_FIELD_IBEACON_UUID);
        if (strTempValue != null)
        {
            uuid = strTempValue;
            nUpdateParaNum++;
        }

        Integer nTempValue = (Integer)dicts.get(JSON_FIELD_IBEACON_MAJORID);
        if (nTempValue != null)
        {
            majorID = nTempValue;
            nUpdateParaNum++;
        }

        nTempValue = (Integer)dicts.get(JSON_FIELD_IBEACON_MINORID);
        if (nTempValue != null)
        {
            minorID = nTempValue;
            nUpdateParaNum++;
        }

        return nUpdateParaNum;
    }

    public HashMap<String, Object> toDictionary()
    {
        HashMap<String, Object>cfgDicts = new HashMap<String, Object>(4);

        if (uuid != null)
        {
            cfgDicts.put(JSON_FIELD_IBEACON_UUID, uuid);
        }

        if (majorID != null)
        {
            cfgDicts.put(JSON_FIELD_IBEACON_MAJORID, majorID);
        }

        if (minorID != null)
        {
            cfgDicts.put(JSON_FIELD_IBEACON_MINORID, minorID);
        }

        return cfgDicts;
    }
}
