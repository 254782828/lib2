package com.msxf.module.updater;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.text.DecimalFormat;

/**
 * Default update dialog to show update information.
 *
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
final class UpdateDialog extends Dialog implements UpdateView, View.OnClickListener {
  private TextView tvMessage;
  private TextView tvProgress;
  private ProgressBar progressBar;
  private Button btnDownload;
  private LinearLayout layoutProgress;
  private LinearLayout layoutBtn;
  private OnClickListener onPositiveClickListener;
  private OnClickListener onNegativeClickListener;

  public UpdateDialog(Context context) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.layout_update);

    DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
    Window window = getWindow();
    if (window != null) {
      WindowManager.LayoutParams lp = window.getAttributes();
      lp.width = dm.widthPixels * 9 / 10;
      window.setAttributes(lp);
    }

    tvMessage = (TextView) findViewById(R.id.update_tv_message);
    tvProgress = (TextView) findViewById(R.id.update_tv_progress);
    progressBar = (ProgressBar) findViewById(R.id.update_progress_bar);
    btnDownload = (Button) findViewById(R.id.update_btn_download);
    Button btnCancel = (Button) findViewById(R.id.update_btn_cancel);
    layoutBtn = (LinearLayout) findViewById(R.id.update_layout_btn);
    layoutProgress = (LinearLayout) findViewById(R.id.update_layout_progress);

    btnDownload.setOnClickListener(this);
    btnCancel.setOnClickListener(this);
    layoutProgress.setVisibility(View.GONE);
  }

  @Override public void onClick(View v) {
    if (v.getId() == R.id.update_btn_download && onPositiveClickListener != null) {
      onPositiveClickListener.onClick(this, BUTTON_POSITIVE);
      tvMessage.setVisibility(View.GONE);
      layoutBtn.setVisibility(View.GONE);
      layoutProgress.setVisibility(View.VISIBLE);
      updateProgress(0);
    } else if (v.getId() == R.id.update_btn_cancel && onNegativeClickListener != null) {
      onNegativeClickListener.onClick(this, BUTTON_NEGATIVE);
    }
  }

  @Override public void showView() {
    show();
    tvMessage.setVisibility(View.VISIBLE);
    layoutProgress.setVisibility(View.GONE);
    layoutBtn.setVisibility(View.VISIBLE);
  }

  @Override public void dismissView() {
    if (isShowing()) {
      dismiss();
    }
  }

  @Override public boolean showing() {
    return super.isShowing();
  }

  @Override public void foreceUpdate(boolean force) {

  }

  @Override public void setCancelable(boolean flag) {
    super.setCancelable(flag);
    super.setCanceledOnTouchOutside(false);
  }

  @SuppressWarnings("deprecation") @Override public void showMessage(String message) {
    Spanned spanned;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      spanned = Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT);
    } else {
      spanned = Html.fromHtml(message);
    }

    tvMessage.setText(spanned);
  }

  @Override public void updateProgress(int progress) {
    progressBar.setProgress(progress);
    DecimalFormat format = new DecimalFormat("#%");
    tvProgress.setText(format.format(progress / 100f));
  }

  @Override public void showFailure(String errorMessage) {
    layoutProgress.setVisibility(View.GONE);
    layoutBtn.setVisibility(View.VISIBLE);
    tvMessage.setVisibility(View.VISIBLE);
    tvMessage.setText(errorMessage);
    btnDownload.setText("重新下载");
  }

  @Override public void setOnPositiveButton(OnClickListener l) {
    onPositiveClickListener = l;
  }

  @Override public void setOnNegativeButton(OnClickListener l) {
    onNegativeClickListener = l;
  }

  @Override public void setOnDissmissListener(OnDismissListener l) {
    super.setOnDismissListener(l);
  }
}
