package bletch.common.Interfaces;

import bletch.common.storage.ModInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IInventoryEntity {

	ModInventory getInventory();
	
	World getWorld();
	
	void onInventoryUpdated(ItemStack updatedItem);
	
}
