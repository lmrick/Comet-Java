package com.cometproject.server.protocol.security;

import java.util.stream.IntStream;

public class SecurityUtil {
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		IntStream.range(0, bytes.length).forEachOrdered(j -> {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		});
		return new String(hexChars);
	}
	
	public static byte[] hexToBytes(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		IntStream.iterate(0, i -> i < len, i -> i + 2).forEachOrdered(i -> data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16)));
		return data;
	}
	
}
