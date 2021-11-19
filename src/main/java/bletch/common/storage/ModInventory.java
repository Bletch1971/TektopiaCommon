package bletch.common.storage;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import bletch.common.Interfaces.IInventoryEntity;

public class ModInventory extends InventoryBasic {

    private final IInventoryEntity owner;

    public ModInventory(IInventoryEntity owner, String title, boolean customName, int slotCount) {
        super(title, customName, slotCount);
        this.owner = owner;
    }

    @SideOnly(Side.CLIENT)
    public ModInventory(ITextComponent title, int slotCount) {
        this(null, title.getUnformattedText(), true, slotCount);
    }

    public ItemStack addItem(ItemStack stack) {
        ItemStack newItem = stack.copy();
        int emptySlot = -1;

        for (int i = 0; i < this.getSizeInventory(); ++i) {
            ItemStack oldItem = this.getStackInSlot(i);

            if (oldItem.isEmpty() && emptySlot < 0) {
                emptySlot = i;
            } else if (areItemsStackable(oldItem, newItem)) {
                int j = Math.min(this.getInventoryStackLimit(), oldItem.getMaxStackSize());
                int k = Math.min(newItem.getCount(), j - oldItem.getCount());

                if (k > 0) {
                    oldItem.grow(k);
                    newItem.shrink(k);

                    if (newItem.isEmpty()) {
                        this.onInventoryUpdated(oldItem);
                        this.markDirty();
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        if (emptySlot >= 0 && !newItem.isEmpty()) {
            this.setInventorySlotContents(emptySlot, newItem);
            return ItemStack.EMPTY;
        }

        if (newItem.getCount() != stack.getCount()) {
            this.markDirty();
        }

        return newItem;
    }

    public static boolean areItemsStackable(ItemStack itemStack1, ItemStack itemStack2) {
        return ItemStack.areItemsEqual(itemStack1, itemStack2) && ItemStack.areItemStackTagsEqual(itemStack1, itemStack2);
    }

    public void cloneFrom(ModInventory other) {
        for (int i = 0; i < other.getSizeInventory(); ++i) {
            this.addItem(other.getStackInSlot(i));
        }
    }

    public static int countItems(List<ItemStack> items) {
        return items.stream()
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    public void deleteOverstock(Predicate<ItemStack> predicate, int max) {
        int count = 0;

        for (int i = 0; i < this.getSizeInventory(); ++i) {
            ItemStack itemStack = this.getStackInSlot(i);

            if (predicate.test(itemStack)) {
                if (count >= max) {
                    this.setInventorySlotContents(i, ItemStack.EMPTY);
                } else if (count + itemStack.getCount() > max) {
                    itemStack.shrink(max - count);
                    this.onInventoryUpdated(itemStack);
                    count = max;
                } else {
                    count += itemStack.getCount();
                }
            }
        }
    }

    private List<ItemStack> findItems(Function<ItemStack, Integer> func, int itemCount, boolean remove) {
        ArrayList<ItemStack> outList = new ArrayList<>();
        int needed = (itemCount > 0) ? itemCount : Integer.MAX_VALUE;
        List<Tuple<ItemStack, Integer>> matchedItems = new ArrayList<>();

        for (int i = 0; i < this.getSizeInventory(); ++i) {
            ItemStack itemStack = this.getStackInSlot(i);

            if (net.tangotek.tektopia.ModItems.canVillagerSee(itemStack) && func.apply(itemStack) > 0) {
                matchedItems.add(new Tuple<>(itemStack, i));
            }
        }

        matchedItems.sort(Comparator.comparing(pair -> func.apply(pair.getFirst())));

        for (int i = matchedItems.size() - 1; i >= 0 && needed > 0; --i) {

            Tuple<ItemStack, Integer> pair2 = matchedItems.get(i);
            ItemStack itemStack2 = pair2.getFirst();
            int slot = pair2.getSecond();

            if (remove) {
                if (itemStack2.getCount() <= needed) {

                    outList.add(itemStack2);
                    this.setInventorySlotContents(slot, ItemStack.EMPTY);
                    needed -= itemStack2.getCount();

                } else {

                    itemStack2.shrink(needed);
                    ItemStack newItem = itemStack2.copy();
                    newItem.setCount(needed);
                    outList.add(newItem);
                    needed = 0;
                    this.onInventoryUpdated(newItem);

                }
            } else {
                outList.add(itemStack2);
                needed -= itemStack2.getCount();
            }
        }

        return outList;
    }

    public ItemStack getItem(Function<ItemStack, Integer> func) {
        List<ItemStack> items = this.findItems(func, 1, false);
        if (!items.isEmpty()) {
            return items.get(0);
        }
        return ItemStack.EMPTY;
    }

    public List<ItemStack> getItems(Predicate<ItemStack> pred, int itemCount) {
        return this.findItems(p -> pred.test(p) ? 1 : 0, itemCount, false);
    }

    public List<ItemStack> getItems(Function<ItemStack, Integer> func, int itemCount) {
        return this.findItems(func, itemCount, false);
    }

    public List<ItemStack> getItemsByStack(Function<ItemStack, Integer> func, int itemCount) {
        return this.findItems(func, itemCount, false);
    }

    public int getItemCount(Function<ItemStack, Integer> func) {
        List<ItemStack> itemList = this.getItems(func, 0);
        int count = 0;

        for (ItemStack itemStack : itemList) {
            count += itemStack.getCount();
        }

        return count;
    }

    public boolean hasSlotFree() {
        for (int i = 0; i < this.getSizeInventory(); ++i) {
            ItemStack itemStack = this.getStackInSlot(i);

            if (itemStack.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public void mergeItems(ModInventory other) {
        for (int i = 0; i < other.getSizeInventory(); ++i) {
            ItemStack itemStack = other.getStackInSlot(i);

            if (itemStack.isEmpty()) {
                this.addItem(itemStack);
            }
        }
    }

    private void onInventoryUpdated(ItemStack itemStack) {
        if (this.owner != null && !this.owner.getWorld().isRemote) {
            this.owner.onInventoryUpdated(itemStack);
        }
    }

    public List<ItemStack> removeItems(Function<ItemStack, Integer> func, int itemCount) {
        return this.findItems(func, itemCount, true);
    }

    public List<ItemStack> removeItems(Predicate<ItemStack> pred, int itemCount) {
        return this.findItems((p) -> pred.test(p) ? 1 : 0, itemCount, true);
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        ItemStack oldItem = this.getStackInSlot(index);

        super.setInventorySlotContents(index, stack);

        if (!oldItem.isEmpty()) {
            this.onInventoryUpdated(oldItem);
        }
        this.onInventoryUpdated(stack);
    }

    public void readNBT(NBTTagCompound compound) {
        this.clear();

        NBTTagList nbttaglist = compound.getTagList("Inventory", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            ItemStack itemstack = new ItemStack(nbttaglist.getCompoundTagAt(i));

            if (!itemstack.isEmpty()) {
                this.addItem(itemstack);
            }
        }
    }

    public void writeNBT(NBTTagCompound compound) {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.getSizeInventory(); ++i) {
            ItemStack itemstack = this.getStackInSlot(i);

            if (!itemstack.isEmpty()) {
                nbttaglist.appendTag(itemstack.writeToNBT(new NBTTagCompound()));
            }
        }

        compound.setTag("Inventory", nbttaglist);
    }
}
