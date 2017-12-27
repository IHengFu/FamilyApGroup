package wifiairscout.changhong.com.wifiairscout.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtil {
	public static Bitmap comp(Bitmap image) {   	       
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();          
	    image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
	    ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());   
	    BitmapFactory.Options newOpts = new BitmapFactory.Options();   
	    newOpts.inJustDecodeBounds = true;   
	    Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);   
	    newOpts.inJustDecodeBounds = false;   
	    int w = newOpts.outWidth;   
	    int h = newOpts.outHeight;   
	    float hh = 400f;
	    float ww = 240f;
	    int be = 1;
	    if (w > h && w > ww) {
	        be = (int) (newOpts.outWidth / ww);   
	    } else if (w < h && h > hh) {
	        be = (int) (newOpts.outHeight / hh);   
	    }   
	    if (be <= 0)   
	        be = 1;   
	    newOpts.inSampleSize = be;
	    isBm = new ByteArrayInputStream(baos.toByteArray());   
	    bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);   
	    return bitmap;
	}  
	
	public static Bitmap getDrawableFromComBitmap(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 30, baos);
		ByteArrayInputStream isBm = new ByteArrayInputStream(
				baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap1=BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		float hh = 400f;
		float ww = 240f;
		int be = 1;
		if (w > h && w > ww) {
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;
		isBm = new ByteArrayInputStream(baos.toByteArray());
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		try {
			baos.close();
			isBm.close();
			recycleBitmap(bitmap1);
			System.gc();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	public static byte[] getBytesFromComBitmap(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 30, baos);
		ByteArrayInputStream isBm = new ByteArrayInputStream(
				baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap1=BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		float hh = 400f;
		float ww = 240f;
		int be = 1;
		if (w > h && w > ww) {
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;
		isBm = new ByteArrayInputStream(baos.toByteArray());
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		try {
			baos.close();
			isBm.close();
			System.gc();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] bytes=bitmapToBytes(bitmap);
		recycleBitmap(bitmap1);
		recycleBitmap(bitmap);
		return bytes;
	}

	private static void recycleBitmap(Bitmap bitmap) {
		if(bitmap!=null&&!bitmap.isRecycled()){
			bitmap.recycle();
			bitmap=null;
		}
	}
	
	private static  byte[] bitmapToBytes(Bitmap image) {  	  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;  
        while ( baos.toByteArray().length / 1024>100) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;
        }  
        byte[] bytes=baos.toByteArray();
        try {
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return bytes;  
    }
}
