package cn.bfy.dualsim;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import cn.richinfo.dualsim.BuildConfig;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * e-mail     : jinfu123.-@163.com
 * createDate : 2017/7/18 0018
 * modifyDate : 2017/7/18 0018
 * @version    : 1.0
 * desc       : 三星手机双卡类
 * </pre>
 */

public class SamsungDualSim extends DualsimBase {

    private static SamsungDualSim mInstance;

    public Class
        androidMSTMClass,
        androidMSMClass,
        samsungMSMClass;

    private Object
        //samsung MultiSimManager实例
        mySamsungMSMObject,
        //SmsManager实例
        mySMObject;


    private final static String CLASS_ANDROID_MULTISIMMANAGER = "com.android.internal.telephony.MultiSimManager";
    //5.0+
    private final static String CLASS_SAMSUNG_MULTISIMMANAGER = "com.samsung.android.telephony.MultiSimManager";
    //5.0-
    private final static String CLASS_ANDROID_MULTISIMTELEPHONYMANAGER = "android.telephony.MultiSimTelephonyManager";

    static SamsungDualSim getInstance(Context context){
        if (mInstance == null) {
            mInstance = new SamsungDualSim(context);
        }
        return mInstance;
    }

    private SamsungDualSim(Context context) {
        super(context);
    }

