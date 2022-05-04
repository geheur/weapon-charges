package com.weaponcharges;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import com.weaponcharges.WeaponChargesPlugin.DartType;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseManager;
import net.runelite.client.ui.overlay.OverlayManager;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.doAnswer;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class WeaponChargesTest
{
	@Mock
	@Bind
	private Client client;

	@Mock
	@Bind
	private ConfigManager configManager;

	@Mock
	@Bind
	private ItemManager itemManager;

	@Mock
	@Bind
	private OverlayManager overlayManager;

	@Mock
	@Bind
	private MouseManager mouseManager;

	@Mock
	@Bind
	private KeyManager keyManager;

	@Mock
	@Bind
	private ClientThread clientThread;

	@Mock
	@Bind
	private WeaponChargesItemOverlay itemOverlay;

	@Mock
	@Bind
	private DialogTracker dialogTracker;

	@Mock
	@Bind
	private WeaponChargesConfig config;

	@Inject
	private WeaponChargesPlugin plugin;

	private Map<String, String> configMap = new HashMap<>();

	private Map<Integer, Integer> equipment = new HashMap<>();

	private int animationId = -1;

	@Before
	public void before()
	{
		final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.setLevel(Level.DEBUG);
		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);

		Player localPlayer = Mockito.mock(Player.class);
//		doReturn(localPlayer).when(client).getLocalPlayer();

		doAnswer(invocation -> {
			InventoryID inventoryId = invocation.getArgument(0, InventoryID.class);
			if (inventoryId.equals(InventoryID.EQUIPMENT)) {
				ItemContainer mock = Mockito.mock(ItemContainer.class);
				doAnswer(i -> {
					Integer slot = i.getArgument(0, Integer.class);
					Integer itemId = equipment.get(slot);
					if (itemId == null) throw new UnsupportedOperationException();
					return new Item(itemId, 1);
				}).when(mock).getItem(anyInt());
				return mock;
			}
			throw new UnsupportedOperationException();
		}).when(client).getItemContainer(any(InventoryID.class));

//		Mockito.when(configManager.getRSProfileKey()).thenAnswer(invocation -> "anrsprofilekey");
		Mockito.when(configManager.getRSProfileConfiguration(eq(WeaponChargesPlugin.CONFIG_GROUP_NAME), anyString())).thenAnswer(invocation -> {
			String key = invocation.getArgument(1, String.class);
			return configMap.get(key);
		});
		doAnswer(invocation -> {
			String key = invocation.getArgument(1, String.class);
			Object value = invocation.getArgument(2, Object.class);
			configMap.put(key, value.toString());
			return null;
		}).when(configManager).setRSProfileConfiguration(eq(WeaponChargesPlugin.CONFIG_GROUP_NAME), anyString(), any());
	}

	// TODO check weapons while they are equipped.
	// TODO check animations.

	@Test
	public void test() {
		for (ChargedWeapon chargedWeapon : ChargedWeapon.values())
		{
			plugin.setCharges(chargedWeapon, 100);

			chargedWeapon.getDisplayWhen(config);
			chargedWeapon.getLowCharge(config);
		}

		checkIbans();

		checkTridentCheckMessages();

		checkTrident(ChargedWeapon.TRIDENT_OF_THE_SEAS);
		checkTrident(ChargedWeapon.TRIDENT_OF_THE_SWAMP);
		checkTrident(ChargedWeapon.TRIDENT_OF_THE_SEAS_E);
		checkTrident(ChargedWeapon.TRIDENT_OF_THE_SWAMP_E);

		checkCrystalHalberd();

		checkAbyssalTentacle();

//		checkTomeOfFire();

		checkScytheOfVitur();
		checkSanguineScytheOfVitur();
		checkHolyScytheOfVitur();

		checkSanguinestiStaff();
		checkHolySanguinestiStaff();

		checkArclight();
		
		checkBlowpipe();
	}

	private void checkTridentCheckMessages()
	{
//		2022-05-04 12:37:05 [Client] INFO  com.weaponcharges.Devtools - 354: GAMEMESSAGE "Your Trident of the swamp (e) has 2,000 charges."
//		2022-05-04 12:38:41 [Client] INFO  com.weaponcharges.Devtools - 514: GAMEMESSAGE "Your Trident of the swamp (e) has one charge."
//		2022-05-04 12:40:27 [Client] INFO  com.weaponcharges.Devtools - 691: GAMEMESSAGE "Your Trident of the seas (e) has one charge."
//		2022-05-04 12:40:36 [Client] INFO  com.weaponcharges.Devtools - 706: GAMEMESSAGE "Your Trident of the seas (e) has 1,001 charges."
//		2022-05-04 07:08:09 [Client] INFO  com.weaponcharges.Devtools - 12: GAMEMESSAGE "Your Trident of the swamp has 6 charges."
//		2022-05-04 07:09:59 [Client] INFO  com.weaponcharges.Devtools - 196: GAMEMESSAGE "Your Trident of the seas has one charge."
//		2022-05-04 07:10:55 [Client] INFO  com.weaponcharges.Devtools - 288: GAMEMESSAGE "Your Trident of the seas has 2 charges."
//		2022-05-04 07:13:15 [Client] INFO  com.weaponcharges.Devtools - 521: GAMEMESSAGE "Your Trident of the seas has 100 charges."

		checkWeaponMessage(ChargedWeapon.TRIDENT_OF_THE_SEAS, "Your Trident of the seas has 2 charges.", 2);
		checkWeaponMessage(ChargedWeapon.TRIDENT_OF_THE_SEAS, "Your Trident of the seas has one charge.", 1);
		checkWeaponMessage(ChargedWeapon.TRIDENT_OF_THE_SEAS, "Your Trident of the seas has no charges.", 0);

		checkWeaponMessage(ChargedWeapon.TRIDENT_OF_THE_SWAMP_E, "Your Trident of the swamp (e) has 2,000 charges.", 2000);
		checkWeaponMessage(ChargedWeapon.TRIDENT_OF_THE_SWAMP_E, "Your Trident of the swamp (e) has one charge.", 1);
		checkWeaponMessage(ChargedWeapon.TRIDENT_OF_THE_SWAMP_E, "Your Trident of the swamp (e) has no charges.", 0);

		checkWeaponMessage(ChargedWeapon.TRIDENT_OF_THE_SEAS_E, "Your Trident of the seas (e) has 1,001 charges.", 1001);
		checkWeaponMessage(ChargedWeapon.TRIDENT_OF_THE_SEAS_E, "Your Trident of the seas (e) has one charge.", 1);
		checkWeaponMessage(ChargedWeapon.TRIDENT_OF_THE_SEAS_E, "Your Trident of the seas (e) has no charges.", 0);

		checkWeaponMessage(ChargedWeapon.TRIDENT_OF_THE_SWAMP, "Your Trident of the swamp has 6 charges.", 6);
		checkWeaponMessage(ChargedWeapon.TRIDENT_OF_THE_SWAMP, "Your Trident of the swamp has one charge.", 1);
		checkWeaponMessage(ChargedWeapon.TRIDENT_OF_THE_SWAMP, "Your Trident of the swamp has no charges.", 0);
	}

	private void checkBlowpipe()
	{
		// TODO some of these unnecessarily check (menu option) the blowpipe.

		checkBlowpipeMessage("Darts: <col=007f00>None</col>. Scales: <col=007f00>99 (0.6%)</col>.", 99, 0, DartType.UNKNOWN);
		checkBlowpipeMessage("Darts: <col=007f00>Adamant dart x 16,383</col>. Scales: <col=007f00>16,383 (100.0%)</col>.", 16383, 16383, DartType.ADAMANT);
		checkBlowpipeMessage("Darts: <col=007f00>Adamant dart x 16,383</col>. Scales: <col=007f00>0 (0.0%)</col>.", 0, 16383, DartType.ADAMANT);

		checkBlowpipeMessage("The blowpipe can't hold any more scales.", WeaponChargesPlugin.MAX_SCALES, null, null);
		checkBlowpipeMessage("The blowpipe can't hold any more darts.", null, WeaponChargesPlugin.MAX_DARTS, null);

		checkBlowpipeMessage("Your blowpipe has run out of darts.", null, 0, DartType.UNKNOWN);
		checkBlowpipeMessage("Your blowpipe needs to be charged with Zulrah's scales.", 0, null, null);
		checkBlowpipeMessage("Your blowpipe has run out of scales and darts.", 0, 0, DartType.UNKNOWN);

		checkBlowpipeMessage("Your blowpipe contains no darts.", null, 0, DartType.UNKNOWN);
		checkBlowpipeMessage("Your blowpipe needs to be charged with Zulrah's scales and loaded with darts.", 0, 0, DartType.UNKNOWN);

		checkBlowpipeMessage("The blowpipe has no darts in it.", null, 0, DartType.UNKNOWN);

		// unload with darts has no chat message. TODO

		// uncharge 2021-09-05 14:40:47 [Client] INFO  com.weaponcharges.Devtools - 481: dialog state changed: DialogState{DESTROY_ITEM, title='Are you sure you want to uncharge it?', itemId=12926, item_name='Toxic blowpipe', text='If you uncharge the blowpipe, all scales and darts will fall out.'}
	}

	// TODO not testing SPAM.

	private void checkBlowpipeMessage(String message, Integer scalesExpected, Integer dartsExpected, DartType dartType)
	{
		checkBlowpipeMessage(message, scalesExpected, dartsExpected, dartType, true);
	}

	private void blowpipeMessage(String message, Integer scalesExpected, Integer dartsExpected, DartType dartType)
	{
		checkBlowpipeMessage(message, scalesExpected, dartsExpected, dartType, false);
	}

	private void checkBlowpipeMessage(String message, Integer scalesExpected, Integer dartsExpected, DartType dartType, boolean checkBlowpipe)
	{
		if ((scalesExpected != null && scalesExpected == 1000) || (dartsExpected != null && dartsExpected == 1000) || dartType == DartType.BRONZE) {
			throw new IllegalArgumentException("Cannot have identical expected to the defaults.");
		}
		plugin.setScalesLeft(1000);
		plugin.setDartsLeft(1000);
		plugin.setDartType(DartType.BRONZE);

		if (checkBlowpipe)
		{
			TestMenuEntry menuEntry = new TestMenuEntry();
			menuEntry.setOption("Check");
			menuEntry.setItemId(ItemID.TOXIC_BLOWPIPE);
			MenuOptionClicked event = new MenuOptionClicked(menuEntry);
			plugin.onMenuOptionClicked(event);
		}

		gameMessage(message);
		plugin.onGameTick(new GameTick());

		if (scalesExpected != null) assertEquals(Float.valueOf(scalesExpected), plugin.getScalesLeft());
		else assertEquals(Float.valueOf(1000), plugin.getScalesLeft());
		if (dartsExpected != null) assertEquals(Float.valueOf(dartsExpected), plugin.getDartsLeft());
		else assertEquals(Float.valueOf(1000), plugin.getDartsLeft());
		if (dartType != null) assertEquals(dartType, plugin.getDartType());
		else assertEquals(DartType.BRONZE, plugin.getDartType());
	}

	private void checkArclight()
	{
		checkWeaponMessage(ChargedWeapon.ARCLIGHT, "Your arclight has 6397 charges left.", 6397);
	}

	private void checkSanguinestiStaff()
	{
		checkWeaponMessage(ChargedWeapon.SANGUINESTI_STAFF, "Your Sanguinesti staff is already fully charged.", 20000);
		checkWeaponMessage(ChargedWeapon.SANGUINESTI_STAFF, "Your Sanguinesti staff has 1,000 charges remaining.", 1000);
		checkWeaponMessage(ChargedWeapon.SANGUINESTI_STAFF, "Your Sanguinesti staff has 1 charges remaining.", 1);

		optionsDialogSelected(ChargedWeapon.SANGUINESTI_STAFF, "Uncharge your staff for all its charges? (regaining 11,748 blood runes)", "Proceed.", 123, 0, "Proceed.", "Cancel.");
	}

	private void checkHolySanguinestiStaff()
	{
		checkWeaponMessage(ChargedWeapon.SANGUINESTI_STAFF, "Your Holy sanguinesti staff is already fully charged.", 20000);
		checkWeaponMessage(ChargedWeapon.SANGUINESTI_STAFF, "Your Holy sanguinesti staff has 1,000 charges remaining.", 1000);
		checkWeaponMessage(ChargedWeapon.SANGUINESTI_STAFF, "Your Holy sanguinesti staff has 1 charges remaining.", 1);

		optionsDialogSelected(ChargedWeapon.SANGUINESTI_STAFF, "Uncharge your staff for all its charges? (regaining 11,748 blood runes)", "Proceed.", 123, 0, "Proceed.", "Cancel.");
	}

	private void checkScytheOfVitur()
	{
		checkWeaponMessage(ChargedWeapon.SCYTHE_OF_VITUR, "Your Scythe of vitur has 19,529 charges remaining.", 19529);

		inputDialog(ChargedWeapon.SCYTHE_OF_VITUR, "How many sets of 100 charges do you wish to apply? (Up to 173)", "120", 100, 12100);
		spriteDialog(ChargedWeapon.SCYTHE_OF_VITUR, "You apply 17,300 charges to your Scythe of vitur.", ChargedWeapon.SCYTHE_OF_VITUR.getItemIds().get(0), 100, 17400);

		spriteDialogOptionSelected(ChargedWeapon.SCYTHE_OF_VITUR, "If you uncharge your scythe into the well, 17,300<br>charges will be added to the well.", ChargedWeapon.SCYTHE_OF_VITUR.getItemIds().get(0), 100, 0);
		// This one isn't tracked because it is superfluous.
//		spriteDialog(ChargedWeapon.SCYTHE_OF_VITUR, "You uncharge your scythe into the well. It now<br>contains 173 sets of 100 charges.", ChargedWeapon.SCYTHE_OF_VITUR.getItemIds().get(0), 100, 0);
	}

	private void checkSanguineScytheOfVitur()
	{
		checkWeaponMessage(ChargedWeapon.SCYTHE_OF_VITUR, "Your Sanguine scythe of vitur has 19,529 charges remaining.", 19529);

		inputDialog(ChargedWeapon.SCYTHE_OF_VITUR, "How many sets of 100 charges do you wish to apply? (Up to 173)", "120", 100, 12100);
		spriteDialog(ChargedWeapon.SCYTHE_OF_VITUR, "You apply 17,300 charges to your Sanguine scythe of vitur.", ChargedWeapon.SCYTHE_OF_VITUR.getItemIds().get(0), 100, 17400);

		spriteDialogOptionSelected(ChargedWeapon.SCYTHE_OF_VITUR, "If you uncharge your scythe into the well, 17,300<br>charges will be added to the well.", ChargedWeapon.SCYTHE_OF_VITUR.getItemIds().get(0), 100, 0);
		// This one isn't tracked because it is superfluous.
//		spriteDialog(ChargedWeapon.SCYTHE_OF_VITUR, "You uncharge your scythe into the well. It now<br>contains 173 sets of 100 charges.", ChargedWeapon.SCYTHE_OF_VITUR.getItemIds().get(0), 100, 0);
	}

	private void checkHolyScytheOfVitur()
	{
		checkWeaponMessage(ChargedWeapon.SCYTHE_OF_VITUR, "Your Holy scythe of vitur has 19,529 charges remaining.", 19529);

		inputDialog(ChargedWeapon.SCYTHE_OF_VITUR, "How many sets of 100 charges do you wish to apply? (Up to 173)", "120", 100, 12100);
		spriteDialog(ChargedWeapon.SCYTHE_OF_VITUR, "You apply 17,300 charges to your Holy scythe of vitur.", ChargedWeapon.SCYTHE_OF_VITUR.getItemIds().get(0), 100, 17400);

		spriteDialogOptionSelected(ChargedWeapon.SCYTHE_OF_VITUR, "If you uncharge your scythe into the well, 17,300<br>charges will be added to the well.", ChargedWeapon.SCYTHE_OF_VITUR.getItemIds().get(0), 100, 0);
		// This one isn't tracked because it is superfluous.
//		spriteDialog(ChargedWeapon.SCYTHE_OF_VITUR, "You uncharge your scythe into the well. It now<br>contains 173 sets of 100 charges.", ChargedWeapon.SCYTHE_OF_VITUR.getItemIds().get(0), 100, 0);
	}

	private void checkTomeOfFire()
	{
		checkWeaponMessage(ChargedWeapon.TOME_OF_FIRE, "Your tome currently holds 6,839 charges.", 6839);
		checkWeaponMessage(ChargedWeapon.TOME_OF_FIRE, "Your tome currently holds one charge.", 1);

		equippedWeaponPeriodicUpdate(ChargedWeapon.TOME_OF_FIRE, "Your Tome of Fire is now empty.", 0);

		equippedWeaponPeriodicUpdate(ChargedWeapon.TOME_OF_FIRE, "You remove a page from the book. Your tome currently holds 6,839 charges.", 6839);
		equippedWeaponPeriodicUpdate(ChargedWeapon.TOME_OF_FIRE, "You remove 2 pages from the book. Your tome currently holds 6,799 charges.", 6799);
		equippedWeaponPeriodicUpdate(ChargedWeapon.TOME_OF_FIRE, "You empty your book of pages.", 0);
		equippedWeaponPeriodicUpdate(ChargedWeapon.TOME_OF_FIRE, "You remove 299 pages from the book. Your tome currently holds one charge.", 1);
	}

	private void checkAbyssalTentacle()
	{
		checkWeaponMessage(ChargedWeapon.ABYSSAL_TENTACLE, "Your abyssal tentacle can perform 190 more attacks.", 190);
	}

	private void checkCrystalHalberd()
	{
		checkWeaponMessage(ChargedWeapon.CRYSTAL_HALBERD, "Your crystal halberd has 278 charges remaining.", 278);
	}

	private void checkIbans()
	{
		checkWeaponMessage(ChargedWeapon.IBANS_STAFF, "You have 2500 charges left on the staff.", 2500);
		checkWeaponMessage(ChargedWeapon.IBANS_STAFF, "You have 116 charges left on the staff.", 116);
		checkWeaponMessage(ChargedWeapon.IBANS_STAFF, "You have a charge left on the staff.", 1);
		checkWeaponMessage(ChargedWeapon.IBANS_STAFF, "You have no charges left on the staff.", 0);

		equippedWeaponPeriodicUpdate(ChargedWeapon.IBANS_STAFF, "<col=ef1020>Your staff only has 100 charges left.</col>", 100);
		equippedWeaponPeriodicUpdate(ChargedWeapon.IBANS_STAFF, "<col=ef1020>Your staff has run out of charges.</col>", 0);
		equippedWeaponPeriodicUpdate(ChargedWeapon.IBANS_STAFF, "You need to recharge your staff to use this spell.", 0);

		spriteDialog(ChargedWeapon.IBANS_STAFF, "You hold the staff above the well and feel the power of<br>Zamorak flow through you.", 12658, 2015, 2500);
	}

	private void checkTrident(ChargedWeapon chargedWeapon)
	{
		equippedWeaponPeriodicUpdate(chargedWeapon, "<col=ef1020>Your trident only has 100 charges left!</col>", 100);
		equippedWeaponPeriodicUpdate(chargedWeapon, "<col=ef1020>Your trident has run out of charges.</col>", 0);
		equippedWeaponPeriodicUpdate(chargedWeapon, "The weapon has no charges left. You need death runes, chaos runes, fire runes and Zulrah's scales to charge it.", 0);

		inputDialog(chargedWeapon, "How many charges would you like to add? (0 - 2,477)", "123", 50, 50 + 123);

		spriteDialog(chargedWeapon, "You add a charge to the weapon.<br>New total: 2016", chargedWeapon.getItemIds().get(0), 2015, 2016);
		spriteDialog(chargedWeapon, "Your weapon is already fully charged.", chargedWeapon.getItemIds().get(0), 2015, chargedWeapon.rechargeAmount);
		spriteDialog(chargedWeapon, "You add 124 charges to the weapon.<br>New total: 247", chargedWeapon.getItemIds().get(0), 123, 247);

		optionsDialogSelected(chargedWeapon, (chargedWeapon == ChargedWeapon.TRIDENT_OF_THE_SEAS || chargedWeapon == ChargedWeapon.TRIDENT_OF_THE_SEAS_E) ? "You will NOT get the coins back." : "Really uncharge the trident?", "Okay, uncharge it.", 123, 0, "Okay, uncharge it.", "No, don't uncharge it.");
		optionsDialogSelected(chargedWeapon, "If you drop it, it will lose all its charges.", "Drop it.", 123, 0, "Drop it.", "No, don't drop it.");
	}

	private void optionsDialogSelected(ChargedWeapon chargedWeapon, String text, String optionSelected, int initialCharges, int charges, String... options)
	{
		if (initialCharges == 0) throw new RuntimeException();
		plugin.lastUnchargeClickedWeapon = chargedWeapon;
		plugin.setCharges(chargedWeapon, initialCharges);
		plugin.optionSelected(DialogTracker.DialogState.options(text, options), optionSelected);
		checkCharges(chargedWeapon, charges);
	}

	private void spriteDialog(ChargedWeapon chargedWeapon, String title, int itemId, int initialCharges, int charges)
	{
		if (initialCharges == 0) throw new RuntimeException();
		plugin.lastUsedOnWeapon = chargedWeapon;
		plugin.setCharges(chargedWeapon, initialCharges);
		plugin.dialogStateChanged(DialogTracker.DialogState.sprite(title, itemId));
		checkCharges(chargedWeapon, charges);
	}

	private void spriteDialogOptionSelected(ChargedWeapon chargedWeapon, String title, int itemId, int initialCharges, int charges)
	{
		if (initialCharges == 0) throw new RuntimeException();
		plugin.lastUsedOnWeapon = chargedWeapon;
		plugin.setCharges(chargedWeapon, initialCharges);
		plugin.optionSelected(DialogTracker.DialogState.sprite(title, itemId), null);
		checkCharges(chargedWeapon, charges);
	}

	private void inputDialog(ChargedWeapon chargedWeapon, String title, String input, int initialCharges, int charges)
	{
		if (initialCharges == 0) throw new RuntimeException();
		plugin.lastUsedOnWeapon = chargedWeapon;
		plugin.setCharges(chargedWeapon, initialCharges);
		plugin.optionSelected(DialogTracker.DialogState.input(title, input), input);
		checkCharges(chargedWeapon, charges);
	}

	private void equippedWeaponPeriodicUpdate(ChargedWeapon chargedWeapon, String message, int charges)
	{
		equipWeapon(chargedWeapon);
		gameMessage(message);
		gameTick();
		checkCharges(chargedWeapon, charges);
	}

	private void gameTick()
	{
		plugin.onGameTick(new GameTick());
	}

	private void equipWeapon(ChargedWeapon chargedWeapon)
	{
		equipment.put(EquipmentInventorySlot.WEAPON.getSlotIdx(), chargedWeapon.getItemIds().get(0));
	}

	private void checkWeaponSpecificMessage(ChargedWeapon chargedWeapon, String message, int charges)
	{
		gameMessage(message);
		checkCharges(chargedWeapon, charges);
	}

	private void checkWeaponMessage(ChargedWeapon chargedWeapon, String message, int charges)
	{
		checkWeapon(chargedWeapon);
		gameMessage(message);
		gameTick(); // This isn't necessary for normal check messages, but some are identical to update messages so I
					// didn't include them in the check messages section but instead in the update section.
		checkCharges(chargedWeapon, charges);
	}

	private void checkWeapon(ChargedWeapon chargedWeapon)
	{
		TestMenuEntry menuEntry = new TestMenuEntry();
		menuEntry.setOption("Check");
		menuEntry.setItemId(chargedWeapon.getItemIds().get(0));
		MenuOptionClicked event = new MenuOptionClicked(menuEntry);
		plugin.onMenuOptionClicked(event);
	}

	private void checkCharges(ChargedWeapon chargedWeapon, int expectedCharges)
	{
		assertEquals(Integer.valueOf(expectedCharges), plugin.getCharges(chargedWeapon));
	}

	private void gameMessage(String message)
	{
		plugin.onChatMessage(new ChatMessage(null, ChatMessageType.GAMEMESSAGE, null, message, null, -1));
	}
}
