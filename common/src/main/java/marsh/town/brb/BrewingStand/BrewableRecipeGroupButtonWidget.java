package marsh.town.brb.BrewingStand;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@Environment(EnvType.CLIENT)
public class BrewableRecipeGroupButtonWidget extends StateSwitchingButton {
    private final BrewingRecipeBookGroup group;

    public BrewableRecipeGroupButtonWidget(BrewingRecipeBookGroup category) {
        super(0, 0, 35, 27, false);
        this.group = category;
        this.initTextureValues(153, 2, 35, 0, BrewingRecipeBookComponent.RECIPE_BOOK_LOCATION);
    }

    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        Minecraft minecraftClient = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.resourceLocation);
        RenderSystem.disableDepthTest();
        int i = this.xTexStart;
        int j = this.yTexStart;
        if (this.isStateTriggered) {
            i += this.xDiffTex;
        }

        if (this.isHoveredOrFocused()) {
            j += this.yDiffTex;
        }

        int k = getX();
        if (this.isStateTriggered) {
            k -= 2;
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(resourceLocation, k, getY(), i, j, this.width, this.height);
        RenderSystem.enableDepthTest();
        this.renderIcons(guiGraphics, minecraftClient.getItemRenderer());
    }

    private void renderIcons(GuiGraphics guiGraphics, ItemRenderer itemRenderer) {
        List<ItemStack> list = this.group.getIcons();
        int i = this.isStateTriggered ? -2 : 0;
        if (list.size() == 1) {
            guiGraphics.renderFakeItem(list.get(0), getX() + 9 + i, getY() + 5);
        } else if (list.size() == 2) {
            guiGraphics.renderFakeItem(list.get(0), getX() + 3 + i, getY() + 5);
            guiGraphics.renderFakeItem(list.get(1), getX() + 14 + i, getY() + 5);
        }

    }

    public BrewingRecipeBookGroup getGroup() {
        return this.group;
    }
}
