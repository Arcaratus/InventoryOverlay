package arcaratus.inventoryoverlay;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

@Mod(InventoryOverlay.MOD_ID)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = InventoryOverlay.MOD_ID)
public class InventoryOverlay
{
    public static final String MOD_ID = "inventoryoverlay";
    public static final String NAME = "Inventory Overlay";
    public static final String VERSION = "1.2.0";

    private static final boolean baublesLoaded = false;//isModLoaded("baubles");

    private static final ResourceLocation OVERLAY = new ResourceLocation(MOD_ID, "textures/gui/overlay.png");
    private static final ResourceLocation BAUBLES_OVERLAY = new ResourceLocation(MOD_ID, "textures/gui/overlay_baubles.png");

    private static final int width = 176;
    private static final int height = 166;

    public static float zLevel = 0;

    public static final KeyBinding KEY_OVERLAY = new KeyBindingOverlay();

    public static final ConfigHandler CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;
    static
    {
        final Pair<ConfigHandler, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ConfigHandler::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public InventoryOverlay()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    @SubscribeEvent
    public static void onDrawScreenPost(RenderGameOverlayEvent.Post event)
    {
        Minecraft mc = Minecraft.getInstance();
        MainWindow res = event.getWindow();
        PlayerEntity player = mc.player;
        MatrixStack matrixStack = event.getMatrixStack();
        boolean baublesOverlay = baublesLoaded && ConfigHandler.baublesOverlay.get();

        if (mc.currentScreen == null && KEY_OVERLAY.isKeyDown())
        {
            int i = (res.getScaledWidth() - width) / 2;
            int j = (res.getScaledHeight() - height) / 2;

            zLevel = -1000;

            // Draw background image
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.param, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.param, GlStateManager.SourceFactor.ONE.param, GlStateManager.DestFactor.ZERO.param);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, ConfigHandler.overlayOpacity.get().floatValue());

            RenderSystem.pushMatrix();
            mc.getTextureManager().bindTexture(OVERLAY);
            drawTexturedModalRect(i + ConfigHandler.xOffset.get(), j + ConfigHandler.yOffset.get(), 0, 0, width, height);
            RenderSystem.popMatrix();

            // Draw foreground
            RenderSystem.disableRescaleNormal();
            RenderSystem.disableDepthTest();
            RenderHelper.enableStandardItemLighting();

            RenderSystem.pushMatrix();
            RenderSystem.translatef((float) i, (float) j, 0);

            RenderSystem.enableRescaleNormal();
//            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

            ItemRenderer itemRenderer = mc.getItemRenderer();
            zLevel = 200.0F;
            itemRenderer.zLevel = 200.0F;
            for (Slot slot : player.container.inventorySlots)
                if (slot.isEnabled() && !slot.getStack().isEmpty())
                    drawSlot(mc, player, slot, itemRenderer, matrixStack);

            itemRenderer.zLevel = 0.0F;
            zLevel = 0.0F;
//            if (baublesOverlay)
//                drawBaublesSlots(mc, player, i, j);

            RenderSystem.disableAlphaTest();
            RenderSystem.disableRescaleNormal();

            RenderSystem.popMatrix();
            RenderSystem.enableDepthTest();
            RenderHelper.disableStandardItemLighting();

            RenderSystem.color4f(1, 1, 1, 1); // Re-renders the food bar

            mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
        }
    }

    private static void drawSlot(Minecraft mc, PlayerEntity player, Slot slot, ItemRenderer itemRenderer, MatrixStack matrixStack)
    {
        int i = slot.xPos + ConfigHandler.xOffset.get();
        int j = slot.yPos + ConfigHandler.yOffset.get();
        ItemStack itemstack = slot.getStack();
//        boolean flag1 = false;

//        if (itemstack.isEmpty() && slot.isEnabled())
//        {
//            com.mojang.datafixers.util.Pair<ResourceLocation, ResourceLocation> pair = slot.getBackground();
//            if (pair != null)
//            {
//                TextureAtlasSprite sprite = mc.getAtlasSpriteGetter(pair.getFirst()).apply(pair.getSecond());
//
//                if (sprite != null && (!baublesLoaded || !sprite.getAtlasTexture().getTextureLocation().toString().equals("minecraft:items/empty_armor_slot_shield")))
//                {
//                    zLevel = 100.0F;
//                    itemRenderer.zLevel = 100.0F;
//                    RenderSystem.disableLighting();
//                    mc.getTextureManager().bindTexture(sprite.getAtlasTexture().getTextureLocation());
//                    blit(matrixStack.getLast().getMatrix(), i, j, i + 16, j + 16, (int) zLevel, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
//                    RenderSystem.enableLighting();
//                    flag1 = true;
//                }
//            }
//        }

//        if (!flag1)
//        {
//            if (baublesLoaded && ConfigHandler.baublesOverlay.get() && slot.slotNumber == 45)
//            {
//                itemRender.renderItemAndEffectIntoGUI(player, itemstack, i + 19, j);
//                itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, itemstack, i + 19, j, null);
//            }
//            else
            {
                itemRenderer.renderItemAndEffectIntoGUI(player, itemstack, i, j);
//                itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, itemstack, i, j, null);
            }
//        }
    }

