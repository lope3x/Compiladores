/*
Start -> {Declaração; | Comando} EOF
Bloco_ou_Comando -> Comando | Bloco
Bloco -> "{"{Comando}"}"
Tipo -> int | float | string | char
Declaração -> Tipo Declaração_Init {,Declaração_Init} | const id = [-] const_value
Declaração_Init -> id [<- [-] const_value]
Escrita -> "("Expressão {, Expressão} ")"
Comando -> id ["["Expressão"]"] <- Expressão ; | Repetição | Teste | ; | readln"("id")" ; | (write| writeln) Escrita ;
Repetição -> while Expressão Bloco_ou_Comando
Teste -> if Expressão Bloco_ou_Comando [else Bloco_ou_Comando]
Operadores_Relacional -> = | != | < | > | <= | >=
Expressão -> Expressão1 { Operadores_Relacional Expressão1}
Expressão1 -> [-] Expressão2 {(- | + | || )  Expressão2}
Expressão2 -> Expressão3 {(* | && | / | div | mod) Expressão3}
Expressão3 -> {!} Expressão4
Expressão4 -> ( (int | float) "("Expressão")"  | Expressão5)
Expressão5 -> "(" Expressão ")" | Expressão6
Expressão6 -> const_value | id ["[" Expressão "]"]
*/

if 2<5 {

}