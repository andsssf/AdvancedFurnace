package me.andandsf.advancedfurnace;

import net.minecraft.block.FurnaceBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class UpdateToolItem extends Item {
    public UpdateToolItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) return ActionResult.PASS;
        BlockPos pos = context.getBlockPos();
        if (world.getBlockState(pos).getBlock() instanceof FurnaceBlock) {
            Direction direction = world.getBlockState(pos).get(FurnaceBlock.FACING);
            world.removeBlock(pos, false);
            world.setBlockState(pos, AdvancedFurnace.ADVANCED_FURNACE_BLOCK.getDefaultState().with(AdvancedFurnaceBlock.FACING, direction));
            context.getStack().decrement(1);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
