package com.cometproject.server.game.players.components.types;

import com.cometproject.api.utilities.caching.Cache;
import com.cometproject.api.utilities.observers.types.messenger.IMessengerObserver;
import com.cometproject.api.utilities.observers.types.messenger.MessengerObserverService;
import com.cometproject.api.game.achievements.types.AchievementType;
import com.cometproject.api.game.players.components.PlayerComponentContext;
import com.cometproject.api.game.players.data.IPlayerAvatar;
import com.cometproject.api.game.players.data.components.IPlayerMessenger;
import com.cometproject.api.game.players.data.components.messenger.IMessengerFriend;
import com.cometproject.api.networking.messages.IMessageComposer;
import com.cometproject.common.caching.LastReferenceCache;
import com.cometproject.server.game.players.PlayerManager;
import com.cometproject.server.game.players.components.PlayerComponent;
import com.cometproject.server.game.players.components.types.messenger.MessengerSearchResult;
import com.cometproject.server.game.players.types.Player;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.outgoing.messenger.BuddyListMessageComposer;
import com.cometproject.server.network.messages.outgoing.messenger.FriendRequestsMessageComposer;
import com.cometproject.server.network.messages.outgoing.messenger.MessengerSearchResultsMessageComposer;
import com.cometproject.server.network.messages.outgoing.messenger.UpdateFriendStateMessageComposer;
import com.cometproject.server.storage.queries.player.messenger.MessengerDao;
import com.cometproject.server.storage.queries.player.messenger.MessengerSearchDao;
import com.cometproject.server.tasks.CometThreadManager;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessengerComponent extends PlayerComponent implements IPlayerMessenger, IMessengerObserver {
	private final Logger LOG = super.getLogger(MessengerComponent.class);
	private final MessengerObserverService messengerObserver;
	private Map<Integer, IMessengerFriend> friends = new ConcurrentHashMap<>();
	private List<Integer> requests = new CopyOnWriteArrayList<>();
    private boolean initialized;

	private final Cache<Integer, IMessengerFriend> friendsCache = 
	new LastReferenceCache<>(43200 * 1000, 10000, (key, val) -> {
	}, CometThreadManager.getInstance().getCoreExecutor());
	
	public MessengerComponent(PlayerComponentContext componentContext) {
		super(componentContext);

		this.messengerObserver = new MessengerObserverService();
		messengerObserver.addObserver(this);
		
		try {
			Map<Integer, IMessengerFriend> initialFriends = MessengerDao.getFriendsByPlayerId(componentContext.getPlayer().getId());
        	initialFriends.forEach((friendId, friend) -> friendsCache.add(friendId, friend));
        	this.friends.putAll(initialFriends);
		} catch (Exception e) {
			Logger.getLogger(MessengerComponent.class.getName()).error("Error while loading messenger friends", e);
		}
	}

	@Override
    public void onFriendAdded(IMessengerFriend friend) {
		this.messengerObserver.onFriendAdded(friend);
    }

	@Override
    public void onFriendRemoved(int friendId) {
		this.messengerObserver.onFriendRemoved(friendId);
    }

	@Override
    public void onStatusUpdated(boolean online, boolean inRoom) {
		this.messengerObserver.onStatusUpdated(online, inRoom);
    }
	
	public void initialize() {
		this.getPlayer().getSession().send(new BuddyListMessageComposer((Player) this.getPlayer(), this.getFriends(), this.getPlayer().getPermissions().getRank().messengerStaffChat(), this.getPlayer().getPermissions().getRank().messengerLogChat(), this.getPlayer().getGroups()));
		this.getPlayer().getSession().send(new FriendRequestsMessageComposer(this.getRequestAvatars()));
		
		if (this.getPlayer().getAchievements().hasStartedAchievement(AchievementType.FRIENDS_LIST)) {
			this.getPlayer().getAchievements().progressAchievement(AchievementType.FRIENDS_LIST, this.getFriends().size());
		}
		
		this.setInitialized(true);
	}

	@Override
	public void dispose() {
		super.dispose();
		this.sendStatus(false, false);
		
		if (this.getRequests() != null) {
			this.requests.clear();
		}
		
		this.friends.clear();
		this.requests = null;
		this.messengerObserver.removeObserver(this);
	}
	
	@Override
	public IMessageComposer search(String query) {
		List<MessengerSearchResult> currentFriends = Lists.newArrayList();
		List<MessengerSearchResult> otherPeople = Lists.newArrayList();
		
		try {
			for (MessengerSearchResult searchResult : MessengerSearchDao.performSearch(query)) {
				if (this.getFriendById(searchResult.id()) != null) {
					currentFriends.add(searchResult);
				} else {
					otherPeople.add(searchResult);
				}
			}
		} catch (Exception e) {
			this.getPlayer().getSession().getLogger().error("Error while searching for players", e);
		}
		
		return new MessengerSearchResultsMessageComposer(currentFriends, otherPeople);
	}
	
	@Override
	public void addRequest(int playerId) {
		this.getRequests().add(playerId);
		this.getPlayer().flush(this);
	}
	
	@Override
	public void addFriend(IMessengerFriend friend) {
		if (!friends.containsKey(friend.getUserId())) {
			friendsCache.add(friend.getUserId(), friend);
            friends.put(friend.getUserId(), friend);
            onFriendAdded(friend);
            this.getPlayer().getAchievements().progressAchievement(AchievementType.FRIENDS_LIST, 1);
        }
	}
	
	@Override
	public void removeFriend(int userId) {
		if (friends.remove(userId) != null) {
			friendsCache.remove(userId);
            onFriendRemoved(userId);
            MessengerDao.deleteFriendship(this.getPlayer().getId(), userId);
            this.getPlayer().getSession().send(new UpdateFriendStateMessageComposer(-1, userId));
            this.getPlayer().flush(this);
        }
	}
	
	@Override
	public Integer getRequestBySender(int sender) {
		return requests.stream()
		.filter(request -> request == sender)
		.findFirst().orElse(null);
	}
	
	@Override
    public void broadcast(IMessageComposer msg) {
        this.getFriends().values().stream()
            .map(friend -> NetworkManager.getInstance().getSessions().getByPlayerId(friend.getUserId()))
            .filter(session -> session != null && session.getPlayer().getMessenger().isInitialized())
            .forEach(session -> session.send(msg));
    }

    @Override
    public void broadcast(List<Integer> friends, IMessageComposer msg) {
        friends.stream()
            .filter(friendId -> this.friends.containsKey(friendId) && this.friends.get(friendId).isOnline())
            .map(friendId -> NetworkManager.getInstance().getSessions().getByPlayerId(friendId))
            .filter(session -> session != null && session.getPlayer() != null)
            .forEach(session -> session.send(msg));
    }
	
	@Override
	public boolean hasRequestFrom(int playerId) {
		if (this.requests == null) return false;
		
		return this.requests.stream().anyMatch(messengerRequest -> messengerRequest == playerId);
	}
	
	@Override
	public List<IPlayerAvatar> getRequestAvatars() {
		return this.getRequests().stream().mapToInt(playerId -> playerId)
		.mapToObj(playerId -> PlayerManager.getInstance().getAvatarByPlayerId(playerId, IPlayerAvatar.USERNAME_FIGURE))
		.filter(Objects::nonNull).collect(Collectors.toList());
	}
	
	@Override
	public void clearRequests() {
		this.requests.clear();
		this.getPlayer().flush(this);
	}
	
	@Override
	public void sendOffline(int friend, boolean online, boolean inRoom) {
		this.getPlayer().getSession().send(new UpdateFriendStateMessageComposer(PlayerManager.getInstance().getAvatarByPlayerId(friend, IPlayerAvatar.USERNAME_FIGURE_MOTTO), online, inRoom, this.getPlayer().getRelationships().get(friend)));
		this.getPlayer().flush(this);
	}
	
	@Override
	public void sendStatus(boolean online, boolean inRoom) {
		if (this.getPlayer().getSettings().getHideOnline()) {
			LOG.debug("Player is hiding online status: playerId=" + this.getPlayer().getId()); // Added debug log
            return;
        }

        onStatusUpdated(online, inRoom);
        this.getPlayer().flush(this);
	}
	
	@Override
	public IMessengerFriend getFriendById(int id) {		
		if(this.friendsCache.getIfPresent(id).isPresent()) {
			return this.friendsCache.get(id);
		}

		return this.getFriends().get(id);
	}
	
	@Override
	public Map<Integer, IMessengerFriend> getFriends() {
		return this.friends;
	}
	
	@Override
	public List<Integer> getRequests() {
		if (this.requests == null) {
			this.requests = MessengerDao.getRequestsByPlayerId(this.getPlayer().getId());
		}
		
		return this.requests;
	}
	
	@Override
	public void removeRequest(Integer request) {
		this.getRequests().remove(request);
	}
	
	@Override
	public boolean isInitialized() {
		return initialized;
	}
	
	@Override
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
}
