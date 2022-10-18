package deck.wrapper;

import android.content.Context;

/*
 * A wrapper which holds Android application context.
 * A instance of this class will be a parameter in developer's run method, and will also be
 * used in Android side code in reflection.
 * TODO: More details in ... Android.DeviceTask.loadDexFile.loadMethod...
 */
public class ContextWrapper {
    private final Context context;

    public ContextWrapper(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
