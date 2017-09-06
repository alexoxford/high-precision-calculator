package piedmontlaunch.org.highprecisioncalculator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.operator.Operator;

import java.util.ArrayList;
import java.util.List;

/**
 * A subclass of Fragment to handle all calculator and UI
 * actions
 */
public class CalcFragment  extends Fragment {

    //Display, up & down Buttons, clear & backspace Buttons
    private TextView mDisplay;
    private Button mButtonUp;
    private Button mButtonDown;
    private Button mButtonBack;
    private Button mButtonClear;

    //Number, function, and constant Buttons
    private CalcButton mButton0;
    private CalcButton mButton1;
    private CalcButton mButton2;
    private CalcButton mButton3;
    private CalcButton mButton4;
    private CalcButton mButton5;
    private CalcButton mButton6;
    private CalcButton mButton7;
    private CalcButton mButton8;
    private CalcButton mButton9;
    private CalcButton mButtonPlus;
    private CalcButton mButtonMinus;
    private CalcButton mButtonMult;
    private CalcButton mButtonDiv;
    private CalcButton mButtonLeftParen;
    private CalcButton mButtonRightParen;
    private CalcButton mButtonPeriod;
    private CalcButton mButtonComma;
    private CalcButton mButtonCaret;
    private CalcButton mButtonE;
    private CalcButton mButtonSqr;
    private CalcButton mButtonSqrt;
    private CalcButton mButtonExp;
    private CalcButton mButtonPi;
    private CalcButton mButtonC;
    private CalcButton mButtonGamma;

    //Equals & answer Buttons
    private Button mButtonEquals;
    private CalcButton mButtonAns;

    //List of previous actions
    private List<String> mActions;

    //Index for scrolling through previous actions
    private int mActionIndex;

    //Current action
    private String mAction;

    //Stores the current action when scrolling so that
    //it can be returned to
    private String mActionTemp;

    //Result of previous expression
    private String mAns;

    //Custom Functions (more can be added as necessary)
    //This is the gamma function from special
    //relativity: 1/sqrt(1 - (v^2/c^2))
    Function mGammaFunction;

