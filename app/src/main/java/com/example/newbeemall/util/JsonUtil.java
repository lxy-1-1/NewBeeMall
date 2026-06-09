package com.example.newbeemall.util;

import com.example.newbeemall.model.Address;
import com.example.newbeemall.model.CartItem;
import com.example.newbeemall.model.CategoryItem;
import com.example.newbeemall.model.Goods;
import com.example.newbeemall.model.OrderItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    private JsonUtil() {
    }

    public static JSONObject dataObject(String json) throws Exception {
        JSONObject root = new JSONObject(json);
        return root.optJSONObject("data");
    }

    public static JSONArray dataArray(String json) throws Exception {
        JSONObject root = new JSONObject(json);
        JSONArray array = root.optJSONArray("data");
        if (array != null) return array;
        JSONObject data = root.optJSONObject("data");
        if (data != null) {
            JSONArray list = data.optJSONArray("list");
            if (list != null) return list;
            JSONArray cartItems = data.optJSONArray("cartItemVOS");
            if (cartItems != null) return cartItems;
            JSONArray shopCartItems = data.optJSONArray("shopCartItemVOS");
            if (shopCartItems != null) return shopCartItems;
            JSONArray orderItems = data.optJSONArray("orderList");
            if (orderItems != null) return orderItems;
        }
        return new JSONArray();
    }

    public static List<Goods> parseGoodsArray(JSONArray array) {
        List<Goods> goodsList = new ArrayList<>();
        if (array == null) return goodsList;
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            Goods goods = parseGoods(item);
            if (goods.getGoodsId() > 0) {
                goodsList.add(goods);
            }
        }
        return goodsList;
    }

    public static Goods parseGoods(JSONObject item) {
        Goods goods = new Goods();
        if (item == null) return goods;
        goods.setGoodsId(item.optLong("goodsId", item.optLong("goodsId")));
        goods.setGoodsName(item.optString("goodsName", item.optString("name")));
        goods.setSellingPrice(item.optDouble("sellingPrice", item.optDouble("price")));
        goods.setGoodsCoverImg(item.optString("goodsCoverImg", item.optString("goodsCarousel")));
        goods.setGoodsIntro(item.optString("goodsIntro", item.optString("goodsName")));
        return goods;
    }

    public static List<CategoryItem> parseCategories(String json) throws Exception {
        JSONArray data = dataArray(json);
        List<CategoryItem> result = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject first = data.optJSONObject(i);
            JSONArray seconds = first == null ? null : first.optJSONArray("secondLevelCategoryVOS");
            if (seconds == null || seconds.length() == 0) {
                CategoryItem item = parseCategory(first);
                if (item.getId() > 0) result.add(item);
                continue;
            }
            for (int j = 0; j < seconds.length(); j++) {
                JSONObject second = seconds.optJSONObject(j);
                CategoryItem parent = parseCategory(second);
                JSONArray thirds = second == null ? null : second.optJSONArray("thirdLevelCategoryVOS");
                for (int k = 0; thirds != null && k < thirds.length(); k++) {
                    CategoryItem child = parseCategory(thirds.optJSONObject(k));
                    if (child.getId() > 0) parent.getChildren().add(child);
                }
                if (parent.getId() > 0) result.add(parent);
            }
        }
        return result;
    }

    private static CategoryItem parseCategory(JSONObject json) {
        if (json == null) return new CategoryItem(0, "");
        long id = json.optLong("categoryId", json.optLong("goodsCategoryId"));
        String name = json.optString("categoryName", json.optString("goodsCategoryName"));
        return new CategoryItem(id, name);
    }

    public static List<CartItem> parseCartItems(String json) throws Exception {
        JSONArray array = dataArray(json);
        List<CartItem> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) continue;
            CartItem cart = new CartItem();
            cart.setCartItemId(item.optLong("cartItemId"));
            cart.setGoodsId(item.optLong("goodsId"));
            cart.setGoodsName(item.optString("goodsName"));
            cart.setGoodsCoverImg(item.optString("goodsCoverImg"));
            cart.setGoodsCount(item.optInt("goodsCount", 1));
            cart.setSellingPrice(item.optDouble("sellingPrice"));
            result.add(cart);
        }
        return result;
    }

    public static List<Address> parseAddresses(String json) throws Exception {
        JSONArray array = dataArray(json);
        List<Address> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Address address = parseAddress(array.optJSONObject(i));
            if (address.getAddressId() > 0) result.add(address);
        }
        return result;
    }

    public static Address parseAddress(JSONObject item) {
        Address address = new Address();
        if (item == null) return address;
        address.setAddressId(item.optLong("addressId"));
        address.setUserName(item.optString("userName"));
        address.setUserPhone(item.optString("userPhone"));
        address.setProvinceName(item.optString("provinceName"));
        address.setCityName(item.optString("cityName"));
        address.setRegionName(item.optString("regionName"));
        address.setDetailAddress(item.optString("detailAddress"));
        address.setDefaultFlag(item.optInt("defaultFlag"));
        return address;
    }

    public static List<OrderItem> parseOrders(String json) throws Exception {
        JSONArray array = dataArray(json);
        List<OrderItem> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) continue;
            OrderItem order = new OrderItem();
            order.setOrderNo(item.optString("orderNo"));
            order.setTotalPrice(item.optDouble("totalPrice"));
            order.setOrderStatus(item.optInt("orderStatus"));
            order.setCreateTime(item.optString("createTime"));
            result.add(order);
        }
        return result;
    }
}
