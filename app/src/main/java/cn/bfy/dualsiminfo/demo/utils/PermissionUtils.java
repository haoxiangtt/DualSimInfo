package cn.bfy.dualsiminfo.demo.utils;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

/**
 * Title: CalendarController.java </br>
 * Description: 权限操作类,主要供自身业务使用 </br>
 * Copyright: Copyright (c) 2016 </br>
 * Company:XXXXXXXXXXXXXXXX </br>
 *
 * @author
 * @version 1.0
 * @CreateDate
 */
public class PermissionUtils {
    /**
     * check current sdk if >= 23
     *
     * @return true is need requestPermission
     */
    public static boolean isNeedRequestPermission() {
        return Build.VERSION.SDK_INT >= /*Build.VERSION_CODES.M*/23;
    }

    /**
     * @param context
     * @param permission {@link permission} or {@link android.Manifest.permission_group}
     * @return false need request Permission
     */
    public static boolean checkSelfPermission(Context context, String permission) {
        PackageManager pm = context.getPackageManager();
        return (PackageManager.PERMISSION_GRANTED == pm.checkPermission(permission, context.getPackageName()));
//        int result = ContextCompat.checkSelfPermission(context, permission);
//        return result == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * request Some ManiFest.Permission </br>
     * use this method you need override {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     *
     * @param activity
     * @param permissions {@link permission} or {@link android.Manifest.permission_group}
     * @param requestCode can yo do some for onAactivityResult
     */
    public static void requestPermission(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    /**
     * user deny and never ask again</br>
     * 当用户勾选了申请权限时不再显示，并且拒绝授权时 ，调用该方法检测，返回false 则用户不授予权限，需要弹窗告知用户需要权限的理由，并让其前往系统设置
     *
     * @param activity
     * @param permission
     * @return
     */
    public static boolean shouldShowRequestPermissiomRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * 请求权限
     *
     * @param activity
     * @param permission
     * @param
     * @return true 不需要申请权限  , false 需要申请权限后在操作
     * @CreateData
     */
    public static boolean easyRequestPermission(Activity activity, String permission, int requestCode) {
        if (isNeedRequestPermission()) {
            if (!checkSelfPermission(activity, permission)) {
                requestPermission(activity, new String[]{permission}, requestCode);
                return false;
            }
        }
        return true;
    }
}
