package com.kasisoft.libs.fmx.internal;

import static com.kasisoft.libs.fmx.FmxConstants.*;

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
public enum FmxElementType {

  // generate a doctype element
  doctype( "doctype" ),
  
  // include the mentioned content
  include( "include" ),
  
  // import the mentioned content
  importDecl( "import" ),
  
  // repeat the content multiple times
  list( "list" ),
  
  // emit the content only when a certain condition is met
  depends( "depends" ),
  
  // setup a variable with a certain name for the inner portion
  with( "with" ),
  
  // represent a directive
  directive( "directive" ),
  
  // escape block
  escape( "escape" ),
  
  // compress block
  compress( "compress" ),
  
  // pseudo element used to wrap the content
  root( "root" );
  
  String   literal;
  
  FmxElementType( String lit ) {
    literal = lit;
    LocalData.map.put( literal, this );
  }
  
  public static FmxElementType valueByNode( @Nonnull Node node ) {
    FmxElementType result = null;
    if( FMX_NAMESPACE.equals( node.getNamespaceURI() ) ) {
      result = LocalData.map.get( node.getLocalName() );
    }
    return result;
  }

  public static FmxElementType valueByNode( @Nonnull Node node, FmxElementType defaultValue ) {
    FmxElementType result = LocalData.map.get( node.getLocalName() );
    if( result == null ) {
      result = defaultValue;
    }
    return result;
  }

  private static class LocalData {
    
    static final Map<String, FmxElementType> map = new HashMap<>();
    
  } /* ENDCLASS */
  
} /* ENDENUM */
