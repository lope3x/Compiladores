/**
 *  TP01 e T02 - Compiladores - 2021/2
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

class CodeGenerator {
    String generatedCode = "";
    long dataCount = 0x10000;
    private int temporaryCount = 0;
    private int labelCount = 0;

    CodeGenerator() {
        generatedCode+="section .data\n" +
                "M:\n" +
                "resb 10000h; Reserva para Temporarios\n" +
                "section .text\n" +
                "global _start\n" +
                "_start:\n";
    }

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

    private String generateEndOfCode() {
        return "mov rax, 60\n" +
                "mov rdi, 0\n" +
                "syscall\n";
    }

    public String getNewLabel() {
        String newLabel = "Label"+labelCount;
        labelCount++;
        return newLabel;
    }

    public long getNewTemporaryAddress(int sizeOfTemporary) {
        long address = temporaryCount;
        temporaryCount+=sizeOfTemporary;
        return address;
    }

    public void resetTemporaryCount() {
        temporaryCount = 0;
    }

    public void codeGenerate3(ExpressionReturn expressionReturn) {
        switch (expressionReturn.type){
            case STRING:
                break;
            case REAL:
                break;
            case BOOLEAN:
            case INTEGER:
                break;
            case CHARACTER:
                break;
        }
    }

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

    public void codeGenerate5(LexicalRegister constvalue, LexicalRegister id, boolean isNegative) {
        Type constType = constvalue.constType.toType();

        switch (constType){
            case STRING:
                id.symbol.address = dataCount;
                id.symbol.size = 255;
                int unUsedStringSpace = 255 - constvalue.size;
                generatedCode+="section .data\n"+
                        "db "+constvalue.symbol.lexeme+", 0\n"+
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
                generatedCode+="section .data\n"+
                        "dd "+lexeme+"\n"+
                        "section .text\n";
                dataCount+=4;
                break;
            case CHARACTER:
                id.symbol.address = dataCount;
                id.symbol.size = 1;
                generatedCode+="section .data\n"+
                        "db "+constvalue.symbol.lexeme+"\n"+
                        "section .text\n";
                break;
        }
    }

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
}

class ExpressionReturn {
    Type type;
    long address;//TODO geração de código
    int size;

    ExpressionReturn(Type type, long address){
        this.type = type;
        this.address = address;
    }
}

class SemanticAnalyzer {
    int lastTokenReadLine;//Usado pelo semântico para imprimir o error

    SemanticAnalyzer() {
    }
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

    public void semanticAction2(LexicalRegister id, Type declarationInitType) throws CompilerError {
        if (id.symbol.idClass == null) {
            id.symbol.idClass = Class.VAR;
            id.symbol.idType = declarationInitType;
        }
        else {
            throw new CompilerError("identificador ja declarado ["+ id.symbol.lexeme +"].", lastTokenReadLine);
        }
    }

    public void semanticAction4(LexicalRegister id) throws CompilerError {
        if(id.symbol.idClass == null) {
            throw new CompilerError("identificador nao declarado ["+ id.symbol.lexeme +"].", lastTokenReadLine);
        }
    }

    public void semanticAction5(LexicalRegister id) throws CompilerError {
        if(id.symbol.idClass == null) {
            throw new CompilerError("identificador nao declarado ["+ id.symbol.lexeme +"].", lastTokenReadLine);
        }
        else if(id.symbol.idClass == Class.CONST) {
            throw new CompilerError("classe de identificador incompativel ["+ id.symbol.lexeme +"].", lastTokenReadLine);
        }
    }

    public void semanticAction6(Type idType, Type expressionType) throws CompilerError {
        if(idType != Type.STRING) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        else if(expressionType != Type.INTEGER) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
    }

    public void semanticAction7(boolean hasStringAccess, Type expressionType, Type idType) throws CompilerError {
        if(hasStringAccess && (expressionType != Type.CHARACTER)) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        else if(idType == Type.REAL && (expressionType != Type.INTEGER && expressionType != Type.REAL)) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        else if(!hasStringAccess && idType != expressionType) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
    }

    public void semanticAction8(Type expressionType) throws CompilerError {
        if(expressionType != Type.BOOLEAN) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
    }

    public ExpressionReturn semanticAction9(Type expression1_1Type) {
        return new ExpressionReturn(expression1_1Type, 0);
    }

    public ExpressionReturn semanticAction10(Type expression1_1Type, Type expression1_2Type, Token operator) throws CompilerError {
        ExpressionReturn expressionReturn = new ExpressionReturn(Type.BOOLEAN, 0);
        if((expression1_1Type == Type.INTEGER || expression1_1Type == Type.REAL) && expression1_2Type == Type.CHARACTER) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        else if((expression1_2Type == Type.INTEGER || expression1_2Type == Type.REAL) && expression1_1Type == Type.CHARACTER) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        else if(expression1_1Type == Type.STRING && expression1_2Type == Type.STRING){
            if(operator != Token.EQUAL) {
                throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
            }
        }
        else if(expression1_1Type == Type.STRING || expression1_2Type == Type.STRING) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        else if(expression1_1Type == Type.BOOLEAN || expression1_2Type == Type.BOOLEAN) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        return expressionReturn;
    }

    public ExpressionReturn semanticAction17(Type expression2_1Type, Type expression2_2Type, Token operator) throws CompilerError {
        Type expressionType;
        if(expression2_1Type == Type.STRING || expression2_1Type == Type.CHARACTER || expression2_2Type == Type.STRING || expression2_2Type == Type.CHARACTER) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        if(expression2_1Type == Type.BOOLEAN && expression2_2Type == Type.BOOLEAN) {
            expressionType = Type.BOOLEAN;
            if(operator != Token.OR)
                throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
            return new ExpressionReturn(expressionType, 0);
        }
        else if(expression2_1Type == Type.BOOLEAN || expression2_2Type == Type.BOOLEAN){
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        else if(expression2_1Type == Type.REAL || expression2_2Type == Type.REAL) {
            expressionType = Type.REAL;
        }
        else {
            expressionType = Type.INTEGER;
        }
        if(operator == Token.OR) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        return new ExpressionReturn(expressionType, 0);
    }

    public void semanticAction22(Type expression2_1Type, boolean isNegative) throws CompilerError {
        if((expression2_1Type != Type.INTEGER && expression2_1Type != Type.REAL) && isNegative) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
    }

    public ExpressionReturn semanticAction23(Type expression3_1Type, Type expression3_2Type, Token operator) throws CompilerError {
        Type expressionType;
        if(expression3_1Type == Type.STRING || expression3_1Type == Type.CHARACTER || expression3_2Type == Type.STRING || expression3_2Type == Type.CHARACTER) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        if(expression3_1Type == Type.BOOLEAN && expression3_2Type == Type.BOOLEAN) {
            expressionType = Type.BOOLEAN;
            if(operator != Token.AND)
                throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
            return new ExpressionReturn(expressionType, 0);
        }
        else if(expression3_1Type == Type.BOOLEAN || expression3_2Type == Type.BOOLEAN){
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        else if(expression3_1Type == Type.REAL || expression3_2Type == Type.REAL) {
            expressionType = Type.REAL;
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
            expressionType = Type.REAL;
        }
        return new ExpressionReturn(expressionType, 0);
    }
    

    public ExpressionReturn semanticAction30(boolean expression3HasExclamationOperator, Type expression4Type) throws CompilerError {
        if (expression3HasExclamationOperator && (expression4Type != Type.BOOLEAN)){
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }

        return new ExpressionReturn(expression4Type, 0);
    }

    public ExpressionReturn semanticAction31(Type expression3_1Type) {
        return new ExpressionReturn(expression3_1Type, 0);
    }

    public void semanticAction32(Type expressionType) throws CompilerError {
        if(expressionType != Type.INTEGER && expressionType != Type.REAL) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
    }

    public void semanticAction37(Type expressionType) throws CompilerError {
        if(expressionType != Type.INTEGER) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
    }

    public void semanticAction46(Type declarationInitType, Type constValueType, boolean isNegative) throws CompilerError {
        if((declarationInitType != Type.INTEGER && declarationInitType != Type.REAL) && isNegative) {
            throw new CompilerError("tipos incompativeis.", lastTokenReadLine);
        }
        if(declarationInitType != constValueType) {
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
        this.semanticAnalyzer = new SemanticAnalyzer();
        this.codeGenerator = new CodeGenerator();
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
            LexicalRegister id = currentRegister;
            matchToken(Token.ID);
            semanticAnalyzer.semanticAction5(id);
            if (currentRegister.symbol.tokenType != Token.ATTRIBUTION) {
                matchToken(Token.LEFT_SQUARE_BRACKET);
                ExpressionReturn expressionReturn = expression();
                matchToken(Token.RIGHT_SQUARE_BRACKET);
                semanticAnalyzer.semanticAction6(id.symbol.idType, expressionReturn.type);//Ação 6
                hasStringAccess = true;
            }
            matchToken(Token.ATTRIBUTION);
            ExpressionReturn expressionReturn = expression();
            semanticAnalyzer.semanticAction7(hasStringAccess, expressionReturn.type, id.symbol.idType);//Ação 7
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
        ExpressionReturn expression1_1Return = expression1();
        ExpressionReturn expressionData = semanticAnalyzer.semanticAction9(expression1_1Return.type); //Dados do Expresion atual
        if (isOnRelationalOperatorsFirst()){
            Token operator = relationalOperator();
            ExpressionReturn expression1_2Return = expression1();
            expressionData = semanticAnalyzer.semanticAction10(expression1_1Return.type, expression1_2Return.type, operator);
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
        semanticAnalyzer.semanticAction22(expression2_1Return.type, isNegative);
        ExpressionReturn expression1Data = expression2_1Return;
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
            expression1Data = semanticAnalyzer.semanticAction17(expression2_1Return.type, expression2_2Return.type, operator);
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
        semanticAnalyzer.semanticAction31(expression3_1Return.type);
        Token currentToken = currentRegister.symbol.tokenType;
        ExpressionReturn expression2Data = expression3_1Return;
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
            expression2Data = semanticAnalyzer.semanticAction23(expression3_1Return.type, expression3_2Return.type, operator);
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
        while(currentRegister.symbol.tokenType == Token.NEGATION){
            matchToken(Token.NEGATION);
            hasExclamationOperator = true;
        }
        ExpressionReturn expression4Return = expression4();

        return semanticAnalyzer.semanticAction30(hasExclamationOperator, expression4Return.type);
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

            expression4Data = new ExpressionReturn(expression4Type, 0);
            matchToken(Token.OPEN_PARENTESIS);
            ExpressionReturn expressionReturn = expression();
            semanticAnalyzer.semanticAction32(expressionReturn.type);
            matchToken(Token.CLOSE_PARENTESIS);
        }
        else {
            expression4Data =  expression5(); // Ação semantica 33
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
            expression5Data = expression();
            matchToken(Token.CLOSE_PARENTESIS);

        }
        else {
            expression5Data = expression6();
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
            expression6Data = new ExpressionReturn(currentRegister.constType.toType(), 0); // Ação 36
            matchToken(Token.CONST_VALUE);
        }
        else {
            LexicalRegister id = currentRegister;
            Type expression6Type = id.symbol.idType;
            matchToken(Token.ID);
            semanticAnalyzer.semanticAction4(id);
            boolean hasStringAccess = false;
            if(currentRegister.symbol.tokenType == Token.LEFT_SQUARE_BRACKET) {
                matchToken(Token.LEFT_SQUARE_BRACKET);
                ExpressionReturn expressionReturn = expression();
                semanticAnalyzer.semanticAction37(expressionReturn.type);
                hasStringAccess = true; // Ação 37
                expression6Type = Type.CHARACTER;
                matchToken(Token.RIGHT_SQUARE_BRACKET);
            }
            if(!hasStringAccess) {
                expression6Type = id.symbol.idType;
            }
            expression6Data = new ExpressionReturn(expression6Type, 0); // Ação 38
        }

        return expression6Data;
    }

    /**
     * Método que implementa o símbolo não terminal Repetition
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private void repetition() throws CompilerError {
        matchToken(Token.WHILE);
        ExpressionReturn expressionReturn = expression();
        semanticAnalyzer.semanticAction8(expressionReturn.type);
        blockOrCommand();
    }

    /**
     * Método que implementa o símbolo não terminal Test
     * @throws CompilerError Erro de compilação, pode ser um error léxico ou sintático.
     */
    private void test() throws CompilerError {
        matchToken(Token.IF);
        ExpressionReturn expressionReturn = expression();
        semanticAnalyzer.semanticAction8(expressionReturn.type);
        blockOrCommand();
        if(currentRegister.symbol.tokenType == Token.ELSE) {
            matchToken(Token.ELSE);
            blockOrCommand();
        }
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
    InputStreamReader reader  = new InputStreamReader(System.in);

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

