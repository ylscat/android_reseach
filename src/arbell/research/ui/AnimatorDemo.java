package arbell.research.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import arbell.research.R;

/**
 * Author: YinLanShan
 * Date: 14-2-14
 * Time: 14:17
 */
public class AnimatorDemo extends Activity implements Animator.AnimatorListener
{
    private View mTargetView;
    private ObjectAnimator mAnimator, mAnimatorTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_animator);

        mTargetView = findViewById(R.id.target);

        PropertyValuesHolder left = PropertyValuesHolder.ofInt("left", 0, 1);
        PropertyValuesHolder right = PropertyValuesHolder.ofInt("right", 0, 1);
        PropertyValuesHolder top = PropertyValuesHolder.ofInt("top", 0, 1);
        PropertyValuesHolder bottom = PropertyValuesHolder.ofInt("bottom", 0, 1);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
                mTargetView, left, right, top, bottom);
        animator.setDuration(1000);
        mAnimator = animator;
        mAnimator.addListener(this);
    }

    public void changePosition(View view)
    {
        mAnimator = mAnimator.clone();
        mAnimator.setupStartValues();
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mTargetView.getLayoutParams();
        switch (lp.gravity)
        {
            case Gravity.LEFT | Gravity.TOP:
            default:
                lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
                break;
            case Gravity.LEFT | Gravity.BOTTOM:
                lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                break;
            case Gravity.RIGHT | Gravity.BOTTOM:
                lp.gravity = Gravity.RIGHT | Gravity.TOP;
                break;
            case Gravity.RIGHT | Gravity.TOP:
                lp.gravity = Gravity.LEFT | Gravity.TOP;
                break;
        }
        mTargetView.requestLayout();
    }

    public void playAnimation(View view)
    {
        mAnimator.setupEndValues();
        mAnimator.start();
    }

    @Override
    public void onAnimationStart(Animator animation)
    {

    }

    @Override
    public void onAnimationEnd(Animator animation)
    {

    }

    @Override
    public void onAnimationCancel(Animator animation)
    {

    }

    @Override
    public void onAnimationRepeat(Animator animation)
    {

    }
}
