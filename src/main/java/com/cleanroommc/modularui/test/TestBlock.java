package com.cleanroommc.modularui.test;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.manager.GuiInfos;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;

public class TestBlock extends Block implements ITileEntityProvider {

    public final static Block testBlock = new TestBlock();
    public final static ItemBlock testItemBlock = new ItemBlock(testBlock);

    public static void preInit() {
        ResourceLocation rl = new ResourceLocation(ModularUI.ID, "test_block");
        testBlock.setRegistryName(rl);
        testItemBlock.setRegistryName(rl);
        GameRegistry.registerTileEntity(TestTile.class, rl);
        TestItem.testItem.setRegistryName(ModularUI.ID, "test_item");
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        registry.register(testBlock);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(testItemBlock);
        registry.register(TestItem.testItem);
    }

    public TestBlock() {
        super(Material.ROCK);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TestTile();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            GuiInfos.TILE_ENTITY.open(playerIn, worldIn, pos);
        }
        return true;
    }
}
