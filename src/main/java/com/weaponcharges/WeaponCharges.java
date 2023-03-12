package com.weaponcharges;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemID;

import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static java.lang.Math.round;
import static com.weaponcharges.ChargedWeapon.SERPENTINE_HELM;
import static com.weaponcharges.WeaponChargesConfig.DisplayWhen.LOW_CHARGE;
import static com.weaponcharges.WeaponChargesConfig.DisplayWhen.NEVER;
import static com.weaponcharges.WeaponChargesConfig.SerpModes.BOTH;
import static com.weaponcharges.WeaponChargesConfig.SerpModes.PERCENT;
import static com.weaponcharges.WeaponChargesPlugin.MAX_SCALES_BLOWPIPE;

public class WeaponCharges {

    public WeaponCharges(){
    }

    public static BottomTopText getWeaponCharges(Graphics2D graphics, WeaponChargesPlugin plugin, Client client, WeaponChargesConfig config,int itemId)
    {
        String topText = "";
        String bottomText = "";
        Color topColor = Color.white;
        Color bottomcolor = Color.white;

        //send in parameters client null and itemId -1 to get the equipped weapon
        if(client != null && itemId == -1 && client.getItemContainer(InventoryID.EQUIPMENT).getItem(EquipmentInventorySlot.WEAPON.getSlotIdx()) != null)
            itemId = client.getItemContainer(InventoryID.EQUIPMENT).getItem(EquipmentInventorySlot.WEAPON.getSlotIdx()).getId();


        boolean found = false;
        for (ChargedWeapon chargedWeapon : ChargedWeapon.values()) {
            Integer charges = null;
            if (chargedWeapon.getItemIds().contains(itemId)) {
                found = true;
                charges = plugin.getCharges(chargedWeapon);//  plugin.getCharges(chargedWeapon);
            } else if (chargedWeapon.getUnchargedIds().contains(itemId)) {
                found = true;
                charges = 0;
            }

            if (found) {
                if (charges == null) {
                    topText = "?";
                } else {
                    WeaponChargesConfig.DisplayWhen displayWhen = WeaponChargesConfig.DisplayWhenNoDefault.getDisplayWhen(chargedWeapon.getDisplayWhen(config), config.defaultDisplay());
                    if (displayWhen == NEVER && !plugin.isShowChargesKeyIsDown()) break;

                    boolean isLowCharge = charges <= chargedWeapon.getLowCharge(config);
                    if (!isLowCharge && displayWhen == LOW_CHARGE && !plugin.isShowChargesKeyIsDown()) break;

                    if (charges == 0 && config.emptyNotZero()) {
                        topText = "Empty";
                    } else {
                        topText = String.valueOf(charges);

                        if (chargedWeapon == SERPENTINE_HELM) {
                            String scalesLeftPercentDisplay = formatPercentage(charges, SERPENTINE_HELM.rechargeAmount);
                            WeaponChargesConfig.SerpModes displayStyle = config.serpentine_helm_DisplayStyle();
                            if (displayStyle == PERCENT) {
                                topText = scalesLeftPercentDisplay;
                            } else if (displayStyle == BOTH) {
                                topText = charges.toString();
                                bottomText= scalesLeftPercentDisplay;
                                if (isLowCharge) bottomcolor = config.chargesTextLowColor();
                            }
                        }
                    }
                    if (isLowCharge) topColor = config.chargesTextLowColor();
                }
                break;
            }
        }

        if (itemId == ItemID.TOXIC_BLOWPIPE) {
            WeaponChargesConfig.DisplayWhen displayWhen = WeaponChargesConfig.DisplayWhenNoDefault.getDisplayWhen(config.blowpipe_Display(), config.defaultDisplay());
            Float dartsLeft1 = plugin.getDartsLeft();
            Float scalesLeft = plugin.getScalesLeft();
            if (dartsLeft1 == null || scalesLeft == null)
            {
                topText = "?";
            } else {
                if (displayWhen == NEVER && !plugin.isShowChargesKeyIsDown()) return null;

                boolean isLowCharge = blowpipeChargesLow(scalesLeft, dartsLeft1,config);
                if (!isLowCharge && displayWhen == LOW_CHARGE && !plugin.isShowChargesKeyIsDown()) return null;

                String dartsString;
                if (dartsLeft1 == null) {
                    dartsString = "?";
                } else {
                    int dartsLeft = (int) (float) dartsLeft1;
                    dartsString = dartsLeft > 9999 ? new DecimalFormat("#0").format(dartsLeft / 1000.0) + "k" : dartsLeft < 1000 ? String.valueOf(dartsLeft) :
                            new DecimalFormat("#0.0").format(dartsLeft / 1000.0) + "k";
                }
                bottomText = dartsString;
                int stringLength = graphics.getFontMetrics().stringWidth(dartsString);

                WeaponChargesPlugin.DartType dartType = plugin.getDartType();
                if (dartType == null) {
                    bottomText = "";
                } else {
                    bottomcolor = dartType.displayColor;
                }

                if (scalesLeft == null) {
                    topText ="??.?%";
                } else {
                    String scalesLeftPercentDisplay = formatPercentage(round(scalesLeft), MAX_SCALES_BLOWPIPE);
                    topText = scalesLeftPercentDisplay;
                    if (blowpipeChargesLow(scalesLeft, dartsLeft1,config)) topColor = config.chargesTextLowColor();
                }
            }
            found = true;
        }

        if (found) {

            BottomTopText bottomTopText = new BottomTopText(topText,bottomText,topColor,bottomcolor);
            return bottomTopText;
        }

        return null;
    }

    static boolean blowpipeChargesLow(float scalesLeft, float dartsLeft,WeaponChargesConfig config)
    {
        int lowCharges = config.blowpipe_LowChargeThreshold();
        int scaleCharges = (int) (scalesLeft * 1.5f);
        int dartCharges = (int) (dartsLeft * 5);
        return scaleCharges <= lowCharges || dartCharges <= lowCharges;
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

    private Color getColorForScalesLeft(float scalesLeftPercent)
    {
        return Color.getHSBColor((float) (scalesLeftPercent * 1/3f), 1f, 1f);
    }
}
