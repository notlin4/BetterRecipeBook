package marsh.town.brb.EnchantingTable;

import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantingResult {
    public Enchantment enchantment;
    public ItemStack representation;

    public EnchantingResult(Enchantment enchantment) {
        this.enchantment = enchantment;
        this.representation = new ItemStack(Items.ENCHANTED_BOOK);
    }

    public boolean hasItem(EnchantmentMenu handledScreen) {
        for (Slot slot : handledScreen.slots) {
            if (enchantment.canEnchant(slot.getItem())) return true;
        }

        return false;
    }
}
