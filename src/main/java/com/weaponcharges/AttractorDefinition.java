/*
 * Copyright (c) 2018, Sir Girion <https://github.com/sirgirion>
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

import lombok.Getter;
import net.runelite.api.ItemID;
import org.apache.commons.lang3.ArrayUtils;

@Getter
enum AttractorDefinition
{
	ATTRACTOR(0.2f, 0.16f, 0.64f, 
		ItemID.AVAS_ATTRACTOR
	),
	ACCUMULATOR(0.2f, 0.08f, 0.72f, 
		ItemID.AVAS_ACCUMULATOR, 
		ItemID.RANGING_CAPE, 
		ItemID.ACCUMULATOR_MAX_CAPE
	),
	ASSEMBLER(0.2f, 0.0f, 0.8f, 
		ItemID.AVAS_ASSEMBLER,
		ItemID.AVAS_ASSEMBLER_L,
		ItemID.ASSEMBLER_MAX_CAPE,
		ItemID.ASSEMBLER_MAX_CAPE_L,
		ItemID.MASORI_ASSEMBLER,
		ItemID.MASORI_ASSEMBLER_L,
		ItemID.MASORI_ASSEMBLER_MAX_CAPE,
		ItemID.MASORI_ASSEMBLER_MAX_CAPE_L
	);

	private final float breakOnImpactChance;
	private final float dropToFloorChance;
	private final float savedChance;
	private final int[] itemIds;

	AttractorDefinition(float breakOnImpactChance, float dropToFloorChance, float savedChance, int... itemIds)
	{
		this.breakOnImpactChance = breakOnImpactChance;
		this.dropToFloorChance = dropToFloorChance;
		this.savedChance = savedChance;
		this.itemIds = itemIds;
	}

	static AttractorDefinition getAttractorById(int itemId)
	{
		for (AttractorDefinition attractorDefinition : values())
		{
			if (ArrayUtils.contains(attractorDefinition.getItemIds(), itemId))
			{
				return attractorDefinition;
			}
		}

		return null;
	}
}
