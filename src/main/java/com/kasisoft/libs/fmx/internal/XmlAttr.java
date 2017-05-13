package com.kasisoft.libs.fmx.internal;

import com.kasisoft.libs.common.model.*;

import javax.annotation.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XmlAttr extends Triple<String, String, String> implements Comparable<XmlAttr> {

  String   localName;
  String   prefix;
  
  public XmlAttr( String value1, String value2, String value3 ) {
    super( value1, value2, value3 );
    localName = value2;
    prefix    = null;
    int colon = value2.indexOf(':');
    if( colon > 0 ) {
      prefix    = value2.substring( 0, colon );
      localName = value2.substring( colon + 1 ); 
    }
  }
  
  public String getNsUri() {
    return getValue1();
  }
  
  public String getQName() {
    return getValue2();
  }

  public String getAttrValue() {
    return getValue3();
  }

  @Override
  public int compareTo( @Nonnull XmlAttr o ) {
    return getQName().compareTo( o.getQName() );
  }
  
} /* ENDCLASS */
