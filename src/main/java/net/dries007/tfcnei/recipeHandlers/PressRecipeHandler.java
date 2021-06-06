package net.dries007.tfcnei.recipeHandlers;

import static codechicken.lib.gui.GuiDraw.getMousePosition;

import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.dunk.tfc.Food.ItemFoodTFC;
import com.dunk.tfc.TileEntities.TEHopper;
import com.dunk.tfc.api.Food;
import com.dunk.tfc.api.TFCBlocks;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.dries007.tfcnei.util.Constants;
import net.dries007.tfcnei.util.Helper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

public class PressRecipeHandler extends TemplateRecipeHandler{

	private static HashMap<Item,Float> pressWeights = new HashMap<Item,Float>();
	private static HashMap<Item,Fluid> fluids = new HashMap<Item,Fluid>();
	private static HashMap<Item,Float> fluidAmounts = new HashMap<Item,Float>();

    @Override
    public String getGuiTexture()
    {
        return Constants.PRESS_TEXTURE.toString();
    }

    @Override
    public String getRecipeName()
    {
        return "Press";
    }

    @Override
    public String getOverlayIdentifier()
    {
        return "press";
    }

    @SuppressWarnings("unchecked")
    @Override
    public TemplateRecipeHandler newInstance()
    {
    	try {
			Field field = TEHopper.class.getDeclaredField("pressWeights");
			field.setAccessible(true);
			pressWeights = (HashMap<Item,Float>)field.get(TEHopper.class);
			field = TEHopper.class.getDeclaredField("fluids");
			field.setAccessible(true);
			fluids = (HashMap<Item,Fluid>)field.get(TEHopper.class);
			field = TEHopper.class.getDeclaredField("fluidAmounts");
			field.setAccessible(true);
			fluidAmounts = (HashMap<Item,Float>)field.get(TEHopper.class);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return super.newInstance();
    }

    @Override
    public void loadTransferRects()
    {
        transferRects.add(new RecipeTransferRect(new Rectangle(71, 23, 24, 18), "press"));
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results)
    {
        if (outputId.equals("press") && getClass() == PressRecipeHandler.class)
        {
            for (Item food : pressWeights.keySet())
                arecipes.add(new CachedPressRecipe(food, pressWeights.get(food), fluids.get(food), fluidAmounts.get(food)));
        }
        else super.loadCraftingRecipes(outputId, results);
    }

    @Override
    public void loadCraftingRecipes(ItemStack result)
    {
    	for (Item food : pressWeights.keySet())
        {
            FluidStack outFluid = new FluidStack(fluids.get(food), (int)(float)fluidAmounts.get(food));

            if (outFluid != null && outFluid.isFluidEqual(result))
            {
            	arecipes.add(new CachedPressRecipe(food, pressWeights.get(food), fluids.get(food), fluidAmounts.get(food)));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient)
    {
        for (Item food : pressWeights.keySet())
        {
            ItemStack inItem = new ItemStack(food,1);
            ItemFoodTFC.createTag(inItem,pressWeights.get(food));

            if ((inItem != null && Helper.areItemStacksEqual(inItem, ingredient)))
            {
            	arecipes.add(new CachedPressRecipe(food, pressWeights.get(food), fluids.get(food), fluidAmounts.get(food)));
            }
        }
    }

    @Override
    public void drawExtras(int recipe)
    {
        CachedRecipe crecipe = arecipes.get(recipe);
        if (crecipe instanceof CachedPressRecipe)
        {
            if (((CachedPressRecipe) crecipe).getOutFluid() != null) Helper.drawFluidInRect(((CachedPressRecipe) crecipe).getOutFluid().getFluid(), recipeOutFluidRect());
        }
    }

    @Override
    public List<String> handleItemTooltip(GuiRecipe gui, ItemStack stack, List<String> currenttip, int recipe)
    {
        CachedRecipe irecipe = arecipes.get(recipe);
        if (irecipe instanceof CachedPressRecipe)
        {
            Point mousepos = getMousePosition();
            Point offset = gui.getRecipePosition(recipe);
            Point relMouse = new Point(mousepos.x - gui.guiLeft - offset.x, mousepos.y - gui.guiTop - offset.y);
            if (recipeOutFluidRect().contains(relMouse) && (((CachedPressRecipe) irecipe).getOutFluid() != null)) currenttip.add(Helper.tooltipForFluid(((CachedPressRecipe) irecipe).getOutFluid()));
        }
        return currenttip;
    }

    @Override
    public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe)
    {
        if (keyCode == NEIClientConfig.getKeyBinding("gui.recipe"))
        {
            if (transferFluid(gui, recipe, false)) return true;
        }
        else if (keyCode == NEIClientConfig.getKeyBinding("gui.usage"))
        {
            if (transferFluid(gui, recipe, true)) return true;
        }

        return super.keyTyped(gui, keyChar, keyCode, recipe);
    }

    @Override
    public boolean mouseClicked(GuiRecipe gui, int button, int recipe)
    {
        if (button == 0)
        {
            if (transferFluid(gui, recipe, false)) return true;
        }
        else if (button == 1)
        {
            if (transferFluid(gui, recipe, true)) return true;
        }

        return super.mouseClicked(gui, button, recipe);
    }

    private boolean transferFluid(GuiRecipe gui, int recipe, boolean usage)
    {
        CachedRecipe crecipe = arecipes.get(recipe);
        if (crecipe instanceof CachedPressRecipe)
        {
            Point mousepos = getMousePosition();
            Point offset = gui.getRecipePosition(recipe);
            Point relMouse = new Point(mousepos.x - gui.guiLeft - offset.x, mousepos.y - gui.guiTop - offset.y);
            ItemStack fluidStack = null;
            if (recipeOutFluidRect().contains(relMouse) && (((CachedPressRecipe) crecipe).getOutFluid() != null)) fluidStack = Helper.getItemStacksForFluid(((CachedPressRecipe) crecipe).getOutFluid())[0];
            if (fluidStack != null && (usage ? GuiUsageRecipe.openRecipeGui("item", fluidStack) : GuiCraftingRecipe.openRecipeGui("item", fluidStack))) return true;
        }
        return false;
    }

    private static Rectangle recipeOutFluidRect()
    {
        return new Rectangle(115, 7, 8, 50);
    }

    public class CachedPressRecipe extends CachedRecipe
    {
        PositionedStack inItem;
        FluidStack outFluid;
        public CachedPressRecipe(Item item, float pressWeight, Fluid fluid, float fluidAmount)
        {
        	ItemStack foodIn = new ItemStack(item,1);
        	ItemFoodTFC.createTag(foodIn,pressWeight);
            setInItem(foodIn);
            outFluid = new FluidStack(fluid, (int)fluidAmount);
        }

        @Override
        public PositionedStack getIngredient()
        {
            if (inItem != null) randomRenderPermutation(inItem, cycleticks / 24);
            return inItem;
        }

        public void setInItem(Object inItem)
        {
            this.inItem = inItem == null ? null : new PositionedStack(inItem, 39, 24);
        }

        public FluidStack getOutFluid()
        {
            return outFluid;
        }

		@Override
		public PositionedStack getResult() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
        public List<PositionedStack> getOtherStacks()
        {
			ArrayList<ItemStack> stackList = OreDictionary.getOres("stoneSmooth");
			ItemStack[] stackArr = new ItemStack[stackList.size()];
			stackArr = stackList.toArray(stackArr);
            PositionedStack smoothStone = new PositionedStack(stackArr, 39, 6, false);
			smoothStone.setPermutationToRender(cycleticks / 24 % smoothStone.items.length);
			
			ItemStack hopperStack = new ItemStack(TFCBlocks.stoneHopper, 1, OreDictionary.WILDCARD_VALUE);
			PositionedStack hopper = new PositionedStack(hopperStack, 39,42, true);
			hopper.setPermutationToRender(cycleticks / 24 % hopper.items.length);
            return Arrays.asList(smoothStone,hopper);
        }
    }
}
