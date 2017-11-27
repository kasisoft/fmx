package com.kasisoft.libs.fmx;

import static com.kasisoft.libs.fmx.FmxConstants.*;

import com.kasisoft.libs.common.io.*;

import javax.annotation.*;

import java.util.function.*;

import java.util.*;

import java.io.*;

import lombok.extern.slf4j.*;

import lombok.experimental.*;

import lombok.*;

import freemarker.cache.*;

/**
 * This loader uses another {@link TemplateLoader} instance to load <code>.fmx</code> templates. It  
 * <b>WON'T</b> load normal <code>.ftl</code> templates.  
 * The translation of fmx to ftl syntax can be followed if logging is set to trace level.
 * 
 * @author daniel.kasmeroglu@kasisoft.net
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class FmxTemplateLoader implements TemplateLoader {

  TemplateLoader      delegate;
  FmxTranslator       translator;
  Predicate<String>   isFmx;
  
  public FmxTemplateLoader( @Nonnull TemplateLoader loader ) {
    this( loader, null );
  }

  public FmxTemplateLoader( @Nonnull TemplateLoader loader, Predicate<String> test ) {
    this( loader, test, null, null );
  }
  
  public FmxTemplateLoader( @Nonnull TemplateLoader loader, Predicate<String> test, Function<String, String> directives, Map<String, BiFunction<String, String, String>> mappers ) {
    delegate    = loader;
    translator  = new FmxTranslator( null, null, null, directives, mappers );
    isFmx       = test != null ? test : IS_FMX;
  }
  
  public void setDebug( boolean debug ) {
    translator.setDebug( debug );
  }
  
  @Override
  public Object findTemplateSource( @Nonnull String name ) throws IOException {
    FmxRecord result = null;
    if( isFmx.test( name ) ) {
      Object resource = delegate.findTemplateSource( name );
      if( resource != null ) {
        result = new FmxRecord( resource, null );
      }
    }
    return result;
  }

  @Override
  public long getLastModified( Object templateSource ) {
    long result = 0L;
    if( templateSource instanceof FmxRecord ) {
      result = delegate.getLastModified( ((FmxRecord) templateSource).resource );
    }
    return result;
  }

  @Override
  public Reader getReader( Object templateSource, String encoding ) throws IOException {
    Reader result = null;
    if( templateSource instanceof FmxRecord ) {
      FmxRecord fmxRecord = (FmxRecord) templateSource;
      if( fmxRecord.getTranslation() == null ) {
        // first call: translate fmx to ftl
        fmxRecord.setTranslation( loadTranslation( fmxRecord.getResource(), encoding ) );
      }
      result = new StringReader( fmxRecord.translation );
    }
    return result;
  }

  @Override
  public void closeTemplateSource( Object templateSource ) throws IOException {
    if( templateSource instanceof FmxRecord ) {
      FmxRecord fmxRecord = (FmxRecord) templateSource;
      delegate.closeTemplateSource( fmxRecord.resource );
      fmxRecord.setTranslation( null );
    }
  }
  
  private String loadTranslation( Object resource, String encoding ) throws IOException {
    try( Reader reader = delegate.getReader( resource, encoding ) ) {
      return loadTranslation( reader );
    }
  }

  private String loadTranslation( Reader reader ) throws IOException {
    try {
      String fullInput = IoFunctions.readTextFully( reader );
      String result    = translator.convert( fullInput );
      if( log.isTraceEnabled() ) {
        log.trace( "<<---- before ---->>" );
        log.trace( fullInput );
        log.trace( "<<---- after ----->>" );
        log.trace( result );
        log.trace( "<<---- done ------>>" );
      }
      return result;
    } catch( RuntimeException ex ) {
      throw new IOException( ex );
    }
  }

  @AllArgsConstructor
  @Data
  private static final class FmxRecord {
    
    Object  resource;
    String  translation;
    
  } /* ENDCLASS */
  
} /* ENDCLASS */
