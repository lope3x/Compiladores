/**
 *  TP01 e T02 - Compiladores - 2021/2
 *  G07 - Bruno Duarte de Paula Assis, Gabriel Lopes Ferreira(619148), Giovanni Carlos Guaceroni(636206)
 */

import java.io.IOException;
import java.io.InputStreamReader;
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
    STRING
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

    /**
     * Construtor da classe
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
                '}';
    }
}

class SyntaxAnalyzer {
    LexicalAnalyzer lexicalAnalyzer;
    LexicalRegister currentRegister;

    public SyntaxAnalyzer(LexicalAnalyzer lexicalAnalyzer) {
        this.lexicalAnalyzer = lexicalAnalyzer;
    }

    public void startSyntaxAnalyzer() throws CompilerError {
        currentRegister = lexicalAnalyzer.getNextToken();
        start();
        System.out.println(lexicalAnalyzer.currentLine + " linhas compiladas.");
    }

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

    private void declaration() throws CompilerError {
        if(isOnTypeFirst()){
            type();
            declarationInit();
            while(currentRegister.symbol.tokenType == Token.COMMA) {
                matchToken(Token.COMMA);
                declarationInit();
            }
        }
        else {
            matchToken(Token.CONST);
            matchToken(Token.ID);
            matchToken(Token.EQUAL);
            if(currentRegister.symbol.tokenType == Token.MINUS){
                matchToken(Token.MINUS);
            }
            matchToken(Token.CONST_VALUE);
        }

    }

    private void declarationInit() throws CompilerError {
        matchToken(Token.ID);
        if(currentRegister.symbol.tokenType == Token.ATTRIBUTION){
            matchToken(Token.ATTRIBUTION);
            if(currentRegister.symbol.tokenType == Token.MINUS){
                matchToken(Token.MINUS);
            }
            matchToken(Token.CONST_VALUE);
        }
    }

    private void type() throws CompilerError {
        Token currentToken = currentRegister.symbol.tokenType;
        if(currentToken == Token.INT){
            matchToken(Token.INT);
        }
        else if(currentToken == Token.FLOAT){
            matchToken(Token.FLOAT);
        }
        else if(currentToken == Token.STRING){
            matchToken(Token.STRING);
        }
        else {
            matchToken(Token.CHAR);
        }
    }

    private void blockOrCommand() throws CompilerError {
        if(isOnCommandFirst()) {
            command();
        }
        else {
            block();
        }
    }

    private void block() throws CompilerError {
        matchToken(Token.LEFT_BRACE);
        while(isOnCommandFirst()){
            command();
        }
        matchToken(Token.RIGHT_BRACE);
    }

    private void command() throws CompilerError {
        Token currentToken = currentRegister.symbol.tokenType;
        if(currentToken == Token.ID){
            matchToken(Token.ID);
            if (currentRegister.symbol.tokenType != Token.ATTRIBUTION) {
                matchToken(Token.LEFT_SQUARE_BRACKET);
                expression();
                matchToken(Token.RIGHT_SQUARE_BRACKET);
            }
            matchToken(Token.ATTRIBUTION);
            expression();
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
            matchToken(Token.ID);
            matchToken(Token.CLOSE_PARENTESIS);
            matchToken(Token.SEMICOLON);
        }
        else {
            if(currentToken == Token.WRITE){
                matchToken(Token.WRITE);
            }
            else {
                matchToken(Token.WRITE_LINE);
            }
            write();
            matchToken(Token.SEMICOLON);
        }
    }

    private void expression() throws CompilerError {
        expression1();
        while (isOnRelationalOperatorsFirst()){
            relationalOperator();
            expression1();
        }
    }

    private void expression1() throws CompilerError {
        if(currentRegister.symbol.tokenType == Token.MINUS){
            matchToken(Token.MINUS);
        }
        expression2();
        Token currentToken = currentRegister.symbol.tokenType;
        while(currentToken == Token.MINUS || currentToken == Token.PLUS || currentToken == Token.OR){
            if(currentToken == Token.MINUS){
                matchToken(Token.MINUS);
            }
            else if(currentToken == Token.PLUS){
                matchToken(Token.PLUS);
            }
            else {
                matchToken(Token.OR);
            }
            expression2();
            currentToken = currentRegister.symbol.tokenType;
        }
    }

    private void expression2() throws CompilerError {
        expression3();
        Token currentToken = currentRegister.symbol.tokenType;
        while(currentToken == Token.MULTIPLICATION || currentToken == Token.AND
                || currentToken == Token.DIVISION || currentToken == Token.DIV
                || currentToken == Token.MOD ){
            if(currentToken == Token.MULTIPLICATION){
                matchToken(Token.MULTIPLICATION);
            }
            else if(currentToken == Token.AND){
                matchToken(Token.AND);
            }
            else if(currentToken == Token.DIVISION){
                matchToken(Token.DIVISION);
            }
            else if(currentToken == Token.DIV){
                matchToken(Token.DIV);
            }
            else {
                matchToken(Token.MOD);
            }
            expression3();
            currentToken = currentRegister.symbol.tokenType;
        }
    }

    private void expression3() throws CompilerError {
        while(currentRegister.symbol.tokenType == Token.NEGATION){
            matchToken(Token.NEGATION);
        }
        expression4();
    }

    private void expression4() throws CompilerError {
        Token currentToken = currentRegister.symbol.tokenType;
        if(currentToken == Token.INT || currentToken == Token.FLOAT){
            if(currentToken == Token.INT){
                matchToken(Token.INT);
            }
            else {
                matchToken(Token.FLOAT);
            }

            matchToken(Token.OPEN_PARENTESIS);
            expression();
            matchToken(Token.CLOSE_PARENTESIS);
        }
        else {
            expression5();
        }
    }

    private void expression5() throws CompilerError {
        if(currentRegister.symbol.tokenType == Token.OPEN_PARENTESIS){
            matchToken(Token.OPEN_PARENTESIS);
            expression();
            matchToken(Token.CLOSE_PARENTESIS);
        }
        else {
            expression6();
        }
    }

    private void expression6() throws CompilerError {
        if(currentRegister.symbol.tokenType == Token.CONST_VALUE) {
            matchToken(Token.CONST_VALUE);
        }
        else {
            matchToken(Token.ID);

            if(currentRegister.symbol.tokenType == Token.LEFT_SQUARE_BRACKET){
                matchToken(Token.LEFT_SQUARE_BRACKET);
                expression();
                matchToken(Token.RIGHT_SQUARE_BRACKET);
            }
        }
    }

    private void repetition() throws CompilerError {
        matchToken(Token.WHILE);
        expression();
        blockOrCommand();
    }

    private void test() throws CompilerError {
        matchToken(Token.IF);
        expression();
        blockOrCommand();
        if(currentRegister.symbol.tokenType == Token.ELSE) {
            matchToken(Token.ELSE);
            blockOrCommand();
        }
    }

    private void write() throws CompilerError {
        matchToken(Token.OPEN_PARENTESIS);
        expression();
        while(currentRegister.symbol.tokenType == Token.COMMA) {
            matchToken(Token.COMMA);
            expression();
        }
        matchToken(Token.CLOSE_PARENTESIS);
    }

    private void relationalOperator() throws CompilerError {
        Token currentToken = currentRegister.symbol.tokenType;
        if(currentToken == Token.EQUAL){
            matchToken(Token.EQUAL);
        }
        else if(currentToken == Token.NOT_EQUAL){
            matchToken(Token.NOT_EQUAL);
        }
        else if(currentToken == Token.LESSER){
            matchToken(Token.LESSER);
        }
        else if(currentToken == Token.GREATER){
            matchToken(Token.GREATER);
        }
        else if(currentToken == Token.LESSER_OR_EQUAL_THAN) {
            matchToken(Token.LESSER_OR_EQUAL_THAN);
        }
        else {
            matchToken(Token.GREATER_OR_EQUAL_THAN);
        }
    }

    private boolean isOnRelationalOperatorsFirst() {
        Token currentToken = currentRegister.symbol.tokenType;

        return currentToken == Token.EQUAL || currentToken == Token.NOT_EQUAL || currentToken == Token.LESSER
                || currentToken == Token.GREATER || currentToken == Token.LESSER_OR_EQUAL_THAN ||
                currentToken == Token.GREATER_OR_EQUAL_THAN;
    }

    private boolean isOnTypeFirst() {
        Token currentToken = currentRegister.symbol.tokenType;
        return currentToken == Token.INT || currentToken == Token.FLOAT || currentToken == Token.STRING ||
                currentToken == Token.CHAR;
    }

    private boolean isOnCommandFirst() {
        Token currentToken = currentRegister.symbol.tokenType;
        return currentToken == Token.ID || currentToken == Token.SEMICOLON || currentToken == Token.READ_LINE
                || currentToken == Token.WRITE || currentToken == Token.WRITE_LINE || currentToken == Token.WHILE
                || currentToken == Token.IF;
    }

    private boolean isOnDeclarationFirst() {
        Token currentToken = currentRegister.symbol.tokenType;
        return isOnTypeFirst()|| currentToken == Token.CONST;
    }

    public void testLexicalAnalyzer() throws CompilerError {
        do {
            currentRegister = lexicalAnalyzer.getNextToken();
            currentRegister.print();
        } while (currentRegister.symbol.tokenType != Token.EOF);
        System.out.println(lexicalAnalyzer.currentLine + " linhas compiladas.");
    }

    private void matchToken(Token expectedToken) throws CompilerError {
        if(currentRegister.symbol.tokenType == expectedToken) {
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

class LexicalAnalyzer {

    int currentLine = 1;
    final CodeReader codeReader;
    private Integer lastCharacterByte = null;
    private final SymbolTable symbolTable = SymbolTable.getInstance();


    private static final HashSet<Character> acceptedCharacters = new HashSet<>(Arrays.asList('0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z','a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',' ', '_', '.', ',', ';', ':', '(', ')',
            '[', ']', '{', '}', '+', '-', '*', '\"','\'','/', '|', '\\', '&', '%', '!', '?', '>', '<', '=', '\n', '\r'));


    public LexicalAnalyzer(CodeReader codeReader) {
        this.codeReader = codeReader;
    }

    public LexicalRegister getNextToken() throws CompilerError {
        int currentState = 0;
        int numberOfDecimal = 0;
        int idLength = 0;
        Character currentCharacter;
        String currentLexeme = "";
        ConstType constType = null;
        Integer constSize = null;
        int currentCharByte;

        if(lastCharacterByte == null) {
            currentCharByte = codeReader.getNextChar();
        }
        else{
            currentCharByte = lastCharacterByte;
            lastCharacterByte = null;
        }

        int finalState = 4;

        while(currentState != finalState) {
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

            if(!verifyIsValidCharacter(currentCharacter)){
                throw new CompilerError("caractere invalido.", currentLine);
            }

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
                        constSize = currentLexeme.length();
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

            if(currentCharByte == 10 && lastCharacterByte == null){
                currentLine++;
            }

            if(currentCharacter != null && lastCharacterByte == null)
                currentLexeme += currentCharacter;

            if(currentState !=4)
                currentCharByte = codeReader.getNextChar();
        }

        return createLexicalRegister(currentLexeme, constType, constSize);
    }

    private LexicalRegister createEOF(){
        Symbol symbol = new Symbol(Token.EOF, "eof");
        return new LexicalRegister(null,symbol, null, null);
    }

    private LexicalRegister createLexicalRegister(String currentLexeme, ConstType constType, Integer constSize) {
        SymbolTableSearchResult result = null;
        Symbol symbol;
        if(constType == null) {
            result = symbolTable.search(currentLexeme);
            if(result == null){
                symbol = new Symbol(Token.ID, currentLexeme);
                result = symbolTable.insert(symbol);
            }
            else {
                symbol = result.symbol;
            }
        }
        else {
            symbol = new Symbol(Token.CONST_VALUE, currentLexeme);
        }
        return new LexicalRegister(result, symbol, constType, constSize);
    }

    private boolean isCharHexadecimal(char c) {
       return isCharDigit(c) || c >= 'A' && c <= 'F';
    }

    private boolean isCharDigit(char c) {
        return c>='0' && c<='9';
    }

    private boolean isCharLetter(char c){
        return (c >= 'a' && c<='z') || (c>='A' && c<='Z');
    }

    private boolean verifyIsValidCharacter(char c){
        return acceptedCharacters.contains(c);
    }
}

class LexicalRegister {
    SymbolTableSearchResult symbolInTable;
    Symbol symbol;
    ConstType constType;
    Integer size;

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

class CodeReader {
    InputStreamReader reader  = new InputStreamReader(System.in);

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

public class Compiler {
    public static void main(String[] args) {
        try{
            SyntaxAnalyzer syntaxAnalyzer = configureSyntaxAnalyzer();
            syntaxAnalyzer.startSyntaxAnalyzer();
//            syntaxAnalyzer.testLexicalAnalyzer();
        }
        catch (CompilerError compilerError){
            compilerError.print();
        }
    }

    public static SyntaxAnalyzer configureSyntaxAnalyzer() throws CompilerError{
        CodeReader codeReader = new CodeReader();
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(codeReader);

        return new SyntaxAnalyzer(lexicalAnalyzer);
    }
}

