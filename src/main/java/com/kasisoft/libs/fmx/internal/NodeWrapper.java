package com.kasisoft.libs.fmx.internal;

import org.w3c.dom.*;

import javax.annotation.*;

import java.util.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@ToString
public abstract class NodeWrapper<T extends Node> {

  T                   node;
  List<NodeWrapper>   children = new ArrayList<>();

  public NodeWrapper( @Nonnull T xmlNode ) {
    node = xmlNode;
  }
  
  public abstract void emit( @Nonnull TranslationContext ctx );
  
  public void emitChildren( @Nonnull TranslationContext ctx ) {
    getChildren().forEach( $ -> $.emit( ctx ) );
  }
  
} /* ENDCLASS */
