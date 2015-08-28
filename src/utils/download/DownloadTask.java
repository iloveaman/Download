package utils.download;

import android.util.Log;
import android.webkit.URLUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import in.srain.cube.concurrent.SimpleTask;

/**
 * 发送给服务端的请求中的参数值，如果含有特殊符号，需要是做URLEncode，服务端才可以正常解析，否则可能会出错。
 * URLEncode主要是把一些特殊字符转换成转移字符，比如：&要转换成&amp;这样的。
 */
public class DownloadTask extends SimpleTask {

    private static final String TAG = "DownloadTask";

    // 10-10 19:14:32.618: D/DownloadService(1926): 测试缓存：41234 32kb
    // 10-10 19:16:10.892: D/DownloadService(2069): 测试缓存：41170 1kb
    // 10-10 19:18:21.352: D/DownloadService(2253): 测试缓存：39899 10kb
    private static final int BUFFER_SIZE = 10 * 1024; // 8k ~ 32K

    public static final int RESULT_OK = 1;
    public static final int RESULT_URL_ERROR = 2;
    public static final int RESULT_DOWNLOAD_ERROR = 3;
    public static final int RESULT_NO_ENOUGH_SPACE = 4;

    private int mResult = RESULT_OK;
    private String mUrl;
    private String mFileName;
    private DownLoadListener mDownLoadListener;

    public DownloadTask(DownLoadListener listener, String url, String fileName) {
        mDownLoadListener = listener;
        mUrl = url;
        mFileName = fileName;
    }

    private void setResult(int result) {
        mResult = result;
    }

    @Override
    public void doInBackground() {
        Log.d(TAG, "文件下载开始.....");
        InputStream in = null;
        FileOutputStream out = null;
        if (!URLUtil.isNetworkUrl(mUrl)) {
            setResult(RESULT_URL_ERROR);
            return;
        }

        File apkFile = new File(mFileName);
        //String updateUrl = mUrl;
        try {
            String encodeUrl = encodedURL(mUrl);//mUrl.replace("备课神器知识库",URLEncoder.encode("备课神器知识库")) ;
            URL url = new URL(encodeUrl);
            Log.d(TAG, "\n" + "下载地址：" + mUrl + "\n转码后地址：" + encodeUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            //long downLength = 0;
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Accept-Language", "zh-CN");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Referer", url.toString());
            urlConnection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            //urlConnection.setRequestProperty("Range", "bytes=" + downLength + "-");
            urlConnection.connect();
            long bytetotal = urlConnection.getContentLength();
            long bytesum = 0;
            int byteread = 0;
            in = urlConnection.getInputStream();
            //Log.i(TAG, apkFile.getAbsolutePath());
            if (apkFile.exists()) {
            } else {
                apkFile.createNewFile();
            }
            out = new FileOutputStream(apkFile);
            byte[] buffer = new byte[BUFFER_SIZE];
            int oldProgress = 0;
            while ((byteread = in.read(buffer)) != -1) {
                bytesum += byteread;
                out.write(buffer, 0, byteread);
                //downLength += byteread;
                int progress = (int) (bytesum * 100L / bytetotal);
                // 如果进度与之前进度相等，则不更新，如果更新太频繁，否则会造成界面卡顿
                if (progress != oldProgress) {
                    if (!isCancelled()) {
                        mDownLoadListener.onPercentUpdate(progress);
                    }
                }
                oldProgress = progress;
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            // 下载完成
            setResult(RESULT_OK);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "download apk file error", e);
            setResult(RESULT_DOWNLOAD_ERROR);
            //下载失败把文件删除（如果要支持断点续传则不要这一步）
            apkFile.delete();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    setResult(RESULT_DOWNLOAD_ERROR);
                    //下载失败把文件删除（如果要支持断点续传则不要这一步）
                    apkFile.delete();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    setResult(RESULT_DOWNLOAD_ERROR);
                    //下载失败把文件删除（如果要支持断点续传则不要这一步）
                    apkFile.delete();
                }
            }
        }
    }


    @Override
    protected void onCancel() {
        mDownLoadListener.onCancel();
    }

    @Override
    public void onFinish(boolean canceled) {
        mDownLoadListener.onDone(canceled, mResult);
    }

    /**
     * 将中文字符转码
     *
     * @param url
     * @return
     */
    public String encodedURL(String url) throws UnsupportedEncodingException {
        StringBuffer encodeUrl = new StringBuffer();
        String chinese = "[\u0391-\uFFE5]";
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
        for (int i = 0; i < url.length(); i++) {
            /* 获取一个字符 */
            String temp = url.substring(i, i + 1);
            /* 判断是否为中文字符 */
            if (temp.matches(chinese)) {
                /* 中文字符长度为2 */
                encodeUrl.append(URLEncoder.encode(temp, "UTF-8"));
            } else {
                /* 其他字符长度为1 */
                encodeUrl.append(temp);
            }
        }
        return encodeUrl.toString().replaceAll(" ", "%20");//转码所有空格
    }

    /**
     * @param paramString
     * @return
     */
    public String toURLEncoded(String paramString) {
        if (paramString == null || paramString.equals("")) {
            Log.d(TAG, "toURLEncoded error:" + paramString);
            return "";
        }

        try {
            String str = new String(paramString.getBytes(), "UTF-8");
            str = URLEncoder.encode(str, "UTF-8");
            return str;
        } catch (Exception localException) {
            Log.d(TAG, "toURLEncoded error:" + paramString, localException);
        }

        return "";
    }

    public static String toURLDecoded(String paramString) {
        if (paramString == null || paramString.equals("")) {
            Log.d(TAG, "toURLDecoded error:" + paramString);
            return "";
        }

        try {
            String str = new String(paramString.getBytes(), "UTF-8");
            str = URLDecoder.decode(str, "UTF-8");
            return str;
        } catch (Exception localException) {
            Log.d(TAG, "toURLDecoded error:" + paramString, localException);
        }

        return "";
    }
}
