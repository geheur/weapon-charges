package com.weaponcharges;

import com.weaponcharges.WeaponChargesConfig.DisplayWhen;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.util.Text;

@RequiredArgsConstructor
@Getter
public enum ChargedWeapon
{
	/* template for data collection:
		check (full, <full & >1, 1, 0/empty):
			full: TODO
			>1: TODO
			1: TODO
			empty: TODO

		periodic updates (periodic, empty):
			periodic: TODO
			empty: TODO
			attacking when empty: TODO

		adding (adding by using items on the weapon, adding via right-click option, any other methods):
			using items: TODO
			right-click options: TODO
			other: TODO

		removing (regular removal methods, dropping:
			regular: TODO
			dropping: TODO

		message overlap:
			TODO
	 */

	/* ibans
		check (full, <full & >1, 1, 0/empty):
			full: 2021-08-29 20:24:05 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 11707: GAMEMESSAGE "You have 2500 charges left on the staff."
			>1: 2021-08-29 18:56:25 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 5107: GAMEMESSAGE "You have 116 charges left on the staff."
			1: 2021-08-29 20:03:05 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 9622: GAMEMESSAGE "You have a charge left on the staff." TODO unimplemented.
			empty: 2021-08-29 20:03:14 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 9638: GAMEMESSAGE "You have no charges left on the staff." TODO unimplemented.

		periodic updates (periodic, empty):
			periodic: 2021-08-29 19:53:15 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 8651: GAMEMESSAGE "<col=ef1020>Your staff only has 100 charges left.</col>" TODO unimplemented.
				2021-08-29 19:57:43 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 9092: GAMEMESSAGE "<col=ef1020>Your staff only has 50 charges left.</col>"
				every 100 but also at 50.
			empty: 2021-08-29 20:03:11 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 9632: GAMEMESSAGE "<col=ef1020>Your staff has run out of charges.</col>" TODO unimplemented.
			attacking when empty: 2021-08-29 20:03:18 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 9644: GAMEMESSAGE "You need to recharge your staff to use this spell." TODO unimplemented.

		adding (adding by using items on the weapon, adding via right-click option, any other methods):
			well: 2021-08-29 20:14:23 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 10744: dialog state changed: NpcDialogState{SPRITE, text='You hold the staff above the well and feel the power of<br>Zamorak flow through you.', itemId=12658}. not skippable. TODO unimplemented.
			using items: none
			right-click options: none
			other: none

		removing:
			regular: impossible
			dropping: no charge loss

		message overlap:
			(u) and non-(u) probably both use the same messages. TODO
	 */
	IBANS_STAFF(
		Arrays.asList(ItemID.IBANS_STAFF, ItemID.IBANS_STAFF_U),
		Arrays.asList(708),
		-1 /*120 for regular, 2500 for (u)*/,
		"ibans_staff",
		Collections.emptyList(),
		Collections.emptyList(),
		Collections.emptyList()
	),

