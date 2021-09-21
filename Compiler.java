import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

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
    LEFT_BRACE("{"), // {
    RIGHT_BRACE("}"),// }
    READ_LINE("readln"),
    DIV("div"),
    WRITE("write"),
    WRITE_LINE("writeln"),
    MOD("mod"),
    LEFT_SQUARE_BRACKET("["),//[
    RIGHT_SQUARE_BRACKET("]"),//]
    CONST("const"),
    CONST_VALUE(null),
    ID(null);

    public final String name;

    Token(String name) {
        this.name = name;
    }
}

enum ConstType {
    INT,
    HEX,
    CHAR,
    FLOAT,
    STRING;
}
/*
 * Size das constantes
 * int = 4
 * char = 1
 * float = 4
 * string = length da string
 * hex = 1
 * */
class Constants {
    public static int DecimalPrecision = 6;
    public static int IntBytesSize = 4;
    public static int CharBytesSize = 1;
    public static int FloatBytesSize = 4;
    public static int HexBytesSize = 1;
}

class CompilerError extends Throwable {
    private final int line;
    private final String message;

    public CompilerError(String message, int line) {
        this.line = line;
        this.message = message;
    }

    @Override
    public String toString() {
        return line+"\n"+message+"\n";
    }

    public void print() {
        System.out.print(this);
    }
}

class SymbolTable {
    private static SymbolTable instance;
    final int size = 1000;
    ArrayList<Symbol>[] table;

    private SymbolTable() {
        table = new ArrayList[size];
        startTable();
    }

    public static SymbolTable getInstance() {
        if(instance == null) {
            instance = new SymbolTable();
        }
        return instance;
    }

    public void startTable() {
        Token[] tokensArray = Token.values();

        for(Token token : tokensArray) {
            if(token.name != null) {
                Symbol symbolToBeAdded = new Symbol(token, token.name);
                insert(symbolToBeAdded);
            }
        }
    }

    public SymbolTableSearchResult insert(Symbol symbol) {
        int positionInHash= hash(symbol.lexeme);

        if (table[positionInHash] == null) {
            table[positionInHash] = new ArrayList<>();
        }

        int addedIndex = table[positionInHash].size();
        table[positionInHash].add(symbol);

        return new SymbolTableSearchResult(positionInHash, addedIndex, symbol);
    }

    public int hash(String lexeme) {
        int n = 0;
        for (int i = 0; i < lexeme.length(); i++) {
            n += lexeme.charAt(i);
        }
        return n % size;
    }

    public SymbolTableSearchResult search(String lexeme) {
        int positionInHash = hash(lexeme);
        ArrayList<Symbol> lexemeList = table[positionInHash];
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

    public void printTable() {
        for (ArrayList<Symbol> symbols: table) {
            if(symbols != null)
                System.out.println(symbols);
        }
    }
}

class SymbolTableSearchResult {
    int positionInHash;
    int positionInArrayList;
    Symbol symbol;

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

class Symbol {
    Token tokenType;
    String lexeme;

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

    public SyntaxAnalyzer(LexicalAnalyzer lexicalAnalyzer) throws CompilerError {
        this.lexicalAnalyzer = lexicalAnalyzer;
//        testLexicalAnalyzer();
    }

    public void startSyntaxAnalyzer() throws CompilerError {
        currentRegister = lexicalAnalyzer.getNextToken();
        start();
        System.out.println(lexicalAnalyzer.currentLine + " linhas compiladas");
    }

    private void start() throws CompilerError {
        if(currentRegister == null){
            return;
        }
        boolean isOnDeclaration = isOnDeclarationFirst();
        boolean isOnBlockOrCommand = isOnBlockOrCommandFirst();
        while(isOnDeclaration || isOnBlockOrCommand){

            if(isOnDeclaration) {
                declaration();
                matchToken(Token.SEMICOLON);
            }
            else {
                blockOrCommand();
            }

            if(currentRegister == null){
                return;
            }

            isOnDeclaration = isOnDeclarationFirst();
            isOnBlockOrCommand = isOnBlockOrCommandFirst();
        }
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
            matchToken(Token.CONST_VALUE);
        }

    }

