双卡信息采集组件说明：
	1、主要类：TelephonyManagement;
	2、主要接口：getTelephonyInfo(Context context), 获取TelephonyInfo实例，里面封装了各种sim卡信息；
				 updateTelephonyInfo(Context context), 更新TelephonyInfo实例信息；
				 getDualSimChip(Context context), 获取sim卡信息采集类实例，可以自行获取sim卡中想要的信息。
	3、集成方式：直接倒入源码或者使用gradle中的打包任务打包成jar导入项目中即可。
	4、能够通过本工具获取到双sim卡的imei、imsi、simState、operator、subId、slotId（simId）、默认上网卡等信息。