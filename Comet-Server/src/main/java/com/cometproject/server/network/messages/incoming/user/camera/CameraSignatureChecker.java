package com.cometproject.server.network.messages.incoming.user.camera;

public class CameraSignatureChecker {

  private static final byte[] SIGNATURE = new byte[] { -119, 80, 78, 71, 13, 10, 26, 10 };

  public static boolean isPngFile(byte[] file) {
		return Arrays.equals(Arrays.copyOfRange(file, 0, 8), SIGNATURE);
	}

}