    private void declarationInit() throws CompilerError {
        matchToken(Token.ID);
        if(currentRegister.symbol.tokenType == Token.ATTRIBUTION){
            matchToken(Token.ATTRIBUTION);
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
            commandFat();
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
        else if(currentToken == Token.WRITE){
            matchToken(Token.WRITE);
            write();
            matchToken(Token.SEMICOLON);
        }
        else {
            matchToken(Token.WRITE_LINE);
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
        if(currentRegister.symbol.tokenType == Token.NEGATION){
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
            blockOrCommand();
        }
    }

    private void commandFat() throws CompilerError {
        if(currentRegister.symbol.tokenType == Token.ATTRIBUTION){
            matchToken(Token.ATTRIBUTION);
            expression();
        }
        else {
            matchToken(Token.LEFT_SQUARE_BRACKET);
            expression();
            matchToken(Token.RIGHT_SQUARE_BRACKET);
            matchToken(Token.ATTRIBUTION);
            expression();
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

    private boolean isOnBlockOrCommandFirst() {
        Token currentToken = currentRegister.symbol.tokenType;
        return isOnCommandFirst() || currentToken == Token.LEFT_BRACE;
    }

    private boolean isOnDeclarationFirst() {
        Token currentToken = currentRegister.symbol.tokenType;
        return isOnTypeFirst()|| currentToken == Token.CONST;
    }

    private void testLexicalAnalyzer() throws CompilerError {
        Reader reader = lexicalAnalyzer.reader;
        while(reader.position < reader.code.length()){
            LexicalRegister register = lexicalAnalyzer.getNextToken();
            if(register!=null)
                register.print();
        }
        System.out.println(lexicalAnalyzer.currentLine + " linhas compiladas");
    }

    private void matchToken(Token expectedToken) throws CompilerError {
        if(currentRegister.symbol.tokenType == expectedToken){
            currentRegister = lexicalAnalyzer.getNextToken();
        }
        else {
            throw new CompilerError("token nao esperado ["+currentRegister.symbol.lexeme+"].", lexicalAnalyzer.currentLine);
        }

    }
}

class LexicalAnalyzer {

    int currentLine = 1;
    final Reader reader;
    private Character lastCharacter = null;
    private final SymbolTable symbolTable = SymbolTable.getInstance();


    private static final HashSet<Character> acceptedCharacters = new HashSet<>(Arrays.asList('0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z','a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',' ', '_', '.', ',', ';', ':', '(', ')',
            '[', ']', '{', '}', '+', '-', '*', '\"','\'','/', '|', '\\', '&', '%', '!', '?', '>', '<', '=', '\n', '\r', '\0' ));


    public LexicalAnalyzer(Reader reader) {
        this.reader = reader;
        reader.readInputCode();
    }

    public LexicalRegister getNextToken() throws CompilerError {
        int currentState = 0;
        int numberOfDecimal = 0;
        Character currentCharacter;
        String currentLexeme = "";
        ConstType constType = null;
        Integer constSize = null;

        if(lastCharacter == null) {
            currentCharacter = reader.code.charAt(reader.position);
        }
        else if (lastCharacter == '\n' || lastCharacter == '\r') {
            currentCharacter = reader.code.charAt(reader.position);
            lastCharacter = null;
        }
        else{
            currentCharacter = lastCharacter;
            lastCharacter = null;
            reader.position--;
        }

        int finalState = 4;

        while(currentState != finalState && reader.position < reader.code.length()) {
            if(!verifyIsValidCharacter(currentCharacter)){
                throw new CompilerError("caractere invalido.", currentLine);
            }

            if(currentCharacter == '\n' || currentCharacter == '\r'){
                currentLine++;
            }

            if(currentCharacter == '\0' && currentState != 0) {
                throw new CompilerError("fim de arquivo nao esperado.", currentLine);
            }

            switch (currentState) {
                case 0:
                    if(currentCharacter == ' ' || currentCharacter == '\n' || currentCharacter == '\r') {
                        currentState = 0;
                        currentCharacter = null;
                    }
                    else if(currentCharacter == '0') {
                        currentState = 16;
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
                            currentCharacter == '}' || currentCharacter == '[' || currentCharacter == ']'){
                        currentState = 4;
                    }
                    else if(currentCharacter >= '1' && currentCharacter <= '9'){
                        currentState = 10;
                    }
                    else if(currentCharacter == '|') {
                        currentState = 5;
                    }
                    else if(currentCharacter == '-'){
                        currentState = 12;
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
                    }
                    else if(currentCharacter == '\0'){
                        currentCharacter = null;
                    }
                    else {
                        throw new CompilerError("lexema nao identificado [" + currentLexeme+currentCharacter + "]", currentLine);
                    }
                    break;
                case 1:
                    if(currentCharacter == '*') {
                        currentState = 2;
                        currentCharacter = null;
                        currentLexeme = "";
                    }
                    else {
                        lastCharacter = currentCharacter;
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
                    else {
                        currentCharacter = null;
                        currentState = 2;
                    }
                    break;
                case 5:
                    if(currentCharacter == '|'){
                        currentState = 4;
                    }
                    break;
                case 6:
                    currentState = 4;
                    if (currentCharacter != '=') {
                        lastCharacter = currentCharacter;
                    }
                    break;
                case 7:
                    currentState  = 4;
                    if (currentCharacter != '-' && currentCharacter != '=') {
                        lastCharacter = currentCharacter;
                    }
                    break;
                case 8:
                    if(currentCharacter != '=') {
                        lastCharacter = currentCharacter;
                    }
                    currentState = 4;
                    break;
                case 9:
                    if(currentCharacter == '&') {
                        currentState = 4;
                    } else {
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "]", currentLine);
                    }
                    break;
                case 10:
                    if(isCharDigit(currentCharacter)) {
                        currentState = 10;
                    }
                    else if(currentCharacter =='.') {
                        currentState = 11;
                    }
                    else {
                        lastCharacter = currentCharacter;
                        constType = ConstType.INT;
                        constSize = Constants.IntBytesSize;
                        currentState = 4;
                    }
                    break;
                case 11:
                    if(numberOfDecimal > Constants.DecimalPrecision){
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "]", currentLine);
                    }
                    else if(isCharDigit(currentCharacter)) {
                        currentState = 11;
                        numberOfDecimal++;
                    }
                    else {
                        lastCharacter = currentCharacter;
                        constType = ConstType.FLOAT;
                        constSize = Constants.FloatBytesSize;
                        currentState = 4;
                    }
                    break;
                case 12:
                    if(isCharDigit(currentCharacter)) {
                        currentState = 10;
                    }
                    else {
                        lastCharacter = currentCharacter;
                        currentState = 4;
                    }
                    break;
                case 13:
                    currentState = 14;
                    break;
                case 14:
                    if(currentCharacter == '\'') {
                        currentState = 4;
                    } else {
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "]", currentLine);
                    }
                    break;
                case 15:
                    if(currentCharacter == '\"') {
                        currentState = 4;
                        constType = ConstType.STRING;
                        constSize = currentLexeme.length()-1;
                    } else if(currentCharacter == '\n' || currentCharacter == '\r' || currentCharacter == '$') {
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "]", currentLine);
                    }
                    break;
                case 16:
                    if(currentCharacter == 'x'){
                        currentState = 17;
                    }
                    else if(isCharDigit(currentCharacter)){
                        currentState = 10;
                    }
                    else {
                        currentState = 4;
                        lastCharacter = currentCharacter;
                        constType = ConstType.INT;
                        constSize = Constants.IntBytesSize;
                    }
                    break;
                case 17:
                    if(isCharHexadecimal(currentCharacter)){
                        currentState = 18;
                    }
                    else {
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "]", currentLine);
                    }
                    break;
                case 18:
                    if(isCharHexadecimal(currentCharacter)){
                        constType = ConstType.HEX;
                        constSize = Constants.HexBytesSize;
                        currentState = 4;
                    }
                    else {
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "]", currentLine);
                    }
                    break;
                case 19:
                    if(!isCharLetter(currentCharacter) && !isCharDigit(currentCharacter) && currentCharacter != '_' && currentCharacter != '.') {
                        lastCharacter = currentCharacter;
                        currentState = 4;
                    }
                    break;
                default:
            }

            if(currentCharacter != null && lastCharacter == null)
                currentLexeme += currentCharacter;
            reader.position++;
            if(reader.position < reader.code.length())
                currentCharacter = reader.code.charAt(reader.position);
        }

        if(currentLexeme.isEmpty())
            return null;

        return createLexicalRegister(currentLexeme, constType, constSize);
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

class Reader {
    String code = "";
    int numOfLines = 1;
    int position = 0;

    public void readInputCode(){
        Scanner scanner = new Scanner(System.in);
        String currentLine = "";

        while(scanner.hasNextLine()&&(currentLine = scanner.nextLine()) != null) {
            code +=currentLine + '\n';
            numOfLines++;
        }
        code+='\0';
        scanner.close();
    }
}

public class Compiler {
    public static void main(String[] args) {
        try{
            SyntaxAnalyzer syntaxAnalyzer = configureSyntaxAnalyzer();
            syntaxAnalyzer.startSyntaxAnalyzer();
        }
        catch (CompilerError compilerError){
            compilerError.print();
        }

    }

    public static SyntaxAnalyzer configureSyntaxAnalyzer() throws CompilerError{
        Reader reader = new Reader();
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(reader);

        return new SyntaxAnalyzer(lexicalAnalyzer);
    }
}

