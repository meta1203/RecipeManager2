package ro.thehunters.digi.recipeManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import ro.thehunters.digi.recipeManager.flags.FlagType;
import ro.thehunters.digi.recipeManager.recipes.BaseRecipe;
import ro.thehunters.digi.recipeManager.recipes.CombineRecipe;
import ro.thehunters.digi.recipeManager.recipes.CraftRecipe;
import ro.thehunters.digi.recipeManager.recipes.ItemResult;
import ro.thehunters.digi.recipeManager.recipes.SmeltRecipe;

/**
 * Collection of conversion and useful methods
 */
public class Tools
{
    /**
     * Proper experience methods.
     * 
     * @author Essentials<br>
     *         https://github.com/essentials/Essentials/blob/master/Essentials/src/net/ess3/craftbukkit/SetExpFix.java
     */
    public static class Exp
    {
        // This method is used to update both the recorded total experience and displayed total experience.
        // We reset both types to prevent issues.
        public static void setTotalExperience(final Player player, final int exp)
        {
            if(exp < 0)
            {
                throw new IllegalArgumentException("Experience is negative!");
            }
            
            player.setExp(0);
            player.setLevel(0);
            player.setTotalExperience(0);
            
            // This following code is technically redundant now, as bukkit now calculates levels more or less correctly
            // At larger numbers however... player.getExp(3000), only seems to give 2999, putting the below calculations off.
            int amount = exp;
            
            while(amount > 0)
            {
                final int expToLevel = getExpAtLevel(player);
                amount -= expToLevel;
                
                if(amount >= 0)
                {
                    // give until next level
                    player.giveExp(expToLevel);
                }
                else
                {
                    // give the rest
                    amount += expToLevel;
                    player.giveExp(amount);
                    amount = 0;
                }
            }
        }
        
        private static int getExpAtLevel(final Player player)
        {
            return getExpAtLevel(player.getLevel());
        }
        
        public static int getExpAtLevel(final int level)
        {
            if(level > 29)
            {
                return 62 + (level - 30) * 7;
            }
            
            if(level > 15)
            {
                return 17 + (level - 15) * 3;
            }
            
            return 17;
        }
        
        public static int getExpToLevel(final int level)
        {
            int currentLevel = 0;
            int exp = 0;
            
            while(currentLevel < level)
            {
                exp += getExpAtLevel(currentLevel);
                currentLevel++;
            }
            
            return exp;
        }
        
        // This method is required because the bukkit player.getTotalExperience() method, shows exp that has been 'spent'.
        // Without this people would be able to use exp and then still sell it.
        public static int getTotalExperience(final Player player)
        {
            int exp = Math.round(getExpAtLevel(player) * player.getExp());
            int currentLevel = player.getLevel();
            
            while(currentLevel > 0)
            {
                currentLevel--;
                exp += getExpAtLevel(currentLevel);
            }
            
            return exp;
        }
        
        public static int getExpUntilNextLevel(final Player player)
        {
            int exp = Math.round(getExpAtLevel(player) * player.getExp());
            int nextLevel = player.getLevel();
            
            return getExpAtLevel(nextLevel) - exp;
        }
    }
    
    public static <T>T parseEnum(String name, T[] values)
    {
        if(name != null && !name.isEmpty())
        {
            name = Tools.parseAliasName(name);
            
            for(T t : values)
            {
                if(t != null)
                {
                    String s = Tools.parseAliasName(((Enum<?>)t).name());
                    
                    if(s.equals(name))
                    {
                        return t;
                    }
                }
            }
        }
        
        return null;
    }
    
    public static Enchantment parseEnchant(String value)
    {
        Enchantment enchant = null;
        
        try
        {
            enchant = Enchantment.getById(Integer.valueOf(value));
        }
        catch(NumberFormatException e)
        {
            value = Tools.parseAliasName(value);
            
            enchant = RecipeManager.getSettings().enchantNames.get(value);
        }
        
        if(enchant != null)
        {
            return enchant;
        }
        
        for(Enchantment e : Enchantment.values())
        {
            String s = e.getName().toLowerCase().replaceAll("[_\\s]+", "");
            
            if(s.equals(value))
            {
                return e;
            }
        }
        
        return null;
    }
    
    public static String removeExtensions(String value, Set<String> extensions)
    {
        int i = value.lastIndexOf('.');
        
        if(i > -1 && extensions.contains(value.substring(i).trim().toLowerCase()))
        {
            return value.substring(0, i);
        }
        
        return value;
    }
    
    public static String hideString(String string)
    {
        char[] data = new char[string.length() * 2];
        
        for(int i = 0; i < data.length; i += 2)
        {
            data[i] = ChatColor.COLOR_CHAR;
            data[i + 1] = string.charAt(i == 0 ? 0 : i / 2);
        }
        
        return new String(data);
    }
    
    public static String unhideString(String string)
    {
        return string.replace(String.valueOf(ChatColor.COLOR_CHAR), "");
    }
    
