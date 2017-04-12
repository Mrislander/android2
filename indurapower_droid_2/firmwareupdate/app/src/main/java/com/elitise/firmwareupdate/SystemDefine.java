package com.elitise.firmwareupdate;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andy.Xiang on 2/15/2017.
 */

public class SystemDefine {
    //
//    public static  String ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN = "AC55D02D-0XXX-4F64-ZZZZ-ABCB5C6D4099";
//
//    public static  String ELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN = "788C5B46-0XXX-473A-ZZZZ-49B981739482";


    public static  String ELT_BATT_MOBBAT_SERVICE_UUID_WITH_SN = "AC55D02D-0XXX-4F64-ZZZZ-ABCB5C6D409D";

    public static  String ELT_BATT_MOBBAT_CHARACTERISTIC_UUID_WITH_SN = "A88C5B46-0XXX-473A-ZZZZ-49B98173948D";

    public static  String[] batteryList = {"Series 108","Series 110","Series 120","Series 210","Series 220"};

    public static final byte[] CloseContactor = {-64, 104, 0, 4, 32, 77, 1, 0, 0, 0, -64};
    public static final byte[] OpenContactor =  {-64, 104, 0, 4, 32, 77, 0, 0, 0, 0, -64};
    public static final byte[] HeaterOn =       {-64, 102, 0, 4, 32, 64, 1, 0, 0, 0, -64};
    public static final byte[] HeaterOff =      {-64, 102, 0, 4, 32, 64, 2, 0, 0, 0, -64};
    public static final byte[] safeModeOn =     {-64, 110, 0, 4, 32, 75, 1, 0, 0, 0, -64};
    public static final byte[] safeModeOff =    {-64, 110, 0, 4, 32, 74, 0, 0, 0, 0, -64};
    public static final byte[] OneLastStartOn = {-64, 111, 0, 4, 32, 74, 1, 0, 0, 0, -64};

    public static final byte SLIP_END = (byte) 0xC0;
    public static final byte SLIP_ESC = (byte) 0xDB;
    public static final byte SLIP_ESC_END = (byte)0xDC;
    public static final byte SLIP_ESC_ESC = (byte)0xDD;


    public static class CMOMANDSTYPE{
        public static final int HOST_COMMANDS = 0;
        public static final int NVMEM_COMMANDS = 32;
        public static final int TEST_COMMANDS = 64;
        public static final int DEBUG_COMMANDS = 96;
        public static final int SERVICE_COMMANDS = 128;
        public static final int LAST_COMMAND_CLASS = 160;
    }


    public static class HOST_CMD{
        public static final int MIN_HOST_CMD = 0;
        public static final int GET_I_CHARGE = 1;
        public static final int SET_I_CHARGE = 2;
        public static final int GET_C_MON = 3;
        public static final int SET_C_MON = 4;
        public static final int GET_BUS_MON = 5;
        public static final int SET_BUS_MON = 6;
        public static final int GET_BANK_V = 7;
        public static final int SET_BANK_V = 8;
        public static final int GET_FUEL_CELL_V = 9;
        public static final int SET_FUEL_CELL_V = 10;
        public static final int GET_BODY_TEMPERATURE = 11;
        public static final int SET_BODY_TEMPERATURE = 12;
        public static final int GET_CELL_TEMPERATURE = 13;
        public static final int SET_CELL_TEMPERATURE = 14;
        public static final int SET_SOC_VAL = 15;
        public static final int GET_SOC_VAL = 16;
        public static final int SET_SOH_VAL = 17;
        public static final int GET_SOH_VAL = 18;
        public static final int SET_SOF_VAL = 19;
        public static final int GET_SOF_VAL = 20;
        public static final int SET_APP_VER = 21;
        public static final int SET_BOOT_VER = 22;
        public static final int SET_BAUD_RATE = 23;
        public static final int SET_BLE_MAC_ADDR = 24;
        public static final int SET_NUM_BATT_NODES = 25 ;
        public static final int SET_DIAG_MODE = 26 ;
        public static final int MAX_HOST_CMD = 27;
    }

    public static class DEBUG_CMD{
        public static final int MIN_DEBUG_CMD = 0;
        public static final int GET_CHARGE_STATE = 1;
        public static final int SET_CHARGE_STATE = 2;
        public static final int GET_IGNITION_STATE = 3;
        public static final int SET_IGNITION_STATE = 4;
        public static final int GET_HEATER_STATE = 5;
        public static final int SET_HEATER_STATE = 6;
        public static final int GET_CONTACTOR_STATE = 7;
        public static final int SET_CONTACTOR_STATE = 8;
        public static final int GET_SLEEP_STATE = 9;
        public static final int SET_SLEEP_STATE = 10;
        public static final int GET_BALANCER_STATE = 11;
        public static final int SET_BALANCER_STATE = 12;
        public static final int GET_SAFEMODE_STATE = 13;
        public static final int SET_SAFEMODE_STATE = 14;
        public static final int ONE_LAST_START = 15;
        public static final int FIRMWARE_UPDATE = 16;
        public static final int REMOTE_HEARTBEAT = 17;
        public static final int SET_SLEEP_STATE_ON = 18;
        public static final int SET_SLEEP_STATE_OFF = 19;
        public static final int PING_REQUEST = 20;
        public static final int PING_RESPONSE = 21;
        public static final int SET_BLE_CONNECTION_STATE = 22;
        public static final int GET_BLE_CONNECTION_STATE = 23;
        public static final int SET_REPROGRAM_MODE =24;
        public static final int GET_REPROGRAM_MODE =25;
        public static final int ENABLE_ACCIDENT_DETECTION = 26;
        public static final int SET_DEEP_CYCLE_MODE = 27;
        public static final int MAX_DEBUG_CMD = 28;
    }

