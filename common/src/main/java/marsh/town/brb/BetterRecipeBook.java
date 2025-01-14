package marsh.town.brb;

import marsh.town.brb.Config.Config;
import marsh.town.brb.Loaders.PotionLoader;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BetterRecipeBook {

    public static final String MOD_ID = "brb";

    // textures
    public static final ResourceLocation PIN_TEXTURE = new ResourceLocation("brb:textures/gui/pin.png");

    public static int queuedScroll;
    public static boolean isFilteringNone;

    public static Config config;

    public static PinnedRecipeManager pinnedRecipeManager;
    public static InstantCraftingManager instantCraftingManager;

    public static boolean rememberedBrewingOpen = true;
    public static boolean rememberedBrewingToggle = false;

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static void init() {
        PotionLoader.init();

        queuedScroll = 0;
        isFilteringNone = true;

        AutoConfig.register(Config.class, Toml4jConfigSerializer::new);

        config = AutoConfig.getConfigHolder(Config.class).getConfig();

        pinnedRecipeManager = new PinnedRecipeManager();
        pinnedRecipeManager.read();
        instantCraftingManager = new InstantCraftingManager();
    }
}
