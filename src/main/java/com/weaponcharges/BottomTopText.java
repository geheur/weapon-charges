package com.weaponcharges;

import java.awt.*;

public class BottomTopText {
    private String topText;
    private String bottomText;
    private Color topColor;
    private Color bottomColor;

    public BottomTopText(String topText,String bottomText,Color topColor,Color bottomColor)
    {
        this.topText = topText;
        this.bottomText = bottomText;
        this.topColor = topColor;
        this.bottomColor = bottomColor;
    }


    public String getTopText(){
        return topText;
    }

    public String getBottomText(){
        return bottomText;
    }

    public Color getTopColor(){
        return topColor;
    }

    public Color getBottomColor(){
        return bottomColor;
    }
}
