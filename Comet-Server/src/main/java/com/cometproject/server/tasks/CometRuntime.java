package com.cometproject.server.tasks;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.win32.Kernel32;
import org.apache.log4j.Logger;
import java.text.MessageFormat;

public class CometRuntime {
	
	public static final String OPERATING_SYSTEM = System.getProperty("os.name");
	public static final String OPERATING_SYSTEM_ARCHITECTURE = System.getProperty("os.arch");
	private static final Logger log = Logger.getLogger(CometRuntime.class.getName());
	public static int processId = 0;
	
	static {
		if (OPERATING_SYSTEM.contains("nix") || OPERATING_SYSTEM.contains("nux")) {
			processId = CLibrary.INSTANCE.getPID();
		} else if (OPERATING_SYSTEM.contains("Windows")) {
			processId = Kernel32.INSTANCE.GetCurrentProcessId();
		} else if (OPERATING_SYSTEM.contains("Mac")) {
			processId = SystemB.INSTANCE.getpid();
		}
		
		if (processId < 1) {
			log.warn(MessageFormat.format("Failed to get process identifier - OS: {0} ({1})", OPERATING_SYSTEM, OPERATING_SYSTEM_ARCHITECTURE));
		}
	}
	
	private interface CLibrary extends Library {
		CLibrary INSTANCE = Native.load("c", CLibrary.class);
		int getPID();
	}
	
}