    public static class connectionStatus{
        public static final int BLE_CONNECTION_CONNECTED = 0;
        public static final int BLE_CONNECTION_DISCONNECTED = 1;
        public static final int BLE_CONNECTION_AUTHENTICATION_SUCCESSFUL = 2;
        public static final int BLE_CONNECTION_FAILED_AUTHENTICATION = 3;
    }

    public static class SERVICE_CMD{
        public static final int ENTER_SERVICE_MODE = 0;
        public static final int EXIT_SERVICE_MODE = 1;
        public static final int SET_ALL_DATA = 2;
        public static final int SERIAL_NUMBER = 3;
        public static final int PART_NUMBER = 4;
        public static final int NUMBER_OF_CELLS = 5;
        public static final int SN_CELL = 6;
        public static final int BIRTH_DATE = 7;
        public static final int SUNRISE_DATE = 8;
        public static final int SUNSET_DATE = 9;
        public static final int C_MON_CAL = 10;
        public static final int INITIAL_SOC = 11;
        public static final int BLE_USE_HIGH_POWER = 12;
        public static final int CONTACTOR_SERIAL_NUMBER = 13;
        public static final int PCB_SERIAL_NUMBER = 14;
        public static final int WRITE_DATA = 15;
        public static final int REQUEST_DATA = 16;
        public static final int SET_RTC = 17;
        public static final int GET_RTC = 18;
        public static final int GET_CURRENT_PERFORMANCE_DATA = 19;
        public static final int GET_STORED_PERFORMANCE_DATA = 20;
        public static final int BLINK_LED = 21;
        public static final int FORCED_RESET = 22;
        public static final int DISCONNECT_ALL = 23;
        public static final int SET_CENTRAL_MAC_ADDRESS = 24;
        public static final int ADD_NODE_MAC_ADDRESS = 25;
        public static final int SET_VREF = 26;
        public static final int GET_VREF = 27;
        public static final int SET_WAKE_CYCLE = 26;
        public static final int SET_BT_TX_POWER = 27;
        public static final int FORCED_RESET_ALL = 28;
        public static final int RESET_SUNRISE_SUNSET_DATE = 29;
        public static final int BASE_SERIAL_NUMBER = 30;
        public static final int BASE_PART_NUMBER = 31;
        public static final int MAX_SERVICE_CMD = 32;

    }

    public static class MEM_CMD{
        public static final int START_MEM_XFER = 0;
        public static final int END_MEM_XFER = 1;
        public static final int VERIFY_IMAGE = 2;
        public static final int APPLY_MEM_UPDATE = 3;
        public static final int GET_XFER_BLOCK_SIZE = 4;
        public static final int BUFFER_IMG_DATA = 5;
        public static final int WRITE_MEM_BLOCK = 6;
        public static final int READ_MEM_BLOCK = 7;
        public static final int UPDATE_RESET = 8;
        public static final int SET_BAUDRATE = 9;
        public static final int GET_BAUDRATE = 10;
        public static final int NODE_UPLOAD_PROGRESS = 11;
    }

    public static class dataType{
        public static final int BOOLEAN = 0;
        public static final int INTEGERX = 1;
        public static final int FLOATINGP = 2;
        public static final int INTINDEXED = 3;
        public static final int FLTINDEXED = 4;
        public static final int STRINGCHARS = 5;
        public static final int MAX_TYPES = 6;
    };
    public static class rotationMode{
        public static final int ACTIVE_MODE = 0;
        public static final int STORAGE_MODE = 1;
        public static final int BLE_TOGGLE_MODE =2;
    }
    public static class BatteryMode{
        public static final int NormalMode = 0;
        public static final int UniDirectionMode = 1;
    }


    public static byte[] encodeCmd(int type, int cmdId, byte[] data, int datatype, int idx,int length){
        List<Byte> result = new ArrayList<>();
        result.add(SLIP_END);
        result.add((byte)(type+cmdId)); //cmd ID byte[0]
        // 2 bytes length byte[1] byte[2]
        byte[] lengthByte = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(length).array();
        result.add(lengthByte[0]);
        result.add(lengthByte[1]);
        //type and index byte[3] Todo:encode type
        result.add((byte)(idx));
        //checkSum byte[4]
        result.add((byte)0);
        //data parm byte[5] ~ byte[8]
        for (int i = 0;i<4;i++) {
            if(i<data.length)
                result.add(data[i]);
            else
                result.add((byte)0);
        }
        result.add(SLIP_END);
        return toByteArray(result);
    }
    public static byte[] toByteArray(List<Byte> in) {
        final int n = in.size();
        byte ret[] = new byte[n];
        for (int i = 0; i < n; i++) {
            ret[i] = in.get(i);
        }
        return ret;
    }

    public static class ValContainer<T> {
        private T val;

        public ValContainer() {
        }

        public ValContainer(T v) {
            this.val = v;
        }

        public T getVal() {
            return val;
        }

        public void setVal(T val) {
            this.val = val;
        }
    }

    public static float getCapacity(String mode){
        String temp = mode.trim().substring(0,12);
        float res  = 10f;
        //String temp = new String("010-01050-00");
        if(temp.equals("010-01050-00")) {
            res = 8f;
        }
        else if(temp.equals("010-01060-00")||temp.equals("010-01080-00")) {
            res = 10f;
        }
        else if(temp .equals("010-01070-00")|| temp.equals("010-01090-00")){
            res = 20f;
        }
        return  res;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


}
