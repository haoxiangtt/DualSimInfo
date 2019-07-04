package cn.bfy.dualsim;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;

import cn.richinfo.dualsim.BuildConfig;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * e-mail     : jinfu123.-@163.com
 * createDate : 2017/7/18 0018
 * modifyDate : 2017/7/18 0018
 * @version    : 1.0
 * desc       : 高通芯片双卡类
 * </pre>
 */

public class QualcommDualSim extends DualsimBase {

    private static QualcommDualSim mInstance;

    //MSimTelephonyManager单例
    private Object myQualcommTMInstance;
    //Android系统API提供的TelephonyManager
//    private TelephonyManager mySystemAPITM;
    private static final String QUALCOMM_CMDC_PLATFORM = "persist.loc.nlp_name";
    private static final String QUALCOMM_CMDC_PLATFORM_KEY = "com.qualcomm.location";

    private static final String QUALCOMM_XIAOMI_PLATFORM = "ro.boot.hardware";
    private static final String QUALCOMM_XIAOMI_PLATFORM_KEY = "qcom";
    //采用的处理器
    private static final String QUALCOMM_NUBIA_PLATFORM_KEY = "ro.product.board";
    private static final String QUALCOMM_NUBIA_PLATFORM_VALUE = "msm";
    //主板平台
    private static final String QUALCOMM_BOARD_PLATFORM = "ro.board.platform";
    //根据主板型号判断
    private static final String QUALCOMM_BOARD_PLATFORM_KEY = "hi3630";

    private static final String QUALCOMM_VIVO_PLATFORM = "persist.radio.multisim.config";

    private static final String QUALCOMM_VIVO_X5_PLATFORM = "ro.vivo.product.solution";
    private static final String QUALCOMM_VIVO_X5_PLATFORM_KEY = "QCOM";

    static QualcommDualSim getInstance(Context context){
        if (mInstance == null) {
            mInstance = new QualcommDualSim(context);
        }
        return mInstance;
    }

    private QualcommDualSim(Context context) {
        super(context);
        myQualcommTMInstance = getDefault();
    }

    @Override
    public DualsimBase update(Context context) {
        mTelephonyInfo = new TelephonyManagement.TelephonyInfo();
        mTelephonyInfo.setChip("Qualcomm");
        mTelephonyInfo.setStateSIM1(getSimState(TYPE_SIM_MAIN));
        mTelephonyInfo.setStateSIM2(getSimState(TYPE_SIM_ASSISTANT));
        mTelephonyInfo.setDefaultDataSlotId(getDefaultDataSlotId(context));
        mTelephonyInfo.setImeiSIM1(getImei(TYPE_SIM_MAIN));
        mTelephonyInfo.setImeiSIM2(getImei(TYPE_SIM_ASSISTANT));
        int stateSim1 = mTelephonyInfo.getStateSIM1();
        int stateSim2 = mTelephonyInfo.getStateSIM2();
        if (stateSim1 != 0 && stateSim1 != 1 && stateSim1 != 7 && stateSim1 != 8) {
            mTelephonyInfo.setSlotIdSIM1(TYPE_SIM_MAIN);
            mTelephonyInfo.setImsiSIM1(getImsi(TYPE_SIM_MAIN));
            mTelephonyInfo.setImeiSIM1(getImei(TYPE_SIM_MAIN));
            mTelephonyInfo.setOperatorSIM1(getOperator(TYPE_SIM_MAIN));
            mTelephonyInfo.setSubIdSIM1(getSubId(null, TYPE_SIM_MAIN));
            if (stateSim2 != 0 && stateSim2 != 1 && stateSim2 != 7 && stateSim2 != 8) {
                mTelephonyInfo.setSlotIdSIM2(TYPE_SIM_ASSISTANT);
                mTelephonyInfo.setImsiSIM2(getImsi(TYPE_SIM_ASSISTANT));
                mTelephonyInfo.setImeiSIM2(getImei(TYPE_SIM_ASSISTANT));
                mTelephonyInfo.setOperatorSIM2(getOperator(TYPE_SIM_ASSISTANT));
                mTelephonyInfo.setSubIdSIM2(getSubId(null, TYPE_SIM_ASSISTANT));
            } else {
                mTelephonyInfo.setDefaultDataSlotId(TYPE_SIM_MAIN);
            }
        } else if (stateSim2 != 0 && stateSim2 != 1 && stateSim2 != 7 && stateSim2 != 8) {
            mTelephonyInfo.setStateSIM1(mTelephonyInfo.getStateSIM2());
            mTelephonyInfo.setSlotIdSIM1(TYPE_SIM_ASSISTANT);
            mTelephonyInfo.setDefaultDataSlotId(TYPE_SIM_ASSISTANT);
            mTelephonyInfo.setImsiSIM1(getImsi(TYPE_SIM_ASSISTANT));
            mTelephonyInfo.setImeiSIM1(getImei(TYPE_SIM_ASSISTANT));
            mTelephonyInfo.setOperatorSIM1(getOperator(TYPE_SIM_ASSISTANT));
            mTelephonyInfo.setSubIdSIM1(getSubId(null, TYPE_SIM_ASSISTANT));

            mTelephonyInfo.setStateSIM2(1);
        }
        return this;
    }

