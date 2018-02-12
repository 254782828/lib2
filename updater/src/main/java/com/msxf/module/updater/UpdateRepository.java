package com.msxf.module.updater;

import okhttp3.Call;
import okhttp3.Callback;

/**
 * Interface that represents a repository for retrieving update related data.
 *
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
public interface UpdateRepository {
  /**
   * Get an {@link Call} to enqueue {@link Callback}.
   *
   * @param packageName package name
   * @param channel channel of current app
   * @param versionCode version code
   * @return an {@link Call}
   */
  Call checkVersion(String packageName, String channel, int versionCode);
}