    public static class Item
    {
        public static ItemResult create(Material type, int data, int amount, String name, String... lore)
        {
            return create(type, data, amount, name, (lore != null && lore.length > 0 ? Arrays.asList(lore) : null));
        }
        
        public static ItemResult create(Material type, int data, int amount, String name, List<String> lore)
        {
            ItemResult item = new ItemResult(type, amount, (short)data, 100);
            ItemMeta meta = item.getItemMeta();
            
            if(meta == null)
            {
                return item;
            }
            
            if(lore != null)
            {
                meta.setLore(lore);
            }
            
            meta.setDisplayName(name);
            item.setItemMeta(meta);
            
            return item;
        }
        
        /**
         * Displays the itemstack in a user-friendly and colorful manner.<br>
         * If item is null or air it will print "nothing" in gray.<br>
         * If item is enchanted it will have aqua color instead of white.<br>
         * Uses aliases to display data values as well.<br>
         * Uses item's display name in italic font if available.<br>
         * <br>
         * NOTE: Will have a RESET color at the end, use {@link #print(ItemStack, ChatColor)} to use a diferent end-color instead.
         * 
         * @param item
         *            the item to print, can be null
         * @return user-friendly item print
         */
        public static String print(ItemStack item)
        {
            return print(item, ChatColor.WHITE, ChatColor.RESET, false);
        }
        
        /**
         * Displays the itemstack in a user-friendly and colorful manner.<br>
         * If item is null or air it will print "nothing" in gray.<br>
         * If item is enchanted it will have aqua color instead of white.<br>
         * Uses aliases to display data values as well.<br>
         * Uses item's display name in italic font if available.
         * 
         * @param item
         *            the item to print, can be null
         * @param defColor
         *            default color, usually white
         * @param endColor
         *            will be appended at the end of string, should be your text color
         * @return user-friendly item print
         */
        public static String print(ItemStack item, ChatColor defColor, ChatColor endColor, boolean alwaysShowAmount)
        {
            if(item == null || item.getTypeId() == 0)
            {
                return ChatColor.GRAY + "(nothing)";
            }
            
            String name = null;
            String itemData = null;
            
            ItemMeta meta = item.getItemMeta();
            
            if(meta != null && meta.hasDisplayName())
            {
                name = ChatColor.ITALIC + meta.getDisplayName();
            }
            else
            {
                name = RecipeManager.getSettings().materialPrint.get(item.getType());
                
                if(name == null)
                {
                    name = parseAliasPrint(item.getType().toString());
                }
            }
            
            Map<Short, String> dataMap = RecipeManager.getSettings().materialDataPrint.get(item.getType());
            
            if(dataMap != null)
            {
                itemData = dataMap.get(item.getDurability());
                
                if(itemData != null)
                {
                    itemData = itemData + " " + name;
                }
            }
            
            if(itemData == null)
            {
                short data = item.getDurability();
                
                if(data != 0)
                {
                    if(data == Vanilla.DATA_WILDCARD)
                    {
                        itemData = name + ChatColor.GRAY + ":" + Messages.ITEM_ANYDATA.get();
                    }
                    else
                    {
                        itemData = name + ChatColor.GRAY + ":" + data;
                    }
                }
                else
                {
                    itemData = name;
                }
            }
            
            String amount = (alwaysShowAmount || item.getAmount() > 1 ? item.getAmount() + "x " : "");
            ChatColor color = (item.getEnchantments().size() > 0 ? ChatColor.AQUA : defColor);
            
            return amount + color + itemData + (endColor == null ? "" : endColor);
        }
        
        public static String getName(ItemStack item)
        {
            String name = null;
            String itemData = null;
            
            ItemMeta meta = item.getItemMeta();
            
            if(meta != null && meta.hasDisplayName())
            {
                name = ChatColor.ITALIC + meta.getDisplayName();
            }
            else
            {
                name = RecipeManager.getSettings().materialPrint.get(item.getType());
                
                if(name == null)
                {
                    name = parseAliasPrint(item.getType().toString());
                }
            }
            
            Map<Short, String> dataMap = RecipeManager.getSettings().materialDataPrint.get(item.getType());
            
            if(dataMap != null)
            {
                itemData = dataMap.get(item.getDurability());
                
                if(itemData != null)
                {
                    itemData = itemData + " " + name;
                }
            }
            
            return (item.getEnchantments().size() > 0 ? ChatColor.AQUA : "") + (itemData == null ? name : itemData);
        }
        
        public static boolean isSimilarDataWildcard(ItemStack source, ItemStack item)
        {
            if(item == null)
            {
                return false;
            }
            
            if(item == source)
            {
                return true;
            }
            
            return source.getTypeId() == item.getTypeId() && (source.getDurability() == Vanilla.DATA_WILDCARD ? true : source.getDurability() == item.getDurability()) && source.hasItemMeta() == item.hasItemMeta() && (source.hasItemMeta() ? Bukkit.getItemFactory().equals(source.getItemMeta(), item.getItemMeta()) : true);
        }
        
