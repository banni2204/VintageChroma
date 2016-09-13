package com.pavelsikun.vintagechroma;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.preference.Preference;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;


import com.pavelsikun.vintagechroma.colormode.ColorMode;

/**
 * Created by Pavel Sikun on 31.03.16.
 * Modified by Jeremy JAMET on 12/09/16.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ChromaPreference extends Preference implements OnColorSelectedListener {

    private ImageView colorPreview;

    private static final int DEFAULT_COLOR = Color.WHITE;
    private static final ColorMode DEFAULT_COLOR_MODE = ColorMode.RGB;
    private static final IndicatorMode DEFAULT_INDICATOR_MODE = IndicatorMode.DECIMAL;
    private static final ShapePreviewPreference DEFAULT_SHAPE_PREVIEW = ShapePreviewPreference.CIRCLE;
    private static final String COLOR_TAG_SUMMARY = "[color]";

    private int color;
    private ColorMode colorMode;
    private IndicatorMode indicatorMode;
    private ShapePreviewPreference shapePreviewPreference;
    private CharSequence summaryPreference;

    private OnColorSelectedListener listener;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ChromaPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    public ChromaPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public ChromaPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ChromaPreference(Context context) {
        super(context);
        init(null);
    }

    private void init(AttributeSet attrs) {
        setWidgetLayoutResource(R.layout.preference_layout);
        loadValuesFromXml(attrs);
        updatePreview();
    }

    private void loadValuesFromXml(AttributeSet attrs) {
        if(attrs == null) {
            setColor(DEFAULT_COLOR);
            colorMode = DEFAULT_COLOR_MODE;
            indicatorMode = DEFAULT_INDICATOR_MODE;
            shapePreviewPreference = DEFAULT_SHAPE_PREVIEW;
            summaryPreference = COLOR_TAG_SUMMARY;
        }
        else {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ChromaPreference);
            try {
                color = a.getColor(R.styleable.ChromaPreference_chromaInitialColor, DEFAULT_COLOR);

                colorMode = ColorMode.values()[
                        a.getInt(R.styleable.ChromaPreference_chromaColorMode,
                                DEFAULT_COLOR_MODE.ordinal())];

                indicatorMode = IndicatorMode.values()[
                        a.getInt(R.styleable.ChromaPreference_chromaIndicatorMode,
                                DEFAULT_INDICATOR_MODE.ordinal())];

                shapePreviewPreference = ShapePreviewPreference.values()[
                        a.getInt(R.styleable.ChromaPreference_chromaShapePreview,
                                DEFAULT_SHAPE_PREVIEW.ordinal())];

                summaryPreference = getSummary();
            }
            finally {
                a.recycle();
            }
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);
        color = getPersistedInt(color);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        colorPreview = (ImageView) view.findViewById(R.id.colorPreview);
        updatePreview();

        if(!isEnabled()) {
            colorPreview.getDrawable().mutate().setColorFilter(Color.LTGRAY, PorterDuff.Mode.MULTIPLY);
        }
    }

    private void updatePreview() {
        if(colorPreview != null) {
            // Update shape of color preview
            switch (shapePreviewPreference) {
                case CIRCLE:
                    colorPreview.setImageResource(R.drawable.circle);
                    break;
                case SQUARE:
                    colorPreview.setImageResource(R.drawable.square);
                    break;
                case ROUNDED_SQUARE:
                    colorPreview.setImageResource(R.drawable.rounded_square);
                    break;
            }
            // Update color
            colorPreview
                    .getDrawable()
                    .mutate()
                    .setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }
        setSummary(summaryPreference);
    }

    @Override
    protected void onClick() {
        new ChromaDialog.Builder()
                .initialColor(color)
                .colorMode(colorMode)
                .indicatorMode(indicatorMode)
                .onColorSelected(this)
                .create().show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "colorPicker");
    }

    @Override
    protected boolean persistInt(int value) {
        color = value;
        updatePreview();
        return super.persistInt(value);
    }

    @Override
    public void onColorSelected(@ColorInt int color) {
        persistInt(color);

        if(listener != null) {
            listener.onColorSelected(color);
        }
    }

    @Override
    /**
     * If [color] is present in summary, it's replaced by string of color
     */
    public void setSummary(CharSequence summary) {
        String summaryWithColor = null;
        if(summary != null) {
            summaryWithColor = summary.toString().replace(COLOR_TAG_SUMMARY,
                    ChromaUtil.getFormattedColorString(color, colorMode == ColorMode.ARGB));
        }
        super.setSummary(summaryWithColor);
    }

    public int getColor() {
        return color;
    }

    public void setColor(@ColorInt int color) {
        persistInt(color);
        notifyChanged();
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }

    public ColorMode getColorMode() {
        return colorMode;
    }

    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
        notifyChanged();
    }

    public IndicatorMode getIndicatorMode() {
        return indicatorMode;
    }

    public void setIndicatorMode(IndicatorMode indicatorMode) {
        this.indicatorMode = indicatorMode;
        notifyChanged();
    }

    public ShapePreviewPreference getShapePreviewPreference() {
        return shapePreviewPreference;
    }

    public void setShapePreviewPreference(ShapePreviewPreference shapePreviewPreference) {
        this.shapePreviewPreference = shapePreviewPreference;
        notifyChanged();
    }
}
