package libs.mjn.fieldset;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by mJafarinejad on 7/1/2018.
 */
public class FieldSet extends FrameLayout {

    private final int DEFAULT_BORDER_WIDTH = 2;
    private final int DEFAULT_BORDER_RADIUS = 0;
    private final String DEFAULT_BORDER_COLOR = "#212121";
    private final String DEFAULT_LEGEND_COLOR = "#212121";
    private final int DEFAULT_LEGEND_MARGIN = 24;
    private final int DEFAULT_LEGEND_PADDING = 12;

    private enum ENUM_TITLE_POSITION {LEFT, RIGHT, CENTER}
    private ENUM_TITLE_POSITION legendPosition = ENUM_TITLE_POSITION.CENTER;
    private TextView mLegend;
    private RelativeLayout mFrame;
    private FrameLayout mContainer;
    private fs_FrameDrawable mBackground;
    private int legendMarginLeft, legendMarginRight;
    private int legendPadding, legendPaddingLeft, legendPaddingRight;
    private Bitmap bitmap;

    public FieldSet(Context context) {
        super(context);
        init(null);
    }

    public FieldSet(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public FieldSet(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FieldSet(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(@Nullable AttributeSet set){
        inflate(getContext(),R.layout.fs_fieldset,this);
        mFrame = (RelativeLayout) findViewById(R.id.fieldset_frame);
        mContainer = (FrameLayout) findViewById(R.id.fieldset_container);
        mLegend = (TextView) findViewById(R.id.fieldset_legend);
        mBackground = new fs_FrameDrawable();

        if(set==null)
            return;
        TypedArray ta = getContext().obtainStyledAttributes(set,R.styleable.FieldSet);

        //Border Color
        mBackground.setBorder_color(ta.getColor(R.styleable.FieldSet_fs_borderColor, Color.parseColor(DEFAULT_BORDER_COLOR)));

        //Border Width
        mBackground.setBorder_width((int) ta.getDimension(R.styleable.FieldSet_fs_borderWidth, DEFAULT_BORDER_WIDTH));

        //Stroke Radius
        mBackground.setBorder_radius((int) ta.getDimension(R.styleable.FieldSet_fs_borderRadius, DEFAULT_BORDER_RADIUS));

        //Stroke Alpha
        mBackground.setBorder_alpha(ta.getFloat(R.styleable.FieldSet_fs_borderAlpha,1f));

        //Title Margins and Paddings
        legendMarginLeft = (int) ta.getDimension(R.styleable.FieldSet_fs_legendMarginLeft, DEFAULT_LEGEND_MARGIN);
        legendMarginRight = (int) ta.getDimension(R.styleable.FieldSet_fs_legendMarginRight, DEFAULT_LEGEND_MARGIN);
        legendPadding = (int) ta.getDimension(R.styleable.FieldSet_fs_legendPadding, -1369f);
        legendPaddingLeft = (int) ta.getDimension(R.styleable.FieldSet_fs_legendPaddingLeft, DEFAULT_LEGEND_PADDING);
        legendPaddingRight = (int) ta.getDimension(R.styleable.FieldSet_fs_legendPaddingRight, DEFAULT_LEGEND_PADDING);
        if(legendPadding!=-1369f){
            legendPaddingLeft=legendPadding;
            legendPaddingRight=legendPadding;
        }

        //Title text
        mLegend.setText(ta.getText(R.styleable.FieldSet_fs_legend));

        //Title text color
        mLegend.setTextColor(ta.getColor(R.styleable.FieldSet_fs_legendColor, Color.parseColor(DEFAULT_LEGEND_COLOR)));

        //Title font
        String fontName = ta.getString(R.styleable.FieldSet_fs_legendFont);
        try {
            Typeface customFont = Typeface.createFromAsset(getContext().getAssets(), fontName);
            mLegend.setTypeface(customFont);
        }
        catch (Exception e){}

        int titleSize = ta.getDimensionPixelSize(R.styleable.FieldSet_fs_legendSize,24);
        if (titleSize > 0) {
            mLegend.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize);
        }
        if(ta.hasValue(R.styleable.FieldSet_fs_legendPosition)){
            switch (ta.getInt(R.styleable.FieldSet_fs_legendPosition,3)){
                case 1: //left
                    legendPosition = ENUM_TITLE_POSITION.LEFT;
                    break;
                case 2: //right
                    legendPosition = ENUM_TITLE_POSITION.RIGHT;
                    break;
                case 3: //center
                    legendPosition = ENUM_TITLE_POSITION.CENTER;
                    break;
            }
        }

        mLegend.post(new Runnable() {
            @Override
            public void run() {
                if (mLegend.length() > 0) {
                    if (mLegend.getMeasuredHeight() <= mBackground.getBorder_width()) {
                        mBackground.setBorder_width((int) (mLegend.getMeasuredHeight() * 0.5));
                        setContainerMargins(mBackground.getBorder_width());
                    }
                    if (legendPosition == ENUM_TITLE_POSITION.RIGHT) {
                        ((LayoutParams) mLegend.getLayoutParams()).gravity = Gravity.TOP | Gravity.RIGHT;
                        int margin = mBackground.getBorder_width()>=mBackground.getBorder_radius() ? mBackground.getBorder_width() + mBackground.getBorder_radius() : mBackground.getBorder_radius();
                        ((LayoutParams) mLegend.getLayoutParams()).setMargins(0, 0, margin + legendMarginRight, 0);
                    } else if (legendPosition == ENUM_TITLE_POSITION.LEFT) {
                        ((LayoutParams) mLegend.getLayoutParams()).gravity = Gravity.TOP | Gravity.LEFT;
                        int margin = mBackground.getBorder_width()>=mBackground.getBorder_radius() ? mBackground.getBorder_width() + mBackground.getBorder_radius() : mBackground.getBorder_radius();
                        ((LayoutParams) mLegend.getLayoutParams()).setMargins(legendMarginLeft + margin, 0, 0, 0);
                    } else {
                        ((LayoutParams) mLegend.getLayoutParams()).gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                    }
                    updateFrame();
                    LayoutParams frameParams = (LayoutParams) mFrame.getLayoutParams();
                    frameParams.setMargins(0, (mLegend.getMeasuredHeight() - mBackground.getBorder_width()) / 2, 0, 0);
                }
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateFrame();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // move child views into container
        for (int i = 1; i<getChildCount(); i++)
        {
            View v = getChildAt(i);
            removeViewAt(i);
            mContainer.addView(v);
        }

        // set padding to container
        mContainer.setPadding(getPaddingLeft(),getPaddingTop(),getPaddingRight(),getPaddingBottom());
        setPadding(0,0,0,0);
    }

    // set margins to container so it fits inside frame and below the title
    private void setContainerMargins(int margin){
        ((RelativeLayout.LayoutParams)mContainer.getLayoutParams()).setMargins(
                margin,
                margin+(mLegend.getMeasuredHeight() - mBackground.getBorder_width()) / 2,
                margin,
                margin+(mLegend.getMeasuredHeight() - mBackground.getBorder_width()) / 2);
    }

    //set frame's background and erase behind the title
    private void updateFrame(){
        post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT <= 23)
                    mBackground.invalidateSelf();
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mFrame.setBackgroundDrawable(mBackground);
                } else {
                    mFrame.setBackground(mBackground);
                }
                if (Build.VERSION.SDK_INT <= 23)
                    mBackground.invalidateSelf();

                setContainerMargins(mBackground.getBorder_width());

                mFrame.post(new Runnable() {
                    @Override
                    public void run() {
                        bitmap = Bitmap.createBitmap(mFrame.getWidth(), mFrame.getHeight(),Bitmap.Config.ARGB_8888);
                        Canvas bitmapCanvas = new Canvas(bitmap);
                        mFrame.getBackground().draw(bitmapCanvas);
                        eraseBitmap(bitmapCanvas, mLegend);
                        mFrame.setBackgroundDrawable(new BitmapDrawable(bitmap));
                    }
                });


            }
        });
    }

    private void eraseBitmap(Canvas canvas,View view){
        if(view.getLeft()- legendPaddingLeft <mBackground.getBorder_width())
            legendPaddingLeft = view.getLeft()-mBackground.getBorder_width();
        if(view.getRight()+ legendPaddingRight >mFrame.getRight()-mBackground.getBorder_width())
            legendPaddingRight = mFrame.getRight()-mBackground.getBorder_width() - view.getRight();

        Paint eraserPaint = new Paint();
        eraserPaint.setAlpha(0);
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        eraserPaint.setAntiAlias(true);
        Rect rect = new Rect(view.getLeft()- legendPaddingLeft,view.getTop(),view.getRight()+ legendPaddingRight,mBackground.getBorder_width());
        canvas.drawRect(rect,eraserPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if(bitmap!=null)
            bitmap.recycle();
    }

    //////// Set-Get Values Programmatically
    public void setLegend(String text){
        mLegend.setText(text);
        updateFrame();
    }

    public String getLegend(){
        return mLegend.getText().toString();
    }
}