    @Override
    public boolean sendDataMessage(String destinationAddress, String scAddress, short destinationPort,
            byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent, int simID) {
        //测试的时候需要特别注意参数
        //顺序是否对应
        if (currentapiVersion >= 21) {
            /*(smObject = Class.forName("android.telephony.SmsManager").getDeclaredMethod("getSmsManagerForSubscriber", long.class)
                .invoke(null, getSubId(null, simID))).getClass().getDeclaredMethod("sendDataMessage",
                new Class[]{String.class, String.class, short.class, byte[].class, PendingIntent.class, PendingIntent.class})
                .invoke(smObject, new Object[]{destinationAddress, scAddress, destinationPort, data, sentIntent, deliveryIntent});*/
            return super.sendDataMessage(destinationAddress, scAddress, destinationPort, data, sentIntent, deliveryIntent, simID);
        } else {
            try {
                Object smObject = Class.forName("android.telephony.MSimSmsManager").getDeclaredMethod("getDefault");
                /*(smObject = Class.forName("android.telephony.MSimSmsManager").getDeclaredMethod("getDefault")
                .invoke(null)).getClass().getDeclaredMethod("sendDataMessage",
                    new Class[]{String.class, String.class, short.class, byte[].class, PendingIntent.class,
                    PendingIntent.class, int.class}).invoke(smObject,
                    new Object[]{destinationAddress, scAddress, destinationPort, data, sentIntent, deliveryIntent, simID});*/
                eval(smObject, "sendDataMessage", new Object[]{destinationAddress, scAddress, destinationPort, data,
                            sentIntent, deliveryIntent, simID},
                        new Class[]{String.class, String.class, short.class, byte[].class, PendingIntent.class,
                            PendingIntent.class, int.class});
                return true;
            }
            catch (Exception e) {
                if (BuildConfig.DEBUG) { e.printStackTrace(); }
            }
        }
        return false;
    }

    @Override
    public String getImei(int simID) {
        if (currentapiVersion >= 21) {
            return super.getImei(simID);
        } else {
            if (myQualcommTMInstance != null) {
                try {
                    String result = (String) eval(myQualcommTMInstance, "getDeviceId",
                        new Object[]{simID}, new Class[]{int.class});
                    if (TextUtils.isEmpty(result)) {
                        return super.getImei(simID);
                    } else {
                        return result;
                    }
                } catch (DualSimMatchException e) {
                    return super.getImei(simID);
                }
            } else {
                return super.getImei(simID);
            }
        }
    }

