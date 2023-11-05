package com.weaponcharges;

import static com.weaponcharges.ChargedWeapon.SERPENTINE_HELM;
import com.weaponcharges.WeaponChargesConfig.DisplayWhen;
import static com.weaponcharges.WeaponChargesConfig.DisplayWhen.LOW_CHARGE;
import static com.weaponcharges.WeaponChargesConfig.DisplayWhen.NEVER;
import static com.weaponcharges.WeaponChargesConfig.DisplayWhenNoDefault;
import com.weaponcharges.WeaponChargesConfig.SerpModes;
import static com.weaponcharges.WeaponChargesConfig.SerpModes.BOTH;
import static com.weaponcharges.WeaponChargesConfig.SerpModes.PERCENT;
import static com.weaponcharges.WeaponChargesPlugin.MAX_SCALES_BLOWPIPE;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import static java.lang.Math.round;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.inject.Inject;
import net.runelite.api.widgets.InterfaceID;
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
		showOnInterfaces(InterfaceID.CHAMBERS_OF_XERIC_INVENTORY);
		showOnInterfaces(InterfaceID.CHAMBERS_OF_XERIC_STORAGE_UNIT_PRIVATE);
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
		bottomText.setColor(config.chargesTextRegularColor());

		boolean found = false;
		for (ChargedWeapon chargedWeapon : ChargedWeapon.values()) {
			Integer charges = null;
			if (chargedWeapon.getItemIds().contains(itemId)) {
				found = true;
				charges = plugin.getCharges(chargedWeapon);
			} else if (chargedWeapon.getUnchargedIds().contains(itemId)) {
				found = true;
				charges = 0;
			}

			if (!found) continue;

			if (chargedWeapon != ChargedWeapon.TOXIC_BLOWPIPE) {
				if (charges == null) {
					topText.setText("?");
				} else {
					DisplayWhen displayWhen = DisplayWhenNoDefault.getDisplayWhen(chargedWeapon.getDisplayWhen(plugin.configManager), config.defaultDisplay());
					if (displayWhen == NEVER && !plugin.isShowChargesKeyIsDown()) break;

					boolean isLowCharge = charges <= chargedWeapon.getLowCharge(plugin.configManager);
					if (!isLowCharge && displayWhen == LOW_CHARGE && !plugin.isShowChargesKeyIsDown()) break;

					if (charges == 0 && config.emptyNotZero()) {
						topText.setText("Empty");
					} else {
						topText.setText(String.valueOf(charges));

						if (chargedWeapon == SERPENTINE_HELM) {
							String scalesLeftPercentDisplay = formatPercentage(charges, SERPENTINE_HELM.rechargeAmount);
							SerpModes displayStyle = plugin.getSerpHelmDisplayStyle();
							if (displayStyle == PERCENT) {
								topText.setText(scalesLeftPercentDisplay);
							} else if (displayStyle == BOTH) {
								topText.setText(charges.toString());
								bottomText.setText(scalesLeftPercentDisplay);
								if (isLowCharge) bottomText.setColor(config.chargesTextLowColor());
							}
						}
					}
					if (isLowCharge) topText.setColor(config.chargesTextLowColor());
				}
				break;
			}
			else
			{
				DisplayWhen displayWhen = DisplayWhenNoDefault.getDisplayWhen(ChargedWeapon.TOXIC_BLOWPIPE.getDisplayWhen(plugin.configManager), config.defaultDisplay());
				Float dartsLeft1 = plugin.getDartsLeft();
				Float scalesLeft = plugin.getScalesLeft();
				if (dartsLeft1 == null || scalesLeft == null)
				{
					topText.setText("?");
				} else {
					if (displayWhen == NEVER && !plugin.isShowChargesKeyIsDown()) return;

					boolean isLowCharge = blowpipeChargesLow(scalesLeft, dartsLeft1);
					if (!isLowCharge && displayWhen == LOW_CHARGE && !plugin.isShowChargesKeyIsDown()) return;

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
						String scalesLeftPercentDisplay = formatPercentage(round(scalesLeft), MAX_SCALES_BLOWPIPE);
						topText.setText(scalesLeftPercentDisplay);
						if (blowpipeChargesLow(scalesLeft, dartsLeft1)) topText.setColor(config.chargesTextLowColor());
					}
				}
			}
		}

		if (found) {
			topText.render(graphics);
			bottomText.render(graphics);
		}
	}

	static String formatPercentage(int numerator, int denominator)
	{
		NumberFormat df = new DecimalFormat("##0.0");
		df.setRoundingMode(RoundingMode.DOWN);
		float scalesLeftPercent = (float) numerator / denominator;
		String percentage = df.format((scalesLeftPercent * 100));
		if (percentage.equals("0.0") && numerator > 0) percentage = "0.1";
		return percentage + "%";
	}

	private boolean blowpipeChargesLow(float scalesLeft, float dartsLeft)
	{
		int lowCharges = ChargedWeapon.TOXIC_BLOWPIPE.getLowCharge(plugin.configManager);
		int scaleCharges = (int) (scalesLeft * 1.5f);
		int dartCharges = (int) (dartsLeft * 5);
		return scaleCharges <= lowCharges || dartCharges <= lowCharges;
	}

	private Color getColorForScalesLeft(float scalesLeftPercent)
	{
		return Color.getHSBColor((float) (scalesLeftPercent * 1/3f), 1f, 1f);
	}
}
