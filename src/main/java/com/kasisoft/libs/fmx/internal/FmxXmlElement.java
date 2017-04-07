package com.kasisoft.libs.fmx.internal;

import com.kasisoft.libs.common.text.*;
import com.kasisoft.libs.fmx.*;

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
@ToString(callSuper = true)
public class FmxXmlElement extends NodeWrapper<Element> {

  List<Attr>    attributes;
  String        tag;
  
  public FmxXmlElement( @Nonnull Element xmlElement, @Nonnull List<Attr> xmlAttributes  ) {
    super( xmlElement );
    attributes = xmlAttributes;
    if( xmlElement.getPrefix() != null ) {
      tag = String.format( "%s:%s", xmlElement.getPrefix(), xmlElement.getLocalName() );
    } else {
      tag = xmlElement.getLocalName();
    }
  }

  @Override
  public void emit( TranslationContext ctx ) {
    String dependsExpression  = getDependsExpression();
    String listExpression     = getListExpression();
    String iteratorName       = getIteratorName();
    String withExpression     = getWithExpression();
    String withName           = getName();
    String wrapExpression     = getWrapExpression();
    openDepends( ctx, dependsExpression );
    String varname = openWith( ctx, withExpression, withName );
    openList( ctx, listExpression, iteratorName );
    openWrap( ctx, wrapExpression );
    if( getChildren().isEmpty() ) {
      ctx.writeXmlTag( tag, attributes );
    } else {
      ctx.openXmlTag( tag, attributes );
      emitChildren( ctx );
      ctx.closeXmlTag( tag );
    }
    closeWrap( ctx, wrapExpression );
    closeList( ctx, listExpression );
    closeWith( ctx, withExpression, withName, varname );
    closeDepends( ctx, dependsExpression );
  }
  
  private void openWrap( TranslationContext ctx, String wrapExpression ) {
    if( wrapExpression != null ) {
      ctx.appendF( "[#if %s]\n", wrapExpression );
    }
  }
  
  private void closeWrap( TranslationContext ctx, String wrapExpression ) {
    if( wrapExpression != null ) {
      ctx.append( "[#else]\n" );
      emitChildren( ctx );
      ctx.append( "[/#if]\n" );
    }
  }

  private String openWith( TranslationContext ctx, String withExpression, String modelName ) {
    String result = null;
    if( withExpression != null ) {
      result = ctx.newVar();
      ctx.appendF( "[#assign %s=%s! /]\n", result, modelName );
      ctx.appendF( "[#assign %s=%s /]\n", modelName, withExpression );
    }
    return result;
  }
  
  private void closeWith( TranslationContext ctx, String withExpression, String modelName, String varname ) {
    if( withExpression != null ) { 
      ctx.appendF( "[#assign %s=%s /]\n", modelName, varname );
    }
  }

  private void openList( TranslationContext ctx, String listExpression, String iteratorName ) {
    if( listExpression != null ) {
      ctx.appendF( "[#list %s as %s]\n", listExpression, iteratorName );
    }
  }
  
  private void closeList( TranslationContext ctx, String listExpression ) {
    if( listExpression != null ) { 
      ctx.append( "[/#list]\n" );
    }
  }
  
  private void openDepends( TranslationContext ctx, String dependsExpression ) {
    if( dependsExpression != null ) {
      ctx.appendF( "[#if %s]\n", dependsExpression );
    }
  }
  
  private void closeDepends( TranslationContext ctx, String dependsExpression ) {
    if( dependsExpression != null ) {
      ctx.append( "[/#if]\n" );
    }
  }
  
  private String getName() {
    String result   = null;
    Attr   nameNode = getNode().getAttributeNodeNS( FmxTranslator2.FMX_NAMESPACE, FmxAttr.name.name() );
    if( nameNode != null ) {
      result = StringFunctions.cleanup( nameNode.getNodeValue() );
    }
    if( result == null ) {
      result = "model";
    }
    return result;
  }
  
  private String getWithExpression() {
    String result   = null;
    Attr   withNode = getNode().getAttributeNodeNS( FmxTranslator2.FMX_NAMESPACE, FmxAttr.with.name() );
    if( withNode != null ) {
      result = StringFunctions.cleanup( withNode.getNodeValue() );
    }
    return result;
  }
  
  private String getListExpression() {
    String result   = null;
    Attr   listNode = getNode().getAttributeNodeNS( FmxTranslator2.FMX_NAMESPACE, FmxAttr.list.name() );
    if( listNode != null ) {
      result = StringFunctions.cleanup( listNode.getNodeValue() );
    }
    return result;
  }

  private String getWrapExpression() {
    String result      = null;
    Attr   dependsNode = getNode().getAttributeNodeNS( FmxTranslator2.FMX_NAMESPACE, FmxAttr.wrap.name() );
    if( dependsNode != null ) {
      result = StringFunctions.cleanup( dependsNode.getNodeValue() );
    }
    return result;
  }

  private String getDependsExpression() {
    String result      = null;
    Attr   dependsNode = getNode().getAttributeNodeNS( FmxTranslator2.FMX_NAMESPACE, FmxAttr.depends.name() );
    if( dependsNode != null ) {
      result = StringFunctions.cleanup( dependsNode.getNodeValue() );
    }
    return result;
  }

  private String getIteratorName() {
    String result = "it";
    Attr   itNode = getNode().getAttributeNodeNS( FmxTranslator2.FMX_NAMESPACE, FmxAttr.it.name() );
    if( itNode != null ) {
      String val = StringFunctions.cleanup( itNode.getNodeValue() );
      if( val != null ) {
        result = val;
      }
    }
    return result;
  }

} /* ENDCLASS */
