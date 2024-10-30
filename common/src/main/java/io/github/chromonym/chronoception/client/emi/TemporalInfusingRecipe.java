package io.github.chromonym.chronoception.client.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TemporalInfusingRecipe extends BasicEmiRecipe {

    private final Text description;

    public TemporalInfusingRecipe(Identifier id, Item input, Item output) {
        super(ChronoceptionEmi.TEMPORAL_INFUSING, id, 126, 29);
        this.inputs.add(EmiIngredient.of(Ingredient.ofItems(input)));
        this.outputs.add(EmiStack.of(output.getDefaultStack()));
        this.description = Text.translatable("emi.".concat(id.toShortTranslationKey()).concat(".desc"));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 27, 0);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 51, 1);
        widgets.addSlot(outputs.get(0), 81, 0).recipeContext(this);
        MinecraftClient client = MinecraftClient.getInstance();
        widgets.addText(description, (this.width - client.textRenderer.getWidth(description))/2, 20, 0xFFFFFF, true);
    }
}