    //Custom Operators
    Operator square;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_calc, container, false);

        ////////////////////////
        //Initialize variables//
        ////////////////////////

        mAction = "";
        mActionTemp = "";
        mActions = new ArrayList<>();
        mAns = "0.0";
        mActionIndex = -1; //-1 means a new expression; the previous ones are 0, 1, 2, etc.

        //Custom Function definitions
        mGammaFunction = new Function("\u213D", 1) {
            @Override
            public double apply(double... args) {
                return 1.0 / Math.sqrt(1 - (Math.pow(args[0] / 300000000.0, 2)));

            }
        };

        //Custom Operator definitions
        square = new Operator("§", 1, true, Operator.PRECEDENCE_POWER) {
            @Override
            public double apply(double... args) {
                return Math.pow(args[0], 2);
            }
        };

        //////////////////////////
        //Initialize UI elements//
        //////////////////////////

        mDisplay = (TextView) v.findViewById(R.id.display);

        mButtonUp = (Button) v.findViewById(R.id.button_up);
        mButtonUp.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if(mActionIndex <= mActions.size() - 2) { //Ensure the index doesn't go past the end of the list
                    if(mActionIndex == -1) {
                        mActionTemp = mAction; //If the user hasn't started scrolling yet, save what they have typed
                    }
                    mActionIndex++;
                    mAction = mActions.get(mActionIndex); //Set the current action to a previous one
                }
                mDisplay.setText(mAction);}}); //Display the current action (this should happen on any button press except equals)

        mButtonDown = (Button) v.findViewById(R.id.button_down);
        mButtonDown.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if(mActionIndex > 0) { //If the user hasn't reached the end of the list, display the next one
                    mActionIndex--;
                    mAction = mActions.get(mActionIndex);
                } else if(mActionIndex == 0) { //If the user has reached the end of the list, restore the saved action
                    mActionIndex--;
                    mAction = mActionTemp;
                }
                mDisplay.setText(mAction); //Display the current action
            }
        });

        mButtonBack = (Button) v.findViewById(R.id.button_back);
        mButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if(mAction.length() > 0) { //If mAction has characters
                    mAction = mAction.substring(0, mAction.length() - 1); //remove the last one
                }
                mDisplay.setText(mAction); //Display the current action
            }
        });

        mButtonClear = (Button) v.findViewById(R.id.button_clear);
        mButtonClear.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                mAction = ""; //Clear the current action
                mActionIndex = -1; //If the user was scrolling through previous actions, return to the current one to prevent weird behaviour with the up and down buttons
                mDisplay.setText(mAction); //Display the current action
            }
        });

        mButtonEquals = (Button) v.findViewById(R.id.button_equals);
        mButtonEquals.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (!mAction.equals("")) { //If the current action isn't empty
                    mActions.add(0, mAction); //add it to the list of previous actions
                    mActionIndex = -1; //start a new action
                    calculate(); //and evaluate the expression
                }
            }
        });


        //Each CalcButton is assigned a string
        //When it is clicked, that string is appended to mAction
        //The CalcButton class does less than I originally thought it would
        //These could really all be changed to regular Buttons

        mButtonAns = (CalcButton) v.findViewById(R.id.button_ans);
        mButtonAns.setExprText("ans");
        mButtonAns.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonAns.getExprText(); mDisplay.setText(mAction);}});

        mButton0 = (CalcButton) v.findViewById(R.id.button_0);
        mButton0.setExprText("0");
        mButton0.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButton0.getExprText(); mDisplay.setText(mAction);}});

        mButton1 = (CalcButton) v.findViewById(R.id.button_1);
        mButton1.setExprText("1");
        mButton1.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButton1.getExprText(); mDisplay.setText(mAction);}});

        mButton2 = (CalcButton) v.findViewById(R.id.button_2);
        mButton2.setExprText("2");
        mButton2.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButton2.getExprText(); mDisplay.setText(mAction);}});

        mButton3 = (CalcButton) v.findViewById(R.id.button_3);
        mButton3.setExprText("3");
        mButton3.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButton3.getExprText(); mDisplay.setText(mAction);}});

        mButton4 = (CalcButton) v.findViewById(R.id.button_4);
        mButton4.setExprText("4");
        mButton4.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButton4.getExprText(); mDisplay.setText(mAction);}});

        mButton5 = (CalcButton) v.findViewById(R.id.button_5);
        mButton5.setExprText("5");
        mButton5.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButton5.getExprText(); mDisplay.setText(mAction);}});

        mButton6 = (CalcButton) v.findViewById(R.id.button_6);
        mButton6.setExprText("6");
        mButton6.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButton6.getExprText(); mDisplay.setText(mAction);}});

        mButton7 = (CalcButton) v.findViewById(R.id.button_7);
        mButton7.setExprText("7");
        mButton7.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButton7.getExprText(); mDisplay.setText(mAction);}});

        mButton8 = (CalcButton) v.findViewById(R.id.button_8);
        mButton8.setExprText("8");
        mButton8.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButton8.getExprText(); mDisplay.setText(mAction);}});

        mButton9 = (CalcButton) v.findViewById(R.id.button_9);
        mButton9.setExprText("9");
        mButton9.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButton9.getExprText(); mDisplay.setText(mAction);}});

        mButtonPlus = (CalcButton) v.findViewById(R.id.button_plus);
        mButtonPlus.setExprText("+");
        mButtonPlus.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonPlus.getExprText(); mDisplay.setText(mAction);}});

        mButtonMinus = (CalcButton) v.findViewById(R.id.button_minus);
        mButtonMinus.setExprText("-");
        mButtonMinus.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonMinus.getExprText(); mDisplay.setText(mAction);}});

        mButtonMult = (CalcButton) v.findViewById(R.id.button_mult);
        mButtonMult.setExprText("*");
        mButtonMult.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonMult.getExprText(); mDisplay.setText(mAction);}});

        mButtonDiv = (CalcButton) v.findViewById(R.id.button_div);
        mButtonDiv.setExprText("/");
        mButtonDiv.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonDiv.getExprText(); mDisplay.setText(mAction);}});

        mButtonLeftParen = (CalcButton) v.findViewById(R.id.button_left_paren);
        mButtonLeftParen.setExprText("(");
        mButtonLeftParen.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonLeftParen.getExprText(); mDisplay.setText(mAction);}});

        mButtonRightParen = (CalcButton) v.findViewById(R.id.button_right_paren);
        mButtonRightParen.setExprText(")");
        mButtonRightParen.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonRightParen.getExprText(); mDisplay.setText(mAction);}});

        mButtonPeriod = (CalcButton) v.findViewById(R.id.button_period);
        mButtonPeriod.setExprText(".");
        mButtonPeriod.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonPeriod.getExprText(); mDisplay.setText(mAction);}});

        mButtonComma = (CalcButton) v.findViewById(R.id.button_comma);
        mButtonComma.setExprText(",");
        mButtonComma.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonComma.getExprText(); mDisplay.setText(mAction);}});

        mButtonCaret = (CalcButton) v.findViewById(R.id.button_caret);
        mButtonCaret.setExprText("^");
        mButtonCaret.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonCaret.getExprText(); mDisplay.setText(mAction);}});

        mButtonE = (CalcButton) v.findViewById(R.id.button_e);
        mButtonE.setExprText("e");
        mButtonE.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonE.getExprText(); mDisplay.setText(mAction);}});

        mButtonSqr = (CalcButton) v.findViewById(R.id.button_sqr);
        mButtonSqr.setExprText("\u00B2");
        mButtonSqr.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonSqr.getExprText(); mDisplay.setText(mAction);}});

        mButtonSqrt = (CalcButton) v.findViewById(R.id.button_sqrt);
        mButtonSqrt.setExprText("sqrt(");
        mButtonSqrt.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonSqrt.getExprText(); mDisplay.setText(mAction);}});

        mButtonExp = (CalcButton) v.findViewById(R.id.button_exp);
        mButtonExp.setExprText("exp(");
        mButtonExp.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonExp.getExprText(); mDisplay.setText(mAction);}});

        mButtonPi = (CalcButton) v.findViewById(R.id.button_pi);
        mButtonPi.setExprText("\u03C0");
        mButtonPi.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonPi.getExprText(); mDisplay.setText(mAction);}});

        mButtonC = (CalcButton) v.findViewById(R.id.button_c);
        mButtonC.setExprText("c");
        mButtonC.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonC.getExprText(); mDisplay.setText(mAction);}});

        mButtonGamma = (CalcButton) v.findViewById(R.id.button_gamma);
        mButtonGamma.setExprText("\u213D(");
        mButtonGamma.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) {mAction += mButtonGamma.getExprText(); mDisplay.setText(mAction);}});

        return v;
    }

    private void calculate() {
        try {
            mAction = mAction.replace('\u00B2', '§'); //Replace ² with §, which is allowed in Operators
            Expression e = new ExpressionBuilder(mAction)
                    .operator(square) //Add custom Operator x²
                    .function(mGammaFunction) //Add custom Function ℽ()
                    .variable("c") //Add custom variable c
                    .variable("ans") //Add custom variable ans
                    .build()
                    .setVariable("c", 300000000.0) //Set c to 3e8 m/s
                    .setVariable("ans", Double.parseDouble(mAns)); //Set ans to the previous answer
            double result = e.evaluate(); //Evaluate expression
            mAns = String.valueOf(result); //Set mAns to answer
            mAction = ""; //Clear mAction
            mDisplay.setText(mAns); //Display the answer
        } catch (AndroidRuntimeException | IllegalArgumentException e) { //Caused by a malformed expression (wrong number of arguments in function, missing close paren, etc.)
            Log.e("CALCULATE FUNCTION", e.getMessage()); //Log error
            mAction = ""; //Clear mAction
            mDisplay.setText("ERROR"); //Let the user know that there was an error
        }
    }
}
