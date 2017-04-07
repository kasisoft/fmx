package com.kasisoft.libs.fmx.internal;

import static com.kasisoft.libs.fmx.internal.Messages.*;

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
  
  private void emitWith( TranslationContext ctx ) {
    String name            = getName();
    String modelExpression = getModelExpression();
    String var             = ctx.newVar();
    ctx.appendF( "[#assign %s=%s! /]\n", var, name );
    ctx.appendF( "[#assign %s=%s /]\n", name, modelExpression );
    emitChildren( ctx );
    ctx.appendF( "[#assign %s=%s /]\n", name, var );
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
  
  private String getModelExpression() {
    Attr modelNode = getNode().getAttributeNodeNS( FmxTranslator2.FMX_NAMESPACE, FmxAttr.model.name() );
    if( modelNode == null ) {
      throw new FmxException( error_with_values );
    }
    String result = StringFunctions.cleanup( modelNode.getNodeValue() );
    if( result == null ) {
      throw new FmxException( error_with_values );
    }
    return result;
  }
  
  private void emitDepends( TranslationContext ctx ) {
    String dependsExpression = getDependsExpression();
    ctx.appendF( "[#if %s]\n", dependsExpression );
    emitChildren( ctx );
    ctx.appendF( "[/#if]\n" );
  }
  
  private String getDependsExpression() {
    Attr valueNode = getNode().getAttributeNodeNS( FmxTranslator2.FMX_NAMESPACE, FmxAttr.value.name() );
    if( valueNode == null ) {
      throw new FmxException( error_depends_values );
    }
    String result = StringFunctions.cleanup( valueNode.getNodeValue() );
    if( result == null ) {
      throw new FmxException( error_depends_values );
    }
    return result;
  }
  
  private void emitList( TranslationContext ctx ) {
    String listExpression = getListExpression();
    String iteratorName   = getIteratorName();
    ctx.appendF( "[#list %s as %s]\n", listExpression, iteratorName );
    emitChildren( ctx );
    ctx.appendF( "[/#list]\n" );
  }
  
  private String getListExpression() {
    Attr   valueNode = getNode().getAttributeNodeNS( FmxTranslator2.FMX_NAMESPACE, FmxAttr.value.name() );
    if( valueNode == null ) {
      throw new FmxException( error_list_values );
    }
    String result = StringFunctions.cleanup( valueNode.getNodeValue() );
    if( result == null ) {
      throw new FmxException( error_list_values );
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

  private void emitInclude( TranslationContext ctx ) {
    String path = getIncludePath();
    ctx.appendF( "[#include '%s' /]\n", path );
  }

  private void emitImport( TranslationContext ctx ) {
    String path = getImportPath();
    String name = getImportName();
    ctx.appendF( "[#import '%s' as %s /]\n", path, name );
  }

  private String getImportName() {
    Attr attr = FmxAttr.name.getAttr( getNode() );
    if( attr == null ) {
      throw new FmxException( error_import_values );
    }
    String result = StringFunctions.cleanup( attr.getNodeValue() ); 
    if( result == null ) {
      throw new FmxException( error_import_values );
    }
    return result;
  }
  
  private String getImportPath() {
    Attr attr = FmxAttr.path.getAttr( getNode() );
    if( attr == null ) {
      throw new FmxException( error_import_values );
    }
    String result = StringFunctions.cleanup( attr.getNodeValue() ); 
    if( result == null ) {
      throw new FmxException( error_import_values );
    }
    return result;
  }

  private String getIncludePath() {
    if( attributes.size() != 1 ) {
      throw new FmxException( error_include_values );
    }
    FmxAttr attr = FmxAttr.valueByAttr( attributes.get(0) );
    if( attr != FmxAttr.path ) {
      throw new FmxException( error_include_values );
    }
    String result = StringFunctions.cleanup( attributes.get(0).getNodeValue() ); 
    if( result == null ) {
      throw new FmxException( error_include_values );
    }
    return result;
  }
  
  private void emitDoctype( TranslationContext ctx ) {
    ctx.appendF( "<!doctype %s>\n", getDoctype() );
  }

  private String getDoctype() {
    String result = null;
    if( attributes.size() > 1 ) {
      throw new FmxException( error_doctype_values );
    } else if( attributes.size() == 1 ) {
      FmxAttr attr = FmxAttr.valueByAttr( attributes.get(0) );
      if( attr != FmxAttr.value ) {
        throw new FmxException( error_doctype_values );
      }
      result = StringFunctions.cleanup( attributes.get(0).getNodeValue() );
    }
    if( result == null ) {
      result = "html";
    }
    return result;
  }
  
  private void emitDirective( TranslationContext ctx ) {
    String name = ctx.getDirectiveProvider().apply( getNode().getLocalName() );
    ctx.appendF( "[@%s", name );
    getAttributes().forEach( ctx::fmAttribute );
    ctx.append( "]\n" );
    emitChildren( ctx );
    ctx.appendF( "[/@%s]\n", name );
  }
  
} /* ENDCLASS */
