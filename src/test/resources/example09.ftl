[#if (wumpi) || wobble]
[#list components as component]
[@cms.component content="component"]
[#include '/dodo/bibo.ftl' /]
  <p>${wobble}</p>
[/@cms.component]
[/#list]
[/#if]
