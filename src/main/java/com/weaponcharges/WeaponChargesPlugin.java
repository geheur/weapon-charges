/*
 * Copyright (c) 2018, Sir Girion <https://github.com/sirgirion>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.weaponcharges;

import com.google.inject.Provides;
import com.weaponcharges.WeaponChargesConfig.DisplayWhen;
import static com.weaponcharges.WeaponChargesConfig.DisplayWhen.ALWAYS;
import static com.weaponcharges.WeaponChargesConfig.DisplayWhen.LOW_CHARGE;
import static com.weaponcharges.WeaponChargesConfig.DisplayWhen.NEVER;
import static com.weaponcharges.WeaponChargesConfig.DisplayWhen.USE_DEFAULT;
import com.weaponcharges.WeaponChargesConfig.SerpModes;
import static com.weaponcharges.WeaponChargesConfig.SerpModes.BOTH;
import static com.weaponcharges.WeaponChargesConfig.SerpModes.PERCENT;
import static com.weaponcharges.WeaponChargesConfig.SerpModes.SCALES;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.HitsplatID;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.Projectile;
import net.runelite.api.Skill;
import net.runelite.api.VarPlayer;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.events.StatChanged;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "Weapon Charges",
	description = "Displays ibans blast and swamp trident charges on the inventory icon or as an infobox.",
	tags = {"iban", "trident", "charge"}
)
// TODO for midrif's weak math brain: https://github.com/runelite/runelite/pull/11044/files
// could also do this for stuff like ibans, but in reverse. You can't charge those, but you might want to know how
// many runes to bring so that you don't run out of runes before charges.
@Slf4j
public class WeaponChargesPlugin extends Plugin implements KeyListener
{
	public static final String CONFIG_GROUP_NAME = "weaponCharges";
	public static final String DEV_MODE_CONFIG_KEY = "logData";
	private static final int[] BLOWPIPE_ATTACK_ANIMATIONS = new int[]{5061, 10656};

	// TODO rename. This is used for when an item is used on a weapon, when a weapon is used on an item, and when "pages" is clicked.
	ChargedWeapon lastUsedOnWeapon;
	ChargedWeapon lastUnchargeClickedWeapon;

	@Inject Client client;
	@Inject private WeaponChargesItemOverlay itemOverlay;
	@Inject private ItemManager itemManager;
	@Inject private WeaponChargesConfig config;
	@Inject private ClientThread clientThread;
	@Inject private EventBus eventBus;
	@Inject private OverlayManager overlayManager;
	@Inject private KeyManager keyManager;
	@Inject private DialogTracker dialogTracker;
	@Inject private ChatboxPanelManager chatboxPanelManager;
	@Inject private ChatMessageManager chatMessageManager;

	private Devtools devtools;

	@Provides
	WeaponChargesConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WeaponChargesConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(itemOverlay);
		if (config.devMode()) enableDevMode();
		dialogTracker.reset();
		eventBus.register(dialogTracker);
		keyManager.registerKeyListener(dialogTracker);
		keyManager.registerKeyListener(this);
		dialogTracker.setStateChangedListener(this::dialogStateChanged);
		dialogTracker.setOptionSelectedListener(this::optionSelected);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(itemOverlay);
		disableDevMode();
		eventBus.unregister(dialogTracker);
		keyManager.unregisterKeyListener(dialogTracker);
		keyManager.unregisterKeyListener(this);
	}

	void dialogStateChanged(DialogTracker.DialogState dialogState)
	{
		if (devtools != null && config.devMode()) devtools.dialogStateChanged(dialogState);

		// TODO if you can calculate the total charges available in the inventory you could get an accurate count on the "add how many charges" dialog, because max charges - max charges addable = current charges.

		for (ChargesDialogHandler nonUniqueDialogHandler : ChargedWeapon.getNonUniqueDialogHandlers())
		{
			nonUniqueDialogHandler.handleDialog(dialogState, this);
		}

		outer_loop:
		for (ChargedWeapon chargedWeapon : ChargedWeapon.values())
		{
			for (ChargesDialogHandler dialogHandler : chargedWeapon.getDialogHandlers())
			{
				if (dialogHandler.handleDialog(dialogState, this)) break outer_loop;
			}
		}
	}

//	private static final Pattern CHARGES_PATTERN = Pattern.compile("How many charges would you like to add\\? \\(0 - ([\\d,]+)\\)");
//
	void optionSelected(DialogTracker.DialogState dialogState, String optionSelected)
	{
		if (devtools != null && config.devMode()) devtools.optionSelected(dialogState, optionSelected);

		// I don't think adding a single charge by using the items on the weapon is going to be trackable if the user
		// skips the sprite dialog.

		for (ChargesDialogHandler nonUniqueDialogHandler : ChargedWeapon.getNonUniqueDialogHandlers())
		{
			nonUniqueDialogHandler.handleDialogOptionSelected(dialogState, optionSelected, this);
		}

		outer_loop:
		for (ChargedWeapon chargedWeapon : ChargedWeapon.values())
		{
			for (ChargesDialogHandler dialogHandler : chargedWeapon.getDialogHandlers())
			{
				if (dialogHandler.handleDialogOptionSelected(dialogState, optionSelected, this)) break outer_loop;
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		if (configChanged.getGroup().equals(CONFIG_GROUP_NAME) && configChanged.getKey().equals(DEV_MODE_CONFIG_KEY)) {
			if (config.devMode()) {
				enableDevMode();
			} else {
				disableDevMode();
			}
		}
	}

	private void enableDevMode()
	{
		if (devtools == null) devtools = new Devtools(this);
		eventBus.register(devtools);
	}

	private int lastDegradedHitsplatTick = -1000; // 1000 is far more than 91, so the serp helm will be able to have its degrading tracked on login rather than having to wait 90 ticks.

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied e) {
		int hitType = e.getHitsplat().getHitsplatType();
		if (checkScytheHitsplats == client.getTickCount() && e.getHitsplat().isMine() && e.getHitsplat().getAmount() > 0 && scytheHitsplatsSeen <= 2) {
			// skip first hit since one charge has already been removed due to the xp drop.
			if (scytheHitsplatsSeen > 0) addCharges(ChargedWeapon.BLOOD_FURY, -1, false);
			scytheHitsplatsSeen++;
		}
		ChargedWeapon helm = getEquippedChargedWeapon(EquipmentInventorySlot.HEAD);
		if (helm == ChargedWeapon.SERPENTINE_HELM) {
			if (e.getHitsplat().isMine()) { // Caused by or dealt to the local player.
				if (client.getTickCount() - lastDegradedHitsplatTick > 90) {
					addCharges(helm, -10, false);
					lastDegradedHitsplatTick = client.getTickCount();
					if (config.devMode())
						client.addChatMessage(ChatMessageType.FRIENDSCHAT, "WeaponCharges", "Serpentine Helmet has Degraded!", "DEVMODE");
				}
			}
		}
		ChargedWeapon body = getEquippedChargedWeapon(EquipmentInventorySlot.BODY);
		ChargedWeapon legs = getEquippedChargedWeapon(EquipmentInventorySlot.LEGS);
		if (e.getActor() == client.getLocalPlayer() && hitType == HitsplatID.DAMAGE_ME) {
			if (helm == ChargedWeapon.CRYSTAL_HELM) {
				addCharges(helm, -1, false);
			}
			if (body == ChargedWeapon.CRYSTAL_BODY) {
				addCharges(body, -1, false);
			}
			if (legs == ChargedWeapon.CRYSTAL_LEGS) {
				addCharges(legs, -1, false);
			}
		}
	}

	private void disableDevMode()
	{
		if (devtools != null) eventBus.unregister(devtools);
	}

	// There are two lists to keep a list of checked weapons not just in the last tick, but in the last 2. I do this because
	// I'm paranoid that someone will somehow check an item without getting a check message, or the check message
	// does not match any regexes for some reason. This can cause the plugin to assign charges to the wrong weapon.
	private List<ChargedWeapon> lastWeaponChecked = new ArrayList<>();
	private List<ChargedWeapon> lastWeaponChecked2 = new ArrayList<>();

	// If this doesn't have -1 priority, it will run prior to the vanilla
	// runelite MES which cannot properly handle other plugins adding menu
	// entries to the menu before it adds its menu entries. This is because it
	// uses the MenuOpened event's list of menu entries which does not reflect
	// changes to the menu entries made by other plugins in their MenuOpened
	// subscribers. It can lead to the vanilla runelite MES menu options being
	// added in the wrong spot.
	@Subscribe(priority = -1)
	public void onMenuOpened(MenuOpened e)
	{
		onMenuOpened2();

		addVorkathsHeadMenuOptions();
	}

	private void addVorkathsHeadMenuOptions()
	{
		if (!client.isKeyPressed(KeyCode.KC_SHIFT) || config.vorkathsHeadMenuOptionDisabled()) {
			return;
		}

		for (MenuEntry menuEntry : client.getMenuEntries())
		{
			int itemId;
			if (WidgetUtil.componentToInterface(menuEntry.getParam1()) == InterfaceID.EQUIPMENT) { // item is equipped.
				int childId = WidgetUtil.componentToId(menuEntry.getParam1());
				if (childId == 16) // cape slot.
				{
					ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);
					if (itemContainer == null) return;

					Item item = itemContainer.getItem(EquipmentInventorySlot.CAPE.getSlotIdx());
					if (item == null) return;

					itemId = item.getId();
				} else {
					return;
				}
			} else if (menuEntry.getItemId() != -1) {
				itemId = menuEntry.getItemId();
			} else {
				continue;
			}

			if (
				itemId == ItemID.RANGING_CAPE ||
				itemId == ItemID.RANGING_CAPET ||
				itemId == ItemID.MAX_CAPE
			) {
				boolean vorkathsHeadUsed = Boolean.valueOf(configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, "vorkathsHeadUsed"));
				MenuEntry submenuEntry = client.createMenuEntry(1)
					.setOption("Weapon charges plugin")
					.setType(MenuAction.RUNELITE_SUBMENU);
				addSubmenu(ColorUtil.wrapWithColorTag("Vorkath's head ammo saving", Color.decode("#ff9040")),
					submenuEntry);
				addSubmenuRadioButtonStyle(vorkathsHeadUsed, ColorUtil.wrapWithColorTag("80%", Color.decode("#49afd6")),
					e -> configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, "vorkathsHeadUsed", true),
					submenuEntry);
				addSubmenuRadioButtonStyle(!vorkathsHeadUsed, ColorUtil.wrapWithColorTag("72%", Color.decode("#5e855a")),
					e -> configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, "vorkathsHeadUsed", false),
					submenuEntry);
				break;
			} else if (
				itemId == ItemID.DIZANAS_MAX_CAPE ||
				itemId == ItemID.DIZANAS_MAX_CAPE_L ||
				itemId == ItemID.DIZANAS_QUIVER ||
				itemId == ItemID.DIZANAS_QUIVER_L ||
				itemId == ItemID.BLESSED_DIZANAS_QUIVER ||
				itemId == ItemID.BLESSED_DIZANAS_QUIVER_L ||
				itemId == ItemID.DIZANAS_QUIVER_UNCHARGED ||
				itemId == ItemID.DIZANAS_QUIVER_UNCHARGED_L
			) {
				String configString = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, "dizanasQuiverAmmoSaving");
				int dizanasQuiverAmmoSaving = configString == null ? 0 : Integer.parseInt(configString);
				MenuEntry submenuEntry = client.createMenuEntry(1)
					.setOption("Weapon charges plugin")
					.setType(MenuAction.RUNELITE_SUBMENU);
				addSubmenu(ColorUtil.wrapWithColorTag("Dizana's quiver ammo saving", Color.decode("#ff9040")),
					submenuEntry);
				addSubmenuRadioButtonStyle(dizanasQuiverAmmoSaving == 0, ColorUtil.wrapWithColorTag("80%", Color.decode("#49afd6")),
					e -> configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, "dizanasQuiverAmmoSaving", 0),
					submenuEntry);
				addSubmenuRadioButtonStyle(dizanasQuiverAmmoSaving == 1, ColorUtil.wrapWithColorTag("72%", Color.decode("#5e855a")),
					e -> configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, "dizanasQuiverAmmoSaving", 1),
					submenuEntry);
				addSubmenuRadioButtonStyle(dizanasQuiverAmmoSaving == 2, ColorUtil.wrapWithColorTag("60%", Color.decode("#3d5885")),
					e -> configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, "dizanasQuiverAmmoSaving", 2),
					submenuEntry);
				break;
			}
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
	    if (event.getMenuOption().equalsIgnoreCase("check")) {
	    	// TODO investigate shift-click.
			if (config.devMode()) log.info("clicked \"check\" on " + event.getMenuTarget());

			if (WidgetUtil.componentToInterface(event.getParam1()) == InterfaceID.EQUIPMENT) { // item is equipped.
				int childId = WidgetUtil.componentToId(event.getParam1());
				if (childId == 18) {
					ChargedWeapon chargedWeapon = getEquippedChargedWeapon(EquipmentInventorySlot.WEAPON);
					if (chargedWeapon != null) lastWeaponChecked.add(chargedWeapon);
				} else if (childId == 20) {
					ChargedWeapon chargedWeapon = getEquippedChargedWeapon(EquipmentInventorySlot.SHIELD);
					if (chargedWeapon != null) lastWeaponChecked.add(chargedWeapon);
				}
			} else {
				for (ChargedWeapon chargedWeapon : ChargedWeapon.values())
				{
					if (chargedWeapon.getItemIds().contains(event.getItemId()) && chargedWeapon.getCheckChargesRegexes().isEmpty())
					{
						if (config.devMode()) log.info("adding last weapon checked to " + chargedWeapon);
						lastWeaponChecked.add(chargedWeapon);
						break;
					}
				}
			}
		} else if (event.getMenuOption().equalsIgnoreCase("uncharge")) {
			for (ChargedWeapon chargedWeapon : ChargedWeapon.values())
			{
				if (chargedWeapon.getItemIds().contains(event.getItemId()))
				{
					if (config.devMode()) log.info("setting lastUnchargeClickedWeapon to " + chargedWeapon);
					lastUnchargeClickedWeapon = chargedWeapon;
					break;
				}
			}
		} else if (event.getMenuOption().equalsIgnoreCase("unload") && event.getItemId() == ItemID.TOXIC_BLOWPIPE) {
			checkBlowpipeUnload = client.getTickCount();
		} else if (event.getMenuOption().equalsIgnoreCase("pages")) {
			if (WidgetUtil.componentToInterface(event.getParam1()) == InterfaceID.EQUIPMENT) { // item is equipped.
				lastUsedOnWeapon = getEquippedChargedWeapon(EquipmentInventorySlot.SHIELD);
			} else {
				lastUsedOnWeapon = ChargedWeapon.getChargedWeaponFromId(event.getItemId());
			}
			if (config.devMode()) log.info("pages checked. setting last used weapon to {}", lastUsedOnWeapon.toString());
		}

		if (event.getMenuAction() == MenuAction.WIDGET_TARGET_ON_WIDGET) {
			ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
			Item itemUsed = itemContainer.getItem(client.getSelectedWidget().getIndex());
			if (itemUsed == null) return;
			int itemUsedId = itemUsed.getId();
			Item itemUsedOn = itemContainer.getItem(event.getWidget().getIndex());
			if (itemUsedOn == null) return;
			int itemUsedOnId = itemUsedOn.getId();
			lastUsedOnWeapon = ChargedWeapon.getChargedWeaponFromId(itemUsedId);
			if (lastUsedOnWeapon == null)
			{
				lastUsedOnWeapon = ChargedWeapon.getChargedWeaponFromId(itemUsedOnId);
				if (lastUsedOnWeapon != null)
				{
					if (config.devMode()) log.info("{}: used {} on {}", client.getTickCount(), itemUsedId, lastUsedOnWeapon);
					checkSingleCrystalShardUse(itemUsed, itemUsedId);
				} else {
					if (config.devMode()) log.info("{}: used {} on {}", client.getTickCount(), itemUsedId, itemUsedOnId);
				}
			} else {
				if (config.devMode()) log.info("{}: used {} on {}", client.getTickCount(), lastUsedOnWeapon, itemUsedOnId);
				checkSingleCrystalShardUse(itemUsedOn, itemUsedOnId);
			}
		}
	}

	private void checkSingleCrystalShardUse(Item itemUsed, int itemUsedId)
	{
		if (itemUsedId == ItemID.CRYSTAL_SHARD && itemUsed.getQuantity() == 1 && ChargedWeapon.CRYSTAL_SHARD_RECHARGABLE_ITEMS.contains(lastUsedOnWeapon)) {
			checkSingleCrystalShardUse = client.getTickCount();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged) {
		if (itemContainerChanged.getContainerId() != InventoryID.INVENTORY.getId()) {
			return;
		}

		if (checkBlowpipeUnload == client.getTickCount() || checkBlowpipeUnload + 1 == client.getTickCount()) {
			setDartsLeft(0);
			setDartType(DartType.UNKNOWN);
		}

		if (checkSingleCrystalShardUse == client.getTickCount() || checkSingleCrystalShardUse + 1 == client.getTickCount()) {
			addCharges(lastUsedOnWeapon, 100, false);
		}
	}

	private final List<Runnable> delayChargeUpdateUntilAfterAnimations = new ArrayList<>();

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		String message = Text.removeTags(event.getMessage());

		for (ChargesMessage checkMessage : ChargedWeapon.getNonUniqueCheckChargesRegexes())
		{
			Matcher matcher = checkMessage.getPattern().matcher(message);
			if (matcher.find()) {
				ChargedWeapon chargedWeapon = removeLastWeaponChecked();
				// TODO possible to mess stuff up by checking a weapon immediately after the tome of water/fire dialog?
				if (chargedWeapon != null) {
					setCharges(chargedWeapon, checkMessage.getChargesLeft(matcher, configManager));
				} else if (lastUsedOnWeapon != null) {
					setCharges(lastUsedOnWeapon, checkMessage.getChargesLeft(matcher, configManager));
					if (config.devMode()) log.info("applying charges to last used-on weapon: {}", lastUsedOnWeapon);
				} else {
					log.warn("saw check message without having seen a charged weapon checked or used: \"" + message + "\"" );
				}
				break;
			}
		}

		for (ChargesMessage checkMessage : ChargedWeapon.getNonUniqueUpdateMessageChargesRegexes())
		{
			Matcher matcher = checkMessage.getPattern().matcher(message);
			if (matcher.find()) {
				int chargeCount = checkMessage.getChargesLeft(matcher, configManager);
				delayChargeUpdateUntilAfterAnimations.add(() -> {
					ChargedWeapon equippedWeapon = getEquippedChargedWeapon(EquipmentInventorySlot.WEAPON);
					if (equippedWeapon != null) {
						setCharges(equippedWeapon, chargeCount);
					} else {
						log.warn("saw charge update message without a weapon being equipped: \"" + message + "\"");
					}
				});
				break;
			}
		}

		outer_loop:
		for (ChargedWeapon chargedWeapon : ChargedWeapon.values())
		{
			if (chargedWeapon.getCheckChargesRegexes().isEmpty()) continue;

			for (ChargesMessage checkMessage : chargedWeapon.getCheckChargesRegexes())
			{
				Matcher matcher = checkMessage.getPattern().matcher(message);
				if (matcher.find()) {
					setCharges(chargedWeapon, checkMessage.getChargesLeft(matcher, configManager));
					break outer_loop;
				}
			}

			for (ChargesMessage checkMessage : chargedWeapon.getUpdateMessageChargesRegexes())
			{
				Matcher matcher = checkMessage.getPattern().matcher(message);
				if (matcher.find()) {
					delayChargeUpdateUntilAfterAnimations.add(() -> setCharges(chargedWeapon, checkMessage.getChargesLeft(matcher, configManager)));
					break outer_loop;
				}
			}
		}

		chatMessageBlowpipe(message);
	}

	private ChargedWeapon removeLastWeaponChecked()
	{
		return !lastWeaponChecked2.isEmpty() ? lastWeaponChecked2.remove(0) :
				!lastWeaponChecked.isEmpty() ? lastWeaponChecked.remove(0) :
				null;
	}

	private ChargedWeapon getEquippedChargedWeapon(EquipmentInventorySlot slot)
	{
		ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		if (itemContainer == null) return null;

		Item item = itemContainer.getItem(slot.getSlotIdx());
		if (item == null) return null;

		return ChargedWeapon.getChargedWeaponFromId(item.getId());
	}

	/* blowpipe:
	// checking:
	// 2021-08-29 14:22:09 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 1135: GAMEMESSAGE "Darts: <col=007f00>None</col>. Scales: <col=007f00>99 (0.6%)</col>."
	// 2021-09-05 13:55:04 [Client] INFO  com.weaponcharges.Devtools - 9: GAMEMESSAGE "Darts: <col=007f00>Adamant dart x 16,383</col>. Scales: <col=007f00>16,383 (100.0%)</col>."
	// 2021-09-05 13:55:26 [Client] INFO  com.weaponcharges.Devtools - 46: GAMEMESSAGE "Darts: <col=007f00>Adamant dart x 16,383</col>. Scales: <col=007f00>0 (0.0%)</col>."

	// adding charges either uses the check messages, or one of the following:
	// using scales on full blowpipe: 2021-09-05 13:48:26 [Client] INFO  com.weaponcharges.Devtools - 640: GAMEMESSAGE "The blowpipe can't hold any more scales."
	// using darts on full blowpipe: 2021-09-05 13:48:25 [Client] INFO  com.weaponcharges.Devtools - 638: GAMEMESSAGE "The blowpipe can't hold any more darts."

	// run out of darts: 2021-08-29 14:19:11 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 841: GAMEMESSAGE "Your blowpipe has run out of darts."
	// run out of scales: 2021-08-29 14:18:27 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 767: GAMEMESSAGE "Your blowpipe needs to be charged with Zulrah's scales."
	// run out of both: 2021-09-05 13:45:24 [Client] INFO  com.weaponcharges.Devtools - 336: GAMEMESSAGE "Your blowpipe has run out of scales and darts."

	// (attacking with no darts: 2021-09-05 13:43:43 [Client] INFO  com.weaponcharges.Devtools - 169: GAMEMESSAGE "Your blowpipe contains no darts."
	// (attacking with no darts or scales (trying to equip blowpipe without EITHER scales or darts in it produces the same message, lol) : 2021-09-05 13:45:43 [Client] INFO  com.weaponcharges.Devtools - 369: GAMEMESSAGE "Your blowpipe needs to be charged with Zulrah's scales and loaded with darts."
	// (attacking with no scales, same as run out of scales message): 2021-09-05 13:47:42 [Client] INFO  com.weaponcharges.Devtools - 566: GAMEMESSAGE "Your blowpipe needs to be charged with Zulrah's scales."

	// unload
	// unload with no darts: 2021-09-05 13:59:25 [Client] INFO  com.weaponcharges.Devtools - 443: GAMEMESSAGE "The blowpipe has no darts in it."
	// unload with darts has no chat message.

	// don't care because when you add charges after it always produces a chat message.
	// uncharge 2021-09-05 14:40:47 [Client] INFO  com.weaponcharges.Devtools - 481: dialog state changed: DialogState{DESTROY_ITEM, title='Are you sure you want to uncharge it?', itemId=12926, item_name='Toxic blowpipe', text='If you uncharge the blowpipe, all scales and darts will fall out.'}
	 */

	// check messages.
	private static final Pattern NO_DARTS_CHECK_PATTERN = Pattern.compile("Darts: None. Scales: ([\\d,]+) \\(\\d+[.]?\\d%\\).");
	private static final Pattern DARTS_AND_SCALE_CHECK_PATTERN = Pattern.compile("Darts: (\\S*)(?: dart)? x ([\\d,]+). Scales: ([\\d,]+) \\(\\d+[.]?\\d%\\).");
	private static final Pattern USE_SCALES_ON_FULL_BLOWPIPE_PATTERN = Pattern.compile("The blowpipe can't hold any more scales.");
	private static final Pattern USE_DARTS_ON_FULL_BLOWPIPE_PATTERN = Pattern.compile("The blowpipe can't hold any more darts.");
	private static final Pattern UNLOAD_EMPTY_BLOWPIPE_PATTERN = Pattern.compile("The blowpipe has no darts in it.");

	// update messages.
	private static final Pattern NO_DARTS_PATTERN = Pattern.compile("Your blowpipe has run out of darts.");
	private static final Pattern NO_SCALES_PATTERN = Pattern.compile("Your blowpipe needs to be charged with Zulrah's scales.");
	private static final Pattern NO_DARTS_OR_SCALES_PATTERN = Pattern.compile("Your blowpipe has run out of scales and darts.");
	private static final Pattern NO_DARTS_PATTERN_2 = Pattern.compile("Your blowpipe contains no darts.");
	private static final Pattern NO_DARTS_OR_SCALES_PATTERN_2 = Pattern.compile("Your blowpipe needs to be charged with Zulrah's scales and loaded with darts.");

	private void chatMessageBlowpipe(String chatMsg)
	{
		Matcher matcher = DARTS_AND_SCALE_CHECK_PATTERN.matcher(chatMsg);
		if (matcher.find())
		{
			setDartsLeft(Integer.parseInt(matcher.group(2).replace(",", "")));
			setScalesLeft(Integer.parseInt(matcher.group(3).replace(",", "")));
			setDartType(DartType.getDartTypeByName(matcher.group(1)));
		}

		matcher = NO_DARTS_CHECK_PATTERN.matcher(chatMsg);
		if (matcher.find())
		{
			setDartsLeft(0);
			setScalesLeft(Integer.parseInt(matcher.group(1).replace(",", "")));
			setDartType(DartType.UNKNOWN);
		}

		matcher = USE_SCALES_ON_FULL_BLOWPIPE_PATTERN.matcher(chatMsg);
		if (matcher.find()) {
			setScalesLeft(MAX_SCALES_BLOWPIPE);
		}

		matcher = USE_DARTS_ON_FULL_BLOWPIPE_PATTERN.matcher(chatMsg);
		if (matcher.find()) {
			setDartsLeft(MAX_DARTS);
		}

		matcher = UNLOAD_EMPTY_BLOWPIPE_PATTERN.matcher(chatMsg);
		if (matcher.find()) {
			setDartsLeft(0);
			setDartType(DartType.UNKNOWN);
		}

		matcher = NO_DARTS_PATTERN.matcher(chatMsg);
		if (matcher.find()) {
			delayChargeUpdateUntilAfterAnimations.add(() -> {
				setDartsLeft(0);
				setDartType(DartType.UNKNOWN);
			});
		}

		matcher = NO_SCALES_PATTERN.matcher(chatMsg);
		if (matcher.find())
		{
			delayChargeUpdateUntilAfterAnimations.add(() -> setScalesLeft(0));
		}

		matcher = NO_DARTS_OR_SCALES_PATTERN.matcher(chatMsg);
		if (matcher.find())
		{
			delayChargeUpdateUntilAfterAnimations.add(() -> {
				setScalesLeft(0);
				setDartsLeft(0);
				setDartType(DartType.UNKNOWN);
			});
		}

		matcher = NO_DARTS_PATTERN_2.matcher(chatMsg);
		if (matcher.find())
		{
			delayChargeUpdateUntilAfterAnimations.add(() -> {
				setDartsLeft(0);
				setDartType(DartType.UNKNOWN);
			});
		}

		matcher = NO_DARTS_OR_SCALES_PATTERN_2.matcher(chatMsg);
		if (matcher.find())
		{
			delayChargeUpdateUntilAfterAnimations.add(() -> {
				setScalesLeft(0);
				setDartsLeft(0);
				setDartType(DartType.UNKNOWN);
			});
		}
	}

	private int checkBlowpipeUnload = -100;
	private int checkSingleCrystalShardUse = -100;

	private int lastLocalPlayerAnimationChangedGameTick = -1;
	// I record the animation id so that animation changing plugins that change the animation (e.g. weapon animation replacer) can't interfere.
	private int lastLocalPlayerAnimationChanged = -1;
	private int lastLocalPlayerGraphicChangedGameTick = -1;
	// I record the graphic id so that graphic changing plugins that change the graphic can't interfere.
	private int lastLocalPlayerGraphicChanged = -1;
	private int checkBlowpipeGameTick = -1;

	@Subscribe(priority = 10.0f) // I want to get ahead of those pesky animation modifying plugins.
	public void onAnimationChanged(AnimationChanged event)
	{
		final Actor actor = event.getActor();
		if (actor != client.getLocalPlayer()) return;

		lastLocalPlayerAnimationChangedGameTick = client.getTickCount();
		lastLocalPlayerAnimationChanged = actor.getAnimation();
	}

	@Subscribe(priority = 10.0f)
	public void onGraphicChanged(GraphicChanged event)
	{
		final Actor actor = event.getActor();
		if (actor != client.getLocalPlayer()) return;

		lastLocalPlayerGraphicChangedGameTick = client.getTickCount();
		lastLocalPlayerGraphicChanged = actor.getGraphic();
	}

	@Subscribe(priority = 10.0f)
	public void onProjectileMoved(ProjectileMoved event)
	{
		Projectile projectile = event.getProjectile();
		if (client.getGameCycle() >= projectile.getStartCycle()) return; // skip already seen projectiles.

		// This is the player's actual location which is what projectiles use as their start position. Player#getX, #getSceneX, etc., do not work here.
		Player player = client.getLocalPlayer();
		final WorldPoint playerPos = player.getWorldLocation();
		if (playerPos == null) return;
		final LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);
		if (playerPosLocal == null) return;

		if (projectile.getX1() != playerPosLocal.getX() || projectile.getY1() != playerPosLocal.getY()) return;

		int id = projectile.getId();
		if (id == 1122 || id == 1936 || (id >= 226 && id <= 231)) {
			checkBlowpipeGameTick = client.getTickCount();
		}
	}

	private static final int TICKS_RAPID_PVM = 2;
