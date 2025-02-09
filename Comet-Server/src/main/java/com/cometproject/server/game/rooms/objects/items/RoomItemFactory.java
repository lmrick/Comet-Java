package com.cometproject.server.game.rooms.objects.items;

import com.cometproject.api.game.furniture.types.IFurnitureDefinition;
import com.cometproject.api.game.furniture.types.GiftData;
import com.cometproject.api.game.furniture.types.LegacyGiftData;
import com.cometproject.api.game.rooms.objects.data.LimitedEditionItemData;
import com.cometproject.api.game.rooms.objects.data.RoomItemData;
import com.cometproject.server.game.rooms.objects.items.types.DefaultFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.DefaultWallItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.*;
import com.cometproject.server.game.rooms.objects.items.types.floor.boutique.MannequinFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.games.football.*;
import com.cometproject.server.game.rooms.objects.items.types.floor.games.banzai.*;
import com.cometproject.server.game.rooms.objects.items.types.floor.games.freeze.*;
import com.cometproject.server.game.rooms.objects.items.types.floor.groups.GroupFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.groups.GroupGateFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.hollywood.HaloTileFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.pet.PetFoodFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.pet.PetNestFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.pet.PetToyFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.pet.breeding.types.*;
import com.cometproject.server.game.rooms.objects.items.types.floor.pet.eggs.PterosaurEggFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.pet.eggs.VelociraptorEggFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.pet.horse.HorseJumpFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.snowboarding.SnowboardJumpFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.snowboarding.SnowboardSlopeFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.summer.SummerShowerFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.totem.TotemBodyFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.totem.TotemHeadFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.totem.TotemPlanetFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions.*;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.actions.custom.*;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.addons.*;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.conditions.negative.*;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.conditions.negative.custom.*;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.conditions.positive.*;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.conditions.positive.custom.*;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.highscore.HighScoreClassicFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.highscore.HighScoreMostWinsFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.highscore.HighScorePerTeamFloorItem;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.triggers.*;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.triggers.custom.WiredTriggerCustomIdle;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.triggers.custom.WiredTriggerLeavesRoom;
import com.cometproject.server.game.rooms.objects.items.types.floor.wired.triggers.custom.WiredTriggerUsersCollide;
import com.cometproject.server.game.rooms.objects.items.types.wall.MoodLightWallItem;
import com.cometproject.server.game.rooms.objects.items.types.wall.PostItWallItem;
import com.cometproject.server.game.rooms.objects.items.types.wall.WheelWallItem;
import com.cometproject.server.game.rooms.types.Room;
import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoomItemFactory {
	
	public static final String STACK_TOOL = "tile_stackmagic";
	public static final String TELEPORT_PAD = "teleport_pad";
	
	private static final int processMs = 500;
	
	private static final Logger log = Logger.getLogger(RoomItemFactory.class.getName());
	
	private static final Map<String, Class<? extends RoomItemFloor>> itemDefinitionMap;
	private static final Map<String, Constructor<? extends RoomItemFloor>> itemConstructorCache;
	
	static {
		itemConstructorCache = new ConcurrentHashMap<>();
		
		itemDefinitionMap = new HashMap<>() {{
			put("roller", RollerFloorItem.class);
			put("dice", DiceFloorItem.class);
			put("teleport", TeleporterFloorItem.class);
			put("teleport_door", TeleporterFloorItem.class);
			put("teleport_pad", TeleportPadFloorItem.class);
			put("onewaygate", OneWayGateFloorItem.class);
			put("gate", GateFloorItem.class);
			put("roombg", BackgroundTonerFloorItem.class);
			put("bed", BedFloorItem.class);
			put("vendingmachine", VendingMachineFloorItem.class);
			put("mannequin", MannequinFloorItem.class);
			put("beach_shower", SummerShowerFloorItem.class);
			put("halo_tile", HaloTileFloorItem.class);
			put("adjustable_height_seat", AdjustableHeightSeatFloorItem.class);
			put("adjustable_height", AdjustableHeightFloorItem.class);
			put("lovelock", LoveLockFloorItem.class);
			put("soundmachine", SoundMachineFloorItem.class);
			put("privatechat", PrivateChatFloorItem.class);
			put("badge_display", BadgeDisplayFloorItem.class);
			put("piano", PianoFloorItem.class);
			
			put("wf_act_flee", WiredActionFlee.class);
			put("wf_act_match_to_sshot", WiredActionMatchToSnapshot.class);
			put("wf_act_teleport_to", WiredActionTeleportPlayer.class);
			put("wf_act_show_message", WiredActionShowMessage.class);
			put("wf_act_toggle_state", WiredActionToggleState.class);
			put("wf_act_give_reward", WiredActionGiveReward.class);
			put("wf_act_move_rotate", WiredActionMoveRotate.class);
			put("wf_act_chase", WiredActionChase.class);
			put("wf_act_kick_user", WiredActionKickUser.class);
			put("wf_act_reset_timers", WiredActionResetTimers.class);
			put("wf_act_join_team", WiredActionJoinTeam.class);
			put("wf_act_leave_team", WiredActionLeaveTeam.class);
			put("wf_act_give_score", WiredActionGiveScore.class);
			put("wf_act_bot_talk", WiredActionBotTalk.class);
			put("wf_act_bot_give_handitem", WiredActionBotGiveHandItem.class);
			put("wf_act_bot_move", WiredActionBotMove.class);
			put("wf_act_comet", WiredActionComet.class);
			put("wf_act_move_to_dir", WiredActionMoveToDirection.class);
			put("wf_act_bot_talk_to_avatar", WiredActionBotTalkToAvatar.class);
			put("wf_act_bot_clothes", WiredActionBotClothes.class);
			put("wf_act_bot_follow_avatar", WiredActionBotFollowAvatar.class);
			put("wf_act_call_stacks", WiredActionExecuteStacks.class);
			put("wf_act_bot_teleport", WiredActionBotTeleport.class);
			put("wf_act_give_score_tm", WiredActionGiveTeamScore.class);
			put("wf_xtra_random", WiredActionRandomEffect.class);
			
			put("wf_trg_says_something", WiredTriggerPlayerSaysKeyword.class);
			put("wf_trg_enter_room", WiredTriggerEnterRoom.class);
			put("wf_trg_periodically", WiredTriggerPeriodically.class);
			put("wf_trg_walks_off_furni", WiredTriggerWalksOffFurni.class);
			put("wf_trg_walks_on_furni", WiredTriggerWalksOnFurni.class);
			put("wf_trg_state_changed", WiredTriggerStateChanged.class);
			put("wf_trg_game_starts", WiredTriggerGameStarts.class);
			put("wf_trg_game_ends", WiredTriggerGameEnds.class);
			put("wf_trg_collision", WiredTriggerCollision.class);
			put("wf_trg_period_long", WiredTriggerPeriodicallyLong.class);
			put("wf_trg_at_given_time", WiredTriggerAtGivenTime.class);
			put("wf_trg_at_given_time_long", WiredTriggerAtGivenTimeLong.class);
			put("wf_trg_score_achieved", WiredTriggerScoreAchieved.class);
			put("wf_trg_bot_reached_avtr", WiredTriggerBotReachedAvatar.class);
			
			put("wf_cnd_trggrer_on_frn", WiredConditionTriggererOnFurni.class);
			put("wf_cnd_not_trggrer_on", WiredNegativeConditionTriggererOnFurni.class);
			put("wf_cnd_actor_in_group", WiredConditionPlayerInGroup.class);
			put("wf_cnd_not_in_group", WiredNegativeConditionPlayerInGroup.class);
			put("wf_cnd_furnis_hv_avtrs", WiredConditionFurniHasPlayers.class);
			put("wf_cnd_not_hv_avtrs", WiredNegativeConditionFurniHasPlayers.class);
			put("wf_cnd_wearing_badge", WiredConditionPlayerHasBadgeEquipped.class);
			put("wf_cnd_not_wearing_badge", WiredNegativeConditionPlayerHasBadgeEquipped.class);
			put("wf_cnd_wearing_effect", WiredConditionPlayerWearingEffect.class);
			put("wf_cnd_not_wearing_effect", WiredNegativeConditionPlayerWearingEffect.class);
			put("wf_cnd_has_furni_on", WiredConditionHasFurniOn.class);
			put("wf_cnd_not_furni_on", WiredNegativeConditionHasFurniOn.class);
			put("wf_cnd_user_count_in", WiredConditionPlayerCountInRoom.class);
			put("wf_cnd_not_user_count", WiredNegativeConditionPlayerCountInRoom.class);
			put("wf_cnd_match_snapshot", WiredConditionMatchSnapshot.class);
			put("wf_cnd_not_match_snap", WiredNegativeConditionMatchSnapshot.class);
			put("wf_cnd_has_handitem", WiredConditionHasHandItem.class);
			put("wf_cnd_time_more_than", WiredConditionTimeMoreThan.class);
			put("wf_cnd_time_less_than", WiredConditionTimeLessThan.class);
			put("wf_cnd_actor_in_team", WiredConditionPlayerInTeam.class);
			put("wf_cnd_not_in_team", WiredNegativeConditionPlayerInTeam.class);
			put("wf_cnd_stuff_is", WiredConditionStuffIs.class);
			put("wf_cnd_not_stuff_is", WiredNegativeConditionStuffIs.class);
			
			put("wf_xtra_unseen", WiredAddonUnseenEffect.class);
			
			put("wf_floor_switch1", WiredAddonFloorSwitch.class);
			put("wf_floor_switch2", WiredAddonFloorSwitch.class);
			put("wf_colorwheel", WiredAddonColourWheel.class);
			put("wf_pressureplate", WiredAddonPressurePlate.class);
			put("wf_arrowplate", WiredAddonPressurePlate.class);
			put("wf_ringplate", WiredAddonPressurePlate.class);
			put("wf_pyramid", WiredAddonPyramid.class);
			put("wf_visual_timer", WiredAddonVisualTimer.class);
			put("wf_blob", WiredAddonBlob.class);
			
			put("wf_trg_afkkkdormeur", WiredTriggerCustomIdle.class);
			put("wf_trg_leave_room", WiredTriggerLeavesRoom.class);
			put("wf_trg_cls_user1", WiredTriggerUsersCollide.class);
			
			put("wf_cstm_freeze", WiredCustomFreeze.class);
			put("wf_cstm_fswalk", WiredCustomFastWalk.class);
			put("wf_cstm_dancee", WiredCustomDance.class);
			put("wf_cstm_enable", WiredCustomEnable.class);
			put("wf_cstm_hnitem", WiredCustomHanditem.class);
			put("wf_act_forwa", WiredCustomForwardRoom.class);
			put("wf_act_raise_furni", WiredCustomFurniUp.class);
			put("wf_act_lower_furni", WiredCustomFurniDown.class);
			put("wf_act_usr_clothes", WiredCustomChangeClothes.class);
			put("wf_act_tiles", WiredCustomForceCollision.class);
			
			put("wf_cnd_habbo_has_diamonds", WiredConditionCustomHasDiamonds.class);
			put("wf_cnd_not_habbo_has_diamonds", WiredNegativeConditionCustomHasDiamonds.class);
			put("wf_cnd_habbo_has_duckets", WiredConditionCustomHasDuckets.class);
			put("wf_cnd_not_habbo_has_duckets", WiredNegativeConditionCustomHasDuckets.class);
			put("wf_cnd_habbo_has_diamondz", WiredConditionCustomHasDance.class);
			put("wf_cnd_habbo_not_danc", WiredNegativeConditionCustomHasDance.class);
			put("wf_cnd_habbo_has_rank", WiredConditionCustomHasRank.class);
			put("wf_cnd_habbo_not_rank", WiredNegativeConditionCustomHasRank.class);
			put("wf_cnd_actor_is_idley", WiredConditionCustomIsIdle.class);
			put("wf_cnd_actor_is_idlen", WiredNegativeConditionCustomIsIdle.class);
			
			
			put("highscore_classic", HighScoreClassicFloorItem.class);
			put("highscore_perteam", HighScorePerTeamFloorItem.class);
			put("highscore_mostwins", HighScoreMostWinsFloorItem.class);
			
			put("pressureplate_seat", PressurePlateSeatFloorItem.class);
			
			put("bb_teleport", BanzaiTeleporterFloorItem.class);
			put("bb_red_gate", BanzaiGateFloorItem.class);
			put("bb_yellow_gate", BanzaiGateFloorItem.class);
			put("bb_blue_gate", BanzaiGateFloorItem.class);
			put("bb_green_gate", BanzaiGateFloorItem.class);
			put("bb_patch", BanzaiTileFloorItem.class);
			put("bb_timer", BanzaiTimerFloorItem.class);
			put("bb_puck", BanzaiPuckFloorItem.class);
			
			put("group_item", GroupFloorItem.class);
			put("group_forum", GroupFloorItem.class);
			put("group_gate", GroupGateFloorItem.class);
			
			put("football_timer", FootballTimerFloorItem.class);
			put("ball", FootballFloorItem.class);
			put("football_gate", FootballGateFloorItem.class);
			put("football_goal", FootballGoalFloorItem.class);
			put("football_score", FootballScoreFloorItem.class);
			
			put("snowb_slope", SnowboardSlopeFloorItem.class);
			put("snowb_rail", SnowboardJumpFloorItem.class);
			put("snowb_jump", SnowboardJumpFloorItem.class);
			
			put("totem_planet", TotemPlanetFloorItem.class);
			put("totem_head", TotemHeadFloorItem.class);
			put("totem_body", TotemBodyFloorItem.class);
			
			put("pet_toy", PetToyFloorItem.class);
			put("pet_food", PetFoodFloorItem.class);
			put("pet_nest", PetNestFloorItem.class);
			
			put("pterosaur_egg", PterosaurEggFloorItem.class);
			put("velociraptor_egg", VelociraptorEggFloorItem.class);
			
			put("breeding_dog", DogBreedingBoxFloorItem.class);
			put("breeding_cat", CatBreedingBoxFloorItem.class);
			put("breeding_pig", PigBreedingBoxFloorItem.class);
			put("breeding_terrier", TerrierBreedingBoxFloorItem.class);
			put("breeding_bear", BearBreedingBoxFloorItem.class);
			
			put("cannon", CannonFloorItem.class);
			
			put("horse_jump", HorseJumpFloorItem.class);
			
			put("water", WaterFloorItem.class);
			put("effect", EffectFloorItem.class);
			
			put("freeze_timer", FreezeTimerFloorItem.class);
			put("freeze_gate", FreezeGateFloorItem.class);
			put("freeze_tile", FreezeTileFloorItem.class);
			put("freeze_block", FreezeBlockFloorItem.class);
			put("freeze_exit", FreezeExitFloorItem.class);
			
			put("clothing", ClothingFloorItem.class);
			put("crackable", CrackableFloorItem.class);
		}};
	}
	
	public static RoomItemFloor createFloor(RoomItemData itemData, Room room, IFurnitureDefinition definition) {
		RoomItemFloor floorItem = null;
		
		if (definition == null) {
			return null;
		}
		
		if (definition.canSit()) {
			floorItem = new SeatFloorItem(itemData, room);
		}
		
		if (definition.getItemName().startsWith(STACK_TOOL)) {
			floorItem = new MagicStackFloorItem(itemData, room);
		}
		
		if (definition.isAdFurni()) {
			floorItem = new AdsFloorItem(itemData, room);
		}
		
		if (definition.getItemName().contains("yttv")) {
			floorItem = new VideoPlayerFloorItem(itemData, room);
		}
		
		if (itemData.getData().startsWith(GiftData.EXTRA_DATA_HEADER) || itemData.getData().startsWith(LegacyGiftData.EXTRA_DATA_HEADER)) {
			try {
				floorItem = new GiftFloorItem(itemData, room);
			} catch (Exception e) {
				return null;
			}
		} else {
			if (itemDefinitionMap.containsKey(definition.getInteraction())) {
				try {
					Constructor<? extends RoomItemFloor> constructor;
					
					if (itemConstructorCache.containsKey(definition.getInteraction())) {
						constructor = itemConstructorCache.get(definition.getInteraction());
					} else {
						constructor = itemDefinitionMap.get(definition.getInteraction()).getConstructor(RoomItemData.class, Room.class);
						itemConstructorCache.put(definition.getInteraction(), constructor);
					}
					
					if (constructor != null) floorItem = constructor.newInstance(itemData, room);
				} catch (Exception e) {
					log.warn("Failed to create instance for item: " + itemData.getId() + ", type: " + definition.getInteraction(), e);
				}
			}
		}
		
		
		if (floorItem == null) {
			floorItem = new DefaultFloorItem(itemData, room);
		}
		
		if (itemData.getLimitedEdition() != null) {
			floorItem.setLimitedEditionItemData((LimitedEditionItemData) itemData.getLimitedEdition());
		}
		
		return floorItem;
	}
	
	public static RoomItemWall createWall(RoomItemData itemData, Room room, IFurnitureDefinition definition) {
		if (definition == null) {
			return null;
		}
		
		RoomItemWall wallItem;
		
		switch (definition.getInteraction()) {
			case "habbowheel" -> wallItem = new WheelWallItem(itemData, room);
			case "dimmer" -> wallItem = new MoodLightWallItem(itemData, room);
			case "postit" -> wallItem = new PostItWallItem(itemData, room);
			default -> wallItem = new DefaultWallItem(itemData, room);
		}
		
		if (itemData.getLimitedEdition() != null) {
			wallItem.setLimitedEditionItemData((LimitedEditionItemData) itemData.getLimitedEdition());
		}
		
		return wallItem;
	}
	
	public static int getProcessTime(double time) {
		long realTime = Math.round(time * 1000 / processMs);
		
		if (realTime < 1) {
			realTime = 1;
		}
		
		return (int) realTime;
	}
	
}
