package com.kkmcn.kbeaconlib.KBSensorHistoryData;

import com.kkmcn.kbeaconlib.ByteConvert;
import com.kkmcn.kbeaconlib.KBCfgPackage.KBCfgTrigger;
import com.kkmcn.kbeaconlib.KBException;
import com.kkmcn.kbeaconlib.KBeacon;


public abstract class KBSensorDataMsgBase extends KBCfgTrigger {
    public static final int READ_RECORD_REVERSE_ORDER = 1;
    public static final int READ_RECORD_ORDER = 0;
    public static final int READ_RECORD_NEW_RECORD = 2;

    private static final int MSG_READ_SENSOR_INFO_REQ = 1;

    private static final int MSG_READ_SENSOR_DATA_REQ = 2;

    private static final int MSG_CLR_SENSOR_DATA_REQ = 3;

    private static final int MSG_READ_SENSOR_INFO_RSP = 0;

    private static final int MSG_READ_HT_SENSOR_INFO_RSP = 1;

    private static final int MSG_READ_SENSOR_DATA_RSP = 2;


    public static final long INVALID_DATA_RECORD_POS = 4294967295L;

    protected ReadSensorCallback mReadSensorCallback;

    public interface ReadSensorCallback {
        void onReadComplete(boolean bConfigSuccess, Object obj, KBException error);
    }

    abstract public int getSensorDataType();

    abstract public byte[] makeReadSensorDataReq(long nReadRcdNo, int nReadOrder, int nMaxRecordNum);

    abstract public void parseSensorDataResponse(final KBeacon beacon, int nDataPtr, byte[] sensorDataRsp);

    abstract public void parseSensorInfoResponse(final KBeacon beacon, int nDataPtr, byte[] sensorDataRsp);

    //////////////////////////////////////////////////////////////////////////////////////////////////
    public void readSensorDataInfo(final KBeacon beacon, ReadSensorCallback readCallback) {
        byte [] bySensorInfoReq = new byte[2];

        bySensorInfoReq[0] = MSG_READ_SENSOR_INFO_REQ;
        bySensorInfoReq[1] = (byte)getSensorDataType();;
        mReadSensorCallback = readCallback;

        //send message
        beacon.sendSensorRequest(bySensorInfoReq, new KBeacon.ReadSensorCallback() {
            @Override
            public void onReadComplete(boolean bReadResult, byte[] readPara, KBException error) {
                if (bReadResult) {
                    //tag
                    if (readPara.length < 2 ||
                            (readPara[0] != MSG_READ_SENSOR_INFO_RSP && readPara[0] != MSG_READ_HT_SENSOR_INFO_RSP)) {
                        mReadSensorCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgFailed, "unknown error"));
                        return;
                    }

                    parseSensorInfoResponse(beacon, 2, readPara);
                }
                else {
                    ReadSensorCallback tempCallback = mReadSensorCallback;
                    mReadSensorCallback = null;
                    tempCallback.onReadComplete(false, null, error);
                }
            }
        });
    }

    public void readSensorRecord(final KBeacon beacon, long nReadRcdNo, int nReadOrder, int nMaxRecordNum, final ReadSensorCallback readCallback) {
        byte[] byMakeReadSensorDataReq = makeReadSensorDataReq(nReadRcdNo, nReadOrder, nMaxRecordNum);
        if (byMakeReadSensorDataReq == null) {
            readCallback.onReadComplete(false,  null, new KBException(KBException.KBEvtCfgFailed, "unknown error"));
            return;
        }

        byte[] bySensorDataReq = new byte[byMakeReadSensorDataReq.length + 2];
        System.arraycopy(byMakeReadSensorDataReq, 0, bySensorDataReq, 2, byMakeReadSensorDataReq.length);
        bySensorDataReq[0] = MSG_READ_SENSOR_DATA_REQ;
        bySensorDataReq[1] = (byte)getSensorDataType();

        //send message
        mReadSensorCallback = readCallback;
        beacon.sendSensorRequest(bySensorDataReq, new KBeacon.ReadSensorCallback() {
            @Override
            public void onReadComplete(boolean bReadResult, byte[] readPara, KBException error) {
            if (bReadResult) {
                //tag
                if (readPara.length < 2 || readPara[0] != MSG_READ_SENSOR_DATA_RSP) {
                    ReadSensorCallback tempCallback = mReadSensorCallback;
                    mReadSensorCallback = null;
                    tempCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgFailed, "unknown error"));
                    return;
                }

                //data length
                int nReadIndex = 1;
                short nDataLen = ByteConvert.bytesToShort(readPara, nReadIndex);
                nReadIndex += 2;

                //data content
                if (nDataLen != readPara.length - 3) {
                    ReadSensorCallback tempCallback = mReadSensorCallback;
                    mReadSensorCallback = null;
                    tempCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgFailed, "unknown error"));
                    return;
                }

                parseSensorDataResponse(beacon, 3, readPara);
            } else {
                ReadSensorCallback tempCallback = mReadSensorCallback;
                mReadSensorCallback = null;
                tempCallback.onReadComplete(false, null, error);
            }
            }
        });
    }

    public void clearSensorRecord(final KBeacon beacon,final ReadSensorCallback readCallback) {
        byte [] bySensorInfoReq = new byte[2];
        bySensorInfoReq[0] = MSG_CLR_SENSOR_DATA_REQ;
        bySensorInfoReq[1] = (byte)getSensorDataType();;
        mReadSensorCallback = readCallback;

        //send message
        beacon.sendSensorRequest(bySensorInfoReq, new KBeacon.ReadSensorCallback() {
            @Override
            public void onReadComplete(boolean bReadResult, byte[] readPara, KBException error) {
                ReadSensorCallback tempCallback = mReadSensorCallback;
                mReadSensorCallback = null;
                tempCallback.onReadComplete(bReadResult, null, error);
            }
        });
    }
}
