package com.kkmcn.kbeaconlib.KBAdvPackage;

public class KBAdvType
{
    public final static int KBAdvTypeSensor = 0x01;
    public final static int KBAdvTypeEddyUID = 0x2;
    public final static int KBAdvTypeEddyTLM = 0x8;
    public final static int KBAdvTypeEddyURL = 0x10;
    public final static int KBAdvTypeIBeacon = 0x4;
    public final static int KBAdvTypeInvalid = 0x80;

    public final static String KBAdvTypeSensorString  = "KSensor";
    public final static String KBAdvTypeEddyUIDString  = "UID";
    public final static String KBAdvTypeIBeaconString = "iBeacon";
    public final static String KBAdvTypeEddyTLMString = "TLM";
    public final static String KBAdvTypeEddyURLString = "URL";
    public final static String KBAdvTypeInvalidString = "invalid";
}