package dev.gustavdev.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin plugin for conditionally loading compatibility mixins.
 * 
 * This plugin checks if target mods are loaded before attempting to apply
 * compatibility mixins, preventing errors and warnings when optional mods
 * are not present.
 */
public class GustavdevMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        // Called when the mixin config is loaded
        // No initialization needed for our use case
    }

    @Override
    public String getRefMapperConfig() {
        // We don't need a custom refmap
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Check if the mixin should be applied based on mod presence
        
        // AerialHell compatibility mixin - only load if AerialHell is present
        if (mixinClassName.equals("dev.gustavdev.mixin.compat.AerialhellTotemCompatMixin")) {
            boolean isLoaded = FabricLoader.getInstance().isModLoaded("aerialhell");
            if (!isLoaded) {
                System.out.println("[GustavdevMixinPlugin] Skipping AerialhellTotemCompatMixin - aerialhell mod not loaded");
            }
            return isLoaded;
        }
        
        // Create compatibility mixins - only load if Create is present
        if (mixinClassName.equals("dev.gustavdev.mixin.compat.CreateGogglesCompatMixin") ||
            mixinClassName.equals("dev.gustavdev.mixin.compat.CreateOverlayCompatMixin")) {
            boolean isLoaded = FabricLoader.getInstance().isModLoaded("create");
            if (!isLoaded) {
                System.out.println("[GustavdevMixinPlugin] Skipping " + mixinClassName + " - create mod not loaded");
            }
            return isLoaded;
        }
        
        // All other mixins should be loaded normally
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // Called to accept target class names
        // No special handling needed
    }

    @Override
    public List<String> getMixins() {
        // Return null to use the mixins defined in the JSON config
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // Called before a mixin is applied to a target class
        // No pre-processing needed
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // Called after a mixin is applied to a target class
        // No post-processing needed
    }
}
