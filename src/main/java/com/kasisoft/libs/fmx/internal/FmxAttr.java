package com.kasisoft.libs.fmx.internal;

import com.kasisoft.libs.fmx.*;

import org.w3c.dom.*;

import javax.annotation.*;

import java.util.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum FmxAttr {

  it        ( "it"      ),
  depends   ( "depends" ),
  list      ( "list"    ),
  name      ( "name"    ),
  model     ( "model"   ),
  path      ( "path"    ),
  with      ( "with"    ),
  wrap      ( "wrap"    ),
  value     ( "value"   );

  String   literal;
  
  FmxAttr( String lit ) {
    literal = lit;
    LocalData.map.put( literal, this );
  }
  
  public Attr getAttr( Element element ) {
    return element.getAttributeNodeNS( FmxTranslator2.FMX_NAMESPACE, literal );
  }

  public static FmxAttr valueByAttr( @Nonnull Attr attr ) {
    FmxAttr result = null;
    if( FmxTranslator2.FMX_NAMESPACE.equals( attr.getNamespaceURI() ) ) {
      result = LocalData.map.get( attr.getLocalName() );
    }
    return result;
  }
  
  private static class LocalData {
    
    static final Map<String, FmxAttr> map = new HashMap<>();
    
  } /* ENDCLASS */
  
} /* ENDENUM */
