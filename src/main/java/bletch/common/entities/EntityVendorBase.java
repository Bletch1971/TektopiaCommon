package bletch.common.entities;

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.leviathanstudio.craftstudio.client.animation.ClientAnimationHandler;
import com.leviathanstudio.craftstudio.common.animation.AnimationHandler;

import bletch.common.core.CommonEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.tangotek.tektopia.TekVillager;
import net.tangotek.tektopia.Village;
import net.tangotek.tektopia.VillagerRole;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import net.tangotek.tektopia.entities.ai.EntityAIReadBook;
import net.tangotek.tektopia.entities.ai.EntityAIWanderStructure;

public abstract class EntityVendorBase extends EntityVillagerTek implements IMerchant, INpc {
	
    protected static final DataParameter<String> ANIMATION_KEY;
    protected static final AnimationHandler<EntityVendorBase> animationHandler;

    protected String modId;
    protected BlockPos firstCheck;
    @Nullable
    protected EntityPlayer buyingPlayer;
    @Nullable
    protected MerchantRecipeList vendorList;

    public EntityVendorBase(World worldIn, String modId) {
        super(worldIn, null, VillagerRole.VENDOR.value | VillagerRole.VISITOR.value);

        this.sleepOffset = 0;

        this.modId = modId;
    }

    @Override
    protected void addTask(int priority, EntityAIBase task) {
        if (task instanceof EntityAIWanderStructure && priority <= 100) {
            return;
        }
        if (task instanceof EntityAIReadBook) {
            return;
        }

        super.addTask(priority, task);
    }

    @Override
    public void addVillagerPosition() {
    }

    @Override
    protected void bedCheck() {
    }

    @Override
    public boolean canNavigate() {
        return !this.isTrading() && super.canNavigate();
    }

    protected abstract void checkStuck();

    @Override
    public float getAIMoveSpeed() {
        return super.getAIMoveSpeed() * 0.9F;
    }

    @Override
    protected boolean getCanUseDoors() {
        return true;
    }

    @Override
    @Nullable
    public EntityPlayer getCustomer() {
        return this.buyingPlayer;
    }

    @Override
    public BlockPos getPos() {
        return new BlockPos(this);
    }

    @Override
    @Nullable
    public MerchantRecipeList getRecipes(EntityPlayer player) {
        if (this.vendorList == null) {
            this.populateBuyingList();
        }

        return this.vendorList;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    protected void initEntityAIBase() {
        setupAITasks();
    }

    @Override
    public boolean isFleeFrom(Entity e) {
        return false;
    }

    @Override
    public com.google.common.base.Predicate<Entity> isHostile() {
        return (e) -> false;
    }

    @Override
    public boolean isLearningTime() {
        return false;
    }

    @Override
    public boolean isSleepingTime() {
        return false;
    }

    public boolean isTrading() {
        return this.buyingPlayer != null;
    }

    @Override
    public boolean isWorkTime() {
        return isWorkTime(this.world, this.sleepOffset) && !this.world.isRaining();
    }
    
    protected abstract void populateBuyingList();

    protected void prepStuck() {
        this.firstCheck = this.getPos();
    }

    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (this.isEntityAlive() && !this.isTrading() && !this.isChild() && !player.isSneaking() && !this.world.isRemote) {
            if (this.vendorList == null) {
                this.populateBuyingList();
            }

            if (this.vendorList != null && !this.vendorList.isEmpty()) {
                this.setCustomer(player);
                player.displayVillagerTradeGui(this);
                this.getNavigator().clearPath();
            }
        }

        return true;
    }

    @Override
    public void setCustomer(@Nullable EntityPlayer player) {
        this.buyingPlayer = player;
        this.getNavigator().clearPath();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setRecipes(@Nullable MerchantRecipeList recipeList) {
    }
    
    protected abstract void setupAITasks();

    @Override
    public void useRecipe(MerchantRecipe recipe) {
        recipe.incrementToolUses();
        this.livingSoundTime = -this.getTalkInterval();
        this.playSound(SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
        int i = 3 + this.rand.nextInt(4);
        if (recipe.getToolUses() == 1 || this.rand.nextInt(5) == 0) {
            i += 5;
        }

        if (recipe.getRewardsExp()) {
            this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY + 0.5D, this.posZ, i));
        }
    }

