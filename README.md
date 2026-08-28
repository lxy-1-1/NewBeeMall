# NewBeeMall

新蜂商城 Android 实训项目，基于 Android 原生 Java 开发。

## 项目特点

- 原生 Android + Java
- 网络请求使用 `HttpURLConnection`
- 支持登录、分类、购物车、订单、地址管理、商品搜索
- 已按实训参考图重做主要页面 UI

## 功能说明

- 首页：顶部轮播、导航入口、新品、热门、推荐
- 分类：左侧分类列表、右侧分类内容
- 搜索：商品搜索与列表展示
- 商品详情：图片展示、加入购物车、立即购买
- 购物车：勾选、数量修改、删除、结算
- 生成订单：地址选择、支付方式、支付流程
- 订单列表：按状态查看订单
- 地址管理：新增、编辑、默认地址
- 我的：个人信息、账号管理、地址管理、关于我们

## 运行说明

1. 使用 Android Studio 打开项目根目录 `NewBeeMall`
2. 等待 Gradle 同步完成
3. 运行 `app` 模块

## 接口配置

接口地址配置在：

```text
app/src/main/java/com/example/newbeemall/util/HttpUtil.java
```

当前支持在「账号管理」页面切换校内 / 校外接口环境。

## 目录说明

- `app/src/main/java`：业务代码
- `app/src/main/res/layout`：页面布局
- `app/src/main/res/drawable`：页面样式与图形资源
- `app/src/main/res/mipmap`：应用图标资源

## 备注

该项目主要用于 Android 实训展示，重点在页面还原、基础交互和接口对接。
