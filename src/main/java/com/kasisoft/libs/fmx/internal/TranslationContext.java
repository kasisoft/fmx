package com.kasisoft.libs.fmx.internal;

import static com.kasisoft.libs.fmx.FmxConstants.*;
import static com.kasisoft.libs.fmx.internal.Messages.*;

import com.kasisoft.libs.common.text.*;
import com.kasisoft.libs.common.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.annotation.*;

import java.util.function.*;

import java.util.stream.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class TranslationContext extends DefaultHandler {

  private static final Bucket<StringFBuilder> STRINGFBUILDER = BucketFactories.newStringFBuilderBucket();
  private static final AtomicLong             COUNTER        = new AtomicLong();
  
  String                        content;
  StringFBuilder                replacer;
  StringFBuilder                builder;
  Locator                       locator;
  
  // mapper for directive names
  Function<String, String>      directiveProvider;
  
  // test for fmx configurations (uri, prefix)
  BiPredicate<String, String>   isFmxRelevant;
  Predicate<List<XmlAttr>>      hasFmxAttribute;
  
  // test if an attribute needs to be put into a context map
  Predicate<XmlAttr>            isCtxAttribute;
  
  // a stack of flags per element: the scenario of a normal xml element which contains fmx attributes
  // requires attributes which aren't passed through endElement so we need to remember
  Stack<Boolean>                fmxXml;
  
  // the with scenario requires to restore the previous model when done, so we need to remember the
  // temporarily used variable
  Stack<WithRecord>             withRecords;
  
  // an xml element with fmx attributes can generate multiple statements which need to be closed in the end,
  // so we remember here which statements had been generated
  Stack<Boolean>                fmxXmlOnElement;
  Stack<String>                 indentions;

  // this stack is used to remember the begin of the inner xml tree, so if the wrapping-condition is false
  // we're rendering the inner content into the else part of the if-then-else ftl statement
  Stack<Integer>                innerWraps;
  
  // remembering newlines in case an fmx:depends is located on a single line, so we're dropping line feeds
  // in order to prevent ugly "disruption" of the FTL text (for instance: <i fmx:depends="x">text<i> will no
  // longer be rendered as [#if x]\n<i>text</i>\n[/#if]). we're storing pairs of [linenumber, newline-index]
  Stack<Integer[]>              dependsNL;
  
  // remember the location after the last opening xml element. if the closing xml element follows immediately
  // thereafter we can generate a directly close xml element
  int                           lastOpen;
  
  public TranslationContext( @Nonnull String fmxPrefix, Function<String, String> directives ) {
    lastOpen          = -1;
    directiveProvider = directives;
    isCtxAttribute    = $_ -> CTX_NAMESPACE.equals( $_.getNsUri() );
    isFmxRelevant     = ($1, $2) -> FMX_NAMESPACE.equals( $1 ) || (($2 != null) && $2.startsWith( fmxPrefix ) );
    hasFmxAttribute   = $ -> $.parallelStream()
      .map( $_ -> isFmxRelevant.test( $_.getNsUri(), $_.getQName() ) )
      .reduce( false, ($1, $2) -> $1 || $2 );
  }
  
  @Override
  public void setDocumentLocator( Locator loc ) {
    locator = loc;
    super.setDocumentLocator( loc );
  }
  
  @Override
  public void startDocument() throws SAXException {
    builder           = STRINGFBUILDER.allocate();
    replacer          = STRINGFBUILDER.allocate();
    withRecords       = new Stack<>();
    fmxXml            = new Stack<>();
    fmxXmlOnElement   = new Stack<>();
    indentions        = new Stack<>();
    innerWraps        = new Stack<>();
    dependsNL         = new Stack<>();
    content           = "";
  }

  @Override
  public void endDocument() throws SAXException {
    content           = builder.toString();
    STRINGFBUILDER.free( builder  );
    STRINGFBUILDER.free( replacer );
    builder           = null;
    replacer          = null;
    withRecords       = null;
    fmxXml            = null;
    fmxXmlOnElement   = null;
    indentions        = null;
    innerWraps        = null;
  }

  private String newVar() {
    return String.format( "fmx_old%d", COUNTER.incrementAndGet() );
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
  public String toString() {
    return content;
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
    
    String indent = getCurrentIndent();
    if( indent.length() > 0 ) {
      // remove the current indention
      builder.setLength( builder.length() - indent.length() );
    }
    indentions.push( indent );
    
    openDepends( indent, dependsExpression );
    openWith( indent, withExpression, withName );
    openList( indent, listExpression, iteratorName );
    openWrap( indent, wrapExpression );
    
    // add the previously removed indention AFTER the ftl conditions have been rendered
    builder.append( indent );
    startXmlElement( uri, localName, qName, attrs );
    
    // remember the begin of the inner xml block
    innerWraps.push( builder.length() );
    
  }

  private void endFmxXmlElement( String uri, String localName, String qName ) {
    
    // capture the range of the inner statement
    int open  = innerWraps.pop();
    int close = builder.length();
    
    endXmlElement( uri, localName, qName );
    
    String indent = indentions.pop();
    closeWrap( indent, open, close );
    closeList( indent );
    closeWith( indent );
    closeDepends( indent );
    
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
      // get rid of '>' and replace it with a directly closing '/>'
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
    withRecords.push( new WithRecord( modelName, var ) );
  }

  private void emitWithClose( String uri, String localName, String qName ) {
    WithRecord record = withRecords.pop();
    builder.appendF( "[#assign %s=%s /]\n", record.getModelname(), record.getVarname() );
  }

  // directive
  
  private void emitDirectiveOpen( String uri, String localName, String qName, List<XmlAttr> attrs ) {
    String             name     = directiveProvider.apply( localName );
    builder.appendF( "[@%s", name );
    // write down all attributes that shall be kept
    attrs.stream().filter( isCtxAttribute.negate() ).forEach( this::fmAttribute );
    // generate a context map attribute if desired
    emitContextAttributes( attrs.stream().filter( isCtxAttribute ).collect( Collectors.toList() ) );
    builder.append( "]" );
  }
  
  private void emitContextAttributes( List<XmlAttr> ctxAttrs ) {
    if( ! ctxAttrs.isEmpty() ) {
      // write down the special attributes passed as a map
      builder.appendF( " %s={", getContextAttributeName( ctxAttrs ) );
      // if there's only one element is the attribute name so we don't need to go through the attributes
      if( ctxAttrs.size() > 1 ) {
        ctxAttrs.forEach( this::ctxAttribute );
        builder.setLength( builder.length() - 2 ); // get rid of the last ', ' sequence
      }
      builder.appendF( "}" );
    }
  }
  
  private String getContextAttributeName( List<XmlAttr> ctxAttrs ) {
    String            result    = null;
    Optional<XmlAttr> attrName  = ctxAttrs.stream().filter( $ -> CTX_ATTRIBUTE_NAME.equals( $.getLocalName() ) ).findAny();
    if( attrName.isPresent() ) {
      result = StringFunctions.cleanup( attrName.get().getAttrValue() );
    }
    return result != null ? result : CTX_DEFAULT_NAME;
  }

  private void emitDirectiveClose( String uri, String localName, String qName ) {
    String name = directiveProvider.apply( localName );
    builder.appendF( "[/@%s]", name );
  }

  private void ctxAttribute( XmlAttr attr ) {
    // ignore this attribute as it's only supposed to provide a name
    if( ! CTX_ATTRIBUTE_NAME.equals( attr.getLocalName() ) ) {
      builder.appendF( "'%s': %s, ", attr.getLocalName(), attr.getAttrValue() );
    }
  }
  
  private void fmAttribute( XmlAttr attr ) {
    if( ! FMX_NAMESPACE.equals( attr.getNsUri() ) ) {
      builder.appendF( " %s=\"%s\"", attr.getQName(), attr.getAttrValue() );
    }
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
  
  private void openWrap( String indent, String wrapExpression ) {
    if( wrapExpression != null ) {
      builder.appendF( "%s[#if %s]\n", indent, wrapExpression );
    }
    fmxXmlOnElement.push( wrapExpression != null );
  }
  
  private void closeWrap( String indent, int open, int close ) {
    boolean hasWrapExpression = fmxXmlOnElement.pop();
    if( hasWrapExpression ) {
      String innerContent = StringFunctions.trim( builder.substring( open, close ), " \t", false );
      builder.appendF( "\n%s[#else]", indent );
      builder.append( innerContent );
      builder.appendF( "%s[/#if]", indent );
    }
  }
  
  private String getCurrentIndent() {
    String result = "";
    int    i      = builder.length() - 1;
    while( i >= 0 ) {
      char ch = builder.charAt(i);
      if( ch == '\n' ) {
        result = builder.substring( i + 1, builder.length() );
        break;
      } else if( ! Character.isWhitespace( ch ) ) {
        // there is some non-whitespace text after the line break,
        // so it seems intentional which means we don't get rid of whitespace
        result = "";
        break;
      }
      i--;
    }
    return result;
  }

  private void openWith( String indent, String withExpression, String modelName ) {
    WithRecord result = null;
    if( withExpression != null ) {
      String varname = newVar();
      builder.appendF( "%s[#assign %s=%s! /]\n", indent, varname, modelName );
      builder.appendF( "%s[#assign %s=%s /]\n", indent, modelName, withExpression );
      result = new WithRecord( modelName, varname );
    }
    withRecords.push( result );
  }
  
  private void closeWith( String indent ) {
    WithRecord result = withRecords.pop();
    if( result != null ) { 
      builder.appendF( "\n%s[#assign %s=%s /]", indent, result.getModelname(), result.getVarname() );
    }
  }

  private void openList( String indent, String listExpression, String iteratorName ) {
    if( listExpression != null ) {
      builder.appendF( "%s[#list %s as %s]\n", indent, listExpression, iteratorName );
    }
    fmxXmlOnElement.push( listExpression != null );
  }
  
  private void closeList( String indent ) {
    boolean hasListExpression = fmxXmlOnElement.pop();
    if( hasListExpression ) { 
      builder.appendF( "\n%s[/#list]", indent );
    }
  }

  private void openDepends( String indent, String dependsExpression ) {
    if( dependsExpression != null ) {
      builder.appendF( "%s[#if %s]\n", indent, dependsExpression );
      dependsNL.push( new Integer[] { locator.getLineNumber(), builder.length() - 1 } );
    }
    fmxXmlOnElement.push( dependsExpression != null );
  }
  
  private void closeDepends( String indent ) {
    boolean hasDependsExpression = fmxXmlOnElement.pop();
    if( hasDependsExpression ) {
      Integer[] nl = dependsNL.pop();
      if( nl[0].intValue() == locator.getLineNumber() ) {
        // this fmx:depends element is located on a single line, so alter the output accordingly
        builder.deleteCharAt( nl[1].intValue() );
        builder.appendF( "%s[/#if]", indent );
      } else {
        builder.appendF( "\n%s[/#if]", indent );
      }
    }
  }

  @Override
  public void characters( char ch[], int start, int length ) throws SAXException {
    builder.append( ch, start, length );
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

  @AllArgsConstructor
  @Getter @Setter
  private static class WithRecord {
  
    String   modelname;
    String   varname;
    
  } /* ENDCLASS */
  
} /* ENDCLASS */
