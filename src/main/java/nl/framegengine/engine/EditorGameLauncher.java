package nl.framegengine.engine;

import nl.jenoah.core.EngineManager;
import nl.jenoah.core.GameInstance;
import nl.jenoah.core.Settings;
import nl.jenoah.core.WindowManager;
import nl.jenoah.core.utils.Constants;

public class EditorGameLauncher {
    private EngineManager engine;

    public void run(int width, int height) {
        WindowManager.createInstance(Constants.TITLE, width, height, Settings.isUseVSync(), false);
        GameInstance game = new GameInstance();
        engine = new EngineManager();

        try{
            engine.start(game, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void render(){
        engine.run();
    }
}
