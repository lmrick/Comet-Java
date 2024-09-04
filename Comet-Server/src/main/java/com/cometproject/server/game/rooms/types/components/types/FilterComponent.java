package com.cometproject.server.game.rooms.types.components.types;

import com.cometproject.api.game.rooms.components.RoomComponentContext;
import com.cometproject.api.game.rooms.components.types.IFilterComponent;
import com.cometproject.server.game.rooms.types.components.RoomComponent;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.storage.queries.rooms.RoomFilterDao;

import java.util.Set;

public class FilterComponent extends RoomComponent implements IFilterComponent {
	private final RoomComponentContext roomComponentContext;
	private final Room room;
	private final Set<String> filteredWords;
	
	public FilterComponent(RoomComponentContext roomComponentContext) {
		super(roomComponentContext);
		
		this.room = (Room) roomComponentContext.getRoom();
		this.roomComponentContext = roomComponentContext;
		this.filteredWords = RoomFilterDao.getFilterForRoom(room.getId());
	}
	
	@Override
	public RoomComponentContext getRoomComponentContext() {
		return roomComponentContext;
	}
	
	public void add(String word) {
		this.filteredWords.add(word);
		RoomFilterDao.saveWord(word, this.room.getId());
	}
	
	public void remove(String word) {
		this.filteredWords.remove(word);
		RoomFilterDao.removeWord(word, this.room.getId());
	}
	
	public String filter(PlayerEntity entity, String message) {
		String msg = message;
		
		if (!entity.hasRights()) {
			for (String word : this.filteredWords) {
				if (message.contains(word)) {
					msg = msg.replace(word, Locale.getOrDefault("filter.bobba", "bobba"));
				}
			}
		}
		
		return msg;
	}
	
	public Set<String> getFilteredWords() {
		return this.filteredWords;
	}
	
}
