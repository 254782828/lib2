package com.msxf.module.updater;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Data source that represents a repository for getting {@link UpdateData} from network.
 *
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
public final class UpdateDataSource implements UpdateRepository {
  private final OkHttpClient okHttpClient;
  private final HttpUrl httpUrl;

  public UpdateDataSource(OkHttpClient okHttpClient, String baseUrl) {
    this.okHttpClient = okHttpClient;
    this.httpUrl = HttpUrl.parse(baseUrl + "update/updateVersion");
  }

  /**
   * Check version on server to see if need update.
   *
   * @param packageName package name
   * @param versionCode version code
   * @param channel channel to update
   * @return {@link Call}
   */
  @Override public Call checkVersion(String packageName, String channel, int versionCode) {
    HttpUrl url = httpUrl.newBuilder()
        .addQueryParameter("packageName", packageName)
        .addQueryParameter("channel", channel)
        .addQueryParameter("versionCode", Integer.toString(versionCode))
        .build();
    Request request = new Request.Builder().url(url).build();
    return okHttpClient.newCall(request);
  }
}
