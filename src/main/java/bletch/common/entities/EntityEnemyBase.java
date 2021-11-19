package bletch.common.entities;

import java.util.Objects;
import java.util.Set;

import com.leviathanstudio.craftstudio.client.animation.ClientAnimationHandler;
import com.leviathanstudio.craftstudio.common.animation.AnimationHandler;

import bletch.common.MovementMode;
import bletch.common.Interfaces.IVillageEnemy;
import bletch.common.core.CommonEntities;
import bletch.common.entities.ai.EntityAIIdleCheck;
import bletch.common.entities.ai.EntityAIUseDoor;
import bletch.common.entities.ai.EntityAIUseGate;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.monster.IMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.tangotek.tektopia.TekVillager;
import net.tangotek.tektopia.VillagerRole;
import net.tangotek.tektopia.entities.EntityVillageNavigator;

public abstract class EntityEnemyBase extends EntityVillageNavigator implements IVillageEnemy, IMob {

    protected static final AnimationHandler<EntityEnemyBase> animationHandler;
    protected static final DataParameter<String> ANIMATION_KEY;
    protected static final DataParameter<Integer> LEVEL;
    protected static final DataParameter<Byte> MOVEMENT_MODE;

    public static final Integer MIN_LEVEL = 1;
    public static final Integer MAX_LEVEL = 5;

    protected String modId;
    protected BlockPos firstCheck;
    protected int idle;
    protected MovementMode lastMovementMode;

	public EntityEnemyBase(World worldIn, String modId) {
		super(worldIn, VillagerRole.ENEMY.value | VillagerRole.VISITOR.value);

        this.modId = modId;
        this.idle = 0;

        this.setSize(0.6F, 1.95F);
        this.setRotation(0.0F, 0.0F);
	}

    public EntityEnemyBase(World worldIn, String modId, int level) {
        this(worldIn, modId);

        this.setLevel(level);
    }

    protected void addTask(int priority, EntityAIBase task) {
        this.tasks.addTask(priority, task);
    }

    protected abstract void checkStuck();
    
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected boolean getCanUseDoors() {
        return true;
    }

    public int getIdle() {
        return this.idle;
    }

	@Override
    public int getLevel() {
        return this.dataManager.get(LEVEL);
    }

    public MovementMode getMovementMode() {
        return MovementMode.valueOf(this.dataManager.get(MOVEMENT_MODE));
    }

	@Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    public World getWorld() {
        return this.world;
    }

    @Override
    public boolean isMale() {
        return this.getUniqueID().getLeastSignificantBits() % 2L == 0L;
    }

    protected void prepStuck() {
        this.firstCheck = this.getPosition();
    }

    public void setIdle(int idle) {
        this.idle = idle;
    }

	@Override
    public void setLevel(int level) {
        this.dataManager.set(LEVEL, Math.max(MIN_LEVEL, Math.min(level, MAX_LEVEL)));
    }

    public void setMovementMode(MovementMode mode) {
        this.dataManager.set(MOVEMENT_MODE, mode.id);
    }
    
    protected void setupAITasks() {
        this.addTask(0, new EntityAISwimming(this));

        this.addTask(15, new EntityAIUseDoor(this));

        this.addTask(15, new EntityAIUseGate(this));

        this.addTask(150, new EntityAIIdleCheck(this));
    }

    @SideOnly(Side.CLIENT)
    protected void startWalking() {
        MovementMode mode = this.getMovementMode();

        if (mode != this.lastMovementMode) {
            if (this.lastMovementMode != null) {
                this.stopWalking();
            }

            this.lastMovementMode = mode;
            if (mode != null) {
                this.playClientAnimation(mode.animation);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    protected void stopWalking() {
        if (this.lastMovementMode != null) {
            this.stopClientAnimation(this.lastMovementMode.animation);
            this.lastMovementMode = null;
        }
    }

	@Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        if (compound.hasKey("level"))
            this.setLevel(compound.getInteger("level"));
    }

	@Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);

        compound.setInteger("level", this.getLevel());
    }

    static {
        ANIMATION_KEY = EntityDataManager.createKey(EntityEnemyBase.class, DataSerializers.STRING);
        LEVEL = EntityDataManager.createKey(EntityEnemyBase.class, DataSerializers.VARINT);
        MOVEMENT_MODE = EntityDataManager.createKey(EntityEnemyBase.class, DataSerializers.BYTE);

        animationHandler = TekVillager.getNewAnimationHandler(EntityEnemyBase.class);
    }

    protected static void setupCraftStudioAnimations(String modId, String modelName) {
        animationHandler.addAnim(modId, CommonEntities.ANIMATION_VILLAGER_WALK, modelName, true);
        animationHandler.addAnim(modId, CommonEntities.ANIMATION_VILLAGER_RUN, modelName, true);
    }

    @Override
    public AnimationHandler<EntityEnemyBase> getAnimationHandler() {
        return animationHandler;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();

        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        this.dataManager.set(ANIMATION_KEY, "");
        this.dataManager.set(MOVEMENT_MODE, MovementMode.WALK.id);
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(ANIMATION_KEY, "");
        this.dataManager.register(LEVEL, 1);
        this.dataManager.register(MOVEMENT_MODE, (byte) 0);

        super.entityInit();
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);

        if (this.isWorldRemote() && ANIMATION_KEY.equals(key)) {
            this.updateClientAnimation(this.dataManager.get(ANIMATION_KEY));
        }

        if (MOVEMENT_MODE.equals(key) && this.isWalking()) {
            this.startWalking();
        }
    }

    @Override
    public void playClientAnimation(String animationName) {
        if (!this.getAnimationHandler().isAnimationActive(this.modId, animationName, this)) {
            this.getAnimationHandler().startAnimation(this.modId, animationName, this);
        }
    }

    @Override
    public void stopClientAnimation(String animationName) {
        super.stopClientAnimation(animationName);

        if (this.getAnimationHandler().isAnimationActive(this.modId, animationName, this)) {
            this.getAnimationHandler().stopAnimation(this.modId, animationName, this);
        }
    }

    protected void updateClientAnimation(String animationName) {
        ClientAnimationHandler<EntityEnemyBase> clientAnimationHandler = (ClientAnimationHandler<EntityEnemyBase>) this.getAnimationHandler();

        Set<String> animChannels = clientAnimationHandler.getAnimChannels().keySet();
        animChannels.forEach(a -> clientAnimationHandler.stopAnimation(a, this));

        if (!animationName.isEmpty()) {
            clientAnimationHandler.startAnimation(this.modId, animationName, this);
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
