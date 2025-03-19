package com.cometproject.server.game.rooms.types.components.types;

import com.cometproject.api.game.rooms.IRoom;
import com.cometproject.api.game.rooms.components.RoomComponentContext;
import com.cometproject.api.game.rooms.components.types.IEntityComponent;
import com.cometproject.api.game.utilities.Position;
import com.cometproject.server.game.players.types.Player;
import com.cometproject.server.game.rooms.objects.entities.RoomEntity;
import com.cometproject.server.game.rooms.objects.entities.RoomEntityType;
import com.cometproject.server.game.rooms.objects.entities.types.BotEntity;
import com.cometproject.server.game.rooms.objects.entities.types.PetEntity;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.types.floor.TeleporterFloorItem;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.game.rooms.types.components.RoomComponent;
import com.cometproject.server.game.rooms.types.components.types.chat.mute.RoomMessageType;
import com.cometproject.server.game.rooms.types.mapping.RoomTile;
import com.cometproject.server.network.messages.outgoing.room.settings.RoomRatingMessageComposer;
import com.cometproject.server.network.ws.messages.WsMessage;
import com.cometproject.server.protocol.messages.MessageComposer;
import com.cometproject.server.utilities.collections.ConcurrentHashSet;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EntityComponent extends RoomComponent implements IEntityComponent {
	private static final Logger log = Logger.getLogger(EntityComponent.class.getName());
	public static final int MAX_BOTS_IN_ROOM = 150;
	public static final int MAX_PETS_IN_ROOM = 50;
	private final Map<Integer, RoomEntity> entities = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> playerIdToEntity = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> botIdToEntity = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> petIdToEntity = new ConcurrentHashMap<>();
	private final Map<String, Integer> nameToPlayerEntity = new ConcurrentHashMap<>();
	private final Set<PlayerEntity> playerEntities = new ConcurrentHashSet<>();
	private final Set<RoomEntity> intelligentEntities = new ConcurrentHashSet<>();
	private final IRoom room;
	private final AtomicInteger entityIdGenerator = new AtomicInteger();
	
	public EntityComponent(RoomComponentContext roomComponentContext) {
		super(roomComponentContext);
		
		this.room = roomComponentContext.getRoom();
	}
	
	@Override
	public RoomComponentContext getRoomComponentContext() {
		return super.getRoomComponentContext();
	}
	
	public List<RoomEntity> getEntitiesAt(Position position) {
		RoomTile tile = this.getRoom().getMapping().getTile(position.getX(), position.getY());
		
		if (tile != null && !tile.getEntities().isEmpty()) {
			return new ArrayList<>(tile.getEntities());
		}
		
		return new ArrayList<>();
	}
	
	public boolean positionHasEntity(Position position) {
		RoomTile tile = this.getRoom().getMapping().getTile(position.getX(), position.getY());
		
		if (tile != null) {
			return !tile.getEntities().isEmpty();
		}
		
		return false;
	}
	
	public boolean positionHasEntity(Position position, final Set<RoomEntity> ignoredEntities) {
		RoomTile tile = this.getRoom().getMapping().getTile(position.getX(), position.getY());
		
		if (tile != null) {
			return tile.getEntities().stream().anyMatch(entity -> !ignoredEntities.contains(entity));
		}
		
		return false;
	}
	
	public PlayerEntity createEntity(Player player) {
		Position startPosition = new Position(this.getRoom().getModel().getRoomModelData().getDoorX(), this.getRoom().getModel().getRoomModelData().getDoorY(), this.getRoom().getModel().getDoorZ());
		
		if (player.isTeleporting()) {
			RoomItemFloor item = ((Room) this.getRoomComponentContext().getRoom()).getItems().getFloorItem(player.getTeleportId());
			
			if (item != null) {
				startPosition = new Position(item.getPosition().getX(), item.getPosition().getY(), item.getPosition().getZ());
			}
		}
		
		int doorRotation = this.getRoom().getModel().getRoomModelData().getDoorRotation();
		
		PlayerEntity entity = new PlayerEntity(player, this.getFreeId(), startPosition, doorRotation, doorRotation, this.getRoom());
		
		if (player.isTeleporting()) {
			RoomItemFloor flItem = ((Room) this.getRoomComponentContext().getRoom()).getItems().getFloorItem(player.getTeleportId());
			
			if ((flItem instanceof TeleporterFloorItem)) {
				((TeleporterFloorItem) flItem).handleIncomingEntity(entity, null);
			}
		}
		
		return entity;
	}
	
	public void addEntity(RoomEntity entity) {
		if (entity.getEntityType() == RoomEntityType.PLAYER) {
			PlayerEntity playerEntity = (PlayerEntity) entity;
			this.nameToPlayerEntity.put(entity.getUsername(), ((PlayerEntity) entity).getPlayerId());
			this.playerIdToEntity.put(playerEntity.getPlayerId(), playerEntity.getId());
			this.playerEntities.add(playerEntity);

		} else if (entity.getEntityType() == RoomEntityType.BOT) {
			BotEntity botEntity = (BotEntity) entity;
			this.botIdToEntity.put(botEntity.getBotId(), botEntity.getId());

		} else if (entity.getEntityType() == RoomEntityType.PET) {
			PetEntity petEntity = (PetEntity) entity;
			this.petIdToEntity.put(petEntity.getData().getId(), petEntity.getId());
		}
		
		if (entity.getAI() != null) {
			this.intelligentEntities.add(entity);
		}
		
		this.entities.put(entity.getId(), entity);
	}
	
	public void removeEntity(RoomEntity entity) {
		final RoomTile tile = this.getRoom().getMapping().getTile(entity.getPosition());
		
		if (tile != null) {
			entity.removeFromTile(tile);
		}
		
		
		if (entity.getEntityType() == RoomEntityType.PLAYER) {
			PlayerEntity playerEntity = (PlayerEntity) entity;
			
			this.playerIdToEntity.remove(playerEntity.getPlayerId());
			this.nameToPlayerEntity.remove(playerEntity.getUsername());
			this.playerEntities.remove(playerEntity);

		} else if (entity.getEntityType() == RoomEntityType.BOT) {
			BotEntity botEntity = (BotEntity) entity;
			this.botIdToEntity.remove(botEntity.getBotId());

		} else if (entity.getEntityType() == RoomEntityType.PET) {
			PetEntity petEntity = (PetEntity) entity;
			this.petIdToEntity.remove(petEntity.getData().getId());
		}
		
		if (entity.getAI() != null) {
			this.intelligentEntities.remove(entity);
		}
		
		this.entities.remove(entity.getId());
	}
	
	public void broadcastMessage(MessageComposer msg, boolean usersWithRightsOnly) {
		broadcastMessage(msg, usersWithRightsOnly, RoomMessageType.GENERIC_COMPOSER);
	}
	
	public void broadcastWs(WsMessage message) {
		this.playerEntities.stream().filter(playerEntity -> playerEntity.getPlayer() != null && playerEntity.getPlayer().getSession() != null).forEachOrdered(playerEntity -> playerEntity.getPlayer().getSession().sendWs(message));
	}
	
	public void broadcastMessage(MessageComposer msg, boolean usersWithRightsOnly, RoomMessageType type) {
		if (msg == null) return;
		
		this.playerEntities.stream().filter(playerEntity -> playerEntity.getPlayer() != null).filter(playerEntity -> !usersWithRightsOnly || ((Room) this.getRoomComponentContext().getRoom()).getRights().hasRights(playerEntity.getPlayerId()) || playerEntity.getPlayer().getPermissions().getRank().roomFullControl()).filter(playerEntity -> type != RoomMessageType.BOT_CHAT || !playerEntity.getPlayer().botsMuted()).filter(playerEntity -> type != RoomMessageType.PET_CHAT || !playerEntity.getPlayer().petsMuted()).forEachOrdered(playerEntity -> playerEntity.getPlayer().getSession().send(msg));
	}
	
	public void broadcastChatMessage(MessageComposer msg, PlayerEntity sender) {
		this.playerEntities.stream().filter(playerEntity -> playerEntity.getPlayer() != null).filter(playerEntity -> playerEntity.getPlayer().ignores(sender.getPlayerId())).forEachOrdered(playerEntity -> playerEntity.getPlayer().getSession().send(msg));
	}
	
	public void broadcastMessage(MessageComposer msg) {
		broadcastMessage(msg, false);
	}
	
	public RoomEntity getEntity(int id) {
		return this.entities.get(id);
	}
	
	public PlayerEntity getEntityByPlayerId(int id) {
		if (!this.playerIdToEntity.containsKey(id)) {
			return null;
		}
		
		int entityId = this.playerIdToEntity.get(id);
		RoomEntity roomEntity = this.entities.get(entityId);
		
		if (roomEntity == null || roomEntity.getEntityType() != RoomEntityType.PLAYER) {
			return null;
		}
		
		return (PlayerEntity) roomEntity;
	}
	
	public PlayerEntity getPlayerEntityByName(final String username) {
		final Integer playerId = this.nameToPlayerEntity.get(username);
		
		if (playerId != null) {
			return this.getEntityByPlayerId(playerId);
		}
		
		return null;
	}
	
	public RoomEntity getEntityByName(String name, RoomEntityType type) {
		return this.getAllEntities().values().stream()
		.filter(entity -> entity.getUsername() != null)
		.filter(entity -> entity.getUsername().equalsIgnoreCase(name) && entity.getEntityType() == type)
		.findFirst().orElse(null);
	}
	
	public BotEntity getEntityByBotId(int id) {
		if (!this.botIdToEntity.containsKey(id)) {
			return null;
		}
		
		int entityId = this.botIdToEntity.get(id);
		RoomEntity roomEntity = this.entities.get(entityId);
		
		if (roomEntity == null || roomEntity.getEntityType() != RoomEntityType.BOT) {
			return null;
		}
		
		return (BotEntity) roomEntity;
	}
	
	public PetEntity getEntityByPetId(int id) {
		if (!this.petIdToEntity.containsKey(id)) {
			return null;
		}
		
		int entityId = this.petIdToEntity.get(id);
		RoomEntity roomEntity = this.entities.get(entityId);
		
		if (roomEntity == null || roomEntity.getEntityType() != RoomEntityType.PET) {
			return null;
		}
		
		return (PetEntity) roomEntity;
	}
	
	public List<BotEntity> getBotEntities() {
		return this.botIdToEntity.values().stream()
		.map(id -> (BotEntity) this.entities.get(id))
		.collect(Collectors.toList());
	}
	
	public List<PetEntity> getPetEntities() {
		return this.petIdToEntity.values().stream()
		.map(id -> (PetEntity) this.entities.get(id))
		.collect(Collectors.toList());
	}
	
	public List<PlayerEntity> getPlayerEntities() {
		return new ArrayList<>(this.playerEntities);
	}
	
	public List<PlayerEntity> getWhisperSeers() {
		List<PlayerEntity> entities = new ArrayList<>();
		
		if (this.entities.isEmpty()) {
			return entities;
		}
		
		entities = this.entities.values().stream()
		.filter(entity -> entity.getEntityType() == RoomEntityType.PLAYER)
		.filter(entity -> ((PlayerEntity) entity).getPlayer().getPermissions().getRank().roomSeeWhispers())
		.map(PlayerEntity.class::cast).collect(Collectors.toList());
		
		return entities;
	}
	
	public void refreshScore() {
		getPlayerEntities().forEach(entity -> entity.getPlayer().getSession().send(new RoomRatingMessageComposer(((Room) this.getRoomComponentContext().getRoom()).getData().getScore(), entity.canRateRoom())));
	}
	
	protected int getFreeId() {
		return this.entityIdGenerator.incrementAndGet();
	}
	
	public int count() {
		return (int) this.entities.values().stream().filter(RoomEntity::isVisible).count();
	}
	
	public int playerCount() {
		return (int) this.playerEntities.stream().filter(RoomEntity::isVisible).count();
	}

	public int botCount() {
		return (int) this.botIdToEntity.values().stream().filter(id -> this.entities.get(id).isVisible()).count();
	}

	public int petCount() {
		return (int) this.petIdToEntity.values().stream().filter(id -> this.entities.get(id).isVisible()).count();
	}
	
	public int realPlayerCount() {
		return this.playerIdToEntity.size();
	}
	
	public Map<Integer, RoomEntity> getAllEntities() {
		return this.entities;
	}
	
	public Set<RoomEntity> getIntelligentEntities() {
		return this.intelligentEntities;
	}
	
	public Room getRoom() {
		return (Room) this.room;
	}

	public RoomEntity getRoomEntity (int entityId) {
		return this.entities.get(entityId);
	}
	
	@Override
	public void dispose() {
		this.entities.forEach((key, value) -> value.onRoomDispose());
		
		this.entities.clear();
		
		this.playerIdToEntity.clear();
		this.botIdToEntity.clear();
		this.petIdToEntity.clear();
		this.playerEntities.clear();
		this.intelligentEntities.clear();
		this.nameToPlayerEntity.clear();
	}
	
}
