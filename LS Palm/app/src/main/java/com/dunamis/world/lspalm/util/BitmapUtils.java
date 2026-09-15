package com.dunamis.world.lspalm.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Bitmap操作工具
 */
public final class BitmapUtils {

    private static final String TAG = "BitmapUtils";

    /**
     * bitmap to bytes
     *
     * @param format 压缩格式
     */
    public static byte[] bitmap2Bytes(final Bitmap bitmap, final Bitmap.CompressFormat format) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, 100, baos);
        return baos.toByteArray();
    }

    /**
     * bitmap to bytes
     *
     * @param format 压缩格式
     */
    public static byte[] bitmap2Bytes(final Bitmap bitmap, final Bitmap.CompressFormat format, int quality) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(format, quality, baos);
        return baos.toByteArray();
    }

    /**
     * bytes to bitmap
     */
    public static Bitmap bytes2Bitmap(final byte[] bytes) {
        return (bytes == null || bytes.length == 0) ? null : BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Drawable to bitmap
     */
    public static Bitmap drawable2Bitmap(final Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        Bitmap bitmap;
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1,
                    drawable.getOpacity() != PixelFormat.OPAQUE
                            ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE
                            ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Bitmap to drawable
     */
    public static Drawable bitmap2Drawable(final Resources resources, final Bitmap bitmap) {
        return bitmap == null ? null : new BitmapDrawable(resources, bitmap);
    }

    /**
     * Drawable to bytes
     *
     * @param format 压缩格式
     */
    public static byte[] drawable2Bytes(final Drawable drawable, final Bitmap.CompressFormat format) {
        return drawable == null ? null : bitmap2Bytes(drawable2Bitmap(drawable), format);
    }

    /**
     * Bytes to drawable
     */
    public static Drawable bytes2Drawable(final Resources resources, final byte[] bytes) {
        return bitmap2Drawable(resources, bytes2Bitmap(bytes));
    }

    /**
     * Return bitmap
     */
    public static Bitmap getBitmap(final File file) {
        if (file == null) {
            return null;
        }
        try {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return bitmap
     */
    public static Bitmap getBitmap(final File file, final int maxWidth, final int maxHeight) {
        if (file == null) {
            return null;
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return bitmap
     */
    public static Bitmap getBitmap(final String filePath) {
        if (StringUtils.isBlank(filePath) || !FileUtils.isFileExists(filePath)) {
            return null;
        }
        try {
            return BitmapFactory.decodeFile(filePath);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return bitmap
     */
    public static Bitmap getBitmap(final String filePath, final int maxWidth, final int maxHeight) {
        if (StringUtils.isBlank(filePath)) {
            return null;
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(filePath, options);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return bitmap
     */
    public static Bitmap getBitmap(Resources res, int resId) {
        try {
            return BitmapFactory.decodeResource(res, resId);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 按宽等比缩放图片
     */
    public static Bitmap zoomByWidth(Bitmap bitmap, int newWidth) {
        try {
            // 获得图片的宽高
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            // 计算缩放比例
            float scaleWidth = (float) newWidth / width;
            float scaleHeight = scaleWidth;
            // 取得想要缩放的matrix参数
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // 得到新的图片
            return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return bitmap
     */
    public static Bitmap getBitmap(final InputStream is) {
        if (is == null) {
            return null;
        }
        return BitmapFactory.decodeStream(is);
    }

    /**
     * Return bitmap
     */
    public static Bitmap getBitmap(final InputStream is, final int maxWidth, final int maxHeight) {
        if (is == null) {
            return null;
        }
        byte[] bytes = input2Byte(is);
        return getBitmap(bytes, 0, maxWidth, maxHeight);
    }

    /**
     * Return bitmap
     */
    public static Bitmap getBitmap(final byte[] data, final int offset) {
        if (data.length == 0) {
            return null;
        }
        return BitmapFactory.decodeByteArray(data, offset, data.length);
    }

    /**
     * Return bitmap
     */
    public static Bitmap getBitmap(final byte[] data, final int offset, final int maxWidth, final int maxHeight) {
        if (data.length == 0) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, offset, data.length, options);
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, offset, data.length, options);
    }

    /**
     * Return the sample size
     */
    private static int calculateInSampleSize(final BitmapFactory.Options options, final int maxWidth, final int maxHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        while (height > maxHeight || width > maxWidth) {
            height >>= 1;
            width >>= 1;
            inSampleSize <<= 1;
        }
        return inSampleSize;
    }

    /**
     * 保存bitmap
     *
     * @param src      bitmap对象
     * @param filePath 保存路径
     * @param format   图片格式
     */
    public static boolean save(final Bitmap src, final String filePath, final Bitmap.CompressFormat format) {
        return save(src, FileUtils.getFileByPath(filePath), format, 100, false);
    }

    /**
     * 保存bitmap
     *
     * @param src    bitmap对象
     * @param file   保存文件
     * @param format 图片格式
     */
    public static boolean save(final Bitmap src, final File file, final Bitmap.CompressFormat format) {
        return save(src, file, format, 100, false);
    }

    /**
     * 保存bitmap
     *
     * @param src      bitmap对象
     * @param filePath 保存路径
     * @param format   图片格式
     * @param quality  质量0-100
     */
    public static boolean save(final Bitmap src, final String filePath, final Bitmap.CompressFormat format, @IntRange(from = 0, to = 100) final int quality) {
        return save(src, FileUtils.getFileByPath(filePath), format, quality, false);
    }

    /**
     * 保存bitmap
     *
     * @param src     bitmap对象
     * @param file    保存文件
     * @param format  图片格式
     * @param quality 质量0-100
     */
    public static boolean save(final Bitmap src, final File file, final Bitmap.CompressFormat format, @IntRange(from = 0, to = 100) final int quality) {
        return save(src, file, format, quality, false);
    }

    /**
     * 保存bitmap
     *
     * @param src      bitmap对象
     * @param filePath 保存路径
     * @param format   图片格式
     * @param quality  质量0-100
     * @param recycle  是否循环
     */
    public static boolean save(final Bitmap src, final String filePath, final Bitmap.CompressFormat format, @IntRange(from = 0, to = 100) final int quality, final boolean recycle) {
        return save(src, FileUtils.getFileByPath(filePath), format, quality, recycle);
    }

    /**
     * 保存bitmap
     *
     * @param src     bitmap对象
     * @param file    保存文件
     * @param format  图片格式
     * @param quality 质量0-100
     * @param recycle 是否循环
     */
    public static boolean save(final Bitmap src, final File file, final Bitmap.CompressFormat format, @IntRange(from = 0, to = 100) final int quality, final boolean recycle) {
        if (isEmptyBitmap(src) || !FileUtils.createFileByDeleteOldFile(file)) {
            return false;
        }
        OutputStream os = null;
        boolean ret = false;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            ret = src.compress(format, quality, os);
            os.flush();
            if (recycle && !src.isRecycled()) {
                src.recycle();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    ///////////////////////////////////////////////////////////////////////////
    // about compress
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 缩放图片
     *
     * @param newWidth  宽
     * @param newHeight 高
     */
    public static Bitmap scale(final Bitmap src, final int newWidth, final int newHeight) {
        return scale(src, newWidth, newHeight, false);
    }

    /**
     * 缩放图片
     *
     * @param newWidth  宽
     * @param newHeight 高
     * @param recycle   是否回收
     */
    public static Bitmap scale(final Bitmap src, final int newWidth, final int newHeight, final boolean recycle) {
        if (isEmptyBitmap(src)) {
            return null;
        }
        Bitmap ret = Bitmap.createScaledBitmap(src, newWidth, newHeight, true);
        if (recycle && !src.isRecycled() && ret != src) {
            src.recycle();
        }
        return ret;
    }

    /**
     * 缩放图片
     */
    public static Bitmap scale(final Bitmap src, final float scaleWidth, final float scaleHeight) {
        return scale(src, scaleWidth, scaleHeight, false);
    }

    /**
     * 缩放图片
     *
     * @param scaleWidth  宽
     * @param scaleHeight 高
     * @param recycle     是否回收
     */
    public static Bitmap scale(final Bitmap src, final float scaleWidth, final float scaleHeight, final boolean recycle) {
        if (isEmptyBitmap(src)) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scaleWidth, scaleHeight);
        Bitmap ret = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        if (recycle && !src.isRecycled() && ret != src) {
            src.recycle();
        }
        return ret;
    }

    /**
     * 添加水印
     *
     * @param src      源.
     * @param content  水印内容.
     * @param textSize 水印尺寸.
     * @param color    水印颜色.
     * @param x        横坐标.
     * @param y        纵坐标.
     */
    public static Bitmap addTextWatermark(final Bitmap src,
                                          final String content,
                                          final int textSize,
                                          @ColorInt final int color,
                                          final float x,
                                          final float y) {
        return addTextWatermark(src, content, textSize, color, x, y, false);
    }

    /**
     * 添加水印.
     *
     * @param recycle 源是否回收.
     */
    public static Bitmap addTextWatermark(final Bitmap src,
                                          final String content,
                                          final float textSize,
                                          @ColorInt final int color,
                                          final float x,
                                          final float y,
                                          final boolean recycle) {
        if (isEmptyBitmap(src) || content == null) return null;
        Bitmap ret = src.copy(src.getConfig(), true);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Canvas canvas = new Canvas(ret);
        paint.setColor(color);
        paint.setTextSize(textSize);
        Rect bounds = new Rect();
        paint.getTextBounds(content, 0, content.length(), bounds);
        canvas.drawText(content, x, y + textSize, paint);
        if (recycle && !src.isRecycled() && ret != src) src.recycle();
        return ret;
    }

    /**
     * 按缩放压缩
     */
    public static Bitmap compressByScale(final Bitmap src, final int newWidth, final int newHeight) {
        return scale(src, newWidth, newHeight, false);
    }

    /**
     * 按缩放压缩
     */
    public static Bitmap compressByScale(final Bitmap src, final int newWidth, final int newHeight, final boolean recycle) {
        return scale(src, newWidth, newHeight, recycle);
    }

    /**
     * 按缩放压缩
     */
    public static Bitmap compressByScale(final Bitmap src, final float scaleWidth, final float scaleHeight) {
        return scale(src, scaleWidth, scaleHeight, false);
    }

    /**
     * 按缩放压缩
     */
    public static Bitmap compressByScale(final Bitmap src, final float scaleWidth, final float scaleHeight, final boolean recycle) {
        return scale(src, scaleWidth, scaleHeight, recycle);
    }

    /**
     * 按质量压缩
     *
     * @param quality 质量0-100
     */
    public static Bitmap compressByQuality(final Bitmap src, @IntRange(from = 0, to = 100) final int quality) {
        return compressByQuality(src, quality, false);
    }

    /**
     * 按质量压缩
     *
     * @param src     The source of bitmap.
     * @param quality 质量0-100
     * @param recycle 是否回收
     */
    public static Bitmap compressByQuality(final Bitmap src, @IntRange(from = 0, to = 100) final int quality, final boolean recycle) {
        if (isEmptyBitmap(src)) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] bytes = baos.toByteArray();
        if (recycle && !src.isRecycled()) {
            src.recycle();
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 按质量压缩
     *
     * @param maxByteSize 最大大小
     */
    public static Bitmap compressByQuality(final Bitmap src, final long maxByteSize) {
        return compressByQuality(src, maxByteSize, false);
    }

    /**
     * 按质量压缩
     *
     * @param maxByteSize 最大大小
     * @param recycle     是否回收bitmap
     */
    public static Bitmap compressByQuality(final Bitmap src, final long maxByteSize, final boolean recycle) {
        if (isEmptyBitmap(src) || maxByteSize <= 0) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes;
        if (baos.size() <= maxByteSize) {
            bytes = baos.toByteArray();
        } else {
            baos.reset();
            src.compress(Bitmap.CompressFormat.JPEG, 0, baos);
            if (baos.size() >= maxByteSize) {
                bytes = baos.toByteArray();
            } else {
                // find the best quality using binary search
                int st = 0;
                int end = 100;
                int mid = 0;
                while (st < end) {
                    mid = (st + end) / 2;
                    baos.reset();
                    src.compress(Bitmap.CompressFormat.JPEG, mid, baos);
                    int len = baos.size();
                    if (len == maxByteSize) {
                        break;
                    } else if (len > maxByteSize) {
                        end = mid - 1;
                    } else {
                        st = mid + 1;
                    }
                }
                if (end == mid - 1) {
                    baos.reset();
                    src.compress(Bitmap.CompressFormat.JPEG, st, baos);
                }
                bytes = baos.toByteArray();
            }
        }
        if (recycle && !src.isRecycled()) {
            src.recycle();
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 按大小压缩图片
     */
    public static Bitmap compressBySize(final Bitmap src, final long maxByteSize) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int options = 100;
            src.compress(Bitmap.CompressFormat.JPEG, options, baos);

            while (baos.toByteArray().length > maxByteSize) {
                baos.reset();
                options -= 5;
                if (options <= 5) {
                    options = 5;
                }

                src.compress(Bitmap.CompressFormat.JPEG, options, baos);
                if (options == 5) {
                    break;
                }
            }
            byte[] bytes = baos.toByteArray();
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return src;
    }

    /**
     * 判断是否图片
     */
    public static boolean isImage(final File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        return isImage(file.getPath());
    }

    /**
     * 判断是否图片
     */
    public static boolean isImage(final String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeFile(filePath, options);
            return options.outWidth != -1 && options.outHeight != -1;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isEmptyBitmap(final Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }

    /**
     * 通过base64计算bitmap文件大小（单位:字节）
     */
    public static int getBitmapSizeFromBase64(String base64Data) {
        try {
            String content = base64Data.substring(22); // 去掉头部 "data:image/png;base64,"
            int equalIndex = content.indexOf("="); // 找出等号，并且去掉
            if (equalIndex > 0) {
                content = content.substring(0, equalIndex);
            }
            return (content.length() + 2) / 3 * 4;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * yuv转bitmap
     *
     * @param nv21   yuv数据源
     * @param width  图片宽度
     * @param height 图片高度
     */
    public static Bitmap yuvToBitmap(byte[] nv21, int width, int height) {
        Bitmap bitmap = null;
        try {
            YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            stream.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * yuv转bitmap
     *
     * @param nv21   yuv数据源
     * @param width  图片宽度
     * @param height 图片高度
     */
    public static Bitmap yuvToBitmap(byte[] nv21, int width, int height, Matrix matrixs) {
        Bitmap bitmap = null;
        try {
            YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrixs, true);
            stream.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * yuv转bitmap
     *
     * @param nv21   yuv数据源
     * @param width  图片宽度
     * @param height 图片高度
     */
    public static Bitmap yuvToBitmap(byte[] nv21, int width, int height, int quality, Matrix matrixs) {
        Bitmap bitmap = null;
        try {
            YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, width, height), quality, stream);
            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrixs, true);
            stream.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * yuv转bitmap
     *
     * @param nv21   yuv数据源
     * @param width  图片宽度
     * @param height 图片高度
     */
    public static Bitmap yuvToBitmap(byte[] nv21, int width, int height, Rect rect, Matrix matrixs) {
        Bitmap bitmap = null;
        try {
            YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, width, height), 100, stream);
            bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            bitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height(), matrixs, true);
            stream.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * yuv转bitmap
     *
     * @param nv21   yuv数据源
     * @param width  图片宽度
     * @param height 图片高度
     */
    @Deprecated
    public static Bitmap yuvToBitmap(Context context, byte[] nv21, int width, int height, Matrix matrixs) {
        Bitmap bitmap = null;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                RenderScript rs = RenderScript.create(context);
                ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
                Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
                Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
                Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
                Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

                in.copyFrom(nv21);
                yuvToRgbIntrinsic.setInput(in);
                yuvToRgbIntrinsic.forEach(out);

                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                out.copyTo(bitmap);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrixs, true);
            } else {
                bitmap = yuvToBitmap(nv21, width, height);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private static byte[] input2Byte(final InputStream is) {
        if (is == null) {
            return null;
        }
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int len;
            while ((len = is.read(b, 0, 1024)) != -1) {
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
        }
    }

    private BitmapUtils() {
    }
}
