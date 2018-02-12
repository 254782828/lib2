package com.msxf.module.updater;

import com.msxf.downloader.DownloadCallback;
import com.msxf.downloader.DownloadManager;

/**
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
final class Shadow {
  private static Shadow singleton;
  UpdateRepository repository;
  Version version;
  String channel;
  boolean force;
  int notifyIconResId;
  boolean isUpdating;
  DownloadManager downloadManager;
  DownloadCallback downloadCallback;
  String updateViewClazzName;

  /**
   * Get a single instance for {@link Shadow}.
   *
   * @return {@link Shadow}
   */
  static Shadow get() {
    synchronized (Shadow.class) {
      if (singleton == null) {
        synchronized (Shadow.class) {
          singleton = new Shadow();
        }
      }
    }

    return singleton;
  }

  void delegateDownloadCallback(DownloadCallback callback) {
    downloadCallback = callback;
  }
}
