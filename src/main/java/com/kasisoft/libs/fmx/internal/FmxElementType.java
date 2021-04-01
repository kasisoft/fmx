package com.kasisoft.libs.fmx.internal;

import javax.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.Map;

import lombok.experimental.FieldDefaults;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * @author daniel.kasmeroglu@kasisoft.com
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum FmxElementType {

  // generate a doctype element
  doctype("doctype"),
  
  // include the mentioned content
  include("include"),
  
  // import the mentioned content
  importDecl("import"),
  
  // repeat the content multiple times
  list("list"),
  
  // emit the content only when a certain condition is met
  depends("depends"),
  
  // setup a variable with a certain name for the inner portion
  with("with"),
  
  // represent a directive
  directive("directive"),
  
  // escape block
  escape("escape"),
  
  // compress block
  compress("compress"),
  
  // pseudo element used to wrap the content
  root("root"),
  
  select("switch"),
  
  option("case"),
  
  defaultcase("default"),
  
  macro("macro"),
  
  nested("nested"),
  
  xescape("xescape");
  
  String   literal;
  
  FmxElementType(String lit) {
    literal = lit;
    LocalData.map.put(literal, this);
  }
  
  public static FmxElementType valueByName(@NotBlank String name, FmxElementType defaultValue) {
    FmxElementType result = LocalData.map.get(name);
    if (result == null) {
      result = defaultValue;
    }
    return result;
  }

  private static class LocalData {
    
    static final Map<String, FmxElementType> map = new HashMap<>();
    
  } /* ENDCLASS */
  
} /* ENDENUM */
