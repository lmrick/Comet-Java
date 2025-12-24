package com.cometproject.server.game.commands.user;

import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.game.commands.CommandManager;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.modules.ModuleManager;
import com.cometproject.server.network.messages.outgoing.notification.MotdNotificationMessageComposer;
import com.cometproject.server.network.sessions.Session;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

public class CommandsCommand extends ChatCommand {
	
	@Override
	public void execute(Session client, String[] params) {
		List<String> list;
		String builder;
		
		list = ModuleManager.getInstance().getEventHandler().getCommands().entrySet().stream().filter(commandInfoEntry -> client.getPlayer().getPermissions().hasCommand(commandInfoEntry.getValue().permission()) || commandInfoEntry.getValue().permission().isEmpty()).map(commandInfoEntry -> commandInfoEntry.getKey() + " - " + commandInfoEntry.getValue().description() + "\n").collect(Collectors.toList());
		CommandManager.getInstance().getChatCommands().entrySet().stream().filter(command -> !command.getValue().isHidden()).filter(command -> client.getPlayer().getPermissions().hasCommand(command.getValue().getPermission())).map(command -> command.getKey().split(",")[0] + " " + command.getValue().getParameter() + " " + command.getValue().getDescription() + "\n").forEachOrdered(list::add);
		list.sort(String::compareToIgnoreCase);
		builder = String.join("", list);
		
		client.send(new MotdNotificationMessageComposer(MessageFormat.format("================================================\n{0}\n================================================\n\n{1}", Locale.get("command.commands.title"), builder)));
	}
	
	@Override
	public String getPermission() {
		return "commands_command";
	}
	
	@Override
	public String getParameter() {
		return "";
	}
	
	@Override
	public String getDescription() {
		return Locale.get("command.commands.description");
	}
	
}
