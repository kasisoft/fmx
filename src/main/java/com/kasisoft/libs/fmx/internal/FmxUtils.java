package com.kasisoft.libs.fmx.internal;

import org.w3c.dom.*;

import javax.annotation.*;

import lombok.experimental.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@UtilityClass
public class FmxUtils {

  public static String getTagName( @Nonnull Element element ) {
    String tag = element.getLocalName();
    if( element.getPrefix() != null ) {
      tag = String.format( "%s:%s", element.getPrefix(), tag );
    }
    return tag;
  }
        
} /* ENDCLASS */