        public static ItemStack nullIfAir(ItemStack item)
        {
            return (item == null || item.getTypeId() == 0 ? null : item);
        }
        
        public static ItemStack merge(ItemStack into, ItemStack item)
        {
            if(into == null || into.getTypeId() == 0)
            {
                return item;
            }
            
            if(item.isSimilar(into) && item.getAmount() <= (into.getMaxStackSize() - into.getAmount()))
            {
                ItemStack clone = item.clone();
                
                clone.setAmount(into.getAmount() + item.getAmount());
                
                return clone;
            }
            
            return null;
        }
        
        public static boolean canMerge(ItemStack intoItem, ItemStack item)
        {
            if(intoItem == null || intoItem.getTypeId() == 0)
            {
                return true;
            }
            
            if(intoItem.isSimilar(item) && item.getAmount() <= (intoItem.getMaxStackSize() - intoItem.getAmount()))
            {
                return true;
            }
            
            return false;
        }
    }
    
    public static int playerFreeSpaceForItem(Player player, ItemStack item)
    {
        Inventory inv = player.getInventory();
        
        int available = 0;
        
        for(ItemStack i : inv.getContents())
        {
            if(i == null)
            {
                available += item.getType().getMaxStackSize();
            }
            else if(item.isSimilar(i))
            {
                available += Math.max(Math.max(i.getMaxStackSize(), inv.getMaxStackSize()) - i.getAmount(), 0);
            }
        }
        
        return available;
    }
    
    public static boolean playerCanAddItem(Player player, ItemStack item)
    {
        Inventory inv = player.getInventory();
        
        int amount = item.getAmount();
        int available = 0;
        
        for(ItemStack i : inv.getContents())
        {
            if(i == null)
            {
                available += item.getType().getMaxStackSize();
            }
            else if(item.isSimilar(i))
            {
                available += Math.max(Math.max(i.getMaxStackSize(), inv.getMaxStackSize()) - i.getAmount(), 0);
            }
            
            if(available >= amount)
            {
                return true;
            }
        }
        
        return false;
    }
    
    public static String parseAliasName(String name)
    {
        return name.replaceAll("[\\s\\W_]+", "").trim().toLowerCase();
    }
    
    public static String parseAliasPrint(String name)
    {
        return WordUtils.capitalize(name.toLowerCase().replace('_', ' ').trim());
    }
    
    public static String printNumber(Number number)
    {
        return NumberFormat.getNumberInstance().format(number);
    }
    
    public static String printLocation(Location l)
    {
        return l.getWorld().getName() + ":" + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }
    
    public static String replaceVariables(String msg, Object... variables)
    {
        if(variables != null && variables.length > 0)
        {
            if(variables.length % 2 > 0)
            {
                throw new IllegalArgumentException("Variables argument must have pairs of 2 arguments!");
            }
            
            for(int i = 0; i < variables.length; i += 2) // loop 2 by 2
            {
                msg = msg.replace(variables[i].toString(), variables[i + 1].toString());
            }
        }
        
        return msg;
    }
    
    public class ParseBit
    {
        public static final byte NO_ERRORS = 1 << 0;
        public static final byte NO_WARNINGS = 1 << 1;
        public static final byte NO_PRINT = NO_ERRORS | NO_WARNINGS;
        
        public static final byte NO_DATA = 1 << 2;
        public static final byte NO_AMOUNT = 1 << 3;
        
        public static final byte NO_ENCHANTMENTS = 1 << 5;
        public static final byte NO_NAME = 1 << 6;
        public static final short NO_LORE = 1 << 7;
        public static final short NO_COLOR = 1 << 8;
        public static final short NO_META = NO_ENCHANTMENTS | NO_NAME | NO_LORE | NO_COLOR;
    }
    
    public static ItemResult parseItemResult(String string, int defaultData)
    {
        return parseItemResult(string, defaultData, 0);
    }
    
    public static ItemResult parseItemResult(String string, int defaultData, int settings)
    {
        String[] split = string.substring(1).trim().split("%");
        ItemResult result = new ItemResult();
        result.setChance(-1);
        
        if(split.length >= 2)
        {
            string = split[0].trim();
            
            if(!string.equals("*") && !string.equalsIgnoreCase("calc"))
            {
                try
                {
                    result.setChance(Math.min(Math.max(Float.valueOf(string), 0), 100));
                }
                catch(NumberFormatException e)
                {
                    ErrorReporter.warning("Invalid percentage number: " + string);
                }
            }
            
            string = split[1];
        }
        else
        {
            string = split[0];
        }
        
        ItemStack item = parseItem(string, defaultData, settings);
        
        if(item == null)
        {
            return null;
        }
        
        result.setItemStack(item);
        
        return result;
    }
    
    public static ItemStack parseItem(String value, int defaultData)
    {
        return parseItem(value, defaultData, 0);
    }
    
