package com.msxf.module.updater;

import java.text.DecimalFormat;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLog;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

import static org.junit.Assert.assertEquals;

/**
 * @author Vincent Cheung (coolingfall@gmail.com)
 */
@RunWith(RobolectricGradleTestRunner.class) @Config(constants = BuildConfig.class, sdk = 21)
public class TestUpdateApi {
  private MockWebServer mockWebServer;
  private UpdateDataSource dataSource;

  @Before public void setUp() throws Exception {
    ShadowLog.stream = System.out;
    mockWebServer = new MockWebServer();

    OkHttpClient client = new OkHttpClient.Builder().addInterceptor(
        new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build();
    Retrofit retrofit = new Retrofit.Builder().baseUrl(mockWebServer.url("/").toString())
        .client(client)
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create())
        .build();
    dataSource = new UpdateDataSource(retrofit);
  }

  @After public void close() throws Exception {
    mockWebServer.shutdown();
  }

  @Test public void testCheckVersion() throws Exception {
    String body = "{\n"
        + "    \"data\": {\n"
        + "        \"id\": \"25\", \n"
        + "        \"name\": \"马上金融\", \n"
        + "        \"versionName\": \"1.4.3\", \n"
        + "        \"versionCode\": 14301, \n"
        + "        \"size\": \"4794687\", \n"
        + "        \"packageName\": \"com.msxf.loan\", \n"
        + "        \"forceUpdate\": 0, \n"
        + "        \"downloadUrl\": \"http://static.msxf.lodev/static/cms/update/2016-04-21/msjr_v1.4.3_production.apk\", \n"
        + "        \"updateDescription\": \"版本迭代\", \n"
        + "        \"affectedVersionCode\": \"all\"\n"
        + "    }, \n"
        + "    \"code\": 0, \n"
        + "    \"msg\": \"成功\"\n"
        + "}";
    MockResponse response = new MockResponse().setBody(body);
    mockWebServer.enqueue(response);
    //String packageName = ShadowApplication.get().getPackageName();
    String packageName = "com.msxf.loan";
    int code = Utils.getVersionCode(ShadowApplication.getInstance().getApplicationContext());
    Version version =
        dataSource.checkVersion(packageName, "wandoujia", code).toBlocking().single().data;
    assertEquals(0, version.forceUpdate);
  }

  @Test public void testFormat() {
    DecimalFormat format = new DecimalFormat("#%");
    String result = format.format(50 / 100f);
    assertEquals("50%", result);
  }
}
