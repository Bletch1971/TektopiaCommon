package bletch.common.entities.renders;

import com.leviathanstudio.craftstudio.client.model.ModelCraftStudio;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.tangotek.tektopia.client.LayerVillagerHeldItem;
import net.tangotek.tektopia.entities.EntityVillagerTek;

public class RenderVillager<T extends EntityVillagerTek> extends RenderLiving<T> {
    protected final String modId;
    protected final String textureName;
    protected final ModelCraftStudio maleModel;
    protected final ModelCraftStudio femaleModel;
    protected ResourceLocation[] maleTextures;
    protected ResourceLocation[] femaleTextures;

    public RenderVillager(RenderManager manager, String modId, String modelName, boolean hasGenderModels, int textureWidth, int textureHeight, String textureName) {
        this(manager, modId, modelName, hasGenderModels, textureWidth, textureHeight, textureName, 0.4F);
    }

    public RenderVillager(RenderManager manager, String modId, String modelName, boolean hasGenderModels, int textureWidth, int textureHeight, String textureName, float shadowSize) {
        super(manager, new ModelCraftStudio(modId, modelName + "_m", textureWidth, textureHeight), shadowSize);

        this.addLayer(new LayerVillagerHeldItem(this));
        this.modId = modId;
        this.textureName = textureName;
        this.maleModel = (ModelCraftStudio) this.mainModel;
        if (hasGenderModels) {
            this.femaleModel = new ModelCraftStudio(modId, modelName + "_f", textureWidth, textureHeight);
        } else {
            this.femaleModel = null;
        }

        this.setupTextures();
    }

    protected void setupTextures() {
        this.maleTextures = new ResourceLocation[]{new ResourceLocation(this.modId, "textures/entity/" + this.textureName + "_m.png")};
        this.femaleTextures = new ResourceLocation[]{new ResourceLocation(this.modId, "textures/entity/" + this.textureName + "_f.png")};
    }

    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (!entity.isMale() && this.femaleModel != null) {
            this.mainModel = this.femaleModel;
        } else {
            this.mainModel = this.maleModel;
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return entity.isMale() ? this.maleTextures[0] : this.femaleTextures[0];
    }

    protected void applyRotations(T entityLiving, float p_77043_2_, float rotationYaw, float partialTicks) {
        if (entityLiving.getForceAxis() >= 0) {
            GlStateManager.rotate((float) (entityLiving.getForceAxis() * -90), 0.0F, 1.0F, 0.0F);
        } else {
            super.applyRotations(entityLiving, p_77043_2_, rotationYaw, partialTicks);
        }
    }

}
