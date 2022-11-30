package com.failall.armageddon.recipe;

import com.failall.armageddon.Armageddon;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeTypes {;
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Armageddon.MODID);

    public static final RegistryObject<RecipeType<AltarRecipe>> ALTAR =
            RECIPE_TYPES.register("altar", () -> AltarRecipe.Type.INSTANCE);

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
    }
}
