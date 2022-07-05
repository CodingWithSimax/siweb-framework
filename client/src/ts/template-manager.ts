class TemplateManager {
    private templateVars: string[] = [];

    constructor(private application: Application) {
        this.loadTemplate();
    }

    private async loadTemplate() {
        const url = this.application.router.getURL();

        // getting vars
        const response: string[] = await (await fetch(`/api/vars${url}`)).json();

        this.templateVars = response;

        this.applyVars();
    }

    private applyVars() {
        this.templateVars.forEach(l => {
            const element = document.getElementById(`var-${l}`);
            if (element !== null) {

            }
        });
    }
}