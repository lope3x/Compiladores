/**
 *  TP01, T02, T03 e T04 - Compiladores - 2021/2
 *  G07 - Bruno Duarte de Paula Assis (639985), Gabriel Lopes Ferreira(619148), Giovanni Carlos Guaceroni(636206)
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 *  Enum responsável por representar os tokens da linguagem
 */
enum Token {
    INT("int"),
    CHAR("char"),
    WHILE("while"),
    STRING("string"),
    IF("if"),
    FLOAT("float"),
    ELSE("else"),
    AND("&&"),
    OR("||"),
    NEGATION("!"),
    ATTRIBUTION("<-"),
    EQUAL("="),
    OPEN_PARENTESIS("("),
    CLOSE_PARENTESIS(")"),
    LESSER("<"),
    GREATER(">"),
    NOT_EQUAL("!="),
    GREATER_OR_EQUAL_THAN(">="),
    LESSER_OR_EQUAL_THAN("<="),
    COMMA(","),
    PLUS("+"),
    MINUS("-"),
    MULTIPLICATION("*"),
    DIVISION("/"),
    SEMICOLON(";"),
    LEFT_BRACE("{"),
    RIGHT_BRACE("}"),
    READ_LINE("readln"),
    DIV("div"),
    WRITE("write"),
    WRITE_LINE("writeln"),
    MOD("mod"),
    LEFT_SQUARE_BRACKET("["),
    RIGHT_SQUARE_BRACKET("]"),
    CONST("const"),
    EOF(null),
    CONST_VALUE(null),
    ID(null);

    public final String name;

    Token(String name) {
        this.name = name;
    }
}

/**
 *  Enum responsável por representar os tipos de constantes possíveis na linguagem.
 */
enum ConstType {
    INT,
    HEX,
    CHAR,
    FLOAT,
    STRING;

    public Type toType(){
        switch (this) {
            case INT:
                return Type.INTEGER;
            case HEX:
            case CHAR:
                return Type.CHARACTER;
            case FLOAT:
                return Type.REAL;
            default:
                return Type.STRING;
        }
    }
}

enum Class {
    VAR,
    CONST
}

enum Type {
    INTEGER,
    REAL,
    CHARACTER,
    STRING,
    BOOLEAN
}

/**
 * Classe pra armazenar valores que são constantes ao longo do programa.
 */
class Constants {
    public static int DecimalPrecision = 6;
    public static int idMaxLength = 32;
    public static int IntBytesSize = 4;
    public static int CharBytesSize = 1;
    public static int FloatBytesSize = 4;
    public static int HexBytesSize = 1;
}

/**
 *  Classe que utilizamos para representar os erros encontrados durante a compilação.
 */
class CompilerError extends Throwable {
    private final int line;
    private final String message;

    public CompilerError(String message, int line) {
        this.line = line;
        this.message = message;
    }

    public void print() {
        System.out.print(line+"\n"+message+"\n");
    }
}

/**
 * Classe para representar nossa tabela de símbolos que iramos usar durante a leitura do arquivo fonte.
 */
class SymbolTable {
    private static SymbolTable instance;
    final int size = 1000;
    ArrayList<ArrayList<Symbol>> table;

    /**
     * Construtor da classe
     */
    private SymbolTable() {
        table = new ArrayList<>(size);

        for(int i = 0; i<size;i++){
            table.add(null);
        }

        startTable();
    }

    /**
     * Função para pegar ou gerar a instância da tabela de símbolos.
     */
    public static SymbolTable getInstance() {
        if(instance == null) {
            instance = new SymbolTable();
        }
        return instance;
    }

    /**
     * Função para inicializar nossa tabela de símbolos com todos os tokens já conhecidos.
     */
    public void startTable() {
        Token[] tokensArray = Token.values();

        for(Token token : tokensArray) {
            if(token.name != null) {
                Symbol symbolToBeAdded = new Symbol(token, token.name);
                insert(symbolToBeAdded);
            }
        }
    }

    /**
     * Função feita para inserir o símbolos na tabela, e caso já esteja existente concatenar com o já inserido.
     */
    public SymbolTableSearchResult insert(Symbol symbol) {
        int positionInHash= hash(symbol.lexeme);

        if (table.get(positionInHash) == null) {
            table.set(positionInHash, new ArrayList<>());
        }

        int addedIndex = table.get(positionInHash).size();
        table.get(positionInHash).add(symbol);

        return new SymbolTableSearchResult(positionInHash, addedIndex, symbol);
    }

    /**
     * Função hash(que utiliza o valor do caractere na tabela ASCII), para inserir na nossa tabela de símbolos.
     */
    public int hash(String lexeme) {
        int n = 0;
        for (int i = 0; i < lexeme.length(); i++) {
            n += lexeme.charAt(i);
        }
        return n % size;
    }

    /**
     * Função para buscar na tabela de símbolos.
     */
    public SymbolTableSearchResult search(String lexeme) {
        int positionInHash = hash(lexeme);
        ArrayList<Symbol> lexemeList = table.get(positionInHash);
        if(lexemeList != null) {
            for(int i=0;i<lexemeList.size(); i++) {
                Symbol symbol = lexemeList.get(i);
                if(symbol.lexeme.equals(lexeme)) {
                    return new SymbolTableSearchResult(positionInHash, i, symbol);
                }
            }
        }
        return null;
    }

    /**
     * Função para printar a tabela.
     */
    public void printTable() {
        for (ArrayList<Symbol> symbols: table) {
            if(symbols != null)
                System.out.println(symbols);
        }
    }
}

/**
 * Classe que representa o símbolo encontrado, e sua posição na tabela
 * hash, e caso esteja numa lista de lexemas, sua posição.
 */
class SymbolTableSearchResult {
    int positionInHash;
    int positionInArrayList;
    Symbol symbol;

    /**
     * Construtor da classe.
     */
    SymbolTableSearchResult(int positionInHash, int positionInArrayList, Symbol symbol) {
        this.positionInHash = positionInHash;
        this.positionInArrayList = positionInArrayList;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "SymbolTableSearchResult{" +
                "positionInHash=" + positionInHash +
                ", positionInArrayList=" + positionInArrayList +
                ", symbol=" + symbol +
                '}';
    }

    public void print() {
        System.out.println(this);
    }
}

/**
 * Classe que representa um símbolo, com as propriedades tipo do token e seu respectivo lexema.
 */

class Symbol {
    Token tokenType;
    String lexeme;
    Class idClass; // Somente tokens ID terão esse valor setado
    Type idType; // Somente tokens ID terão esse valor setado
    long address;
    int size;

    /**
     * Construtor da classe.
     */
    Symbol(Token token, String lexeme) {
        this.tokenType = token;
        this.lexeme = lexeme;
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "tokenType=" + tokenType +
                ", lexeme='" + lexeme + '\'' +
                ", idClass=" + idClass +
                ", idType=" + idType +
                ", address=" + address +
                ", size=" + size +
                '}';
    }

    public void print() {
        System.out.println(this);
    }
}

/**
 * Classe em que implementamos a nossa geração de código.
 */
class CodeGenerator {
    String generatedCode = "";
    long dataCount = 65536; //Primeira posição de memória válida, após os temporários
    private int temporaryCount = 0; //Contador para os temporários
    private int labelCount = 0; //Contador de rótulos do programa

    /**
     * Inicio da geração de código, reservando 10000h(endereços na memória) para os temporários.
     */
    CodeGenerator() {
        generatedCode+="section .data\n" +
                "M:\n" +
                "resb 10000h; Reserva para Temporarios\n" +
                "section .text\n" +
                "global _start\n" +
                "_start:\n";
    }

    //Método para gerar o arquivo .asm
    public void printCode() {
        generatedCode+=generateEndOfCode();
        File output = new File("arq.asm");
//        System.out.println(generatedCode);
        try {
            if(!output.exists())
                output.createNewFile();
            PrintWriter out = new PrintWriter(output);
            out.println(generatedCode);
            out.close();
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }


    }

    /**
     * Método para a geração da parte final do código assembly
     */
    private String generateEndOfCode() {
        return "mov rax, 60\n" +
                "mov rdi, 0\n" +
                "syscall\n";
    }

    /**
     * Método para gerar um novo rótulo
     */
    public String getNewLabel() {
        String newLabel = "Label"+labelCount;
        labelCount++;
        return newLabel;
    }

    /**
     * Método para obter o endereço de um novo temporário
     */
    public long getNewTemporaryAddress(int sizeOfTemporary) {
        long address = temporaryCount;
        temporaryCount+=sizeOfTemporary;
        return address;
    }
    /**
     * Método para resetar o contador de temporários
     */
    public void resetTemporaryCount() {
        temporaryCount = 0;
    }

