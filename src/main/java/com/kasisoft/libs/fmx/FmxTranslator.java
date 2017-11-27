package com.kasisoft.libs.fmx;

import static com.kasisoft.libs.fmx.FmxConstants.*;

import com.kasisoft.libs.common.config.*;
import com.kasisoft.libs.common.xml.adapters.*;
import com.kasisoft.libs.fmx.internal.*;

import org.xml.sax.*;

import javax.annotation.*;
import javax.xml.parsers.*;

import java.util.function.*;

import java.io.*;

import lombok.extern.slf4j.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class FmxTranslator {
  
  // a root element so it's made sure that multiple xml elements are embedded in a root element
  private static final String WRAPPER = "<%s:root xmlns:%s=\"%s\" xmlns:%s=\"%s\" xmlns:%s=\"%s\">%%s</%s:root>";
  
  private static final SimpleProperty<Boolean> DEBUG = new SimpleProperty<>( "kasisoft.fmx.translator.debug", new BooleanAdapter() )
    .withDefault( false );
  
  String                      nsPrefix;
  String                      wrapper;
  Function<String, String>    directiveProvider;
  SAXParserFactory            saxParserFactory;
  BiConsumer<String, String>  debug;
  
  public FmxTranslator() {
    this( null, null, null, null );
  }
  
  public FmxTranslator( Function<String, String> directives ) {
    this( null, null, null, directives );
  }
  
  public FmxTranslator( String prefix, String nsTPrefix, String ctxPrefix, Function<String, String> directives ) {
    nsPrefix          = prefix    != null ? prefix    : FMX_PREFIX;
    nsTPrefix         = nsTPrefix != null ? nsTPrefix : FMT_PREFIX;
    ctxPrefix         = ctxPrefix != null ? ctxPrefix : CTX_PREFIX;
    debug             = this::debugOff;
    wrapper           = String.format( WRAPPER, nsPrefix, nsPrefix, FMX_NAMESPACE, nsTPrefix, FMT_NAMESPACE, ctxPrefix, CTX_NAMESPACE, nsPrefix );
    directiveProvider = directives != null ? directives : $ -> $;
    saxParserFactory  = SAXParserFactory.newInstance();
    saxParserFactory.setNamespaceAware( true );
    setDebug( DEBUG.getValue() );
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
      TranslationContext ctx = new TranslationContext( nsPrefix, directiveProvider );
      convert( xmlInput, ctx );
      String result = ctx.toString();
      debug.accept( xmlInput, result );
      return result;
    } catch( Exception ex ) {
      throw FmxException.wrap( ex );
    }
  }

  private void convert( String xmlInput, TranslationContext ctx ) throws Exception {
    String wrapped = String.format( wrapper, xmlInput );
    try( Reader reader = new StringReader( wrapped ) ) {
      SAXParser saxParser = saxParserFactory.newSAXParser();
      saxParser.parse( new InputSource( reader ), ctx );
    }
  }
  
  public void setDebug( boolean enable ) {
    debug = enable ? this::debugOn : this::debugOff;
  }

  private void debugOn( String xml, String ftl ) {
    String message = String.format( "{xml:\n%s\n} -=>\n{ftl:\n%s\n}", xml, ftl );
    log.debug( message );
  }
  
  private void debugOff( String xml, String ftl ) {
  }
  
} /* ENDCLASS */
