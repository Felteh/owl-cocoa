package com.owl.cocoa.common;

import java.util.HashMap;
import java.util.Map;

public class Inventory {

    public final String objectName;
    public final Integer maxInventory;
    public final Integer totalInventory;
    public final Map<String, Integer> inventory;

    private Inventory(String objectName, Integer maxInventory, Integer totalInventory, Map<String, Integer> inventory) {
        this.objectName = objectName;
        this.maxInventory = maxInventory;
        this.totalInventory = totalInventory;
        this.inventory = inventory;
    }

    public Inventory(String objectName, Integer maxInventory) {
        this.objectName = objectName;
        this.maxInventory = maxInventory;
        this.totalInventory = 0;
        this.inventory = new HashMap<>();
    }

    public Inventory withItem(String item, Integer newQuan) {
        Integer curQuan = inventory.get(item);
        Integer sumInv = this.totalInventory;
        if (curQuan == null) {
            sumInv = sumInv + newQuan;
        } else {
            sumInv = sumInv + newQuan - curQuan;
        }

        inventory.put(item, newQuan);
        return new Inventory(objectName, maxInventory, sumInv, inventory);
    }

    public Inventory withInventory(Map<String, Integer> inventory) {
        return new Inventory(objectName, 0, 0, inventory);
    }

    @Override
    public String toString() {
        return "Inventory{" + "objectName=" + objectName + ", inventory=" + inventory + '}';
    }

}
