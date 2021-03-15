package com.tencent.wmpf.demo.wxfacepay;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 网络请求工具类，目前主要处理获取authInfo和实现付款逻辑
 * <p>
 * created by javayhu on 2020/11/26.
 */
public class OkhttpUtils {

    public static final String TAG = "OkhttpUtils";
    public static final String URL_PAY = "https://miniprog.pay.weixin.qq.com/xphp/cfacepaydemo/getWxpayfaceCode";

    private static OkhttpUtils singleInstance = null;

    private OkHttpClient mClient;
    private Request mRequest;
    private Handler mHandler;
    private SSLSocketFactory sslSocketFactory = null;

    private OkhttpUtils() {
        mClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(30, TimeUnit.SECONDS)
                .sslSocketFactory(getSslSocketFactory())
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .build();
        mHandler = new Handler(Looper.getMainLooper());
    }

    private SSLSocketFactory getSslSocketFactory() {
        try {
            //   Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };
            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            sslSocketFactory = sslContext.getSocketFactory();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sslSocketFactory;
    }

    public void request(HashMap<String, Object> map, String url, final CallBack callBack) {
        FormBody.Builder builder = new FormBody.Builder();
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                builder.add(entry.getKey(), (String) entry.getValue());
                Log.d(TAG, "key:" + entry.getKey() + ", value:" + entry.getValue());
            }
        }
        RequestBody body = builder.build();
        mRequest = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        mClient.newCall(mRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "[onFailure] error:", e);
                OnHandleFailed(e, callBack);
            }

            @Override
            public void onResponse(Call call, Response response) {
                Log.d(TAG, "[onResponse] response:" + response.toString());
                OnHandleSuccess(response, callBack);
            }
        });
    }

    public interface CallBack {
        void onSuccess(Response response);

        void onFailed(Exception e);
    }

    private void OnHandleSuccess(final Response response, final CallBack callBack) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callBack.onSuccess(response);
            }
        });
    }

    private void OnHandleFailed(final Exception e, final CallBack callBack) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callBack.onFailed(e);
            }
        });
    }

    public static OkhttpUtils getInstance() {
        if (singleInstance == null) {
            synchronized (OkhttpUtils.class) {
                if (singleInstance == null) {
                    singleInstance = new OkhttpUtils();
                }
            }
        }
        return singleInstance;
    }
}
