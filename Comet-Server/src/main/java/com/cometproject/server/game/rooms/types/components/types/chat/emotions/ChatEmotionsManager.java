package com.cometproject.server.game.rooms.types.components.types.chat.emotions;

import com.cometproject.server.game.rooms.RoomManager;
import java.util.HashMap;
import java.util.Map;

public class ChatEmotionsManager {
	
	private final Map<String, ChatEmotion> emotions;
	
	public ChatEmotionsManager() {
		emotions = new HashMap<>() {{
			put(":)", ChatEmotion.SMILE);
			put(";)", ChatEmotion.SMILE);
			put(":]", ChatEmotion.SMILE);
			put(";]", ChatEmotion.SMILE);
			put("=)", ChatEmotion.SMILE);
			put("=]", ChatEmotion.SMILE);
			put(":-)", ChatEmotion.SMILE);
			put(">:(", ChatEmotion.ANGRY);
			put(">:[", ChatEmotion.ANGRY);
			put(">;[", ChatEmotion.ANGRY);
			put(">;(", ChatEmotion.ANGRY);
			put(">=(", ChatEmotion.ANGRY);
			put(":o", ChatEmotion.SHOCKED);
			put(";o", ChatEmotion.SHOCKED);
			put(">;o", ChatEmotion.SHOCKED);
			put(">:o", ChatEmotion.SHOCKED);
			put(">=o", ChatEmotion.SHOCKED);
			put("=o", ChatEmotion.SHOCKED);
			put(";'(", ChatEmotion.SAD);
			put(";[", ChatEmotion.SAD);
			put(":[", ChatEmotion.SAD);
			put(";(", ChatEmotion.SAD);
			put("=(", ChatEmotion.SAD);
			put("='(", ChatEmotion.SAD);
			put(":(", ChatEmotion.SAD);
			put(":-(", ChatEmotion.SAD);
			put(";D", ChatEmotion.LAUGH);
			put(":D", ChatEmotion.LAUGH);
			put(":L", ChatEmotion.LAUGH);
			put("leon", ChatEmotion.SMILE);
			put("alex", ChatEmotion.SMILE);
			put("comet", ChatEmotion.SMILE);
			put("java", ChatEmotion.SMILE);
			put("meesha", ChatEmotion.SMILE);
			put("luna", ChatEmotion.SMILE);
			put("luck", ChatEmotion.SMILE);
			put("phoenix", ChatEmotion.SAD);
			put("butterfly", ChatEmotion.SAD);
			put("matou19", ChatEmotion.ANGRY);
			put("mathis", ChatEmotion.ANGRY);
			put("helpi", ChatEmotion.ANGRY);
			put("gladius", ChatEmotion.ANGRY);
			put("minette", ChatEmotion.SHOCKED);
		}};
		
		RoomManager.log.info("Loaded " + this.emotions.size() + " chat emotions");
	}
	
	public ChatEmotion getEmotion(String message) {
		return emotions.entrySet().stream().filter(emotion -> message.toLowerCase().contains(emotion.getKey().toLowerCase())).findFirst().map(Map.Entry::getValue).orElse(ChatEmotion.NONE);
	}
	
}