    /**
     * Método responsável pela geração de código 3
     * @param expressionReturn ExpressionReturn da expressão a ser impressa
     */
    public void codeGenerate3(ExpressionReturn expressionReturn) {
        switch (expressionReturn.type){
            case STRING:
                generatedCode+="mov rsi, M+"+expressionReturn.address +"\n" +
                        "mov rdx, "+expressionReturn.size +"\n" +
                        "mov rax, 1 ;chamada para saída\n" +
                        "mov rdi, 1 ;saída para tela\n" +
                        "syscall; chamada do sistema\n";
                break;
            case BOOLEAN:
            case INTEGER:
                long temporaryAddress = getNewTemporaryAddress(4);
                String label0 = getNewLabel();
                String label1 = getNewLabel();
                String label2 = getNewLabel();
                generatedCode+= "mov eax, [M+"+expressionReturn.address+"]\n" +
                        "mov rsi, M+"+temporaryAddress+"\n" +
                        "mov rcx, 0 ;contador pilha\n" +
                        "mov rdi, 0 ;tam. string convertido\n" +
                        "cmp eax, 0 ;verifica sinal\n" +
                        "jge "+label0+";salta se número positivo\n" +
                        "mov bl, '-' ;senão, escreve sinal –\n" +
                        "mov [rsi], bl\n" +
                        "add rsi, 1 ;incrementa índice\n" +
                        "add rdi, 1 ;incrementa tamanho\n" +
                        "neg eax ;toma módulo do número\n" +
                        label0+":\n" +
                        "mov ebx, 10 ;divisor\n" +
                        label1+":\n" +
                        "add rcx, 1 ;incrementa contador\n" +
                        "cdq ;estende edx:eax p/ div.\n" +
                        "idiv ebx ;divide edx;eax por ebx\n" +
                        "push dx ;empilha valor do resto\n" +
                        "cmp eax, 0 ;verifica se quoc. é 0\n" +
                        "jne "+label1+";se não é 0, continua\n" +
                        "add rdi,rcx ;atualiza tam. string\n" +
                        ";agora, desemp. os valores e escreve o string\n" +
                        label2+":\n" +
                        "pop dx ;desempilha valor\n" +
                        "add dl, '0' ;transforma em caractere\n" +
                        "mov [rsi], dl ;escreve caractere\n" +
                        "add rsi, 1 ;incrementa base\n" +
                        "sub rcx, 1 ;decrementa contador\n" +
                        "cmp rcx, 0 ;verifica pilha vazia\n" +
                        "jne "+label2+";se não pilha vazia, loop\n" +
                        "; Interrupção de saida\n" +
                        "mov rsi, M+"+temporaryAddress+";ou buffer.end\n" +
                        "mov rdx, rdi ;ou buffer.tam\n" +
                        "mov rax, 1 ;chamada para saída\n" +
                        "mov rdi, 1 ;saída para tela\n" +
                        "syscall\n";
                break;
            case REAL:
                temporaryAddress = getNewTemporaryAddress(4);
                label0 = getNewLabel();
                label1 = getNewLabel();
                label2 = getNewLabel();
                String label3 = getNewLabel();
                String label4 = getNewLabel();
                generatedCode+="movss xmm0, [qword M+"+expressionReturn.address+"];real a ser impresso\n" +
                        "mov rsi, M+"+temporaryAddress+";end. temporário\n" +
                        "mov rcx, 0 ;contador pilha\n" +
                        "mov rdi, 6 ;precisao 6 casas compart\n" +
                        "mov rbx, 10 ;divisor\n" +
                        "cvtsi2ss xmm2, rbx ;divisor real\n" +
                        "subss xmm1, xmm1 ;zera registrador\n" +
                        "comiss xmm0, xmm1 ;verifica sinal\n" +
                        "jae "+label0+";salta se número positivo\n" +
                        "mov dl, '-' ;senão, escreve sinal –\n" +
                        "mov [rsi], dl\n" +
                        "mov rdx, -1 ;Carrega -1 em RDX\n" +
                        "cvtsi2ss xmm1, rdx ;Converte para real\n" +
                        "mulss xmm0, xmm1 ;Toma módulo\n" +
                        "add rsi, 1 ;incrementa índice\n" +
                        label0+":\n" +
                        "roundss xmm1, xmm0, 0b0011 ;parte inteira xmm1\n" +
                        "subss xmm0, xmm1 ;parte frac xmm0\n" +
                        "cvtss2si rax, xmm1 ;convertido para int\n" +
                        ";converte parte inteira que está em rax\n" +
                        label1+":\n" +
                        "add rcx, 1 ;incrementa contador\n" +
                        "cdq ;estende edx:eax p/ div.\n" +
                        "idiv ebx ;divide edx;eax por ebx\n" +
                        "push dx ;empilha valor do resto\n" +
                        "cmp eax, 0 ;verifica se quoc. é 0\n" +
                        "jne "+label1+";se não é 0, continua\n" +
                        "sub rdi, rcx ;decrementa precisao\n" +
                        ";agora, desemp valores e escreve parte int\n" +
                        label2+":\n" +
                        "pop dx ;desempilha valor\n" +
                        "add dl, '0' ;transforma em caractere\n" +
                        "mov [rsi], dl ;escreve caractere\n" +
                        "add rsi, 1 ;incrementa base\n" +
                        "sub rcx, 1 ;decrementa contador\n" +
                        "cmp rcx, 0 ;verifica pilha vazia\n" +
                        "jne "+label2 +";se não pilha vazia, loop\n" +
                        "mov dl, '.' ;escreve ponto decimal\n" +
                        "mov [rsi], dl\n" +
                        "add rsi, 1 ;incrementa base\n" +
                        ";converte parte fracionaria que está em xmm0\n" +
                        label3+":\n" +
                        "cmp rdi, 0 ;verifica precisao\n" +
                        "jle "+label4+";terminou precisao ?\n" +
                        "mulss xmm0,xmm2 ;desloca para esquerda\n" +
                        "roundss xmm1,xmm0,0b0011 ;parte inteira xmm1\n" +
                        "subss xmm0,xmm1 ;atualiza xmm0\n" +
                        "cvtss2si rdx, xmm1 ;convertido para int\n" +
                        "add dl, '0' ;transforma em caractere\n" +
                        "mov [rsi], dl ;escreve caractere\n" +
                        "add rsi, 1 ;incrementa base\n" +
                        "sub rdi, 1 ;decrementa precisao\n" +
                        "jmp +"+label3+"\n" +
                        "; impressão\n" +
                        label4+":\n" +
                        "mov dl, 0 ;fim string, opcional\n" +
                        "mov [rsi], dl ;escreve caractere\n" +
                        "mov rdx, rsi ;calc tam str convertido\n" +
                        "mov rbx, M+"+temporaryAddress+"\n" +
                        "sub rdx, rbx ;tam=rsi-M-buffer.end\n" +
                        "mov rsi, M+"+temporaryAddress+"; endereço do buffer\n" +
                        "\n" +
                        "; Interrupção de saida\n" +
                        "mov rax, 1 ;chamada para saída\n" +
                        "mov rdi, 1 ;saída para tela\n" +
                        "syscall\n";
                break;
            case CHARACTER:
                generatedCode+="mov rsi, M+"+expressionReturn.address+"\n" +
                        "mov rdx, 1 ;buffer end\n" +
                        "mov rax, 1 ;chamada para saída\n" +
                        "mov rdi, 1 ;saída para tela\n" +
                        "syscall; chamada do sistema\n";
                break;
        }
    }

    /**
     * Método responsável pela geração de código 4
     * @param isWriteLn Se é um writeln, imprime o \n (byte 10)
     */
    public void codeGenerate4(boolean isWriteLn) {
        if (isWriteLn) {
            generatedCode += "section .data\n" +
                    "db 10\n" +
                    "section .text\n" +
                    "mov rsi, M+" +
                    dataCount + "\n"+
                    "mov rdx, 1 ;1 byte apenas\n" +
                    "mov rax, 1 ;chamada para saida\n" +
                    "mov rdi, 1 ;saida para tela\n" +
                    "syscall; chamada do sistema\n";
            dataCount += 1; //java
        }
    }

    /**
     * Método responsável pela geração de código 5
     * @param constvalue LexicalRegister da constante
     * @param id LexicalRegister do identificador
     * @param isNegative Se a constante é negativa ou não
     */
    public void codeGenerate5(LexicalRegister constvalue, LexicalRegister id, boolean isNegative) {
        Type constType = constvalue.constType.toType();

        switch (constType){
            case STRING:
                id.symbol.address = dataCount;
                id.symbol.size = 255;
                int unUsedStringSpace = 255 - constvalue.size;
                generatedCode+="section .data\n"+
                        "db "+constvalue.symbol.lexeme+", 0 ;"+dataCount+ "\n"+
                        "resb "+unUsedStringSpace+"\n"+
                        "section .text\n";
                dataCount+=256;
                break;
            case BOOLEAN:
            case INTEGER:
            case REAL:
                id.symbol.address = dataCount;
                id.symbol.size = 4;
                String lexeme= "";
                if(isNegative)
                    lexeme +="-"+constvalue.symbol.lexeme;
                else
                    lexeme = constvalue.symbol.lexeme;

                if(constType == Type.INTEGER && id.symbol.idType == Type.REAL) {
                    lexeme+=".0";
                }
                generatedCode+="section .data\n"+
                        "dd "+lexeme+" ;"+dataCount+ "\n"+
                        "section .text\n";
                dataCount+=4;
                break;
            case CHARACTER:
                id.symbol.address = dataCount;
                id.symbol.size = 1;
                generatedCode+="section .data\n"+
                        "db "+constvalue.symbol.lexeme+";"+dataCount+ "\n"+
                        "section .text\n";
                dataCount+=1;
                break;
        }
    }

    /**
     * Método responsável pela geração de código 6
     * @param id LexicalRegister do identificador
     */
    public void codeGenerate6(LexicalRegister id) {
        switch (id.symbol.idType){
            case STRING:
                id.symbol.address = dataCount;
                id.symbol.size = 255;
                generatedCode += "section .data\n"+
                        "resb 256\n"+
                        "section .text\n";
                dataCount+=256;
                break;
            case BOOLEAN:
            case INTEGER:
            case REAL:
                id.symbol.address = dataCount;
                id.symbol.size = 4;
                generatedCode += "section .data\n"+
                        "resd 1\n"+
                        "section .text\n";
                dataCount+=4;
                break;
            case CHARACTER:
                id.symbol.address = dataCount;
                id.symbol.size = 1;
                generatedCode += "section .data\n"+
                        "resb 1\n"+
                        "section .text\n";
                dataCount+=1;
                break;
        }
    }

    /**
     * Método responsável pela geração de código 7
     * @param constValue LexicalRegister da constante
     */
    public ExpressionReturn codeGenerate7(LexicalRegister constValue) {
        long expression6Address = 0;
        int expression6Size = 0;
        switch (constValue.constType.toType()){
            case STRING:
                expression6Address = dataCount;
                expression6Size = 255;
                int unUsedStringSpace = 255 - constValue.size;
                generatedCode+="section .data\n"+
                        "db "+constValue.symbol.lexeme+", 0;"+dataCount+"\n"+
                        "resb "+unUsedStringSpace+"\n"+
                        "section .text\n";
                dataCount+=256;
                break;
            case BOOLEAN:
            case INTEGER:
            case REAL:
                expression6Address = dataCount;
                expression6Size = 4;
                String lexeme= "";
                generatedCode+="section .data\n"+
                        "dd "+constValue.symbol.lexeme+";"+dataCount+"\n"+
                        "section .text\n";
                dataCount+=4;
                break;
            case CHARACTER:
                expression6Address = dataCount;
                expression6Size = 1;
                generatedCode+="section .data\n"+
                        "db "+constValue.symbol.lexeme+";"+dataCount+"\n"+
                        "section .text\n";
                dataCount+=1;
                break;
        }
        return new ExpressionReturn(constValue.constType.toType(), expression6Address, expression6Size);
    }

    /**
     * Método responsável pela geração de código 8
     * @param hasStringAccess Se tem o acesso a posição de uma string ou não
     * @param expressionReturn ExpressionReturn da expressão
     * @param id LexicalRegister do identificador
     */
    public ExpressionReturn codeGenerate8(boolean hasStringAccess, ExpressionReturn expressionReturn, LexicalRegister id) {
        if(hasStringAccess) {
            long newTemporary = getNewTemporaryAddress(1);
            generatedCode+="mov rax, M+"+id.symbol.address+";endereço base da string\n" +
                    "mov rbx, [M+"+expressionReturn.address+"];pega resultado da expressão e joga em rbx\n" +
                    "add rax, rbx ; soma o endereço base ao indice, para pegar o endereço do char\n" +
                    "mov al, [rax] ;move o char pra al\n" +
                    "mov [M+"+newTemporary+"], al;move o char para o temporario\n";
            return new ExpressionReturn(Type.CHARACTER, newTemporary, 1);
        }
        else {
            return new ExpressionReturn(id.symbol.idType, id.symbol.address, id.symbol.size);
        }
    }

    /**
     * Método responsável pela geração de código 12
     * @param expression4Type Type da expression4
     * @param expressionReturn ExpressionReturn da expressão a ser convertida
     */
    public ExpressionReturn codeGenerate12(Type expression4Type, ExpressionReturn expressionReturn) {
        if(expression4Type == Type.INTEGER){
            if(expressionReturn.type == Type.INTEGER) {
                return expressionReturn;
            }
            else {
                long newTemporaryAddress = getNewTemporaryAddress(4);
                generatedCode+="movss xmm0, [M+"+expressionReturn.address+"]\n" +
                        "cvtss2si rax,xmm0\n" +
                        "mov [M+"+newTemporaryAddress+"], rax\n";
                return new ExpressionReturn(Type.INTEGER, newTemporaryAddress, 4);
            }
        }
        else {
            if(expressionReturn.type == Type.INTEGER) {
                long newTemporaryAddress = getNewTemporaryAddress(4);
                generatedCode+="mov eax, [M+"+expressionReturn.address+"]\n" +
                        "cdqe\n"+
                        "cvtsi2ss xmm0,rax\n" +
                        "movss [M+"+newTemporaryAddress+"], xmm0\n";
                return new ExpressionReturn(Type.REAL, newTemporaryAddress, 4);
            }
            else {
                return expressionReturn;
            }
        }
    }

    /**
     * Método responsável pela geração de código 13
     * @param shouldNegateExpression Se a expressão deve ser negativada ou não
     * @param expression4Return ExpressionReturn da expressão 4
     */
    public ExpressionReturn codeGenerate13(boolean shouldNegateExpression, ExpressionReturn expression4Return){
        if(shouldNegateExpression) {
            long newTemporary = getNewTemporaryAddress(expression4Return.size);
            generatedCode+="mov eax, [M+"+expression4Return.address+"]\n" +
                    "neg eax\n" +
                    "add eax, 1\n" +
                    "mov[M+"+newTemporary+"], eax\n";
            return new ExpressionReturn(expression4Return.type, newTemporary, expression4Return.size);
        }
        else {
            return expression4Return;
        }
    }

