package com.weaponcharges;

public class ConfigGenerator
{
	static int i = 0;
	public static void main(String[] args)
	{
		String s = "";
//		s += generateConfig("blowpipe", "Blowpipe", default_description + " Calculated as number of shots before the Blowpipe runs out of either scales or darts, assuming the assembler is used.", 1500);
		s += generateConfig(ChargedWeapon.TRIDENT_OF_THE_SEAS.getConfigKeyName(), "Seas trident", default_description, 500);
		s += generateConfig(ChargedWeapon.TRIDENT_OF_THE_SWAMP.getConfigKeyName(), "Swamp trident", default_description, 500);
		s += generateConfig(ChargedWeapon.TRIDENT_OF_THE_SEAS_E.getConfigKeyName(), "Seas trident (e)", default_description, 500);
		s += generateConfig(ChargedWeapon.TRIDENT_OF_THE_SWAMP_E.getConfigKeyName(), "Swamp trident (e)", default_description, 500);
		s += generateConfig(ChargedWeapon.IBANS_STAFF.getConfigKeyName(), "Iban's staff", default_description, 250);
		s += generateConfig(ChargedWeapon.CRYSTAL_HALBERD.getConfigKeyName(), "Crystal halberd", default_description, 25);
		s += generateConfig(ChargedWeapon.ABYSSAL_TENTACLE.getConfigKeyName(), "Abyssal tentacle", default_description, 500);
		s += generateConfig(ChargedWeapon.TOME_OF_FIRE.getConfigKeyName(), "Tome of fire", default_description, 500);
		s += generateConfig(ChargedWeapon.SCYTHE_OF_VITUR.getConfigKeyName(), "Scythe of vitur", default_description, 500);
		s += generateConfig(ChargedWeapon.SANGUINESTI_STAFF.getConfigKeyName(), "Sanguinesti staff", default_description, 500);
		s += generateConfig(ChargedWeapon.ARCLIGHT.getConfigKeyName(), "Arclight", default_description, 500);
		System.out.println(s);
	}

	static String default_description = "Number of charges considered \\\"low\\\". Set to -1 to never show charges as being low.";

	private static String generateConfig(ChargedWeapon chargedWeapon, String prettyName, String descriptionOverride, int lowChargeDefault) {
		return generateConfig(chargedWeapon.getConfigKeyName(), prettyName, descriptionOverride, lowChargeDefault);
	}

	private static String generateConfig(String configKeyPrefix, String prettyName, String descriptionOverride, int lowChargeDefault) {
		return
			"\t@ConfigItem(" + "\n" +
			"\t\tkeyName = \"" + configKeyPrefix + ChargedWeapon.DISPLAY_CONFIG_KEY_SUFFIX + "\"," + "\n" +
			"\t\tname = \"" + prettyName + "\"," + "\n" +
			"\t\tdescription = \"When the " + prettyName + " should show the charge counter.\"," + "\n" +
			"\t\tsection = WEAPON_SPECIFIC_SETTING," + "\n" +
			"\t\tposition = " + i++ + "\n" +
		"\t)" + "\n" +
		"\tdefault WeaponChargesConfig.DisplayWhen " + configKeyPrefix + "_Display()" + "\n" +
		"\t{" + "\n" +
			"\t\treturn WeaponChargesConfig.DisplayWhen.USE_DEFAULT;" + "\n" +
		"\t}" + "\n" +
		"" + "\n" +
		"\t@ConfigItem(" + "\n" +
			"\t\tkeyName = \"" + configKeyPrefix + ChargedWeapon.LOW_CHARGE_CONFIG_KEY_SUFFIX + "\"," + "\n" +
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
