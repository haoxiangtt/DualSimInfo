package cn.bfy.dualsim;

import android.content.Context;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * e-mail     : jinfu123.-@163.com
 * createDate : 2017/7/18 0018
 * modifyDate : 2017/7/18 0018
 * @version    : 1.0
 * desc       : 普通双卡类
 * </pre>
 */

public class NormalDualSim extends DualsimBase {

    private static NormalDualSim mInstance;

    static NormalDualSim getInstance(Context context){
        if (mInstance == null) {
            mInstance = new NormalDualSim(context);
        }
        return mInstance;
    }

    private NormalDualSim(Context context) {
        super(context);
    }

    @Override
    public NormalDualSim update(Context context) {

        mTelephonyInfo = new TelephonyManagement.TelephonyInfo();
        mTelephonyInfo.setChip("unknown");
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

}
