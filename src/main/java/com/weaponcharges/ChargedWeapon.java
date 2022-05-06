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
	/*
	 * I think my minimum reqs should be: check message >1, periodic update, animation-based reduction, charge, uncharge.
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
		Collections.emptyList(),
		Arrays.asList(708),
		2500 /*120 for regular, 2500 for (u)*/, // TODO fix this for regular staff?
		"ibans_staff",
		Arrays.asList(
			ChargesMessage.matcherGroupChargeMessage("You have ([\\d,]+) charges left on the staff.", 1),
			ChargesMessage.staticChargeMessage("You have a charge left on the staff.", 1),
			ChargesMessage.staticChargeMessage("You have no charges left on the staff.", 0)
		),
		Arrays.asList(
			ChargesMessage.matcherGroupChargeMessage(Text.removeTags("<col=ef1020>Your staff only has ([\\d,]+) charges left.</col>"), 1),
			ChargesMessage.staticChargeMessage(Text.removeTags("<col=ef1020>Your staff has run out of charges.</col>"), 0),
			ChargesMessage.staticChargeMessage("You need to recharge your staff to use this spell.", 0)
		),
		Arrays.asList(
			new ChargesDialogHandler(
				DialogStateMatcher.sprite(Pattern.compile("You hold the staff above the well and feel the power of Zamorak flow through you."), null),
				ChargesDialogHandler.genericSpriteDialogFullChargeMessage()
			)
		)
	),

	/* Tridents
		Tridents all work the same way, afaik (only tested swap trident and partially seas trident).

		check:
			2022-05-04 12:37:05 [Client] INFO  com.weaponcharges.Devtools - 354: GAMEMESSAGE "Your Trident of the swamp (e) has 2,000 charges."
			2022-05-04 12:38:41 [Client] INFO  com.weaponcharges.Devtools - 514: GAMEMESSAGE "Your Trident of the swamp (e) has one charge."
			2022-05-04 12:40:27 [Client] INFO  com.weaponcharges.Devtools - 691: GAMEMESSAGE "Your Trident of the seas (e) has one charge."
			2022-05-04 12:40:36 [Client] INFO  com.weaponcharges.Devtools - 706: GAMEMESSAGE "Your Trident of the seas (e) has 1,001 charges."
			2022-05-04 07:08:09 [Client] INFO  com.weaponcharges.Devtools - 12: GAMEMESSAGE "Your Trident of the swamp has 6 charges."
			2022-05-04 07:09:59 [Client] INFO  com.weaponcharges.Devtools - 196: GAMEMESSAGE "Your Trident of the seas has one charge."
			2022-05-04 07:10:55 [Client] INFO  com.weaponcharges.Devtools - 288: GAMEMESSAGE "Your Trident of the seas has 2 charges."
			2022-05-04 07:13:15 [Client] INFO  com.weaponcharges.Devtools - 521: GAMEMESSAGE "Your Trident of the seas has 100 charges."

			// These are useless because the uncharged version has a different item id anyways.
			2022-05-04 12:36:51 [Client] INFO  com.weaponcharges.Devtools - 332: GAMEMESSAGE "Your Uncharged toxic trident (e) has no charges."
			2022-05-04 12:40:13 [Client] INFO  com.weaponcharges.Devtools - 667: GAMEMESSAGE "Your Uncharged trident (e) has no charges."

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

		message overlap: all 4 tridents use the same messages except for the check messages.
	 */
	TRIDENT_OF_THE_SEAS(
		Arrays.asList(ItemID.TRIDENT_OF_THE_SEAS),
		Arrays.asList(ItemID.UNCHARGED_TRIDENT),
		Arrays.asList(1167),
		2500,
		"trident_of_the_seas",
		Arrays.asList(
			ChargesMessage.matcherGroupChargeMessage("Your Trident of the seas has ([\\d,]+) charges.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the seas has one charge.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the seas has no charges.", 0)
		),
		Collections.emptyList(),
		Collections.emptyList()
	),
	TRIDENT_OF_THE_SWAMP(
		Arrays.asList(ItemID.TRIDENT_OF_THE_SWAMP),
		Arrays.asList(ItemID.UNCHARGED_TOXIC_TRIDENT),
		Arrays.asList(1167),
		2500,
		"trident_of_the_swamp",
		Arrays.asList(
			ChargesMessage.matcherGroupChargeMessage("Your Trident of the swamp has ([\\d,]+) charges.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the swamp has one charge.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the swamp has no charges.", 0)
		),
		Collections.emptyList(),
		Collections.emptyList()
	),
	TRIDENT_OF_THE_SEAS_E(
		Arrays.asList(ItemID.TRIDENT_OF_THE_SEAS_E),
		Arrays.asList(ItemID.UNCHARGED_TRIDENT_E),
		Arrays.asList(1167),
		10_000,
		"trident_of_the_seas_e",
		Arrays.asList(
			ChargesMessage.matcherGroupChargeMessage("Your Trident of the seas \\(e\\) has ([\\d,]+) charges.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the seas \\(e\\) has one charge.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the seas \\(e\\) has no charges.", 0)
		),
		Collections.emptyList(),
		Collections.emptyList()
	),
	TRIDENT_OF_THE_SWAMP_E(
		Arrays.asList(ItemID.TRIDENT_OF_THE_SWAMP_E),
		Arrays.asList(ItemID.UNCHARGED_TOXIC_TRIDENT_E),
		Arrays.asList(1167),
		10_000,
		"trident_of_the_swamp_e",
		Arrays.asList(
			ChargesMessage.matcherGroupChargeMessage("Your Trident of the swamp \\(e\\) has ([\\d,]+) charges.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the swamp \\(e\\) has one charge.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the swamp \\(e\\) has no charges.", 0)
		),
		Collections.emptyList(),
		Collections.emptyList()
	),

	ABYSSAL_TENTACLE(
		Arrays.asList(ItemID.ABYSSAL_TENTACLE),
		Collections.emptyList(),
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
		Collections.emptyList(), // TODO add proper empty halberd ID oh God help me
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

		message overlap: definitely overlaps with the tome of water.

		The tome of fire needs an additional check for fire spells being cast, which is done in onClientTick by checking for a gfx value.
	 */
	TOME_OF_FIRE(
		Arrays.asList(ItemID.TOME_OF_FIRE),
		Arrays.asList(ItemID.TOME_OF_FIRE_EMPTY),
		Arrays.asList(711, 1162, 727, 1167, 7855),
		20_000,
		"tome_of_fire",
		Collections.emptyList(),
		Arrays.asList(
			ChargesMessage.staticChargeMessage("Your Tome of Fire is now empty.", 0)
		),
		Collections.emptyList()
	),
	/* Tome of water:
		checking:
			same as ToF

		periodic updates:
			"Your Tome of Water is now empty."

		adding:
		removing:
			same as ToF

		message overlap: definitely overlaps with the Tome of fire.

		The Tome of water needs an additional check for water and curse spells being cast, which is done in onClientTick by checking for a gfx value.
	 */
	TOME_OF_WATER(
			Arrays.asList(ItemID.TOME_OF_WATER),
			Arrays.asList(ItemID.TOME_OF_WATER_EMPTY),
			Arrays.asList(1161 /*bind/snare/entangle*/, 1162 /*strike/bolt/blast*/, 1163 /*confuse*/, 1164 /*weaken*/, 1165 /*curse/vulnerability*/, 1167 /*wave*/, 1168 /*enfeeble*/, 1169 /*stun*/, 7855 /*surge*/),
			20_000,
			"tome_of_water",
			Collections.emptyList(),
			Arrays.asList(
					ChargesMessage.staticChargeMessage("Your Tome of Water is now empty.", 0)
			),
			Collections.emptyList()
	),
	/* scythe
		check (full, <full & >1, 1, 0/empty):
			full: TODO
			>1: "Your Scythe of vitur has 19,529 charges remaining."
				2022-05-02 14:40:15 [Client] INFO  com.weaponcharges.Devtools - 3388: GAMEMESSAGE "Your Sanguine scythe of vitur has 2,056 charges remaining."
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
		// TODO some kind of optional graphic to show when a scythe is uncharged? like a "(u)" that shows up on the item.
		Arrays.asList(ItemID.SCYTHE_OF_VITUR, ItemID.HOLY_SCYTHE_OF_VITUR, ItemID.SANGUINE_SCYTHE_OF_VITUR),
		Arrays.asList(ItemID.SCYTHE_OF_VITUR_UNCHARGED, ItemID.HOLY_SCYTHE_OF_VITUR_UNCHARGED, ItemID.SANGUINE_SCYTHE_OF_VITUR_UNCHARGED),
		Arrays.asList(8056),
		20_000,
		"scythe_of_vitur",
		Arrays.asList(
			ChargesMessage.matcherGroupChargeMessage("Your (Sanguine s|Holy s|S)cythe of vitur has ([\\d,]+) charges remaining.", 2)
		),
		Arrays.asList( // TODO one of these would be really good.
		),
		Arrays.asList(
			new ChargesDialogHandler(
				DialogStateMatcher.inputOptionSelected(Pattern.compile("How many sets of 100 charges do you wish to apply\\? \\(Up to ([\\d,]+)\\)"), null),
				(matchers, dialogState, optionSelected, plugin) -> {
					String chargeCountString = matchers.getNameMatcher().group(1).replaceAll(",", "");
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
//			new ChargesDialogHandler(
//				DialogStateMatcher.sprite(Pattern.compile("You apply ([\\d,]+) charges to your (Sanguine s|Holy s|S)cythe of vitur."), null /* TODO find out what this should be */),
//				ChargesDialogHandler.genericSpriteDialogChargesMessage(false, 1)
//			),
			new ChargesDialogHandler(
				DialogStateMatcher.spriteOptionSelected(Pattern.compile("If you uncharge your scythe into the well, ([\\d,]+) charges will be added to the well."), null /* TODO find out what this should be */),
				ChargesDialogHandler.genericSpriteDialogUnchargeMessage()
			)
		)
	),
	/* blood fury
		check (full, <full & >1, 1, 0/empty):
			full: "Your Amulet of blood fury will work for 30,000 more hits." TODO unimplemented, copied from screenshot
			>1: GAMEMESSAGE "Your Amulet of blood fury will work for 9,016 more hits." TODO unimplemented
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

		How to track? either xp drop + hitsplats if it's possible to always see all of your own hitsplats, or xp drop + animation.
	 */
