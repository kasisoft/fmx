package com.kasisoft.libs.fmx;

import static com.kasisoft.libs.fmx.FmxConstants.*;

import com.kasisoft.libs.fmx.internal.*;

import org.xml.sax.*;

import javax.validation.constraints.*;
import javax.xml.parsers.*;

import java.util.function.*;

import java.util.*;

import java.io.*;

import lombok.extern.log4j.*;

import lombok.experimental.*;

import lombok.*;

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
  boolean                                           squareBrackets;
  
  public FmxTranslator() {
    this(null, null, null, null, null, true);
  }
  
  public FmxTranslator(boolean square) {
    this(null, null, null, null, null, square);
  }

  public FmxTranslator(SimpleFunction<String> directives) {
    this(null, null, null, directives, null, true);
  }
  
  public FmxTranslator(SimpleFunction<String> directives, boolean square) {
    this(null, null, null, directives, null, square);
  }

  public FmxTranslator(SimpleFunction<String> directives, Map<String, BiFunction<String, String, String>> mappers) {
    this(null, null, null, directives, mappers, true);
  }
  
  public FmxTranslator(SimpleFunction<String> directives, Map<String, BiFunction<String, String, String>> mappers, boolean square) {
    this(null, null, null, directives, mappers, square);
  }

  public FmxTranslator(String prefix, String nsTPrefix, String ctxPrefix, SimpleFunction<String> directives, Map<String, BiFunction<String, String, String>> mappers) {
    this(prefix, nsTPrefix, ctxPrefix, directives, mappers, true);
  }
  
  public FmxTranslator(String prefix, String nsTPrefix, String ctxPrefix, SimpleFunction<String> directives, Map<String, BiFunction<String, String, String>> mappers, boolean square) {
    nsPrefix          = prefix    != null ? prefix    : FMX_PREFIX;
    nsTPrefix         = nsTPrefix != null ? nsTPrefix : FMT_PREFIX;
    ctxPrefix         = ctxPrefix != null ? ctxPrefix : CTX_PREFIX;
    debug             = this::debugOff;
    squareBrackets    = square;
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
      TranslationContext ctx = new TranslationContext(nsPrefix, directiveProvider, attributeMappers, squareBrackets);
      convert(xmlInput, ctx);
      String result = ctx.toString();
      debug.accept(xmlInput, result);
      return result;
    } catch (Exception ex) {
      throw FmxException.wrap( ex );
    }
  }

  private void convert(String xmlInput, TranslationContext ctx) throws Exception {
    String wrapped = String.format(wrapper, xmlInput);
    try (StringReader reader = new StringReader(wrapped)) {
      SAXParser saxParser = saxParserFactory.newSAXParser();
      saxParser.parse(new InputSource(reader), ctx);
    }
  }
  
  public void setDebug(boolean enable) {
    debug = enable ? this::debugOn : this::debugOff;
  }

  private void debugOn(String xml, String ftl) {
    String message = String.format("{xml:\n%s\n} -=>\n{ftl:\n%s\n}", xml, ftl);
    log.debug(message);
  }
  
  private void debugOff(String xml, String ftl) {
  }
  
} /* ENDCLASS */
