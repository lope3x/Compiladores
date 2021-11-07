# Compiladores

###TP01, T02, T03 e T04 - Compiladores - 2021/2
###G07 - Bruno Duarte de Paula Assis (639985), Gabriel Lopes Ferreira(619148), Giovanni Carlos Guaceroni(636206)


## Como compilar o código e executar
- Escreva o programa da linguagem L valido em um arquivo por exemplo``teste.l``
- Em seguida execute o comando ``javac Compiler.java && java Compiler < teste.l`` ou rode o script  ``./run.sh``
- Se a compilação ocorrer com sucesso será criado um arquivo chamado ``arq.asm`` no diretório local
- Em seguida para rodar o programa execute o ``./run_asm.sh`` que irá montar o assembly utilizando o NASM, e linkeditar o programa e em seguida irá rodar o mesmo.


##Descrição dos arquivos
- No arquivo Compiler.java temos o compilador, com a implementação dos analisadores Léxico, Sintático e Semântico, além da geração de código.
- Dividimos o nosso esquema de tradução em dois arquivos, um deles com o esquema de tradução com a verificação de tipos o outro com o esquema de tradução com a geração de código.
- No arquivo ``GRAMATICA_ESQUEMA_TRADUCAO_SEMANTICO`` temos o nosso esquema de tradução para verificação de tipos da linguagem.
- No arquivo ``GRAMATICA_ESQUEMA_GERACAO_CODIGO`` temos o nosso esquema de tradução para geração de código assembly, ele foi feito considerando que a verificação semântica já ocorreu e não temos problema semânticos.