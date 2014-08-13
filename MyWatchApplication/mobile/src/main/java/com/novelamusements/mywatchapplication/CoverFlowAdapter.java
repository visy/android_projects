package com.novelamusements.mywatchapplication;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import at.technikum.mti.fancycoverflow.FancyCoverFlow;
import at.technikum.mti.fancycoverflow.FancyCoverFlowAdapter;

/**
 * Created by visy on 30/07/14.
 */
public class CoverFlowAdapter extends FancyCoverFlowAdapter {
    // =============================================================================
    // Private members
    // =============================================================================

    private int[] images;
    private int count;
    private int resLimit;
    private Bitmap[] bitmaps;

    // =============================================================================
    // Supertype overrides
    // =============================================================================

    public void setImages(int[] i) {
        images = null;
        images = new int[i.length];
        System.arraycopy( i, 0, images, 0, i.length );
    }

    public void setBitmaps(Bitmap[] bps) {
        bitmaps = null;
        bitmaps = new Bitmap[bps.length];
        System.arraycopy( bps, 0, bitmaps, 0, bps.length);
    }

    public void setResLimit(int rl) {
        resLimit = rl;
    }

    public void setCount(int _count) {
        count = _count;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Integer getItem(int i) {
        return images[i];
    }

    public Bitmap getBitmap(int i) { return bitmaps[i-(resLimit)]; }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getCoverFlowItem(int i, View reuseableView, ViewGroup viewGroup) {
        ImageView imageView = null;

        if (reuseableView != null) {
            imageView = (ImageView) reuseableView;
        } else {
            imageView = new ImageView(viewGroup.getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setLayoutParams(new FancyCoverFlow.LayoutParams(300, 400));

        }

        if (i <= resLimit-1) imageView.setImageResource(this.getItem(i));
        else imageView.setImageBitmap(this.getBitmap(i));
        return imageView;
    }
}
