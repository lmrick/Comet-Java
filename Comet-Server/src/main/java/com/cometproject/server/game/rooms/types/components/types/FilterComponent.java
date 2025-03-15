package com.cometproject.server.game.rooms.types.components.types;

import com.cometproject.api.game.rooms.IRoom;
import com.cometproject.api.game.rooms.components.RoomComponentContext;
import com.cometproject.api.game.rooms.components.types.IFilterComponent;
import com.cometproject.server.game.rooms.types.components.RoomComponent;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.storage.queries.rooms.RoomFilterDao;
import java.util.Set;

public class FilterComponent extends RoomComponent implements IFilterComponent {
	private final IRoom room;
	private final Set<String> filteredWords;
	
	public FilterComponent(RoomComponentContext roomComponentContext) {
		super(roomComponentContext);
		
		this.room = roomComponentContext.getRoom();
		this.filteredWords = RoomFilterDao.getFilterForRoom(room.getData().getId());
	}
	
	@Override
	public RoomComponentContext getRoomComponentContext() {
		return super.getRoomComponentContext();
	}
	
	public void add(String word) {
		this.filteredWords.add(word);
		RoomFilterDao.saveWord(word, this.room.getData().getId());
	}
	
	public void remove(String word) {
		this.filteredWords.remove(word);
		RoomFilterDao.removeWord(word, this.room.getData().getId());
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
