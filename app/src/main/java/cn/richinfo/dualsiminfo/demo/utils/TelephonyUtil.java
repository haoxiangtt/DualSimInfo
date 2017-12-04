package cn.richinfo.dualsiminfo.demo.utils;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.Method;

/**
 * Created by Pan on 2017/3/2.
 */

public class TelephonyUtil {
    public static void printTelephonyManagerMethodNamesForThisDevice(Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Class<?> telephonyClass;
        try {
            telephonyClass = Class.forName(telephony.getClass().getName());
            Method[] methods = telephonyClass.getMethods();
            for (int idx = 0; idx < methods.length; idx++) {
                System.out.println("\n" + methods[idx] + " declared by " + methods[idx].getDeclaringClass());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取网络运营商 1.移动流量 2.联通流量网络 3.电信流量网络
     *
     * @param context
     * @return
     */
    public static int getSimOperator(Context context, String operator) {
        int mode = Settings.System.getInt(context.getApplicationContext().getContentResolver(),
                "airplane_mode_on", 0);
        if (mode == 1) {
            return 0;
        }
        try {
            if (TextUtils.isEmpty(operator)) {
                TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                operator = manager.getSimOperator();
            }
            if (!TextUtils.isEmpty(operator)) {
                if (operator.equals("46000") || operator.equals("46002")
                        || operator.equals("46007") || operator.equals("46004")) {
                    // 中国移动
                    return 1;
                } else if ("46001".equals(operator) || operator.equals("46006")
                        || operator.equals("46009")) {
                    // 中国联通
                    return 2;
                } else if ("46003".equals(operator) || operator.equals("46005")
                        || operator.equals("46011")) {
                    // 中国电信
                    return 3;
                }
            }
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 获取短信上行端口号
     *
     * @param context
     * @return
     */
    public static String getSendNumber(Context context, String operator) {
        int operetor = getSimOperator(context, operator);
        if (1 == operetor) {
            return "106581021";
        } else if (2 == operetor) {
            return "1065553610039";
        } else if (3 == operetor) {
            return "1065987711";
        }
        return "";
    }

    public static String getSmsContent(Context context, String imsi, String operator) {
        String content = imsi;
        if (getSimOperator(context, operator) == 3) {
            content = "op#cx" + imsi;
        }
        return content;
    }

}
