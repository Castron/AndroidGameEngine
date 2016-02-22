package dk.kea.andriod2016spring.benjamin.gameengine;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;

import java.util.Random;

/**
 * Created by Benjamin on 17-02-2016.
 */
public class SimpleScreen extends Screen
{
    Bitmap bitmap;
    int x = 0;
    int y = 0;
    Random rand = new Random();
    int clearColor = Color.RED;

    public SimpleScreen(Game game)
    {
        super(game);
        bitmap = game.loadBitmap("bob.png");
    }

    public void update(float deltaTime)
    {
        game.clearFrameBugger(clearColor);
        game.drawBitmap(bitmap, 10, 10);
        game.drawBitmap(bitmap, 100, 140, 0, 0, 64, 64);
        if (game.isKeyPressed(KeyEvent.KEYCODE_BACK))
        {
            clearColor = rand.nextInt();
        }
    }

    public void pause()
    {
        Log.d("Simple Screen", "We are pausing");
    }
    public void resume()
    {
        Log.d("Simple Screen", "We are resuming");
    }
    public void dispose()
    {
        Log.d("Simple Screen", "We are diposing the game");
    }
}
