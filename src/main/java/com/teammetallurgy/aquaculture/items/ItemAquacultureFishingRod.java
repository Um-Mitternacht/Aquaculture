package com.teammetallurgy.aquaculture.items;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.teammetallurgy.aquaculture.handlers.EntityCustomFishHook;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class ItemAquacultureFishingRod extends ItemTool {
    private static Set<Block> effectiveBlockSet = Sets.newHashSet(new Block[]{});
    public String type;
    public int enchantability;

    public ItemAquacultureFishingRod(int d, int enchantability, String type) {
        super(0F, 0F, Objects.requireNonNull(EnumHelper.addToolMaterial("Fishing" + type, 0, d, 0, 0, enchantability)), effectiveBlockSet);
        setMaxDamage(d);
        setMaxStackSize(1);
        this.type = type;
        this.enchantability = enchantability;
        addPropertyOverride(new ResourceLocation("cast"), (stack, world, entity) -> {
            if (entity != null && ((EntityPlayer) entity).fishEntity != null && !stack.isEmpty() && entity.getHeldItemMainhand() == stack) {
                return 1.0F;
            }
            return 0;
        });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldRotateAroundWhenRendering() {
        return true;
    }

    @Override
    public int getItemEnchantability() {
        return enchantability;
    }

    @Override
    @Nonnull
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot slot, @Nonnull ItemStack stack) {
        return HashMultimap.create();
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entityplayer, @Nonnull EnumHand hand) {
        ItemStack itemstack = entityplayer.getHeldItem(hand);

        if (entityplayer.fishEntity != null) {
            int i = entityplayer.fishEntity.handleHookRetraction();
            itemstack.damageItem(i, entityplayer);
            entityplayer.swingArm(hand);

            if (!itemstack.hasTagCompound())
                itemstack.setTagCompound(new NBTTagCompound());

            NBTTagCompound tag = itemstack.getTagCompound();
            Preconditions.checkNotNull(tag, "tagCompound");
            tag.setBoolean("using", false);
        } else {
            world.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            if (!world.isRemote) {
                world.spawnEntity(new EntityCustomFishHook(world, entityplayer));
            }
            entityplayer.swingArm(hand);

            if (!itemstack.hasTagCompound())
                itemstack.setTagCompound(new NBTTagCompound());

            NBTTagCompound tag = itemstack.getTagCompound();
            Preconditions.checkNotNull(tag, "tagCompound");
            tag.setBoolean("using", true);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }
}