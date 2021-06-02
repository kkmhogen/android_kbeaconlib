package com.kkmcn.kbeaconlib.KBCfgPackage;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class KBCfgHandler {
    private final static String LOG_TAG = "KBCfgHandler";
    private HashMap<String, KBCfgBase> kbCfgPara;

    private static HashMap<String, Class> kbCfgTypeObjects;

    static
    {
        kbCfgTypeObjects = new HashMap<>(5);
        kbCfgTypeObjects.put(String.valueOf(KBCfgType.KBConfigTypeCommon), KBCfgCommon.class);
        kbCfgTypeObjects.put(String.valueOf(KBCfgType.KBConfigTypeEddyURL), KBCfgEddyURL.class);
        kbCfgTypeObjects.put(String.valueOf(KBCfgType.KBConfigTypeEddyUID), KBCfgEddyUID.class);
        kbCfgTypeObjects.put(String.valueOf(KBCfgType.KBConfigTypeIBeacon), KBCfgIBeacon.class);
        kbCfgTypeObjects.put(String.valueOf(KBCfgType.KBConfigTypeSensor), KBCfgSensor.class);
        kbCfgTypeObjects.put(String.valueOf(KBCfgType.KBConfigTypeTrigger), KBCfgSensor.class);
    }

    public KBCfgHandler()
    {
        kbCfgPara = new HashMap<>(6);
    }

    //get all configruation paramaters
    public KBCfgBase[] configParamaters()
    {
        if (kbCfgPara.size() == 0)
        {
            return null;
        }

        KBCfgBase[] cfgArrays = new KBCfgBase[kbCfgPara.size()];
        kbCfgPara.values().toArray(cfgArrays);
        return cfgArrays;
    }

    public KBCfgBase getConfigruationByType(int cfgType)
    {
        return kbCfgPara.get(String.valueOf(cfgType));
    }

    //init config paramaters when read para from kebeacon
    public void initConfigFromJsonString(String jsonMsg)
    {
        kbCfgPara.clear();
        ArrayList<KBCfgBase> objects = KBCfgHandler.jsonStringToObjects(jsonMsg);
        if (objects != null) {
            for (KBCfgBase obj : objects) {
                kbCfgPara.put(String.valueOf(obj.cfgParaType()), obj);
            }
        }
    }

    //init config paramaters when read para from kbeacon
    public void initConfigFromJsonDcits(HashMap<String,Object> dictPara)
    {
        kbCfgPara.clear();
        ArrayList<KBCfgBase> objects = KBCfgHandler.dictParaToObjects(dictPara);
        if (objects != null) {
            for (KBCfgBase obj : objects) {
                kbCfgPara.put(String.valueOf(obj.cfgParaType()), obj);
            }
        }
    }

    //update configruation
    public void updateConfig(ArrayList<KBCfgBase> newCfgArray)
    {
        for (KBCfgBase obj: newCfgArray)
        {
            HashMap<String, Object> updatePara = obj.toDictionary();
            if (updatePara == null || updatePara.size() == 0)
            {
                continue;
            }

            String strCfgParaType = String.valueOf(obj.cfgParaType());
            KBCfgBase kbCfgObj = kbCfgPara.get(strCfgParaType);
            if (kbCfgObj == null) {
                Class classObj = kbCfgTypeObjects.get(strCfgParaType);
                try {
                    if (classObj != null) {
                        kbCfgObj = (KBCfgBase) classObj.newInstance();
                        kbCfgPara.put(strCfgParaType, kbCfgObj);
                    }
                } catch (Exception excpt) {
                    excpt.printStackTrace();
                    Log.e(LOG_TAG, "create adv packet class failed");
                    return;
                }
            }
            if (kbCfgObj != null)
            {
                kbCfgObj.updateConfig(updatePara);
            }
        }
    }

    //translate object to json string for download to beacon
    public static String objectsToJsonString(ArrayList<KBCfgBase> cfgObjects)
    {
        HashMap<String, Object> paraDicts = KBCfgHandler.objectsToParaDict(cfgObjects);
        if (paraDicts != null)
        {
            JSONObject jsonObj = new JSONObject();
            KBCfgBase.HashMap2JsonObject(paraDicts, jsonObj);
            if (jsonObj.length() > 0)
            {
                return jsonObj.toString().replace("\\", "");
            }
        }

        return null;
    }

    //parse command para to string
    public static String cmdParaToJsonString(HashMap<String, Object> paraDicts)
    {
        if (paraDicts != null)
        {
            JSONObject jsonObj = new JSONObject();
            KBCfgBase.HashMap2JsonObject(paraDicts, jsonObj);
            if (jsonObj.length() > 0)
            {
                return jsonObj.toString();
            }
        }

        return null;
    }


    //read json string to configruation
    private static ArrayList<KBCfgBase> jsonStringToObjects(String jsonMsg)
    {
        try {
            JSONObject jsonObj = new JSONObject(jsonMsg);
            HashMap<String, Object> paraDicts = new HashMap<>(10);
            KBCfgBase.JsonObject2HashMap(jsonObj, paraDicts);
            if (paraDicts.size() > 0){
                return KBCfgHandler.dictParaToObjects(paraDicts);
            }
        }
        catch (JSONException excp)
        {
            Log.e(LOG_TAG, "Parse Jason config string failed");
        }

        return null;
    }

    private static ArrayList<KBCfgBase> dictParaToObjects(HashMap<String, Object> dicts)
    {
        ArrayList<KBCfgBase> arrCfgList = new ArrayList<>(5);

        //read basic capibility
        Integer msgSubType = (Integer) dicts.get(KBCfgBase.JSON_MSG_CFG_SUBTYPE);
        if (msgSubType == null)
        {
            return null;
        }

        //check if need read adv type config
        for (String keyCfgType : kbCfgTypeObjects.keySet())
        {
            if ((Integer.valueOf(keyCfgType) & msgSubType) > 0) {
                Class classNewObj = kbCfgTypeObjects.get(keyCfgType);
                if (classNewObj == null){
                    continue;
                }

                try {
                    KBCfgBase kbCfgObj = (KBCfgBase)classNewObj.newInstance();
                    kbCfgObj.updateConfig(dicts);
                    arrCfgList.add(kbCfgObj);
                } catch (Exception excpt) {
                    excpt.printStackTrace();
                }
            }
        }

        return arrCfgList;
    }



    private static HashMap<String,Object> objectsToParaDict(ArrayList<KBCfgBase> cfgObjects)
    {
        HashMap<String, Object> paraDicts = new HashMap<>(10);
        int nCfgType = 0;

        for(KBCfgBase obj : cfgObjects)
        {
            HashMap<String, Object> objPara = obj.toDictionary();
            if (objPara != null && objPara.size() > 0)
            {
                nCfgType = (nCfgType | obj.cfgParaType());
                paraDicts.putAll(objPara);
            }
        }

        //check if is no data need config
        if (paraDicts.size() == 0)
        {
            Log.e(LOG_TAG, "no paramaters need to be config");
            return null;
        }

        //add configruation type
        paraDicts.put(KBCfgBase.JSON_MSG_CFG_SUBTYPE, nCfgType);

        //config message
        paraDicts.put(KBCfgBase.JSON_MSG_TYPE_KEY, KBCfgBase.JSON_MSG_TYPE_CFG);

        return paraDicts;
    }
}
