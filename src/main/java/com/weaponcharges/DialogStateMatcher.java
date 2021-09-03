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
public class DialogStateMatcher
{
	private final boolean isOptionSelected;
	private final DialogTracker.DialogState.DialogType type;

	private final Pattern nameMatch;
	private final Pattern textMatch;
	private final Integer spriteDialogId;
	private final List<Pattern> optionMatches;

	private final Pattern optionMatch;

	public static DialogStateMatcher sprite(Pattern textMatch, Integer itemId)
	{
		return sprite(textMatch, itemId, false);
	}

	public static DialogStateMatcher spriteOptionSelected(Pattern textMatch, Integer itemId)
	{
		return sprite(textMatch, itemId, true);
	}

	private static DialogStateMatcher sprite(Pattern textMatch, Integer itemId, boolean isOptionSelected)
	{
		return DialogStateMatcher.builder()
			.type(DialogTracker.DialogState.DialogType.SPRITE)
			.isOptionSelected(isOptionSelected)
			.textMatch(textMatch)
			.spriteDialogId(itemId)
			.build();
	}

	public static DialogStateMatcher player(Pattern textMatch, Pattern nameMatch)
	{
		return player(textMatch, nameMatch, false);
	}

	public static DialogStateMatcher playerOptionSelected(Pattern textMatch, Pattern nameMatch)
	{
		return player(textMatch, nameMatch, true);
	}

	private static DialogStateMatcher player(Pattern textMatch, Pattern nameMatch, boolean isOptionSelected)
	{
		return DialogStateMatcher.builder()
			.type(DialogTracker.DialogState.DialogType.PLAYER)
			.isOptionSelected(isOptionSelected)
			.nameMatch(nameMatch)
			.textMatch(textMatch)
			.build();
	}

	public static DialogStateMatcher npc(Pattern textMatch, Pattern nameMatch)
	{
		return npc(textMatch, nameMatch, false);
	}

	public static DialogStateMatcher npcOptionSelected(Pattern textMatch, Pattern nameMatch)
	{
		return npc(textMatch, nameMatch, true);
	}

	private static DialogStateMatcher npc(Pattern textMatch, Pattern nameMatch, boolean isOptionSelected)
	{
		return DialogStateMatcher.builder()
			.type(DialogTracker.DialogState.DialogType.NPC)
			.isOptionSelected(isOptionSelected)
			.nameMatch(nameMatch)
			.textMatch(textMatch)
			.build();
	}

	public static DialogStateMatcher options(Pattern textMatch, List<Pattern> optionsMatch)
	{
		return options(textMatch, optionsMatch, null, false);
	}

	public static DialogStateMatcher optionsOptionSelected(Pattern textMatch, List<Pattern> optionsMatch, Pattern optionSelectedMatch)
	{
		return options(textMatch, optionsMatch, optionSelectedMatch, true);
	}

	private static DialogStateMatcher options(Pattern textMatch, List<Pattern> optionsMatch, Pattern optionSelectedMatch, boolean isOptionSelected)
	{
		return DialogStateMatcher.builder()
			.type(DialogTracker.DialogState.DialogType.OPTIONS)
			.isOptionSelected(isOptionSelected)
			.textMatch(textMatch)
			.optionMatches(optionsMatch)
			.optionMatch(optionSelectedMatch)
			.build();
	}

	public static DialogStateMatcher input(Pattern textMatch)
	{
		return input(textMatch, null, false);
	}

	public static DialogStateMatcher inputOptionSelected(Pattern textMatch, Pattern optionSelectedMatcher)
	{
		return input(textMatch, optionSelectedMatcher, true);
	}

	private static DialogStateMatcher input(Pattern textMatch, Pattern optionSelectedMatcher, boolean isOptionSelected)
	{
		return DialogStateMatcher.builder()
			.type(DialogTracker.DialogState.DialogType.INPUT)
			.isOptionSelected(isOptionSelected)
			.nameMatch(textMatch)
			.build();
	}

	public DialogStateMatchers matchDialog(DialogTracker.DialogState dialogState)
	{
		return matchDialog(dialogState, false, null);
	}

	public DialogStateMatchers matchDialogOptionSelected(DialogTracker.DialogState dialogState, String isOptionSelected)
	{
		return matchDialog(dialogState, true, isOptionSelected);
	}

	private DialogStateMatchers matchDialog(DialogTracker.DialogState dialogState, boolean isOptionSelected, String optionSelected)
	{
		if (this.isOptionSelected != isOptionSelected)
		{
			return null;
		}
		if (this.getType() != dialogState.type)
		{
			return null;
		}
		if (this.getSpriteDialogId() != null && this.getSpriteDialogId() != dialogState.spriteDialogItemId)
		{
			return null;
		}

		Matcher nameMatcher = null;
		if (this.getNameMatch() != null)
		{
			if (dialogState.name == null)
			{
				return null; // argument should never be null, but let's be safe.
			}
			nameMatcher = this.getNameMatch().matcher(dialogState.name);
			if (!nameMatcher.find())
			{
				return null;
			}
		}

		Matcher textMatcher = null;
		String text = dialogState.text;
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
			if (this.getOptionMatches().size() != dialogState.options.size())
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

				String option = dialogState.options.get(i);
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

		return new DialogStateMatchers(nameMatcher, textMatcher, dialogState.spriteDialogItemId, optionMatchers, optionMatcher);
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
