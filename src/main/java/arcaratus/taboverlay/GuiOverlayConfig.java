package arcaratus.taboverlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

import java.util.Set;

public class GuiOverlayConfig extends GuiConfig
{
    public GuiOverlayConfig(GuiScreen parentScreen)
    {
        super(parentScreen, ConfigElement.from(TabOverlay.ConfigHandler.class).getChildElements(), TabOverlay.MOD_ID, false, false, TabOverlay.NAME);
    }

    public static class Factory implements IModGuiFactory
    {
        @Override
        public void initialize(Minecraft minecraftInstance) {}

        @Override
        public boolean hasConfigGui()
        {
            return true;
        }

        @Override
        public GuiScreen createConfigGui(GuiScreen parentScreen)
        {
            return new GuiOverlayConfig(parentScreen);
        }

        @Override
        public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
        {
            return null;
        }
    }
}
