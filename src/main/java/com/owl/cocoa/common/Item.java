package com.owl.cocoa.common;

public enum Item {

    COCOA(100, 200),
    CHOCOLATE(5000, 10000);

    public final double minPrice;
    public final double maxPrice;

    private Item(double minPrice, double maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
}
