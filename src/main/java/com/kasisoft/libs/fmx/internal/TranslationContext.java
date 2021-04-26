package com.kasisoft.libs.fmx.internal;

import static com.kasisoft.libs.fmx.FmxConstants.*;
import static com.kasisoft.libs.fmx.internal.Messages.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.validation.constraints.*;

import java.util.function.*;

import java.util.stream.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import lombok.experimental.*;

import lombok.*;

/**
 * @author daniel.kasmeroglu@kasisoft.com
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class TranslationContext extends DefaultHandler {

  private static final String FMT_S_CLOSE_DEPENDS               = "\n%s[/#if]";
  private static final String FMT_A_CLOSE_DEPENDS               = "\n%s</#if>";

  private static final String FMT_S_CLOSE_LIST                  = "\n%s[/#list]";
  private static final String FMT_A_CLOSE_LIST                  = "\n%s</#list>";

  private static final String FMT_S_OPEN_LIST                   = "%s[#list %s as %s]\n";
  private static final String FMT_A_OPEN_LIST                   = "%s<#list %s as %s>\n";

  private static final String FMT_S_CLOSE_WITH                  = "\n%s[#assign %s=%s /]";
  private static final String FMT_A_CLOSE_WITH                  = "\n%s<#assign %s=%s />";

  private static final String FMT_S_OPEN_WITH_2                 = "%s[#assign %s=%s /]\n";
  private static final String FMT_A_OPEN_WITH_2                 = "%s<#assign %s=%s />\n";

  private static final String FMT_S_OPEN_WITH_1                 = "%s[#assign %s=%s! /]\n";
  private static final String FMT_A_OPEN_WITH_1                 = "%s<#assign %s=%s! />\n";

  private static final String FMT_S_CLOSE_WRAP_2                = "%s[/#if]";
  private static final String FMT_A_CLOSE_WRAP_2                = "%s</#if>";

  private static final String FMT_S_CLOSE_WRAP_1                = "\n%s[#else]";
  private static final String FMT_A_CLOSE_WRAP_1                = "\n%s<#else>";

  private static final String FMT_S_OPEN_WRAP                   = "%s[#if %s]\n";
  private static final String FMT_A_OPEN_WRAP                   = "%s<#if %s>\n";

  private static final String FMT_S_EMIT_LIST_CLOSE             = "[/#list]";
  private static final String FMT_A_EMIT_LIST_CLOSE             = "</#list>";

  private static final String FMT_S_EMIT_LIST_OPEN              = "[#list %s as %s]";
  private static final String FMT_A_EMIT_LIST_OPEN              = "<#list %s as %s>";

  private static final String FMT_S_EMIT_IMPORT_OPEN            = "[#import '%s' as %s /]";
  private static final String FMT_A_EMIT_IMPORT_OPEN            = "<#import '%s' as %s />";

  private static final String FMT_S_EMIT_ASSIGN                 = "[#assign %s = %s /]";
  private static final String FMT_A_EMIT_ASSIGN                 = "<#assign %s = %s />";

  private static final String FMT_S_EMIT_INCLUDE_OPEN           = "[#include '%s' /]";
  private static final String FMT_A_EMIT_INCLUDE_OPEN           = "<#include '%s' />";

  private static final String FMT_S_EMIT_DEPENDS_CLOSE          = "[/#if]";
  private static final String FMT_A_EMIT_DEPENDS_CLOSE          = "</#if>";

  private static final String FMT_S_EMIT_DEPENDS_OPEN           = "[#if %s]";
  private static final String FMT_A_EMIT_DEPENDS_OPEN           = "<#if %s>";

  private static final String FMT_S_EMIT_DIRECTIVE_CLOSE        = "[/@%s]";
  private static final String FMT_A_EMIT_DIRECTIVE_CLOSE        = "</@%s>";

  private static final String FMT_S_EMIT_DIRECTIVE_OPEN         = "[@%s";
  private static final String FMT_A_EMIT_DIRECTIVE_OPEN         = "<@%s";

  private static final String FMT_S_EMIT_WITH_CLOSE             = "[#assign %s=%s /]\n";
  private static final String FMT_A_EMIT_WITH_CLOSE             = "<#assign %s=%s />\n";

  private static final String FMT_S_EMIT_WITH_OPEN_2            = "[#assign %s=%s /]";
  private static final String FMT_A_EMIT_WITH_OPEN_2            = "<#assign %s=%s />";

  private static final String FMT_S_EMIT_WITH_OPEN_1            = "[#assign %s=%s! /]\n";
  private static final String FMT_A_EMIT_WITH_OPEN_1            = "<#assign %s=%s! />\n";

  private static final String FMT_S_EMIT_ESCAPE_CLOSE           = "[/#escape]";
  private static final String FMT_A_EMIT_ESCAPE_CLOSE           = "</#escape>";

  private static final String FMT_S_EMIT_ESCAPE_OPEN            = "[#escape %s as %s]";
  private static final String FMT_A_EMIT_ESCAPE_OPEN            = "<#escape %s as %s>";

  private static final String FMT_S_EMIT_COMPRESS_CLOSE         = "[/#compress]";
  private static final String FMT_A_EMIT_COMPRESS_CLOSE         = "</#compress>";

  private static final String FMT_S_EMIT_COMPRESS_OPEN          = "[#compress]";
  private static final String FMT_A_EMIT_COMPRESS_OPEN          = "<#compress>";

  private static final String FMT_S_EMIT_DEFAULT_OPEN           = "[#default]";
  private static final String FMT_A_EMIT_DEFAULT_OPEN           = "<#default>";

  private static final String FMT_S_EMIT_NESTED_OPEN            = "[#nested]";
  private static final String FMT_A_EMIT_NESTED_OPEN            = "<#nested>";

  private static final String FMT_S_EMIT_MACRO_CLOSE            = "[/#macro]";
  private static final String FMT_A_EMIT_MACRO_CLOSE            = "</#macro>";

  private static final String FMT_S_EMIT_MACRO_OPEN_2           = "]";
  private static final String FMT_A_EMIT_MACRO_OPEN_2           = ">";

  private static final String FMT_S_EMIT_MACRO_OPEN_1           = "[#macro %s";
  private static final String FMT_A_EMIT_MACRO_OPEN_1           = "<#macro %s";

  private static final String FMT_S_EMIT_OPTION_CLOSE           = "[#break]";
  private static final String FMT_A_EMIT_OPTION_CLOSE           = "<#break>";

  private static final String FMT_S_EMIT_OPTION_OPEN            = "[#case %s]";
  private static final String FMT_A_EMIT_OPTION_OPEN            = "<#case %s>";

  private static final String FMT_S_EMIT_SELECT_CLOSE           = "[/#switch]";
  private static final String FMT_A_EMIT_SELECT_CLOSE           = "</#switch>";

  private static final String FMT_S_EMIT_SELECT_OPEN            = "[#switch %s]";
  private static final String FMT_A_EMIT_SELECT_OPEN            = "<#switch %s>";

  private static final String FMT_S_XML_ATTRIBUTE               = "[#if (%s)?has_content] %s=\"${%s}\"[/#if]";
  private static final String FMT_A_XML_ATTRIBUTE               = "<#if (%s)?has_content> %s=\"${%s}\"</#if>";

  private static final AtomicLong COUNTER = new AtomicLong();

  String                                            content;
  StringBuilder                                     replacer;
  StringBuilder                                     builder;
  Locator                                           locator;

  // mapper for directive names
  Function<String, String>                          directiveProvider;
  Map<String, BiFunction<String, String, String>>   attributeMappers;

  // test for fmx configurations (uri, prefix)
  BiPredicate<String, String>                       isFmxRelevant;
  Predicate<List<XmlAttr>>                          hasFmxAttribute;

  // test if an attribute needs to be put into a context map
  Predicate<XmlAttr>                                isCtxAttribute;
  Predicate<XmlAttr>                                isNotCtxAttribute;

  // a stack of flags per element: the scenario of a normal xml element which contains fmx attributes
  // requires attributes which aren't passed through endElement so we need to remember
  Stack<Boolean>                                    fmxXml;

  // the with scenario requires to restore the previous model when done, so we need to remember the
  // temporarily used variable
  Stack<WithRecord>                                 withRecords;

  // an xml element with fmx attributes can generate multiple statements which need to be closed in the end,
  // so we remember here which statements had been generated
  Stack<Boolean>                                    fmxXmlOnElement;
  Stack<String>                                     indentions;

  // this stack is used to remember the begin of the inner xml tree, so if the wrapping-condition is false
  // we're rendering the inner content into the else part of the if-then-else ftl statement
  Stack<Integer>                                    innerWraps;

  // remembering newlines in case an fmx:depends is located on a single line, so we're dropping line feeds
  // in order to prevent ugly "disruption" of the FTL text (for instance: <i fmx:depends="x">text<i> will no
  // longer be rendered as [#if x]\n<i>text</i>\n[/#if]). we're storing pairs of [linenumber, newline-index]
  Stack<Integer[]>                                  dependsNL;

  // remember the location after the last opening xml element. if the closing xml element follows immediately
  // thereafter we can generate a directly close xml element
  int                                               lastOpen;

  // xml escaping
  int                                               xescape;
  Stack<Boolean>                                    xescapes;
  
  Set<String>                                       dontShorten;
  
  // formatting strings used to generate the code
  String                                            fmtCloseDepends;
  String                                            fmtCloseList;
  String                                            fmtOpenList;
  String                                            fmtCloseWith;
  String                                            fmtOpenWith2;
  String                                            fmtOpenWith1;
  String                                            fmtCloseWrap2;
  String                                            fmtCloseWrap1;
  String                                            fmtOpenWrap;
  String                                            fmtEmitAssign;
  String                                            fmtEmitListClose;
  String                                            fmtEmitListOpen;
  String                                            fmtEmitImportOpen;
  String                                            fmtEmitIncludeOpen;
  String                                            fmtEmitDependsClose;
  String                                            fmtEmitDependsOpen;
  String                                            fmtEmitDirectiveClose;
  String                                            fmtEmitDirectiveOpen;
  String                                            fmtEmitWithClose;
  String                                            fmtEmitWithOpen2;
  String                                            fmtEmitWithOpen1;
  String                                            fmtEmitEscapeClose;
  String                                            fmtEmitEscapeOpen;
  String                                            fmtEmitCompressClose;
  String                                            fmtEmitCompressOpen;
  String                                            fmtEmitDefaultOpen;
  String                                            fmtEmitNestedOpen;
  String                                            fmtEmitMacroClose;
  String                                            fmtEmitMacroOpen2;
  String                                            fmtEmitMacroOpen1;
  String                                            fmtEmitOptionClose;
  String                                            fmtEmitOptionOpen;
  String                                            fmtEmitSelectClose;
  String                                            fmtEmitSelectOpen;
  String                                            fmtXmlAttribute;

  
  public TranslationContext(@NotNull String fmxPrefix, Function<String, String> directives, Map<String, BiFunction<String, String, String>> mappers, boolean square) {
    dontShorten       = new HashSet<String>(Arrays.asList("script", "span", "i", "strong"));
    directiveProvider = directives;
    attributeMappers  = mappers != null ? mappers : Collections.emptyMap();
    isCtxAttribute    = $_ -> CTX_NAMESPACE.equals($_.getNsUri());
    isNotCtxAttribute = isCtxAttribute.negate();
    isFmxRelevant     = ($1, $2) -> FMX_NAMESPACE.equals($1) || (($2 != null) && $2.startsWith(fmxPrefix));
    hasFmxAttribute   = $ -> $.parallelStream()
      .map($_ -> isFmxRelevant.test($_.getNsUri(), $_.getQName()))
      .reduce(false, ($1, $2) -> $1 || $2)
      ;
    if (square) {
      initFmtSquareBrackets();
    } else {
      initFmtAngularBrackets();
    }
  }

  private void initFmtAngularBrackets() {
    fmtCloseDepends         = FMT_A_CLOSE_DEPENDS;
    fmtCloseList            = FMT_A_CLOSE_LIST;
    fmtOpenList             = FMT_A_OPEN_LIST;
    fmtCloseWith            = FMT_A_CLOSE_WITH;
    fmtOpenWith2            = FMT_A_OPEN_WITH_2;
    fmtOpenWith1            = FMT_A_OPEN_WITH_1;
    fmtCloseWrap2           = FMT_A_CLOSE_WRAP_2;
    fmtCloseWrap1           = FMT_A_CLOSE_WRAP_1;
    fmtOpenWrap             = FMT_A_OPEN_WRAP;
    fmtEmitAssign           = FMT_A_EMIT_ASSIGN;
    fmtEmitListClose        = FMT_A_EMIT_LIST_CLOSE;
    fmtEmitListOpen         = FMT_A_EMIT_LIST_OPEN;
    fmtEmitImportOpen       = FMT_A_EMIT_IMPORT_OPEN;
    fmtEmitIncludeOpen      = FMT_A_EMIT_INCLUDE_OPEN;
    fmtEmitDependsClose     = FMT_A_EMIT_DEPENDS_CLOSE;
    fmtEmitDependsOpen      = FMT_A_EMIT_DEPENDS_OPEN;
    fmtEmitDirectiveClose   = FMT_A_EMIT_DIRECTIVE_CLOSE;
    fmtEmitDirectiveOpen    = FMT_A_EMIT_DIRECTIVE_OPEN;
    fmtEmitWithClose        = FMT_A_EMIT_WITH_CLOSE;
    fmtEmitWithOpen2        = FMT_A_EMIT_WITH_OPEN_2;
    fmtEmitWithOpen1        = FMT_A_EMIT_WITH_OPEN_1;
    fmtEmitEscapeClose      = FMT_A_EMIT_ESCAPE_CLOSE;
    fmtEmitEscapeOpen       = FMT_A_EMIT_ESCAPE_OPEN;
    fmtEmitCompressClose    = FMT_A_EMIT_COMPRESS_CLOSE;
    fmtEmitCompressOpen     = FMT_A_EMIT_COMPRESS_OPEN;
    fmtEmitDefaultOpen      = FMT_A_EMIT_DEFAULT_OPEN;
    fmtEmitNestedOpen       = FMT_A_EMIT_NESTED_OPEN;
    fmtEmitMacroClose       = FMT_A_EMIT_MACRO_CLOSE;
    fmtEmitMacroOpen2       = FMT_A_EMIT_MACRO_OPEN_2;
    fmtEmitMacroOpen1       = FMT_A_EMIT_MACRO_OPEN_1;
    fmtEmitOptionClose      = FMT_A_EMIT_OPTION_CLOSE;
    fmtEmitOptionOpen       = FMT_A_EMIT_OPTION_OPEN;
    fmtEmitSelectClose      = FMT_A_EMIT_SELECT_CLOSE;
    fmtEmitSelectOpen       = FMT_A_EMIT_SELECT_OPEN;
    fmtXmlAttribute         = FMT_A_XML_ATTRIBUTE;
  }
  
  private void initFmtSquareBrackets() {
    fmtCloseDepends         = FMT_S_CLOSE_DEPENDS;
    fmtCloseList            = FMT_S_CLOSE_LIST;
    fmtOpenList             = FMT_S_OPEN_LIST;
    fmtCloseWith            = FMT_S_CLOSE_WITH;
    fmtOpenWith2            = FMT_S_OPEN_WITH_2;
    fmtOpenWith1            = FMT_S_OPEN_WITH_1;
    fmtCloseWrap2           = FMT_S_CLOSE_WRAP_2;
    fmtCloseWrap1           = FMT_S_CLOSE_WRAP_1;
    fmtOpenWrap             = FMT_S_OPEN_WRAP;
    fmtEmitAssign           = FMT_S_EMIT_ASSIGN;
    fmtEmitListClose        = FMT_S_EMIT_LIST_CLOSE;
    fmtEmitListOpen         = FMT_S_EMIT_LIST_OPEN;
    fmtEmitImportOpen       = FMT_S_EMIT_IMPORT_OPEN;
    fmtEmitIncludeOpen      = FMT_S_EMIT_INCLUDE_OPEN;
    fmtEmitDependsClose     = FMT_S_EMIT_DEPENDS_CLOSE;
    fmtEmitDependsOpen      = FMT_S_EMIT_DEPENDS_OPEN;
    fmtEmitDirectiveClose   = FMT_S_EMIT_DIRECTIVE_CLOSE;
    fmtEmitDirectiveOpen    = FMT_S_EMIT_DIRECTIVE_OPEN;
    fmtEmitWithClose        = FMT_S_EMIT_WITH_CLOSE;
    fmtEmitWithOpen2        = FMT_S_EMIT_WITH_OPEN_2;
    fmtEmitWithOpen1        = FMT_S_EMIT_WITH_OPEN_1;
    fmtEmitEscapeClose      = FMT_S_EMIT_ESCAPE_CLOSE;
    fmtEmitEscapeOpen       = FMT_S_EMIT_ESCAPE_OPEN;
    fmtEmitCompressClose    = FMT_S_EMIT_COMPRESS_CLOSE;
    fmtEmitCompressOpen     = FMT_S_EMIT_COMPRESS_OPEN;
    fmtEmitDefaultOpen      = FMT_S_EMIT_DEFAULT_OPEN;
    fmtEmitNestedOpen       = FMT_S_EMIT_NESTED_OPEN;
    fmtEmitMacroClose       = FMT_S_EMIT_MACRO_CLOSE;
    fmtEmitMacroOpen2       = FMT_S_EMIT_MACRO_OPEN_2;
    fmtEmitMacroOpen1       = FMT_S_EMIT_MACRO_OPEN_1;
    fmtEmitOptionClose      = FMT_S_EMIT_OPTION_CLOSE;
    fmtEmitOptionOpen       = FMT_S_EMIT_OPTION_OPEN;
    fmtEmitSelectClose      = FMT_S_EMIT_SELECT_CLOSE;
    fmtEmitSelectOpen       = FMT_S_EMIT_SELECT_OPEN;
    fmtXmlAttribute         = FMT_S_XML_ATTRIBUTE;
  }
  
  @Override
  public void setDocumentLocator(Locator loc) {
    locator = loc;
    super.setDocumentLocator(loc);
  }

  @Override
  public void startDocument() throws SAXException {
    builder           = new StringBuilder();
    replacer          = new StringBuilder();
    withRecords       = new Stack<>();
    fmxXml            = new Stack<>();
    fmxXmlOnElement   = new Stack<>();
    indentions        = new Stack<>();
    innerWraps        = new Stack<>();
    dependsNL         = new Stack<>();
    xescapes          = new Stack<>();
    content           = "";
    xescape           = 0;
    lastOpen          = -1;
  }

  @Override
  public void endDocument() throws SAXException {
    content           = builder.toString();
    builder.setLength(0);
    replacer.setLength(0);
    builder           = null;
    replacer          = null;
    withRecords       = null;
    fmxXml            = null;
    fmxXmlOnElement   = null;
    indentions        = null;
    innerWraps        = null;
  }

  private String newVar() {
    return String.format("fmx_old%d", COUNTER.incrementAndGet());
  }

  private void xmlAttribute(@NotNull XmlAttr attr) {
    String nsUri = attr.getNsUri();
    // only emit non-fmx attributes
    if (!FMX_NAMESPACE.equals(nsUri)) {
      String nodeValue = escapeValue(attr.getAttrValue());
      String prefix    = attr.getPrefix();
      String localName = attr.getLocalName();
      if (prefix != null) {
        // generate a test for the attribute value indicated by the namespace
        if (FMT_NAMESPACE.equals(nsUri)) {
          builder.append(String.format(fmtXmlAttribute, nodeValue, localName, nodeValue));
        } else {
          builder.append(String.format(" %s:%s=\"%s\"", prefix, localName, nodeValue));
        }
      } else {
        builder.append(String.format(" %s=\"%s\"", localName, nodeValue));
      }
    }
  }

  private String escapeValue(String nodeValue) {
    replacer.setLength(0);
    replacer.append(nodeValue);
    for (int i = replacer.length() - 1; i >= 0; i--) {
      char ch = replacer.charAt(i);
      if (ch == '\"') {
        replacer.deleteCharAt(i);
        replacer.insert(i, "&quot;");
      } else if (ch == '<') {
        replacer.deleteCharAt(i);
        replacer.insert(i, "&lt;");
      } else if (ch == '>') {
        replacer.deleteCharAt(i);
        replacer.insert(i, "&gt;");
      }
    }
    return replacer.toString();
  }

  @Override
  public String toString() {
    return content;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    boolean       isFmxXml = false;
    List<XmlAttr> attrs    = getAttributes(attributes);
    if (isFmxRelevant.test(uri, qName)) {
      // dedicated fmx:??? element
      startFmxElement(uri, localName, qName, attrs);
    } else if (hasFmxAttribute.test(attrs)) {
      // xml element which contains an fmx attribute
      startFmxXmlElement(uri, localName, qName, attrs);
      isFmxXml = true;
    } else {
      // simple xml element
      startXmlElement(uri, localName, qName, attrs);
    }
    fmxXml.push(isFmxXml);
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    boolean isFmxXml = fmxXml.pop();
    if (isFmxRelevant.test(uri, qName)) {
      // dedicated fmx:??? element
      endFmxElement(uri, localName, qName);
    } else if (isFmxXml) {
      // xml element which contains an fmx attribute
      endFmxXmlElement(uri, localName, qName);
    } else {
      // simple xml element
      endXmlElement(uri, localName, qName);
    }
  }

  private void startFmxElement(String uri, String localName, String qName, List<XmlAttr> attrs) {
    /** @todo [25-AUG-2020:KASI]   Use a map with functions for that */
    FmxElementType type = FmxElementType.valueByName(localName, FmxElementType.directive);
    switch (type) {
    case assign       : emitAssign       (uri, localName, qName, attrs); break;
    case directive    : emitDirectiveOpen(uri, localName, qName, attrs); break;
    case doctype      : emitDoctypeOpen  (uri, localName, qName, attrs); break;
    case include      : emitIncludeOpen  (uri, localName, qName, attrs); break;
    case importDecl   : emitImportOpen   (uri, localName, qName, attrs); break;
    case list         : emitListOpen     (uri, localName, qName, attrs); break;
    case depends      : emitDependsOpen  (uri, localName, qName, attrs); break;
    case with         : emitWithOpen     (uri, localName, qName, attrs); break;
    case escape       : emitEscapeOpen   (uri, localName, qName, attrs); break;
    case compress     : emitCompressOpen (uri, localName, qName, attrs); break;
    case select       : emitSelectOpen   (uri, localName, qName, attrs); break;
    case option       : emitOptionOpen   (uri, localName, qName, attrs); break;
    case defaultcase  : emitDefaultOpen  (uri, localName, qName, attrs); break;
    case macro        : emitMacroOpen    (uri, localName, qName, attrs); break;
    case nested       : emitNestedOpen   (uri, localName, qName, attrs); break;
    case xescape      : emitXescapeOpen  (uri, localName, qName, attrs); break;
    }
  }

  private void endFmxElement( String uri, String localName, String qName ) {
    /** @todo [25-AUG-2020:KASI]   Use a map with functions for that */
    FmxElementType type = FmxElementType.valueByName(localName, FmxElementType.directive);
    switch (type) {
    case directive    : emitDirectiveClose (uri, localName, qName); break;
    case list         : emitListClose      (uri, localName, qName); break;
    case depends      : emitDependsClose   (uri, localName, qName); break;
    case with         : emitWithClose      (uri, localName, qName); break;
    case escape       : emitEscapeClose    (uri, localName, qName); break;
    case compress     : emitCompressClose  (uri, localName, qName); break;
    case select       : emitSelectClose    (uri, localName, qName); break;
    case option       : emitOptionClose    (uri, localName, qName); break;
    case defaultcase  : emitDefaultClose   (uri, localName, qName); break;
    case macro        : emitMacroClose     (uri, localName, qName); break;
    case nested       : emitNestedClose    (uri, localName, qName); break;
    case xescape      : emitXescapeClose   (uri, localName, qName); break;
    }
  }

  private void startFmxXmlElement(String uri, String localName, String qName, List<XmlAttr> attrs) {

    String dependsExpression  = FmxAttr . depends . getValue(attrs);
    String listExpression     = FmxAttr . list    . getValue(attrs);
    String iteratorName       = FmxAttr . it      . getValue(attrs, "it");
    String withExpression     = FmxAttr . with    . getValue(attrs);
    String withName           = FmxAttr . name    . getValue(attrs, "model");
    String wrapExpression     = FmxAttr . wrap    . getValue(attrs);
    String xescapeExpression  = FmxAttr . xescape . getValue(attrs);

    String indent = getCurrentIndent();
    if (indent.length() > 0) {
      // remove the current indention
      builder.setLength(builder.length() - indent.length());
    }
    indentions.push(indent);

    openDepends(indent, dependsExpression);
    openWith(indent, withExpression, withName);
    openList(indent, listExpression, iteratorName);
    openWrap(indent, wrapExpression);

    if (xescapeExpression != null) {
      xescape++;
      xescapes.push(true);
    } else {
      xescapes.push(false);
    }

    // add the previously removed indention AFTER the ftl conditions have been rendered
    builder.append(indent);
    startXmlElement(uri, localName, qName, attrs);

    // remember the begin of the inner xml block
    innerWraps.push(builder.length());

  }

  private void endFmxXmlElement(String uri, String localName, String qName) {

    // capture the range of the inner statement
    int open  = innerWraps.pop();
    int close = builder.length();

    endXmlElement(uri, localName, qName);

    boolean isxescape = xescapes.pop();
    if (isxescape) {
      xescape--;
    }

    String indent = indentions.pop();
    closeWrap(indent, open, close);
    closeList(indent);
    closeWith(indent);
    closeDepends(indent);

  }

  private void startXmlElement(String uri, String localName, String qName, List<XmlAttr> attrs) {
    String prefix = null;
    int    colon  = qName.indexOf(':');
    if (colon > 0) {
      prefix = qName.substring(0, colon);
    }
    builder.append(xlt()).append(qName);
    attrs.forEach(this::xmlAttribute);
    if (prefix != null) {
      xmlAttribute(new XmlAttr(null, String.format("xmlns:%s", prefix), uri));
    }
    builder.append(xgt());
    lastOpen = builder.length();
  }

  private void endXmlElement(String uri, String localName, String qName) {
    /** @note [13-SEP-2020:KASI]   Don't shorten for a 'script' tag as following tag will be embedded for some weird reason */
    boolean doNotShort = (qName != null) && dontShorten.contains(qName.toLowerCase());
    if ((builder.length() == lastOpen) && (!doNotShort)) {
      // get rid of '>' and replace it with a directly closing '/>'
      builder.setLength(builder.length() - xgt().length());
      builder.append(String.format("/%s", xgt()));
    } else {
      builder.append(String.format("%s/%s%s", xlt(), qName, xgt()));
    }
  }

  private String xlt() {
    return xescape > 0 ? "&lt;" : "<";
  }

  private String xgt() {
    return xescape > 0 ? "&gt;" : ">";
  }

  // xescape
  private void emitXescapeOpen(String uri, String localName, String qName, List<XmlAttr> attrs) {
    xescape++;
  }

  private void emitXescapeClose(String uri, String localName, String qName) {
    xescape--;
  }

  // select (switch)

  private void emitSelectOpen(String uri, String localName, String qName, List<XmlAttr> attrs) {
    String value = FmxAttr.value.getValue(attrs);
    builder.append(String.format(fmtEmitSelectOpen, value));
  }

  private void emitSelectClose(String uri, String localName, String qName) {
    builder.append(fmtEmitSelectClose);
  }

  // option (case)

  private void emitOptionOpen(String uri, String localName, String qName, List<XmlAttr> attrs) {
    String expected = FmxAttr.value.getValue(attrs);
    builder.append(String.format(fmtEmitOptionOpen, expected));
  }

  private void emitOptionClose(String uri, String localName, String qName) {
    builder.append(fmtEmitOptionClose);
  }

  // macro
  private void emitMacroOpen(String uri, String localName, String qName, List<XmlAttr> attrs) {
    String name = FmxAttr.name.getRequiredValue(attrs, error_macro_without_name);
    builder.append(String.format(fmtEmitMacroOpen1, name));
    Predicate<XmlAttr> isFmxAttr    = $ -> isFmxRelevant.test($.getNsUri(), $.getQName());
    Predicate<XmlAttr> isNoFmxAttr  = isFmxAttr.negate();
    attrs.stream()
      .filter(isNoFmxAttr)
      .map(XmlAttr::getLocalName)
      .forEach($ -> builder.append(' ').append($));
    builder.append(fmtEmitMacroOpen2);
  }

  private void emitMacroClose(String uri, String localName, String qName) {
    builder.append(fmtEmitMacroClose);
  }

  // macro
  private void emitNestedOpen(String uri, String localName, String qName, List<XmlAttr> attrs) {
    builder.append(fmtEmitNestedOpen);
  }

  private void emitNestedClose(String uri, String localName, String qName) {
  }

  // defaultcase (default)

  private void emitDefaultOpen(String uri, String localName, String qName, List<XmlAttr> attrs) {
    builder.append(fmtEmitDefaultOpen);
  }

  private void emitDefaultClose(String uri, String localName, String qName) {
  }

  // compress

  private void emitCompressOpen(String uri, String localName, String qName, List<XmlAttr> attrs) {
    builder.append(fmtEmitCompressOpen);
  }

  private void emitCompressClose(String uri, String localName, String qName) {
    builder.append(fmtEmitCompressClose);
  }

  // with

  private void emitEscapeOpen(String uri, String localName, String qName, List<XmlAttr> attrs) {
    String name = FmxAttr.name.getRequiredValue(attrs, error_escape_without_name);
    String expr = FmxAttr.expr.getRequiredValue(attrs, error_escape_without_expr);
    builder.append(String.format(fmtEmitEscapeOpen, name, expr));
  }

  private void emitEscapeClose(String uri, String localName, String qName) {
    builder.append(fmtEmitEscapeClose);
  }

  // with

  private void emitWithOpen(String uri, String localName, String qName, List<XmlAttr> attrs) {
    String modelName  = FmxAttr.name.getValue(attrs, "model");
    String modelExpr  = FmxAttr.value.getRequiredValue(attrs, error_with_values);
    // this variable name is used in case it already exists within the model, so we're backing it up before
    String var = newVar();
    builder.append(String.format(fmtEmitWithOpen1, var, modelName));
    builder.append(String.format(fmtEmitWithOpen2, modelName, modelExpr));
    withRecords.push(new WithRecord(modelName, var));
  }

  private void emitWithClose(String uri, String localName, String qName) {
    WithRecord record = withRecords.pop();
    builder.append(String.format(fmtEmitWithClose, record.getModelname(), record.getVarname()));
  }

  // assign
  private void emitAssign(String uri, String localName, String qName, List<XmlAttr> attrs) {
    String name  = FmxAttr.name.getValue(attrs);
    String value = FmxAttr.value.getValue(attrs);
    builder.append(String.format(fmtEmitAssign, name, value));
  }
  
  // directive

  private void emitDirectiveOpen(String uri, String localName, String qName, List<XmlAttr> attrs) {
    String ftlName = directiveProvider.apply(localName);
    builder.append(String.format(fmtEmitDirectiveOpen, ftlName));
    // write down all attributes that shall be kept
    attrs.stream().filter(isNotCtxAttribute).forEach($ -> fmAttribute(ftlName, $));
    // generate a context map attribute if desired
    emitContextAttributes(attrs.stream().filter(isCtxAttribute).collect(Collectors.toList()));
    builder.append(fmtEmitMacroOpen2);
  }

  private void emitContextAttributes(List<XmlAttr> ctxAttrs) {
    if (!ctxAttrs.isEmpty()) {
      // write down the special attributes passed as a map
      builder.append(String.format(" %s={", getContextAttributeName(ctxAttrs)));
      // if there's only one element is the attribute name so we don't need to go through the attributes
      if (ctxAttrs.size() > 1) {
        ctxAttrs.forEach(this::ctxAttribute);
        builder.setLength(builder.length() - 2); // get rid of the last ', ' sequence
      }
      builder.append("}");
    }
  }

  private String getContextAttributeName(List<XmlAttr> ctxAttrs) {
    String            result    = null;
    Optional<XmlAttr> attrName  = ctxAttrs.stream().filter($ -> CTX_ATTRIBUTE_NAME.equals($.getLocalName())).findAny();
    if (attrName.isPresent()) {
      result = FmxUtils.cleanup(attrName.get().getAttrValue());
    }
    return result != null ? result : CTX_DEFAULT_NAME;
  }

  private void emitDirectiveClose(String uri, String localName, String qName) {
    String name = directiveProvider.apply(localName);
    builder.append(String.format(fmtEmitDirectiveClose, name));
  }

  private void ctxAttribute(XmlAttr attr) {
    // ignore this attribute as it's only supposed to provide a name
    if (!CTX_ATTRIBUTE_NAME.equals(attr.getLocalName())) {
      builder.append(String.format("'%s': %s, ", attr.getLocalName(), attr.getAttrValue()));
    }
  }

  private void fmAttribute(String ftlName, XmlAttr attr) {
    if (!FMX_NAMESPACE.equals(attr.getNsUri())) {
      // change the attribute value depending on the current directive
      String value = getAttributeMapper(ftlName).apply(attr.getLocalName(), attr.getAttrValue());
      builder.append(String.format(" %s=%s", attr.getQName(), value));
    }
  }

  @NotNull
  private BiFunction<String, String, String> getAttributeMapper(String ftlName) {
    BiFunction<String, String, String> result = attributeMappers.get(ftlName);
    if (result == null) {
      result = this::defaultAttributeMapper;
    }
    return result;
  }

  private String defaultAttributeMapper(String localName, String value) {
    if (value.startsWith("'") && value.endsWith("'")) {
      // passing an ordinary value
      value = String.format("\"%s\"", value.substring(1, value.length() - 1));
    }
    return value;
  }

  // depends

  private void emitDependsOpen(String uri, String localName, String qName, List<XmlAttr> attrs) {
    String dependsExpr = FmxAttr.value.getRequiredValue(attrs, error_depends_values);
    builder.append(String.format(fmtEmitDependsOpen, dependsExpr));
  }

  private void emitDependsClose(String uri, String localName, String qName) {
    builder.append(fmtEmitDependsClose);
  }

  // doctype

  private void emitDoctypeOpen(String uri, String localName, String qName, List<XmlAttr> attrs) {
    builder.append(String.format("<!doctype %s>", FmxAttr.value.getValue(attrs, "html")));
  }

  // include

  private void emitIncludeOpen(String uri, String localName, String qName, List<XmlAttr> attrs) {
    String path = FmxAttr.path.getRequiredValue(attrs, error_include_values);
    builder.append(String.format(fmtEmitIncludeOpen, path));
  }

  // import

  private void emitImportOpen(String uri, String localName, String qName, List<XmlAttr> attrs) {
    String path = FmxAttr.path.getRequiredValue(attrs, error_import_values);
    String name = FmxAttr.name.getRequiredValue(attrs, error_import_values);
    builder.append(String.format(fmtEmitImportOpen, path, name));
  }

  // list

  private void emitListOpen(String uri, String localName, String qName, List<XmlAttr> attrs) {
    String listExpression = FmxAttr.value.getRequiredValue(attrs, error_list_values);
    String iteratorName   = FmxAttr.it.getValue(attrs, "it");
    builder.append(String.format(fmtEmitListOpen, listExpression, iteratorName));
  }

  private void emitListClose(String uri, String localName, String qName) {
    builder.append(fmtEmitListClose);
  }

  private void openWrap(String indent, String wrapExpression) {
    if (wrapExpression != null) {
      builder.append(String.format(fmtOpenWrap, indent, wrapExpression));
    }
    fmxXmlOnElement.push(wrapExpression != null);
  }

  private void closeWrap(String indent, int open, int close) {
    boolean hasWrapExpression = fmxXmlOnElement.pop();
    if (hasWrapExpression) {
      String innerContent = FmxUtils.trimTrailing(builder.substring(open, close), " \t");
      builder.append(String.format(fmtCloseWrap1, indent));
      builder.append(innerContent);
      builder.append(String.format(fmtCloseWrap2, indent));
    }
  }

  private String getCurrentIndent() {
    String result = "";
    int    i      = builder.length() - 1;
    while (i >= 0) {
      char ch = builder.charAt(i);
      if (ch == '\n') {
        result = builder.substring(i + 1, builder.length());
        break;
      } else if (!Character.isWhitespace(ch)) {
        // there is some non-whitespace text after the line break,
        // so it seems intentional which means we don't get rid of whitespace
        result = "";
        break;
      }
      i--;
    }
    return result;
  }

  private void openWith(String indent, String withExpression, String modelName) {
    WithRecord result = null;
    if (withExpression != null) {
      String varname = newVar();
      builder.append(String.format(fmtOpenWith1, indent, varname, modelName));
      builder.append(String.format(fmtOpenWith2, indent, modelName, withExpression));
      result = new WithRecord(modelName, varname);
    }
    withRecords.push(result);
  }

  private void closeWith(String indent) {
    WithRecord result = withRecords.pop();
    if (result != null) {
      builder.append(String.format(fmtCloseWith, indent, result.getModelname(), result.getVarname()));
    }
  }

  private void openList(String indent, String listExpression, String iteratorName) {
    if (listExpression != null) {
      builder.append(String.format(fmtOpenList, indent, listExpression, iteratorName));
    }
    fmxXmlOnElement.push(listExpression != null);
  }

  private void closeList(String indent) {
    boolean hasListExpression = fmxXmlOnElement.pop();
    if (hasListExpression) {
      builder.append(String.format(fmtCloseList, indent));
    }
  }

  private void openDepends(String indent, String dependsExpression) {
    if (dependsExpression != null) {
      builder.append(String.format(fmtOpenWrap, indent, dependsExpression));
      dependsNL.push(new Integer[] {locator.getLineNumber(), builder.length() - 1});
    }
    fmxXmlOnElement.push(dependsExpression != null);
  }

  private void closeDepends(String indent) {
    boolean hasDependsExpression = fmxXmlOnElement.pop();
    if (hasDependsExpression) {
      Integer[] nl = dependsNL.pop();
      if (nl[0].intValue() == locator.getLineNumber()) {
        // this fmx:depends element is located on a single line, so alter the output accordingly
        builder.deleteCharAt(nl[1].intValue());
        builder.append(String.format(fmtCloseWrap2, indent));
      } else {
        builder.append(String.format(fmtCloseDepends, indent));
      }
    }
  }

  @Override
  public void characters(char ch[], int start, int length) throws SAXException {
    builder.append(ch, start, length);
  }

  @Override
  public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
    characters(ch, start, length);
  }

  private List<XmlAttr> getAttributes(Attributes attributes) {
    List<XmlAttr> result = Collections.<XmlAttr>emptyList();
    if (attributes != null) {
      result = new ArrayList<>(attributes.getLength());
      for (int i = 0; i < attributes.getLength(); i++) {
        result.add(new XmlAttr(attributes.getURI(i), attributes.getQName(i), attributes.getValue(i)));
      }
      Collections.sort(result);
    }
    return result;
  }

  @AllArgsConstructor
  @Getter @Setter
  private static class WithRecord {

    String   modelname;
    String   varname;

  } /* ENDCLASS */

} /* ENDCLASS */
