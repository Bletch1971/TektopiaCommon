package bletch.common.entities.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.tangotek.tektopia.entities.EntityVillageNavigator;
import net.tangotek.tektopia.pathing.BasePathingNode;
import net.tangotek.tektopia.pathing.PathNavigateVillager2;
import net.tangotek.tektopia.structures.VillageStructure;

public abstract class EntityAIMoveToBlock extends EntityAIBase {
    protected static int STUCK_TIME = 40;
    protected static int MAX_STUCK_COUNT = 1000;

    protected final EntityVillageNavigator navigator;
    protected BlockPos destinationPos;
    protected BlockPos walkPos;
    protected int pathUpdateTick = 20;
    protected boolean arrived = false;
    protected int stuckCheck;
    protected Vec3d stuckPos;
    protected boolean stuck;
    protected int lastPathIndex;
    protected int stuckCount = 0;

    public EntityAIMoveToBlock(EntityVillageNavigator navigator) {
        this.navigator = navigator;
        this.stuckCheck = STUCK_TIME;
        this.stuckPos = Vec3d.ZERO;
        this.stuck = false;
        this.lastPathIndex = -1;
        this.setMutexBits(1);
    }

    public boolean shouldExecute() {
        if (this.navigator.hasVillage() && this.navigator.getNavigator() instanceof PathNavigateVillager2 && this.canNavigate()) {
            this.destinationPos = this.getDestinationBlock();

            if (this.destinationPos != null) {
                this.stuck = false;
                this.stuckPos = new Vec3d(0.0D, -400.0D, 0.0D);
                this.arrived = false;
                this.pathUpdateTick = 40;
                this.doMove();

                return !this.stuck;
            }
        }

        return false;
    }

    public void startExecuting() {
        this.updateMovementMode();
    }

    public boolean shouldContinueExecuting() {
    	return !this.arrived && !this.stuck && this.navigator.canNavigate();
    }

    public void updateTask() {
        --this.pathUpdateTick;
        if (this.pathUpdateTick <= 0 && !this.arrived) {
            this.pathUpdateTick = 40;
            this.navigator.updateMovement(this.arrived);
        }

        if (!this.arrived && this.isNearWalkPos()) {
            this.arrived = true;
            this.navigator.getNavigator().clearPath();
            this.onArrival();
        }

        this.updateFacing();
        if (!this.arrived) {

            if (this.navigator.getNavigator().noPath()) {
                this.doMove();
            } else {

                int pathIndex = this.navigator.getNavigator().getPath().getCurrentPathIndex();
                if (this.lastPathIndex != pathIndex) {
                    this.lastPathIndex = pathIndex;
                }
            }

            --this.stuckCheck;
            if (this.stuckCheck < 0) {
                this.stuckCheck = STUCK_TIME;

                if (!this.navigator.getNavigator().noPath()) {
                    this.stuck = this.navigator.getPositionVector().squareDistanceTo(this.stuckPos) < 1.0D;
                    this.stuckPos = this.navigator.getPositionVector();
                }
            }

            if (this.stuck) {
                this.stuckCount = Math.min(MAX_STUCK_COUNT, ++this.stuckCount);

                if (this.stuckCount < MAX_STUCK_COUNT && this.attemptStuckFix() && this.lastPathIndex >= 0) {
                    this.navigator.getNavigator().clearPath();
                    this.doMove();
                } else {
                    this.onStuck();
                }
            }
        }
    }

    public void resetTask() {
        super.resetTask();
        this.arrived = false;
        this.stuckCheck = STUCK_TIME;
        this.navigator.resetMovement();
    }

    protected boolean attemptStuckFix() {
        return false;
    }

    protected boolean canNavigate() {
        return this.navigator.onGround;
    }

    protected void doMove() {
        this.arrived = false;
        this.stuckCheck = STUCK_TIME;
        this.walkPos = this.findWalkPos();

        if (this.walkPos == null) {
            this.stuck = true;

        } else if (!this.isNearWalkPos() && this.canNavigate()) {
            boolean pathFound = this.navigator.getNavigator().tryMoveToXYZ(this.walkPos.getX(), this.walkPos.getY(), this.walkPos.getZ(), this.navigator.getAIMoveSpeed());

            if (pathFound) {
                this.navigator.getLookHelper().setLookPosition(this.walkPos.getX(), this.walkPos.getY(), this.walkPos.getZ(), 50.0F, (float) this.navigator.getVerticalFaceSpeed());
            } else {
                this.onPathFailed(this.walkPos);
            }
        }
    }

    protected BlockPos findWalkPos() {
        final BlockPos pos = this.destinationPos;
        final BlockPos diff = this.navigator.getPosition().subtract(pos);
        final EnumFacing facing = EnumFacing.getFacingFromVector((float) diff.getX(), 0.0f, (float) diff.getZ());

        BlockPos testPos = pos.offset(facing);
        if (this.isWalkable(testPos, this.navigator)) {
            return testPos;
        }
        testPos = pos.offset(facing).offset(facing.rotateY());
        if (this.isWalkable(testPos, this.navigator)) {
            return testPos;
        }
        testPos = pos.offset(facing).offset(facing.rotateYCCW());
        if (this.isWalkable(testPos, this.navigator)) {
            return testPos;
        }
        testPos = pos.offset(facing.rotateY());
        if (this.isWalkable(testPos, this.navigator)) {
            return testPos;
        }
        testPos = pos.offset(facing.rotateYCCW());
        if (this.isWalkable(testPos, this.navigator)) {
            return testPos;
        }
        testPos = pos.offset(facing.getOpposite());
        if (this.isWalkable(testPos, this.navigator)) {
            return testPos;
        }
        testPos = pos.offset(facing.getOpposite()).offset(facing.rotateY());
        if (this.isWalkable(testPos, this.navigator)) {
            return testPos;
        }
        testPos = pos.offset(facing.getOpposite()).offset(facing.rotateYCCW());
        if (this.isWalkable(testPos, this.navigator)) {
            return testPos;
        }
        if (this.isWalkable(pos, this.navigator)) {
            return pos;
        }
        return null;
    }

    protected abstract BlockPos getDestinationBlock();

    public BlockPos getWalkPos() {
        return this.walkPos;
    }

    protected boolean hasArrived() {
        return this.arrived;
    }

    protected boolean isNearDestination(double range) {
        return this.destinationPos.distanceSq(this.navigator.getPosition()) < range * range;
    }

    protected boolean isNearWalkPos() {
        return this.walkPos != null && this.walkPos.distanceSq(this.navigator.getPosition()) <= 1.0D;
    }

    protected boolean isWalkable(BlockPos pos, EntityVillageNavigator entity) {
        if (entity.getVillage() != null) {
            BasePathingNode baseNode = entity.getVillage().getPathingGraph().getBaseNode(pos.getX(), pos.getY(), pos.getZ());

            if (baseNode != null) {
                return !VillageStructure.isWoodDoor(entity.world, pos) && !VillageStructure.isGate(entity.world, pos);
            }
        }

        return false;
    }

    protected void onArrival() {
    }

    protected void onPathFailed(BlockPos pos) {
        this.stuck = true;
    }

    protected void onStuck() {
        this.navigator.getNavigator().clearPath();
    }

    protected void setArrived() {
        this.arrived = true;
    }

    protected void updateFacing() {
        if (!this.arrived) {
            if (!this.navigator.getNavigator().noPath()) {
                Vec3d lookPos = this.navigator.getNavigator().getPath().getCurrentPos();
                this.navigator.faceLocation(lookPos.x, lookPos.z, 4.0F);
            }
        }
    }

    protected abstract void updateMovementMode();
}
