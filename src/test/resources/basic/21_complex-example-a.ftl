<#if gollum>
  <#list dodo as loki>
    <#if loki>
    <my-element a="${loki}" b="second">
      <inner1 c="third"/>
      <inner2 c="third"/>
    </my-element>
    <#else>
      <inner1 c="third"/>
      <inner2 c="third"/>
    </#if>
  </#list>
</#if>