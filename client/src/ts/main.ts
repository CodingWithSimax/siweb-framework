class Application {
    public readonly router: Router;
    public readonly templateManager: TemplateManager;

    constructor() {
        this.router = new Router(this);
        this.templateManager = new TemplateManager(this);
    }

}

const siwebApplication = new Application();
