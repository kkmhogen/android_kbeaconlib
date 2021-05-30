package com.kbeacon.kbeaconlib;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanRecord;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.kbeacon.kbeaconlib.KBAdvPackage.KBAdvPacketBase;
import com.kbeacon.kbeaconlib.KBAdvPackage.KBAdvPacketHandler;
import com.kbeacon.kbeaconlib.KBCfgPackage.KBCfgBase;
import com.kbeacon.kbeaconlib.KBCfgPackage.KBCfgCommon;
import com.kbeacon.kbeaconlib.KBCfgPackage.KBCfgHandler;
import com.kbeacon.kbeaconlib.KBCfgPackage.KBCfgHumidityTrigger;
import com.kbeacon.kbeaconlib.KBCfgPackage.KBCfgNearbyTrigger;
import com.kbeacon.kbeaconlib.KBCfgPackage.KBCfgTrigger;
import com.kbeacon.kbeaconlib.KBCfgPackage.KBCfgType;
import com.kbeacon.kbeaconlib.KBSensorNotifyData.KBNotifyDataBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class KBeacon implements KBAuthHandler.KBAuthDelegate{
    private static final String LOG_TAG = "KBeacon";

    //connection state
    public final static int KBStateDisconnected = 0;
    public final static int KBStateConnecting = 1;
    public final static int KBStateDisconnecting = 2;
    public final static int KBStateConnected = 3;


    private ConnStateDelegate delegate;
    private SubscribeNotifyInstance mToAddedSubscribeInstance = null;

    //advertisement
    private String mac;
    private int rssi;
    private String name;
    private int state; //connection state

    //adv and config manager
    private int mCloseReason;
    private KBAdvPacketHandler mAdvPacketMgr;
    private KBCfgHandler mCfgMgr;
    private String mPassword;
    private BluetoothDevice mBleDevice;
    private Context mContext;
    private final BluetoothGattCallback mGattCallback;
    private BluetoothGatt mGattConnection;
    private int mActionStatus;
    private final static int MSG_CONNECT_TIMEOUT = 201;
    private final static int MSG_ACTION_TIME_OUT = 202;
    private final static int MSG_SYS_CONNECTION_EVT = 203;
    private final static int MSG_SERVICES_DISCOVERD = 204;
    private final static int MSG_START_AUTHENTICATION = 205;
    private final static int MSG_BEACON_DATA_RECEIVED = 206;
    private final static int MSG_CLOSE_CONNECTION_TIMEOUT = 207;
    private final static int MSG_START_REQUST_MAX_MTU = 208;
    private final static int MSG_NTF_CONNECT_SUCCESS = 209;
    private final static int MSG_NTF_IND_ENABLE = 210;
    private final static int MSG_BEACON_INDICATION_RECEIVED = 211;
    private final static int MSG_NTF_SUBSCRIBE_INDICATION_CMP = 212;

    //timer
    private final static int MAX_READ_CFG_TIMEOUT = 15*1000;

    private final static int MAX_WRITE_CFG_TIMEOUT = 15*1000;

    //action status
    private final static int ACTION_IDLE = 0x0;
    private final static int ACTION_WRITE_CFG = 0x1;
    private final static int ACTION_WRITE_CMD = 0x2;
    private final static int ACTION_INIT_READ_CFG = 0x3;
    private final static int ACTION_USR_READ_CFG = 0x4;
    private final static int ACTION_READ_SENSOR = 0x5;
    private final static int ACTION_ENABLE_NTF = 0x6;

    private final static int DATA_TYPE_AUTH = 0x1;
    private final static int DATA_TYPE_JSON = 0x2;

    //frame tag
    private final static int PDU_TAG_START = 0x0;
    private final static int PDU_TAG_MIDDLE = 0x1;
    private final static int  PDU_TAG_END = 0x2;
    private final static int PDU_TAG_SINGLE = 0x3;
    private final static int MSG_PDU_HEAD_LEN = 0x3;

    private final static int DATA_ACK_HEAD_LEN = 6;

    //down json data
    private final static int  CENT_PERP_TX_JSON_DATA =  2;
    private final static int  PERP_CENT_TX_JSON_ACK  = 2;
    private final static int  PERP_CENT_DATA_RPT = 3;
    private final static int  CENT_PERP_DATA_RPT_ACK  = 3;
    private final static int  CENT_PERP_TX_HEX_DATA = 0;
    private final static int  PERP_CENT_TX_HEX_ACK  = 0;
    private final static int  PERP_CENT_HEX_DATA_RPT = 5;
    private final static int  CENT_PERP_HEX_DATA_RPT_ACK  = 5;

    private final static int  BEACON_ACK_SUCCESS = 0x0;
    private final static int  BEACON_ACK_EXPECT_NEXT = 0x4;
    private final static int  BEACON_ACK_CAUSE_CMD_RCV = 0x5;
    private final static int  BEACON_ACK_EXE_CMD_CMP = 0x6;
    private final static int MAX_MTU_SIZE = 251;
    private final static int MAX_BUFFER_DATA_SIZE = 1024;

    private ActionCallback mWriteCfgCallback;
    private ActionCallback mWriteCmdCallback;
    private ActionCallback mEnableSubscribeNotifyCallback;
    private ReadConfigCallback mReadCfgCallback;
    private ReadSensorCallback mReadSensorCallback;
    private byte[] mByDownloadDatas;
    private byte mByDownDataType;
    private byte[] mReceiveData;
    private int mReceiveDataLen;
    private ArrayList<KBCfgBase> mToBeCfgData;
    private KBAuthHandler mAuthHandler;
    private HashMap<Integer, SubscribeNotifyInstance> notifyData2ClassMap;

    private class SubscribeNotifyInstance
    {
        int nNotifyType;
        Class notifyClass;
        NotifyDataDelegate delegate;
    };

    public interface ConnStateDelegate {
        void onConnStateChange(KBeacon beacon, int state, int nReason);
    }

    public interface NotifyDataDelegate {
        void onNotifyDataReceived(KBeacon beacon, int nDataType, KBNotifyDataBase sensorData);
    }

    public interface ActionCallback {
        void onActionComplete(boolean bConfigSuccess, KBException error);
    }

    public interface ReadConfigCallback {
        void onReadComplete(boolean bConfigSuccess, HashMap<String, Object> readPara, KBException error);
    }

    public interface ReadSensorCallback {
        void onReadComplete(boolean bReadResult, byte[] readPara, KBException error);
    }

    public KBeacon(String strMacAddress, Context ctx)
    {
        mac = strMacAddress;
        mAdvPacketMgr = new KBAdvPacketHandler();
        mCfgMgr = new KBCfgHandler();
        mContext = ctx;
        mGattCallback = new KBeaconGattCallback();
        mAuthHandler = new KBAuthHandler(this);
        mReceiveData = new byte[MAX_BUFFER_DATA_SIZE];
        mReceiveDataLen = 0;
        notifyData2ClassMap = new HashMap<>(10);
    }

    void setAdvTypeFilter(int nAdvTypeFilter)
    {
        mAdvPacketMgr.setAdvTypeFilter(nAdvTypeFilter);
    }

    void attach2Device(BluetoothDevice bleDevice, KBeaconsMgr beaconMgr)
    {
        mBleDevice = bleDevice;
    }

    //get all advertisment
    public KBAdvPacketBase[] allAdvPackets()
    {
        return mAdvPacketMgr.advPackets();
    }

    //get specified advertisement packet
    public KBAdvPacketBase getAdvPacketByType(int nAdvType)
    {
        return mAdvPacketMgr.getAdvPacket(nAdvType);
    }

    //get mac address
    public String getMac()
    {
        return mac;
    }

    //get rssi
    public Integer getRssi()
    {
        return rssi;
    }

    public String getName()
    {
        return name;
    }

    public Integer getBatteryPercent()
    {
        return mAdvPacketMgr.getBatteryPercent();
    }


    public int getState()
    {
        return state;
    }

    public Integer maxTxPower()
    {
        KBCfgCommon commCfg = (KBCfgCommon)mCfgMgr.getConfigruationByType(KBCfgType.KBConfigTypeCommon);
        if (commCfg != null){
            return commCfg.getMaxTxPower();
        }else{
            return null;
        }
    }

    public Integer minTxPower()
    {
        KBCfgCommon commCfg = (KBCfgCommon)mCfgMgr.getConfigruationByType(KBCfgType.KBConfigTypeCommon);
        if (commCfg != null){
            return commCfg.getMinTxPower();
        }else{
            return null;
        }
    }

    public String model()
    {
        KBCfgCommon commCfg = (KBCfgCommon)mCfgMgr.getConfigruationByType(KBCfgType.KBConfigTypeCommon);
        if (commCfg != null){
            return commCfg.getModel();
        }else{
            return null;
        }
    }

    //hardware version
    public String hardwareVersion()
    {
        KBCfgCommon commCfg = (KBCfgCommon)mCfgMgr.getConfigruationByType(KBCfgType.KBConfigTypeCommon);
        if (commCfg != null){
            return commCfg.getHardwareVersion();
        }else{
            return null;
        }
    }

    public String version()
    {
        KBCfgCommon commCfg = (KBCfgCommon)mCfgMgr.getConfigruationByType(KBCfgType.KBConfigTypeCommon);
        if (commCfg != null){
            return commCfg.getVersion();
        }else{
            return null;
        }
    }

    public Integer capability()
    {
        KBCfgCommon commCfg = (KBCfgCommon)mCfgMgr.getConfigruationByType(KBCfgType.KBConfigTypeCommon);
        if (commCfg != null){
            return commCfg.getBasicCapability();
        }else{
            return null;
        }
    }

    public Integer triggerCapability()
    {
        KBCfgCommon commCfg = (KBCfgCommon)mCfgMgr.getConfigruationByType(KBCfgType.KBConfigTypeCommon);
        if (commCfg != null){
            return commCfg.getTrigCapability();
        }else{
            return null;
        }
    }

    public KBCfgBase[] configParamaters()
    {
        return mCfgMgr.configParamaters();
    }

    public KBCfgBase getConfigruationByType(int nCfgType)
    {
        return mCfgMgr.getConfigruationByType(nCfgType);
    }

    public boolean isConnected()
    {
        return state == KBStateConnected;
    }

    public boolean parseAdvPacket(ScanRecord data, int nRssi, String strName)
    {
        name = strName;
        rssi = nRssi;

        return mAdvPacketMgr.parseAdvPacket(data, rssi, strName);
    }

    public void setConnStateDelegate(ConnStateDelegate connectCallback)
    {
        delegate = connectCallback;
    }

    public boolean isSensorDataSubscribe(Class sensorNtfMsgClass)
    {
        try {
            KBNotifyDataBase notifyData = (KBNotifyDataBase) sensorNtfMsgClass.newInstance();
            if (notifyData2ClassMap.get(notifyData.getSensorDataType()) != null){
                return true;
            }
        }
        catch (Exception excpt)
        {
            excpt.printStackTrace();
        }

        return false;
    }

    public void subscribeSensorDataNotify(Class sensorNtfMsgClass,
                                          NotifyDataDelegate notifyDataCallback,
                                          ActionCallback callback)
    {
        try {
            if (!isSupportSensorDataNotification()) {
                if (callback != null) {
                    callback.onActionComplete(false, new KBException(KBException.KBEvtCfgBusy, "device not support subscription"));
                }
                return;
            }

            KBNotifyDataBase notifyData = (KBNotifyDataBase) sensorNtfMsgClass.newInstance();
            SubscribeNotifyInstance instance = new SubscribeNotifyInstance();
            instance.notifyClass = sensorNtfMsgClass;
            instance.delegate = notifyDataCallback;
            instance.nNotifyType = notifyData.getSensorDataType();
            if (this.notifyData2ClassMap.size() == 0)
            {
                if (mActionStatus != ACTION_IDLE)
                {
                    if (callback != null) {
                        callback.onActionComplete(false, new KBException(KBException.KBEvtCfgBusy, "device is not in idle"));
                    }
                    return;
                }
                if (state != KBStateConnected)
                {
                    if (callback != null) {
                        callback.onActionComplete(false, new KBException(KBException.KBEvtCfgStateError, "device is not in connected"));
                    }
                    return;
                }

                //save callback
                mToAddedSubscribeInstance = instance;
                mEnableSubscribeNotifyCallback = callback;
                if (startEnableIndication(KBUtility.KB_CFG_SERVICE_UUID, KBUtility.KB_IND_CHAR_UUID, true))
                {
                    startNewAction(ACTION_ENABLE_NTF, 3000);
                }else{
                    if (callback != null) {
                        callback.onActionComplete(false, new KBException(KBException.KBEvtCfgFailed, "enable notification failed"));
                    }
                }
            } else {
                this.notifyData2ClassMap.put(instance.nNotifyType, instance);
                if (callback != null) {
                    callback.onActionComplete(true, null);
                }
            }
        }catch (Exception excpt)
        {
            excpt.printStackTrace();
        }
    }

    public void removeSubscribeSensorDataNotify(Class sensorNtfMsgClass, ActionCallback callback)
    {
        try {
            if (!isSupportSensorDataNotification()) {
                if (callback != null) {
                    callback.onActionComplete(false, new KBException(KBException.KBEvtCfgBusy, "device not support subscription"));
                }
                return;
            }

            KBNotifyDataBase notifyData = (KBNotifyDataBase) sensorNtfMsgClass.newInstance();
            SubscribeNotifyInstance instance = new SubscribeNotifyInstance();
            instance.nNotifyType = notifyData.getSensorDataType();
            if (this.notifyData2ClassMap.get(instance.nNotifyType) == null)
            {
                if (callback != null) {
                    callback.onActionComplete(false, new KBException(KBException.KBEvtCfgBusy, "Subscription not found"));
                }
                return;
            }

            if (this.notifyData2ClassMap.size() == 1)
            {
                if (mActionStatus != ACTION_IDLE)
                {
                    if (callback != null) {
                        callback.onActionComplete(false, new KBException(KBException.KBEvtCfgBusy, "device is not in idle"));
                    }
                    return;
                }
                if (state != KBStateConnected)
                {
                    if (callback != null) {
                        callback.onActionComplete(false, new KBException(KBException.KBEvtCfgStateError, "device is not in connected"));
                    }
                    return;
                }

                //save callback
                mToAddedSubscribeInstance = null;
                mEnableSubscribeNotifyCallback = callback;
                if (startEnableIndication(KBUtility.KB_CFG_SERVICE_UUID, KBUtility.KB_IND_CHAR_UUID, false))
                {
                    startNewAction(ACTION_ENABLE_NTF, 3000);
                }
            } else {
                this.notifyData2ClassMap.remove(instance.nNotifyType);
                if (callback != null) {
                    callback.onActionComplete(true, null);
                }
            }
        }catch (Exception excpt)
        {
            excpt.printStackTrace();
        }
    }

    private void handleBeaconEnableSubscribeComplete()
    {
        cancelActionTimer();

        if (mToAddedSubscribeInstance != null)
        {
            this.notifyData2ClassMap.put(mToAddedSubscribeInstance.nNotifyType, mToAddedSubscribeInstance);
            mToAddedSubscribeInstance = null;

            if (mEnableSubscribeNotifyCallback != null) {
                ActionCallback tmpAction = mEnableSubscribeNotifyCallback;
                mEnableSubscribeNotifyCallback = null;
                tmpAction.onActionComplete(true, null);
            }
        }
        else
        {
            this.notifyData2ClassMap.clear();
            if (mEnableSubscribeNotifyCallback != null) {
                ActionCallback tmpAction = mEnableSubscribeNotifyCallback;
                mEnableSubscribeNotifyCallback = null;
                tmpAction.onActionComplete(true, null);
            }
        }
    }

    public ConnStateDelegate getConnStateDelegate()
    {
        return delegate;
    }

    public boolean connect(String password, int timeout, ConnStateDelegate connectCallback)
    {
        return connectEnhanced(password, timeout, null, connectCallback);
    }

    public boolean connectEnhanced(String password, int timeout, KBConnPara connPara, ConnStateDelegate connectCallback)
    {
        if (state == KBStateDisconnected && password.length() <= 16 && password.length() >= 8)
        {
            delegate = connectCallback;

            mGattConnection = mBleDevice.connectGatt(mContext, false, mGattCallback);
            Log.v(LOG_TAG, "start connect to device " + mac);

            mPassword = password;
            state = KBStateConnecting;

            //cancel action timer
            this.cancelActionTimer();

            //cancel connect timer
            mAuthHandler.setConnPara(connPara);
            mMsgHandler.removeMessages(MSG_CONNECT_TIMEOUT);
            mMsgHandler.sendEmptyMessageDelayed(MSG_CONNECT_TIMEOUT, timeout);

            //notify connecting
            if (delegate != null) {
                this.delegate.onConnStateChange(this, KBStateConnecting, 0);
            }
            return true;
        }
        else
        {
            //notify connecting
            Log.e(LOG_TAG, "input paramaters false");
            return false;
        }
    }

    public void clearDeviceCache() {
        if (mGattConnection != null) {
            try {
                Method localMethod = mGattConnection.getClass().getMethod("refresh", new Class[0]);
                if (localMethod != null) {
                    localMethod.invoke(mGattConnection, new Object[0]);
                }
            } catch (Exception localException) {
                Log.e(LOG_TAG, "An exception occured while refreshing device");
            }
        }
    }

    private void connectingTimeout()
    {
        this.closeBeacon(KBConnectionEvent.ConnTimeout);
    }

    private void cancelActionTimer()
    {
        mMsgHandler.removeMessages(MSG_ACTION_TIME_OUT);
        mActionStatus = ACTION_IDLE;
    }

    private boolean startNewAction(int nNewAction, int timeout)
    {
        if (mActionStatus != ACTION_IDLE)
        {
            return false;
        }

        mActionStatus = nNewAction;
        if (timeout > 0)
        {
            mMsgHandler.sendEmptyMessageDelayed(MSG_ACTION_TIME_OUT, timeout);
        }

        return true;
    }

    //connect device timeout
    private void actionTimeout()
    {
        if (mActionStatus == ACTION_INIT_READ_CFG)
        {
            mActionStatus = ACTION_IDLE;
            closeBeacon(KBConnectionEvent.ConnTimeout);
        }
        else if (mActionStatus == ACTION_USR_READ_CFG)
        {
            mActionStatus = ACTION_IDLE;
            if (mReadCfgCallback != null){
                ReadConfigCallback tempCallback = mReadCfgCallback;
                mReadCfgCallback = null;
                tempCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgTimeout,
                        "read configuration message timeout"));
            }
        }
        else if (mActionStatus == ACTION_WRITE_CFG)
        {
            mActionStatus = ACTION_IDLE;
            if (mWriteCfgCallback != null)
            {
                ActionCallback tmpAction = mWriteCfgCallback;
                mWriteCfgCallback = null;
                tmpAction.onActionComplete(false, new KBException(KBException.KBEvtCfgTimeout,
                        "write configuration msg timeout"));
            }
        }
        else if (mActionStatus == ACTION_WRITE_CMD)
        {
            mActionStatus = ACTION_IDLE;
            if (mWriteCmdCallback != null)
            {
                ActionCallback tmpAction = mWriteCfgCallback;
                mWriteCfgCallback = null;
                tmpAction.onActionComplete(false, new KBException(KBException.KBEvtCfgTimeout,
                        "write configuration msg timeout"));
            }
        }
        else if (mActionStatus == ACTION_READ_SENSOR)
        {
            mActionStatus = ACTION_IDLE;
            if (mReadSensorCallback != null)
            {
                ReadSensorCallback tempCallback = mReadSensorCallback;
                mReadSensorCallback = null;
                tempCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgTimeout,
                        "write configuration msg timeout"));
            }
        }
        else if (mActionStatus == ACTION_ENABLE_NTF)
        {
            mActionStatus = ACTION_IDLE;
            if (mEnableSubscribeNotifyCallback != null)
            {
                ActionCallback tmpAction = mEnableSubscribeNotifyCallback;
                mEnableSubscribeNotifyCallback = null;
                tmpAction.onActionComplete(false, new KBException(KBException.KBEvtCfgTimeout,
                        "write configuration msg timeout"));
            }
        }
    }

    public void disconnect()
    {
        if (state != KBStateDisconnected
                && state != KBStateDisconnecting) {
            this.closeBeacon(KBConnectionEvent.ConnManualDisconnecting);
        }
    }

    private void handleCentralBLEEvent(int status, int nNewState)
    {
        if (status == BluetoothGatt.GATT_SUCCESS)
        {
            if (state == KBStateConnecting && nNewState == BluetoothGatt.STATE_CONNECTED)
            {
                mGattConnection.discoverServices();
            }
        }
        else
        {
            if (state == KBStateDisconnecting)
            {
                clearGattResource(mCloseReason);

                checkClearGattBuffer(status);
            }
            else if (state == KBStateConnecting || state == KBStateConnected)
            {
                if (nNewState == BluetoothGatt.STATE_DISCONNECTED)
                {
                    state = KBStateDisconnecting;
                    clearGattResource(KBConnectionEvent.ConnException);
                    checkClearGattBuffer(status);
                }
                this.closeBeacon(KBConnectionEvent.ConnException);
            }
        }
    }

    public void checkClearGattBuffer(int status)
    {
        if (status == 133 || status == BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED)
        {
            Log.e(LOG_TAG, "remove device gatt catch:" + mac);

            clearDeviceCache();
        }
    }

    public void authStateChange(int authRslt)
    {
        if (authRslt == KBAuthHandler.KBAuthFailed)
        {
            this.closeBeacon(KBConnectionEvent.ConnAuthFail);
        }
        else if (authRslt == KBAuthHandler.KBAuthSuccess)
        {
            this.cancelActionTimer();

            if (state == KBStateConnecting) {
                int nReadCfgType = KBCfgType.KBConfigTypeCommon | KBCfgType.KBConfigTypeIBeacon | KBCfgType.KBConfigTypeEddyURL
                        | KBCfgType.KBConfigTypeEddyUID | KBCfgType.KBConfigTypeSensor;
                configReadBeaconParamaters(nReadCfgType, ACTION_INIT_READ_CFG);
            }
        }
    }

    public void writeAuthData(byte[] data)
    {
        this.startWriteCfgValue(data);
    }

    private void clearGattResource(int nReason)
    {
        if (state == KBStateDisconnecting)
        {
            Log.v(LOG_TAG, "clear gatt connection resource");
            state = KBStateDisconnected;
            mGattConnection.close();
            if (delegate != null) {
                delegate.onConnStateChange(this, state, nReason);
            }
        }
    }

    private void closeBeacon(int nReason)
    {
        mCloseReason = nReason;

        this.cancelActionTimer();
        mMsgHandler.removeMessages(MSG_CONNECT_TIMEOUT);
        mMsgHandler.removeMessages(MSG_CLOSE_CONNECTION_TIMEOUT);

        if (state == KBStateConnected || state == KBStateConnecting)
        {
            state = KBStateDisconnecting;
            //cancel connection
            mGattConnection.disconnect();

            mMsgHandler.sendEmptyMessageDelayed(MSG_CLOSE_CONNECTION_TIMEOUT, 7000);

            if (delegate != null) {
                delegate.onConnStateChange(this, state, mCloseReason);
            }
        }
        else
        {
            if (state != KBStateDisconnected)
            {
                Log.e(LOG_TAG, "disconnected kbeacon for reason");
                state = KBStateDisconnected;
                if (delegate != null){
                    delegate.onConnStateChange(this, state, mCloseReason);
                }
            }
        }
    }

    private boolean startEnableNotification(UUID srvUUID, UUID charUUID)
    {
        BluetoothGattCharacteristic characteristic = getCharacteristicByID(srvUUID,
                charUUID);
        if (characteristic == null) {
            Log.e(LOG_TAG, ":startWriteCharatics getCharacteristicByID failed." + charUUID);
            Message msgCentralEvt = mMsgHandler.obtainMessage(MSG_SYS_CONNECTION_EVT, BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED, BluetoothGatt.STATE_DISCONNECTING);
            mMsgHandler.sendMessage(msgCentralEvt);
            return false;
        }

        //set enable
        if (!mGattConnection.setCharacteristicNotification(characteristic, true)) {
            Message msgCentralEvt = mMsgHandler.obtainMessage(MSG_SYS_CONNECTION_EVT, BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED, BluetoothGatt.STATE_DISCONNECTING);
            mMsgHandler.sendMessage(msgCentralEvt);
            return false;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(KBUtility.CHARACTERISTIC_NOTIFICATION_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        return mGattConnection.writeDescriptor(descriptor);
    }

    private boolean startEnableIndication(UUID srvUUID, UUID charUUID, boolean bEnable)
    {
        BluetoothGattCharacteristic characteristic = getCharacteristicByID(srvUUID,
                charUUID);
        if (characteristic == null) {
            Log.e(LOG_TAG, ":startWriteCharatics getCharacteristicByID failed." + charUUID);
            Message msgCentralEvt = mMsgHandler.obtainMessage(MSG_SYS_CONNECTION_EVT, BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED, BluetoothGatt.STATE_DISCONNECTING);
            mMsgHandler.sendMessage(msgCentralEvt);
            return false;
        }

        //set enable
        if (!mGattConnection.setCharacteristicNotification(characteristic, bEnable)) {
            Message msgCentralEvt = mMsgHandler.obtainMessage(MSG_SYS_CONNECTION_EVT, BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED, BluetoothGatt.STATE_DISCONNECTING);
            mMsgHandler.sendMessage(msgCentralEvt);
            return false;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(KBUtility.CHARACTERISTIC_NOTIFICATION_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        return mGattConnection.writeDescriptor(descriptor);
    }


    public void sendCommand(HashMap<String,Object>cmdPara, ActionCallback callback)
    {
        if (mActionStatus != ACTION_IDLE)
        {
            if (callback != null) {
                callback.onActionComplete(false, new KBException(KBException.KBEvtCfgBusy, "device is not in idle"));
            }
            return;
        }
        if (state != KBStateConnected)
        {
            if (callback != null) {
                callback.onActionComplete(false, new KBException(KBException.KBEvtCfgStateError, "device is not in connected"));
            }
            return;
        }

        //save callback
        String strJsonCfgData = KBCfgHandler.cmdParaToJsonString(cmdPara);
        if (strJsonCfgData == null || strJsonCfgData.length() == 0) {
            if (callback != null) {
                callback.onActionComplete(false, new KBException(KBException.KBEvtCfgInputInvalid, "input command invalid"));
            }
            return;
        }
        mToBeCfgData = null;
        mWriteCmdCallback = callback;
		
        mByDownloadDatas = strJsonCfgData.getBytes(StandardCharsets.UTF_8);
        mByDownDataType = CENT_PERP_TX_JSON_DATA;
        startNewAction(ACTION_WRITE_CMD, MAX_READ_CFG_TIMEOUT);
        sendNextCfgData(0);
    }

    public void readConfig(int nConfigType, final ReadConfigCallback callback)
    {
        if (mActionStatus != ACTION_IDLE)
        {
            callback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgBusy, "device is not in idle"));
            return;
        }
        if (state != KBStateConnected)
        {
            if (callback != null) {
                callback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgStateError, "device is not in connected"));
            }
            return;
        }

        mReadCfgCallback = callback;
        if (!configReadBeaconParamaters(nConfigType, ACTION_USR_READ_CFG))
        {
            ReadConfigCallback tempCallback = mReadCfgCallback;
            mReadCfgCallback = null;
            tempCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgInputInvalid, "input is not valid"));
        }
    }

    public void readConfig(HashMap<String,Object>readPara, final ReadConfigCallback callback)
    {
        if (mActionStatus != ACTION_IDLE)
        {
            callback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgBusy, "device is not in idle"));
            return;
        }
        if (state != KBStateConnected)
        {
            if (callback != null) {
                callback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgStateError, "device is not in connected"));
            }
            return;
        }

        String strJsonCfgData = KBCfgBase.HashMap2JsonString(readPara);
        if (strJsonCfgData == null || strJsonCfgData.length() == 0) {
            if (callback != null) {
                callback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgInputInvalid, "paramaters invalid"));
            }
            return;
        }
        mReadCfgCallback = callback;
        mByDownloadDatas = strJsonCfgData.getBytes(StandardCharsets.UTF_8);
        mByDownDataType = CENT_PERP_TX_JSON_DATA;
        mReceiveDataLen = 0;
        startNewAction(ACTION_USR_READ_CFG, MAX_READ_CFG_TIMEOUT);
        sendNextCfgData(0);
    }

    public void readTriggerConfig(int nTriggerType, final ReadConfigCallback callback)
    {
        HashMap<String,Object> hashPara = new HashMap<>(5);
        hashPara.put(KBCfgBase.JSON_MSG_TYPE_KEY, KBCfgBase.JSON_MSG_TYPE_GET_PARA);
        hashPara.put(KBCfgBase.JSON_MSG_CFG_SUBTYPE, KBCfgType.KBConfigTypeTrigger);
        hashPara.put(KBCfgTrigger.JSON_FIELD_TRIGGER_TYPE, nTriggerType);
        readConfig(hashPara,new KBeacon.ReadConfigCallback()
        {
            public void onReadComplete(boolean bConfigSuccess, HashMap<String, Object> readPara, KBException error)
            {
                if (bConfigSuccess){
                    ArrayList<KBCfgTrigger> triggerCfgList = new ArrayList<KBCfgTrigger>(2);
                    boolean bCfgDataRet = false;

                    try {
                        if (readPara.containsKey("trObj")) {
                            JSONArray triggerArray = (JSONArray) readPara.get("trObj");
                            if (triggerArray != null) {
                                for (int i = 0; i < triggerArray.length(); i++) {
                                    JSONObject jsonObject = triggerArray.getJSONObject(i);
                                    HashMap<String, Object> paraDicts = new HashMap<>(10);
                                    KBCfgBase.JsonObject2HashMap(jsonObject, paraDicts);

                                    KBCfgTrigger cfgTrigger;
                                    Integer trType = (Integer)paraDicts.get("trType");
                                    if (trType != null)
                                    {
                                        if (trType == KBCfgTrigger.KBTriggerTypeNearby)
                                        {
                                            cfgTrigger = new KBCfgNearbyTrigger();
                                        }
                                        else if (trType == KBCfgTrigger.KBTriggerTypeHumidity)
                                        {
                                            cfgTrigger = new KBCfgHumidityTrigger();
                                        }
                                        else {
                                            cfgTrigger = new KBCfgTrigger();
                                        }

                                        //update configruation
                                        try {
                                            cfgTrigger.updateConfig(paraDicts);

                                            triggerCfgList.add(cfgTrigger);
                                        }
                                        catch (Exception excpt)
                                        {
                                            excpt.printStackTrace();
                                        }
                                    }
                                }
                                readPara.put("trObj", triggerCfgList);
                                bCfgDataRet = true;
                            }
                        }
                    }
                    catch(JSONException excp)
                    {
                        excp.printStackTrace();
                    }

                    if (bCfgDataRet){
                        if (callback != null) {
                            callback.onReadComplete(true, readPara, null);
                        }
                    } else {
                        if (callback != null) {
                            callback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgNotSupport, "device not support trigger"));
                        }
                    }
                }
                else
                {
                    if (callback != null) {
                        callback.onReadComplete(false, null, error);
                    }
                }
            }
        });
    }

    public void modifyTrigger(KBCfgTrigger cfgTrigger, ActionCallback callback)
    {
        if (cfgTrigger.getTriggerAction() == null || cfgTrigger.getTriggerType() == null){
            if (callback != null) {
                callback.onActionComplete(false, new KBException(KBException.KBEvtCfgInputInvalid, "input data invalid"));
            }
            return;
        }
        ArrayList<KBCfgBase> cfgList = new ArrayList<>(1);
        cfgList.add(cfgTrigger);
        modifyConfig(cfgList,callback);
    }

    public void modifyConfig(ArrayList<KBCfgBase> cfgList, ActionCallback callback)
    {
        if (mActionStatus != ACTION_IDLE)
        {
            if (callback != null) {
                callback.onActionComplete(false, new KBException(KBException.KBEvtCfgBusy, "device is not in idle"));
            }
            return;
        }
        if (state != KBStateConnected)
        {
            if (callback != null) {
                callback.onActionComplete(false, new KBException(KBException.KBEvtCfgStateError, "device is not in connected"));
            }
            return;
        }

        //get configruation json
        String strJsonCfgData = KBCfgHandler.objectsToJsonString(cfgList);
        if (strJsonCfgData == null || strJsonCfgData.length() == 0){
            if (callback != null) {
                callback.onActionComplete(false, new KBException(KBException.KBEvtCfgNoParameters, "no valid paramaters in config object"));
            }
            return;
        }

        //save data
        mByDownloadDatas = strJsonCfgData.getBytes(StandardCharsets.UTF_8);
        mByDownDataType = CENT_PERP_TX_JSON_DATA;
        mWriteCfgCallback = callback;
        mToBeCfgData = cfgList;

        //write data
        startNewAction(ACTION_WRITE_CFG, MAX_WRITE_CFG_TIMEOUT);
        sendNextCfgData(0);
    }

    public void sendSensorRequest(byte[] msgReq, ReadSensorCallback callback)
    {
        if (mActionStatus != ACTION_IDLE)
        {
            if (callback != null) {
                callback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgBusy, "device is not in idle"));
            }
            return;
        }
        if (state != KBStateConnected)
        {
            if (callback != null) {
                callback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgStateError, "device is not in connected"));
            }
            return;
        }

        if (msgReq == null || msgReq.length == 0) {
            if (callback != null) {
                callback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgInputInvalid, "input command invalid"));
            }
            return;
        }

        mReadSensorCallback = callback;
        mByDownloadDatas = msgReq;
        mReceiveDataLen = 0;
        mByDownDataType = CENT_PERP_TX_HEX_DATA;
        startNewAction(ACTION_READ_SENSOR, MAX_READ_CFG_TIMEOUT);
        sendNextCfgData(0);
    }

    private void sendNextCfgData(int nReqDataSeq)
    {
        if (mByDownloadDatas == null)
        {
            return;
        }

        if (nReqDataSeq >= mByDownloadDatas.length)
        {
            Log.v(LOG_TAG, "tx config data complete");
            return;
        }

        //get mtu tag
        byte nPduTag = PDU_TAG_START;
        int nMaxTxDataSize = mAuthHandler.getMtuSize() - MSG_PDU_HEAD_LEN;
        int nDataLen = nMaxTxDataSize;
        if (mByDownloadDatas.length <= nMaxTxDataSize)
        {
            nPduTag = PDU_TAG_SINGLE;
            nDataLen = mByDownloadDatas.length;
        }
        else if (nReqDataSeq == 0)
        {
            nPduTag = PDU_TAG_START;
            nDataLen = nMaxTxDataSize;
        }
        else if (nReqDataSeq + nMaxTxDataSize < mByDownloadDatas.length)
        {
            nPduTag = PDU_TAG_MIDDLE;
            nDataLen = nMaxTxDataSize;
        }
        else if (nReqDataSeq + nMaxTxDataSize >= mByDownloadDatas.length)
        {
            nPduTag = PDU_TAG_END;
            nDataLen = mByDownloadDatas.length - nReqDataSeq;
        }

        //down data head
        byte[] downData = new byte[nDataLen + MSG_PDU_HEAD_LEN];
        downData[0] = (byte)(((mByDownDataType << 4) + nPduTag) & 0xFF);
        byte nNetOrderSeq[] = KBUtility.htonbyte((short)nReqDataSeq);
        downData[1] = nNetOrderSeq[0];
        downData[2] = nNetOrderSeq[1];

        //fill data body
        System.arraycopy(mByDownloadDatas, nReqDataSeq, downData, 3, nDataLen);

        //send to device
        Log.v(LOG_TAG, "tx data seq:" + nReqDataSeq);
        startWriteCfgValue(downData);
    }

    private void configHandleDownCmdAck(byte frameType, byte byDataType, byte[]data)
    {
        short nReqDataSeq = KBUtility.htonshort(data[0], data[1]);
        short nAckCause = KBUtility.htonshort(data[4], data[5]);

        if (nAckCause == BEACON_ACK_CAUSE_CMD_RCV)  //beacon has received the command, now start execute
        {
            if (ACTION_READ_SENSOR == mActionStatus && byDataType == PERP_CENT_TX_HEX_ACK)
            {
                if (data.length > DATA_ACK_HEAD_LEN) {
                    System.arraycopy(data, DATA_ACK_HEAD_LEN, mReceiveData, 0, data.length - DATA_ACK_HEAD_LEN);
                    mReceiveDataLen = (data.length - DATA_ACK_HEAD_LEN);

                    Log.v(LOG_TAG, "beacon has receive command:" + mReceiveDataLen);

                    //if has next data, send report ack
                    configSendDataRptAck((short) mReceiveDataLen, (byte) CENT_PERP_HEX_DATA_RPT_ACK, (short) 0);
                }
            }
        }
        else if (nAckCause == BEACON_ACK_SUCCESS)   //write command receive
        {
            if (ACTION_WRITE_CFG == mActionStatus)
            {
                cancelActionTimer();

                //update config to local
                if (mToBeCfgData != null)
                {
                    mCfgMgr.updateConfig(mToBeCfgData);
                }

                //download data complete
                if (mWriteCfgCallback != null) {
                    ActionCallback tmpAction = mWriteCfgCallback;
                    mWriteCfgCallback = null;

                    tmpAction.onActionComplete(true, null);
                }
            }
            else if (ACTION_WRITE_CMD == mActionStatus)
            {
                cancelActionTimer();

                //download data complete
                if (mWriteCmdCallback != null) {
                    ActionCallback tmpAction = mWriteCmdCallback;
                    mWriteCmdCallback = null;
                    tmpAction.onActionComplete(true, null);
                }
            }
            else if (ACTION_READ_SENSOR == mActionStatus)
            {
                cancelActionTimer();

                if (data.length > DATA_ACK_HEAD_LEN) {
                    System.arraycopy(data, DATA_ACK_HEAD_LEN, mReceiveData, 0, data.length - DATA_ACK_HEAD_LEN);
                    mReceiveDataLen = (data.length - DATA_ACK_HEAD_LEN);
                }
                //download data complete
                if (mReadSensorCallback != null) {
                    ReadSensorCallback tempCallback = mReadSensorCallback;
                    mReadSensorCallback = null;

                    byte[] inBoundData = null;
					if (mReceiveDataLen > 0)
					{
                    	inBoundData = new byte[mReceiveDataLen];
                    	System.arraycopy(mReceiveData, 0, inBoundData, 0, mReceiveDataLen);
					}
						
                    Log.v(LOG_TAG, "beacon inbond command execute complete:");
                    tempCallback.onReadComplete(true, inBoundData, null);
				}
            }
        }
        else if (nAckCause == BEACON_ACK_EXPECT_NEXT)
        {
            if (ACTION_IDLE != mActionStatus)
            {
                this.sendNextCfgData(nReqDataSeq);
            }
        }
        else if (nAckCause == BEACON_ACK_EXE_CMD_CMP)
        {
            Log.v(LOG_TAG, "beacon execute command complete");
        }
        else   //command failed
        {
            if (ACTION_INIT_READ_CFG == mActionStatus) {
                cancelActionTimer();

                closeBeacon(KBConnectionEvent.ConnException);
            }
            else if (ACTION_WRITE_CFG == mActionStatus)
            {
                cancelActionTimer();

                if (mWriteCfgCallback != null) {
                    ActionCallback tmpAction = mWriteCfgCallback;
                    mWriteCfgCallback = null;
                    tmpAction.onActionComplete(false, new KBException(KBException.KBEvtCfgFailed,
                            nAckCause,
                            "last write action failed"));
                }
            }
            else if (ACTION_WRITE_CMD == mActionStatus)
            {
                cancelActionTimer();

                if (mWriteCmdCallback != null) {
                    ActionCallback tmpAction = mWriteCmdCallback;
                    mWriteCmdCallback = null;
                    tmpAction.onActionComplete(false, new KBException(KBException.KBEvtCfgFailed,
                            nAckCause,
                            "last write action failed"));
                }
            }
            else if (ACTION_USR_READ_CFG == mActionStatus) {
                cancelActionTimer();

                //read config data failed
                if (mReadCfgCallback != null) {
                    ReadConfigCallback tempCallback = mReadCfgCallback;
                    mReadCfgCallback = null;
                    tempCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgFailed,
                            nAckCause,
                            "last read action failed"));
                }
            }
            else if (ACTION_READ_SENSOR == mActionStatus) {
                cancelActionTimer();

                //read config data failed
                if (mReadSensorCallback != null) {
                    ReadSensorCallback tempCallback = mReadSensorCallback;
                    mReadSensorCallback = null;
                    Log.v(LOG_TAG, "beacon sensor read execute failed");
                    tempCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgFailed,
                            nAckCause,
                            "last read action failed"));
                }
            }
        }
    }

  
	
	private void configHandleReadDataRpt(byte frameType, byte byDataType, byte[]data)
    {
        boolean bRcvDataCmp = false;
        short nDataSeq = KBUtility.htonshort(data[0], data[1]);
        int nDataPayloadLen = data.length - 2;
        //frame start
        if (frameType == PDU_TAG_START)
        {
            //new read configruation
            System.arraycopy(data, 2, mReceiveData, 0, nDataPayloadLen);
            mReceiveDataLen = nDataPayloadLen;

            //send ack
            configSendDataRptAck((short)mReceiveDataLen, byDataType, (short)0);
        }
        else if (frameType == PDU_TAG_MIDDLE)
        {
            if (nDataSeq != mReceiveDataLen || mReceiveDataLen + nDataPayloadLen > MAX_BUFFER_DATA_SIZE)
            {
                Log.v(LOG_TAG, "receive unknown data sequence:" + nDataSeq + ", expect seq:" + mReceiveDataLen);
                configSendDataRptAck((short)mReceiveDataLen, byDataType, (short)0x1);
            }
            else
            {
                System.arraycopy(data, 2, mReceiveData, mReceiveDataLen, nDataPayloadLen);
                mReceiveDataLen += nDataPayloadLen;

                configSendDataRptAck((short)mReceiveDataLen, byDataType, (short)0x0);
            }
        }
        else if (frameType == PDU_TAG_END)
        {
            if (nDataSeq != mReceiveDataLen || mReceiveDataLen + nDataPayloadLen > MAX_BUFFER_DATA_SIZE)
            {
                configSendDataRptAck((short)mReceiveDataLen, byDataType, (short)0x1);            }
            else
            {
                System.arraycopy(data, 2, mReceiveData, mReceiveDataLen, nDataPayloadLen);
                mReceiveDataLen += nDataPayloadLen;

                //configSendDataRptAck((short)mReceiveDataLen, byDataType, (short)0x0);
                bRcvDataCmp = true;
            }
        }
        else if (frameType == PDU_TAG_SINGLE)
        {
            //new read message command
            System.arraycopy(data, 2, mReceiveData, mReceiveDataLen, nDataPayloadLen);
            mReceiveDataLen += nDataPayloadLen;

            //configSendDataRptAck((short)mReceiveDataLen, byDataType, (short)0x0);
            bRcvDataCmp = true;
        }

        if (bRcvDataCmp)
        {
            Log.v(LOG_TAG, "receive report data complete:" + nDataSeq + ", expect seq:" + mReceiveDataLen);

            if (byDataType == PERP_CENT_DATA_RPT) {
                handleJsonRptDataComplete();
            }
            else if (byDataType == PERP_CENT_HEX_DATA_RPT)
            {
                handleHexRptDataComplete();
            }
        }
    }

    private void configSendDataRptAck(short nAckDataSeq, byte dataType, short cause)
    {
        ByteBuffer ackDataBuff = ByteBuffer.allocate(7);

        //ack head
        byte byAckHead = (byte)((dataType << 4) & 0xFF);
        byAckHead += PDU_TAG_SINGLE;
        ackDataBuff.put(byAckHead);

        //ack seq
        byte []ackDataSeq = KBUtility.htonbyte(nAckDataSeq);
        ackDataBuff.put(ackDataSeq);

        //windows
        short window = 1000;
        byte[] ackWindow = KBUtility.htonbyte(window);
        ackDataBuff.put(ackWindow);
        //cause
        byte[] ackCause = KBUtility.htonbyte(cause);
        ackDataBuff.put(ackCause);

        this.startWriteCfgValue(ackDataBuff.array());
    }

    private void handleHexRptDataComplete()
    {
        if (mActionStatus == ACTION_READ_SENSOR) {
            this.cancelActionTimer();

            byte[] validData = new byte[mReceiveDataLen];
            System.arraycopy(mReceiveData, 0, validData, 0, mReceiveDataLen);
            if (validData[0] == 0x10)
            {
                printBleLogMessage(validData);
            }
            else {
                if (mReadSensorCallback != null) {

                    ReadSensorCallback tempCallback = mReadSensorCallback;
                    mReadSensorCallback = null;
                    tempCallback.onReadComplete(true, validData, null);
                }
            }
        }
    }

    private void printBleLogMessage(byte[] logMessage)
    {
        byte[] byStrMessage = new byte[logMessage.length -1];
        System.arraycopy(logMessage, 1, byStrMessage, 0, byStrMessage.length);

        String jstrLogString = new String(byStrMessage);
        Log.e(LOG_TAG, jstrLogString);
    }

    public boolean isSupportSensorDataNotification()
    {
        if (getCharacteristicByID(KBUtility.KB_CFG_SERVICE_UUID, KBUtility.KB_IND_CHAR_UUID) != null)
        {
            return true;
        }
        return false;
    }

    private void handleJsonRptDataComplete()
    {
        HashMap<String, Object> dictRcvData = new HashMap<>(20);

        if (mReceiveDataLen == 0)
        {
            return;
        }

        byte[] validData = new byte[mReceiveDataLen];
        System.arraycopy(mReceiveData, 0, validData, 0, mReceiveDataLen);
        String jsonString = new String(validData);

        KBCfgBase.JsonString2HashMap(jsonString, dictRcvData);
        if (dictRcvData.size() == 0) {
            Log.e(LOG_TAG, "Parse Json response failed");
            if (mActionStatus == ACTION_INIT_READ_CFG) {
                closeBeacon(KBConnectionEvent.ConnException);
            } else if (mActionStatus == ACTION_USR_READ_CFG) {
                this.cancelActionTimer();
                if (mReadCfgCallback != null) {
                    ReadConfigCallback tempCallback = mReadCfgCallback;
                    mReadCfgCallback = null;

                    tempCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgReadNull,
                            "read data is null"));
                }
            }
        } else {
            //check if is connecting
            if (mActionStatus == ACTION_INIT_READ_CFG) {
                this.cancelActionTimer();

                //invalid connection timer
                mMsgHandler.removeMessages(MSG_CONNECT_TIMEOUT);

                //get configruation
                mCfgMgr.initConfigFromJsonDcits(dictRcvData);

                //change connection state
                if (isSupportSensorDataNotification() && notifyData2ClassMap.size() > 0) {
                    //enable indication for receive
                    mMsgHandler.sendEmptyMessageDelayed(MSG_NTF_IND_ENABLE, 100);
                }else{
                    Log.v(LOG_TAG, "read para complete, connect to device(" + mac + ") success");
                    state = KBStateConnected;
                    mMsgHandler.sendEmptyMessageDelayed(MSG_NTF_CONNECT_SUCCESS, 200);
                }
            } else if (mActionStatus == ACTION_USR_READ_CFG) {
                this.cancelActionTimer();

                if (mReadCfgCallback != null) {
                    ReadConfigCallback tempCallback = mReadCfgCallback;
                    mReadCfgCallback = null;
                    tempCallback.onReadComplete(true, dictRcvData, null);
                }
            } else {
                this.cancelActionTimer();

                Log.e(LOG_TAG, "receive data report error");
            }
        }
    }
	
	  private boolean configReadBeaconParamaters(int nReadCfgType, int nActionType)
    {
        if (ACTION_IDLE != mActionStatus)
        {
            Log.e(LOG_TAG, "last action command not complete");
            return false;
        }

        HashMap<String, Object> readCfgReq = new HashMap<>(10);
        readCfgReq.put(KBCfgBase.JSON_MSG_TYPE_KEY, KBCfgBase.JSON_MSG_TYPE_GET_PARA);
        readCfgReq.put(KBCfgBase.JSON_MSG_CFG_SUBTYPE, nReadCfgType);
        String strJsonCfgData = KBCfgBase.HashMap2JsonString(readCfgReq);
        if (strJsonCfgData == null || strJsonCfgData.length() == 0)
        {
            return false;
        }

        mByDownloadDatas = strJsonCfgData.getBytes(StandardCharsets.UTF_8);
        mByDownDataType = CENT_PERP_TX_JSON_DATA;
        mReceiveDataLen = 0;
        startNewAction(nActionType, MAX_READ_CFG_TIMEOUT);

        sendNextCfgData(0);

        return true;
    }
	
	 //write configruation to beacon
    private boolean startWriteCfgValue(byte[] data)
    {
        BluetoothGattCharacteristic characteristic = getCharacteristicByID(KBUtility.KB_CFG_SERVICE_UUID, KBUtility.KB_WRITE_CHAR_UUID);
        if (characteristic == null) {
            Log.e(LOG_TAG, ":startWriteCfgValue get CharacteristicByID failed.");
            return false;
        }

        characteristic.setValue(data);
        if (!mGattConnection.writeCharacteristic(characteristic)) {
            Log.e(LOG_TAG, ":startWriteCfgValue failed, data len:" + data.length);
            return false;
        }

        return true;
    }


    private BluetoothGattCharacteristic getCharacteristicByID(java.util.UUID srvUUID, java.util.UUID charaID) {
        if (mGattConnection == null) {
            Log.e(LOG_TAG, ":mBleGatt is null");
            return null;
        }

        BluetoothGattService service = mGattConnection.getService(srvUUID);
        if (service == null) {
            Log.e(LOG_TAG, ":getCharacteristicByID get services failed." + srvUUID);
            return null;
        }

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(charaID);
        if (characteristic == null) {
            Log.e(LOG_TAG, ":getCharacteristicByID get characteristic failed." + charaID);
            return null;
        }

        return characteristic;
    }

    private Handler mMsgHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                //start pair scan
                case MSG_CONNECT_TIMEOUT: {
                    connectingTimeout();
                    break;
                }

                case MSG_ACTION_TIME_OUT: {
                    actionTimeout();
                    break;
                }

                case MSG_SYS_CONNECTION_EVT: {
                    handleCentralBLEEvent(msg.arg1, msg.arg2);
                    break;
                }

                case MSG_SERVICES_DISCOVERD: {
                    startEnableNotification(KBUtility.KB_CFG_SERVICE_UUID, KBUtility.KB_NTF_CHAR_UUID);
                    break;
                }

                case MSG_NTF_IND_ENABLE: {
                    startEnableIndication(KBUtility.KB_CFG_SERVICE_UUID, KBUtility.KB_IND_CHAR_UUID, true);
                    break;
                }

                case MSG_START_AUTHENTICATION: {
                    mAuthHandler.authSendMd5Request(mac, mPassword);
                    break;
                }

                case MSG_BEACON_DATA_RECEIVED: {
                    handleBeaconNtfData((byte[]) msg.obj);
                    break;
                }

                case MSG_NTF_SUBSCRIBE_INDICATION_CMP:{
                    handleBeaconEnableSubscribeComplete();
                    break;
                }

                case MSG_BEACON_INDICATION_RECEIVED:{
                    handleBeaconIndData((byte[]) msg.obj);
                    break;
                }

                case MSG_CLOSE_CONNECTION_TIMEOUT:{
                    clearGattResource(mCloseReason);
                    break;
                }

                case MSG_START_REQUST_MAX_MTU:{
                    mGattConnection.requestMtu(MAX_MTU_SIZE);
                    break;
                }

                case MSG_NTF_CONNECT_SUCCESS:
                {
                    if (delegate != null){
                        if (KBeacon.this.isConnected()) {
                            delegate.onConnStateChange(KBeacon.this, KBStateConnected, KBConnectionEvent.ConnSuccess);
                        }
                    }
                    break;
                }
                default: {
                    break;
                }
            }

            return true;
        }
    });


    private void handleBeaconIndData(byte[] data)
    {
        int nDataType = data[0];
        SubscribeNotifyInstance sensorInstance = this.notifyData2ClassMap.get(nDataType);
        if (sensorInstance == null){
            return;
        }

        try {
            KBNotifyDataBase sensorDataBase = (KBNotifyDataBase)sensorInstance.notifyClass.newInstance();
            sensorDataBase.parseSensorDataResponse(this, data);
            if (sensorInstance.delegate != null) {
                sensorInstance.delegate.onNotifyDataReceived(KBeacon.this, nDataType, sensorDataBase);
            }
        }
        catch (Exception excpt)
        {
            excpt.printStackTrace();
        }
    }

    private void handleBeaconNtfData(byte[] data)
    {
        if (data.length < 2)
        {
            return;
        }

        byte byDataType = (byte)((data[0] >> 4) & 0xF);
        byte byFrameType = (byte)(data[0] & 0xF);
        byte[] ntfDataBody = new byte[data.length - 1];
        System.arraycopy(data, 1, ntfDataBody, 0, ntfDataBody.length);

        if (byDataType == DATA_TYPE_AUTH)
        {
            mAuthHandler.authHandleResponse(ntfDataBody);
        }
        else if (byDataType == PERP_CENT_TX_JSON_ACK || byDataType == PERP_CENT_TX_HEX_ACK)
        {
            this.configHandleDownCmdAck(byFrameType, byDataType, ntfDataBody);
        }
        else if (byDataType == PERP_CENT_DATA_RPT || byDataType == PERP_CENT_HEX_DATA_RPT)
        {
            this.configHandleReadDataRpt(byFrameType, byDataType, ntfDataBody);
        }
    }

    public class KBeaconGattCallback extends android.bluetooth.BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BluetoothDevice tmpBlePerp = gatt.getDevice();
            if (!mac.equals(tmpBlePerp.getAddress())) {
                return;
            }

            //update connection handle
            mGattConnection = gatt;

            //check if result is success
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(LOG_TAG, mac + "onConnectionStateChange connection fail, error code:" + status);
                Message msgCentralEvt = mMsgHandler.obtainMessage(MSG_SYS_CONNECTION_EVT, status, newState);
                mMsgHandler.sendMessage(msgCentralEvt);
            } else {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.e(LOG_TAG, mac + " onConnectionStateChange success");
                    mMsgHandler.sendEmptyMessageDelayed(MSG_START_REQUST_MAX_MTU, 100);
                } else{
                    Log.e(LOG_TAG, mac + " onConnectionStateChange detected other gatt fail:" + newState );
                    Message msgCentralEvt = mMsgHandler.obtainMessage(MSG_SYS_CONNECTION_EVT, status, newState);
                    mMsgHandler.sendMessage(msgCentralEvt);
                }
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.v(LOG_TAG, "The max mtu size is:" + mtu);
            }

            if (state == KBStateConnecting) {
                Message msgCentralEvt = mMsgHandler.obtainMessage(MSG_SYS_CONNECTION_EVT, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED);
                mMsgHandler.sendMessageDelayed(msgCentralEvt, 300);  //delay 200ms for next action
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothDevice tmpBlePerp = gatt.getDevice();
            if (!mac.equals(tmpBlePerp.getAddress())) {
                return;
            }

            mGattConnection = gatt;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //delay 100ms for next action
                mMsgHandler.sendEmptyMessageDelayed(MSG_SERVICES_DISCOVERD, 300);
            } else {
                //error
                Message msgCentralEvt = mMsgHandler.obtainMessage(MSG_SYS_CONNECTION_EVT, BluetoothGatt.GATT_FAILURE,
                        BluetoothGatt.STATE_DISCONNECTING);
                mMsgHandler.sendMessage(msgCentralEvt);
            }
        }


        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //check if success
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Message msgCentralEvt = mMsgHandler.obtainMessage(MSG_SYS_CONNECTION_EVT, BluetoothGatt.GATT_FAILURE,
                        BluetoothGatt.STATE_DISCONNECTING);
                mMsgHandler.sendMessage(msgCentralEvt);
            }else{
                UUID uuid = descriptor.getCharacteristic().getUuid();
                if (uuid.equals(KBUtility.KB_NTF_CHAR_UUID))
                {
                    if (state == KBStateConnecting) {
                        mMsgHandler.sendEmptyMessageDelayed(MSG_START_AUTHENTICATION, 100);
                    }
                }
                else if (uuid.equals(KBUtility.KB_IND_CHAR_UUID))
                {
					if (state == KBStateConnecting) 
					{
                        Log.v(LOG_TAG, "enable indication success, connection setup complete");
                        state = KBStateConnected;
                        mMsgHandler.sendEmptyMessageDelayed(MSG_NTF_CONNECT_SUCCESS, 300);
                    }
					else
					{                    
						Log.v(LOG_TAG, "enable indication success, connection setup complete");
                    	mMsgHandler.sendEmptyMessageDelayed(MSG_NTF_SUBSCRIBE_INDICATION_CMP, 100);
					}
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            String strMac = gatt.getDevice().getAddress();
            if (!strMac.equals(mac)) {
                Log.e(LOG_TAG, "on characteristic failed.");
                return;
            }

            UUID charUuid = characteristic.getUuid();
            if (charUuid.equals(KBUtility.KB_NTF_CHAR_UUID))
            {
                byte[] ntfData = characteristic.getValue();
                Message msg = mMsgHandler.obtainMessage(MSG_BEACON_DATA_RECEIVED, ntfData);
                mMsgHandler.sendMessageDelayed(msg, 100);
            }
            else if (charUuid.equals(KBUtility.KB_IND_CHAR_UUID))
            {
                byte[] ntfData = characteristic.getValue();
                Message msg = mMsgHandler.obtainMessage(MSG_BEACON_INDICATION_RECEIVED, ntfData);
                mMsgHandler.sendMessage(msg);
            }
        };
    }
}