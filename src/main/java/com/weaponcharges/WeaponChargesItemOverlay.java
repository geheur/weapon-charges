package com.weaponcharges;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import javax.inject.Inject;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetItem;
import static com.weaponcharges.WeaponChargesConfig.*;
import com.weaponcharges.WeaponChargesConfig.DisplayWhen;
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
		graphics.setFont(FontManager.getRunescapeSmallFont());

		Rectangle bounds = itemWidget.getCanvasBounds();
		TextComponent topText = new TextComponent();
		topText.setPosition(new java.awt.Point(bounds.x - 1, bounds.y + 10));
		topText.setText("");
		topText.setColor(config.chargesTextRegularColor());
		TextComponent bottomText = new TextComponent();
		bottomText.setPosition(new java.awt.Point(bounds.x - 1, bounds.y + 30));
		bottomText.setText("");

		boolean found = false;
		for (ChargedWeapon chargedWeapon : ChargedWeapon.values()) {
			if (chargedWeapon.getItemIds().contains(itemId)) {
				found = true;

				DisplayWhen displayWhen = DisplayWhenNoDefault.getDisplayWhen(chargedWeapon.getDisplayWhen(config), config.defaultDisplay());
				Integer charges = plugin.getCharges(chargedWeapon);
				if (charges == null) {
					topText.setText("?");
				} else {
					if (displayWhen == DisplayWhen.NEVER && !plugin.isShowChargesKeyIsDown()) break;

					boolean isLowCharge = charges <= chargedWeapon.getLowCharge(config);
					if (!isLowCharge && displayWhen == DisplayWhen.LOW_CHARGE && !plugin.isShowChargesKeyIsDown()) break;

					topText.setText(String.valueOf(charges));
					if (isLowCharge) topText.setColor(config.chargesTextLowColor());
				}
				break;
			}
		}

		if (itemId == ItemID.TOXIC_BLOWPIPE) {
			DisplayWhen displayWhen = DisplayWhenNoDefault.getDisplayWhen(config.blowpipe_Display(), config.defaultDisplay());
			Float dartsLeft1 = plugin.getDartsLeft();
			Float scalesLeft = plugin.getScalesLeft();
			if (dartsLeft1 == null || scalesLeft == null)
			{
				topText.setText("?");
			} else {
				if (displayWhen == DisplayWhen.NEVER && !plugin.isShowChargesKeyIsDown()) return;

				boolean isLowCharge = blowpipeChargesLow(scalesLeft, dartsLeft1);
				if (!isLowCharge && displayWhen == DisplayWhen.LOW_CHARGE && !plugin.isShowChargesKeyIsDown()) return;

				String dartsString;
				if (dartsLeft1 == null) {
					dartsString = "?";
				} else {
					int dartsLeft = (int) (float) dartsLeft1;
					dartsString = dartsLeft > 9999 ? new DecimalFormat("#0").format(dartsLeft / 1000.0) + "k" : dartsLeft < 1000 ? String.valueOf(dartsLeft) :
						new DecimalFormat("#0.0").format(dartsLeft / 1000.0) + "k";
				}
				bottomText.setText(dartsString);
				int stringLength = graphics.getFontMetrics().stringWidth(dartsString);
				bottomText.setPosition(new java.awt.Point(bounds.x - 1 + 30 - stringLength, bounds.y + 30));

				WeaponChargesPlugin.DartType dartType = plugin.getDartType();
				if (dartType == null) {
					bottomText.setText("");
				} else {
					bottomText.setColor(dartType.displayColor);
				}

				if (scalesLeft == null) {
					topText.setText("??.?%");
				} else {
					float scalesLeftPercent = scalesLeft / WeaponChargesPlugin.MAX_SCALES;
					topText.setText(String.format("%d%%", (int) (scalesLeftPercent * 100)));
					if (String.format("%d%%", (int) (scalesLeftPercent * 100)).equals(String.format("%d%%", 0)) && scalesLeft > 0) {
						topText.setText("1%");
					}
					if (blowpipeChargesLow(scalesLeft, dartsLeft1)) topText.setColor(config.chargesTextLowColor());
				}
			}
			found = true;
		}

		if (found) {
			topText.render(graphics);
			bottomText.render(graphics);
		}
	}

	private boolean blowpipeChargesLow(float scalesLeft, float dartsLeft)
	{
		int lowCharges = config.blowpipe_LowChargeThreshold();
		int scaleCharges = (int) (scalesLeft * 1.5f);
		int dartCharges = (int) (dartsLeft * 5);
		return scaleCharges <= lowCharges || dartCharges <= lowCharges;
	}

	private Color getColorForScalesLeft(float scalesLeftPercent)
	{
		return Color.getHSBColor((float) (scalesLeftPercent * 1/3f), 1f, 1f);
	}
}
