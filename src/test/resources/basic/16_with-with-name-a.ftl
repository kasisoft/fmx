<#assign fmx_old2=bilbo! />
<#assign bilbo=items />
  <my-element a="first" b="second">
    <inner c="${bilbo}"/>
  </my-element>
<#assign bilbo=fmx_old2 />
