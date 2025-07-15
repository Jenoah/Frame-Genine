package nl.jenoah.core.lighting;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DirectionalLight extends Light{

    public DirectionalLight(){ super(); }

    public DirectionalLight(Vector3f color, Vector3f direction, float intensity) {
        super(color, new Vector3f(0), intensity, 0);
        lookAtDirection(direction);
    }

    public DirectionalLight(Vector3f color, Quaternionf direction, float intensity) {
        super(color, new Vector3f(0), intensity, 0);
        lookAtDirection(direction);
    }
}
