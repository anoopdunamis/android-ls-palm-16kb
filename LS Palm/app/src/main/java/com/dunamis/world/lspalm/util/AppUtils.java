package com.dunamis.world.lspalm.util;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Application;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * app工具
 */
public final class AppUtils {

    private static final String TAG = "AppUtils";

    private static final ActivityLifecycleImpl ACTIVITY_LIFECYCLE = new ActivityLifecycleImpl();
    private static final ExecutorService APP_UTIL_POOL = Executors.newFixedThreadPool(3);
    private static final Handler APP_UTIL_HANDLER = new Handler(Looper.getMainLooper());

    @SuppressLint("StaticFieldLeak")
    private static Application instance;

    public static void init(final Context context) {
        if (context == null) {
            init(getApplicationByReflect());
            return;
        }
        init((Application) context.getApplicationContext());
    }

    public static void init(final Application app) {
        if (instance == null) {
            if (app == null) {
                instance = getApplicationByReflect();
            } else {
                instance = app;
            }
            instance.registerActivityLifecycleCallbacks(ACTIVITY_LIFECYCLE);
        } else {
            if (app != null && app.getClass() != instance.getClass()) {
                instance.unregisterActivityLifecycleCallbacks(ACTIVITY_LIFECYCLE);
                ACTIVITY_LIFECYCLE.mActivityList.clear();
                instance = app;
                instance.registerActivityLifecycleCallbacks(ACTIVITY_LIFECYCLE);
            }
        }
    }

    public static Application getApp() {
        if (instance != null) {
            return instance;
        }
        Application app = getApplicationByReflect();
        init(app);
        return app;
    }

    /**
     * 判断 App 是否是系统应用
     */
    public static boolean isAppSystem() {
        return isAppSystem(AppUtils.getApp().getPackageName());
    }

