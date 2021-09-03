package com.weaponcharges;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ChargesDialogHandler
{
	@FunctionalInterface
	public interface MatchHandler
	{
		void handleDialog(NpcDialogStateMatcher.DialogStateMatchers matchers, NpcDialogTracker.NpcDialogState npcDialogState, String optionSelected, WeaponChargesPlugin plugin);
	}

	private final NpcDialogStateMatcher dialogStateMatcher;
	private final MatchHandler matchHandler;

	public boolean handleDialog(NpcDialogTracker.NpcDialogState npcDialogState, WeaponChargesPlugin plugin)
	{
		NpcDialogStateMatcher.DialogStateMatchers matchers = dialogStateMatcher.matchDialog(npcDialogState);
		boolean matched = matchers != null;
		if (matched)
		{
			matchHandler.handleDialog(matchers, npcDialogState, null, plugin);
		}
		return matched;
	}

	public boolean handleDialogOptionSelected(NpcDialogTracker.NpcDialogState npcDialogState, String optionSelected, WeaponChargesPlugin plugin)
	{
		NpcDialogStateMatcher.DialogStateMatchers matchers = dialogStateMatcher.matchDialogOptionSelected(npcDialogState, optionSelected);
		boolean matched = matchers != null;
		if (matched)
		{
			matchHandler.handleDialog(matchers, npcDialogState, optionSelected, plugin);
		}
		return matched;
	}

	public static MatchHandler genericSpriteDialogChargesMessage(boolean chargesAbsolute, int group) {
		return (matchers, npcDialogState, optionSelected, plugin) -> {
			if (npcDialogState.spriteDialogItemId == null) throw new IllegalArgumentException("This handler is for sprite dialogs only.");

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
		return (matchers, npcDialogState, optionSelected, plugin) -> {
			if (npcDialogState.spriteDialogItemId == null) throw new IllegalArgumentException("This handler is for sprite dialogs only.");

			ChargedWeapon chargedWeapon = ChargedWeapon.getChargedWeaponFromId(matchers.getSpriteDialogId());
			if (chargedWeapon != null)
			{
				plugin.setCharges(chargedWeapon, 0);
			}
		};
	}

	public static MatchHandler genericSpriteDialogFullChargeMessage()
	{
		return (matchers, npcDialogState, optionSelected, plugin) -> {
			if (npcDialogState.spriteDialogItemId == null) throw new IllegalArgumentException("This handler is for sprite dialogs only.");

			ChargedWeapon chargedWeapon = ChargedWeapon.getChargedWeaponFromId(matchers.getSpriteDialogId());
			if (chargedWeapon != null)
			{
				plugin.setCharges(chargedWeapon, chargedWeapon.getRechargeAmount());
			}
		};
	}

	public static MatchHandler genericInputChargeMessage()
	{
		return (matchers, npcDialogState, optionSelected, plugin) -> {
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

			plugin.addCharges(plugin.lastUsedOnWeapon, chargesEntered, true);
		};
	}
}
