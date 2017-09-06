package piedmontlaunch.org.highprecisioncalculator;

import android.support.v4.app.Fragment;

/**
 * A class that holds a single CalcFragment
 */
public class CalcActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new CalcFragment();
    }
}
