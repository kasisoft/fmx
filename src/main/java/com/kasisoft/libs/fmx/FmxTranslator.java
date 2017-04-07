package com.kasisoft.libs.fmx;

import static com.kasisoft.libs.fmx.internal.Messages.*;
import static com.kasisoft.libs.fmx.FmxConstants.*;

import com.kasisoft.libs.fmx.internal.*;

import org.slf4j.*;
import org.w3c.dom.*;
import org.w3c.dom.Element;

import javax.annotation.*;
import javax.xml.bind.*;

import java.util.function.*;

import java.util.*;

import java.io.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FmxTranslator {
  
  private static final Logger log = LoggerFactory.getLogger( FmxTranslator.class );

  // a root element so it's made sure that multiple xml elements are embedded in a root element
  private static final String WRAPPER = "<%s:root xmlns:%s=\"%s\" xmlns:%s=\"%s\">%%s</%s:root>";
  
  String                     nsPrefix;
  String                     nsTPrefix;
  String                     wrapper;
  Function<String, String>   directiveProvider;
  
  public FmxTranslator() {
    this( null, null, null );
  }
  
  public FmxTranslator( Function<String, String> directives ) {
    this( null, null, directives );
  }
  
  public FmxTranslator( String prefix, String tPrefix, Function<String, String> directives ) {
    nsPrefix          = prefix  != null ? prefix  : FMX_PREFIX;
    nsTPrefix         = tPrefix != null ? tPrefix : FMT_PREFIX;
    wrapper           = String.format( WRAPPER, nsPrefix, nsPrefix, FMX_NAMESPACE, nsTPrefix, FMT_NAMESPACE, nsPrefix );
    directiveProvider = directives != null ? directives : $ -> $;
  }
  
  
  /**
   * This main function converts the supplied xml based input into the corresponding fmx ftl code.
   * 
   * @param xmlInput   The xml based ftl input.
   * 
   * @return   The fmx ftl code.
   * 
   * @throws   FmxException
   */
  public String convert( @Nonnull String xmlInput ) {
    try {
      NodeWrapper rootWrapper = decorate( xmlInput );
      String      result      = null;
      logWrapper( rootWrapper );
      if( rootWrapper != null ) {
        try( TranslationContext ctx = new TranslationContext( directiveProvider ) ) {
          rootWrapper.emit( ctx );
          result = ctx.toString();
        }
      }
      return result;
    } catch( Exception ex ) {
      throw FmxException.wrap( ex );
    }
  }
  
  private void logWrapper( NodeWrapper wrapper ) {
    if( log.isTraceEnabled() ) {
      if( wrapper != null ) {
        log.trace( "{}", wrapper );
      } else {
        log.trace( error_no_root_element );
      }
    }
  }
  
  private NodeWrapper decorate( String xmlInput ) throws IOException {
    String wrapped     = String.format( wrapper, xmlInput );
    Node   rootElement = null;
    try( Reader reader = new StringReader( wrapped ) ) {
      rootElement = (Node) JAXB.unmarshal( reader, Object.class );
    }
    NodeWrapper result = null;
    if( rootElement != null ) {
      result = decorate( rootElement ); 
    }
    return result;
  }
  
  private NodeWrapper decorate( Node node ) {
    NodeWrapper result = null;
    if( node.getNodeType() == Node.ELEMENT_NODE ) {
      Element     element    = (Element) node;
      List<Attr>  attributes = getAttributes( element.getAttributes() );
      if( IS_FMX_RELEVANT.test( element ) ) {
        // a specific fmx element
        result = new FmxElement( element, FmxElementType.valueByNode( element, FmxElementType.directive ), attributes );
      } else if( HAS_FMX_ATTRIBUTE.test( attributes ) ) {
        // some fmx attributes
        result = new FmxXmlElement( element, attributes );
      } else {
        // simple xml element
        result = new XmlElement( element, attributes );
      }
      
    } else {
      // simple node
      result = new XmlNode( node );
    }
    addChildren( result );
    return result;
  }
  
  private void addChildren( NodeWrapper parent ) {
    NodeList children = parent.getNode().getChildNodes();
    for( int i = 0; i < children.getLength(); i++ ) {
      parent.getChildren().add( decorate( children.item(i) ) );
    }
  }
    
  private List<Attr> getAttributes( NamedNodeMap namedNodeMap ) {
    List<Attr> result = Collections.emptyList();
    if( namedNodeMap != null ) {
      result = new ArrayList<>( namedNodeMap.getLength() );
      for( int i = 0; i < namedNodeMap.getLength(); i++ ) {
        result.add( (Attr) namedNodeMap.item(i) );
      }
      Collections.sort( result, this::compareAttr );
    }
    return result;
  }
  
  private int compareAttr( Attr attr1, Attr attr2 ) {
    String s1 = attr1.getLocalName();
    String s2 = attr2.getLocalName();
    return s1.compareTo( s2 );
  }

} /* ENDCLASS */
