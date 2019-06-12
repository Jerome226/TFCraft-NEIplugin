/*
 * Copyright (c) 2019 mabrowning
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted (subject to the limitations in the
 * disclaimer below) provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 *  * Neither the name of Dries007 nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE
 * GRANTED BY THIS LICENSE.  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.dries007.tfcnei.recipeHandlers;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.dunk.tfc.Items.ItemFlatGeneric;
import com.dunk.tfc.Items.ItemLooseRock;
import com.dunk.tfc.Reference;
import com.dunk.tfc.GUI.GuiSewing;
import com.dunk.tfc.api.Crafting.CraftingManagerTFC;
import com.dunk.tfc.api.Crafting.SewingRecipe;
import com.dunk.tfc.api.Crafting.ClothingManager;
import com.dunk.tfc.api.Crafting.ShapedRecipesTFC;
import com.dunk.tfc.api.TFCItems;
import net.dries007.tfcnei.util.Helper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

/**
 * @author mabrowning
 */
public class ClothingRecipeHandler extends TemplateRecipeHandler
{
    private static List<SewingRecipe> recipeList;

    @Override
    public String getGuiTexture()
    {
        return GuiSewing.texture.toString();
    }

    @Override
    public String getRecipeName()
    {
        return "Sewing";
    }

    @Override
    public String getOverlayIdentifier()
    {
        return "sewing";
    }

    @Override
    public TemplateRecipeHandler newInstance()
    {
        if (recipeList == null)
        {
            recipeList = ClothingManager.getInstance().getRecipes();
        }
        return super.newInstance();
    }

    @Override
    public int recipiesPerPage()
    {
        return 1;
    }

    @Override
    public void drawBackground(int recipe)
    {
        GL11.glColor4f(1, 1, 1, 1);
        changeTexture(getGuiTexture());
        drawTexturedModalRect(0, 0, 10, 105, 160, 40);
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results)
    {
        if (outputId.equals("sewing") && getClass() == ClothingRecipeHandler.class)
        {
            for (SewingRecipe recipe : recipeList)
                arecipes.add(new CachedSewingRecipe(recipe));
        }
        else super.loadCraftingRecipes(outputId, results);
    }

    @Override
    public void loadCraftingRecipes(ItemStack result)
    {
        for (SewingRecipe recipe : recipeList)
        {
            if (Helper.areItemStacksEqual(result, recipe.getSewingPattern().getOutput())) arecipes.add(new CachedSewingRecipe(recipe));
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient)
    {
        for (SewingRecipe recipe : recipeList)
        {
            for (ItemStack inStack : recipe.getRequiredPieces())
            {
                if (inStack == null || inStack.getItem() != ingredient.getItem() ) continue;
				arecipes.add(new CachedSewingRecipe(recipe));
                break;
            }
        }
    }

    public class CachedSewingRecipe extends CachedRecipe
    {
        final List<PositionedStack> inputs;
        final PositionedStack result;
        final PositionedStack needle;
        
        final int x0 = 43;
        final int y0 = 3;

        int i=0;
        int j=0;
        public CachedSewingRecipe(SewingRecipe recipe)
        {
            this.inputs = new ArrayList<>();
            ItemStack[] inputItems = recipe.getRequiredPieces();
            for (ItemStack inStack : inputItems)
            {
            	this.inputs.add(new PositionedStack(inStack, x0+18*i++, y0+18*j));
            	if( i > 3 )
            	{
            		i = 0;
            		j = 1;
            	}
            }
            this.result = new PositionedStack(recipe.getSewingPattern().getOutput(), x0+90, y0+9);
            ItemStack[] needles = new ItemStack[] { 
            		new ItemStack(TFCItems.boneNeedleStrung), 
            		new ItemStack(TFCItems.ironNeedleStrung)
				};

            this.needle = new PositionedStack(needles, x0+28, y0+37,true);
        }

        @Override
        public List<PositionedStack> getIngredients()
        {
            return inputs;
        }

        @Override
        public PositionedStack getResult()
        {
            return result;
        }
        @Override
        public PositionedStack getOtherStack()
        {
        	needle.setPermutationToRender(cycleticks / 24 % needle.items.length);
            return needle;
        }
    }
}
