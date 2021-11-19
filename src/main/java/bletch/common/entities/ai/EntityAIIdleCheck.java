package bletch.common.entities.ai;

import bletch.common.entities.EntityEnemyBase;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIIdleCheck extends EntityAIBase {
    protected final EntityEnemyBase entity;
    private int idleTicks = 0;

    public EntityAIIdleCheck(EntityEnemyBase entity) {
        this.entity = entity;
        this.setMutexBits(7);
    }

    @Override
    public boolean shouldExecute() {
        return this.entity.isAITick() && this.entity.hasVillage();
    }

    @Override
    public void startExecuting() {
        this.idleTicks = 0;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return true;
    }

    @Override
    public void updateTask() {
        ++this.idleTicks;
        if (this.idleTicks % 80 == 0) {
            this.entity.setStoragePriority();
        }

        this.entity.setIdle(this.idleTicks);
    }

    @Override
    public void resetTask() {
        this.entity.setIdle(0);
    }
}
