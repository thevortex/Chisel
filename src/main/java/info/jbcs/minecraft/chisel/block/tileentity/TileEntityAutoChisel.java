package info.jbcs.minecraft.chisel.block.tileentity;

import info.jbcs.minecraft.chisel.carving.CarvableHelper;
import info.jbcs.minecraft.chisel.carving.CarvableVariation;
import info.jbcs.minecraft.chisel.carving.Carving;
import info.jbcs.minecraft.chisel.carving.CarvingVariation;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityAutoChisel extends TileEntity implements ISidedInventory{

	private ItemStack[] inventory = new ItemStack[3];
    private String name = "autoChisel";
    boolean equal = false;

    @Override
    public boolean canUpdate(){
        return true;
    }

	@Override
	public ItemStack decrStackSize(int slot, int size) {
		if (inventory[slot] != null) {
			ItemStack is;
			if (inventory[slot].stackSize <= size) {
				is = inventory[slot];
				inventory[slot] = null;
				return is;
			} else {
				is = inventory[slot].splitStack(size);
				if (inventory[slot].stackSize == 0)
					inventory[slot] = null;
				return is;
			}
		} else
			return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList tags = nbt.getTagList("Items", 10);
		inventory = new ItemStack[getSizeInventory()];

		for (int i = 0; i < tags.tagCount(); i++) {
			NBTTagCompound data = tags.getCompoundTagAt(i);
			int j = data.getByte("Slot") & 255;

			if (j >= 0 && j < inventory.length) {
				inventory[j] = ItemStack.loadItemStackFromNBT(data);
			}
		}

        if (nbt.hasKey("CustomName", 8))
        {
            this.name = nbt.getString("CustomName");
        }
    }

    @Override
    public void updateEntity(){
        if(!worldObj.isRemote && worldObj.getWorldTime() % 20 == 0) {
            if (inventory[0] != null && inventory[1] != null) {
                if(inventory[0].getItem() instanceof ItemBlock && inventory[1].getItem() instanceof ItemBlock){
                    if(Carving.chisel.getOre(Block.getBlockFromItem(inventory[0].getItem()), inventory[0].getItemDamage()) == Carving.chisel.getOre(Block.getBlockFromItem(inventory[1].getItem()), inventory[1].getItemDamage()))
                        equal = true;
                    if (equal) {
                        if (inventory[2] == null) {
                            setInventorySlotContents(2, new ItemStack(inventory[1].getItem(), 1, inventory[1].getItemDamage()));
                            decrStackSize(0, 1);
                        } else {
                            if (inventory[0].stackSize != 0 && inventory[2].stackSize < getInventoryStackLimit()) {
                                decrStackSize(0, 1);
                                inventory[2].stackSize++;
                            } else {
                                inventory[0] = null;
                            }
                        }
                    }
                }
            }
        }

        markDirty();
    }

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		NBTTagList tags = new NBTTagList();

		for (int i = 0; i < inventory.length; i++) {
			if (inventory[i] != null) {
				NBTTagCompound data = new NBTTagCompound();
				data.setByte("Slot", (byte) i);
				inventory[i].writeToNBT(data);
				tags.appendTag(data);
			}
		}
		
		nbt.setTag("Items", tags);

        if (this.hasCustomInventoryName())
        {
            nbt.setString("CustomName", this.name);
        }
    }

    @Override
    public int getSizeInventory(){
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot){
        return inventory[slot];
    }

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if (inventory[slot] != null) {
			ItemStack is = inventory[slot];
			inventory[slot] = null;
			return is;
		} else
			return null;
	}

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack){
        inventory[slot] = stack;

        if(stack != null && stack.stackSize > getInventoryStackLimit()){
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName(){
        return name;
    }

    @Override
    public final boolean isUseableByPlayer(EntityPlayer player){
        return true;
    }

    @Override
    public int getInventoryStackLimit(){
        return 64;
    }

    @Override
    public boolean hasCustomInventoryName(){
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side){
        int[] slots = new int[getSizeInventory()];

        for(int c = 0; c < slots.length; c++){
            slots[c] = c;
        }

        if(side != 0){
            return slots;
        } else if(side == 0){
            return new int[] {2};
        } else {
            return null;
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack itemStack){
        return slot != 1;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, int side){
        if(side != 0){
            if(slot == 0){
                return true;
            } else {
                return false;
            }
        } else if(side == 0){
            if(slot == 1){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemStack, int side){
        if(slot == 2){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void openInventory(){}

    @Override
    public void closeInventory(){
        NBTTagList tags = new NBTTagList();
        System.out.print(tags);
    }
    
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
	}
		
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		readFromNBT(packet.func_148857_g());
	}
}
