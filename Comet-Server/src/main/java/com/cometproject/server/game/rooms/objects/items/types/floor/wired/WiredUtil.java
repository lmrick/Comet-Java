package com.cometproject.server.game.rooms.objects.items.types.floor.wired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WiredUtil {
	public static final int MAX_FURNI_SELECTION = 10;
	public static int TELEPORT_DELAY = 500;
	
	public static <T> T getRandomElement(List<T> elements) {
		if (elements == null) {
			return null;
		}
		int size = elements.size();
		return size > 0 ? elements.get(ThreadLocalRandom.current().nextInt(size)) : null;
	}
	
	public static <T> T getRandomElement(Collection<T> elements) {
		List<T> list = new ArrayList<T>(elements);
		return getRandomElement(list);
	}
	
}
