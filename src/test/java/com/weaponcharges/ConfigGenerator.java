package com.weaponcharges;

import static com.weaponcharges.ChargedWeapon.*;

public class ConfigGenerator
{
	static int i = 0;
	public static void main(String[] args)
	{
		String s = "\t// begin autogenerated\n";
//		s += generateConfig("blowpipe", "Blowpipe", default_description + " Calculated as number of shots before the Blowpipe runs out of either scales or darts, assuming the assembler is used.", 1500);
		i += 0; // blowpipe uses negative indexes.
		s += generateConfig(TRIDENT_OF_THE_SEAS, "Seas trident", 500);
		s += generateConfig(TRIDENT_OF_THE_SWAMP, "Swamp trident", 500);
		s += generateConfig(TRIDENT_OF_THE_SEAS_E, "Seas trident (e)", 500);
		s += generateConfig(TRIDENT_OF_THE_SWAMP_E, "Swamp trident (e)", 500);
		s += generateConfig(IBANS_STAFF, "Iban's staff", 250);
		s += generateConfig(CRYSTAL_HALBERD, "Crystal halberd", 25);
		s += generateConfig(ABYSSAL_TENTACLE, "Abyssal tentacle", 500);
		s += generateConfig(TOME_OF_FIRE, "Tome of fire", 500);
		s += generateConfig(TOME_OF_WATER, "Tome of water", 500);
		s += generateConfig(SCYTHE_OF_VITUR, "Scythe of vitur", 500);
		s += generateConfig(SANGUINESTI_STAFF, "Sanguinesti staff", 500);
		s += generateConfig(ARCLIGHT, "Arclight", 500);
		s += generateConfig(CRAWS, "Craw's bow", 500);
		s += generateConfigNoArticle(VIGGORAS, "Viggora's chainmace", 500);
		s += generateConfigNoArticle(THAMMARONS, "Thammaron's sceptre", 500);
		s += generateConfigNoArticle(CRYSTAL_BOW, "Crystal bow", 500);
		s += generateConfig(BOW_OF_FAERDHINEN, "Bow of faerdhinen", 500);
		s += generateConfig(CRYSTAL_HELM, "Crystal armor", 500); // All 3 pieces share config.
		s += "\t// end autogenerated\n";
		System.out.println(s);
	}

	static String default_description = "Number of charges considered \\\"low\\\".";

	private static String generateConfigNoArticle(ChargedWeapon chargedWeapon, String prettyName, int lowChargeDefault) {
		return generateConfig(chargedWeapon.getSettingsConfigKey(), prettyName, default_description, lowChargeDefault, true);
	}

	private static String generateConfig(ChargedWeapon chargedWeapon, String prettyName, int lowChargeDefault) {
		return generateConfig(chargedWeapon.getSettingsConfigKey(), prettyName, default_description, lowChargeDefault, false);
	}

	private static String generateConfig(String configKeyPrefix, String prettyName, String descriptionOverride, int lowChargeDefault, boolean noArticle) {
		return
			"\t@ConfigItem(" + "\n" +
			"\t\tkeyName = \"" + configKeyPrefix + DISPLAY_CONFIG_KEY_SUFFIX + "\"," + "\n" +
			"\t\tname = \"" + prettyName + "\"," + "\n" +
			"\t\tdescription = \"When " + (noArticle ? "" : "the ") + prettyName + " should show the charge counter.\"," + "\n" +
			"\t\tsection = WEAPON_SPECIFIC_SETTING," + "\n" +
			"\t\tposition = " + i++ + "\n" +
		"\t)" + "\n" +
		"\tdefault WeaponChargesConfig.DisplayWhen " + configKeyPrefix + "_Display()" + "\n" +
		"\t{" + "\n" +
			"\t\treturn WeaponChargesConfig.DisplayWhen.USE_DEFAULT;" + "\n" +
		"\t}" + "\n" +
		"" + "\n" +
		"\t@ConfigItem(" + "\n" +
			"\t\tkeyName = \"" + configKeyPrefix + LOW_CHARGE_CONFIG_KEY_SUFFIX + "\"," + "\n" +
			"\t\tname = \"Low (" + prettyName + ")\"," + "\n" +
			"\t\tdescription = \"" + descriptionOverride + "\"," + "\n" +
			"\t\tsection = WEAPON_SPECIFIC_SETTING," + "\n" +
			"\t\tposition = " + i++ + "\n" +
		"\t)" + "\n" +
		"\tdefault int " + configKeyPrefix + "_LowChargeThreshold()" + "\n" +
		"\t{" + "\n" +
			"\t\treturn " + lowChargeDefault + ";" + "\n" +
		"\t}" + "\n" +
		"" + "\n";
	}
}
