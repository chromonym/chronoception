package io.github.chromonym.chronoception.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;

public class MoonRecipeSerializer implements RecipeSerializer<MoonRecipe> {

    public static final MapCodec<MoonRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("input").forGetter(MoonRecipe::getInput),
        ItemStack.OPTIONAL_CODEC.fieldOf("full").forGetter(MoonRecipe::getFullOutput),
        ItemStack.OPTIONAL_CODEC.fieldOf("gibbous").forGetter(MoonRecipe::getGibbousOutput),
        ItemStack.OPTIONAL_CODEC.fieldOf("quarter").forGetter(MoonRecipe::getQuarterOutput),
        ItemStack.OPTIONAL_CODEC.fieldOf("crescent").forGetter(MoonRecipe::getCrescentOutput),
        ItemStack.OPTIONAL_CODEC.fieldOf("new").forGetter(MoonRecipe::getNewOutput)
    ).apply(inst, MoonRecipe::new));

    public static final PacketCodec<RegistryByteBuf, MoonRecipe> PACKET_CODEC = PacketCodec.tuple(
        Ingredient.PACKET_CODEC, MoonRecipe::getInput,
        ItemStack.OPTIONAL_PACKET_CODEC, MoonRecipe::getFullOutput,
        ItemStack.OPTIONAL_PACKET_CODEC, MoonRecipe::getGibbousOutput,
        ItemStack.OPTIONAL_PACKET_CODEC, MoonRecipe::getQuarterOutput,
        ItemStack.OPTIONAL_PACKET_CODEC, MoonRecipe::getCrescentOutput,
        ItemStack.OPTIONAL_PACKET_CODEC, MoonRecipe::getNewOutput,
        MoonRecipe::new
    );

    @Override
    public MapCodec<MoonRecipe> codec() {
        return CODEC;
    }

    @Override
    public PacketCodec<RegistryByteBuf, MoonRecipe> packetCodec() {
        return PACKET_CODEC;
    }
    
}
