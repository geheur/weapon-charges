package com.weaponcharges;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.CommandExecuted;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class Devtools
{
	private final WeaponChargesPlugin plugin;

	public Devtools(WeaponChargesPlugin plugin) {
		this.plugin = plugin;
	}

	private void message(String message) {
		log.info(plugin.client.getTickCount() + ": " + message);
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE || chatMessage.getType() == ChatMessageType.SPAM)
		{
			message(chatMessage.getType() + " \"" + chatMessage.getMessage() + "\"");
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged) {
		if (animationChanged.getActor().equals(plugin.client.getLocalPlayer())) {
			message("animation changed: " + animationChanged.getActor().getAnimation());
		}
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted) {
		if ("setcharges".equals(commandExecuted.getCommand())) {
			String weapon = commandExecuted.getArguments()[0];
			String key = getKey(weapon);
			plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "setting " + key + " to " + commandExecuted.getArguments()[1], "");
			plugin.configManager.setRSProfileConfiguration(plugin.CONFIG_GROUP_NAME, key, commandExecuted.getArguments()[1]);
		} else if ("unsetcharges".equals(commandExecuted.getCommand())) {
			String weapon = commandExecuted.getArguments()[0];
			String key = getKey(weapon);
			plugin.client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "unsetting " + key, "");
			plugin.configManager.unsetRSProfileConfiguration(plugin.CONFIG_GROUP_NAME, key);
		}
	}

	private String getKey(String weapon)
	{
		switch (weapon) {
			case "bpscales":
				return "blowpipeScales";
			case "bpdarts":
				return "blowpipeDarts";
			case "swamp":
				return ChargedWeapon.TRIDENT_OF_THE_SWAMP.configKeyName;
			case "seas":
				return ChargedWeapon.TRIDENT_OF_THE_SEAS.configKeyName;
			case "ibans":
				return ChargedWeapon.IBANS_STAFF.configKeyName;
			case "chally":
				return ChargedWeapon.CRYSTAL_HALBERD.configKeyName;
			case "tent":
				return ChargedWeapon.ABYSSAL_TENTACLE.configKeyName;
			case "tome":
				return ChargedWeapon.TOME_OF_FIRE.configKeyName;
			case "serp":
				return ChargedWeapon.SERPENTINE_HELM.configKeyName;
			default:
				return null;
		}
	}

	public void dialogStateChanged(DialogTracker.DialogState dialogState)
	{
		message("dialog state changed: " + dialogState);
	}

	public void optionSelected(DialogTracker.DialogState dialogState, String s)
	{
		message("option selected: \"" + s + "\" from " + dialogState);
	}
}
