package piedmontlaunch.org.highprecisioncalculator;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

/**
 * A subclass of Button that contains a string to be added to the expression
 * being built
 */

public class CalcButton extends AppCompatButton {

    //Text to be added to the expression
    private String exprText;

    //Constructor
    public CalcButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //exprText getter
    public String getExprText() {
        return exprText;
    }

    //exprText setter
    public void setExprText(String et) {
        exprText = et;
    }
}
