package com.kasisoft.libs.fmx.internal;

import com.kasisoft.libs.common.i18n.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class Messages {

  @I18N("Missing 'fmx:model' attribute for 'fmx:with' element !")
  public static String    missing_fmx_model;
  
  static {
    I18NSupport.initialize( Messages.class );
  }

} /* ENDCLASS */
