package com.owl.cocoa.ship;

import com.owl.cocoa.common.Item;

public class TradeableItem {

    public final Item item;

    public String buyFrom;
    public int buyQuantity;
    public double buyPrice;

    public String sellFrom;
    public int sellQuantity;
    public double sellPrice;

    public double roi;

    public TradeableItem(Item item, String key1, Integer e1Inventory, Double e1Price, String key2, Integer e2Inventory, Double e2Price) {
        this.item = item;

        if (e1Inventory == 0) {
            buy(key2, e2Inventory, e2Price);
            sell(key1, e1Inventory, e1Price);
        } else if (e2Inventory == 0) {
            sell(key2, e2Inventory, e2Price);
            buy(key1, e1Inventory, e1Price);
        } else if (e1Price < e2Price) {
            sell(key2, e2Inventory, e2Price);
            buy(key1, e1Inventory, e1Price);
        } else {
            buy(key2, e2Inventory, e2Price);
            sell(key1, e1Inventory, e1Price);
        }
        calculateRoi();
    }

    private void buy(String buyFrom, Integer buyQuantity, Double buyPrice) {
        this.buyFrom = buyFrom;
        this.buyQuantity = buyQuantity;
        this.buyPrice = buyPrice;
    }

    private void sell(String sellFrom, Integer sellQuantity, Double sellPrice) {
        this.sellFrom = sellFrom;
        this.sellQuantity = sellQuantity;
        this.sellPrice = sellPrice;
    }

    private void calculateRoi() {
        roi = sellPrice / buyPrice;
    }

    @Override
    public String toString() {
        return "TradeableItem{" + "item=" + item + ", buyFrom=" + buyFrom + ", buyQuantity=" + buyQuantity + ", buyPrice=" + buyPrice + ", sellFrom=" + sellFrom + ", sellQuantity=" + sellQuantity + ", sellPrice=" + sellPrice + ", roi=" + roi + '}';
    }

}
