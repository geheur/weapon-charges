/*
 * Copyright (c) 2018, JerwuQu <marcus@ramse.se>
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

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(WeaponChargesPlugin.CONFIG_GROUP_NAME)
public interface WeaponChargesConfig extends Config
{
	@ConfigItem(
		keyName = "chargesTextRegularColor",
		name = "Charge Text Color",
		description = "The color to display charge count text in when charges are not low."
	)
	default Color chargesTextRegularColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		keyName = "chargesTextLowColor",
		name = "Low Charge Text Color",
		description = "The color to display charge count text in when charges are low."
	)
	default Color chargesTextLowColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		keyName = WeaponChargesPlugin.DEV_MODE_CONFIG_KEY,
		name = "log data",
		description = "fills your logs with stuff, if you're collecting game messages/dialogs that have to do with weapon charges."
	)
	default boolean devMode()
	{
		return false;
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
		description = "When weapons should show their charges, if you haven't specified anything for that weapon in \"" + WEAPON_SPECIFIC_SETTING + "\"."
	)
	default DisplayWhenNoDefault defaultDisplay()
	{
		return DisplayWhenNoDefault.ALWAYS;
	}

	enum DisplayWhen {
		USE_DEFAULT,
		ALWAYS,
		LOW_CHARGE,
		NEVER,
	}

	@ConfigSection(
		name = "Weapon Specific Config",
		description = "Specify display and low charge threshold values for specific weapons.",
		position = 0,
		closedByDefault = true
	)
	String WEAPON_SPECIFIC_SETTING = "weaponSpecificConfig";

	@ConfigItem(
		keyName = "blowpipeDisplay",
		name = "Blowpipe",
		description = "When the Blowpipe should show the charge counter.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 0
	)
	default DisplayWhen blowpipeDisplay()
	{
		return DisplayWhen.USE_DEFAULT;
	}

	@ConfigItem(
		keyName = "blowpipeLowChargeThreshold",
		name = "Low (Blowpipe)",
		description = "Number of charges considered \"low\". Set to -1 to never show charges as being low. Calculated as number of shots before the Blowpipe runs out of either scales or darts, assuming the assembler is used.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 1
	)
	default int blowpipeLowChargeThreshold()
	{
		return 1500;
	}

	@ConfigItem(
		keyName = "seasTridentDisplay",
		name = "Seas trident",
		description = "When the Seas trident should show the charge counter.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 2
	)
	default DisplayWhen seasTridentDisplay()
	{
		return DisplayWhen.USE_DEFAULT;
	}

	@ConfigItem(
		keyName = "seasTridentLowChargeThreshold",
		name = "Low (Seas trident)",
		description = "Number of charges considered \"low\". Set to -1 to never show charges as being low.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 3
	)
	default int seasTridentLowChargeThreshold()
	{
		return 500;
	}

	@ConfigItem(
		keyName = "swampTridentDisplay",
		name = "Swamp trident",
		description = "When the Swamp trident should show the charge counter.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 4
	)
	default DisplayWhen swampTridentDisplay()
	{
		return DisplayWhen.USE_DEFAULT;
	}

	@ConfigItem(
		keyName = "swampTridentLowChargeThreshold",
		name = "Low (Swamp trident)",
		description = "Number of charges considered \"low\". Set to -1 to never show charges as being low.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 5
	)
	default int swampTridentLowChargeThreshold()
	{
		return 500;
	}

	@ConfigItem(
		keyName = "seasTridentEDisplay",
		name = "Seas trident (e)",
		description = "When the Seas trident (e) should show the charge counter.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 6
	)
	default DisplayWhen seasTridentEDisplay()
	{
		return DisplayWhen.USE_DEFAULT;
	}

	@ConfigItem(
		keyName = "seasTridentELowChargeThreshold",
		name = "Low (Seas trident (e))",
		description = "Number of charges considered \"low\". Set to -1 to never show charges as being low.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 7
	)
	default int seasTridentELowChargeThreshold()
	{
		return 500;
	}

	@ConfigItem(
		keyName = "swampTridentEDisplay",
		name = "Swamp trident (e)",
		description = "When the Swamp trident (e) should show the charge counter.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 8
	)
	default DisplayWhen swampTridentEDisplay()
	{
		return DisplayWhen.USE_DEFAULT;
	}

	@ConfigItem(
		keyName = "swampTridentELowChargeThreshold",
		name = "Low (Swamp trident (e))",
		description = "Number of charges considered \"low\". Set to -1 to never show charges as being low.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 9
	)
	default int swampTridentELowChargeThreshold()
	{
		return 500;
	}

	@ConfigItem(
		keyName = "ibansStaffDisplay",
		name = "Iban's staff",
		description = "When the Iban's staff should show the charge counter.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 10
	)
	default DisplayWhen ibansStaffDisplay()
	{
		return DisplayWhen.USE_DEFAULT;
	}

	@ConfigItem(
		keyName = "ibansStaffLowChargeThreshold",
		name = "Low (Iban's staff)",
		description = "Number of charges considered \"low\". Set to -1 to never show charges as being low.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 11
	)
	default int ibansStaffLowChargeThreshold()
	{
		return 250;
	}

	@ConfigItem(
		keyName = "crystalHalberdDisplay",
		name = "Crystal halberd",
		description = "When the Crystal halberd should show the charge counter.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 12
	)
	default DisplayWhen crystalHalberdDisplay()
	{
		return DisplayWhen.USE_DEFAULT;
	}

	@ConfigItem(
		keyName = "crystalHalberdLowChargeThreshold",
		name = "Low (Crystal halberd)",
		description = "Number of charges considered \"low\". Set to -1 to never show charges as being low.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 13
	)
	default int crystalHalberdLowChargeThreshold()
	{
		return 25;
	}

	@ConfigItem(
		keyName = "abyssalTentacleDisplay",
		name = "Abyssal tentacle",
		description = "When the Abyssal tentacle should show the charge counter.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 14
	)
	default DisplayWhen abyssalTentacleDisplay()
	{
		return DisplayWhen.USE_DEFAULT;
	}

	@ConfigItem(
		keyName = "abyssalTentacleLowChargeThreshold",
		name = "Low (Abyssal tentacle)",
		description = "Number of charges considered \"low\". Set to -1 to never show charges as being low.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 15
	)
	default int abyssalTentacleLowChargeThreshold()
	{
		return 500;
	}

	@ConfigItem(
		keyName = "tomeOfFireDisplay",
		name = "Tome of fire",
		description = "When the Tome of fire should show the charge counter.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 16
	)
	default DisplayWhen tomeOfFireDisplay()
	{
		return DisplayWhen.USE_DEFAULT;
	}

	@ConfigItem(
		keyName = "tomeOfFireLowChargeThreshold",
		name = "Low (Tome of fire)",
		description = "Number of charges considered \"low\". Set to -1 to never show charges as being low.",
		section = WEAPON_SPECIFIC_SETTING,
		position = 17
	)
	default int tomeOfFireLowChargeThreshold()
	{
		return 500;
	}
}
