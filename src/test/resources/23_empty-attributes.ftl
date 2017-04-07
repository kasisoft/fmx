[#list items as it]
<my-element a="first"[#if (it)?has_content] b="${it}"[/#if]>
  <inner c="third"/>
</my-element>
[/#list]
