package nl.framegengine.core.fonts.fontRendering;

import nl.framegengine.core.debugging.RenderMetrics;
import nl.framegengine.core.fonts.fontMeshCreator.FontType;
import nl.framegengine.core.fonts.fontMeshCreator.GUIText;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.List;
import java.util.Map;

public class FontRenderer {

    private final FontShader shader;

    private RenderMetrics metrics;
    private boolean recordMetrics = false;

    public FontRenderer(){
        try {
            shader = new FontShader();
            shader.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void render(Map<FontType, List<GUIText>> texts){
        if (recordMetrics) metrics.recordShaderBind();
        prepare();
        for(FontType font : texts.keySet()){
            if (recordMetrics) metrics.recordStateChange();

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, font.getTextureAtlas());
            for(GUIText text : texts.get(font)){
                if (recordMetrics) metrics.recordDrawCall();
                renderText(text);
            }
        }
        endRendering();
    }

    private void renderText(GUIText text){
        GL30.glBindVertexArray(text.getMesh());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        shader.setColor(text.getColor());
        shader.setPosition(text.getPosition());
        shader.prepare();
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, text.getVertexCount());
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL30.glBindVertexArray(0);
    }

    public void cleanUp(){
        shader.cleanUp();
    }

    private void prepare(){
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        shader.bind();
    }

    private void endRendering(){
        shader.unbind();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public void setMetrics(RenderMetrics metrics){
        this.metrics = metrics;
        recordMetrics = true;
    }

    public void recordMetrics(boolean recordMetrics) {
        this.recordMetrics = recordMetrics;
    }
}
