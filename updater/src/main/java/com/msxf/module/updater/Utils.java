package com.msxf.module.updater;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.List;
import timber.log.Timber;

/**
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
final class Utils {
  static final int DEFAULT_READ_TIMEOUT = 30 * 1000;
  static final int DEFAULT_WRITE_TIMEOUT = 30 * 1000;
  static final int DEFAULT_CONNECT_TIMEOUT = 20 * 1000;
  static final String EXTRA_PERMISSIONS = "com.msxf.EXTRA_PERMISSIONS";
  static final String EXTRA_FORCE_CHECK = "com.msxf.EXTRA_FORCE_CHECK";
  static final String EXTRA_SILENT = "com.msxf.EXTRA_SILENT";
  static final String EXTRA_COMMAND = "com.msxf.EXTRA_COMMAND";
  static final String DEVELOPMENT_URL = "http://update2.msxf.lotest/";
  static final String PRODUCTION_URL = "https://update.msxf.com/";
  static final int FORCE_UPDATE = 1;

  private Utils() {
    /* no instance */
  }

  /**
   * Get version code of current application.
   *
   * @param context the context to use
   * @return version code
   */
  static int getVersionCode(Context context) {
    try {
      PackageInfo info = context.getApplicationContext()
          .getPackageManager()
          .getPackageInfo(context.getPackageName(), 0);
      return info.versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      return 0;
    }
  }

  /**
   * Calculate the hash of given file.
   *
   * @param filePath file path
   * @return hash
   */
  static String fileHash(String filePath) {
    File file = new File(filePath);
    if (!file.exists()) {
      return "";
    }

    String result;
    FileInputStream fis = null;

    try {
      fis = new FileInputStream(file);
      MappedByteBuffer mbf = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(mbf);
      BigInteger bi = new BigInteger(1, md.digest());
      result = bi.toString(16);

      while (result.length() < 32) {
        result = "0" + result;
      }
    } catch (Exception e) {
      return "";
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException ignore) {
          /* ignore */
        }
      }
    }

    return result;
  }

  /**
   * Get destination file path with given url.
   *
   * @param url url
   * @return destination file path
   */
  static String getDestinationPath(String url) {
    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    if (file == null) {
      return null;
    }

    int index = url.lastIndexOf("/") + 1;

    return file + File.separator + url.substring(index);
  }

  /**
   * Get install {@link Intent}.
   *
   * @param apkPath apk path
   * @return install {@link Intent}
   */
  static Intent getInstallIntent(Context context, String apkPath) {
    if (apkPath == null) {
      return null;
    }

    Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        | Intent.FLAG_GRANT_READ_URI_PERMISSION);

    Uri uri;
    File file = new File(apkPath);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
    } else {
      uri = Uri.fromFile(file);
    }
    intent.setData(uri);

    return intent;
  }

  /**
   * Install apk with given apk path.
   *
   * @param context the context to use
   * @param apkPath apk path
   */
  static void installApk(Context context, String apkPath) {
    Intent intent = getInstallIntent(context, apkPath);
    if (intent == null) {
      return;
    }

    try {
      context.startActivity(intent);
    } catch (ActivityNotFoundException e) {
      Timber.e(e, "Install error: %s", e.getMessage());
    }
  }

  /**
   * Check {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} permission.
   *
   * @return true if granted permission, otherwise return false
   */
  static boolean checkPermissions(Context context) {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        == PackageManager.PERMISSION_GRANTED;
  }

  /**
   * To check if current app is running foreground.
   *
   * @param context context to use
   * @return true if running foreground, otherwise return false
   */
  static boolean isForeground(Context context) {
    ActivityManager activityManager =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> appProcesses =
        activityManager.getRunningAppProcesses();
    if (appProcesses == null) {
      return false;
    }

    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
      if (appProcess.processName.equals(context.getPackageName())) {
        return appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
      }
    }

    return false;
  }

  /**
   * Kill app.
   *
   * @param activity activity to use.
   */
  static void killApp(Activity activity) {
    try {
      activity.moveTaskToBack(true);
    } catch (Exception ignore) {
    }

    System.exit(0);
    Runtime.getRuntime().exit(0);
  }
}
