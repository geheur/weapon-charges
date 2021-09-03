package com.weaponcharges;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.util.Text;

@Getter
@Builder
public class NpcDialogStateMatcher
{
	private final boolean isOptionSelected;
	private final NpcDialogTracker.NpcDialogState.NpcDialogType type;

	private final Pattern nameMatch;
	private final Pattern textMatch;
	private final Integer spriteDialogId;
	private final List<Pattern> optionMatches;

	private final Pattern optionMatch;

	public static NpcDialogStateMatcher sprite(Pattern textMatch, Integer itemId)
	{
		return sprite(textMatch, itemId, false);
	}

	public static NpcDialogStateMatcher spriteOptionSelected(Pattern textMatch, Integer itemId)
	{
		return sprite(textMatch, itemId, true);
	}

	private static NpcDialogStateMatcher sprite(Pattern textMatch, Integer itemId, boolean isOptionSelected)
	{
		return NpcDialogStateMatcher.builder()
			.type(NpcDialogTracker.NpcDialogState.NpcDialogType.SPRITE)
			.isOptionSelected(isOptionSelected)
			.textMatch(textMatch)
			.spriteDialogId(itemId)
			.build();
	}

	public static NpcDialogStateMatcher player(Pattern textMatch, Pattern nameMatch)
	{
		return player(textMatch, nameMatch, false);
	}

	public static NpcDialogStateMatcher playerOptionSelected(Pattern textMatch, Pattern nameMatch)
	{
		return player(textMatch, nameMatch, true);
	}

	private static NpcDialogStateMatcher player(Pattern textMatch, Pattern nameMatch, boolean isOptionSelected)
	{
		return NpcDialogStateMatcher.builder()
			.type(NpcDialogTracker.NpcDialogState.NpcDialogType.PLAYER)
			.isOptionSelected(isOptionSelected)
			.nameMatch(nameMatch)
			.textMatch(textMatch)
			.build();
	}

	public static NpcDialogStateMatcher npc(Pattern textMatch, Pattern nameMatch)
	{
		return npc(textMatch, nameMatch, false);
	}

	public static NpcDialogStateMatcher npcOptionSelected(Pattern textMatch, Pattern nameMatch)
	{
		return npc(textMatch, nameMatch, true);
	}

	private static NpcDialogStateMatcher npc(Pattern textMatch, Pattern nameMatch, boolean isOptionSelected)
	{
		return NpcDialogStateMatcher.builder()
			.type(NpcDialogTracker.NpcDialogState.NpcDialogType.NPC)
			.isOptionSelected(isOptionSelected)
			.nameMatch(nameMatch)
			.textMatch(textMatch)
			.build();
	}

	public static NpcDialogStateMatcher options(Pattern textMatch, List<Pattern> optionsMatch)
	{
		return options(textMatch, optionsMatch, null, false);
	}

	public static NpcDialogStateMatcher optionsOptionSelected(Pattern textMatch, List<Pattern> optionsMatch, Pattern optionSelectedMatch)
	{
		return options(textMatch, optionsMatch, optionSelectedMatch, true);
	}

	private static NpcDialogStateMatcher options(Pattern textMatch, List<Pattern> optionsMatch, Pattern optionSelectedMatch, boolean isOptionSelected)
	{
		return NpcDialogStateMatcher.builder()
			.type(NpcDialogTracker.NpcDialogState.NpcDialogType.OPTIONS)
			.isOptionSelected(isOptionSelected)
			.textMatch(textMatch)
			.optionMatches(optionsMatch)
			.optionMatch(optionSelectedMatch)
			.build();
	}

	public static NpcDialogStateMatcher input(Pattern textMatch)
	{
		return input(textMatch, null, false);
	}

	public static NpcDialogStateMatcher inputOptionSelected(Pattern textMatch, Pattern optionSelectedMatcher)
	{
		return input(textMatch, optionSelectedMatcher, true);
	}

	private static NpcDialogStateMatcher input(Pattern textMatch, Pattern optionSelectedMatcher, boolean isOptionSelected)
	{
		return NpcDialogStateMatcher.builder()
			.type(NpcDialogTracker.NpcDialogState.NpcDialogType.INPUT)
			.isOptionSelected(isOptionSelected)
			.nameMatch(textMatch)
			.build();
	}

	public DialogStateMatchers matchDialog(NpcDialogTracker.NpcDialogState npcDialogState)
	{
		return matchDialog(npcDialogState, false, null);
	}

	public DialogStateMatchers matchDialogOptionSelected(NpcDialogTracker.NpcDialogState npcDialogState, String isOptionSelected)
	{
		return matchDialog(npcDialogState, true, isOptionSelected);
	}

	private DialogStateMatchers matchDialog(NpcDialogTracker.NpcDialogState npcDialogState, boolean isOptionSelected, String optionSelected)
	{
		if (this.isOptionSelected != isOptionSelected)
		{
			return null;
		}
		if (this.getType() != npcDialogState.type)
		{
			return null;
		}
		if (this.getSpriteDialogId() != null && this.getSpriteDialogId() != npcDialogState.spriteDialogItemId)
		{
			return null;
		}

		Matcher nameMatcher = null;
		if (this.getNameMatch() != null)
		{
			if (npcDialogState.name == null)
			{
				return null; // argument should never be null, but let's be safe.
			}
			nameMatcher = this.getNameMatch().matcher(npcDialogState.name);
			if (!nameMatcher.find())
			{
				return null;
			}
		}

		Matcher textMatcher = null;
		String text = npcDialogState.text;
		text = Text.removeTags(text.replaceAll("<br>", " "));
		if (this.getTextMatch() != null)
		{
			if (text == null)
			{
				return null; // argument should never be null, but let's be safe.
			}
			textMatcher = this.getTextMatch().matcher(text);
			if (!textMatcher.find())
			{
				return null;
			}
		}

		List<Matcher> optionMatchers = new ArrayList<>();
		if (this.getOptionMatches() != null)
		{
			if (this.getOptionMatches().size() != npcDialogState.options.size())
			{
				return null;
			}
			for (int i = 0; i < this.getOptionMatches().size(); i++)
			{
				Pattern optionMatch = this.getOptionMatches().get(i);
				if (optionMatch == null)
				{
					optionMatchers.add(null);
					continue;
				}

				String option = npcDialogState.options.get(i);
				if (option == null)
				{
					return null;
				}
				Matcher optionMatcher = optionMatch.matcher(option);
				if (!optionMatcher.find())
				{
					return null;
				}
				optionMatchers.add(optionMatcher);
			}
		}

		Matcher optionMatcher = null;
		if (this.getOptionMatch() != null)
		{
			if (optionSelected == null)
			{
				return null; // argument should never be null, but let's be safe.
			}
			optionMatcher = this.getOptionMatch().matcher(optionSelected);
			if (!optionMatcher.find())
			{
				return null;
			}
		}

		return new DialogStateMatchers(nameMatcher, textMatcher, npcDialogState.spriteDialogItemId, optionMatchers, optionMatcher);
	}

	//		public static NpcDialogStateMatcher noDialog() {
//			return new NpcDialogStateMatcher(NpcDialogType.NO_DIALOG, null, null, null, null);
//		}
//
	@Getter
	@RequiredArgsConstructor
	public static final class DialogStateMatchers
	{
		private final Matcher nameMatcher;
		private final Matcher textMatcher;
		private final Integer spriteDialogId;
		private final List<Matcher> optionMatchers;

		private final Matcher optionMatch;
	}
}
