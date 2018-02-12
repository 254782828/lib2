package com.msxf.module.updater;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * This class represents a shadow {@link Activity} with translucent theme.
 *
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
@TargetApi(Build.VERSION_CODES.M) public final class ShadowActivity extends Activity {
  private static final int REQUEST_CODE = 0x53;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState == null) {
      handleIntent(getIntent());
    }
  }

  @Override protected void onNewIntent(Intent intent) {
    handleIntent(intent);
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    boolean granted = true;
    for (int grantedResult : grantResults) {
      if (grantedResult != PackageManager.PERMISSION_GRANTED) {
        granted = false;
        break;
      }
    }

    Permission.get(this).invokeCallback(granted);
    finish();
  }

  private void handleIntent(Intent intent) {
    String[] permissions = intent.getStringArrayExtra(Utils.EXTRA_PERMISSIONS);
    requestPermissions(permissions, REQUEST_CODE);
  }
}
