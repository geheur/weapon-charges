package com.weaponcharges;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ChargesDialogHandler
{
	@FunctionalInterface
	public interface MatchHandler
	{
		void handleDialog(DialogStateMatcher.DialogStateMatchers matchers, DialogTracker.DialogState dialogState, String optionSelected, WeaponChargesPlugin plugin);
	}

	private final DialogStateMatcher dialogStateMatcher;
	private final MatchHandler matchHandler;

	public boolean handleDialog(DialogTracker.DialogState dialogState, WeaponChargesPlugin plugin)
	{
		DialogStateMatcher.DialogStateMatchers matchers = dialogStateMatcher.matchDialog(dialogState);
		boolean matched = matchers != null;
		if (matched)
		{
			matchHandler.handleDialog(matchers, dialogState, null, plugin);
		}
		return matched;
	}

	public boolean handleDialogOptionSelected(DialogTracker.DialogState dialogState, String optionSelected, WeaponChargesPlugin plugin)
	{
		DialogStateMatcher.DialogStateMatchers matchers = dialogStateMatcher.matchDialogOptionSelected(dialogState, optionSelected);
		boolean matched = matchers != null;
		if (matched)
		{
			matchHandler.handleDialog(matchers, dialogState, optionSelected, plugin);
		}
		return matched;
	}

	public static MatchHandler genericSpriteDialogChargesMessage(boolean chargesAbsolute, int group) {
		return (matchers, dialogState, optionSelected, plugin) -> {
			if (dialogState.spriteDialogItemId == null) throw new IllegalArgumentException("This handler is for sprite dialogs only.");

			String chargeCountString = matchers.getTextMatcher().group(group).replaceAll(",", "");
			int charges = Integer.parseInt(chargeCountString);
			ChargedWeapon chargedWeapon = ChargedWeapon.getChargedWeaponFromId(matchers.getSpriteDialogId());
			if (chargedWeapon != null)
			{
				if (chargesAbsolute)
				{
					plugin.setCharges(chargedWeapon, charges);
				} else {
					plugin.addCharges(chargedWeapon, charges, true);
				}
			}
		};
	}

	public static MatchHandler genericSpriteDialogUnchargeMessage()
	{
		return (matchers, dialogState, optionSelected, plugin) -> {
			if (dialogState.spriteDialogItemId == null) throw new IllegalArgumentException("This handler is for sprite dialogs only.");

			ChargedWeapon chargedWeapon = ChargedWeapon.getChargedWeaponFromId(matchers.getSpriteDialogId());
			if (chargedWeapon != null)
			{
				plugin.setCharges(chargedWeapon, 0);
			}
		};
	}

	public static MatchHandler genericSpriteDialogFullChargeMessage()
	{
		return (matchers, dialogState, optionSelected, plugin) -> {
			if (dialogState.spriteDialogItemId == null) throw new IllegalArgumentException("This handler is for sprite dialogs only.");

			ChargedWeapon chargedWeapon = ChargedWeapon.getChargedWeaponFromId(matchers.getSpriteDialogId());
			if (chargedWeapon != null)
			{
				plugin.setCharges(chargedWeapon, chargedWeapon.getRechargeAmount());
			}
		};
	}

	public static MatchHandler genericInputChargeMessage()
	{
		return genericInputChargeMessage(1);
	}

	public static MatchHandler genericInputChargeMessage(int multiplier)
	{
		return (matchers, dialogState, optionSelected, plugin) -> {
			String chargeCountString = matchers.getNameMatcher().group(1).replaceAll(",", "");
			int maxChargeCount = Integer.parseInt(chargeCountString);
			int chargesEntered;
			try
			{
				chargesEntered = Integer.parseInt(optionSelected.replaceAll("k", "000").replaceAll("m", "000000").replaceAll("b", "000000000"));
			} catch (NumberFormatException e) {
				// can happen if the input is empty for example.
				return;
			}

			if (chargesEntered > maxChargeCount) chargesEntered = maxChargeCount;

			plugin.addCharges(plugin.lastUsedOnWeapon, chargesEntered * multiplier, true);
		};
	}

	public static MatchHandler genericUnchargeDialog()
	{
		return (matchers, dialogState, optionSelected, plugin) -> {
			plugin.setCharges(plugin.lastUnchargeClickedWeapon, 0, true);
		};
	}
}
