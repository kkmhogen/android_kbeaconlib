package com.kbeacon.kbeaconlib.KBCfgPackage;

import com.kbeacon.kbeaconlib.KBException;

import java.util.HashMap;

public class KBCfgEddyURL extends KBCfgBase{

    public static final String JSON_FIELD_EDDY_URL_ADDR  = "url";

    private String url;

    public int cfgParaType()
    {
        return KBCfgType.KBConfigTypeEddyURL;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String  strUrl) throws KBException
    {
        strUrl = strUrl.replace(" ", "");
        if (strUrl.length() >= 3)
        {
            url = strUrl;
        }
        else
        {
            throw new KBException(KBException.KBEvtCfgInputInvalid,"url invalid");
        }
    }

    public int updateConfig(HashMap<String, Object> dicts)
    {
        if (dicts.get(JSON_FIELD_EDDY_URL_ADDR) != null)
        {
            url = (String)dicts.get(JSON_FIELD_EDDY_URL_ADDR);
            return 1;
        }

        return 0;
    }

    public HashMap<String, Object> toDictionary()
    {
        HashMap<String, Object> cfgDicts = new HashMap<>(1);
        if (url != null)
        {
            cfgDicts.put(JSON_FIELD_EDDY_URL_ADDR, url);
        }

        return cfgDicts;
    }
}
