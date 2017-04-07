package com.kasisoft.libs.fmx.internal;

import static com.kasisoft.libs.fmx.FmxConstants.*;

import com.kasisoft.libs.common.text.*;
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
    return element.getAttributeNodeNS( FMX_NAMESPACE, literal );
  }
  
  public String getValue( Element element ) {
    String result = null;
    Attr   attr   = getAttr( element );
    if( attr != null ) {
      result = StringFunctions.cleanup( attr.getNodeValue() );
    }
    return result;
  }

  public String getValue( Element element, String defValue ) {
    String result = getValue( element );
    if( result == null ) {
      result = defValue;
    }
    return result;
  }

  public String getRequiredValue( Element element, String errorMessage ) {
    String result = getValue( element );
    if( result == null ) {
      throw new FmxException( errorMessage );
    }
    return result;
  }

  public static FmxAttr valueByAttr( @Nonnull Attr attr ) {
    FmxAttr result = null;
    if( FMX_NAMESPACE.equals( attr.getNamespaceURI() ) ) {
      result = LocalData.map.get( attr.getLocalName() );
    }
    return result;
  }
  
  private static class LocalData {
    
    static final Map<String, FmxAttr> map = new HashMap<>();
    
  } /* ENDCLASS */
  
} /* ENDENUM */
