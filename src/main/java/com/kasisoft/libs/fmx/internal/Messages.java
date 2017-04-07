package com.kasisoft.libs.fmx.internal;

import com.kasisoft.libs.common.i18n.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class Messages {

  @I18N("The 'fmx:doctype' element only allows 'fmx:value' (default is 'html' if omitted).")
  public static String    error_doctype_values;

  @I18N("The 'fmx:include' element must provide an 'fmx:path' value.")
  public static String    error_include_values;

  @I18N("The 'fmx:import' element must provide an 'fmx:path' and an 'fmx:name' value.")
  public static String    error_import_values;
  
  @I18N("The 'fmx:depends' element requires an 'fmx:value' attribute.")
  public static String    error_depends_values;
  
  @I18N("The 'fmx:list' element requires an 'fmx:value' attribute and allows 'fmx:it' to provide an iterator name.")
  public static String    error_list_values;

  @I18N("The 'fmx:with' element requires an 'fmx:model' attribute and allows to rename it using 'fmx:name'.")
  public static String    error_with_values;
  
  @I18N("Missing 'fmx:model' attribute for 'fmx:with' element !")
  public static String    missing_fmx_model;
  
  static {
    I18NSupport.initialize( Messages.class );
  }

} /* ENDCLASS */
