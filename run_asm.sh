rm arq.o
rm arq
nasm arq.asm -g -w-zeroing -w-other -f elf64 -o arq.o
ld arq.o -o arq
./arq