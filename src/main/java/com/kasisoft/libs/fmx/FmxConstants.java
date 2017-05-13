package com.kasisoft.libs.fmx;

import java.util.function.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
public interface FmxConstants {

  String                FMX_NAMESPACE     = "https://kasisoft.com/namespaces/fmx/0.1";
  String                FMT_NAMESPACE     = "https://kasisoft.com/namespaces/fmt/0.1";
  
  String                FMT_PREFIX        = "fmt";
  String                FMX_PREFIX        = "fmx";
  
  String                FMX_SUFFIX        = ".fmx";

  Predicate<String>     IS_FMX            = $ -> $.endsWith( FMX_SUFFIX ); 

} /* ENDINTERFACE */
