package com.kasisoft.libs.fmx;

import org.w3c.dom.*;

import java.util.function.*;

import java.util.*;

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

  Predicate<Node>       IS_FMX_RELEVANT   = $ -> FMX_NAMESPACE.equals( $.getNamespaceURI() ) || FMX_PREFIX.equals( $.getPrefix() ); 

  Predicate<List<Attr>> HAS_FMX_ATTRIBUTE = $ -> $.parallelStream()
      .map( $1 -> IS_FMX_RELEVANT.test($1) )
      .reduce( false, ($1, $2) -> $1 || $2 );

} /* ENDINTERFACE */
