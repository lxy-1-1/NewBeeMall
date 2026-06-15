# NewBeeMall

新蜂商城 Android 客户端实训项目。

## 项目说明

本项目基于 Android 原生 Java 开发，实现新蜂商城移动端主要功能页面，网络访问使用 `HttpURLConnection`，符合实训文档要求。

## 已实现功能

- 登录 / 注册
- 首页：轮播图、导航、新品上线、热门商品、最新推荐
- 分类：左侧分类、右侧子分类入口
- 商品搜索：推荐、新品、价格排序
- 商品详情：详情展示、加入购物车
- 购物车：列表、合计、删除、结算
- 地址管理：新增、编辑、默认地址
- 生成订单：选择支付方式、模拟支付
- 我的订单：按订单状态筛选
- 我的：小组信息和功能入口

## 接口配置

接口根地址位于：

```text
app/src/main/java/com/example/newbeemall/util/HttpUtil.java
```

当前支持在「账号管理」页切换校内 / 校外接口环境，默认使用校内服务器。

```java
public static final String DEFAULT_INTERNAL_BASE_URL = "http://172.21.3.8:28019/mallapi";
public static final String DEFAULT_EXTERNAL_BASE_URL = "http://47.99.134.126:28019/mallapi";
```

注意：实训文档要求同一套接口地址不要混用。

## 构建方式

使用 Android Studio 打开项目根目录：

```text
NewBeeMall
```

同步 Gradle 后运行 `app` 模块即可。

Debug APK 输出路径：

```text
app/build/outputs/apk/debug/app-debug.apk
```
