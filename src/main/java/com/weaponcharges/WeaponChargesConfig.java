package com.weaponcharges;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;

@ConfigGroup(WeaponChargesPlugin.CONFIG_GROUP_NAME)
public interface WeaponChargesConfig extends Config
{
	@ConfigItem(
		keyName = "chargesTextRegularColor",
		name = "Charge Text Color",
		description = "The color to display charge count text in when charges are not low.",
		position = 0
	)
	default Color chargesTextRegularColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "chargesTextLowColor",
		name = "Low Charge Text Color",
		description = "The color to display charge count text in when charges are low.",
		position = 1
	)
	default Color chargesTextLowColor()
	{
		return Color.RED;
	}

	enum DisplayWhenNoDefault {
		ALWAYS,
		LOW_CHARGE,
		NEVER,
		;

		public static DisplayWhen getDisplayWhen(DisplayWhen specificDisplayWhen, DisplayWhenNoDefault defaultDisplayWhen)
		{
			if (specificDisplayWhen != DisplayWhen.USE_DEFAULT) return specificDisplayWhen;
			switch (defaultDisplayWhen) {
				case ALWAYS:
					return DisplayWhen.ALWAYS;
				case LOW_CHARGE:
					return DisplayWhen.LOW_CHARGE;
				case NEVER:
					return DisplayWhen.NEVER;
				default:
					throw new IllegalStateException("Unexpected value: " + defaultDisplayWhen);
			}
		}
	}

	@ConfigItem(
		keyName = "defaultDisplay",
		name = "Show Charges",
		description = "When weapons should show their charges, if you haven't specified anything for that weapon in \"" + WEAPON_SPECIFIC_SETTING + "\".",
		position = 2
	)
	default DisplayWhenNoDefault defaultDisplay()
	{
		return DisplayWhenNoDefault.ALWAYS;
	}

	@ConfigItem(
			keyName = "emptyNotZero",
			name = "Show \"Empty\"",
			description = "Enable to show \"Empty\" instead of \"0\" when something has no charges.",
			position = 3
	)
	default boolean emptyNotZero()
	{
		return false;
	}

	@ConfigItem(
		keyName = WeaponChargesPlugin.DEV_MODE_CONFIG_KEY,
		name = "log data",
		description = "fills your logs with stuff, if you're collecting game messages/dialogs that have to do with weapon charges.",
		position = 4
	)
	default boolean devMode()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showOnHotkey",
		name = "Always show charge when held",
		description = "When this key is held, show charges on all tracked weapons.",
		position = 5
	)
	default Keybind showOnHotkey()
	{
		return Keybind.ALT;
	}

	enum DisplayWhen {
		USE_DEFAULT,
		ALWAYS,
		LOW_CHARGE,
		NEVER,
	}

	@ConfigSection(
		name = "Vorkath's head ammo saving",
		description = "Instructions for setting vorkath's head ammo saving status.",
		position = 6,
		closedByDefault = true
	)
	String VORKATHS_HEAD_AMMO_SAVING_INSTRUCTIONS = "vorkathsHeadAmmoSavingInstructionsSection";

	@ConfigItem(
		keyName = "vorkathsHeadInstructions",
		name = "Set vorkath's head ammo saving",
		description = "When the Blowpipe should show the charge counter.",
		section = VORKATHS_HEAD_AMMO_SAVING_INSTRUCTIONS,
		position = 1
	)
	default String vorkathsHeadInstructions()
	{
		return "If you have used vorkath's head on your ranged or max cape, shift-right-click the cape and select that you have done so, so that this plugin will know the correct ammo saving chance.";
	}

	@ConfigItem(
		keyName = "vorkathsHeadMenuOptionDisabled",
		name = "Never show option on cape",
		description = "When enabled, the option to select vorkath's head status is never shown. Normally it is shown when holding shift and right-clicking the cape.",
		section = VORKATHS_HEAD_AMMO_SAVING_INSTRUCTIONS,
		position = 2
	)
	default boolean vorkathsHeadMenuOptionDisabled()
	{
		return false;
	}

	@ConfigSection(
		name = "Weapon Specific Config",
		description = "Specify display and low charge threshold values for specific weapons.",
		position = 7,
		closedByDefault = true
	)
	String WEAPON_SPECIFIC_SETTING = "weaponSpecificConfig";

	@ConfigItem(
		keyName = "hideShiftRightClickOptions",
		name = "Hide shift-right-click options",
		description = "Weapon specific settings are now available by holding shift and right-clicking the weapon. Turning this option on hides these menu entries.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 2
	)
	default boolean hideShiftRightClickOptions()
	{
		return false;
	}

	enum SerpModes {
		SCALES,
		PERCENT,
		BOTH,
	}

}
