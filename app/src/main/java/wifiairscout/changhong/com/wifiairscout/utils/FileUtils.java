package wifiairscout.changhong.com.wifiairscout.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;


public class FileUtils {
    private static String sdcaPath;


    static {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            sdcaPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator;
    }

/*	public static String getSdCardPath() {
        return sdcaPath;
	}*/

    public static File createSDDir(String dir) {
        File d = new File(sdcaPath + dir + File.separator);
        if (d.exists())
            return d;
        d.mkdirs();
        return d;
    }

    private static void createSDDirByAbsPath(String absdir) {
        File d = new File(absdir);
        if (!d.exists())
            d.mkdirs();
    }

    public static File createSDFile(String dir, String fileName) {
        File file = new File(sdcaPath + dir + File.separator + fileName);
        try {
            if (file.exists())
                return file;
            file.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return file;
    }

    public static File createSDFile(String path) {
        File file = new File(sdcaPath + path);
        if (file.exists())
            return file;
        else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return file;
    }

    public static boolean directoryIsExist(String dir) {
        File file = new File(sdcaPath + dir);
        return file.isDirectory();
    }

    public static boolean fileIsExist(String dir, String fileName) {
        File file = new File(sdcaPath + dir + File.separator + fileName);
        return file.exists();
    }

    public static boolean fileIsExist(String filePath) {
        File file = new File(sdcaPath + File.separator + filePath);
        return file.exists();
    }


    public static File writeToFileFormInputStream(String destDir, String destFileName, InputStream input) throws IOException {
        createSDDir(destDir);
        File file = createSDFile(destDir, destFileName);
        FileOutputStream fileOutputStream = null;
        fileOutputStream = new FileOutputStream(file);
        byte buffer[] = new byte[4 * 1024];
        int temp;
        while ((temp = input.read(buffer)) != -1)
            fileOutputStream.write(buffer, 0, temp);
        fileOutputStream.flush();
        fileOutputStream.close();
        return file;
    }

    public static File writeToFileFormInputStream(byte[] bytes, String destDir, String destFileName) throws IOException {
        createSDDir(destDir);
        File file = createSDFile(destDir, destFileName);
        FileOutputStream fileOutputStream = null;
        fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(bytes);
        fileOutputStream.flush();
        fileOutputStream.close();
        return file;
    }

    public static String readToStringFormInputStream(InputStream in) {
        StringBuffer sb = new StringBuffer();
        String line;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(in));
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            try {
                bufferedReader.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String readToStringFormInputStreamUTF_8(InputStream in) {
        StringBuffer sb = new StringBuffer();
        String line;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(in,
                    "utf-8"));
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            try {
                bufferedReader.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static byte[] readFiletoBytes(String dir, String fileName) throws IOException {
        String path = sdcaPath + dir + File.separator + fileName;
        InputStream inStream = new FileInputStream(path);
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inStream.read(buffer)) != -1) {
            outstream.write(buffer, 0, len);
        }
        outstream.close();
        inStream.close();
        return outstream.toByteArray();
    }

    public static byte[] readFiletoByte(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inputStream.read(buffer)) != -1) {
            outstream.write(buffer, 0, len);
        }
        outstream.close();
        inputStream.close();
        return outstream.toByteArray();
    }

    public static byte[] readAbsFiletoByte(String filePath) throws Exception {
        InputStream inStream = new FileInputStream(filePath);
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inStream.read(buffer)) != -1) {
            outstream.write(buffer, 0, len);
        }
        outstream.close();
        inStream.close();
        return outstream.toByteArray();
    }

    public static String readToStringFormFile(String path) throws IOException {
        StringBuffer sb = new StringBuffer();
        String line;
        BufferedReader bufferedReader = null;
        File file = new File(path);
        bufferedReader = new BufferedReader(new FileReader(file));
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        bufferedReader.close();
        return sb.toString();
    }


    public static Bitmap readBitmapFormFile(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }

    public static Bitmap readBitmapFormFile(byte data[]) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    private static String decodeToFileFromBase64Bytes(byte[] data, String destDir, String destFilePath) {
        createSDDir(destDir);
        File file = createSDFile(destDir, destFilePath);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(data);
            out.flush();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return sdcaPath + destDir + File.separator + destFilePath;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String geTypeOfImageFile(FileInputStream is) {
        byte[] b = new byte[4];
        try {
            is.read(b, 0, b.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String type = bytesToHexString(b).toUpperCase();
        if (type.contains("FFD8FF")) {
            return "jpg";
        } else if (type.contains("89504E47")) {
            return "png";
        } else if (type.contains("47494638")) {
            return "gif";
        } else if (type.contains("49492A00")) {
            return "tif";
        } else if (type.contains("424D")) {
            return "bmp";
        }
        return type;
    }

    public static String getTypeOfImageFile(byte[] data) {
        byte[] b = new byte[4];
        b[0] = data[0];
        b[1] = data[1];
        b[2] = data[2];
        b[3] = data[3];
        String type = bytesToHexString(b).toUpperCase();
        if (type.contains("FFD8FF")) {
            return "jpg";
        } else if (type.contains("89504E47")) {
            return "png";
        } else if (type.contains("47494638")) {
            return "gif";
        } else if (type.contains("49492A00")) {
            return "tif";
        } else if (type.contains("424D")) {
            return "bmp";
        }
        return type;
    }

    public static String getAbsPath(String dir, String fileName) {
        if (fileName != null)
            return sdcaPath + dir + File.separator + fileName;
        else
            return sdcaPath + dir + File.separator;
    }

    public static String saveToFile(byte[] data, String dir, String fileName) {
        createSDDir(dir);
        File file = createSDFile(dir, fileName);
        if (file.exists())
            return "file is exist";
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(data);
            out.flush();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return sdcaPath + dir + File.separator + fileName;
    }

    public static void saveToFile(InputStream inputStream, String dir, String fileName) throws IOException {
        writeToFileFormInputStream(dir, fileName, inputStream);
    }

    public static void saveToFile(String str, String dir, String fileName) throws IOException {
        createSDDir(dir);
        File file = createSDFile(dir, fileName);
        FileWriter out = null;
        out = new FileWriter(file);
        out.write(str);
        out.flush();
        out.close();
    }

    public static void deleteFileByRelativePath(String dir, String fileName) {
        String path = sdcaPath + dir + File.separator + fileName;
        deleteFileAbsolutePath(path);
    }

    public static void deleteDirByRelativePath(String dir) {
        String path = sdcaPath + dir + File.separator;
        deleteFileAbsolutePath(path);
    }

    /**
     * 删除整个文件夹(目录)或文件
     */
    public static void deleteFileAbsolutePath(String absPath) {
        File file = new File(absPath);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFileAbsolutePath(files[i].getAbsolutePath());
                }
                System.err.println("delete dir:" + file.getAbsolutePath());
            }
            file.delete();
        }
    }

    public static String getFileMimeType(File f) {
        String type = "";
        String fName = f.getName();
        String end = fName
                .substring(fName.lastIndexOf(".") + 1, fName.length())
                .toLowerCase();
        if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
                || end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            type = "audio";
        } else if (end.equals("3gp") || end.equals("mp4")) {
            type = "video";
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
                || end.equals("jpeg") || end.equals("bmp")) {
            type = "image";
        } else if (end.equals("apk")) {
            type = "application/vnd.android.package-archive";
        } else {
            type = "*";
        }
        if (end.equals("apk")) {
        } else {
            type += "/*";
        }
        return type;
    }

	/*public static String getFileName(String url){
        int index=url.lastIndexOf("/");
		String fileName=url.substring(index+1);
		return fileName;
		}*/

    /**
     * 获取文件名
     *
     * @throws IOException
     */

    public static String getFileNameByUrl(String fileUrl) throws IOException {
        if (fileUrl.contains("http:")) {
            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String filename = url.toString().substring(url.toString().lastIndexOf('/') + 1);

            if (filename == null || "".equals(filename.trim())) {//如果获取不到文件名称

                for (int i = 0; ; i++) {

                    String mine = conn.getHeaderField(i);

                    if (mine == null) break;
                    String headerFieldKey = conn.getHeaderFieldKey(i);
                    if (headerFieldKey != null && "content-disposition".equals(headerFieldKey.toLowerCase())) {

                        Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());

                        if (m.find()) return m.group(1);

                    }

                }

                filename = UUID.randomUUID() + ".tmp";//默认取一个文件名

            }

            return filename;
        } else {
            int index = fileUrl.lastIndexOf("/");
            String fileName = fileUrl.substring(index + 1);
            return fileName;
        }

    }

    public static ArrayList<String> getFileNamesInDir(String path) {
        ArrayList<String> fileNames = new ArrayList<String>();
        File file = new File(path);
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File tempFile = files[i];
                if (file.isDirectory()) {
                    String filePath = tempFile.getAbsolutePath();
                    ArrayList<String> tempFilesName = getFileNamesInDir(filePath);
                    fileNames.addAll(tempFilesName);
                }
                fileNames.add(files[i].getName());
            }
        }
        return fileNames;
    }

    public static long getSizeOfFile(String path) {
        long size = 0;
        File file = new File(path);
        if (!file.exists())
            return size;
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File tempFile = files[i];
                if (file.isDirectory()) {
                    String filePath = tempFile.getAbsolutePath();
                    long tempSize = getSizeOfFile(filePath);
                    size += tempSize;
                }
                size += tempFile.length();
            }
        } else {
            size = file.length();
        }
        return size;
    }

    public static ArrayList<File> getFileListOfDir(String dir) {
        ArrayList<File> list = new ArrayList<File>();
        File file = new File(sdcaPath + dir + File.separator);
        if (!file.exists())
            return list;
        else {
            if (!file.isDirectory()) {
                list.add(file);
                return list;
            } else {
                ArrayList<File> folderList = new ArrayList<File>();
                ArrayList<File> fileList = new ArrayList<File>();
                File[] files = file.listFiles();
                if (files.length <= 0)
                    return list;
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory())
                        folderList.add(files[i]);
                    else
                        fileList.add(files[i]);
                }
                list.addAll(folderList);
                list.addAll(fileList);
                return list;
            }
        }
    }


    public static int upZip(String zipFile, String folderPath) throws ZipException, IOException {
        createSDDirByAbsPath(folderPath);
        ZipFile zfile = new ZipFile(zipFile);
        @SuppressWarnings("rawtypes")
        Enumeration zList = zfile.entries();
        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        while (zList.hasMoreElements()) {
            ze = (ZipEntry) zList.nextElement();
            if (ze.isDirectory()) {
                String dirstr = folderPath + ze.getName();
                dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
                createSDDirByAbsPath(dirstr);
                continue;
            }
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(getRealFileName(folderPath, ze.getName())));
            InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
            int readLen = 0;
            while ((readLen = is.read(buf, 0, 1024)) != -1) {
                os.write(buf, 0, readLen);
            }
            is.close();
            os.close();
        }
        zfile.close();
        return 0;
    }

    /**
     * 给定根目录，返回一个相对路径所对应的实际文件名.
     *
     * @param baseDir     指定根目录
     * @param absFileName 相对路径名，来自于ZipEntry中的name
     * @return java.io.File 实际的文件
     */
    public static File getRealFileName(String baseDir, String absFileName) {
        String[] dirs = absFileName.split("/");
        File ret = new File(baseDir);
        String substr = null;
        if (dirs.length > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                substr = dirs[i];
                try {
                    //substr.trim();
                    substr = new String(substr.getBytes("8859_1"), "GB2312");

                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                ret = new File(ret, substr);

            }
            if (!ret.exists())
                ret.mkdirs();
            substr = dirs[dirs.length - 1];
            try {
                //substr.trim();
                substr = new String(substr.getBytes("8859_1"), "GB2312");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            ret = new File(ret, substr);
            return ret;
        }
        ret = new File(baseDir, absFileName);
        return ret;
    }

    public static boolean sdCardIsExist() {
        boolean isExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        return isExist;
    }

    public static void updateSdPath() {
        sdcaPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + File.separator;
    }

    public static Bitmap getBitmapFromAssets(Context context, String imageFileName) {
        InputStream inputStream;
        Bitmap bitmap = null;
        try {
            inputStream = context.getAssets().open(imageFileName);
            byte[] datas = FileUtils.readFiletoByte(inputStream);
            bitmap = BitmapFactory.decodeByteArray(datas, 0, datas.length);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bitmap;
    }

    public static String getTextFromAssets(Context context, String txtFileName,
                                           String charsetName) {
        InputStream inputStream = null;
        String string = null;
        try {
            inputStream = context.getAssets().open(txtFileName);
            byte[] datas = FileUtils.readFiletoByte(inputStream);
            string = new String(datas);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return string;
    }

    public static void unZipCommonRes(String realTargetDir, String realZipFileDir, String zipFileName) throws ZipException, IOException {
        String targetDir = FileUtils.getAbsPath(realTargetDir, null);
        String zipFile = FileUtils.getAbsPath(realZipFileDir, zipFileName);
        if (FileUtils.fileIsExist(realZipFileDir, zipFileName)) {
            FileUtils.upZip(zipFile, targetDir);
            FileUtils.deleteFileAbsolutePath(zipFile);
        }
    }

}
