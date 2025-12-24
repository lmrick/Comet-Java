package com.cometproject.server.game.rooms.types;

import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.bots.IBotData;
import com.cometproject.api.game.groups.types.IGroup;
import com.cometproject.api.game.pets.IPetData;
import com.cometproject.api.game.rooms.IRoom;
import com.cometproject.api.game.rooms.IRoomCategory;
import com.cometproject.api.game.rooms.IRoomData;
import com.cometproject.api.game.rooms.RoomContext;
import com.cometproject.api.game.rooms.components.IRoomComponent;
import com.cometproject.api.game.rooms.components.RoomComponentContext;
import com.cometproject.api.game.rooms.models.CustomFloorMapData;
import com.cometproject.api.game.rooms.models.IRoomModel;
import com.cometproject.api.game.rooms.models.RoomModelData;
import com.cometproject.api.game.rooms.settings.RoomType;
import com.cometproject.api.utilities.JsonUtil;
import com.cometproject.server.game.navigator.NavigatorManager;
import com.cometproject.server.game.rooms.RoomManager;
import com.cometproject.server.game.rooms.RoomQueue;
import com.cometproject.server.game.rooms.objects.entities.types.BotEntity;
import com.cometproject.server.game.rooms.objects.entities.types.PetEntity;
import com.cometproject.server.game.rooms.objects.entities.types.data.PlayerBotData;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.triggers.WiredTriggerAtGivenTime;
import com.cometproject.server.game.rooms.types.components.RoomComponentFactory;
import com.cometproject.server.game.rooms.types.components.types.*;
import com.cometproject.server.game.rooms.types.components.types.promotion.RoomPromotion;
import com.cometproject.server.game.rooms.types.mapping.RoomMapping;
import com.cometproject.server.network.messages.outgoing.room.polls.QuickPollMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.polls.QuickPollResultsMessageComposer;
import com.cometproject.server.network.ws.messages.youtube.YouTubeVideoMessage;
import com.cometproject.server.storage.cache.CacheManager;
import com.cometproject.server.storage.cache.objects.RoomDataObject;
import com.cometproject.server.storage.cache.objects.items.FloorItemDataObject;
import com.cometproject.server.storage.cache.objects.items.WallItemDataObject;
import com.cometproject.server.utilities.attributes.Attributable;
import com.cometproject.storage.api.factories.rooms.RoomModelDataFactory;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Room implements Attributable, IRoom {
	
	private final RoomContext roomContext;
	private RoomComponentContext roomComponentContext;
	
	public static final boolean useCycleForItems = false;
	public static final boolean useCycleForEntities = false;
	
	public final Logger log;
	
	private final IRoomData data;
	
	private final RoomDataObject cachedData;
	private final AtomicInteger wiredTimer = new AtomicInteger(0);
	
	private IRoomModel model;
	private RoomMapping mapping;
	
	private ProcessComponent process;
	private RightsComponent rights;
	private ItemsComponent items;
	private ItemProcessComponent itemProcess;
	private TradeComponent trade;
	private RoomBotComponent bots;
	private PetComponent pets;
	private GameComponent game;
	private EntityComponent entities;
	private FilterComponent filter;
	
	private IGroup group;
	private Map<String, Object> attributes;
	private Set<Integer> ratings;
	private String question;
	private Set<Integer> yesVotes;
	private Set<Integer> noVotes;
	private boolean isDisposed = false;
	private int idleTicks = 0;
	private boolean isReloading = false;
	private boolean forcedUnload = false;
	private YouTubeVideoMessage roomVideo = null;
	
	public Room(IRoomData data) {
		this.data = data;
		this.log = LogManager.getLogger(MessageFormat.format("Room \"{0}\"", this.getData().getName()));
		this.cachedData = null;
		
		this.roomContext = new RoomContext(this);
		RoomContext.setCurrentContext(this.roomContext);
	}
	
	private void injectComponentDependencies() {
		this.roomComponentContext = new RoomComponentContext(this);
		var componentFactory = new RoomComponentFactory(roomComponentContext);
		
		injectComponents(roomComponentContext, componentFactory);
		assignComponents(roomComponentContext);
	}
	
	private void injectComponents(RoomComponentContext ctx, RoomComponentFactory factory) {
		ctx.setEntityComponent(factory.createEntityComponent());
		ctx.setFilterComponent(factory.createFilterComponent());
		ctx.setGameComponent(factory.createGameComponent());
		ctx.setItemsProcessComponent(factory.createItemsProcessComponent());
		ctx.setItemsComponent(factory.createItemsComponent());
		ctx.setPetComponent(factory.createPetComponent());
		ctx.setProcessComponent(factory.createProcessComponent());
		ctx.setRightsComponent(factory.createRightsComponent());
		ctx.setRoomBotComponent(factory.createRoomBotComponent());
		ctx.setTradeComponent(factory.createTradeComponent());
	}
	
	private void assignComponents(RoomComponentContext ctx) {
		this.itemProcess = (ItemProcessComponent) ctx.getItemsProcessComponent();
		this.process = (ProcessComponent) ctx.getProcessComponent();
		this.rights = (RightsComponent) ctx.getRightsComponent();
		this.items = (ItemsComponent) ctx.getItemsComponent();
		this.trade = (TradeComponent) ctx.getTradeComponent();
		this.game = (GameComponent) ctx.getGameComponent();
		this.entities = (EntityComponent) ctx.getEntityComponent();
		this.bots = (RoomBotComponent) ctx.getRoomBotComponent();
		this.pets = (PetComponent) ctx.getPetComponent();
		this.filter = (FilterComponent) ctx.getFilterComponent();
	}
	
	private void disposeAllComponents() {
		RoomComponentFactory.getComponents().forEach(IRoomComponent::dispose);
	}
	
	public Room(RoomDataObject cachedRoomObject) {
		this(cachedRoomObject.getData());
	}
	
	public Room load() {
		this.model = GameContext.getCurrent().getRoomModelService().getModel(this.getData().getModel());
		
		if (this.getData().getHeightmap() != null) {
			RoomModelData roomModelData;
			
			try {
				if (this.getData().getHeightmap().startsWith("{")) {
					CustomFloorMapData mapData = JsonUtil.getInstance().fromJson(this.getData().getHeightmap(), CustomFloorMapData.class);
					roomModelData = RoomModelDataFactory.instance.createData(mapData);
				} else {
					roomModelData = RoomModelDataFactory.instance.createData("dynamic_heightmap", this.getData().getHeightmap(), this.getModel().getRoomModelData().getDoorX(), this.getModel().getRoomModelData().getDoorY(), this.getModel().getRoomModelData().getDoorRotation());
				}
				
				if (roomModelData != null) {
					this.model = GameContext.getCurrent().getRoomModelService().getRoomModelFactory().createModel(roomModelData);
				}
			} catch (Exception e) {
				log.error("Failed to load dynamic room model", e);
			}
		}
		
		// Cache the group.
		this.group = GameContext.getCurrent().getGroupService().getGroup(this.getData().getGroupId());
		
		this.attributes = new HashMap<>();
		this.ratings = new HashSet<>();
		this.mapping = new RoomMapping(this);
		
		injectComponentDependencies();
		
		this.mapping.init();
		
		this.setAttribute("loadTime", System.currentTimeMillis());
		
		if (this.data.getType() == RoomType.PUBLIC) {
			RoomQueue.getInstance().addQueue(this.getId(), 0);
		}
		
		this.log.debug("Room loaded");
		return this;
	}
	
	public IRoomCategory getCategory() {
		return NavigatorManager.getInstance().getCategory(this.data.getCategoryId());
	}
	
	public RoomDataObject getCacheObject() {
		final List<FloorItemDataObject> floorItems;
		final List<WallItemDataObject> wallItems;
		final List<IPetData> petData;
		final List<IBotData> botData;
		
		floorItems = this.getItems().getFloorItems().values().stream().filter(Objects::nonNull).map(floorItem -> new FloorItemDataObject(floorItem.getId(), floorItem.getItemData().getItemId(), this.getId(), floorItem.getItemData().getOwnerId(), floorItem.getItemData().getOwnerName(), floorItem.getDataObject(), floorItem.getPosition(), floorItem.getRotation(), floorItem.getLimitedEditionItemData())).collect(Collectors.toList());
		
		wallItems = this.getItems().getWallItems().values().stream().filter(Objects::nonNull).map(wallItem -> new WallItemDataObject(wallItem.getId(), wallItem.getItemData().getItemId(), this.getId(), wallItem.getItemData().getOwnerId(), wallItem.getItemData().getOwnerName(), wallItem.getItemData().getData(), wallItem.getWallPosition(), wallItem.getLimitedEditionItemData())).collect(Collectors.toList());
		
		final List<Integer> rights = new ArrayList<>(this.rights.getAll());
		
		petData = this.getEntities().getPetEntities().stream().map(PetEntity::getData).filter(Objects::nonNull).collect(Collectors.toList());
		
		botData = this.getEntities().getBotEntities().stream().map(BotEntity::getData).filter(PlayerBotData.class::isInstance).collect(Collectors.toList());
		
		return new RoomDataObject(this.getId(), this.getData(), rights, floorItems, wallItems, petData, botData);
	}
	
	public boolean isIdle() {
		if (this.idleTicks < 600 && this.getEntities().realPlayerCount() > 0) {
			this.idleTicks = 0;
		} else {
			if (this.idleTicks >= 600) {
				return true;
			} else {
				this.idleTicks += 10;
			}
		}
		
		return false;
	}
	
	public void setIdleNow() {
		this.idleTicks = 600;
		this.forcedUnload = true;
	}
	
	public void reload() {
		this.setIdleNow();
		this.isReloading = true;
	}
	
	public void dispose() {
		if (this.isDisposed) {
			return;
		}
		
		long currentTime = System.currentTimeMillis();
		
		boolean isCacheEnabled = CacheManager.getInstance().isEnabled();
		
		if (isCacheEnabled) {
			CacheManager.getInstance().put("rooms." + this.getId(), this.getCacheObject());
		}
		
		this.getItems().commit();
		
		this.isDisposed = true;
		
		
		this.process.stop();
		this.itemProcess.stop();
		this.game.stop();
		
		this.game.dispose();
		this.entities.dispose();
		this.items.dispose();
		this.bots.dispose();
		
		disposeAllComponents();
		
		this.mapping.dispose();
		
		if (this.data.getType() == RoomType.PUBLIC) {
			RoomQueue.getInstance().removeQueue(this.getId());
		}
		
		if (this.forcedUnload) {
			RoomManager.getInstance().removeData(this.getId());
		}
		
		if (this.yesVotes != null) {
			this.yesVotes.clear();
		}
		
		if (this.noVotes != null) {
			this.noVotes.clear();
		}
		
		long timeTaken = System.currentTimeMillis() - currentTime;
		if (timeTaken >= 250) {
			this.log.warn("Room [{}][{}] took {} MS to dispose", this.getData().getId(), this.getData().getName(), timeTaken);
		}
		
		this.log.debug("Room has been disposed");
	}
	
	public void tick() {
		WiredTriggerAtGivenTime.executeTriggers(this, this.wiredTimer.incrementAndGet());
		
		if (this.rights != null) {
			this.rights.tick();
		}
		
		if (this.mapping != null) {
			this.mapping.tick();
		}
	}
	
	public void startQuestion(String question) {
		this.question = question;
		this.yesVotes = Sets.newConcurrentHashSet();
		this.noVotes = Sets.newConcurrentHashSet();
		
		this.getEntities().broadcastMessage(new QuickPollMessageComposer(question));
	}
	
	public void endQuestion() {
		this.question = null;
		
		this.getEntities().broadcastMessage(new QuickPollResultsMessageComposer(this.yesVotes.size(), this.noVotes.size()));
		
		if (this.yesVotes != null) {
			this.yesVotes.clear();
		}
		
		if (this.noVotes != null) {
			this.noVotes.clear();
		}
	}
	
	public int getWiredTimer() {
		return this.wiredTimer.get();
	}
	
	public void resetWiredTimer() {
		this.wiredTimer.set(0);
	}
	
	public RoomPromotion getPromotion() {
		return RoomManager.getInstance().getRoomPromotions().get(this.getId());
	}
	
	@Override
	public void setAttribute(String attributeKey, Object attributeValue) {
		if (this.attributes.containsKey(attributeKey)) {
			this.removeAttribute(attributeKey);
		}
		
		this.attributes.put(attributeKey, attributeValue);
	}
	
	@Override
	public Object getAttribute(String attributeKey) {
		return this.attributes.get(attributeKey);
	}
	
	@Override
	public boolean hasAttribute(String attributeKey) {
		return this.attributes.containsKey(attributeKey);
	}
	
	@Override
	public void removeAttribute(String attributeKey) {
		this.attributes.remove(attributeKey);
	}
	
	public int getId() {
		return this.data.getId();
	}
	
	@Override
	public RoomContext getContext() {
		return this.roomContext;
	}
	
	public IRoomData getData() {
		return this.data;
	}
	
	public IRoomModel getModel() {
		return this.model;
	}
	
	public ProcessComponent getProcess() {
		return (ProcessComponent) this.roomComponentContext.getProcessComponent();
	}
	
	public ItemProcessComponent getItemProcess() {
		return (ItemProcessComponent) this.roomComponentContext.getItemsProcessComponent();
	}
	
	public ItemsComponent getItems() {
		return (ItemsComponent) this.roomComponentContext.getItemsComponent();
	}
	
	public TradeComponent getTrade() {
		return (TradeComponent) this.roomComponentContext.getTradeComponent();
	}
	
	
	public RightsComponent getRights() {
		return (RightsComponent) this.roomComponentContext.getRightsComponent();
	}
	
	public RoomBotComponent getBots() {
		return (RoomBotComponent) this.roomComponentContext.getRoomBotComponent();
	}
	
	public PetComponent getPets() {
		return (PetComponent) this.roomComponentContext.getPetComponent();
	}
	
	public GameComponent getGame() {
		return (GameComponent) this.roomComponentContext.getGameComponent();
	}
	
	public EntityComponent getEntities() {
		return (EntityComponent) this.roomComponentContext.getEntityComponent();
	}
	
	public RoomMapping getMapping() {
		return this.mapping;
	}
	
	public IGroup getGroup() {
		if (this.group == null || this.group.getData() == null) return null;
		
		return this.group;
	}
	
	public void setGroup(final IGroup group) {
		this.group = group;
	}
	
	public boolean hasRoomMute() {
		return this.attributes.containsKey("room_muted") && (boolean) this.attributes.get("room_muted");
	}
	
	public void setRoomMute(boolean mute) {
		if (this.attributes.containsKey("room_muted")) {
			this.attributes.replace("room_muted", mute);
		} else {
			this.attributes.put("room_muted", mute);
		}
	}
	
	public Set<Integer> getRatings() {
		return ratings;
	}
	
	public Set<Integer> getNoVotes() {
		return noVotes;
	}
	
	public Set<Integer> getYesVotes() {
		return yesVotes;
	}
	
	public String getQuestion() {
		return question;
	}
	
	public RoomDataObject getCachedData() {
		return cachedData;
	}
	
	public boolean isReloading() {
		return this.isReloading;
	}
	
	public FilterComponent getFilter() {
		return filter;
	}
	
	public YouTubeVideoMessage getRoomVideo() {
		return roomVideo;
	}
	
	public void setRoomVideo(YouTubeVideoMessage roomVideo) {
		this.roomVideo = roomVideo;
	}
	
}
