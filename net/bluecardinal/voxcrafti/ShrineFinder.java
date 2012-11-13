/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.bluecardinal.voxcrafti;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author eli
 */
public class ShrineFinder {

    private List<Shrine> shrineList = new ArrayList<Shrine>();
    private List<ActiveShrine> activeShrineList = new LinkedList<ActiveShrine>();
    private List<ActiveShrine> inactiveShrineList = new ArrayList<ActiveShrine>();
    private EnumSet<Material> matList = EnumSet.noneOf(Material.class);
    private VoxCrafti plugin;

    public ShrineFinder(VoxCrafti mommy) {
        plugin = mommy;
    }

    public void addShrine(Material[][][] newShrine, Deity deity) {
        Shrine shrine = new Shrine(newShrine, deity);
        shrineList.add(shrine);
        matList.addAll(shrine.matList);
        for (int i = 0; i < inactiveShrineList.size(); i++) {
            ActiveShrine candidate = inactiveShrineList.get(i);
            if (candidate.deityName().equalsIgnoreCase(deity.getDeityName())) {
                inactiveShrineList.remove(candidate);
                activeShrineList.add(candidate);
                candidate.activate(deity);
            }
        }
    }

    public boolean isPossibleShrine(Material mat) {
        return matList.contains(mat);
    }

    public void checkShrine(Block b, Player p) {
        if (isPossibleShrine(b.getType())) {
            for (Shrine s : shrineList) {
                if (s.matList.contains(b.getType())) {
                    Set<Block> shrineBlocks = s.check(b);
                    if (shrineBlocks != null) {
                        for (Block infringingBlock : shrineBlocks) { //check to see if overlapping shrines
                            if (checkShrineDead(infringingBlock, true)) {
                                for (Block deleteMe : shrineBlocks) { //ack recursion! but there's a return in here so it'll only happen once
                                    deleteMe.setType(Material.AIR); //DON'T CROSS THE SHRINES!!!
                                }
                                return;
                            }
                        }
                        if (!s.deity.activeFor(p.getName())) {
                            ActiveShrine newShrine = new ActiveShrine(p, shrineBlocks, s.deity);
                            activeShrineList.add(newShrine);
                        }
                    }
                }
            }
        }
    }

    /**
     * For saving. The ConfigurationNode will be modified.
     * @param config 
     */
    public void toConfig(VoxConfig config) {
        VoxConfig activeShrinesConfig = config.getChild("activeShrines");
        List<ActiveShrine> combined = new ArrayList<ActiveShrine>();
        combined.addAll(activeShrineList);
        combined.addAll(inactiveShrineList);
        for (int i = 0; i < combined.size(); i++) {
            VoxConfig curShrine = activeShrinesConfig.getChild("" + i);
            combined.get(i).toConfig(curShrine);
        }
    }

    /**
     * For loading.
     * @param b
     * @param removeShrine
     * @return 
     */
    public void fromConfig(VoxConfig config) {
        VoxConfig activeShrinesConfig = config.getChild("activeShrines");
        for (int i = 0; i < activeShrinesConfig.numChildren(); i++) {
            VoxConfig curShrine = activeShrinesConfig.getChild("" + i);
            inactiveShrineList.add(new ActiveShrine(curShrine));
        }
    }

    /**
     * Check to see if an action would break a shrine.
     * @param b Block that is gettin' moved
     * @param removeShrine Go ahead or remove the shrine, or no? You sometimes don't
     * wanna because you want to cancel the offending action instead, i.e. pistons.
     * @return Would/did this thing break a shrine?
     */
    public boolean checkShrineDead(Block b, boolean removeShrine) {
        if (isPossibleShrine(b.getType())) {
            for (ActiveShrine shrine : activeShrineList) {
                if (shrine.checkBroken(b, removeShrine)) {
                    activeShrineList.remove(shrine);
                    return true;
                }
            }
        }
        return false;
    }
}

class Coord {

    public int x;
    public int y;
    public int z;

