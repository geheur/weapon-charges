package com.weaponcharges;

import com.weaponcharges.WeaponChargesConfig.DisplayWhen;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.util.Text;

@Getter
public enum ChargedWeapon
{
	// many weapons are handled specially in ways not found in this file.
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
	IBANS_STAFF(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.IBANS_STAFF, ItemID.IBANS_STAFF_U)
		.animationIds(708)
		.name("Iban's staff")
		.rechargeAmount(2500) /*120 for regular, 2500 for (u)*/ // TODO fix this for regular staff?
		.defaultLowChargeThreshold(250)
		.configKeyName("ibans_staff")
		.checkChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("You have ([\\d,]+) charges left on the staff.", 1),
			ChargesMessage.staticChargeMessage("You have a charge left on the staff.", 1),
			ChargesMessage.staticChargeMessage("You have no charges left on the staff.", 0)
		)
		.updateMessageChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage(Text.removeTags("<col=ef1020>Your staff only has ([\\d,]+) charges left.</col>"), 1),
			ChargesMessage.staticChargeMessage(Text.removeTags("<col=ef1020>Your staff has run out of charges.</col>"), 0),
			ChargesMessage.staticChargeMessage("You need to recharge your staff to use this spell.", 0)
		)
		.dialogHandlers(
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
			seas (e): presumably the same as seas.
			swamp trident: 2023-03-10 17:14:02 [Client] INFO  com.weaponcharges.Devtools - 2786: option selected: "Okay, uncharge it." from DialogState{OPTIONS, text='Really uncharge the Trident of the swamp?', options=[Okay, uncharge it., No, don't uncharge it.]}
			swamp (e): just adds " (e)" to it, according to a screenshot I recieved.
			dropping: 2021-08-29 19:03:59 [Client] INFO  n.r.c.plugins.weaponcharges.Devtools - 5862: option selected: "Drop it." from NpcDialogState{OPTIONS, text='If you drop it, it will lose all its charges.', options=[Drop it., No, don't drop it.]} TODO unimplemented
				(the charge loss happens when you select "Drop it.".

		message overlap: all 4 tridents use the same messages except for the check messages.
	 */
	TRIDENT_OF_THE_SEAS(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.TRIDENT_OF_THE_SEAS)
		.unchargedItemIds(ItemID.UNCHARGED_TRIDENT)
		.animationIds(1167)
		.name("Trident of the seas")
		.rechargeAmount(2500)
		.configKeyName("trident_of_the_seas")
		.checkChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("Your Trident of the seas has ([\\d,]+) charges.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the seas has one charge.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the seas has no charges.", 0)
		)
	),
	TRIDENT_OF_THE_SWAMP(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.TRIDENT_OF_THE_SWAMP)
		.unchargedItemIds(ItemID.UNCHARGED_TOXIC_TRIDENT)
		.animationIds(1167)
		.name("Trident of the swamp")
		.rechargeAmount(2500)
		.configKeyName("trident_of_the_swamp")
		.checkChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("Your Trident of the swamp has ([\\d,]+) charges.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the swamp has one charge.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the swamp has no charges.", 0)
		)
		.dialogHandlers(
			new ChargesDialogHandler(
				DialogStateMatcher.optionsOptionSelected(Pattern.compile("Really uncharge the Trident of the swamp?"), null, Pattern.compile("Okay, uncharge it.")),
				ChargesDialogHandler.genericUnchargeDialog()
			)
		)
	),
	TRIDENT_OF_THE_SEAS_E(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.TRIDENT_OF_THE_SEAS_E)
		.unchargedItemIds(ItemID.UNCHARGED_TRIDENT_E)
		.animationIds(1167)
		.name("Trident of the seas (e)")
		.rechargeAmount(10_000)
		.configKeyName("trident_of_the_seas_e")
		.checkChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("Your Trident of the seas \\(e\\) has ([\\d,]+) charges.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the seas \\(e\\) has one charge.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the seas \\(e\\) has no charges.", 0)
		)
	),
	TRIDENT_OF_THE_SWAMP_E(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.TRIDENT_OF_THE_SWAMP_E)
		.unchargedItemIds(ItemID.UNCHARGED_TOXIC_TRIDENT_E)
		.animationIds(1167)
		.name("Trident of the swamp (e)")
		.rechargeAmount(10_000)
		.configKeyName("trident_of_the_swamp_e")
		.checkChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("Your Trident of the swamp \\(e\\) has ([\\d,]+) charges.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the swamp \\(e\\) has one charge.", 1),
			ChargesMessage.staticChargeMessage("Your Trident of the swamp \\(e\\) has no charges.", 0)
		)
		.dialogHandlers(
			new ChargesDialogHandler(
				DialogStateMatcher.optionsOptionSelected(Pattern.compile("Really uncharge the Trident of the swamp (e)?"), null, Pattern.compile("Okay, uncharge it.")),
				ChargesDialogHandler.genericUnchargeDialog()
			)
		)
	),

	ABYSSAL_TENTACLE(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.ABYSSAL_TENTACLE, ItemID.ABYSSAL_TENTACLE_OR)
		.animationIds(1658)
		.name("Abyssal tentacle")
		.rechargeAmount(10_000)
		.configKeyName("abyssal_tentacle")
		.checkChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("Your abyssal tentacle can perform ([\\d,]+) more attacks.", 1)
		)
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
	CRYSTAL_HALBERD(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.CRYSTAL_HALBERD)
		.unchargedItemIds() // TODO add proper empty halberd ID oh God help me
		.animationIds(428, 440, 1203)
		.name("Crystal halberd")
		.rechargeAmount(10_000/*TODO is this correct?*/)
		.defaultLowChargeThreshold(25)
		.configKeyName("crystal_halberd")
		.checkChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("Your crystal halberd has ([\\d,]+) charges remaining.", 1)
		)
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
	TOME_OF_FIRE(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.TOME_OF_FIRE)
		.unchargedItemIds(ItemID.TOME_OF_FIRE_EMPTY)
		.animationIds(711, 1162, 727, 1167, 7855)
		.name("Tome of fire")
		.rechargeAmount(20_000)
		.configKeyName("tome_of_fire")
		.updateMessageChargesRegexes(
			ChargesMessage.staticChargeMessage("Your Tome of Fire is now empty.", 0)
		)
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
	TOME_OF_WATER(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.TOME_OF_WATER)
		.unchargedItemIds(ItemID.TOME_OF_WATER_EMPTY)
		.animationIds(1161 /*bind/snare/entangle*/, 1162 /*strike/bolt/blast*/, 1163 /*confuse*/, 1164 /*weaken*/, 1165 /*curse/vulnerability*/, 1167 /*wave*/, 1168 /*enfeeble*/, 1169 /*stun*/, 7855 /*surge*/)
		.name("Tome of water")
		.rechargeAmount(20_000)
		.configKeyName("tome_of_water")
		.updateMessageChargesRegexes(
			ChargesMessage.staticChargeMessage("Your Tome of Water is now empty.", 0)
		)
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
	SCYTHE_OF_VITUR(new ChargedWeaponBuilder()
		// TODO some kind of optional graphic to show when a scythe is uncharged? like a "(u)" that shows up on the item.
		.chargedItemIds(ItemID.SCYTHE_OF_VITUR, ItemID.HOLY_SCYTHE_OF_VITUR, ItemID.SANGUINE_SCYTHE_OF_VITUR)
		.unchargedItemIds(ItemID.SCYTHE_OF_VITUR_UNCHARGED, ItemID.HOLY_SCYTHE_OF_VITUR_UNCHARGED, ItemID.SANGUINE_SCYTHE_OF_VITUR_UNCHARGED)
		.animationIds(8056)
		.name("Scythe of vitur")
		.rechargeAmount(20_000)
		.configKeyName("scythe_of_vitur")
		.checkChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("Your (Sanguine s|Holy s|S)cythe of vitur has ([\\d,]+) charges remaining.", 2)
		)
		.updateMessageChargesRegexes( // TODO one of these would be really good.
		)
		.dialogHandlers(
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
	SANGUINESTI_STAFF(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.SANGUINESTI_STAFF, ItemID.HOLY_SANGUINESTI_STAFF)
		.unchargedItemIds(ItemID.SANGUINESTI_STAFF_UNCHARGED, ItemID.HOLY_SANGUINESTI_STAFF_UNCHARGED)
		.animationIds(1167)
		.name("Sanguinesti staff")
		.rechargeAmount(20_000)
		.configKeyName("sanguinesti_staff")
		.checkChargesRegexes(
			ChargesMessage.staticChargeMessage("Your (Holy s|S)anguinesti staff is already fully charged.", 20000)
			// Some check messages omitted because they are the same as update messages.
		)
		.updateMessageChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("Your (Holy s|S)anguinesti staff has ([\\d,]+) charges remaining.", 2),
			ChargesMessage.matcherGroupChargeMessage(Text.removeTags("<col=ef1020>Your (Holy s|S)anguinesti staff only has ([\\d,]+) charges left!</col>"), 2),
			ChargesMessage.staticChargeMessage("Your (Holy s|S)anguinesti staff has run out of charges.", 0)
			// ChargesMessage.staticChargeMessage("Your sanguinesti staff has no charges! You need to charge it with blood runes.", 0) // (sic) sang is not capitalized. bug report sent to os team
		)
		.dialogHandlers(
			new ChargesDialogHandler(
				DialogStateMatcher.optionsOptionSelected(Pattern.compile("Uncharge your staff for all its charges\\? \\(regaining [\\d,]+ blood runes\\)"), null, Pattern.compile("Proceed.")),
				ChargesDialogHandler.genericUnchargeDialog()
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
				ChargesDialogHandler.genericSpriteDialogChargesMessage(true, 3)
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
	ARCLIGHT(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.ARCLIGHT)
		.animationIds(386, 390)
		.name("Arclight")
		.rechargeAmount(10_000)
		.configKeyName("arclight")
		.checkChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("Your arclight has ([\\d,]+) charges left.", 1)
		)
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
	CRAWS(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.CRAWS_BOW)
		.unchargedItemIds(ItemID.CRAWS_BOW_U)
		.animationIds(426)
		.name("Craw's bow")
		.rechargeAmount(16_000)
		.configKeyName("craws_bow")
	),

	WEBWEAVER(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.WEBWEAVER_BOW_27655)
		.unchargedItemIds(ItemID.WEBWEAVER_BOW_U_27652)
		.name("Webweaver bow")
		.animationIds(426)
		.rechargeAmount(16_000)
		.configKeyName("webweaver_bow")
	),
	/* Vigorra's chainmace
		message overlap:
			see Ether Weapon common
	 */
	VIGGORAS(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.VIGGORAS_CHAINMACE)
		.unchargedItemIds(ItemID.VIGGORAS_CHAINMACE_U)
		.animationIds(245, 7200)
		.name("Viggora's chainmace")
		.rechargeAmount(16_000)
		.configKeyName("viggoras_chainmace")
	),

	URSINE (new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.URSINE_CHAINMACE_27660)
		.unchargedItemIds(ItemID.URSINE_CHAINMACE_U_27657)
		.name("Ursine chainmace")
		.animationIds(245, 7200)
		.rechargeAmount(16_000)
		.configKeyName("ursine_chainmace")
	),

	/* Thammaron's sceptre
		message overlap:
			see Ether Weapon common
	 */
	THAMMARONS(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.THAMMARONS_SCEPTRE, ItemID.THAMMARONS_SCEPTRE_A)
		.unchargedItemIds(ItemID.THAMMARONS_SCEPTRE_U, ItemID.THAMMARONS_SCEPTRE_AU)
		.name("Thammaron's sceptre")
		.animationIds(1167)
		.rechargeAmount(16_000)
		.configKeyName("thammarons_sceptre")
	),
	ACCURSED(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.ACCURSED_SCEPTRE_27665)
		.unchargedItemIds(ItemID.ACCURSED_SCEPTRE_U_27662)
		.name("Accursed sceptre")
		.animationIds(1167)
		.rechargeAmount(16_000)
		.configKeyName("accursed_sceptre")
	),
	/*
	check:
2022-06-12 20:17:48 [Client] INFO  com.weaponcharges.Devtools - 939: GAMEMESSAGE "Your crystal bow has 299 charges remaining."
	update:
		there may be no message; I didn't see one at 100 charges.
	 */
	CRYSTAL_BOW(new ChargedWeaponBuilder() // crystal bow, for ctrl-f
		.chargedItemIds(ItemID.CRYSTAL_BOW)
		.animationIds(426)
		.name("Crystal bow")
		.configKeyName("crystal_bow")
		.checkChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("Your crystal bow has ([\\d,]+) charges remaining.", 1)
		)
	),
	/*
	check:
2022-06-07 12:47:57 [Client] INFO  com.weaponcharges.Devtools - 76: GAMEMESSAGE "Your bow of Faerdhinen has 180 charges remaining."
	update:
2022-06-07 18:53:09 [Client] INFO  com.weaponcharges.Devtools - 3353: GAMEMESSAGE "<col=ff0000>Your bow of Faerdhinen has 100 charges remaining.</col>"
	 */
	BOW_OF_FAERDHINEN(new ChargedWeaponBuilder() // bofa bowfa, for ctrl-f :)
		.chargedItemIds(ItemID.BOW_OF_FAERDHINEN)
		.unchargedItemIds(ItemID.BOW_OF_FAERDHINEN_INACTIVE)
		.animationIds(426)
		.name("Bow of faerdhinen")
		.configKeyName("bow_of_faerdhinen")
		.checkChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("Your bow of Faerdhinen has ([\\d,]+) charges remaining.", 1)
		)
		.updateMessageChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage(Text.removeTags("<col=ff0000>Your bow of Faerdhinen has ([\\d,]+) charges remaining.</col>"), 1)
		)
	),
	/*
	crystal armor
	check:
2022-06-07 12:47:59 [Client] INFO  com.weaponcharges.Devtools - 78: GAMEMESSAGE "Your crystal helm has 1,011 charges remaining."
2022-06-07 12:47:59 [Client] INFO  com.weaponcharges.Devtools - 79: GAMEMESSAGE "Your crystal body has 996 charges remaining."
2022-06-07 12:48:00 [Client] INFO  com.weaponcharges.Devtools - 81: GAMEMESSAGE "Your crystal legs has 982 charges remaining."
	 */
	CRYSTAL_HELM(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.CRYSTAL_HELM, ItemID.CRYSTAL_HELM_27705, ItemID.CRYSTAL_HELM_27717, ItemID.CRYSTAL_HELM_27729, ItemID.CRYSTAL_HELM_27741, ItemID.CRYSTAL_HELM_27753, ItemID.CRYSTAL_HELM_27765, ItemID.CRYSTAL_HELM_27777)
		.unchargedItemIds(ItemID.CRYSTAL_HELM_INACTIVE, ItemID.CRYSTAL_HELM_INACTIVE_27707, ItemID.CRYSTAL_HELM_INACTIVE_27719, ItemID.CRYSTAL_HELM_INACTIVE_27731, ItemID.CRYSTAL_HELM_INACTIVE_27743, ItemID.CRYSTAL_HELM_INACTIVE_27755, ItemID.CRYSTAL_HELM_INACTIVE_27767, ItemID.CRYSTAL_HELM_INACTIVE_27779)
		.name("Crystal armour")
		.configKeyName("crystal_helm")
		.settingsConfigKey("crystal_armour")
		.checkChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("Your crystal helm has ([\\d,]+) charges remaining.", 1)
		)
	),
	CRYSTAL_BODY(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.CRYSTAL_BODY, ItemID.CRYSTAL_BODY_27697, ItemID.CRYSTAL_BODY_27709, ItemID.CRYSTAL_BODY_27721, ItemID.CRYSTAL_BODY_27733, ItemID.CRYSTAL_BODY_27745, ItemID.CRYSTAL_BODY_27757, ItemID.CRYSTAL_BODY_27769)
		.unchargedItemIds(ItemID.CRYSTAL_BODY_INACTIVE, ItemID.CRYSTAL_BODY_INACTIVE_27699, ItemID.CRYSTAL_BODY_INACTIVE_27711, ItemID.CRYSTAL_BODY_INACTIVE_27723, ItemID.CRYSTAL_BODY_INACTIVE_27735, ItemID.CRYSTAL_BODY_INACTIVE_27747, ItemID.CRYSTAL_BODY_INACTIVE_27759, ItemID.CRYSTAL_BODY_INACTIVE_27771)
		.name("Crystal armour")
		.configKeyName("crystal_body")
		.settingsConfigKey("crystal_armour")
		.checkChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("Your crystal body has ([\\d,]+) charges remaining.", 1)
		)
	),
	CRYSTAL_LEGS(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.CRYSTAL_LEGS, ItemID.CRYSTAL_LEGS_27701, ItemID.CRYSTAL_LEGS_27713, ItemID.CRYSTAL_LEGS_27725, ItemID.CRYSTAL_LEGS_27737, ItemID.CRYSTAL_LEGS_27749, ItemID.CRYSTAL_LEGS_27761, ItemID.CRYSTAL_LEGS_27773)
		.unchargedItemIds(ItemID.CRYSTAL_LEGS_INACTIVE, ItemID.CRYSTAL_LEGS_INACTIVE_27703, ItemID.CRYSTAL_LEGS_INACTIVE_27715, ItemID.CRYSTAL_LEGS_INACTIVE_27727, ItemID.CRYSTAL_LEGS_INACTIVE_27739, ItemID.CRYSTAL_LEGS_INACTIVE_27751, ItemID.CRYSTAL_LEGS_INACTIVE_27763, ItemID.CRYSTAL_LEGS_INACTIVE_27775)
		.name("Crystal armour")
		.configKeyName("crystal_legs")
		.settingsConfigKey("crystal_armour")
		.checkChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("Your crystal legs has ([\\d,]+) charges remaining.", 1)
		)
	),
	/* Serpentine Helmet:
	degradation mechanics:
		-10 Scales when taking or dealing damage, and it has been more than 90 ticks (i.e. minimum 91) since the last time scales were removed.
		- taking or dealing damage is generally approximated by hitsplats dealt to or dealt by the player.
			- ranged attacks cause the helmet to degrade sometimes before the hitsplat it seems. This is not taken into account by this plugin.
			- splashes do not degrade it.
			- unknown how enemy ranged attacks and splashes interact with this.
		- the 90-tick timer is shared by barrows items - e.g. you could avoid all scale loss by having barrows items equipped and unequipping the serp helm at the right time.

    notes:
        Number = ([\\d,]+)
        Percent = \\(\\d+[.]?\\d%\\)

    check (full, <full & >1, 1, 0/empty):
        full: 2022-06-20 15:31:21 [Client] INFO  com.weaponcharges.Devtools - 562: GAMEMESSAGE "Scales: <col=007f00>11,000 (100.0%)</col>"
        >1: 2022-06-20 15:32:14 [Client] INFO  com.weaponcharges.Devtools - 650: GAMEMESSAGE "Scales: <col=007f00>5 (0.1%)</col>"
        1: 2022-06-20 15:33:02 [Client] INFO  com.weaponcharges.Devtools - 730: GAMEMESSAGE "Scales: <col=007f00>1 (0.1%)</col>"
        empty: None

    periodic updates (periodic, empty):
        periodic: TODO
        empty: 2022-06-20 18:19:29 [Client] INFO  com.weaponcharges.Devtools - 8380: GAMEMESSAGE "Your serpentine helm has run out of Zulrah's scales."
        attacking when empty: TODO

    adding (adding by using items on the weapon, adding via right-click option, any other methods):
        using items: 2022-06-20 15:40:12 [Client] INFO  com.weaponcharges.Devtools - 1438: GAMEMESSAGE "Scales: <col=007f00>5 (0.1%)</col>"
        right-click options: None
        other: None

    removing (regular removal methods, dropping):
        regular: None
        dropping: None

    message overlap:
        TODO
	*/
	SERPENTINE_HELM(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.SERPENTINE_HELM, ItemID.TANZANITE_HELM, ItemID.MAGMA_HELM)
		.unchargedItemIds(ItemID.SERPENTINE_HELM_UNCHARGED, ItemID.TANZANITE_HELM_UNCHARGED, ItemID.MAGMA_HELM_UNCHARGED)
		.name("Serpentine helm")
		.rechargeAmount(11_000)
		.configKeyName("serpentine_helm")
		.checkChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage(Text.removeTags("^Scales: <col=007f00>([\\d,]+) (\\(\\d+[.]?\\d%\\))</col>$"), 1)
		)
		.updateMessageChargesRegexes(
			ChargesMessage.staticChargeMessage("Your serpentine helm has run out of Zulrah's scales.", 0),
			ChargesMessage.staticChargeMessage("Your tanzanite helm has run out of Zulrah's scales.", 0),
			ChargesMessage.staticChargeMessage("Your magma helm has run out of Zulrah's scales.", 0)
		)
		.dialogHandlers(
			new ChargesDialogHandler(
				DialogStateMatcher.inputOptionSelected(Pattern.compile("How many scales would you like to use\\? \\(0 - ([\\d,]+)\\)"), null),
				ChargesDialogHandler.genericInputChargeMessage()
			)
		)
	),
	/* Tumeken's shadow
		https://github.com/geheur/weapon-charges/issues/14 log here.
		check (full, <full & >1, 1, 0/empty):
			full:
			>1: GAMEMESSAGE "Tumeken's shadow has 99 charges remaining."
			1:
			empty: GAMEMESSAGE "Tumeken's Shadow has no charges! You need to charge it with soul runes and chaos runes." // TODO is this one actually from checking?

		periodic updates (periodic, empty):
			periodic: GAMEMESSAGE "Tumeken's shadow has 200 charges remaining."
			low: GAMEMESSAGE "<col=e00a19>Tumeken's shadow only has 100 charges left!</col>"
			empty: GAMEMESSAGE "Tumeken's shadow has run out of charges."

			TODO check used on item id to see if he used it on an uncharged staff.

		adding (adding by using items on the weapon, adding via right-click option, any other methods):
			using items:
234589: used 566 on 27277
234590: dialog state changed: DialogState{INPUT, title='How many charges do you want to apply? (Up to 994)', input=''}
234592: option selected: "201" from DialogState{INPUT, title='How many charges do you want to apply? (Up to 994)', input=''}
234592: dialog state changed: DialogState{NO_DIALOG}
234593: dialog state changed: DialogState{SPRITE, text='You apply 201 charges to your Tumeken's shadow.', itemId=27275}
234594: option selected: "null" from DialogState{SPRITE, text='You apply 201 charges to your Tumeken's shadow.', itemId=27275}
234595: dialog state changed: DialogState{NO_DIALOG}

234643: used 566 on 27275
234643: dialog state changed: DialogState{INPUT, title='How many charges do you want to apply? (Up to 793)', input=''}
234645: dialog state changed: DialogState{INPUT, title='How many charges do you want to apply? (Up to 793)', input='301'}
234646: option selected: "301" from DialogState{INPUT, title='How many charges do you want to apply? (Up to 793)', input='301'}
234646: dialog state changed: DialogState{SPRITE, text='You apply an additional 301 charges to your<br>Tumeken's shadow. It now has 498 charges in total.', itemId=27275}
234648: dialog state changed: DialogState{NO_DIALOG}

234692: dialog state changed: DialogState{SPRITE, text='You apply 1 charges to your Tumeken's shadow.', itemId=27275}
			right-click options:
			// TODO there is a "Charge" option but I haven't seen it used.
			other:

		removing (regular removal methods, dropping:
234582: dialog state changed: DialogState{OPTIONS, text='Uncharge all the charges from your staff?', options=[Proceed., Cancel.]}
234584: option selected: "Proceed." from DialogState{OPTIONS, text='Uncharge all the charges from your staff?', options=[Proceed., Cancel.]}
234584: dialog state changed: DialogState{SPRITE, text='You uncharge your Tumeken's shadow, regaining 198<br>soul runes and 495 chaos runes in the process.', itemId=27277}
234586: option selected: "null" from DialogState{SPRITE, text='You uncharge your Tumeken's shadow, regaining 198<br>soul runes and 495 chaos runes in the process.', itemId=27277}
234586: dialog state changed: DialogState{NO_DIALOG}
	 */
	TUMEKENS_SHADOW(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.TUMEKENS_SHADOW)
		.unchargedItemIds(ItemID.TUMEKENS_SHADOW_UNCHARGED)
		.animationIds(9493)
		.name("Tumeken's shadow")
		.rechargeAmount(20_000)
		.configKeyName("tumekens_shadow")
		.checkChargesRegexes(
			ChargesMessage.staticChargeMessage("Your Tumeken's shadow is already fully charged.", 20000) // I guessed this one.
			// Some check messages omitted because they are the same as update messages.
		)
		.updateMessageChargesRegexes(
			ChargesMessage.matcherGroupChargeMessage("Tumeken's shadow has ([\\d,]+) charges remaining.", 1),
			ChargesMessage.matcherGroupChargeMessage(Text.removeTags("<col=e00a19>Tumeken's shadow only has ([\\d,]+) charges left!</col>"), 1),
			ChargesMessage.staticChargeMessage("Tumeken's shadow has run out of charges.", 0)
		)
		.dialogHandlers(
			new ChargesDialogHandler(
				DialogStateMatcher.optionsOptionSelected(Pattern.compile("Uncharge all the charges from your staff?"), null, Pattern.compile("Proceed.")),
				ChargesDialogHandler.genericUnchargeDialog()
			),
			new ChargesDialogHandler(
				DialogStateMatcher.inputOptionSelected(Pattern.compile("How many charges do you want to apply\\? \\(Up to ([\\d,]+)\\)"), null),
				ChargesDialogHandler.genericInputChargeMessage()
			),
			new ChargesDialogHandler(
				DialogStateMatcher.sprite(Pattern.compile("You apply ([\\d,]+) charges to your Tumeken's shadow."), null),
				ChargesDialogHandler.genericSpriteDialogChargesMessage(true, 1)
			),
			new ChargesDialogHandler(
				DialogStateMatcher.sprite(Pattern.compile("You apply an additional ([\\d,]+) charges to your Tumeken's shadow. It now has ([\\d,]+) charges in total."), null),
				ChargesDialogHandler.genericSpriteDialogChargesMessage(true, 2)
			)
		)
	),
	/**
	 * Only used to access settings. Lots of things in the blowpipe are handled specially because it holds two kinds of
	 * charges: darts and scales.
	 */
	TOXIC_BLOWPIPE(new ChargedWeaponBuilder()
		.chargedItemIds(ItemID.TOXIC_BLOWPIPE)
		.name("Toxic blowpipe")
		.settingsConfigKey("blowpipe")
	),
	;

	public static final List<ChargedWeapon> CRYSTAL_SHARD_RECHARGABLE_ITEMS = Arrays.asList(CRYSTAL_BOW, CRYSTAL_HELM, CRYSTAL_BODY, CRYSTAL_LEGS, BOW_OF_FAERDHINEN, CRYSTAL_HALBERD);

	@Getter
	private static final List<ChargesMessage> nonUniqueCheckChargesRegexes = Arrays.asList(
		// ether weapons
		ChargesMessage.matcherGroupChargeMessage("Your weapon has ([\\d,]+) charges.", 1),
		ChargesMessage.matcherGroupChargeMessage("Your bow has ([\\d,]+) charges? left powering it.", 1),
		ChargesMessage.matcherGroupChargeMessage("Your chainmace has ([\\d,]+) charges? left powering it.", 1),
		ChargesMessage.matcherGroupChargeMessage("Your sceptre has ([\\d,]+) charges? left powering it.", 1),
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
		ChargesMessage.staticChargeMessage(Text.removeTags("<col=ef1020>Your weapon has run out of revenant ether.</col>"), 0),
		ChargesMessage.staticChargeMessage("There is not enough revenant ether left powering your bow.", 0)
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
			ChargesDialogHandler.genericUnchargeDialog()
		),
		new ChargesDialogHandler(
			DialogStateMatcher.optionsOptionSelected(Pattern.compile("If you drop it, it will lose all its charges."), null, Pattern.compile("Drop it.")),
			ChargesDialogHandler.genericUnchargeDialog()
		),
		// Crystal shard recharging.
