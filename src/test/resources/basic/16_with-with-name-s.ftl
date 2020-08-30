[#assign fmx_old6=bilbo! /]
[#assign bilbo=items /]
  <my-element a="first" b="second">
    <inner c="${bilbo}"/>
  </my-element>
[#assign bilbo=fmx_old6 /]
