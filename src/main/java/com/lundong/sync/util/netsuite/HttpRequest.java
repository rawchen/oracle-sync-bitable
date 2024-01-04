package com.lundong.sync.util.netsuite;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    //获取请求头部
    public static String requestURL(AuthInfo authInfo, String params, String requestMethod, Map<String, String> map) throws Exception {
		/*TokenAuth ta = new TokenAuth();
		String auth=ta.generateAuthorization(authInfo,requestMethod, params);*/
        //获取NetSuite签名, 2021.1版本使用HMAC-SHA256加密签名
        String authorization = constructAuthHeader(authInfo, requestMethod);
        System.out.println("Authorization: " + authorization);
        //设置返回类型与签名等Header
        if (map == null) map = new HashMap<String, String>();
        map.put("content-type", "application/json");
        map.put("Authorization", authorization);
        return requestNetSuite(authInfo.getUrl(), params, requestMethod, map);
    }

    //发送请求
    private static String requestNetSuite(String url, String params, String requestMethod, Map<String, String> map) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader in = null;
        String xml = "";
        //越过Https证书
        //SslUtils.ignoreSsl();
        URL realUrl = new URL(url);
        //添加url参数:token and client_id
        conn = (HttpURLConnection) realUrl.openConnection();
        conn.setConnectTimeout(10000);//5000
        conn.setReadTimeout(30000);//8000
        // 设置是否向httpUrlConnection输出，post请求，参数要放在
        // http正文内，因此需要设为true, 默认情况下是false;
        conn.setDoOutput(true);
        // Post 请求不能使用缓存
        conn.setUseCaches(false);
        // 设定传送的内容类型是可序列化的java对象
        // (如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
        if (map != null) {
            for (String key : map.keySet()) {
                conn.setRequestProperty(key, map.get(key));
            }
        }
        //conn.setRequestProperty("Content-Length", ""+(params!=null?params.getBytes().length:0));
        // 设定请求的方法为"POST"，默认是GET
        conn.setRequestMethod(requestMethod);
        // 设置是否从httpUrlConnection读入，默认情况下是true;
        conn.setDoInput(true);
        conn.setDoOutput(true);

        if (params != null && !params.equals("")) {
            byte[] body = params.getBytes("UTF-8");
            conn.getOutputStream().write(body);
        }
        // 连接，从上述第2条中url.openConnection()至此的配置必须要在connect之前完成，
        conn.connect();
        int retCode = conn.getResponseCode();
        if (retCode >= 400) {
            in = new BufferedReader(new InputStreamReader(
                    conn.getErrorStream(), "UTF-8"));
        } else {
            in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "UTF-8"));
        }
        // 定义BufferedReader输入流来读取URL的响应  
        String line = "";
        while ((line = in.readLine()) != null) {
            xml += line;
        }
        return xml;
    }

    /**
     * 生成签名并组合Authorization头
     * 如"/", 编码后为: %2F
     *
     * @param auth 传入数据
     * @param method 方法
     * @return 编码后的字符串
     */
    private static String constructAuthHeader(AuthInfo auth, String method) throws Exception {
        //基础信息
        String url = auth.getUrl();
        //时间戳
        long timestamp = new Date().getTime() / 1000;
        //转成大写
        method = method.toUpperCase();
        //签名方法
        String signature_method = "HMAC-SHA256", signature_method_code = "HmacSHA256";
        //基础信息
        String access_token = auth.getToken_id(), token_secret = auth.getToken_secret(),
                consumer_key = auth.getConsumer_key(), consumer_secret = auth.getConsumer_secret(),
                realm = auth.getRealm(), nonce = generateNonce();
        //组合Key
        String key = consumer_secret + '&' + token_secret;
        //截取问号前的URL
        String encodeURL = url.split("\\?")[0];

        //获取URL参数
        Map<String, String> parameters = urlSplit(url);
        parameters.put("oauth_consumer_key", consumer_key);
        parameters.put("oauth_nonce", nonce);
        parameters.put("oauth_signature_method", signature_method);
        System.out.println(timestamp);
        System.out.println(nonce);
        parameters.put("oauth_timestamp", String.valueOf(timestamp));
        parameters.put("oauth_token", access_token);
        parameters.put("oauth_version", "1.0");
        String parameterString = sortAndConcat(parameters);
        //组合待签字符串
        StringBuilder signatureBaseString = new StringBuilder(100);
        signatureBaseString.append(method.toUpperCase());
        signatureBaseString.append('&');
        signatureBaseString.append(urlEncode(encodeURL));
        signatureBaseString.append('&');
        signatureBaseString.append(urlEncode(parameterString));
        //转换成String类型
        String signatureString = signatureBaseString.toString();
        System.out.println(signatureString);
        //生成签名
        byte[] bytesToSign = signatureString.getBytes("UTF-8");
        byte[] keyBytes = key.getBytes("UTF-8");
        SecretKeySpec signingKey = new SecretKeySpec(keyBytes, signature_method_code);
        Mac mac = Mac.getInstance(signature_method_code);
        mac.init(signingKey);
        byte[] signedBytes = mac.doFinal(bytesToSign);
        System.out.println(new String(Base64.encodeBase64(signedBytes, false)));
        String signature = urlEncode(new String(Base64.encodeBase64(signedBytes, false)));
        System.out.println(signature);

        return new StringBuilder().append("OAuth ")
                .append("realm").append("=\"").append(realm)
                .append("\",").append("oauth_consumer_key").append("=\"").append(consumer_key)
                .append("\",").append("oauth_token").append("=\"").append(access_token)
                .append("\",").append("oauth_signature_method").append("=\"").append(signature_method)
                .append("\",").append("oauth_timestamp").append("=\"").append(timestamp)
                .append("\",").append("oauth_nonce").append("=\"").append(nonce)
                .append("\",").append("oauth_version").append("=\"").append("1.0")
                .append("\",").append("oauth_signature").append("=\"").append(signature)
                .append("\"").toString();
    }

    /**
     * 对字符串进行URL编码
     * 如"/", 编码后为: %2F
     *
     * @param str 传入字符串
     * @return 编码后的字符串
     */
    private static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8")
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 生成随机数
     *
     * @return 随机数
     */
    private static String generateNonce() {
        String nonce = "";
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 32; i++) {
            nonce += possible.charAt((int) (Math.floor(Math.random() * possible.length())));
        }
        return nonce;
    }

    /**
     * 对URL参数进行排序
     * 如"script:248,deploy:1", 排序后deploy=1&script=248
     *
     * @param parameters 参数的键值对
     * @return 排序后的字符串
     */
    private static String sortAndConcat(Map<String, String> parameters) {
        StringBuilder encodedParams = new StringBuilder(100);
        Object[] arr = parameters.keySet().toArray();
        Arrays.sort(arr);
        for (Object key : arr) {
            if (encodedParams.length() > 0) {
                encodedParams.append('&');
            }
            encodedParams.append(key).append('=').append(parameters.get(key));

        }
        return encodedParams.toString();
    }

    /**
     * 去掉url中的路径，留下请求参数部分
     *
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String TruncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;
        strURL = strURL.trim().toLowerCase();
        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                for (int i = 1; i < arrSplit.length; i++) {
                    strAllParam = arrSplit[i];
                }
            }
        }
        return strAllParam;
    }

    /**
     * 解析出url参数中的键值对
     * 如 "restlet.nl?script=248&deploy=1"，解析出script:24,deploy:1存入map中
     *
     * @param URL url地址
     * @return url请求参数部分
     */
    public static Map<String, String> urlSplit(String URL) {
        Map<String, String> mapRequest = new HashMap<String, String>();
        String[] arrSplit = null;
        String strUrlParam = TruncateUrlPage(URL);
        if (strUrlParam == null) {
            return mapRequest;
        }
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");
            //解析出键值
            if (arrSplitEqual.length > 1) {
                //正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            } else {
                if (arrSplitEqual[0] != "") {
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }
}