//		2022-06-13 09:38:09 [Client] INFO  com.weaponcharges.Devtools - 25: dialog state changed: DialogState{INPUT, title='How many shards do you wish to add? (0 - 5)', input=''}
		new ChargesDialogHandler(
			DialogStateMatcher.inputOptionSelected(Pattern.compile("How many shards do you wish to add\\? \\(0 - ([\\d,]+)\\)"), null),
			ChargesDialogHandler.genericInputChargeMessage(100)
		)
	);

	private static class ChargedWeaponBuilder {
		List<Integer> chargedItemIds = Collections.emptyList();
		public ChargedWeaponBuilder chargedItemIds(Integer... chargedItemIds) {
			this.chargedItemIds = Arrays.asList(chargedItemIds);
			return this;
		}
		List<Integer> unchargedItemIds = Collections.emptyList();
		public ChargedWeaponBuilder unchargedItemIds(Integer... unchargedItemIds) {
			this.unchargedItemIds = Arrays.asList(unchargedItemIds);
			return this;
		}
		List<Integer> animationIds = Collections.emptyList();
		public ChargedWeaponBuilder animationIds(Integer... animationIds) {
			this.animationIds = Arrays.asList(animationIds);
			return this;
		}
		String name = null;
		public ChargedWeaponBuilder name(String name) {
			this.name = name;
			return this;
		}
		Integer rechargeAmount;
		public ChargedWeaponBuilder rechargeAmount(Integer rechargeAmount) {
			this.rechargeAmount = rechargeAmount;
			return this;
		}
		String configKeyName;
		public ChargedWeaponBuilder configKeyName(String configKeyName) {
			this.configKeyName = configKeyName;
			return this;
		}
		String settingsConfigKey;
		/**
		 * If set, it is used in place of configKeyName when accessing config for the item (low charges and display when).
		 */
		public ChargedWeaponBuilder settingsConfigKey(String settingsConfigKey) {
			this.settingsConfigKey = settingsConfigKey;
			return this;
		}
		List<ChargesMessage> checkChargesRegexes = Collections.emptyList();
		public ChargedWeaponBuilder checkChargesRegexes(ChargesMessage... checkChargesRegexes) {
			this.checkChargesRegexes = Arrays.asList(checkChargesRegexes);
			return this;
		}
		List<ChargesMessage> updateMessageChargesRegexes = Collections.emptyList();
		public ChargedWeaponBuilder updateMessageChargesRegexes(ChargesMessage... updateMessageChargesRegexes) {
			this.updateMessageChargesRegexes = Arrays.asList(updateMessageChargesRegexes);
			return this;
		}
		List<ChargesDialogHandler> dialogHandlers = Collections.emptyList();
		public ChargedWeaponBuilder dialogHandlers(ChargesDialogHandler... dialogHandlers) {
			this.dialogHandlers = Arrays.asList(dialogHandlers);
			return this;
		}
		int defaultLowChargeThreshold = 500;
		public ChargedWeaponBuilder defaultLowChargeThreshold(int defaultLowChargeThreshold) {
			this.defaultLowChargeThreshold = defaultLowChargeThreshold;
			return this;
		}
	}

	public final List<Integer> itemIds;
	public final List<Integer> unchargedIds;
	public final List<Integer> animationIds;
	public final String name;
	public final Integer rechargeAmount;
	public final int defaultLowChargeThreshold;
	public final String configKeyName;
	public final String settingsConfigKey;
	// check messages are those produced by menu actions like "Check". update messages are those produced by the weapon
	// being used (e.g. those that notify you it's empty, or has 100 charges left, etc.).
	// These must be kept separate because the check messages [seem to always] have the charges of the weapon before
	// any attacks the weapon is making that tick, while the update messages have the charges of the weapon after any
	// attacks it makes on that tick.
	private final List<ChargesMessage> checkChargesRegexes;
	private final List<ChargesMessage> updateMessageChargesRegexes;
	private final List<ChargesDialogHandler> dialogHandlers;

	ChargedWeapon(ChargedWeaponBuilder builder) {
		this.itemIds = builder.chargedItemIds;
		this.unchargedIds = builder.unchargedItemIds;
		this.animationIds = builder.animationIds;
		if (builder.name == null) throw new IllegalStateException("cannot have a null name for charged weapon.");
		this.name = builder.name;
		this.rechargeAmount = builder.rechargeAmount;
		this.defaultLowChargeThreshold = builder.defaultLowChargeThreshold;
		this.configKeyName = builder.configKeyName;
		this.settingsConfigKey = builder.settingsConfigKey == null ? builder.configKeyName : builder.settingsConfigKey;
		this.checkChargesRegexes = builder.checkChargesRegexes;
		this.updateMessageChargesRegexes = builder.updateMessageChargesRegexes;
		this.dialogHandlers = builder.dialogHandlers;
	}

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

	public DisplayWhen getDisplayWhen(ConfigManager configManager) {
		try
		{
			return DisplayWhen.valueOf(configManager.getConfiguration(WeaponChargesPlugin.CONFIG_GROUP_NAME, settingsConfigKey + DISPLAY_CONFIG_KEY_SUFFIX));
		}
		catch (NullPointerException | IllegalArgumentException e)
		{
			// return default value.
			return DisplayWhen.USE_DEFAULT;
		}
	}

	public void setDisplayWhen(ConfigManager configManager, DisplayWhen displayWhen)
	{
		if (displayWhen == null) throw new IllegalArgumentException("displaywhen cannot be set to null");
		configManager.setConfiguration(WeaponChargesPlugin.CONFIG_GROUP_NAME, settingsConfigKey + DISPLAY_CONFIG_KEY_SUFFIX, displayWhen);
	}

	public int getLowCharge(ConfigManager configManager)
	{
		try
		{
			return Integer.parseInt(configManager.getConfiguration(WeaponChargesPlugin.CONFIG_GROUP_NAME, settingsConfigKey + LOW_CHARGE_CONFIG_KEY_SUFFIX));
		}
		catch (NullPointerException | IllegalArgumentException e)
		{
			// return default value.
			return getDefaultLowChargeThreshold();
		}
	}

	public void setLowCharge(ConfigManager configManager, int charges)
	{
		configManager.setConfiguration(WeaponChargesPlugin.CONFIG_GROUP_NAME, settingsConfigKey + LOW_CHARGE_CONFIG_KEY_SUFFIX, charges);
	}

	private static ChargedWeapon get_scythe_circumvent_illegal_self_reference() {
		return SCYTHE_OF_VITUR;
	}
}

