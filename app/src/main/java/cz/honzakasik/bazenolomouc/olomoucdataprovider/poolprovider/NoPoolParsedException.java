package cz.honzakasik.bazenolomouc.olomoucdataprovider.poolprovider;

/**
 * This exception is thrown when no pool was parsed
 */
public class NoPoolParsedException extends Exception {

    public NoPoolParsedException(String message) {
        super(message);
    }
}
