package com.kasisoft.libs.fmx;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

import com.kasisoft.libs.common.io.*;

import org.testng.annotations.*;

import java.io.*;

import lombok.experimental.*;

import lombok.*;

import freemarker.cache.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FmxTemplateLoaderTest {
  
  ClassTemplateLoader   classTemplateLoader;
  FmxTemplateLoader     fmxTemplateLoader;
  
  @BeforeTest
  public void setup() {
    classTemplateLoader = new ClassTemplateLoader( FmxTemplateLoaderTest.class, "/" );
    fmxTemplateLoader   = new FmxTemplateLoader( classTemplateLoader );
  }
  
  @Test
  public void findTemplateSource() throws Exception {
    
    Object template1 = fmxTemplateLoader.findTemplateSource( "basic/bibo.fmx" );
    assertNull( template1 );

    Object template2 = fmxTemplateLoader.findTemplateSource( "basic/01_unchanged.ftl" );
    assertNull( template2 );

    Object template3 = fmxTemplateLoader.findTemplateSource( "basic/01_unchanged.fmx" );
    assertNotNull( template3 );

  }

  @Test
  public void getLastModified() throws Exception {

    Object template1 = fmxTemplateLoader.findTemplateSource( "basic/01_unchanged.fmx" );
    assertNotNull( template1 );

    long lm1 = fmxTemplateLoader.getLastModified( template1 );
    assertTrue( lm1 > 0L );

  }

  @Test
  public void getReader() throws Exception {

    Object template1 = fmxTemplateLoader.findTemplateSource( "basic/22_import.fmx" );
    assertNotNull( template1 );

    String text = null;
    try( Reader reader = fmxTemplateLoader.getReader( template1, "UTF-8" ) ) {
      text = IoFunctions.readTextFully( reader );
    }
    assertThat( text, is( "[#import '/bibo/sample.ftl' as dodo /]\n" ) );

  }

} /* ENDCLASS */
