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
    final int size = 1000;
    ArrayList<Symbol>[] table;

    SymbolTable() {
        table = new ArrayList[size];
        startTable();
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

        return new SymbolTableSearchResult(positionInHash, addedIndex);
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
                if(lexemeList.get(i).lexeme.equals(lexeme)) {
                    return new SymbolTableSearchResult(positionInHash, i);
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

    SymbolTableSearchResult(int positionInHash, int positionInArrayList) {
        this.positionInHash = positionInHash;
        this.positionInArrayList = positionInArrayList;
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
    private Code code;
    private final int finalState = 4;
    private Character lastCharacter = null;


    private static final HashSet<Character> acceptedCharacters =  new HashSet<Character>(Arrays.asList( '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z','a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',' ', '_', '.', ',', ';', ':', '(', ')',
            '[', ']', '{', '}', '+', '-', '\"','\'','/', '|', '\\', '&', '%', '!', '?', '>', '<', '=', '\n', '\r' ));


    public LexicalAnalyzer(Code code) {
        this.code = code;
    }

    public LexicalRegister getNextToken() throws CompilerError {
        int currentState = 0;
        char currentCharacter;
        String currentLexeme = "";

        if(lastCharacter == null) {
            currentCharacter = code.code.charAt(position);
        }
        else{
            currentCharacter = lastCharacter;
            position--;
        }

        while(currentState != finalState && position < code.code.length()) {
            if(!verifyIsValidCharacter(currentCharacter)){
                throw new CompilerError("caractere invalido.", currentLine);
            }

            if(currentCharacter == '\n'){
                currentLine++;
            }

            switch (currentState) {

            }
            position++;
            currentCharacter = code.code.charAt(position);
        }

        return null;
    }

    private boolean verifyIsValidCharacter(char c){
        return acceptedCharacters.contains(c);
    }
}

class LexicalRegister {
    Token token;
    String lexeme;
    SymbolTableSearchResult positionInTable;
    ConstType constType;
    int size;

    public LexicalRegister(Token token, String lexeme, SymbolTableSearchResult positionInTable, ConstType constType, int size) {
        this.token = token;
        this.lexeme = lexeme;
        this.positionInTable = positionInTable;
        this.constType = constType;
        this.size = size;
    }

    @Override
    public String toString() {
        return "LexicalRegister{" +
                "token=" + token +
                ", lexeme='" + lexeme + '\'' +
                ", positionInTable=" + positionInTable +
                ", constType=" + constType +
                ", size=" + size +
                '}';
    }

    public void print() {
        System.out.println(this);
    }
}

class Code {
    String code = "";
    int numOfLines = 1;

    public void read(){
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
        Code code = new Code();
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzer(code);

        return new SyntaxAnalyzer(lexicalAnalyzer);
    }
}

