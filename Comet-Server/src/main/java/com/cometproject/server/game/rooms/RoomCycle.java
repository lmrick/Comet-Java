package com.cometproject.server.game.rooms;

import com.cometproject.server.game.rooms.types.RoomPromotion;
import com.cometproject.server.tasks.ICometTask;
import com.cometproject.server.tasks.CometThreadManager;
import com.cometproject.server.utilities.TimeSpan;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RoomCycle implements ICometTask {
	
	private final static int PERIOD = 500;
	private final static int FLAG = 2000;
	private static final Logger log = Logger.getLogger(RoomCycle.class.getName());
	private ScheduledFuture<?> myFuture;
	
	public RoomCycle() {
	}
	
	public void start() {
		this.myFuture = CometThreadManager.getInstance().executePeriodic(this, PERIOD, PERIOD, TimeUnit.MILLISECONDS);
	}
	
	public void stop() {
		this.myFuture.cancel(false);
	}
	
	public boolean isActive() {
		return (!this.myFuture.isCancelled());
	}
	
	@Override
	public void run() {
		try {
			long start = System.currentTimeMillis();
			
			// run this before ticking
			RoomManager.getInstance().unloadIdleRooms();
			
			List<Integer> expiredPromotedRooms = RoomManager.getInstance().getRoomPromotions().values().stream().filter(RoomPromotion::isExpired).map(RoomPromotion::getRoomId).collect(Collectors.toList());
			
			if (!expiredPromotedRooms.isEmpty()) {
				expiredPromotedRooms.forEach(roomId -> RoomManager.getInstance().getRoomPromotions().remove(roomId));
				
				expiredPromotedRooms.clear();
			}

            /*final Map<Integer, Integer> userCount = Maps.newHashMap();

            for(Room room : RoomManager.getInstance().getRoomInstances().values()) {
                final int playerCount = room.getEntities().playerCount();

                if(playerCount > 0) {
                    userCount.put(room.getId(), playerCount);
                }
            }

            RoomDao.saveUserCounts(userCount);
            userCount.clear();*/
			
			TimeSpan span = new TimeSpan(start, System.currentTimeMillis());
			
			if (span.toMilliseconds() > FLAG) {
				log.warn(MessageFormat.format("Global room processing ({0} rooms) took: {1}MS to execute.", RoomManager.getInstance().getRoomInstances().size(), span.toMilliseconds()));
			}
		} catch (Exception e) {
			log.error("Error while cycling rooms", e);
		}
	}
	
}
