package com.kbeacon.kbeaconlib.KBCfgPackage;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class KBCfgBase{

    private final static String LOG_TAG = "KBCfgBase";

    public final static String JSON_MSG_TYPE_KEY = "msg";
    public final static String JSON_MSG_TYPE_CFG = "cfg";
    public final static String JSON_MSG_TYPE_GET_PARA = "getPara";
    public final static String JSON_MSG_CFG_SUBTYPE = "stype";


    public int cfgParaType()
    {
        return KBCfgType.KBConfigTypeInvalid;
    }

    public int updateConfig(HashMap<String, Object> dicts)
    {
        return 0;
    }

    public HashMap<String, Object> toDictionary()
    {
        return null;
    }

    public static void JsonString2HashMap(String  strJsonMsg, Map<String, Object> rstList) {
        JSONObject mRspJason;
        try
        {
            mRspJason = new JSONObject(strJsonMsg);
            JsonObject2HashMap(mRspJason, rstList);
        }
        catch(JSONException excp)
        {
            Log.e(LOG_TAG, "Parse Jason network command response failed");
        }
    }

    public static void JsonObject2HashMap(JSONObject jo, Map<String, Object> rstList) {
        for (Iterator<String> keys = jo.keys(); keys.hasNext();) {
            try {
                String key1 = keys.next();
                rstList.put(key1, jo.get(key1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void HashMap2JsonObject( Map<String, Object> paraMap, JSONObject jo) {
        for (Map.Entry entry : paraMap.entrySet()) {
            try {
                jo.put((String)entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static String HashMap2JsonString( Map<String, Object> paraMap) {
        JSONObject jsonObj = new JSONObject();
        KBCfgBase.HashMap2JsonObject(paraMap, jsonObj);
        if (jsonObj.length() > 0){
            return jsonObj.toString();
        }else{
            return null;
        }
    }
}