	/* Tridents
		Tridents all work the same way, afaik (only tested swap trident and partially seas trident).

		check:
			2021-08-29 18:47:39 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 4235: GAMEMESSAGE "Your weapon has 2,500 charges."
			2021-08-27 23:18:30 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - GAMEMESSAGE "Your weapon has 2,040 charges."
			2021-08-27 22:59:23 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - GAMEMESSAGE "Your weapon has one charge."
			2021-08-27 23:09:33 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - GAMEMESSAGE "Your weapon has no charges."

		periodic updates:
			2021-08-27 23:02:13 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - GAMEMESSAGE "<col=ef1020>Your trident only has 100 charges left!</col>"
		emptying by consuming charge:
			2021-08-27 22:59:08 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - GAMEMESSAGE "<col=ef1020>Your trident has run out of charges.</col>"
			attacking when empty: 2021-08-29 20:04:16 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 9740: GAMEMESSAGE "The weapon has no charges left. You need death runes, chaos runes, fire runes and Zulrah's scales to charge it." TODO unimplemented.

		adding:
			dialog, only non-skippable indication of charges added is the number the player inputs.
			2021-09-02 23:29:44 [AWT-EventQueue-0] INFO  com.weaponcharges.Devtools - 13154: option selected: "123" from NpcDialogState{INPUT, title='How many charges would you like to add? (0 - 2,477)', input='123'}
			(skippable) 2021-08-28 04:00:20 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - dialog state changed: NpcDialogState{SPRITE, text='You add a charge to the weapon.<br>New total: 2016'}
			The above message does not have a comma in the second number if the weapon is at max charges (tested with swamp trident, 2500).
			if adding only one charge because only 1 charge is in your inventory, or the item is 1 charge from full, it skips the dialog and goes straight to the skippable sprite dialog.
			2021-09-02 23:39:44 [Client] INFO  com.weaponcharges.Devtools - 14154: dialog state changed: NpcDialogState{SPRITE, text='You add 123 charges to the weapon.<br>New total: 246', itemId=12899}
			2021-08-29 18:08:48 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 368: dialog state changed: NpcDialogState{SPRITE, text='Your weapon is already fully charged.'}
			2021-08-29 18:13:57 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 882: dialog state changed: NpcDialogState{SPRITE, text='You uncharge your weapon.'}

		removing:
			dialog choice. the dialog that confirms the player's choice is skippable.
			seas trident: 2021-08-29 18:48:38 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 4334: dialog state changed: NpcDialogState{OPTIONS, text='You will NOT get the coins back.', options=[Okay, uncharge it., No, don't uncharge it.]}
			swamp trident: 2021-08-28 03:33:54 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - option selected: "Okay, uncharge it." from NpcDialogState{OPTIONS, text='Really uncharge the trident?', options=[Okay, uncharge it., No, don't uncharge it.]}
			dropping: 2021-08-29 19:03:59 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 5862: option selected: "Drop it." from NpcDialogState{OPTIONS, text='If you drop it, it will lose all its charges.', options=[Drop it., No, don't drop it.]} TODO unimplemented
				(the charge loss happens when you select "Drop it.".

		message overlap: all 4 tridents use the same messages.
	 */
	TRIDENT_OF_THE_SEAS(
		Arrays.asList(ItemID.TRIDENT_OF_THE_SEAS, ItemID.UNCHARGED_TRIDENT),
		Arrays.asList(1167),
		2500,
		"trident_of_the_seas",
		Collections.emptyList(),
		Collections.emptyList(),
		Collections.emptyList()
	),
	TRIDENT_OF_THE_SWAMP(
		Arrays.asList(ItemID.TRIDENT_OF_THE_SWAMP, ItemID.UNCHARGED_TOXIC_TRIDENT),
		Arrays.asList(1167),
		2500,
		"trident_of_the_swamp",
		Collections.emptyList(),
		Collections.emptyList(),
		Collections.emptyList()
	),
	TRIDENT_OF_THE_SEAS_E(
		Arrays.asList(ItemID.TRIDENT_OF_THE_SEAS_E,	ItemID.UNCHARGED_TRIDENT_E),
		Arrays.asList(1167),
		10_000,
		"trident_of_the_seas_e",
		Collections.emptyList(),
		Collections.emptyList(),
		Collections.emptyList()
	),
	TRIDENT_OF_THE_SWAMP_E(
		Arrays.asList(ItemID.TRIDENT_OF_THE_SWAMP_E, ItemID.UNCHARGED_TOXIC_TRIDENT_E),
		Arrays.asList(1167),
		10_000,
		"trident_of_the_swamp_e",
		Collections.emptyList(),
		Collections.emptyList(),
		Collections.emptyList()
	),

	ABYSSAL_TENTACLE(
		Arrays.asList(ItemID.ABYSSAL_TENTACLE),
		Arrays.asList(1658),
		10_000,
		"abyssal_tentacle",
		Arrays.asList(
			ChargesMessage.matcherGroupChargeMessage("Your abyssal tentacle can perform ([\\d,]+) more attacks.", 1)
		),
		Collections.emptyList(),
		Collections.emptyList()
	),
	/* chally
		checking:
			full: unknown. TODO
			>1: 2021-08-29 18:38:45 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 3351: GAMEMESSAGE "Your crystal halberd has 278 charges remaining."
			1 charge: unknown. TODO
			0 charges: not possible, item degrades into seed.

		periodic updates:
			unknown TODO

		adding:
			using crystal shards on it. This does not have a game message. TODO unimplemented

		removing:
			attack/spec - 1 charge.
			reverting TODO unimplemented

		message overlap: none that I'm aware of.
	 */
	CRYSTAL_HALBERD(
		Arrays.asList(ItemID.CRYSTAL_HALBERD),
		Arrays.asList(428, 440, 1203),
		10_000/*TODO is this correct?*/,
		"crystal_halberd",
		Arrays.asList(
			ChargesMessage.matcherGroupChargeMessage("Your crystal halberd has ([\\d,]+) charges remaining.", 1)
		),
		Collections.emptyList(),
		Collections.emptyList()
	),

