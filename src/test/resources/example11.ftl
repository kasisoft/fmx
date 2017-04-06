[#if model.link?has_content]
<a href="${model.link.link!}">
  <div>my content</div>
</a>
[#else]

  <div>my content</div>
[/#if]
