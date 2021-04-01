package com.kasisoft.libs.fmx;

import static com.kasisoft.libs.fmx.FmxConstants.IS_FMX;

import com.kasisoft.libs.fmx.internal.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import java.util.Map;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import lombok.extern.log4j.Log4j2;

import lombok.experimental.FieldDefaults;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import freemarker.cache.TemplateLoader;

/**
 * This loader uses another {@link TemplateLoader} instance to load <code>.fmx</code> templates. It  
 * <b>WON'T</b> load normal <code>.ftl</code> templates.  
 * The translation of fmx to ftl syntax can be followed if logging is set to trace level.
 * 
 * @author daniel.kasmeroglu@kasisoft.com
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Log4j2
public class FmxTemplateLoader implements TemplateLoader {

  TemplateLoader      delegate;
  FmxTranslator       translator;
  Predicate<String>   isFmx;
  
  public FmxTemplateLoader(@NotNull TemplateLoader loader) {
    this(loader, null, null, null, true);
  }

  public FmxTemplateLoader(@NotNull TemplateLoader loader, boolean square) {
    this(loader, null, null, null, false);
  }

  public FmxTemplateLoader(@NotNull TemplateLoader loader, Predicate<String> test) {
    this(loader, test, null, null, true);
  }

  public FmxTemplateLoader(@NotNull TemplateLoader loader, Predicate<String> test, boolean square) {
    this(loader, test, null, null, square);
  }

  public FmxTemplateLoader(@NotNull TemplateLoader loader, Predicate<String> test, SimpleFunction<String> directives, Map<String, BiFunction<String, String, String>> mappers, boolean square) {
    delegate    = loader;
    translator  = new FmxTranslator(null, null, null, directives, mappers, square);
    isFmx       = test != null ? test : IS_FMX;
  }
  
  public void setDebug(boolean debug) {
    translator.setDebug(debug);
  }
  
  @Override
  public Object findTemplateSource(@NotBlank String name) throws IOException {
    FmxRecord result = null;
    if (isFmx.test(name)) {
      Object resource = delegate.findTemplateSource(name);
      if (resource != null) {
        result = new FmxRecord(resource, null);
      }
    }
    return result;
  }

  @Override
  public long getLastModified(Object templateSource) {
    long result = 0L;
    if (templateSource instanceof FmxRecord) {
      result = delegate.getLastModified(((FmxRecord) templateSource).resource);
    }
    return result;
  }

  @Override
  public Reader getReader(Object templateSource, String encoding) throws IOException {
    Reader result = null;
    if (templateSource instanceof FmxRecord) {
      FmxRecord fmxRecord = (FmxRecord) templateSource;
      if (fmxRecord.getTranslation() == null) {
        // first call: translate fmx to ftl
        fmxRecord.setTranslation(loadTranslation(fmxRecord.getResource(), encoding));
      }
      result = new StringReader(fmxRecord.translation);
    }
    return result;
  }

  @Override
  public void closeTemplateSource(Object templateSource) throws IOException {
    if (templateSource instanceof FmxRecord) {
      FmxRecord fmxRecord = (FmxRecord) templateSource;
      delegate.closeTemplateSource(fmxRecord.resource);
      fmxRecord.setTranslation(null);
    }
  }
  
  private String loadTranslation(Object resource, String encoding) throws IOException {
    try (Reader reader = delegate.getReader(resource, encoding)) {
      return loadTranslation(reader);
    }
  }

  private String loadTranslation(Reader reader) throws IOException {
    try {
      String fullInput = FmxUtils.readText(reader);
      String result    = translator.convert(fullInput);
      if (log.isTraceEnabled()) {
        log.trace("<<---- before ---->>");
        log.trace(fullInput);
        log.trace("<<---- after ----->>");
        log.trace(result);
        log.trace("<<---- done ------>>");
      }
      return result;
    } catch (Exception ex) {
      throw new IOException(ex);
    }
  }

  @AllArgsConstructor
  @Data
  private static final class FmxRecord {
    
    Object  resource;
    String  translation;
    
  } /* ENDCLASS */
  
} /* ENDCLASS */
