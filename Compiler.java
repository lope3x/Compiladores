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
    ATRIBUITION("<-"),
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

class CompilerError extends Throwable {
    private int line;
    private String message;

    public CompilerError(String message, int line) {
        this.line = line;
        this.message = message;
    }

    @Override
    public String toString() {
        return line+'\n'+message+'\n';
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
            table[positionInHash] = new ArrayList<Symbol>();
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
                System.out.println(symbols.toString());
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
    }
}

class LexicalAnalyzer {

    private int position = 0;
    private int currentLine = 1;
    private Reader reader;
    private final int finalState = 4;
    private Character lastCharacter = null;
    private SymbolTable symbolTable = SymbolTable.getInstance();


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
        char currentCharacter;
        String currentLexeme = "";
        ConstType constType = null;
        Integer constSize = null;

        if(lastCharacter == null) {
            currentCharacter = reader.code.charAt(position);
        }
        else{
            currentCharacter = lastCharacter;
            position--;
        }

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
                    break;
                case 4:
                    SymbolTableSearchResult result = null;
                    if(constType == null) {
                        result = symbolTable.search(currentLexeme);
                        if(result == null){
                            result = symbolTable.insert(new Symbol(Token.ID, currentLexeme));
                        }
                    }
                    return new LexicalRegister(result, constType, constSize);
                default:
            }
            currentLexeme += currentCharacter;
            position++;
            currentCharacter = reader.code.charAt(position);
        }

        return null;
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
    ConstType constType;
    Integer size;

    public LexicalRegister(SymbolTableSearchResult symbolInTable, ConstType constType, Integer size) {
        this.symbolInTable = symbolInTable;
        this.constType = constType;
        this.size = size;
    }

    @Override
    public String toString() {
        return "LexicalRegister{" +
                "symbolInTable=" + symbolInTable +
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

