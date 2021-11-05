section .data
M:
resb 10000h; Reserva para Temporarios
section .text
global _start
_start:
section .data
dd 5 ;65536
section .text
mov eax, [M+65536]
mov rsi, M+0
mov rcx, 0 ;contador pilha
mov rdi, 0 ;tam. string convertido
cmp eax, 0 ;verifica sinal
jge Label0;salta se número positivo
mov bl, '-' ;senão, escreve sinal –
mov [rsi], bl
add rsi, 1 ;incrementa índice
add rdi, 1 ;incrementa tamanho
neg eax ;toma módulo do número
Label0:
mov ebx, 10 ;divisor
Label1:
add rcx, 1 ;incrementa contador
cdq ;estende edx:eax p/ div.
idiv ebx ;divide edx;eax por ebx
push dx ;empilha valor do resto
cmp eax, 0 ;verifica se quoc. é 0
jne Label1;se não é 0, continua
add rdi,rcx ;atualiza tam. string
;agora, desemp. os valores e escreve o string
Label2:
pop dx ;desempilha valor
add dl, '0' ;transforma em caractere
mov [rsi], dl ;escreve caractere
add rsi, 1 ;incrementa base
sub rcx, 1 ;decrementa contador
cmp rcx, 0 ;verifica pilha vazia
jne Label2;se não pilha vazia, loop
; Interrupção de saida
mov rsi, M+0;ou buffer.end
mov rdx, rdi ;ou buffer.tam
mov rax, 1 ;chamada para saída
mov rdi, 1 ;saída para tela
syscall
section .data
db 10
section .text
mov rsi, M+65540
mov rdx, 1 ;1 byte apenas
mov rax, 1 ;chamada para saida
mov rdi, 1 ;saida para tela
syscall; chamada do sistema
mov rsi, M+4
mov rdx, 0Ch ;tamanho do buffer
mov rax, 0 ;chamada para leitura
mov rdi, 0 ;leitura do teclado
syscall
mov eax, 0 ;acumulador
mov ebx, 0 ;caractere
mov ecx, 10 ;base 10
mov dx, 1 ;sinal
mov rsi, M+4;end. buffer
mov bl, [rsi] ;carrega caractere
cmp bl, '-' ;sinal - ?
jne Label3
mov dx, -1 ;senão, armazena -
add rsi, 1 ;inc. ponteiro string
mov bl, [rsi] ;carrega caractere
Label3:
push dx ;empilha sinal
mov edx, 0 ;reg. multiplicação
Label4:
cmp bl, 0Ah ;verifica fim string
je Label5; 
imul ecx ;mult. eax por 10
sub bl, '0' ;converte caractere
add eax, ebx ;soma valor caractere
add rsi, 1 ;incrementa base
mov bl, [rsi] ;carrega caractere
jmp Label4
Label5:
pop cx ;desempilha sinal
cmp cx, 0
jg Label6
neg eax ;mult. sinal
Label6:
mov [M+65536], eax
section .data
dd 5;65541
section .text
mov eax, [M+65536]
mov ebx, [M+65541]
add eax, ebx
mov [M+16], eax
mov eax, [M+16]
mov rsi, M+20
mov rcx, 0 ;contador pilha
mov rdi, 0 ;tam. string convertido
cmp eax, 0 ;verifica sinal
jge Label7;salta se número positivo
mov bl, '-' ;senão, escreve sinal –
mov [rsi], bl
add rsi, 1 ;incrementa índice
add rdi, 1 ;incrementa tamanho
neg eax ;toma módulo do número
Label7:
mov ebx, 10 ;divisor
Label8:
add rcx, 1 ;incrementa contador
cdq ;estende edx:eax p/ div.
idiv ebx ;divide edx;eax por ebx
push dx ;empilha valor do resto
cmp eax, 0 ;verifica se quoc. é 0
jne Label8;se não é 0, continua
add rdi,rcx ;atualiza tam. string
;agora, desemp. os valores e escreve o string
Label9:
pop dx ;desempilha valor
add dl, '0' ;transforma em caractere
mov [rsi], dl ;escreve caractere
add rsi, 1 ;incrementa base
sub rcx, 1 ;decrementa contador
cmp rcx, 0 ;verifica pilha vazia
jne Label9;se não pilha vazia, loop
; Interrupção de saida
mov rsi, M+20;ou buffer.end
mov rdx, rdi ;ou buffer.tam
mov rax, 1 ;chamada para saída
mov rdi, 1 ;saída para tela
syscall
section .data
db 10
section .text
mov rsi, M+65545
mov rdx, 1 ;1 byte apenas
mov rax, 1 ;chamada para saida
mov rdi, 1 ;saida para tela
syscall; chamada do sistema
mov rax, 60
mov rdi, 0
syscall

