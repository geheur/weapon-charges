package com.weaponcharges;

import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;

public class WeaponChargesInfoboxOverlay extends OverlayPanel
{
    private final WeaponChargesPlugin plugin;

    private final WeaponChargesConfig config;

    private ItemManager itemManager;

    private Client client;

    @Inject
    public WeaponChargesInfoboxOverlay(WeaponChargesPlugin plugin,Client client, WeaponChargesConfig config,ItemManager itemManager)
    {
        super(plugin);
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        this.plugin = plugin;
        this.config = config;
        this.itemManager = itemManager;
        this.client = client;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if(config.showInfoBoxOverlay())
        {
            BottomTopText bottomTopText = WeaponCharges.getWeaponCharges(graphics,plugin,client,config,-1);

            if(bottomTopText != null)
            {
                BufferedImage image = itemManager.getImage(client.getItemContainer(InventoryID.EQUIPMENT).getItem(EquipmentInventorySlot.WEAPON.getSlotIdx()).getId());

                graphics.drawImage(image,panelComponent.getBounds().x + 5, 0,null);

                panelComponent.getChildren().add(TitleComponent.builder()
                        .text(bottomTopText.getTopText())
                        .color(bottomTopText.getTopColor())
                        .build());

                panelComponent.getChildren().add(TitleComponent.builder()
                        .text(bottomTopText.getBottomText())
                        .color(bottomTopText.getBottomColor())
                        .build());

                panelComponent.setPreferredSize(new Dimension(
                        image.getWidth() + 10,
                        0));

            }
            return super.render(graphics);
        }

        return null;
    }
}
