package dev.gustavdev.mixin;

import fr.factionbedrock.aerialhell.Item.EffectTotemItem;
import fr.factionbedrock.aerialhell.Registry.AerialHellItems;
import fr.factionbedrock.aerialhell.Registry.AerialHellMobEffects;
import fr.factionbedrock.aerialhell.Registry.Misc.AerialHellTags;
import fr.factionbedrock.aerialhell.Util.EntityHelper;
import fr.factionbedrock.aerialhell.Util.ItemHelper;
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

import java.lang.reflect.Method;
import java.util.List;

@Mixin(EffectTotemItem.class)
public abstract class EffectTotemItemMixin {
    
    @Shadow private int timer;
    
    // Cache for reflected methods to avoid repeated lookups
    private static Method accessoriesCapabilityGetMethod;
    private static Method capabilityGetEquippedMethod;
    private static boolean accessoriesChecked = false;
    private static boolean accessoriesAvailable = false;
    
    @Inject(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"))
    private void checkAccessoriesSlot(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot, CallbackInfo ci) {
        if (!world.isClient() && this.timer <= 0 && entity instanceof LivingEntity livingEntityIn) {
            // Original check - main hand and off-hand
            Item stackItem = stack.getItem();
            boolean isInMainOrOffHand = livingEntityIn.getMainHandStack().getItem() == stackItem || 
                                        livingEntityIn.getOffHandStack().getItem() == stackItem;
            
            // New check - accessories slot (using reflection for soft dependency)
            boolean isInAccessoriesSlot = false;
            if (!isInMainOrOffHand) {
                isInAccessoriesSlot = checkAccessoriesSlotViaReflection(livingEntityIn, stackItem);
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
    
    /**
     * Check if the item is equipped in an Accessories slot using reflection.
     * This allows the mod to work without requiring Accessories as a compile-time dependency.
     * 
     * @param entity The living entity to check
     * @param item The item to look for
     * @return true if the item is equipped in an accessories slot, false otherwise
     */
    private static boolean checkAccessoriesSlotViaReflection(LivingEntity entity, Item item) {
        // One-time check to see if Accessories is available
        if (!accessoriesChecked) {
            accessoriesChecked = true;
            try {
                // Try to load the AccessoriesCapability class
                Class<?> accessoriesCapabilityClass = Class.forName("io.wispforest.accessories.api.AccessoriesCapability");
                // Get the static 'get' method: AccessoriesCapability.get(LivingEntity)
                accessoriesCapabilityGetMethod = accessoriesCapabilityClass.getMethod("get", LivingEntity.class);
                
                // Get the 'getEquipped' method from the returned capability object
                // We need to find the interface/class that the get() method returns
                Class<?> capabilityClass = Class.forName("io.wispforest.accessories.api.AccessoriesCapability");
                capabilityGetEquippedMethod = capabilityClass.getMethod("getEquipped", Item.class);
                
                accessoriesAvailable = true;
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                // Accessories mod is not present, which is fine
                accessoriesAvailable = false;
            }
        }
        
        // If Accessories is not available, return false
        if (!accessoriesAvailable) {
            return false;
        }
        
        try {
            // Call AccessoriesCapability.get(entity)
            Object capability = accessoriesCapabilityGetMethod.invoke(null, entity);
            
            if (capability == null) {
                return false;
            }
            
            // Call capability.getEquipped(item)
            @SuppressWarnings("unchecked")
            List<?> equippedSlots = (List<?>) capabilityGetEquippedMethod.invoke(capability, item);
            
            // If the list is not empty, the item is equipped
            return equippedSlots != null && !equippedSlots.isEmpty();
            
        } catch (Exception e) {
            // If anything goes wrong with reflection, just return false
            return false;
        }
    }
}
