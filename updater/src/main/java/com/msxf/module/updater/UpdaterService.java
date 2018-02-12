package com.msxf.module.updater;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import com.msxf.downloader.DownloadCallback;
import com.msxf.downloader.DownloadRequest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.json.JSONObject;

import static com.msxf.module.updater.Utils.EXTRA_COMMAND;
import static com.msxf.module.updater.Utils.EXTRA_FORCE_CHECK;
import static com.msxf.module.updater.Utils.EXTRA_SILENT;
import static com.msxf.module.updater.Utils.FORCE_UPDATE;

/**
 * This class represents a updater {@link Service}.
 *
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
public final class UpdaterService extends Service {
  public static final int COMMAND_CHECK = 0x01;
  public static final int COMMAND_DOWNLOAD = 0x02;
  private Notifier notifier;
  private Shadow shadow = Shadow.get();

  /**
   * Check update for app.
   *
   * @param context context to use
   * @param silent silent or not
   */
  static void check(Context context, boolean silent) {
    if (Shadow.get().isUpdating && !silent) {
      Toast.makeText(context, "版本更新中", Toast.LENGTH_SHORT).show();
      return;
    }

    Intent intent = new Intent(context.getPackageName() + ".UPDATER_SERVICE");
    intent.setPackage(context.getPackageName());
    intent.putExtra(EXTRA_COMMAND, COMMAND_CHECK);
    intent.putExtra(EXTRA_SILENT, silent);
    context.startService(intent);
  }

  /**
   * Download apk from network.
   *
   * @param context context to use
   */
  static void download(Context context) {
    Intent intent = new Intent(context.getPackageName() + ".UPDATER_SERVICE");
    intent.setPackage(context.getPackageName());
    intent.putExtra(EXTRA_COMMAND, COMMAND_DOWNLOAD);
    context.startService(intent);
  }

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      int command = intent.getIntExtra(EXTRA_COMMAND, 0);
      switch (command) {
        case COMMAND_CHECK:
          checkUpdate(intent.getBooleanExtra(EXTRA_SILENT, false));
          break;

        case COMMAND_DOWNLOAD:
          download(shadow.version);
          break;
      }
    }

    return super.onStartCommand(intent, flags, startId);
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }

  public void checkUpdate(final boolean silent) {
    shadow.repository.checkVersion(getPackageName(), shadow.channel, Utils.getVersionCode(this))
        .enqueue(new Callback() {
          @Override public void onFailure(Call call, IOException e) {
            shadow.version = null;
            showFailureMessage(silent);
          }

          @Override public void onResponse(Call call, Response response) throws IOException {
            try {
              final UpdateData updateData = parseBody(response.body().string());
              Version version = updateData.data;
              if (version == null) {
                if (!silent) {
                  runOnUiThread(new Runnable() {
                    @Override public void run() {
                      Intent intent = new Intent(UpdaterService.this, UpdaterActivity.class);
                      intent.putExtra(EXTRA_FORCE_CHECK, false);
                      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                      startActivity(intent);
                      Toast.makeText(UpdaterService.this, updateData.message, Toast.LENGTH_SHORT)
                          .show();
                    }
                  });
                }
                return;
              }

              shadow.version = version;
              shadow.force = version.forceUpdate == FORCE_UPDATE;
              showUpdate();
            } catch (Exception e) {
              shadow.version = null;
              showFailureMessage(silent);
            }
          }
        });
  }

  private void showFailureMessage(boolean silent) {
    if (silent) {
      return;
    }

    runOnUiThread(new Runnable() {
      @Override public void run() {
        Toast.makeText(UpdaterService.this, "更新失败", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void runOnUiThread(Runnable task) {
    new Handler(Looper.getMainLooper()).post(task);
  }

  private UpdateData parseBody(String body) throws Exception {
    JSONObject root = new JSONObject(body);
    JSONObject version = root.optJSONObject("data");

    UpdateData.Builder dataBuilder = UpdateData.builder();

    if (version != null) {
      Version.Builder versionBuilder = Version.builder();
      versionBuilder.name(version.optString("name"))
          .versionName(version.optString("versionName"))
          .size(version.optLong("size"))
          .forceUpdate(version.getInt("forceUpdate"))
          .downloadUrl(version.getString("downloadUrl"))
          .updateDescription(version.optString("updateDescription"))
          .md5(version.getString("md5"));
      dataBuilder.data(versionBuilder.build());
    }

    return dataBuilder.code(root.getString("code")).message(root.getString("message")).build();
  }

  private void showUpdate() {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        if (Utils.isForeground(UpdaterService.this)) {
          Intent intent = new Intent(UpdaterService.this, UpdaterActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(intent);
        }
      }
    });
  }

  /**
   * Start to download after clicking positive button.
   */
  void download(final Version version) {
    if (version == null) {
      return;
    }

    NotificationManager manager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationCompat.Builder noficationBuilder =
        new NotificationCompat.Builder(this).setTicker(String.format("正在下载%s", version.name))
            .setSmallIcon(shadow.notifyIconResId)
            .setContentTitle(version.name);
    notifier = new Notifier(manager, noficationBuilder);

    String filePath = Utils.getDestinationPath(version.downloadUrl);
    DownloadRequest request = new DownloadRequest.Builder().url(version.downloadUrl)
        .destinationFilePath(filePath)
        .progressInterval(350, TimeUnit.MILLISECONDS)
        .downloadCallback(new CallBack(version))
        .build();
    shadow.downloadManager.add(request);
    shadow.isUpdating = true;
  }

  private final class CallBack extends DownloadCallback {
    Version version;

    public CallBack(Version version) {
      this.version = version;
    }

    @Override public void onProgress(int downloadId, long bytesWritten, long totalBytes) {
      if (totalBytes == 0) {
        return;
      }

      int progress = (int) (bytesWritten * 100 / totalBytes);

      if (!shadow.force) {
        notifier.notifyProgress(progress);
      }

      shadow.downloadCallback.onProgress(downloadId, bytesWritten, totalBytes);
    }

    @Override public void onSuccess(int downloadId, String filePath) {
      shadow.isUpdating = false;

      Utils.installApk(UpdaterService.this, filePath);
      if (!shadow.force) {
        Intent intent = Utils.getInstallIntent(UpdaterService.this, filePath);
        if (intent == null) {
          return;
        }

        PendingIntent pendingIntent =
            PendingIntent.getActivity(UpdaterService.this, 2016, intent, 0);
        notifier.notifySuccess(pendingIntent);
      }

      shadow.downloadCallback.onSuccess(downloadId, filePath);
    }

    @Override public void onFailure(int downloadId, int statusCode, String errMsg) {
      shadow.isUpdating = false;

      if (!shadow.force) {
        notifier.notifyFailure();
      }

      shadow.downloadCallback.onFailure(downloadId, statusCode, errMsg);
    }
  }
}
