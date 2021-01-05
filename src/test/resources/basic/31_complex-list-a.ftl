<#list model.list as it>
  <li>
    <a href="${it.link!}"><#if it.icon?has_content><i class="fa ${it.icon}"></i></#if>${it.name!}</a>
  </li>
</#list>
