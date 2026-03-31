package com.example.weather.model;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
public class SunArcView extends View {
    private long sunriseTime = 0;
    private long sunsetTime  = 0;
    private long currentTime = System.currentTimeMillis() / 1000L;
    private final Paint arcPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sunPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dashPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    public SunArcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(4f);
        arcPaint.setColor(Color.parseColor("#BDBDBD"));

        dashPaint.setStyle(Paint.Style.STROKE);
        dashPaint.setStrokeWidth(3f);
        dashPaint.setColor(Color.parseColor("#BDBDBD"));
        dashPaint.setPathEffect(new DashPathEffect(new float[]{15, 15}, 0));

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(8f);
        progressPaint.setColor(Color.parseColor("#FFA000")); // Orange 700

        sunPaint.setStyle(Paint.Style.FILL);
        sunPaint.setColor(Color.parseColor("#FFD54F")); // Amber 300
    }

    public void setTimes(long sunrise, long sunset) {
        this.sunriseTime = sunrise;
        this.sunsetTime  = sunset;
        this.currentTime = System.currentTimeMillis() / 1000L;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        if (w == 0 || h == 0) return;
        
        float pad = 60;
        float radius = Math.min((w - 2 * pad) / 2f, h - pad - 20);
        float centerX = w / 2f;
        float centerY = h - 20; 

        RectF oval = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        // Draw the background dashed arc
        canvas.drawArc(oval, 180, 180, false, dashPaint);

        // Calculate progress (0 to 1)
        float progress = 0f;
        if (sunriseTime > 0 && sunsetTime > sunriseTime) {
            progress = (float)(currentTime - sunriseTime) / (sunsetTime - sunriseTime);
            progress = Math.max(0f, Math.min(1f, progress));
        } else {
            progress = 0.5f; 
        }

        // Draw progress arc
        canvas.drawArc(oval, 180, 180 * progress, false, progressPaint);

        // Sun position
        double angle = Math.PI + Math.PI * progress;
        float sunX = centerX + radius * (float) Math.cos(angle);
        float sunY = centerY + radius * (float) Math.sin(angle);

        // Sun glow
        sunPaint.setShadowLayer(25, 0, 0, Color.parseColor("#FFA000"));
        canvas.drawCircle(sunX, sunY, 18f, sunPaint);
        
        // Sun core
        sunPaint.clearShadowLayer();
        sunPaint.setColor(Color.WHITE);
        canvas.drawCircle(sunX, sunY, 8f, sunPaint);
        sunPaint.setColor(Color.parseColor("#FFD54F"));
    }
}
