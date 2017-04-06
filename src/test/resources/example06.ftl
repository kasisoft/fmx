[#if wobble]
[#list components as component]
[@cms.component content="component"]
  <p>${wobble}</p>
[/@cms.component]
[/#list]
[/#if]
