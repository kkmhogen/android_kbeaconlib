package com.kbeacon.kbeaconlib.KBSensorHistoryData;

import com.kbeacon.kbeaconlib.ByteConvert;
import com.kbeacon.kbeaconlib.KBException;
import com.kbeacon.kbeaconlib.KBeacon;
import com.kbeacon.kbeaconlib.UTCTime;

import java.util.ArrayList;

public class KBProximityDmDataMsg extends KBSensorDataMsgBase {

    public class ReadDmSensorInfoRsp
    {
        public Integer readInfoRecordNumber;

        public Long readInfoUtcSeconds;

        public Integer lastMessageID;

        public Integer firstMessageID;
    };

    public class ReadSensorDmDataRsp
    {
        public Long readDataNextMsgID;

        public ArrayList<KBProximityDmRecord> readDataRspNearbyList;
    };

    public static final int KBSensorDataTypeProxmity = 1;
    public static final long MIN_UTC_TIME_SECONDS = 946080000;

    private long mUtcOffset;

    public int getSensorDataType()
    {
        return KBSensorDataTypeProxmity;
    }

    public byte[] makeReadSensorDataReq(long nReadRcdNo, int nReadOrder, int nMaxRecordNum)
    {
        byte[] byMsgReq = new byte[7];
        int nIndex = 0;

        //read pos
        byMsgReq[nIndex++] = (byte)((nReadRcdNo >> 24) & 0xFF);
        byMsgReq[nIndex++] = (byte)((nReadRcdNo >> 16) & 0xFF);
        byMsgReq[nIndex++] = (byte)((nReadRcdNo >> 8) & 0xFF);
        byMsgReq[nIndex++] = (byte)(nReadRcdNo  & 0xFF);

        //read num
        byMsgReq[nIndex++] = (byte)((nMaxRecordNum >> 8) & 0xFF);
        byMsgReq[nIndex++] = (byte)(nMaxRecordNum & 0xFF);

        //read direction
        byMsgReq[nIndex] = (byte)nReadOrder;

        return byMsgReq;
    }

    public void parseSensorInfoResponse(final KBeacon beacon, int nDataPtr, byte[] sensorDataRsp)
    {
        if (sensorDataRsp.length -  nDataPtr < 8)
        {
            mReadSensorCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgFailed, "data length error"));
            return;
        }

        ReadDmSensorInfoRsp infoRsp = new ReadDmSensorInfoRsp();

        //total record number
        infoRsp.readInfoRecordNumber = (int) ByteConvert.bytesTo2Int(sensorDataRsp, nDataPtr);
        nDataPtr+=2;

        //utc offset
        infoRsp.readInfoUtcSeconds = (Long) ByteConvert.bytesTo4Long(sensorDataRsp, nDataPtr );
        mUtcOffset = UTCTime.getUTCTimeSeconds() - infoRsp.readInfoUtcSeconds;
        nDataPtr += 4;

        //get last data message
        infoRsp.lastMessageID = (int) ByteConvert.bytesTo2Int(sensorDataRsp, nDataPtr);
        nDataPtr += 2;

        //get first data message
        infoRsp.firstMessageID = (int) ByteConvert.bytesTo2Int(sensorDataRsp, nDataPtr);
        nDataPtr += 2;

        mReadSensorCallback.onReadComplete(true, infoRsp, null);
    }

    public void parseSensorDataResponse(final KBeacon beacon, int nDataPtr, byte[] sensorDataRsp)
    {
        //sensor data type
        int nReadIndex = nDataPtr;
        if (sensorDataRsp[nReadIndex] != KBSensorDataTypeProxmity)
        {
            mReadSensorCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgFailed, "unknown sensor"));
            return;
        }
        nReadIndex++;

        //next read data pos
        ReadSensorDmDataRsp readDataRsp = new ReadSensorDmDataRsp();
        readDataRsp.readDataNextMsgID = ByteConvert.bytesTo4Long(sensorDataRsp, nReadIndex);
        nReadIndex += 4;

        //check payload length
        int nPayLoad = (sensorDataRsp.length - nReadIndex);
        if (nPayLoad % 12 != 0)
        {
            readDataRsp.readDataNextMsgID = INVALID_DATA_RECORD_POS;
            mReadSensorCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgFailed, "data length error"));
            return;
        }

        //read record
        readDataRsp.readDataRspNearbyList = new ArrayList<>(30);
        int nRecordStartPtr = nReadIndex;
        int nTotalRecordLen = nPayLoad / 12;
        for (int i = 0; i < nTotalRecordLen; i++)
        {
            int nRecordPtr = nRecordStartPtr + i * 12;

            KBProximityDmRecord record = new KBProximityDmRecord();

            //nearby time
            record.mNearbyTime = (sensorDataRsp[nRecordPtr++] & 0xFF) * 2;

            //distance
            record.mDistance = (sensorDataRsp[nRecordPtr++] & 0xFF);

            //message id
            record.mMessageID = ByteConvert.bytesTo2Int(sensorDataRsp, nRecordPtr);
            nRecordPtr += 2;

            //utc time
            record.mNearbyUtcTime = ByteConvert.bytesTo4Long(sensorDataRsp, nRecordPtr);
            if (record.mNearbyUtcTime < MIN_UTC_TIME_SECONDS)
            {
                record.mNearbyUtcTime += mUtcOffset;
            }
            nRecordPtr += 4;

            record.mDeviceID = (int)ByteConvert.bytesTo4Long(sensorDataRsp, nRecordPtr);

            readDataRsp.readDataRspNearbyList.add(record);
        }

        mReadSensorCallback.onReadComplete(true, readDataRsp, null);
    }
}

