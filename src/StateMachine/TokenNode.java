package StateMachine;

/**
 * Created by Gabriel Brolo on 01/09/2017.
 */
public class TokenNode {
    private String ident;
    private String value;

    public TokenNode (String ident, String value) {
        this.ident = ident;
        this.value = value;
    }

    public String getIdent() { return this.ident; }
    public String getValue() { return this.value; }
    public String toString() {
        return "<" + ident + " = \""+ value + "\"" + ">";
    }
}
