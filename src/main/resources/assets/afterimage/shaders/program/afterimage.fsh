#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;
uniform float GrayAmount;
uniform float NoiseIntensity;
uniform float ScreenRoll;
uniform float Blackout;

in vec2 texCoord;
out vec4 fragColor;

float hash(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

void main() {
    vec2 centeredCoord = texCoord - vec2(0.5);
    float rollSin = sin(ScreenRoll);
    float rollCos = cos(ScreenRoll);
    vec2 rolledCoord = vec2(
        centeredCoord.x * rollCos - centeredCoord.y * rollSin,
        centeredCoord.x * rollSin + centeredCoord.y * rollCos
    ) + vec2(0.5);

    vec4 col = texture(DiffuseSampler, rolledCoord);

    float luminance = dot(col.rgb, vec3(0.299, 0.587, 0.114));
    vec3 gray = vec3(luminance);

    vec2 centered = texCoord - vec2(0.5);
    float dist = length(centered);
    float vignette = smoothstep(0.30, 0.80, dist);
    gray *= 1.0 - vignette * 0.9;

    vec3 color = mix(col.rgb, gray, clamp(GrayAmount, 0.0, 1.0));

    float noise = hash(gl_FragCoord.xy + vec2(Time * 913.7, Time * 431.9));
    float grain = (noise - 0.5) * 2.0;
    float intensity = clamp(NoiseIntensity, 0.0, 1.0);
    float edgeBoost = 0.65 + vignette * 0.6;

    color += vec3(grain) * intensity * 0.35 * edgeBoost;
    color = mix(color, vec3(luminance * 0.65), intensity * 0.18);
    color = mix(color, vec3(0.0), clamp(Blackout, 0.0, 1.0));

    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
