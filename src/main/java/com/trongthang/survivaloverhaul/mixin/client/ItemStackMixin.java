package com.trongthang.survivaloverhaul.mixin.client;

import com.trongthang.survivaloverhaul.client.tooltip.HydrationTooltipData;
import com.trongthang.survivaloverhaul.config.ModConfig;
import com.trongthang.survivaloverhaul.item.custom.CanteenItem;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract Item getItem();

    @Inject(method = "getTooltipData", at = @At("RETURN"), cancellable = true)
    private void survivalOverhaul$getTooltipData(CallbackInfoReturnable<Optional<TooltipData>> cir) {
        if (!cir.getReturnValue().isPresent()) {
            if (!ModConfig.enableThirst)
                return;

            Item item = this.getItem();

            if (item instanceof CanteenItem) {
                boolean isPurified = ((CanteenItem) item).isPurified();
                cir.setReturnValue(Optional.of(new HydrationTooltipData(12, isPurified ? 2.5f : 0.5f))); // this is what
                                                                                                         // Canteen
                                                                                                         // thirst gives
                                                                                                         // per sip
                return;
            }

            String itemId = Registries.ITEM.getId(item).toString();
            for (String entry : ModConfig.itemThirstValues) {
                String[] parts = entry.split("=");
                if (parts.length == 2 && parts[0].trim().equals(itemId)) {
                    try {
                        int amount = Integer.parseInt(parts[1].trim());
                        cir.setReturnValue(Optional.of(new HydrationTooltipData(amount, amount * 0.1f)));
                        break;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
    }
}
