#version 400 core
const int MAXIMUM_POINT_LIGHTS = 5;
const int MAXIMUM_SPOT_LIGHTS = 5;

in vec2 texCoords;
in vec3 vertexNormal;
in vec3 vertexColor;
in vec3 fragPosition;
in float fogFactor;
in vec4 shadowCoords;

out vec4 outColor;

struct Material {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    int hasTexture;
    float reflectance;
};

struct DirectionalLight {
    vec3 color;
    vec3 direction;
    float intensity;
};

struct PointLight{
    vec3 color;
    vec3 position;
    float intensity;
    float constant;
    float linear;
    float exponent;
};

struct SpotLight{
    vec3 color;
    vec3 position;
    float intensity;
    float constant;
    float linear;
    float exponent;
    vec3 coneDirection;
    float cutOff;
    float outerCutOff;
};

uniform sampler2D textureSampler;
uniform sampler2D shadowMap;
uniform vec3 ambientColor;
uniform vec3 viewPosition;
uniform vec3 fogColor;
uniform float specularPower;
uniform Material material;
uniform DirectionalLight directionalLight;
uniform PointLight pointLights[MAXIMUM_POINT_LIGHTS];
uniform SpotLight spotLights[MAXIMUM_SPOT_LIGHTS];

vec3 fragNormal;
vec3 viewDirection;

vec3 ambient;
vec3 diffuse;

vec4 diffuseMap;

vec4 calculatePointLight(PointLight light){
    //Shadow calculation
    float objectNearestLight = texture(shadowMap, shadowCoords.xy).r;
    float shadowFactor = 1.0;
    if(shadowCoords.z > objectNearestLight){
        shadowFactor = 0.6;
    }

    //Light direction
    vec3 lightDirection = normalize(light.position - fragPosition);

    //Diffuse
    float diffuseInfluence = max(dot(fragNormal, lightDirection), 0.0);

    //Specular
    vec3 reflectedDirection = reflect(-lightDirection, fragNormal);
    float specularInfuence = pow(max(dot(viewDirection, reflectedDirection), 0.0), material.reflectance);

    //Distance
    float distance = length(light.position - fragPosition);
    float attenuation = 1.0 / (light.constant + light.linear * distance + light.exponent * (distance * distance));

    //Calculation
    vec3 ambientOutput = ambient * diffuse * attenuation;
    vec3 diffuseOutput = light.color * diffuseInfluence * diffuse * attenuation * light.intensity * shadowFactor;
    vec3 specularOutput = light.color * specularInfuence * specularPower * attenuation * light.intensity;

    return vec4(ambientOutput + diffuseOutput + specularOutput, 0.0);
}

vec4 calculateSpotLight(SpotLight light){
    vec3 lightDirection = normalize(light.position - fragPosition);

    float theta = dot(lightDirection, normalize(-light.coneDirection));
    float epsilon = light.cutOff - light.outerCutOff;
    float intensity = clamp((theta - light.outerCutOff) / epsilon, 0.0, 1.0);

    vec4 outColor = vec4(0);

    if(theta > light.outerCutOff){
        //Diffuse
        float diffuseInfluence = max(dot(fragNormal, lightDirection), 0.0);

        //Specular
        vec3 reflectedDirection = reflect(-lightDirection, fragNormal);
        float specularInfuence = pow(max(dot(viewDirection, reflectedDirection), 0.0), material.reflectance);

        //Distance
        float distance = length(light.position - fragPosition);
        float attenuation = 1.0 / (light.constant + light.linear * distance + light.exponent * (distance * distance));

        //Calculation
        vec3 ambientOutput = ambient * diffuse * attenuation;
        vec3 diffuseOutput = light.color * diffuseInfluence * diffuse * attenuation * light.intensity;
        vec3 specularOutput = light.color * specularInfuence * specularPower * attenuation * light.intensity;

        outColor = vec4(ambientOutput + diffuseOutput + specularOutput, 0.0);

        float fade = smoothstep(light.outerCutOff, light.cutOff, theta);
        outColor *= fade;
    }

    return outColor;
}

vec4 calculateDirectionalLight(DirectionalLight light){
    vec3 lightDirection = normalize(light.direction);

    //Diffuse
    float diffuseInfluence = max(dot(fragNormal, lightDirection), 0.0);

    //Specular
    vec3 reflectedDirection = reflect(-lightDirection, fragNormal);
    float specularInfuence = pow(max(dot(viewDirection, reflectedDirection), 0.0), material.reflectance);
    specularInfuence = max(specularInfuence, 0.0);

    //Calculation
    vec3 ambientOutput = ambient * diffuse;
    vec3 diffuseOutput = light.color * diffuseInfluence * diffuse;
    vec3 specularOutput = light.color * specularInfuence * specularPower;
    specularOutput = vec3(0);

    return vec4((ambientOutput + diffuseOutput + specularOutput), 0.0);
}

void main(void) {
    //Declare textures
    if(material.hasTexture == 1){
        diffuseMap = texture(textureSampler, texCoords) * material.ambient;
    }else{
        diffuseMap = material.ambient;
    }

    diffuseMap = vec4(vertexColor, 1);

    //Correct intensities of textures and lighting
    ambient = vec3(0.3) * ambientColor;
    diffuse = vec3(1.0) * diffuseMap.rgb;

    fragNormal = normalize(vertexNormal);
    viewDirection = normalize(viewPosition - fragPosition);


    //Directional Light
    outColor = calculateDirectionalLight(directionalLight);

    for(int i = 0; i < MAXIMUM_POINT_LIGHTS; i++){
        if(pointLights[i].intensity > 0){
            outColor += calculatePointLight(pointLights[i]);
        }
    }

    for(int i = 0; i < MAXIMUM_SPOT_LIGHTS; i++){
        if(spotLights[i].intensity > 0){
            outColor += calculateSpotLight(spotLights[i]);
        }
    }

    outColor.rgb = mix(vec4(fogColor, 1.0), outColor, fogFactor).rgb;
    outColor.a = diffuseMap.a;
}