    /**
     * 判断 App 是否是系统应用
     *
     * @param packageName 包名
     */
    public static boolean isAppSystem(final String packageName) {
        if (StringUtils.isBlank(packageName)) {
            return false;
        }
        try {
            PackageManager pm = AppUtils.getApp().getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return ai != null && (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 是否app已经在前端运行
     */
    public static boolean isAppForeground() {
        ActivityManager am =
                (ActivityManager) AppUtils.getApp().getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return false;
        }
        List<ActivityManager.RunningAppProcessInfo> info = am.getRunningAppProcesses();
        if (info == null || info.size() == 0) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo aInfo : info) {
            if (aInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return aInfo.processName.equals(AppUtils.getApp().getPackageName());
            }
        }
        return false;
    }

    /**
     * 判断 App 是否处于前台
     * {@code <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />}</p>
     *
     * @param packageName 包名
     */
    public static boolean isAppForeground(@NonNull final String packageName) {
        return StringUtils.isNotEmpty(packageName) && packageName.equals(getForegroundProcessName());
    }

    /**
     * 判断 App 是否运行
     *
     * @param pkgName 包名
     */
    public static boolean isAppRunning(@NonNull final String pkgName) {
        int uid;
        PackageManager packageManager = AppUtils.getApp().getPackageManager();
        try {
            ApplicationInfo ai = packageManager.getApplicationInfo(pkgName, 0);
            if (ai == null) {
                return false;
            }
            uid = ai.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        ActivityManager am = (ActivityManager) AppUtils.getApp().getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(Integer.MAX_VALUE);
            if (taskInfo != null && taskInfo.size() > 0) {
                for (ActivityManager.RunningTaskInfo aInfo : taskInfo) {
                    if (pkgName.equals(aInfo.baseActivity.getPackageName())) {
                        return true;
                    }
                }
            }
            List<ActivityManager.RunningServiceInfo> serviceInfo = am.getRunningServices(Integer.MAX_VALUE);
            if (serviceInfo != null && serviceInfo.size() > 0) {
                for (ActivityManager.RunningServiceInfo aInfo : serviceInfo) {
                    if (uid == aInfo.uid) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断Service是否在运行
     *
     * @param context
     * @param serviceName 包名+服务的类名（例如：com.hfims.android.assist.daemon.DaemonService）
     */
    public static boolean isServiceRunning(Context context, String serviceName) {
        boolean isWork = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = am.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    /**
     * 重启app
     */
    public static void relaunchApp() {
        relaunchApp(false);
    }

    /**
     * 重启app
     *
     * @param isKillProcess 是否杀掉app进程
     */
    public static void relaunchApp(final boolean isKillProcess) {
        Intent intent = getLaunchAppIntent(AppUtils.getApp().getPackageName(), true);
        if (intent == null) {
            return;
        }
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        AppUtils.getApp().startActivity(intent);
        if (!isKillProcess) {
            return;
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * 退出程序
     */
    public static void exitApp() {
        List<Activity> activityList = getActivityList();
        for (int i = activityList.size() - 1; i >= 0; --i) {// remove from top
            Activity activity = activityList.get(i);
            // sActivityList remove the index activity at onActivityDestroyed
            activity.finish();
        }
        System.exit(0);
    }

    /**
     * 结束所有activity
     */
    public static void finishAllActivities() {
        finishAllActivities(false);
    }

    /**
     * 结束所有activity
     *
     * @param isLoadAnim 是否显示动画
     */
    public static void finishAllActivities(final boolean isLoadAnim) {
        List<Activity> activityList = getActivityList();
        for (Activity act : activityList) {
            // sActivityList remove the index activity at onActivityDestroyed
            act.finish();
            if (!isLoadAnim) {
                act.overridePendingTransition(0, 0);
            }
        }
    }

    /**
     * 获取app版本名
     */
    public static String getAppVersionName() {
        return getAppVersionName(AppUtils.getApp().getPackageName());
    }

    /**
     * 获取app版本名
     *
     * @param packageName 包名.
     */
    public static String getAppVersionName(final String packageName) {
        if (StringUtils.isBlank(packageName)) {
            return "";
        }
        try {
            PackageManager pm = AppUtils.getApp().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? null : pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取app版本号
     */
    public static int getAppVersionCode() {
        return getAppVersionCode(AppUtils.getApp().getPackageName());
    }

    /**
     * 获取app版本号
     *
     * @param packageName 包名
     */
    public static int getAppVersionCode(final String packageName) {
        if (StringUtils.isBlank(packageName)) {
            return -1;
        }
        try {
            PackageManager pm = AppUtils.getApp().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            return pi == null ? -1 : pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 打开指定App
     *
     * @param packageName
     */
    public static boolean runApp(String packageName, boolean toFront) {
        try {
            PackageManager packageManager = AppUtils.getApp().getPackageManager();
            if (checkPackageInfo(packageName)) {
                Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (toFront) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                }
                AppUtils.getApp().startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 打开指定App
     *
     * @param packageName
     */
    public static boolean runApp(String packageName, boolean toFront, Map<String, String> map) {
        try {
            PackageManager packageManager = AppUtils.getApp().getPackageManager();
            if (checkPackageInfo(packageName)) {
                Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (toFront) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                }
                if (null != map && !map.isEmpty()) {
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        intent.putExtra(entry.getKey(), entry.getValue());
                    }
                }
                AppUtils.getApp().startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 打开指定App
     *
     * @param packageName
     */
    public static boolean runApp(Context context, String packageName, boolean toFront, Map<String, String> map) {
        try {
            PackageManager packageManager = context.getPackageManager();
            if (checkPackageInfo(packageName)) {
                Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (toFront) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                }
                if (null != map && !map.isEmpty()) {
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        intent.putExtra(entry.getKey(), entry.getValue());
                    }
                }
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检查包是否存在
     *
     * @param packname
     */
    public static boolean checkPackageInfo(String packname) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = AppUtils.getApp().getPackageManager().getPackageInfo(packname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }

    /**
     * 检查包是否存在
     *
     * @param packname
     */
    public static boolean checkPackageInfo(Context context, String packname) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packname, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }

    /**
     * 当前屏幕是否为竖屏
     */
    public static boolean isScreenOriatationPortrait() {
        return AppUtils.getApp().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private static Intent getLaunchAppIntent(final String packageName, final boolean isNewTask) {
        String launcherActivity = getLauncherActivity(packageName);
        if (!launcherActivity.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName(packageName, launcherActivity);
            intent.setComponent(cn);
            return isNewTask ? intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) : intent;
        }
        return null;
    }

    private static String getLauncherActivity(@NonNull final String pkg) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(pkg);
        PackageManager pm = AppUtils.getApp().getPackageManager();
        List<ResolveInfo> info = pm.queryIntentActivities(intent, 0);
        int size = info.size();
        if (size == 0) {
            return "";
        }
        for (int i = 0; i < size; i++) {
            ResolveInfo ri = info.get(i);
            if (ri.activityInfo.processName.equals(pkg)) {
                return ri.activityInfo.name;
            }
        }
        return info.get(0).activityInfo.name;
    }

    private static String getForegroundProcessName() {
        ActivityManager am = (ActivityManager) AppUtils.getApp().getSystemService(Context.ACTIVITY_SERVICE);
        //noinspection ConstantConditions
        List<ActivityManager.RunningAppProcessInfo> pInfo = am.getRunningAppProcesses();
        if (pInfo != null && pInfo.size() > 0) {
            for (ActivityManager.RunningAppProcessInfo aInfo : pInfo) {
                if (aInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return aInfo.processName;
                }
            }
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            PackageManager pm = AppUtils.getApp().getPackageManager();
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (list.size() <= 0) {
                return "";
            }
            try {// Access to usage information.
                ApplicationInfo info = pm.getApplicationInfo(AppUtils.getApp().getPackageName(), 0);
                AppOpsManager aom = (AppOpsManager) AppUtils.getApp().getSystemService(Context.APP_OPS_SERVICE);
                //noinspection ConstantConditions
                if (aom.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        info.uid,
                        info.packageName) != AppOpsManager.MODE_ALLOWED) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    AppUtils.getApp().startActivity(intent);
                }
                if (aom.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        info.uid,
                        info.packageName) != AppOpsManager.MODE_ALLOWED) {
                    return "";
                }
                UsageStatsManager usageStatsManager = (UsageStatsManager) AppUtils.getApp().getSystemService(Context.USAGE_STATS_SERVICE);
                List<UsageStats> usageStatsList = null;
                if (usageStatsManager != null) {
                    long endTime = System.currentTimeMillis();
                    long beginTime = endTime - 86400000 * 7;
                    usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginTime, endTime);
                }
                if (usageStatsList == null || usageStatsList.isEmpty()) {
                    return null;
                }
                UsageStats recentStats = null;
                for (UsageStats usageStats : usageStatsList) {
                    if (recentStats == null || usageStats.getLastTimeUsed() > recentStats.getLastTimeUsed()) {
                        recentStats = usageStats;
                    }
                }
                return recentStats == null ? null : recentStats.getPackageName();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private static boolean isFileExists(final File file) {
        return file != null && file.exists();
    }

    private static File getFileByPath(final String filePath) {
        return StringUtils.isBlank(filePath) ? null : new File(filePath);
    }

    private AppUtils() {
    }

    static class ActivityLifecycleImpl implements Application.ActivityLifecycleCallbacks {

        final LinkedList<Activity> mActivityList = new LinkedList<>();
        final HashMap<Object, OnAppStatusChangedListener> mStatusListenerMap = new HashMap<>();
        final Map<Activity, Set<OnActivityDestroyedListener>> mDestroyedListenerMap = new HashMap<>();

        private int mForegroundCount = 0;
        private int mConfigCount = 0;
        private boolean mIsBackground = false;

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            setAnimatorsEnabled();
            setTopActivity(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (!mIsBackground) {
                setTopActivity(activity);
            }
            if (mConfigCount < 0) {
                ++mConfigCount;
            } else {
                ++mForegroundCount;
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            setTopActivity(activity);
            if (mIsBackground) {
                mIsBackground = false;
                postStatus(true);
            }
            processHideSoftInputOnActivityDestroy(activity, false);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            // ignore
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (activity.isChangingConfigurations()) {
                --mConfigCount;
            } else {
                --mForegroundCount;
                if (mForegroundCount <= 0) {
                    mIsBackground = true;
                    postStatus(false);
                }
            }
            processHideSoftInputOnActivityDestroy(activity, true);
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            // ignore
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            mActivityList.remove(activity);
            consumeOnActivityDestroyedListener(activity);
            fixSoftInputLeaks(activity.getWindow());
        }

        Activity getTopActivity() {
            if (!mActivityList.isEmpty()) {
                for (int i = mActivityList.size() - 1; i >= 0; i--) {
                    Activity activity = mActivityList.get(i);
                    if (activity == null
                            || activity.isFinishing()
                            || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed())) {
                        continue;
                    }
                    return activity;
                }
            }
            Activity topActivityByReflect = getTopActivityByReflect();
            if (topActivityByReflect != null) {
                setTopActivity(topActivityByReflect);
            }
            return topActivityByReflect;
        }

        void addOnAppStatusChangedListener(final Object object, final OnAppStatusChangedListener listener) {
            mStatusListenerMap.put(object, listener);
        }

        void removeOnAppStatusChangedListener(final Object object) {
            mStatusListenerMap.remove(object);
        }

        void addOnActivityDestroyedListener(final Activity activity, final OnActivityDestroyedListener listener) {
            if (activity == null || listener == null) {
                return;
            }
            Set<OnActivityDestroyedListener> listeners;
            if (!mDestroyedListenerMap.containsKey(activity)) {
                listeners = new HashSet<>();
                mDestroyedListenerMap.put(activity, listeners);
            } else {
                listeners = mDestroyedListenerMap.get(activity);
                if (listeners.contains(listener)) {
                    return;
                }
            }
            listeners.add(listener);
        }

        void removeOnActivityDestroyedListener(final Activity activity) {
            if (activity == null) {
                return;
            }
            mDestroyedListenerMap.remove(activity);
        }

        /**
         * To solve close keyboard when activity onDestroy.
         * The preActivity set windowSoftInputMode will prevent
         * the keyboard from closing when curActivity onDestroy.
         */
        private void processHideSoftInputOnActivityDestroy(final Activity activity, boolean isSave) {
            if (isSave) {
                final WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
                final int softInputMode = attrs.softInputMode;
                activity.getWindow().getDecorView().setTag(-123, softInputMode);
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            } else {
                final Object tag = activity.getWindow().getDecorView().getTag(-123);
                if (!(tag instanceof Integer)) {
                    return;
                }
                runOnUiThreadDelayed(new Runnable() {
                    @Override
                    public void run() {
                        activity.getWindow().setSoftInputMode(((Integer) tag));
                    }
                }, 100);
            }
        }

        private void postStatus(final boolean isForeground) {
            if (mStatusListenerMap.isEmpty()) {
                return;
            }
            for (OnAppStatusChangedListener onAppStatusChangedListener : mStatusListenerMap.values()) {
                if (onAppStatusChangedListener == null) {
                    return;
                }
                if (isForeground) {
                    onAppStatusChangedListener.onForeground();
                } else {
                    onAppStatusChangedListener.onBackground();
                }
            }
        }

        private void setTopActivity(final Activity activity) {
            if (mActivityList.contains(activity)) {
                if (!mActivityList.getLast().equals(activity)) {
                    mActivityList.remove(activity);
                    mActivityList.addLast(activity);
                }
            } else {
                mActivityList.addLast(activity);
            }
        }

        private void consumeOnActivityDestroyedListener(Activity activity) {
            Iterator<Map.Entry<Activity, Set<OnActivityDestroyedListener>>> iterator
                    = mDestroyedListenerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Activity, Set<OnActivityDestroyedListener>> entry = iterator.next();
                if (entry.getKey() == activity) {
                    Set<OnActivityDestroyedListener> value = entry.getValue();
                    for (OnActivityDestroyedListener listener : value) {
                        listener.onActivityDestroyed(activity);
                    }
                    iterator.remove();
                }
            }
        }

        private Activity getTopActivityByReflect() {
            try {
                @SuppressLint("PrivateApi")
                Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
                Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
                Field activitiesField = activityThreadClass.getDeclaredField("mActivityList");
                activitiesField.setAccessible(true);
                Map activities = (Map) activitiesField.get(activityThread);
                if (activities == null) {
                    return null;
                }
                for (Object activityRecord : activities.values()) {
                    Class activityRecordClass = activityRecord.getClass();
                    Field pausedField = activityRecordClass.getDeclaredField("paused");
                    pausedField.setAccessible(true);
                    if (!pausedField.getBoolean(activityRecord)) {
                        Field activityField = activityRecordClass.getDeclaredField("activity");
                        activityField.setAccessible(true);
                        return (Activity) activityField.get(activityRecord);
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    static ActivityLifecycleImpl getActivityLifecycle() {
        return ACTIVITY_LIFECYCLE;
    }

    static LinkedList<Activity> getActivityList() {
        return ACTIVITY_LIFECYCLE.mActivityList;
    }

    static Context getTopActivityOrApp() {
        if (isAppForeground()) {
            Activity topActivity = ACTIVITY_LIFECYCLE.getTopActivity();
            return topActivity == null ? AppUtils.getApp() : topActivity;
        } else {
            return AppUtils.getApp();
        }
    }

    static <T> Task<T> doAsync(final Task<T> task) {
        APP_UTIL_POOL.execute(task);
        return task;
    }

    public static void runOnUiThread(final Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            APP_UTIL_HANDLER.post(runnable);
        }
    }

    public static void runOnUiThreadDelayed(final Runnable runnable, long delayMillis) {
        APP_UTIL_HANDLER.postDelayed(runnable, delayMillis);
    }

    static void fixSoftInputLeaks(final Window window) {
        InputMethodManager imm = (InputMethodManager) AppUtils.getApp().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        String[] leakViews = new String[]{"mLastSrvView", "mCurRootView", "mServedView", "mNextServedView"};
        for (String leakView : leakViews) {
            try {
                Field leakViewField = InputMethodManager.class.getDeclaredField(leakView);
                if (leakViewField == null) {
                    continue;
                }
                if (!leakViewField.isAccessible()) {
                    leakViewField.setAccessible(true);
                }
                Object obj = leakViewField.get(imm);
                if (!(obj instanceof View)) {
                    continue;
                }
                View view = (View) obj;
                if (view.getRootView() == window.getDecorView().getRootView()) {
                    leakViewField.set(imm, null);
                }
            } catch (Throwable ignore) {/**/}
        }
    }

    private static Application getApplicationByReflect() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object thread = activityThread.getMethod("currentActivityThread").invoke(null);
            Object app = activityThread.getMethod("getApplication").invoke(thread);
            if (app == null) {
                throw new NullPointerException("u should init first");
            }
            return (Application) app;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("u should init first");
    }

    /**
     * Set animators enabled.
     */
    private static void setAnimatorsEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && ValueAnimator.areAnimatorsEnabled()) {
            return;
        }
        try {
            //noinspection JavaReflectionMemberAccess
            Field sDurationScaleField = ValueAnimator.class.getDeclaredField("sDurationScale");
            sDurationScaleField.setAccessible(true);
            float sDurationScale = (Float) sDurationScaleField.get(null);
            if (sDurationScale == 0f) {
                sDurationScaleField.set(null, 1f);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // interface
    ///////////////////////////////////////////////////////////////////////////

    public abstract static class Task<Result> implements Runnable {

        private static final int NEW = 0;
        private static final int COMPLETING = 1;
        private static final int CANCELLED = 2;
        private static final int EXCEPTIONAL = 3;

        private volatile int state = NEW;

        abstract Result doInBackground();

        private Callback<Result> mCallback;

        public Task(final Callback<Result> callback) {
            mCallback = callback;
        }

        @Override
        public void run() {
            try {
                final Result t = doInBackground();

                if (state != NEW) {
                    return;
                }
                state = COMPLETING;
                APP_UTIL_HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.onCall(t);
                    }
                });
            } catch (Throwable th) {
                if (state != NEW) {
                    return;
                }
                state = EXCEPTIONAL;
            }
        }

        public void cancel() {
            state = CANCELLED;
        }

        public boolean isDone() {
            return state != NEW;
        }

        public boolean isCanceled() {
            return state == CANCELLED;
        }
    }

    public interface Callback<T> {
        void onCall(T data);
    }

    public interface OnAppStatusChangedListener {
        void onForeground();

        void onBackground();
    }

    public interface OnActivityDestroyedListener {
        void onActivityDestroyed(Activity activity);
    }
}
