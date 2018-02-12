package com.msxf.module.updater;

import android.content.DialogInterface;

/**
 * Interface representing update dialog view.
 *
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
public interface UpdateView {
  /**
   * Show update dialog.
   */
  void showView();

  /**
   * Dismiss update dialog.
   */
  void dismissView();

  /**
   * To check if current view is showing.
   *
   * @return true if showing, otherwise return false
   */
  boolean showing();

  /**
   * Set force update.
   *
   * @param force true or false
   */
  void foreceUpdate(boolean force);

  /**
   * Show update message in dialog.
   *
   * @param message message
   */
  void showMessage(String message);

  /**
   * Update progress in dialog.
   *
   * @param progress download progress
   */
  void updateProgress(int progress);

  /**
   * Show failure message.
   *
   * @param errMsg error message
   */
  void showFailure(String errMsg);

  /**
   * Sets whether this dialog is cancelable or out touch cancelable
   *
   * @param flag flag
   */
  void setCancelable(boolean flag);

  /**
   * Set {@link DialogInterface.OnClickListener} for positive button.
   *
   * @param l {@link DialogInterface.OnClickListener}
   */
  void setOnPositiveButton(DialogInterface.OnClickListener l);

  /**
   * Set {@link DialogInterface.OnClickListener} for negative button.
   *
   * @param l {@link DialogInterface.OnClickListener}
   */
  void setOnNegativeButton(DialogInterface.OnClickListener l);

  /**
   * Set {@link DialogInterface.OnDismissListener} when dialog dismissing.
   *
   * @param l {@link DialogInterface.OnDismissListener}
   */
  void setOnDissmissListener(DialogInterface.OnDismissListener l);
}
