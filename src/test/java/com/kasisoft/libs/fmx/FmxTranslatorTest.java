package com.kasisoft.libs.fmx;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import com.kasisoft.libs.common.io.*;

import org.testng.annotations.*;

import java.util.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public class FmxTranslatorTest {

  private static int TESTCASES = 11;
  
  private FmxTranslator translator = new FmxTranslator();
  
  @DataProvider(name = "convertData")
  public Object[][] convertData() {
    List<Object[]> list = new ArrayList<>( TESTCASES );
    ClassLoader    cl   = Thread.currentThread().getContextClassLoader();
    for( int i = 1; i <= TESTCASES; i++ ) {
      list.add( new Object[] {
        IoFunctions.readTextFully( cl.getResource( String.format( "example%02d.fmx", i ) ) ),
        IoFunctions.readTextFully( cl.getResource( String.format( "example%02d.ftl", i ) ) )
      } );
    }
    return list.toArray( new Object[ list.size() ][2] );
  }
  
  @Test(dataProvider = "convertData")
  public void convert( String fmxContent, String ftlContent ) {
    String converted  = translator.convert( fmxContent );
    assertThat( converted, is( ftlContent ) );
  }

} /* ENDCLASS */
