package com.weaponcharges;

public class ConfigGenerator
{
	static int i = 0;
	public static void main(String[] args)
	{
		String s = "";
		s += generateConfig("blowpipe", "Blowpipe", default_description + " Calculated as number of shots before the Blowpipe runs out of either scales or darts, assuming the assembler is used.", 1500);
		s += generateConfig("seasTrident", "Seas trident", default_description, 500);
		s += generateConfig("swampTrident", "Swamp trident", default_description, 500);
		s += generateConfig("seasTridentE", "Seas trident (e)", default_description, 500);
		s += generateConfig("swampTridentE", "Swamp trident (e)", default_description, 500);
		s += generateConfig("ibansStaff", "Iban's staff", default_description, 250);
		s += generateConfig("crystalHalberd", "Crystal halberd", default_description, 25);
		s += generateConfig("abyssalTentacle", "Abyssal tentacle", default_description, 500);
		s += generateConfig("tomeOfFire", "Tome of fire", default_description, 500);
		System.out.println(s);
	}

	static String default_description = "Number of charges considered \\\"low\\\". Set to -1 to never show charges as being low.";

	private static String generateConfig(String configKeyPrefix, String prettyName, String descriptionOverride, int lowChargeDefault) {
		return
			"\t@ConfigItem(" + "\n" +
			"\t\tkeyName = \"" + configKeyPrefix + "Display\"," + "\n" +
			"\t\tname = \"" + prettyName + "\"," + "\n" +
			"\t\tdescription = \"When the " + prettyName + " should show the charge counter.\"," + "\n" +
			"\t\tsection = WEAPON_SPECIFIC_SETTING," + "\n" +
			"\t\tposition = " + i++ + "\n" +
		"\t)" + "\n" +
		"\tdefault WeaponChargesConfig.DisplayWhen " + configKeyPrefix + "Display()" + "\n" +
		"\t{" + "\n" +
			"\t\treturn WeaponChargesConfig.DisplayWhen.USE_DEFAULT;" + "\n" +
		"\t}" + "\n" +
		"" + "\n" +
		"\t@ConfigItem(" + "\n" +
			"\t\tkeyName = \"" + configKeyPrefix + "LowChargeThreshold\"," + "\n" +
			"\t\tname = \"Low (" + prettyName + ")\"," + "\n" +
			"\t\tdescription = \"" + descriptionOverride + "\"," + "\n" +
			"\t\tsection = WEAPON_SPECIFIC_SETTING," + "\n" +
			"\t\tposition = " + i++ + "\n" +
		"\t)" + "\n" +
		"\tdefault int " + configKeyPrefix + "LowChargeThreshold()" + "\n" +
		"\t{" + "\n" +
			"\t\treturn " + lowChargeDefault + ";" + "\n" +
		"\t}" + "\n" +
		"" + "\n";
	}
}