	/* Tome of fire:
		checking:
			// cannot check empty book, there is no such menu option.
			"Your tome currently holds 6,839 charges."
			"Your tome currently holds one charge."

		periodic updates:
			"Your Tome of Fire is now empty."

		adding:
			using pages on it, or using the "Pages" menu option and the associated dialog, auto-uses all and produces the same chat message as checking.

		removing:
			2021-08-26 16:35:43 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - GAMEMESSAGE "You remove a page from the book. Your tome currently holds 6,839 charges."
			2021-08-26 16:35:51 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - GAMEMESSAGE "You remove 2 pages from the book. Your tome currently holds 6,799 charges."
			2021-08-26 16:36:44 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - GAMEMESSAGE "You empty your book of pages."
			"You remove 299 pages from the book. Your tome currently holds one charge."

		message overlap: probably overlaps with the tome of water.

		The tome of fire needs an additional check for fire spells being cast, which is done in onClientTick by checking for a gfx value.
	 */
	TOME_OF_FIRE(
		Arrays.asList(ItemID.TOME_OF_FIRE),
		Arrays.asList(711, 1162, 727, 1167, 7855),
		20_000,
		"tome_of_fire",
		Arrays.asList(
			ChargesMessage.staticChargeMessage("You empty your book of pages.", 0),
			ChargesMessage.staticChargeMessage("(You remove [\\S]+ pages? from the book. )?Your tome currently holds one charge.", 1),
			ChargesMessage.matcherGroupChargeMessage("(You remove [\\S]+ pages? from the book. )?Your tome currently holds ([\\d,]+) charges.", 2)
		),
		Arrays.asList(
			ChargesMessage.staticChargeMessage("Your Tome of Fire is now empty.", 0)
		),
		Collections.emptyList()
	),
	/* scythe
		check (full, <full & >1, 1, 0/empty):
			full: TODO
			>1: "Your Scythe of vitur has 19,529 charges remaining."
			1: TODO
			empty: TODO

		periodic updates (periodic, empty):
			periodic: TODO
			empty: TODO
			attacking when empty: TODO

		adding (adding by using items on the weapon, adding via right-click option, any other methods):
			using items: (input) "How many sets of 100 charges do you wish to apply? (Up to 173)" TODO
				receipt dialog: (sprite dialog, unknown id) "You apply 17,300 charges to your Scythe of vitur." TODO
			right-click options: TODO
			other: TODO

		removing (regular removal methods, dropping:
			regular: (sprite dialog, unknown id) "If you uncharge your scythe into the well, 17,300<br>charges will be added to the well." TODO
				receipt dialog: (sprite dialog, unknown id) "You uncharge your scythe into the well. It now<br>contains 173 sets of 100 charges." probably redundant.
			dropping: TODO

		message overlap:
			TODO
	 */
	SCYTHE_OF_VITUR(
		Arrays.asList(ItemID.SCYTHE_OF_VITUR), // I do not included the uncharged version as there is a reasonable reason for people to have both a charged and an uncharged scythe. TODO some kind of optional graphic to show when a scythe is uncharged? like a "(u)" that shows up on the item.
		Arrays.asList(8056),
		20_000,
		"scythe_of_vitur",
		Arrays.asList(
			ChargesMessage.matcherGroupChargeMessage("Your Scythe of vitur has ([\\d,]+) charges remaining.", 1)
		),
		Arrays.asList( // TODO one of these would be really good.
		),
		Arrays.asList(
			new ChargesDialogHandler(
				NpcDialogStateMatcher.inputOptionSelected(Pattern.compile("How many sets of 100 charges do you wish to apply\\? \\(Up to ([\\d,]+)\\)"), null),
				(matchers, npcDialogState, optionSelected, plugin) -> {
					String chargeCountString = matchers.getTextMatcher().group(1).replaceAll(",", "");
					int maxChargeCount = Integer.parseInt(chargeCountString);
					int chargesEntered;
					try {
						chargesEntered = Integer.parseInt(optionSelected.replaceAll("k", "000").replaceAll("m", "000000").replaceAll("b", "000000000"));
					} catch (NumberFormatException e) {
						// can happen if the input is empty for example.
						return;
					}

					if (chargesEntered > maxChargeCount) {
						chargesEntered = maxChargeCount;
					}

					plugin.addCharges(get_scythe_circumvent_illegal_self_reference(), chargesEntered * 100, true);
				}
			),
				new ChargesDialogHandler(
				NpcDialogStateMatcher.sprite(Pattern.compile("You apply ([\\d,]+) charges to your Scythe of vitur."), null /* TODO find out what this should be */),
				ChargesDialogHandler.genericSpriteDialogChargesMessage(false, 1)
			),
			new ChargesDialogHandler(
				NpcDialogStateMatcher.spriteOptionSelected(Pattern.compile("If you uncharge your scythe into the well, ([\\d,]+) charges will be added to the well."), null /* TODO find out what this should be */),
				ChargesDialogHandler.genericSpriteDialogUnchargeMessage()
			)
		)
	),
	/* blood fury
		check (full, <full & >1, 1, 0/empty):
			full: "Your Amulet of blood fury will work for 30,000 more hits." TODO unimplemented, copied from screenshot
			>1: "Your Amulet of blood fury will work for 10,000 more hits." TODO unimplemented, copied from screenshot
			1: TODO
			empty: TODO

		periodic updates (periodic, empty):
			periodic: TODO
			empty: TODO
			attacking when empty: TODO

		adding (adding by using items on the weapon, adding via right-click option, any other methods):
			using items: TODO
			right-click options: TODO
			other: TODO
			TODO overcharge warning, different text?

		removing (regular removal methods, dropping:
			regular: TODO
			dropping: TODO

		message overlap:
			TODO
	 */
//	BLOOD_FURY(),
	/* template for data collection:
		check (full, <full & >1, 1, 0/empty):
			full: TODO
			>1: TODO
			1: TODO
			empty: TODO

		periodic updates (periodic, empty):
			periodic: TODO
			empty: TODO
			attacking when empty: TODO

		adding (adding by using items on the weapon, adding via right-click option, any other methods):
			using items: TODO
			right-click options: TODO
			other: TODO

		removing (regular removal methods, dropping:
			regular: TODO
			dropping: TODO

		message overlap:
			TODO
	 */
	/* template for data collection:
		check (full, <full & >1, 1, 0/empty):
			full: TODO
			>1: TODO
			1: TODO
			empty: TODO

		periodic updates (periodic, empty):
			periodic: TODO
			empty: TODO
			attacking when empty: TODO

		adding (adding by using items on the weapon, adding via right-click option, any other methods):
			using items: TODO
			right-click options: TODO
			other: TODO

		removing (regular removal methods, dropping:
			regular: TODO
			dropping: TODO

		message overlap:
			TODO
	 */
	/* template for data collection:
		check (full, <full & >1, 1, 0/empty):
			full: TODO
			>1: TODO
			1: TODO
			empty: TODO

		periodic updates (periodic, empty):
			periodic: TODO
			empty: TODO
			attacking when empty: TODO

		adding (adding by using items on the weapon, adding via right-click option, any other methods):
			using items: TODO
			right-click options: TODO
			other: TODO

		removing (regular removal methods, dropping:
			regular: TODO
			dropping: TODO

		message overlap:
			TODO
	 */
	;

