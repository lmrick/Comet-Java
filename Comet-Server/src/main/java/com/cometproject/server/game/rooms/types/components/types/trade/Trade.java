package com.cometproject.server.game.rooms.types.components.types.trade;

import com.cometproject.api.game.players.data.components.inventory.IPlayerItem;
import com.cometproject.api.game.rooms.entities.RoomEntityStatus;
import com.cometproject.server.composers.catalog.UnseenItemsMessageComposer;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.rooms.objects.entities.types.PlayerEntity;
import com.cometproject.server.game.rooms.types.components.types.TradeComponent;
import com.cometproject.server.network.messages.outgoing.notification.AlertMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.trading.*;
import com.cometproject.server.network.messages.outgoing.user.inventory.UpdateInventoryMessageComposer;
import com.cometproject.server.protocol.messages.MessageComposer;
import com.cometproject.server.storage.queries.items.TradeDao;
import com.cometproject.server.tasks.CometThreadManager;
import com.cometproject.server.utilities.collections.ConcurrentHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Trade {
	private final PlayerEntity firstPlayer;
	private final PlayerEntity secondPlayer;
	private int tradeStage = 1;
	private final Set<IPlayerItem> firstPlayerItems;
	private final Set<IPlayerItem> secondPlayerItems;
	private boolean firstPlayerAccepted = false;
	private boolean secondPlayerAccepted = false;
	private TradeComponent tradeComponent;
	
	public Trade(PlayerEntity firstPlayer, PlayerEntity secondPlayer) {
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
		
		firstPlayerItems = new ConcurrentHashSet<>();
		secondPlayerItems = new ConcurrentHashSet<>();
		
		if (!firstPlayer.hasStatus(RoomEntityStatus.TRADE)) {
			firstPlayer.addStatus(RoomEntityStatus.TRADE, "");
			firstPlayer.markNeedsUpdate();
		}
		
		if (!secondPlayer.getPlayer().getEntity().hasStatus(RoomEntityStatus.TRADE)) {
			secondPlayer.addStatus(RoomEntityStatus.TRADE, "");
			secondPlayer.markNeedsUpdate();
		}
		
		firstPlayer.getPlayer().getSession().getPlayer().getInventory().send();
		secondPlayer.getPlayer().getSession().getPlayer().getInventory().send();
		
		sendToPLayers(new TradeStartMessageComposer(firstPlayer.getPlayer().getId(), secondPlayer.getPlayer().getId()));
	}
	
	public void cancel(int playerId) {
		this.cancel(playerId, true);
	}
	
	public void cancel(int playerId, boolean isLeave) {
		this.firstPlayerItems.clear();
		this.secondPlayerItems.clear();
		
		boolean sendToFirstPlayer = true;
		boolean sendToSecondPlayer = true;
		
		if (isLeave) {
			if (firstPlayer.getPlayer() == null || playerId == firstPlayer.getPlayer().getId()) {
				sendToFirstPlayer = false;
			} else {
				sendToSecondPlayer = false;
			}
		}
		
		if (firstPlayer != null && firstPlayer.getPlayer() != null && sendToFirstPlayer) {
			firstPlayer.removeStatus(RoomEntityStatus.TRADE);
			firstPlayer.markNeedsUpdate();
		}
		
		if (secondPlayer != null && secondPlayer.getPlayer() != null && sendToSecondPlayer) {
			secondPlayer.removeStatus(RoomEntityStatus.TRADE);
			secondPlayer.markNeedsUpdate();
		}
		
		this.sendToPLayers(new TradeCloseMessageComposer(playerId));
		this.tradeComponent.remove(this);
	}
	
	public void addItem(int playerIndex, IPlayerItem item, boolean update) {
		if (this.firstPlayerAccepted || this.secondPlayerAccepted) {
			return;
		}
		
		if (playerIndex == 1) {
			this.firstPlayerItems.add(item);
		} else {
			this.secondPlayerItems.add(item);
		}
		
		if (firstPlayer != null && firstPlayer.getPlayer() != null) {
			this.sendToPLayers(new TradeAcceptUpdateMessageComposer(firstPlayer.getPlayer().getId(), false));
			this.firstPlayerAccepted = false;
		}
		
		if (secondPlayer != null && secondPlayer.getPlayer() != null) {
			this.sendToPLayers(new TradeAcceptUpdateMessageComposer(secondPlayer.getPlayer().getId(), false));
			this.secondPlayerAccepted = false;
		}
		
		if (this.tradeStage == 2) this.tradeStage = 1;
		
		if (update) {
			this.updateWindow();
		}
	}
	
	public boolean isOffered(IPlayerItem item) {
		return this.firstPlayerItems.contains(item) || this.secondPlayerItems.contains(item);
	}
	
	public int getPlayerIndex(PlayerEntity playerEntity) {
		return (firstPlayer == playerEntity) ? 1 : 0;
	}
	
	public void removeItem(int playerIndex, IPlayerItem item) {
		if (this.firstPlayerAccepted || this.secondPlayerAccepted) {
			return;
		}
		
		if (playerIndex == 1) {
			this.firstPlayerItems.remove(item);
		} else {
			this.secondPlayerItems.remove(item);
		}
		
		this.updateWindow();
	}
	
	public void accept(int playerIndex) {
		if (playerIndex == 1) this.firstPlayerAccepted = true;
		else this.secondPlayerAccepted = true;
		
		this.sendToPLayers(new TradeAcceptUpdateMessageComposer(((playerIndex == 1) ? firstPlayer : secondPlayer).getPlayer().getId(), true));
		
		if (firstPlayerAccepted && secondPlayerAccepted) {
			this.tradeStage++;
			this.sendToPLayers(new TradeConfirmationMessageComposer());
			this.firstPlayerAccepted = false;
			this.secondPlayerAccepted = false;
		}
	}
	
	public void unAccept(int playerIndex) {
		if (this.firstPlayerAccepted && secondPlayerAccepted) {
			this.tradeStage--;
		}
		
		if (playerIndex == 1) this.firstPlayerAccepted = false;
		else this.secondPlayerAccepted = false;
		
		this.sendToPLayers(new TradeAcceptUpdateMessageComposer(((playerIndex == 1) ? firstPlayer : secondPlayer).getPlayer().getId(), false));
	}
	
	public void confirm(int playerIndex) {
		if (tradeStage < 2) {
			return;
		}
		
		if (playerIndex == 1) this.firstPlayerAccepted = true;
		else this.secondPlayerAccepted = true;
		
		sendToPLayers(new TradeAcceptUpdateMessageComposer(((playerIndex == 1) ? firstPlayer : secondPlayer).getPlayer().getId(), true));
		
		if (firstPlayerAccepted && secondPlayerAccepted) {
			complete();
			
			this.firstPlayerItems.clear();
			this.secondPlayerItems.clear();
			
			if (firstPlayer.getPlayer().getEntity().hasStatus(RoomEntityStatus.TRADE)) {
				firstPlayer.getPlayer().getEntity().removeStatus(RoomEntityStatus.TRADE);
				firstPlayer.getPlayer().getEntity().markNeedsUpdate();
			}
			
			if (secondPlayer.getPlayer().getEntity().hasStatus(RoomEntityStatus.TRADE)) {
				secondPlayer.getPlayer().getEntity().removeStatus(RoomEntityStatus.TRADE);
				secondPlayer.getPlayer().getEntity().markNeedsUpdate();
			}
		}
	}
	
	public void complete() {
		for (IPlayerItem item : this.firstPlayerItems) {
			if (firstPlayer.getPlayer().getInventory().getItem(item.getId()) == null) {
				sendToPLayers(new AlertMessageComposer(Locale.get("game.trade.error")));
				return;
			}
		}
		
		for (IPlayerItem item : this.secondPlayerItems) {
			if (secondPlayer.getPlayer().getInventory().getItem(item.getId()) == null) {
				sendToPLayers(new AlertMessageComposer(Locale.get("game.trade.error")));
				return;
			}
		}
		
		final Map<Long, Integer> itemsToSave = new HashMap<>();
		
		this.firstPlayerItems.forEach(item -> {
			firstPlayer.getPlayer().getInventory().removeItem(item);
			secondPlayer.getPlayer().getInventory().addItem(item);
			itemsToSave.put(item.getId(), secondPlayer.getPlayer().getId());
		});
		
		this.secondPlayerItems.forEach(item -> {
			secondPlayer.getPlayer().getInventory().removeItem(item);
			firstPlayer.getPlayer().getInventory().addItem(item);
			itemsToSave.put(item.getId(), firstPlayer.getPlayer().getId());
		});
		
		CometThreadManager.getInstance().executeOnce(() -> {
			TradeDao.updateTradeItems(itemsToSave);
			itemsToSave.clear();
		});
		
		firstPlayer.getPlayer().getSession().send(new UnseenItemsMessageComposer(secondPlayerItems, ItemManager.getInstance()));
		secondPlayer.getPlayer().getSession().send(new UnseenItemsMessageComposer(firstPlayerItems, ItemManager.getInstance()));
		
		sendToPLayers(new UpdateInventoryMessageComposer());
		sendToPLayers(new TradeCompletedMessageComposer());
		
		this.tradeComponent.remove(this);
	}
	
	public void updateWindow() {
		this.sendToPLayers(new TradeUpdateMessageComposer(this.firstPlayer.getPlayerId(), this.secondPlayer.getPlayerId(), this.firstPlayerItems, this.secondPlayerItems));
	}
	
	public void sendToPLayers(MessageComposer msg) {
		if (firstPlayer != null && firstPlayer.getPlayer() != null && firstPlayer.getPlayer().getSession() != null) {
			firstPlayer.getPlayer().getSession().send(msg);
		}
		
		if (secondPlayer != null && secondPlayer.getPlayer() != null && secondPlayer.getPlayer().getSession() != null) {
			secondPlayer.getPlayer().getSession().send(msg);
		}
	}
	
	public PlayerEntity getFirstPlayer() {
		return this.firstPlayer;
	}
	
	public PlayerEntity getSecondPlayer() {
		return this.secondPlayer;
	}
	
	public TradeComponent getTradeComponent() {
		return tradeComponent;
	}
	
	public void setTradeComponent(TradeComponent tradeComponent) {
		this.tradeComponent = tradeComponent;
	}
	
}
