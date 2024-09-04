package com.cometproject.server.game.players.components.types;

import com.cometproject.api.game.players.components.PlayerComponentContext;
import com.cometproject.api.game.players.data.components.IPlayerSubscription;
import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.players.components.PlayerComponent;

public class SubscriptionComponent extends PlayerComponent implements IPlayerSubscription {
	private boolean hasSub;
	private int expire;
	
	public SubscriptionComponent(PlayerComponentContext componentContext) {
		super(componentContext);
		
		this.load();
	}
	
	@Override
	public void load() {
		this.hasSub = true;
		this.expire = (int) Comet.getTime() + 315569260;
		
	}
	
	@Override
	public void add(int days) {
	}
	@Override
	public void delete() {
		this.hasSub = false;
		this.expire = 0;
	}
	@Override
	public boolean isValid() {
		return this.getExpire() > Comet.getTime();
	}
	@Override
	public boolean exists() {
		return this.hasSub;
	}
	@Override
	public int getExpire() {
		return this.expire;
	}
	
}
