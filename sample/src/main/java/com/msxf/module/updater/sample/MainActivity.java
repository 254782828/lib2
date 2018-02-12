package com.msxf.module.updater.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.msxf.downloader.DownloadManager;
import com.msxf.downloader.OkHttpDownloader;
import com.msxf.module.channel.ChannelUtils;
import com.msxf.module.updater.UpdateManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
  private DownloadManager downloadManager;
  private UpdateManager manager;
  private EditText updateUrlEdit;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    updateUrlEdit = (EditText) findViewById(R.id.edit_update_url);
    findViewById(R.id.btn_set_update_url).setOnClickListener(this);
    findViewById(R.id.btn_force_check).setOnClickListener(this);
    findViewById(R.id.btn_force_check).setOnClickListener(this);

    HttpLoggingInterceptor loggingInterceptor =
        new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
          @Override public void log(String message) {
            Log.d("Download", message);
          }
        }).setLevel(HttpLoggingInterceptor.Level.BODY);
    OkHttpClient client = new OkHttpClient.Builder().addInterceptor(loggingInterceptor).build();
    downloadManager = new DownloadManager.Builder().context(this)
        .downloader(OkHttpDownloader.create(client))
        .build();
    manager = new UpdateManager.Builder().context(this)
        .downloadManager(downloadManager)
        .channel(ChannelUtils.getChannel(this, "msxf"))
        .updateViewClazz(CustomUpdateDialog.class)
        .notifyIconResId(R.mipmap.ic_launcher)
        .debug(true)
        .build();
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_set_update_url:
        String updateUrl = updateUrlEdit.getText().toString();
        if (!TextUtils.isEmpty(updateUrl)) {
          // TODO UpdateManager.newBuilder
          manager = new UpdateManager.Builder().context(this)
              .downloadManager(downloadManager)
              .channel(ChannelUtils.getChannel(this, "msxf"))
              .updateViewClazz(CustomUpdateDialog.class)
              .notifyIconResId(R.mipmap.ic_launcher)
              .baseUrl(updateUrl)
              .debug(true)
              .build();
        }
        break;
      case R.id.btn_normal_check:
        manager.check();
        break;
      case R.id.btn_force_check:
        manager.forceCheck();
        break;
    }
  }
}
