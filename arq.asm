section .data
M:
resb 10000h; Reserva para Temporarios
section .text
global _start
_start:
section .data
dd 5
section .text
section .data
dd 2.0
section .text
mov eax, [M+65536]
cdqe
cvtsi2ss xmm0,rax
movss xmm1, [M+65540]
divss xmm0, xmm1
movss [M+0], xmm0
movss xmm0, [qword M+0];real a ser impresso
mov rsi, M+4;end. temporário
mov rcx, 0 ;contador pilha
mov rdi, 6 ;precisao 6 casas compart
mov rbx, 10 ;divisor
cvtsi2ss xmm2, rbx ;divisor real
subss xmm1, xmm1 ;zera registrador
comiss xmm0, xmm1 ;verifica sinal
jae Label0;salta se número positivo
mov dl, '-' ;senão, escreve sinal –
mov [rsi], dl
mov rdx, -1 ;Carrega -1 em RDX
cvtsi2ss xmm1, rdx ;Converte para real
mulss xmm0, xmm1 ;Toma módulo
add rsi, 1 ;incrementa índice
Label0:
roundss xmm1, xmm0, 0b0011 ;parte inteira xmm1
subss xmm0, xmm1 ;parte frac xmm0
cvtss2si rax, xmm1 ;convertido para int
;converte parte inteira que está em rax
Label1:
add rcx, 1 ;incrementa contador
cdq ;estende edx:eax p/ div.
idiv ebx ;divide edx;eax por ebx
push dx ;empilha valor do resto
cmp eax, 0 ;verifica se quoc. é 0
jne Label1;se não é 0, continua
sub rdi, rcx ;decrementa precisao
;agora, desemp valores e escreve parte int
Label2:
pop dx ;desempilha valor
add dl, '0' ;transforma em caractere
mov [rsi], dl ;escreve caractere
add rsi, 1 ;incrementa base
sub rcx, 1 ;decrementa contador
cmp rcx, 0 ;verifica pilha vazia
jne Label2;se não pilha vazia, loop
mov dl, '.' ;escreve ponto decimal
mov [rsi], dl
add rsi, 1 ;incrementa base
;converte parte fracionaria que está em xmm0
Label3:
cmp rdi, 0 ;verifica precisao
jle Label4;terminou precisao ?
mulss xmm0,xmm2 ;desloca para esquerda
roundss xmm1,xmm0,0b0011 ;parte inteira xmm1
subss xmm0,xmm1 ;atualiza xmm0
cvtss2si rdx, xmm1 ;convertido para int
add dl, '0' ;transforma em caractere
mov [rsi], dl ;escreve caractere
add rsi, 1 ;incrementa base
sub rdi, 1 ;decrementa precisao
jmp +Label3
; impressão
Label4:
mov dl, 0 ;fim string, opcional
mov [rsi], dl ;escreve caractere
mov rdx, rsi ;calc tam str convertido
mov rbx, M+4
sub rdx, rbx ;tam=rsi-M-buffer.end
mov rsi, M+4; endereço do buffer

; Interrupção de saida
mov rax, 1 ;chamada para saída
mov rdi, 1 ;saída para tela
syscall
mov rax, 60
mov rdi, 0
syscall

