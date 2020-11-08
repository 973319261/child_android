package com.android.utils;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;

/**
 * Created by jac_cheng on 2017/8/18.
 */

public class MediaScanner {
    private MediaScannerConnection mediaScanConn = null;
    private PhotoSannerClient client = null;
    private String filePath = null;
    private String fileType = null;
    private static MediaScanner mediaScanner = null;
    private Context context;
    /**
     * 然后调用MediaScanner.scanFile("/sdcard/2.mp3");
     */

    public MediaScanner(Context context) {
        // 创建MusicSannerClient
        if (client == null) {
            client = new PhotoSannerClient();
        }
        if (mediaScanConn == null) {
            mediaScanConn = new MediaScannerConnection(context, client);
        }
    }

    public static MediaScanner getInstanc(Context context) {
        if (mediaScanner == null) {
            mediaScanner = new MediaScanner(context);
        }
        return mediaScanner;
    }

    private class PhotoSannerClient implements
            MediaScannerConnection.MediaScannerConnectionClient {

        public void onMediaScannerConnected() {

            if (filePath != null) {
                mediaScanConn.scanFile(filePath, fileType);
            }

            filePath = null;
            fileType = null;
        }

        public void onScanCompleted(String path, Uri uri) {
            mediaScanConn.disconnect();
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(uri);
            context.sendBroadcast(mediaScanIntent);
        }

    }

    /**
     * 扫描文件标签信息
     *
     * @param filePath 文件路径 eg:/sdcard/MediaPlayer/dahai.mp3
     * @param fileType 文件类型 eg: audio/mp3 media/* application/ogg
     */

    public void scanFile(String filePath, String fileType) {
        this.filePath = filePath;
        this.fileType = fileType;
        // 连接之后调用MusicSannerClient的onMediaScannerConnected()方法
        mediaScanConn.connect();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

}
