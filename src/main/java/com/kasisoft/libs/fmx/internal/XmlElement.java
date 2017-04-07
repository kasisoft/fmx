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
@ToString(callSuper = true)
public class XmlElement extends NodeWrapper<Element> {

  List<Attr>    attributes;
  String        tag;
  
  public XmlElement( @Nonnull Element xmlElement, @Nonnull List<Attr> xmlAttributes ) {
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
    if( getChildren().isEmpty() ) {
      ctx.writeXmlTag( tag, attributes );
    } else {
      ctx.openXmlTag( tag, attributes );
      emitChildren( ctx );
      ctx.closeXmlTag( tag );
    }
  }
  
} /* ENDCLASS */
