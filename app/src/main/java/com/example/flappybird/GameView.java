package com.example.flappybird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.widget.AppCompatDrawableManager;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback , SensorEventListener {

    private float measuredWidth;

    private float measuredHeight;

    private SurfaceHolder surfaceHolder;

    private Paint paint;

    private Bitmap bitmap;

    // The colors
    private static final int colorPipe = Color.parseColor("#C75B39");

    // The current score
    private int score = 0;

    public int getScore() {
        return score;
    }

    // For the bird
    private float positionX = 0.0f;
    private float positionY = 0.0f;
    private float velocityX = 0.0f;
    private float velocityY = 0.0f;
    private float accelerationX = 0.0f;
    private float accelerationY = 0.7f;

    // For the pipes
    private int iteratorInt = 0;
    private static final int interval = 150;
    private static final float gap = 450.0f;
    private static final float base = 100.0f;
    private float pipeWidth = 100.0f;
    private List<Pipe> pipeList;
    private static final float pipeVelocity = 4.0f;

    public GameView(Context context) {
        super(context);

        // Initialize
        init();
    }

    public GameView(Context context, AttributeSet a) {
        super(context, a);

        // Initialize
        init();
    }

    public GameView(Context context, AttributeSet a, int b) {
        super(context, a, b);

        // Initialize
        init();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Lấy đối tượng SensorManager
        SensorManager sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);

        // Đăng ký lắng nghe cảm biến gia tốc
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Lấy đối tượng SensorManager
        SensorManager sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);

        // Hủy đăng ký lắng nghe cảm biến gia tốc
        sensorManager.unregisterListener(this);
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        setZOrderOnTop(true);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        paint = new Paint();
        paint.setAntiAlias(true);

        // For the bird
        bitmap = getBitmapFromVectorDrawable(getContext(), R.drawable.img_bird);
        bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);

        // For the pipes
        pipeList = new ArrayList<Pipe>();

        setKeepScreenOn(true);
    }

    /**
     * Updates the UI.
     */
    public void update() {
        paint.setStyle(Paint.Style.FILL);

        Canvas canvas = surfaceHolder.lockCanvas();
        canvas.drawText(""+getScore(),10,10,new Paint(){{
            setColor(Color.RED);
            setStrokeWidth(3);
            setTextSize(30);

        }});

        // Clear the canvas
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // Draw the bird
        canvas.drawBitmap(bitmap, positionX - 100.0f / 2.0f, positionY - 100.0f / 2.0f, null);

        // Draw the pipes
        paint.setColor(colorPipe);
        List<Integer> removeList = new ArrayList<>();
        int size = pipeList.size();
        for (int index = 0; index < size; index++) {
            Pipe pipe = pipeList.get(index);
            if (isPipeOut(pipe)) {
                removeList.add(index);
            } else {
                // Draw the upper part of the pipe
                canvas.drawRect(pipe.getPositionX() - pipeWidth / 2.0f,
                        0.0f,
                        pipe.getPositionX() + pipeWidth / 2.0f,
                        measuredHeight - pipe.getHeight() - gap,
                        paint);

                // Draw the lower part of the pipe
                canvas.drawRect(pipe.getPositionX() - pipeWidth / 2.0f,
                        measuredHeight - pipe.getHeight(),
                        pipe.getPositionX() + pipeWidth / 2.0f,
                        measuredHeight,
                        paint);
            }
        }
        removeItemsFromPipeList(removeList);

        surfaceHolder.unlockCanvasAndPost(canvas);

        // Update the data for the bird
        positionX += velocityX;
        positionY += velocityY;
        velocityX += accelerationX;
//        velocityY += accelerationY;
        // Only accelerate velocityY when it is not too large
        if (velocityY <= 10.0F) {
            velocityY += accelerationY;
        }

        // Update the data for the pipes
        for (Pipe pipe : pipeList) {
            pipe.setPositionX(pipe.getPositionX() - pipeVelocity);
        }
        if (iteratorInt == interval) {
            addPipe();
            iteratorInt = 0;
        } else {
            iteratorInt++;
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Get the measured size of the view
        measuredWidth = getMeasuredWidth();
        measuredHeight = getMeasuredHeight();

        // Set the initial position
        setPosition(measuredWidth / 2.0f, measuredHeight / 2.0f);

        // Add the initial pipe
        addPipe();
    }

    public void jump() {
        velocityY = -13.0f;
    }

    public void setPosition(float positionX, float positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Returns true if the bird is still alive, false otherwise.
     *
     * @return True if the bird is still alive, false otherwise.
     */
    public boolean isAlive() {
        // Check if the bird hits the pipes
        for (Pipe pipe : pipeList) {
            if ((pipe.getPositionX() >= measuredWidth / 2.0f - pipeWidth / 2.0f - 100.0f / 2.0f) &&
                    (pipe.getPositionX() <= measuredWidth / 2.0f + pipeWidth / 2.0f + 100.0f / 2.0f)) {
                if ((positionY <= measuredHeight - pipe.getHeight() - gap + 50.0f / 2.0f) ||
                        (positionY >= measuredHeight - pipe.getHeight() - 50.0f / 2.0f)) {
                    return false;
                } else {
                    if (pipe.getPositionX() - pipeVelocity <
                            measuredWidth / 2.0f - pipeWidth / 2.0f - 100.0f / 2.0f) {
                        score++;


                        // Update the score in MainActivity
                        Context context = getContext();
                        if (context instanceof GameActivity) {
                            ((GameActivity) context).updateScore(getScore());
                            ((GameActivity) context).playScoreMusic();
                        }
                        if (context instanceof GameFriendActivity) {
                            ((GameFriendActivity) context).updateScore(getScore());
                            ((GameFriendActivity) context).playScoreMusic();
                        }
                    }
                }
            }
        }

        // Check if the bird goes beyond the border
        if ((positionY < 0.0f + 100.0f / 2.0f) || (positionY > measuredHeight - 100.0f / 2.0f)) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the pipe is out of the screen, false otherwise.
     *
     * @param pipe The pipe to be judged.
     *
     * @return True if the pipe is out of the screen, false otherwise.
     */
    private boolean isPipeOut(Pipe pipe) {
        return (pipe.getPositionX() + pipeWidth / 2.0f) < 0.0f;
    }

    /**
     * Removes all the items at the indices specified by removeList.
     *
     * @param removeList The list of indices.
     */
    private void removeItemsFromPipeList(List<Integer> removeList) {
        List<Pipe> newList = new ArrayList<>();
        int size = pipeList.size();
        for (int index = 0; index < size; index++) {
            if (!removeList.remove(Integer.valueOf(index))) {
                newList.add(pipeList.get(index));
            }
        }

        pipeList = newList;
    }

    /**
     * Resets all the data of the over game.
     */
    public void resetData() {
        // For the bird
        positionX = 0.0f;
        positionY = 0.0f;
        velocityX = 0.0f;
        velocityY = 0.0f;
        accelerationX = 0.0f;
        accelerationY = 0.7f;

        // For the pipes
        iteratorInt = 0;
        pipeList = new ArrayList<>();

        score = 0;

        // Set the initial position
        setPosition(measuredWidth / 2.0f, measuredHeight / 2.0f);

        // Add the initial pipe
        addPipe();
    }

    /**
     * Adds a pipe into the list of pipes.
     */
    private void addPipe() {
        pipeList.add(new Pipe(measuredWidth + pipeWidth / 2.0f,
                base + (measuredHeight - 2 * base - gap) * new Random().nextFloat()));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

//        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//        // Lấy đối tượng WindowManager
//        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//
//        // Kiểm tra trạng thái xoay của màn hình
//        int rotation = windowManager.getDefaultDisplay().getRotation();
//
//        // Xác định trạng thái xoay ngang hoặc xoay ngược của thiết bị
//        if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270) {
//            // Màn hình đang xoay ngược
//            // Thực hiện việc đổi ảnh chim tại đây khi màn hình bị xoay ngược
//            // Ví dụ: bird.setImageBitmap(newBitmap);
//            bitmap = getBitmapFromVectorDrawable(getContext(), R.drawable.bird_nguoc);
//            bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
//        } else {
//            // Màn hình không bị xoay ngược
//            // Thực hiện các thao tác khác tại đây khi màn hình không bị xoay ngược
//            bitmap = getBitmapFromVectorDrawable(getContext(), R.drawable.img_bird);
//            bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
//        }
//            // Yêu cầu vẽ lại GameView
//            //invalidate();
//            // Cập nhật lại giao diện người dùng
//            update();
//    }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];

            if (Math.abs(x) > Math.abs(y)) {
                if (x < 0) {
                    // Thiết bị đang ở chế độ xoay ngang ngược
                    bitmap = getBitmapFromVectorDrawable(getContext(), R.drawable.img_bird_nguoc);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                } else {
                    // Thiết bị đang ở chế độ xoay ngang bình thường
                    bitmap = getBitmapFromVectorDrawable(getContext(), R.drawable.img_bird);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                }
            } else {
                // Thiết bị đang ở chế độ dọc
                if (y < 0) {
                    // Thiết bị đang ở chế độ xoay dọc ngược
                    bitmap = getBitmapFromVectorDrawable(getContext(), R.drawable.img_bird_nguoc);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                } else {
                    // Thiết bị đang ở chế độ xoay dọc bình thường
                    bitmap = getBitmapFromVectorDrawable(getContext(), R.drawable.img_bird);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                }
            }

            // Cập nhật lại giao diện người dùng
            update();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

