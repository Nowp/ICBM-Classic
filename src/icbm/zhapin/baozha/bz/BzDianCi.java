package icbm.zhapin.baozha.bz;

import ic2.api.item.ISpecialElectricItem;
import ic2.api.tile.IEnergyStorage;
import icbm.api.IDisableable;
import icbm.api.IMissile;
import icbm.api.RadarRegistry;
import icbm.api.explosion.IEMPBlock;
import icbm.api.explosion.IEMPItem;
import icbm.core.ZhuYaoICBM;
import icbm.zhapin.ZhuYaoZhaPin;
import icbm.zhapin.baozha.BaoZha;
import icbm.zhapin.zhapin.EZhaDan;

import java.util.List;

import mffs.api.IForceFieldBlock;
import mffs.api.fortron.IFortronStorage;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import universalelectricity.core.block.IElectricalStorage;
import universalelectricity.core.item.IItemElectric;
import universalelectricity.core.vector.Vector3;

public class BzDianCi extends BaoZha
{
	private boolean effectEntities = false;
	private boolean effectBlocks = false;

	public BzDianCi(World world, Entity entity, double x, double y, double z, float size)
	{
		super(world, entity, x, y, z, size);
	}

	public BzDianCi setEffectBlocks()
	{
		this.effectBlocks = true;
		return this;
	}

	public BzDianCi setEffectEntities()
	{
		this.effectEntities = true;
		return this;
	}

	@Override
	public void doExplode()
	{
		if (this.effectBlocks)
		{
			for (int x = (int) -this.getRadius(); x < (int) this.getRadius(); x++)
			{
				for (int y = (int) -this.getRadius(); y < (int) this.getRadius(); y++)
				{
					for (int z = (int) -this.getRadius(); z < (int) this.getRadius(); z++)
					{
						double dist = MathHelper.sqrt_double((x * x + y * y + z * z));

						Vector3 searchPosition = Vector3.add(position, new Vector3(x, y, z));
						if (dist > this.getRadius())
							continue;

						if (Math.round(position.x + y) == position.intY())
						{
							worldObj.spawnParticle("largesmoke", searchPosition.x, searchPosition.y, searchPosition.z, 0, 0, 0);
						}

						int blockID = searchPosition.getBlockID(worldObj);
						Block block = Block.blocksList[blockID];
						TileEntity tileEntity = searchPosition.getTileEntity(worldObj);

						if (block != null)
						{
							if (block instanceof IForceFieldBlock)
							{
								((IForceFieldBlock) Block.blocksList[blockID]).weakenForceField(worldObj, searchPosition.intX(), searchPosition.intY(), searchPosition.intZ(), 1000);
							}
							else if (block instanceof IEMPBlock)
							{
								((IEMPBlock) block).onEMP(worldObj, searchPosition, this);
							}
							else if (tileEntity != null)
							{
								if (tileEntity instanceof IElectricalStorage)
								{
									((IElectricalStorage) tileEntity).setEnergyStored(0);
								}

								if (tileEntity instanceof IDisableable)
								{
									((IDisableable) tileEntity).onDisable(400);
								}

								if (tileEntity instanceof IFortronStorage)
								{
									((IFortronStorage) tileEntity).provideFortron((int) worldObj.rand.nextFloat() * ((IFortronStorage) tileEntity).getFortronCapacity(), true);
								}

								if (tileEntity instanceof IEnergyStorage)
								{
									((IEnergyStorage) tileEntity).setStored(0);
								}
							}
						}
					}
				}
			}
		}

		if (this.effectEntities)
		{
			// Drop all missiles
			List<Entity> entitiesNearby = RadarRegistry.getEntitiesWithinRadius(position.toVector2(), (int) this.getRadius());

			for (Entity entity : entitiesNearby)
			{
				if (entity instanceof IMissile && !entity.isEntityEqual(this.controller))
				{
					if (((IMissile) entity).getTicksInAir() > -1)
					{
						((IMissile) entity).dropMissileAsItem();
					}
				}
			}

			AxisAlignedBB bounds = AxisAlignedBB.getBoundingBox(position.x - this.getRadius(), position.y - this.getRadius(), position.z - this.getRadius(), position.x + this.getRadius(), position.y + this.getRadius(), position.z + this.getRadius());
			List<Entity> entities = worldObj.getEntitiesWithinAABB(Entity.class, bounds);

			for (Entity entity : entities)
			{
				if (entity instanceof EntityPlayer)
				{
					IInventory inventory = ((EntityPlayer) entity).inventory;

					for (int i = 0; i < inventory.getSizeInventory(); i++)
					{
						ItemStack itemStack = inventory.getStackInSlot(i);

						if (itemStack != null)
						{
							if (itemStack.getItem() instanceof IEMPItem)
							{
								((IEMPItem) itemStack.getItem()).onEMP(itemStack, entity, this);
							}
							else if (itemStack.getItem() instanceof IItemElectric)
							{
								((IItemElectric) itemStack.getItem()).setElectricity(itemStack, 0);
							}
							else if (itemStack.getItem() instanceof ISpecialElectricItem)
							{
								((ISpecialElectricItem) itemStack.getItem()).getManager(itemStack).discharge(itemStack, ((ISpecialElectricItem) itemStack.getItem()).getMaxCharge(itemStack), 0, true, false);
							}
						}
					}
				}
				else if (entity instanceof EZhaDan)
				{
					entity.setDead();
				}
			}
		}

		ZhuYaoZhaPin.proxy.spawnParticle("shockwave", worldObj, position, 0, 0, 0, 0, 0, 255, 10, 3);
		this.worldObj.playSoundEffect(position.x, position.y, position.z, ZhuYaoICBM.PREFIX + "emp", 4.0F, (1.0F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
	}

	@Override
	public float getEnergy()
	{
		return 3000;
	}
}