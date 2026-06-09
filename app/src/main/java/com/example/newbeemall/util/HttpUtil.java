package com.example.newbeemall.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 网络请求工具类 - 基于 HttpURLConnection
 * 文档要求：必须使用 HttpURLConnection，不能用第三方网络库
 */
public class HttpUtil {

    private static final String TAG = "HttpUtil";

    // ========== 基础地址（根据网络环境切换） ==========
    // 实训服务器：Swagger 在 /mallapi/swagger-ui/index.html，API 也需要 /mallapi 前缀。
    // 备用地址可改为：http://172.30.130.131:28019/mallapi
    public static final String BASE_URL = "http://172.21.3.8:28019/mallapi";

    // ========== GET 请求 ==========
    public static String get(String path, Context context) {
        return request("GET", path, null, context);
    }

    // ========== POST 请求（JSON body） ==========
    public static String post(String path, String jsonBody, Context context) {
        return request("POST", path, jsonBody, context);
    }

    public static String put(String path, String jsonBody, Context context) {
        return request("PUT", path, jsonBody, context);
    }

    public static String delete(String path, Context context) {
        return request("DELETE", path, null, context);
    }

    private static String request(String method, String path, String jsonBody, Context context) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(path.startsWith("http") ? path : BASE_URL + path);
            Log.d(TAG, method + ": " + url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            String token = getToken(context);
            if (token != null && !token.isEmpty()) {
                conn.setRequestProperty("token", token);
            }
            if (jsonBody != null && !jsonBody.isEmpty()) {
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();
            }
            int code = conn.getResponseCode();
            if (code >= 200 && code < 300) {
                return readStream(conn.getInputStream());
            }
            InputStream errorStream = conn.getErrorStream();
            String error = errorStream == null ? "" : readStream(errorStream);
            Log.e(TAG, method + " error, code=" + code + ", body=" + error);
            return null;
        } catch (Exception e) {
            Log.e(TAG, method + " exception", e);
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // ========== Token 管理 ==========
    public static void saveToken(Context context, String token) {
        SharedPreferences sp = context.getSharedPreferences("info", Context.MODE_PRIVATE);
        sp.edit().putString("token", token).apply();
    }

    public static String getToken(Context context) {
        SharedPreferences sp = context.getSharedPreferences("info", Context.MODE_PRIVATE);
        return sp.getString("token", "");
    }

    public static boolean hasToken(Context context) {
        return !getToken(context).isEmpty();
    }

    public static void clearToken(Context context) {
        SharedPreferences sp = context.getSharedPreferences("info", Context.MODE_PRIVATE);
        sp.edit().remove("token").apply();
    }

    // ========== 读取流 ==========
    private static String readStream(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
}
