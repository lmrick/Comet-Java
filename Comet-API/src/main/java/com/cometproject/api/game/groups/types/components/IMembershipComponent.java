package com.cometproject.api.game.groups.types.components;

import com.cometproject.api.game.groups.types.IGroupComponent;
import com.cometproject.api.game.groups.types.GroupMemberAvatar;
import com.cometproject.api.game.groups.types.components.membership.IGroupMember;
import com.cometproject.api.networking.messages.IMessageComposer;
import com.cometproject.api.networking.sessions.ISessionService;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IMembershipComponent extends IGroupComponent {
    void broadcastMessage(ISessionService sessionService, IMessageComposer messageComposer, int sender);

    boolean hasMembership(final int playerId);

    Map<Integer, IGroupMember> getAll();

    List<IGroupMember> getMembersAsList();

    Set<Integer> getAdministrators();

    Set<Integer> getMembershipRequests();

    List<GroupMemberAvatar> getMemberAvatars();

    List<GroupMemberAvatar> getRequestAvatars();

    List<GroupMemberAvatar> getAdminAvatars();
}
