/*
 * This file is part of the Jose Project
 * see http://jose-chess.sourceforge.net/
 * (c) 2002-2006 Peter Sch�fer
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 */

package de.jose.util.style;

import de.jose.view.style.JoFontConstants;
import de.jose.Util;

import javax.swing.text.*;
import java.awt.*;

/**
 * StyleUtil
 *
 * @author Peter Sch�fer
 */

public class StyleUtil
{
    // ------------------------------------------------------------
     //  Styles Constants
     // ------------------------------------------------------------
     protected static StyleContext editStyles = new StyleContext();

     protected static Style empty = editStyles.addStyle("empty",null);
     protected static Style plain = editStyles.addStyle("plain",null);

     public static Style bold = editStyles.addStyle("bold",null);
     public static Style italic = editStyles.addStyle("italic",null);
     public static Style underline = editStyles.addStyle("underline",null);

     public static Style unbold = editStyles.addStyle("unbold",null);
     public static Style unitalic = editStyles.addStyle("unitalic",null);
     public static Style ununderline = editStyles.addStyle("ununderline",null);

     public static Style left = editStyles.addStyle("left",null);
     public static Style center = editStyles.addStyle("center",null);
     public static Style right = editStyles.addStyle("right",null);

     protected static Style color = editStyles.addStyle("color",null);
     protected static Style size = editStyles.addStyle("size",null);
     protected static Style scale = editStyles.addStyle("scale",null);

     static {
         StyleConstants.setBold(bold,true);
         StyleConstants.setBold(unbold,false);

         StyleConstants.setItalic(italic,true);
         StyleConstants.setItalic(unitalic,false);

         StyleConstants.setUnderline(underline,true);
         StyleConstants.setUnderline(ununderline,false);

         StyleConstants.setBold(plain,false);
         StyleConstants.setItalic(plain,false);
         StyleConstants.setUnderline(plain,false);

         StyleConstants.setAlignment(left,StyleConstants.ALIGN_LEFT);
         StyleConstants.setAlignment(center,StyleConstants.ALIGN_CENTER);
         StyleConstants.setAlignment(right,StyleConstants.ALIGN_RIGHT);
     }

     public static Style coloredStyle(Color color)
     {
         StyleConstants.setForeground(StyleUtil.color,color);
         return StyleUtil.color;
     }

    public static Style plainStyle(Color color, String family, int size)
    {
        if (color==null)
            StyleUtil.plain.removeAttribute(StyleConstants.Foreground);
        else
            StyleConstants.setForeground(StyleUtil.plain,color);
        if (family==null)
            StyleUtil.plain.removeAttribute(StyleConstants.FontFamily);
        else
            StyleConstants.setFontFamily(StyleUtil.plain,family);
        if (size <= 0)
            StyleUtil.plain.removeAttribute(StyleConstants.FontSize);
        else
            StyleConstants.setFontSize(StyleUtil.plain,size);
        return StyleUtil.plain;
    }

     public static Style sizedStyle(int size)
     {
         StyleConstants.setFontSize(StyleUtil.size,size);
         return StyleUtil.size;
     }

    public static Style sizedStyle(int size, float scale)
    {
        StyleConstants.setFontSize(StyleUtil.scale,size);
        JoFontConstants.setFontScaleFactor(StyleUtil.scale,scale);
        return StyleUtil.scale;
    }

    public static boolean differsSize(AttributeSet style, AttributeSet base)
    {
        return (StyleConstants.getFontSize(style) != StyleConstants.getFontSize(base));
    }

    public static boolean differsColor(AttributeSet style, AttributeSet base)
    {
        return !Util.equals(StyleConstants.getForeground(style), StyleConstants.getForeground(base));
    }

    public static boolean differsFamily(AttributeSet style, AttributeSet base)
    {
        return !Util.equals(StyleConstants.getFontFamily(style), StyleConstants.getFontFamily(base));
    }

    /**
     * @return true if a contiguous area has the same attribute (in this case: bold)
     */
    public static boolean isContiguous(StyledDocument doc, int from, int to, Object attrKey)
    {
        for (int i=from; i < to; )
        {
            Element celm = doc.getCharacterElement(i);
            AttributeSet cstyle = celm.getAttributes();

            Boolean value = (Boolean)cstyle.getAttribute(attrKey);
            if (value==null || !value.booleanValue())
                return false;

            i = celm.getEndOffset();
        }
        return true;
    }

      /**
       * get character style at one location
       * @param doc
       * @param pos
       * @return
       */
      public static AttributeSet getCharacterStyleAt(StyledDocument doc, int pos)
      {
          Element celm = doc.getCharacterElement(pos);
          if (celm!=null)
              return celm.getAttributes();
          else
              return null;
      }

      public static boolean hasDifferentCharacterAttributes(AttributeSet style, AttributeSet base)
      {
          return  (StyleConstants.isBold(style) != StyleConstants.isBold(base)) ||
                  (StyleConstants.isItalic(style) != StyleConstants.isItalic(base)) ||
                  (StyleConstants.isUnderline(style) != StyleConstants.isUnderline(base)) ||
                  (StyleConstants.getFontSize(style) != StyleConstants.getFontSize(base)) ||
                  (!StyleConstants.getForeground(style).equals(StyleConstants.getForeground(base)));
      }

}