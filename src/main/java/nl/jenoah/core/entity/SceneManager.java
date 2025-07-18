package nl.jenoah.core.entity;

import nl.jenoah.core.components.Component;
import nl.jenoah.core.components.RenderComponent;
import nl.jenoah.core.debugging.Debug;
import nl.jenoah.core.lighting.DirectionalLight;
import nl.jenoah.core.lighting.Light;
import nl.jenoah.core.lighting.PointLight;
import nl.jenoah.core.lighting.SpotLight;
import nl.jenoah.core.loaders.OBJLoader.OBJLoader;
import nl.jenoah.core.rendering.MeshMaterialSet;
import nl.jenoah.core.shaders.ShaderManager;
import nl.jenoah.core.shaders.SimpleLitShader;
import nl.jenoah.core.components.ComponentLoader;
import nl.jenoah.core.utils.Utils;
import org.joml.Vector3f;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.InputStream;

import java.net.URL;
import java.util.*;

public class SceneManager {
    private List<Scene> scenes = new ArrayList<>();
    private Scene currentScene = null;
    public static Vector3f fogColor = new Vector3f(1);
    public static float fogDensity = 0.01f;
    public static float fogGradient = 15f;

    private static SceneManager instance = null;
    private ComponentLoader componentLoader;

    public static synchronized SceneManager getInstance()
    {
        if (instance == null) {
            instance = new SceneManager();
        }

        return instance;
    }

