package nc.block.tile.processor;

import static nc.block.property.BlockProperties.FACING_HORIZONTAL;

import java.util.Random;

import nc.NuclearCraft;
import nc.block.tile.IActivatable;
import nc.init.NCBlocks;
import nc.tab.NCTabs;
import nc.tile.processor.TileNuclearFurnace;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockNuclearFurnace extends BlockContainer implements ITileEntityProvider, IActivatable {
	
	private final boolean isBurning;
	private static boolean keepInventory;
	
	public BlockNuclearFurnace(boolean isBurning) {
		super(Material.IRON);
		setDefaultState(blockState.getBaseState().withProperty(FACING_HORIZONTAL, EnumFacing.NORTH));
		this.isBurning = isBurning;
		if (!isBurning) setCreativeTab(NCTabs.MACHINES);
		setHarvestLevel("pickaxe", 0);
		setHardness(2F);
		setResistance(15F);
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(NCBlocks.nuclear_furnace_idle);
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		setDefaultFacing(world, pos, state);
	}
	
	private static void setDefaultFacing(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote) {
			IBlockState state0 = world.getBlockState(pos.north());
			IBlockState state1 = world.getBlockState(pos.south());
			IBlockState state2 = world.getBlockState(pos.west());
			IBlockState state3 = world.getBlockState(pos.east());
			EnumFacing enumfacing = state.getValue(FACING_HORIZONTAL);
			
			if (enumfacing == EnumFacing.NORTH && state0.isFullBlock() && !state1.isFullBlock()) {
				enumfacing = EnumFacing.SOUTH;
			} else if (enumfacing == EnumFacing.SOUTH && state1.isFullBlock() && !state0.isFullBlock()) {
				enumfacing = EnumFacing.NORTH;
			} else if (enumfacing == EnumFacing.WEST && state2.isFullBlock() && !state3.isFullBlock()) {
				enumfacing = EnumFacing.EAST;
			} else if (enumfacing == EnumFacing.EAST && state3.isFullBlock() && !state2.isFullBlock()) {
				enumfacing = EnumFacing.WEST;
			}
			world.setBlockState(pos, state.withProperty(FACING_HORIZONTAL, enumfacing), 2);
		}
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileNuclearFurnace();
	}
	
	@Override
	public Block getBlockType(boolean active) {
		return active ? NCBlocks.nuclear_furnace_active : NCBlocks.nuclear_furnace_idle;
	}
	
	@Override
	public void setState(boolean isActive, TileEntity tile) {
		World world = tile.getWorld();
		BlockPos pos = tile.getPos();
		IBlockState state = world.getBlockState(pos);
		keepInventory = true;
		
		if (isActive) {
			world.setBlockState(pos, NCBlocks.nuclear_furnace_active.getDefaultState().withProperty(FACING_HORIZONTAL, state.getValue(FACING_HORIZONTAL)), 3);
		} else {
			world.setBlockState(pos, NCBlocks.nuclear_furnace_idle.getDefaultState().withProperty(FACING_HORIZONTAL, state.getValue(FACING_HORIZONTAL)), 3);
		}
		keepInventory = false;
		
		tile.validate();
		world.setTileEntity(pos, tile);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (player == null || hand != EnumHand.MAIN_HAND || player.isSneaking()) return false;
		if (world.isRemote) return true;
		
		TileEntity tileentity = world.getTileEntity(pos);
		if (tileentity instanceof TileNuclearFurnace) {
			FMLNetworkHandler.openGui(player, NuclearCraft.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings("incomplete-switch")
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		if (isBurning) {
			EnumFacing enumfacing = state.getValue(FACING_HORIZONTAL);
			double d0 = pos.getX() + 0.5D;
			double d1 = pos.getY() + rand.nextDouble() * 0.4D;
			double d2 = pos.getZ() + 0.5D;
			double d3 = 0.52D;
			double d4 = rand.nextDouble() * 0.6D - 0.3D;
			
			if (rand.nextDouble() < 0.2D) {
				world.playSound(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1F, 1F, false);
			}
			
			switch (enumfacing) {
				case WEST:
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 - d3, d1, d2 + d4, 0D, 0D, 0D);
					world.spawnParticle(EnumParticleTypes.FLAME, d0 - d3, d1, d2 + d4, 0D, 0D, 0D);
					break;
				case EAST:
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d3, d1, d2 + d4, 0D, 0D, 0D);
					world.spawnParticle(EnumParticleTypes.FLAME, d0 + d3, d1, d2 + d4, 0D, 0D, 0D);
					break;
				case NORTH:
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 - d3, 0D, 0D, 0D);
					world.spawnParticle(EnumParticleTypes.FLAME, d0 + d4, d1, d2 - d3, 0D, 0D, 0D);
					break;
				case SOUTH:
					world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 + d3, 0D, 0D, 0D);
					world.spawnParticle(EnumParticleTypes.FLAME, d0 + d4, d1, d2 + d3, 0D, 0D, 0D);
			}
		}
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(FACING_HORIZONTAL, placer.getHorizontalFacing().getOpposite());
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		world.setBlockState(pos, state.withProperty(FACING_HORIZONTAL, placer.getHorizontalFacing().getOpposite()), 2);
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (!keepInventory) {
			TileEntity tileentity = world.getTileEntity(pos);
			
			if (tileentity instanceof TileNuclearFurnace) {
				InventoryHelper.dropInventoryItems(world, pos, ((TileNuclearFurnace)tileentity).getInventory());
				world.updateComparatorOutputLevel(pos, this);
			}
		}
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
		return Container.calcRedstone(world.getTileEntity(pos));
	}
	
	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(NCBlocks.nuclear_furnace_idle);
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.byIndex(meta);
		
		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}
		
		return getDefaultState().withProperty(FACING_HORIZONTAL, enumfacing);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING_HORIZONTAL).getIndex();
	}
	
	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return super.withRotation(state, rot);
	}
	
	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return super.withMirror(state, mirrorIn);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {FACING_HORIZONTAL});
	}
}
