package com.dunamis.world.lspalm.util;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dunamis.world.lspalm.R;

public class DialogUtils {

    /**
     * 设置对话框宽高
     *
     * @param dialog
     * @param width
     * @param height
     */
    public static void setProgressDialogWidth(AlertDialog dialog, int width, int height) {
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = (width <= 0) ? ViewGroup.LayoutParams.WRAP_CONTENT : width;
        lp.height = (height <= 0) ? ViewGroup.LayoutParams.WRAP_CONTENT : height;
        if (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            WindowManager m = dialog.getWindow().getWindowManager();
            Display d = m.getDefaultDisplay();
            lp.width = (int) (d.getWidth() * 0.95);
        }
        dialog.getWindow().setAttributes(lp);
    }

    /**
     * 设置对话框宽高
     *
     * @param dialog
     * @param width
     * @param height
     */
    public static void setDialogWidth(AlertDialog dialog, int width, int height) {
        dialog.getWindow().setBackgroundDrawable(new BitmapDrawable());
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = (width <= 0) ? ViewGroup.LayoutParams.WRAP_CONTENT : width;
        lp.height = (height <= 0) ? ViewGroup.LayoutParams.WRAP_CONTENT : height;
        if (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            WindowManager m = dialog.getWindow().getWindowManager();
            Display d = m.getDefaultDisplay();
            lp.width = (int) (d.getWidth() * 0.95);
        }
        dialog.getWindow().setAttributes(lp);
    }

    /**
     * 创建加载对话框
     */
    public static Dialog createLoadingDialog(Context context, String message, boolean modal) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null);

        // 获取整个布局
        LinearLayout layout = view
                .findViewById(R.id.dialog_view);
        // 页面中的Img
        ImageView img = view.findViewById(R.id.img);
        // 页面中显示文本
        TextView tipText = view.findViewById(R.id.tipTextView);

        // 加载动画，动画用户使img图片不停的旋转
        Animation animation = AnimationUtils.loadAnimation(
                context,
                R.anim.anim_loding
        );
        // 显示动画
        img.startAnimation(animation);
        // 显示文本
        tipText.setText(message);

        // 创建自定义样式的Dialog
        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);
        // 设置返回键无效
        loadingDialog.setCancelable(!modal);
        loadingDialog.setContentView(
                layout, new ActionBar.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );
        return loadingDialog;
    }
}
