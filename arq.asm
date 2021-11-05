section .data
M:
resb 10000h; Reserva para Temporarios
section .text
global _start
_start:
section .data
resd 1
section .text
section .data
db "Digite o valor de x a ser calculado", 0;65540
resb 220
section .text
mov rsi, M+65540
mov rdx, 255
mov rax, 1 ;chamada para saída
mov rdi, 1 ;saída para tela
syscall; chamada do sistema
section .data
db 10
section .text
mov rsi, M+65796
mov rdx, 1 ;1 byte apenas
mov rax, 1 ;chamada para saida
mov rdi, 1 ;saida para tela
syscall; chamada do sistema
mov rsi, M+0
mov rdx, 0Ch ;tamanho do buffer
mov rax, 0 ;chamada para leitura
mov rdi, 0 ;leitura do teclado
syscall
mov eax, 0 ;acumulador
mov ebx, 0 ;caractere
mov ecx, 10 ;base 10
mov dx, 1 ;sinal
mov rsi, M+0;end. buffer
mov bl, [rsi] ;carrega caractere
cmp bl, '-' ;sinal - ?
jne Label0
mov dx, -1 ;senão, armazena -
add rsi, 1 ;inc. ponteiro string
mov bl, [rsi] ;carrega caractere
Label0:
push dx ;empilha sinal
mov edx, 0 ;reg. multiplicação
Label1:
cmp bl, 0Ah ;verifica fim string
je Label2; 
imul ecx ;mult. eax por 10
sub bl, '0' ;converte caractere
add eax, ebx ;soma valor caractere
add rsi, 1 ;incrementa base
mov bl, [rsi] ;carrega caractere
jmp Label1
Label2:
pop cx ;desempilha sinal
cmp cx, 0
jg Label3
neg eax ;mult. sinal
Label3:
mov [M+65536], eax
section .data
dd 1 ;65797
section .text
section .data
dd 1 ;65801
section .text
Label4:
mov eax, [M+65801]
mov ebx, [M+65536]
cmp eax, ebx
jle Label6
Label7:
mov eax, 0
mov [M+12], eax
jmp Label8
Label6:
mov eax, 1
mov [M+12], eax
Label8:
mov eax, [M+12]
cmp eax, 1
jne Label5
mov eax, [M+65797]
mov ebx, [M+65801]
imul ebx
cdqe
mov [M+16], rax
mov eax, [M+16]
mov [M+65797], eax
section .data
dd 1;65805
section .text
mov eax, [M+65801]
mov ebx, [M+65805]
add eax, ebx
mov [M+20], eax
mov eax, [M+20]
mov [M+65801], eax
jmp Label4
Label5:
section .data
db "O valor do fatorial eh: ", 0;65809
resb 231
section .text
mov rsi, M+65809
mov rdx, 255
mov rax, 1 ;chamada para saída
mov rdi, 1 ;saída para tela
syscall; chamada do sistema
mov eax, [M+65797]
mov rsi, M+24
mov rcx, 0 ;contador pilha
mov rdi, 0 ;tam. string convertido
cmp eax, 0 ;verifica sinal
jge Label9;salta se número positivo
mov bl, '-' ;senão, escreve sinal –
mov [rsi], bl
add rsi, 1 ;incrementa índice
add rdi, 1 ;incrementa tamanho
neg eax ;toma módulo do número
Label9:
mov ebx, 10 ;divisor
Label10:
add rcx, 1 ;incrementa contador
cdq ;estende edx:eax p/ div.
idiv ebx ;divide edx;eax por ebx
push dx ;empilha valor do resto
cmp eax, 0 ;verifica se quoc. é 0
jne Label10;se não é 0, continua
add rdi,rcx ;atualiza tam. string
;agora, desemp. os valores e escreve o string
Label11:
pop dx ;desempilha valor
add dl, '0' ;transforma em caractere
mov [rsi], dl ;escreve caractere
add rsi, 1 ;incrementa base
sub rcx, 1 ;decrementa contador
cmp rcx, 0 ;verifica pilha vazia
jne Label11;se não pilha vazia, loop
; Interrupção de saida
mov rsi, M+24;ou buffer.end
mov rdx, rdi ;ou buffer.tam
mov rax, 1 ;chamada para saída
mov rdi, 1 ;saída para tela
syscall
section .data
db 10
section .text
mov rsi, M+66065
mov rdx, 1 ;1 byte apenas
mov rax, 1 ;chamada para saida
mov rdi, 1 ;saida para tela
syscall; chamada do sistema
mov rax, 60
mov rdi, 0
syscall

