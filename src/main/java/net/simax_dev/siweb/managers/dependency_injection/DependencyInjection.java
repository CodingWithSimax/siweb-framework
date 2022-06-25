package net.simax_dev.siweb.managers.dependency_injection;

import net.simax_dev.siweb.annotations.Service;
import net.simax_dev.siweb.annotations.StaticService;
import net.simax_dev.siweb.events.OnAfterInit;
import net.simax_dev.siweb.events.OnDestroy;
import net.simax_dev.siweb.events.OnInit;
import net.simax_dev.siweb.exceptions.DependencyInjectionException;
import net.simax_dev.siweb.exceptions.DependencyInjectionNotLoaded;
import net.simax_dev.siweb.exceptions.ParentDependencyInjectionNotLoaded;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Create custom dependency injection
 */
public class DependencyInjection {
    private final static Logger logger = LogManager.getLogger(DependencyInjection.class.getName());

    // both attributes are including the same services, while "services" will be sorted in right order
    private final Set<Class<?>> dependencies = new HashSet<>();
    private final List<Class<?>> services = new ArrayList<>();
    private final Map<Class<?>, Object> serviceObjects = new HashMap<>();
    private final Class<? extends Annotation> annotation;
    private final DependencyInjection parent;
    private boolean loaded;
    private boolean instantiated;

    public DependencyInjection(Class<? extends Annotation> annotation) {
        this(annotation, null);
    }
    public DependencyInjection(Class<? extends Annotation> annotation, DependencyInjection parent) {
        this.annotation = annotation;
        this.parent = parent;
    }
    private DependencyInjection(Class<? extends Annotation> annotation, DependencyInjection parent, Set<Class<?>> dependencies, List<Class<?>> services, boolean loaded) {
        this(annotation, parent);
        this.loaded = loaded;
        this.dependencies.addAll(dependencies);
        this.services.addAll(services);
    }

    public DependencyInjection getParent() {
        return parent;
    }
    public Class<? extends Annotation> getAnnotation() {
        return annotation;
    }
    public List<Class<?>> getServices() {
        return services;
    }
    public Object getServiceObject(Class<?> clazz) {
        return serviceObjects.get(clazz);
    }
    public boolean isLoaded() {
        return loaded;
    }
    public boolean isInstantiated() {
        return instantiated;
    }

    /**
     * Load all dependencies given from a class set --> annotation needs to be present on the class
     * @param classes classes to load
     */
    public void loadDependencies(Set<Class<?>> classes) {
        for (Class<?> aClass : classes) {
            this.loadDependency(aClass);
        }
    }

    /**
     * Load all classes in a package with the used annotation (using reflections)
     * @param pkg the package object, can be obtained by Class.getPackage()
     */
    public void loadDependencies(Package pkg) {
        // load all classes in package (using reflections)
        Reflections reflections = new Reflections(pkg.getName(), Scanners.SubTypes.filterResultsBy((data) -> true));
        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
        this.loadDependencies(classes);
    }

    /**
     * Load a single dependency -> Needs to have the specified annotation
     * @param clazz the class to load
     */
    public void loadDependency(Class<?> clazz) {
        this.dependencies.add(clazz);
    }

    /**
     * Load all imported dependencies
     */
    public void load(Set<Class<?>> whitelistedClasses) {
        logger.debug("Loading dependencies for " + this.annotation.getSimpleName() + "...");

        if (this.parent != null && !parent.isLoaded()) {
            throw new ParentDependencyInjectionNotLoaded("Parent dependency injection was not loaded: " + this.parent.getAnnotation().getSimpleName());
        }

        // initialise dependencies
        for (Class<?> dependency : this.dependencies) {
            if (dependency.isAnnotationPresent(this.annotation)) {
                this.loadInjectable(dependency, new ArrayList<>(), whitelistedClasses);
            }
        }

        this.loaded = true;

        logger.debug("Dependencies for " + this.annotation.getSimpleName() + " loaded.");
    }

