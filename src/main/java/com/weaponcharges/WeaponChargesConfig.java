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
		keyName = "showOnUncharged",
		name = "Show on uncharged items.",
		description = "When the charged item has an uncharged version that is separate, do not draw \"0\" or \"Empty\" on it.",
		position = 4
	)
	default boolean showOnUncharged()
	{
		return true;
	}

	@ConfigItem(
		keyName = WeaponChargesPlugin.DEV_MODE_CONFIG_KEY,
		name = "log data",
		description = "fills your logs with stuff, if you're collecting game messages/dialogs that have to do with weapon charges.",
		position = 5
	)
	default boolean devMode()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showOnHotkey",
		name = "Always show charge when held",
		description = "When this key is held, show charges on all tracked weapons.",
		position = 6
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
		name = "Ammo saving capes",
		description = "Instructions for setting vorkath's head ammo saving status.",
		position = 7,
		closedByDefault = true
	)
	String AMMO_SAVING_CAPES_SECTION = "vorkathsHeadAmmoSavingInstructionsSection";

	@ConfigItem(
		keyName = "vorkathsHeadInstructions",
		name = "<html>" +
			   "The ranging skillcape and dizana's<br>" +
			   "quiver have variable ammo saving<br>" +
			   "changes. Shift-right-click these to<br>" +
			   "choose what ammo saving you have on<br>" +
			   "it. This setting is per account." +
		       "</html>",
		description = "",
		section = AMMO_SAVING_CAPES_SECTION,
		position = 1
	)
	default void vorkathsHeadInstructions()
	{
	}

	@ConfigItem(
		keyName = "vorkathsHeadMenuOptionDisabled",
		name = "Never show option on cape",
		description = "When enabled, the option to select ammo saving status is never shown. Normally it is shown when holding shift and right-clicking the cape.",
		section = AMMO_SAVING_CAPES_SECTION,
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
