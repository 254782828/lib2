package com.msxf.module.updater;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.NotificationCompat;
import java.text.DecimalFormat;

/**
 * A notifier to show download notification.
 *
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
final class Notifier {
  private static final int NOTIFICATION_ID = 20160428;
  private NotificationManager notificationManager;
  private NotificationCompat.Builder builder;

  public Notifier(NotificationManager notificationManager, NotificationCompat.Builder builder) {
    this.notificationManager = notificationManager;
    this.builder = builder;
  }

  /**
   * Notify progress in notification.
   *
   * @param progress progress
   */
  void notifyProgress(int progress) {
    DecimalFormat format = new DecimalFormat("#%");
    builder.setProgress(100, progress, false).setContentInfo(format.format(progress / 100f));
    notificationManager.notify(NOTIFICATION_ID, builder.build());
  }

  /**
   * Notify sucess information.
   *
   * @param pendingIntent install {@link PendingIntent}
   */
  void notifySuccess(PendingIntent pendingIntent) {
    builder.setAutoCancel(true);
    Notification notification = builder.build();
    notification.contentIntent = pendingIntent;
    notificationManager.notify(NOTIFICATION_ID, notification);
  }

  /**
   * Notify failure information.
   */
  void notifyFailure() {
    notificationManager.cancel(NOTIFICATION_ID);
  }
}
