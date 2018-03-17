package utils.initializers;

import utils.Initializers;

public abstract class InitializerParent implements Initializer {
    private Initializers initializers;

    @Override
    public final void setInitializers(Initializers initializers) {
       this.initializers = initializers;
    }

    protected Initializers getInitializers() {
        return initializers;
    }
}
