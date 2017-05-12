package com.kasisoft.libs.fmx.internal;

import com.kasisoft.libs.common.text.*;

import org.w3c.dom.*;

import javax.annotation.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.net
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@ToString(callSuper = true)
public class XmlNode extends NodeWrapper<Node> {

  public XmlNode( @Nonnull Node xmlNode ) {
    super( xmlNode );
  }
  
  @Override
  public void emit( TranslationContext ctx ) {
    int    type    = getNode().getNodeType();
    String value   = getNode().getNodeValue();
    String content = StringFunctions.cleanup( value );
    if( content != null ) {
      if( type == Node.CDATA_SECTION_NODE ) {
        ctx.append( "<![CDATA[" );
      }
      ctx.append( content );
      if( type == Node.CDATA_SECTION_NODE ) {
        ctx.append( "]]>");
      }
      ctx.append( "\n" );
    }
  }
  
} /* ENDCLASS */
