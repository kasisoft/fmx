package com.kasisoft.libs.fmx.internal;

import org.w3c.dom.*;

import javax.annotation.*;

import java.util.*;

import lombok.experimental.*;

import lombok.*;

/**
 * This wrapper allows to process elements where some of the features are applied as attributes.
 * 
 * @author daniel.kasmeroglu@kasisoft.net
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@ToString(callSuper = true)
public class FmxXmlElement extends XmlElement {

  public FmxXmlElement( @Nonnull Element xmlElement, @Nonnull List<Attr> xmlAttributes  ) {
    super( xmlElement, xmlAttributes );
  }

  @Override
  public void emit( TranslationContext ctx ) {
    
    String dependsExpression  = FmxAttr . depends . getValue( getNode() );
    String listExpression     = FmxAttr . list    . getValue( getNode() );
    String iteratorName       = FmxAttr . it      . getValue( getNode(), "it" );
    String withExpression     = FmxAttr . with    . getValue( getNode() );
    String withName           = FmxAttr . name    . getValue( getNode(), "model" );
    String wrapExpression     = FmxAttr . wrap    . getValue( getNode() );
    
    openDepends( ctx, dependsExpression );
    String varname = openWith( ctx, withExpression, withName );
    openList( ctx, listExpression, iteratorName );
    openWrap( ctx, wrapExpression );
    
    super.emit( ctx );
    
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

} /* ENDCLASS */
