package com.cometproject.game.items.inventory;

import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.furniture.types.IFurnitureDefinition;
import com.cometproject.api.game.furniture.types.ILimitedEditionItem;
import com.cometproject.api.game.players.data.components.inventory.IPlayerItem;
import com.cometproject.api.game.players.data.components.inventory.InventoryItemData;
import com.cometproject.api.game.players.data.components.inventory.IPlayerItemSnapshot;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;

public class InventoryItem implements IPlayerItem {
	private final InventoryItemData itemData;
	private final IFurnitureDefinition furnitureDefinition;
	
	public InventoryItem(InventoryItemData itemData, IFurnitureDefinition furnitureDefinition) {
		this.itemData = itemData;
		this.furnitureDefinition = furnitureDefinition;
	}
	
	protected boolean composeData(IComposerDataWrapper msg) {
		msg.writeInt(1);
		
		if (this.itemData.limitedEditionItem() != null) {
			msg.writeString("");
			msg.writeBoolean(true);
			msg.writeBoolean(false);
		} else {
			msg.writeInt(0);
		}
		
		return true;
	}
	
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(this.getVirtualId());
		msg.writeString(this.getDefinition().getType());
		msg.writeInt(this.getVirtualId());
		msg.writeInt(this.getSpriteId());
		
		if (this.composeData(msg)) {
			msg.writeString(this.getExtraData());
		}
		
		if (this.itemData.limitedEditionItem() != null) {
			msg.writeInt(this.itemData.limitedEditionItem().getLimitedRare());
			msg.writeInt(this.itemData.limitedEditionItem().getLimitedRareTotal());
		}
		
		msg.writeBoolean(this.canRecycle());
		msg.writeBoolean(this.canTrade());
		msg.writeBoolean(this.itemData.limitedEditionItem() == null && this.canInventoryStack());
		msg.writeBoolean(this.canMarketplace());
		
		msg.writeInt(-1);
		msg.writeBoolean(true);
		msg.writeInt(-1);
		msg.writeString("");
		msg.writeInt(this.getExtraInt());
	}
	
	@Override
	public IPlayerItemSnapshot createSnapshot() {
		return null;
	}
	
	private boolean canTrade() {
		return this.furnitureDefinition.canTrade();
	}
	
	private boolean canRecycle() {
		return this.furnitureDefinition.canRecycle();
	}
	
	private boolean canInventoryStack() {
		return this.furnitureDefinition.canInventoryStack();
	}
	
	private boolean canMarketplace() {
		return this.furnitureDefinition.canMarket();
	}
	
	protected int getExtraInt() {
		return 0;
	}
	
	protected int getSpriteId() {
		return this.furnitureDefinition.getSpriteId();
	}
	
	@Override
	public long getId() {
		return this.itemData.id();
	}
	
	@Override
	public IFurnitureDefinition getDefinition() {
		return this.furnitureDefinition;
	}
	
	@Override
	public int getBaseId() {
		return this.itemData.baseId();
	}
	
	@Override
	public String getExtraData() {
		return this.itemData.extraData();
	}
	
	@Override
	public ILimitedEditionItem getLimitedEditionItem() {
		return this.itemData.limitedEditionItem();
	}
	
	@Override
	public int getVirtualId() {
		return GameContext.getCurrent().getFurnitureService().getItemVirtualId(this.itemData.id());
	}
	
}
