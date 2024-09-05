package com.cometproject.server.network.messages.incoming.catalog.groups;

import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.groups.types.IGroupData;
import com.cometproject.server.composers.group.GroupDataMessageComposer;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GroupFurnitureCatalogMessageEvent implements Event {
	
	@Override
	public void handle(Session client, MessageEvent msg) throws Exception {
		final List<IGroupData> groupData = client.getPlayer().getGroups().stream().map(groupId -> GameContext.getCurrent().getGroupService().getData(groupId)).filter(Objects::nonNull).collect(Collectors.toList());
		
		client.send(new GroupDataMessageComposer(groupData, GameContext.getCurrent().getGroupService().getItemService(), client.getPlayer().getId()));
	}
	
}