    private void loadInjectable(Class<?> service, List<Class<?>> initOrder, Set<Class<?>> whitelistedClasses) {
        if (this.services.contains(service)) {
            return;
        }

        logger.debug("loading " + this.annotation.getSimpleName() + ": " + service.getName());

        // check if class is already in init order
        if (initOrder.contains(service)) {
            List<String> names = new ArrayList<>();
            for (Class<?> aClass : initOrder) {
                names.add(aClass.getSimpleName());
            }
            names.add(service.getSimpleName());

            throw new RuntimeException("Circular dependency detected: " + String.join(" -> ", names));
        }

        Class<?>[] reqDependencies = service.getConstructors()[0].getParameterTypes();

        for (Class<?> reqDependency : reqDependencies) {
            if (reqDependency.isAnnotationPresent(this.annotation)) {
                initOrder.add(service);
                this.loadInjectable(reqDependency, initOrder, whitelistedClasses);
            } else if (this.parent != null && reqDependency.isAnnotationPresent(this.parent.getAnnotation()) && this.parent.getServices().contains(reqDependency)) {
                // in case it is a dependency from the parent, we will simply ignore it
            } else if (whitelistedClasses.contains(reqDependency)) {
                // in case it is whitelisted, but not found, we will also ignore this
            } else {
                throw new RuntimeException("Service dependency not found: " + reqDependency.getName());
            }

        }

        this.services.add(service);
    }

    private Object instantiate(Class<?> clazz, Map<Class<?>, Object> args) {
        Constructor<?> constructor = clazz.getConstructors()[0];
        Parameter[] reqDependencies = constructor.getParameters();
        Object[] dependencies = new Object[reqDependencies.length];

        for (int i = 0; i < dependencies.length; i++) {
            Class<?> reqDependency = reqDependencies[i].getType();
            if (reqDependency.isAnnotationPresent(this.annotation)) {
                dependencies[i] = this.serviceObjects.get(reqDependency);
            } else if (this.parent != null && reqDependency.isAnnotationPresent(this.parent.getAnnotation())) {
                Object object = this.parent.getServiceObject(reqDependency);
                if (object != null) {
                    dependencies[i] = object;
                } else {
                    throw new RuntimeException("Service parent dependency not found: " + reqDependency.getName());
                }
            } else if (args.containsKey(reqDependency)) {
                dependencies[i] = args.get(reqDependency);
            } else {
                throw new RuntimeException("Service dependency not found: " + reqDependency.getName());
            }
        }

        try {
            return constructor.newInstance(dependencies);
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate service: " + clazz.getName(), e);
        }
    }

    /**
     * Instantiate all loaded dependencies
     */
    public void instantiateDependencies(Map<Class<?>, Object> args) {
        if (!this.loaded) {
            throw new DependencyInjectionNotLoaded("Dependency injection was not loaded: " + this.annotation.getSimpleName());
        }
        if (this.parent != null && !this.parent.isInstantiated()) {
            throw new ParentDependencyInjectionNotLoaded("Parent dependency injection was not instantiated: " + this.parent.getAnnotation().getSimpleName());
        }

        for (Class<?> service : this.services) {
            Object object = this.instantiate(service, args);

            if (object instanceof OnInit) {
                ((OnInit) object).onInit();
            }

            this.serviceObjects.put(service, object);
        }

        for (Object value : this.serviceObjects.values()) {
            if (value instanceof OnAfterInit) {
                ((OnAfterInit) value).onAfterInit();
            }
        }

        this.instantiated = true;
    }
    public void instantiateDependencies() {
        this.instantiateDependencies(new HashMap<>());
    }

    /**
     * Clone a dependency injection
     * @param parent the parent dependency injection, can be null, cloned etc.
     * @return new dependency injection with same attributes, but without stored objects
     */
    public DependencyInjection clone(DependencyInjection parent) {
        if (this.parent == null || !this.parent.getAnnotation().equals(parent.getAnnotation())) {
            throw new RuntimeException("Parent dependency injection does not match: " + this.annotation.getSimpleName());
        }
        return new DependencyInjection(this.annotation, parent, this.dependencies, this.services, this.loaded);
    }
    public DependencyInjection clone() {
        return this.clone(this.parent);
    }

    public void destroy() {
        for (Object value : this.serviceObjects.values()) {
            if (value instanceof OnDestroy) {
                ((OnDestroy) value).onDestroy();
            }
        }
    }
}
