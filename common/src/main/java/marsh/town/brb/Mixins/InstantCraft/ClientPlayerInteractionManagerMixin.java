package marsh.town.brb.Mixins.InstantCraft;

import marsh.town.brb.BetterRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "handlePlaceRecipe", at = @At("TAIL"))
    private void handlePlaceRecipe(int syncId, Recipe<?> recipe, boolean craftAll, CallbackInfo ci) {
        assert minecraft.level != null;
        BetterRecipeBook.instantCraftingManager.recipeClicked(recipe, minecraft.level.registryAccess());
    }
}