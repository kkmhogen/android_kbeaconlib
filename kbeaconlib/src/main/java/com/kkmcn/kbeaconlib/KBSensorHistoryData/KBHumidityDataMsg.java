package com.kkmcn.kbeaconlib.KBSensorHistoryData;

import com.kkmcn.kbeaconlib.ByteConvert;
import com.kkmcn.kbeaconlib.KBException;
import com.kkmcn.kbeaconlib.KBUtility;
import com.kkmcn.kbeaconlib.KBeacon;
import com.kkmcn.kbeaconlib.UTCTime;

import java.util.ArrayList;

public class KBHumidityDataMsg extends KBSensorDataMsgBase{
    public class ReadHTSensorInfoRsp
    {
        public Integer totalRecordNumber;

        public Integer unreadRecordNumber;

        public Long readInfoUtcSeconds;
    };

    public class ReadHTSensorDataRsp
    {
        public Long readDataNextPos;

        public ArrayList<KBHumidityRecord> readDataRspList;
    };

    public static final int KBSensorDataTypeHumidity = 2;
    public static final long MIN_UTC_TIME_SECONDS = 946080000;

    private long mUtcOffset;

    public int getSensorDataType()
    {
        return KBSensorDataTypeHumidity;
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
            if (mReadSensorCallback != null) {
                mReadSensorCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgFailed, "data length error"));
            }
            return;
        }

        ReadHTSensorInfoRsp infoRsp = new ReadHTSensorInfoRsp();

        //total record number
        infoRsp.totalRecordNumber = (int) ByteConvert.bytesTo2Int(sensorDataRsp, nDataPtr);
        nDataPtr+=2;

        //total record number
        infoRsp.unreadRecordNumber = (int) ByteConvert.bytesTo2Int(sensorDataRsp, nDataPtr);
        nDataPtr+=2;

        //utc offset
        infoRsp.readInfoUtcSeconds = (Long) ByteConvert.bytesTo4Long(sensorDataRsp, nDataPtr );
        mUtcOffset = UTCTime.getUTCTimeSeconds() - infoRsp.readInfoUtcSeconds;
        nDataPtr += 4;
        mReadSensorCallback.onReadComplete(true, infoRsp, null);
    }

    public void parseSensorDataResponse(final KBeacon beacon, int nDataPtr, byte[] sensorDataRsp)
    {
        //sensor data type
        int nReadIndex = nDataPtr;
        if (sensorDataRsp[nReadIndex] != KBSensorDataTypeHumidity)
        {
            mReadSensorCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgFailed, "unknown sensor"));
            return;
        }
        nReadIndex++;

        //next read data pos
        ReadHTSensorDataRsp readDataRsp = new ReadHTSensorDataRsp();
        readDataRsp.readDataNextPos = ByteConvert.bytesTo4Long(sensorDataRsp, nReadIndex);
        nReadIndex += 4;

        //check payload length
        int nPayLoad = (sensorDataRsp.length - nReadIndex);
        if (nPayLoad % 8 != 0)
        {
            readDataRsp.readDataNextPos = INVALID_DATA_RECORD_POS;
            mReadSensorCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgFailed, "data length error"));
            return;
        }

        //read record
        readDataRsp.readDataRspList = new ArrayList<>(30);
        int nRecordStartPtr = nReadIndex;
        int nTotalRecordLen = nPayLoad / 8;
        for (int i = 0; i < nTotalRecordLen; i++)
        {
            int nRecordPtr = nRecordStartPtr + i * 8;

            KBHumidityRecord record = new KBHumidityRecord();

            //utc time
            record.mUtcTime = ByteConvert.bytesTo4Long(sensorDataRsp, nRecordPtr);
            if (record.mUtcTime < MIN_UTC_TIME_SECONDS)
            {
                record.mUtcTime += mUtcOffset;
            }
            nRecordPtr += 4;


            record.mTemperature = KBUtility.signedBytes2Float(sensorDataRsp[nRecordPtr], sensorDataRsp[nRecordPtr+1]);
            nRecordPtr += 2;

            record.mHumidity = KBUtility.signedBytes2Float(sensorDataRsp[nRecordPtr], sensorDataRsp[nRecordPtr+1]);
            nRecordPtr += 2;

            readDataRsp.readDataRspList.add(record);
        }

        mReadSensorCallback.onReadComplete(true, readDataRsp, null);
    }
}
