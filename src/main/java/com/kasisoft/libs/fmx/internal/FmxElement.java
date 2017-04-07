package com.kasisoft.libs.fmx.internal;

import static com.kasisoft.libs.fmx.internal.Messages.*;

import org.w3c.dom.*;

import javax.annotation.*;

import java.util.*;

import lombok.experimental.*;

import lombok.*;

/**
 * This wrapper is used for dedicated generation elements. The <code>root</code> type is artificial whereas
 * all unsupported types will be mapped to corresponding directives.
 * 
 * @author daniel.kasmeroglu@kasisoft.net
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@ToString(callSuper = true)
public class FmxElement extends NodeWrapper<Element> {

  FmxElementType    type;
  List<Attr>        attributes;
  
  public FmxElement( @Nonnull Element xmlElement, @Nonnull FmxElementType fmxType, @Nonnull List<Attr> xmlAttributes ) {
    super( xmlElement );
    type       = fmxType;
    attributes = xmlAttributes;
  }

  @Override
  public void emit( TranslationContext ctx ) {
    switch( type ) {
    case directive  : emitDirective ( ctx ); break;
    case doctype    : emitDoctype   ( ctx ); break;
    case include    : emitInclude   ( ctx ); break;
    case list       : emitList      ( ctx ); break;
    case depends    : emitDepends   ( ctx ); break;
    case root       : emitRoot      ( ctx ); break;
    case with       : emitWith      ( ctx ); break;
    case importDecl : emitImport    ( ctx ); break;
    }
  }

  /**
   * The root element is artificial so it won't generate any outcome.
   * 
   * @param ctx   Receiver for the emitted code.
   */
  private void emitRoot( TranslationContext ctx ) {
    emitChildren( ctx );
  }
  
  /**
   * Causes the inner content to be rendered after a model attribute will be set to simplify the usage.
   *  
   * @param ctx   Receiver for the emitted code.
   */
  private void emitWith( TranslationContext ctx ) {
    String modelName  = FmxAttr.name.getValue( getNode(), "model" );
    String modelExpr  = FmxAttr.value.getRequiredValue( getNode(), error_with_values );
    // this variable name is used in case it already exists within the model, so we're backing it up before
    String var        = ctx.newVar();
    ctx.appendF( "[#assign %s=%s! /]\n", var, modelName );
    ctx.appendF( "[#assign %s=%s /]\n", modelName, modelExpr );
    emitChildren( ctx );
    ctx.appendF( "[#assign %s=%s /]\n", modelName, var );
  }

  /**
   * Only render the content if a certain condition is valid.
   *  
   * @param ctx   Receiver for the emitted code.
   */
  private void emitDepends( TranslationContext ctx ) {
    String dependsExpr = FmxAttr.value.getRequiredValue( getNode(), error_depends_values );
    ctx.appendF( "[#if %s]\n", dependsExpr );
    emitChildren( ctx );
    ctx.appendF( "[/#if]\n" );
  }
  
  /**
   * Iterates through a list.
   *  
   * @param ctx   Receiver for the emitted code.
   */
  private void emitList( TranslationContext ctx ) {
    String listExpression = FmxAttr.value.getRequiredValue( getNode(), error_list_values );
    String iteratorName   = FmxAttr.it.getValue( getNode(), "it" );
    ctx.appendF( "[#list %s as %s]\n", listExpression, iteratorName );
    emitChildren( ctx );
    ctx.appendF( "[/#list]\n" );
  }
  
  /**
   * Generates an include statement.
   *  
   * @param ctx   Receiver for the emitted code.
   */
  private void emitInclude( TranslationContext ctx ) {
    String path = FmxAttr.path.getRequiredValue( getNode(), error_include_values );
    ctx.appendF( "[#include '%s' /]\n", path );
  }

  /**
   * Generates an import statement.
   *  
   * @param ctx   Receiver for the emitted code.
   */
  private void emitImport( TranslationContext ctx ) {
    String path = FmxAttr.path.getRequiredValue( getNode(), error_import_values );
    String name = FmxAttr.name.getRequiredValue( getNode(), error_import_values );
    ctx.appendF( "[#import '%s' as %s /]\n", path, name );
  }

  /**
   * Generates a doctype declaration.
   *  
   * @param ctx   Receiver for the emitted code.
   */
  private void emitDoctype( TranslationContext ctx ) {
    ctx.appendF( "<!doctype %s>\n", FmxAttr.value.getValue( getNode(), "html" ) );
  }

  /**
   * Generates a directive call.
   *  
   * @param ctx   Receiver for the emitted code.
   */
  private void emitDirective( TranslationContext ctx ) {
    String name = ctx.getDirectiveProvider().apply( getNode().getLocalName() );
    ctx.appendF( "[@%s", name );
    getAttributes().forEach( ctx::fmAttribute );
    ctx.append( "]\n" );
    emitChildren( ctx );
    ctx.appendF( "[/@%s]\n", name );
  }
  
} /* ENDCLASS */
