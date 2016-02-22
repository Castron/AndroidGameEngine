package dk.kea.andriod2016spring.benjamin.gameengine;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class Game extends Activity implements Runnable, View.OnKeyListener
{
    private Thread mainLoopThread;
    private State state = State.Paused;
    private List<State> stateChanges = new ArrayList<>();
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Screen screen;
    private Canvas canvas;
    private Bitmap offscreenSurface;
    private boolean pressedKeys[] = new boolean[256];

    public abstract Screen createStartScreen();

    protected void onCreate(Bundle instanceBundle)
    {
        super.onCreate(instanceBundle);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Probably not needed, addFlags() calls setFlags()
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        surfaceView = new SurfaceView(this);
        setContentView(surfaceView);
        surfaceHolder = surfaceView.getHolder();
        screen = createStartScreen();
        if (surfaceView.getWidth() > surfaceView.getHeight())
        {
            setOffscreenSurface(480, 320);
        }else
        {
            setOffscreenSurface(320, 480);
        }
        surfaceView.setFocusableInTouchMode(true);
        surfaceView.requestFocus();
        surfaceView.setOnKeyListener(this);
    }

    public void setOffscreenSurface(int width, int height)
    {
        if (offscreenSurface != null)
        {
            offscreenSurface.recycle();
        }
        offscreenSurface = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        canvas = new Canvas(offscreenSurface);
    }

    public void setScreen(Screen screen)
    {
        if (this.screen != null) this.screen.dispose();
            this.screen = screen;

    }

    public Bitmap loadBitmap(String fileName)
    {
        InputStream in = null;
        Bitmap bitmap = null;
        try
        {
            in = getAssets().open(fileName);
            bitmap = BitmapFactory.decodeStream(in);
            if (bitmap == null)
            {
                throw new RuntimeException(
                        "Could not get a bitmap from the file" + fileName
                );
            }
            return bitmap;
        }catch (IOException e)
        {
            throw new RuntimeException("Could not load the file" + fileName);
        }finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }catch (IOException e)
                {
                    Log.d("Closing inputstream", "Not good");
                }
            }
        }
    }

    /*
        public Music loadMusic(String fileName)
        {
            return null;
        }

        public Sound loadSound(String fileName)
        {
            return null;
        }
    */
    public void clearFrameBugger(int color)
    {
        if(canvas != null) canvas.drawColor(color);
    }

    public int getFameBufferWidth()
    {
        return surfaceView.getWidth();
    }

    public int getFrameBufferHeight()
    {
        return surfaceView.getHeight();
    }

    public void drawBitmap(Bitmap bitmap, int x, int y)
    {
        if (canvas != null)
        {
            canvas.drawBitmap(bitmap, x, y, null);
        }
    }

    Rect src = new Rect();
    Rect dst = new Rect();
    public void drawBitmap(Bitmap bitmap, int x, int y, int srcX, int srcY, int srcWidth, int
            srcHeight)
    {
        if (canvas == null)
        {
            return;
        }
        src.left = srcX;
        src.top = srcY;
        src.right = srcX + srcWidth;
        src.bottom = srcY + srcHeight;

        dst.left = x;
        dst.top = y;
        dst.right = x + srcWidth;
        dst.bottom = y + srcHeight;

        canvas.drawBitmap(bitmap, src, dst, null);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            pressedKeys[keyCode] = true;
        } else if (event.getAction() == KeyEvent.ACTION_UP)
        {
            pressedKeys[keyCode] = false;
        }
        return false;
    }

    public boolean isKeyPressed(int keyCode)
    {
        return pressedKeys[keyCode];
    }

    public boolean isTouchDown(int pointer)
    {
        return false;
    }

    public int getTouchX(int pointer)
    {
        return 0;
    }

    public int getTouchY(int pointer)
    {
        return 0;
    }

    /*
        public List<KeyEvent> getKeyEvent()
        {
            return null;
        }
    */
    public float[] getAccelerometer()
    {
        return null;
    }

    //This is the main method for the game Loop
    public void run()
    {
        while (true)
        {
            synchronized (stateChanges)
            {
                for (int i = 0; i < stateChanges.size(); i++)
                {
                    state = stateChanges.get(i);
                    if (state == State.Disposed)
                    {
                        if (screen!= null)screen.dispose();
                        Log.d("Game", "State is disposed");
                    } else if (state == State.Paused)
                    {
                        if (screen != null) screen.pause();
                        Log.d("Game", "State is Paused");
                    } else if (state == State.Resumed)
                    {
                        if (screen != null)screen.resume();
                        state = State.Running;
                        Log.d("Game", "State is Resumed -> Running");
                    }
                }
                stateChanges.clear();
            }
            if (state == State.Running)
            {
                if (!surfaceHolder.getSurface().isValid()) continue;
                Canvas physicalCanvas = surfaceHolder.lockCanvas();
                //Here we should do some drawing on the screen
                //canvas.drawColor(Color.YELLOW); //Screen test
                if (screen != null)
                {
                    screen.update(0);
                }

                src.left = 0;
                src.top = 0;
                src.right = offscreenSurface.getWidth() - 1;
                src.bottom = offscreenSurface.getHeight() - 1;

                dst.left = 0;
                dst.top = 0;
                dst.right = surfaceView.getWidth();
                dst.bottom = surfaceView.getHeight();

                physicalCanvas.drawBitmap(offscreenSurface, src, dst, null);
                surfaceHolder.unlockCanvasAndPost(physicalCanvas);

                physicalCanvas = null;
            }
        }
    }

    public void onPause()
    {
        super.onPause();
        synchronized (stateChanges)
        {
            if (isFinishing())
            {
                stateChanges.add(stateChanges.size(), State.Disposed);
            } else
            {
                stateChanges.add(stateChanges.size(), State.Paused);
            }
        }
        try
        {
            mainLoopThread.join();
        } catch (InterruptedException e)
        {

        }
    }

    public void onResume()
    {
        super.onResume();
        mainLoopThread = new Thread(this);
        mainLoopThread.start();
        synchronized (stateChanges)
        {
            stateChanges.add(stateChanges.size(), State.Resumed);
        }
    }
}
