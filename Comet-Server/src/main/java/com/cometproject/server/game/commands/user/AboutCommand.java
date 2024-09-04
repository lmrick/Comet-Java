package com.cometproject.server.game.commands.user;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.stats.CometStats;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.GameCycle;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.network.messages.outgoing.notification.AdvancedAlertMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.utilities.CometRuntime;

import java.lang.management.ManagementFactory;
import java.text.MessageFormat;
import java.text.NumberFormat;

public class AboutCommand extends ChatCommand {
	
	@Override
	public void execute(Session client, String[] message) {
		StringBuilder about = new StringBuilder();
		NumberFormat format = NumberFormat.getInstance();
		
		CometStats cometStats = Comet.getStats();
		
		boolean aboutDetailed = client.getPlayer().getPermissions().getRank().aboutDetailed();
		boolean aboutStats = client.getPlayer().getPermissions().getRank().aboutStats();
		
		if (CometSettings.aboutShowRoomsActive || CometSettings.aboutShowUptime || aboutDetailed) {
			about.append("<b>Server Status</b><br>");
			
			if (CometSettings.aboutShowPlayersOnline || aboutDetailed)
				about.append("Users online: ").append(format.format(cometStats.getPlayers())).append("<br>");
			
			if (CometSettings.aboutShowRoomsActive || aboutDetailed)
				about.append("Active rooms: ").append(format.format(cometStats.getRooms())).append("<br>");
			
			if (CometSettings.aboutShowUptime || aboutDetailed)
				about.append("Uptime: ").append(cometStats.getUptime()).append("<br>");
			
			about.append("Client version: ").append(Session.CLIENT_VERSION).append("<br>");
		}
		
		if (client.getPlayer().getPermissions().getRank().aboutDetailed()) {
			about.append("<br><b>Server Info</b><br>");
			about.append("Allocated memory: ").append(format.format(cometStats.getAllocatedMemory())).append("MB<br>");
			about.append("Used memory: ").append(format.format(cometStats.getUsedMemory())).append("MB<br>");
			
			about.append("Process ID: ").append(CometRuntime.processId).append("<br>");
			about.append("OS: ").append(cometStats.getOperatingSystem()).append("<br>");
			about.append("CPU cores:  ").append(cometStats.getCpuCores()).append("<br>");
			about.append("Threads:  ").append(ManagementFactory.getThreadMXBean().getThreadCount()).append("<br>");
		}
		
		if (aboutStats) {
			about.append("<br><br><b>Hotel Stats</b><br>");
			about.append("Online record: ").append(GameCycle.getInstance().getOnlineRecord()).append("<br>");
			about.append("Record since last reboot: ").append(GameCycle.getInstance().getCurrentOnlineRecord()).append("<br>");
		}
		
		about.append("<br><b>Comet Server (PRIVATE-2020)</b><br>Official Comet Server, a private server made with | in the UK.");
		
		client.send(new AdvancedAlertMessageComposer(MessageFormat.format("Comet Server - {0}", Comet.getBuild()), about.toString(), "www.cometsrv.com", "https://www.cometsrv.com", CometSettings.aboutImg));
	}
	
	@Override
	public String getPermission() {
		return "about_command";
	}
	
	@Override
	public String getParameter() {
		return "";
	}
	
	@Override
	public String getDescription() {
		return Locale.get("command.about.description");
	}
	
}
