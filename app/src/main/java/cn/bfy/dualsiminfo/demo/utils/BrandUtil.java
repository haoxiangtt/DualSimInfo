package cn.bfy.dualsiminfo.demo.utils;

import android.annotation.SuppressLint;
import android.os.Build;

/**
 * Util class to check if the brand of device.
 * Created by Pan on 2015/12/23.
 */
@SuppressLint("NewApi")
public class BrandUtil {

    public enum Brand {
        UNKNOWN,SAMSUNG,HUAWEI
    }

    public static Brand getBrand(){
        String brand = Build.BRAND;
        if (brand.equalsIgnoreCase("samsung")) {
            return Brand.SAMSUNG;
        } else if(brand.equalsIgnoreCase("Huawei")) {
            return Brand.HUAWEI;
        } else {
            return Brand.UNKNOWN;
        }
    }

    public static int classifyBrand(){
        return classifyBrand(getBrand());
    }

    public static int classifyBrand(Brand brand){
        switch (brand){
            case HUAWEI:{
                return 0;
            }
            case SAMSUNG:{
                return 1;
            }
            default:{
                return -1;
            }
        }
    }

    /**
     * @return true when the caller API brand is samsung
     */
    public static boolean samsung() {
        return Build.BRAND.equalsIgnoreCase("samsung");
    }

    /**
     * @return true when the caller API brand is huawei
     */
    public static boolean huawei() {
        return Build.BRAND.equalsIgnoreCase("Huawei");
    }

    /**
     * @return true when the caller API brand is xiaomi
     */
    public static boolean xiaomi() {
        return Build.BRAND.equalsIgnoreCase("Xiaomi");
    }

    /**
     * @return true when the caller API brand is google
     */
    public static boolean google() {
    	return Build.BRAND.equalsIgnoreCase("google");
    }

    /**
     * @return true when the caller API brand is Meizu
     */
    public static boolean meizu() {
    	return Build.BRAND.equalsIgnoreCase("Meizu");
    }

    /**
     * @return true when the caller API brand is Lenovo
     */
    public static boolean lenovo() {
    	return Build.BRAND.equalsIgnoreCase("Lenovo");
    }

    /**
     * @return true when the caller API brand is Coolpad
     */
    public static boolean coolpad() {
    	return Build.BRAND.equalsIgnoreCase("Coolpad");
    }

    /**
     * @return true when the caller API brand is nubia
     */
    public static boolean nubia() {
    	return Build.BRAND.equalsIgnoreCase("nubia");
    }

    /**
     * @return true when the caller API brand is GiONEE
     */
    public static boolean gionee() {
    	return Build.BRAND.equalsIgnoreCase("GiONEE");
    }

    /**
     * @return true when the caller API brand is CMDC
     */
    public static boolean cmdc() {
    	return Build.BRAND.equalsIgnoreCase("CMDC");
    }

    /**
     * @return true when the caller API brand is ONE
     */
    public static boolean one() {
    	return Build.BRAND.equalsIgnoreCase("ONE");
    }

    /**
     * @return true when the caller API brand is ONE
     */
    public static boolean htc() {
        return Build.BRAND.equalsIgnoreCase("htc");
    }
}
