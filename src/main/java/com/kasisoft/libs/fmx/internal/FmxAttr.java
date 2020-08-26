package com.kasisoft.libs.fmx.internal;

import static com.kasisoft.libs.fmx.FmxConstants.FMX_NAMESPACE;

import com.kasisoft.libs.common.text.StringFunctions;
import com.kasisoft.libs.fmx.FmxException;

import org.w3c.dom.Attr;

import javax.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.experimental.FieldDefaults;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * @author daniel.kasmeroglu@kasisoft.com
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum FmxAttr {

  depends ("depends"),
  expr ("expr"),
  it ("it"),
  list ("list"),
  name ("name"),
  path ("path"),
  with ("with"),
  wrap ("wrap"),
  value ("value"),
  xescape ("xescape");

  String   literal;
  
  FmxAttr(String lit) {
    literal = lit;
    LocalData.map.put(literal, this);
  }

  private XmlAttr getAttr(@NotNull List<XmlAttr> attrs) {
    XmlAttr result = null;
    for (var attr : attrs) {
      if (FMX_NAMESPACE.equals(attr.getNsUri()) && attr.getLocalName().equals(literal)) {
        result = attr;
        break;
      }
    }
    return result;
  }
  
  public String getValue(@NotNull List<XmlAttr> attrs) {
    String result = null;
    var    attr   = getAttr(attrs);
    if (attr != null) {
      result = StringFunctions.cleanup(attr.getAttrValue());
    }
    return result;
  }
  
  public String getValue(@NotNull List<XmlAttr> attrs, String defValue) {
    var result = getValue(attrs);
    if (result == null) {
      result = defValue;
    }
    return result;
  }
  
  public String getRequiredValue(@NotNull List<XmlAttr> attrs, String errorMessage) {
    var result = getValue(attrs);
    if (result == null) {
      throw new FmxException(errorMessage);
    }
    return result;
  }
  
  public static FmxAttr valueByAttr(@NotNull Attr attr) {
    FmxAttr result = null;
    if (FMX_NAMESPACE.equals(attr.getNamespaceURI())) {
      result = LocalData.map.get(attr.getLocalName());
    }
    return result;
  }
  
  public static FmxAttr valueByXmlAttr(@NotNull XmlAttr attr) {
    FmxAttr result = null;
    if (FMX_NAMESPACE.equals(attr.getNsUri())) {
      result = LocalData.map.get(attr.getLocalName());
    }
    return result;
  }
  
  private static class LocalData {
    
    static final Map<String, FmxAttr> map = new HashMap<>();
    
  } /* ENDCLASS */
  
} /* ENDENUM */
