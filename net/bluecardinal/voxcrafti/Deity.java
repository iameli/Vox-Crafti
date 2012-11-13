/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bluecardinal.voxcrafti;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;

/**
 * Parent class for all gods. 
 * @author eli
 */
public interface Deity {
        /**
     * This will eventually return whether or not a player has the Anubis shrine active.
     * @param player
     * @return 
     */
    public boolean activeFor(String player);
    public void shrineCreated(String player);
    public String getDeityName();
    public void shrineDestroyed(String player);
    public Material[][][] getShrine();
    /**
     * Deities are expected to maintain an up-to-date VoxConfig that can dump
     * all relevant save data whenever this is called.
     * @return 
     */
    public VoxConfig getVoxConfig();
    /**
     * Loading everything up. Here's your config. Do good.
     */
    public void fromConfig (VoxConfig conf);
}
