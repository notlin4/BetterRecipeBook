package marsh.town.brb.Mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import marsh.town.brb.BetterRecipeBook;
import marsh.town.brb.BrewingStand.RecipeBookWidget;
import marsh.town.brb.EnchantingTable.EnchantingRecipeBookWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantingTable extends AbstractContainerScreen<EnchantmentMenu> {
    private final EnchantingRecipeBookWidget recipeBook = new EnchantingRecipeBookWidget();
    private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");
    private boolean narrow;

    public EnchantingTable(EnchantmentMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    protected void init(CallbackInfo ci) {
        if (BetterRecipeBook.config.enableBook) {
            this.narrow = this.width < 379;
            System.out.println(this.width);
            System.out.println(this.height);
            this.recipeBook.initialize(this.width, this.height, Minecraft.getInstance(), narrow, this.menu);

            if (!BetterRecipeBook.config.keepCentered) {
                this.leftPos = this.recipeBook.findLeftEdge(this.width, this.imageWidth);
            }

            this.addRenderableWidget(new ImageButton(this.leftPos + 135, this.height / 2 - 50, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (button) -> {
                this.recipeBook.toggleOpen();
                BetterRecipeBook.rememberedEnchantingOpen = this.recipeBook.isOpen();
                if (!BetterRecipeBook.config.keepCentered) {
                    this.leftPos = this.recipeBook.findLeftEdge(this.width, this.imageWidth);
                }
                ((ImageButton)button).setPosition(this.leftPos + 135, this.height / 2 - 50);
            }));

            this.addWidget(this.recipeBook);
            this.setInitialFocus(this.recipeBook);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void render(PoseStack poseStack, int i, int j, float f, CallbackInfo ci) {
        this.recipeBook.render(poseStack, i, j, f);
        super.render(poseStack, i, j, f);
        this.recipeBook.drawGhostSlots(poseStack, this.leftPos, this.topPos, false, f);

        this.renderTooltip(poseStack, i, j);
        this.recipeBook.drawTooltip(poseStack, this.leftPos, this.topPos, i, j);
    }

    @ModifyArg(
            method = "renderBg",
            index = 1,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/EnchantmentScreen;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V"
            )
    )
    public int drawBackground(int i) {
        if (this.recipeBook.isOpen() && !BetterRecipeBook.config.keepCentered) {
            return i + 77;
        } else {
            return i;
        }
    }
}
