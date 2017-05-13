[#assign fmx_old2=gollum! /]
[#assign gollum=items /]
  <my-element a="first" b="second">
    <inner c="${gollum}"/>
  </my-element>
[#assign gollum=fmx_old2 /]
