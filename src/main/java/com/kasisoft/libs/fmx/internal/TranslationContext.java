package com.kasisoft.libs.fmx.internal;

import com.kasisoft.libs.common.text.*;
import com.kasisoft.libs.fmx.*;

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
public final class TranslationContext {

  private static final String INDENTION = "  ";
  
  StringFBuilder                            builder;
  Function<String, String>                  directiveProvider;
  StringBuilder                             indent;
  StringBuilder                             replacer;
  long                                      counter;
  
  public TranslationContext( StringFBuilder stringBuilder, Function<String, String> directives ) {
    builder           = stringBuilder;
    directiveProvider = directives;
    indent            = new StringBuilder();
    replacer          = new StringBuilder();
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
    if( ! FmxTranslator2.FMX_NAMESPACE.equals( nsUri ) ) {
      String nodeValue = escapeValue( attr );
      if( attr.getPrefix() != null ) {
        if( FmxTranslator2.FMT_NAMESPACE.equals( nsUri ) ) {
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
    if( ! FmxTranslator2.FMX_NAMESPACE.equals( attr.getNamespaceURI() ) ) {
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

} /* ENDCLASS */
