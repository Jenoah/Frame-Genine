package nl.framegengine.core.shaders.postProcessing;

import nl.framegengine.core.shaders.Shader;
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
