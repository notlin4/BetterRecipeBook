package marsh.town.brb.EnchantingTable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import marsh.town.brb.BetterRecipeBook;
import marsh.town.brb.BrewingStand.*;
import marsh.town.brb.Config.Config;
import marsh.town.brb.RecipeGroupButtonWidget;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static marsh.town.brb.BrewingStand.PlatformPotionUtil.getFrom;
import static marsh.town.brb.BrewingStand.PlatformPotionUtil.getIngredient;

public class EnchantingRecipeBookWidget extends GuiComponent implements Widget, GuiEventListener, NarratableEntry {
    public static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/recipe_book.png");
    private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation("brb:textures/gui/buttons.png");
    protected EnchantmentMenu enchantmentMenu;
    Minecraft client;
    private int parentWidth;
    private int parentHeight;
    private boolean narrow;
    EnchantingClientRecipeBook recipeBook;
    private int leftOffset;
    protected final EnchantingRecipeBookGhostSlots ghostSlots = new EnchantingRecipeBookGhostSlots();
    private boolean open;
    private final EnchantingRecipeBookResults recipesArea = new EnchantingRecipeBookResults();
    @Nullable
    private EditBox searchField;
    private final StackedContents recipeFinder = new StackedContents();
    protected StateSwitchingButton toggleBrewableButton;
    private static final Component SEARCH_HINT_TEXT;
    private final List<RecipeGroupButtonWidget> tabButtons = Lists.newArrayList();
    @Nullable
    private RecipeGroupButtonWidget currentTab;
    private boolean searching;
    protected ImageButton settingsButton;
    private String searchText;
    private static final Component TOGGLE_ENCHANTABLE_RECIPES_TEXT;
    private static final Component TOGGLE_ALL_RECIPES_TEXT;
    private static final Component OPEN_SETTINGS_TEXT;
    boolean doubleRefresh = true;


    public void initialize(int parentWidth, int parentHeight, Minecraft client, boolean narrow, EnchantmentMenu enchantmentMenu) {
        this.client = client;
        this.parentWidth = parentWidth;
        this.parentHeight = parentHeight;
        this.enchantmentMenu = enchantmentMenu;
        this.narrow = narrow;
        assert client.player != null;
        client.player.containerMenu = enchantmentMenu;
        this.recipeBook = new EnchantingClientRecipeBook();
        this.open = BetterRecipeBook.rememberedEnchantingOpen;
        // this.cachedInvChangeCount = client.player.getInventory().getChangeCount();
        this.reset();

        if (BetterRecipeBook.config.keepCentered) {
            this.leftOffset = this.narrow ? 0 : 162;
        } else {
            this.leftOffset = this.narrow ? 0 : 86;
        }

        client.keyboardHandler.setSendRepeatsToGui(true);
    }

