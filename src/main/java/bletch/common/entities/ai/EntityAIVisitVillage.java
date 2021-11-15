package bletch.common.entities.ai;

import net.minecraft.util.math.BlockPos;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import net.tangotek.tektopia.entities.EntityVillagerTek.MovementMode;

import java.util.function.Function;
import java.util.function.Predicate;

public class EntityAIVisitVillage extends EntityAIMoveToBlock {
    protected final Function<EntityVillagerTek, BlockPos> whereFunc;
    protected final Predicate<EntityVillagerTek> shouldPred;
    protected final Runnable resetRunner;
    protected final Runnable startRunner;
    protected final EntityVillagerTek entity;
    protected final MovementMode moveMode;

    public EntityAIVisitVillage(EntityVillagerTek entity, Predicate<EntityVillagerTek> shouldPred, Function<EntityVillagerTek, BlockPos> whereFunc, MovementMode moveMode, Runnable startRunner, Runnable resetRunner) {
        super(entity);
        this.entity = entity;
        this.shouldPred = shouldPred;
        this.whereFunc = whereFunc;
        this.resetRunner = resetRunner;
        this.startRunner = startRunner;
        this.moveMode = moveMode;
    }

    public boolean shouldExecute() {
        if (this.entity.isAITick() && this.entity.hasVillage() && this.shouldPred.test(this.entity))
            return super.shouldExecute();
        return false;
    }

    public void startExecuting() {
        if (this.startRunner != null) {
            this.startRunner.run();
        }

        super.startExecuting();
    }

    public void resetTask() {
        if (this.resetRunner != null) {
            this.resetRunner.run();
        }

        super.resetTask();
    }

    protected BlockPos getDestinationBlock() {
        return this.whereFunc.apply(this.entity);
    }

    protected boolean isNearWalkPos() {
        return this.entity.getPosition().distanceSq(this.destinationPos) < 4.0D;
    }

    protected void updateMovementMode() {
        this.entity.setMovementMode(this.moveMode);
    }
}
