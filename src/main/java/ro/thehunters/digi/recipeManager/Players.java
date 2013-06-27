package ro.thehunters.digi.recipeManager;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import ro.thehunters.digi.recipeManager.commands.RecipeCommand;

public class Players
{
    private static final Map<String, Integer> joined = new HashMap<String, Integer>();
    
    private Players()
    {
    }
    
    protected static void init()
    {
    }
    
    protected static void clean()
    {
        joined.clear();
        RecipeCommand.clean();
    }
    
    protected static void remove(Player player)
    {
        removeJoined(player);
        RecipeCommand.clean(player.getName());
    }
    
    public static void addJoined(Player player)
    {
        Validate.notNull(player, "player can not be null");
        
        joined.put(player.getName(), (int)System.currentTimeMillis() / 1000);
    }
    
    public static void removeJoined(Player player)
    {
        Validate.notNull(player, "player can not be null");
        
        joined.remove(player.getName());
    }
    
    public static Integer getJoinedTime(Player player)
    {
        return joined.get(player.getName());
    }
}
