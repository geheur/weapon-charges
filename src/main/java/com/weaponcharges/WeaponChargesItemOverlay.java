package com.weaponcharges;

import com.weaponcharges.WeaponChargesConfig.DisplayWhen;
import com.weaponcharges.WeaponChargesConfig.SerpModes;
import static com.weaponcharges.WeaponChargesPlugin.MAX_SCALES;
import static com.weaponcharges.WeaponChargesConfig.DisplayWhenNoDefault;
import static java.lang.Math.round;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.inject.Inject;
import net.runelite.api.ItemID;
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
			Integer charges = null;
			if (chargedWeapon.getItemIds().contains(itemId)) {
				found = true;
				charges = plugin.getCharges(chargedWeapon);
			} else if (chargedWeapon.getUnchargedIds().contains(itemId)) {
				found = true;
				charges = 0;
			}

			if (found) {
				if (charges == null) {
					topText.setText("?");
				} else {
					DisplayWhen displayWhen = DisplayWhenNoDefault.getDisplayWhen(chargedWeapon.getDisplayWhen(config), config.defaultDisplay());
					if (displayWhen == DisplayWhen.NEVER && !plugin.isShowChargesKeyIsDown()) break;

					boolean isLowCharge = charges <= chargedWeapon.getLowCharge(config);
					if (!isLowCharge && displayWhen == DisplayWhen.LOW_CHARGE && !plugin.isShowChargesKeyIsDown()) break;

					if (config.emptyNotZero() && charges == 0) {
						topText.setText("Empty");
					} else {
						topText.setText(String.valueOf(charges));
					}
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
					int scalesLeftInt = round(scalesLeft);
					String scalesLeftPercentDisplay = getScalesPercent(scalesLeftInt, MAX_SCALES);
					topText.setText(scalesLeftPercentDisplay);
					if (scalesLeftPercentDisplay == "0%" && scalesLeft > 0) {
						topText.setText("1%");
					}
					if (blowpipeChargesLow(scalesLeft, dartsLeft1)) topText.setColor(config.chargesTextLowColor());
				}
			}
			found = true;
		}

		if (ChargedWeapon.SERPENTINE_HELM.getItemIds().contains(itemId)) {
			DisplayWhen displayWhen = DisplayWhenNoDefault.getDisplayWhen(config.serpentine_helm_Display(), config.defaultDisplay());
			Integer charges = null;
			ChargedWeapon chargedWeapon = ChargedWeapon.SERPENTINE_HELM;
			SerpModes displayStyle = config.serpentine_helm_DisplayStyle();
			charges = plugin.getCharges(chargedWeapon);
			if (charges == null)
			{
				if (displayStyle == SerpModes.SCALES) {
					topText.setText("?");
				} else if (displayStyle == SerpModes.PERCENT) {
					topText.setText("??.?%");
				} else if (displayStyle == SerpModes.BOTH) {
					topText.setText("?");
					bottomText.setText("??.?%");
				}
			} else {
				if (displayWhen == DisplayWhen.NEVER && !plugin.isShowChargesKeyIsDown()) return;
				String scalesLeftPercentDisplay = getScalesPercent(charges, chargedWeapon.rechargeAmount);
				if (displayStyle == SerpModes.SCALES) {
					topText.setText(charges.toString());
				} else if (displayStyle == SerpModes.PERCENT) {
					topText.setText(scalesLeftPercentDisplay);
				} else if (displayStyle == SerpModes.BOTH) {
					topText.setText(charges.toString());
					bottomText.setText(scalesLeftPercentDisplay);
					boolean isLowCharge = charges <= chargedWeapon.getLowCharge(config);
					if (isLowCharge) bottomText.setColor(config.chargesTextLowColor());
				}
				if (scalesLeftPercentDisplay.equals("0.0%") && charges > 0) {
					if (displayStyle == SerpModes.PERCENT){
						topText.setText("0.1%");
					} else if (displayStyle == SerpModes.BOTH) {
						bottomText.setText("0.1%");
					}
				}
			}
			found = true;
		}

		if (found) {
			topText.render(graphics);
			bottomText.render(graphics);
		}
	}

	private String getScalesPercent(int currentCharges, int maxCharges)
	{
		NumberFormat df = new DecimalFormat("##0.0");
		float scalesLeftPercent = (float) currentCharges / maxCharges;
		df.setRoundingMode(RoundingMode.DOWN);
		String scalesLeftPercentDisplay = df.format((scalesLeftPercent * 100)) + "%";
		return scalesLeftPercentDisplay;
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
