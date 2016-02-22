package dk.kea.andriod2016spring.benjamin.gameengine;

/**
 * Created by Benjamin on 17-02-2016.
 */
public class SimpleGame extends Game
{
    @Override
    public Screen createStartScreen()
    {
        return new SimpleScreen(this);
    }
}
