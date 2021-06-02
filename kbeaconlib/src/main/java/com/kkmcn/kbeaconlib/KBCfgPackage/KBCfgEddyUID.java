package com.kkmcn.kbeaconlib.KBCfgPackage;

import com.kkmcn.kbeaconlib.KBException;
import com.kkmcn.kbeaconlib.KBUtility;

import java.util.HashMap;

public class KBCfgEddyUID extends KBCfgBase
{
    public final static String JSON_FIELD_EDDY_UID_NID  = "nid";
    public final static String JSON_FIELD_EDDY_UID_SID  = "sid";

    private String nid;

    private String sid;


    public int cfgParaType()
    {
        return KBCfgType.KBConfigTypeEddyUID;
    }

    public String getNid()
    {
        return nid;
    }

    public String getSid()
    {
        return sid;
    }

    public void setNid(String strNid) throws KBException
    {
        if (strNid.length() == 22 && KBUtility.isHexString(strNid))
        {
            nid = strNid;
        }
        else
        {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "nid invalid");
        }
    }

    public void setSid(String strSid) throws KBException {
        if (strSid.length() == 14 && KBUtility.isHexString(strSid)) {
            sid = strSid;
        } else {
            throw new KBException(KBException.KBEvtCfgInputInvalid, "sid invalid");
        }
    }


    public int updateConfig(HashMap<String, Object>dicts)
    {
        int nUpdatePara = 0;

        if (dicts.get(JSON_FIELD_EDDY_UID_NID) != null)
        {
            nid = (String)dicts.get(JSON_FIELD_EDDY_UID_NID);
            nUpdatePara++;
        }

        if (dicts.get(JSON_FIELD_EDDY_UID_SID) != null)
        {
            sid = (String)dicts.get(JSON_FIELD_EDDY_UID_SID);
            nUpdatePara++;
        }

        return nUpdatePara;
    }

    public HashMap<String, Object> toDictionary()
    {
        HashMap<String, Object> cfgDicts = new HashMap<>(2);

        if (nid != null)
        {
            cfgDicts.put(JSON_FIELD_EDDY_UID_NID, nid);
        }

        if (sid != null)
        {
            cfgDicts.put(JSON_FIELD_EDDY_UID_SID, sid);
        }

        return cfgDicts;
    }
}
