package marsh.town.brb.EnchantingTable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import marsh.town.brb.BetterRecipeBook;
import marsh.town.brb.BrewingStand.RecipeBookGroup;
import marsh.town.brb.BrewingStand.Result;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.List;

import static marsh.town.brb.BrewingStand.PlatformPotionUtil.getIngredient;

@Environment(EnvType.CLIENT)
public class EnchantingAnimatedResultButton extends AbstractWidget {
    private float time;
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("textures/gui/recipe_book.png");
    private EnchantingResult potionRecipe;
    private RecipeBookGroup group;
    private EnchantmentMenu brewingStandScreenHandler;

    public EnchantingAnimatedResultButton() {
        super(0, 0, 25, 25, TextComponent.EMPTY);
    }

    public void showEnchantment(EnchantingResult potionRecipe, RecipeBookGroup group, EnchantmentMenu brewingStandScreenHandler) {
        this.potionRecipe = potionRecipe;
        this.group = group;
        this.brewingStandScreenHandler = brewingStandScreenHandler;
    }

    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (!Screen.hasControlDown()) {
            this.time += delta;
        }

        Minecraft minecraftClient = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        int i;
        int j;

//        if (BetterRecipeBook.config.enablePinning && BetterRecipeBook.pinnedRecipeManager.hasPotion(potionRecipe.enchantment)) {
//            RenderSystem.setShaderTexture(0, new ResourceLocation("brb:textures/gui/pinned.png"));
//            i = 25;
//            j = 0;
//        } else {
            RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
            i = 29 + 25;
            j = 206;
//        }

        if (potionRecipe.hasItem(brewingStandScreenHandler)) {
            i -= 25;
        }

        PoseStack matrixStack = RenderSystem.getModelViewStack();
        this.blit(matrices, this.x, this.y, i, j, this.width, this.height);
        int k = 4;

        matrixStack.pushPose();
        matrixStack.mulPoseMatrix(matrices.last().pose().copy()); // No idea what this does
        minecraftClient.getItemRenderer().renderAndDecorateItem(potionRecipe.representation, this.x + k, this.y + k); // Why do we do this twice?
        minecraftClient.getItemRenderer().renderGuiItemDecorations(Minecraft.getInstance().font, potionRecipe.representation, this.x + k, this.y + k); // ^
        RenderSystem.enableDepthTest();
        matrixStack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.disableDepthTest();
    }

    public EnchantingResult getRecipe() {
        return potionRecipe;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void updateNarration(NarrationElementOutput builder) {
        ItemStack inputStack = this.potionRecipe.representation;

        builder.add(NarratedElementType.TITLE, new TranslatableComponent("narration.recipe", inputStack.getHoverName()));
        builder.add(NarratedElementType.USAGE, new TranslatableComponent("narration.button.usage.hovered"));
    }

    public List<Component> getTooltip() {
        List<Component> list = Lists.newArrayList();

        list.add(potionRecipe.representation.getHoverName());
        PotionUtils.addPotionTooltip(potionRecipe.representation, list, 1);
        list.add(new TextComponent(""));

        ChatFormatting colour = ChatFormatting.DARK_GRAY;
        if (potionRecipe.hasItem(brewingStandScreenHandler)) {
            colour = ChatFormatting.WHITE;
        }

//        list.add(new TextComponent(getIngredient(potionRecipe.representation).getItems()[0].getHoverName().getString()).withStyle(colour));

        list.add(new TextComponent("↓").withStyle(ChatFormatting.DARK_GRAY));

        ItemStack inputStack = this.potionRecipe.representation;

        if (!potionRecipe.hasItem(brewingStandScreenHandler)) {
            colour = ChatFormatting.DARK_GRAY;
        }

        list.add(new TextComponent(inputStack.getHoverName().getString()).withStyle(colour));

//        if (BetterRecipeBook.config.enablePinning) {
//            if (BetterRecipeBook.pinnedRecipeManager.hasPotion(this.potionRecipe.representation)) {
//                list.add(new TranslatableComponent("brb.gui.pin.remove"));
//            } else {
//                list.add(new TranslatableComponent("brb.gui.pin.add"));
//            }
//        }

        return list;
    }
}