    /**
     * Método responsável pela geração de código 15
     * @param expression2Data ExpressionReturn da expressão 2
     * @param expression3_2Return ExpressionReturn da expressão a ser avaliada
     * @param operator Token para saber qual operação será realizada
     */
    public ExpressionReturn codeGenerate15(ExpressionReturn expression2Data, ExpressionReturn expression3_2Return, Token operator) {
        long newTemporary = getNewTemporaryAddress(4);
        Type expressionType = expression2Data.type;
        switch (operator){
            case MULTIPLICATION:
                if(expression2Data.type == Type.REAL) {
                    if(expression3_2Return.type == Type.REAL){
                        generatedCode+="movss xmm0, [M+"+expression2Data.address+"]\n" +
                                "movss xmm1, [M+"+expression3_2Return.address+"]\n" +
                                "mulss xmm0, xmm1\n" +
                                "movss [M+"+newTemporary+"], xmm0\n";
                    }
                    else {
                        generatedCode+="mov eax, [M+"+expression3_2Return.address+"]\n" +
                                "cdqe\n" +
                                "cvtsi2ss xmm0,rax\n" +
                                "movss xmm1, [M+"+expression2Data.address+"]\n" +
                                "mulss xmm0, xmm1\n" +
                                "movss [M+"+newTemporary+"], xmm0\n";
                    }
                }
                else {
                    if(expression3_2Return.type == Type.REAL) {
                        expressionType = Type.REAL;
                        generatedCode += "mov eax, [M+"+expression2Data.address+"]\n" +
                                "cdqe\n" +
                                "cvtsi2ss xmm0,rax\n" +
                                "movss xmm1, [M+"+expression3_2Return.address+"]\n" +
                                "mulss xmm0, xmm1\n" +
                                "movss [M+"+newTemporary+"], xmm0\n";

                    }
                    else {
                        generatedCode += "mov eax, [M+"+expression2Data.address+"]\n" +
                                "mov ebx, [M+"+expression3_2Return.address+"]\n" +
                                "imul ebx\n" +
                                "cdqe\n" +
                                "mov [M+"+newTemporary+"], rax\n";
                    }
                }
                break;
            case AND:
                generatedCode+="mov eax, [M+"+expression2Data.address+"]\n" +
                        "mov ebx, [M+"+expression3_2Return.address+"]\n" +
                        "imul ebx\n" +
                        "mov [M+"+newTemporary+"], eax\n";
                break;
            case DIVISION:
                expressionType = Type.REAL;
                if(expression2Data.type == Type.REAL) {
                    if(expression3_2Return.type == Type.REAL){
                        generatedCode+="movss xmm0, [M+"+expression2Data.address+"]\n" +
                                "movss xmm1, [M+"+expression3_2Return.address+"]\n" +
                                "divss xmm0, xmm1\n" +
                                "movss [M+"+newTemporary+"], xmm0\n";
                    }
                    else {
                        generatedCode+="mov eax, [M+"+expression3_2Return.address+"]\n" +
                                "cdqe\n" +
                                "cvtsi2ss xmm0,rax\n" +
                                "movss xmm1, [M+"+expression2Data.address+"]\n" +
                                "divss xmm1, xmm0\n" +
                                "movss [M+"+newTemporary+"], xmm1\n";
                    }
                }
                else {
                    if(expression3_2Return.type == Type.REAL) {
                        generatedCode += "mov eax, [M+"+expression2Data.address+"]\n" +
                                "cdqe\n" +
                                "cvtsi2ss xmm0,rax\n" +
                                "movss xmm1, [M+"+expression3_2Return.address+"]\n" +
                                "divss xmm0, xmm1\n" +
                                "movss [M+"+newTemporary+"], xmm0\n";

                    }
                    else {
                        generatedCode += "mov eax, [M+"+expression2Data.address+"]\n" +
                                "cdqe\n" +
                                "cvtsi2ss xmm0,rax\n" +
                                "mov eax, [M+"+expression3_2Return.address+"]\n" +
                                "cdqe\n" +
                                "cvtsi2ss xmm1,rax\n" +
                                "divss xmm0, xmm1\n" +
                                "movss [M+"+newTemporary+"], xmm0\n";
                    }
                }
                break;
            case DIV:
                generatedCode+="mov eax, [M+"+expression2Data.address+"]\n" +
                        "mov ebx, [M+"+expression3_2Return.address+"]\n" +
                        "cdq\n"+
                        "idiv ebx\n"+
                        "mov [M+"+newTemporary+"], eax\n";
                break;
            case MOD:
                generatedCode+="mov eax, [M+"+expression2Data.address+"]\n" +
                        "mov ebx, [M+"+expression3_2Return.address+"]\n" +
                        "cdq\n"+
                        "idiv ebx\n"+
                        "mov [M+"+newTemporary+"], edx\n";
                break;
        }

        return new ExpressionReturn(expressionType, newTemporary, 4);
    }

    /**
     * Método responsável pela geração de código 16
     * @param isNegative Para saber se o número é negativo
     * @param expression2_1Return ExpressionReturn da expressão a ser avaliada
     */
    public ExpressionReturn codeGenerate16(boolean isNegative, ExpressionReturn expression2_1Return) {
        if(isNegative){
            long newTemporaryAddress = getNewTemporaryAddress(4);
            ExpressionReturn expressionReturn;
            if(expression2_1Return.type == Type.REAL) {
                generatedCode+="mov rax, -1\n" +
                        "cvtsi2ss xmm0, rax\n" +
                        "movss xmm1, [M+"+expression2_1Return.address+"]\n" +
                        "mulss xmm0, xmm1\n" +
                        "movss [M+"+newTemporaryAddress+"], xmm0\n";
                expressionReturn = new ExpressionReturn(Type.REAL, newTemporaryAddress, expression2_1Return.size);
            }
            else {
                generatedCode+="mov eax, [M+"+expression2_1Return.address+"]\n" +
                        "neg eax\n" +
                        "mov [M+"+newTemporaryAddress+"], eax\n";
                expressionReturn = new ExpressionReturn(Type.INTEGER, newTemporaryAddress, expression2_1Return.size);
            }
            return expressionReturn;
        }
        else {
            return expression2_1Return;
        }
    }

    /**
     * Método responsável pela geração de código 17
     * @param expression1Data ExpressionReturn da expressão 1
     * @param expression2_2Return ExpressionReturn da expressão a ser avaliada
     * @param operator Token para saber qual operação será realizada
     */
    public ExpressionReturn codeGenerate17(ExpressionReturn expression1Data,ExpressionReturn expression2_2Return,Token operator) {
        long newTemporary = getNewTemporaryAddress(4);
        Type expressionType = expression1Data.type;
        switch (operator) {
            case MINUS:
                if(expression1Data.type == Type.REAL){
                    if(expression2_2Return.type == Type.REAL) {
                        generatedCode += "movss xmm0, [M+"+expression1Data.address+"]\n" +
                                "movss xmm1, [M+"+expression2_2Return.address+"]\n" +
                                "subss xmm0, xmm1\n" +
                                "movss [M+"+newTemporary+"], xmm0\n";
                    }
                    else {
                        generatedCode += " mov eax, [M+"+expression2_2Return.address+"]\n" +
                                "cdqe\n" +
                                "cvtsi2ss xmm1,rax\n" +
                                "movss xmm0, [M+"+expression1Data.address+"]\n" +
                                "subss xmm0, xmm1\n" +
                                "movss [M+"+newTemporary+"], xmm0\n";
                    }
                }
                else {
                    if(expression2_2Return.type == Type.REAL){
                        expressionType = Type.REAL;
                        generatedCode += "mov eax, [M+"+expression1Data.address+"]\n" +
                                "cdqe\n" +
                                "cvtsi2ss xmm0,rax\n" +
                                "movss xmm1, [M+"+expression2_2Return.address+"]\n" +
                                "subss xmm0, xmm1\n" +
                                "movss [M+"+newTemporary+"], xmm0\n";
                    }
                    else {
                        generatedCode += "mov eax, [M+"+expression1Data.address+"]\n" +
                                "mov ebx, [M+"+expression2_2Return.address+"]\n" +
                                "sub eax, ebx\n" +
                                "mov [M+"+newTemporary+"], eax\n";
                    }
                }
                break;
            case PLUS:
                if(expression1Data.type == Type.REAL){
                    if(expression2_2Return.type == Type.REAL){
                        generatedCode+="movss xmm0, [M+"+expression1Data.address+"]\n" +
                                "movss xmm1, [M+"+expression2_2Return.address+"]\n" +
                                "addss xmm0, xmm1\n" +
                                "movss [M+"+newTemporary+"], xmm0\n";
                    }
                    else {
                        generatedCode+="mov eax, [M+"+expression2_2Return.address+"]\n" +
                                "cdqe\n" +
                                "cvtsi2ss xmm1,rax\n" +
                                "movss xmm0, [M+"+expression1Data.address+"]\n" +
                                "addss xmm0, xmm1\n" +
                                "movss [M+"+newTemporary+"], xmm0\n";
                    }
                }
                else {
                    if(expression2_2Return.type == Type.REAL){
                        expressionType = Type.REAL;
                        generatedCode+="mov eax, [M+"+expression1Data.address+"]\n" +
                                "cdqe\n" +
                                "cvtsi2ss xmm1,rax\n" +
                                "movss xmm0, [M+"+expression2_2Return.address+"]\n" +
                                "addss xmm0, xmm1\n" +
                                "movss [M+"+newTemporary+"], xmm0\n";
                    }
                    else {
                        generatedCode+="mov eax, [M+"+expression1Data.address+"]\n" +
                                "mov ebx, [M+"+expression2_2Return.address+"]\n" +
                                "add eax, ebx\n" +
                                "mov [M+"+newTemporary+"], eax\n";
                    }
                }
                break;
            case OR:
                expressionType = Type.BOOLEAN;
                generatedCode += "mov eax, [M+"+expression1Data.address+"]\n" +
                        "mov ebx, [M+"+expression2_2Return.address+"]\n" +
                        "add eax, ebx\n" +
                        "mov ecx, 2\n" +
                        "idiv ecx\n" +
                        "add eax, edx\n" +
                        "mov [M+"+newTemporary+"], eax\n";
                break;
        }

        return new ExpressionReturn(expressionType, newTemporary, 4);
    }

