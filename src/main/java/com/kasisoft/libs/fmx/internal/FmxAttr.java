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

  depends   ( "depends" ),
  expr      ( "expr"    ),
  it        ( "it"      ),
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

  private XmlAttr getAttr( List<XmlAttr> attrs ) {
    XmlAttr result = null;
    for( XmlAttr attr : attrs ) {
      if( FMX_NAMESPACE.equals( attr.getNsUri() ) && attr.getLocalName().equals( literal ) ) {
        result = attr;
        break;
      }
    }
    return result;
  }
  
  public String getValue( List<XmlAttr> attrs ) {
    String  result = null;
    XmlAttr attr   = getAttr( attrs );
    if( attr != null ) {
      result = StringFunctions.cleanup( attr.getAttrValue() );
    }
    return result;
  }
  
  public String getValue( List<XmlAttr> attrs, String defValue ) {
    String result = getValue( attrs );
    if( result == null ) {
      result = defValue;
    }
    return result;
  }
  
  public String getRequiredValue( List<XmlAttr> attrs, String errorMessage ) {
    String result = getValue( attrs );
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
  
  public static FmxAttr valueByXmlAttr( @Nonnull XmlAttr attr ) {
    FmxAttr result = null;
    if( FMX_NAMESPACE.equals( attr.getNsUri() ) ) {
      result = LocalData.map.get( attr.getLocalName() );
    }
    return result;
  }
  
  private static class LocalData {
    
    static final Map<String, FmxAttr> map = new HashMap<>();
    
  } /* ENDCLASS */
  
} /* ENDENUM */
