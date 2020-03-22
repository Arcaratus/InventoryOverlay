package arcaratus.inventoryoverlay;

import baubles.common.container.ContainerPlayerExpanded;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.Locale;

@Mod(modid = InventoryOverlay.MOD_ID, name = InventoryOverlay.NAME, version = InventoryOverlay.VERSION, clientSideOnly = true, dependencies = "after:baubles@[1.5.2,)", guiFactory = "arcaratus.taboverlay.GuiOverlayConfig$Factory")
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = InventoryOverlay.MOD_ID)
public class InventoryOverlay
{
    public static final String MOD_ID = "inventoryoverlay";
    public static final String NAME = "Inventory Overlay";
    public static final String VERSION = "@VERSION@";

    private static final boolean baublesLoaded = Loader.isModLoaded("baubles");

    private static final ResourceLocation OVERLAY = new ResourceLocation(MOD_ID, "textures/gui/overlay.png");
    private static final ResourceLocation BAUBLES_OVERLAY = new ResourceLocation(MOD_ID, "textures/gui/overlay_baubles.png");

    private static final int width = 176;
    private static final int height = 166;

    public static float zLevel = 0;

    public static final KeyBinding KEY_OVERLAY = new KeyBindingOverlay();

    @SubscribeEvent
    public static void onDrawScreenPost(RenderGameOverlayEvent.Post event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = event.getResolution();
        EntityPlayer player = mc.player;
        boolean baublesOverlay = baublesLoaded && ConfigHandler.baublesOverlay;

        if (mc.currentScreen == null && Keyboard.isKeyDown(KEY_OVERLAY.getKeyCode()))
        {
            int i = (res.getScaledWidth() - width) / 2;
            int j = (res.getScaledHeight() - height) / 2;

            zLevel = -1000;

            // Draw background image
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1.0F, 1.0F, 1.0F, (float) ConfigHandler.overlayOpacity);

            GlStateManager.pushMatrix();
            mc.getTextureManager().bindTexture(baublesOverlay ? BAUBLES_OVERLAY : OVERLAY);
            drawTexturedModalRect(i + ConfigHandler.xOffset, j + ConfigHandler.yOffset, 0, 0, width, height);
            GlStateManager.popMatrix();

            // Draw foreground
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableDepth();
            RenderHelper.enableGUIStandardItemLighting();

            GlStateManager.pushMatrix();
            GlStateManager.translate((float)i, (float)j, 0);

            GlStateManager.enableRescaleNormal();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

            for (int i1 = 0; i1 < player.inventoryContainer.inventorySlots.size(); ++i1)
            {
                Slot slot = player.inventoryContainer.inventorySlots.get(i1);
                if (slot.isEnabled())
                    drawSlot(mc, player, slot);
            }

            if (baublesOverlay)
                drawBaublesSlots(mc, player, i, j);

            GlStateManager.disableAlpha();
            GlStateManager.disableRescaleNormal();

            GlStateManager.popMatrix();
            GlStateManager.enableDepth();
            RenderHelper.disableStandardItemLighting();

            GlStateManager.color(1, 1, 1, 1); // Re-renders the food bar
            zLevel = 0;
            mc.getTextureManager().bindTexture(Gui.ICONS);
        }
    }

    private static void drawSlot(Minecraft mc, EntityPlayer player, Slot slotIn)
    {
        int i = slotIn.xPos + ConfigHandler.xOffset;
        int j = slotIn.yPos + ConfigHandler.yOffset;
        ItemStack itemstack = slotIn.getStack();
        RenderItem itemRender = mc.getRenderItem();
        boolean flag1 = false;

        zLevel = 100.0F;
        itemRender.zLevel = 100.0F;

        if (itemstack.isEmpty() && slotIn.isEnabled())
        {
            TextureAtlasSprite textureatlassprite = slotIn.getBackgroundSprite();

            if (textureatlassprite != null && (!baublesLoaded || !textureatlassprite.getIconName().equals("minecraft:items/empty_armor_slot_shield")))
            {
                GlStateManager.disableLighting();
                mc.getTextureManager().bindTexture(slotIn.getBackgroundLocation());
                drawTexturedModalRect(i, j, textureatlassprite, 16, 16);
                GlStateManager.enableLighting();
                flag1 = true;
            }
        }

        itemRender.zLevel = 0.0F;
        zLevel = 0.0F;

        if (!flag1)
        {
            zLevel = 200.0F;
            itemRender.zLevel = 200.0F;

            if (baublesLoaded && ConfigHandler.baublesOverlay && slotIn.slotNumber == 45)
            {
                itemRender.renderItemAndEffectIntoGUI(player, itemstack, i + 19, j);
                itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, itemstack, i + 19, j, null);
            }
            else
            {
                itemRender.renderItemAndEffectIntoGUI(player, itemstack, i, j);
                itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, itemstack, i, j, null);
            }

            zLevel = 0.0F;
            itemRender.zLevel = 0.0F;
        }
    }

    @Optional.Method(modid = "baubles")
    private static void drawBaublesSlots(Minecraft mc, EntityPlayer player, int i, int j)
    {
        ContainerPlayerExpanded container = new ContainerPlayerExpanded(player.inventory, !player.getEntityWorld().isRemote, player);
        for (int i1 = 0; i1 < container.inventorySlots.size(); i1++)
        {
            Slot slot = container.inventorySlots.get(i1);
            if (slot.getHasStack() && slot.getSlotStackLimit()==1)
                drawSlot(mc, player, slot);
        }
    }

    private static void drawTexturedModalRect(int xCoord, int yCoord, TextureAtlasSprite textureSprite, int widthIn, int heightIn)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(xCoord, yCoord + heightIn, zLevel).tex(textureSprite.getMinU(), textureSprite.getMaxV()).endVertex();
        bufferbuilder.pos(xCoord + widthIn, yCoord + heightIn, zLevel).tex(textureSprite.getMaxU(), textureSprite.getMaxV()).endVertex();
        bufferbuilder.pos(xCoord + widthIn, yCoord, zLevel).tex(textureSprite.getMaxU(), textureSprite.getMinV()).endVertex();
        bufferbuilder.pos(xCoord, yCoord, zLevel).tex(textureSprite.getMinU(), textureSprite.getMinV()).endVertex();
        tessellator.draw();
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

    @SideOnly(Side.CLIENT)
    public static class KeyBindingOverlay extends KeyBinding
    {
        public KeyBindingOverlay()
        {
            super(MOD_ID + ".keybind.overlay".toLowerCase(Locale.ENGLISH), KeyConflictContext.IN_GAME, KeyModifier.NONE, Keyboard.KEY_GRAVE, NAME);
            ClientRegistry.registerKeyBinding(this);
        }
    }

    @Config(modid = MOD_ID, name = NAME + "/" + MOD_ID)
    @Mod.EventBusSubscriber(modid = MOD_ID)
    public static class ConfigHandler
    {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if (event.getModID().equals(MOD_ID))
                ConfigManager.sync(event.getModID(), Config.Type.INSTANCE);
        }

        @Config.Comment({ "Adjust the X-position of the overlay." })
        @Config.RangeInt(min = -1000, max = 1000)
        public static int xOffset = 0;

        @Config.Comment({ "Adjust the Y-position of the overlay." })
        @Config.RangeInt(min = -1000, max = 1000)
        public static int yOffset = 0;

        @Config.Comment({ "Toggle the opacity of the overlay." })
        @Config.RangeDouble(min = 0, max = 1)
        public static double overlayOpacity = 0.05;

        @Config.Comment({ "Disable the Baubles overlay." })
        public static boolean baublesOverlay = true;
    }
}
