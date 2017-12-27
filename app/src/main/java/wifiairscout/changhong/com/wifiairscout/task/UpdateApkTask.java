//package wifiairscout.changhong.com.wifiairscout.task;
//
//import android.content.Context;
//import android.text.TextUtils;
//
//public class UpdateApkTask extends GenericTask {
//	public static final String CURRENT_VERSION_KEY = "version";
//	public static final String CHECK_TYPE_KEY="checkType";
//
//	private UpdateInfoBean mUpdateApkInfo;
//	private Context mContext;
//	private CmucApplication cmucApplication;
//
//
//	public UpdateApkTask(Context mContext) {
//		super();
//		this.mContext = mContext;
//		cmucApplication=(CmucApplication) mContext.getApplicationContext();
//	}
//
//	@Override
//	protected TaskResult _doInBackground(TaskParams... params) {
//		// TODO Auto-generated method stub
//		TaskParams param = params[0];
//		String curVersion = param.getString(CURRENT_VERSION_KEY);
//		String checkType = param.getString(CHECK_TYPE_KEY);
//		try {
//
//			// 获取第三方设备类型信息，如果不存在，才是默认版本，否则专用版本 add by fuheng
//			String tag = cmucApplication.getAppTag();
//			if (!TextUtils.isEmpty(JavaScriptConfig.sThirdPartyDeviceType))
//				tag = JavaScriptConfig.sThirdPartyDeviceType;
//
//			publishProgress(mContext.getString(R.string.checking_application_update));
//			mUpdateApkInfo = CmucApplication.sServerClient
//					.getUpdateInfoByOsInfo(curVersion,cmucApplication.getOsInfo(),
//							checkType,tag);
//			return TaskResult.OK;
//		} catch (HttpException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			setException(e);
//			return TaskResult.FAILED;
//		} catch (BusinessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			setException(e);
//			return TaskResult.FAILED;
//		}
//	}
//
//	public UpdateInfoBean getUpdateApkInfo() {
//		return mUpdateApkInfo;
//	}
//
//}