    /**
     * Método responsável pela geração de código 19
     * @param operator Token para saber qual operação será realizada
     * @param expressionData ExpressionReturn da expressão 1
     * @param expression1_2Return ExpressionReturn da expressão a ser avaliada
     */
    public ExpressionReturn codeGenerate19(Token operator, ExpressionReturn expressionData, ExpressionReturn expression1_2Return) {
        long newTemporary = getNewTemporaryAddress(4);
        if(expressionData.type == Type.STRING) {
            String labelLoop = getNewLabel();
            String labelAlis0 = getNewLabel();
            String labelBlis0 = getNewLabel();
            String endLabel = getNewLabel();
            String trueLabel = getNewLabel();
            String falseLabel = getNewLabel();
            generatedCode+="mov rsi, M+"+expressionData.address+"\n" +
                    "mov rdi, M+"+expression1_2Return.address+"\n" +
                    labelLoop+":;Label Loop\n"+
                    "mov al, [rsi]\n" +
                    "mov bl, [rdi]\n" +
                    "cmp al, 0\n" +
                    "je "+labelAlis0+"; Al eh 0\n" +
                    "cmp bl, 0\n" +
                    "je "+labelBlis0+"; Bl eh 0\n" +
                    "add rsi, 1\n" +
                    "add rdi, 1\n" +
                    "cmp al, bl\n" +
                    "je "+labelLoop+"; Label Loop\n" +
                    "jmp "+falseLabel+"; RotFalse\n" +
                    labelAlis0+": ; Al eh 0\n"+
                    "cmp bl, 0\n" +
                    "je "+trueLabel+"; Rot True\n"+
                    "jmp "+falseLabel+"; Rot false\n"+
                    labelBlis0+":; bl eh 0\n" +
                    "cmp al, 0\n" +
                    "je "+trueLabel+"; Rot True\n" +
                    "jmp "+falseLabel+"; Rot False\n" +
                    trueLabel+":; Rot True\n" +
                    "mov eax, 1\n" +
                    "mov [M+"+newTemporary+"], eax\n" +
                    "jmp "+endLabel+"; Rot Fim\n" +
                    falseLabel+":; Rot False\n" +
                    "mov eax, 0\n" +
                    "mov [M+"+newTemporary+"], eax\n" +
                    endLabel+": ; Rot fim\n";
        }
        else {
            String operation = "";
            String trueLabel = getNewLabel();
            String falseLabel = getNewLabel();
            String endLabel = getNewLabel();
            if(operator == Token.EQUAL){
                operation = "je "+trueLabel+"\n";
            }
            else if(operator == Token.NOT_EQUAL){
                operation = "jne "+trueLabel+"\n";
            }
            else if(operator == Token.LESSER) {
                if(expressionData.type == Type.REAL || expression1_2Return.type == Type.REAL) {
                    operation = "jb "+trueLabel+"\n";
                }
                else {
                    operation = "jl "+trueLabel+"\n";
                }
            }
            else if(operator == Token.GREATER) {
                if(expressionData.type == Type.REAL || expression1_2Return.type == Type.REAL) {
                    operation = "ja "+trueLabel+"\n";
                }
                else {
                    operation = "jg "+trueLabel+"\n";
                }
            }
            else if(operator == Token.LESSER_OR_EQUAL_THAN) {
                if(expressionData.type == Type.REAL || expression1_2Return.type == Type.REAL) {
                    operation = "jbe "+trueLabel+"\n";
                }
                else {
                    operation = "jle "+trueLabel+"\n";
                }
            }
            else {
                if(expressionData.type == Type.REAL || expression1_2Return.type == Type.REAL) {
                    operation = "jae "+trueLabel+"\n";
                }
                else {
                    operation = "jge "+trueLabel+"\n";
                }
            }

            if(expressionData.type == Type.REAL) {
                if(expression1_2Return.type == Type.REAL) {
                    generatedCode += "movss xmm0, [M+"+expressionData.address+"]\n" +
                            "movss xmm1, [M+"+expression1_2Return.address+"]\n" +
                            "comiss xmm0, xmm1\n" +
                            ""+operation+"\n" +
                            ""+falseLabel+":\n" +
                            "mov eax, 0\n" +
                            "mov [M+"+newTemporary+"], eax\n" +
                            "jmp "+endLabel+"\n" +
                            ""+trueLabel+":\n" +
                            "mov eax, 1\n" +
                            "mov [M+"+newTemporary+"], eax\n" +
                            ""+endLabel+":\n";
                }
                else {
                    generatedCode += "movss xmm0, [M+"+expressionData.address+"]\n" +
                            "mov eax, [M+"+expression1_2Return.address+"]\n" +
                            "cdqe\n" +
                            "cvtsi2ss xmm1, rax\n" +
                            "comiss xmm0, xmm1\n" +
                            ""+operation+"\n" +
                            ""+falseLabel+":\n" +
                            "mov eax, 0\n" +
                            "mov [M+"+newTemporary+"], eax\n" +
                            "jmp "+endLabel+"\n" +
                            ""+trueLabel+":\n" +
                            "mov eax, 1\n" +
                            "mov [M+"+newTemporary+"], eax\n" +
                            ""+endLabel+":\n";

                }
            }
            else {
                if(expression1_2Return.type == Type.REAL) {
                    generatedCode += "movss xmm0, [M+"+expression1_2Return.address+"]\n" +
                            "mov eax, [M+"+expressionData.address+"]\n" +
                            "cdqe\n" +
                            "cvtsi2ss xmm1, rax\n" +
                            "comiss xmm1, xmm0\n" +
                            operation +
                            falseLabel+":\n" +
                            "mov eax, 0\n" +
                            "mov [M+"+newTemporary+"], eax\n" +
                            "jmp "+endLabel+"\n" +
                            trueLabel+":\n" +
                            "mov eax, 1\n" +
                            "mov [M+"+newTemporary+"], eax\n" +
                            endLabel +":\n";
                }
                else {
                    generatedCode += "mov eax, [M+"+expressionData.address+"]\n" +
                            "mov ebx, [M+"+expression1_2Return.address+"]\n" +
                            "cmp eax, ebx\n" +
                            operation +
                            falseLabel+":\n" +
                            "mov eax, 0\n" +
                            "mov [M+"+newTemporary+"], eax\n" +
                            "jmp "+endLabel+"\n" +
                            trueLabel+":\n" +
                            "mov eax, 1\n" +
                            "mov [M+"+newTemporary+"], eax\n" +
                            endLabel +":\n";
                }
            }
        }

        return new ExpressionReturn(Type.BOOLEAN, newTemporary, 4);
    }

    /**
     * Método responsável pela geração de código 20
     * @param id LexicalRegister do identificador
     * @param expression1Return ExpressionReturn da expressão a ser utilizada como índice da string
     * @param expression2Return ExpressionReturn da expressão a ser atribuída ao identificador
     */
    public void codeGenerate20(LexicalRegister id, ExpressionReturn expression1Return, ExpressionReturn expression2Return) {
        switch (id.symbol.idType) {
            case STRING:
                if(expression2Return.type == Type.CHARACTER){
                    generatedCode+="mov eax, [M+"+expression1Return.address+"]\n" +
                            "cdqe\n" +
                            "mov rsi, M+"+id.symbol.address+"\n" +
                            "add rsi, rax\n" +
                            "mov bl, [M+"+expression2Return.address+"]\n" +
                            "mov [rsi], bl\n";
                }
                else {
                    String label0 = getNewLabel();
                    String label1 = getNewLabel();
                    generatedCode+="mov rdi, M+"+id.symbol.address+"\n" +
                            "mov ebx, 0\n" +
                            "mov al, 0\n" +
                            label0+":\n"+
                            "mov [rdi], al\n" +
                            "add rdi, 1\n" +
                            "add ebx, 1\n" +
                            "cmp ebx, 255\n" +
                            "jne "+label0+"\n"+
                            "mov rdi, M+"+id.symbol.address+"\n" +
                            "mov rsi, M+"+expression2Return.address+"\n" +
                            label1+":\n"+
                            "mov al, [rsi]\n" +
                            "mov [rdi], al\n" +
                            "add rdi, 1\n" +
                            "add rsi, 1\n" +
                            "cmp al, 0\n" +
                            "jne "+label1+"\n" +
                            "mov al, 0 \n" +
                            "mov [rdi], al\n";
                    id.symbol.size = expression2Return.size;
                }
                break;
            case REAL:
                if(expression2Return.type == Type.INTEGER) {
                    generatedCode+="mov eax, [M+"+expression2Return.address+"]\n" +
                            "cdqe\n" +
                            "cvtsi2ss xmm0, rax\n" +
                            "movss [M+"+id.symbol.address+"], xmm0\n";
                }
                else {
                    generatedCode+="movss xmm0, [M+"+expression2Return.address+"]\n" +
                            "movss [M+"+id.symbol.address+"], xmm0\n";
                }
                break;
            case INTEGER:
                generatedCode+="mov eax, [M+"+expression2Return.address+"]\n" +
                        "mov [M+"+id.symbol.address+"], eax\n";
                break;
            case CHARACTER:
                generatedCode+="mov al, [M+"+expression2Return.address+"]\n" +
                        "mov [M+"+id.symbol.address+"], al\n";
                break;
        }
    }

    /**
     * Método responsável pela geração de código 21
     * @param labelStartWhile rótulo do começo do while
     */
    public void codeGenerate21(String labelStartWhile) {
        generatedCode+=labelStartWhile+":\n";
    }

    /**
     * Método responsável pela geração de código 22
     * @param labelEndWhile rótulo do final do while
     * @param expressionReturn expression return a ser comparada
     */
    public void codeGenerate22(String labelEndWhile, ExpressionReturn expressionReturn) {
        generatedCode+="mov eax, [M+"+expressionReturn.address+"]\n" +
                "cmp eax, 1\n" +
                "jne "+labelEndWhile+"\n";
    }

    /**
     * Método responsável pela geração de código 23
     * @param labelStartWhile rótulo do começo do while
     * @param labelEndWhile rótulo do final do while
     */
    public void codeGenerate23(String labelStartWhile, String labelEndWhile) {
        generatedCode+="jmp "+ labelStartWhile+"\n" +
                labelEndWhile+":\n";
    }

    /**
     * Método responsável pela geração de código 24
     * @param expressionReturn expressão a ser comparada no if
     * @param labelElse rótulo do else
     */
    public void codeGenerate24(ExpressionReturn expressionReturn, String labelElse) {
        generatedCode+="mov eax, [M+"+expressionReturn.address+"]\n" +
                "cmp eax, 1\n" +
                "jne "+labelElse+"\n";
    }

    /**
     * Método responsável pela geração de código 25
     * @param labelElse rótulo do else
     * @param labelEnd rótulo do final do teste
     */
    public void codeGenerate25(String labelElse, String labelEnd){
        generatedCode+="jmp "+labelEnd+"\n" +
                labelElse+":\n";
    }

    /**
     * Método responsável pela geração de código 26
     * @param labelEnd rótulo do final do teste
     */
    public void codeGenerate26(String labelEnd) {
        generatedCode+=labelEnd+":\n";
    }

