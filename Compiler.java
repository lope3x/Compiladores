import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

enum Token {
    INT("int"),
    CHAR("char"),
    WHILE("while"),
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
    ID("");

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
            if(token != Token.ID) {
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

    public SyntaxAnalyzer(LexicalAnalyzer lexicalAnalyzer) throws CompilerError {
        this.lexicalAnalyzer = lexicalAnalyzer;

        //Teste
        LexicalRegister register = lexicalAnalyzer.getNextToken();
        register.print();
        register = lexicalAnalyzer.getNextToken();
        register.print();
        register = lexicalAnalyzer.getNextToken();
        register.print();
        register = lexicalAnalyzer.getNextToken();
        register.print();
        register = lexicalAnalyzer.getNextToken();
        register.print();
    }
}

class LexicalAnalyzer {

    private int position = 0;
    private int currentLine = 1;
    private final Reader reader;
    private Character lastCharacter = null;
    private final SymbolTable symbolTable = SymbolTable.getInstance();


    private static final HashSet<Character> acceptedCharacters =  new HashSet<Character>(Arrays.asList( '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z','a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',' ', '_', '.', ',', ';', ':', '(', ')',
            '[', ']', '{', '}', '+', '-', '\"','\'','/', '|', '\\', '&', '%', '!', '?', '>', '<', '=', '\n', '\r' ));


    public LexicalAnalyzer(Reader reader) {
        this.reader = reader;
        reader.readInputCode();
    }

    public LexicalRegister getNextToken() throws CompilerError {
        int currentState = 0;
        int numberOfDecimal = 0;
        char currentCharacter;
        String currentLexeme = "";
        ConstType constType = null;
        Integer constSize = null;

        if(lastCharacter == null || lastCharacter == '\n') {
            currentCharacter = reader.code.charAt(position);
        }
        else{
            currentCharacter = lastCharacter;
            lastCharacter = null;
            position--;
        }

        int finalState = 4;
        while(currentState != finalState && position < reader.code.length()) {
            if(!verifyIsValidCharacter(currentCharacter)){
                throw new CompilerError("caractere invalido.", currentLine);
            }

            if(currentCharacter == '\n'){
                currentLine++;
            }

            switch (currentState) {
                case 0:
                    if(currentCharacter == ' ' || currentCharacter == '\n' || currentCharacter == '\r') {
                        currentState = 0;
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
                    else {
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
                case 16:
                    if(currentCharacter == 'x'){
                        currentState = 17;
                    }
                    else if(isCharDigit(currentCharacter)){
                        currentState = 10;
                    }
                    else {
                        throw new CompilerError("lexema nao identificado [" + currentLexeme + "]", currentLine);
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
                default:
            }
            if(currentState != 0 && currentCharacter != ' ' && lastCharacter == null)
                currentLexeme += currentCharacter;
            position++;
            currentCharacter = reader.code.charAt(position);
        }
        //Final State

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
            symbol = new Symbol(Token.CONST, currentLexeme);
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

    public void readInputCode(){
        Scanner scanner = new Scanner(System.in);
        String currentLine = "";

        while(scanner.hasNextLine()&&(currentLine = scanner.nextLine()) != null) {
            code+=currentLine + '\n';
            numOfLines++;
        }
        code +="\0";
        scanner.close();
    }
}

public class Compiler {
    public static void main(String[] args) {
        try{
            SyntaxAnalyzer syntaxAnalyzer = configureSyntaxAnalyzer();
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

