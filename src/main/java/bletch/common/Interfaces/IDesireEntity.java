package bletch.common.Interfaces;

import java.util.Random;

import bletch.common.storage.ItemDesireSet;
import net.minecraft.item.ItemStack;
import net.tangotek.tektopia.Village;

public interface IDesireEntity extends IInventoryEntity {
	
	ItemDesireSet getDesireSet();
	
	int getLevel();
	
	Random getRNG();
	
	Village getVillage();
	
	boolean hasVillage();
	
	boolean isMale();
	
	boolean isStoragePriority();
	
	boolean isWorkTime();

	void pickupDesiredItem(ItemStack desiredItem);
	
	void setupDesires();
	
}
