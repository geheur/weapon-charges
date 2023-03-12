package com.weaponcharges;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.inject.Inject;

import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.TextComponent;

public class WeaponChargesItemOverlay extends WidgetItemOverlay
{
	private final WeaponChargesPlugin plugin;
	private final WeaponChargesConfig config;

	@Inject
	WeaponChargesItemOverlay(WeaponChargesPlugin plugin, WeaponChargesConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		showOnInventory();
		showOnEquipment();
		showOnBank();
		showOnInterfaces(WidgetID.CHAMBERS_OF_XERIC_STORAGE_UNIT_INVENTORY_GROUP_ID);
		showOnInterfaces(WidgetID.CHAMBERS_OF_XERIC_STORAGE_UNIT_PRIVATE_GROUP_ID);
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem itemWidget)
	{
		BottomTopText bottomTopText = WeaponCharges.getWeaponCharges(graphics,plugin, null,config,itemId);
		graphics.setFont(FontManager.getRunescapeSmallFont());

		Rectangle bounds = itemWidget.getCanvasBounds();
		TextComponent topText = new TextComponent();
		topText.setPosition(new java.awt.Point(bounds.x - 1, bounds.y + 10));
		topText.setText("");
		topText.setColor(config.chargesTextRegularColor());
		TextComponent bottomText = new TextComponent();
		bottomText.setPosition(new java.awt.Point(bounds.x - 1, bounds.y + 30));
		bottomText.setText("");
		bottomText.setColor(config.chargesTextRegularColor());

		if(bottomTopText != null)
		{
			topText.setText(bottomTopText.getTopText());
			topText.setColor(bottomTopText.getTopColor());
			bottomText.setText(bottomTopText.getBottomText());
			bottomText.setColor(bottomTopText.getBottomColor());

			topText.render(graphics);
			bottomText.render(graphics);
		}
	}
}
