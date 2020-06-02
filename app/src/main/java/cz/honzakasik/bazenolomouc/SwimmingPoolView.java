package cz.honzakasik.bazenolomouc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import cz.honzakasik.bazenolomouc.pool.SwimmingPool;

@SuppressWarnings("SuspiciousNameCombination")
public class SwimmingPoolView extends View {

    private static final Logger logger = LoggerFactory.getLogger(SwimmingPool.class);

    private SwimmingPool swimmingPool;

    private List<Rect> preAllocatedRectangles;

    private Paint availableForPublic = initAvailableForPublicPaint();
    private Paint notAvailableForPublic = initNotAvailableForPublicPaint();
    private Paint linePaint = initLinePaint();
    private Paint indexPaint = initIndexPaint();
    private Rect backgroundRect = new Rect();

    private int measuredWidth, measuredHeight;

    private final int X_PADDING = 20;
    private final int Y_PADDING = 10;

    private Paint initLinePaint() {
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.swimmingPoolBorder));
        linePaint.setStrokeWidth(2);
        return linePaint;
    }

    private Paint initIndexPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(15 * getResources().getDisplayMetrics().density);
        return paint;
    }

    public SwimmingPoolView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSwimmingPool(SwimmingPool swimmingPool) {
        this.swimmingPool = swimmingPool;
        if (swimmingPool.getTracks() != null) {
            preAllocatedRectangles = new ArrayList<>(swimmingPool.getTracks().size());
            for (int i = 0; i < swimmingPool.getTracks().size(); i++) {
                preAllocatedRectangles.add(new Rect());
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (swimmingPool == null) {
            logger.debug("Trying to draw null swimming pool.");
            return;
        }

        backgroundRect.set(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.drawRect(backgroundRect, linePaint);

        if (!swimmingPool.isOpen()) {
            drawSwimmingPoolIsClosedMessage(canvas, backgroundRect);
            logger.debug("Swimming pool is currently closed!");
        } else if (swimmingPool.getOrientation() == SwimmingPool.TrackOrientation.HORIZONTAL) {
            drawHorizontalSwimmingPool(canvas);
        } else {
            drawVerticalSwimmingPool(canvas);
        }
    }

    private void drawSwimmingPoolIsClosedMessage(Canvas canvas, Rect rect) {
        String text = getResources().getString(R.string.swimming_pool_is_closed_now);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.swimmingPoolIsClosedText));
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(15 * getResources().getDisplayMetrics().density);

        float textWidth = paint.measureText(text);
        canvas.drawText(text, rect.exactCenterX() - (textWidth/2), rect.exactCenterY(), paint);
    }

    private Paint initNotAvailableForPublicPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.swimmingPoolNotForPublic));
        return paint;
    }

    private Paint initAvailableForPublicPaint() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.swimmingPoolForPublic));
        return paint;
    }

    private void drawVerticalSwimmingPool(Canvas canvas) {

        final int COUNT_OF_TRACKS = swimmingPool.getTracks().size();
        final int INNER_POOL_WIDTH = canvas.getWidth() - 2 * Y_PADDING;
        final int TRACK_WIDTH = INNER_POOL_WIDTH / COUNT_OF_TRACKS;
        final int TRACK_LENGTH = getHeight() - X_PADDING;

        for (int i = 0; i < COUNT_OF_TRACKS; i++) {
            SwimmingPool.Track currentTrack = swimmingPool.getTracks().get(i);

            Rect rect = preAllocatedRectangles.get(i);
            rect.set(0, X_PADDING, TRACK_WIDTH, TRACK_LENGTH);
            rect.offset((TRACK_WIDTH * i) + Y_PADDING, 0);

            canvas.drawRect(rect, currentTrack.isForPublic() ? availableForPublic : notAvailableForPublic);
            drawIndexOnCenterOfRectangle(canvas, rect, i, currentTrack);

            int lineOffset = (TRACK_WIDTH * i) + Y_PADDING;
            canvas.drawLine(lineOffset, X_PADDING, lineOffset, TRACK_LENGTH, linePaint);
        }
    }

    private void drawHorizontalSwimmingPool(Canvas canvas) {

        final int COUNT_OF_TRACKS = swimmingPool.getTracks().size();
        final int INNER_POOL_HEIGHT = canvas.getHeight() - 2 * Y_PADDING;
        final int TRACK_WIDTH = INNER_POOL_HEIGHT / COUNT_OF_TRACKS;
        final int TRACK_LENGTH = getWidth() - X_PADDING;

        for (int i = 0; i < COUNT_OF_TRACKS; i++) {
            SwimmingPool.Track currentTrack = swimmingPool.getTracks().get(i);

            Rect rect = preAllocatedRectangles.get(i);
            rect.set(X_PADDING, 0, TRACK_LENGTH, TRACK_WIDTH);
            rect.offset(0, (TRACK_WIDTH * i) + Y_PADDING);

            canvas.drawRect(rect, currentTrack.isForPublic() ? availableForPublic : notAvailableForPublic);
            drawIndexOnCenterOfRectangle(canvas, rect, i, currentTrack);

            int lineOffset = (TRACK_WIDTH * i) + Y_PADDING;
            canvas.drawLine(X_PADDING, lineOffset, TRACK_LENGTH, lineOffset, linePaint);
        }
    }

    private void drawIndexOnCenterOfRectangle(Canvas canvas, Rect rect, int i, SwimmingPool.Track track) {
        final String text = String.valueOf(i + 1);

        indexPaint.setColor(track.isForPublic() ? ContextCompat.getColor(getContext(), R.color.swimmingPoolForPublicText) :
                ContextCompat.getColor(getContext(), R.color.swimmingPoolNotForPublicText));

        float textWidth = indexPaint.measureText(text);
        canvas.drawText(text, rect.exactCenterX() - (textWidth/2), rect.exactCenterY() + (textWidth/2), indexPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measure(widthMeasureSpec, heightMeasureSpec, 2);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    /**
     * http://stackoverflow.com/a/13846628/4402950
     * Measure with a specific aspect ratio
     * @param widthMeasureSpec The width <tt>MeasureSpec</tt> passed in your <tt>View.onMeasure()</tt> method
     * @param heightMeasureSpec The height <tt>MeasureSpec</tt> passed in your <tt>View.onMeasure()</tt> method
     * @param aspectRatio The aspect ratio to calculate measurements in respect to
     */
    private void measure(int widthMeasureSpec, int heightMeasureSpec, double aspectRatio) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = widthMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = heightMode == MeasureSpec.UNSPECIFIED ? Integer.MAX_VALUE : MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode == MeasureSpec.EXACTLY && widthMode == MeasureSpec.EXACTLY) {
        /*
         * Possibility 1: Both width and height fixed
         */
            measuredWidth = widthSize;
            measuredHeight = heightSize;

        } else if (heightMode == MeasureSpec.EXACTLY) {
        /*
         * Possibility 2: Width dynamic, height fixed
         */
            measuredWidth = (int) Math.min(widthSize, heightSize * aspectRatio);
            measuredHeight = (int) (measuredWidth / aspectRatio);

        } else if (widthMode == MeasureSpec.EXACTLY) {
        /*
         * Possibility 3: Width fixed, height dynamic
         */
            measuredHeight = (int) Math.min(heightSize, widthSize / aspectRatio);
            measuredWidth = (int) (measuredHeight * aspectRatio);

        } else {
        /*
         * Possibility 4: Both width and height dynamic
         */
            if (widthSize > heightSize * aspectRatio) {
                measuredHeight = heightSize;
                measuredWidth = (int) (measuredHeight * aspectRatio);
            } else {
                measuredWidth = widthSize;
                measuredHeight = (int) (measuredWidth / aspectRatio);
            }

        }
    }
}
