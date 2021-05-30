package com.kbeacon.kbeaconlib.KBSensorHistoryData;

import com.kbeacon.kbeaconlib.ByteConvert;
import com.kbeacon.kbeaconlib.KBException;
import com.kbeacon.kbeaconlib.KBUtility;
import com.kbeacon.kbeaconlib.KBeacon;
import com.kbeacon.kbeaconlib.UTCTime;

import java.util.ArrayList;

public class KBProximityDataMsg extends KBSensorDataMsgBase {

    public class ReadSensorInfoRsp
    {
        public Integer readInfoRecordNumber;

        public Long readInfoUtcSeconds;
    };

    public class ReadSensorDataRsp
    {
        public Long readDataNextNum;

        public ArrayList<KBProximityRecord> readDataRspNearbyList;
    };

    public static final int KBSensorDataTypeProxmity = 1;
    static final long MIN_UTC_TIME_SECONDS = 946080000;

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
        if (sensorDataRsp.length -  nDataPtr < 2)
        {
            mReadSensorCallback.onReadComplete(false, null, new KBException(KBException.KBEvtCfgFailed, "data length error"));
            return;
        }

        ReadSensorInfoRsp infoRsp = new ReadSensorInfoRsp();
        infoRsp.readInfoRecordNumber = (int)ByteConvert.bytesTo2Int(sensorDataRsp, nDataPtr);

        if (sensorDataRsp.length -  nDataPtr >= 6) {
            infoRsp.readInfoUtcSeconds = (Long) ByteConvert.bytesTo4Long(sensorDataRsp, nDataPtr + 2);
            mUtcOffset = UTCTime.getUTCTimeSeconds() - infoRsp.readInfoUtcSeconds;
        }

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
        ReadSensorDataRsp readDataRsp = new ReadSensorDataRsp();
        readDataRsp.readDataNextNum = ByteConvert.bytesTo4Long(sensorDataRsp, nReadIndex);
        nReadIndex += 4;

        //check payload length
        int nPayLoad = (sensorDataRsp.length - nReadIndex);
        if (nPayLoad % 12 != 0)
        {
            readDataRsp.readDataNextNum = INVALID_DATA_RECORD_POS;
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

            KBProximityRecord record = new KBProximityRecord();

            //nearby time
            record.mNearbyTime = (sensorDataRsp[nRecordPtr++] & 0xFF);

            //mac address;
            byte[] byMacAddress = new byte[3];
            byMacAddress[0] = sensorDataRsp[nRecordPtr++];
            byMacAddress[1] = sensorDataRsp[nRecordPtr++];
            byMacAddress[2] = sensorDataRsp[nRecordPtr++];

            String strMacPrefex = beacon.getMac().substring(0, 8);
            String strMacTail = KBUtility.bytesToHexString(byMacAddress);
            if (strMacTail != null) {
                strMacTail = strMacTail.toUpperCase();
                record.mMacAddress = strMacPrefex +
                        ":" + strMacTail.substring(0, 2) +
                        ":" + strMacTail.substring(2, 4) +
                        ":" + strMacTail.substring(4, 6);
            }
            //utc time
            record.mNearbyUtcTime = ByteConvert.bytesTo4Long(sensorDataRsp, nRecordPtr);

            if (record.mNearbyUtcTime < MIN_UTC_TIME_SECONDS)
            {
                record.mNearbyUtcTime += mUtcOffset;
            }
            nRecordPtr += 4;

            record.mMajorID = ByteConvert.bytesTo2Int(sensorDataRsp, nRecordPtr);
            nRecordPtr += 2;

            record.mMinorID = ByteConvert.bytesTo2Int(sensorDataRsp, nRecordPtr);

            readDataRsp.readDataRspNearbyList.add(record);
        }

        mReadSensorCallback.onReadComplete(true, readDataRsp, null);
    }
}
