package com.cometproject.server.game.navigator.types.search;

import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.groups.types.IGroupData;
import com.cometproject.api.game.players.data.components.messenger.IMessengerFriend;
import com.cometproject.api.game.rooms.IRoomData;
import com.cometproject.api.game.rooms.settings.RoomAccessType;
import com.cometproject.server.game.navigator.NavigatorManager;
import com.cometproject.server.game.navigator.types.categories.Category;
import com.cometproject.server.game.players.types.Player;
import com.cometproject.server.game.rooms.RoomManager;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.network.messages.outgoing.navigator.updated.NavigatorSearchResultSetMessageComposer;
import com.cometproject.server.tasks.ICometTask;
import com.google.common.collect.Lists;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class NavigatorSearchService implements ICometTask {
	private static NavigatorSearchService searchServiceInstance;
	private final Executor searchExecutor = Executors.newFixedThreadPool(8);
	
	public NavigatorSearchService() {
		//CometThreadManager.getInstance().executePeriodic(this, 0, 3000, TimeUnit.MILLISECONDS);
	}
	
	public static List<IRoomData> order(List<IRoomData> rooms, int limit) {
		try {
			rooms.sort((room1, room2) -> {
				boolean is1Active = RoomManager.getInstance().isActive(room1.getId());
				boolean is2Active = RoomManager.getInstance().isActive(room2.getId());
				return ((!is2Active ? 0 : RoomManager.getInstance().get(room2.getId()).getEntities().playerCount()) - (!is1Active ? 0 : RoomManager.getInstance().get(room1.getId()).getEntities().playerCount()));
			});
		} catch (Exception ignored) {
		
		}
		
		List<IRoomData> returnRooms = new LinkedList<>();
		rooms.stream().takeWhile(roomData -> returnRooms.size() < limit).forEachOrdered(returnRooms::add);
		
		return returnRooms;
	}
	
	public static NavigatorSearchService getInstance() {
		if (searchServiceInstance == null) searchServiceInstance = new NavigatorSearchService();
		return searchServiceInstance;
	}
	
	@Override
	public void run() {
		// TODO: Cache navigator search results.
	}
	
	public void submitRequest(Player player, String category, String data) {
		this.searchExecutor.execute(() -> {
			if (data.isEmpty()) {
				
				List<Category> categoryList = Lists.newArrayList();
				
				for (Category navigatorCategory : NavigatorManager.getInstance().getCategories().values()) {
					if (navigatorCategory.getCategory().equals(category)) {
						if (navigatorCategory.isVisible() && !navigatorCategory.getCategoryType().toString().equalsIgnoreCase("with_rights") && !navigatorCategory.getCategoryType().toString().toLowerCase().equals("with_friends") && !navigatorCategory.getCategoryType().toString().toLowerCase().equals("my_groups") && !navigatorCategory.getCategoryType().toString().toLowerCase().equals("my_friends_rooms"))
							categoryList.add(navigatorCategory);
					}
					
					if (category.equals("myworld_view")) {
						if (navigatorCategory.getCategoryType().toString().equalsIgnoreCase("my_friends_rooms")) {
							boolean friendsRoomsNotEmpty = false;
							
							for (IMessengerFriend messengerFriend : player.getMessenger().getFriends().values()) {
								if (friendsRoomsNotEmpty) {
									continue;
								}
								
								if (messengerFriend.isInRoom()) {
									PlayerEntity playerEntity = (PlayerEntity) messengerFriend.getSession().getPlayer().getEntity();
									
									if (playerEntity != null) {
										if (playerEntity.getRoom().getData().getAccess() == RoomAccessType.INVISIBLE && player.getData().getRank() < 3) {
											if (playerEntity.getRoom().getGroup() != null) {
												continue;
											} else {
												if (!playerEntity.getRoom().getRights().hasRights(player.getId())) {
													continue;
												}
											}
										}
										
										friendsRoomsNotEmpty = true;
									}
								}
							}
							
							if (friendsRoomsNotEmpty) {
								categoryList.add(navigatorCategory);
							}
						}
						
						if (navigatorCategory.getCategoryType().toString().equalsIgnoreCase("with_friends")) {
							boolean withFriendsRoomsNotEmpty = false;
							
							for (IMessengerFriend messengerFriend : player.getMessenger().getFriends().values()) {
								if (withFriendsRoomsNotEmpty) {
									continue;
								}
								
								if (messengerFriend.isInRoom()) {
									PlayerEntity playerEntity = (PlayerEntity) messengerFriend.getSession().getPlayer().getEntity();
									
									if (playerEntity != null && !playerEntity.getPlayer().getSettings().getHideOnline()) {
										if (playerEntity.getRoom().getData().getAccess() == RoomAccessType.INVISIBLE && player.getData().getRank() < 3) {
											if (playerEntity.getRoom().getGroup() != null) {
												if (!player.getGroups().contains(playerEntity.getRoom().getGroup().getId())) {
													continue;
												}
											} else {
												if (!playerEntity.getRoom().getRights().hasRights(player.getId())) {
													continue;
												}
											}
										}
										
										withFriendsRoomsNotEmpty = true;
									}
								}
							}
							
							if (withFriendsRoomsNotEmpty) {
								categoryList.add(navigatorCategory);
							}
						}
						
						if (navigatorCategory.getCategoryType().toString().equalsIgnoreCase("my_groups")) {
							boolean groupHomeRoomsNotEmpty = false;
							
							for (int groupId : player.getGroups()) {
								if (groupHomeRoomsNotEmpty) {
									continue;
								}
								
								IGroupData groupData = GameContext.getCurrent().getGroupService().getData(groupId);
								
								if (groupData != null) {
									IRoomData roomData = GameContext.getCurrent().getRoomService().getRoomData(groupData.getRoomId());
									
									if (roomData != null) {
										groupHomeRoomsNotEmpty = true;
									}
								}
							}
							
							if (groupHomeRoomsNotEmpty) {
								categoryList.add(navigatorCategory);
							}
						}
						
						if (navigatorCategory.getCategoryType().toString().equalsIgnoreCase("with_rights") && !player.getRoomsWithRights().isEmpty()) {
							categoryList.add(navigatorCategory);
						}
					}
				}
				
				if (categoryList.isEmpty()) {
					NavigatorManager.getInstance().getCategories().values().stream().filter(navigatorCategory -> navigatorCategory.getCategoryType().toString().toLowerCase().equals(category) && navigatorCategory.isVisible()).forEachOrdered(categoryList::add);
				}
				
				if (categoryList.isEmpty()) {
					NavigatorManager.getInstance().getCategories().values().stream().filter(navigatorCategory -> navigatorCategory.getCategoryId().equals(category) && navigatorCategory.isVisible()).forEachOrdered(categoryList::add);
				}
				
				player.getSession().send(new NavigatorSearchResultSetMessageComposer(category, data, categoryList, player));
			} else {
				player.getSession().send(new NavigatorSearchResultSetMessageComposer("hotel_view", data, null, player));
			}
		});
	}
	
	public List<IRoomData> search(Category category, Player player, boolean expanded) {
		List<IRoomData> rooms = Lists.newCopyOnWriteArrayList();
		
		switch (category.getCategoryType()) {
			case RECOMMENDED -> {
			}
			case QUERY -> {
			}
			case MY_ROOMS -> {
				if (player.getRooms() == null) {
					break;
				}
				
				rooms = new LinkedList<>(player.getRooms()).stream().filter(roomId -> GameContext.getCurrent().getRoomService().getRoomData(roomId) != null).map(roomId -> GameContext.getCurrent().getRoomService().getRoomData(roomId)).collect(Collectors.toCollection(Lists::newCopyOnWriteArrayList));
			}
			case MY_FAVORITES -> {
				List<IRoomData> favouriteRooms = Lists.newArrayList();
				
				if (player.getNavigator() == null) {
					return rooms;
				}
				
				player.getNavigator().getFavouriteRooms().stream().takeWhile(roomId -> favouriteRooms.size() != 50).map(roomId -> GameContext.getCurrent().getRoomService().getRoomData(roomId)).filter(Objects::nonNull).forEachOrdered(favouriteRooms::add);
				
				rooms.addAll(order(favouriteRooms, expanded ? category.getRoomCountExpanded() : category.getRoomCount()));
				favouriteRooms.clear();
			}
			case POPULAR ->
							rooms.addAll(order(RoomManager.getInstance().getRoomsByCategory(-1, 1, player), expanded ? category.getRoomCountExpanded() : category.getRoomCount()));
			case CATEGORY ->
							rooms.addAll(order(RoomManager.getInstance().getRoomsByCategory(category.getId(), player), expanded ? category.getRoomCountExpanded() : category.getRoomCount()));
			case TOP_PROMOTIONS -> {
				List<IRoomData> promotedRooms = RoomManager.getInstance().getRoomPromotions().values().stream().filter(Objects::nonNull).map(roomPromotion -> GameContext.getCurrent().getRoomService().getRoomData(roomPromotion.getRoomId())).filter(Objects::nonNull).collect(Collectors.toList());
				
				rooms.addAll(order(promotedRooms, expanded ? category.getRoomCountExpanded() : category.getRoomCount()));
				promotedRooms.clear();
			}
			case PUBLIC -> {
				
				List<IRoomData> publicRooms = NavigatorManager.getInstance().getPublicRooms(category.getCategoryId()).values().stream().map(publicRoom -> GameContext.getCurrent().getRoomService().getRoomData(publicRoom.roomId())).filter(Objects::nonNull).collect(Collectors.toList());
				
				rooms.addAll(order(publicRooms, expanded ? category.getRoomCountExpanded() : category.getRoomCount()));
				publicRooms.clear();
			}
			case STAFF_PICKS -> {
				List<IRoomData> staffPicks = NavigatorManager.getInstance().getStaffPicks().stream().mapToInt(roomId -> roomId).mapToObj(roomId -> GameContext.getCurrent().getRoomService().getRoomData(roomId)).filter(Objects::nonNull).collect(Collectors.toList());
				
				rooms.addAll(order(staffPicks, expanded ? category.getRoomCountExpanded() : category.getRoomCount()));
				staffPicks.clear();
			}
			case MY_GROUPS -> {
				List<IRoomData> groupHomeRooms = player.getGroups().stream().mapToInt(groupId -> groupId).mapToObj(groupId -> GameContext.getCurrent().getGroupService().getData(groupId)).filter(Objects::nonNull).map(groupData -> GameContext.getCurrent().getRoomService().getRoomData(groupData.getRoomId())).filter(Objects::nonNull).collect(Collectors.toList());
				
				rooms.addAll(order(groupHomeRooms, expanded ? category.getRoomCountExpanded() : category.getRoomCount()));
				groupHomeRooms.clear();
			}
			case MY_HISTORY -> {
			}
			case MY_FRIENDS_ROOMS -> {
				List<IRoomData> friendsRooms = Lists.newArrayList();
				
				if (player.getMessenger() == null) {
					return rooms;
				}
				
				player.getMessenger().getFriends().values().stream().filter(IMessengerFriend::isInRoom).map(messengerFriend -> (PlayerEntity) messengerFriend.getSession().getPlayer().getEntity()).filter(Objects::nonNull).filter(playerEntity -> !friendsRooms.contains(playerEntity.getRoom().getData())).forEachOrdered(playerEntity -> {
					if (playerEntity.getRoom().getData().getAccess() == RoomAccessType.INVISIBLE && player.getData().getRank() < 3) {
						if (playerEntity.getRoom().getGroup() != null) {
							return;
						} else {
							if (!playerEntity.getRoom().getRights().hasRights(player.getId())) {
								return;
							}
						}
					}
					friendsRooms.add(playerEntity.getRoom().getData());
				});
				
				rooms.addAll(order(friendsRooms, expanded ? category.getRoomCountExpanded() : category.getRoomCount()));
				friendsRooms.clear();
			}
			case WITH_FRIENDS -> {
				List<IRoomData> withFriendsRooms = Lists.newArrayList();
				
				if (player.getMessenger() == null) {
					return rooms;
				}
				
				player.getMessenger().getFriends().values().stream().filter(IMessengerFriend::isInRoom).map(messengerFriend -> (PlayerEntity) messengerFriend.getSession().getPlayer().getEntity()).filter(playerEntity -> playerEntity != null && !playerEntity.getPlayer().getSettings().getHideOnline()).filter(playerEntity -> !withFriendsRooms.contains(playerEntity.getRoom().getData())).forEachOrdered(playerEntity -> {
					if (playerEntity.getRoom().getData().getAccess() == RoomAccessType.INVISIBLE && player.getData().getRank() < 3) {
						if (playerEntity.getRoom().getGroup() != null) {
							if (!player.getGroups().contains(playerEntity.getRoom().getGroup().getId())) {
								return;
							}
						} else {
							if (!playerEntity.getRoom().getRights().hasRights(player.getId())) {
								return;
							}
						}
					}
					withFriendsRooms.add(playerEntity.getRoom().getData());
				});
				
				rooms.addAll(order(withFriendsRooms, expanded ? category.getRoomCountExpanded() : category.getRoomCount()));
				withFriendsRooms.clear();
			}
			case WITH_RIGHTS -> {
				if (player.getRoomsWithRights() == null) {
					break;
				}
				
				for (Integer roomId : new LinkedList<>(player.getRoomsWithRights())) {
					if (GameContext.getCurrent().getRoomService().getRoomData(roomId) == null) continue;
					
					rooms.add(GameContext.getCurrent().getRoomService().getRoomData(roomId));
				}
			}
			case MY_HISTORY_FREQ -> {
			}
			case PROMOTION_CATEGORY -> {
			}
		}
		
		return rooms;
	}
	
}