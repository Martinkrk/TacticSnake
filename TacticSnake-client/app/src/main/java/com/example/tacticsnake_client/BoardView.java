package com.example.tacticsnake_client;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.*;

public class BoardView extends View {
    private boolean testfirsttimesnakeplacement = true;
    private final int mColumns = 8;
    private final int mRows = 8;
    private float mRectangleSize;
    private Paint mPaint;
    private Paint whitePaint;
    private Paint greenPaint;

    //Board for sprites
    private List<List<Integer>> board = new ArrayList<>();
    private List<List<Bitmap>> sBoard = new ArrayList<>();
    private List<Bitmap> snakeParts = new ArrayList<>();
    private Matrix matrix = new Matrix();

    private ObjectAnimator mFadeInOutAnimator;
    private int mSelectedRow;
    private int mSelectedColumn;

    private int diagonalState;
    private int jumpState;

    private long mLastClickTime = 0;

    private List<Point> validMoves = new ArrayList<>();
    private Point snakeHead = new Point();

    public BoardView(Context context) {
        super(context);
        init();
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.dracula_bg));
        whitePaint = new Paint();
        whitePaint.setColor(getResources().getColor(R.color.dracula_fg));
        whitePaint.setStyle(Paint.Style.STROKE);
        whitePaint.setStrokeWidth(1);

        snakeParts.add(BitmapFactory.decodeResource(getResources(), R.drawable.head));
        snakeParts.add(BitmapFactory.decodeResource(getResources(), R.drawable.connectedhead));
        snakeParts.add(BitmapFactory.decodeResource(getResources(), R.drawable.body));
        snakeParts.add(BitmapFactory.decodeResource(getResources(), R.drawable.bodyturn));
        snakeParts.add(BitmapFactory.decodeResource(getResources(), R.drawable.bodyburied));
        snakeParts.add(BitmapFactory.decodeResource(getResources(), R.drawable.bodyreburied));
        snakeParts.add(BitmapFactory.decodeResource(getResources(), R.drawable.headdead));
        snakeParts.add(BitmapFactory.decodeResource(getResources(), R.drawable.connectedheaddead));
        snakeParts.add(BitmapFactory.decodeResource(getResources(), R.drawable.tail));

        greenPaint = new Paint();
        greenPaint.setColor(Color.GREEN);

        mFadeInOutAnimator = ObjectAnimator.ofInt(this, "circleAlpha", 0, 255);
        mFadeInOutAnimator.setDuration(1000);
        mFadeInOutAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mFadeInOutAnimator.setRepeatCount(ValueAnimator.INFINITE);

        //Boards
        for (int i = 0; i < mRows; i++) {
            sBoard.add(new ArrayList<>());
            board.add(new ArrayList<>());
            for (int j = 0; j < mColumns; j++) {
                sBoard.get(i).add(null);
                board.get(i).add(0);
            }
        }

        mFadeInOutAnimator.start();
    }

    private int mCircleAlpha = 0;

    public int getCircleAlpha() {
        return mCircleAlpha;
    }

    public void setCircleAlpha(int alpha) {
        mCircleAlpha = alpha;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < mRows; i++) {
            for (int j = 0; j < mColumns; j++) {

                //BOARD
                if (i == mSelectedRow && j == mSelectedColumn) {
                    mPaint.setColor(getResources().getColor(R.color.dracula_selection));
                } else {
                    mPaint.setColor(getResources().getColor(R.color.dracula_bg));
                }
                canvas.drawRect(i * mRectangleSize, j * mRectangleSize, (i + 1) * mRectangleSize, (j + 1) * mRectangleSize, mPaint);
                canvas.drawRect(i * mRectangleSize, j * mRectangleSize, (i + 1) * mRectangleSize, (j + 1) * mRectangleSize, whitePaint);

                //SNAKE DRAW
                if (board.get(i).get(j) > 0) {
                    canvas.drawBitmap(sBoard.get(i).get(j), i * mRectangleSize, j * mRectangleSize, mPaint);
                }
            }
        }

        //BLINKING CIRCLE
        greenPaint.setAlpha(mCircleAlpha);
        float centerX = snakeHead.x * mRectangleSize + mRectangleSize / 2;
        float centerY = snakeHead.y * mRectangleSize + mRectangleSize / 2;
        float radius = mRectangleSize / 4;

        int index = 0;
        for (Point p : validMoves) {
            if (board.get(snakeHead.x + p.x).get(snakeHead.y + p.y) == 0) {
                canvas.drawCircle(centerX + (mRectangleSize * p.x), centerY + (mRectangleSize * p.y), radius, greenPaint);
            }
            index++;
        }
    }

    public void evaluateValidMoves(int ds, int js) {
        if (snakeHead == null) return;
        validMoves.clear();
        int[][] directions;
        diagonalState = ds;
        jumpState = js;

        if (jumpState == 1 && diagonalState == 1) {
            directions = new int[][] {{1, 2}, {2, 1}, {1, -2}, {2, -1}, {-1, 2}, {-2, 1}, {-1, -2}, {-2, -1}, };
        } else if (jumpState == 1) {
            directions = new int[][] {{-2, 0}, {2, 0}, {0, -2}, {0, 2}};
        } else if (diagonalState == 1) {
            directions = new int[][] {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        } else {
            directions = new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        }

        if (jumpState == 0 && diagonalState == 1) {
            int rotation;
            int index = 0;
            for (int[] direction : directions) {
                int adjX = snakeHead.x + direction[0];
                int adjY = snakeHead.y + direction[1];
                if (adjX >= 0 && adjX < mRows && adjY >= 0 && adjY < mColumns && board.get(adjX).get(adjY) == 0 && (board.get(snakeHead.x).get(adjY) == 0 || board.get(adjX).get(snakeHead.y) == 0)) {
                    validMoves.add(new Point(direction[0], direction[1]));
                    index++;
                }
            }
        }
        else {
            for (int[] direction : directions) {
                int adjX = snakeHead.x + direction[0];
                int adjY = snakeHead.y + direction[1];
                if (adjX >= 0 && adjX < mRows && adjY >= 0 && adjY < mColumns && board.get(adjX).get(adjY) == 0) {
                    validMoves.add(new Point(direction[0], direction[1]));
                }
            }
        }
    }

    public void placeSnake() {
        snakeHead.x = mSelectedRow;
        snakeHead.y = mSelectedColumn;

        Bitmap resizedSnake = Bitmap.createScaledBitmap(snakeParts.get(0), (int) mRectangleSize, (int) mRectangleSize, false);

        Matrix matrix = new Matrix();
        matrix.setRotate(90, resizedSnake.getWidth() / 2, resizedSnake.getHeight() / 2);
        Bitmap rotatedSnake = Bitmap.createBitmap(resizedSnake, 0, 0, resizedSnake.getWidth(), resizedSnake.getHeight(), matrix, true);
        Bitmap newBitmap = Bitmap.createBitmap(rotatedSnake.getWidth(), rotatedSnake.getHeight(), rotatedSnake.getConfig());

        for (int x = 0; x < rotatedSnake.getWidth(); x++) {
            for (int y = 0; y < rotatedSnake.getHeight(); y++) {
                int pixel = rotatedSnake.getPixel(x, y);
                int alpha = Color.alpha(pixel);
                if (Color.red(pixel) == 255 && Color.green(pixel) == 255 && Color.blue(pixel) == 255) {
                    int greenPixel = Color.argb(alpha, 0, 255, 0);
                    newBitmap.setPixel(x, y, greenPixel);
                } else if (pixel == Color.RED) {
                    newBitmap.setPixel(x, y, Color.RED);
                }
            }
        }

        sBoard.get(mSelectedRow).set(mSelectedColumn, newBitmap);
        board.get(mSelectedRow).set(mSelectedColumn, 1);
    }

    public void changeMoveBtnStyle() {
        boolean moveState = false;
        for (Point vm : validMoves) {
            if (vm.x + snakeHead.x == mSelectedRow && vm.y + snakeHead.y == mSelectedColumn) {
                moveState = true;
                break;
            }
        }
        ((GameActivity) getContext()).setMoveBtnState(moveState);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        mRectangleSize = Math.min(width / mColumns, height / mRows);
        setMeasuredDimension(width, height);

        //TEST
        if (testfirsttimesnakeplacement) {
            mSelectedRow = 0;
            mSelectedColumn = 0;
            placeSnake();
            evaluateValidMoves(0, 0);
            testfirsttimesnakeplacement = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 250){
            return false;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        mSelectedRow = (int)(event.getX() / mRectangleSize);
        mSelectedColumn = (int)(event.getY() / mRectangleSize);

        changeMoveBtnStyle();

        return true;
    }
}

