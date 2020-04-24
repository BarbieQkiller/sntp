package main.java;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NtpMessage {
    private byte leapIndicator = 0;
    private byte version = 4;
    private byte mode = 0;
    private short stratum = 0;
    private byte pollInterval = 0;
    private byte precision = 0;
    private double rootDelay = 0;
    private double rootDispersion  = 0;
    private byte[] referenceIdentifier = {0, 0, 0, 0};
    private double referenceTimestamp = 0;
    private double originateTimestamp = 0;
    private double receiveTimestamp = 0;
    private double transmitTimestamp = 0;

    public NtpMessage(byte[] array) {
        byte b = array[0];
        leapIndicator = (byte)((b >> 6) & 0x3); // 0
        version = (byte)((b >> 3) & 0x7); // 4
        mode = (byte)(b & 0x7); // 4
        stratum = unsignedByteToShort(array[1]);
        pollInterval = array[2];
        precision = array[3];

        //array[4], array[5], array[6], array[7]
        rootDelay = (array[4] * 256.0) +
                unsignedByteToShort(array[5]) +
                (unsignedByteToShort(array[6])/256.0) +
                (unsignedByteToShort(array[7])/65536.0);

        //array[8], array[9], array[10], array[11]
        rootDispersion = (array[8] * 256.0) +
                unsignedByteToShort(array[9]) +
                (unsignedByteToShort(array[10])/256.0) +
                (unsignedByteToShort(array[11])/65536.0);

        referenceIdentifier[0] = array[12];
        referenceIdentifier[1] = array[13];
        referenceIdentifier[2] = array[14];
        referenceIdentifier[3] = array[15];

        //-30, 67, -18, 83, 0, 0, 0, 0
        referenceTimestamp = byteArrayToDouble(array, 16); 
        originateTimestamp = byteArrayToDouble(array, 24);
        receiveTimestamp = byteArrayToDouble(array, 32);
        transmitTimestamp = byteArrayToDouble(array, 40);
    }

    public NtpMessage() {
        mode = 3;
        transmitTimestamp = (System.currentTimeMillis()/1000.0) + 2208988800.0;
    }

    private double byteArrayToDouble(byte[] array, int position) {
        double result = 0.0;
        for(int i = 0; i < 8; i++) {
            result += unsignedByteToShort(array[position + i]) * Math.pow(2, (3-i)*8);
        }
        return result;
    }


    public void doubleToByteArray(byte[] array, int position, double d) {
        for(int i = 0; i < 8; i++) {
            array[position + i] = (byte) (d / Math.pow(2, (3-i)*8));
            d -= (double) (unsignedByteToShort(array[position + i]) * Math.pow(2, (3-i)*8));
        }
    }


    // 1000 0000
    //0000 0000 1000 0000
    private short unsignedByteToShort(byte b) {
        if((b & 0x80) == 0x80) {
            return (short)(128 + (b & 0x7f));
        }
        return (short) b;
    }

	public byte[] toByteArray() {
        byte[] array = new byte[48];
        // 0000 0000
        // 0010 0011
        array[0] = (byte) (leapIndicator << 6 | version << 3 | mode);
        array[1] = (byte) stratum;
        array[2] = pollInterval;
        array[3] = precision;

        int data = (int) (rootDelay * 65536.0);
        array[4] = (byte) ((data >> 24) & 0xff);
        array[5] = (byte) ((data >> 16) & 0xff);
        array[6] = (byte) ((data >> 8) & 0xff);
        array[7] = (byte) (data & 0xff);

        long rd = (long) (rootDispersion * 65536.0);
        array[8] = (byte) ((rd >> 24) & 0xff);
        array[9] = (byte) ((rd >> 16) & 0xff);
        array[10] = (byte) ((rd >> 8) & 0xff);
        array[11] = (byte) (rd & 0xff);

        array[12] = referenceIdentifier[0];
        array[13] = referenceIdentifier[1];
        array[14] = referenceIdentifier[2];
        array[15] = referenceIdentifier[3];
        doubleToByteArray(array, 16, referenceTimestamp);
        doubleToByteArray(array, 24, originateTimestamp);
        doubleToByteArray(array, 32, receiveTimestamp);
        doubleToByteArray(array, 40, transmitTimestamp);

        return array;
    }
    
    private String referenceIdentifierToString() {
        if(stratum == 0 || stratum == 1) {
            return new String(referenceIdentifier);
        }
        if(version==3) {
            return unsignedByteToShort(referenceIdentifier[0]) + "." +
            unsignedByteToShort(referenceIdentifier[1]) + "." +
            unsignedByteToShort(referenceIdentifier[2]) + "." +
            unsignedByteToShort(referenceIdentifier[3]) + ".";
        }
        else if(version==4) {
            return "" + ((unsignedByteToShort(referenceIdentifier[0]) / 256.0) +
            (unsignedByteToShort(referenceIdentifier[1]) / 65536.0) +
            (unsignedByteToShort(referenceIdentifier[2]) / 16777216.0)+
            (unsignedByteToShort(referenceIdentifier[3]) / 4294967296.0));
        }
        return "";
    } 

    public String toString() {
        String precisionString = new DecimalFormat("0.#E0").format(Math.pow(2, precision));
        return "Leap indicator: " + leapIndicator + "\n" +
        "Version: " + version + "\n" +
        "Mode: " + mode + "\n" +
        "Stratum: " + stratum + "\n" +
        "Poll: " + pollInterval + "\n" +
        "Precision: " + precision + "(" + precisionString + " seconds)\n" +
        "Root delay: " + new DecimalFormat("0.00").format(rootDelay*1000) + " ms\n" +
        "Root dispersion: " + new DecimalFormat("0.00").format(rootDispersion*1000) + " ms\n" +
        "Reference identifier: " + referenceIdentifierToString() + "\n" +
        "Reference timestamp: " + timestampToString(referenceTimestamp) + "\n" +
        "Orininate timestamp: " + timestampToString(originateTimestamp) + "\n" +
        "Receive timestamp: " + timestampToString(receiveTimestamp) + "\n" +
        "Transmit timestamp: " + timestampToString(transmitTimestamp);
    }

    public static String timestampToString(double timestamp) {
        // timestamp is realtive to 1 jan 1900, timestamp in java is relative
        // 1 jan 1970
        double utc = timestamp - (2208988800.0);
        long ms = (long) (utc * 1000);

        // Date/time
        String date = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date(ms));
        
        // fraction
        double fraction = timestamp -((long) timestamp);
        String fractionString = new DecimalFormat(".000000").format(fraction);
        return date + fractionString;
    }

    public double getOriginateTimestamp() {
        return originateTimestamp;
    }
    public double getTransmitTimestamp() {
        return transmitTimestamp;
    }
    public double getReceiveTimestamp() {
        return receiveTimestamp;
    }
}