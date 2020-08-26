package com.kasisoft.libs.fmx;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.kasisoft.libs.common.io.IoFunctions;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.function.BiFunction;

import java.util.stream.Collectors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.experimental.FieldDefaults;

import lombok.AccessLevel;

/**
 * @author daniel.kasmeroglu@kasisoft.com
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FmxTranslatorTest {

  private static List<String> TESTCASES = Arrays.asList(new String[] {
      
    // plain xml with no extras must be rendered as is
    "01_unchanged",
    
    // plain xml where one element contains another one
    "02_unchanged-with-children",

    // plain xml which contains an xml element with an unspecified xml namespace
    "03_unchanged-with-additional-ns",

    // plain xml which contains xml attributes with values that are supposed to be escaped
    "04_unchanged-with-attr-escaping",

    // generation of a doctype declaration (must be the first element to produce valid html output)
    "05_doctype",

    // generation of a doctype declaration with an empty value
    "06_doctype-with-empty-value",

    // generation of a doctype declaration with a value
    "07_doctype-with-value",

    // realize an ftl include
    "08_include",

    // a list element
    "09_list", 

    // a list element with a certain iterator
    "10_list-with-iterator", 

    // a list directly attached to an element
    "11_list-on-element", 

    // a list directly attached to an element
    "12_list-on-element-with-iterator", 

    // embed a block if an expression is met
    "13_depends", 

    // embed a block if an expression is met
    "14_depends-on-element", 

    // create a 'shortcut' for an inner block
    "15_with", 

    // create a 'shortcut' for an inner block with a dedicated name
    "16_with-with-name", 

    // create a 'shortcut' for an inner block on an element
    "17_with-on-element", 

    // create a 'shortcut' for an inner block on an element and a dedicated name
    "18_with-on-element-with-name", 

    // create a 'shortcut' for an inner block on an element and a dedicated name
    "19_directive", 

    // a wrapping element
    "20_wrap", 

    // a wrapping element
    "21_complex-example", 

    // a wrapping element
    "22_import", 

    // only emit attributes if there non-empty
    "23_empty-attributes", 

    // embedded ftl
    "24_embedded-ftl", 
    
    // escape block
    "25_escape",
    
    // compress block
    "26_compress",
    
    // cdata block
    "27_cdata",

    // wrap on an element with list
    "28_wrap-with-list",
    
    // a list which value is based on a null/missing parent object
    "29_list-without-parent-object",

    // scenario which allows to pass multiple attributes as a json map 
    "30_context-attributes",
    
    // complex list construction
    "31_complex-list",

    // simple switch statement without cases/defaults
    "32_switch-no-match",
    
    // switch statement with a single case
    "33_switch-one-case",
    
    // switch with multiple cases
    "34_switch-many-cases",

    // switch with a default scenario
    "35_switch-with-default",

    // directive example using an ftl expression
    "36_directive-with-expression",

    // directive example using an ftl expression
    "37_directive-with-empty-literal",

    // directive example using an ftl expression
    "38_directive-with-attribute-value-mapper",

    // example for a macro declaration
    "39_macro",

    // including a macro
    "40_include-ftl",
      
    // escaping of xml
    "41_xescape",

    // escaping of xml
    "42_xescape-by-attr",

  });
  
  FmxTranslator    translator;
  
  @BeforeClass
  public void setup() {
    Map<String, BiFunction<String, String, String>> mappers = new HashMap<>();
    mappers.put("axolotl.frogger", this::customMapper); 
    translator = new FmxTranslator(null, null, null, this::directiveMapper, mappers);
  }
  
  private String customMapper(String attrLocalName, String attributeValue) {
    return String.format("\"TOTO-%s-TOTO\"", attributeValue);
  }
  
  private String directiveMapper(String name) {
    if ("cms-component".equals(name)) {
      return name.replace('-', '.');
    } else {
      return "axolotl." + name;
    }
  }
  
  @DataProvider(name = "convertData")
  public Object[][] convertData() {
    ClassLoader    cl   = Thread.currentThread().getContextClassLoader();
    List<Object[]> list = TESTCASES.stream().map($ -> createRecord(cl, $)).collect(Collectors.toList());
    return list.toArray(new Object[list.size()][2]);
  }
  
  private Object[] createRecord(ClassLoader cl, String testcase) {
    String fmx = loadText(cl, String.format("basic/%s.fmx", testcase));
    String ftl = loadText(cl, String.format("basic/%s.ftl", testcase)); 
    return new Object[] {testcase, fmx, ftl};
  }
  
  private String loadText(ClassLoader cl, String resource) {
    return IoFunctions.readText(cl.getResource(resource));
  }

  @Test(dataProvider = "convertData")
  public void convert(String testcase, String fmxContent, String ftlContent) {
    String converted  = translator.convert(fmxContent);
    assertThat(converted, is(ftlContent));
  }

} /* ENDCLASS */
