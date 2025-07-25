package nl.framegengine.core.entity;

import nl.framegengine.core.IJsonSerializable;
import nl.framegengine.core.loaders.TextureLoader;
import nl.framegengine.core.utils.JsonHelper;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;

public class Texture implements IJsonSerializable {

    private int id;
    private final String texturePath;
    private boolean pointFilter = false;
    private boolean flipped = false;
    private boolean repeat = true;
    private boolean isNormalMap = false;

    public int getId() {
        return id;
    }

    public Texture(int id) {
        this.id = id;
        this.texturePath = "";
    }

    public Texture(String texturePath){

        this.id = TextureLoader.loadTexture(texturePath);
        this.texturePath = texturePath;
    }

    public Texture(String texturePath, boolean pointFilter){
        this.id = TextureLoader.loadTexture(texturePath, pointFilter);
        this.texturePath = texturePath;
        this.pointFilter = pointFilter;

    }

    public Texture(String texturePath, boolean pointFilter, boolean flipped){
        this.id = TextureLoader.loadTexture(texturePath, pointFilter, flipped);
        this.texturePath = texturePath;
        this.pointFilter = pointFilter;
        this.flipped = flipped;
    }

    public Texture(String texturePath, boolean pointFilter, boolean flipped, boolean repeat, boolean isNormalMap){
        this.id = TextureLoader.loadTexture(texturePath, pointFilter, flipped, repeat, isNormalMap);
        this.texturePath = texturePath;
        this.pointFilter = pointFilter;
        this.flipped = flipped;
        this.repeat = repeat;
        this.isNormalMap = isNormalMap;
    }

    @Override
    public JsonObject serializeToJson() {
        return JsonHelper.objectToJson(this);
    }

    @Override
    public void deserializeFromJson(String json) {
        JsonReader jsonReader = Json.createReader(new StringReader(json));
        JsonObject jsonInfo = jsonReader.readObject();
        JsonHelper.loadVariableIntoObject(this, jsonInfo);
        if(!texturePath.isEmpty()) this.id = TextureLoader.loadTexture(texturePath, pointFilter, flipped, repeat, isNormalMap);
    }
}
