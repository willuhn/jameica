/**********************************************************************
 *
 * Copyright (c) 2004 Olaf Willuhn
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details.
 *
 **********************************************************************/

package de.willuhn.jameica.gui.internal.ext;

import de.willuhn.jameica.gui.util.Color;
import de.willuhn.logging.Level;

public class LogUtil
{
  public static Color getColor(Level level)
  {
    switch (level)
    {
      case TRACE:
      case DEBUG:
        return Color.COMMENT;
      case INFO:
        // das ist die default Schriftfarbe
        return Color.FOREGROUND;
      case WARN:
        return Color.LINK_ACTIVE;
      case ERROR:
        return Color.ERROR;
      default:
        throw new UnsupportedOperationException("Don't know what to do with " + level);
    }
  }
}
