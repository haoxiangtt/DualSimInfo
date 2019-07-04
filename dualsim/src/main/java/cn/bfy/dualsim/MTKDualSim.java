package cn.bfy.dualsim;

import android.app.PendingIntent;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import cn.richinfo.dualsim.BuildConfig;


/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * e-mail     : jinfu123.-@163.com
 * createDate : 2017/7/18 0018
 * modifyDate : 2017/7/18 0018
 * @version    : 1.0
 * desc       : 联发科芯片系统双卡类
 * </pre>
 */

public class MTKDualSim extends DualsimBase {

    private static MTKDualSim mInstance;

    //Android系统API提供的TelephonyManager
//    private TelephonyManager mySystemAPITM;

    //MTK芯片系统TelephonyManagerEx单例
    private Object myMTKTMInstance;

    //4.0+ com.mediatek.telephony.smsManagerEx单例
    private  Object mySmsManagerExInstance;

    //4.0- android.telephony.gemini.GeminiSmsManager静态方法类
    private Class myGeminiSmsManagerClass;

    private static final String MTK_PLATFORM_KEY = "ro.mediatek.platform";
    private static final String MTK_GIONEE_PLATFORM_KEY = "ro.gn.platform.support";

    static MTKDualSim getInstance(Context context){
        if (mInstance == null) {
            mInstance = new MTKDualSim(context);
        }
        return mInstance;
    }

    private MTKDualSim(Context context) {
        super(context);
        myMTKTMInstance = getMTKTMDefault();
        initSM();
    }

