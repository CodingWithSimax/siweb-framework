package net.simax_dev.siweb.objects;

import net.simax_dev.siweb.managers.dependency_injection.DependencyInjection;

public class UserDependencyInjection {
    private final DependencyInjection internServices;
    private final DependencyInjection services;

    public UserDependencyInjection(
        DependencyInjection internServices,
        DependencyInjection services
    ) {
        this.internServices = internServices;
        this.services = services;
    }

    public void destroy() {

    }
}
