package com.owl.cocoa.common;

public enum Item {

    WHEAT(100, 200),
    GOLD(5000, 10000);

    public final double minPrice;
    public final double maxPrice;

    private Item(double minPrice, double maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
}
