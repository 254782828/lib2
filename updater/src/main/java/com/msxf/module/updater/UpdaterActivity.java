package com.msxf.module.updater;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.msxf.downloader.DownloadCallback;
import java.lang.reflect.Constructor;

import static com.msxf.module.updater.Utils.EXTRA_FORCE_CHECK;
import static com.msxf.module.updater.Utils.killApp;

/**
 * This class represents an {@link Activity} to pop updater dialog.
 *
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
public final class UpdaterActivity extends AppCompatActivity {
  private ProgressDialog progressDialog;
  private Shadow shadow = Shadow.get();
  private UpdateView updateView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState == null) {
      handleIntent(getIntent());
    }
  }

  @Override protected void onDestroy() {
    hideCheckDialog();
    dismissView(updateView);
    super.onDestroy();
  }

  @Override protected void onNewIntent(Intent intent) {
    handleIntent(intent);
  }

  private void handleIntent(Intent intent) {
    boolean forceCheck = intent.getBooleanExtra(EXTRA_FORCE_CHECK, false);
    if (forceCheck) {
      showCheckDialog();
    } else {
      hideCheckDialog();
      if (shadow.version != null) {
        initUpdateView();
      } else {
        finish();
      }
    }
  }

  private void showCheckDialog() {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(this);
      progressDialog.setCanceledOnTouchOutside(false);
      progressDialog.setCancelable(false);
      progressDialog.setMessage(getString(R.string.force_checking));
    }

    progressDialog.show();
  }

  private void hideCheckDialog() {
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
  }

  private void initUpdateView() {
    updateView = updateView(this);
    shadow.delegateDownloadCallback(new Callback(updateView, shadow.version, shadow.force));

    final Context context = this;
    updateView.foreceUpdate(shadow.force);
    updateView.setCancelable(!shadow.force);
    updateView.showView();
    updateView.showMessage(shadow.version.updateDescription);
    updateView.setOnDissmissListener(new DialogInterface.OnDismissListener() {
      @Override public void onDismiss(DialogInterface dialog) {
        if (!isFinishing()) {
          finish();
        }
      }
    });
    updateView.setOnPositiveButton(new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Utils.checkPermissions(context)) {
          Permission.get(context).requestPermissions(new Permission.Callback() {
            @Override public void requestResult(boolean granted) {
              if (granted) {
                UpdaterService.download(context);
              } else {
                Toast.makeText(context, "未获取到存储卡读写权限，无法下载", Toast.LENGTH_SHORT).show();
                updateView.dismissView();
                if (shadow.force) {
                  /* kill kill kill! */
                  killApp(UpdaterActivity.this);
                } else {
                  finish();
                }
              }
            }
          });
        } else {
          UpdaterService.download(context);
        }
      }
    });
    updateView.setOnNegativeButton(new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        updateView.dismissView();
        if (shadow.force) {
          /* kill kill kill! */
          killApp(UpdaterActivity.this);
        } else {
          finish();
        }
      }
    });
  }

  /**
   * Create a default {@link UpdateView}
   *
   * @param context activity context to use
   * @return {@link UpdateView}
   */
  private static UpdateView defaultView(Context context) {
    return new UpdateDialog(context);
  }

  private static UpdateView updateView(Context context) {
    String clazzName = Shadow.get().updateViewClazzName;
    if (clazzName == null) {
      return defaultView(context);
    }

    try {
      Class<?> customUpdateViewClazz = Class.forName(clazzName);
      Constructor constructor = customUpdateViewClazz.getConstructor(Context.class);
      Object o = constructor.newInstance(context);
      return o instanceof Dialog ? (UpdateView) o : defaultView(context);
    } catch (Exception e) {
      return defaultView(context);
    }
  }

  private class Callback extends DownloadCallback {
    final UpdateView updateView;
    final Version version;
    final boolean force;

    public Callback(UpdateView updateView, Version version, boolean force) {
      this.updateView = updateView;
      this.version = version;
      this.force = force;
    }

    @Override public void onProgress(int downloadId, long bytesWritten, long totalBytes) {
      int progress = (int) (bytesWritten * 100 / totalBytes);
      updateView.updateProgress(progress);
    }

    @Override public void onSuccess(int downloadId, String filePath) {
      String hash = Utils.fileHash(filePath);
      if (!hash.equalsIgnoreCase(version.md5)) {
        updateView.showFailure("文件校验失败, 请重新下载");
        return;
      }

      dismissView(updateView);

      if (force) {
        killApp(UpdaterActivity.this);
      } else {
        finish();
      }
    }

    @Override public void onFailure(int downloadId, int statusCode, String errMsg) {
      if (updateView != null && updateView.showing()) {
        updateView.showFailure("网络异常, 请稍后重试");
      }
    }
  }

  public void dismissView(UpdateView updateView) {
    if (updateView != null && updateView.showing()) {
      updateView.dismissView();
    }
  }
}
