package cn.richinfo.dualsim;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

/**
 *<pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * e-mail     : ouyangjinfu@richinfo.cn
 * createDate : 2017/3/16 0023
 * modifyDate : 2017/3/27 0023
 * @version     : 1.1
 * desc       : telephony information utils
 *              双卡手机sim卡信息管理器，能够获取与sim卡相关的信息，如imsi、imei、operator等
 *              信息
 *</pre>
 */

public class TelephonyManagement {

    private static final String TAG = "chip";

    public static class TelephonyInfo{

        String imeiSIM1 = "";

        String imeiSIM2 = "";


        String imsiSIM1 = "";

        String imsiSIM2 = "";

        int stateSIM1 = 1;

        int stateSIM2 = 1;


        int slotIdSIM1 = DualsimBase.TYPE_SIM_EMPTY;

        int slotIdSIM2 = DualsimBase.TYPE_SIM_EMPTY;


        int subIdSIM1 = DualsimBase.TYPE_SIM_EMPTY;

        int subIdSIM2 = DualsimBase.TYPE_SIM_EMPTY;

        String operatorSIM1 = "";

        String operatorSIM2 = "";

        String chip = "";

        /**
         * The default data slotId(see {@link DualsimBase#TYPE_SIM_MAIN}
         * or {@link DualsimBase#TYPE_SIM_ASSISTANT})
         */
        int defaultDataSlotId = DualsimBase.TYPE_SIM_EMPTY;//subId(simId) value is 0/1/-1;


        public String getChip() {
            return chip;
        }

        void setChip(String chip) {
            this.chip = chip;
        }

        public String getImeiSIM1() {
            return imeiSIM1;
        }

        void setImeiSIM1(String imeiSIM1) {
            if (imeiSIM1 != null) {
                this.imeiSIM1 = imeiSIM1;
            }
        }

        public String getImeiSIM2() {
            return imeiSIM2;
        }

        void setImeiSIM2(String imeiSIM2) {
            if (imeiSIM2 != null) {
                this.imeiSIM2 = imeiSIM2;
            }
        }

        public String getImsiSIM1() {
            return imsiSIM1;
        }

        void setImsiSIM1(String imsiSIM1) {
            if (imsiSIM1 != null) {
                this.imsiSIM1 = imsiSIM1;
            }
        }

        public String getImsiSIM2() {
            return imsiSIM2;
        }

        void setImsiSIM2(String imsiSIM2) {
            if (imsiSIM2 != null) {
                this.imsiSIM2 = imsiSIM2;
            }
        }

        public String getOperatorSIM1() {
            return operatorSIM1;
        }

        void setOperatorSIM1(String operatorSIM1) {
            if (operatorSIM1 != null) {
                this.operatorSIM1 = operatorSIM1;
            }
        }

        public String getOperatorSIM2() {
            return operatorSIM2;
        }

        void setOperatorSIM2(String operatorSIM2) {
            this.operatorSIM2 = operatorSIM2;
        }

        public int getStateSIM1() {
            return stateSIM1;
        }

        void setStateSIM1(int state) {
            stateSIM1 = state;
        }

        public int getStateSIM2() {
            return stateSIM2;
        }

        void setStateSIM2(int state) {
            stateSIM2 = state;
        }

        /**
         * see{@link DualsimBase#TYPE_SIM_MAIN}
         * or {@link DualsimBase#TYPE_SIM_ASSISTANT}
         * or {@link DualsimBase#TYPE_SIM_EMPTY}
         * @return subId(simId) : 0/1/-1
         *
         */
        public int getDefaultDataSlotId() {
            return defaultDataSlotId;
        }

        void setDefaultDataSlotId(int defaultDataSlotId) {
            this.defaultDataSlotId = defaultDataSlotId;
        }

        public int getSlotIdSIM1() {
            return slotIdSIM1;
        }

        void setSlotIdSIM1(int slotIdSIM1) {
            this.slotIdSIM1 = slotIdSIM1;
        }

        public int getSlotIdSIM2() {
            return slotIdSIM2;
        }

        void setSlotIdSIM2(int slotIdSIM2) {
            this.slotIdSIM2 = slotIdSIM2;
        }

