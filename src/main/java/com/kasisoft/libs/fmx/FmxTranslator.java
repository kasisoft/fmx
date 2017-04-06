package com.kasisoft.libs.fmx;

import static com.kasisoft.libs.fmx.internal.Messages.*;

import com.kasisoft.libs.common.text.*;
import com.kasisoft.libs.common.util.*;
import com.kasisoft.libs.common.xml.*;

import org.slf4j.*;
import org.w3c.dom.*;
import org.w3c.dom.Element;

import javax.xml.bind.*;

import java.util.function.*;

import java.util.*;

import java.io.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class FmxTranslator {
  
  private static final Logger log = LoggerFactory.getLogger( FmxTranslator.class );

  private static final String FMX_NAMESPACE       = "https://kasisoft.com/namespaces/fmx/0.1";
  private static final String FMX_NAMESPACE_S     = "https://kasisoft.com/namespaces/fmx/0.1/";
  
  private static final String DEFAULT_PREFIX      = "fmx";
  
  private static final String WRAPPER             = "<fmx:wrappingElement xmlns:fmx=\"%s\">%s</fmx:wrappingElement>";
  
  // TODO: properly distinguish elements from attributes (incl. ns)
  private static final String FMX_DOCTYPE         = "doctype";
  private static final String FMX_LIST            = "list";
  private static final String FMX_IT              = "it";
  private static final String FMX_DEPENDS         = "depends";
  private static final String FMX_DISABLE_DEPENDS = "disable-depends";
  private static final String FMX_INCLUDE         = "include";
  private static final String FMX_MODEL           = "model";
  private static final String FMX_NAME            = "name";
  private static final String FMX_PATH            = "fmx:path";
  private static final String FMX_VALUE           = "fmx:value";
  private static final String FMX_WITH            = "with";
  private static final String FMX_WRAP            = "wrap";
  
  private static final Set<String> IGNORE = new HashSet<>( Arrays.asList(
    FMX_IT, FMX_LIST, FMX_DEPENDS, FMX_DISABLE_DEPENDS, FMX_MODEL, FMX_NAME, FMX_WRAP, "path", "value"
  ) );
  
  private static final Bucket<StringFBuilder> STRINGFBUILDER = BucketFactories.newStringFBuilderBucket();
  private static final Bucket<List<Attr>>     LIST_ATTR      = BucketFactories.newArrayListBucket();
  
  private long   counter = 0;
  
  public String convert( String xmlInput ) {
    return STRINGFBUILDER.forInstance( this::convertImpl, new StringReader( String.format( WRAPPER, FMX_NAMESPACE, xmlInput ) ) );
  }
  
  private String convertImpl( StringFBuilder builder, Reader reader ) {
    Node          wrapper   = (Node) JAXB.unmarshal( reader, Object.class );
    List<Element> elements  = XmlFunctions.getChildElements( wrapper );
    StringBuilder indention = new StringBuilder();
    for( Element element : elements ) {
      element.normalize();
      serialize( builder, indention, element );
      indention.setLength(0);
    }
    return builder.toString();
  }

  private List<Node> getChildren( Node parent ) {
    List<Node> result   = Collections.emptyList();
    NodeList   children = parent.getChildNodes();
    if( children != null ) {
      result = new ArrayList<>( children.getLength() );
      for( int i = 0; i < children.getLength(); i++ ) {
        Node node = children.item(i);
        if( node.getNodeType() == Node.TEXT_NODE ) {
          Text text = (Text) node;
          if( StringFunctions.cleanup( text.getTextContent() ) == null ) {
            continue;
          }
        }
        result.add( node );
      }
    }
    return result;
  }
  
  private void serialize( StringFBuilder builder, StringBuilder indention, Node node ) {
    if( hasFmxRelevantPortion( node ) ) {
      serializeFmxNode( builder, indention, node );
    } else {
      serializeOrdinaryNode( builder, indention, node );
    }
  }
  
  private void serializeFmxNode( StringFBuilder builder, StringBuilder indention, Node node ) {
    
    List<Attr>  standardAttributes  = LIST_ATTR.allocate();
    List<Attr>  fmxAttributes       = LIST_ATTR.allocate();
    
    try {
    
      collectAttributes( node.getAttributes(), standardAttributes, fmxAttributes );
      
      String       list            = parseSingle  ( FMX_LIST            , fmxAttributes );
      String       iterator        = parseSingle  ( FMX_IT              , fmxAttributes );
      String       wrap            = parseSingle  ( FMX_WRAP            , fmxAttributes );
      List<String> depends         = parseMany    ( FMX_DEPENDS         , fmxAttributes );
      List<String> disableDepends  = parseMany    ( FMX_DISABLE_DEPENDS , fmxAttributes );
      String       withModel       = parseSingle  ( FMX_MODEL           , fmxAttributes );
      String       withName        = parseSingle  ( FMX_NAME            , fmxAttributes );
      
      List<Node> children = getChildren( node );
      if( wrap != null ) {
        builder.appendF( "%s[#if %s]\n", indention, wrap );
      }
      openDepends( builder, indention, depends, disableDepends );
      openList( builder, indention, list, iterator );
      if( isFmxRelevant( node ) ) {
        String  name   = node.getLocalName().replace( '-', '.' );
        if( FMX_WITH.equals( name ) ) {
          fmxWith( builder, indention, withModel, withName, children );
        } else if( FMX_INCLUDE.equals( name ) ) {
          String path = ((Element) node).getAttribute( FMX_PATH );
          builder.appendF( "[#include '%s' /]\n", path );
        } else if( FMX_DOCTYPE.equals( name ) ) {
          String value = ((Element) node).getAttribute( FMX_VALUE );
          builder.appendF( "<!doctype %s>\n", value );
        } else {
          fmxDefault( builder, indention, node, standardAttributes, fmxAttributes, children, name );
        }
      } else {
        serializeOrdinaryNode( builder, indention, node );
      }
      closeFtlTag( builder, indention, "list", list );
      closeFtlTag( builder, indention, "if", depends );
      
      if( wrap != null ) {
        builder.appendF( "%s[#else]\n", indention );
        serializeChildren( builder, indention, children );
        builder.appendF( "%s[/#if]\n", indention );
      }
      

    } finally {
      LIST_ATTR.free( standardAttributes );
      LIST_ATTR.free( fmxAttributes      );
    }
    
  }

  private void fmxDefault( StringFBuilder builder, StringBuilder indention, Node node, List<Attr> standardAttributes, List<Attr> fmxAttributes, List<Node> children, String name ) {
    if( isEmpty( children ) ) {
      if( node.getNodeType() == Node.TEXT_NODE ) {
        builder.append( node.getNodeValue() );
      } else {
        builder.appendF( "%s[@%s", indention, name );
        serializeAttributes( builder, standardAttributes, false );
        serializeAttributes( builder, fmxAttributes, true );
        builder.appendF( " /]\n" );
      }
    } else {
      builder.appendF( "%s[@%s", indention, name );
      serializeAttributes( builder, standardAttributes, false );
      serializeAttributes( builder, fmxAttributes, true );
      builder.appendF( "]" );
      serializeChildren( builder, indention, children );
      builder.appendF( "[/@%s]\n", name );
    }
  }

  private void fmxWith( StringFBuilder builder, StringBuilder indention, String withModel, String withName, List<Node> children ) {
    if( withName == null ) {
      withName = "model";
    }
    if( withModel != null ) {
      long idx = counter++;
      builder.appendF( "[#assign fmxOldModel%d=%s! /]\n", idx, withName );
      builder.appendF( "[#assign %s=%s /]\n", withName, withModel );
      serializeChildren( builder, indention, children );
      builder.appendF( "[#assign %s=fmxOldModel%d /]\n", withName, idx );
    } else {
      log.warn( missing_fmx_model );
    }
  }

  private void closeFtlTag( StringFBuilder builder, StringBuilder indention, String tag, Object test ) {
    if( test != null ) {
      builder.appendF( "%s[/#%s]\n", indention, tag );
    }
  }
  
  private void openList( StringFBuilder builder, StringBuilder indention, String list, String iterator ) {
    if( list != null ) {
      String it = "it";
      if( iterator != null ) {
        it = iterator;
      }
      builder.appendF( "%s[#list %s as %s]\n", indention, list, it );
    }
  }

  private void openDepends( StringFBuilder builder, StringBuilder indention, List<String> depends, List<String> disableDepends ) {
    if( depends != null ) {
      builder.appendF( "%s[#if ", indention );
      if( disableDepends != null ) {
        builder.append("(");
        for( String expr : disableDepends ) {
          builder.appendF( "%s || ", expr, expr, expr );
        }
        builder.setLength( builder.length() - " || ".length() );
        builder.append(") || ");
      }
      for( String expr : depends ) {
        builder.appendF( "%s && ", expr, expr, expr );
      }
      builder.setLength( builder.length() - " && ".length() );
      builder.appendF( "]\n" );
    }
  }
  
  private void serializeOrdinaryNode( StringFBuilder builder, StringBuilder indention, Node node ) {
    List<Node> children           = getChildren( node );
    List<Attr> standardAttributes = LIST_ATTR.allocate();
    List<Attr> fmxAttributes      = LIST_ATTR.allocate();
    try {
      collectAttributes( node.getAttributes(), standardAttributes, fmxAttributes );
      if( isEmpty( children ) ) {
        if( node.getNodeType() == Node.TEXT_NODE ) {
          builder.append( node.getNodeValue() );
        } else {
          builder.appendF( "%s<%s", indention, node.getNodeName() );
          serializeAttributes( builder, standardAttributes, false );
          serializeAttributes( builder, fmxAttributes, true );
          builder.appendF( " />\n" );
        }
      } else {
        builder.appendF( "%s<%s", indention, node.getNodeName() );
        serializeAttributes( builder, standardAttributes, false );
        serializeAttributes( builder, fmxAttributes, true );
        builder.appendF( ">" );
        serializeChildren( builder, indention, children );
        builder.appendF( "</%s>\n", node.getNodeName() );
      }
    } finally {
      LIST_ATTR.free( standardAttributes );
      LIST_ATTR.free( fmxAttributes      );
    }
  }
  
  private void serializeChildren( StringFBuilder builder, StringBuilder indention, List<Node> children ) {
    boolean indent = (children.size() > 1) || (children.get(0).getNodeType() != Node.TEXT_NODE);
    int     len = -1;
    if( indent ) {
      builder.appendF( "\n" );
      len = indention.length();
      indention.append( "  ");
    }
    for( Node child : children ) {
      serialize( builder, indention, child );
    }
    if( indent ) {
      indention.setLength( len );
      builder.append( indention );
    }
  }
  
  private void collectAttributes( NamedNodeMap attributes, List<Attr> normal, List<Attr> fmx ) {
    Consumer<Attr> normalAdd = normal != null ? normal::add : a -> {};
    Consumer<Attr> fmxAdd    = fmx    != null ? fmx::add    : a -> {};
    if( ! isEmpty( attributes ) ) {
      for( int i = 0; i < attributes.getLength(); i++ ) {
        Attr attr = (Attr) attributes.item(i);
        if( isFmxRelevant( attr ) ) {
          fmxAdd.accept( attr );
        } else if( ! isFmxNamespace( attr.getNodeValue() ) ) {
          normalAdd.accept( attr );
        }
      }
    }
    Collections.sort( normal, this::compareAttributes );
  }
  
  private void serializeAttributes( StringFBuilder builder, List<Attr> attributes, boolean test ) {
    if( ! isEmpty( attributes ) ) {
      attributes.forEach( $ -> serializeAttribute( builder, $, test ) );
    }
  }
  
  private void serializeAttribute( StringFBuilder builder, Attr attr, boolean test ) {
    if( test ) {
      String name = attr.getLocalName();
      if( ! IGNORE.contains( name ) ) {
        builder.appendF( "[#if (%s)?has_content] %s=\"${%s}\"[/#if]", attr.getNodeValue(), name, attr.getNodeValue() );
      }
    } else {
      builder.appendF( " %s=\"%s\"", attr.getNodeName(), attr.getNodeValue() );
    }
  }
  
  private int compareAttributes( Attr attr1, Attr attr2 ) {
    String name1 = attr1.getNodeName();
    String name2 = attr2.getNodeName();
    return name1.compareTo( name2 );
  }
  
  private boolean hasFmxRelevantPortion( Node node ) {
    boolean result = isFmxRelevant( node );
    if( ! result ) {
      NamedNodeMap attributes = node.getAttributes(); 
      if( ! isEmpty( attributes ) ) {
        for( int i = 0; i < attributes.getLength(); i++ ) {
          result = isFmxRelevant( attributes.item(i) );
          if( result ) {
            break;
          }
        }
      }
    }
    return result;
  }

  private boolean isFmxRelevant( Node node ) {
    return isFmxNamespace( node.getNamespaceURI() ) || DEFAULT_PREFIX.equals( node.getPrefix() );
  }

  private boolean isEmpty( List list ) {
    return (list == null) || list.isEmpty();
  }

  private boolean isEmpty( NamedNodeMap map ) {
    return (map == null) || (map.getLength() == 0);
  }

  private boolean isFmxNamespace( String text ) {
    return FMX_NAMESPACE.equals( text ) || FMX_NAMESPACE_S.equals( text );
  }
  
  private String parseSingle( String name, List<Attr> attributes ) {
    String result = null;
    for( int i = attributes.size() - 1; i >= 0; i-- ) {
      Attr attr = attributes.get(i);
      if( name.equals( attr.getLocalName() ) ) {
        attributes.remove(i);
        result = StringFunctions.cleanup( attr.getNodeValue() );
      }
    }
    return result;
  }

  private List<String> parseMany( String name, List<Attr> attributes ) {
    List<String> result = null;
    for( int i = attributes.size() - 1; i >= 0; i-- ) {
      Attr attr = attributes.get(i);
      if( name.equals( attr.getLocalName() ) ) {
        attributes.remove(i);
        List<String> values = getValues( attr );
        if( ! values.isEmpty() ) {
          result = values;
        }
      }
    }
    return result;
  }

  private List<String> getValues( Node node ) {
    List<String> result = Collections.emptyList();
    String value = StringFunctions.cleanup( node.getNodeValue() );
    if( value != null ) {
      result = new ArrayList<>( Arrays.asList( value.split( "," ) ) );
      for( int j = result.size() - 1; j >= 0; j-- ) {
        String v = StringFunctions.cleanup( result.get(j) );
        if( v == null ) {
          result.remove(j);
        } else {
          result.set( j, v );
        }
      }
    }
    return result;
  }

} /* ENDCLASS */
