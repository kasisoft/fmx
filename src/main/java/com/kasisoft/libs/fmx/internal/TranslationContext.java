package com.kasisoft.libs.fmx.internal;

import static com.kasisoft.libs.fmx.FmxConstants.*;

import com.kasisoft.libs.common.text.*;
import com.kasisoft.libs.common.util.*;

import org.w3c.dom.*;

import javax.annotation.*;

import java.util.function.*;

import java.util.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data @AllArgsConstructor
public final class TranslationContext implements AutoCloseable {

  private static final Bucket<StringFBuilder> STRINGFBUILDER = BucketFactories.newStringFBuilderBucket();
  
  private static final String INDENTION = "  ";
  
  StringFBuilder                            builder;
  Function<String, String>                  directiveProvider;
  StringFBuilder                            indent;
  StringFBuilder                            replacer;
  long                                      counter;
  
  public TranslationContext( Function<String, String> directives ) {
    builder           = STRINGFBUILDER.allocate();
    indent            = STRINGFBUILDER.allocate();
    replacer          = STRINGFBUILDER.allocate();
    directiveProvider = directives;
    counter           = 0;
  }
  
  public String newVar() {
    return String.format( "fmx_old%d", counter++ );
  }
  
  public TranslationContext append( @Nonnull String str ) {
    builder.append( str );
    return this;
  }

  public TranslationContext appendF( @Nonnull String fmt, Object ... args ) {
    builder.appendF( fmt, args );
    return this;
  }
  
  public TranslationContext writeXmlTag( @Nonnull String tag, @Nonnull List<Attr> attributes ) {
    builder.appendF( "%s<%s", indent, tag );
    attributes.forEach( this::xmlAttribute );
    builder.append( "/>\n" );
    return this;
  }
  
  public TranslationContext openXmlTag( @Nonnull String tag, @Nonnull List<Attr> attributes ) {
    builder.appendF( "%s<%s", indent, tag );
    attributes.forEach( this::xmlAttribute );
    builder.append( ">\n" );
    indent.append( INDENTION );
    return this;
  }
  
  public TranslationContext closeXmlTag( @Nonnull String tag ) {
    indent.setLength( Math.max( 0, indent.length() - INDENTION.length() ) );
    builder.appendF( "%s</%s>\n", indent, tag );
    return this;
  }
  
  public void xmlAttribute( Attr attr ) {
    String nsUri = attr.getNamespaceURI();
    // only emit non-fmx attributes
    if( ! FMX_NAMESPACE.equals( nsUri ) ) {
      String nodeValue = escapeValue( attr );
      if( attr.getPrefix() != null ) {
        // generate a test for the attribute value indicated by the namespace
        if( FMT_NAMESPACE.equals( nsUri ) ) {
          appendF( "[#if (%s)?has_content] %s=\"${%s}\"[/#if]", nodeValue, attr.getLocalName(), nodeValue );
        } else {
          appendF( " %s:%s=\"%s\"", attr.getPrefix(), attr.getLocalName(), nodeValue );
        }
      } else {
        appendF( " %s=\"%s\"", attr.getLocalName(), nodeValue );
      }
    }
  }

  public void fmAttribute( Attr attr ) {
    if( ! FMX_NAMESPACE.equals( attr.getNamespaceURI() ) ) {
      String nodeValue = attr.getNodeValue();
      if( attr.getPrefix() != null ) {
        appendF( " %s:%s=\"%s\"", attr.getPrefix(), attr.getLocalName(), nodeValue );
      } else {
        appendF( " %s=\"%s\"", attr.getLocalName(), nodeValue );
      }
    }
  }

  private String escapeValue( Attr attr ) {
    replacer.setLength(0);
    replacer.append( attr.getNodeValue() );
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
      STRINGFBUILDER.free( indent   );
      STRINGFBUILDER.free( replacer );
      builder  = null;
      indent   = null;
      replacer = null;
    }
  }
  
  @Override
  public String toString() {
    return builder.toString();
  }

} /* ENDCLASS */