    @Override
    public void verifySellingItem(ItemStack stack) {
        if (!this.world.isRemote && this.livingSoundTime > -this.getTalkInterval() + 20) {
            this.livingSoundTime = -this.getTalkInterval();
            this.playSound(stack.isEmpty() ? SoundEvents.ENTITY_VILLAGER_NO : SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        
        if (compound.hasKey("Offers", 10)) {
            NBTTagCompound nbttagcompound = compound.getCompoundTag("Offers");
            this.vendorList = new MerchantRecipeList(nbttagcompound);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

        if (this.vendorList != null) {
            compound.setTag("Offers", this.vendorList.getRecipiesAsTags());
        }
    }

    static {
        ANIMATION_KEY = EntityDataManager.createKey(EntityVendorBase.class, DataSerializers.STRING);

        animationHandler = TekVillager.getNewAnimationHandler(EntityVendorBase.class);
    }

    public static boolean isWorkTime(World world, int sleepOffset) {
        return Village.isTimeOfDay(world, WORK_START_TIME, WORK_END_TIME, sleepOffset);
    }

    protected static void setupCraftStudioAnimations(String modId, String modelName) {
        animationHandler.addAnim(modId, CommonEntities.ANIMATION_VILLAGER_EAT, modelName, true);
        animationHandler.addAnim(modId, CommonEntities.ANIMATION_VILLAGER_READ, modelName, true);
        animationHandler.addAnim(modId, CommonEntities.ANIMATION_VILLAGER_RUN, modelName, true);
        animationHandler.addAnim(modId, CommonEntities.ANIMATION_VILLAGER_SIT, modelName, true);
        animationHandler.addAnim(modId, CommonEntities.ANIMATION_VILLAGER_SITCHEER, modelName, true);
        animationHandler.addAnim(modId, CommonEntities.ANIMATION_VILLAGER_SLEEP, modelName, true);
        animationHandler.addAnim(modId, CommonEntities.ANIMATION_VILLAGER_WALK, modelName, true);
        animationHandler.addAnim(modId, CommonEntities.ANIMATION_VILLAGER_WALKSAD, modelName, true);
    }

    @Override
    public AnimationHandler<EntityVendorBase> getAnimationHandler() {
        return animationHandler;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.dataManager.set(ANIMATION_KEY, "");
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(ANIMATION_KEY, "");
        super.entityInit();
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);

        if (this.isWorldRemote() && ANIMATION_KEY.equals(key)) {
            this.updateClientAnimation(this.dataManager.get(ANIMATION_KEY));
        }
    }

    @Override
    public void playClientAnimation(String animationName) {
        if (!this.getAnimationHandler().isAnimationActive(modId, animationName, this)) {
            this.getAnimationHandler().startAnimation(modId, animationName, this);
        }
    }

    @Override
    public void stopClientAnimation(String animationName) {
        super.stopClientAnimation(animationName);
        if (this.getAnimationHandler().isAnimationActive(modId, animationName, this)) {
            this.getAnimationHandler().stopAnimation(modId, animationName, this);
        }
    }

    protected void updateClientAnimation(String animationName) {
        ClientAnimationHandler<EntityVendorBase> clientAnimationHandler = (ClientAnimationHandler<EntityVendorBase>) this.getAnimationHandler();

        Set<String> animChannels = clientAnimationHandler.getAnimChannels().keySet();
        animChannels.forEach(a -> clientAnimationHandler.stopAnimation(a, this));

        if (!animationName.isEmpty()) {
            clientAnimationHandler.startAnimation(modId, animationName, this);
        }
    }

    @Override
    public boolean isPlayingAnimation(String animationName) {
        return Objects.equals(animationName, this.dataManager.get(ANIMATION_KEY));
    }

    @Override
    public void playServerAnimation(String animationName) {
        this.dataManager.set(ANIMATION_KEY, animationName);
    }

    @Override
    public void stopServerAnimation(String animationName) {
        this.dataManager.set(ANIMATION_KEY, "");
    }

}