    @Override
    public DualsimBase update(Context context) {
        mTelephonyInfo = new TelephonyManagement.TelephonyInfo();
        mTelephonyInfo.setChip("MTK");
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
        //测试的时候需要特别注意参数顺序是否对应
        try {
            if (mySmsManagerExInstance != null) {
                //4.0+ com.mediatek.telephony.smsManagerEx单例
                /*mySmsManagerExInstance.getClass().getDeclaredMethod("sendDataMessage",
                    new Class[]{String.class, String.class, short.class, byte[].class, PendingIntent.class,
                    PendingIntent.class, int.class}).invoke(mySmsManagerExInstance,
                    new Object[]{destinationAddress, scAddress, destinationPort, data, sentIntent, deliveryIntent, simID});*/
                eval(mySmsManagerExInstance, "sendDataMessage", new Object[]{destinationAddress, scAddress, destinationPort, data,
                            sentIntent, deliveryIntent, simID},
                        new Class[]{String.class, String.class, short.class, byte[].class, PendingIntent.class,
                            PendingIntent.class, int.class});
                return true;
            } else if (myGeminiSmsManagerClass != null) {
                //4.0- android.telephony.gemini.GeminiSmsManager静态方法类
                /*myGeminiSmsManagerClass.getDeclaredMethod("sendDataMessageGemini", new Class[]{String.class, String.class,
                    short.class, byte[].class, int.class, PendingIntent.class, PendingIntent.class}).invoke(null,
                    new Object[]{destinationAddress, scAddress, destinationPort, data, simID, sentIntent, deliveryIntent});*/
                eval(myGeminiSmsManagerClass, null, "sendDataMessageGemini", new Object[]{destinationAddress, scAddress,
                            destinationPort, data, simID, sentIntent, deliveryIntent},
                        new Class[]{String.class, String.class,
                                short.class, byte[].class, int.class, PendingIntent.class, PendingIntent.class});
                return true;
            } else {
                return super.sendDataMessage(destinationAddress, scAddress, destinationPort,
                    data, sentIntent, deliveryIntent, simID);
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
            return super.sendDataMessage(destinationAddress, scAddress, destinationPort,
                    data, sentIntent, deliveryIntent, simID);
        }
    }


    /**
     * 获取SIM卡状态
     *
     * @param simID
     * @return
     */
    @Override
    public int getSimState(int simID) {

        //5.0及以上的系统使用API的TelephonyManager
        if (currentapiVersion >= 21) {
           return super.getSimState(simID);
        } else {//5.0以下系统使用MTK的TelephonyManagerEx类，无法获取时使用API的TelephonyManager提供的getCallStateGemini方法
            if (myMTKTMInstance != null) {
                try {
                    /*return (Integer) myMTKTMInstance.getClass().getDeclaredMethod("getSimState", int.class)
                        .invoke(myMTKTMInstance, simID);*/
                    return (Integer) eval(myMTKTMInstance, "getSimState", new Object[]{simID}, new Class[]{int.class});
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) {
                        e.printStackTrace();
                        Log.d("mydebug", "isMTKDoubleSim-error:" + e.getMessage());
                    }
                    return super.getSimState(simID);
                }
            } else {
                return super.getSimState(simID);
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

        //这个很逗比，5.0以下的是int类型参数，只有5.0是long类型的，5.0以上又都变回int类型，为什么呢。。。
        if (currentapiVersion >= 21) {
            /*Object subScriptionObject = null;
            if (mTelephonyManager != null) {
                if (currentapiVersion == 21)
                    return (String) mTelephonyManager.getClass().getDeclaredMethod("getSubscriberId", long.class)
                        .invoke(mTelephonyManager, (subScriptionObject = getSubScriptionId(simID)) == null ?
                        simID : ((long[]) subScriptionObject)[0]);
                else
                    return (String) mTelephonyManager.getClass().getDeclaredMethod("getSubscriberId", int.class)
                            .invoke(mTelephonyManager, (subScriptionObject = getSubScriptionId(simID)) == null ?
                            simID : ((int[]) subScriptionObject)[0]);
            }*/
            return super.getImsi(simID);
        } else if (myMTKTMInstance != null) {
            try {
                /*String result = (String) myMTKTMInstance.getClass().getDeclaredMethod("getSubscriberId", int.class)
                        .invoke(myMTKTMInstance, simID);*/
                String result = (String) eval(myMTKTMInstance, "getSubscriberId", new Object[]{simID}, new Class[]{int.class});
                if (TextUtils.isEmpty(result)) {
                    return super.getImsi(simID);
                } else {
                    return result;
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                    Log.d("mydebug", "isMTKDoubleSim-error:" + e.getMessage());
                }
                return super.getImsi(simID);
            }
        } else {
            return super.getImsi(simID);
        }
    }


    @Override
    public String getImei(int simID) {
        if (currentapiVersion >= 21) {
            return super.getImei(simID);
        } else if (myMTKTMInstance != null) {
            try {
                String result = (String) eval(myMTKTMInstance, "getDeviceId", new Object[]{simID}, new Class[]{int.class});
                if (TextUtils.isEmpty(result)) {
                    return super.getImei(simID);
                } else {
                    return result;
                }
            } catch (DualSimMatchException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
                return super.getImei(simID);
            }
        } else {
            return super.getImei(simID);
        }
    }

    @Override
    public String getOperator(int simID) {
        if (currentapiVersion >= 21) {
            return super.getOperator(simID);
        } else if (myMTKTMInstance != null) {
            try {
                String result = (String) eval(myMTKTMInstance, "getSimOperator", new Object[]{simID}, new Class[]{int.class});
                if (TextUtils.isEmpty(result)) {
                    return super.getOperator(simID);
                } else {
                    return result;
                }
            } catch (DualSimMatchException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
                return super.getOperator(simID);
            }
        } else {
            return super.getOperator(simID);
        }
    }


    /**
     * 初始化SmsManager Class
     */
    private void initSM() {
        try {
            if (myGeminiSmsManagerClass == null) {
                myGeminiSmsManagerClass = Class.forName("android.telephony.gemini.GeminiSmsManager");
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
        }
        try {
            if (mySmsManagerExInstance == null)
                mySmsManagerExInstance = eval(Class.forName("com.mediatek.telephony.SmsManagerEx")
                    , null, "getDefault", null, null);
                        /*(Class.forName("com.mediatek.telephony.SmsManagerEx"))
                        .getDeclaredMethod("getDefault").invoke(null);*/
        }catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
        }
    }

    /**
     * MTK芯片系统判断
     *
     * @return
     */
    public boolean isMTKSystem() {
        boolean isMTKSystem = false;
        try {
            //Normal MTK Platform
            String normalMTKPlatform;
            Log.d("mydebug", "check MTKSystem");
            if (!TextUtils.isEmpty(normalMTKPlatform = getProperty(MTK_PLATFORM_KEY))) {
                if (normalMTKPlatform.startsWith("MT") || normalMTKPlatform.startsWith("mt")){
                    isMTKSystem = true;
                }
            }
            //Gionee MTK Platform
            if (!isMTKSystem) {
                String gioneeMTKPlatform;
                Log.d("mydebug", "check MTKSystem");
                if (!TextUtils.isEmpty(gioneeMTKPlatform = getProperty(MTK_GIONEE_PLATFORM_KEY))) {
                    if (gioneeMTKPlatform.startsWith("MT") || gioneeMTKPlatform.startsWith("mt")){
                        isMTKSystem = true;
                    }
                }
            }
            Log.d("mydebug", "check MTKSystem");
        } catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
        }
        return isMTKSystem;
    }


    /**
     * 判断当前MTK芯片系统是否支持双卡
     *
     * @return
     */
    private boolean isMTKDoubleSim() {
        boolean isDoubleSim = false;
        try {
            if (!(isDoubleSim = (Boolean) eval(mTelephonyManager, "mtkGeminiSupport", null, null)
                   /* mTelephonyManager.getClass().getDeclaredMethod("mtkGeminiSupport")
                    .invoke(mTelephonyManager)*/)) {
                isDoubleSim = (Boolean) eval(mTelephonyManager, "isMultiSimEnabled", null, null);
                       /* mTelephonyManager.getClass().getDeclaredMethod("isMultiSimEnabled")
                        .invoke(mTelephonyManager);*/
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
            Log.d("mydebug", "isMTKDoubleSim-error:" + e.getMessage());
        }
        return isDoubleSim;
    }

    /**
     * 获取MTK芯片系统TelephonyManagerEx单例
     *
     * @return
     */
    protected Object getMTKTMDefault() {
//        Class<?> clazz;
        Object mtkTMInstance = null;
        try {
            mtkTMInstance = eval(Class.forName("com.mediatek.telephony.TelephonyManagerEx"), null, "getDefault", null, null);
//                    (clazz = Class.forName("com.mediatek.telephony.TelephonyManagerEx")).getDeclaredMethod("getDefault").invoke(clazz);
        } catch (Exception e) {
            Log.d("mydebug", "isMTKDoubleSim-error:" + e.getMessage());
        }
        return mtkTMInstance;
    }


}
