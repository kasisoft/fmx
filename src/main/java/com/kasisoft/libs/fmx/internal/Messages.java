package com.kasisoft.libs.fmx.internal;

import com.kasisoft.libs.common.i18n.I18N;
import com.kasisoft.libs.common.i18n.I18NSupport;

/**
 * @author daniel.kasmeroglu@kasisoft.com
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

  @I18N("The 'fmx:with' element requires an 'fmx:value' attribute and allows to rename it using 'fmx:name'.")
  public static String    error_with_values;
  
  @I18N("The 'fmx:escape' elements must provide an attribute 'fmx:expr'.")
  public static String    error_escape_without_expr;
  
  @I18N("The 'fmx:escape' elements must provide an attribute 'fmx:name'.")
  public static String    error_escape_without_name;

  @I18N("The 'fmx:macro' elements must provide an attribute 'fmx:name'.")
  public static String    error_macro_without_name;

  @I18N("Missing 'fmx:model' attribute for 'fmx:with' element !")
  public static String    missing_fmx_model;

  @I18N("Missing root element.")
  public static String    error_no_root_element;

  static {
    I18NSupport.initialize(Messages.class);
  }

} /* ENDCLASS */
