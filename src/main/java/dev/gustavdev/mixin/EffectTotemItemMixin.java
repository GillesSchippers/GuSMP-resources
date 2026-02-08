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
    
    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true)
    private void onInventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot, CallbackInfo ci) {
        if (!world.isClient() && timer <= 0) {
            if (entity instanceof LivingEntity livingEntityIn) {
                boolean isEquipped = false;
                
                // Check main hand and off-hand (original behavior)
                if (livingEntityIn.getMainHandStack().getItem() == (Object)this || 
                    livingEntityIn.getOffHandStack().getItem() == (Object)this) {
                    isEquipped = true;
                }
                
                // Check accessories slot (new behavior)
                if (!isEquipped) {
                    var capability = AccessoriesCapability.get(livingEntityIn);
                    if (capability != null) {
                        List<SlotReference> totemSlots = capability.getEquipped(stack.getItem());
                        if (!totemSlots.isEmpty()) {
                            isEquipped = true;
                        }
                    }
                }
                
                if (isEquipped) {
                    EffectTotemItem item = (EffectTotemItem)(Object)this;
                    
                    if (item == AerialHellItems.REGENERATION_TOTEM) {
                        livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 1200, 0));
                    } else if (item == AerialHellItems.SPEED_TOTEM) {
                        livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 0));
                    } else if (item == AerialHellItems.SPEED_II_TOTEM) {
                        livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 1));
                    } else if (item == AerialHellItems.NIGHT_VISION_TOTEM) {
                        livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1200, 0));
                    } else if (item == AerialHellItems.AGILITY_TOTEM) {
                        livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 0));
                        livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 1200, 0));
                    } else if (item == AerialHellItems.HEAD_IN_THE_CLOUDS_TOTEM) {
                        livingEntityIn.addStatusEffect(new StatusEffectInstance(AerialHellMobEffects.HEAD_IN_THE_CLOUDS, 1000, 0));
                    } else if (item == AerialHellItems.HERO_TOTEM) {
                        livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 1200, 0));
                    } else if (item == AerialHellItems.GOD_TOTEM) {
                        livingEntityIn.addStatusEffect(new StatusEffectInstance(AerialHellMobEffects.GOD, 1200, 0));
                    } else if (item == AerialHellItems.CURSED_TOTEM) {
                        if (!(ItemHelper.getItemInTagCount(EntityHelper.getEquippedHumanoidArmorItemList(livingEntityIn), AerialHellTags.Items.SHADOW_ARMOR) >= 4)) {
                            livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 300, 0));
                            livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 600, 0));
                            livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 900, 0));
                        }
                        livingEntityIn.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 1500, 2));
                    } else if (item == AerialHellItems.SHADOW_TOTEM) {
                        livingEntityIn.addStatusEffect(new StatusEffectInstance(AerialHellMobEffects.SHADOW_IMMUNITY, 1000, 0));
                    }
                    
                    timer = 300;
                }
            }
        } else if (timer > -10) {
            timer--;
        }
        
        ci.cancel(); // Cancel the original method since we've replaced it
    }
}
