/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bluecardinal.voxcrafti;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitScheduler;

/**
 *
 * @author eli
 */
class VoxCraftiEntityListener implements Listener {
    public static VoxCrafti plugin; 
    public VoxCraftiEntityListener(VoxCrafti instance) {
        plugin = instance;
    }
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        List<Block> blocks = e.blockList();
        List<BlockState> saveUs = new ArrayList<BlockState>();
        for (Block b : blocks) {
            plugin.shrineFinder.checkShrineDead(b, true);
            if(plugin.isExplosionImmune(b)) {
                saveUs.add(b.getState());
                b.setType(Material.AIR); //hide!! we don't want to get exploded.
                plugin.clearExplosionImmune(b); //so that multiple explosions don't kick in multiple immunities
                //thus saving the air state or other nasty bidness
            }
        }
        if (!blocks.isEmpty()) {
            BukkitScheduler scheduler = plugin.getServer().getScheduler();
            scheduler.scheduleAsyncDelayedTask(plugin, new BlockRestore(saveUs, plugin), 2); //come back when 'splosion is done
        }
    }
}
class BlockRestore implements Runnable {
    private List<BlockState> blocks;
    private VoxCrafti plugin;
    public BlockRestore(List<BlockState> blocks, VoxCrafti vox) {
        this.blocks = blocks;
        this.plugin = vox;
    }
    public void run() {
        for (BlockState block : this.blocks) {
            block.update(true);
            plugin.setExplosionImmune(block.getBlock());
        }
    }
}