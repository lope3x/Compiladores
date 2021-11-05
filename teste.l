int n;

writeln("Digite o valor de x a ser calculado");

readln(n);

int fat <- 1;

int i <-1;

while i<=n {
    fat<- fat *i;
    i <- i+1;
}

writeln("O valor do fatorial eh: ", fat);