    public static ItemStack parseItem(String value, int defaultData, int settings)
    {
        value = value.trim();
        
        if(value.length() == 0)
        {
            return null;
        }
        
        String[] args = value.split(";");
        String[] split = args[0].trim().split(":");
        
        if(split.length <= 0 || split[0].isEmpty())
        {
            return new ItemStack(0);
        }
        
        value = split[0].trim();
        
        Material material = RecipeManager.getSettings().materialNames.get(Tools.parseAliasName(value));
        
        if(material == null)
        {
            material = Material.matchMaterial(value);
        }
        
        if(material == null)
        {
            if((settings & ParseBit.NO_ERRORS) != ParseBit.NO_ERRORS)
            {
                ErrorReporter.error("Item '" + value + "' does not exist!", "Name could be different, look in '" + Files.FILE_INFO_NAMES + "' or '" + Files.FILE_ITEM_ALIASES + "' for material names.");
            }
            
            return null;
        }
        
        int type = material.getId();
        
        if(type <= 0)
        {
            return new ItemStack(0);
        }
        
        int data = defaultData;
        
        if(split.length > 1)
        {
            if((settings & ParseBit.NO_DATA) != ParseBit.NO_DATA)
            {
                value = split[1].toLowerCase().trim();
                
                if(value.charAt(0) == '*' || value.equals("any"))
                {
                    data = Vanilla.DATA_WILDCARD;
                }
                else
                {
                    Map<String, Short> dataMap = RecipeManager.getSettings().materialDataNames.get(material);
                    Short dataValue = (dataMap != null ? dataMap.get(Tools.parseAliasName(value)) : null);
                    
                    if(dataValue != null)
                    {
                        data = dataValue.shortValue();
                    }
                    else
                    {
                        try
                        {
                            data = Integer.valueOf(value);
                        }
                        catch(NumberFormatException e)
                        {
                            if((settings & ParseBit.NO_WARNINGS) != ParseBit.NO_WARNINGS)
                            {
                                ErrorReporter.warning("Item '" + material + " has unknown data number/alias: '" + value + "', defaulting to " + defaultData);
                            }
                        }
                    }
                    
                    if(data == -1)
                    {
                        if((settings & ParseBit.NO_WARNINGS) != ParseBit.NO_WARNINGS)
                        {
                            ErrorReporter.warning("Item '" + material + "' has data value -1, use * instead!", "The -1 value no longer works since Minecraft 1.5, for future compatibility use * instead or don't define a data value.");
                        }
                    }
                }
            }
            else
            {
                if((settings & ParseBit.NO_WARNINGS) != ParseBit.NO_WARNINGS)
                {
                    ErrorReporter.warning("Item '" + material + "' can't have data value defined here, data value ignored.");
                }
            }
        }
        
        int amount = 1;
        
        if(split.length > 2)
        {
            if((settings & ParseBit.NO_AMOUNT) != ParseBit.NO_AMOUNT)
            {
                value = split[2].trim();
                
                try
                {
                    amount = Integer.valueOf(value);
                }
                catch(NumberFormatException e)
                {
                    if((settings & ParseBit.NO_WARNINGS) != ParseBit.NO_WARNINGS)
                    {
                        ErrorReporter.warning("Item '" + material + "' has amount value that is not a number: " + value + ", defaulting to 1");
                    }
                }
            }
            else
            {
                if((settings & ParseBit.NO_WARNINGS) != ParseBit.NO_WARNINGS)
                {
                    ErrorReporter.warning("Item '" + material + "' can't have amount defined here, amount ignored.");
                }
            }
        }
        
        ItemStack item = new ItemStack(type, amount, (short)data);
        
        if(args.length > 1)
        {
            ItemMeta meta = item.getItemMeta();
            
            if(meta == null && (settings & ParseBit.NO_WARNINGS) != ParseBit.NO_WARNINGS)
            {
                ErrorReporter.warning("The " + type + " material doesn't support item meta, name/lore/enchants ignored.");
                return item;
            }
            
            String original;
            
            for(int i = 1; i < args.length; i++)
            {
                original = args[i].trim();
                value = original.toLowerCase();
                
                if(value.startsWith("name"))
                {
                    value = original.substring("name".length()).trim();
                    
                    meta.setDisplayName(Tools.parseColors(value, false));
                }
                else if(value.startsWith("lore"))
                {
                    value = original.substring("lore".length()).trim();
                    
                    List<String> lore = meta.getLore();
                    
                    if(lore == null)
                    {
                        lore = new ArrayList<String>();
                    }
                    
                    lore.add(Tools.parseColors(value, false));
                    meta.setLore(lore);
                }
                else if(value.startsWith("enchant"))
                {
                    split = value.substring("enchant".length()).trim().split(" ");
                    value = split[0].trim();
                    
                    Enchantment enchant = Tools.parseEnchant(value);
                    
                    if(enchant == null && (settings & ParseBit.NO_WARNINGS) != ParseBit.NO_WARNINGS)
                    {
                        ErrorReporter.error("Invalid enchantment: " + value, "Read '" + Files.FILE_INFO_NAMES + "' for enchantment names.");
                        continue;
                    }
                    
                    int level = enchant.getStartLevel();
                    
                    if(split.length > 1)
                    {
                        value = split[1].trim();
                        
                        if(!value.equals("max"))
                        {
                            try
                            {
                                level = Integer.valueOf(value);
                            }
                            catch(NumberFormatException e)
                            {
                                if((settings & ParseBit.NO_WARNINGS) != ParseBit.NO_WARNINGS)
                                {
                                    ErrorReporter.error("Invalid enchantment level number: " + value);
                                    continue;
                                }
                            }
                        }
                        else
                        {
                            level = enchant.getMaxLevel();
                        }
                    }
                    
                    item.addUnsafeEnchantment(enchant, level);
                }
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public static Potion parsePotion(String value, FlagType type)
    {
        String[] split = value.toLowerCase().split("\\|");
        
        if(split.length == 0)
        {
            ErrorReporter.error("Flag " + type + " doesn't have any arguments!", "It must have at least 'type' argument, read '" + Files.FILE_INFO_NAMES + "' for potion types list.");
            return null;
        }
        
        Potion potion = new Potion(null);
        boolean splash = false;
        boolean extended = false;
        int level = 1;
        
        for(String s : split)
        {
            s = s.trim();
            
            if(s.equals("splash"))
            {
                splash = true;
            }
            else if(s.equals("extended"))
            {
                extended = true;
            }
            else if(s.startsWith("type"))
            {
                split = s.split(" ", 2);
                
                if(split.length <= 1)
                {
                    ErrorReporter.error("Flag " + type + " has 'type' argument with no type!", "Read '" + Files.FILE_INFO_NAMES + "' for potion types.");
                    return null;
                }
                
                value = split[1].trim();
                
                try
                {
                    potion.setType(PotionType.valueOf(value.toUpperCase()));
                }
                catch(IllegalArgumentException e)
                {
                    ErrorReporter.error("Flag " + type + " has invalid 'type' argument value: " + value, "Read '" + Files.FILE_INFO_NAMES + "' for potion types.");
                    return null;
                }
            }
            else if(s.startsWith("level"))
            {
                split = s.split(" ", 2);
                
                if(split.length <= 1)
                {
                    ErrorReporter.error("Flag " + type + " has 'level' argument with no level!");
                    continue;
                }
                
                value = split[1].trim();
                
                if(value.equals("max"))
                {
                    level = 9999;
                }
                else
                {
                    try
                    {
                        level = Integer.valueOf(value);
                    }
                    catch(NumberFormatException e)
                    {
                        ErrorReporter.error("Flag " + type + " has invalid 'level' number: " + value);
                    }
                }
            }
            else
            {
                ErrorReporter.error("Flag " + type + " has unknown argument: " + s, "Maybe it's spelled wrong, check it in '" + Files.FILE_INFO_FLAGS + "' file.");
            }
        }
        
        if(potion.getType() == null)
        {
            ErrorReporter.error("Flag " + type + " is missing 'type' argument !", "Read '" + Files.FILE_INFO_NAMES + "' for potion types.");
            return null;
        }
        
        if(potion.getType().getMaxLevel() > 0)
        {
            potion.setLevel(Math.min(Math.max(level, 1), potion.getType().getMaxLevel()));
        }
        
        if(!potion.getType().isInstant())
        {
            potion.setHasExtendedDuration(extended);
        }
        
        potion.setSplash(splash);
        
        return potion;
    }
    
    public static PotionEffect parsePotionEffect(String value, FlagType type)
    {
        String[] split = value.toLowerCase().split("\\|");
        
        if(split.length == 0)
        {
            ErrorReporter.error("Flag " + type + " doesn't have any arguments!", "It must have at least 'type' argument, read '" + Files.FILE_INFO_NAMES + "' for potion effect types list.");
            return null;
        }
        
        PotionEffectType effectType = null;
        boolean ambient = false;
        float duration = 1;
        int amplify = 0;
        
        for(String s : split)
        {
            s = s.trim();
            
            if(s.equals("ambient"))
            {
                ambient = true;
            }
            else if(s.startsWith("type"))
            {
                split = s.split(" ", 2);
                
                if(split.length <= 1)
                {
                    ErrorReporter.error("Flag " + type + " has 'type' argument with no type!", "Read '" + Files.FILE_INFO_NAMES + "' for potion effect types.");
                    return null;
                }
                
                value = split[1].trim();
                effectType = PotionEffectType.getByName(value.toUpperCase());
                
                if(effectType == null)
                {
                    ErrorReporter.error("Flag " + type + " has invalid 'type' argument value: " + value, "Read '" + Files.FILE_INFO_NAMES + "' for potion effect types.");
                    return null;
                }
            }
            else if(s.startsWith("duration"))
            {
                split = s.split(" ", 2);
                
                if(split.length <= 1)
                {
                    ErrorReporter.error("Flag " + type + " has 'duration' argument with no number!");
                    continue;
                }
                
                value = split[1].trim();
                
                try
                {
                    duration = Float.valueOf(value);
                }
                catch(NumberFormatException e)
                {
                    ErrorReporter.error("Flag " + type + " has invalid 'duration' number: " + value);
                }
            }
            else if(s.startsWith("amplify"))
            {
                split = s.split(" ", 2);
                
                if(split.length <= 1)
                {
                    ErrorReporter.error("Flag " + type + " has 'amplify' argument with no number!");
                    continue;
                }
                
                value = split[1].trim();
                
                try
                {
                    amplify = Integer.parseInt(value);
                }
                catch(NumberFormatException e)
                {
                    ErrorReporter.error("Flag " + type + " has invalid 'amplify' number: " + value);
                }
            }
            else
            {
                ErrorReporter.warning("Flag " + type + " has unknown argument: " + s, "Maybe it's spelled wrong, check it in '" + Files.FILE_INFO_FLAGS + "' file.");
            }
        }
        
        if(effectType == null)
        {
            ErrorReporter.error("Flag " + type + " is missing 'type' argument !", "Read '" + Files.FILE_INFO_NAMES + "' for potion effect types.");
            return null;
        }
        
        if(duration != 1 && (effectType == PotionEffectType.HEAL || effectType == PotionEffectType.HARM))
        {
            ErrorReporter.warning("Flag " + type + " can't have duration on HEAL or HARM because they're instant!");
        }
        
        return new PotionEffect(effectType, Math.round(duration * 20), amplify, ambient);
    }
    
    public static FireworkEffect parseFireworkEffect(String value, FlagType type)
    {
        String[] split = value.toLowerCase().split("\\|");
        
        if(split.length == 0)
        {
            ErrorReporter.error("Flag " + type + " doesn't have any arguments!", "It must have at least one 'color' argument, read '" + Files.FILE_INFO_FLAGS + "' for syntax.");
            return null;
        }
        
        Builder build = FireworkEffect.builder();
        
        for(String s : split)
        {
            s = s.trim();
            
            if(s.equals("trail"))
            {
                build.withTrail();
            }
            else if(s.equals("flicker"))
            {
                build.withFlicker();
            }
            else if(s.startsWith("color"))
            {
                split = s.split(" ", 2);
                
                if(split.length <= 1)
                {
                    ErrorReporter.error("Flag " + type + " has 'color' argument with no colors!", "Add colors separated by , in RGB format (3 numbers ranged 0-255)");
                    return null;
                }
                
                split = split[1].split(",");
                List<Color> colors = new ArrayList<Color>();
                Color color;
                
                for(String c : split)
                {
                    color = Tools.parseColor(c.trim());
                    
                    if(color == null)
                    {
                        ErrorReporter.warning("Flag " + type + " has an invalid color!");
                    }
                    else
                    {
                        colors.add(color);
                    }
                }
                
                if(colors.isEmpty())
                {
                    ErrorReporter.error("Flag " + type + " doesn't have any valid colors, they are required!");
                    return null;
                }
                
                build.withColor(colors);
            }
            else if(s.startsWith("fadecolor"))
            {
                split = s.split(" ", 2);
                
                if(split.length <= 1)
                {
                    ErrorReporter.error("Flag " + type + " has 'fadecolor' argument with no colors!", "Add colors separated by , in RGB format (3 numbers ranged 0-255)");
                    return null;
                }
                
                split = split[1].split(",");
                List<Color> colors = new ArrayList<Color>();
                Color color;
                
                for(String c : split)
                {
                    color = Tools.parseColor(c.trim());
                    
                    if(color == null)
                    {
                        ErrorReporter.warning("Flag " + type + " has an invalid fade color! Moving on...");
                    }
                    else
                    {
                        colors.add(color);
                    }
                }
                
                if(colors.isEmpty())
                {
                    ErrorReporter.error("Flag " + type + " doesn't have any valid fade colors! Moving on...");
                }
                else
                {
                    build.withFade(colors);
                }
            }
            else if(s.startsWith("type"))
            {
                split = s.split(" ", 2);
                
                if(split.length <= 1)
                {
                    ErrorReporter.error("Flag " + type + " has 'type' argument with no value!", "Read " + Files.FILE_INFO_NAMES + " for list of firework effect types.");
                    return null;
                }
                
                value = split[1].trim();
                
                try
                {
                    build.with(FireworkEffect.Type.valueOf(value.toUpperCase()));
                }
                catch(IllegalArgumentException e)
                {
                    ErrorReporter.error("Flag " + type + " has invalid 'type' setting value: " + value, "Read " + Files.FILE_INFO_NAMES + " for list of firework effect types.");
                    return null;
                }
            }
            else
            {
                ErrorReporter.warning("Flag " + type + " has unknown argument: " + s, "Maybe it's spelled wrong, check it in " + Files.FILE_INFO_FLAGS + " file.");
            }
        }
        
        return build.build();
    }
    
    public static String collectionToString(Collection<?> collection)
    {
        if(collection.isEmpty())
        {
            return "";
        }
        
        StringBuilder s = new StringBuilder(collection.size() * 16);
        boolean first = true;
        
        for(Object o : collection)
        {
            if(first)
            {
                first = false;
            }
            else
            {
                s.append(", ");
            }
            
            s.append(o.toString());
        }
        
        return s.toString();
    }
    
    public static Color parseColor(String rgbString)
    {
        String[] split = rgbString.split(" ");
        
        if(split.length == 3)
        {
            try
            {
                int r = Integer.valueOf(split[0].trim());
                int g = Integer.valueOf(split[1].trim());
                int b = Integer.valueOf(split[2].trim());
                
                return Color.fromRGB(r, g, b);
            }
            catch(Throwable e)
            {
            }
        }
        
        return null;
    }
    
    public static String parseColors(String message, boolean removeColors)
    {
        for(ChatColor color : ChatColor.values())
        {
            message = message.replaceAll("(?i)<" + color.name() + ">", (removeColors ? "" : color.toString()));
        }
        
        return removeColors ? ChatColor.stripColor(message) : ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * For use in furnace smelting and fuel recipes hashmap
     */
    public static String convertItemToStringId(ItemStack item)
    {
        return item.getTypeId() + (item.getDurability() == Vanilla.DATA_WILDCARD ? "" : ":" + item.getDurability());
    }
    
    /**
     * For use in shaped/shapeless recipe's result
     */
    public static ItemStack createItemRecipeId(ItemStack result, int id)
    {
        result = result.clone();
        ItemMeta meta = result.getItemMeta();
        
        if(meta == null)
        {
            Messages.error(null, new IllegalAccessError(), "Can't mark result because it doesn't support item meta!");
            return result;
        }
        
        List<String> lore = meta.getLore();
        
        if(lore == null)
        {
            lore = new ArrayList<String>();
        }
        
        lore.add(Recipes.RECIPE_ID_STRING + id);
        meta.setLore(lore);
        result.setItemMeta(meta);
        
        return result;
    }
    
    public static int getRecipeIdFromItem(ItemStack result)
    {
        if(!result.hasItemMeta())
        {
            return -1;
        }
        
        ItemMeta meta = result.getItemMeta();
        
        if(meta == null)
        {
            return -1;
        }
        
        List<String> lore = meta.getLore();
        
        if(lore == null || lore.isEmpty())
        {
            return -1;
        }
        
        for(int i = 0; i < lore.size(); i++)
        {
            String s = lore.get(i);
            
            if(s != null && s.startsWith(Recipes.RECIPE_ID_STRING))
            {
                try
                {
                    return Integer.valueOf(s.substring(Recipes.RECIPE_ID_STRING.length()));
                }
                catch(Throwable e)
                {
                    Messages.debug("Invalid recipe identifier found: " + s);
                    break;
                }
            }
        }
        
        return -1;
    }
    
    public static int findItemInIngredients(BaseRecipe recipe, Material type, Short data)
    {
        int found = 0;
        
        if(recipe instanceof CraftRecipe)
        {
            CraftRecipe r = (CraftRecipe)recipe;
            
            for(ItemStack i : r.getIngredients())
            {
                if(i == null)
                {
                    continue;
                }
                
                if(i.getType() == type && (data == null || data == Vanilla.DATA_WILDCARD ? true : i.getDurability() == data))
                {
                    found++;
                }
            }
        }
        else if(recipe instanceof CombineRecipe)
        {
            CombineRecipe r = (CombineRecipe)recipe;
            
            for(ItemStack i : r.getIngredients())
            {
                if(i == null)
                {
                    continue;
                }
                
                if(i.getType() == type && (data == null || data == Vanilla.DATA_WILDCARD ? true : i.getDurability() == data))
                {
                    found++;
                }
            }
        }
        else if(recipe instanceof SmeltRecipe)
        {
            SmeltRecipe r = (SmeltRecipe)recipe;
            ItemStack i = r.getIngredient();
            
            if(i.getType() == type && (data == null || data == Vanilla.DATA_WILDCARD ? true : i.getDurability() == data))
            {
                found++;
            }
        }
        
        return found;
    }
    
    public static boolean compareShapedRecipeToMatrix(ShapedRecipe recipe, ItemStack[] matrix, ItemStack[] matrixMirror)
    {
        ItemStack[] ingredients = Tools.convertShapedRecipeToItemMatrix(recipe);
        
        boolean result = compareItemMatrix(ingredients, matrix);
        
        if(!result)
        {
            result = compareItemMatrix(ingredients, matrixMirror);
        }
        
        return result;
    }
    
    public static boolean compareItemMatrix(ItemStack[] ingredients, ItemStack[] matrix)
    {
        for(int i = 0; i < 9; i++)
        {
            if(matrix[i] == null && ingredients[i] == null)
            {
                continue;
            }
            
            if(matrix[i] == null || ingredients[i] == null || ingredients[i].getTypeId() != matrix[i].getTypeId() || (ingredients[i].getDurability() != Vanilla.DATA_WILDCARD && ingredients[i].getDurability() != matrix[i].getDurability()))
            {
                return false;
            }
        }
        
        return true;
    }
    
    public static ItemStack[] convertShapedRecipeToItemMatrix(ShapedRecipe bukkitRecipe)
    {
        Map<Character, ItemStack> items = bukkitRecipe.getIngredientMap();
        ItemStack[] matrix = new ItemStack[9];
        String[] shape = bukkitRecipe.getShape();
        int slot = 0;
        
        for(int r = 0; r < shape.length; r++)
        {
            for(char col : shape[r].toCharArray())
            {
                matrix[slot] = items.get(col);
                slot++;
            }
            
            slot = ((r + 1) * 3);
        }
        
        trimItemMatrix(matrix);
        
        return matrix;
    }
    
    public static ItemStack[] mirrorItemMatrix(ItemStack[] matrix)
    {
        ItemStack[] m = new ItemStack[9];
        
        for(int r = 0; r < 3; r++)
        {
            m[(r * 3)] = matrix[(r * 3) + 2];
            m[(r * 3) + 1] = matrix[(r * 3) + 1];
            m[(r * 3) + 2] = matrix[(r * 3)];
        }
        
        trimItemMatrix(m);
        
        return m;
    }
    
    public static void trimItemMatrix(ItemStack[] matrix)
    {
        while(matrix[0] == null && matrix[1] == null && matrix[2] == null)
        {
            matrix[0] = matrix[3];
            matrix[1] = matrix[4];
            matrix[2] = matrix[5];
            
            matrix[3] = matrix[6];
            matrix[4] = matrix[7];
            matrix[5] = matrix[8];
            
            matrix[6] = null;
            matrix[7] = null;
            matrix[8] = null;
        }
        
        while(matrix[0] == null && matrix[3] == null && matrix[6] == null)
        {
            matrix[0] = matrix[1];
            matrix[3] = matrix[4];
            matrix[6] = matrix[7];
            
            matrix[1] = matrix[2];
            matrix[4] = matrix[5];
            matrix[7] = matrix[8];
            
            matrix[2] = null;
            matrix[5] = null;
            matrix[8] = null;
        }
    }
    
    public static boolean compareIngredientList(List<ItemStack> sortedIngr, List<ItemStack> ingredients)
    {
        int size = ingredients.size();
        
        if(size != sortedIngr.size())
        {
            return false;
        }
        
        sortIngredientList(ingredients);
        
        for(int i = 0; i < size; i++)
        {
            if(!sortedIngr.get(i).equals(ingredients.get(i)))
            {
                return false;
            }
        }
        
        return true;
    }
    
    public static void sortIngredientList(List<ItemStack> ingredients)
    {
        Collections.sort(ingredients, new Comparator<ItemStack>()
        {
            int id1;
            int id2;
            
            @Override
            public int compare(ItemStack item1, ItemStack item2)
            {
                id1 = item1.getTypeId();
                id2 = item2.getTypeId();
                
                return (id1 == id2 ? (item1.getDurability() > item2.getDurability() ? -1 : 1) : (id1 > id2 ? -1 : 1));
            }
        });
    }
    
    public static String convertShapedRecipeToString(ShapedRecipe recipe)
    {
        StringBuilder str = new StringBuilder("s_");
        
        for(Entry<Character, ItemStack> entry : recipe.getIngredientMap().entrySet())
        {
            if(entry.getKey() != null && entry.getValue() != null)
            {
                str.append(entry.getKey()).append("=").append(entry.getValue().getTypeId()).append(":").append(entry.getValue().getDurability()).append(";");
            }
        }
        
        for(String row : recipe.getShape())
        {
            str.append(row).append(";");
        }
        
        return str.toString();
    }
    
    public static String convertShapelessRecipeToString(ShapelessRecipe recipe)
    {
        StringBuilder str = new StringBuilder("l_");
        
        for(ItemStack ingredient : recipe.getIngredientList())
        {
            if(ingredient == null)
            {
                continue;
            }
            
            str.append(ingredient.getTypeId()).append(":").append(ingredient.getDurability()).append(";");
        }
        
        return str.toString();
    }
    
    public static String convertFurnaceRecipeToString(FurnaceRecipe recipe)
    {
        return "f_" + recipe.getInput().getTypeId() + ":" + recipe.getInput().getDurability();
    }
    
    public static String convertLocationToString(Location location)
    {
        return location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }
    
    public static boolean saveTextToFile(String text, String filePath)
    {
        try
        {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            BufferedWriter stream = new BufferedWriter(new FileWriter(file, false));
            stream.write(text);
            stream.close();
            return true;
        }
        catch(Throwable e)
        {
            Messages.error(null, e, null);
        }
        
        return false;
    }
}
