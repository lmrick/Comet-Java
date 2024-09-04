package com.cometproject.server.utilities;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BadgeUtil {
	
	private static String format(int num) {
		return (num < 10 ? "0" : "") + num;
	}
	
	public static String generate(int guildBase, int guildBaseColor, List<Integer> guildStates) {
		return IntStream.iterate(0, i -> i < 3 * 4, i -> i + 3).mapToObj(i -> i >= guildStates.size() ? "s" : "s" + format(guildStates.get(i)) + format(guildStates.get(i + 1)) + guildStates.get(i + 2)).collect(Collectors.joining("", "b" + format(guildBase) + format(guildBaseColor), ""));
	}
	
}
