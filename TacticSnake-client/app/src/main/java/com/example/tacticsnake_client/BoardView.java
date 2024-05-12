package com.example.tacticsnake_client;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BoardView extends View {
    private int mColumns = 8;
    private int mRows = 8;
    private float mRectangleSize;
    private Paint mPaint;
    private Paint whitePaint;
    private Paint greenPaint;

    //Board for sprites
    private List<List<Integer>> board;
    private List<List<Bitmap>> sBoard;
    private List<Bitmap> snakeParts = new ArrayList<>();

    private ObjectAnimator mFadeInOutAnimator;
    private int mSelectedRow;
    private int mSelectedColumn;

    private int diagonalState;
    private int jumpState;

    private long mLastClickTime = 0;

    private List<Point> validMoves = new ArrayList<>();
    private Point snakeHead = new Point();
    private boolean myTurn;

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

        //Board size
        setBoardSize(mRows, mColumns);
        mFadeInOutAnimator.start();
    }

    public void setBoardSize(int rows, int cols) {
        mRows = rows;
        mColumns = cols;

        sBoard = new ArrayList<>();
        board = new ArrayList<>();
        for (int i = 0; i < mRows; i++) {
            sBoard.add(new ArrayList<>());
            board.add(new ArrayList<>());
            for (int j = 0; j < mColumns; j++) {
                sBoard.get(i).add(null);
                board.get(i).add(0);
            }
        }
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
                if (isMyTurn() && i == mSelectedRow && j == mSelectedColumn) {
                    mPaint.setColor(getResources().getColor(R.color.dracula_selection));
                } else {
                    mPaint.setColor(getResources().getColor(R.color.dracula_bg));
                }
                canvas.drawRect(j * mRectangleSize, i * mRectangleSize, (j + 1) * mRectangleSize, (i + 1) * mRectangleSize, mPaint);
                canvas.drawRect(j * mRectangleSize, i * mRectangleSize, (j + 1) * mRectangleSize, (i + 1) * mRectangleSize, whitePaint);

                //SNAKE DRAW
                if (sBoard.get(i).get(j) != null) {
                    canvas.drawBitmap(sBoard.get(i).get(j), j * mRectangleSize, i * mRectangleSize, mPaint);
                }
            }
        }

        //BLINKING CIRCLE
        greenPaint.setAlpha(mCircleAlpha);
        float centerX = snakeHead.x * mRectangleSize + mRectangleSize / 2;
        float centerY = snakeHead.y * mRectangleSize + mRectangleSize / 2;
        float radius = mRectangleSize / 4;

        for (Point p : validMoves) {
            if (snakeHead.x + p.x < mColumns && snakeHead.x + p.x >= 0 && snakeHead.y + p.y < mRows && snakeHead.y + p.y >= 0 && board.get(snakeHead.y + p.y).get(snakeHead.x + p.x) == 0) {
                canvas.drawCircle(centerX + (mRectangleSize * p.x), centerY + (mRectangleSize * p.y), radius, greenPaint);
            }
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
        for (int[] direction : directions) {
            int adjX = snakeHead.x + direction[0];
            int adjY = snakeHead.y + direction[1];
            if (adjY >= 0 && adjY < mRows && adjX >= 0 && adjX < mColumns && board.get(adjY).get(adjX) == 0) {
                validMoves.add(new Point(direction[0], direction[1]));
            }
        }
    }

    public Bitmap createSprite(int snakeRotation, int[] snakeColor, int sprite, int bodyMirror) {
        Bitmap snake = Bitmap.createScaledBitmap(snakeParts.get(sprite), (int) mRectangleSize, (int) mRectangleSize, false);
        Matrix matrix = new Matrix();
        if (bodyMirror > 0) matrix.setScale(1, -1);
        matrix.setRotate(snakeRotation, snake.getWidth() / 2, snake.getHeight() / 2);
        snake = Bitmap.createBitmap(snake, 0, 0, snake.getWidth(), snake.getHeight(), matrix, true);
        Bitmap newBitmap = Bitmap.createBitmap(snake.getWidth(), snake.getHeight(), snake.getConfig());

        for (int x = 0; x < snake.getWidth(); x++) {
            for (int y = 0; y < snake.getHeight(); y++) {
                int pixel = snake.getPixel(x, y);
                int alpha = Color.alpha(pixel);
                if (Color.red(pixel) == 255 && Color.green(pixel) == 255 && Color.blue(pixel) == 255) {
                    int newPixel = Color.argb(alpha, snakeColor[0], snakeColor[1], snakeColor[2]);
                    newBitmap.setPixel(x, y, newPixel);
                } else if (pixel == Color.RED) {
                    newBitmap.setPixel(x, y, Color.RED);
                }
            }
        }
        return newBitmap;
    }

    public void placeSnake(int[] snakePos, int snakeRotation, int[] snakeColor, int sprite, int bodyMirror) {
        Bitmap bitmap = createSprite(snakeRotation, snakeColor, sprite, bodyMirror);
        sBoard.get(snakePos[1]).set(snakePos[0], bitmap);
        board.get(snakePos[1]).set(snakePos[0], 1);
    }

    public void placeDeadHead(int[] headPos, int snakeRotation, int[] snakeColor, int snakeBuried) {
        int sprite = snakeBuried > 0 ? 6 : 7;
        Bitmap bitmap = createSprite(snakeRotation*90, snakeColor, sprite, 0);
        sBoard.get(headPos[1]).set(headPos[0], bitmap);
    }

    public void removeSnake(List<int[]> moves) {
        for (int[] move : moves) {
            board.get(move[1]).set(move[0], 0);
        }
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        AtomicInteger counter = new AtomicInteger(0);

        // Define the task to remove the snake for each move
        Runnable removeSnakeRunnable = () -> {
            int[] move = moves.get(counter.getAndIncrement());
            if (move != null) {
                sBoard.get(move[1]).set(move[0], null);
                if (counter.get() >= moves.size()) {
                    executor.shutdown();
                }
            }
        };

        // Schedule the countdown task to execute every second
        executor.scheduleAtFixedRate(removeSnakeRunnable, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void changeMoveBtnStyle() {
        boolean moveState = false;
        for (Point vm : validMoves) {
            if (vm.x + snakeHead.x == mSelectedColumn && vm.y + snakeHead.y == mSelectedRow) {
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

        mSelectedRow = 0;
        mSelectedColumn = 0;
        ((GameActivity) getContext()).setStartingSnakes();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 250){
            return false;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        mSelectedRow = (int)(event.getY() / mRectangleSize);
        mSelectedColumn = (int)(event.getX() / mRectangleSize);

        if (myTurn) changeMoveBtnStyle();

        return true;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
        GameActivity parentActivity = (GameActivity) getContext();
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isMyTurn()) {
                    evaluateValidMoves(diagonalState, jumpState);
                } else {
                    validMoves.clear();
                }
            }
        });
    }

    public Point getSnakeHead() {
        return snakeHead;
    }

    public void setSnakeHead(Point snakeHead) {
        this.snakeHead = snakeHead;
    }

    public void setDiagonalState(int diagonalState) {
        this.diagonalState = diagonalState;
    }

    public void setJumpState(int jumpState) {
        this.jumpState = jumpState;
    }

    public int getmSelectedRow() {
        return mSelectedRow;
    }

    public int getmSelectedColumn() {
        return mSelectedColumn;
    }

    public boolean isMyTurn() {
        return myTurn;
    }
}

