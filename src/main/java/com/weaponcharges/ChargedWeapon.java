package com.weaponcharges;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;
import static com.weaponcharges.WeaponChargesConfig.*;
import com.weaponcharges.WeaponChargesConfig.DisplayWhen;
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
		"Iban's staff",
		Arrays.asList(ItemID.IBANS_STAFF, ItemID.IBANS_STAFF_U),
		Arrays.asList(708),
		-1 /*120 for regular, 2500 for (u)*/,
		"ibans_staff",
		Collections.emptyList(),
		Collections.emptyList(),
		WeaponChargesConfig::ibansStaffDisplay,
		WeaponChargesConfig::ibansStaffLowChargeThreshold
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
			2021-08-28 03:28:56 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - dialog state changed: NpcDialogState{INPUT, title='How many charges would you like to add? (0 - 2,013)', input=}
			(skippable) 2021-08-28 04:00:20 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - dialog state changed: NpcDialogState{SPRITE, text='You add a charge to the weapon.<br>New total: 2016'}
			The above message does not have a comma in the second number if the weapon is at max charges (tested with swamp trident, 2500).
			if adding only one charge because only 1 charge is in your inventory, or the item is 1 charge from full, it skips the dialog and goes straight to the skippable sprite dialog.
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
	TRIDENT_OF_THE_SEAS("Trident of the seas",
		Arrays.asList(ItemID.TRIDENT_OF_THE_SEAS, ItemID.UNCHARGED_TRIDENT),
		Arrays.asList(1167),
		2500,
		"trident_of_the_seas",
		Collections.emptyList(),
		Collections.emptyList(),
		WeaponChargesConfig::seasTridentDisplay,
		WeaponChargesConfig::seasTridentLowChargeThreshold
	),
	TRIDENT_OF_THE_SWAMP("Trident of the swamp",
		Arrays.asList(ItemID.TRIDENT_OF_THE_SWAMP, ItemID.UNCHARGED_TOXIC_TRIDENT),
		Arrays.asList(1167),
		2500,
		"trident_of_the_swamp",
		Collections.emptyList(),
		Collections.emptyList(),
		WeaponChargesConfig::swampTridentDisplay,
		WeaponChargesConfig::swampTridentLowChargeThreshold
	),
	TRIDENT_OF_THE_SEAS_E("Trident of the seas (e)",
		Arrays.asList(ItemID.TRIDENT_OF_THE_SEAS_E,	ItemID.UNCHARGED_TRIDENT_E),
		Arrays.asList(1167),
		10_000,
		"trident_of_the_seas_e",
		Collections.emptyList(),
		Collections.emptyList(),
		WeaponChargesConfig::seasTridentEDisplay,
		WeaponChargesConfig::seasTridentELowChargeThreshold
	),
	TRIDENT_OF_THE_SWAMP_E("Trident of the swamp (e)",
		Arrays.asList(ItemID.TRIDENT_OF_THE_SWAMP_E, ItemID.UNCHARGED_TOXIC_TRIDENT_E),
		Arrays.asList(1167),
		10_000,
		"trident_of_the_swamp_e",
		Collections.emptyList(),
		Collections.emptyList(),
		WeaponChargesConfig::swampTridentEDisplay,
		WeaponChargesConfig::swampTridentELowChargeThreshold
	),

	ABYSSAL_TENTACLE("Abyssal tentacle",
		Arrays.asList(ItemID.ABYSSAL_TENTACLE),
		Arrays.asList(1658),
		10_000,
		"abyssal_tentacle",
		Arrays.asList(
			ChargesMessage.matcherGroupChargeMessage("Your abyssal tentacle can perform ([\\d,]+) more attacks.", 1)
		),
		Collections.emptyList(),
		WeaponChargesConfig::abyssalTentacleDisplay,
		WeaponChargesConfig::abyssalTentacleLowChargeThreshold
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
	CRYSTAL_HALBERD("Crystal halberd",
		Arrays.asList(ItemID.CRYSTAL_HALBERD),
		Arrays.asList(428, 440, 1203),
		10_000/*TODO is this correct?*/,
		"crystal_halberd",
		Arrays.asList(
			ChargesMessage.matcherGroupChargeMessage("Your crystal halberd has ([\\d,]+) charges remaining.", 1)
		),
		Collections.emptyList(),
		WeaponChargesConfig::crystalHalberdDisplay,
		WeaponChargesConfig::crystalHalberdLowChargeThreshold
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
	TOME_OF_FIRE("Tome of fire",
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
		WeaponChargesConfig::tomeOfFireDisplay,
		WeaponChargesConfig::tomeOfFireLowChargeThreshold
	),
	;

	public final String itemName;
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
	private final Function<WeaponChargesConfig, DisplayWhen> displayWhen;
	private final Function<WeaponChargesConfig, Integer> lowCharges;

	public DisplayWhen getDisplayWhen(WeaponChargesConfig config)
	{
		DisplayWhen specificDisplayWhen = displayWhen.apply(config);
		return DisplayWhenNoDefault.getDisplayWhen(specificDisplayWhen, config.defaultDisplay());
	}

	public int getLowCharge(WeaponChargesConfig config)
	{
		return lowCharges.apply(config);
	}

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

	@RequiredArgsConstructor
	public static class ChargesMessage
	{
		@Getter
		private final Pattern pattern;
		private final Function<Matcher, Integer> chargeLeft;
//		@Getter
//		private final boolean unique;

		public int getChargesLeft(Matcher matcher) {
			return chargeLeft.apply(matcher);
		}

//		public static ChargesMessage staticChargeMessage(String s, int charges) {
//			staticChargeMessage(s, charges, true);
//		}
//
		public static ChargesMessage staticChargeMessage(String s, int charges) {
			return new ChargesMessage(Pattern.compile(s), matcher -> charges);
		}

//		public static ChargesMessage matcherGroupChargeMessage(String s, int group)
//		{
//			matcherGroupChargeMessage(s, group, true);
//		}
//
		public static ChargesMessage matcherGroupChargeMessage(String s, int group)
		{
			return new ChargesMessage(Pattern.compile(s), matcher -> {
					String chargeCountString = matcher.group(group).replaceAll(",", "");
					return Integer.parseInt(chargeCountString);
				}
			);
		}
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
