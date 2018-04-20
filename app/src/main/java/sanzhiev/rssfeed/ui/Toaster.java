package sanzhiev.rssfeed.ui;

import android.content.Context;
import android.widget.Toast;

public class Toaster {
    private Toaster() {
        throw new UnsupportedOperationException();
    }

    public static void makeShortToast(final Context context, final String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
