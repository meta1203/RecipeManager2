package ro.thehunters.digi.recipeManager.flags;

import ro.thehunters.digi.recipeManager.ErrorReporter;
import ro.thehunters.digi.recipeManager.Messages;
import ro.thehunters.digi.recipeManager.recipes.BaseRecipe;
import ro.thehunters.digi.recipeManager.recipes.RecipeInfo.RecipeStatus;

public class FlagOverride extends Flag
{
    // Flag definition and documentation
    
    private static final FlagType TYPE;
    protected static final String[] A;
    protected static final String[] D;
    protected static final String[] E;
    
    static
    {
        TYPE = FlagType.OVERRIDE;
        
        A = new String[]
        {
            "{flag} [true or false]",
        };
        
        D = new String[]
        {
            "Overwrites an existing recipe from vanilla Minecraft or other plugins/mods.",
            "The recipe definition must have the exact ingredients of the recipe you want to overwrite.",
            "",
            "You may set whatever result(s) you want and add any other flags, this flag allows RecipeManager to take control over that recipe.",
            "If you don't know the exact ingredients you can use 'rmextract' command to extract all existing recipes in RecipeManager format.",
            "",
            "Value is optional, if value is not specified it will just be enabled.",
        };
        
        E = new String[]
        {
            "{flag}",
        };
    }
    
    // Flag code
    
    public FlagOverride()
    {
    }
    
    public FlagOverride(FlagOverride flag)
    {
    }
    
    @Override
    public FlagOverride clone()
    {
        return new FlagOverride(this);
    }
    
    @Override
    public FlagType getType()
    {
        return TYPE;
    }
    
    @Override
    protected boolean onValidate()
    {
        if(getFlagsContainer().hasFlag(FlagType.REMOVE))
        {
            return ErrorReporter.error("Flag " + getType() + " can't work with @remove flag!");
        }
        
        return true;
    }
    
    @Override
    protected boolean onParse(String value)
    {
        return true;
    }
    
    @Override
    protected void onRegistered()
    {
        BaseRecipe recipe = getRecipe();
        
        if(recipe == null)
        {
            Messages.debug("ERROR: invalid recipe pointer: " + recipe);
            remove();
        }
        else
        {
            recipe.getInfo().setStatus(RecipeStatus.OVERRIDDEN);
        }
    }
    
    /*
    @Override
    public List<String> information()
    {
        List<String> list = new ArrayList<String>(1);
        
        list.add(Messages.FLAG_OVERRIDE.get());
        
        return list;
    }
    */
}
