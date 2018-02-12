package com.msxf.module.updater;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.UiThread;
import com.msxf.downloader.DownloadManager;
import com.msxf.downloader.OkHttpDownloader;
import okhttp3.OkHttpClient;

import static com.msxf.module.updater.Preconditions.checkNotNull;
import static com.msxf.module.updater.Preconditions.checkNotZero;
import static com.msxf.module.updater.Utils.EXTRA_FORCE_CHECK;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * A manager used to manage app's update. We should create single instance and check
 * version in entry activity. This manager provides a default update view, it's easy to set custom
 * update view via implementing {@link UpdateView}.
 *
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
public final class UpdateManager {
  private Context context;

  private UpdateManager(Builder builder) {
    Shadow shadow = Shadow.get();

    context = builder.context;
    OkHttpClient client = builder.client;
    if (client == null) {
      client = defaultOkHttpClient();
    }
    shadow.downloadManager = builder.downloadManager;
    if (shadow.downloadManager == null) {
      shadow.downloadManager = defaultDownloadManager(context, client);
    }
    shadow.updateViewClazzName = builder.updateViewClazzName;
    shadow.channel = checkNotNull(builder.channel, "channel == null");
    shadow.notifyIconResId = checkNotZero(builder.notifyIconResId, "notifyIconResId == 0");

    boolean debug = builder.debug;
    String baseUrl = builder.baseUrl;
    shadow.repository = builder.repository;
    if (shadow.repository == null) {
      shadow.repository = new UpdateDataSource(client,
          baseUrl != null ? baseUrl : debug ? Utils.DEVELOPMENT_URL : Utils.PRODUCTION_URL);
    }
  }

  @UiThread public void check() {
    check(false);
  }

  @UiThread public void check(final boolean silent) {
    UpdaterService.check(context, silent);
  }

  /**
   * Force check update with {@link ProgressDialog}.
   */
  @UiThread public void forceCheck() {
    Intent intent = new Intent(context, UpdaterActivity.class);
    intent.putExtra(EXTRA_FORCE_CHECK, true);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
    UpdaterService.check(context, false);
  }

  /**
   * To check if was updating.
   *
   * @return true if was updating, otherwise return false
   */
  public boolean isUpdating() {
    return Shadow.get().isUpdating;
  }

  static OkHttpClient defaultOkHttpClient() {
    return new OkHttpClient.Builder().connectTimeout(Utils.DEFAULT_CONNECT_TIMEOUT, SECONDS)
        .readTimeout(Utils.DEFAULT_READ_TIMEOUT, SECONDS)
        .writeTimeout(Utils.DEFAULT_WRITE_TIMEOUT, SECONDS)
        .build();
  }

  /**
   * Create a default {@link DownloadManager}.
   *
   * @return {@link DownloadManager}
   */
  static DownloadManager defaultDownloadManager(Context context, OkHttpClient client) {
    return new DownloadManager.Builder().threadPoolSize(1)
        .context(context)
        .downloader(OkHttpDownloader.create(client))
        .build();
  }

  public static final class Builder {
    private DownloadManager downloadManager;
    private UpdateRepository repository;
    private OkHttpClient client;
    private Context context;
    private String channel;
    private int notifyIconResId;
    private boolean debug;
    private String updateViewClazzName;
    private String baseUrl;

    public Builder downloadManager(DownloadManager downloadManager) {
      this.downloadManager = downloadManager;
      return this;
    }

    public Builder updateRepository(UpdateRepository repository) {
      this.repository = repository;
      return this;
    }

    public <T> Builder updateViewClazz(Class<T> type) {
      this.updateViewClazzName = type.getCanonicalName();
      return this;
    }

    public Builder okHttpClient(OkHttpClient client) {
      this.client = client;
      return this;
    }

    public Builder context(Context context) {
      this.context = context;
      return this;
    }

    public Builder channel(String channel) {
      this.channel = channel;
      return this;
    }

    public Builder notifyIconResId(int notifyIconResId) {
      this.notifyIconResId = notifyIconResId;
      return this;
    }

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder debug(boolean debug) {
      this.debug = debug;
      return this;
    }

    public UpdateManager build() {
      return new UpdateManager(this);
    }
  }
}