//	BLOOD_FURY(),
	/* sang staff
		check (full, <full & >1, 1, 0/empty):
			full: GAMEMESSAGE "Your Sanguinesti staff is already fully charged."
			>1: GAMEMESSAGE "Your Sanguinesti staff has 1,000 charges remaining."
				2022-05-02 14:40:16 [Client] INFO  com.weaponcharges.Devtools - 3390: GAMEMESSAGE "Your Holy sanguinesti staff has 144 charges remaining."
			1: GAMEMESSAGE "Your Sanguinesti staff has 1 charges remaining."
			empty: no option when uncharged

		periodic updates (periodic, empty):
			periodic: GAMEMESSAGE "Your Sanguinesti staff has 200 charges remaining."
			low: "<col=ef1020>Your Sanguinesti staff only has 100 charges left!</col>"
			empty: GAMEMESSAGE "Your Sanguinesti staff has run out of charges."
			attacking when empty: GAMEMESSAGE "Your sanguinesti staff has no charges! You need to charge it with blood runes."

		adding (adding by using items on the weapon, adding via right-click option, any other methods):
			using items:
			right-click options:
			DialogState{INPUT, title='How many charges do you want to apply? (Up to 1,033)', input='1'}
			DialogState{SPRITE, text='You apply 1 charges to your Sanguinesti staff.', itemId=22323}
			DialogState{SPRITE, text='You apply an additional 33 charges to your Sanguinesti<br>staff. It now has 1,033 charges in total.', itemId=22323}
			other:

		removing (regular removal methods, dropping:
			regular: DialogState{OPTIONS, text='Uncharge your staff for all its charges? (regaining 3 blood runes)', options=[Proceed., Cancel.]}
				receipt: DialogState{SPRITE, text='You uncharge your Sanguinesti staff, regaining 3 blood<br>runes in the process.', itemId=22481}
			dropping: not droppable while charged

		message overlap:
			none afaik
	 */
	SANGUINESTI_STAFF(
		Arrays.asList(ItemID.SANGUINESTI_STAFF, ItemID.HOLY_SANGUINESTI_STAFF),
		Arrays.asList(ItemID.SANGUINESTI_STAFF_UNCHARGED, ItemID.HOLY_SANGUINESTI_STAFF_UNCHARGED),
		Arrays.asList(1167),
		20_000,
		"sanguinesti_staff",
		Arrays.asList(
			ChargesMessage.staticChargeMessage("Your (Holy s|S)anguinesti staff is already fully charged.", 20000)
			// Some check messages omitted because they are the same as update messages.
		),
		Arrays.asList(
			ChargesMessage.matcherGroupChargeMessage("Your (Holy s|S)anguinesti staff has ([\\d,]+) charges remaining.", 2),
			ChargesMessage.matcherGroupChargeMessage(Text.removeTags("<col=ef1020>Your (Holy s|S)anguinesti staff only has ([\\d,]+) charges left!</col>"), 2),
			ChargesMessage.staticChargeMessage("Your (Holy s|S)anguinesti staff has run out of charges.", 0)
			// ChargesMessage.staticChargeMessage("Your sanguinesti staff has no charges! You need to charge it with blood runes.", 0) // (sic) sang is not capitalized. bug report sent to os team
		),
		Arrays.asList(
			new ChargesDialogHandler(
				DialogStateMatcher.optionsOptionSelected(Pattern.compile("Uncharge your staff for all its charges\\? \\(regaining [\\d,]+ blood runes\\)"), null, Pattern.compile("Proceed.")),
				(matchers, dialogState, optionSelected, plugin) -> {
					plugin.setCharges(get_sang_circumvent_illegal_self_reference(), 0, true);
				}
			),
			new ChargesDialogHandler(
				DialogStateMatcher.inputOptionSelected(Pattern.compile("How many charges do you want to apply\\? \\(Up to ([\\d,]+)\\)"), null),
				ChargesDialogHandler.genericInputChargeMessage()
			),
			new ChargesDialogHandler(
				DialogStateMatcher.sprite(Pattern.compile("You apply ([\\d,]+) charges to your (Holy s|S)anguinesti staff."), null),
				ChargesDialogHandler.genericSpriteDialogChargesMessage(true, 1)
			),
			new ChargesDialogHandler(
				DialogStateMatcher.sprite(Pattern.compile("You apply an additional ([\\d,]+) charges to your (Holy s|S)anguinesti staff. It now has ([\\d,]+) charges in total."), null),
				ChargesDialogHandler.genericSpriteDialogChargesMessage(true, 2)
			)
		)
	),
	/* arclight
		check (full, <full & >1, 1, 0/empty):
			full: TODO
			>1: "Your arclight has 6397 charges left."
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
	ARCLIGHT(
		Arrays.asList(ItemID.ARCLIGHT),
		Collections.emptyList(),
		Arrays.asList(386, 390),
		10_000,
		"arclight",
		Arrays.asList(
			ChargesMessage.matcherGroupChargeMessage("Your arclight has ([\\d,]+) charges left.", 1)
		),
		Collections.emptyList(),
		Collections.emptyList()
	),
	/* Ether Weapon common
		check (full, <full & >1, 1, 0/empty):
			<weapon> is chainmace, sceptre, or bow (THIS CHECK SECTION ONLY)
			full: "Your <weapon> has 16,000 charges left powering it."
			>1: "Your <weapon> has 666 charges left powering it."
			1: "Your <weapon> has 1 charge left powering it."
			empty: "Your <weapon> has 0 charges left powering it."

		adding (adding by using items on the weapon, adding via right-click option, any other methods):
			using items:
				GAMEMESSAGE "You require at least 1000 revenant ether to activate this weapon."
				GAMEMESSAGE "You use 1000 ether to activate the weapon."
				GAMEMESSAGE "You add a further 16,000 revenant ether to your weapon, giving it a total of 16,000 charges."
			right-click options: n/a
			other: n/a

		periodic updates (periodic, empty):
			periodic:
				GAMEMESSAGE "Your weapon has 1,000 charges remaining."
				GAMEMESSAGE "Your weapon has 500 charges remaining."
				GAMEMESSAGE "<col=ef1020>Your weapon only has 100 charges left.</col>"
				GAMEMESSAGE "<col=ef1020>Your weapon only has 50 charges left.</col>"
			empty: GAMEMESSAGE "<col=ef1020>Your weapon has run out of revenant ether.</col>"

		removing (regular removal methods, dropping:
			uncharge: widget doesn't show in the logger
				Are you sure you want to uncharge it?
				<Weapon> Yes No
				If you uncharge this weapon, all the revenant ether will be returned to your inventory.
			dropping: no drop option while charged
	 */

	/* Craw's bow
		attacking when empty: GAMEMESSAGE "There is not enough revenant ether left powering your bow."

		message overlap:
			see Ether Weapon common
	 */
	CRAWS(
			Arrays.asList(ItemID.CRAWS_BOW),
			Arrays.asList(ItemID.CRAWS_BOW_U),
			Arrays.asList(426),
			16_000,
			"craws_bow",
			Arrays.asList(
					ChargesMessage.matcherGroupChargeMessage("Your bow has ([\\d,]+) charges? left powering it.", 1)
			),
			Arrays.asList(
					ChargesMessage.staticChargeMessage("There is not enough revenant ether left powering your bow.", 0)
			),
			Collections.emptyList()
	),
	/* Vigorra's chainmace
		message overlap:
			see Ether Weapon common
	 */
	VIGGORAS(
			Arrays.asList(ItemID.VIGGORAS_CHAINMACE),
			Arrays.asList(ItemID.VIGGORAS_CHAINMACE_U),
			Arrays.asList(245, 7200),
			16_000,
			"viggoras_chainmace",
			Arrays.asList(
					ChargesMessage.matcherGroupChargeMessage("Your chainmace has ([\\d,]+) charges? left powering it.", 1)
			),
			Collections.emptyList(),
			Collections.emptyList()
	),
	/* Thammaron's sceptre
		message overlap:
			see Ether Weapon common
	 */
	THAMMARONS(
			Arrays.asList(ItemID.THAMMARONS_SCEPTRE),
			Arrays.asList(ItemID.THAMMARONS_SCEPTRE_U),
			Collections.emptyList(),
			16_000,
			"thammarons_sceptre",
			Arrays.asList(
					ChargesMessage.matcherGroupChargeMessage("Your sceptre has ([\\d,]+) charges? left powering it.", 1)
			),
			Collections.emptyList(),
			Collections.emptyList()
	),
	;

	@Getter
	private static final List<ChargesMessage> nonUniqueCheckChargesRegexes = Arrays.asList(
		// ether weapons
		ChargesMessage.matcherGroupChargeMessage("Your weapon has ([\\d,]+) charges.", 1),
		//ChargesMessage.staticChargeMessage("You require at least 1000 revenant ether to activate this weapon.", 0),
		ChargesMessage.staticChargeMessage("You use 1000 ether to activate the weapon.", 0),
		ChargesMessage.matcherGroupChargeMessage("You add (a further )?([\\d,]+) revenant ether to your weapon, giving it a total of ([\\d,]+) charges?.", 3),
		// elemental tomes
		ChargesMessage.matcherGroupChargeMessage("(You remove [\\S]+ pages? from the book. )?Your tome currently holds ([\\d,]+) charges.", 2),
		ChargesMessage.staticChargeMessage("(You remove [\\S]+ pages? from the book. )?Your tome currently holds one charge.", 1),
		ChargesMessage.staticChargeMessage("You empty your book of pages.", 0)
	);
	@Getter
	private static final List<ChargesMessage> nonUniqueUpdateMessageChargesRegexes = Arrays.asList(
		// trident
		ChargesMessage.matcherGroupChargeMessage(Text.removeTags("<col=ef1020>Your trident only has ([\\d,]+) charges left!</col>"), 1),
		ChargesMessage.staticChargeMessage(Text.removeTags("<col=ef1020>Your trident has run out of charges.</col>"), 0),
		// ether weapons
		ChargesMessage.matcherGroupChargeMessage(Text.removeTags("<col=ef1020>Your weapon only has ([\\d,]+) charges left.</col>"), 1),
		ChargesMessage.staticChargeMessage(Text.removeTags("<col=ef1020>Your weapon has run out of revenant ether.</col>"), 0)
	);

	@Getter
	private static final List<ChargesDialogHandler> nonUniqueDialogHandlers = Arrays.asList(
		// trident
		new ChargesDialogHandler(
			DialogStateMatcher.sprite(Pattern.compile("You add [\\S]+ [\\S]+ to the weapon. New total: ([\\d,]+)"), null),
			ChargesDialogHandler.genericSpriteDialogChargesMessage(true, 1)
		),
		new ChargesDialogHandler(
			DialogStateMatcher.sprite(Pattern.compile("Your weapon is already fully charged."), null),
			ChargesDialogHandler.genericSpriteDialogFullChargeMessage()
		),
		new ChargesDialogHandler( // This one is entirely redundant, I think. Haven't checked (e) tridents though wrt the message they show in the uncharging options dialog.
			DialogStateMatcher.sprite(Pattern.compile("You uncharge your weapon."), null),
			ChargesDialogHandler.genericSpriteDialogUnchargeMessage()
		),
		new ChargesDialogHandler(
			DialogStateMatcher.inputOptionSelected(Pattern.compile("How many charges would you like to add\\? \\(0 - ([\\d,]+)\\)"), null),
			ChargesDialogHandler.genericInputChargeMessage()
		),
		new ChargesDialogHandler(
			DialogStateMatcher.optionsOptionSelected(Pattern.compile("You will NOT get the coins back."), null, Pattern.compile("Okay, uncharge it.")),
			(matchers, dialogState, optionSelected, plugin) -> {
				plugin.setCharges(plugin.lastUnchargeClickedWeapon, 0);
			}
		),
		new ChargesDialogHandler(
			DialogStateMatcher.optionsOptionSelected(Pattern.compile("Really uncharge the trident?"), null, Pattern.compile("Okay, uncharge it.")),
			(matchers, dialogState, optionSelected, plugin) -> {
				plugin.setCharges(plugin.lastUnchargeClickedWeapon, 0);
			}
		),
		new ChargesDialogHandler(
			DialogStateMatcher.optionsOptionSelected(Pattern.compile("If you drop it, it will lose all its charges."), null, Pattern.compile("Drop it.")),
			(matchers, dialogState, optionSelected, plugin) -> {
				plugin.setCharges(plugin.lastUnchargeClickedWeapon, 0);
			}
		)
	);

	public final List<Integer> itemIds;
	public final List<Integer> unchargedIds;
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
			if (weapon.getItemIds().contains(itemId) || weapon.getUnchargedIds().contains(itemId))
			{
				return weapon;
			}
		}

		return null;
	}

	public static final String DISPLAY_CONFIG_KEY_SUFFIX = "_display";
	public static final String LOW_CHARGE_CONFIG_KEY_SUFFIX = "_low_charge_threshold";

	public DisplayWhen getDisplayWhen(WeaponChargesConfig config) {
		return invokeMethodWithConfigKey(configKeyName + DISPLAY_CONFIG_KEY_SUFFIX, config);
	}

	public int getLowCharge(WeaponChargesConfig config)
	{
		return invokeMethodWithConfigKey(configKeyName + LOW_CHARGE_CONFIG_KEY_SUFFIX, config);
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
		throw new IllegalArgumentException("That config key doesn't exist: " + key);
	}

	private static ChargedWeapon get_scythe_circumvent_illegal_self_reference() {
		return SCYTHE_OF_VITUR;
	}

	private static ChargedWeapon get_sang_circumvent_illegal_self_reference() {
		return SANGUINESTI_STAFF;
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
	GAMEMESSAGE "Your crystal bow has 2,250 charges remaining."
	saeldor
	fbow
	blessed sara sword
	regular trident
	(e) sea tridents
	(e) swamp trident
	tome of fire

	crystal armour?
	crystal shield?
	GAMEMESSAGE "Your crystal shield has 2,369 charges remaining."
	crystal tools?
	GAMEMESSAGE "Your crystal harpoon has 3,957 charges remaining."
	GAMEMESSAGE "Your saw has 8 charges left." //crystal saw
	barrows armour (maybe just show 100/75/50/25).
	 */
