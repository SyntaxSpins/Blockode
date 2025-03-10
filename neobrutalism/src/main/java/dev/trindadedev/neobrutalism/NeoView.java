package dev.trindadedev.neobrutalism;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NeoView extends RelativeLayout {

  protected static final int DISABLED_BACKGROUND_COLOR = Color.parseColor("#BDBDBD");
  protected final GradientDrawable foregroundGradientDrawable = new GradientDrawable();
  protected final GradientDrawable backgroundGradientDrawable = new GradientDrawable();
  protected final State state = new State();
  protected RelativeLayout root;
  protected View backgroundView;
  protected View.OnClickListener onClickListener;

  public NeoView(final Context context) {
    super(context);
    init(null);
  }

  public NeoView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public NeoView(final Context context, final AttributeSet attrs, final int defStyleRes) {
    super(context, attrs, defStyleRes);
    init(attrs);
  }

  @SuppressLint("isClickableViewAccessibility")
  @Override
  public void addView(final View child, final int index, final ViewGroup.LayoutParams params) {
    if (child.getId() != R.id.root) {
      child
          .getViewTreeObserver()
          .addOnGlobalLayoutListener(
              new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                  child.setBackground(foregroundGradientDrawable);
                  final LayoutParams params = new LayoutParams(child.getWidth(), child.getHeight());
                  params.setMargins(state.backgroundMarginStart, state.backgroundMarginTop, 0, 0);
                  backgroundView.setLayoutParams(params);
                  child.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
              });
      child.setOnTouchListener(
          (view, event) -> {
            final var action = event.getAction();
            switch (action) {
              case MotionEvent.ACTION_DOWN -> {
                if (state.isClickable && state.isEnabled) {
                  onClickAnimation(child);
                }
                break;
              }
              case MotionEvent.ACTION_UP -> {
                onReleaseAnimation(child);
                if (state.isClickable && state.isEnabled) {
                  onClickListener.onClick(this);
                }
                break;
              }
              case MotionEvent.ACTION_CANCEL -> {
                onReleaseAnimation(child);
                break;
              }
            }
            return true;
          });
    }
    super.addView(child, index, params);
  }

  public void onClickAnimation(final View view) {
    view.animate()
        .translationX(state.backgroundMarginStart)
        .translationY(state.backgroundMarginTop)
        .setDuration(state.animationDuration)
        .start();
  }

  public void onReleaseAnimation(final View view) {
    view.animate().translationX(0).translationY(0).setDuration(state.animationDuration).start();
  }

  @Override
  public void setOnClickListener(final View.OnClickListener onClickListener) {
    this.onClickListener = onClickListener;
  }

  @Override
  public void setEnabled(final boolean isEnabled) {
    if (isEnabled == state.isEnabled) return;
    super.setEnabled(isEnabled);
    state.isEnabled = isEnabled;
  }

  @Override
  public boolean isEnabled() {
    return state.isEnabled;
  }

  private void init(@Nullable final AttributeSet attrs) {
    state.setOnStateValueChange(() -> invalidate());
    state.originalX = getX();
    state.originalY = getY();

    final TypedArray attributes =
        getContext().obtainStyledAttributes(attrs, R.styleable.NeoView, 0, 0);
    state.backgroundMarginTop =
        attributes.getDimensionPixelSize(R.styleable.NeoView_backgroundMarginTop, 6);
    state.backgroundMarginStart =
        attributes.getDimensionPixelSize(R.styleable.NeoView_backgroundMarginStart, 6);
    state.backgroundRadius =
        attributes.getDimensionPixelSize(R.styleable.NeoView_backgroundRadius, 3);
    state.foregroundStrokeWidth =
        attributes.getDimensionPixelSize(R.styleable.NeoView_foregroundStrokeWidth, 3);
    state.backgroundShadowColor =
        attributes.getColor(R.styleable.NeoView_backgroundShadowColor, Color.BLACK);
    state.foregroundColor = attributes.getColor(R.styleable.NeoView_foregroundColor, Color.WHITE);
    state.foregroundStrokeColor =
        attributes.getColor(R.styleable.NeoView_foregroundStrokeColor, Color.BLACK);
    state.isClickable = attributes.getBoolean(R.styleable.NeoView_android_clickable, true);
    state.isEnabled = attributes.getBoolean(R.styleable.NeoView_android_enabled, true);
    attributes.recycle();
    initViews();
  }

  private void initViews() {
    inflate(getContext(), R.layout.neo_view, this);
    root = findViewById(R.id.root);
    backgroundView = findViewById(R.id.background_view);
    final ViewGroup.LayoutParams backgroundViewLayoutParams = backgroundView.getLayoutParams();
    backgroundViewLayoutParams.height = 0;
    backgroundViewLayoutParams.width = 0;
    root.getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                applyBackground();
                configureForeground();
              }
            });
    onClickListener =
        v -> {
          // do nothing, just to avoid crashes
        };
  }

  private void applyBackground() {
    backgroundGradientDrawable.setShape(GradientDrawable.RECTANGLE);
    backgroundGradientDrawable.setCornerRadius(state.backgroundRadius);
    backgroundGradientDrawable.setColor(
        ColorStateList.valueOf(
            state.isEnabled ? state.backgroundShadowColor : DISABLED_BACKGROUND_COLOR));
    backgroundView.setBackground(backgroundGradientDrawable);
  }

  private void configureForeground() {
    foregroundGradientDrawable.setShape(GradientDrawable.RECTANGLE);
    foregroundGradientDrawable.setCornerRadius(state.backgroundRadius);
    foregroundGradientDrawable.setStroke(
        state.foregroundStrokeWidth, ColorStateList.valueOf(state.foregroundStrokeColor));
    foregroundGradientDrawable.setColor(ColorStateList.valueOf(state.foregroundColor));
  }

  public void setBackgroundMargins(final int marginTop, final int marginStart) {
    final LayoutParams params =
        new LayoutParams(backgroundView.getWidth(), backgroundView.getHeight());
    params.setMargins(marginTop, marginStart, 0, 0);
    backgroundView.setLayoutParams(params);
  }

  @NonNull
  public State getState() {
    return state;
  }

  /**
   * data class with all values used in Neobrutalism View.
   *
   * <p>use NeoView#getState() to change values.
   */
  public class State {
    private int backgroundMarginTop = 6;
    private int backgroundMarginStart = 6;
    private int backgroundRadius = 3;
    private int backgroundShadowColor = Color.BLACK;
    private int foregroundColor = Color.WHITE;
    private int foregroundStrokeColor = Color.BLACK;
    private int foregroundStrokeWidth = 3;
    private float originalX = 0f;
    private float originalY = 0f;
    private long animationDuration = 100;
    private boolean isClickable = false;
    private boolean isEnabled = true;
    private OnStateValueChange onStateValueChange;

    public int getBackgroundMarginTop() {
      return this.backgroundMarginTop;
    }

    public void setBackgroundMarginTop(final int backgroundMarginTop) {
      this.backgroundMarginTop = backgroundMarginTop;
    }

    public int getBackgroundMarginStart() {
      return this.backgroundMarginStart;
    }

    public void setBackgroundMarginStart(final int backgroundMarginStart) {
      this.backgroundMarginStart = backgroundMarginStart;
    }

    public int getBackgroundRadius() {
      return this.backgroundRadius;
    }

    public void setBackgroundRadius(final int backgroundRadius) {
      this.backgroundRadius = backgroundRadius;
    }

    public int getBackgroundShadowColor() {
      return this.backgroundShadowColor;
    }

    public void setBackgroundShadowColor(final int backgroundShadowColor) {
      this.backgroundShadowColor = backgroundShadowColor;
    }

    public int getForegroundColor() {
      return this.foregroundColor;
    }

    public void setForegroundColor(final int foregroundColor) {
      this.foregroundColor = foregroundColor;
    }

    public int getForegroundStrokeColor() {
      return this.foregroundStrokeColor;
    }

    public void setForegroundStrokeColor(final int foregroundStrokeColor) {
      this.foregroundStrokeColor = foregroundStrokeColor;
    }

    public int getForegroundStrokeWidth() {
      return this.foregroundStrokeWidth;
    }

    public void setForegroundStrokeWidth(final int foregroundStrokeWidth) {
      this.foregroundStrokeWidth = foregroundStrokeWidth;
    }

    public long getAnimationDuration() {
      return this.animationDuration;
    }

    public void setAnimationDuration(final long animationDuration) {
      this.animationDuration = animationDuration;
    }

    public boolean getIsClickable() {
      return this.isClickable;
    }

    public void setIsClickable(final boolean isClickable) {
      this.isClickable = isClickable;
    }

    public boolean getisEnabled() {
      return this.isEnabled;
    }

    public void setisEnabled(final boolean isEnabled) {
      this.isEnabled = isEnabled;
    }

    public OnStateValueChange getOnStateValueChange() {
      return this.onStateValueChange;
    }

    public void setOnStateValueChange(final OnStateValueChange onStateValueChange) {
      this.onStateValueChange = onStateValueChange;
    }

    public float getOriginalX() {
      return this.originalX;
    }

    public void setOriginalX(final float originalX) {
      this.originalX = originalX;
    }

    public float getOriginalY() {
      return this.originalY;
    }

    public void setOriginalY(final float originalY) {
      this.originalY = originalY;
    }
  }

  @FunctionalInterface
  public interface OnStateValueChange {
    void call();
  }
}
