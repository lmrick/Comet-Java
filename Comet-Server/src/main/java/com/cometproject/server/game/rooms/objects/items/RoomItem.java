package com.cometproject.server.game.rooms.objects.items;

import com.cometproject.api.game.furniture.types.IFurnitureDefinition;
import com.cometproject.api.game.rooms.objects.IRoomItemData;
import com.cometproject.api.game.rooms.objects.data.LimitedEditionItemData;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.game.items.types.LowPriorityItemProcessor;
import com.cometproject.server.game.rooms.objects.BigRoomFloorObject;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.items.types.AdvancedFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.SoundMachineFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.games.football.FootballGateFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.games.banzai.BanzaiTeleporterFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.WiredFloorItem;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.games.RoomGame;
import com.cometproject.server.utilities.attributes.Attributable;
import org.apache.commons.lang.StringUtils;
import com.cometproject.server.utilities.collections.ConcurrentHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class RoomItem extends BigRoomFloorObject implements Attributable {
	
	private final Set<Long> wiredItems = new ConcurrentHashSet<>();
	private final IRoomItemData itemData;
	protected int ticksTimer;
	private LimitedEditionItemData limitedEditionItemData;
	private Map<String, Object> attributes;
	private int moveDirection = -1;
	
	public RoomItem(IRoomItemData roomItemData, Room room) {
		super(roomItemData.getId(), roomItemData.getPosition(), room);
		
		this.itemData = roomItemData;
		this.ticksTimer = -1;
	}
	
	public boolean toggleInteract(boolean state) {
		if (!state) {
			if (!(this instanceof WiredFloorItem)) this.getItemData().setData("0");
			return true;
		}
		
		if (!StringUtils.isNumeric(this.getItemData().getData())) {
			return true;
		}
		
		if (this.getDefinition().getInteractionCycleCount() > 1) {
			if (this.getItemData().getData().isEmpty() || this.getItemData().getData().equals(" ")) this.getItemData().setData("0");
			int interactionState = Integer.parseInt(this.getItemData().getData()) + 1;
			this.getItemData().setData(interactionState > (this.getDefinition().getInteractionCycleCount() - 1) ? "0" : interactionState + "");
			return true;
		} else {
			return false;
		}
	}
	
	public Set<Long> getWiredItems() {
		return this.wiredItems;
	}
	
	public final boolean requiresTick() {
		return this.hasTicks() || this instanceof WiredFloorItem || this instanceof AdvancedFloorItem;
	}
	
	protected final boolean hasTicks() {
		return this.ticksTimer > 0;
	}
	
	protected final void setTicks(int time) {
		this.ticksTimer = time;
		
		if (this instanceof BanzaiTeleporterFloorItem) {
			LowPriorityItemProcessor.getInstance().submit(((RoomItemFloor) this));
		}
	}
	
	protected final void cancelTicks() {
		this.ticksTimer = -1;
	}
	
	public final void tick() {
		this.onTick();
		
		if (this.ticksTimer > 0) {
			this.ticksTimer--;
		}
		
		if (this.ticksTimer == 0) {
			this.cancelTicks();
			this.onTickComplete();
		}
	}
	
	protected void onTick() {}
	protected void onTickComplete() {}
	public void onPlaced() {}
	public void onPickup() {}
	
	public boolean onInteract(RoomEntity entity, int requestData, boolean isWiredTrigger) {
		// Override this
		return true;
	}
	
	public void onLoad() {}
	public void onUnload() {}
	public void onEntityLeaveRoom(RoomEntity entity) {}
	public void onGameStarts(RoomGame roomGame) {}
	public void onGameEnds(RoomGame roomGame) {}
	
	public void composeItemData(IComposerDataWrapper msg) {
		msg.writeInt(1);
		msg.writeInt(0);
		
		msg.writeString((this instanceof FootballGateFloorItem) ? "" : (this instanceof WiredFloorItem) ? ((WiredFloorItem) this).getState() ? "1" : "0" : (this instanceof SoundMachineFloorItem) ? ((SoundMachineFloorItem) this).getState() ? "1" : "0" : this.getItemData().getData());
	}
	
	@Override
	public void setAttribute(String attributeKey, Object attributeValue) {
		if (this.attributes == null) {
			this.attributes = new HashMap<>();
		}
		
		if (this.attributes.containsKey(attributeKey)) {
			this.attributes.replace(attributeKey, attributeValue);
		} else {
			this.attributes.put(attributeKey, attributeValue);
		}
	}
	
	@Override
	public Object getAttribute(String attributeKey) {
		if (this.attributes == null) {
			this.attributes = new HashMap<>();
		}
		
		return this.attributes.get(attributeKey);
	}
	
	@Override
	public boolean hasAttribute(String attributeKey) {
		if (this.attributes == null) {
			this.attributes = new HashMap<>();
		}
		
		return this.attributes.containsKey(attributeKey);
	}
	
	@Override
	public void removeAttribute(String attributeKey) {
		if (this.attributes == null) {
			this.attributes = new HashMap<>();
		}
		
		this.attributes.remove(attributeKey);
	}
	
	public abstract void serialize(IComposerDataWrapper msg);
	public abstract IFurnitureDefinition getDefinition();
	public abstract void sendUpdate();
	public abstract void save();
	public abstract void saveData();
	public abstract int getRotation();
	
	public void dispose() {
	
	}
	
	public IRoomItemData getItemData() {
		return this.itemData;
	}
	
	public LimitedEditionItemData getLimitedEditionItemData() {
		return limitedEditionItemData;
	}
	
	public void setLimitedEditionItemData(LimitedEditionItemData limitedEditionItemData) {
		this.limitedEditionItemData = limitedEditionItemData;
	}
	
	public int getMoveDirection() {
		return moveDirection;
	}
	
	public void setMoveDirection(int moveDirection) {
		this.moveDirection = moveDirection;
	}
	
}
