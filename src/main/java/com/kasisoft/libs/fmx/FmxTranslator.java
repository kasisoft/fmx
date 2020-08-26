package com.kasisoft.libs.fmx;

import static com.kasisoft.libs.fmx.FmxConstants.CTX_NAMESPACE;
import static com.kasisoft.libs.fmx.FmxConstants.CTX_PREFIX;
import static com.kasisoft.libs.fmx.FmxConstants.FMT_NAMESPACE;
import static com.kasisoft.libs.fmx.FmxConstants.FMT_PREFIX;
import static com.kasisoft.libs.fmx.FmxConstants.FMX_NAMESPACE;
import static com.kasisoft.libs.fmx.FmxConstants.FMX_PREFIX;

import com.kasisoft.libs.common.functional.SimpleFunction;
import com.kasisoft.libs.fmx.internal.TranslationContext;

import org.xml.sax.InputSource;

import javax.validation.constraints.NotNull;
import javax.xml.parsers.SAXParserFactory;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import java.util.Collections;
import java.util.Map;

import java.io.StringReader;

import lombok.extern.log4j.Log4j2;

import lombok.experimental.FieldDefaults;

import lombok.AccessLevel;

/**
 * @author daniel.kasmeroglu@kasisoft.com
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Log4j2
public class FmxTranslator {
  
  // a root element so it's made sure that multiple xml elements are embedded in a root element
  private static final String WRAPPER = "<%s:root xmlns:%s=\"%s\" xmlns:%s=\"%s\" xmlns:%s=\"%s\">%%s</%s:root>";
  
  String                                            nsPrefix;
  String                                            wrapper;
  SimpleFunction<String>                            directiveProvider;
  Map<String, BiFunction<String, String, String>>   attributeMappers;
  SAXParserFactory                                  saxParserFactory;
  BiConsumer<String, String>                        debug;
  
  public FmxTranslator() {
    this(null, null, null, null, null);
  }

  public FmxTranslator(SimpleFunction<String> directives) {
    this(null, null, null, directives, null);
  }

  public FmxTranslator(SimpleFunction<String> directives, Map<String, BiFunction<String, String, String>> mappers) {
    this(null, null, null, directives, mappers);
  }
  
  public FmxTranslator(String prefix, String nsTPrefix, String ctxPrefix, SimpleFunction<String> directives, Map<String, BiFunction<String, String, String>> mappers) {
    nsPrefix          = prefix    != null ? prefix    : FMX_PREFIX;
    nsTPrefix         = nsTPrefix != null ? nsTPrefix : FMT_PREFIX;
    ctxPrefix         = ctxPrefix != null ? ctxPrefix : CTX_PREFIX;
    debug             = this::debugOff;
    wrapper           = String.format(WRAPPER, nsPrefix, nsPrefix, FMX_NAMESPACE, nsTPrefix, FMT_NAMESPACE, ctxPrefix, CTX_NAMESPACE, nsPrefix);
    directiveProvider = directives != null ? directives : $ -> $;
    attributeMappers  = mappers != null ? mappers : Collections.emptyMap();  
    saxParserFactory  = SAXParserFactory.newInstance();
    saxParserFactory.setNamespaceAware( true );
    setDebug(false);
  }
  
  /**
   * This main function converts the supplied xml based input into the corresponding fmx ftl code.
   * 
   * @param xmlInput   The xml based ftl input.
   * 
   * @return   The fmx ftl code.
   * 
   * @throws   FmxException   In case of an error.
   */
   public String convert(@NotNull String xmlInput) {
    try {
      var ctx = new TranslationContext(nsPrefix, directiveProvider, attributeMappers);
      convert(xmlInput, ctx);
      String result = ctx.toString();
      debug.accept(xmlInput, result);
      return result;
    } catch (Exception ex) {
      throw FmxException.wrap( ex );
    }
  }

  private void convert(String xmlInput, TranslationContext ctx) throws Exception {
    var wrapped = String.format(wrapper, xmlInput);
    try (var reader = new StringReader(wrapped)) {
      var saxParser = saxParserFactory.newSAXParser();
      saxParser.parse(new InputSource(reader), ctx);
    }
  }
  
  public void setDebug(boolean enable) {
    debug = enable ? this::debugOn : this::debugOff;
  }

  private void debugOn(String xml, String ftl) {
    var message = String.format("{xml:\n%s\n} -=>\n{ftl:\n%s\n}", xml, ftl);
    log.debug( message );
  }
  
  private void debugOff(String xml, String ftl) {
  }
  
} /* ENDCLASS */
