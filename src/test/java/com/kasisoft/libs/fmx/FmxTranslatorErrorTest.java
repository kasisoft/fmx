package com.kasisoft.libs.fmx;

import static org.testng.Assert.fail;

import com.kasisoft.libs.fmx.internal.*;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.stream.Collectors;

import java.util.Arrays;
import java.util.List;

import lombok.experimental.FieldDefaults;

import lombok.AccessLevel;

/**
 * @author daniel.kasmeroglu@kasisoft.com
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FmxTranslatorErrorTest {

  private static List<String> TESTCASES = Arrays.asList(new String[] {
      
    "01_invalid-xml",
    "02_incomplete-depends",
    "03_import-without-name",
    "04_import-without-path",
    "05_include-without-path",
    "06_list-without-value",
    "07_with-without-value",
      
  });
  
  FmxTranslator    translator;
  
  @BeforeClass
  public void setup() {
    translator = new FmxTranslator(null, null, null, $ -> "axolotl." + $, null);
  }
  
  @DataProvider(name = "convertData")
  public Object[][] convertData() {
    ClassLoader  cl   = Thread.currentThread().getContextClassLoader();
    List<Object> list = TESTCASES.stream().map($ -> createRecord(cl, $)).collect(Collectors.toList());
    return list.toArray(new Object[list.size()][2]);
  }
  
  private Object[] createRecord(ClassLoader cl, String testcase) {
    String fmx = loadText(cl, String.format("invalid/%s.fmx", testcase));
    return new Object[] {fmx};
  }
  
  private String loadText(ClassLoader cl, String resource) {
    return FmxUtils.readText(cl.getResource( resource) );
  }

  @Test(dataProvider = "convertData", expectedExceptions = FmxException.class)
  public void convert(String fmxContent) {
    translator.convert(fmxContent);
    fail(fmxContent);
  }

} /* ENDCLASS */
