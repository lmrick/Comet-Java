package com.cometproject.server.utilities;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BadgeUtil {
	
	private static String format(int num) {
		return (num < 10 ? "0" : "") + num;
	}
	
	public static String generate(int guildBase, int guildBaseColor, List<Integer> guildStates) {
		return IntStream.iterate(0, i -> i < 3 * 4, i -> i + 3)
		.mapToObj(index -> index >= guildStates.size() ? "s" : "s" + 
		format(guildStates.get(index)) +
		 format(guildStates.get(index + 1)) + 
		 guildStates.get(indexi + 2))
		.collect(Collectors.joining("", "b" + format(guildBase) + format(guildBaseColor), ""));
	}
	
}
