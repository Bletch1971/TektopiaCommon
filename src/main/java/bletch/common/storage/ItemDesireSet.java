package bletch.common.storage;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import bletch.common.Interfaces.IDesireEntity;

public class ItemDesireSet {
	
	private static final Random random = new Random();
	
    protected List<ItemDesire> itemDesires;
    protected boolean deliveryDirty;
    protected int deliveryId;
    protected byte deliverySlot;
    protected short deliveryCount;
    protected int totalDeliverySize;

    public ItemDesireSet() {
        this.itemDesires = new ArrayList<>();
        this.deliveryDirty = true;
        this.deliveryId = 0;
        this.deliverySlot = -1;
        this.deliveryCount = 0;
        this.totalDeliverySize = 0;
    }

    public void clear() {
        this.itemDesires.clear();
    }

    public void addItemDesire(ItemDesire desire) {
        this.itemDesires.add(desire);
    }

    public void forceUpdate() {
        this.itemDesires.forEach(d -> d.forceUpdate());
        this.deliveryDirty = true;
    }

    public void onStorageUpdated(IDesireEntity entity, ItemStack storageItem) {
        this.itemDesires.forEach(d -> d.onStorageUpdated(entity, storageItem));
    }

    public void onInventoryUpdated(IDesireEntity entity, ItemStack updatedItem) {
        this.itemDesires.forEach(d -> d.onInventoryUpdated(entity, updatedItem));
        this.deliveryDirty = true;
    }

    public ItemDesire getNeededDesire(IDesireEntity entity) {
        Collections.shuffle(this.itemDesires, random);

        for (ItemDesire desire : this.itemDesires) {
            if (desire.shouldPickUp(entity)) {
                return desire;
            }
        }

        return null;
    }

}
