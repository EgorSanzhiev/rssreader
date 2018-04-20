package sanzhiev.rssfeed.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import sanzhiev.rssfeed.R;

final class FeedItemDecoration extends RecyclerView.ItemDecoration {
    private final Drawable drawable;

    FeedItemDecoration(final Context context) {
        final Resources resources = context.getResources();
        drawable = resources.getDrawable(R.drawable.recyclerview_divider);
    }

    @Override
    public void onDrawOver(final Canvas c, final RecyclerView parent, final RecyclerView.State state) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childrenCount = parent.getChildCount();

        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        for (int i = 0; i < childrenCount - 1; ++i) {
            final View child = parent.getChildAt(i);
            final int top = layoutManager.getDecoratedBottom(child);
            final int bottom = top + drawable.getIntrinsicHeight();
            drawable.setBounds(left, top, right, bottom);
            drawable.draw(c);
        }
    }
}
