package com.cometproject.server.game.moderation;

import com.cometproject.api.utilities.Initializable;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.moderation.types.Ban;
import com.cometproject.server.game.moderation.types.BanType;
import com.cometproject.server.storage.queries.moderation.BanDao;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BanManager implements Initializable {
	private static BanManager banManagerInstance;
	private static final Logger log = Logger.getLogger(BanManager.class.getName());
	private Map<String, Ban> bans;
	
	public BanManager() {
	
	}
	
	public static BanManager getInstance() {
		if (banManagerInstance == null) banManagerInstance = new BanManager();
		return banManagerInstance;
	}
	
	@Override
	public void initialize() {
		loadBans();
		log.info("BanManager initialized");
	}
	
	public void loadBans() {
		if (this.bans != null) this.bans.clear();
		
		try {
			this.bans = BanDao.getActiveBans();
			log.info("Loaded " + this.bans.size() + " bans");
		} catch (Exception e) {
			log.error("Error while loading bans", e);
		}
	}
	
	public void processBans() {
		List<Ban> bansToRemove = this.bans.values().stream().filter(ban -> ban.getExpire() != 0 && Comet.getTime() >= ban.getExpire()).collect(Collectors.toList());
		
		if (!bansToRemove.isEmpty()) {
			bansToRemove.forEach(ban -> this.bans.remove(ban.getData()));
		}
		
		bansToRemove.clear();
	}
	
	public void banPlayer(BanType type, String data, int length, long expire, String reason, int bannerId) {
		int banId = BanDao.createBan(type, length, expire, data, bannerId, reason);
		this.add(new Ban(banId, data, length == 0 ? length : expire, type, reason));
	}
	
	private void add(Ban ban) {
		this.bans.put(ban.getData(), ban);
	}
	
	public boolean hasBan(String data, BanType type) {
		if (this.bans.containsKey(data)) {
			Ban ban = this.bans.get(data);
			
			if (ban != null && ban.getType() == type) {
				return ban.getExpire() == 0 || Comet.getTime() < ban.getExpire();
			}
		}
		
		return false;
	}
	
	public boolean unBan(String data) {
		if (!data.equals("0")) {
			if (this.bans.containsKey(data)) {
				this.bans.remove(data);
				removeBan(data);
				
				initialize();
				return true;
			} else return false;
		} else return false;
	}
	
	private void removeBan(String data) {
		BanDao.deleteBan(data);
	}
	
	public Ban get(String data) {
		return this.bans.get(data);
	}
	
}