//    @Optional.Method(modid = "baubles")
//    private static void drawBaublesSlots(Minecraft mc, EntityPlayer player, int i, int j)
//    {
//        ContainerPlayerExpanded container = new ContainerPlayerExpanded(player.inventory, !player.getEntityWorld().isRemote, player);
//        for (int i1 = 0; i1 < container.inventorySlots.size(); i1++)
//        {
//            Slot slot = container.inventorySlots.get(i1);
//            if (slot.getHasStack() && slot.getSlotStackLimit() == 1)
//                drawSlot(mc, player, slot);
//        }
//    }

    private static void blit(Matrix4f matrix, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV)
    {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(matrix, (float)x1, (float)y2, (float)blitOffset).tex(minU, maxV).endVertex();
        bufferbuilder.pos(matrix, (float)x2, (float)y2, (float)blitOffset).tex(maxU, maxV).endVertex();
        bufferbuilder.pos(matrix, (float)x2, (float)y1, (float)blitOffset).tex(maxU, minV).endVertex();
        bufferbuilder.pos(matrix, (float)x1, (float)y1, (float)blitOffset).tex(minU, minV).endVertex();
        bufferbuilder.finishDrawing();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.draw(bufferbuilder);
    }

    private static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
        float f = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, zLevel).tex((float) (textureX) * f, (float) (textureY + height) * f).endVertex();
        bufferbuilder.pos(x + width, y + height, zLevel).tex((float) (textureX + width) * f, (float) (textureY + height) * f).endVertex();
        bufferbuilder.pos(x + width, y, zLevel).tex((float) (textureX + width) * f, (float) textureY * f).endVertex();
        bufferbuilder.pos(x, y, zLevel).tex((float) textureX * f, (float) textureY * f).endVertex();
        tessellator.draw();
    }

    public static class KeyBindingOverlay extends KeyBinding
    {
        public KeyBindingOverlay()
        {
            super(MOD_ID + ".keybind.overlay", KeyConflictContext.IN_GAME, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, NAME);
            ClientRegistry.registerKeyBinding(this);
        }
    }

    public static class ConfigHandler
    {
        public static ForgeConfigSpec.IntValue xOffset;
        public static ForgeConfigSpec.IntValue yOffset;
        public static ForgeConfigSpec.DoubleValue overlayOpacity;

        public static ForgeConfigSpec.BooleanValue baublesOverlay;

        public ConfigHandler(ForgeConfigSpec.Builder builder)
        {
            builder.push("general");
            xOffset = builder.comment("Adjusts the X-position of the overlay.").defineInRange("xOffset", 0, -1000, 1000);
            yOffset = builder.comment("Adjusts the Y-position of the overlay.").defineInRange("yOffset", 0, -1000, 1000);
            overlayOpacity = builder.comment("Adjusts the opacity of the overlay.").defineInRange("overlayOpacity", 0.05, 0, 1);
            baublesOverlay = builder.comment("Uses the Baubles overlay when Baubles is in the game.").define("baublesOverlay", true);
        }
    }
}
