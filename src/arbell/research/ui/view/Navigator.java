package arbell.research.ui.view;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import arbell.research.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YinLanshan
 * creation time 2015/3/27.
 */
public class Navigator implements
        View.OnClickListener,
        LayoutTransition.TransitionListener {
    private LinearLayout mContainerView;
    private View mExpandingGroup;
    private LayoutInflater mInflater;
    private int mInferiorViewPaddingUnit;
    private Drawable mIcon;

    private ArrayList<View> mPendingViews = new ArrayList<View>();
    private ArrayList<View> mRecycledViews = new ArrayList<View>();

    public Navigator(LinearLayout container, TreeNode root) {
        mContainerView = container;
        mInflater = LayoutInflater.from(mContainerView.getContext());
        Resources res = mContainerView.getContext().getResources();
        mInferiorViewPaddingUnit = res.getDimensionPixelSize(R.dimen.navi_padding);
        if(root.data != null)
            expand(0, root.data);
        Drawable d = res.getDrawable(R.drawable.fold);
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        mIcon = d;
    }

    @Override
    public void onClick(View v) {
        TreeNode currentNode = (TreeNode) v.getTag();

        if (currentNode.data != null && currentNode.data.size() > 0) {
            expandOrCollapseList(v, currentNode);
        }
        else {
            if(currentNode instanceof TreeLeafNode) {
                Context context = mContainerView.getContext();
                TreeLeafNode leaf = (TreeLeafNode)currentNode;
                String clazz = leaf.data;
                Intent intent = new Intent();
                intent.setClassName(context, clazz);
                context.startActivity(intent);
            }
        }
    }

    @Override
    public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {

    }

    @Override
    public void endTransition(LayoutTransition transition,
                              ViewGroup container, View view, int transitionType) {
        if (transitionType == LayoutTransition.CHANGE_APPEARING ||
                transitionType == LayoutTransition.CHANGE_DISAPPEARING)
            if (!transition.isChangingLayout()) {
                mRecycledViews.addAll(mPendingViews);
                mPendingViews.clear();
            }
    }

    /*private void expandOrCollapseList(View currentView, TreeNode currentNode) {
        View previousExpandingView = mExpandingGroup;
        TreeNode expandedNode = previousExpandingView == null ?
                null : (TreeNode) previousExpandingView.getTag();
        LinearLayout container = mContainerView;
        mExpandingGroup = currentView;

        if (expandedNode != null && currentNode.level <= expandedNode.level) {
            TreeNode upperNode = expandedNode;
            int upperViewIndex = container.indexOfChild(previousExpandingView);
            View upperView = previousExpandingView;
            while (currentNode.level < upperNode.level)
            {
                upperViewIndex--;
                upperView = container.getChildAt(upperViewIndex);
                upperNode = (TreeNode) upperView.getTag();
            }

            //Remove view for collapse
            int indexToRemove = upperViewIndex + 1;
            View toRemove = container.getChildAt(indexToRemove);
            TreeNode nodeOfRemovingView = toRemove == null ?
                    null : (TreeNode) toRemove.getTag();
            while (nodeOfRemovingView != null && nodeOfRemovingView.level > currentNode.level)
            {
                container.removeView(toRemove);
                recycleView(toRemove);
                toRemove = container.getChildAt(indexToRemove);
                nodeOfRemovingView = toRemove == null ? null : (TreeNode) toRemove.getTag();
            }

            //If the clicking view has already expanded, and we collapse it, then
            //the new expanding view should be its upper level one
            if (upperView == currentView) {
                do {
                    upperViewIndex--;
                    upperView = container.getChildAt(upperViewIndex);
                    upperNode = upperView == null ? null : (TreeNode) upperView.getTag();
                }
                while (upperNode != null && upperNode.level == currentNode.level);
                mExpandingGroup = upperView;
            }
        }

        //add views to expand
        if (mExpandingGroup == currentView)
        {
            int index = container.indexOfChild(currentView) + 1;
            expand(index, currentNode.data);
        }
    }*/

    private void expandOrCollapseList(View currentView, TreeNode currentNode) {
        View previousExpandingView = mExpandingGroup;
        TreeNode expandedNode = previousExpandingView == null ?
                null : (TreeNode) previousExpandingView.getTag();
        LinearLayout container = mContainerView;
        int currentIndex = container.indexOfChild(currentView);
        View lower = container.getChildAt(currentIndex + 1);
        if(lower == null) {

        }

        mExpandingGroup = currentView;

        if (expandedNode != null && currentNode.level <= expandedNode.level) {
            TreeNode upperNode = expandedNode;
            int upperViewIndex = container.indexOfChild(previousExpandingView);
            View upperView = previousExpandingView;
            while (currentNode.level < upperNode.level)
            {
                upperViewIndex--;
                upperView = container.getChildAt(upperViewIndex);
                upperNode = (TreeNode) upperView.getTag();
            }

            //Remove view for collapse
            int indexToRemove = upperViewIndex + 1;
            View toRemove = container.getChildAt(indexToRemove);
            TreeNode nodeOfRemovingView = toRemove == null ?
                    null : (TreeNode) toRemove.getTag();
            while (nodeOfRemovingView != null && nodeOfRemovingView.level > currentNode.level)
            {
//                container.removeView(toRemove);
                recycleView(toRemove);
                toRemove = container.getChildAt(++indexToRemove);
                nodeOfRemovingView = toRemove == null ? null : (TreeNode) toRemove.getTag();
            }

            //If the clicking view has already expanded, and we collapse it, then
            //the new expanding view should be its upper level one
            if (upperView == currentView) {
                do {
                    upperViewIndex--;
                    upperView = container.getChildAt(upperViewIndex);
                    upperNode = upperView == null ? null : (TreeNode) upperView.getTag();
                }
                while (upperNode != null && upperNode.level == currentNode.level);
                mExpandingGroup = upperView;
            }
        }

        //add views to expand
        if (mExpandingGroup == currentView)
        {
            int index = container.indexOfChild(currentView) + 1;
            expand(index, currentNode.data);
        }

        for(View view : mPendingViews) {
            container.removeView(view);
        }
    }

    private void expand(int index, List<TreeNode> children) {
        LinearLayout container = mContainerView;
        for(TreeNode child : children) {
            View view = getInferiorView();
            TextView tv = (TextView) view.findViewById(android.R.id.text1);
            tv.setText(child.name);
            view.setPadding(mInferiorViewPaddingUnit * (child.level - 1),
                    view.getPaddingTop(),
                    view.getPaddingRight(),
                    view.getPaddingBottom());
            view.setTag(child);
            if(child instanceof TreeLeafNode) {
                tv.setCompoundDrawables(mIcon, null, null, null);
            }
            else {
                tv.setCompoundDrawables(null, null, null, null);
            }
            container.addView(view, index++);
        }
    }

    @SuppressLint("InflateParams")
    private View getInferiorView()
    {
        View view;
        if (mRecycledViews.size() > 0)
        {
            view = mRecycledViews.remove(0);
        } else
        {
            view = mInflater.inflate(R.layout.navi_view_item, null);
            view.setOnClickListener(this);
        }
        return view;
    }

    private void recycleView(View view)
    {
        if (view.getWindowToken() != null)
            mPendingViews.add(view);
        else
            mRecycledViews.add(view);
    }
}
