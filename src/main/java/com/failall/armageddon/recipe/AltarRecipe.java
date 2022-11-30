package com.failall.armageddon.recipe;

import com.failall.armageddon.Armageddon;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AltarRecipe implements Recipe<SimpleContainer> {

    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<Ingredient> recipeItems;
    protected static final List<Boolean> itemMatchesSlot = new ArrayList<>();

    public AltarRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> recipeItems) {
        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;

        for(int i = 0; i < 8; i++) {
            itemMatchesSlot.add(false);
        }
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        if (pLevel.isClientSide()) {
            return false;
        }

        for(int i = 0; i < 8; i++)
            itemMatchesSlot.set(i, false);

        boolean testIng = false;

        // cycle through each recipe slot
        for(int j = 0; j < 8; j++) {
            //cycle through each slot for each recipe slot
            for (int i = 0; i < 8; i++) {
                //if the recipe matches a slot
                if (recipeItems.get(j).test(pContainer.getItem(i))) {
                    // if the slot is not taken up
                    if (!itemMatchesSlot.get(i)) {
                        //mark the slot as taken up
                        itemMatchesSlot.set(i, true);
                        testIng = true;
                        break;
                    }
                }
            }
            //this is where it breaks out early to stop the craft
            if(!testIng)
                break;
            //reset the flag for the next iteration
            testIng = false;
        }

        // checks if a slot is not taken up, if its not taken up then it'll not craft
        for(int i = 0; i < 8; i++) {
            if (!itemMatchesSlot.get(i))
                return false;
        }
        //if it reaches here that means it has completed the shapeless craft and should craft it
        return true;
    }


    @Override
    public ItemStack assemble(SimpleContainer pContainer) {
        return output;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<AltarRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "altar";
    }


    public static class Serializer implements RecipeSerializer<AltarRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(Armageddon.MODID, "altar");

        @Override
        public AltarRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "output"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredients");
            NonNullList<Ingredient> inputs = NonNullList.withSize(8, Ingredient.EMPTY); // Limits input value to 1, no need for "count" in JSON

            for (int i = 0; i < inputs.size(); i++) {
                if(i > ingredients.size() - 1) inputs.set(i,Ingredient.of(Items.AIR));
                else inputs.set(i,Ingredient.fromJson(ingredients.get(i)));
            }

            return new AltarRecipe(pRecipeId, output, inputs);
        }

        @Override
        public @Nullable AltarRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            NonNullList<Ingredient> inputs = NonNullList.withSize(pBuffer.readInt(), Ingredient.EMPTY);
            //FluidStack fluid = pBuffer.readFluidStack();

            for (int i = 0; i < inputs.size(); i++) {
                inputs.set(i, Ingredient.fromNetwork(pBuffer));
            }

            ItemStack output = pBuffer.readItem();
            return new AltarRecipe(pRecipeId, output, inputs); //fluid
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, AltarRecipe pRecipe) {
            pBuffer.writeInt(pRecipe.getIngredients().size());
            //pBuffer.writeFluidStack(pRecipe.fluidStack);

            for (Ingredient ing : pRecipe.getIngredients()) {
                ing.toNetwork(pBuffer);
            }
            pBuffer.writeItemStack(pRecipe.getResultItem(), false);
        }
        }
    }

