package com.builtbroken.mc.framework.multiblock;

import com.builtbroken.mc.api.tile.multiblock.IMultiTile;
import com.builtbroken.mc.api.tile.multiblock.IMultiTileHost;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.lang.ref.WeakReference;

/**
 * Basic implementation of a multi block
 * Created by Dark on 8/9/2015.
 */
public class TileMulti extends TileEntity implements IMultiTile
{
    public static final String NBT_HOST_POS = "hostPos";

    private WeakReference<IMultiTileHost> hostWeakReference;
    private BlockPos hostPosition = null;

    @Override
    public IMultiTileHost getHost()
    {
        if (hostWeakReference != null && hostWeakReference.get() != null)
        {
            return hostWeakReference.get();
        }
        else if (hostPosition != null && world.isBlockLoaded(hostPosition))
        {
            TileEntity tile = world.getTileEntity(hostPosition);
            if (tile instanceof IMultiTileHost)
            {
                setHost((IMultiTileHost) tile);
                return (IMultiTileHost) tile;
            }
        }
        return null;
    }

    public boolean hasHost()
    {
        return hostPosition != null;
    }

    public boolean isHostLoaded()
    {
        return hostPosition != null && world.isBlockLoaded(hostPosition);
    }

    @Override
    public void setHost(IMultiTileHost host)
    {
        if (host != null)
        {
            hostWeakReference = new WeakReference(host);
            hostPosition = new BlockPos(host.xi(), host.yi(), host.zi());
        }
        else
        {
            hostWeakReference.clear();
            hostWeakReference = null;
            hostPosition = null;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey(NBT_HOST_POS))
        {
            int[] data = compound.getIntArray(NBT_HOST_POS);
            hostPosition = new BlockPos(data[0], data[1], data[2]);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        if (hostPosition != null)
        {
            compound.setIntArray(NBT_HOST_POS, new int[]{hostPosition.getX(), hostPosition.getY(), hostPosition.getZ()});
        }
        return this.writeToNBT(compound);
    }

    @Override
    public void invalidate()
    {
        if (getHost() != null)
        {
            getHost().onTileInvalidate(this);
        }
        super.invalidate();
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[ DIM@" + (world != null && world.provider != null ? world.provider.getDimension() + " " : "null ") + getPos().getX() + "x " + getPos().getY() + "y " + getPos().getZ() + "z " + "]@" + hashCode();
    }
}