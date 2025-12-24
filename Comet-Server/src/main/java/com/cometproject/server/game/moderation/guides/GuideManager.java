package com.cometproject.server.game.moderation.guides;

import com.cometproject.api.utilities.process.Initializable;
import com.cometproject.server.game.moderation.guides.types.HelpRequest;
import com.cometproject.server.game.moderation.guides.types.HelperSession;
import com.cometproject.server.network.messages.outgoing.help.guides.GuideSessionAttachedMessageComposer;
import com.cometproject.server.tasks.CometConstants;
import com.cometproject.server.tasks.CometThreadManager;
import com.cometproject.server.utilities.collections.ConcurrentHashSet;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class GuideManager implements Initializable {
	
	private static GuideManager guideManagerInstance;
	private final Map<Integer, HelperSession> sessions = new ConcurrentHashMap<>();
	private final Map<Integer, Boolean> activeGuides = new ConcurrentHashMap<>();
	private final Set<Integer> activeGuardians = new ConcurrentHashSet<>();
	private final Map<Integer, HelpRequest> activeHelpRequests = new ConcurrentHashMap<>();
	
	public static GuideManager getInstance() {
		if (guideManagerInstance == null) guideManagerInstance = new GuideManager();
		return guideManagerInstance;
	}
	
	@Override
	public void initialize() {
		CometThreadManager.getInstance().executePeriodic(this::processRequests, CometConstants.GUIDE_HELP_REQUEST_DELAY, CometConstants.GUIDE_HELP_REQUEST_DELAY, TimeUnit.MILLISECONDS);
	}
	
	private void processRequests() {
		
		this.activeHelpRequests.values().stream().filter(helpRequest -> !helpRequest.hasGuide()).forEachOrdered(helpRequest -> {
			if (helpRequest.getProcessTicks() >= 60) {
				
				for (Map.Entry<Integer, Boolean> activeGuide : activeGuides.entrySet()) {
					if (!activeGuide.getValue()) {
						if (!helpRequest.declined(activeGuide.getKey())) {
							helpRequest.setGuide(activeGuide.getKey());
							
							helpRequest.getPlayerSession().send(new GuideSessionAttachedMessageComposer(helpRequest, false));
							helpRequest.getGuideSession().send(new GuideSessionAttachedMessageComposer(helpRequest, true));
							break;
						}
					}
				}
				
				if (helpRequest.hasGuide()) {
					this.activeGuides.put(helpRequest.guideId, true);
				}
				
				helpRequest.resetProcessTicks();
			} else {
				helpRequest.incrementProcessTicks();
			}
		});
	}
	
	public void startPlayerDuty(final HelperSession helperSession) {
		this.sessions.put(helperSession.getPlayerId(), helperSession);
		
		if (helperSession.handlesHelpRequests()) {
			this.activeGuides.put(helperSession.getPlayerId(), false);
		}
		
		if (helperSession.handlesBullyReports()) {
			this.activeGuardians.add(helperSession.getPlayerId());
		}
	}
	
	public void finishPlayerDuty(final HelperSession helperSession) {
		
		this.sessions.remove(helperSession.getPlayerId());
		
		if (helperSession.handlesHelpRequests()) {
			this.activeGuides.remove(helperSession.getPlayerId());
		}
		
		if (helperSession.handlesBullyReports()) {
			this.activeGuardians.remove(helperSession.getPlayerId());
		}
	}
	
	public void requestHelp(final HelpRequest helpRequest) {
		this.activeHelpRequests.put(helpRequest.getPlayerId(), helpRequest);
	}
	
	public HelpRequest getHelpRequestByCreator(final int playerId) {
		return this.activeHelpRequests.get(playerId);
	}
	
	public int getActiveGuideCount() {
		return this.activeGuides.size();
	}
	
	public int getActiveGuardianCount() {
		return this.activeGuardians.size();
	}
	
}
