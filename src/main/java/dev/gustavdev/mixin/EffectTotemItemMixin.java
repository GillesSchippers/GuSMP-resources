package dev.gustavdev.mixin;

import dev.gustavdev.AccessoriesIntegration;
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

@Mixin(EffectTotemItem.class)
public abstract class EffectTotemItemMixin {
    
    @Shadow private int timer;
    
    @Inject(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"))
    private void checkAccessoriesSlot(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot, CallbackInfo ci) {
        if (!world.isClient() && this.timer <= 0 && entity instanceof LivingEntity livingEntityIn) {
            Item stackItem = stack.getItem();
            boolean isEquipped = false;
            
            // Check accessories slots FIRST (if Accessories is available)
            if (AccessoriesIntegration.isAccessoriesAvailable()) {
                isEquipped = AccessoriesIntegration.isEquippedInAccessoriesSlot(livingEntityIn, stackItem);
            }
            
            // Only check main/off-hand if not found in accessories slot
            if (!isEquipped) {
                isEquipped = livingEntityIn.getMainHandStack().getItem() == stackItem || 
                            livingEntityIn.getOffHandStack().getItem() == stackItem;
            }
            
            // If equipped anywhere, apply totem effects
            if (isEquipped) {
                applyTotemEffects((EffectTotemItem)(Object)this, livingEntityIn);
                this.timer = 300;
            }
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
