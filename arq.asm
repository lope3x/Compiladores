section .data
M:
resb 10000h; Reserva para Temporarios
section .text
global _start
_start:
section .data
db "Palavra muito grande sdfjoghdsiofhdsioh", 0
resb 216
section .text
section .data
db 0x61
section .text
mov rsi, M+65792
mov rdx, 1 ;buffer end
mov rax, 1 ;chamada para saída
mov rdi, 1 ;saída para tela
syscall; chamada do sistema
section .data
db 10
section .text
mov rsi, M+65793
mov rdx, 1 ;1 byte apenas
mov rax, 1 ;chamada para saida
mov rdi, 1 ;saida para tela
syscall; chamada do sistema
mov rax, 60
mov rdi, 0
syscall

