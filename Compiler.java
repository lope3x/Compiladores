import java.util.ArrayList;

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

public class Compiler {
    public static void main(String args[]) {
        SymbolTable st = new SymbolTable();
        st.printTable();
        st.insert(new Symbol(Token.ELSE, "sele"));
        st.search("sele").print();
    }
}

