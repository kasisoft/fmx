package com.kasisoft.libs.fmx;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.kasisoft.libs.fmx.internal.*;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

import java.io.Reader;

import lombok.experimental.FieldDefaults;

import lombok.AccessLevel;

import freemarker.cache.ClassTemplateLoader;

/**
 * @author daniel.kasmeroglu@kasisoft.com
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FmxTemplateLoaderTest {
  
  ClassTemplateLoader   classTemplateLoader;
  FmxTemplateLoader     fmxTemplateLoader;
  
  @BeforeTest
  public void setup() {
    classTemplateLoader = new ClassTemplateLoader(FmxTemplateLoaderTest.class.getClassLoader(), "");
    fmxTemplateLoader   = new FmxTemplateLoader(classTemplateLoader);
  }
  
  @Test
  public void findTemplateSource() throws Exception {
    
    Object template1 = fmxTemplateLoader.findTemplateSource("basic/bibo.fmx");
    assertNull(template1);

    Object template2 = fmxTemplateLoader.findTemplateSource("basic/01_unchanged.ftl");
    assertNull(template2);

    Object template3 = fmxTemplateLoader.findTemplateSource("basic/01_unchanged.fmx");
    assertNotNull(template3);

  }

  @Test
  public void getLastModified() throws Exception {

    Object template1 = fmxTemplateLoader.findTemplateSource("basic/01_unchanged.fmx");
    assertNotNull(template1);

    long lm1 = fmxTemplateLoader.getLastModified(template1);
    assertTrue(lm1 > 0L);

  }

  @Test
  public void getReader() throws Exception {

    Object template1 = fmxTemplateLoader.findTemplateSource("basic/22_import.fmx");
    assertNotNull( template1 );

    String text = null;
    try (Reader reader = fmxTemplateLoader.getReader(template1, StandardCharsets.UTF_8.name())) {
      text = FmxUtils.readText(reader);
    }
    assertThat(text, is("[#import '/bibo/sample.ftl' as dodo /]\n"));

  }

} /* ENDCLASS */
