package cz.honzakasik.bazenolomouc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import cz.honzakasik.bazenolomouc.pool.SwimmingPool;

@SuppressWarnings("SuspiciousNameCombination")
public class SwimmingPoolView extends View {

    private SwimmingPool swimmingPool = initTestSwimmmingPool();

    private SwimmingPool initTestSwimmmingPool() {
        return new SwimmingPool.Builder()
                .track(new SwimmingPool.Track(false))
                .track(new SwimmingPool.Track(true))
                .track(new SwimmingPool.Track(true))
                .track(new SwimmingPool.Track(true))
                .track(new SwimmingPool.Track(false))
                .track(new SwimmingPool.Track(false))
                .track(new SwimmingPool.Track(false))
                .track(new SwimmingPool.Track(false))
                .orientation(SwimmingPool.TrackOrientation.HORIZONTAL)
                .build();

    }

    private Paint availableForPublic = initAvailableForPublicPaint();
    private Paint notAvailableForPublic = initNotAvailableForPublicPaint();
    private Paint linePaint = initLinePaint();

    private Paint initLinePaint() {
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.rgb(99, 179, 255));
        linePaint.setStrokeWidth(2);
        return linePaint;
    }

    public SwimmingPoolView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSwimmingPool(SwimmingPool swimmingPool) {
        this.swimmingPool = swimmingPool;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        super.onDraw(canvas);

        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), linePaint);
        drawHorizontalSwimmingPool(canvas);
    }

    private Paint initNotAvailableForPublicPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.rgb(244, 69, 66));
        return paint;
    }

    private Paint initAvailableForPublicPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(66, 244, 116));
        return paint;
    }

    private void drawHorizontalSwimmingPool(Canvas canvas) {
        final boolean IS_IN_LANDSCAPE = isInLandscape();

        final int COUNT_OF_TRACKS = swimmingPool.getTracks().size();
        final int X_PADDING = 10;
        final int Y_PADDING = 10;
        final int INNER_POOL_HEIGHT = canvas.getHeight() - 2 * Y_PADDING;
        final int INNER_POOL_WIDTH = canvas.getWidth() - 2 * X_PADDING;
        final int TRACK_WIDTH = IS_IN_LANDSCAPE ? INNER_POOL_WIDTH / COUNT_OF_TRACKS : INNER_POOL_HEIGHT / COUNT_OF_TRACKS;
        final int TRACK_LENGTH = IS_IN_LANDSCAPE ? getHeight() - Y_PADDING : getWidth() - X_PADDING;

        for (int i = 0; i < COUNT_OF_TRACKS; i++) {
            SwimmingPool.Track currentTrack = swimmingPool.getTracks().get(i);

            Rect rect;
            if (IS_IN_LANDSCAPE) {
                rect = new Rect(0, X_PADDING, TRACK_WIDTH, TRACK_LENGTH);
                rect.offset((TRACK_WIDTH * i) + Y_PADDING, 0);
            } else {
                rect = new Rect(X_PADDING, 0, TRACK_LENGTH, TRACK_WIDTH);
                rect.offset(0, (TRACK_WIDTH * i) + Y_PADDING);
            }

            canvas.drawRect(rect, currentTrack.isForPublic() ? availableForPublic : notAvailableForPublic);
            drawIndexOnCenterOfRectangle(canvas, rect, i);

            if (IS_IN_LANDSCAPE) {
                int lineOffset = (TRACK_WIDTH * i) + Y_PADDING;
                canvas.drawLine(lineOffset, X_PADDING, lineOffset, TRACK_LENGTH, linePaint);
            } else {
                int lineOffset = (TRACK_WIDTH * i) + Y_PADDING;
                canvas.drawLine(X_PADDING, lineOffset, TRACK_LENGTH, lineOffset, linePaint);
            }
        }
    }

    private void drawIndexOnCenterOfRectangle(Canvas canvas, Rect rect, int i) {
        final String text = String.valueOf(i + 1);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setShadowLayer(2f, 0f, 1f, Color.GRAY);
        paint.setTextAlign(Paint.Align.CENTER);
        //paint.getTextBounds(text, 0, text.length(), rect);
        paint.setTextSize(15 * getResources().getDisplayMetrics().density);

        canvas.drawText(text, rect.centerX(), rect.centerY(), paint);
    }

    private boolean isInLandscape(){
        final int ROTATION = getDisplay().getRotation();
        return ROTATION == Surface.ROTATION_90 || ROTATION == Surface.ROTATION_270;
    }
}
