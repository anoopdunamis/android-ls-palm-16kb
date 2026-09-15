package com.dunamis.world.lspalm.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.RawRes;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 资源处理工具
 */
public final class ResourceUtils {

    private static final int BUFFER_SIZE = 8192;

    /**
     * 从assets获取bitmap
     *
     * @param path 图片名称
     */
    public static Bitmap getAssetsBitmap(String path) {
        InputStream inputStream = null;
        Bitmap bitmap = null;
        try {
            inputStream = AppUtils.getApp().getAssets().open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (null != inputStream) {
            bitmap = BitmapFactory.decodeStream(inputStream);
        }
        return bitmap;
    }

    /**
     * 从assets目录复制文件
     */
    public static boolean copyFileFromAssets(final String assetsFilePath, final String destFilePath) {
        boolean res = true;
        try {
            String[] assets = AppUtils.getApp().getAssets().list(assetsFilePath);
            if (null != assets && assets.length > 0) {
                for (String asset : assets) {
                    res &= copyFileFromAssets(assetsFilePath + "/" + asset, destFilePath + "/" + asset);
                }
            } else {
                res = writeFileFromIS(destFilePath, AppUtils.getApp().getAssets().open(assetsFilePath), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
            res = false;
        }
        return res;
    }

    /**
     * 获取assets目录的文件列表
     */
    public static String[] getListFromAssets(final String assetsFilePath) {
        try {
            return AppUtils.getApp().getAssets().list(assetsFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取assets文件内容
     *
     * @param assetsFilePath assets文件路径
     */
    public static String readAssets2String(final String assetsFilePath) {
        return readAssets2String(assetsFilePath, null);
    }

    /**
     * 获取assets文件内容
     *
     * @param assetsFilePath assets文件路径
     * @param charsetName    编码格式
     */
    public static String readAssets2String(final String assetsFilePath, final String charsetName) {
        InputStream is;
        try {
            is = AppUtils.getApp().getAssets().open(assetsFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        byte[] bytes = is2Bytes(is);
        if (bytes == null) {
            return null;
        }
        if (StringUtils.isBlank(charsetName)) {
            return new String(bytes);
        } else {
            try {
                return new String(bytes, charsetName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    /**
     * 获取assets文件内容
     *
     * @param assetsPath assets文件路径
     */
    public static List<String> readAssets2List(final String assetsPath) {
        return readAssets2List(assetsPath, null);
    }

    /**
     * 获取assets文件内容
     *
     * @param assetsPath  assets文件路径
     * @param charsetName 编码格式
     */
    public static List<String> readAssets2List(final String assetsPath, final String charsetName) {
        try {
            return is2List(AppUtils.getApp().getResources().getAssets().open(assetsPath), charsetName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从raw复制文件
     *
     * @param resId        资源ID
     * @param destFilePath 目标路径
     */
    public static boolean copyFileFromRaw(@RawRes final int resId, final String destFilePath) {
        return writeFileFromIS(destFilePath, AppUtils.getApp().getResources().openRawResource(resId), false);
    }

    public static String readRaw2String(@RawRes final int resId) {
        return readRaw2String(resId, null);
    }

    public static String readRaw2String(@RawRes final int resId, final String charsetName) {
        InputStream is = AppUtils.getApp().getResources().openRawResource(resId);
        byte[] bytes = is2Bytes(is);
        if (bytes == null) {
            return null;
        }
        if (StringUtils.isBlank(charsetName)) {
            return new String(bytes);
        } else {
            try {
                return new String(bytes, charsetName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    public static List<String> readRaw2List(@RawRes final int resId) {
        return readRaw2List(resId, null);
    }

    public static List<String> readRaw2List(@RawRes final int resId, final String charsetName) {
        return is2List(AppUtils.getApp().getResources().openRawResource(resId), charsetName);
    }

    private static boolean writeFileFromIS(final String filePath, final InputStream is, final boolean append) {
        return writeFileFromIS(FileUtils.getFileByPath(filePath), is, append);
    }

    private static boolean writeFileFromIS(final File file, final InputStream is, final boolean append) {
        if (!FileUtils.createOrExistsFile(file) || is == null) {
            return false;
        }
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, append));
            byte[] data = new byte[BUFFER_SIZE];
            int len;
            while ((len = is.read(data, 0, BUFFER_SIZE)) != -1) {
                os.write(data, 0, len);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] is2Bytes(final InputStream is) {
        if (is == null) {
            return null;
        }
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            byte[] b = new byte[BUFFER_SIZE];
            int len;
            while ((len = is.read(b, 0, BUFFER_SIZE)) != -1) {
                os.write(b, 0, len);
            }
            return os.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<String> is2List(final InputStream is, final String charsetName) {
        BufferedReader reader = null;
        try {
            List<String> list = new ArrayList<>();
            if (StringUtils.isBlank(charsetName)) {
                reader = new BufferedReader(new InputStreamReader(is));
            } else {
                reader = new BufferedReader(new InputStreamReader(is, charsetName));
            }
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
            return list;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ResourceUtils() {
    }
}
