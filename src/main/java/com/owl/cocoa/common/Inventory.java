package com.owl.cocoa.common;

import java.util.HashMap;
import java.util.Map;

public class Inventory {

    public final String objectName;
    public final Integer maxInventory;
    public final Integer totalInventory;
    public final Map<Item, Integer> inventory;
    public final Map<Item, Double> price;

    private Inventory(String objectName, Integer maxInventory, Integer totalInventory, Map<Item, Integer> inventory, Map<Item, Double> price) {
        this.objectName = objectName;
        this.maxInventory = maxInventory;
        this.totalInventory = totalInventory;
        this.inventory = inventory;
        this.price = price;
    }

    public Inventory(String objectName, Integer maxInventory) {
        this.objectName = objectName;
        this.maxInventory = maxInventory;
        this.totalInventory = 0;
        this.inventory = new HashMap<>();
        this.price = new HashMap<>();
    }

    public Inventory withItem(Item item, Integer newQuan) {
        Integer curQuan = inventory.get(item);
        Integer sumInv = this.totalInventory;
        if (curQuan == null) {
            sumInv = sumInv + newQuan;
        } else {
            sumInv = sumInv + newQuan - curQuan;
        }

        inventory.put(item, newQuan);
        if (price.get(item) == null) {
            price.put(item, item.minPrice + ((item.maxPrice - item.minPrice) / 2));
        }
        return new Inventory(objectName, maxInventory, sumInv, inventory, price);
    }

    public Inventory withInventory(Map<Item, Integer> inventory) {
        return new Inventory(objectName, 0, 0, inventory, price);
    }

    public Inventory withLowerPrice(Item item) {
        if (Math.random() > 0.95) {
            Double p = price.get(item);
            Double dropAmount = (Math.random() * (item.maxPrice - item.minPrice)) / 100;
            p = p - dropAmount;
            if (p < item.minPrice) {
                p = item.minPrice;
            }
            price.put(item, p);
            return new Inventory(objectName, maxInventory, totalInventory, inventory, price);
        } else {
            return this;
        }
    }

    public Inventory withIncreasePrice(Item item) {
        if (Math.random() > 0.95) {
            Double p = price.get(item);
            Double hikeAmount = (Math.random() * (item.maxPrice - item.minPrice)) / 100;
            p = p + hikeAmount;
            if (p > item.maxPrice) {
                p = item.maxPrice;
            }
            price.put(item, p);
            return new Inventory(objectName, maxInventory, totalInventory, inventory, price);
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        return "Inventory{" + "objectName=" + objectName + ", maxInventory=" + maxInventory + ", totalInventory=" + totalInventory + ", inventory=" + inventory + ", price=" + price + '}';
    }

}
