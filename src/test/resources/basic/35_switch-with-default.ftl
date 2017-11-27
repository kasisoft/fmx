[#switch model.type]
  [#case model.t]
    <p>NO CASE</p>
  [#break]
  [#case 'myliteral']
    <p>LITERAL</p>
  [#break]
  [#default]
    <p>DEFAULT</p>
  
[/#switch]
