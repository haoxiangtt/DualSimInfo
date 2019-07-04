package cn.bfy.dualsiminfo.demo;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bfy.dualsim.DualsimBase;
import cn.bfy.dualsim.TelephonyManagement;
import cn.bfy.dualsiminfo.demo.utils.TelephonyUtil;
import cn.bfy.dualsiminfo.demo.utils.PermissionUtils;
import cn.richinfo.dualsiminfo.demo.R;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "DualCard";
    public static final String SENT_SMS_ACTION = "umc_sent_sms_action";

    private StringBuilder data;
    private TelephonyManagement.TelephonyInfo telephonyInfo;

    private TextView mText;
    private TextView mText2;

    private EditText etPhoneNumber;
    private EditText etContent;
    private RadioGroup rgSimCard;
    private RadioButton rbSim1;
    private RadioButton rbSim2;
    private Button btSend;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = getResultCode();
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "发送失败,resultCode = " +resultCode, Toast.LENGTH_SHORT).show();
            }
            checkSimCard();
        }
    };

//    private SubscriptionManager mSubscriptionManager;
//    public List<SubscriptionInfo> subInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        data = new StringBuilder();
//        mSubscriptionManager = SubscriptionManager.from(MainActivity.this);
        mText = (TextView) findViewById(R.id.text_info);
        mText2 = (TextView) findViewById(R.id.text_info2);
        findViewById(R.id.btn_read).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, 100);
                } else {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        isDualSimOrNot();
//                    } else {
//                        Toast.makeText(MainActivity.this, "android 版本过低！", Toast.LENGTH_SHORT).show();
//                    }
                }
            }
        });
        etPhoneNumber = (EditText)findViewById(R.id.et_phone_number);
        etContent = (EditText) findViewById(R.id.et_content);
        rgSimCard = (RadioGroup) findViewById(R.id.rg_sim);
        rbSim1 = (RadioButton) findViewById(R.id.rb_sim1);
        rbSim2 = (RadioButton) findViewById(R.id.rb_sim2);
        btSend = (Button) findViewById(R.id.bt_send);

        rgSimCard.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.rb_sim1) {
                    etPhoneNumber.setText(TelephonyUtil.getSendNumber(MainActivity.this, telephonyInfo.getOperatorSIM1()));
                    etContent.setText(TelephonyUtil.getSmsContent(MainActivity.this, telephonyInfo.getImsiSIM1(),
                        telephonyInfo.getOperatorSIM1()));
                } else {
                    etPhoneNumber.setText(TelephonyUtil.getSendNumber(MainActivity.this, telephonyInfo.getOperatorBySlotId(
                            DualsimBase.TYPE_SIM_ASSISTANT)));
                    etContent.setText(TelephonyUtil.getSmsContent(MainActivity.this, telephonyInfo.getSubscriberIdBySlotId(
                            DualsimBase.TYPE_SIM_ASSISTANT), telephonyInfo.getOperatorBySlotId(DualsimBase.TYPE_SIM_ASSISTANT)));
                }
            }
        });

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PermissionUtils.isNeedRequestPermission()
                        && ! PermissionUtils.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)) {
                    PermissionUtils.requestPermission(MainActivity.this,
                        new String[]{Manifest.permission.SEND_SMS}, 110);
                } else {
                    sendMessage();
                }
            }
        });

        if (!PermissionUtils.isNeedRequestPermission()) {
            checkSimCard();
        } else {
            if (!PermissionUtils.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
                PermissionUtils.requestPermission(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 100);
            } else {
                checkSimCard();
            }
        }

        Log.e("TAG", "---------------------------------------");
//        TelephonyUtil.printTelephonyManagerMethodNamesForThisDevice(this);
        Log.e("TAG", "---------------------------------------");
