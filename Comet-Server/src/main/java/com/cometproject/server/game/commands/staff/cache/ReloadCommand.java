package com.cometproject.server.game.commands.staff.cache;

import com.cometproject.api.game.GameContext;
import com.cometproject.api.game.achievements.IAchievementsService;
import com.cometproject.server.composers.catalog.CatalogPublishMessageComposer;
import com.cometproject.server.locale.Locale;
import com.cometproject.server.game.catalog.CatalogManager;
import com.cometproject.server.game.commands.ChatCommand;
import com.cometproject.server.game.commands.CommandManager;
import com.cometproject.server.game.items.ItemManager;
import com.cometproject.server.game.landing.LandingManager;
import com.cometproject.server.game.moderation.BanManager;
import com.cometproject.server.game.moderation.ModerationManager;
import com.cometproject.server.game.navigator.NavigatorManager;
import com.cometproject.server.game.permissions.PermissionsManager;
import com.cometproject.server.game.pets.PetManager;
import com.cometproject.server.game.pets.commands.PetCommandManager;
import com.cometproject.server.game.polls.PollManager;
import com.cometproject.server.game.polls.types.Poll;
import com.cometproject.server.game.quests.QuestManager;
import com.cometproject.server.game.rooms.RoomManager;
import com.cometproject.server.game.rooms.bundles.RoomBundleManager;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.outgoing.moderation.ModToolMessageComposer;
import com.cometproject.server.network.messages.outgoing.notification.MotdNotificationMessageComposer;
import com.cometproject.server.network.messages.outgoing.room.polls.InitializePollMessageComposer;
import com.cometproject.server.network.sessions.Session;
import com.cometproject.server.storage.queries.config.ConfigDao;


public class ReloadCommand extends ChatCommand {

    @Override
    public void execute(Session client, String[] params) {
        String command = params.length == 0 ? "" : params[0];
			
			switch (command) {
				case "bans" -> {
					BanManager.getInstance().loadBans();
					
					sendNotification(Locale.get("command.reload.bans"), client);
				}
				case "catalog" -> {
					CatalogManager.getInstance().loadItemsAndPages();
					CatalogManager.getInstance().loadGiftBoxes();
					
					NetworkManager.getInstance().getSessions().broadcast(new CatalogPublishMessageComposer(true));
					sendNotification(Locale.get("command.reload.catalog"), client);
				}
				case "navigator" -> {
					NavigatorManager.getInstance().loadCategories();
					NavigatorManager.getInstance().loadPublicRooms();
					NavigatorManager.getInstance().loadStaffPicks();
					
					sendNotification(Locale.get("command.reload.navigator"), client);
				}
				case "permissions" -> {
					PermissionsManager.getInstance().loadRankPermissions();
					PermissionsManager.getInstance().loadPerks();
					PermissionsManager.getInstance().loadCommands();
					PermissionsManager.getInstance().loadOverrideCommands();
					PermissionsManager.getInstance().getEffects();
					
					sendNotification(Locale.get("command.reload.permissions"), client);
				}
				case "config" -> {
					ConfigDao.getAll();
					
					sendNotification(Locale.get("command.reload.config"), client);
				}
				case "news" -> {
					LandingManager.getInstance().loadArticles();
					
					sendNotification(Locale.get("command.reload.news"), client);
				}
				case "items" -> {
					ItemManager.getInstance().loadItemDefinitions();
					
					sendNotification(Locale.get("command.reload.items"), client);
				}
				case "filter" -> {
					RoomManager.getInstance().getFilter().loadFilter();
					
					sendNotification(Locale.get("command.reload.filter"), client);
				}
				case "locale" -> {
					Locale.reload();
					CommandManager.getInstance().reloadAllCommands();
					
					sendNotification(Locale.get("command.reload.locale"), client);
				}
				case "modpresets" -> {
					ModerationManager.getInstance().loadPresets();
					
					sendNotification(Locale.get("command.reload.modpresets"), client);
					
					ModerationManager.getInstance().getModerators().forEach((session -> {
						session.send(new ModToolMessageComposer());
					}));
				}
				case "groupitems" -> {
					GameContext.getCurrent().getGroupService().getItemService().load();
					sendNotification(Locale.get("command.reload.groupitems"), client);
				}
				case "models" -> {
					GameContext.getCurrent().getRoomModelService().loadModels();
					
					sendNotification(Locale.get("command.reload.models"), client);
				}
				case "music" -> {
					ItemManager.getInstance().loadMusicData();
					sendNotification(Locale.get("command.reload.music"), client);
				}
				case "quests" -> {
					QuestManager.getInstance().loadQuests();
					sendNotification(Locale.get("command.reload.quests"), client);
				}
				case "achievements" -> {
					GameContext.getCurrent().getService(IAchievementsService.class).loadAchievements();					
					sendNotification(Locale.get("command.reload.achievements"), client);
				}
				case "pets" -> {
					PetManager.getInstance().loadPetRaces();
					PetManager.getInstance().loadPetSpeech();
					PetManager.getInstance().loadTransformablePets();
					PetManager.getInstance().loadPetBreedPallets();
					
					PetCommandManager.getInstance().initialize();
					
					sendNotification(Locale.get("command.reload.pets"), client);
				}
				case "polls" -> {
					PollManager.getInstance().initialize();
					
					if (PollManager.getInstance().roomHasPoll(client.getPlayer().getEntity().getRoom().getId())) {
						Poll poll = PollManager.getInstance().getPollByRoomId(client.getPlayer().getEntity().getRoom().getId());
						
						client.getPlayer().getEntity().getRoom().getEntities().broadcastMessage(new InitializePollMessageComposer(poll.getPollId(), poll.getPollTitle(), poll.getThanksMessage()));
					}
					
					sendNotification(Locale.get("command.reload.polls"), client);
				}
				case "bundles" -> {
					RoomBundleManager.getInstance().initialize();
					
					sendNotification(Locale.get("command.reload.bundles"), client);
					
				}
				default -> client.send(new MotdNotificationMessageComposer("Here's a list of what you can reload using the :reload <type> command!\n\n- bans\n- catalog\n- navigator\n- permissions\n- rooms\n- catalog\n- news\n- config\n- items\n- filter\n- locale\n- modpresets\n- groupitems\n- models\n- music\n- quests\n- achievements\n- pets\n- polls\n- bundles"));
			}
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public String getPermission() {
        return "reload_command";
    }

    @Override
    public String getParameter() {
        return "";
    }

    @Override
    public String getDescription() {
        return Locale.get("command.reload.description");
    }
}
