package com.lepu.nordicble.ble.cmd.oxy;


import com.lepu.nordicble.ble.cmd.er1.Er1BleCRC;

import org.json.JSONException;
import org.json.JSONObject;

import static com.lepu.nordicble.utils.StringUtilsKt.makeTimeStr;

public class OxyBleCmd {

    public static int OXY_CMD_INFO = 0x14;
    public static int OXY_CMD_PARA_SYNC = 0x16;
    public static int OXY_CMD_RT_DATA = 0x1B;
    public static int OXY_CMD_RESET = 0x18;


    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 65535) {
            seqNo = 0;
        }
    }

    public static byte[] getInfo() {
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_INFO;
        buf[2] = (byte) ~OXY_CMD_INFO;
        buf[3] = (byte) seqNo;
        buf[4] = (byte) (seqNo >> 8);

        buf[7] = Er1BleCRC.calCRC8(buf);

        addNo();

        return buf;
    }

    public static byte[] syncTime() {
        long time = System.currentTimeMillis();
        JSONObject j = new JSONObject();
        try {
            j.put("SetTIME", makeTimeStr());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        char[] chars = j.toString().toCharArray();
        int size = chars.length;
        byte[] buf = new byte[8 + size];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_PARA_SYNC;
        buf[2] = (byte) ~OXY_CMD_PARA_SYNC;
        buf[3] = (byte) seqNo;
        buf[4] = (byte) (seqNo >> 8);
        buf[5] = (byte) size;
        buf[6] = (byte) (size >> 8);

        for (int i = 0; i<size; i++) {
            buf[7+i] = (byte) chars[i];
        }

        buf[8+size-1] = Er1BleCRC.calCRC8(buf);

        addNo();

        return buf;
    }

    public static byte[] getRtWave() {
        int len = 1;

        byte[] buf = new byte[8 + len];
        buf[0] = (byte) 0xAA;
        buf[1] = (byte) OXY_CMD_RT_DATA;
        buf[2] = (byte) ~OXY_CMD_RT_DATA;
        buf[3] = (byte) seqNo;
        buf[4] = (byte) (seqNo >> 8 );
        buf[5] = (byte) len;
        buf[6] = (byte) (len >> 8);

        buf[7] = (byte) 0;  // 0 -> 125hz;  1-> 62.5hz

        buf[8] = Er1BleCRC.calCRC8(buf);

        addNo();

        return buf;

    }
}
