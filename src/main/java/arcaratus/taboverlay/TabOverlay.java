package arcaratus.taboverlay;

import net.minecraft.client.Minecraft;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.Locale;

@Mod(modid = TabOverlay.MOD_ID, name = TabOverlay.NAME, version = TabOverlay.VERSION, clientSideOnly = true, guiFactory = "arcaratus.taboverlay.GuiOverlayConfig$Factory")
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = TabOverlay.MOD_ID)
public class TabOverlay
{
    public static final String MOD_ID = "taboverlay";
    public static final String NAME = "Tab Overlay";
    public static final String VERSION = "@VERSION@";

    private static final ResourceLocation OVERLAY = new ResourceLocation(MOD_ID, "textures/gui/overlay.png");

    private static final int width = 176;
    private static final int height = 166;

    public static float zLevel = 0;

    public static final KeyBinding KEY_OVERLAY = new KeyBindingOverlay();

    @SubscribeEvent
    public static void onDrawScreenPost(RenderGameOverlayEvent.Post event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc);
        EntityPlayer player = mc.player;

        if (mc.currentScreen == null && Keyboard.isKeyDown(KEY_OVERLAY.getKeyCode()))
        {
            int i = (res.getScaledWidth() - width) / 2;
            int j = (res.getScaledHeight() - height) / 2;

            GlStateManager.pushMatrix();
            mc.getTextureManager().bindTexture(OVERLAY);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.color(1.0F, 1.0F, 1.0F, (float) ConfigHandler.overlayOpacity);
            drawTexturedModalRect(i, j, 0, 0, width, height);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();

            GlStateManager.disableRescaleNormal();
//            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)i, (float)j, 0.0F);

            GlStateManager.enableAlpha();
            GlStateManager.enableRescaleNormal();

            for (int i1 = 0; i1 < player.inventoryContainer.inventorySlots.size(); ++i1)
            {
                Slot slot = player.inventoryContainer.inventorySlots.get(i1);

                if (slot.isEnabled())
                {
                    drawSlot(mc, player, slot);
                }
            }

            GlStateManager.disableAlpha();
            GlStateManager.disableRescaleNormal();

            GlStateManager.popMatrix();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
//            RenderHelper.enableStandardItemLighting();
        }
    }

    private static void drawSlot(Minecraft mc, EntityPlayer player, Slot slotIn)
    {
        int i = slotIn.xPos;
        int j = slotIn.yPos;
        ItemStack itemstack = slotIn.getStack();
        RenderItem itemRender = mc.getRenderItem();
        boolean flag1 = false;

        if (itemstack.isEmpty() && slotIn.isEnabled())
        {
            TextureAtlasSprite textureatlassprite = slotIn.getBackgroundSprite();

            if (textureatlassprite != null)
            {
                GlStateManager.disableLighting();
                mc.getTextureManager().bindTexture(slotIn.getBackgroundLocation());
                drawTexturedModalRect(i, j, textureatlassprite, 16, 16);
                GlStateManager.enableLighting();
                flag1 = true;
            }
        }

        if (!flag1)
        {
            GlStateManager.enableDepth();
            itemRender.renderItemAndEffectIntoGUI(player, itemstack, i, j);
            itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, itemstack, i, j, null);
        }
    }

    private static void drawTexturedModalRect(int xCoord, int yCoord, TextureAtlasSprite textureSprite, int widthIn, int heightIn)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)(xCoord ), (double)(yCoord + heightIn), (double)zLevel).tex((double)textureSprite.getMinU(), (double)textureSprite.getMaxV()).endVertex();
        bufferbuilder.pos((double)(xCoord + widthIn), (double)(yCoord + heightIn), (double)zLevel).tex((double)textureSprite.getMaxU(), (double)textureSprite.getMaxV()).endVertex();
        bufferbuilder.pos((double)(xCoord + widthIn), (double)(yCoord), (double)zLevel).tex((double)textureSprite.getMaxU(), (double)textureSprite.getMinV()).endVertex();
        bufferbuilder.pos((double)(xCoord), (double)(yCoord), (double)zLevel).tex((double)textureSprite.getMinU(), (double)textureSprite.getMinV()).endVertex();
        tessellator.draw();
    }

    private static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
        float f = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)(x), (double)(y + height), (double)zLevel).tex((double)((float)(textureX) * f), (double)((float)(textureY + height) * f)).endVertex();
        bufferbuilder.pos((double)(x + width), (double)(y + height), (double)zLevel).tex((double)((float)(textureX + width) * f), (double)((float)(textureY + height) * f)).endVertex();
        bufferbuilder.pos((double)(x + width), (double)(y), (double)zLevel).tex((double)((float)(textureX + width) * f), (double)((float)(textureY) * f)).endVertex();
        bufferbuilder.pos((double)(x), (double)(y), (double)zLevel).tex((double)((float)(textureX) * f), (double)((float)(textureY) * f)).endVertex();
        tessellator.draw();
    }

    @SideOnly(Side.CLIENT)
    public static class KeyBindingOverlay extends KeyBinding
    {
        public KeyBindingOverlay()
        {
            super(MOD_ID + ".keybind.overlay".toLowerCase(Locale.ENGLISH), KeyConflictContext.IN_GAME, KeyModifier.NONE, Keyboard.KEY_TAB, NAME);
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

        @Config.Comment({ "Toggle the opacity of the overlay." })
        @Config.RangeDouble(min = 0, max = 1)
        public static double overlayOpacity = 0.05;
    }
}
