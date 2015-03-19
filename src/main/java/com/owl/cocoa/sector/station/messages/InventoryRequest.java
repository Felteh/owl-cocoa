package com.owl.cocoa.sector.station.messages;

import com.owl.cocoa.common.Item;

public class InventoryRequest {

    public final InventoryRequestType requestType;
    public final Item item;
    public final int quantity;
    public final double price;

    public InventoryRequest(InventoryRequestType requestType, Item item, int quantity, double price) {
        this.requestType = requestType;
        this.item = item;
        this.quantity = quantity;
        this.price = price;
    }

    @Override
    public String toString() {
        return "InventoryRequest{" + "requestType=" + requestType + ", item=" + item + ", quantity=" + quantity + ", price=" + price + '}';
    }

}
