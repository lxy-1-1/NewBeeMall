package com.example.newbeemall.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.newbeemall.R;

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

    // API 基础地址（包含 /mallapi 上下文路径）
    public static final String DEFAULT_INTERNAL_BASE_URL = "http://172.21.3.8:28019/mallapi";
    public static final String DEFAULT_EXTERNAL_BASE_URL = "http://47.99.134.126:28019/mallapi";
    public static final String BASE_URL = DEFAULT_INTERNAL_BASE_URL;

    private static final String PREF_NAME = "config";
    private static final String KEY_API_BASE_URL = "api_base_url";
    private static final String KEY_SIGNATURE = "signature";

    // 图片基础地址候选列表（按优先级排序，自动检测哪个可用）
    private static final String[] IMAGE_SERVER_CANDIDATES = {
            "http://115.158.64.84:28019",   // 实训文档：校内
            "http://172.21.3.8:28019/mallapi", // API服务器（含/mallapi上下文路径，静态资源也在其下）
            "http://172.21.3.8:28019",      // API服务器根路径
            "http://47.99.134.126:28019",   // 实训文档：校外
    };

    // 当前生效的图片服务器地址（检测完成后更新）
    private static volatile String effectiveImageBaseUrl = IMAGE_SERVER_CANDIDATES[0];
    // 检测是否已完成
    private static volatile boolean imageServerDetected = false;

    /**
     * 自动检测可用的图片服务器
     * 必须在后台线程调用（已在 HomeFragment.loadHomeData 的线程中使用）
     * 依次尝试每个候选服务器，找到第一个能连通的即使用
     */
    public static void detectImageServer() {
        if (imageServerDetected) return;
        Log.d(TAG, "===== 开始检测图片服务器 =====");
        for (String candidate : IMAGE_SERVER_CANDIDATES) {
            try {
                // 尝试 HEAD 请求检测连通性
                URL url = new URL(candidate + "/goods-img/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                int code = conn.getResponseCode();
                conn.disconnect();
                Log.d(TAG, "图片服务器测试: " + candidate + " -> HTTP " + code);
                // 200(目录列表) 或 404(目录不可列但服务器可达) 或 403 都算服务器可达
                if (code < 500) {
                    effectiveImageBaseUrl = candidate;
                    imageServerDetected = true;
                    Log.d(TAG, "===== 图片服务器已确定: " + candidate + " =====");
                    return;
                }
            } catch (Exception e) {
                Log.w(TAG, "图片服务器不可达: " + candidate + " (" + e.getMessage() + ")");
            }
        }
        // 全部不可达，保持默认
        imageServerDetected = true;
        Log.e(TAG, "===== 所有图片服务器均不可达，使用默认: " + effectiveImageBaseUrl + " =====");
    }

    /**
     * 构建完整的图片URL
     * 处理相对路径和绝对路径，确保URL格式正确
     * @param path 图片路径（可能是相对路径如 /goods-img/xxx.png 或完整URL）
     * @return 完整的图片URL
     */
    public static String buildImageUrl(String path) {
        if (path == null || path.isEmpty()) return "";
        // 已经是完整URL，直接返回
        if (path.startsWith("http://") || path.startsWith("https://")) {
            Log.d(TAG, "ImageUrl(full): " + path);
            return path;
        }
        // 拼接当前生效的图片基础地址
        String base = effectiveImageBaseUrl;
        String url = base + (path.startsWith("/") ? path : "/" + path);
        Log.d(TAG, "ImageUrl(built): " + url);
        return url;
    }

    public static String getBaseUrl(Context context) {
        if (context == null) return BASE_URL;
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_API_BASE_URL, BASE_URL);
    }

    public static void saveBaseUrl(Context context, String baseUrl) {
        if (context == null) return;
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_API_BASE_URL, baseUrl == null || baseUrl.isEmpty() ? BASE_URL : baseUrl).apply();
    }

    public static String getBaseUrlLabel(Context context) {
        String baseUrl = getBaseUrl(context);
        if (DEFAULT_EXTERNAL_BASE_URL.equals(baseUrl)) {
            return "校外服务器";
        }
        return "校内服务器";
    }

    public static void saveSignature(Context context, String signature) {
        if (context == null) return;
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_SIGNATURE, signature == null ? "" : signature.trim()).apply();
    }

    public static String getSignature(Context context) {
        if (context == null) return "随新所欲，蜂富多彩!!!";
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_SIGNATURE, "随新所欲，蜂富多彩!!!");
    }

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

    // ========== 图片加载（Glide + 自动URL构建 + 错误日志） ==========
    public static void loadImage(Context context, String imagePath, ImageView imageView) {
        String url = buildImageUrl(imagePath);
        if (url.isEmpty()) {
            Log.w(TAG, "loadImage: empty path, skipping");
            return;
        }
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .fitCenter()
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e,
                                               Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                               boolean isFirstResource) {
                        Log.e(TAG, "Glide load FAILED: " + url, e);
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource,
                                                   Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target,
                                                   com.bumptech.glide.load.DataSource dataSource,
                                                   boolean isFirstResource) {
                        return false;
                    }
                })
                .into(imageView);
    }

    private static String request(String method, String path, String jsonBody, Context context) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(path.startsWith("http") ? path : getBaseUrl(context) + path);
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
            // 返回错误响应体，让调用方可以解析服务器返回的具体错误信息
            InputStream errorStream = conn.getErrorStream();
            if (errorStream != null) {
                String errorBody = readStream(errorStream);
                if (!errorBody.isEmpty()) {
                    Log.e(TAG, method + " error, code=" + code + ", body=" + errorBody);
                    return errorBody;
                }
            }
            Log.e(TAG, method + " error, code=" + code);
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
        sp.edit().remove("token").remove("phone").apply();
    }

    // ========== Phone 管理（登录时保存手机号，用于显示登录名） ==========
    public static void savePhone(Context context, String phone) {
        SharedPreferences sp = context.getSharedPreferences("info", Context.MODE_PRIVATE);
        sp.edit().putString("phone", phone).apply();
    }

    public static String getPhone(Context context) {
        SharedPreferences sp = context.getSharedPreferences("info", Context.MODE_PRIVATE);
        return sp.getString("phone", "");
    }

    /**
     * 检查 API 返回结果是否表示 token 失效
     * 新蜂商城 token 失效时 resultCode 通常为 416 或返回特定 message
     * 如果检测到 token 失效，自动清除 token 并返回 true
     */
    public static boolean isTokenExpired(String result, Context context) {
        if (result == null) return false;
        try {
            org.json.JSONObject root = new org.json.JSONObject(result);
            int resultCode = root.optInt("resultCode");
            // 416 = token失效/未登录，401 = Unauthorized
            if (resultCode == 416 || resultCode == 401) {
                clearToken(context);
                return true;
            }
            // 有些返回 resultCode != 200 + message 包含 "token" 或 "登录"
            String message = root.optString("message", "");
            if (resultCode != 200 && (message.toLowerCase().contains("token")
                    || message.contains("登录") || message.contains("未登录"))) {
                clearToken(context);
                return true;
            }
        } catch (Exception e) {
            // JSON 解析失败，忽略
        }
        return false;
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
