package wifiairscout.changhong.com.wifiairscout.utils;
import android.content.Context;
import android.text.format.DateFormat;

public class UnitUtils {

    public static int dip2px(Context context, float dpValue) {
    	final float scale = context.getResources().getDisplayMetrics().density;
    	return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context,float pxValue) {
    	  final float scale = context.getResources().getDisplayMetrics().density;
    	  return (int) (pxValue / scale + 0.5f);
    	 }

	 public static String formatTime(long time)throws Throwable{
		 String s=DateFormat.format("mm:ss", time).toString();
		 return s;
	 }
}
