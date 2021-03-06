package bletch.common.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

public class RenderUtils {

	@SideOnly(Side.CLIENT)
	public static void renderEntity(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) posX, (float) posY, 50.0F);
		GlStateManager.scale((float) (-scale), (float) scale, (float) scale);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);

		float f = ent.renderYawOffset;
		float f1 = ent.rotationYaw;
		float f2 = ent.rotationPitch;
		float f3 = ent.prevRotationYawHead;
		float f4 = ent.rotationYawHead;

		GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();

		ent.rotationYaw = (float) Math.atan(mouseX / 40.0F) * 40.0F;
		ent.rotationPitch = -((float) Math.atan(mouseY / 40.0F)) * 20.0F;
		ent.rotationYawHead = ent.rotationYaw;
		ent.prevRotationYawHead = ent.rotationYaw;

		GlStateManager.translate(0.0F, 0.0F, 0.0F);

		RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
		rendermanager.setPlayerViewY(180.0F);
		rendermanager.setRenderShadow(false);
		rendermanager.renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 0F, false);
		rendermanager.setRenderShadow(true);

		ent.renderYawOffset = f;
		ent.rotationYaw = f1;
		ent.rotationPitch = f2;
		ent.prevRotationYawHead = f3;
		ent.rotationYawHead = f4;

		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		GlStateManager.popMatrix();

		RenderHelper.disableStandardItemLighting();

		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	@SideOnly(Side.CLIENT)
	public static void renderItemIntoGUI(ItemStack stack, int x, int y) {
		renderItemIntoGUI(Minecraft.getMinecraft().getRenderItem(), stack, x, y);
	}

	@SideOnly(Side.CLIENT)
	public static void renderItemIntoGUI(RenderItem renderItem, ItemStack stack, int x, int y) {
		GlStateManager.pushMatrix();
		renderItem.renderItemIntoGUI(stack, x, y);
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();
	}

	@SideOnly(Side.CLIENT)
	public static void renderItemAndEffectIntoGUI(ItemStack stack, int x, int y) {
		renderItemAndEffectIntoGUI(Minecraft.getMinecraft().getRenderItem(), stack, x, y, false);
	}

	@SideOnly(Side.CLIENT)
	public static void renderItemAndEffectIntoGUI(RenderItem renderItem, ItemStack stack, int x, int y) {
		renderItemAndEffectIntoGUI(renderItem, stack, x, y, false);
	}

	@SideOnly(Side.CLIENT)
	public static void renderItemAndEffectIntoGUI(RenderItem renderItem, ItemStack stack, int x, int y, Boolean resetZLevel) {
		if (resetZLevel)
			renderItem.zLevel -= 150.0F;
		GlStateManager.pushMatrix();
		renderItem.renderItemAndEffectIntoGUI(stack, x, y);
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();
		if (resetZLevel)
			renderItem.zLevel += 150.0F;
	}

	@SideOnly(Side.CLIENT)
	/**
	 * Renders the stack size and/or damage bar for the given ItemStack.
	 */
	 public static void renderItemOverlayIntoGUI(FontRenderer fontRenderer, ItemStack stack, int x, int y) {
		 renderItemOverlayIntoGUI(Minecraft.getMinecraft().getRenderItem(), fontRenderer, stack, x, y);
	 }

	@SideOnly(Side.CLIENT)
	/**
	 * Renders the stack size and/or damage bar for the given ItemStack.
	 */
	public static void renderItemOverlayIntoGUI(RenderItem renderItem, FontRenderer fontRenderer, ItemStack stack, int x, int y) {
		GlStateManager.pushMatrix();
		renderItem.renderItemOverlays(fontRenderer, stack, x, y);
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();
	}

	@SideOnly(Side.CLIENT)
	/**
	 * Renders the stack size and/or damage bar for the given ItemStack.
	 */
	public static void renderItemOverlayIntoGUI(FontRenderer fontRenderer, ItemStack stack, int x, int y, @Nullable String text) {
		renderItemOverlayIntoGUI(Minecraft.getMinecraft().getRenderItem(), fontRenderer, stack, x, y, text);
	}

	@SideOnly(Side.CLIENT)
	/**
	 * Renders the stack size and/or damage bar for the given ItemStack.
	 */
	public static void renderItemOverlayIntoGUI(RenderItem renderItem, FontRenderer fontRenderer, ItemStack stack, int x, int y, @Nullable String text) {
		GlStateManager.pushMatrix();
		renderItem.renderItemOverlayIntoGUI(fontRenderer, stack, x, y, text);
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();
	}

	public static void drawModalRectWithCustomSizedTextureWithZLevel(ResourceLocation resource, float zLevel, int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight) {
		drawModalRectWithCustomSizedTextureWithZLevel(Minecraft.getMinecraft().getTextureManager(), resource, zLevel, x, y, u, v, width, height, textureWidth, textureHeight);
	}
	
	public static void drawModalRectWithCustomSizedTextureWithZLevel(TextureManager textureManager, ResourceLocation resource, float zLevel, int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight) {
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		textureManager.bindTexture(resource);
		
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)x, (double)(y + height), (double)zLevel).tex((double)(u * f), (double)((v + (float)height) * f1)).endVertex();
        bufferbuilder.pos((double)(x + width), (double)(y + height), (double)zLevel).tex((double)((u + (float)width) * f), (double)((v + (float)height) * f1)).endVertex();
        bufferbuilder.pos((double)(x + width), (double)y, (double)zLevel).tex((double)((u + (float)width) * f), (double)(v * f1)).endVertex();
        bufferbuilder.pos((double)x, (double)y, (double)zLevel).tex((double)(u * f), (double)(v * f1)).endVertex();
        tessellator.draw();
        
        GlStateManager.popMatrix();
    }

}
