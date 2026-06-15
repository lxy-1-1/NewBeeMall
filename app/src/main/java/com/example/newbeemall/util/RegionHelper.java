package com.example.newbeemall.util;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 省市区联动数据
 */
public class RegionHelper {

    private static final Map<String, Map<String, List<String>>> DATA = new LinkedHashMap<>();

    static {
        // 北京
        Map<String, List<String>> bj = new LinkedHashMap<>();
        bj.put("东城区", Arrays.asList("永安大街", "东直门", "王府井", "建国门"));
        bj.put("西城区", Arrays.asList("西单", "金融街", "德胜门"));
        bj.put("朝阳区", Arrays.asList("三里屯", "国贸", "望京", "亚运村"));
        bj.put("海淀区", Arrays.asList("中关村", "五道口", "西二旗"));
        bj.put("丰台区", Arrays.asList("方庄", "科技园"));
        DATA.put("北京市", bj);

        // 上海
        Map<String, List<String>> sh = new LinkedHashMap<>();
        sh.put("黄浦区", Arrays.asList("南京东路", "外滩", "人民广场"));
        sh.put("徐汇区", Arrays.asList("徐家汇", "衡山路"));
        sh.put("浦东新区", Arrays.asList("陆家嘴", "张江", "世纪公园"));
        sh.put("静安区", Arrays.asList("南京西路", "静安寺"));
        DATA.put("上海市", sh);

        // 广东
        Map<String, List<String>> gd = new LinkedHashMap<>();
        gd.put("广州市", Arrays.asList("天河区", "越秀区", "海珠区", "白云区"));
        gd.put("深圳市", Arrays.asList("南山区", "福田区", "罗湖区", "宝安区"));
        gd.put("东莞市", Arrays.asList("莞城区", "南城区", "东城区"));
        DATA.put("广东省", gd);

        // 河南
        Map<String, List<String>> hn = new LinkedHashMap<>();
        hn.put("郑州市", Arrays.asList("金水区", "中原区", "二七区", "管城区"));
        hn.put("信阳市", Arrays.asList("浉河区", "平桥区", "潢川县"));
        hn.put("洛阳市", Arrays.asList("涧西区", "老城区", "西工区"));
        DATA.put("河南省", hn);

        // 湖北
        Map<String, List<String>> hb = new LinkedHashMap<>();
        hb.put("武汉市", Arrays.asList("武昌区", "江汉区", "洪山区", "汉阳区"));
        hb.put("宜昌市", Arrays.asList("西陵区", "伍家岗区"));
        DATA.put("湖北省", hb);

        // 浙江
        Map<String, List<String>> zj = new LinkedHashMap<>();
        zj.put("杭州市", Arrays.asList("西湖区", "上城区", "拱墅区", "滨江区"));
        zj.put("宁波市", Arrays.asList("海曙区", "鄞州区"));
        DATA.put("浙江省", zj);

        // 江苏
        Map<String, List<String>> js = new LinkedHashMap<>();
        js.put("南京市", Arrays.asList("玄武区", "秦淮区", "鼓楼区", "建邺区"));
        js.put("苏州市", Arrays.asList("姑苏区", "工业园区", "虎丘区"));
        DATA.put("江苏省", js);

        // 四川
        Map<String, List<String>> sc = new LinkedHashMap<>();
        sc.put("成都市", Arrays.asList("武侯区", "锦江区", "青羊区", "高新区"));
        sc.put("绵阳市", Arrays.asList("涪城区", "游仙区"));
        DATA.put("四川省", sc);

        // 湖南
        Map<String, List<String>> hunan = new LinkedHashMap<>();
        hunan.put("长沙市", Arrays.asList("岳麓区", "天心区", "芙蓉区", "雨花区"));
        DATA.put("湖南省", hunan);

        // 山东
        Map<String, List<String>> sd = new LinkedHashMap<>();
        sd.put("济南市", Arrays.asList("历下区", "市中区", "槐荫区"));
        sd.put("青岛市", Arrays.asList("市南区", "市北区", "崂山区"));
        DATA.put("山东省", sd);
    }

    public static List<String> getProvinces() {
        return new ArrayList<>(DATA.keySet());
    }

    public static List<String> getCities(String province) {
        Map<String, List<String>> cities = DATA.get(province);
        return cities != null ? new ArrayList<>(cities.keySet()) : new ArrayList<>();
    }

    public static List<String> getDistricts(String province, String city) {
        Map<String, List<String>> cities = DATA.get(province);
        if (cities == null) return new ArrayList<>();
        List<String> districts = cities.get(city);
        return districts != null ? new ArrayList<>(districts) : new ArrayList<>();
    }
}
