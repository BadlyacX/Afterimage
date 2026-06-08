#version 150

// 主畫面貼圖（Minecraft 當前畫面）
uniform sampler2D DiffuseSampler;

// 時間參數，用於動態噪點
uniform float Time;

// 雜訊強度
uniform float NoiseIntensity;

in vec2 texCoord;

out vec4 fragColor;

// 簡易 hash 雜訊函數，用於生成 grain/noise
float hash(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);

    // 重建暗角遮罩，用來在邊緣額外強化噪點
    vec2 centered = texCoord - vec2(0.5);
    float dist = length(centered);
    float vignette = smoothstep(0.30, 0.80, dist);

    // 動態噪點，映射到 -1 ~ 1
    float noise = hash(gl_FragCoord.xy + vec2(Time * 913.7, Time * 431.9));
    float grain = (noise - 0.5) * 2.0;

    // 限制強度範圍
    float intensity = clamp(NoiseIntensity, 0.0, 1.0);

    // 邊緣額外強化噪點
    float edgeBoost = 0.65 + vignette * 0.6;
    float luminance = dot(color.rgb, vec3(0.299, 0.587, 0.114));

    // 添加雜訊
    vec3 result = color.rgb + vec3(grain) * intensity * 0.35 * edgeBoost;

    // 額外降低飽和感
    result = mix(result, vec3(luminance * 0.65), intensity * 0.18);

    fragColor = vec4(clamp(result, 0.0, 1.0), color.a);
}
