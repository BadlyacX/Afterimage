#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 col = texture(DiffuseSampler, texCoord);

    // --- 灰階 --- //
    float g = dot(col.rgb, vec3(0.299, 0.587, 0.114));
    vec3 gray = vec3(g);

    // --- Vignette（暗角）--- //
    vec2 centered = texCoord - vec2(0.5);
    float dist = length(centered);

    // 暗角強度（數值越大，邊緣越黑）//
    // 暗角更靠近中心 → 把 0.45 調小（例如 0.35）//
    // 暗角更外圍才出現 → 把 0.75 調大（例如 0.85）//
    float vignette = smoothstep(0.30, 0.80, dist);

    // 將暗角套用到亮度 //
    // 更壓迫 → 0.6 ~ 0.8 //
    // 更柔和 → 0.3 //
    gray *= (1.0 - vignette * 0.9);

    fragColor = vec4(gray, col.a);
}