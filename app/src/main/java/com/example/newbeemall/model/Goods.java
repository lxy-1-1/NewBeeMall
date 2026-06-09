package com.example.newbeemall.model;

/**
 * 商品数据模型
 */
public class Goods {
    private long goodsId;
    private String goodsName;
    private double sellingPrice;
    private String goodsCoverImg;
    private String goodsIntro;

    // ====== Getter / Setter ======
    public long getGoodsId() { return goodsId; }
    public void setGoodsId(long goodsId) { this.goodsId = goodsId; }

    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }

    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }

    public String getGoodsCoverImg() { return goodsCoverImg; }
    public void setGoodsCoverImg(String goodsCoverImg) { this.goodsCoverImg = goodsCoverImg; }

    public String getGoodsIntro() { return goodsIntro; }
    public void setGoodsIntro(String goodsIntro) { this.goodsIntro = goodsIntro; }
}