    /**
     * Método responsável pela geração de código 27
     * @param id objeto que irá receber o valor lido do teclado
     */
    public void codeGenerate27(LexicalRegister id) {
        switch (id.symbol.idType) {
            case STRING:
                long newTemporary = getNewTemporaryAddress(256);
                String label0 = getNewLabel();
                String label1 = getNewLabel();
                generatedCode+="mov rsi, M+"+newTemporary+"\n" +
                        "mov rdx, 100h ;tamanho do buffer\n" +
                        "mov rax, 0 ;chamada para leitura\n" +
                        "mov rdi, 0 ;leitura do teclado\n" +
                        "syscall\n" +
                        "mov rsi, M+"+newTemporary+"\n" +
                        "mov rdi, M+"+id.symbol.address+"\n" +
                        "mov ebx, 0\n" +
                        label0+":\n" +
                        "mov al, 0\n" +
                        "mov [rdi], al\n" +
                        "add rdi, 1\n" +
                        "add ebx, 1\n" +
                        "cmp ebx, 255\n" +
                        "jne "+ label0 +"\n" +
                        "mov rdi, M+"+id.symbol.address+"\n" +
                        label1+":\n" +
                        "mov al, [rsi]\n" +
                        "mov [rdi], al\n" +
                        "add rdi, 1\n" +
                        "add rsi, 1\n" +
                        "cmp al, 10\n" +
                        "jne "+ label1 +"\n" +
                        "sub rdi, 1\n" +
                        "mov al, 0\n" +
                        "mov [rdi], al\n";
                break;
            case REAL:
                label0 = getNewLabel();
                label1 = getNewLabel();
                String label2 = getNewLabel();
                String label3 = getNewLabel();
                newTemporary = getNewTemporaryAddress(128);
                generatedCode+="mov rsi, M+"+newTemporary+"\n" +
                        "mov rdx, 100h ;tamanho do buffer\n" +
                        "mov rax, 0 ;chamada para leitura\n" +
                        "mov rdi, 0 ;leitura do teclado\n" +
                        "syscall\n" +
                        "mov rax, 0 ;acumul. parte int.\n" +
                        "subss xmm0,xmm0 ;acumul. parte frac.\n" +
                        "mov rbx, 0 ;caractere\n" +
                        "mov rcx, 10 ;base 10\n" +
                        "cvtsi2ss xmm3,rcx ;base 10\n" +
                        "movss xmm2,xmm3 ;potência de 10\n" +
                        "mov rdx, 1 ;sinal\n" +
                        "mov rsi, M+"+newTemporary+"\n" +
                        "mov bl, [rsi] ;carrega caractere\n" +
                        "cmp bl, '-' ;sinal - ?\n" +
                        "jne "+label0+"\n"+
                        "mov rdx, -1 ;senão, armazena -\n" +
                        "add rsi, 1 ;inc. ponteiro string\n" +
                        "mov bl, [rsi] ;carrega caractere\n" +
                        label0+":\n" +
                        "push rdx ;empilha sinal\n" +
                        "mov rdx, 0 ;reg. multiplicação\n" +
                        label1 + ":\n" +
                        "cmp bl, 0Ah ;verifica fim string\n" +
                        "je "+label2+"\n" +
                        "cmp bl, '.' ;senão verifica ponto\n" +
                        "je "+label3+"\n" +
                        "imul ecx ;mult. eax por 10\n" +
                        "sub bl, '0' ;converte caractere\n" +
                        "add eax, ebx ;soma valor caractere\n" +
                        "add rsi, 1 ;incrementa base\n" +
                        "mov bl, [rsi] ;carrega caractere\n" +
                        "jmp "+label1+"\n" +
                        label3+":\n" +
                        ";calcula parte fracionária em xmm0\n" +
                        "add rsi, 1 ;inc. ponteiro string\n" +
                        "mov bl, [rsi] ;carrega caractere\n" +
                        "cmp bl, 0Ah ;*verifica fim string\n" +
                        "je "+label2+"\n"+
                        "sub bl, '0' ;converte caractere\n" +
                        "cvtsi2ss xmm1,rbx ;conv real\n" +
                        "divss xmm1,xmm2 ;transf. casa decimal\n" +
                        "addss xmm0,xmm1 ;soma acumul.\n" +
                        "mulss xmm2,xmm3 ;atualiza potência\n" +
                        "jmp "+label3+"\n"+
                        label2+":\n"+
                        "cvtsi2ss xmm1,rax ;conv parte inteira\n" +
                        "addss xmm0,xmm1 ;soma parte frac.\n" +
                        "pop rcx ;desempilha sinal\n" +
                        "cvtsi2ss xmm1,rcx ;conv sinal\n" +
                        "mulss xmm0,xmm1 ;mult. sinal\n" +
                        "movss [M+"+id.symbol.address+"], xmm0\n";
                break;
            case CHARACTER:
                newTemporary = getNewTemporaryAddress(1);
                generatedCode+="mov rsi, M+"+newTemporary+"\n" +
                        "mov rdx, 100h ;tamanho do buffer\n" +
                        "mov rax, 0 ;chamada para leitura\n" +
                        "mov rdi, 0 ;leitura do teclado\n" +
                        "syscall\n" +
                        "mov rsi, M+"+newTemporary+"\n" +
                        "mov al, [rsi]\n" +
                        "mov [M+"+id.symbol.address+"], al\n";
                break;
            case INTEGER:
                label0 = getNewLabel();
                label1 = getNewLabel();
                label2 = getNewLabel();
                label3 = getNewLabel();
                newTemporary = getNewTemporaryAddress(12);
                generatedCode+="mov rsi, M+"+newTemporary+"\n" +
                        "mov rdx, 0Ch ;tamanho do buffer\n" +
                        "mov rax, 0 ;chamada para leitura\n" +
                        "mov rdi, 0 ;leitura do teclado\n" +
                        "syscall\n" +
                        "mov eax, 0 ;acumulador\n" +
                        "mov ebx, 0 ;caractere\n" +
                        "mov ecx, 10 ;base 10\n" +
                        "mov dx, 1 ;sinal\n" +
                        "mov rsi, M+"+newTemporary+";end. buffer\n" +
                        "mov bl, [rsi] ;carrega caractere\n" +
                        "cmp bl, '-' ;sinal - ?\n" +
                        "jne "+label0+"\n" +
                        "mov dx, -1 ;senão, armazena -\n" +
                        "add rsi, 1 ;inc. ponteiro string\n" +
                        "mov bl, [rsi] ;carrega caractere\n" +
                        label0+":\n" +
                        "push dx ;empilha sinal\n" +
                        "mov edx, 0 ;reg. multiplicação\n" +
                        label1+":\n" +
                        "cmp bl, 0Ah ;verifica fim string\n" +
                        "je "+label2+"; \n" +
                        "imul ecx ;mult. eax por 10\n" +
                        "sub bl, '0' ;converte caractere\n" +
                        "add eax, ebx ;soma valor caractere\n" +
                        "add rsi, 1 ;incrementa base\n" +
                        "mov bl, [rsi] ;carrega caractere\n" +
                        "jmp "+label1+"\n" +
                        label2+":\n" +
                        "pop cx ;desempilha sinal\n" +
                        "cmp cx, 0\n" +
                        "jg "+label3+"\n" +
                        "neg eax ;mult. sinal\n" +
                        label3+":\n" +
                        "mov [M+"+id.symbol.address+"], eax\n";
                break;
        }
    }

}

/**
 * Classe que indica o tipo, endereço e tamanho do retorno de uma expressão
 */
class ExpressionReturn {
    Type type;
    long address;
    int size;

    ExpressionReturn(Type type, long address, int size){
        this.type = type;
        this.address = address;
        this.size = size;
    }

    @Override
    public String toString() {
        return "ExpressionReturn{" +
                "type=" + type +
                ", address=" + address +
                ", size=" + size +
                '}';
    }

    public void print() {
        System.out.println(this);
    }
}
/**
 * Classe em que implementamos o nosso analisador semântico.
 */
class SemanticAnalyzer {
    CodeGenerator codeGenerator;
    int lastTokenReadLine;//Usado pelo semântico para imprimir o error

