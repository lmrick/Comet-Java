package com.cometproject.server.utilities.comporators;

import com.cometproject.server.game.rooms.objects.RoomFloorObject;
import com.cometproject.server.game.rooms.objects.RoomObject;
import java.util.Comparator;

public class PositionComparator implements Comparator<RoomFloorObject> {
	private final RoomObject roomFloorObject;
	
	public PositionComparator(RoomObject roomFloorObject) {
		this.roomFloorObject = roomFloorObject;
	}
	
	@Override
	public int compare(RoomFloorObject firstObject, RoomFloorObject secondObject) {
		final double distanceOne = firstObject.getPosition().distanceTo(this.roomFloorObject.getPosition());
		final double distanceTwo = secondObject.getPosition().distanceTo(this.roomFloorObject.getPosition());
		
		if (distanceOne > distanceTwo) return 1;
		else if (distanceOne < distanceTwo) return -1;
		
		return 0;
	}
	
}
