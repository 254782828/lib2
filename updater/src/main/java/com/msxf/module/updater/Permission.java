package com.msxf.module.updater;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

/**
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
final class Permission {
  @SuppressLint("StaticFieldLeak") private static Permission singleton;
  private static final String[] PERMISSIONS = { Manifest.permission.WRITE_EXTERNAL_STORAGE };
  private final Context context;
  private Callback callback;

  private Permission(Context context) {
    this.context = context.getApplicationContext();
  }

  /**
   * Get a single instance for {@link Permission}
   *
   * @param context context to use
   * @return {@link Permission}
   */
  static Permission get(Context context) {
    synchronized (Permission.class) {
      if (singleton == null) {
        synchronized (Permission.class) {
          singleton = new Permission(context);
        }
      }
    }

    return singleton;
  }

  void requestPermissions(Callback callback) {
    this.callback = callback;
    startPermissionActivity();
  }

  interface Callback {
    void requestResult(boolean granted);
  }

  void invokeCallback(boolean granted) {
    if (callback != null) {
      callback.requestResult(granted);
    }
  }

  void startPermissionActivity() {
    Intent intent = new Intent(context, ShadowActivity.class);
    intent.putExtra(Utils.EXTRA_PERMISSIONS, PERMISSIONS);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }
}
