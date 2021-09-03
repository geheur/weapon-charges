package com.weaponcharges;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChargesMessage
{
	@Getter
	private final Pattern pattern;
	private final Function<Matcher, Integer> chargeLeft;

	public int getChargesLeft(Matcher matcher)
	{
		return chargeLeft.apply(matcher);
	}

	public static ChargesMessage staticChargeMessage(String s, int charges)
	{
		return new ChargesMessage(Pattern.compile(s), matcher -> charges);
	}

	public static ChargesMessage matcherGroupChargeMessage(String s, int group)
	{
		return new ChargesMessage(Pattern.compile(s), matcher -> {
			String chargeCountString = matcher.group(group).replaceAll(",", "");
			return Integer.parseInt(chargeCountString);
		}
		);
	}
}
