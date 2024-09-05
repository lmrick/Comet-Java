package com.cometproject.server.network.messages.incoming.user.camera;

import com.cometproject.api.config.CometSettings;
import com.cometproject.server.api.ApiClient;
import com.cometproject.server.composers.camera.PhotoPreviewMessageComposer;
import com.cometproject.server.game.players.types.Player;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.protocol.messages.MessageEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import org.joda.time.DateTime;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.UUID;

public class RenderRoomMessageEvent implements Event {
	
	private static final byte[] SIGNATURE = new byte[] { -119, 80, 78, 71, 13, 10, 26, 10 };
	
	@Override
	public void handle(Session client, MessageEvent msg) throws Exception {
		final int length = msg.readInt();
		final byte[] payload = msg.readBytes(length);
		
		final String photoID = UUID.randomUUID().toString();
		final String URL = MessageFormat.format("{0}_{1}.png", client.getPlayer().getData().getId(), photoID);
		final String URL_SMALL = MessageFormat.format("{0}_{1}_small.png", client.getPlayer().getData().getId(), photoID);
		final String base = CometSettings.cameraPhotoUrl.replace("%photoId%", "");
		client.getPlayer().setLastPhoto(URL);
		
		if (RenderRoomMessageEvent.isPngFile(payload)) {
			try {
				ByteBuf buf = Unpooled.copiedBuffer(payload);
				BufferedImage image = ImageIO.read(new ByteBufInputStream(buf));
				ImageIO.write(image, "png", new File(CometSettings.cameraUploadUrl + URL));
				ImageIO.write(image, "png", new File(CometSettings.cameraUploadUrl + URL_SMALL));
			} catch (IOException e) {
				e.printStackTrace();
				client.getPlayer().sendNotification("Camera", "Failed to upload image");
			}
			
			client.getPlayer().setLastPhoto(photoID);
			client.getPlayer().setLastPhotoTaken(DateTime.now().getMillis());
			client.send(new PhotoPreviewMessageComposer(URL));
		}
		
	}
	
	private static boolean isPngFile(byte[] file) {
		return Arrays.equals(Arrays.copyOfRange(file, 0, 8), SIGNATURE);
	}
	
}
