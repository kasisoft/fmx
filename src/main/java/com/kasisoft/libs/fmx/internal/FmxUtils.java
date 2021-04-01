package com.kasisoft.libs.fmx.internal;

import com.kasisoft.libs.fmx.*;

import javax.validation.constraints.*;

import java.net.*;

import java.nio.charset.*;

import java.io.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class FmxUtils {

  public static String cleanup(String instr) {
    String result = instr;
    if (result != null) {
      result = result.trim();
      if (result.length() == 0) {
        result = null;
      }
    }
    return result;
  }

  public static String readText(@NotNull URL url) {
    try (InputStreamReader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
      return readText(reader);
    } catch (Exception ex) {
      throw FmxException.wrap(ex);
    }
  }
  
  public static String readText(@NotNull Reader reader) {
    StringWriter result = new StringWriter();
    copy(reader, result);
    return result.toString();
  }
  
  private static void copy(Reader reader, Writer writer) {
    try {
      char[] charray = new char[8192];
      int    read    = reader.read(charray);
      while (read != -1) {
        if (read > 0) {
          writer.write(charray, 0, read);
        }
        read = reader.read(charray);
      }
    } catch (Exception ex) {
      throw FmxException.wrap(ex);
    }
  }
  
  /**
   * This function removes trailing whitespace from this buffer.
   * 
   * @param chars   The whitespace characters.
   */
  public static String trimTrailing(String input, String chars) {
    StringBuilder result = new StringBuilder(input);
    while (result.length() > 0) {
      int  length = result.length();
      char ch     = result.charAt(length - 1);
      if (chars.indexOf(ch) == -1) {
        break;
      }
      result.deleteCharAt(length - 1);
    }
    return result.toString();
  }
  
} /* ENDCLASS */