	@Getter
	private static final List<ChargesMessage> nonUniqueCheckChargesRegexes = Arrays.asList(
		// trident
		ChargesMessage.matcherGroupChargeMessage("Your weapon has ([\\d,]+) charges.", 1),
		ChargesMessage.staticChargeMessage("Your weapon has one charge.", 1),
		ChargesMessage.staticChargeMessage("Your weapon has no charges.", 0),

		// ibans
		ChargesMessage.matcherGroupChargeMessage("You have ([\\d,]+) charges left on the staff.", 1)
	);
	@Getter
	private static final List<ChargesMessage> nonUniqueUpdateMessageChargesRegexes = Arrays.asList(
		// trident
		ChargesMessage.matcherGroupChargeMessage(Text.removeTags("<col=ef1020>Your trident only has ([\\d,]+) charges left!</col>"), 1),
		ChargesMessage.staticChargeMessage(Text.removeTags("<col=ef1020>Your trident has run out of charges.</col>"), 0)
	);

	@Getter
	private static final List<ChargesDialogHandler> nonUniqueDialogHandlers = Arrays.asList(
		// trident
		new ChargesDialogHandler(
			NpcDialogStateMatcher.sprite(Pattern.compile("You add [\\S]+ [\\S]+ to the weapon. New total: ([\\d,]+)"), null),
			ChargesDialogHandler.genericSpriteDialogChargesMessage(true, 1)
		),
		new ChargesDialogHandler(
			NpcDialogStateMatcher.sprite(Pattern.compile("Your weapon is already fully charged."), null),
			ChargesDialogHandler.genericSpriteDialogFullChargeMessage()
		),
		new ChargesDialogHandler( // This one is entirely redundant, I think. Haven't checked (e) tridents though wrt the message they show in the uncharging options dialog.
			NpcDialogStateMatcher.sprite(Pattern.compile("You uncharge your weapon."), null),
			ChargesDialogHandler.genericSpriteDialogUnchargeMessage()
		),
		new ChargesDialogHandler(
			NpcDialogStateMatcher.inputOptionSelected(Pattern.compile("How many charges would you like to add\\? \\(0 - ([\\d,]+)\\)"), null),
			ChargesDialogHandler.genericInputChargeMessage()
		),
		new ChargesDialogHandler(
			NpcDialogStateMatcher.optionsOptionSelected(Pattern.compile("You will NOT get the coins back."), null, Pattern.compile("Okay, uncharge it.")),
			(matchers, npcDialogState, optionSelected, plugin) -> {
				plugin.setCharges(plugin.lastUnchargeClickedWeapon, 0);
			}
		),
		new ChargesDialogHandler(
			NpcDialogStateMatcher.optionsOptionSelected(Pattern.compile("Really uncharge the trident?"), null, Pattern.compile("Okay, uncharge it.")),
			(matchers, npcDialogState, optionSelected, plugin) -> {
				plugin.setCharges(plugin.lastUnchargeClickedWeapon, 0);
			}
		),
		new ChargesDialogHandler(
			NpcDialogStateMatcher.optionsOptionSelected(Pattern.compile("If you drop it, it will lose all its charges."), null, Pattern.compile("Drop it.")),
			(matchers, npcDialogState, optionSelected, plugin) -> {
				plugin.setCharges(plugin.lastUnchargeClickedWeapon, 0);
			}
		)
	);

