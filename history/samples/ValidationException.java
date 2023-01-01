
public class ValidationException extends RuntimeException {

    public ValidationException(String s) {
        super(s);
    }


    public ValidationException(String s, Throwable thrwble) {
        super(s, thrwble);
    }

}

