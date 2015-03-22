package com.owl.cocoa.ship;

import com.owl.cocoa.common.Item;

public class TradeableItem {

    public final Item item;

    public final String buyFrom;
    public final Integer buyQuantity;
    public final Double buyPrice;

    public final String sellFrom;
    public final Integer sellQuantity;
    public final Double sellPrice;

    public final Double roi;

    public TradeableItem(Item item) {
        this.item = item;
        this.buyFrom = null;
        this.buyQuantity = null;
        this.buyPrice = null;
        this.sellFrom = null;
        this.sellQuantity = null;
        this.sellPrice = null;
        this.roi = null;
    }

    private TradeableItem(Item item, String buyFrom, Integer buyQuantity, Double buyPrice, String sellFrom, Integer sellQuantity, Double sellPrice, Double roi) {
        this.item = item;
        this.buyFrom = buyFrom;
        this.buyQuantity = buyQuantity;
        this.buyPrice = buyPrice;
        this.sellFrom = sellFrom;
        this.sellQuantity = sellQuantity;
        this.sellPrice = sellPrice;
        if (sellPrice != null && buyPrice != null) {
            this.roi = sellPrice / buyPrice;
        } else {
            this.roi = 0d;
        }
    }

    public TradeableItem withBuy(String buyFrom, Integer buyQuantity, Double buyPrice) {
        return new TradeableItem(item, buyFrom, buyQuantity, buyPrice, sellFrom, sellQuantity, sellPrice, roi);
    }

    public TradeableItem withSell(String sellFrom, Integer sellQuantity, Double sellPrice) {
        return new TradeableItem(item, buyFrom, buyQuantity, buyPrice, sellFrom, sellQuantity, sellPrice, roi);
    }

    @Override
    public String toString() {
        return "TradeableItem{" + "item=" + item + ", buyFrom=" + buyFrom + ", buyQuantity=" + buyQuantity + ", buyPrice=" + buyPrice + ", sellFrom=" + sellFrom + ", sellQuantity=" + sellQuantity + ", sellPrice=" + sellPrice + ", roi=" + roi + '}';
    }

}
