/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bluecardinal.voxcrafti;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author eli
 */
class VoxCraftiBlockListener implements Listener {
    public static VoxCrafti plugin; 
    public VoxCraftiBlockListener(VoxCrafti instance) {
        plugin = instance;
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        plugin.shrineFinder.checkShrine(e.getBlock(), e.getPlayer());
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        VoxCrafti.debug("onBlockBreak called");
        Block block = e.getBlock();
        plugin.shrineFinder.checkShrineDead(block, true);
    }
    @EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
        VoxCrafti.debug("onBlockBurn called");
        Block block = e.getBlock();
        plugin.shrineFinder.checkShrineDead(block, true);

    }
    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent e) {
        List<Block> blocks = e.getBlocks();
        VoxCrafti.debug("onBlockPistonExtend called");
        for (Block block : blocks) {
            boolean hitShrine = plugin.shrineFinder.checkShrineDead(block, false);
            if (hitShrine) { e.setCancelled(true); }
        }
    }
    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent e) {
        if (e.isSticky()) {
            VoxCrafti.debug("onPistonRetract called");
            Block block = e.getRetractLocation().getBlock();
            boolean hitShrine = plugin.shrineFinder.checkShrineDead(block, false);
            if (hitShrine) { e.setCancelled(true); }
        }
    }
}
