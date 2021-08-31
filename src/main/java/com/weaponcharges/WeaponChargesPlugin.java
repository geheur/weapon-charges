package com.weaponcharges;

import com.google.inject.Provides;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.AnimationID;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.VarPlayer;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import com.weaponcharges.NpcDialogTracker.NpcDialogState.NpcDialogType;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
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
public class WeaponChargesPlugin extends Plugin
{
	public static final String CONFIG_GROUP_NAME = "weaponCharges";
	public static final String DEV_MODE_CONFIG_KEY = "logData";
	private static final int BLOWPIPE_ATTACK_ANIMATION = 5061;

	private ChargedWeapon rechargedWeapon;

	@Inject
	Client client;

	@Inject
	private WeaponChargesItemOverlay itemOverlay;

	@Inject
	private ItemManager itemManager;

	@Inject
	private WeaponChargesConfig config;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private NpcDialogTracker npcDialogTracker;

	private Devtools devtools;

	@Provides
	WeaponChargesConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WeaponChargesConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(itemOverlay);
		if (config.devMode()) enableDevMode();
		npcDialogTracker.reset();
		eventBus.register(npcDialogTracker);
		keyManager.registerKeyListener(npcDialogTracker);
		npcDialogTracker.setStateChangedListener(this::npcDialogStateChanged);
		npcDialogTracker.setOptionSelectedListener(this::optionSelected);
	}

	private void npcDialogStateChanged(NpcDialogTracker.NpcDialogState npcDialogState)
	{
		if (devtools != null && config.devMode()) devtools.npcDialogStateChanged(npcDialogState);

		// TODO if you can calculate the total charges available in the inventory you could get an accurate count on the "add how many charges" dialog, because max charges - max charges addable = current charges.

		if (npcDialogState.type == NpcDialogType.SPRITE) {
			/*
			(skippable) 2021-08-28 04:00:20 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - dialog state changed: NpcDialogState{SPRITE, text='You add a charge to the weapon.<br>New total: 2016'}
			2021-08-29 18:08:48 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 368: dialog state changed: NpcDialogState{SPRITE, text='Your weapon is already fully charged.'}
			2021-08-29 18:13:57 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 882: dialog state changed: NpcDialogState{SPRITE, text='You uncharge your weapon.'}
			 */
			Matcher matcher = Pattern.compile("You add [\\S]+ [\\S]+ to the weapon.<br>New total: ([\\d,]+)").matcher(npcDialogState.text);
			if (matcher.find()) {
				String chargeCountString = matcher.group(1).replaceAll(",", "");
				int charges = Integer.parseInt(chargeCountString);
				ChargedWeapon chargedWeapon = getChargedWeaponFromId(npcDialogState.spriteDialogItemId);
				if (chargedWeapon != null)
				{
					setCharges(chargedWeapon, charges);
				}
			} else if (npcDialogState.text.equals("Your weapon is already fully charged.")) {
				ChargedWeapon chargedWeapon = getChargedWeaponFromId(npcDialogState.spriteDialogItemId);
				if (chargedWeapon != null)
				{
					setCharges(chargedWeapon, chargedWeapon.rechargeAmount);
				}
			} else if (npcDialogState.text.equals("You uncharge your weapon.")) { // This one is entirely redundant, I think.
				ChargedWeapon chargedWeapon = getChargedWeaponFromId(npcDialogState.spriteDialogItemId);
				if (chargedWeapon != null)
				{
					setCharges(chargedWeapon, 0);
				}
			}
		}
	}

	private static final Pattern CHARGES_PATTERN = Pattern.compile("How many charges would you like to add\\? \\(0 - ([\\d,]+)\\)");

	private void optionSelected(NpcDialogTracker.NpcDialogState npcDialogState, String optionSelected)
	{
		if (devtools != null && config.devMode()) devtools.optionSelected(npcDialogState, optionSelected);

		// I don't think adding a single charge by using the items on the weapon is going to be trackable if the user
		// skips the sprite dialog.
		if (npcDialogState.type == NpcDialogType.INPUT && rechargedWeapon != null) {
			Matcher matcher = CHARGES_PATTERN.matcher(npcDialogState.name);
			if (matcher.find()) {
				String chargeCountString = matcher.group(1).replaceAll(",", "");
				int maxChargeCount = Integer.parseInt(chargeCountString);
				int chargesEntered;
				try
				{
					chargesEntered = Integer.parseInt(optionSelected.replaceAll("k", "000").replaceAll("m", "000000").replaceAll("b", "000000000"));
				} catch (NumberFormatException e) {
					// can happen if the input is empty for example.
					return;
				}

				if (chargesEntered > maxChargeCount) chargesEntered = maxChargeCount;

				addCharges(rechargedWeapon, chargesEntered, true);
			}
		} else if (npcDialogState.type == NpcDialogType.OPTIONS) {
			if ((npcDialogState.text.equals("Really uncharge the trident?") || npcDialogState.text.equals("You will NOT get the coins back.")) && optionSelected.equals("Okay, uncharge it.")) {
				// TODO I don't think rechargedWeapon is guaranteed to have the right value here.
				setCharges(rechargedWeapon, 0);
			}
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(itemOverlay);
		disableDevMode();
		eventBus.unregister(npcDialogTracker);
		keyManager.unregisterKeyListener(npcDialogTracker);
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

	private void disableDevMode()
	{
		if (devtools != null) eventBus.unregister(devtools);
	}

	ChargedWeapon getChargedWeaponFromId(int itemId)
	{
		for (ChargedWeapon weapon : ChargedWeapon.values())
		{
			if (weapon.getItemIds().contains(itemId))
			{
				return weapon;
			}
		}

		return null;
	}

	// There are two lists to keep a list of checked weapons not just in the last tick, but in the last 2. I do this because
	// I'm paranoid that someone will somehow check an item without getting a check message, or the check message
	// does not match any regexes for some reason. This can cause the plugin to assign charges to the wrong weapon.
	private List<ChargedWeapon> lastWeaponChecked = new ArrayList<>();
	private List<ChargedWeapon> lastWeaponChecked2 = new ArrayList<>();

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
	    if (event.getMenuOption().equalsIgnoreCase("check")) {
	    	// TODO investigate shift-click.
			if (config.devMode()) log.info("clicked \"check\" on " + event.getMenuTarget());

			if (WidgetInfo.TO_GROUP(event.getParam1()) == WidgetID.EQUIPMENT_GROUP_ID) { // item is equipped.
				int childId = WidgetInfo.TO_CHILD(event.getParam1());
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
					if (chargedWeapon.getItemIds().contains(event.getId()) && chargedWeapon.getCheckChargesRegexes().isEmpty())
					{
						if (config.devMode()) log.info("adding last weapon checked to " + chargedWeapon);
						lastWeaponChecked.add(chargedWeapon);
						break;
					}
				}
			}
		}

	    if (event.getMenuAction() == MenuAction.ITEM_USE_ON_WIDGET_ITEM) {
			rechargedWeapon = getChargedWeaponFromId(event.getId());
			if (config.devMode()) log.info("used item on " + rechargedWeapon + " " + client.getTickCount());
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

		for (ChargedWeapon.ChargesMessage checkMessage : ChargedWeapon.getNonUniqueCheckChargesRegexes())
		{
			Matcher matcher = checkMessage.getPattern().matcher(message);
			if (matcher.find()) {
				ChargedWeapon chargedWeapon = removeLastWeaponChecked();
				if (chargedWeapon != null) {
					setCharges(chargedWeapon, checkMessage.getChargesLeft(matcher));
				} else {
					log.warn("saw check message without having seen an item checked: \"" + message + "\"");
				}
				break;
			}
		}

		for (ChargedWeapon.ChargesMessage checkMessage : ChargedWeapon.getNonUniqueUpdateMessageChargesRegexes())
		{
			Matcher matcher = checkMessage.getPattern().matcher(message);
			if (matcher.find()) {
				int chargeCount = checkMessage.getChargesLeft(matcher);
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

			for (ChargedWeapon.ChargesMessage checkMessage : chargedWeapon.getCheckChargesRegexes())
			{
				Matcher matcher = checkMessage.getPattern().matcher(message);
				if (matcher.find()) {
					setCharges(chargedWeapon, checkMessage.getChargesLeft(matcher));
					break outer_loop;
				}
			}

			for (ChargedWeapon.ChargesMessage checkMessage : chargedWeapon.getUpdateMessageChargesRegexes())
			{
				Matcher matcher = checkMessage.getPattern().matcher(message);
				if (matcher.find()) {
					delayChargeUpdateUntilAfterAnimations.add(() -> setCharges(chargedWeapon, checkMessage.getChargesLeft(matcher)));
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

		return getChargedWeaponFromId(item.getId());
	}

	private void chatMessageBlowpipe(String chatMsg)
	{
// TODO		2021-08-29 14:19:11 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 841: GAMEMESSAGE "Your blowpipe has run out of darts."

// TODO		2021-08-29 14:18:27 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 767: GAMEMESSAGE "Your blowpipe needs to be charged with Zulrah's scales."

		// TODO messages for using scales/darts on a full blowpipe.

		Matcher matcher = DART_AND_SCALE_PATTERN.matcher(chatMsg);

		if (matcher.find())
		{
			System.out.println("matcher: " + matcher.group(1) + " " + matcher.group(2) + " " + matcher.group(3));
			setDartsLeft(Integer.parseInt(matcher.group(2).replace(",", "")));
			setScalesLeft(Integer.parseInt(matcher.group(3).replace(",", "")));
			setDartType(DartType.getDartTypeByName(matcher.group(1)));
		}
	}

	private int checkTomeOfFire = -1; // TODO make this boolean.

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		if (checkTomeOfFire == 0) {
			int graphic = client.getLocalPlayer().getGraphic();
			if (
					graphic == 99 ||
					graphic == 126 ||
					graphic == 129 ||
					graphic == 155 ||
					graphic == 1464
			) {
				// The tome of fire has only one charge update message and it's for emptying, so the nonzero check
				// prevents double charge reduction due to the 1 client tick delay.
				Integer charges = getCharges(ChargedWeapon.TOME_OF_FIRE);
				if (charges != 0)
				{
					setCharges(ChargedWeapon.TOME_OF_FIRE, (charges == null ? 0 : charges) + -1, false);
				}
			}
		}
		checkTomeOfFire--;
	}

	private int lastLocalPlayerAnimationChangedGameTick = -1;
	// I record the animation id so that animation changing plugins change the animation between onAnimationChanged and onGameTick.
	private int lastLocalPlayerAnimationChanged = -1;

	@Subscribe(priority = 10.0f) // I want to get ahead of those pesky animation modifying plugins.
	public void onAnimationChanged(AnimationChanged event)
	{
		final Actor actor = event.getActor();
		if (actor != client.getLocalPlayer()) return;

		lastLocalPlayerAnimationChangedGameTick = client.getTickCount();
		lastLocalPlayerAnimationChanged = actor.getAnimation();
	}

	private int ticks = 0;
	private int ticksInAnimation;
	private int attackStyleVarbit = -1;
	// TODO 2021-08-29 14:22:09 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 1135: GAMEMESSAGE "Darts: <col=007f00>None</col>. Scales: <col=007f00>99 (0.6%)</col>."
	private static final Pattern DART_AND_SCALE_PATTERN = Pattern.compile("Darts: (\\S*)(?: dart)? x ([\\d,]+). Scales: ([\\d,]+) \\(\\d+[.]?\\d%\\).");

	private static final int TICKS_RAPID_PVM = 2;
	private static final int TICKS_RAPID_PVP = 3;
	private static final int TICKS_NORMAL_PVM = 3;
	private static final int TICKS_NORMAL_PVP = 4;
	public static final int MAX_SCALES = 16383;

//	private static int blowpipeHits = 0;
//	private static int blowpipeHitsBySound = 0;
//
//	@Subscribe
//	public void onSoundEffectPlayed(SoundEffectPlayed soundEffectPlayed)
//	{
//		if (soundEffectPlayed.getSoundId() == 2696) {
//			blowpipeHitsBySound++;
//			System.out.println(client.getTickCount() + " blowpipe hits (by sound): " + blowpipeHits + " " + blowpipeHitsBySound);
//		}
//	}
//
	private int lastAnimationStart = 0;

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		// This delay is necessary because equipped items are updated after onAnimationChanged, so with items that share
		// a game message it will not be possible to tell which item the message is for.
		// The order must be: check messages, animation, charge update messages.
		// Runelite's order is: onChatMessage, onAnimationChanged, onGameTick.
		// charge update messages must also be delayed due to equipment slot info not being current in onChatMessage.
		if (lastLocalPlayerAnimationChangedGameTick == client.getTickCount()) checkAnimation();

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

		Player player = client.getLocalPlayer();

		if (player.getAnimation() != BLOWPIPE_ATTACK_ANIMATION)
		{
			return;
		}

		if (ticks == 0) {
			lastAnimationStart = client.getTickCount();
		} else {
			if (client.getTickCount() - lastAnimationStart > ticksInAnimation) {
				ticks = 0;
				lastAnimationStart = client.getTickCount();
			}
		}

		ticks++;
//		System.out.println(client.getTickCount() + " blowpipe: " + ticks + " " + ticksInAnimation);

		if (ticks == ticksInAnimation)
		{
//			System.out.println(client.getTickCount() + " blowpipe hits (animation update): " + ++blowpipeHits + " " + blowpipeHitsBySound);
			consumeBlowpipeCharges();
			ticks = 0;
		}
	}

	private void checkAnimation()
	{
		ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		if (itemContainer == null) return;

		Item weapon = itemContainer.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		int weaponItemId = (weapon == null) ? -1 : weapon.getId();

		Item offhand = itemContainer.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx());
		int offhandItemId = (offhand == null) ? -1 : offhand.getId();

		for (ChargedWeapon chargedWeapon : ChargedWeapon.values()) {
			if (
				(chargedWeapon.getItemIds().contains(weaponItemId) || chargedWeapon.getItemIds().contains(offhandItemId)) &&
					chargedWeapon.animationIds.contains(lastLocalPlayerAnimationChanged))
			{
				if (chargedWeapon == ChargedWeapon.TOME_OF_FIRE) {
					checkTomeOfFire = 1;
				} else {
					addCharges(chargedWeapon, -1, false);
				}
			}
		}
	}

	private void consumeBlowpipeCharges()
	{
		AttractorDefinition attractorDefinition = getAttractorForPlayer();
		addDartsLeft(
			attractorDefinition == null ?
				-1 :
				-1 * (attractorDefinition.getDropToFloorChance() + attractorDefinition.getBreakOnImpactChance()),
			false
		);

		addScalesLeft(2/3f, false);
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if ((attackStyleVarbit == -1 || attackStyleVarbit != client.getVar(VarPlayer.ATTACK_STYLE)) && client.getLocalPlayer() != null)
		{
			attackStyleVarbit = client.getVar(VarPlayer.ATTACK_STYLE);

			if (attackStyleVarbit == 0 || attackStyleVarbit == 3)
			{
				ticksInAnimation = client.getLocalPlayer().getInteracting() instanceof Player ? TICKS_NORMAL_PVP : TICKS_NORMAL_PVM;
			}
			else if (attackStyleVarbit == 1)
			{
				ticksInAnimation = client.getLocalPlayer().getInteracting() instanceof Player ? TICKS_RAPID_PVP : TICKS_RAPID_PVM;
			}
		}
	}

	private AttractorDefinition getAttractorForPlayer()
	{
		int attractorEquippedId = client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.CAPE);
		return AttractorDefinition.getAttractorById(attractorEquippedId);
	}

	@Inject
	ConfigManager configManager;

	public Integer getCharges(ChargedWeapon weapon) {
		String configString = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, weapon.configKeyName);
		if (configString == null) return null;
		return Integer.parseInt(configString);
	}

	private void setCharges(ChargedWeapon weapon, int charges) {
		setCharges(weapon, charges, true);
	}

	private void setCharges(ChargedWeapon weapon, int charges, boolean logChange) {
		configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, weapon.configKeyName, charges);
		if (logChange)
		{
			log.info("set charges for " + weapon + " to " + charges + " (" + configManager.getRSProfileKey() + ")");
		}
	}

	public void addCharges(ChargedWeapon weapon, int change, boolean logChange) {
		Integer charges = getCharges(weapon);
		setCharges(weapon, (charges == null ? 0 : charges) + change, logChange);
	}

	public Float getDartsLeft()
	{
		String configString = configManager.getRSProfileConfiguration(CONFIG_GROUP_NAME, "blowpipeDarts");
		if (configString == null) return null;
		return Float.parseFloat(configString);
	}

	private void setDartsLeft(float dartsLeft)
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
		if (configString == null) return null;
		return DartType.valueOf(configString);
	}

	private void setDartType(DartType dartType)
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

	private void setScalesLeft(float scalesLeft)
	{
		setScalesLeft(scalesLeft, true);
	}

	private void setScalesLeft(float scalesLeft, boolean logChange)
	{
		configManager.setRSProfileConfiguration(CONFIG_GROUP_NAME, "blowpipeScales", scalesLeft);
		if (scalesLeft < 100) {
			System.out.println("went below 100.");
			new Exception().printStackTrace(System.out);
		}
		if (logChange)
		{
			log.info("set scales left to " + scalesLeft + " (" + configManager.getRSProfileKey() + ")");
		}
	}

	private void addScalesLeft(float change, boolean logChange) {
		Float scalesLeft = getScalesLeft();
		setScalesLeft((scalesLeft == null ? 0 : scalesLeft) + change, logChange);
	}

	@RequiredArgsConstructor
	public enum DartType {
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
				if (dartType.checkBlowpipeMessageName.equals(group)) {
					return dartType;
				}
			}
			return null;
		}
	}
}