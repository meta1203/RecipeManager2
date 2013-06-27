package ro.thehunters.digi.recipeManager.flags;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;

import ro.thehunters.digi.recipeManager.Messages;
import ro.thehunters.digi.recipeManager.ErrorReporter;
import ro.thehunters.digi.recipeManager.Tools;

public class FlagWeather extends Flag
{
    // Flag definition and documentation
    
    private static final FlagType TYPE;
    protected static final String[] A;
    protected static final String[] D;
    protected static final String[] E;
    
    static
    {
        TYPE = FlagType.WEATHER;
        
        A = new String[]
        {
            "{flag} <type>, [type] | [fail message]",
        };
        
        D = new String[]
        {
            "Sets the weather type(s) required to allow crafting.",
            "Using this flag more than once will overwrite the previous one.",
            "",
            "The 'type' argument can be:",
            "  clear    = clear skies, no precipitation.",
            "  downfall = precipitation (rain/snow depends on biome).",
            "  thunder  = precipitation + thundering.",
            "You can set more than one type separated by , character, but setting all of them is pointless.",
            "",
            "Optionally you can set the 'fail message' argument to overwrite the failure message or set it to 'false' to hide it.",
            "In the fail message you can use the following variables:",
            "  {weather} = the weather type required.",
            "",
            "NOTE: If you need to check if it's raining or snowing then use the " + FlagType.BIOME + " flag.",
        };
        
        E = new String[]
        {
            "{flag} downfall // works only if it's raining peacefully.",
            "{flag} clear, thunder | <red>To be struck by lightning... or to be not.",
        };
    }
    
    // Flag code
    
    public class Bit
    {
        public static final byte CLEAR = 1 << 0;
        public static final byte DOWNFALL = 1 << 1;
        public static final byte THUNDER = 1 << 2;
    }
    
    private byte weather;
    private String failMessage;
    
    public FlagWeather()
    {
    }
    
    public FlagWeather(FlagWeather flag)
    {
        weather = flag.weather;
        failMessage = flag.failMessage;
    }
    
    @Override
    public FlagWeather clone()
    {
        return new FlagWeather(this);
    }
    
    @Override
    public FlagType getType()
    {
        return TYPE;
    }
    
    /**
     * Get the set weather requirement.<br>
     * 
     * @return 0 = none, 1 = rain/snow, 2 = thunder
     */
    public byte getWeather()
    {
        return weather;
    }
    
    /**
     * Set the weather requirement.<br>
     * 
     * @param weather
     *            0 = none, 1 = rain/snow, 2 = thunder
     */
    public void setWeather(int weather)
    {
        this.weather = (byte)weather;
    }
    
    public String getWeatherString()
    {
        List<String> list = new ArrayList<String>(3);
        
        if((weather & Bit.CLEAR) == Bit.CLEAR)
        {
            list.add("clear");
        }
        
        if((weather & Bit.DOWNFALL) == Bit.DOWNFALL)
        {
            list.add("downfall");
        }
        
        if((weather & Bit.THUNDER) == Bit.THUNDER)
        {
            list.add("thunder");
        }
        
        return Tools.collectionToString(list);
    }
    
    public String getFailMessage()
    {
        return failMessage;
    }
    
    public void setFailMessage(String failMessage)
    {
        this.failMessage = failMessage;
    }
    
    @Override
    protected boolean onParse(String value)
    {
        String[] split = value.split("\\|");
        
        if(split.length > 1)
        {
            setFailMessage(split[1].trim());
        }
        
        split = split[0].toLowerCase().split(",");
        
        for(String arg : split)
        {
            arg = arg.trim();
            
            switch(arg.charAt(0))
            {
                case 'c':
                case 'n':
                    weather |= Bit.CLEAR;
                    break;
                
                case 'd':
                case 'r':
                case 's':
                    weather |= Bit.DOWNFALL;
                    break;
                
                case 't':
                case 'l':
                    weather |= Bit.THUNDER;
                    break;
                
                default:
                    ErrorReporter.warning("Flag " + getType() + " has unknown weather type: " + value);
            }
        }
        
        if(weather == 0)
        {
            ErrorReporter.error("Flag " + getType() + " needs at least one valid weather type!");
            return false;
        }
        
        return true;
    }
    
    @Override
    protected void onCheck(Args a)
    {
        if(!a.hasLocation())
        {
            a.addCustomReason("Needs location!");
            return;
        }
        
        World w = a.location().getWorld();
        byte weather = (w.hasStorm() ? (w.isThundering() ? Bit.THUNDER : Bit.DOWNFALL) : Bit.CLEAR);
        
        if((getWeather() & weather) != weather)
        {
            a.addReason(Messages.FLAG_WEATHER, failMessage, "{weather}", getWeatherString());
        }
    }
    
    /*
    @Override
    public List<String> information()
    {
        List<String> list = new ArrayList<String>(1);
        
        list.add(Messages.FLAG_WEATHER.get("{weather}", getWeatherString()));
        
        return list;
    }
    */
}
