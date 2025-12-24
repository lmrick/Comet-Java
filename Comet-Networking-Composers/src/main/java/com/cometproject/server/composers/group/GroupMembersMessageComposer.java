package com.cometproject.server.composers.group;

import com.cometproject.api.game.groups.types.GroupMemberAvatar;
import com.cometproject.api.game.groups.types.IGroupData;
import com.cometproject.api.game.groups.types.components.membership.IGroupMember;
import com.cometproject.api.networking.messages.wrappers.IComposerDataWrapper;
import com.cometproject.server.protocol.headers.Composers;
import com.cometproject.server.protocol.messages.MessageComposer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GroupMembersMessageComposer extends MessageComposer {
	
	private static final int MEMBERS_PER_PAGE = 14;
	
	private final IGroupData group;
	private final int page;
	private final List<GroupMemberAvatar> groupMembers;
	private final int requestType;
	private final String searchQuery;
	private final boolean isAdmin;
	
	public GroupMembersMessageComposer(final IGroupData group, final int page, final List<GroupMemberAvatar> groupMembers, final int requestType, final String searchQuery, final boolean isAdmin) {
		this.group = group;
		this.page = page;
		this.groupMembers = groupMembers;
		this.requestType = requestType;
		this.searchQuery = searchQuery;
		this.isAdmin = isAdmin;
	}
	
	@Override
	public short getId() {
		return Composers.GroupMembersMessageComposer;
	}
	
	@Override
	public void compose(IComposerDataWrapper msg) {
		msg.writeInt(group.getId());
		msg.writeString(group.getTitle());
		msg.writeInt(group.getRoomId());
		msg.writeString(group.getBadge());
		
		msg.writeInt(groupMembers.size());
		
		if (groupMembers.isEmpty()) {
			msg.writeInt(0);
		} else {
			List<List<GroupMemberAvatar>> paginatedMembers = paginateMembers(groupMembers);
			
			msg.writeInt(paginatedMembers.get(page).size());
			
			int dateJoined = 0;
			
			for (GroupMemberAvatar groupMember : paginatedMembers.get(page)) {
				if (groupMember.groupMember() == null) {
					msg.writeInt(requestType == 1 ? groupMember.playerAvatar().getId() == group.getOwnerId() ? 0 : 1 : 3);
				} else {
					final IGroupMember member = groupMember.groupMember();
					
					dateJoined = member.getDateJoined();
					
					msg.writeInt(member.getAccessLevel().isAdmin() ? group.getOwnerId() == groupMember.playerAvatar().getId() ? 0 : 1 : requestType == 2 ? 3 : 2);
				}
				
				msg.writeInt(groupMember.playerAvatar().getId());
				msg.writeString(groupMember.playerAvatar().getUsername());
				msg.writeString(groupMember.playerAvatar().getFigure());
				
				msg.writeString(groupMember.playerAvatar() != null ? GroupInformationMessageComposer.getDate(dateJoined) : "");
			}
			
		}
		
		msg.writeBoolean(isAdmin);
		msg.writeInt(MEMBERS_PER_PAGE);
		msg.writeInt(page);
		
		msg.writeInt(requestType);
		msg.writeString(searchQuery);
	}
	
	private List<List<GroupMemberAvatar>> paginateMembers(List<GroupMemberAvatar> originalList) {
		List<List<GroupMemberAvatar>> listOfChunks = IntStream.range(0, originalList.size() / GroupMembersMessageComposer.MEMBERS_PER_PAGE).mapToObj(i -> originalList.subList(i * GroupMembersMessageComposer.MEMBERS_PER_PAGE, i * GroupMembersMessageComposer.MEMBERS_PER_PAGE + GroupMembersMessageComposer.MEMBERS_PER_PAGE)).collect(Collectors.toList());
		
		if (originalList.size() % GroupMembersMessageComposer.MEMBERS_PER_PAGE != 0) {
			listOfChunks.add(originalList.subList(originalList.size() - originalList.size() % GroupMembersMessageComposer.MEMBERS_PER_PAGE, originalList.size()));
		}
		
		return listOfChunks;
	}
	
}
