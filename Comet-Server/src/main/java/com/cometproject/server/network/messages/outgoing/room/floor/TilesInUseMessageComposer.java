package com.cometproject.server.network.messages.outgoing.room.floor;

import com.cometproject.api.game.utilities.Position;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.List;

public class TilesInUseMessageComposer extends MessageComposer {
	
	private final List<Position> tiles;
	
	public TilesInUseMessageComposer(final List<Position> tiles) {
		this.tiles = tiles;
	}
	
	@Override
	public short getId() {
		return Composers.FloorPlanFloorMapMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(tiles.size());
		
		tiles.forEach(position -> {
			msg.writeInt(position.getX());
			msg.writeInt(position.getY());
		});
	}
	
	@Override
	public void dispose() {
		this.tiles.clear();
	}
	
}
