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

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author Nicbo
 */

public class RandomAssignedBlockDrops extends JavaPlugin implements Listener {
    private static final Map<Material, Material> MATERIAL_MAP;
    private static final Map<Material, LootTables> MATERIAL_LOOT_TABLES_MAP;

    static {
        MATERIAL_MAP = new HashMap<>();
        MATERIAL_LOOT_TABLES_MAP = new HashMap<>();

        // Materials minus the legacy ones
        List<Material> materials = Arrays.stream(Material.values())
                .filter(material -> !material.name().startsWith("LEGACY"))
                .collect(Collectors.toList());

        // Create clone of materials and shuffle them
        List<Material> shuffledMaterials = new ArrayList<>(materials);
        Collections.shuffle(shuffledMaterials);

        // Make room for loot tables
        trimList(materials, LootTables.values().length);

        // Loop through the shuffled materials
        for (int i = 0, j = 0; i < shuffledMaterials.size(); i++) {
            final Material keyMaterial = shuffledMaterials.get(i);

            if (i >= materials.size()) { // Add loot tables
                MATERIAL_LOOT_TABLES_MAP.put(keyMaterial, LootTables.values()[j++]);
                continue;
            }

            MATERIAL_MAP.put(keyMaterial, materials.get(i));
        }

        /*
        System.out.println("MATERIAL_LOOT_MAP:");
        System.out.println(MATERIAL_MAP.toString());

        System.out.println("MATERIAL_LOOT_TABLES_MAP");
        System.out.println(MATERIAL_LOOT_TABLES_MAP.toString());
         */
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

        Iterable<ItemStack> drops;

        Material selectedMaterial = MATERIAL_MAP.get(material);
        if (selectedMaterial == null) { // Material is mapped to a loot table
            LootTable table = MATERIAL_LOOT_TABLES_MAP.get(material).getLootTable();
            drops = table.populateLoot(ThreadLocalRandom.current(), new LootContext.Builder(location).build());
        } else {
            drops = Collections.singletonList(new ItemStack(selectedMaterial));
        }

        // Drop the items
        World world = block.getWorld();
        Location dropLocation = location.clone().add(0.5D, 0.0D, 0.5D);
        for (ItemStack drop : drops) {
            if (drop.getType() != Material.AIR) {
                world.dropItemNaturally(dropLocation, drop);
            }
        }

        // Don't drop original items
        event.setDropItems(false);
    }

    private static void trimList(List<?> list, int amount) {
        list.subList(list.size() - amount, list.size()).clear();
    }
}
