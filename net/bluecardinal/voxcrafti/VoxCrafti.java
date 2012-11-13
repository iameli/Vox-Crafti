/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bluecardinal.voxcrafti;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author eli0-o
 */
public class VoxCrafti extends JavaPlugin {

    File configFile = new File("vox_crafti.yml");
    public static Logger log = Logger.getLogger("Minecraft");
    public static final boolean DEBUG = true;
    public ShrineFinder shrineFinder = new ShrineFinder(this);
    private Set<Block> explosionImmune = new HashSet<Block>();
    private final VoxCraftiBlockListener blockListener = new VoxCraftiBlockListener(this);
    private final VoxCraftiEntityListener entityListener = new VoxCraftiEntityListener(this);
    private PluginManager pm;
    private Set<Deity> deities = new HashSet<Deity>();
    private FileConfiguration myConfig;
    
    public static void debug(String msg) {
        if (DEBUG) log.info(msg);
    }
    

    public void onEnable() {
        log.info("Vox Crafti enabled. On your knees.");
        pm = this.getServer().getPluginManager();
        pm.registerEvents(blockListener, this);
        pm.registerEvents(entityListener, this);
        
    }
    
    public void onDisable() {
        log.info("Vox Crafti disabled. No gods, no masters.");
    }
    
    public void registerDeity(Deity deity) {
        deities.add(deity);
        shrineFinder.addShrine(deity.getShrine(), deity);
    }
    
   public Deity getDeity(String name) {
       for (Deity god : deities) {
           if (god.getDeityName().equalsIgnoreCase(name)) return god;
       }
       return null;
   }
   
   private void writeConfig() {
       
   }
    
    /**
     * A couple different gods are gonna do this and it's slightly involved so it lives up here
     * @param b block to make immune.
     */
    public void setExplosionImmune(Block b) {
        explosionImmune.add(b);
    }
    
    public void setAllExplosionImmune(Collection<Block> c) {
        explosionImmune.addAll(c);
    }
    
    public void clearExplosionImmune(Block b) {
        explosionImmune.remove(b);
    }
    
    public void clearAllExplosionImmune(Collection<Block> c) {
        explosionImmune.removeAll(c);
    }
    
    public boolean isExplosionImmune(Block b) {
        return explosionImmune.contains(b);
    }


    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (this.DEBUG) {
            if (cmd.getName().equalsIgnoreCase("g")) { // If the player typed /basic then do the following...
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.getGameMode() == GameMode.CREATIVE) player.setGameMode(GameMode.SURVIVAL);
                    else player.setGameMode(GameMode.CREATIVE);
                    return true;
                }
            } //If this has happened the function will break and return true. if this hasn't happened the a value of false will be returned.
        }
        return false;
    }
}
