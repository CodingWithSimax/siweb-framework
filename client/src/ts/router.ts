class Router {
    private curURL!: string;

    constructor(private application: Application) {
        this.loadURL();
    }

    public getURL(): string {
        return this.curURL;
    }

    private loadURL() {
        this.curURL = window.location.pathname;
    }

    public openURL(url: string) {
        if (!url.startsWith("/")) url = "/" + url;

        window.history.pushState("", "", url);
        this.curURL = url;
    }
}