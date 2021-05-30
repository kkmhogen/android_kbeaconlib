package com.kbeacon.kbeaconlib;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.kbeacon.kbeaconlib.KBAdvPackage.KBAdvType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KBeaconsMgr {
    private final static String TAG = "beacon.KBeaconsMgr";


    public static final int SCAN_MODE_BALANCED = 1;
    public static final int SCAN_MODE_LOW_LATENCY = 2;
    public static final int SCAN_MODE_LOW_POWER = 0;
    public static final int SCAN_MODE_OPPORTUNISTIC = -1;

    public final static int SCAN_ERROR_NO_PERMISSION = 0x1;
    public final static int SCAN_ERROR_BLE_NOT_ENABLE = 0x2;
    public final static int SCAN_ERROR_UNKNOWN = 0x3;

    public final static int BLEStatePowerOn = 0;
    public final static int BLEStatePowerOff = 1;
    public final static int BLEStateUnknown = 2;

    public KBeaconMgrDelegate delegate;
    private int scanMode;
    private String scanNameFilter;
    private int advTypeFilter;
    private String advMacFilter;
    private boolean nameFilterIgnoreCase;

    private Integer scanMinRssiFilter = -100;

    private Context mContext;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    //all scaned beacons, include kkm beacon and other beacons
    private HashMap<String, KBeacon> mCbAllBeacons;

    //all kkm beacons
    private HashMap<String, KBeacon> mCbKBeacons;

    //the beacons that need notify to UI
    private HashMap<String, KBeacon> mCBNtfBeacons;

    private IntentFilter mIntentFilter;

    private NPhoneScancallback mNPhoneCallback;

    private boolean mIsScanning;

    private long mLastScanTick;

    private static KBeaconsMgr sharedStaticBeaconMgr = null;


    private final int MSG_NEW_DEVICE_FOUND = 201;
    private final int MSG_DELAY_ADV_REPORT = 202;
    private final int MAX_DELAY_TIMEOUT_INTERVAL = 300;

    public interface KBeaconMgrDelegate {
        void onBeaconDiscovered(KBeacon[] beacons);

        void onCentralBleStateChang(int nNewState);

        void onScanFailed(int errorCode);
    }

    //get the beacon manager instance
    public static KBeaconsMgr sharedBeaconManager(Context ctx)
    {
        if (sharedStaticBeaconMgr == null)
        {
            sharedStaticBeaconMgr = new KBeaconsMgr(ctx);
            if (!sharedStaticBeaconMgr.initialize()){
                sharedStaticBeaconMgr = null;
            }
        }

        return sharedStaticBeaconMgr;
    }

    //remove the single beacon manager instance
    public static void clearBeaconManager() {
        if (sharedStaticBeaconMgr != null)
        {
            sharedStaticBeaconMgr.clearBeacons();
            if (sharedStaticBeaconMgr.mReceiver != null) {
                sharedStaticBeaconMgr.mContext.unregisterReceiver(sharedStaticBeaconMgr.mReceiver);
                sharedStaticBeaconMgr.mReceiver = null;
            }
            sharedStaticBeaconMgr.delegate = null;
            sharedStaticBeaconMgr = null;
        }
    }


    private KBeaconsMgr(Context c){
        mContext = c;
    }

    private boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        mIntentFilter = makeGattUpdateIntentFilter();
        mContext.registerReceiver(mReceiver, mIntentFilter);

        mCbAllBeacons = new HashMap<>(100);

        mCbKBeacons = new HashMap<>(50);

        mCBNtfBeacons = new HashMap<>(10);

        mNPhoneCallback = new NPhoneScancallback();

        advTypeFilter = KBAdvType.KBAdvTypeEddyTLM | KBAdvType.KBAdvTypeSensor |
                KBAdvType.KBAdvTypeIBeacon | KBAdvType.KBAdvTypeEddyUID | KBAdvType.KBAdvTypeEddyURL;

        return true;
    }

    public HashMap Beacons()
    {
        return mCbKBeacons;
    }

    public void removeBeacon(String strMacAddress)
    {
        KBeacon beacon = mCbKBeacons.get(strMacAddress);
        if (beacon != null){
            beacon.disconnect();
        }
        mCbKBeacons.remove(strMacAddress);
        mCbAllBeacons.remove(strMacAddress);
        mCBNtfBeacons.remove(strMacAddress);
    }

    public void clearBeacons() {
        for (Map.Entry<String, KBeacon> entry : mCbKBeacons.entrySet()) {
            KBeacon kbeacon = entry.getValue();
            kbeacon.disconnect();
        }

        mCbAllBeacons.clear();

        mCbKBeacons.clear();

        mCBNtfBeacons.clear();
    }

    public boolean isBluetoothEnable()
    {
        if (mBluetoothAdapter == null){
            return false;
        }
        return mBluetoothAdapter.isEnabled();
    }

    public void setScanNameFilter(String strFilterName, boolean bCaseIgnore)
    {
        scanNameFilter = strFilterName;
        nameFilterIgnoreCase = bCaseIgnore;
    }

    public void setScanAdvTypeFilter(int advType)
    {
        advTypeFilter = advType;
    }

    public void setScanMacFilter(String strMacAddress)
    {
        advMacFilter = strMacAddress;
    }

    public String getScanNameFilter()
    {
        return scanNameFilter;
    }

    public void setScanMinRssiFilter(Integer nMinRssiFilter)
    {
        scanMinRssiFilter = nMinRssiFilter;
    }

    public void setScanMode(int nScanMode)
    {
        if (nScanMode == SCAN_MODE_BALANCED || nScanMode == SCAN_MODE_LOW_LATENCY
        || nScanMode == SCAN_MODE_LOW_POWER || nScanMode == SCAN_MODE_OPPORTUNISTIC) {
            scanMode = nScanMode;
        }
    }

    public Integer getScanMinRssiFilter()
    {
        return scanMinRssiFilter;
    }

    public KBeacon getBeacon(String strMacAddress)
    {
        return mCbKBeacons.get(strMacAddress);
    }

    public int startScanning()
    {
        if (!KBUtility.isLocationBluePermission(mContext))
        {
            Log.e(TAG, "No location permission for scanning.");
            return SCAN_ERROR_NO_PERMISSION;
        }

        if (!mBluetoothAdapter.isEnabled())
        {
            Log.e(TAG, "No location permission for scanning.");
            return SCAN_ERROR_BLE_NOT_ENABLE;
        }

        try {
            BluetoothLeScanner scaner = mBluetoothAdapter.getBluetoothLeScanner();

            if (mIsScanning) {
                Log.e(TAG, "current is scan, now start scan again");
                scaner.stopScan(mNPhoneCallback);
                mIsScanning = false;
            }

            if (scanMode !=  ScanSettings.SCAN_MODE_BALANCED &&
                    scanMode != ScanSettings.SCAN_MODE_LOW_LATENCY &&
                    scanMode != ScanSettings.SCAN_MODE_LOW_POWER &&
                    scanMode != ScanSettings.SCAN_MODE_OPPORTUNISTIC)
            {
                scanMode = ScanSettings.SCAN_MODE_BALANCED;
            }

            //start scan
            ScanSettings.Builder setsBuild;
            setsBuild = new ScanSettings.Builder().setScanMode(scanMode);
            scaner.startScan(null, setsBuild.build(), mNPhoneCallback);
            mLastScanTick = System.currentTimeMillis();
            mIsScanning = true;

            Log.e(TAG, "ble start scan success fully");
        }catch (RuntimeException excp)
        {
            Log.e(TAG, "start scan error" + excp.getCause());
            return SCAN_ERROR_UNKNOWN;
        }

        return 0;
    }

    public boolean isScanning()
    {
        return mIsScanning;
    }

    public void stopScanning()
    {
        BluetoothLeScanner scaner = mBluetoothAdapter.getBluetoothLeScanner();
        if (mIsScanning) {
            Log.e(TAG, "current is scan, now stop scanning");
            scaner.stopScan(mNPhoneCallback);
            mIsScanning = false;
        }
    }

    BluetoothManager getBleCentralMgr()
    {
        return mBluetoothManager;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return intentFilter;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                if (delegate == null){
                    return;
                }
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                if (state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_TURNING_ON) {
                    delegate.onCentralBleStateChang(BLEStatePowerOn);
                }
                else if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {
                    delegate.onCentralBleStateChang(BLEStatePowerOff);
                }
                else
                {
                    delegate.onCentralBleStateChang(BLEStateUnknown);
                }
            }
        }
    };

    private class NPhoneScancallback extends ScanCallback
    {
        public void onScanResult(int callbackType, ScanResult result) {
            if (result != null) {
                onDeviceFound(result);
            }
        }

        public void onBatchScanResults(List< ScanResult > results) {
            if (results.size() > 0){
                for(ScanResult rslt: results) {
                    onScanResult(10, rslt);
                }
            }else{
                Log.e(TAG, "Start N scan found 0 result");
            }
        }

        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Start N scan failed:" + errorCode);
            if (delegate != null){
                delegate.onScanFailed(errorCode);
            }
        }
    }

    private void onDeviceFound(final ScanResult rslt) {
        int rssi = rslt.getRssi();
        if (rssi < scanMinRssiFilter) {
            return;
        }

        if (scanNameFilter != null && scanNameFilter.length() >= 1) {
            String strDevName = rslt.getDevice().getName();
            if (strDevName != null){
                if (nameFilterIgnoreCase)
                {
                    String strDevNameLowCase = strDevName.toLowerCase();
                    String strFilterLowCase = scanNameFilter.toLowerCase();
                    if (!strDevNameLowCase.contains(strFilterLowCase)) {
                        return;
                    }
                }
                else if (!strDevName.contains(scanNameFilter)) {
                    return;
                }
            }
            else
            {
                return;
            }
        }

        if (advMacFilter != null && advMacFilter.length() >= 1)
        {
            String strAddress = rslt.getDevice().getAddress();
            if (strAddress != null){
                String strAddressLowCase = strAddress.toLowerCase();
                String strFilterLowCase = advMacFilter.toLowerCase();
                if (!strAddressLowCase.contains(strFilterLowCase)) {
                    return;
                }
            }
            else
            {
                return;
            }
        }

        Message msg = mMsgHandler.obtainMessage(MSG_NEW_DEVICE_FOUND);
        msg.obj = rslt;
        mMsgHandler.sendMessage(msg);
    }

    private Handler mMsgHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                //start pair scan
                case MSG_NEW_DEVICE_FOUND: {
                    ScanResult rslt = (ScanResult)msg.obj;
                    if (rslt != null) {
                        handleBleScanRslt(rslt);
                    }
                    break;
                }

                case MSG_DELAY_ADV_REPORT:{
                    delayReportAdvTimer();
                    break;
                }

                default:
                    break;
            }

            return true;
        }
    });

    private void delayReportAdvTimer()
    {
        if (mCBNtfBeacons.size() > 0)
        {
            KBeacon[] beacons = new KBeacon[mCBNtfBeacons.size()];
            mCBNtfBeacons.values().toArray(beacons);
            if (this.delegate != null) {
                this.delegate.onBeaconDiscovered(beacons);
            }
            mCBNtfBeacons.clear();
        }
    }

    private void handleBleScanRslt(final ScanResult rslt) {
        BluetoothDevice device = rslt.getDevice();
        String strMacAddress = device.getAddress();
        int rssi = rslt.getRssi();

        ScanRecord record = rslt.getScanRecord();
        if (record == null) {
            return;
        }
        String strDevName = record.getDeviceName();

        KBeacon pUnknownBeacon = null;
        boolean bParseAdvData = false;
        if (record.getBytes() != null && record.getBytes().length > 0) {
            pUnknownBeacon = mCbAllBeacons.get(strMacAddress);
            if (pUnknownBeacon == null) {
                pUnknownBeacon = new KBeacon(strMacAddress, mContext);
                pUnknownBeacon.setAdvTypeFilter(advTypeFilter);
                mCbAllBeacons.put(strMacAddress, pUnknownBeacon);
            }

            bParseAdvData = pUnknownBeacon.parseAdvPacket(record, rssi, strDevName);
        }

        if (bParseAdvData) {
            if (mCbKBeacons.get(strMacAddress) == null) {
                pUnknownBeacon.attach2Device(device, this);
                mCbKBeacons.put(strMacAddress, pUnknownBeacon);
            }

            //add to notify list
            mCBNtfBeacons.put(strMacAddress, pUnknownBeacon);

            if (!mMsgHandler.hasMessages(MSG_DELAY_ADV_REPORT)) {
                mMsgHandler.sendEmptyMessageDelayed(MSG_DELAY_ADV_REPORT, MAX_DELAY_TIMEOUT_INTERVAL);
            }
        }
    }
}
