package nl.jenoah.core.shaders.postProcessing;

import nl.jenoah.core.shaders.Shader;
import nl.jenoah.core.utils.Utils;
import org.joml.Vector2f;

public class PPFXFxaaShader extends Shader {
    public PPFXFxaaShader() throws Exception {
        super();
    }

    public void init() throws Exception {
        loadVertexShaderFromFile("/shaders/postProcessing/ppfxGeneric.vs");
        loadFragmentShaderFromFile("/shaders/postProcessing/ppfxFxaa.fs");
        link();
        super.init();
    }

    @Override
    public void createRequiredUniforms() throws Exception {
        createUniform("resolution");
    }

    public void setResolution(Vector2f resolution){
        setUniform("resolution", resolution);
    }
}