        public int getSubIdSIM1() {
            return subIdSIM1;
        }

        void setSubIdSIM1(int subIdSIM1) {
            this.subIdSIM1 = subIdSIM1;
        }

        public int getSubIdSIM2() {
            return subIdSIM2;
        }

        void setSubIdSIM2(int subIdSIM2) {
            this.subIdSIM2 = subIdSIM2;
        }

        public boolean isDualSIM() {
            return stateSIM1 == TelephonyManager.SIM_STATE_READY
                && stateSIM2 == TelephonyManager.SIM_STATE_READY;
        }

        /**
         * Get number of SIM card
         * @return number of SIM card
         */
        public int getSIMCount(){
            if (stateSIM1 == TelephonyManager.SIM_STATE_READY
                    && stateSIM2 == TelephonyManager.SIM_STATE_READY) {
                return 2;
            } else if (stateSIM1 == TelephonyManager.SIM_STATE_READY
                    || stateSIM2 == TelephonyManager.SIM_STATE_READY) {
                return 1;
            } else {
                return 0;
            }
        }


        public String getSubscriberIdBySlotId(int slotId){
            if (slotIdSIM1 == slotId) {
                return imsiSIM1;
            } else if (slotIdSIM2 == slotId) {
                return imsiSIM2;
            } else {
                return "";
            }
        }

        public String getDeviceIdBySlotId(int slotId){
            if (slotIdSIM1 == slotId) {
                return imeiSIM1;
            } else if (slotIdSIM2 == slotId) {
                return imeiSIM2;
            } else {
                return "";
            }
        }

        public String getOperatorBySlotId(int slotId){
            if (slotIdSIM1 == slotId) {
                return operatorSIM1;
            } else if (slotIdSIM2 == slotId) {
                return operatorSIM2;
            } else {
                return "";
            }
        }

        public int getSubId(int slotId){
            if (slotIdSIM1 == slotId) {
                return subIdSIM1;
            } else if (slotIdSIM2 == slotId) {
                return subIdSIM2;
            } else {
                return DualsimBase.TYPE_SIM_EMPTY;
            }
        }

        public int getSlotIdByImsi(String imsi) {
            if (TextUtils.isEmpty(imsi)) {
                return DualsimBase.TYPE_SIM_EMPTY;
            }
            if (imsi.equals(imsiSIM1)) {
                return slotIdSIM1;
            } else if (imsi.equals(imsiSIM2)) {
                return slotIdSIM2;
            } else {
                return DualsimBase.TYPE_SIM_EMPTY;
            }
        }

        /**
         * 获取配置信息中的默认卡slot id
         * @return slotId
         */
        public int getConfigDefaultSlotId(){
            return  getSIMCount() == 1 ? getSlotIdSIM1() :
                    isDualSIM() ? getDefaultDataSlotId() : DualsimBase.TYPE_SIM_EMPTY;
        }

        public String getConfigDefaultSubscriberId(Context context){
            if (isDualSIM()) {
                return getSubscriberIdBySlotId(getConfigDefaultSlotId());
            } else {
                return getSubscriberIdBySlotId(getConfigDefaultSlotId());
            }
        }

        public String getConfigDefaultDeviceId(Context context){
            if (isDualSIM()) {
                return getDeviceIdBySlotId(getConfigDefaultSlotId());
            } else {
                return getDeviceIdBySlotId(getConfigDefaultSlotId());
            }
        }


        public String getConfigDefaultOperator(Context context) {
            if (isDualSIM()) {
                return  getOperatorBySlotId(getConfigDefaultSlotId());
            } else {
                return getOperatorBySlotId(getConfigDefaultSlotId());
            }
        }


