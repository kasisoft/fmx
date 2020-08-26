package com.kasisoft.libs.fmx.internal;

import javax.validation.constraints.NotNull;

import lombok.experimental.FieldDefaults;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * @author daniel.kasmeroglu@kasisoft.com
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XmlAttr implements Comparable<XmlAttr> {

  String   nsUri;
  String   qName;
  String   attrValue;
  String   localName;
  String   prefix;
  
  public XmlAttr(String namespaceUri, String qualifiedName, String value) {
    nsUri     = namespaceUri;
    qName     = qualifiedName;
    attrValue = value;
    localName = qualifiedName;
    prefix    = null;
    int colon = qualifiedName.indexOf(':');
    if (colon > 0) {
      prefix    = qualifiedName.substring( 0, colon );
      localName = qualifiedName.substring( colon + 1 ); 
    }
  }
  
  @Override
  public int compareTo(@NotNull XmlAttr o) {
    return getQName().compareTo(o.getQName());
  }
  
} /* ENDCLASS */
