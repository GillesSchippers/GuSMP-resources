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
    
    // Cache for reflected methods to avoid repeated lookups (only used if needed)
    private static Method accessoriesCapabilityGetMethod;
    private static Method capabilityGetEquippedMethod;
    private static Boolean accessoriesAvailable = null; // null = not checked, true/false = checked
    
    @Inject(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"))
    private void checkAccessoriesSlot(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot, CallbackInfo ci) {
        if (!world.isClient() && this.timer <= 0 && entity instanceof LivingEntity livingEntityIn) {
            Item stackItem = stack.getItem();
            
            // Check if item is in main hand or off-hand (original behavior)
            boolean isInMainOrOffHand = livingEntityIn.getMainHandStack().getItem() == stackItem || 
                                        livingEntityIn.getOffHandStack().getItem() == stackItem;
            
            // Only check accessories if not in main/off-hand
            if (!isInMainOrOffHand) {
                // Try to detect if this is being called from an accessories slot
                // First, check if Accessories API is available and use it only if needed
                boolean isInAccessoriesSlot = isEquippedInAccessoriesSlot(livingEntityIn, stackItem);
                
                if (!isInAccessoriesSlot) {
                    // Item is not equipped anywhere that we can detect, skip
                    return;
                }
            }
            
            // Apply totem effects (item is equipped somewhere)
            applyTotemEffects((EffectTotemItem)(Object)this, livingEntityIn);
            this.timer = 300;
        }
    }
    
    /**
     * Check if item is equipped in an Accessories slot.
     * Uses reflection to avoid compile-time dependency on Accessories API.
     * 
     * @param entity The living entity to check
     * @param item The item to look for
     * @return true if the item is equipped in an accessories slot, false otherwise or if Accessories is not available
     */
    private static boolean isEquippedInAccessoriesSlot(LivingEntity entity, Item item) {
        // Lazy initialization: only check for Accessories API on first call
        if (accessoriesAvailable == null) {
            accessoriesAvailable = initializeAccessoriesReflection();
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
            // If reflection fails, return false
            return false;
        }
    }
    
    /**
     * Initialize reflection for Accessories API.
     * This is only called once to check if Accessories is available.
     * 
     * @return true if Accessories API is available and reflection setup succeeded, false otherwise
     */
    private static boolean initializeAccessoriesReflection() {
        try {
            // Try to load the AccessoriesCapability class
            Class<?> accessoriesCapabilityClass = Class.forName("io.wispforest.accessories.api.AccessoriesCapability");
            
            // Get the static 'get' method: AccessoriesCapability.get(LivingEntity)
            accessoriesCapabilityGetMethod = accessoriesCapabilityClass.getMethod("get", LivingEntity.class);
            
            // Get the 'getEquipped' method from the capability interface
            capabilityGetEquippedMethod = accessoriesCapabilityClass.getMethod("getEquipped", Item.class);
            
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // Accessories mod is not present, which is fine
            return false;
        }
    }
    
    /**
     * Apply the appropriate totem effects based on which totem this is.
     * 
     * @param totem The totem item instance
     * @param entity The living entity to apply effects to
     */
    private static void applyTotemEffects(EffectTotemItem totem, LivingEntity entity) {
        if (totem == AerialHellItems.REGENERATION_TOTEM) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 1200, 0));
        } else if (totem == AerialHellItems.SPEED_TOTEM) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 0));
        } else if (totem == AerialHellItems.SPEED_II_TOTEM) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 1));
        } else if (totem == AerialHellItems.NIGHT_VISION_TOTEM) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1200, 0));
        } else if (totem == AerialHellItems.AGILITY_TOTEM) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 1200, 0));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 1200, 0));
        } else if (totem == AerialHellItems.HEAD_IN_THE_CLOUDS_TOTEM) {
            entity.addStatusEffect(new StatusEffectInstance(AerialHellMobEffects.HEAD_IN_THE_CLOUDS, 1000, 0));
        } else if (totem == AerialHellItems.HERO_TOTEM) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 1200, 0));
        } else if (totem == AerialHellItems.GOD_TOTEM) {
            entity.addStatusEffect(new StatusEffectInstance(AerialHellMobEffects.GOD, 1200, 0));
        } else if (totem == AerialHellItems.CURSED_TOTEM) {
            if (!(ItemHelper.getItemInTagCount(EntityHelper.getEquippedHumanoidArmorItemList(entity), AerialHellTags.Items.SHADOW_ARMOR) >= 4)) {
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 300, 0));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 600, 0));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 900, 0));
            }
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 1500, 2));
        } else if (totem == AerialHellItems.SHADOW_TOTEM) {
            entity.addStatusEffect(new StatusEffectInstance(AerialHellMobEffects.SHADOW_IMMUNITY, 1000, 0));
        }
    }
}
