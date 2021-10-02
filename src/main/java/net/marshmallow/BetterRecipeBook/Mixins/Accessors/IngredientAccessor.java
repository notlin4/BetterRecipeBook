package net.marshmallow.BetterRecipeBook.Mixins.Accessors;

import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Ingredient.class)
public interface IngredientAccessor {
    @Accessor("entries")
    Ingredient.Entry[] getEntries();
}
