package com.kkmcn.kbeaconlib.KBCfgPackage;

public class KBCfgSleepTime {
    public byte mSleepStartHour;
    public byte mSleepStartMinute;
    public byte mSleepEndHour;
    public byte mSleepEndMinute;

    public KBCfgSleepTime()
    {
        mSleepStartHour = mSleepStartMinute = mSleepEndHour = mSleepEndMinute = 0;
    }
}
