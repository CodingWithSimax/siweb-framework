package net.simax_dev.siweb.managers.dependency_injection;

import net.simax_dev.siweb.WebApplication;
import net.simax_dev.siweb.annotations.Component;
import net.simax_dev.siweb.annotations.InternService;
import net.simax_dev.siweb.annotations.Service;
import net.simax_dev.siweb.annotations.StaticService;
import net.simax_dev.siweb.objects.UserDependencyInjection;
import net.simax_dev.siweb.services.NetworkClient;
import net.simax_dev.siweb.services.NetworkClientInformation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.*;

public class DependencyLoader {
    private static final Logger logger = LogManager.getLogger(DependencyLoader.class.getName());

    private final WebApplication webApplication;

    private final Set<Class<?>> dependencies = new HashSet<>();
    private final Set<Class<?>> components = new HashSet<>();
    private final DependencyInjection staticServices;
    private final DependencyInjection internServices;
    private final DependencyInjection services;


    public DependencyLoader(WebApplication webApplication) {
        this.webApplication = webApplication;

        this.staticServices = new DependencyInjection(StaticService.class);
        this.internServices = new DependencyInjection(InternService.class, this.staticServices);
        this.services = new DependencyInjection(Service.class, this.internServices);
    }

    public void loadDependencies(Class<?> ...classes) {
        for (Class<?> aClass : classes) {
            this.loadDependency(aClass);
        }
    }
    public Set<Class<?>> getComponents() {
        return this.components;
    }
    public void loadDependencies(Set<Class<?>> classes) {
        for (Class<?> aClass : classes) {
            this.loadDependency(aClass);
        }
    }
    public void loadDependencies(Package pkg) {
        // load all classes in package (using reflections)
        Reflections reflections = new Reflections(pkg.getName(), Scanners.SubTypes.filterResultsBy((data) -> true));
        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
        this.loadDependencies(classes);
    }
    public void loadDependency(Class<?> clazz) {
        this.dependencies.add(clazz);
    }

    public void load() {
        // extract all components
        this.dependencies.forEach((clazz) -> {
            if (clazz.isAnnotationPresent(Component.class)) {
                this.components.add(clazz);
            }
        });

        this.staticServices.loadDependencies(this.dependencies);

        this.internServices.loadDependency(NetworkClient.class);

        this.services.loadDependencies(this.dependencies);

        this.staticServices.load(new HashSet<>());
        this.internServices.load(new HashSet<>() {{
            add(NetworkClientInformation.class);
        }});
        this.services.load(new HashSet<>());

        this.staticServices.instantiateDependencies();
    }

    public UserDependencyInjection loadUser() {
        DependencyInjection internServices = this.internServices.clone();
        internServices.instantiateDependencies(new HashMap<>() {{
            put(NetworkClientInformation.class, new NetworkClientInformation("http://localhost:8080", "http://localhost:8080"));
        }});
        DependencyInjection services = this.services.clone(internServices);
        services.instantiateDependencies();

        return new UserDependencyInjection(
                internServices,
                services
        );
    }
}
