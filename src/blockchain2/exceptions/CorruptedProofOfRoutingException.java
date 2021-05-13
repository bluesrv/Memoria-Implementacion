package blockchain2.exceptions;

public class CorruptedProofOfRoutingException extends Exception{
    public CorruptedProofOfRoutingException(){
        super();
    }

    public CorruptedProofOfRoutingException(final String msg) {
        super(msg);
    }

    public CorruptedProofOfRoutingException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}