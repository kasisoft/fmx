[#assign fmx_old0=gollum! /]
[#assign gollum=items /]
  <my-element a="first" b="second">
    <inner c="${gollum}"/>
  </my-element>
[#assign gollum=fmx_old0 /]
