package com.cometproject.server.network.messages.incoming.user.camera;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.game.achievements.types.AchievementType;
import com.cometproject.api.game.players.data.components.inventory.IPlayerItem;
import com.cometproject.server.composers.camera.PurchasedPhotoMessageComposer;
import com.cometproject.server.composers.catalog.UnseenItemsMessageComposer;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.players.components.types.inventory.InventoryItem;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.outgoing.notification.NotificationMessageComposer;
import com.cometproject.server.network.messages.outgoing.user.inventory.UpdateInventoryMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;
import com.cometproject.storage.api.StorageContext;
import com.cometproject.storage.api.data.DataWrapper;
import com.google.common.collect.Sets;
import java.text.MessageFormat;

public class PurchasePhotoMessageEvent implements Event {
	
	@Override
	public void handle(Session client, MessageEvent msg) throws Exception {
		final String code = client.getPlayer().getLastPhoto();
		final long time = System.currentTimeMillis();
		final String photoUrl = CometSettings.CAMERA_PHOTO_URL.replace("%photoId%", code);
		final String itemExtraData = getExtraData(time, code, photoUrl);

		final DataWrapper<Long> itemIdData = DataWrapper.createEmpty();
		StorageContext.getCurrentContext().getRoomItemRepository().createItem(client.getPlayer().getId(), CometSettings.CAMERA_PHOTO_ITEM_ID, itemExtraData, itemIdData::set);
		
		final IPlayerItem playerItem = new InventoryItem(itemIdData.get(), CometSettings.CAMERA_PHOTO_ITEM_ID, itemExtraData);
		
		client.getPlayer().getInventory().addItem(playerItem);
		
		client.send(new NotificationMessageComposer("generic", Locale.getOrDefault("camera.photoTaken", "You successfully took a photo!")));
		client.send(new UpdateInventoryMessageComposer());
		
		client.send(new UnseenItemsMessageComposer(Sets.newHashSet(playerItem), ItemManager.getInstance()));
		client.send(new PurchasedPhotoMessageComposer());
		
		client.getPlayer().getAchievements().progressAchievement(AchievementType.CAMERA_PHOTO, 1);
		StorageContext.getCurrentContext().getPhotoRepository().savePhoto(client.getPlayer().getId(), client.getPlayer().getEntity().getRoom().getId(), photoUrl, (int) time / 1000);
	}
	
	private static String getExtraData(long time, String code, String photoUrl) {
		return MessageFormat.format("'{'\"t\":{0},\"u\":\"{1}\",\"n\":\"{2}\",\"m\":\"\",\"s\":{3},\"w\":\"{4}\"'}'", time, code, client.getPlayer().getData().getUsername(), client.getPlayer().getId(), photoUrl);
	}

}
