package net.simax_dev.siweb.exceptions;

public class DependencyInjectionNotLoaded extends RuntimeException {
    public DependencyInjectionNotLoaded(String message) {
        super(message);
    }
}
