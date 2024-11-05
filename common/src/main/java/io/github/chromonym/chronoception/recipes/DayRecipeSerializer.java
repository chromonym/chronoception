package io.github.chromonym.chronoception.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;

public class DayRecipeSerializer implements RecipeSerializer<DayRecipe> {

    public static final MapCodec<DayRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("input").forGetter(DayRecipe::getInput),
        ItemStack.OPTIONAL_CODEC.fieldOf("day").forGetter(DayRecipe::getDayOutput),
        ItemStack.OPTIONAL_CODEC.fieldOf("dusk").forGetter(DayRecipe::getDuskOutput),
        ItemStack.OPTIONAL_CODEC.fieldOf("night").forGetter(DayRecipe::getNightOutput)
    ).apply(inst, DayRecipe::new));

    public static final PacketCodec<RegistryByteBuf, DayRecipe> PACKET_CODEC = PacketCodec.tuple(
        Ingredient.PACKET_CODEC, DayRecipe::getInput,
        ItemStack.OPTIONAL_PACKET_CODEC, DayRecipe::getDayOutput,
        ItemStack.OPTIONAL_PACKET_CODEC, DayRecipe::getDuskOutput,
        ItemStack.OPTIONAL_PACKET_CODEC, DayRecipe::getNightOutput,
        DayRecipe::new
    );

    @Override
    public MapCodec<DayRecipe> codec() {
        return CODEC;
    }

    @Override
    public PacketCodec<RegistryByteBuf, DayRecipe> packetCodec() {
        return PACKET_CODEC;
    }
    
}
