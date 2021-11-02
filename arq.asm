section .data
M:
resb 10000h; Reserva para Temporarios
section .text
global _start
_start:
section .data
dd 3;65536
section .text
section .data
dd 3;65540
section .text
mov eax, [M+65536]
mov ebx, [M+65540]
cmp eax, ebx
je Label0
Label1:
mov eax, 0
mov [M+0], eax
jmp Label2
Label0:
mov eax, 1
mov [M+0], eax
Label2:
mov eax, [M+0]
neg eax
add eax, 1
mov[M+4], eax
mov eax, [M+4]
mov rsi, M+8
mov rcx, 0 ;contador pilha
mov rdi, 0 ;tam. string convertido
cmp eax, 0 ;verifica sinal
jge Label3;salta se número positivo
mov bl, '-' ;senão, escreve sinal –
mov [rsi], bl
add rsi, 1 ;incrementa índice
add rdi, 1 ;incrementa tamanho
neg eax ;toma módulo do número
Label3:
mov ebx, 10 ;divisor
Label4:
add rcx, 1 ;incrementa contador
cdq ;estende edx:eax p/ div.
idiv ebx ;divide edx;eax por ebx
push dx ;empilha valor do resto
cmp eax, 0 ;verifica se quoc. é 0
jne Label4;se não é 0, continua
add rdi,rcx ;atualiza tam. string
;agora, desemp. os valores e escreve o string
Label5:
pop dx ;desempilha valor
add dl, '0' ;transforma em caractere
mov [rsi], dl ;escreve caractere
add rsi, 1 ;incrementa base
sub rcx, 1 ;decrementa contador
cmp rcx, 0 ;verifica pilha vazia
jne Label5;se não pilha vazia, loop
; Interrupção de saida
mov rsi, M+8;ou buffer.end
mov rdx, rdi ;ou buffer.tam
mov rax, 1 ;chamada para saída
mov rdi, 1 ;saída para tela
syscall
section .data
db 10
section .text
mov rsi, M+65544
mov rdx, 1 ;1 byte apenas
mov rax, 1 ;chamada para saida
mov rdi, 1 ;saida para tela
syscall; chamada do sistema
mov rax, 60
mov rdi, 0
syscall

