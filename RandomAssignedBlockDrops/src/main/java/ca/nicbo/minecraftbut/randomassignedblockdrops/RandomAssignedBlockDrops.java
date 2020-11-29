package ca.nicbo.minecraftbut.randomassignedblockdrops;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author Nicbo
 */

public class RandomAssignedBlockDrops extends JavaPlugin implements Listener {
    private static final Map<Material, Material> MATERIAL_MAP;
    private static final Map<Material, LootTable> MATERIAL_LOOT_TABLES_MAP;

    static {
        Map<Material, Material> materialMap = new EnumMap<>(Material.class);
        Map<Material, LootTable> materialLootTablesMap = new EnumMap<>(Material.class);

        // List of shuffled block materials
        List<Material> blockMaterials = Arrays.stream(Material.values())
                .filter(Material::isBlock)
                .collect(Collectors.toList());
        Collections.shuffle(blockMaterials);

        // List of shuffled item materials
        List<Material> itemMaterials = Arrays.stream(Material.values())
                .filter(Material::isItem)
                .collect(Collectors.toList());
        Collections.shuffle(itemMaterials);

        // List of shuffled loot tables that do not need a killer/entity
        List<LootTable> lootTables = new ArrayList<>(Arrays.asList(
                LootTables.ABANDONED_MINESHAFT.getLootTable(), LootTables.BURIED_TREASURE.getLootTable(),
                LootTables.DESERT_PYRAMID.getLootTable(), LootTables.END_CITY_TREASURE.getLootTable(),
                LootTables.IGLOO_CHEST.getLootTable(), LootTables.JUNGLE_TEMPLE.getLootTable(),
                LootTables.JUNGLE_TEMPLE_DISPENSER.getLootTable(), LootTables.NETHER_BRIDGE.getLootTable(),
                LootTables.PILLAGER_OUTPOST.getLootTable(), LootTables.BASTION_TREASURE.getLootTable(),
                LootTables.BASTION_OTHER.getLootTable(), LootTables.BASTION_BRIDGE.getLootTable(),
                LootTables.BASTION_HOGLIN_STABLE.getLootTable(), LootTables.RUINED_PORTAL.getLootTable(),
                LootTables.SHIPWRECK_MAP.getLootTable(), LootTables.SHIPWRECK_SUPPLY.getLootTable(),
                LootTables.SHIPWRECK_TREASURE.getLootTable(), LootTables.SIMPLE_DUNGEON.getLootTable(),
                LootTables.SPAWN_BONUS_CHEST.getLootTable(), LootTables.STRONGHOLD_CORRIDOR.getLootTable(),
                LootTables.STRONGHOLD_CROSSING.getLootTable(), LootTables.STRONGHOLD_LIBRARY.getLootTable(),
                LootTables.UNDERWATER_RUIN_BIG.getLootTable(), LootTables.UNDERWATER_RUIN_SMALL.getLootTable(),
                LootTables.VILLAGE_ARMORER.getLootTable(), LootTables.VILLAGE_BUTCHER.getLootTable(),
                LootTables.VILLAGE_CARTOGRAPHER.getLootTable(), LootTables.VILLAGE_DESERT_HOUSE.getLootTable(),
                LootTables.VILLAGE_FISHER.getLootTable(), LootTables.VILLAGE_FLETCHER.getLootTable(),
                LootTables.VILLAGE_MASON.getLootTable(), LootTables.VILLAGE_PLAINS_HOUSE.getLootTable(),
                LootTables.VILLAGE_SAVANNA_HOUSE.getLootTable(), LootTables.VILLAGE_SHEPHERD.getLootTable(),
                LootTables.VILLAGE_SNOWY_HOUSE.getLootTable(), LootTables.VILLAGE_TAIGA_HOUSE.getLootTable(),
                LootTables.VILLAGE_TANNERY.getLootTable(), LootTables.VILLAGE_TEMPLE.getLootTable(),
                LootTables.VILLAGE_TOOLSMITH.getLootTable(), LootTables.VILLAGE_WEAPONSMITH.getLootTable(),
                LootTables.WOODLAND_MANSION.getLootTable()
        ));
        Collections.shuffle(lootTables);

        // Put materials and loot tables into maps
        final int lootTablesIndex = blockMaterials.size() - lootTables.size();
        for (int i = 0; i < blockMaterials.size(); i++) {
            final Material keyMaterial = blockMaterials.get(i);

            if (i >= lootTablesIndex) { // Add loot tables
                materialLootTablesMap.put(keyMaterial, lootTables.get(i - lootTablesIndex));
                continue;
            }

            materialMap.put(keyMaterial, itemMaterials.get(i));
        }

        MATERIAL_MAP = Collections.unmodifiableMap(materialMap);
        MATERIAL_LOOT_TABLES_MAP = Collections.unmodifiableMap(materialLootTablesMap);
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material material = block.getType();
        Location location = block.getLocation();

        // Get drops from the material
        Material selectedMaterial = MATERIAL_MAP.get(material);
        Collection<ItemStack> drops = selectedMaterial == null ? // Material is mapped to a loot table
                MATERIAL_LOOT_TABLES_MAP.get(material).populateLoot(ThreadLocalRandom.current(), new LootContext.Builder(location).build()) :
                Collections.singletonList(new ItemStack(selectedMaterial));

        // Drop the items
        World world = block.getWorld();
        Location dropLocation = location.clone().add(0.5, 0.0, 0.5);
        for (ItemStack drop : drops) {
            world.dropItemNaturally(dropLocation, drop);
        }

        // Don't drop original items
        event.setDropItems(false);
    }
}