        public String getSimStateStr(int simState) {
            StringBuilder sb = new StringBuilder();
            switch (simState) {
                case TelephonyManager.SIM_STATE_ABSENT:
                    sb.append("没插卡");
                    break;
                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                    sb.append("锁定状态，需要用户的PIN码解锁");
                    break;
                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                    sb.append("锁定状态，需要用户的PUK码解锁");
                    break;
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                    sb.append("锁定状态，需要网络的PIN码解锁");
                    break;
                case TelephonyManager.SIM_STATE_READY:
                    sb.append("就绪状态");
                    break;
                default:
                    sb.append("未知状态");
                    break;
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return "TelephonyInfo{" +
                    "imeiSIM1='" + imeiSIM1 + '\'' +
                    ", imeiSIM2='" + imeiSIM2 + '\'' +
                    ", imsiSIM1='" + imsiSIM1 + '\'' +
                    ", imsiSIM2='" + imsiSIM2 + '\'' +
                    ", stateSIM1=" + stateSIM1 +
                    ", stateSIM2=" + stateSIM2 +
                    ", slotIdSIM1=" + slotIdSIM1 +
                    ", slotIdSIM2=" + slotIdSIM2 +
                    ", subIdSIM1=" + subIdSIM1 +
                    ", subIdSIM2=" + subIdSIM2 +
                    ", operatorSIM1='" + operatorSIM1 + '\'' +
                    ", operatorSIM2='" + operatorSIM2 + '\'' +
                    ", chip='" + chip + '\'' +
                    ", defaultDataSlotId=" + defaultDataSlotId +
                    '}';
        }
    }

    private TelephonyManagement(){}

    public static TelephonyManagement getInstance(){
        if(mInstance == null){
            mInstance = new TelephonyManagement();
        }
        return mInstance;
    }

    private static TelephonyManagement mInstance;

    private static DualsimBase mDualsimChip;

    /**
     * Get telephony SIM information
     * request permission {@link android.Manifest.permission#READ_PHONE_STATE}
     * @param context 上下文
     * @return TelephonyInfo object
     */
    public TelephonyInfo getTelephonyInfo(Context context){
        DualsimBase dualsim = getDualSimChip(context);
        if (dualsim.getTelephonyInfo() == null) {
            dualsim.update(context);
        }
        return dualsim.getTelephonyInfo();
    }

    /**
     * Reload telephonyinfo
     * * request permission {@link android.Manifest.permission#READ_PHONE_STATE}
     * @param context 上下文
     * @return this
     */
    public TelephonyManagement updateTelephonyInfo(Context context){

        getDualSimChip(context).update(context);

        return this;
    }

    /**
     ** request permission {@link android.Manifest.permission#READ_PHONE_STATE}
     * @param context 上下文
     * @return DualsimBase object
     */
    public DualsimBase getDualSimChip(Context context) {

        if (mDualsimChip != null) {
            return mDualsimChip;
        }

        //打开这段代码, 表示全部使用默认方案获取sim卡信息
//        if (Build.VERSION.SDK_INT >= 21) {
//            return mDualsimChip = NormalDualSim.getInstance(context);
//        }

        //samsung chip
        if (SamsungDualSim.getInstance(context).isSamsungDualSystem()) {
            if (SamsungDualSim.getInstance(context).getSimState(DualsimBase.TYPE_SIM_MAIN) == 0
                    && SamsungDualSim.getInstance(context).getSimState(DualsimBase.TYPE_SIM_ASSISTANT) == 0){
                Log.w(TAG, ">>>>>>>>>use normal chip<<<<<<<<<<<");
                return mDualsimChip = NormalDualSim.getInstance(context);
            }
            Log.w(TAG, ">>>>>>>>>use samsung chip<<<<<<<<<<<");
            return mDualsimChip = SamsungDualSim.getInstance(context);
        }

        //MTK chip
        if (MTKDualSim.getInstance(context).isMTKSystem()) {
            Log.w(TAG, ">>>>>>>>>use MTK chip<<<<<<<<<<<");
            return mDualsimChip = MTKDualSim.getInstance(context);
        }

        //qualcomm chip
        if (QualcommDualSim.getInstance(context).isQualcommSystem(context)) {
            Log.w(TAG, ">>>>>>>>>use qualcomm chip<<<<<<<<<<<");
            return mDualsimChip = QualcommDualSim.getInstance(context);
        }

        //normal chip
        Log.w(TAG, ">>>>>>>>>use normal chip<<<<<<<<<<<");
        return mDualsimChip = NormalDualSim.getInstance(context);
    }

}
