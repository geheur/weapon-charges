package com.weaponcharges;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.config.ConfigManager;

@RequiredArgsConstructor
public class ChargesMessage
{
	@Getter
	private final Pattern pattern;
	private final matcherthing chargeLeft;

	@FunctionalInterface private interface matcherthing {
		Integer customHandler(Matcher matcher, ConfigManager configManager);
	}

	public ChargesMessage(Pattern pattern, Function<Matcher, Integer> chargeLeft2) {
		this(pattern, (matcher, configManager) -> chargeLeft2.apply(matcher));
	}

	public int getChargesLeft(Matcher matcher, ConfigManager configManager)
	{
		return chargeLeft.customHandler(matcher, configManager);
	}

	public static ChargesMessage staticChargeMessage(String s, int charges)
	{
		return new ChargesMessage(Pattern.compile(s), matcher -> charges);
	}

	public static ChargesMessage matcherGroupChargeMessage(String s, int group)
	{
		return new ChargesMessage(Pattern.compile(s), matcher -> {
			String chargeCountString = matcher.group(group);
			return parseCharges(chargeCountString);
		}
		);
	}

	private static int parseCharges(String chargeCountString)
	{
		if (chargeCountString.equals("one")) {
			return 1;
		}
		return Integer.parseInt(chargeCountString.replaceAll(",", ""));
	}

	@FunctionalInterface public interface CustomChargeMatcher {
		Integer customHandler(Matcher matcher, Integer chargeCount, ConfigManager configManager);
	}

	public static ChargesMessage matcherGroupChargeMessage(String s, int group, CustomChargeMatcher customMatcher)
	{
		return new ChargesMessage(Pattern.compile(s), (matcher, configManager) -> {
			String chargeCountString = matcher.group(group).replaceAll(",", "");
			int chargeCount = parseCharges(chargeCountString);
			return customMatcher.customHandler(matcher, chargeCount, configManager);
		}
		);
	}
}