    public void reset() {
        if (BetterRecipeBook.config.keepCentered) {
            this.leftOffset = this.narrow ? 0 : 162;
        } else {
            this.leftOffset = this.narrow ? 0 : 86;
        }

        int i = (this.parentWidth - 147) / 2 - this.leftOffset;
        int j = (this.parentHeight - 166) / 2;
        this.recipeFinder.clear();
        assert this.client.player != null;
        this.client.player.getInventory().fillStackedContents(this.recipeFinder);
        String string = this.searchField != null ? this.searchField.getValue() : "";
        Font var10003 = this.client.font;
        int var10004 = i + 25;
        int var10005 = j + 14;
        Objects.requireNonNull(this.client.font);
        this.searchField = new EditBox(var10003, var10004, var10005, 80, 9 + 5, new TranslatableComponent("itemGroup.search"));
        this.searchField.setMaxLength(50);
        this.searchField.setBordered(false);
        this.searchField.setVisible(true);
        this.searchField.setTextColor(16777215);
        this.searchField.setValue(string);
        this.recipesArea.initialize(this.client, i, j, enchantmentMenu);
        this.tabButtons.clear();
        this.recipeBook.setFilteringCraftable(BetterRecipeBook.rememberedBrewingToggle);
        this.toggleBrewableButton = new StateSwitchingButton(i + 110, j + 12, 26, 16, this.recipeBook.isFilteringCraftable());
        this.setBookButtonTexture();

        for (RecipeBookGroup recipeBookGroup : RecipeBookGroup.getGroups()) {
            this.tabButtons.add(new RecipeGroupButtonWidget(recipeBookGroup));
        }

        if (this.currentTab != null) {
            this.currentTab = this.tabButtons.stream().filter((button) -> button.getGroup().equals(this.currentTab.getGroup())).findFirst().orElse(null);
        }

        if (this.currentTab == null) {
            this.currentTab = this.tabButtons.get(0);
        }

        if (BetterRecipeBook.config.settingsButton) {
            int u = 0;
            if (BetterRecipeBook.config.darkMode) {
                u = 18;
            }

            this.settingsButton = new ImageButton(i + 11, j + 137, 16, 18, u, 77, 19, BUTTON_TEXTURE, button -> {
                Minecraft.getInstance().setScreen(AutoConfig.getConfigScreen(Config.class, Minecraft.getInstance().screen).get());
            });
        }

        this.currentTab.setStateTriggered(true);
        this.refreshResults(false);
        this.refreshTabButtons();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.open && !Objects.requireNonNull(this.client.player).isSpectator()) {
            if (this.recipesArea.mouseClicked(mouseX, mouseY, button)) {
                EnchantingResult result = this.recipesArea.getLastClickedRecipe();
                if (result != null) {
                    if (this.currentTab == null) return false;
                    this.ghostSlots.reset();

                    if (!result.hasItem(enchantmentMenu)) {
                        showGhostRecipe(result, enchantmentMenu.slots);
                        return false;
                    }

//                    Ingredient ingredient = getIngredient(result.enchantment);

                    int slotIndex = 0;
                    int usedInputSlots = 0;
                    for (Slot slot : enchantmentMenu.slots) {
                        ItemStack itemStack = slot.getItem();

//                        assert inputStack.getTag() != null;
//                        if (inputStack.getTag().equals(itemStack.getTag()) && inputStack.getItem().equals(itemStack.getItem())) {
//                            if (usedInputSlots <= 2) {
//                                System.out.println(usedInputSlots);
//                                assert Minecraft.getInstance().gameMode != null;
//                                Minecraft.getInstance().gameMode.handleInventoryMouseClick(enchantmentMenu.containerId, enchantmentMenu.getSlot(slotIndex).index, 0, ClickType.PICKUP, Minecraft.getInstance().player);
//                                Minecraft.getInstance().gameMode.handleInventoryMouseClick(enchantmentMenu.containerId, enchantmentMenu.getSlot(usedInputSlots).index, 0, ClickType.PICKUP, Minecraft.getInstance().player);
//                                ++usedInputSlots;
//                            }
//                        } else if (ingredient.getItems()[0].getItem().equals(slot.getItem().getItem())) {
//                            assert Minecraft.getInstance().gameMode != null;
//                            Minecraft.getInstance().gameMode.handleInventoryMouseClick(enchantmentMenu.containerId, enchantmentMenu.getSlot(slotIndex).index, 0, ClickType.PICKUP, Minecraft.getInstance().player);
//                            Minecraft.getInstance().gameMode.handleInventoryMouseClick(enchantmentMenu.containerId, enchantmentMenu.getSlot(3).index, 0, ClickType.PICKUP, Minecraft.getInstance().player);
//                        }

                        ++slotIndex;
                    }

                    this.refreshResults(false);
                }

                return true;
            } else {
                assert this.searchField != null;
                if (this.searchField.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                } else if (this.toggleBrewableButton.mouseClicked(mouseX, mouseY, button)) {
                    boolean bl = this.toggleFilteringBrewable();
                    this.toggleBrewableButton.setStateTriggered(bl);
                    BetterRecipeBook.rememberedBrewingToggle = bl;
                    this.refreshResults(false);
                    return true;
                } else if (this.settingsButton != null) {
                    if (this.settingsButton.mouseClicked(mouseX, mouseY, button) && BetterRecipeBook.config.settingsButton) {
                        return true;
                    }
                }

                Iterator<RecipeGroupButtonWidget> var6 = this.tabButtons.iterator();

                RecipeGroupButtonWidget recipeGroupButtonWidget;
                do {
                    if (!var6.hasNext()) {
                        return false;
                    }

                    recipeGroupButtonWidget = var6.next();
                } while (!recipeGroupButtonWidget.mouseClicked(mouseX, mouseY, button));

                if (this.currentTab != recipeGroupButtonWidget) {
                    if (this.currentTab != null) {
                        this.currentTab.setStateTriggered(false);
                    }

                    this.currentTab = recipeGroupButtonWidget;
                    this.currentTab.setStateTriggered(true);
                    this.refreshResults(true);
                }
                return false;
            }
        } else {
            return false;
        }
    }

    public void showGhostRecipe(EnchantingResult result, List<Slot> slots) {
//        this.ghostSlots.addSlot(getIngredient(result.enchantment).getItems()[0], slots.get(3).x, slots.get(3).y);

//        assert currentTab != null;
//        ItemStack inputStack = result.inputAsItemStack(currentTab.getGroup());
//        this.ghostSlots.addSlot(result.ingredient, slots.get(0).x, slots.get(0).y);
//        this.ghostSlots.addSlot(inputStack, slots.get(1).x, slots.get(1).y);
//        this.ghostSlots.addSlot(inputStack, slots.get(2).x, slots.get(2).y);
    }

    private boolean toggleFilteringBrewable() {
        boolean bl = !this.recipeBook.isFilteringCraftable();
        this.recipeBook.setFilteringCraftable(bl);
        BetterRecipeBook.rememberedBrewingToggle = bl;
        return bl;
    }

    private void refreshResults(boolean resetCurrentPage) {
        if (this.currentTab == null) return;
        if (this.searchField == null) return;

        List<EnchantingResult> results = recipeBook.getResultsForCategory();

//        String string = this.searchField.getValue();
//        if (!string.isEmpty()) {
//            results.removeIf(itemStack -> !itemStack.enchantment.getHoverName().getString().toLowerCase(Locale.ROOT).contains(string.toLowerCase(Locale.ROOT)));
//        }
//
//        if (this.recipeBook.isFilteringCraftable()) {
//            results.removeIf((result) -> !result.hasMaterials(currentTab.getGroup(), enchantmentMenu));
//        }
//
//        if (BetterRecipeBook.config.enablePinning) {
//            List<Result> tempResults = Lists.newArrayList(results);
//
//            for (Result result : tempResults) {
//                if (BetterRecipeBook.pinnedRecipeManager.hasPotion(result.recipe)) {
//                    results.remove(result);
//                    results.add(0, result);
//                }
//            }
//        }

        this.recipesArea.setResults(results, resetCurrentPage, currentTab.getGroup());
    }

    private void refreshTabButtons() {
        int i = (this.parentWidth - 147) / 2 - this.leftOffset - 30;
        int j = (this.parentHeight - 166) / 2 + 3;
        int l = 0;

        for (RecipeGroupButtonWidget recipeGroupButtonWidget : this.tabButtons) {
            RecipeBookGroup recipeBookGroup = recipeGroupButtonWidget.getGroup();
            if (recipeBookGroup == RecipeBookGroup.BREWING_SEARCH) {
                recipeGroupButtonWidget.visible = true;
            }
            recipeGroupButtonWidget.setPosition(i, j + 27 * l++);
        }
    }

    public boolean isOpen() {
        return open;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if (this.searchField == null) return;

        if (doubleRefresh) {
            // Minecraft doesn't populate the inventory on initialization so this is the only solution I have
            refreshResults(true);
            doubleRefresh = false;
        }
        if (this.isOpen()) {
            matrices.pushPose();
            matrices.translate(0.0D, 0.0D, 100.0D);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int i = (this.parentWidth - 147) / 2 - this.leftOffset;
            int j = (this.parentHeight - 166) / 2;
            this.blit(matrices, i, j, 1, 1, 147, 166);

            if (!this.searchField.isFocused() && this.searchField.getValue().isEmpty()) {
                drawString(matrices, this.client.font, SEARCH_HINT_TEXT, i + 25, j + 14, -1);
            } else {
                this.searchField.render(matrices, mouseX, mouseY, delta);
            }

            for (RecipeGroupButtonWidget recipeGroupButtonWidget : this.tabButtons) {
                recipeGroupButtonWidget.render(matrices, mouseX, mouseY, delta);
            }

            this.toggleBrewableButton.render(matrices, mouseX, mouseY, delta);

            if (BetterRecipeBook.config.settingsButton) {
                this.settingsButton.render(matrices, mouseX, mouseY, delta);
            }

            this.recipesArea.draw(matrices, i, j, mouseX, mouseY, delta);
            matrices.popPose();
        }
    }

    public int findLeftEdge(int width, int backgroundWidth) {
        int j;
        if (this.isOpen() && !this.narrow) {
            j = 177 + (width - backgroundWidth - 200) / 2;
        } else {
            j = (width - backgroundWidth) / 2;
        }

        return j;
    }

    public void drawGhostSlots(PoseStack matrices, int x, int y, boolean bl, float delta) {
        this.ghostSlots.draw(matrices, this.client, x, y, bl, delta);
    }

    private void setOpen(boolean opened) {
        if (opened) {
            this.reset();
        }
        this.open = opened;
    }

    public void toggleOpen() {
        this.setOpen(!this.isOpen());
    }

    protected void setBookButtonTexture() {
        this.toggleBrewableButton.initTextureValues(152, 41, 28, 18, TEXTURE);
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.open ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
    }

    public void drawTooltip(PoseStack matrices, int x, int y, int mouseX, int mouseY) {
        if (this.isOpen()) {
            this.recipesArea.drawTooltip(matrices, mouseX, mouseY);
            if (this.toggleBrewableButton.isHoveredOrFocused()) {
                Component text = this.getCraftableButtonText();
                if (this.client.screen != null) {
                    this.client.screen.renderTooltip(matrices, text, mouseX, mouseY);
                }
            }

            if (this.settingsButton != null) {
                if (this.settingsButton.isHoveredOrFocused() && BetterRecipeBook.config.settingsButton) {
                    if (this.client.screen != null) {
                        this.client.screen.renderTooltip(matrices, OPEN_SETTINGS_TEXT, mouseX, mouseY);
                    }
                }
            }

            this.drawGhostSlotTooltip(matrices, x, y, mouseX, mouseY);
        }
    }

    private Component getCraftableButtonText() {
        return this.toggleBrewableButton.isStateTriggered() ? this.getToggleCraftableButtonText() : TOGGLE_ALL_RECIPES_TEXT;
    }

    protected Component getToggleCraftableButtonText() {
        return TOGGLE_ENCHANTABLE_RECIPES_TEXT;
    }

    private void drawGhostSlotTooltip(PoseStack matrices, int x, int y, int mouseX, int mouseY) {
        ItemStack itemStack = null;

        for(int i = 0; i < this.ghostSlots.getSlotCount(); ++i) {
            EnchantingRecipeBookGhostSlots.GhostSlot ghostInputSlot = this.ghostSlots.getSlot(i);
            int j = ghostInputSlot.getX() + x;
            int k = ghostInputSlot.getY() + y;
            if (mouseX >= j && mouseY >= k && mouseX < j + 16 && mouseY < k + 16) {
                itemStack = ghostInputSlot.getCurrentItemStack();
            }
        }

        if (itemStack != null && this.client.screen != null) {
            this.client.screen.renderComponentTooltip(matrices, this.client.screen.getTooltipFromItem(itemStack), mouseX, mouseY);
        }

    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.searching = false;
        if (this.isOpen() && !Objects.requireNonNull(this.client.player).isSpectator()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.setOpen(false);
                return true;
            } else {
                assert this.searchField != null;
                if (this.searchField.keyPressed(keyCode, scanCode, modifiers)) {
                    this.refreshSearchResults();
                    return true;
                } else if (this.searchField.isFocused() && this.searchField.isVisible()) {
                    return true;
                } else if (keyCode == GLFW.GLFW_KEY_F) {
                    if (BetterRecipeBook.config.enablePinning) {
                        for (EnchantingAnimatedResultButton resultButton : this.recipesArea.resultButtons) {
                            if (resultButton.isHoveredOrFocused()) {
//                                BetterRecipeBook.pinnedRecipeManager.addOrRemoveFavouritePotion(resultButton.getRecipe().enchantment);
                                this.refreshResults(false);
                                return true;
                            }
                        }
                    }
                    return false;
                } else if (this.client.options.keyChat.matches(keyCode, scanCode) && !this.searchField.isFocused()) {
                    this.searching = true;
                    this.searchField.setFocus(true);
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.searching = false;
        return GuiEventListener.super.keyReleased(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char chr, int modifiers) {
        if (this.searching) {
            return false;
        } else if (this.isOpen() && !Objects.requireNonNull(this.client.player).isSpectator()) {
            assert this.searchField != null;
            if (this.searchField.charTyped(chr, modifiers)) {
                this.refreshSearchResults();
                return true;
            } else {
                return GuiEventListener.super.charTyped(chr, modifiers);
            }
        } else {
            return false;
        }
    }

    private void refreshSearchResults() {
        assert this.searchField != null;
        String string = this.searchField.getValue().toLowerCase(Locale.ROOT);
        if (!string.equals(this.searchText)) {
            this.refreshResults(false);
            this.searchText = string;
        }

    }

    @Override
    public void updateNarration(NarrationElementOutput builder) {

    }

    static {
        SEARCH_HINT_TEXT = (new TranslatableComponent("gui.recipebook.search_hint")).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
        TOGGLE_ENCHANTABLE_RECIPES_TEXT = new TranslatableComponent("brb.gui.togglePotions.brewable");
        TOGGLE_ALL_RECIPES_TEXT = new TranslatableComponent("gui.recipebook.toggleRecipes.all");
        OPEN_SETTINGS_TEXT = new TranslatableComponent("brb.gui.settings.open");
    }
}
