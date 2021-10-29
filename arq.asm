section .data
M:
resb 10000h; Reserva para Temporarios
section .text
global _start
_start:
section .data
db 10
section .text
mov rsi, M+65536
mov rdx, 1 ;1 byte apenas
mov rax, 1 ;chamada para saida
mov rdi, 1 ;saida para tela
syscall; chamada do sistema
section .data
resd 1
section .text
section .data
db 10
section .text
mov rsi, M+65541
mov rdx, 1 ;1 byte apenas
mov rax, 1 ;chamada para saida
mov rdi, 1 ;saida para tela
syscall; chamada do sistema
section .data
dd 10.555
section .text
section .data
db 10
section .text
mov rsi, M+65546
mov rdx, 1 ;1 byte apenas
mov rax, 1 ;chamada para saida
mov rdi, 1 ;saida para tela
syscall; chamada do sistema
section .data
resd 1
section .text
section .data
resb 1
section .text
section .data
db "Gabriel", 0
resb 248
section .text
section .data
db 10
section .text
mov rsi, M+65808
mov rdx, 1 ;1 byte apenas
mov rax, 1 ;chamada para saida
mov rdi, 1 ;saida para tela
syscall; chamada do sistema
section .data
db 10
section .text
mov rsi, M+65809
mov rdx, 1 ;1 byte apenas
mov rax, 1 ;chamada para saida
mov rdi, 1 ;saida para tela
syscall; chamada do sistema
section .data
db 10
section .text
mov rsi, M+65810
mov rdx, 1 ;1 byte apenas
mov rax, 1 ;chamada para saida
mov rdi, 1 ;saida para tela
syscall; chamada do sistema
section .data
db 10
section .text
mov rsi, M+65811
mov rdx, 1 ;1 byte apenas
mov rax, 1 ;chamada para saida
mov rdi, 1 ;saida para tela
syscall; chamada do sistema
mov rax, 60
mov rdi, 0
syscall