    Coord(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

class Shrine {

    private Map<Coord, Material> coordList = new HashMap<Coord, Material>();
    public Deity deity;
    public EnumSet<Material> matList = EnumSet.noneOf(Material.class);
    public Material[][][] layout;

    Shrine(Material[][][] layout, Deity deity) {
        this.layout = layout;
        this.deity = deity;
        for (int x = 0; x < layout.length; x++) {
            for (int y = 0; y < layout[x].length; y++) {
                for (int z = 0; z < layout[x][y].length; z++) {
                    if (layout[x][y][z] != null) {
                        coordList.put(new Coord(x, y, z), layout[x][y][z]);
                        matList.add(layout[x][y][z]);
                    }
                }
            }
        }
    }

    /**
     * 
     * @param b Block just placed, let's see if it's placement creates a shrine
     * @return null if not a shrine, a list of blocks used in the shrine if it is
     */
    public Set<Block> check(Block b) {
        World world = b.getWorld();
        Material mat = b.getType();
        Set<Coord> locs = new HashSet<Coord>();
        for (Coord c : coordList.keySet()) {
            if (coordList.get(c) == mat) {
                locs.add(c);
            }
        }
        for (Coord c : locs) {

            for (int i = 0; i < 4; i++) {
                Coord modC = cMod(c, i);
                int searchX = b.getX() - modC.x;
                int searchY = b.getY() - modC.y;
                int searchZ = b.getZ() - modC.z;
                Block possBottom = world.getBlockAt(searchX, searchY, searchZ);
                Set<Block> check = checkFromBottom(possBottom, i);
                if (check != null) {
                    return check;
                }
            }
        }
        return null;
    }

    /**
     * 
     * @param b
     * @param step
     * @return Null if not shrine, list of shrineblocks if shrine
     */
    private Set<Block> checkFromBottom(Block b, int step) {
        Set<Block> blockSet = new HashSet<Block>();
        for (Coord c : coordList.keySet()) {
            blockSet.add(b);
            Material mat = coordList.get(c);
            Coord modC = cMod(c, step);
            Block maybeBlock = b.getRelative(modC.x, modC.y, modC.z);
            if (maybeBlock == null || maybeBlock.getType() != mat) {
                return null;
            }
            blockSet.add(maybeBlock);
        }
        return blockSet;
    }

    private Coord cMod(Coord c, int step) {
        if (step == 1) {
            return new Coord(c.x * -1, c.y, c.z);
        }
        if (step == 2) {
            return new Coord(c.z, c.y, c.x);
        }
        if (step == 3) {
            return new Coord(c.z, c.y, c.x * -1);
        }
        return c; // step == 0
    }
}

class ActiveShrine {

    private String player;
    private Set<Block> blocks;
    private Deity deity;
    private String deityName;

    public ActiveShrine(Player player, Set<Block> blocks, Deity deity) {
        this.player = player.getName();
        this.blocks = blocks;
        this.deity = deity;
        this.deityName = deity.getDeityName();
        this.deity.shrineCreated(this.player);
    }

    /**
     * For loading.
     * @return 
     */
    public ActiveShrine(VoxConfig config) {
        String deityConf = config.getAttribute("Deity");
        String playerConf = config.getAttribute("Player");
        Set<Block> blockSet = new HashSet<Block>();
        VoxConfig blockConf = config.getChild("blocks");
        for (String key : blockConf.getChildren().keySet()) {

            if (blockConf.getChild(key) instanceof VoxConfigBlock) {
                VoxConfigBlock b = (VoxConfigBlock) blockConf.getChild(key);
                blockSet.add(b.getBlock());
            }
        }
        this.player = playerConf;
        this.blocks = blockSet;
        this.deityName = deityConf;
        this.deity = null;
    }

    public Set<Block> getBlocks() {
        return blocks;
    }

    public void activate(Deity d) {
        this.deity = d;
        this.deity.shrineCreated(player);
    }

    public String deityName() {
        return this.deityName;
    }

    /**
     * 
     * @param b
     * @param destroy destroy the shrine? sometimes we don't wanna--part of the piston code
     * @return 
     */
    public boolean checkBroken(Block b, boolean destroy) {
        if (blocks.contains(b)) {
            if (destroy) {
                deity.shrineDestroyed(player);
            }
            return true;
        }
        return false;
    }

    /**
     * For saving to file. Pass it the node it should write its data to.
     * @param config 
     */
    public void toConfig(VoxConfig config) {
        config.setAttribute("Player", player);
        config.setAttribute("Deity", deityName);
        VoxConfig blockConfig = config.getChild("blocks");
        int i = 0;
        for (Block block : blocks) {
            VoxConfigBlock node = new VoxConfigBlock(block);
            blockConfig.addChild("" + i, node);
            i++;
        }
    }
}