//	private static final int TICKS_RAPID_PVP = 3;
	private static final int TICKS_NORMAL_PVM = 3;
//	private static final int TICKS_NORMAL_PVP = 4;
	public static final int MAX_SCALES_BLOWPIPE = 16383;
	public static final int MAX_DARTS = 16383;

	private int blowpipeCooldownUp = 0;

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		bloodFuryAppliedThisTick = false;
		int tickCount = client.getTickCount();

		// This delay is necessary because equipped items are updated after onAnimationChanged, so with items that share
		// a game message it will not be possible to tell which item the message is for.
		// The order must be: check messages, animation, charge update messages.
		// Runelite's order is: onChatMessage, onAnimationChanged, onGameTick.
		// charge update messages must also be delayed due to equipment slot info not being current in onChatMessage.
		checkAnimation(lastLocalPlayerAnimationChangedGameTick == tickCount, lastLocalPlayerGraphicChangedGameTick == tickCount);

		if (checkBlowpipeGameTick == tickCount && tickCount >= blowpipeCooldownUp && Arrays.stream(BLOWPIPE_ATTACK_ANIMATIONS).anyMatch(id -> id == lastLocalPlayerAnimationChanged)) {
			blowpipeCooldownUp = tickCount +
				client.getVarpValue(VarPlayer.ATTACK_STYLE) == 1 ? TICKS_RAPID_PVM : TICKS_NORMAL_PVM
			;
			consumeBlowpipeCharges();
		}

		if (!delayChargeUpdateUntilAfterAnimations.isEmpty()) {
			for (Runnable runnable : delayChargeUpdateUntilAfterAnimations)
			{
				runnable.run();
			}
			delayChargeUpdateUntilAfterAnimations.clear();
		}

		if (!lastWeaponChecked2.isEmpty()) {
			log.warn("checked weapons with no check message: " + lastWeaponChecked2);
		}
		lastWeaponChecked2.clear();
		lastWeaponChecked2 = lastWeaponChecked;
		lastWeaponChecked = new ArrayList<>();
	}

	private void checkAnimation(boolean checkAnimation, boolean checkGraphic)
	{
		if (!checkAnimation && !checkGraphic) return;

		ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		if (itemContainer == null) return;
		Item weapon = itemContainer.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		int weaponItemId = (weapon == null) ? -1 : weapon.getId();
		Item offhand = itemContainer.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx());
		int offhandItemId = (offhand == null) ? -1 : offhand.getId();

		for (ChargedWeapon chargedWeapon : ChargedWeapon.values()) {
			if (chargedWeapon.getItemIds().contains(weaponItemId) || chargedWeapon.getItemIds().contains(offhandItemId)) {
				if (
					checkAnimation && chargedWeapon.animationIds.contains(lastLocalPlayerAnimationChanged) ||
					checkGraphic && chargedWeapon.graphicIds.contains(lastLocalPlayerGraphicChanged)
				) {
					addCharges(chargedWeapon, -1, false);
				}
			}
		}
	}

	public int leaguesRelic()
	{
		return client.getVarbitValue(10052);
	}

	private void consumeBlowpipeCharges()
	{
		addDartsLeft(-1 * getAmmoLossChance(), false);
		addScalesLeft(-2/3f, false);
	}

	private float getAmmoLossChance()
	{
		int attractorEquippedId = client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.CAPE);
		switch (attractorEquippedId) {
			case ItemID.AVAS_ATTRACTOR:
				return 0.4f;
			case ItemID.AVAS_ACCUMULATOR:
			case ItemID.ACCUMULATOR_MAX_CAPE:
				return 0.28f;
			case ItemID.RANGING_CAPE:
			case ItemID.RANGING_CAPET:
			case ItemID.MAX_CAPE:
				boolean vorkathsHeadUsed = Boolean.valueOf(configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, "vorkathsHeadUsed"));
				return vorkathsHeadUsed ? 0.2f : 0.28f;
			case ItemID.AVAS_ASSEMBLER:
			case ItemID.AVAS_ASSEMBLER_L:
			case ItemID.ASSEMBLER_MAX_CAPE:
			case ItemID.ASSEMBLER_MAX_CAPE_L:
			case ItemID.MASORI_ASSEMBLER:
			case ItemID.MASORI_ASSEMBLER_L:
			case ItemID.MASORI_ASSEMBLER_MAX_CAPE:
			case ItemID.MASORI_ASSEMBLER_MAX_CAPE_L:
				return 0.2f;
			case ItemID.DIZANAS_MAX_CAPE:
			case ItemID.DIZANAS_MAX_CAPE_L:
			case ItemID.DIZANAS_QUIVER:
			case ItemID.DIZANAS_QUIVER_L:
			case ItemID.BLESSED_DIZANAS_QUIVER:
			case ItemID.BLESSED_DIZANAS_QUIVER_L:
			case ItemID.DIZANAS_QUIVER_UNCHARGED:
			case ItemID.DIZANAS_QUIVER_UNCHARGED_L:
				String configString = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, "dizanasQuiverAmmoSaving");
				int dizanasQuiverAmmoSaving = configString == null ? 0 : Integer.parseInt(configString);
				return dizanasQuiverAmmoSaving == 0 ? 0.2f : dizanasQuiverAmmoSaving == 1 ? 0.28f : 0.4f;
			default:
				// no ammo-saving thing equipped.
				return 1f;
		}
	}

	@Inject
	ConfigManager configManager;

	public Float getCharges(ChargedWeapon weapon) {
		String configString = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, weapon.configKeyName);
		if (configString == null) return null;
		return Float.parseFloat(configString);
	}

	public void setCharges(ChargedWeapon weapon, int charges) {
		setCharges(weapon, charges, true);
	}

	public void setCharges(ChargedWeapon weapon, float charges, boolean logChange) {
		configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, weapon.configKeyName, Math.max(charges, 0));
		if (logChange)
		{
			log.info("set charges for " + weapon + " to " + charges + " (" + configManager.getRSProfileKey() + ")");
		}
	}

	public void addCharges(ChargedWeapon weapon, float change, boolean logChange) {
		Float charges = getCharges(weapon);
		setCharges(weapon, (charges == null ? 0f : charges) + change, logChange);
	}

	public Float getDartsLeft()
	{
		String configString = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, "blowpipeDarts");
		if (configString == null) return null;
		return Float.parseFloat(configString);
	}

	void setDartsLeft(float dartsLeft)
	{
		setDartsLeft(dartsLeft, true);
	}

	private void setDartsLeft(float dartsLeft, boolean logChange)
	{
		configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, "blowpipeDarts", dartsLeft);
		if (logChange)
		{
			log.info("set darts left to " + dartsLeft + " (" + configManager.getRSProfileKey() + ")");
		}
	}

	private void addDartsLeft(float change, boolean logChange) {
		Float dartsLeft = getDartsLeft();
		setDartsLeft((dartsLeft == null ? 0 : dartsLeft) + change, logChange);
	}

	public DartType getDartType()
	{
		String configString = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, "blowpipeDartType");
		if (configString == null) return DartType.UNKNOWN;
		return DartType.valueOf(configString);
	}

	void setDartType(DartType dartType)
	{
		configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, "blowpipeDartType", dartType);
		log.info("set dart type to " + dartType + " (" + configManager.getRSProfileKey() + ")");
	}

	public Float getScalesLeft()
	{
		String configString = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, "blowpipeScales");
		if (configString == null) return null;
		return Float.parseFloat(configString);
	}

	void setScalesLeft(float scalesLeft)
	{
		setScalesLeft(scalesLeft, true);
	}

	private void setScalesLeft(float scalesLeft, boolean logChange)
	{
		configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, "blowpipeScales", scalesLeft);
		if (logChange)
		{
			log.info("set scales left to " + scalesLeft + " (" + configManager.getRSProfileKey() + ")");
		}
	}

	private void addScalesLeft(float change, boolean logChange) {
		Float scalesLeft = getScalesLeft();
		setScalesLeft((scalesLeft == null ? 0 : scalesLeft) + change, logChange);
	}

	@Getter
	private boolean showChargesKeyIsDown = false;

	@Override
	public void keyPressed(KeyEvent e)
	{
		if (config.showOnHotkey().matches(e)) {
			showChargesKeyIsDown = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		if (config.showOnHotkey().matches(e)) {
			showChargesKeyIsDown = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e)
	{

	}

	@Subscribe
	public void onFocusChanged(FocusChanged focusChanged) {
		if (!focusChanged.isFocused()) {
			showChargesKeyIsDown = false;
		}
	}

	@RequiredArgsConstructor
	public enum DartType {
		UNKNOWN(-1, Color.LIGHT_GRAY, null),
		BRONZE(ItemID.BRONZE_DART, new Color(0x6e5727), "bronze"),
		IRON(ItemID.IRON_DART, new Color(0x52504c), "iron"),
		STEEL(ItemID.STEEL_DART, new Color(0x7a7873), "steel"),
		MITHRIL(ItemID.MITHRIL_DART, new Color(0x414f78), "mithril"),
		ADAMANT(ItemID.ADAMANT_DART, new Color(0x417852), "adamant"),
		RUNE(ItemID.RUNE_DART, new Color(0x67e0f5), "rune"),
		AMETHYST(ItemID.AMETHYST_DART, new Color(0xc87dd4), "amethyst"),
		DRAGON(ItemID.DRAGON_DART, new Color(0x3e7877), "dragon"),
		;

		public final int itemId;
		public final Color displayColor;
		public final String checkBlowpipeMessageName;

		public static DartType getDartTypeByName(String group)
		{
			group = group.toLowerCase();
			for (DartType dartType : DartType.values())
			{
				if (dartType.checkBlowpipeMessageName != null && dartType.checkBlowpipeMessageName.equals(group)) {
					return dartType;
				}
			}
			return null;
		}
	}

	public void onMenuOpened2()
	{
		if (!client.isKeyPressed(KeyCode.KC_SHIFT) || config.hideShiftRightClickOptions())
		{
			return;
		}

		MenuEntry[] entries = client.getMenuEntries();
		for (int i = 0; i < entries.length; i++)
		{
			MenuEntry entry = entries[i];
			Widget w = entry.getWidget();

			int itemId;
			if (w != null && WidgetUtil.componentToInterface(w.getId()) == InterfaceID.INVENTORY
				&& "Examine".equals(entry.getOption()) && entry.getIdentifier() == 10)
			{
				itemId = entry.getItemId();
			}
			else if (w != null && WidgetUtil.componentToInterface(w.getId()) == InterfaceID.EQUIPMENT
				&& "Examine".equals(entry.getOption()) && entry.getIdentifier() == 10)
			{
				w = w.getChild(1);
				if (w == null) continue;
				itemId = w.getItemId();
			}
			else
			{
				continue;
			}

			for (ChargedWeapon chargedWeapon : ChargedWeapon.values())
			{
				if (!chargedWeapon.getItemIds().contains(itemId) && !chargedWeapon.getUnchargedIds().contains(itemId))
				{
					continue;
				}

				// I want to insert the menu entry underneath everything else (except "Cancel"), such as the runelite MES left and shift click swap options, and inventory tags, because those are more useful to people.
				MenuEntry submenuEntry = client.createMenuEntry(1)
					.setOption("Weapon charges plugin")
					.setType(MenuAction.RUNELITE_SUBMENU);
				/*
				Set low charge threshold (500)
				Show charge count on item
				[ ] Use default setting
				[ ] Always
				[x] Only when low
				[ ] Never
				 */
				addSubmenu("Set low charge threshold (" + chargedWeapon.getLowCharge(configManager) + ")",
					e -> openChangeLowChargeDialog(chargedWeapon, chargedWeapon.getLowCharge(configManager)),
					submenuEntry);
				addSubmenu(ColorUtil.wrapWithColorTag("Show charge count on item", Color.decode("#ff9040")),
					submenuEntry);
				DisplayWhen displayWhen = chargedWeapon.getDisplayWhen(configManager);
				addSubmenuRadioButtonStyle(displayWhen == USE_DEFAULT, "Use default settings",
					e -> chargedWeapon.setDisplayWhen(configManager, USE_DEFAULT),
					submenuEntry);
				addSubmenuRadioButtonStyle(displayWhen == LOW_CHARGE, "When low",
					e -> chargedWeapon.setDisplayWhen(configManager, LOW_CHARGE),
					submenuEntry);
				addSubmenuRadioButtonStyle(displayWhen == ALWAYS, "Always",
					e -> chargedWeapon.setDisplayWhen(configManager, ALWAYS),
					submenuEntry);
				addSubmenuRadioButtonStyle(displayWhen == NEVER, "Never",
					e -> chargedWeapon.setDisplayWhen(configManager, NEVER),
					submenuEntry);
				if (chargedWeapon == ChargedWeapon.SERPENTINE_HELM) {
					addSubmenu(ColorUtil.wrapWithColorTag("Display style", Color.decode("#ff9040")),
						submenuEntry);
					SerpModes serpMode = getSerpHelmDisplayStyle();
					addSubmenuRadioButtonStyle(serpMode == PERCENT, "Percent",
						e -> setSerpHelmDisplayStyle(PERCENT),
						submenuEntry);
					addSubmenuRadioButtonStyle(serpMode == SCALES, "Scales",
						e -> setSerpHelmDisplayStyle(SCALES),
						submenuEntry);
					addSubmenuRadioButtonStyle(serpMode == BOTH, "Both",
						e -> setSerpHelmDisplayStyle(BOTH),
						submenuEntry);
				}
				chargedWeapon.addMenuEntries(this, submenuEntry);
				break;
			}
		}
	}

	public void setSerpHelmDisplayStyle(SerpModes percent)
	{
		configManager.setConfiguration(CONFIG_GROUP_NAME, "serpentine_helm_display_style", percent);
	}

	public SerpModes getSerpHelmDisplayStyle()
	{
		String serpentine_helm_display_style = configManager.getConfiguration(CONFIG_GROUP_NAME, "serpentine_helm_display_style");
		try
		{
			return SerpModes.valueOf(serpentine_helm_display_style);
		} catch (IllegalArgumentException | NullPointerException e) {
			return PERCENT;
		}
	}

	private void openChangeLowChargeDialog(ChargedWeapon chargedWeapon, int currentLowCharge)
	{
		chatboxPanelManager.openTextInput("Set low charge threshold for " + chargedWeapon.getName() + ", (currently " + currentLowCharge + "):")
			.addCharValidator(c -> "-0123456789".indexOf(c) != -1)
			.onDone((Consumer<String>) (input) -> clientThread.invoke(() ->
			{
				int newLowChargeThreshold;
				try
				{
					newLowChargeThreshold = Integer.parseInt(input);
				} catch (NumberFormatException e) {
					final String message = new ChatMessageBuilder()
						.append(ChatColorType.HIGHLIGHT)
						.append("\"" + input + "\" is not a number.")
						.build();

					chatMessageManager.queue(
						QueuedMessage.builder()
							.type(ChatMessageType.CONSOLE)
							.runeLiteFormattedMessage(message)
							.build());
					return;
				}
				chargedWeapon.setLowCharge(configManager, newLowChargeThreshold);
			}))
			.build();
	}

	void addSubmenu(String option, MenuEntry submenuEntry)
	{
		addSubmenu(option, e -> {}, submenuEntry);
	}

	void addSubmenuRadioButtonStyle(boolean selected, String option, Consumer<MenuEntry> callback, MenuEntry submenuEntry)
	{
		addSubmenu("(" + (selected ? "x" : "  ") + ") " + option,
			callback,
			submenuEntry);
	}

	void addSubmenuCheckboxStyle(boolean selected, String option, Consumer<MenuEntry> callback, MenuEntry submenuEntry)
	{
		addSubmenu("[" + (selected ? "x" : "  ") + "] " + option,
			callback,
			submenuEntry);
	}

	void addSubmenu(String option, Consumer<MenuEntry> callback, MenuEntry submenuEntry)
	{
		client.createMenuEntry(0)
			.setOption(option)
			.setType(MenuAction.RUNELITE)
			.onClick(callback)
			.setParent(submenuEntry);
	}

	private boolean bloodFuryAppliedThisTick = false;
	private final Set<Integer> MELEE_ATTACK_ANIMATIONS = Set.of(8056, 245,376,381,386,390,8288,8290,8289,9471,6118,393,0,395,400,401,406,407,414,419,422,423,428,429,440,1058,1060,1062,1378,1658,1665,1667,2066,2067,2078,2661,3297,3298,3852,4503,5865,7004,7045,7054,7055,7514,7515,7516,7638,7639,7640,7641,7642,7643,7644,7645,8145,9171,1203, 5439, 8640);
	private int checkScytheHitsplats = -1;
	private int scytheHitsplatsSeen = 0;

	@Subscribe
	public void onStatChanged(StatChanged e) {
		Skill skill = e.getSkill();
		if (
			!bloodFuryAppliedThisTick &&
			(
				skill == Skill.ATTACK ||
				skill == Skill.STRENGTH ||
				skill == Skill.DEFENCE && MELEE_ATTACK_ANIMATIONS.contains(client.getLocalPlayer().getAnimation())
			) &&
			getEquippedChargedWeapon(EquipmentInventorySlot.AMULET) == ChargedWeapon.BLOOD_FURY
		) {
			bloodFuryAppliedThisTick = true;
			if (client.getLocalPlayer().getAnimation() == 8056) {
				checkScytheHitsplats = client.getTickCount() + 1;
				scytheHitsplatsSeen = 0;
			}
			addCharges(ChargedWeapon.BLOOD_FURY, -1, false);
		}
	}

}