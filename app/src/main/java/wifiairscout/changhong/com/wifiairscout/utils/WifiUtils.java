package wifiairscout.changhong.com.wifiairscout.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class WifiUtils {
	private final static String TAG = "wifi";
	private static Context mContext = null;

	private static String mPassWord = null;
    private static String mApName = null;

	private WifiManager mWifiManager = null;

	private final int SECURITY_OPEN = 0;
	private final int SECURITY_WPA = 1;
	private final int SECURITY_WEP = 2;
	private final int SECURITY_EAP = 3;
	private final int SECURITY_UNKNOW = 4;

	public WifiUtils(Context context) {
		mContext = context;
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
	}

	public int openWifi() {
		int state = mWifiManager.getWifiState();

		if (state == WifiManager.WIFI_STATE_DISABLED
				|| state == WifiManager.WIFI_STATE_DISABLING
				|| state == WifiManager.WIFI_STATE_UNKNOWN) {
			mWifiManager.setWifiEnabled(true);
		}

		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}

		return mWifiManager.getWifiState();
	}

	public int getWifiState() {

		return mWifiManager.getWifiState();
	}

	public int closeWifi() {

		int state = mWifiManager.getWifiState();
		if (state == WifiManager.WIFI_STATE_ENABLED
				|| state == WifiManager.WIFI_STATE_ENABLING) {
			mWifiManager.setWifiEnabled(false);
		}
		return mWifiManager.getWifiState();

	}

	public void stopWifi(WifiConfiguration existingConfig) {
		mWifiManager.disableNetwork(existingConfig.networkId);
	}

	public void connect2AccessPoint(ScanResult scanResult, String password) {	
		mPassWord = password;
        mApName = scanResult.SSID;
        Log.d(TAG, "[elven]--->connect2AccessPoint() mApName:"+mApName+",mPassWord:"+mPassWord);
		int securityType = getSecurityType(scanResult);
		Log.d(TAG, "[elven]--->connect2AccessPoint() securityType:"+securityType);
		List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
		Log.d(TAG, "[elven]--->connect2AccessPoint() configs.size:"+configs.size());
		WifiConfiguration config = getSavedWifiConfig(scanResult.SSID, configs);

		if (config == null) {
			Log.d(TAG, "[elven]===== It's a new AccessPoint!!! ");
			config = new WifiConfiguration();
			// config.BSSID = scanResult.BSSID;
			config.SSID = "\"" + scanResult.SSID + "\"";
			config = getConfigBySecurityType(config, securityType);
			// config.priority = 1;
			config.status = WifiConfiguration.Status.ENABLED;
			int netId = mWifiManager.addNetwork(config);
			mWifiManager.enableNetwork(netId, true);
            mWifiManager.saveConfiguration();
            Log.d(TAG, "[elven]--->connect2AccessPoint() netId:"+netId);
            if(netId == -1)
            {
            	Toast.makeText(mContext, "SSID:"+scanResult.SSID+" connect fail!", Toast.LENGTH_SHORT).show();
            }
		} else {
			Log.d(TAG, "[elven]===== It's a saved AccessPoint!!! config.status:"+config.status);
			config.status = WifiConfiguration.Status.ENABLED;
			config = getConfigBySecurityType(config, securityType);
			int netId = mWifiManager.addNetwork(config);
			mWifiManager.enableNetwork(netId, true);
			//mWifiManager.enableNetwork(config.networkId, true);
            mWifiManager.updateNetwork(config);
            mWifiManager.saveConfiguration();//test
            Log.d(TAG, "[elven]--->connect2AccessPoint() config.networkId:"+config.networkId+",netId="+netId);
            if(netId == -1)
            {
            	Toast.makeText(mContext, "SSID:"+scanResult.SSID+" connect fail!", Toast.LENGTH_SHORT).show();
            }
		}

	}

	private WifiConfiguration getConfigBySecurityType(WifiConfiguration config,
			int securityType) {

		switch (securityType) {
		case SECURITY_OPEN:
			config.allowedKeyManagement.set(KeyMgmt.NONE);
			break;

		case SECURITY_WPA:
			config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
			config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
			config.preSharedKey = "\"" + getPassWord() + "\"";

			break;
		case SECURITY_EAP:
			config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
			config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
			config.preSharedKey = "\"" + getPassWord() + "\"";
			break;
        default :
            config.allowedKeyManagement.set(KeyMgmt.NONE);
			break;
		}

		return config;

	}

	public static void setPassWord(String password) {
		mPassWord = password;

	}

	public static void setApName(String name) {
		mApName = name;

	}

    public static  String getApName(){
        return mApName ;
    }

	public static String getPassWord() {
		return mPassWord;
	}


	public void unSaveConfig(WifiConfiguration existingConfig) {
		mWifiManager.removeNetwork(existingConfig.networkId);
	}

	public boolean saveConfiguration() {
		return mWifiManager.saveConfiguration();
	}

	public boolean disconnect() {
		return mWifiManager.disconnect();
	}

	public WifiInfo getCurrentWifiInfo() {
		WifiInfo wInfo = mWifiManager.getConnectionInfo();
		return wInfo;
	}

	public WifiConfiguration getSavedWifiConfig(String SSID,
			List<WifiConfiguration> existingConfigs) {

		for (WifiConfiguration existingConfig : existingConfigs) {

			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}

		return null;
	}

	public List<ScanResult> getWifiAccessPointList() {
		List<ScanResult> list = new ArrayList<ScanResult>();
		List<ScanResult> new_list = new ArrayList<ScanResult>();
		list.clear();
		new_list.clear();
		list = mWifiManager.getScanResults();

		for (ScanResult result : list) {
			if (result.SSID == null || result.SSID.length() == 0
					|| result.capabilities.contains("[IBSS]")) {
				continue;
			}
			new_list.add(result);
		}
		Collections.sort(new_list, new sortByLevel());
		return new_list;
	}

	public class sortByLevel implements Comparator<ScanResult> {
		public int compare(ScanResult obj1, ScanResult obj2) {
			if (obj1.level > obj2.level)
				return 1;
			else
				return 0;
		}
	}

	public boolean startScan() {
		return mWifiManager.startScan();
	}

	public int getWifiLevel(ScanResult result) {
		return result.level;
	}

	public int getSecurityType(ScanResult result) {
		if (result.capabilities == null) {
			return SECURITY_OPEN;
		}

		if (result.capabilities.contains("WPA")) {
			return SECURITY_WPA;
		} else if (result.capabilities.contains("WEP")) {
			return SECURITY_WEP;
		} else if (result.capabilities.contains("EAP")) {
			return SECURITY_EAP;
		} else {
			return SECURITY_UNKNOW;
		}
	}

}