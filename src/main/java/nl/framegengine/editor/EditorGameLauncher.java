package nl.framegengine.editor;

import nl.framegengine.core.EngineManager;
import nl.framegengine.core.GameInstance;
import nl.framegengine.core.Settings;
import nl.framegengine.core.WindowManager;
import nl.framegengine.core.utils.Constants;

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

    public void stop(){
        engine.stop();
        engine = null;
    }
}
