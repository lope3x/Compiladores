section .data
M:
resb 10000h; Reserva para Temporarios
section .text
global _start
_start:
section .data
dd -5
section .text
mov rax, 60
mov rdi, 0
syscall