    @Override
    public DualsimBase update(Context context) {
        mTelephonyInfo = new TelephonyManagement.TelephonyInfo();
        mTelephonyInfo.setChip("Samsung");
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
        if (mySMObject == null) {
            mySMObject = getSmsManagerDefault(simID);
        }

        if (mySMObject == null) {
            Log.e(TAG, "the mySMObject is null, cannot sendDataMessage!");
            return false;
        }
        try {
            //三星发送数据短信不需要将simID作为参数传入
            /*if (currentapiVersion >= 21) {
                //5.0+直接获取实例所以需要setDefaultSubId将simID作为参数
                mySMObject.getClass().getDeclaredMethod("setDefaultSubId", new Class<?>[]{int.class, long.class})
                        .invoke(mySMObject, new Object[]{2, ((long[]) (mySMObject.getClass().getDeclaredMethod("getSubId", int.class)
                                .invoke(mySMObject, simID)))[0]});
            }*/
            //5.0-通过simID获取到对应的单例
            /*mySMObject.getClass().getDeclaredMethod("sendDataMessage", new Class[]{String.class, String.class, short.class
                    , byte[].class, PendingIntent.class, PendingIntent.class}).invoke(mySMObject,
                    new Object[]{destinationAddress, scAddress, destinationPort, data, sentIntent, deliveryIntent});*/
            eval(mySMObject, "sendDataMessage", new Object[]{destinationAddress, scAddress, destinationPort, data,
                sentIntent, deliveryIntent}, new Class[]{String.class, String.class, short.class, byte[].class,
                PendingIntent.class, PendingIntent.class});
            return true;

        } catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
        }
        return false;
    }

    @Override
    public String getImsi(int simID) {
        //SimManager
        //为什么getDeclaredMethod会报NoSuchMethod
        if (currentapiVersion < 21) {
            try {
                /*Object myObject = null;
                String result = (String) (myObject = getSimManagerDefault(simID)).getClass()
                        .getMethod("getSubscriberId").invoke(myObject);*/
                String result = (String) eval(getSimManagerDefault(simID), "getSubscriberId", null, null);
                if (TextUtils.isEmpty(result)) {
                    return super.getImsi(simID);
                } else {
                    return result;
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) { e.printStackTrace(); }
                return super.getImsi(simID);
            }
        } else {
            return super.getImsi(simID);
            /*Object obj;
            return (String) (obj = getSimManagerDefault(simID)).getClass()
                .getDeclaredMethod("getSubscriberId", int.class).invoke(obj, simID);*/
        }
    }

    @Override
    public int getSimState(int simID) {
        //SimManager
        if (currentapiVersion < 21) {
            try {
                /*Object myObject = null;
                return (Integer) (myObject = getSimManagerDefault(simID)).getClass()
                        .getDeclaredMethod("getSimState").invoke(myObject);*/
                return (Integer) eval(getSimManagerDefault(simID), "getSimState", null, null);
            } catch (Exception e) {
                if (BuildConfig.DEBUG) { e.printStackTrace(); }
                return super.getSimState(simID);
            }
        } else {
            return super.getSimState(simID);
            /*return (Integer) (myObject = getSimManagerDefault(simID)).getClass()
                    .getDeclaredMethod("getSimState", int.class).invoke(myObject, simID);*/
        }
    }

    @Override
    public String getImei(int simID) {

            if (currentapiVersion < 21) {
                try {
                    /*Object myObject = null;
                    String result = (String) (myObject = getSimManagerDefault(simID)).getClass()
                            .getDeclaredMethod("getDeviceId").invoke(myObject);*/
                    String result = (String) eval(getSimManagerDefault(simID), "getDeviceId", null, null);
                    if (TextUtils.isEmpty(result)) {
                        return super.getImei(simID);
                    } else {
                        return result;
                    }
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) { e.printStackTrace(); }
                    return super.getImei(simID);
                }
            } else {
                return super.getImei(simID);
                /*return (String) (myObject = getSimManagerDefault(simID)).getClass()
                        .getDeclaredMethod("getDeviceId", int.class).invoke(myObject, simID);*/
            }
    }

    @Override
    public String getOperator(int simID) {

        if (currentapiVersion < 21) {
            try {
                /*Object myObject;
                String result = (String) (myObject = getSimManagerDefault(simID)).getClass()
                        .getDeclaredMethod("getSimOperator").invoke(myObject);*/
                String result = (String) eval(getSimManagerDefault(simID), "getSimOperator", null, null);
                if (TextUtils.isEmpty(result)) {
                    return super.getOperator(simID);
                } else {
                    return result;
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) { e.printStackTrace(); }
                return super.getOperator(simID);
            }

        } else {
            return super.getOperator(simID);
            /*return (String) (myObject = getSimManagerDefault(simID)).getClass()
                    .getDeclaredMethod("getSimOperator", int.class).invoke(myObject, simID);*/
        }
    }

    /**
     * 三星系统判断
     *
     * @return
     */
    public boolean isSamsungDualSystem() {

        if ("samsung".equalsIgnoreCase(Build.MANUFACTURER)) {

            if (currentapiVersion < 21)
                return checkByStrings();
            else
                return checkByfunction();
        }
        return false;
    }


    /**
     * 5.0-双卡支持判断
     *
     * @return
     */
    private boolean checkByStrings() {
        try {
            if (androidMSMClass == null)
                androidMSMClass = Class.forName(CLASS_ANDROID_MULTISIMMANAGER);
            if (((Integer) eval(androidMSMClass, androidMSMClass.newInstance(), "getSimSlotCount", null, null)
                    /*androidMSMClass.getDeclaredMethod("getSimSlotCount").invoke(androidMSMClass.newInstance())*/) >= 2)
                return true;
            else
                return false;
        } catch (Exception e) {}
        return false;

    }

    /**
     * 5.0+双卡支持判断
     *
     * @return
     */
    private boolean checkByfunction() {
        try {
            if (samsungMSMClass == null)
                samsungMSMClass = Class.forName(CLASS_SAMSUNG_MULTISIMMANAGER);

            if ((Integer)eval(samsungMSMClass, samsungMSMClass.newInstance(), "getSimSlotCount", null, null)
                    /*samsungMSMClass.getDeclaredMethod("getSimSlotCount").invoke(samsungMSMClass.newInstance())*/ >= 2)
                return true;
            else
                return false;
        } catch (Exception e) {}
        return false;
    }


    /**
     * 获取SimManager实例
     *
     * @param simID
     * @return
     */

    private Object getSimManagerDefault(int simID) {
        try {

            if (currentapiVersion < 21) {//5.0-根据simID获取对应实例
               /* return (androidMSTMClass == null ? (androidMSTMClass = Class.forName(CLASS_ANDROID_MULTISIMTELEPHONYMANAGER))
                        : androidMSTMClass).getDeclaredMethod("getDefault", int.class).invoke(null, getLogicalSimSlot(simID));*/
                if (androidMSTMClass == null) {
                    androidMSTMClass = Class.forName(CLASS_ANDROID_MULTISIMTELEPHONYMANAGER);
                }
               return eval(androidMSTMClass, null, "getDefault", new Object[]{getLogicalSimSlot(simID)}, new Class[]{int.class});
            } else {//5.0+直接获取实例
                if (mySamsungMSMObject == null)
                    return mySamsungMSMObject = Class.forName(CLASS_SAMSUNG_MULTISIMMANAGER).newInstance();
                else
                    return mySamsungMSMObject;
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
        }
        return null;
    }

    /**
     * 获取SmsManager单例
     *
     * @param simID
     * @return
     */
    private Object getSmsManagerDefault(int simID) {
        try {
            //5.0-根据simID获取单例
            if (currentapiVersion < 21) {

                return eval(Class.forName("android.telephony.MultiSimSmsManager"), null, "getDefault",
                    new Object[]{getLogicalSimSlot(simID)}, new Class[]{int.class});
                        /*Class.forName("android.telephony.MultiSimSmsManager").getDeclaredMethod("getDefault", int.class)
                        .invoke(null, getLogicalSimSlot(simID));*/

                //5.0+直接获取单例
            } else {
//                return Class.forName("android.telephony.SmsManager").getDeclaredMethod("getDefault").invoke(null);
                if (currentapiVersion == 21) {
                    return eval(Class.forName("android.telephony.SmsManager"), SmsManager.getDefault(), "getSmsManagerForSubscriber"
                        , new Object[]{(long)getSubId(null, simID)}, new Class[]{long.class});
                            /*Class.forName("android.telephony.SmsManager").getDeclaredMethod("getSmsManagerForSubscriber", long.class)
                        .invoke(SmsManager.getDefault(), (long)getSubId(null, simID));*/
                } else {
                    return eval(Class.forName("android.telephony.SmsManager"), SmsManager.getDefault(), "getSmsManagerForSubscriptionId"
                            , new Object[]{getSubId(null, simID)}, new Class[]{int.class});
                            /*Class.forName("android.telephony.SmsManager").getDeclaredMethod("getSmsManagerForSubscriptionId", int.class)
                        .invoke(SmsManager.getDefault(), getSubId(null, simID));*/
                }
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
        }
        return null;
    }

    /**
     * 获取simID对应的卡槽ID
     *
     * @param simID
     * @return
     */
    private int getLogicalSimSlot(int simID) {
        try {
            if (androidMSMClass == null)
                androidMSMClass = Class.forName(CLASS_ANDROID_MULTISIMMANAGER);
            return (Integer)eval(androidMSMClass, androidMSMClass.newInstance(), "getLogicalSimSlot",
                new Object[]{simID}, new Class[]{int.class});
//                    (Integer) androidMSMClass.getDeclaredMethod("getLogicalSimSlot", int.class).invoke(androidMSMClass.newInstance(), simID);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
        }
        return -1;
    }


}
