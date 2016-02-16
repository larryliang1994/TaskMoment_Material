package com.jiubai.taskmoment.net;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;

import java.util.HashMap;
import java.util.Map;

/**
 * Volley框架
 */
public class VolleyUtil {
    public static RequestQueue requestQueue = null;

    public static void initRequestQueue(Context context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
    }

    /**
     * 进行soap通信获取后缀，然后进行http请求
     *
     * @param soapKey         soap通信的键
     * @param soapValue       soap通信的值
     * @param httpKey         http请求的键
     * @param httpValue       http请求的值
     * @param successCallback 通信成功回调
     * @param errorCallback   通信失败回调
     */
    public static void requestWithSoap(final String[] soapKey, final String[] soapValue,
                                       final String[] httpKey, final String[] httpValue,
                                       final Response.Listener<String> successCallback,
                                       final Response.ErrorListener errorCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 先进行soap通信，获取url后缀
                String soapUrl = SoapUtil.getUrlBySoap("ajax", soapKey, soapValue);

                // 构建Post请求对象
                StringRequest stringRequest = new StringRequest(Request.Method.POST,
                        Urls.SERVER_URL + "/act/ajax.php?a=" + soapUrl + "&is_app=1",
                        successCallback, errorCallback) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        if (httpKey != null) {
                            Map<String, String> map = new HashMap<>();
                            for (int i = 0; i < httpKey.length; i++) {
                                map.put(httpKey[i], httpValue[i]);
                            }
                            return map;
                        } else {
                            return super.getParams();
                        }
                    }
                };

                stringRequest.setRetryPolicy(
                        new DefaultRetryPolicy(Constants.REQUEST_TIMEOUT, 1, 1.0f));

                // 加入请求队列
                requestQueue.add(stringRequest);
            }
        }).start();
    }

    /**
     * 进行带有Cookie的网络请求
     *
     * @param url             请求参数
     * @param key             请求参数的键
     * @param value           请求参数的值
     * @param successCallback 通信成功回调
     * @param errorCallback   通信失败回调
     */
    public static void requestWithCookie(final String url, final String[] key, final String[] value,
                                         Response.Listener<String> successCallback,
                                         Response.ErrorListener errorCallback) {
        // 构建Post请求对象
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Urls.SERVER_URL + "/ajax.php?a=" + url,
                successCallback, errorCallback) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Cookie", "memberCookie=" + Config.COOKIE);

                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if (key != null) {
                    Map<String, String> params = new HashMap<>();
                    for (int i = 0; i < key.length; i++) {
                        params.put(key[i], value[i]);
                    }
                    return params;
                } else {
                    return super.getParams();
                }
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(Constants.REQUEST_TIMEOUT, 1, 1.0f));

        // 加入请求队列
        requestQueue.add(stringRequest);
    }
}