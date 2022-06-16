package net.simax_dev.siweb.exceptions;

public class ParentDependencyInjectionNotLoaded extends RuntimeException {
    public ParentDependencyInjectionNotLoaded(String message) {
        super(message);
    }
}