//        TelephonyInfo.samsungTwoSims(this);
        Log.e("TAG", "---------------------------------------");

        registerReceiver(receiver, new IntentFilter(SENT_SMS_ACTION));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void sendMessage() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String content = etContent.getText().toString();
        int simID = rgSimCard.getCheckedRadioButtonId() == R.id.rb_sim1 ?
            telephonyInfo.getSlotIdSIM1() : DualsimBase.TYPE_SIM_ASSISTANT;
        PendingIntent sendPending = PendingIntent.getBroadcast(this, 0, new Intent(SENT_SMS_ACTION), 0);
        boolean flag = TelephonyManagement.getInstance().getDualSimChip(this).sendDataMessage(
            phoneNumber, null, (short)0, content.getBytes(), sendPending, null, simID);
        if (flag) {
            btSend.setEnabled(false);
        }
    }

    private void checkSimCard() {
        telephonyInfo = TelephonyManagement.getInstance().updateTelephonyInfo(this).getTelephonyInfo(this);
        if (!telephonyInfo.isDualSIM()) {
            if (telephonyInfo.getSlotIdSIM1() == DualsimBase.TYPE_SIM_MAIN) {
                rbSim2.setEnabled(false);
                rbSim1.setEnabled(true);
                rgSimCard.check(R.id.rb_sim1);
                btSend.setEnabled(true);
            } else if (telephonyInfo.getSlotIdSIM1() == DualsimBase.TYPE_SIM_ASSISTANT) {
                rbSim1.setEnabled(false);
                rbSim2.setEnabled(true);
                rgSimCard.check(R.id.rb_sim2);
                btSend.setEnabled(true);
            } else {
                rbSim1.setEnabled(false);
                rbSim2.setEnabled(false);
                rgSimCard.check(-1);
                btSend.setEnabled(false);
            }
        } else {
            rbSim1.setEnabled(true);
            rbSim2.setEnabled(true);
            rgSimCard.check(R.id.rb_sim1);
            btSend.setEnabled(true);
        }

        if (rgSimCard.getCheckedRadioButtonId() == R.id.rb_sim1) {
            etPhoneNumber.setText(TelephonyUtil.getSendNumber(MainActivity.this, telephonyInfo.getOperatorSIM1()));
            etContent.setText(TelephonyUtil.getSmsContent(MainActivity.this, telephonyInfo.getImsiSIM1(),
                telephonyInfo.getOperatorSIM1()));
        } else if (rgSimCard.getCheckedRadioButtonId() == R.id.rb_sim2){
            etPhoneNumber.setText(TelephonyUtil.getSendNumber(MainActivity.this, telephonyInfo.getOperatorBySlotId(
                    DualsimBase.TYPE_SIM_ASSISTANT)));
            etContent.setText(TelephonyUtil.getSmsContent(MainActivity.this, telephonyInfo.getSubscriberIdBySlotId(
                    DualsimBase.TYPE_SIM_ASSISTANT), telephonyInfo.getOperatorBySlotId(DualsimBase.TYPE_SIM_ASSISTANT)));
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isDualSimOrNot();
            } else {
                Toast.makeText(MainActivity.this, "用户拒绝授权！", Toast.LENGTH_SHORT).show();
            }
        } else  if (requestCode == 110) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendMessage();
            } else {
                Toast.makeText(MainActivity.this, "用户拒绝授权！", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String strSimSlotInfo(int slotid){
        if (slotid == 0) {
            return "主卡";
        } else if (slotid == 1) {
            return "副卡";
        } else {
            return "未知";
        }
    }

    private String strSimOperatorInfo(String op){
        if("46000".equals(op) || "46002".equals(op) || "46007".equals(op)) {
            return "移动运营商";
        } else if ("46001".equals(op)) {
            return "联通运营商";
        } else if ("46003".equals(op)) {
            return "电信运营商";
        } else {
            return "未知运营商";
        }
    }

    private String strNetWorkInfo(int state){
        if (state == -1) {
            return "无网络";
        } else if (state == 0) {
            return "数据网络";
        } else if (state == 1) {
            return "Wifi网络";
        } else {
            return "未知";
        }
    }

    private void isDualSimOrNot() {
        Log.e("TAG", "---------------------------------------");
        if (data.length() > 1) {
            data.delete(0, data.length() - 1);
        }
        Log.e("TAG", data.toString());
//        TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
//        UMCTelephonyManagement utm = UMCTelephonyManagement.getInstance();
//        telephonyInfo = utm.updateTelephonyInfo(this).getTelephonyInfo(this);
        checkSimCard();
        String imeiSIM1 = telephonyInfo.getImeiSIM1();
        String imeiSIM2 = telephonyInfo.getImeiSIM2();

        String imsiSIM1 = telephonyInfo.getImsiSIM1();
        String imsiSIM2 = telephonyInfo.getImsiSIM2();

        int isSIM1Ready = telephonyInfo.getStateSIM1();
        int isSIM2Ready = telephonyInfo.getStateSIM2();

        boolean isDualSIM = telephonyInfo.isDualSIM();
//        int networkState = telephonyInfo.getNetworkState();
        int slotId = telephonyInfo.getDefaultDataSlotId();
        data.append(" IMEI1 : " + imeiSIM1 + "\n" +
                " IMEI2 : " + imeiSIM2 + "\n" +
                " IMSI1 : " + imsiSIM1 + "\n" +
                " IMSI2 : " + imsiSIM2 + "\n" +
                " SlotId1 : " + telephonyInfo.getSlotIdSIM1() +
                    "(" + strSimSlotInfo(telephonyInfo.getSlotIdSIM1()) + ")\n" +
                " SlotId2 : " + telephonyInfo.getSlotIdSIM2() +
                    "(" + strSimSlotInfo(telephonyInfo.getSlotIdSIM2()) + ")\n" +
                " SubId1 : " + telephonyInfo.getSubIdSIM1() + "(sim卡1索引id)\n" +
                " SubId2 : " + telephonyInfo.getSubIdSIM2() + "(sim卡2索引id)\n" +
                " Operator1 : " + telephonyInfo.getOperatorSIM1() +
                    "(" + strSimOperatorInfo(telephonyInfo.getOperatorSIM1()) + ")\n" +
                " Operator2 : " + telephonyInfo.getOperatorSIM2() +
                    "(" + strSimOperatorInfo(telephonyInfo.getOperatorSIM2()) + ")\n" +
                " IS SIM1 READY : " + isSIM1Ready + "\n" +
                " IS SIM2 READY : " + isSIM2Ready + "\n" +
                " IS DUAL SIM : " + isDualSIM + "\n" +
//                " NETWORK STATE :  " + networkState +
//                    "(" + strNetWorkInfo(networkState) + ")\n" +
                " Default data slotId :  " + slotId +
                "(默认数据网络卡：" + strSimSlotInfo(slotId) + ")\n" +
                " Default data imei : " + telephonyInfo.getDeviceIdBySlotId(slotId) + "\n" +
                " Default data imsi : " + telephonyInfo.getSubscriberIdBySlotId(slotId) + "\n" +
                " chip : " + telephonyInfo.getChip() + "\n" +
                " Manufacturer : " + Build.MANUFACTURER + "\n" +
                " Model : " + Build.MODEL + "\n" +
                " Mac : " + getAdresseMAC(this) + "\n" +
                " -----------------------------------\n");
        data.append(" -----------------------------------\n");
        Log.e(TAG, data.toString());
        Log.e("TAG", "---------------------------------------");
        mText.setText(data.toString());
        getIdInDB();
    }

    private void getIdInDB(){
        Uri uri = Uri.parse("content://telephony/siminfo");
        Cursor cursor = null;
        ContentResolver resolver = getApplicationContext().getContentResolver();
        try{
            cursor = resolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() != 0) {
                List<String> list = new ArrayList<String>();
                cursor.moveToFirst();
                do {
                    StringBuilder row = new StringBuilder();
                    for (int i = 0 ; i < cursor.getColumnCount(); i++) {
                        row.append(cursor.getColumnName(i) + ":"
                                + cursor.getString(i) + ",");
                    }
                    row.append("\n\n");
                    list.add(row.toString());
                } while (cursor.moveToNext());
                StringBuilder data = new StringBuilder();
                for(String str : list){
                    data.append("row: " + str);
                    Log.e(TAG,"row: " + str);
                }
                data.append("--------------------------------\n");
                Log.e(TAG,"--------------------------------\n");
                mText2.setText(data.toString());
            } else {
                Log.e(TAG,">>>>>>>>>>>>the db cursor is null<<<<<<<<<<<");
                mText2.setText("没有相关的数据库存在\n\n--------------------------------\n");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
    }

    private static final String marshmallowMacAddress = "02:00:00:00:00:00";
    private static final String fileAddressMac = "/sys/class/net/wlan0/address";

    public static String getAdresseMAC(Context context) {
        WifiManager wifiMan = (WifiManager)context.getSystemService(Context.WIFI_SERVICE) ;
        WifiInfo wifiInf = wifiMan.getConnectionInfo();

        if(wifiInf !=null && marshmallowMacAddress.equals(wifiInf.getMacAddress())){
            String result = null;
            try {
                result= getAdressMacByInterface();
                if (result != null){
                    return result;
                } else {
                    result = getAddressMacByFile(wifiMan);
                    return result;
                }
            } catch (IOException e) {
                Log.e("MobileAccess", "Erreur lecture propriete Adresse MAC");
            } catch (Exception e) {
                Log.e("MobileAcces", "Erreur lecture propriete Adresse MAC ");
            }
        } else{
            if (wifiInf != null && wifiInf.getMacAddress() != null) {
                return wifiInf.getMacAddress();
            } else {
                return "";
            }
        }
        return marshmallowMacAddress;
    }

    private static String getAdressMacByInterface(){
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (nif.getName().equalsIgnoreCase("wlan0")) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }

                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:",b));
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }

        } catch (Exception e) {
            Log.e("MobileAcces", "Erreur lecture propriete Adresse MAC ");
        }
        return null;
    }

    private static String getAddressMacByFile(WifiManager wifiMan) throws Exception {
        String ret;
        int wifiState = wifiMan.getWifiState();

        wifiMan.setWifiEnabled(true);
        File fl = new File(fileAddressMac);
        FileInputStream fin = new FileInputStream(fl);
        ret = crunchifyGetStringFromStream(fin);
        fin.close();

        boolean enabled = WifiManager.WIFI_STATE_ENABLED == wifiState;
        wifiMan.setWifiEnabled(enabled);
        return ret;
    }

    private static String crunchifyGetStringFromStream(InputStream crunchifyStream) throws IOException {
        if (crunchifyStream != null) {
            Writer crunchifyWriter = new StringWriter();

            char[] crunchifyBuffer = new char[2048];
            try {
                Reader crunchifyReader = new BufferedReader(new InputStreamReader(crunchifyStream, "UTF-8"));
                int counter;
                while ((counter = crunchifyReader.read(crunchifyBuffer)) != -1) {
                    crunchifyWriter.write(crunchifyBuffer, 0, counter);
                }
            } finally {
                crunchifyStream.close();
            }
            return crunchifyWriter.toString();
        } else {
            return "No Contents";
        }
    }

}
