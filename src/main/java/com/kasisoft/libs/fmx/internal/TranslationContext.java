package com.kasisoft.libs.fmx.internal;

import static com.kasisoft.libs.fmx.FmxConstants.*;
import static com.kasisoft.libs.fmx.internal.Messages.*;

import com.kasisoft.libs.common.model.*;
import com.kasisoft.libs.common.text.*;
import com.kasisoft.libs.common.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.annotation.*;

import java.util.function.*;

import java.util.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class TranslationContext extends DefaultHandler implements AutoCloseable {

  private static final Bucket<StringFBuilder> STRINGFBUILDER = BucketFactories.newStringFBuilderBucket();
  
  StringFBuilder                builder;
  Function<String, String>      directiveProvider;
  StringFBuilder                replacer;
  long                          counter;
  Stack<Object>                 closings;
  Stack<Boolean>                fmxXml;
  Stack<Object>                 fmxXmlExpressions;
  Stack<Integer>                innerWraps;
  int                           lastOpen;
  Character                     consume;  
  BiPredicate<String, String>   isFmxRelevant;
  Predicate<List<XmlAttr>>      hasFmxAttribute;

  
  public TranslationContext( @Nonnull String fmxPrefix, Function<String, String> directives ) {
    builder           = STRINGFBUILDER.allocate();
    replacer          = STRINGFBUILDER.allocate();
    directiveProvider = directives;
    counter           = 0;
    closings          = new Stack<>();
    fmxXml            = new Stack<>();
    fmxXmlExpressions = new Stack<>();
    innerWraps        = new Stack<>();
    lastOpen          = -1;
    consume           = null;
    isFmxRelevant     = ($1, $2) -> FMX_NAMESPACE.equals( $1 ) || (($2 != null) && $2.startsWith( fmxPrefix ) );
    hasFmxAttribute   = $ -> $.parallelStream()
      .map( $_ -> isFmxRelevant.test( $_.getNsUri(), $_.getQName() ) )
      .reduce( false, ($1, $2) -> $1 || $2 );
  }
  
  private String newVar() {
    return String.format( "fmx_old%d", counter++ );
  }
  
  private void xmlAttribute( XmlAttr attr ) {
    String nsUri = attr.getNsUri();
    // only emit non-fmx attributes
    if( ! FMX_NAMESPACE.equals( nsUri ) ) {
      String nodeValue = escapeValue( attr.getAttrValue() );
      String prefix    = attr.getPrefix();
      String localName = attr.getLocalName();
      if( prefix != null ) {
        // generate a test for the attribute value indicated by the namespace
        if( FMT_NAMESPACE.equals( nsUri ) ) {
          builder.appendF( "[#if (%s)?has_content] %s=\"${%s}\"[/#if]", nodeValue, localName, nodeValue );
        } else {
          builder.appendF( " %s:%s=\"%s\"", prefix, localName, nodeValue );
        }
      } else {
        builder.appendF( " %s=\"%s\"", localName, nodeValue );
      }
    }
  }

  private void fmAttribute( XmlAttr attr ) {
    if( ! FMX_NAMESPACE.equals( attr.getNsUri() ) ) {
      builder.appendF( " %s=\"%s\"", attr.getQName(), attr.getAttrValue() );
    }
  }

  private String escapeValue( String nodeValue ) {
    replacer.setLength(0);
    replacer.append( nodeValue );
    for( int i = replacer.length() - 1; i >= 0; i-- ) {
      char ch = replacer.charAt(i);
      if( ch == '\"' ) {
        replacer.deleteCharAt(i);
        replacer.insert( i, "&quot;" );
      } else if( ch == '<' ) {
        replacer.deleteCharAt(i);
        replacer.insert( i, "&lt;" );
      } else if( ch == '>' ) {
        replacer.deleteCharAt(i);
        replacer.insert( i, "&gt;" );
      }
    }
    return replacer.toString();
  }

  @Override
  public void close() {
    if( builder != null ) {
      STRINGFBUILDER.free( builder  );
      STRINGFBUILDER.free( replacer );
      builder  = null;
      replacer = null;
    }
  }
  
  @Override
  public String toString() {
    return builder.toString();
  }

  @Override
  public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException {
    boolean       isFmxXml = false;
    List<XmlAttr> attrs    = getAttributes( attributes );
    if( isFmxRelevant.test( uri, qName ) ) {
      // dedicated fmx:??? element
      startFmxElement( uri, localName, qName, attrs );
    } else if( hasFmxAttribute.test( attrs ) ) {
      // xml element which contains an fmx attribute
      startFmxXmlElement( uri, localName, qName, attrs );
      isFmxXml = true;
    } else {
      // simple xml element
      startXmlElement( uri, localName, qName, attrs );
    }
    fmxXml.push( isFmxXml );
  }
  
  @Override
  public void endElement( String uri, String localName, String qName ) throws SAXException {
    boolean isFmxXml = fmxXml.pop();
    if( isFmxRelevant.test( uri, qName ) ) {
      // dedicated fmx:??? element
      endFmxElement( uri, localName, qName );
    } else if( isFmxXml ) {
      // xml element which contains an fmx attribute
      endFmxXmlElement( uri, localName, qName );
    } else {
      // simple xml element
      endXmlElement( uri, localName, qName );
    }
  }
  
  private void startFmxElement( String uri, String localName, String qName, List<XmlAttr> attrs ) {
    FmxElementType type = FmxElementType.valueByName( localName, FmxElementType.directive );
    switch( type ) {
    case directive  : emitDirectiveOpen( uri, localName, qName, attrs ); break;
    case doctype    : emitDoctypeOpen  ( uri, localName, qName, attrs ); break;
    case include    : emitIncludeOpen  ( uri, localName, qName, attrs ); break;
    case importDecl : emitImportOpen   ( uri, localName, qName, attrs ); break;
    case list       : emitListOpen     ( uri, localName, qName, attrs ); break;
    case depends    : emitDependsOpen  ( uri, localName, qName, attrs ); break;
    case with       : emitWithOpen     ( uri, localName, qName, attrs ); break;
    case escape     : emitEscapeOpen   ( uri, localName, qName, attrs ); break;
    case compress   : emitCompressOpen ( uri, localName, qName, attrs ); break;
    }
  }
  
  private void endFmxElement( String uri, String localName, String qName ) {
    FmxElementType type = FmxElementType.valueByName( localName, FmxElementType.directive );
    switch( type ) {
    case directive  : emitDirectiveClose ( uri, localName, qName ); break;
    case list       : emitListClose      ( uri, localName, qName ); break;
    case depends    : emitDependsClose   ( uri, localName, qName ); break;
    case with       : emitWithClose      ( uri, localName, qName ); break;
    case escape     : emitEscapeClose    ( uri, localName, qName ); break;
    case compress   : emitCompressClose  ( uri, localName, qName ); break;
    }
  }

  private void startFmxXmlElement( String uri, String localName, String qName, List<XmlAttr> attrs ) {

    String dependsExpression  = FmxAttr . depends . getValue( attrs );
    String listExpression     = FmxAttr . list    . getValue( attrs );
    String iteratorName       = FmxAttr . it      . getValue( attrs, "it" );
    String withExpression     = FmxAttr . with    . getValue( attrs );
    String withName           = FmxAttr . name    . getValue( attrs, "model" );
    String wrapExpression     = FmxAttr . wrap    . getValue( attrs );
    
    openDepends( dependsExpression );
    openWith( withExpression, withName );
    openList( listExpression, iteratorName );
    openWrap( wrapExpression );
    
    startXmlElement( uri, localName, qName, attrs );
    
    innerWraps.push( builder.length() );
    
  }

  private void endFmxXmlElement( String uri, String localName, String qName ) {
    int open  = innerWraps.pop();
    int close = builder.length();
    endXmlElement( uri, localName, qName );
    builder.append( "\n" );
    consume = '\n';
    closeWrap( open, close );
    closeList();
    closeWith();
    closeDepends();
  }

  private void startXmlElement( String uri, String localName, String qName, List<XmlAttr> attrs ) {
    String prefix = null;
    int    colon  = qName.indexOf(':');
    if( colon > 0 ) {
      prefix = qName.substring( 0, colon );
    }
    builder.appendF( "<%s", qName );
    attrs.forEach( this::xmlAttribute );
    if( prefix != null ) {
      xmlAttribute( new XmlAttr( null, String.format( "xmlns:%s", prefix ), uri ) );
    }
    builder.append( ">" );
    lastOpen = builder.length();
  }
  
  private void endXmlElement( String uri, String localName, String qName ) {
    if( builder.length() == lastOpen ) {
      builder.setLength( builder.length() - 1 );
      builder.append( "/>" );
    } else {
      builder.appendF( "</%s>", qName );
    }
  }
  
  // compress
  
  private void emitCompressOpen( String uri, String localName, String qName, List<XmlAttr> attrs ) {
    builder.append( "[#compress]" );
  }

  private void emitCompressClose( String uri, String localName, String qName ) {
    builder.append( "[/#compress]" );
  }

  // with
  
  private void emitEscapeOpen( String uri, String localName, String qName, List<XmlAttr> attrs ) {
    String name = FmxAttr.name.getRequiredValue( attrs, error_escape_without_name );
    String expr = FmxAttr.expr.getRequiredValue( attrs, error_escape_without_expr );
    builder.appendF( "[#escape %s as %s]", name, expr );
  }

  private void emitEscapeClose( String uri, String localName, String qName ) {
    builder.append( "[/#escape]" );
  }

  // with
  
  private void emitWithOpen( String uri, String localName, String qName, List<XmlAttr> attrs ) {
    String modelName  = FmxAttr.name.getValue( attrs, "model" );
    String modelExpr  = FmxAttr.value.getRequiredValue( attrs, error_with_values );
    // this variable name is used in case it already exists within the model, so we're backing it up before
    String var = newVar();
    builder.appendF( "[#assign %s=%s! /]\n", var, modelName );
    builder.appendF( "[#assign %s=%s /]", modelName, modelExpr );
    closings.push( new Pair<String, String>( modelName, var ) );
  }

  private void emitWithClose( String uri, String localName, String qName ) {
    Pair<String, String> pair = (Pair<String, String>) closings.pop();
    builder.appendF( "[#assign %s=%s /]\n", pair.getValue1(), pair.getValue2() );
  }

  // directive
  
  private void emitDirectiveOpen( String uri, String localName, String qName, List<XmlAttr> attrs ) {
    String name = directiveProvider.apply( localName );
    builder.appendF( "[@%s", name );
    attrs.forEach( this::fmAttribute );
    builder.append( "]" );
  }

  private void emitDirectiveClose( String uri, String localName, String qName ) {
    String name = directiveProvider.apply( localName );
    builder.appendF( "[/@%s]", name );
  }

  // depends
  
  private void emitDependsOpen( String uri, String localName, String qName, List<XmlAttr> attrs ) {
    String dependsExpr = FmxAttr.value.getRequiredValue( attrs, error_depends_values );
    builder.appendF( "[#if %s]", dependsExpr );
  }

  private void emitDependsClose( String uri, String localName, String qName ) {
    builder.appendF( "[/#if]" );
  }

  // doctype
  
  private void emitDoctypeOpen( String uri, String localName, String qName, List<XmlAttr> attrs ) {
    builder.appendF( "<!doctype %s>", FmxAttr.value.getValue( attrs, "html" ) );
  }

  // include
  
  private void emitIncludeOpen( String uri, String localName, String qName, List<XmlAttr> attrs ) {
    String path = FmxAttr.path.getRequiredValue( attrs, error_include_values );
    builder.appendF( "[#include '%s' /]", path );
  }

  // import
  
  private void emitImportOpen( String uri, String localName, String qName, List<XmlAttr> attrs ) {
    String path = FmxAttr.path.getRequiredValue( attrs, error_import_values );
    String name = FmxAttr.name.getRequiredValue( attrs, error_import_values );
    builder.appendF( "[#import '%s' as %s /]", path, name );
  }
  
  // list
  
  private void emitListOpen( String uri, String localName, String qName, List<XmlAttr> attrs ) {
    String listExpression = FmxAttr.value.getRequiredValue( attrs, error_list_values );
    String iteratorName   = FmxAttr.it.getValue( attrs, "it" );
    builder.appendF( "[#list %s as %s]", listExpression, iteratorName );
  }

  private void emitListClose( String uri, String localName, String qName ) {
    builder.appendF( "[/#list]" );
  }
  
  private void openWrap( String wrapExpression ) {
    String offset = "";
    if( wrapExpression != null ) {
      offset = getOffset();
      builder.appendF( "[#if %s]\n", wrapExpression );
      builder.append( offset );
    }
    fmxXmlExpressions.push( new Pair<Boolean, String>( wrapExpression != null, offset ) );
  }
  
  private String getOffset() {
    String result = "";
    int i = builder.length() - 1;
    while( i >= 0 ) {
      if( builder.charAt( i ) == '\n' ) {
        result = builder.substring( i + 1, builder.length() );
        break;
      }
      i--;
    }
    return result;
  }
  
  private void closeWrap( int open, int close ) {
    Pair<Boolean, String> expr = (Pair<Boolean, String>) fmxXmlExpressions.pop();
    boolean hasWrapExpression = expr.getValue1();
    if( hasWrapExpression ) {
      builder.append( expr.getValue2() );
      builder.append( "[#else]" );
      builder.append( builder.substring( open, close ) );
      builder.append( "[/#if]\n" );
    }
  }

  private void openWith( String withExpression, String modelName ) {
    Pair<String, String> result = null;
    if( withExpression != null ) {
      String varname = newVar();
      builder.appendF( "[#assign %s=%s! /]\n", varname, modelName );
      builder.appendF( "[#assign %s=%s /]\n", modelName, withExpression );
      result = new Pair<>( modelName, varname );
    }
    fmxXmlExpressions.push( result );
  }
  
  private void closeWith() {
    Pair<String, String> result = (Pair<String, String>) fmxXmlExpressions.pop();
    if( result != null ) { 
      builder.appendF( "[#assign %s=%s /]\n", result.getValue1(), result.getValue2() );
    }
  }

  private void openList( String listExpression, String iteratorName ) {
    if( listExpression != null ) {
      builder.appendF( "[#list %s as %s]\n", listExpression, iteratorName );
    }
    fmxXmlExpressions.push( listExpression != null );
  }
  
  private void closeList() {
    boolean hasListExpression = (Boolean) fmxXmlExpressions.pop();
    if( hasListExpression ) { 
      builder.append( "[/#list]\n" );
    }
  }

  private void openDepends( String dependsExpression ) {
    if( dependsExpression != null ) {
      builder.appendF( "[#if %s]\n", dependsExpression );
    }
    fmxXmlExpressions.push( dependsExpression != null );
  }
  
  private void closeDepends() {
    boolean hasDependsExpression = (Boolean) fmxXmlExpressions.pop();
    if( hasDependsExpression ) {
      builder.append( "[/#if]\n" );
    }
  }

  @Override
  public void characters( char ch[], int start, int length ) throws SAXException {
    int pos = builder.length();
    builder.append( ch, start, length );
    if( consume != null ) {
      for( int i = pos; i < pos + length; i++ ) {
        if( builder.charAt(i) == consume.charValue() ) {
          builder.deleteCharAt(i);
          consume = null;
          break;
        }
      }
    }
  }

  @Override
  public void ignorableWhitespace( char ch[], int start, int length ) throws SAXException {
    characters( ch, start, length );
  }
  
  private List<XmlAttr> getAttributes( Attributes attributes ) {
    List<XmlAttr> result = Collections.emptyList();
    if( attributes != null ) {
      result = new ArrayList<>( attributes.getLength() );
      for( int i = 0; i < attributes.getLength(); i++ ) {
        result.add( new XmlAttr( attributes.getURI(i), attributes.getQName(i), attributes.getValue(i) ) );
      }
      Collections.sort( result );
    }
    return result;
  }

} /* ENDCLASS */
