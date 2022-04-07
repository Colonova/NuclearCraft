package nc.multiblock.fission.salt.block;

import nc.NuclearCraft;
import nc.enumm.MetaEnums;
import nc.multiblock.fission.FissionReactor;
import nc.multiblock.fission.block.BlockFissionMetaPart;
import nc.multiblock.fission.salt.tile.TileSaltFissionHeater;
import nc.util.*;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class BlockSaltFissionMetaHeater2 extends BlockFissionMetaPart<MetaEnums.CoolantHeaterType2> {
	
	public final static PropertyEnum<MetaEnums.CoolantHeaterType2> TYPE = PropertyEnum.create("type", MetaEnums.CoolantHeaterType2.class);
	
	public BlockSaltFissionMetaHeater2() {
		super(MetaEnums.CoolantHeaterType2.class, TYPE);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TYPE);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		switch (metadata) {
			case 0:
				return new TileSaltFissionHeater.Tin();
			case 1:
				return new TileSaltFissionHeater.Lead();
			case 2:
				return new TileSaltFissionHeater.Boron();
			case 3:
				return new TileSaltFissionHeater.Lithium();
			case 4:
				return new TileSaltFissionHeater.Magnesium();
			case 5:
				return new TileSaltFissionHeater.Manganese();
			case 6:
				return new TileSaltFissionHeater.Aluminum();
			case 7:
				return new TileSaltFissionHeater.Silver();
			case 8:
				return new TileSaltFissionHeater.Fluorite();
			case 9:
				return new TileSaltFissionHeater.Villiaumite();
			case 10:
				return new TileSaltFissionHeater.Carobbiite();
			case 11:
				return new TileSaltFissionHeater.Arsenic();
			case 12:
				return new TileSaltFissionHeater.LiquidNitrogen();
			case 13:
				return new TileSaltFissionHeater.LiquidHelium();
			case 14:
				return new TileSaltFissionHeater.Enderium();
			case 15:
				return new TileSaltFissionHeater.Cryotheum();
			default:
				break;
		}
		return new TileSaltFissionHeater.Tin();
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (player == null) {
			return false;
		}
		if (hand != EnumHand.MAIN_HAND || player.isSneaking()) {
			return false;
		}
		
		if (!world.isRemote) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileSaltFissionHeater) {
				TileSaltFissionHeater heater = (TileSaltFissionHeater) tile;
				FissionReactor reactor = heater.getMultiblock();
				if (reactor != null) {
					FluidStack fluidStack = FluidStackHelper.getFluid(player.getHeldItem(hand));
					if (heater.canModifyFilter(0) && heater.getTanks().get(0).isEmpty() && fluidStack != null && !FluidStackHelper.stacksEqual(heater.getFilterTanks().get(0).getFluid(), fluidStack) && heater.getTanks().get(0).canFillFluidType(fluidStack)) {
						player.sendMessage(new TextComponentString(Lang.localise("message.nuclearcraft.filter") + " " + TextFormatting.BOLD + Lang.localise(fluidStack.getUnlocalizedName())));
						FluidStack filter = fluidStack.copy();
						filter.amount = 1000;
						heater.getFilterTanks().get(0).setFluid(filter);
						heater.onFilterChanged(0);
					}
					else {
						player.openGui(NuclearCraft.instance, 203, world, pos.getX(), pos.getY(), pos.getZ());
					}
					return true;
				}
			}
		}
		return rightClickOnPart(world, pos, player, hand, facing, true);
	}
}