    /**
     * 获取SIM卡IMSI
     *
     * @param simID
     * @return
     */
    @Override
    public String getImsi(int simID) {
        try {
            //这个很逗比，5.0以下的是int类型参数，只有5.0是long类型的，5.0以上又都变回int类型，为什么呢。。。
            if (currentapiVersion >= 21) {
                return super.getImsi(simID);
            } else {
                if (myQualcommTMInstance != null) {
                    /*String result = (String) myQualcommTMInstance.getClass().getMethod("getSubscriberId", int.class)
                            .invoke(myQualcommTMInstance, simID);*/
                    String result = (String)eval(myQualcommTMInstance, "getSubscriberId", new Object[]{simID},
                            new Class[]{int.class});
                    if (TextUtils.isEmpty(result)) {
                        return super.getImsi(simID);
                    } else {
                        return result;
                    }
                } else {
                    return super.getImsi(simID);
                }
            }

        } catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
            Log.d("mydebug", "getImsi-error:" + e.getMessage());
        }
        return null;
    }


    /**
     * 获取SIM卡状态
     *
     * @param simID
     * @return
     */
    @Override
    public int getSimState(int simID) {
        if (currentapiVersion >= 21) {
            return super.getSimState(simID);
        } else {
            if (myQualcommTMInstance != null) {
                try {
                    /*return (Integer) myQualcommTMInstance.getClass().getDeclaredMethod("getSimState", int.class)
                            .invoke(myQualcommTMInstance, simID);*/
                    return (Integer)eval(myQualcommTMInstance, "getSimState",
                            new Object[]{simID}, new Class[]{int.class});
                } catch (Exception e) {
                    return super.getSimState(simID);
                }
            } else {
                return super.getSimState(simID);
            }
        }
    }

    @Override
    public String getOperator(int simID) {
        if (currentapiVersion >= 21) {
            return super.getOperator(simID);
        } else if (myQualcommTMInstance != null) {
            try {
                String result = (String)eval(myQualcommTMInstance, "getSimOperator",
                    new Object[]{simID}, new Class[]{int.class});
                if (TextUtils.isEmpty(result)) {
                    return super.getOperator(simID);
                } else {
                    return result;
                }
            } catch (DualSimMatchException e) {
                return super.getOperator(simID);
            }
        } else {
            return super.getOperator(simID);
        }
    }

    /**
     * 判断是否高通系统
     * 通过CPU，主板型号判断较局限
     *
     * @param myContext
     * @return
     */
    public boolean isQualcommSystem(Context myContext) {

        //华为手机
        if (isHuaWeiDualSimQualcommSystem(myContext)) {
            Log.d("mydebug", "HUAWEI-System");
            return true;
        }

        //小米
        if (isMiDualSimQualcommSystem()) {
            Log.d("mydebug", "XIAOMI-System");
            return true;
        }

        //vivo手机
        if (isVivoQualcommSystem()) {
            Log.d("mydebug", "Vivo-System");
            return true;
        }

        //vivoX5手机
        if (isVivoX5DualSimQualcommSystem()) {
            Log.d("mydebug", "VivoX5-System");
            return true;
        }

        //移动
        if (isCMDualSimQualcommSystem(myContext)) {
            Log.d("mydebug", "\n" +
                    "主板型号：CM-System");
            return true;
        }

        //努比亚
        if (isNubiaDualSimQualcommSystem(myContext)) {
            Log.d("mydebug", "\n" +
                    "主板型号：NUBIA-System");
            return true;
        }

        return false;
    }

    /**
     * 判断移动双卡系统
     *
     * @param myContext
     * @return
     */
    private boolean isCMDualSimQualcommSystem(Context myContext) {

        if (!"CMDC".equals(Build.MANUFACTURER)) {
            Log.d("mydebug", "!cmdc");
            return false;
        }

        if (currentapiVersion >= 21) {
            try {
                boolean reflectionIsMultiSimEnable = (Boolean) eval(mTelephonyManager, "isMultiSimEnabled", null, null);
//                        mTelephonyManager.getClass().getDeclaredMethod("isMultiSimEnabled").invoke(mTelephonyManager);
                return reflectionIsMultiSimEnable;
            } catch (DualSimMatchException e) {
                if (BuildConfig.DEBUG) { e.printStackTrace(); }
            }
        } else {
            try {
                String execResult = getProperty(QUALCOMM_CMDC_PLATFORM);
                if (!TextUtils.isEmpty(execResult)) {
                    if (execResult.equals(QUALCOMM_CMDC_PLATFORM_KEY))
                        return true;
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) { e.printStackTrace(); }
            }
        }

        return false;
    }

    /**
     * 判断努比亚双卡系统
     *
     * @param myContext
     * @return
     */
    private boolean isNubiaDualSimQualcommSystem(Context myContext) {
        TelephonyManager telephonyManager;

        if (!("nubia".equals(Build.MANUFACTURER.toLowerCase(Locale.ENGLISH)))) {
            Log.d("mydebug", "!nubia");
            return false;
        }

        if (currentapiVersion >= 21) {
            try {
                boolean reflectionIsMultiSimEnable = (Boolean) eval(mTelephonyManager, "isMultiSimEnabled", null, null);
//                        mTelephonyManager.getClass().getDeclaredMethod("isMultiSimEnabled").invoke(mTelephonyManager);
                return reflectionIsMultiSimEnable;
            } catch (Exception e) {
                if (BuildConfig.DEBUG) { e.printStackTrace(); }
            }
        } else {
            try {
                String execResult = getProperty(QUALCOMM_NUBIA_PLATFORM_KEY);
                Log.d("mydebug", "nubia execResult:" + execResult);

                if (!TextUtils.isEmpty(execResult)) {
                    int index = 0;
                    if ((index = execResult.toLowerCase().indexOf(QUALCOMM_NUBIA_PLATFORM_VALUE)) >= 0) {
                        Log.d("mydebug", "nubia index:" + index);
                        return true;
                    }
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) { e.printStackTrace(); }
            }
        }

        return false;

    }

    /**
     * 判断小米双卡系统
     *
     * @return
     */
    private boolean isMiDualSimQualcommSystem() {
        if (!"xiaomi".equals(Build.MANUFACTURER.toLowerCase(Locale.ENGLISH))) {
            Log.d("mydebug", "!xiaomi");
            return false;
        }

        try {
            String execResult = getProperty(QUALCOMM_XIAOMI_PLATFORM);

            if (!TextUtils.isEmpty(execResult)) {
                if (execResult.equals(QUALCOMM_XIAOMI_PLATFORM_KEY))
                    return true;
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
        }

        return false;
    }


    /**
     * 判断华为双卡系统（某个型号）
     *
     * @param myContext
     * @return
     */
    private boolean isHuaWeiDualSimQualcommSystem(Context myContext) {
        if (!"huawei".equals(Build.MANUFACTURER.toLowerCase(Locale.ENGLISH))) {
            return false;
        }

        if (currentapiVersion >= 21) {
            try {
                boolean reflectionIsMultiSimEnable = (Boolean) eval(mTelephonyManager, "isMultiSimEnabled", null, null);
//                        mTelephonyManager.getClass().getDeclaredMethod("isMultiSimEnabled").invoke(myContext);
                return reflectionIsMultiSimEnable;
            } catch (Exception e) {
                if (BuildConfig.DEBUG) { e.printStackTrace(); }
            }
        } else {
            try {
                String execResult = getProperty(QUALCOMM_BOARD_PLATFORM);
                Log.d("mydebug", "huawei-execResult:" + execResult);
                if (!TextUtils.isEmpty(execResult)) {
                    if (execResult.equals(QUALCOMM_BOARD_PLATFORM_KEY))
                        return true;
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) { e.printStackTrace(); }
            }
        }

        return false;
    }

    private boolean isVivoQualcommSystem() {
        if (!"BBK".equals(Build.MANUFACTURER))
            return false;

        try {
            String execResult = getProperty(QUALCOMM_VIVO_PLATFORM);
            if (!TextUtils.isEmpty(execResult)) {
                if ("dsds".equals(execResult) || "dsds".equals(execResult) || "tsts".equals(execResult))
                    return true;
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
        }
        return false;
    }

    private boolean isVivoX5DualSimQualcommSystem() {

        if (!"vivo".equals(Build.MANUFACTURER.toLowerCase(Locale.ENGLISH))) {
            return false;
        }

        try {
            String execResult = getProperty(QUALCOMM_VIVO_X5_PLATFORM);
            if (!TextUtils.isEmpty(execResult)) {
                if (QUALCOMM_VIVO_X5_PLATFORM_KEY.equals(execResult))
                    return true;
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
        }

        return false;
    }


    /**
     * 获取MSimTelephonyManager单例
     *
     * @return
     */
    protected Object getDefault() {
        try {
            return eval(Class.forName("android.telephony.MSimTelephonyManager"), null, "getDefault", null, null);
                    /*Class.forName("android.telephony.MSimTelephonyManager")
                .getDeclaredMethod("getDefault").invoke(null);*/
        } catch (Exception e) {}
        return null;
    }






}
