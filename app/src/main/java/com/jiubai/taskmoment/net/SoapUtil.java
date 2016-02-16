package com.jiubai.taskmoment.net;

import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Urls;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * Soap通信相关操作
 */
public class SoapUtil {
    /**
     * 获取连接地址的后缀乱码
     *
     * @param method soap通信的方法名
     * @param key    soap通信所需的键
     * @param value  soap通信所需的值
     * @return 后缀乱码
     */
    public static String getUrlBySoap(String method, String[] key, String[] value) {
        SoapObject request = new SoapObject(Urls.SOAP_TARGET, "soap_server");

        request.addProperty("param", method);

        for (int i = 0; i < key.length; i++) {
            request.addProperty(key[i], value[i]);
        }

        final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        final HttpTransportSE transport = new HttpTransportSE(Urls.SOAP_TARGET);
        transport.debug = true;

        try {
            transport.call(Urls.SOAP_TARGET + "&soap_server=1", envelope);

            if (envelope.getResponse() != null) {
                return envelope.getResponse().toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * 延长cookie寿命至一年
     *
     * @param cookie 默认的只有10分钟寿命的cookie
     */
    public static void extendCookieLifeTime(String cookie) {
        Config.COOKIE = cookie;

        String[] decodeKey = {"string", "operation"};
        String[] decodeValue = {cookie, "DECODE"};
        String urlString = getUrlBySoap("authcode", decodeKey, decodeValue);

        try {
            JSONObject jsonObject = new JSONObject(urlString);
            Config.MID = jsonObject.getString("id");
            Config.NICKNAME = jsonObject.getString("real_name");
            Config.PORTRAIT = Urls.MEDIA_CENTER_PORTRAIT + Config.MID + ".jpg";
            Config.MOBILE = jsonObject.getString("mobile");

            if ("".equals(Config.NICKNAME) || "null".equals(Config.NICKNAME)) {
                Config.NICKNAME = "昵称";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        String[] encodeKey = {"string", "operation", "p1", "p2"};
//        String[] encodeValue = {urlString, "ENCODE", "jbw", "40000000"};
//        Config.COOKIE = getUrlBySoap("authcode", encodeKey, encodeValue);
    }
}