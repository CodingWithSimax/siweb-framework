package net.simax_dev.siweb.managers.dependency_injection;

import net.simax_dev.siweb.WebApplication;
import net.simax_dev.siweb.annotations.Component;
import net.simax_dev.siweb.annotations.Service;
import net.simax_dev.siweb.annotations.StaticService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.*;

public class DependencyLoader {
    private static final Logger logger = LogManager.getLogger(DependencyLoader.class.getName());

    private final WebApplication webApplication;

    private final Set<Class<?>> dependencies = new HashSet<>();

    private final List<Class<?>> services = new ArrayList<>();
    private final List<Class<?>> staticServices = new ArrayList<>();
    private final Map<Class<?>, Object> serviceInstances = new HashMap<>();
    private final Set<Class<?>> components = new HashSet<>();


    public DependencyLoader(WebApplication webApplication) {
        this.webApplication = webApplication;
    }

    public void loadDependencies(Class<?> ...classes) {
        for (Class<?> aClass : classes) {
            this.loadDependency(aClass);
        }
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
        logger.debug("Loading dependencies...");

        // initialise dependencies
        for (Class<?> dependency : this.dependencies) {
            if (dependency.isAnnotationPresent(Component.class)) {
                logger.debug("Initialising component: " + dependency.getName());
                this.components.add(dependency);
                this.loadService(dependency, new ArrayList<>(), false);
            }
            if (dependency.isAnnotationPresent(Service.class)) {
                this.loadService(dependency, new ArrayList<>(), true);
            }
            if (dependency.isAnnotationPresent(StaticService.class)) {
                this.loadStaticService(dependency, new ArrayList<>());
            }
        }

        List<String> serviceNames = new ArrayList<>();
        for (Class<?> service : this.services) {
            serviceNames.add(service.getSimpleName());
        }

        logger.debug("preloaded non-static services: " + String.join(", ", serviceNames));

        List<String> staticServiceNames = new ArrayList<>();
        for (Class<?> staticService : this.staticServices) {
            staticServiceNames.add(staticService.getSimpleName());
        }

        logger.debug("initialising static services: " + String.join(", ", staticServiceNames));

        for (Class<?> staticService : this.staticServices) {
            List<Object> args = new ArrayList<>();
            for (Class<?> parameterType : staticService.getConstructors()[0].getParameterTypes()) {
                args.add(this.serviceInstances.get(parameterType));
            }

            try {
                this.serviceInstances.put(staticService, staticService.getConstructors()[0].newInstance(args.toArray()));
            } catch (Exception e) {
                logger.error("Could not initialise static service: " + staticService.getName(), e);
                throw new RuntimeException(e);
            }
        }
    }

    private void loadService(Class<?> service, List<Class<?>> initOrder, boolean registerAsService) {
        if (this.services.contains(service)) {
            return;
        }

        logger.debug("Loading service: " + service.getName());

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
            if (reqDependency.isAnnotationPresent(Service.class)) {
                if (registerAsService) initOrder.add(service);
                this.loadService(reqDependency, initOrder, true);
            } else if (reqDependency.isAnnotationPresent(StaticService.class)) {
                this.loadStaticService(reqDependency, initOrder);
            } else {
                throw new RuntimeException("Service dependency not found: " + reqDependency.getName());
            }

        }

        if (!registerAsService) return;
        services.add(service);
    }

    private void loadStaticService(Class<?> service, List<Class<?>> initOrder) {
        if (this.staticServices.contains(service)) {
            return;
        }

        logger.debug("Loading service: " + service.getName());

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
            if (!reqDependency.isAnnotationPresent(StaticService.class)) {
                throw new RuntimeException("Service dependency not found or does not have StaticService annotation: " + reqDependency.getName());
            }
            initOrder.add(service);
            this.loadStaticService(reqDependency, initOrder);
        }

        staticServices.add(service);
    }
}
