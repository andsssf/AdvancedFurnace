package me.andandsf.advancedfurnace;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AdvancedFurnaceBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, Inventory, SidedInventory {
    private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);
    private int burnTime;
    private int fuelTime;
    private int[] cookTime = new int[4];
    private int[] cookTimeTotal = new int[4];

    private static final int[] TOP_SLOTS = new int[]{1, 3, 5, 7};
    private static final int[] BOTTOM_SLOTS = new int[]{2, 4, 6, 8};
    private static final int[] SIDE_SLOTS = new int[]{0};

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return burnTime;
                case 1:
                    return fuelTime;
                case 2:
                    return cookTime[0];
                case 3:
                    return cookTimeTotal[0];
                case 4:
                    return cookTime[1];
                case 5:
                    return cookTimeTotal[1];
                case 6:
                    return cookTime[2];
                case 7:
                    return cookTimeTotal[2];
                case 8:
                    return cookTime[3];
                case 9:
                    return cookTimeTotal[3];
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    burnTime = value;
                case 1:
                    fuelTime = value;
                case 2:
                    cookTime[0] = value;
                case 3:
                    cookTimeTotal[0] = value;
                case 4:
                    cookTime[1] = value;
                case 5:
                    cookTimeTotal[1] = value;
                case 6:
                    cookTime[2] = value;
                case 7:
                    cookTimeTotal[2] = value;
                case 8:
                    cookTime[3] = value;
                case 9:
                    cookTimeTotal[3] = value;
            }
        }

        //this is supposed to return the amount of integers you have in your delegate, in our example only one
        @Override
        public int size() {
            return 10;
        }
    };

    public AdvancedFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(AdvancedFurnace.ADVANCED_FURNACE_BLOCK_ENTITY, pos, state);
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(inventory, slot, amount);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new AdvancedFurnaceScreenHandler(syncId, inv, this, propertyDelegate);
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.inventory);
        this.burnTime = nbt.getShort("BurnTime");
        this.cookTime = nbt.getIntArray("CookTime");
        this.cookTimeTotal = nbt.getIntArray("CookTimeTotal");
        this.fuelTime = this.getFuelTime((ItemStack)this.inventory.get(1));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putShort("BurnTime", (short)this.burnTime);
        nbt.putIntArray("CookTime", this.cookTime);
        nbt.putIntArray("CookTimeTotal", this.cookTimeTotal);
        Inventories.writeNbt(nbt, this.inventory);
        return nbt;
    }

    private boolean isBurning() {
        return this.burnTime > 0;
    }

    public static void tick(World world, BlockPos pos, BlockState state, AdvancedFurnaceBlockEntity be) {
        boolean bl = be.isBurning();
        boolean bl2 = false;
        if (be.isBurning()) {
            be.burnTime -= 4;
        }

        if (!world.isClient) {
            ItemStack itemStack = (ItemStack)be.inventory.get(0);
            if (!be.isBurning() && (itemStack.isEmpty() || ((ItemStack)be.inventory.get(0)).isEmpty())) {
                for (int i = 0; i < 4; i++) {
                    if (!be.isBurning() && be.cookTime[i] > 0) {
                        be.cookTime[i] = MathHelper.clamp(be.cookTime[i] - 2, 0, be.cookTimeTotal[i]);
                    }
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    Inventory tempInventory = new SimpleInventory(3);
                    tempInventory.setStack(0, be.inventory.get(1+i*2));
                    Recipe<?> recipe = world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, tempInventory, world).orElse(null);

                    if (!be.isBurning() && be.canAcceptRecipeOutput(recipe, i)) {
                        be.burnTime = be.getFuelTime(itemStack);
                        be.fuelTime = be.burnTime;
                        if (be.isBurning()) {
                            bl2 = true;
                            if (!itemStack.isEmpty()) {
                                Item item = itemStack.getItem();
                                itemStack.decrement(1);
                                if (itemStack.isEmpty()) {
                                    Item item2 = item.getRecipeRemainder();
                                    be.inventory.set(0, item2 == null ? ItemStack.EMPTY : new ItemStack(item2));
                                }
                            }
                        }
                    }

                    if (be.isBurning() && be.canAcceptRecipeOutput(recipe, i)) {
                        be.cookTime[i] += 2;
                        be.cookTimeTotal[i] = world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, tempInventory, world).map(AbstractCookingRecipe::getCookTime).orElse(200);
                        if (be.cookTime[i] == be.cookTimeTotal[i]) {
                            be.cookTime[i] = 0;
                            be.cookTimeTotal[i] = be.world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, tempInventory, be.world).map(AbstractCookingRecipe::getCookTime).orElse(200);
                            be.craftRecipe(recipe, i);
                            bl2 = true;
                        }
                    } else {
                        be.cookTime[i] = 0;
                    }
                }
            }
            if (bl != be.isBurning()) {
                bl2 = true;
                world.setBlockState(be.pos, (BlockState)world.getBlockState(be.pos).with(AdvancedFurnaceBlock.LIT, be.isBurning()), 3);
            }
        }
        if (bl2) {
            be.markDirty();
        }
    }

    protected boolean canAcceptRecipeOutput(@Nullable Recipe<?> recipe, int i) {
        if (!(this.inventory.get(1+2*i)).isEmpty() && recipe != null) {
            ItemStack itemStack = recipe.getOutput();
            if (itemStack.isEmpty()) {
                return false;
            } else {
                ItemStack itemStack2 = (ItemStack)this.inventory.get(2+2*i);
                if (itemStack2.isEmpty()) {
                    return true;
                } else if (!itemStack2.isItemEqualIgnoreDamage(itemStack)) {
                    return false;
                } else if (itemStack2.getCount() < this.getMaxCountPerStack() && itemStack2.getCount() < itemStack2.getMaxCount()) {
                    return true;
                } else {
                    return itemStack2.getCount() < itemStack.getMaxCount();
                }
            }
        } else {
            return false;
        }
    }

    protected int getFuelTime(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        } else {
            Item item = fuel.getItem();
            return AbstractFurnaceBlockEntity.createFuelTimeMap().getOrDefault(item, 0);
        }
    }

    private void craftRecipe(@Nullable Recipe<?> recipe, int i) {
        if (recipe != null && this.canAcceptRecipeOutput(recipe, i)) {
            ItemStack itemStack = this.inventory.get(1+2*i);
            ItemStack itemStack2 = recipe.getOutput();
            ItemStack itemStack3 = this.inventory.get(2+2*i);
            if (itemStack3.isEmpty()) {
                this.inventory.set(2+2*i, itemStack2.copy());
            } else if (itemStack3.getItem() == itemStack2.getItem()) {
                itemStack3.increment(1);
            }
            itemStack.decrement(1);
        }
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) {
            return BOTTOM_SLOTS;
        } else {
            return side == Direction.UP ? TOP_SLOTS : SIDE_SLOTS;
        }
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }
}
