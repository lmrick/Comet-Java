package com.cometproject.server.game.rooms.types.components.types;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.game.furniture.types.IFurnitureDefinition;
import com.cometproject.api.game.furniture.types.ItemType;
import com.cometproject.api.game.furniture.types.ILimitedEditionItem;
import com.cometproject.api.game.players.data.components.inventory.IPlayerItem;
import com.cometproject.api.game.rooms.components.RoomComponentContext;
import com.cometproject.api.game.rooms.components.types.IItemsComponent;
import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.composers.catalog.UnseenItemsMessageComposer;
import com.cometproject.server.game.rooms.types.components.RoomComponent;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.players.types.Player;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.pathfinding.AffectedTile;
import com.cometproject.server.game.rooms.objects.items.RoomItem;
import com.cometproject.server.game.rooms.objects.items.RoomItemFactory;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.RoomItemWall;
import com.cometproject.server.game.rooms.objects.items.types.floor.*;
import com.cometproject.server.game.rooms.objects.items.types.floor.games.GameTimerFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.WiredFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.highscore.HighScoreFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.wall.MoodLightWallItem;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.mapping.RoomTile;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.outgoing.notification.NotificationMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.avatar.AvatarUpdateMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.engine.UpdateStackMapMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.items.RemoveFloorItemMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.items.RemoveWallItemMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.items.SendFloorItemMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.items.SendWallItemMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.inventory.UpdateInventoryMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.storage.api.StorageContext;
import com.cometproject.storage.api.data.DataWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ItemsComponent extends RoomComponent implements IItemsComponent {
	private static final int MAX_FOOTBALLS_IN_ROOM = 15;
	private static final int MAX_JUKEBOX_IN_ROOM = 1;
	private final Logger log;
	private final Map<Long, RoomItemFloor> floorItems = new ConcurrentHashMap<>();
	private final Map<Long, RoomItemWall> wallItems = new ConcurrentHashMap<>();
	private final Map<Integer, String> itemOwners = new ConcurrentHashMap<>();
	private final Room room;
	private final Map<Class<? extends RoomItemFloor>, Set<Long>> itemClassIndex = new ConcurrentHashMap<>();
	private final Map<String, Set<Long>> itemInteractionIndex = new ConcurrentHashMap<>();
	private long soundMachineId = 0;
	private long moodLightId;
	
	public ItemsComponent(RoomComponentContext roomComponentContext) {
		super(roomComponentContext);
		
		this.log = LogManager.getLogger(MessageFormat.format("Room Items Component [{0}]", roomComponentContext.getRoom().getData().getName()));
		this.room = (Room) roomComponentContext.getRoom();

		this.itemClassIndex.put(HighScoreFloorItem.class, Sets.newConcurrentHashSet());
		this.itemClassIndex.put(GameTimerFloorItem.class, Sets.newConcurrentHashSet());
		
		this.loadItems();
	}
	
	@Override
	public RoomComponentContext getRoomComponentContext() {
		return super.getRoomComponentContext();
	}
	
	private void loadItems() {
		if (room.getCachedData() != null) {
			room.getCachedData().getFloorItems().forEach(floorItemDataObject -> {
				final RoomItemData data = new RoomItemData(floorItemDataObject.getId(), floorItemDataObject.getItemDefinitionId(), floorItemDataObject.getOwner(), floorItemDataObject.getOwnerName(), floorItemDataObject.getPosition(), floorItemDataObject.getRotation(), floorItemDataObject.getData(), "", floorItemDataObject.getLimitedEditionItemData());
				this.floorItems.put(floorItemDataObject.getId(), RoomItemFactory.createFloor(data, room, ItemManager.getInstance().getDefinition(floorItemDataObject.getItemDefinitionId())));
			});
			
			room.getCachedData().getWallItems().forEach(wallItemDataObject -> {
				final RoomItemData data = new RoomItemData(wallItemDataObject.getId(), wallItemDataObject.getItemDefinitionId(), wallItemDataObject.getOwner(), wallItemDataObject.getOwnerName(), new Position(), 0, wallItemDataObject.getData(), wallItemDataObject.getWallPosition(), wallItemDataObject.getLimitedEditionItemData());
				this.wallItems.put(wallItemDataObject.getId(), RoomItemFactory.createWall(data, room, ItemManager.getInstance().getDefinition(wallItemDataObject.getItemDefinitionId())));
			});
		} else {
			final DataWrapper<List<RoomItemData>> items = DataWrapper.createEmpty();
			
			StorageContext.getCurrentContext().getRoomItemRepository().getItemsByRoomId(this.room.getId(), items::set);
			
			if (items.has()) {
				items.get().forEach(roomItem -> {
					final IFurnitureDefinition itemDefinition = ItemManager.getInstance().getDefinition(roomItem.getItemId());
					if (itemDefinition == null) return;
					if (itemDefinition.getItemType() == ItemType.FLOOR) {
						this.floorItems.put(roomItem.getId(), RoomItemFactory.createFloor(roomItem, room, itemDefinition));
					} else if (itemDefinition.getItemType() == ItemType.WALL) {
						this.wallItems.put(roomItem.getId(), RoomItemFactory.createWall(roomItem, room, itemDefinition));
					}
				});
			}
		}
		
		this.indexItems();
	}
	
	private void indexItems() {
		this.floorItems.values().forEach(floorItem -> {
			if (floorItem instanceof SoundMachineFloorItem) {
				soundMachineId = floorItem.getId();
			}
			if (floorItem instanceof WiredFloorItem) {
				List<Long> itemsToRemove = Lists.newArrayList();
				
				((WiredFloorItem) floorItem).getWiredData().getSelectedIds().forEach(selectedItemId -> {
					final RoomItemFloor floor = this.getFloorItem(selectedItemId);
					if (floor != null) {
						floor.getWiredItems().add(floorItem.getId());
					} else {
						itemsToRemove.add(selectedItemId);
					}
				});
				
				itemsToRemove.forEach(itemId -> ((WiredFloorItem) floorItem).getWiredData().getSelectedIds().remove(itemId));
				
				itemsToRemove.clear();
			}
			this.indexItem(floorItem);
		});
	}
	
	public void onLoaded() {
		floorItems.values().forEach(RoomItem::onLoad);
		wallItems.values().forEach(RoomItem::onLoad);
	}
	
	@Override
	public void dispose() {
		floorItems.values().forEach(floorItem -> {
			ItemManager.getInstance().disposeItemVirtualId(floorItem.getId());
			floorItem.onUnload();
		});
		
		wallItems.values().forEach(wallItem -> {
			ItemManager.getInstance().disposeItemVirtualId(wallItem.getId());
			wallItem.onUnload();
		});
		
		this.floorItems.clear();
		this.wallItems.clear();
		
		this.itemClassIndex.values().forEach(Set::clear);
		
		itemInteractionIndex.values().forEach(Set::clear);
		
		this.itemOwners.clear();
		this.itemInteractionIndex.clear();
		this.itemClassIndex.clear();
	}
	
	public boolean setMoodLight(long moodLight) {
		if (this.moodLightId != 0) return false;
		this.moodLightId = moodLight;
		return true;
	}
	
	public boolean removeMoodLight() {
		if (this.moodLightId == 0) return false;
		this.moodLightId = 0;
		return true;
	}
	
	public void commit() {
		if (!CometSettings.storageItemQueueEnabled) {
		}
	}
	
	public boolean isMoodLightMatches(RoomItem item) {
		if (this.moodLightId == 0) return false;
		return (this.moodLightId == item.getId());
	}
	
	public MoodLightWallItem getMoodLight() {
		return (MoodLightWallItem) this.getWallItem(this.moodLightId);
	}
	
	public RoomItemFloor addFloorItem(long id, int baseId, Room room, int ownerId, String ownerName, int x, int y, int rot, double height, String data, ILimitedEditionItem limitedEditionItem) {
		final RoomItemData itemData = new RoomItemData(id, baseId, ownerId, ownerName, new Position(x, y, height), rot, data, "", limitedEditionItem);
		
		RoomItemFloor floor = RoomItemFactory.createFloor(itemData, room, ItemManager.getInstance().getDefinition(baseId));
		
		if (floor == null) return null;
		
		this.floorItems.put(id, floor);
		this.indexItem(floor);
		
		return floor;
	}
	
	public RoomItemWall addWallItem(long id, int baseId, Room room, int ownerId, String ownerName, String position, String data) {
		final RoomItemData itemData = new RoomItemData(id, baseId, ownerId, ownerName, new Position(), 0, data, position, null);
		RoomItemWall wall = RoomItemFactory.createWall(itemData, room, ItemManager.getInstance().getDefinition(baseId));
		this.getWallItems().put(id, wall);
		
		return wall;
	}
	
	public List<RoomItemFloor> getItemsOnSquare(int x, int y) {
		RoomTile tile = this.getRoom().getMapping().getTile(x, y);
		
		if (tile == null) {
			return Lists.newArrayList();
		}
		
		return new ArrayList<>(tile.getItems());
	}
	
	public RoomItemFloor getFloorItem(int id) {
		Long itemId = ItemManager.getInstance().getItemIdByVirtualId(id);
		
		if (itemId == null) {
			return null;
		}
		
		return this.floorItems.get(itemId);
	}
	
	public RoomItemWall getWallItem(int id) {
		Long itemId = ItemManager.getInstance().getItemIdByVirtualId(id);
		
		if (itemId == null) {
			return null;
		}
		
		return this.wallItems.get(itemId);
	}
	
	public RoomItemFloor getFloorItem(long id) {
		return this.floorItems.get(id);
	}
	
	public RoomItemWall getWallItem(long id) {
		return this.wallItems.get(id);
	}
	
	public List<RoomItemFloor> getByInteraction(String interaction) {
		List<RoomItemFloor> items = new ArrayList<>();
		
		this.floorItems.values().stream().filter(floorItem -> floorItem != null && floorItem.getDefinition() != null).forEachOrdered(floorItem -> {
			if (floorItem.getDefinition().getInteraction().equals(interaction)) {
				items.add(floorItem);
			} else if (interaction.contains("%")) {
				if (interaction.startsWith("%") && floorItem.getDefinition().getInteraction().endsWith(interaction.replace("%", ""))) {
					items.add(floorItem);
				} else if (interaction.endsWith("%") && floorItem.getDefinition().getInteraction().startsWith(interaction.replace("%", ""))) {
					items.add(floorItem);
				}
			}
		});
		
		return items;
	}
	
	public <T extends RoomItemFloor> List<T> getByClass(Class<T> clazz) {
		List<T> items = new ArrayList<>();
		
		if (this.itemClassIndex.containsKey(clazz)) {
			items = this.itemClassIndex.get(clazz).stream()
			.mapToLong(itemId -> itemId).mapToObj(this::getFloorItem)
			.filter(floorItem -> floorItem != null && floorItem.getDefinition() != null)
			.map(clazz::cast).collect(Collectors.toList());
		}
		
		return items;
	}
	
	public void removeItem(RoomItemWall item, int ownerId, Session client) {
		StorageContext.getCurrentContext().getRoomItemRepository().removeItemFromRoom(item.getId(), ownerId, item.getItemData().getData());
		
		room.getEntities().broadcastMessage(new RemoveWallItemMessageComposer(ItemManager.getInstance().getItemVirtualId(item.getId()), ownerId));
		this.getWallItems().remove(item.getId());
		
		if (client != null && client.getPlayer() != null) {
			client.getPlayer().getInventory().add(item.getId(), item.getItemData().getItemId(), item.getItemData().getData(), item.getLimitedEditionItemData());
			client.send(new UpdateInventoryMessageComposer());
		}
	}
	
	public void removeItem(RoomItemFloor item, Session client) {
		removeItem(item, client, true);
	}
	
	public void removeItem(RoomItemFloor item, Session client, boolean toInventory) {
		if (item instanceof SoundMachineFloorItem) {
			this.soundMachineId = 0;
		}
		
		if (!item.getWiredItems().isEmpty()) {
			item.getWiredItems().stream().mapToLong(wiredItem -> wiredItem).mapToObj(wiredItem -> (WiredFloorItem) this.getFloorItem(wiredItem)).filter(Objects::nonNull).forEachOrdered(floorItem -> floorItem.getWiredData().getSelectedIds().remove(item.getId()));
		}
		
		removeItem(item, client, toInventory, false);
	}
	
	public void removeItem(RoomItemFloor item, Session session, boolean toInventory, boolean delete) {
		List<RoomEntity> affectEntities = room.getEntities().getEntitiesAt(item.getPosition());
		List<Position> tilesToUpdate = new ArrayList<>();
		
		tilesToUpdate.add(new Position(item.getPosition().getX(), item.getPosition().getY(), 0d));
		
		affectEntities.forEach(item::onEntityStepOff);
		
		if (item instanceof SoundMachineFloorItem) {
			if (this.soundMachineId == item.getId()) {
				this.soundMachineId = 0;
			}
		}
		
		AffectedTile.getAffectedTilesAt(item.getDefinition().getLength(), item.getDefinition().getWidth(), item.getPosition().getX(), item.getPosition().getY(), item.getRotation()).forEach(tile -> {
			List<RoomEntity> entitiesOnItem = room.getEntities().getEntitiesAt(new Position(tile.x, tile.y));
			tilesToUpdate.add(new Position(tile.x, tile.y, 0d));
			entitiesOnItem.forEach(item::onEntityStepOff);
		});
		
		Session client = session;
		int owner = item.getItemData().getOwnerId();
		
		if (session != null) {
			if (owner != session.getPlayer().getId()) {
				client = NetworkManager.getInstance().getSessions().getByPlayerId(owner);
			}
		}
		
		this.getRoom().getEntities().broadcastMessage(new RemoveFloorItemMessageComposer(item.getVirtualId(), (session != null) ? owner : 0));
		this.getFloorItems().remove(item.getId());
		
		StorageContext.getCurrentContext().getRoomItemRepository().removeItemFromRoom(item.getId(), owner, item.getDataObject());
		
		if (toInventory && client != null) {
			final IPlayerItem playerItem = client.getPlayer().getInventory().add(item.getId(), item.getItemData().getItemId(), item.getItemData().getData(), item instanceof GiftFloorItem ? ((GiftFloorItem) item).getGiftData() : null, item.getLimitedEditionItemData());
			client.sendQueue(new UpdateInventoryMessageComposer());
			client.sendQueue(new UnseenItemsMessageComposer(Sets.newHashSet(playerItem), ItemManager.getInstance()));
			client.flush();
		} else {
			if (delete) StorageContext.getCurrentContext().getRoomItemRepository().deleteItem(item.getId());
		}
		
		tilesToUpdate.stream().map(tileToUpdate -> this.room.getMapping().getTile(tileToUpdate.getX(), tileToUpdate.getY())).filter(Objects::nonNull).forEachOrdered(tileInstance -> {
			tileInstance.reload();
			room.getEntities().broadcastMessage(new UpdateStackMapMessageComposer(tileInstance));
		});
	}
	
	public void removeItem(RoomItemWall item, Session client, boolean toInventory) {
		this.getRoom().getEntities().broadcastMessage(new RemoveWallItemMessageComposer(item.getVirtualId(), item.getItemData().getOwnerId()));
		this.getWallItems().remove(item.getId());
		
		if (toInventory) {
			StorageContext.getCurrentContext().getRoomItemRepository().removeItemFromRoom(item.getId(), item.getItemData().getOwnerId(), item.getItemData().getData());
			Session session = client;
			
			if (item.getItemData().getOwnerId() != client.getPlayer().getId()) {
				session = NetworkManager.getInstance().getSessions().getByPlayerId(item.getItemData().getOwnerId());
			}
			
			if (session != null) {
				session.getPlayer().getInventory().add(item.getId(), item.getItemData().getItemId(), item.getItemData().getData(), item.getLimitedEditionItemData());
				session.send(new UpdateInventoryMessageComposer());
				session.send(new UnseenItemsMessageComposer(new HashMap<>() {{
					put(1, Lists.newArrayList(item.getVirtualId()));
				}}));
			}
		} else {
			StorageContext.getCurrentContext().getRoomItemRepository().deleteItem(item.getId());
		}
	}
	
	public boolean moveFloorItem(long itemId, Position newPosition, int rotation, boolean save) {
		return moveFloorItem(itemId, newPosition, rotation, save, true, null);
	}
	
	public boolean moveFloorItem(long itemId, Position newPosition, int rotation, boolean save, boolean obeyStack, Player mover) {
		RoomItemFloor item = this.getFloorItem(itemId);
		if (item == null) return false;
		
		RoomTile tile = this.getRoom().getMapping().getTile(newPosition.getX(), newPosition.getY());
		
		if (this.verifyItemPosition(item.getDefinition(), item, tile, item.getPosition(), rotation)) {
			return false;
		}
		
		double height = obeyStack ? tile.getStackHeight(item) : newPosition.getZ();
		
		if (mover != null) {
			if (mover.getItemPlacementHeight() >= 0) {
				height = mover.getItemPlacementHeight();
			}
		}
		
		List<RoomItemFloor> floorItemsAt = this.getItemsOnSquare(newPosition.getX(), newPosition.getY());
		
		floorItemsAt.stream().filter(stackItem -> item.getId() != stackItem.getId()).forEachOrdered(stackItem -> stackItem.onItemAddedToStack(item));
		
		item.onPositionChanged(newPosition);
		
		List<RoomEntity> affectEntities0 = room.getEntities().getEntitiesAt(item.getPosition());
		
		affectEntities0.forEach(item::onEntityStepOff);
		
		List<Position> tilesToUpdate = new ArrayList<>();
		
		tilesToUpdate.add(new Position(item.getPosition().getX(), item.getPosition().getY()));
		tilesToUpdate.add(new Position(newPosition.getX(), newPosition.getY()));
		
		try {
			
			AffectedTile.getAffectedBothTilesAt(item.getDefinition().getLength(), item.getDefinition().getWidth(), item.getPosition().getX(), item.getPosition().getY(), item.getRotation()).forEach(affectedTile -> {
				tilesToUpdate.add(new Position(affectedTile.x, affectedTile.y));
				List<RoomEntity> affectEntities1 = room.getEntities().getEntitiesAt(new Position(affectedTile.x, affectedTile.y));
				affectEntities1.forEach(entity1 -> {
					item.onEntityStepOff(entity1);
					if (tile.getWalkHeight() != entity1.getPosition().getZ()) {
						entity1.getPosition().setZ(tile.getWalkHeight());
						this.getRoom().getEntities().broadcastMessage(new AvatarUpdateMessageComposer(entity1));
					}
				});
			});
			
			AffectedTile.getAffectedBothTilesAt(item.getDefinition().getLength(), item.getDefinition().getWidth(), newPosition.getX(), newPosition.getY(), rotation).forEach(affectedTile -> {
				tilesToUpdate.add(new Position(affectedTile.x, affectedTile.y));
				List<RoomEntity> affectEntities2 = room.getEntities().getEntitiesAt(new Position(affectedTile.x, affectedTile.y));
				affectEntities2.forEach(item::onEntityStepOn);
			});
		} catch (Exception e) {
			log.error("Failed to update entity positions for changing item position", e);
		}
		
		item.getPosition().setX(newPosition.getX());
		item.getPosition().setY(newPosition.getY());
		
		item.getPosition().setZ(height);
		item.getItemData().setRotation(rotation);
		
		List<RoomEntity> affectEntities3 = room.getEntities().getEntitiesAt(newPosition);
		
		affectEntities3.forEach(item::onEntityStepOn);
		
		if (save) item.save();
		
		tilesToUpdate.stream().map(tileToUpdate -> this.room.getMapping().getTile(tileToUpdate.getX(), tileToUpdate.getY())).filter(Objects::nonNull).forEachOrdered(tileInstance -> {
			tileInstance.reload();
			room.getEntities().broadcastMessage(new UpdateStackMapMessageComposer(tileInstance));
		});
		
		tilesToUpdate.clear();
		return true;
	}
	
	private boolean verifyItemPosition(IFurnitureDefinition item, RoomItemFloor floor, RoomTile tile, Position currentPosition, int rotation) {
		if (tile != null) {
			if (currentPosition != null && currentPosition.getX() == tile.getPosition().getX() && currentPosition.getY() == tile.getPosition().getY())
				return false;
			
			List<AffectedTile> affectedTiles = AffectedTile.getAffectedBothTilesAt(item.getLength(), item.getWidth(), tile.getPosition().getX(), tile.getPosition().getY(), rotation);
			
			for (AffectedTile affectedTile : affectedTiles) {
				final RoomTile roomTile = this.getRoom().getMapping().getTile(affectedTile.x, affectedTile.y);
				
				if (roomTile != null) {
					if (!this.verifyItemTilePosition(item, floor, roomTile, rotation)) {
						return true;
					}
				} else {
					return true;
				}
			}
		} else {
			return true;
		}
		
		return false;
	}
	
	private boolean verifyItemTilePosition(IFurnitureDefinition item, RoomItemFloor floorItem, RoomTile tile, int rotation) {
		if (!tile.canPlaceItemHere()) {
			return false;
		}
		
		if (floorItem instanceof RollableFloorItem && this.itemClassIndex.containsKey(RollableFloorItem.class)) {
			final int count = this.itemClassIndex.get(RollableFloorItem.class).size();
			
			if (count >= MAX_FOOTBALLS_IN_ROOM) {
				return false;
			}
		}
		
		if (!tile.canStack() && tile.getTopItem() != 0 && (floorItem == null || tile.getTopItem() != floorItem.getId())) {
			if (!item.getItemName().startsWith(RoomItemFactory.STACK_TOOL)) return false;
		}
		
		if (!item.getInteraction().equals(RoomItemFactory.TELEPORT_PAD) && tile.getPosition().getX() == this.getRoom().getModel().getDoorX() && tile.getPosition().getY() == this.getRoom().getModel().getDoorY()) {
			return false;
		}
		
		if (item.getInteraction().equals("dice")) {
			boolean hasOtherDice = false;
			boolean hasStackTool = false;
			
			for (RoomItemFloor itemFloor : tile.getItems()) {
				if (itemFloor instanceof DiceFloorItem) {
					hasOtherDice = true;
				}
				
				if (itemFloor instanceof MagicStackFloorItem) {
					hasStackTool = true;
				}
			}
			
			if (hasOtherDice && hasStackTool) return false;
		}
		
		if (!CometSettings.roomCanPlaceItemOnEntity) {
			return tile.getEntities().isEmpty();
		}
		
		return true;
	}
	
	public void placeWallItem(IPlayerItem item, String position, Player player) {
		int roomId = this.room.getId();
		
		StorageContext.getCurrentContext().getRoomItemRepository().placeWallItem(roomId, position, item.getExtraData().trim().isEmpty() ? "0" : item.getExtraData(), item.getId());
		player.getInventory().removeItem(item.getId());
		
		RoomItemWall wallItem = this.addWallItem(item.getId(), item.getBaseId(), this.room, player.getId(), player.getData().getUsername(), position, (item.getExtraData().isEmpty() || item.getExtraData().equals(" ")) ? "0" : item.getExtraData());
		
		this.room.getEntities().broadcastMessage(new SendWallItemMessageComposer(wallItem));
		
		wallItem.onPlaced();
	}
	
	public Room getRoom() {
		return this.room;
	}
	
	public Map<Long, RoomItemFloor> getFloorItems() {
		return this.floorItems;
	}
	
	public Map<Long, RoomItemWall> getWallItems() {
		return this.wallItems;
	}
	
	public void placeFloorItem(IPlayerItem item, int x, int y, int rot, Player player) {
		RoomTile tile = room.getMapping().getTile(x, y);
		
		if (tile == null) return;
		
		double height = player.getItemPlacementHeight() >= 0 ? player.getItemPlacementHeight() : tile.getStackHeight();
		
		if (this.verifyItemPosition(item.getDefinition(), null, tile, null, rot)) return;
		
		if (item.getDefinition().getInteraction().equals("soundmachine")) {
			if (this.soundMachineId > 0) {
				Map<String, String> notificationParams = Maps.newHashMap();
				
				notificationParams.put("message", Locale.get("game.room.jukeboxExists"));
				
				player.getSession().send(new NotificationMessageComposer("furni_placement_error", notificationParams));
				return;
			} else {
				this.soundMachineId = item.getId();
			}
		}
		
		List<RoomItemFloor> floorItems = room.getItems().getItemsOnSquare(x, y);
		
		if (item.getDefinition() != null && item.getDefinition().getInteraction() != null) {
			if (item.getDefinition().getInteraction().equals("mannequin")) {
				rot = 2;
			}
		}
		
		StorageContext.getCurrentContext().getRoomItemRepository().placeFloorItem(room.getId(), x, y, height, rot, (item.getExtraData().isEmpty() || item.getExtraData().equals(" ")) ? "0" : item.getExtraData(), item.getBaseId(), item.getId());
		player.getInventory().removeItem(item.getId());
		
		RoomItemFloor floorItem = room.getItems().addFloorItem(item.getId(), item.getBaseId(), room, player.getId(), player.getData().getUsername(), x, y, rot, height, (item.getExtraData().isEmpty() || item.getExtraData().equals(" ")) ? "0" : item.getExtraData(), item.getLimitedEditionItem());
		List<Position> tilesToUpdate = new ArrayList<>();
		
		floorItems.stream().filter(stackItem -> item.getId() != stackItem.getId()).forEachOrdered(stackItem -> stackItem.onItemAddedToStack(floorItem));
		
		tilesToUpdate.add(new Position(floorItem.getPosition().getX(), floorItem.getPosition().getY(), 0d));
		
		AffectedTile.getAffectedBothTilesAt(item.getDefinition().getLength(), item.getDefinition().getWidth(), floorItem.getPosition().getX(), floorItem.getPosition().getY(), floorItem.getRotation()).forEach(affTile -> {
			tilesToUpdate.add(new Position(affTile.x, affTile.y, 0d));
			List<RoomEntity> affectEntities0 = room.getEntities().getEntitiesAt(new Position(affTile.x, affTile.y));
			affectEntities0.forEach(floorItem::onEntityStepOn);
		});
		
		tilesToUpdate.stream().map(tileToUpdate -> this.room.getMapping().getTile(tileToUpdate.getX(), tileToUpdate.getY())).filter(Objects::nonNull).forEachOrdered(tileInstance -> {
			tileInstance.reload();
			room.getEntities().broadcastMessage(new UpdateStackMapMessageComposer(tileInstance));
		});
		
		room.getEntities().broadcastMessage(new SendFloorItemMessageComposer(floorItem));
		
		
		floorItem.onPlaced();
		floorItem.saveData();
	}
	
	private void indexItem(RoomItemFloor floorItem) {
		this.itemOwners.put(floorItem.getItemData().getOwnerId(), floorItem.getItemData().getOwnerName());
		
		if (!this.itemClassIndex.containsKey(floorItem.getClass())) {
			itemClassIndex.put(floorItem.getClass(), new HashSet<>());
		}
		
		if (floorItem instanceof HighScoreFloorItem) {
			itemClassIndex.get(HighScoreFloorItem.class).add(floorItem.getId());
		}
		
		if (floorItem instanceof GameTimerFloorItem) {
			itemClassIndex.get(GameTimerFloorItem.class).add(floorItem.getId());
		}
		
		if (!this.itemInteractionIndex.containsKey(floorItem.getDefinition().getInteraction())) {
			this.itemInteractionIndex.put(floorItem.getDefinition().getInteraction(), new HashSet<>());
		}
		
		this.itemClassIndex.get(floorItem.getClass()).add(floorItem.getId());
		this.itemInteractionIndex.get(floorItem.getDefinition().getInteraction()).add(floorItem.getId());
	}
	
	public SoundMachineFloorItem getSoundMachine() {
		if (this.soundMachineId != 0) {
			return ((SoundMachineFloorItem) this.getFloorItem(this.soundMachineId));
		}
		
		return null;
	}
	
	public Map<Integer, String> getItemOwners() {
		return this.itemOwners;
	}
	
}
