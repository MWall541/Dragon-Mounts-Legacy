package com.github.kay9.dragonmounts;

import com.github.kay9.dragonmounts.data.loot.DragonEggLootMod;
import com.github.kay9.dragonmounts.dragon.DragonBreed;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.kay9.dragonmounts.dragon.DragonBreed.BuiltIn.*;

/**
 * JSON-based configuration for Fabric. Replaces Forge's ForgeConfigSpec.
 * Config is stored at {@code config/dragonmounts.json}.
 */
public class DMLConfig
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("dragonmounts.json");

    // Common config
    private static boolean allowEggOverride = true;
    private static boolean replenishEggs = true;
    private static boolean useLootTables = false;
    private static boolean updateHabitats = true;

    // Client config
    private static boolean cameraDrivenFlight = true;
    private static boolean thirdPersonOnMount = true;

    // Camera offsets: [0] = back, [1] = front; each has [distance, vertical, horizontal]
    private static final double[][] cameraOffsets = {
            {6.0, 4.0, 0.0},  // back
            {6.0, 4.0, 0.0}   // front
    };

    // Egg chances (keyed by formatted path string)
    private static final Map<String, Double> eggChances = new LinkedHashMap<>();

    // Reproduction limits (keyed by breed path, e.g. "aether")
    private static final Map<String, Integer> reproLimits = new LinkedHashMap<>();

    // ========================
    //       Accessors
    // ========================

    public static boolean allowEggOverride() { return allowEggOverride; }
    public static boolean replenishEggs() { return replenishEggs; }
    public static boolean useLootTables() { return useLootTables; }
    public static boolean updateHabitats() { return updateHabitats; }
    public static boolean cameraDrivenFlight() { return cameraDrivenFlight; }
    public static boolean thirdPersonOnMount() { return thirdPersonOnMount; }

    public static void setCameraDrivenFlight(boolean value)
    {
        cameraDrivenFlight = value;
        save();
    }

    /**
     * Returns the camera perspective offset values for back or front third person.
     * Index 0 = distance, 1 = vertical, 2 = horizontal.
     */
    public static double[] getCameraPerspectiveOffset(boolean back)
    {
        return cameraOffsets[back ? 0 : 1];
    }

    public static float getEggChanceFor(String configTarget)
    {
        var chance = eggChances.get(configTarget);
        if (chance == null) return -1f;
        return chance.floatValue();
    }

    public static int getReproLimitFor(String configTarget)
    {
        var key = configTarget.replace("config:", "");
        var limit = reproLimits.get(key);
        if (limit == null) return -1;
        return limit;
    }

    public static String formatEggTargetAsPath(ResourceKey<DragonBreed> forBreed, ResourceKey<LootTable> forTarget)
    {
        return String.format("%s_in_%s_chance",
                forBreed.identifier().getPath(),
                forTarget.identifier().getPath().substring(forTarget.identifier().getPath().lastIndexOf('/') + 1));
    }

    // ========================
    //     Load / Save
    // ========================

    public static void load()
    {
        // Initialize defaults for egg chances and repro limits
        initDefaults();

        if (Files.exists(CONFIG_PATH))
        {
            try
            {
                var json = GSON.fromJson(Files.readString(CONFIG_PATH), JsonObject.class);
                if (json != null) readFromJson(json);
            }
            catch (IOException e)
            {
                DragonMountsLegacy.LOG.error("Failed to read config file, using defaults.", e);
            }
        }

        // Always save to ensure new fields are written
        save();
    }

    public static void save()
    {
        try
        {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(writeToJson()));
        }
        catch (IOException e)
        {
            DragonMountsLegacy.LOG.error("Failed to save config file.", e);
        }
    }

    // ========================
    //       Defaults
    // ========================

    @SuppressWarnings("unchecked")
    private static void initDefaults()
    {
        // Egg chances defaults from DragonEggLootMod.BUILT_IN_CHANCES
        // We initialize them here statically to avoid circular dependency at class-load time.
        // The actual built-in values are set up by DragonEggLootMod; we mirror the keys here.
        // Default chance values will be overwritten if the config file already has them.

        // Reproduction limits
        for (var type : new ResourceKey[]{AETHER, END, FIRE, FOREST, GHOST, ICE, NETHER, WATER})
        {
            reproLimits.putIfAbsent(type.identifier().getPath(), TameableDragon.BASE_REPRO_LIMIT);
        }
    }

    /** Populate egg chance defaults from the built-in array. Called after DragonEggLootMod is available. */
    public static void initEggChanceDefaults()
    {
        for (var target : DragonEggLootMod.BUILT_IN_CHANCES)
        {
            var path = formatEggTargetAsPath(target.forBreed(), target.target());
            eggChances.putIfAbsent(path, target.chance());
        }
    }

    // ========================
    //     JSON Serialization
    // ========================

    private static void readFromJson(JsonObject json)
    {
        if (json.has("allow_egg_override")) allowEggOverride = json.get("allow_egg_override").getAsBoolean();
        if (json.has("replenish_eggs")) replenishEggs = json.get("replenish_eggs").getAsBoolean();
        if (json.has("use_loot_tables")) useLootTables = json.get("use_loot_tables").getAsBoolean();
        if (json.has("update_habitats")) updateHabitats = json.get("update_habitats").getAsBoolean();

        if (json.has("camera_driven_flight")) cameraDrivenFlight = json.get("camera_driven_flight").getAsBoolean();
        if (json.has("third_person_on_mount")) thirdPersonOnMount = json.get("third_person_on_mount").getAsBoolean();

        if (json.has("camera_offsets") && json.get("camera_offsets").isJsonObject())
        {
            var offsets = json.getAsJsonObject("camera_offsets");
            readCameraOffset(offsets, "back", 0);
            readCameraOffset(offsets, "front", 1);
        }

        if (json.has("egg_loot_chances") && json.get("egg_loot_chances").isJsonObject())
        {
            var chances = json.getAsJsonObject("egg_loot_chances");
            for (var entry : chances.entrySet())
            {
                eggChances.put(entry.getKey(), entry.getValue().getAsDouble());
            }
        }

        if (json.has("reproduction_limits") && json.get("reproduction_limits").isJsonObject())
        {
            var limits = json.getAsJsonObject("reproduction_limits");
            for (var entry : limits.entrySet())
            {
                reproLimits.put(entry.getKey(), entry.getValue().getAsInt());
            }
        }
    }

    private static void readCameraOffset(JsonObject offsets, String key, int index)
    {
        if (offsets.has(key) && offsets.get(key).isJsonObject())
        {
            var perspective = offsets.getAsJsonObject(key);
            if (perspective.has("distance")) cameraOffsets[index][0] = perspective.get("distance").getAsDouble();
            if (perspective.has("vertical")) cameraOffsets[index][1] = perspective.get("vertical").getAsDouble();
            if (perspective.has("horizontal")) cameraOffsets[index][2] = perspective.get("horizontal").getAsDouble();
        }
    }

    private static JsonObject writeToJson()
    {
        var json = new JsonObject();

        json.addProperty("allow_egg_override", allowEggOverride);
        json.addProperty("replenish_eggs", replenishEggs);
        json.addProperty("use_loot_tables", useLootTables);
        json.addProperty("update_habitats", updateHabitats);

        json.addProperty("camera_driven_flight", cameraDrivenFlight);
        json.addProperty("third_person_on_mount", thirdPersonOnMount);

        // Camera offsets
        var offsets = new JsonObject();
        writeCameraOffset(offsets, "back", 0);
        writeCameraOffset(offsets, "front", 1);
        json.add("camera_offsets", offsets);

        // Egg chances
        var chances = new JsonObject();
        for (var entry : eggChances.entrySet())
        {
            chances.add(entry.getKey(), new JsonPrimitive(entry.getValue()));
        }
        json.add("egg_loot_chances", chances);

        // Reproduction limits
        var limits = new JsonObject();
        for (var entry : reproLimits.entrySet())
        {
            limits.add(entry.getKey(), new JsonPrimitive(entry.getValue()));
        }
        json.add("reproduction_limits", limits);

        return json;
    }

    private static void writeCameraOffset(JsonObject parent, String key, int index)
    {
        var perspective = new JsonObject();
        perspective.addProperty("distance", cameraOffsets[index][0]);
        perspective.addProperty("vertical", cameraOffsets[index][1]);
        perspective.addProperty("horizontal", cameraOffsets[index][2]);
        parent.add(key, perspective);
    }
}
