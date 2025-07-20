package nl.jenoah.core.shaders;

import nl.jenoah.core.entity.Camera;
import nl.jenoah.core.utils.Transformation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DebugShader extends Shader{

    public DebugShader() throws Exception {
        super();
        loadVertexShaderFromFile("/shaders/debug/vertex.vs");
        loadFragmentShaderFromFile("/shaders/debug/fragment.fs");
        link();
    }

    @Override
    public void createRequiredUniforms() throws Exception {
        createUniform("modelMatrix");
        createUniform("viewMatrix");
        createUniform("projectionMatrix");
    }

    public void prepare(Vector3f position, Quaternionf rotation, Vector3f scale, Camera camera) {
        //glDepthMask(true);

        Matrix4f modelMatrix = Transformation.toModelMatrix(position, rotation, scale);

        this.setUniform("modelMatrix", modelMatrix);
        this.setUniform("viewMatrix", camera.getViewMatrix());
        this.setUniform("projectionMatrix", window.getProjectionMatrix());
    }
}
