package cn.richinfo.dualsim;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * e-mail     : ouyangjinfu@richinfo.cn
 * createDate : 2017/7/18 0018
 * modifyDate : 2017/7/18 0018
 * @version    : 1.0
 * desc       : 双卡适配抽象类
 * </pre>
 */
public abstract class DualsimBase {

    static class DualSimMatchException extends Exception {

        private static final long serialVersionUID = -996812356902545308L;

        DualSimMatchException(String info) {
            super(info);
        }

    }

    public interface ForceNetCallback{
        void call(Network network);
    }

    protected static final String TAG = "DualsimBase";

    /**
     * the data connection was connected by the main SIM
     */
    public static final int TYPE_SIM_MAIN = 0;

    /**
     * the data connection was connected by the Assistant SIM
     */
    public static final int TYPE_SIM_ASSISTANT = 1;
    /**
     * there was no data connection been connected
     */
    public static final int TYPE_SIM_EMPTY = -1;

    protected int currentapiVersion;

    protected TelephonyManager mTelephonyManager;

    protected TelephonyManagement.TelephonyInfo mTelephonyInfo;

    protected DualsimBase (Context context) {
        currentapiVersion = Build.VERSION.SDK_INT;
        mTelephonyManager = ((TelephonyManager) context.
                getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE));
    }

    public TelephonyManagement.TelephonyInfo getTelephonyInfo() {
        return mTelephonyInfo;
    }

    public abstract DualsimBase update(Context context);

    /**
     * 强制打开数据网络访问网络；(注意：此方法会阻塞线程)
     * @param context
     * @param host
     * @param callback
     */
    public void forceNet(Context context,final String host, final ForceNetCallback callback) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
            NetworkRequest networkRequest = builder.build();
            ConnectivityManager.NetworkCallback myNetCallback = new ConnectivityManager.NetworkCallback() {
                @RequiresApi(api = 21)
                @Override
                public void onAvailable(Network network) {

                    //需要使用数据网络的链接,均调用network建立连接
                   if (callback != null) {
                       callback.call(network);
                   }
                }
            };
            connectivityManager.requestNetwork(networkRequest, myNetCallback);
        }else {

            //4.4及其以下版本不能按次控制哪个网卡访问url而是按时间段(路由表更新时间)来控制的.
            //重点在第一次访问时的路由,新配路由会在一段时间后(20-30s)失效又会回归默认,此特性不影响数据网络取号.

            //enableHIPRI,开启数据网卡
            connectivityManager.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableHIPRI");
            //wait some time needed to connection manager for waking up
            try {
                for (int counter = 0; counter < 30; counter++) {
                    NetworkInfo.State checkState = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI)
                        .getState();
                    if (0 == checkState.compareTo(NetworkInfo.State.CONNECTED))
                        break;
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "check hipri failed");
                }
            }
            int hostAddress = lookupHost(extractAddressFromUrl(host));
            //新增路由
            boolean resultBool1 = connectivityManager.requestRouteToHost(ConnectivityManager.TYPE_MOBILE_HIPRI, hostAddress);
            //新增域则需要新增路由,比如有302跳转到其他域下,就需要把location的路由加入.

            if (BuildConfig.DEBUG) {
                Log.e(TAG, "change network result >>> " + resultBool1 );
            }

            if (resultBool1) {
                if (callback != null) {
                    callback.call(null);
                }
            } else {
                Log.e(TAG, "change network failed or mobile network is null");
            }
        }

    }

    /**
     * method
     *
     * @param hostname
     * @return -1 if the host doesn't exists, elsewhere its translation
     * to an integer
     */
    protected int lookupHost(String hostname) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            return -1;
        }
        byte[] addrBytes;
        int addr;
        addrBytes = inetAddress.getAddress();
        addr = ((addrBytes[3] & 0xff) << 24)
                | ((addrBytes[2] & 0xff) << 16)
                | ((addrBytes[1] & 0xff) << 8)
                | (addrBytes[0] & 0xff);
        return addr;
    }

    /**
     * This method extracts from address the hostname
     *
     * @param url eg. http://some.where.com:8080/sync
     * @return some.where.com
     */
    private String extractAddressFromUrl(String url) {
        String urlToProcess = null;

        //find protocol
        int protocolEndIndex = url.indexOf("://");
        if (protocolEndIndex > 0) {
            urlToProcess = url.substring(protocolEndIndex + 3);
        } else {
            urlToProcess = url;
        }

        // If we have port number in the address we strip everything
        // after the port number
        int pos = urlToProcess.indexOf(':');
        if (pos >= 0) {
            urlToProcess = urlToProcess.substring(0, pos);
        }

        // If we have resource location in the address then we strip
        // everything after the '/'
        pos = urlToProcess.indexOf('/');
        if (pos >= 0) {
            urlToProcess = urlToProcess.substring(0, pos);
        }

        // If we have ? in the address then we strip
        // everything after the '?'
        pos = urlToProcess.indexOf('?');
        if (pos >= 0) {
            urlToProcess = urlToProcess.substring(0, pos);
        }
        return urlToProcess;
    }

    public boolean sendDataMessage(String destinationAddress, String scAddress, short destinationPort,
            byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent, int simID) {
        //测试的时候需要特别注意参数
        //顺序是否对应
        if (currentapiVersion >= 21) {
            try {
                Object smsManager;
                if (currentapiVersion == 21) {
                    smsManager = eval(SmsManager.getDefault(), "getSmsManagerForSubscriber",
                            new Object[]{(long) getSubId(null, simID)}, new Class[]{long.class});
                } else {
                    smsManager = eval(SmsManager.getDefault(), "getSmsManagerForSubscriptionId",
                            new Object[]{getSubId(null, simID)}, new Class[]{int.class});
                }
                if (smsManager == null) {
                    smsManager = SmsManager.getDefault();
                }
                eval(smsManager, "sendDataMessage",
                        new Object[]{destinationAddress, scAddress, destinationPort, data, sentIntent, deliveryIntent},
                        new Class[]{String.class, String.class, short.class,
                                byte[].class, PendingIntent.class, PendingIntent.class});
                return true;
            } catch (DualSimMatchException e) {
                if (BuildConfig.DEBUG) { e.printStackTrace(); }
                return sendDataMessageDefault(destinationAddress, scAddress, destinationPort, data, sentIntent, deliveryIntent, simID);
            }
        } else {
            return sendDataMessageDefault(destinationAddress, scAddress, destinationPort, data, sentIntent, deliveryIntent, simID);
        }
    }

    protected boolean sendDataMessageDefault(String destinationAddress, String scAddress, short destinationPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent, int simID) {
        SmsManager manager = SmsManager.getDefault();
        try {
            Class smsClass = manager.getClass();
            Field field = smsClass.getDeclaredField("mSubId");
            field.setAccessible(true);
            field.set(manager, getSubId(null, simID));
        } catch (Exception e) {}
        manager.sendDataMessage(destinationAddress, scAddress, destinationPort, data, sentIntent, deliveryIntent);
        return true;
    }

    @SuppressLint("MissingPermission")
    public String getImsi(int simID) {
        /** read imsi */
        try {
            if (currentapiVersion == 21) {
                return getReflexData(mTelephonyManager, "getSubscriberId", (long)getSubId(null, simID));
            } else {
                return getReflexData(mTelephonyManager, "getSubscriberId", getSubId(null, simID));
            }
        } catch (DualSimMatchException e) {
            try {
                return getReflexData(mTelephonyManager, "getSubscriberIdGemini", simID);
            } catch (DualSimMatchException e1) {
                if (simID == TYPE_SIM_MAIN) {
                    return mTelephonyManager.getSubscriberId();
                }
            }
        }
        return "";
    }

    public int getSimState(int simID) {
        /** read isready */
        try {
            return getReflexState(mTelephonyManager, "getSimState", simID);
        } catch (DualSimMatchException e) {
            try {
                return getReflexState(mTelephonyManager, "getSimStateGemini", simID);
            } catch (DualSimMatchException e1) {
                if (simID == TYPE_SIM_MAIN) {
                    return mTelephonyManager.getSimState();
                }
            }
        }
        return 0;
    }

    @SuppressLint({"NewApi", "MissingPermission"})
    public String getImei(int simID) {
        try {
            if (currentapiVersion >= 21) {
                return getReflexData(mTelephonyManager, "getImei", simID);
            } else {
                return getReflexData(mTelephonyManager, "getDeviceId", simID);
            }
        }catch (DualSimMatchException e){
            try {
                return getReflexData(mTelephonyManager,"getDeviceIdGemini",simID);
            }catch (DualSimMatchException ex){
                if (simID == TYPE_SIM_MAIN) {
                    return mTelephonyManager.getDeviceId();
                }
            }
        }
        return "";
    }

    public String getOperator(int simID) {
        try {
            int subId = getSubId(null, simID);
            if (currentapiVersion == 21) {
                return getReflexData(mTelephonyManager,"getSimOperator",(long)(subId == -1 ? simID : subId));
            } else {
                return getReflexData(mTelephonyManager, "getSimOperator", subId == -1 ? simID : subId);
            }
        } catch (DualSimMatchException e) {

            try {
                return getReflexData(mTelephonyManager,"getSimOperatorGemini",simID);
            } catch (DualSimMatchException ex) {
                if (simID == TYPE_SIM_MAIN) {
                    return mTelephonyManager.getSimOperator();
                }
            }
        }
        return "";
    }

    @SuppressLint("NewApi")
    public int getSubId(Context context, int simID) {
       if (currentapiVersion >= 22 && context != null) {
            List<SubscriptionInfo> subscriptionInfos = getSubscriptionInfos(context);
            SubscriptionInfo subInfo = findSubInfo(subscriptionInfos,simID);
            return subInfo.getSubscriptionId();
        } else {
           Object obj = getSubScriptionId(simID);
           if (obj != null) {
               if (Build.VERSION.SDK_INT == 21) {
                   return (int)(((long[])obj)[0]);
               }
               return ((int[])obj)[0];
           } else {
               return simID;
           }
        }
    }

    /**
     * Read default data slotId
     * @param context
     */
    @SuppressLint("NewApi")
    public int getDefaultDataSlotId(Context context) {
        if (currentapiVersion >= 22 && context != null) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(context.getApplicationContext());
            if (subscriptionManager != null) {
                try {
                    SubscriptionInfo subInfo = getReflexSubscriptionInfo(subscriptionManager,
                            "getDefaultDataSubscriptionInfo", null);
                    if (subInfo != null) {
                        return subInfo.getSimSlotIndex();
                    }
                } catch (DualSimMatchException e) {
                    return TYPE_SIM_EMPTY;
                }
            }
        } else {
            try {
                Class cls = Class.forName("android.telephony.SubscriptionManager");
                Method getSubId;
                try {
                    getSubId = cls.getDeclaredMethod("getDefaultDataSubId");
                }catch (NoSuchMethodException e) {
                    getSubId = cls.getDeclaredMethod("getDefaultDataSubscriptionId");
                }

                int subId = (int)getSubId.invoke(null);
                int slotId;
                if (currentapiVersion == 21) {
                    Method getSlotId = cls.getDeclaredMethod("getSlotId",long.class);
                    slotId = (int)getSlotId.invoke(null, (long)subId);
                } else {
                    Method getSlotId = cls.getDeclaredMethod("getSlotId",int.class);
                    slotId = (int)getSlotId.invoke(null, subId);
                }
                return slotId;
            }catch (Exception e){
                return TYPE_SIM_EMPTY;
            }
        }

        return TYPE_SIM_EMPTY;
    }

    protected SubscriptionInfo getReflexSubscriptionInfo(Object real, String predictedMethodName, Object params[])
            throws DualSimMatchException {
        return (SubscriptionInfo)eval(real,predictedMethodName,params,null);
    }

    protected Object eval(Object evalObj, String predictedMethodName, Object params[], Class[] paramsCls)
            throws DualSimMatchException {
        try {
            Class<?> telephonyClass = Class.forName(evalObj.getClass().getName());
            if (params != null && paramsCls != null) {
                Method getSimID = telephonyClass.getMethod(predictedMethodName, paramsCls);
                return getSimID.invoke(evalObj, params);
            } else {
                Method getSimID = telephonyClass.getMethod(predictedMethodName);
                return getSimID.invoke(evalObj);
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
            throw new DualSimMatchException(predictedMethodName);
        }
    }

    protected Object eval(Class evalCls, Object evalObj, String predictedMethodName, Object params[], Class[] paramsCls)
            throws DualSimMatchException {
        if (evalCls == null) {
            return null;
        }
        try {
            if (params != null && paramsCls != null) {
                Method getSimID = evalCls.getMethod(predictedMethodName, paramsCls);
                return getSimID.invoke(evalObj, params);
            } else {
                Method getSimID = evalCls.getMethod(predictedMethodName);
                return getSimID.invoke(evalObj);
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
            throw new DualSimMatchException(predictedMethodName);
        }
    }

    @SuppressLint("NewApi")
    protected SubscriptionInfo findSubInfo(List<SubscriptionInfo> subscriptionInfos, int slotId) {
        SubscriptionInfo subInfo = subscriptionInfos.get(0);
        for(SubscriptionInfo sf : subscriptionInfos){
            if(sf.getSimSlotIndex() == slotId){
                subInfo = sf;
                break;
            }
        }
        return subInfo;
    }

    @SuppressLint("NewApi")
    protected List<SubscriptionInfo> getSubscriptionInfos(Context context){
        SubscriptionManager subscriptionManager = SubscriptionManager.from(context.getApplicationContext());
        List<SubscriptionInfo> subscriptionInfos = null;
        if (subscriptionManager != null) {
            subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();
        }
        return subscriptionInfos;
    }

    protected String getReflexData(TelephonyManager telephony, String predictedMethodName, int id)
            throws DualSimMatchException {
        String data = "";
        Object ob_phone = eval(telephony,predictedMethodName,new Object[]{id},new Class[]{int.class});
        if (ob_phone != null) {
            data = ob_phone.toString();
        }
        return data;
    }

    protected String getReflexData(TelephonyManager telephony, String predictedMethodName, long id)
            throws DualSimMatchException {
        String data = "";
        Object ob_phone = eval(telephony,predictedMethodName,new Object[]{id},new Class[]{long.class});
        if (ob_phone != null) {
            data = ob_phone.toString();
        }
        return data;
    }

    protected int getReflexState(TelephonyManager telephony, String predictedMethodName, int slotID)
            throws DualSimMatchException {
        Object ob_phone = eval(telephony,predictedMethodName,new Object[]{slotID},new Class[]{int.class});
        if (ob_phone != null) {
            return Integer.parseInt(ob_phone.toString());
        }
        return 0;
    }

    /**
     * 根据simID获取SubScriptionId（认购ID？）
     *
     * @param simID
     * @return
     */
    protected Object getSubScriptionId(int simID) {
        Class clazz;
        try {
            return Class.forName("android.telephony.SubscriptionManager").getDeclaredMethod("getSubId", int.class)
                    .invoke(null, simID);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) { e.printStackTrace(); }
        }
        return null;
    }


    protected String getProperty(String propertyKey) throws IOException, InterruptedException {
        String execResult = execCommandGetLine("getprop " + propertyKey);
        return execResult;
    }

    private String execCommandGetLine(String command) throws IOException, InterruptedException {
        String resultStr = null;

        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command);

        int exit = proc.waitFor();
        if (exit == 0) {
            InputStream inputStream = proc.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            try {
                resultStr = br.readLine();
            } finally {
                inputStream.close();
            }
        }
        return resultStr;
    }


    @Override
    public String toString() {
        return mTelephonyInfo.toString();
    }

}