    SemanticAnalyzer(CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    /**
     * Método responsável pela ação semântica 1
     * @param id LexicalRegister do identificador
     * @param constValue LexicalRegister da constante
     * @param isNegative Se possui sinal de negativo
     * @throws CompilerError
     */
    public void semanticAction1(LexicalRegister id, LexicalRegister constValue, boolean isNegative) throws CompilerError {
        Type constValueType = constValue.constType.toType();
        if((constValueType !=Type.INTEGER && constValueType != Type.REAL) && isNegative){
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        if (id.symbol.idClass == null) {
            id.symbol.idClass = Class.CONST;
            id.symbol.idType = constValueType;
        }
        else {
            throw new CompilerError("identificador ja declarado ["+ id.symbol.lexeme +"].", lastTokenReadLine);
        }
    }

    /**
     * Método responsável pela ação semântica 2
     * @param id  LexicalRegister do identificador
     * @param declarationInitType Tipo do identificador na declaração
     * @throws CompilerError
     */
    public void semanticAction2(LexicalRegister id, Type declarationInitType) throws CompilerError {
        if (id.symbol.idClass == null) {
            id.symbol.idClass = Class.VAR;
            id.symbol.idType = declarationInitType;
        }
        else {
            throw new CompilerError("identificador ja declarado ["+ id.symbol.lexeme +"].", lastTokenReadLine);
        }
    }

    /**
     * Método responsável pela ação semântica 4
     * @param id LexicalRegister do identificador
     * @throws CompilerError
     */
    public void semanticAction4(LexicalRegister id) throws CompilerError {
        if(id.symbol.idClass == null) {
            throw new CompilerError("identificador nao declarado ["+ id.symbol.lexeme +"].", lastTokenReadLine);
        }
    }

    /**
     * Método responsável pela ação semântica 5
     * @param id LexicalRegister do identificador
     * @throws CompilerError
     */
    public void semanticAction5(LexicalRegister id) throws CompilerError {
        if(id.symbol.idClass == null) {
            throw new CompilerError("identificador nao declarado ["+ id.symbol.lexeme +"].", lastTokenReadLine);
        }
        else if(id.symbol.idClass == Class.CONST) {
            throw new CompilerError("classe de identificador incompativel ["+ id.symbol.lexeme +"].", lastTokenReadLine);
        }
    }

    /**
     * Método responsável pela ação semântica 6
     * @param idType tipo do identificador
     * @param expressionType tipo da expressão
     * @throws CompilerError
     */
    public void semanticAction6(Type idType, Type expressionType) throws CompilerError {
        if(idType != Type.STRING) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        else if(expressionType != Type.INTEGER) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
    }

    /**
     * Método responsável pela ação semântica 7
     * @param hasStringAccess se está acessando a posição de uma string
     * @param expressionType tipo da expressão
     * @param idType tipo do id
     * @throws CompilerError
     */
    public void semanticAction7(boolean hasStringAccess, Type expressionType, Type idType) throws CompilerError {
        if(hasStringAccess && (expressionType != Type.CHARACTER)) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        if(idType == Type.REAL) {
            if(expressionType != Type.INTEGER && expressionType != Type.REAL){
                throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
            }
        }
        else if(!hasStringAccess && idType != expressionType) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
    }

    /**
     * Método responsável pela ação semântica 8
     * @param expressionType tipo da expressão
     * @throws CompilerError
     */
    public void semanticAction8(Type expressionType) throws CompilerError {
        if(expressionType != Type.BOOLEAN) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
    }

    /**
     * Método responsável pela ação semântica 9
     * @param expression1_1Data ExpressionReturn da Expressão1_1
     * @param expression1_2Type Tipo da Expressão1_2
     * @param operator Operador relacional
     * @return ExpressionReturn da expressão
     * @throws CompilerError
     */
    public ExpressionReturn semanticAction10(ExpressionReturn expression1_1Data, Type expression1_2Type, Token operator) throws CompilerError {
        ExpressionReturn expressionReturn = new ExpressionReturn(expression1_1Data.type, expression1_1Data.address, 4);
        if((expression1_1Data.type == Type.INTEGER || expression1_1Data.type == Type.REAL) && expression1_2Type == Type.CHARACTER) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        else if((expression1_2Type == Type.INTEGER || expression1_2Type == Type.REAL) && expression1_1Data.type == Type.CHARACTER) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        else if(expression1_1Data.type == Type.STRING && expression1_2Type == Type.STRING){
            if(operator != Token.EQUAL) {
                throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
            }
        }
        else if(expression1_1Data.type == Type.STRING || expression1_2Type == Type.STRING) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        else if(expression1_1Data.type == Type.BOOLEAN || expression1_2Type == Type.BOOLEAN) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        return expressionReturn;
    }

    /**
     * Método responsável pela ação semântica 17
     * @param expression2_2Type Tipo da Expressão2_2
     * @param operator operador usado
     * @param expression1Data ExpressionReturn da Expressão1
     * @return ExpressionReturn da expressão
     * @throws CompilerError
     */
    public ExpressionReturn semanticAction17(Type expression2_2Type, Token operator, ExpressionReturn expression1Data) throws CompilerError {
        Type expressionType = expression1Data.type;
        if(expression1Data.type == Type.STRING || expression1Data.type  == Type.CHARACTER || expression2_2Type == Type.STRING || expression2_2Type == Type.CHARACTER) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        if(expression1Data.type  == Type.BOOLEAN && expression2_2Type == Type.BOOLEAN) {
            if(operator != Token.OR)
                throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
            return new ExpressionReturn(expressionType, expression1Data.address, expression1Data.size);
        }
        else if(expression1Data.type  == Type.BOOLEAN || expression2_2Type == Type.BOOLEAN){
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        if(operator == Token.OR) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        return new ExpressionReturn(expressionType, expression1Data.address, expression1Data.size);
    }

    /**
     * Método responsável pela ação semântica 22
     * @param expression2_1Return ExpressionReturn da Expressão2_1
     * @param isNegative se possui sinal de negativo
     * @return ExpressionReturn da expressão
     * @throws CompilerError
     */
    public ExpressionReturn semanticAction22(ExpressionReturn expression2_1Return, boolean isNegative) throws CompilerError {
        if((expression2_1Return.type != Type.INTEGER && expression2_1Return.type != Type.REAL) && isNegative) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }

        return codeGenerator.codeGenerate16(isNegative, expression2_1Return);
    }

    /**
     * Método responsável pela ação semântica 23
     * @param expression3_2Type Tipo da Expressão3_2
     * @param operator operador usado
     * @param expression2Data ExpressionReturn da Expressão2
     * @return ExpressionReturn da expressão
     * @throws CompilerError
     */
    public ExpressionReturn semanticAction23(Type expression3_2Type, Token operator, ExpressionReturn expression2Data) throws CompilerError {
        Type expressionType;
        if(expression2Data.type == Type.STRING || expression2Data.type == Type.CHARACTER || expression3_2Type == Type.STRING || expression3_2Type == Type.CHARACTER) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        if(expression2Data.type == Type.BOOLEAN && expression3_2Type == Type.BOOLEAN) {
            expressionType = Type.BOOLEAN;
            if(operator != Token.AND)
                throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
            return new ExpressionReturn(expressionType, expression2Data.address, expression2Data.size);
        }
        else if(expression2Data.type == Type.BOOLEAN || expression3_2Type == Type.BOOLEAN){
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        else if(expression2Data.type == Type.REAL || expression3_2Type == Type.REAL) {
            expressionType = expression2Data.type;
            if(operator == Token.DIV || operator == Token.MOD)
                throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        else {
            expressionType = Type.INTEGER;
        }
        if(operator == Token.AND) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        if(operator == Token.DIVISION) {
            expressionType = expression2Data.type;
        }
        return new ExpressionReturn(expressionType, expression2Data.address, expression2Data.size);
    }


    /**
     * Método responsável pela ação semântica 30
     * @param expression3HasExclamationOperator se possui sinal de negação
     * @param expression4Return ExpressionReturn da Expressão4
     * @param shouldNegateExpression se expressão irá ser negada ou não
     * @return ExpressionReturn da expressão
     * @throws CompilerError
     */
    public ExpressionReturn semanticAction30(boolean expression3HasExclamationOperator, ExpressionReturn expression4Return, boolean shouldNegateExpression) throws CompilerError {
        if (expression3HasExclamationOperator && (expression4Return.type != Type.BOOLEAN)){
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }

        return codeGenerator.codeGenerate13(shouldNegateExpression, expression4Return);
    }

    /**
     * Método responsável pela ação semântica 32
     * @param expressionType tipo da expressão
     * @throws CompilerError
     */
    public void semanticAction32(Type expressionType) throws CompilerError {
        if(expressionType != Type.INTEGER && expressionType != Type.REAL) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
    }

    /**
     * Método responsável pela ação semântica 37
     * @param expressionType tipo da expressão
     * @throws CompilerError
     */
    public void semanticAction37(Type expressionType) throws CompilerError {
        if(expressionType != Type.INTEGER) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
    }

    /**
     * Método responsável pela ação semântica 46
     * @param declarationInitType tipo do identificador na declaração
     * @param constValueType tipo da constante
     * @param isNegative se possui sinal de negativo
     * @throws CompilerError
     */
    public void semanticAction46(Type declarationInitType, Type constValueType, boolean isNegative) throws CompilerError {
        if((declarationInitType != Type.INTEGER && declarationInitType != Type.REAL) && isNegative) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        if(declarationInitType == Type.REAL) {
            if(constValueType != Type.INTEGER && constValueType != Type.REAL){
                throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
            }
        } else if(declarationInitType != constValueType) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
    }
}

/**
 * Classe em que implementamos o nosso analisador sintático.
 */
class SyntaxAnalyzer {
    LexicalAnalyzer lexicalAnalyzer;
    SemanticAnalyzer semanticAnalyzer;
    LexicalRegister currentRegister;
    CodeGenerator codeGenerator;

    /**
     * Construtor do analisador sintático, recebe como parâmetro o analisador léxico
     * @param lexicalAnalyzer Instância do analisador léxico
     */
    public SyntaxAnalyzer(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
        this.codeGenerator = new CodeGenerator();
        this.semanticAnalyzer = new SemanticAnalyzer(this.codeGenerator);
    }

    /**
     * Método responsável por iniciar a análise sintática.
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    public void startSyntaxAnalyzer() throws CompilerError {
        currentRegister = lexicalAnalyzer.getNextToken();
        start();
        System.out.println(lexicalAnalyzer.currentLine + " linhas compiladas.");
        codeGenerator.printCode(); // Comentar essa linha quando for enviar o semântico no verde.
    }

    /**
     * Método que implementa o símbolo não terminal Start
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private void start() throws CompilerError {
        boolean isOnDeclaration = isOnDeclarationFirst();
        boolean isOnCommand = isOnCommandFirst();
        while(isOnDeclaration || isOnCommand){

            if(isOnDeclaration) {
                declaration();
                matchToken(Token.SEMICOLON);
            }
            else {
                blockOrCommand();
            }
            isOnDeclaration = isOnDeclarationFirst();
            isOnCommand = isOnCommandFirst();
        }

        matchToken(Token.EOF);
    }

    /**
     * Método que implementa o símbolo não terminal Declaration
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private void declaration() throws CompilerError {
        if(isOnTypeFirst()){
            Type declarationInitType = types(); // Ação semantica 3
            declarationInit(declarationInitType);
            while(currentRegister.symbol.tokenType == Token.COMMA) {
                matchToken(Token.COMMA);
                declarationInit(declarationInitType);
            }
        }
        else {
            boolean isNegative = false;
            matchToken(Token.CONST);
            LexicalRegister id = currentRegister;
            matchToken(Token.ID);
            matchToken(Token.EQUAL);
            if(currentRegister.symbol.tokenType == Token.MINUS){
                matchToken(Token.MINUS);
                isNegative = true; // ação 47
            }
            LexicalRegister constValue =  currentRegister;
            matchToken(Token.CONST_VALUE);
            semanticAnalyzer.semanticAction1(id, constValue, isNegative);
            codeGenerator.codeGenerate5(constValue, id, isNegative);
        }

    }

    /**
     * Método que implementa o símbolo não terminal DeclarationInit
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private void declarationInit(Type declarationInitType) throws CompilerError {
        boolean isNegative = false;
        LexicalRegister id = currentRegister;
        matchToken(Token.ID);
        semanticAnalyzer.semanticAction2(id, declarationInitType);
        if(currentRegister.symbol.tokenType == Token.ATTRIBUTION){
            matchToken(Token.ATTRIBUTION);
            if(currentRegister.symbol.tokenType == Token.MINUS){
                matchToken(Token.MINUS);
                isNegative = true; // ação 45
            }
            LexicalRegister constValue = currentRegister;
            matchToken(Token.CONST_VALUE);
            semanticAnalyzer.semanticAction46(declarationInitType, constValue.constType.toType(), isNegative);
            codeGenerator.codeGenerate5(constValue, id, isNegative);
        }
        else {
            codeGenerator.codeGenerate6(id);
        }
    }

    /**
     * Método que implementa o símbolo não terminal Type
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private Type types() throws CompilerError {
        Token currentToken = currentRegister.symbol.tokenType;
        Type returnType;
        if(currentToken == Token.INT){
            matchToken(Token.INT);
            returnType = Type.INTEGER;// Ação semantica 39
        }
        else if(currentToken == Token.FLOAT){
            matchToken(Token.FLOAT);
            returnType = Type.REAL;// Ação semantica 40
        }
        else if(currentToken == Token.STRING){
            matchToken(Token.STRING);
            returnType = Type.STRING;// Ação semantica 41
        }
        else {
            matchToken(Token.CHAR);
            returnType = Type.CHARACTER;// Ação semantica 42
        }

        return returnType;
    }

    /**
     * Método que implementa o símbolo não terminal Block or Command
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private void blockOrCommand() throws CompilerError {
        if(isOnCommandFirst()) {
            command();
        }
        else {
            block();
        }
    }

    /**
     * Método que implementa o símbolo não terminal Block
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private void block() throws CompilerError {
        matchToken(Token.LEFT_BRACE);
        while(isOnCommandFirst()){
            command();
        }
        matchToken(Token.RIGHT_BRACE);
    }

    /**
     * Método que implementa o símbolo não terminal Command
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private void command() throws CompilerError {
        boolean hasStringAccess = false;
        Token currentToken = currentRegister.symbol.tokenType;
        if(currentToken == Token.ID){
            ExpressionReturn expression1Return = null;
            LexicalRegister id = currentRegister;
            matchToken(Token.ID);
            semanticAnalyzer.semanticAction5(id);
            if (currentRegister.symbol.tokenType != Token.ATTRIBUTION) {
                matchToken(Token.LEFT_SQUARE_BRACKET);
                expression1Return = expression();
                matchToken(Token.RIGHT_SQUARE_BRACKET);
                semanticAnalyzer.semanticAction6(id.symbol.idType, expression1Return.type);//Ação 6
                hasStringAccess = true;
            }
            matchToken(Token.ATTRIBUTION);
            ExpressionReturn expression2Return = expression();
            semanticAnalyzer.semanticAction7(hasStringAccess, expression2Return.type, id.symbol.idType);//Ação 7
            codeGenerator.codeGenerate20(id, expression1Return, expression2Return);
            matchToken(Token.SEMICOLON);
        }
        else if(currentToken == Token.WHILE){
            repetition();
        }
        else if(currentToken == Token.IF){
            test();
        }
        else if(currentToken == Token.SEMICOLON){
            matchToken(Token.SEMICOLON);
        }
        else if(currentToken == Token.READ_LINE){
            matchToken(Token.READ_LINE);
            matchToken(Token.OPEN_PARENTESIS);
            LexicalRegister id = currentRegister;
            matchToken(Token.ID);
            semanticAnalyzer.semanticAction5(id);
            matchToken(Token.CLOSE_PARENTESIS);
            codeGenerator.codeGenerate27(id);
            matchToken(Token.SEMICOLON);
        }
        else {
            boolean isWriteLn = false;
            if(currentToken == Token.WRITE){
                matchToken(Token.WRITE);
            }
            else {
                matchToken(Token.WRITE_LINE);
                isWriteLn = true;
            }
            write(isWriteLn);
            matchToken(Token.SEMICOLON);
        }
    }

    /**
     * Método que implementa o símbolo não terminal Expression
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private ExpressionReturn expression() throws CompilerError {
        ExpressionReturn expressionData = expression1();//Ação semantica 9 e code generate 18
        if (isOnRelationalOperatorsFirst()){
            Token operator = relationalOperator();
            ExpressionReturn expression1_2Return = expression1();
            expressionData = semanticAnalyzer.semanticAction10(expressionData, expression1_2Return.type, operator);
            expressionData = codeGenerator.codeGenerate19(operator, expressionData, expression1_2Return);
        }
        return expressionData;
    }

    /**
     * Método que implementa o símbolo não terminal Expression1
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private ExpressionReturn expression1() throws CompilerError {
        boolean isNegative = false;
        if(currentRegister.symbol.tokenType == Token.MINUS){
            matchToken(Token.MINUS);
            isNegative = true;
        }
        ExpressionReturn expression2_1Return = expression2();
        ExpressionReturn expression1Data = semanticAnalyzer.semanticAction22(expression2_1Return, isNegative); //Code generate 16
        Token currentToken = currentRegister.symbol.tokenType;
        while(currentToken == Token.MINUS || currentToken == Token.PLUS || currentToken == Token.OR){
            Token operator = currentToken;
            if(currentToken == Token.MINUS){ //semantic action 18
                matchToken(Token.MINUS);
            }
            else if(currentToken == Token.PLUS){ //semantic action 19
                matchToken(Token.PLUS);
            }
            else { //semantic action 20
                matchToken(Token.OR);
            }
            ExpressionReturn expression2_2Return = expression2();
            expression1Data = semanticAnalyzer.semanticAction17(expression2_2Return.type, operator, expression1Data);
            expression1Data = codeGenerator.codeGenerate17(expression1Data, expression2_2Return, operator);
            currentToken = currentRegister.symbol.tokenType;
        }
        return expression1Data;
    }

    /**
     * Método que implementa o símbolo não terminal Expression2
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private ExpressionReturn expression2() throws CompilerError {
        ExpressionReturn expression3_1Return = expression3();
        Token currentToken = currentRegister.symbol.tokenType;
        ExpressionReturn expression2Data = expression3_1Return; //Code Generate 14
        while(currentToken == Token.MULTIPLICATION || currentToken == Token.AND
                || currentToken == Token.DIVISION || currentToken == Token.DIV
                || currentToken == Token.MOD ){
            Token operator = currentToken;
            if(currentToken == Token.MULTIPLICATION){
                matchToken(Token.MULTIPLICATION); // Ação 24
            }
            else if(currentToken == Token.AND){
                matchToken(Token.AND); // Ação 25
            }
            else if(currentToken == Token.DIVISION){
                matchToken(Token.DIVISION); // Ação 26
            }
            else if(currentToken == Token.DIV){
                matchToken(Token.DIV); // Ação 27
            }
            else {
                matchToken(Token.MOD); // Ação 28
            }
            ExpressionReturn expression3_2Return = expression3();
            expression2Data = semanticAnalyzer.semanticAction23(expression3_2Return.type, operator, expression2Data);
            expression2Data = codeGenerator.codeGenerate15(expression2Data, expression3_2Return, operator);
            currentToken = currentRegister.symbol.tokenType;
        }
        return expression2Data;
    }

    /**
     * Método que implementa o símbolo não terminal Expression3
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private ExpressionReturn expression3() throws CompilerError {
        boolean hasExclamationOperator = false;
        int numberOfNegation = 0;
        while(currentRegister.symbol.tokenType == Token.NEGATION){
            matchToken(Token.NEGATION);
            hasExclamationOperator = true;
            numberOfNegation ++;
        }

        ExpressionReturn expression4Return = expression4();
        return semanticAnalyzer.semanticAction30(hasExclamationOperator, expression4Return, numberOfNegation % 2 == 1);
    }

    /**
     * Método que implementa o símbolo não terminal Expression4
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private ExpressionReturn expression4() throws CompilerError {
        ExpressionReturn expression4Data;
        Token currentToken = currentRegister.symbol.tokenType;
        if(currentToken == Token.INT || currentToken == Token.FLOAT){
            Type expression4Type;
            if(currentToken == Token.INT){
                expression4Type = Type.INTEGER;//Ação 43
                matchToken(Token.INT);
            }
            else {
                expression4Type = Type.REAL;//Ação 44
                matchToken(Token.FLOAT);
            }
            matchToken(Token.OPEN_PARENTESIS);
            ExpressionReturn expressionReturn = expression();
            semanticAnalyzer.semanticAction32(expressionReturn.type);
            expression4Data = codeGenerator.codeGenerate12(expression4Type, expressionReturn);
            matchToken(Token.CLOSE_PARENTESIS);
        }
        else {
            expression4Data =  expression5(); // Ação semantica 33 e Code Generate 11
        }

        return expression4Data;
    }

    /**
     * Método que implementa o símbolo não terminal Expression5
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private ExpressionReturn expression5() throws CompilerError {
        ExpressionReturn expression5Data;
        if(currentRegister.symbol.tokenType == Token.OPEN_PARENTESIS){
            matchToken(Token.OPEN_PARENTESIS);
            expression5Data = expression();//Code Generate 10
            matchToken(Token.CLOSE_PARENTESIS);
        }
        else {
            expression5Data = expression6();//Code generate 9
        }

        return expression5Data;
    }

    /**
     * Método que implementa o símbolo não terminal Expression6
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private ExpressionReturn expression6() throws CompilerError {
        ExpressionReturn expression6Data;
        if(currentRegister.symbol.tokenType == Token.CONST_VALUE) {
            LexicalRegister constValue = currentRegister;
            matchToken(Token.CONST_VALUE);
            expression6Data = codeGenerator.codeGenerate7(constValue);// Ação 36 + CodeAção7
        }
        else {
            LexicalRegister id = currentRegister;
            matchToken(Token.ID);
            semanticAnalyzer.semanticAction4(id);
            boolean hasStringAccess = false;
            ExpressionReturn expressionReturn = null;
            if(currentRegister.symbol.tokenType == Token.LEFT_SQUARE_BRACKET) {
                matchToken(Token.LEFT_SQUARE_BRACKET);
                expressionReturn = expression();
                semanticAnalyzer.semanticAction37(expressionReturn.type);
                hasStringAccess = true; // Ação 37
                matchToken(Token.RIGHT_SQUARE_BRACKET);
            }
            expression6Data = codeGenerator.codeGenerate8(hasStringAccess, expressionReturn, id);
        }

        return expression6Data;
    }

    /**
     * Método que implementa o símbolo não terminal Repetition
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private void repetition() throws CompilerError {
        String labelStartWhile = codeGenerator.getNewLabel();
        String labelEndWhile = codeGenerator.getNewLabel();
        matchToken(Token.WHILE);
        codeGenerator.codeGenerate21(labelStartWhile);
        ExpressionReturn expressionReturn = expression();
        semanticAnalyzer.semanticAction8(expressionReturn.type);
        codeGenerator.codeGenerate22(labelEndWhile, expressionReturn);
        blockOrCommand();
        codeGenerator.codeGenerate23(labelStartWhile, labelEndWhile);
    }

    /**
     * Método que implementa o símbolo não terminal Test
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private void test() throws CompilerError {
        String labelElse = codeGenerator.getNewLabel();
        String labelEnd = codeGenerator.getNewLabel();
        matchToken(Token.IF);
        ExpressionReturn expressionReturn = expression();
        semanticAnalyzer.semanticAction8(expressionReturn.type);
        codeGenerator.codeGenerate24(expressionReturn, labelElse);
        blockOrCommand();
        codeGenerator.codeGenerate25(labelElse, labelEnd);
        if(currentRegister.symbol.tokenType == Token.ELSE) {
            matchToken(Token.ELSE);
            blockOrCommand();
        }
        codeGenerator.codeGenerate26(labelEnd);
    }

    /**
     * Método que implementa o símbolo não terminal Write
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private void write(boolean isWriteLn) throws CompilerError {
        matchToken(Token.OPEN_PARENTESIS);
        ExpressionReturn expressionReturn = expression();
        codeGenerator.codeGenerate3(expressionReturn);
        while(currentRegister.symbol.tokenType == Token.COMMA) {
            matchToken(Token.COMMA);
            expressionReturn = expression();
            codeGenerator.codeGenerate3(expressionReturn);
        }
        matchToken(Token.CLOSE_PARENTESIS);
        codeGenerator.codeGenerate4(isWriteLn);
    }

    /**
     * Método que implementa o símbolo não terminal Relational Operator
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private Token relationalOperator() throws CompilerError {
        Token currentToken = currentRegister.symbol.tokenType;
        if(currentToken == Token.EQUAL){
            matchToken(Token.EQUAL);
            return Token.EQUAL;//Ação 11
        }
        else if(currentToken == Token.NOT_EQUAL){
            matchToken(Token.NOT_EQUAL);
            return Token.NOT_EQUAL;//Ação 12
        }
        else if(currentToken == Token.LESSER){
            matchToken(Token.LESSER);
            return Token.LESSER;//Ação 13
        }
        else if(currentToken == Token.GREATER){
            matchToken(Token.GREATER);
            return Token.GREATER;//Ação 14
        }
        else if(currentToken == Token.LESSER_OR_EQUAL_THAN) {
            matchToken(Token.LESSER_OR_EQUAL_THAN);
            return Token.LESSER_OR_EQUAL_THAN;//Ação 15
        }
        else {
            matchToken(Token.GREATER_OR_EQUAL_THAN);
            return Token.GREATER_OR_EQUAL_THAN;//Ação 16
        }
    }

    /**
     * Método que valida se o registro léxico atual está no first de Operators
     * @return um booleano representando se está ou não no first
     */
    private boolean isOnRelationalOperatorsFirst() {
        Token currentToken = currentRegister.symbol.tokenType;

        return currentToken == Token.EQUAL || currentToken == Token.NOT_EQUAL || currentToken == Token.LESSER
                || currentToken == Token.GREATER || currentToken == Token.LESSER_OR_EQUAL_THAN ||
                currentToken == Token.GREATER_OR_EQUAL_THAN;
    }

    /**
     * Método que valida se o registro léxico atual está no first de Type
     * @return um booleano representando se está ou não no first
     */
    private boolean isOnTypeFirst() {
        Token currentToken = currentRegister.symbol.tokenType;
        return currentToken == Token.INT || currentToken == Token.FLOAT || currentToken == Token.STRING ||
                currentToken == Token.CHAR;
    }

    /**
     * Método que valida se o registro léxico atual está no first de Command
     * @return um booleano representando se está ou não no first
     */
    private boolean isOnCommandFirst() {
        Token currentToken = currentRegister.symbol.tokenType;
        return currentToken == Token.ID || currentToken == Token.SEMICOLON || currentToken == Token.READ_LINE
                || currentToken == Token.WRITE || currentToken == Token.WRITE_LINE || currentToken == Token.WHILE
                || currentToken == Token.IF;
    }

    /**
     * Método que valida se o registro léxico atual está no first de Declaration
     * @return um booleano representando se está ou não no first
     */
    private boolean isOnDeclarationFirst() {
        Token currentToken = currentRegister.symbol.tokenType;
        return isOnTypeFirst()|| currentToken == Token.CONST;
    }

    /**
     * Método utilizado durante a construção do projeto para testar o analisador léxico
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    public void testLexicalAnalyzer() throws CompilerError {
        do {
            currentRegister = lexicalAnalyzer.getNextToken();
            currentRegister.print();
        } while (currentRegister.symbol.tokenType != Token.EOF);
        System.out.println(lexicalAnalyzer.currentLine + " linhas compiladas.");
    }

    /**
     * Método casa token, é nele que validamos se o token atual é igual ao token esperado,
     * caso seja solicitamos o próximo token para o léxico senão levantamos um erro de compilação.
     * @param expectedToken Token esperado
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private void matchToken(Token expectedToken) throws CompilerError {
        if(currentRegister.symbol.tokenType == expectedToken) {
            semanticAnalyzer.lastTokenReadLine = lexicalAnalyzer.currentLine;
            currentRegister = lexicalAnalyzer.getNextToken();
        }
        else if(currentRegister.symbol.tokenType == Token.EOF) {
            throw new CompilerError("fim de arquivo nao esperado.", lexicalAnalyzer.currentLine);
        }
        else {
            throw new CompilerError("token nao esperado ["+currentRegister.symbol.lexeme+"].", lexicalAnalyzer.currentLine);
        }

    }
}

/**
 * Classe em que implementamos o nosos analisador léxico.
 */
class LexicalAnalyzer {

    int currentLine = 1; //linha atual sendo lida
    final CodeReader codeReader;
    private Integer lastCharacterByte = null;
    private final SymbolTable symbolTable = SymbolTable.getInstance();


    /**
     * Lista de caracteres aceito na linguagem
     */
    private static final HashSet<Character> acceptedCharacters = new HashSet<>(Arrays.asList('0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z','a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',' ', '_', '.', ',', ';', ':', '(', ')',
            '[', ']', '{', '}', '+', '-', '*', '\"','\'','/', '|', '\\', '&', '%', '!', '?', '>', '<', '=', '\n', '\r'));


    public LexicalAnalyzer(CodeReader codeReader) {
        this.codeReader = codeReader;
    }

    /**
     * Obtêm o próximo token do código.
     * @return LexicalRegister contendo dados sobre o token.
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    public LexicalRegister getNextToken() throws CompilerError {
        int currentState = 0; //estado atual
        int numberOfDecimal = 0; //quantidade de casas decimais
        int idLength = 0; //quantidade de caracteres do identificador
        Character currentCharacter; //caractere atual sendo lido
        String currentLexeme = ""; //caracteres que compõem o lexema sendo lido
        ConstType constType = null; //tipo da constante
        Integer constSize = null; //tamanho da constante
        int currentCharByte;

        //checa se lexema inicia com um caractere lido previamente
        if(lastCharacterByte == null) {
            currentCharByte = codeReader.getNextChar();
        }
        else{
            currentCharByte = lastCharacterByte;
            lastCharacterByte = null;
        }

        int finalState = 4;
        //se estado não é final
        while(currentState != finalState) {
            //se chegou no final do arquivo (não retornou um char na leitura)
            if(currentCharByte == -1) {
                if(currentState == 0)
                    return createEOF();
                else
                    throw new CompilerError("fim de arquivo nao esperado.", currentLine);
            }

            currentCharacter = (char) currentCharByte;

            if(currentCharByte == 13) {
                currentCharByte = codeReader.getNextChar();
            }

            //checa se caractere lido é válido
            if(!verifyIsValidCharacter(currentCharacter)){
                throw new CompilerError("caractere invalido.", currentLine);
            }

            //checa qual o estado atual e encaminha para o próximo estado a partir do caractere lido
            switch (currentState) {
                case 0:
                    if(currentCharacter == ' ' || currentCharacter == '\n' || currentCharacter == '\r') {
                        currentState = 0;
                        currentCharacter = null;
                    }
                    else if(currentCharacter == '0') {
                        currentState = 16;
                        numberOfDecimal++;
                    }
                    else if(currentCharacter == '/') {
                        currentState = 1;
                    }
                    else if(currentCharacter == '>') {
                        currentState = 8;
                    }
                    else if(currentCharacter == '&') {
                        currentState = 9;
                    }
                    else if(currentCharacter == '<') {
                        currentState = 7;
                    }
                    else if(currentCharacter == '*' || currentCharacter == '+' || currentCharacter == '=' || currentCharacter == ','
                            || currentCharacter == ';' || currentCharacter == '(' || currentCharacter == ')' || currentCharacter == '{' ||
                            currentCharacter == '}' || currentCharacter == '[' || currentCharacter == ']' || currentCharacter == '-'){
                        currentState = 4;
                    }
                    else if(currentCharacter >= '1' && currentCharacter <= '9'){
                        currentState = 10;
                        numberOfDecimal ++;
                    }
                    else if(currentCharacter == '|') {
                        currentState = 5;
                    }
                    else if(currentCharacter == '!'){
                        currentState = 6;
                    }
                    else if(currentCharacter == '\"'){
                        currentState = 15;
                    }
                    else if(currentCharacter == '\''){
                        currentState = 13;
                    }
                    else if(isCharLetter(currentCharacter) || currentCharacter == '_' ){
                        currentState = 19;
                        idLength++;
                    }
                    else if(currentCharacter == '.'){
                        currentState = 12;
                    }
                    else {
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + currentCharacter + "].", currentLine);
                    }
                    break;
                case 1:
                    if(currentCharacter == '*') {
                        currentState = 2;
                        currentCharacter = null;
                        currentLexeme = "";
                    }
                    else {
                        lastCharacterByte = currentCharByte;
                        currentState = 4;
                    }
                    break;
                case 2:
                    if(currentCharacter == '*') {
                        currentState = 3;
                    }
                    currentCharacter = null;
                    break;
                case 3:
                    if(currentCharacter == '/') {
                        currentState = 0;
                        currentCharacter = null;
                    }
                    else if(currentCharacter == '*') {
                        currentState=3;
                        currentCharacter = null;
                    }
                    else {
                        currentCharacter = null;
                        currentState = 2;
                    }
                    break;
                case 5:
                    if(currentCharacter == '|'){
                        currentState = 4;
                    }
                    else {
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "].", currentLine);
                    }
                    break;
                case 6:
                    currentState = 4;
                    if (currentCharacter != '=') {
                        lastCharacterByte = currentCharByte;
                    }
                    break;
                case 7:
                    currentState  = 4;
                    if (currentCharacter != '-' && currentCharacter != '=') {
                        lastCharacterByte = currentCharByte;
                    }
                    break;
                case 8:
                    if(currentCharacter != '=') {
                        lastCharacterByte = currentCharByte;
                    }
                    currentState = 4;
                    break;
                case 9:
                    if(currentCharacter == '&') {
                        currentState = 4;
                    } else {
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "].", currentLine);
                    }
                    break;
                case 10:
                    if(isCharDigit(currentCharacter)) {
                        currentState = 10;
                        numberOfDecimal ++;
                    }
                    else if(currentCharacter =='.') {
                        currentState = 12;
                    }
                    else {
                        lastCharacterByte = currentCharByte;
                        constType = ConstType.INT;
                        constSize = Constants.IntBytesSize;
                        currentState = 4;
                    }
                    break;
                case 11:
                    if(isCharDigit(currentCharacter)) {
                        currentState = 11;
                        numberOfDecimal++;
                    }
                    else {
                        lastCharacterByte = currentCharByte;
                        constType = ConstType.FLOAT;
                        constSize = Constants.FloatBytesSize;
                        currentState = 4;
                    }
                    if(numberOfDecimal > Constants.DecimalPrecision){
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "].", currentLine);
                    }
                    break;
                case 12:
                    if(isCharDigit(currentCharacter)) {
                        currentState = 11;
                        numberOfDecimal++;
                    }
                    else {
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "].", currentLine);
                    }
                    break;
                case 13:
                    currentState = 14;
                    break;
                case 14:
                    if(currentCharacter == '\'') {
                        currentState = 4;
                        constType = ConstType.CHAR;
                        constSize = Constants.CharBytesSize;
                    } else {
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "].", currentLine);
                    }
                    break;
                case 15:
                    if(currentCharacter == '\"') {
                        currentState = 4;
                        constType = ConstType.STRING;
                        constSize = currentLexeme.length() - 1;
                    } else if(currentCharacter == '\n' || currentCharacter == '\r') {
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "].", currentLine);
                    }
                    break;
                case 16:
                    if(currentCharacter == 'x'){
                        currentState = 17;
                    }
                    else if(isCharDigit(currentCharacter)){
                        currentState = 10;
                        numberOfDecimal++;
                    }
                    else if(currentCharacter == '.') {
                        currentState = 12;
                    }
                    else {
                        currentState = 4;
                        lastCharacterByte = currentCharByte;
                        constType = ConstType.INT;
                        constSize = Constants.IntBytesSize;
                    }
                    break;
                case 17:
                    if(isCharHexadecimal(currentCharacter)){
                        currentState = 18;
                    }
                    else {
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "].", currentLine);
                    }
                    break;
                case 18:
                    if(isCharHexadecimal(currentCharacter)){
                        constType = ConstType.HEX;
                        constSize = Constants.HexBytesSize;
                        currentState = 4;
                    }
                    else {
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "].", currentLine);
                    }
                    break;
                case 19:
                    if (isCharLetter(currentCharacter) || isCharDigit(currentCharacter) || currentCharacter == '_' || currentCharacter == '.') {
                        currentState = 19;
                        idLength++;
                    } else {
                        lastCharacterByte = currentCharByte;
                        currentState = 4;
                    }
                    if(idLength > Constants.idMaxLength){
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "].", currentLine);
                    }
                    break;
                default:
            }

            //se caractere é quebra de linha, incrementa 1 na variável que representa qual a linha atual
            if(currentCharByte == 10 && lastCharacterByte == null){
                currentLine++;
            }


            if(currentCharacter != null && lastCharacterByte == null)
                currentLexeme += currentCharacter;

            //se estado não é final, lê o próximo caractere
            if(currentState !=4)
                currentCharByte = codeReader.getNextChar();
        }

        return createLexicalRegister(currentLexeme, constType, constSize);
    }

    /**
     * Obtêm o registro léxico que representa final de arquivo
     * @return LexicalRegister contendo Token.EOF
     */
    private LexicalRegister createEOF(){
        Symbol symbol = new Symbol(Token.EOF, "eof");
        return new LexicalRegister(null,symbol, null, null);
    }

    /**
     * Cria ou procura um token na tabela a partir do lexema lido.
     * @param currentLexeme lexema lido.
     * @param constType tipo da constante.
     * @param constSize tamanho da constante.
     * @return LexicalRegister contendo informação sobre o token inserido/encontrado.
     */
    private LexicalRegister createLexicalRegister(String currentLexeme, ConstType constType, Integer constSize) {
        SymbolTableSearchResult result = null;
        Symbol symbol;
        if(constType == null) { //se não é constante
            result = symbolTable.search(currentLexeme); //procura lexema na tabela
            if(result == null){ //se já não existe símbolo na tabela com o lexema lido
                symbol = new Symbol(Token.ID, currentLexeme); //insere na tabela como um identificador
                result = symbolTable.insert(symbol);
            }
            else { //se já existe símbolo na tabela com o lexema lido
                symbol = result.symbol;
            }
        }
        else { //se é constante
            symbol = new Symbol(Token.CONST_VALUE, currentLexeme);
            symbol.size = constSize;
        }
        return new LexicalRegister(result, symbol, constType, constSize);
    }

    /**
     * Checa se caractere é válido para um hexadecimal.
     * @param c caractere a ser checado.
     * @return boolean - se caractere é válido (true) ou não (false).
     */
    private boolean isCharHexadecimal(char c) {
        return isCharDigit(c) || c >= 'A' && c <= 'F';
    }

    /**
     * Checa se caractere é um dígito.
     * @param c caractere a ser checado.
     * @return boolean - se caractere é dígito (true) ou não (false).
     */
    private boolean isCharDigit(char c) {
        return c>='0' && c<='9';
    }

    /**
     * Checa se caractere é um letra.
     * @param c caractere a ser checado.
     * @return boolean - se caractere é letra (true) ou não (false).
     */
    private boolean isCharLetter(char c){
        return (c >= 'a' && c<='z') || (c>='A' && c<='Z');
    }

    /**
     * Checa se caractere é válido na linguagem
     * @param c caractere a ser checado
     * @return boolean - se caractere é válido (true) ou não (false)
     */
    private boolean verifyIsValidCharacter(char c){
        return acceptedCharacters.contains(c);
    }
}

/**
 * Classe que possui dados sobre o registro léxico encontrado/criado na tabela
 */
class LexicalRegister {
    SymbolTableSearchResult symbolInTable;
    Symbol symbol;
    ConstType constType;
    Integer size;

    /**
     * Inicializa objeto de registro léxico
     * @param symbolInTable dados sobre o símbolo na tabela
     * @param symbol objeto que representa o símbolo
     * @param constType tipo da constante (caso seja uma)
     * @param size tamanho da constante (caso seja uma)
     */
    public LexicalRegister(SymbolTableSearchResult symbolInTable, Symbol symbol, ConstType constType, Integer size) {
        this.symbolInTable = symbolInTable;
        this.symbol = symbol;
        this.constType = constType;
        this.size = size;
    }

    @Override
    public String toString() {
        return "LexicalRegister{" +
                "symbolInTable=" + symbolInTable +
                ", symbol=" + symbol +
                ", constType=" + constType +
                ", size=" + size +
                '}';
    }

    public void print() {
        System.out.println(this);
    }
}

/**
 * Classe responsável por realizar a leitura do código.
 */
class CodeReader {
    InputStreamReader reader = new InputStreamReader(System.in);

    /**
     * Método responsável por ler o próximo byte do programa.
     * @return retorna o valor do byte lido.
     */
    public int getNextChar() {
        try{
            return reader.read();
        }
        catch (IOException ioe){
            ioe.printStackTrace();
        }
        return -1;
    }
}

/**
 * Classe principal do compilador.
 */
public class Compiler {
    /**
     * Método main do programa.
     * @param args parâmetros da main.
     */
    public static void main(String[] args) {
        try{
            SyntaxAnalyzer syntaxAnalyzer = configureSyntaxAnalyzer();
            syntaxAnalyzer.startSyntaxAnalyzer();
        }
        catch (CompilerError compilerError){
            compilerError.print();
        }
    }

    /**
     * Método responsável por inicializar o analisador sintático.
     * @return Retorna analisador sintático.
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    public static SyntaxAnalyzer configureSyntaxAnalyzer() throws CompilerError{
        CodeReader codeReader = new CodeReader();
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(codeReader);

        return new SyntaxAnalyzer(lexicalAnalyzer);
    }
}
