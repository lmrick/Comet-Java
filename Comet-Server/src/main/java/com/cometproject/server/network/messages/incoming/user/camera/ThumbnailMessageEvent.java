package com.cometproject.server.network.messages.incoming.user.camera;

import com.cometproject.api.config.CometSettings;
import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.rooms.IRoomData;
import com.cometproject.server.api.ApiClient;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.messages.outgoing.room.engine.RoomDataMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.settings.ThumbnailTakenMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.UUID;

public class ThumbnailMessageEvent implements Event {
	
	private static final byte[] SIGNATURE = new byte[] { -119, 80, 78, 71, 13, 10, 26, 10 };
	
	@Override
	public void handle(Session client, MessageEvent msg) throws Exception {
		final int length = msg.readInt();
		final byte[] payload = msg.readBytes(length);
		
		final Room room = client.getPlayer().getEntity().getRoom();
		final IRoomData roomData = room.getData();
		
		if (!room.getRights().hasRights(client.getPlayer().getId(), true) && !client.getPlayer().getPermissions().getRank().roomFullControl()) {
			return;
		}
		
		if (ThumbnailMessageEvent.isPngFile(payload)) {
			try {
				ByteBuf test = Unpooled.copiedBuffer(payload);
				BufferedImage image = ImageIO.read(new ByteBufInputStream(test));
				ImageIO.write(image, "png", new File(MessageFormat.format("{0}{1}.png", CometSettings.CAMERA_UPLOAD_URL, roomData.getId())));
			} catch (IOException e) {
				e.printStackTrace();
				client.getPlayer().sendNotification("Camera", "Failed to upload image");
				return;
			}
			
			roomData.setThumbnail(MessageFormat.format("{0}/{1}.png", CometSettings.CAMERA_UPLOAD_URL, roomData.getId()));
			GameContext.getCurrent().getRoomService().saveRoomData(roomData);
			client.send(new RoomDataMessageComposer(client.getPlayer().getEntity().getRoom()));
			client.send(new ThumbnailTakenMessageComposer());
		}
	}
	
	private static boolean isPngFile(byte[] file) {
		return Arrays.equals(Arrays.copyOfRange(file, 0, 8), SIGNATURE);
	}
	
}