    public Scene loadScene(String filePath) throws Exception {
        Scene newScene = new Scene();

        if(componentLoader == null){
            URL inputResourceUrl = ComponentLoader.class.getResource("/scripts/");
            URL compiledResourceUrl = ComponentLoader.class.getResource("/scripts/bin/");
            if (inputResourceUrl == null){
                Debug.Log("File not found " + "scripts");
                return newScene;
            }

            componentLoader = new ComponentLoader(inputResourceUrl.toURI().getPath(), compiledResourceUrl.toURI().getPath());
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
             JsonReader reader = Json.createReader(is)) {

            if (is == null) {
                throw new IllegalArgumentException("File not found: " + filePath);
            }

            JsonObject sceneInfo = reader.readObject();

            Utils.loadVariableIntoObject(newScene, sceneInfo);

            // Game Objects
            sceneInfo.getJsonArray("gameObjects").forEach(goInfoContainer -> {
                JsonObject goInfo = goInfoContainer.asJsonObject();
                String goTypeName = "GameObject";
                if(Utils.hasJsonKey(goInfo, "goType")) goTypeName = goInfo.getString("goType");
                GoType goType = GoType.fromJsonName(goTypeName);

                GameObject go = null;
                try {
                    go = goType.createInstance();
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }

                Utils.loadVariableIntoObject(go, goInfo, new String[]{"parentGuid", "goType", "meshPath", "texturePath", "isMain"});

                if (Utils.hasJsonKey(goInfo, "meshPath")) {
                    Set<MeshMaterialSet> meshMaterialSets = OBJLoader.loadOBJModel(goInfo.getString("meshPath"));

                    if(Utils.hasJsonKey(goInfo, "texturePath")){
                        JsonObject textureInfo = goInfo.getJsonObject("texturePath");
                        if(!Utils.hasJsonKey(textureInfo, "diffuse")) return;
                        Material meshMaterial = new Material(ShaderManager.pbrShader);
                        meshMaterial.setAlbedoTexture(new Texture(textureInfo.getString("diffuse")));
                        if(Utils.hasJsonKey(textureInfo, "normal")) meshMaterial.setNormalMap(new Texture(textureInfo.getString("normal"), false, false, true, true));
                        if(Utils.hasJsonKey(textureInfo, "roughness")) meshMaterial.setRoughnessMap(new Texture(textureInfo.getString("roughness"), false, false, true, false));
                        meshMaterial.setRoughness(.6f);
                        meshMaterialSets.forEach(meshMaterialSet -> meshMaterialSet.material = meshMaterial);
                    }

                    RenderComponent renderComponent = new RenderComponent(meshMaterialSets);
                    go.addComponent(renderComponent);
                }
                switch (goType){
                    case GoType.DIRECTIONAL_LIGHT:
                    case GoType.POINT_LIGHT:
                    case GoType.SPOT_LIGHT:
                        tryAddLight((Light) go, newScene);
                        break;
                    case GoType.CAMERA:
                        if(Utils.hasJsonKey(goInfo, "isMain") && goInfo.getBoolean("isMain")) ((Camera)go).setAsMain();
                        break;
                }

                if(Utils.hasJsonKey(goInfo, "parentGuid")) go.setParent(newScene.getGameObjectByGUID(goInfo.getString("parentGuid")));

                tryAddComponent(goInfo, go);
                newScene.addEntity(go, false);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        newScene.getGameObjects().forEach(go -> go.getComponents().forEach(Component::initiate));

        for (SimpleLitShader s : Arrays.asList(ShaderManager.litShader, ShaderManager.triplanarShader, ShaderManager.pbrShader))
            s.setLights(newScene.getDirectionalLight(), newScene.getPointLights(), newScene.getSpotLights());
        return newScene;
    }

    private void tryAddLight(Light lightObject, Scene scene){
        switch (lightObject) {
            case DirectionalLight directionalLight -> {
                scene.setDirectionalLight(directionalLight);
                directionalLight.showProxy();
            }
            case SpotLight spotLight -> {
                spotLight.showProxy();
                scene.addSpotLight(spotLight);
            }
            case PointLight pointLight -> {
                pointLight.showProxy();
                scene.addPointLight(pointLight);
            }
            default -> throw new IllegalStateException("Unexpected value: " + lightObject);
        }
    }

    private void tryAddComponent(JsonObject jsonObject, GameObject gameObject){
        if(Utils.hasJsonKey(jsonObject, "components")){
            jsonObject.getJsonArray("components").forEach(componentInfoContainer -> {
                JsonObject componentInfo = componentInfoContainer.asJsonObject();
                if (!(componentInfo.containsKey("class") && !componentInfo.isNull("class"))) return;
                String className = componentInfo.getString("class");

                try {
                    Component component = componentLoader.loadComponent("nl.framegengine.customScripts." + className);
                    if(component != null) {

                        Utils.loadVariableIntoObject(component, componentInfo, new String[]{"class"});

                        component.setRoot(gameObject);
                        gameObject.addComponent(component);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    public void setScenes(List<Scene> scenes) {
        this.scenes = scenes;
    }

    public void addScene(Scene scene){
        this.scenes.add(scene);
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    public void setCurrentScene(int sceneIndex) {
        this.currentScene = scenes.get(sceneIndex);
        fogColor = currentScene.getFogColor();
        fogDensity = currentScene.getFogDensity();
        fogGradient = currentScene.getFogGradient();
        Debug.Log("Loading " + currentScene.getLevelName());
    }

    public void cleanUp(){
        currentScene.cleanUp();
    }

    public enum GoType{
        GAME_OBJECT("GameObject", GameObject.class),
        CAMERA("Camera", Camera.class),
        DIRECTIONAL_LIGHT("DirectionalLight", DirectionalLight.class),
        POINT_LIGHT("PointLight", PointLight.class),
        SPOT_LIGHT("SpotLight", SpotLight.class);

        private final String jsonName;
        private final Class<? extends GameObject> clazz;

        GoType(String jsonName, Class<? extends GameObject> clazz) {
            this.jsonName = jsonName;
            this.clazz = clazz;
        }

        public static GoType fromJsonName(String name) {
            for (GoType type : values()) {
                if (type.jsonName.equals(name)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown classType: " + name);
        }

        public GameObject createInstance() throws ReflectiveOperationException {
            return clazz.getDeclaredConstructor().newInstance();
        }
    }
}
