package bletch.common.entities.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.tangotek.tektopia.entities.EntityVillageNavigator;
import net.tangotek.tektopia.pathing.PathNavigateVillager2;

public class EntityAIUseGate extends EntityAIBase {
    private final EntityLiving entity;
    protected BlockFenceGate gateBlock;
    protected BlockPos gatePosition;
    int closeTimer;

    public EntityAIUseGate(EntityLiving entitylivingIn) {
        this.gatePosition = BlockPos.ORIGIN;
        this.entity = entitylivingIn;
    }

    public boolean shouldExecute() {
        if (!this.entity.collidedHorizontally) {
            return false;
        }

        PathNavigateVillager2 pathNavigate = (PathNavigateVillager2) this.entity.getNavigator();
        Path path = pathNavigate.getPath();

        if (path != null && !path.isFinished() && pathNavigate.getEnterDoors()) {
            for (int i = 0; i < Math.min(path.getCurrentPathIndex() + 2, path.getCurrentPathLength()); ++i) {
                PathPoint pathpoint = path.getPathPointFromIndex(i);
                this.gatePosition = new BlockPos(pathpoint.x, pathpoint.y + 1, pathpoint.z);

                if (this.entity.getDistanceSq(this.gatePosition.getX(), this.entity.posY, this.gatePosition.getZ()) <= 2.25D) {
                    this.gateBlock = this.getBlockGate(this.gatePosition);

                    if (this.gateBlock != null) {
                        return true;
                    }
                }
            }

            this.gatePosition = new BlockPos(this.entity);
            this.gateBlock = this.getBlockGate(this.gatePosition);
            return this.gateBlock != null;
        }

        return false;
    }

    public void startExecuting() {
        this.closeTimer = 20;
        this.toggleGate(true);
    }

    public boolean shouldContinueExecuting() {
        return this.closeTimer > 0;
    }

    public void updateTask() {
        --this.closeTimer;

        if (this.closeTimer == 0 && !this.isGateClear()) {
            this.toggleGate(true);
            this.closeTimer = 25;
        }

        super.updateTask();
    }

    public void resetTask() {
        this.toggleGate(false);
    }

    protected BlockFenceGate getBlockGate(BlockPos pos) {
        IBlockState iblockstate = this.entity.world.getBlockState(pos);
        Block block = iblockstate.getBlock();
        return block instanceof BlockFenceGate ? (BlockFenceGate) block : null;
    }

    protected boolean isGateClear() {
        return this.entity.world.getEntitiesWithinAABB(EntityVillageNavigator.class, new AxisAlignedBB(this.gatePosition)).isEmpty();
    }

    protected void toggleGate(boolean open) {
        IBlockState iblockstate = this.entity.world.getBlockState(this.gatePosition);

        if (this.getBlockGate(gatePosition) != null && iblockstate.getValue(BlockFenceGate.OPEN) != open) {
            iblockstate = iblockstate.withProperty(BlockFenceGate.OPEN, open);
            this.entity.world.setBlockState(gatePosition, iblockstate, 10);
            this.entity.world.playEvent(null, iblockstate.getValue(BlockFenceGate.OPEN) ? 1008 : 1014, this.gatePosition, 0);
        }
    }
}
