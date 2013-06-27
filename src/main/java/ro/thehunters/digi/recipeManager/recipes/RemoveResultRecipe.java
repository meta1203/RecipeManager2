package ro.thehunters.digi.recipeManager.recipes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import ro.thehunters.digi.recipeManager.RecipeManager;
import ro.thehunters.digi.recipeManager.recipes.RecipeInfo.RecipeOwner;

public class RemoveResultRecipe extends BaseRecipe
{
    private Map<BaseRecipe, RecipeInfo> removed;
    private ItemStack result;
    
    public RemoveResultRecipe(ItemStack result)
    {
        Validate.notNull(result, "Result must not be null!");
        
        this.result = result;
    }
    
    /**
     * @return copy of the result item
     */
    public ItemStack getResult()
    {
        return result.clone();
    }
    
    // TODO trigger this somewhere..
    public void apply()
    {
        if(removed != null)
        {
            throw new IllegalAccessError("Recipe has already been applied!");
        }
        
        removed = new HashMap<BaseRecipe, RecipeInfo>();
        
        for(Entry<BaseRecipe, RecipeInfo> e : RecipeManager.getRecipes().getRecipeList().entrySet())
        {
            BaseRecipe recipe = e.getKey();
            RecipeInfo info = e.getValue();
            
            if(info.getOwner() != RecipeOwner.RECIPEMANAGER)
            {
                ItemStack result = null;
                
                if(recipe instanceof MultiResultRecipe)
                {
                    MultiResultRecipe r = (MultiResultRecipe)recipe;
                    result = r.getFirstResult();
                }
                
                // TODO  if smeltrecipe gets back to single result
                /*
                else if(recipe instanceof SmeltRecipe)
                {
                    SmeltRecipe r = (SmeltRecipe)recipe;
                    
                    result = r.getResult();
                }
                */
                
                if(result != null && result.equals(this.result))
                {
                    removeRecipe(recipe, info);
                }
            }
        }
    }
    
    private void removeRecipe(BaseRecipe recipe, RecipeInfo info)
    {
        removed.put(recipe, info);
        recipe.remove();
    }
    
    @Override
    public Recipe remove()
    {
        if(removed == null)
        {
            throw new IllegalAccessError("Recipe has not been applied, therefore it can't be undone.");
        }
        
        for(Entry<BaseRecipe, RecipeInfo> e : removed.entrySet())
        {
            RecipeManager.getRecipes().registerRecipe(e.getKey(), e.getValue());
        }
        
        return super.remove();
    }
}
