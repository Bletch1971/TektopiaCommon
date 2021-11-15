package bletch.common.entities.ai;

import net.minecraft.util.math.BlockPos;
import net.tangotek.tektopia.entities.EntityVillagerTek;
import net.tangotek.tektopia.entities.ai.EntityAIPatrolPoint;
import net.tangotek.tektopia.structures.VillageStructure;
import net.tangotek.tektopia.structures.VillageStructureType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class EntityAIWanderVillage extends EntityAIPatrolPoint {
    protected final EntityVillagerTek entity;
    protected VillageStructure structure;

    public EntityAIWanderVillage(EntityVillagerTek entity, Predicate<EntityVillagerTek> shouldPred, int distanceFromPoint, int waitTime) {
        super(entity, shouldPred, distanceFromPoint, waitTime);
        this.entity = entity;
    }

    @Override
    public boolean shouldExecute() {
        return this.villager.isAITick() && this.navigator.hasVillage() && this.shouldPred.test(this.villager) && super.shouldExecute();
    }

    protected BlockPos getPatrolPoint() {
        List<VillageStructure> structures = new ArrayList<>();
        structures.addAll(this.villager.getVillage().getStructures(VillageStructureType.BLACKSMITH));
        structures.addAll(this.villager.getVillage().getStructures(VillageStructureType.MERCHANT_STALL));

        if (structures.isEmpty())
            return null;

        Collections.shuffle(structures);
        this.structure = structures.get(0);
        return this.structure != null ? this.structure.getDoor() : null;
    }
}
