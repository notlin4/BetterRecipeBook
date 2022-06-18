package marsh.town.brb.EnchantingTable;

import marsh.town.brb.BrewingStand.RecipeBookGroup;
import marsh.town.brb.BrewingStand.Result;
import marsh.town.brb.Loaders.PotionLoader;
import net.minecraft.core.Registry;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

public class EnchantingClientRecipeBook extends RecipeBook {
    private boolean filteringCraftable;

    public boolean isFilteringCraftable() {
        return filteringCraftable;
    }

    public List<EnchantingResult> getResultsForCategory() {
        ArrayList<EnchantingResult> results = new ArrayList<EnchantingResult>();

        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            results.add(new EnchantingResult(enchantment));
        }

        return results;
    }

    public boolean isFiltering(RecipeBookType category) {
        return filteringCraftable;
    }

    public void setFilteringCraftable(boolean filteringCraftable) {
        this.filteringCraftable = filteringCraftable;
    }
}
