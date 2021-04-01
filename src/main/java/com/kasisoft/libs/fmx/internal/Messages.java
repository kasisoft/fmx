package com.kasisoft.libs.fmx.internal;

/**
 * @author daniel.kasmeroglu@kasisoft.com
 */
public class Messages {

  public static String    error_doctype_values = "The 'fmx:doctype' element only allows 'fmx:value' (default is 'html' if omitted).";

  public static String    error_include_values = "The 'fmx:include' element must provide an 'fmx:path' value.";

  public static String    error_import_values = "The 'fmx:import' element must provide an 'fmx:path' and an 'fmx:name' value.";
  
  public static String    error_depends_values = "The 'fmx:depends' element requires an 'fmx:value' attribute.";
  
  public static String    error_list_values = "The 'fmx:list' element requires an 'fmx:value' attribute and allows 'fmx:it' to provide an iterator name.";

  public static String    error_with_values = "The 'fmx:with' element requires an 'fmx:value' attribute and allows to rename it using 'fmx:name'.";
  
  public static String    error_escape_without_expr = "The 'fmx:escape' elements must provide an attribute 'fmx:expr'.";
  
  public static String    error_escape_without_name = "The 'fmx:escape' elements must provide an attribute 'fmx:name'.";

  public static String    error_macro_without_name = "The 'fmx:macro' elements must provide an attribute 'fmx:name'.";

  public static String    missing_fmx_model = "Missing 'fmx:model' attribute for 'fmx:with' element !";

  public static String    error_no_root_element = "Missing root element.";

} /* ENDCLASS */
