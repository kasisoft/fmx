package com.kasisoft.libs.fmx;

import com.kasisoft.libs.common.text.*;
import com.kasisoft.libs.common.util.*;
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
public class FmxTranslator2 {
  
  private static final Logger log = LoggerFactory.getLogger( FmxTranslator2.class );

  public  static final String FMX_NAMESPACE       = "https://kasisoft.com/namespaces/fmx/0.1";
  public  static final String FMT_NAMESPACE       = "https://kasisoft.com/namespaces/fmt/0.1";
  
  private static final String FMT_PREFIX          = "fmt";
  private static final String FMX_PREFIX          = "fmx";
  
  private static final String WRAPPER             = "<%s:root xmlns:%s=\"%s\" xmlns:%s=\"%s\">%%s</%s:root>";
  
  private static final Bucket<StringFBuilder> STRINGFBUILDER = BucketFactories.newStringFBuilderBucket();
  
  String                     nsPrefix;
  String                     nsTPrefix;
  String                     wrapper;
  Function<String, String>   directiveProvider;
  
  public FmxTranslator2() {
    this( null, null, null );
  }
  
  public FmxTranslator2( String prefix, String tPrefix, Function<String, String> directives ) {
    nsPrefix          = prefix  != null ? prefix  : FMX_PREFIX;
    nsTPrefix         = tPrefix != null ? tPrefix : FMT_PREFIX;
    wrapper           = String.format( WRAPPER, nsPrefix, nsPrefix, FMX_NAMESPACE, nsTPrefix, FMT_NAMESPACE, nsPrefix );
    directiveProvider = directives != null ? directives : $ -> $;
  }
  
  
  public String convert( @Nonnull String xmlInput ) {
    NodeWrapper rootWrapper = decorate( xmlInput );
    String      result      = null;
    if( rootWrapper != null ) {
      if( log.isTraceEnabled() ) {
        log.trace( "{}", rootWrapper );
      }
      result = STRINGFBUILDER.forInstance( this::emit, rootWrapper );
    } else {
      if( log.isTraceEnabled() ) {
        log.trace( "no root element" );
      }
    }
    return result;
  }
  
  private String emit( StringFBuilder builder, NodeWrapper wrapper ) {
    TranslationContext context = new TranslationContext( builder, directiveProvider );
    wrapper.emit( context );
    return builder.toString();
  }
  
  private NodeWrapper decorate( String xmlInput ) {
    String wrapped     = String.format( wrapper, xmlInput );
    Node   rootElement = null;
    try( Reader reader = new StringReader( wrapped ) ) {
      rootElement = (Node) JAXB.unmarshal( reader, Object.class );
    } catch( Exception ex ) {
      ex.printStackTrace();
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
      if( isFmxRelevant( element ) ) {
        result = new FmxElement( element, FmxElementType.valueByNode( element, FmxElementType.directive ), attributes );
      } else if( hasFmxAttribute( attributes ) ) {
        result = new FmxXmlElement( element, attributes );
      } else {
        result = new XmlElement( element, attributes );
      }
    } else {
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
    
  private boolean hasFmxAttribute( List<Attr> attributes ) {
    return attributes.parallelStream()
      .map( this::isFmxRelevant )
      .reduce( false, ($1, $2) -> $1 || $2 );
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
  
  private boolean isFmxRelevant( Node node ) {
    return isFmxNamespace( node.getNamespaceURI() ) || FMX_PREFIX.equals( node.getPrefix() );
  }

  private boolean isFmxNamespace( String text ) {
    return FMX_NAMESPACE.equals( text );
  }

} /* ENDCLASS */