	public final List<Integer> itemIds;
	public final List<Integer> animationIds;
	public final Integer rechargeAmount;
	public final String configKeyName;
	// check messages are those produced by menu actions like "Check". update messages are those produced by the weapon
	// being used (e.g. those that notify you it's empty, or has 100 charges left, etc.).
	// These must be kept separate because the check messages [seem to always] have the charges of the weapon before
	// any attacks the weapon is making that tick, while the update messages have the charges of the weapon after any
	// attacks it makes on that tick.
	private final List<ChargesMessage> checkChargesRegexes;
	private final List<ChargesMessage> updateMessageChargesRegexes;
	private final List<ChargesDialogHandler> dialogHandlers;

	public static ChargedWeapon getChargedWeaponFromId(int itemId)
	{
		for (ChargedWeapon weapon : values())
		{
			if (weapon.getItemIds().contains(itemId))
			{
				return weapon;
			}
		}

		return null;
	}

	public DisplayWhen getDisplayWhen(WeaponChargesConfig config) {
		DisplayWhen displayWhen = invokeMethodWithConfigKey(configKeyName + "_display", config);
		return displayWhen == null ? DisplayWhen.USE_DEFAULT : displayWhen;
	}

	public int getLowCharge(WeaponChargesConfig config)
	{
		Integer lowChargeThreshold = invokeMethodWithConfigKey(configKeyName + "_low_charges_threshold", config);
		return lowChargeThreshold == null ? 100 : lowChargeThreshold;
	}

	private <T> T invokeMethodWithConfigKey(String key, WeaponChargesConfig config)
	{
		for (final Method method : WeaponChargesConfig.class.getMethods())
		{
			if (!method.isDefault()) continue;

			final ConfigItem annotation = method.getAnnotation(ConfigItem.class);

			if (annotation == null) continue;

			if (key.equals(annotation.keyName())) {
				try
				{
					return (T) method.invoke(config);
				}
				catch (IllegalAccessException | InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private static ChargedWeapon get_scythe_circumvent_illegal_self_reference() {
		return SCYTHE_OF_VITUR;
	}
}

	/*
	TODO

	toggle per tracked item.
	show only at low charges.

	Scythe
	Sang
	Arclight
	Craws
	thammarons
	viggoras
	Crystal bow
	saeldor
	fbow
	blessed sara sword
	regular trident
	(e) sea tridents
	(e) swamp trident
	tome of fire

	crystal armour?
	crystal shield?
	crystal tools?
	barrows armour (maybe just show 100/75/50/25).
	 */
