package dev.gustavdev.mixin;

import fr.factionbedrock.aerialhell.Item.EffectTotemItem;
import fr.factionbedrock.aerialhell.Registry.AerialHellItems;
import fr.factionbedrock.aerialhell.Registry.AerialHellMobEffects;
import fr.factionbedrock.aerialhell.Registry.Misc.AerialHellTags;
import fr.factionbedrock.aerialhell.Util.EntityHelper;
import fr.factionbedrock.aerialhell.Util.ItemHelper;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EffectTotemItem.class)
public abstract class EffectTotemItemMixin {
    
    @Shadow private int timer;
    
    @Inject(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"))
    private void checkAccessoriesSlot(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot, CallbackInfo ci) {
        if (!world.isClient() && this.timer <= 0 && entity instanceof LivingEntity livingEntityIn) {
            // Original check - main hand and off-hand
            Item stackItem = stack.getItem();
            boolean isInMainOrOffHand = livingEntityIn.getMainHandStack().getItem() == stackItem || 
                                        livingEntityIn.getOffHandStack().getItem() == stackItem;
            
            // New check - accessories slot
            boolean isInAccessoriesSlot = false;
            if (!isInMainOrOffHand) {
                var capability = AccessoriesCapability.get(livingEntityIn);
                if (capability != null) {
                    List<SlotReference> totemSlots = capability.getEquipped(stackItem);
                    isInAccessoriesSlot = !totemSlots.isEmpty();
                }
            }
            
            // If in accessories slot but not in main/off hand, we need to execute the effect logic
            // that would have been skipped by the original method
            if (isInAccessoriesSlot) {
                EffectTotemItem thisItem = (EffectTotemItem)(Object)this;
                
                if (thisItem == AerialHellItems.REGENERATION_TOTEM) {
                    livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 1200, 0));
                } else if (thisItem == AerialHellItems.SPEED_TOTEM) {
                    livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 0));
                } else if (thisItem == AerialHellItems.SPEED_II_TOTEM) {
                    livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 1));
                } else if (thisItem == AerialHellItems.NIGHT_VISION_TOTEM) {
                    livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1200, 0));
                } else if (thisItem == AerialHellItems.AGILITY_TOTEM) {
                    livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 0));
                    livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 1200, 0));
                } else if (thisItem == AerialHellItems.HEAD_IN_THE_CLOUDS_TOTEM) {
                    livingEntityIn.addStatusEffect(new StatusEffectInstance(AerialHellMobEffects.HEAD_IN_THE_CLOUDS, 1000, 0));
                } else if (thisItem == AerialHellItems.HERO_TOTEM) {
                    livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 1200, 0));
                } else if (thisItem == AerialHellItems.GOD_TOTEM) {
                    livingEntityIn.addStatusEffect(new StatusEffectInstance(AerialHellMobEffects.GOD, 1200, 0));
                } else if (thisItem == AerialHellItems.CURSED_TOTEM) {
                    if (!(ItemHelper.getItemInTagCount(EntityHelper.getEquippedHumanoidArmorItemList(livingEntityIn), AerialHellTags.Items.SHADOW_ARMOR) >= 4)) {
                        livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 300, 0));
                        livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 600, 0));
                        livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 900, 0));
                    }
                    livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 1500, 2));
                } else if (thisItem == AerialHellItems.SHADOW_TOTEM) {
                    livingEntityIn.addStatusEffect(new StatusEffectInstance(AerialHellMobEffects.SHADOW_IMMUNITY, 1000, 0));
                }
                
                this.timer = 300;
            }
        }
    }
}
