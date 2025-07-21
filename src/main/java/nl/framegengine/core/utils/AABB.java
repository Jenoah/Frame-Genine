package nl.framegengine.core.utils;

import org.joml.Vector3f;

public class AABB {
    public final Vector3f min = new Vector3f(0);
    public final Vector3f max = new Vector3f(0);
    private Vector3f size = new Vector3f(0);
    private float length = 1;

    public AABB(Vector3f min, Vector3f max) {
        this.min.set(min);
        this.max.set(max);
        this.size.set(Math.abs(min.x - max.x), Math.abs(min.y - max.y), Math.abs(min.z - max.z));
        this.length = Vector3f.distance(min.x, min.y, min.z, max.x, max.y, max.z);
    }

    public AABB(AABB aabb) {
        this.min.set(aabb.min);
        this.max.set(aabb.max);
        this.size = aabb.getSize();
        this.length = aabb.getLength();
    }

    public final Vector3f getSize(){
        return this.size;
    }

    public final float getLength(){
        return length;
    }

    public AABB offset(Vector3f offset){
        this.min.add(offset);
        this.max.add(offset);

        return this;
    }

    public Vector3f getCenter(){
        return min.lerp(max, 0.5f);
    }
}