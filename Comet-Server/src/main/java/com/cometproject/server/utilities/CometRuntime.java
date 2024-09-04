package com.cometproject.server.utilities;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import org.apache.log4j.Logger;
import java.text.MessageFormat;

public class CometRuntime {
	public static final String operatingSystem = System.getProperty("os.name");
	public static final String operatingSystemArchitecture = System.getProperty("os.arch");
	private static final Logger log = Logger.getLogger(CometRuntime.class.getName());
	public static int processId = 0;
	
	static {
		if (operatingSystem.contains("nix") || operatingSystem.contains("nux")) {
			processId = CLibrary.INSTANCE.getPID();
		} else if (operatingSystem.contains("Windows")) {
			processId = Kernel32.INSTANCE.GetCurrentProcessId();
		} else if (operatingSystem.contains("Mac")) {
			processId = -1000;
		}
		
		if (processId < 1) {
			log.warn(MessageFormat.format("Failed to get process identifier - OS: {0} ({1})", operatingSystem, operatingSystemArchitecture));
		}
	}
	
	private interface CLibrary extends Library {
		
		CLibrary INSTANCE = (CLibrary) Native.loadLibrary("c", CLibrary.class);
		int getPID();
		
	}
	
}
