#version 150

// 主畫面貼圖（Minecraft 當前畫面）
uniform sampler2D DiffuseSampler;

// 時間參數，用於動態噪點
uniform float Time;

// 灰階混合強度
// 0.0 = 原色
// 1.0 = 完全灰階
uniform float GrayAmount;

// 雜訊強度
uniform float NoiseIntensity;

// 畫面旋轉角度（弧度）
// 用來模擬頭部傾斜 / 畫面翻轉
uniform float ScreenRoll;

// 黑畫面覆蓋程度
// 0.0 = 正常
// 1.0 = 完全黑
uniform float Blackout;

// 從 vertex shader 傳入的 UV
in vec2 texCoord;

// 最終輸出顏色
out vec4 fragColor;

// 簡易 hash 雜訊函數
// 用於生成 grain/noise
float hash(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

void main() {

    // =========================
    // 畫面旋轉處理
    // =========================

    // 將 UV 中心移到畫面中央
    vec2 centeredCoord = texCoord - vec2(0.5);

    // 計算旋轉矩陣
    float rollSin = sin(ScreenRoll);
    float rollCos = cos(ScreenRoll);

    // 套用 2D 旋轉
    vec2 rolledCoord = vec2(
        centeredCoord.x * rollCos - centeredCoord.y * rollSin,
        centeredCoord.x * rollSin + centeredCoord.y * rollCos
    ) + vec2(0.5);

    // 取樣旋轉後的畫面
    vec4 col = texture(DiffuseSampler, rolledCoord);

    // =========================
    // 灰階計算
    // =========================

    // 使用標準亮度權重轉換灰階
    float luminance = dot(col.rgb, vec3(0.299, 0.587, 0.114));

    // 灰階色
    vec3 gray = vec3(luminance);

    // =========================
    // 暗角（Vignette）效果
    // =========================

    // 重新取得中心座標
    vec2 centered = texCoord - vec2(0.5);

    // 計算距離畫面中心的距離
    float dist = length(centered);

    // 產生平滑暗角遮罩
    float vignette = smoothstep(0.30, 0.80, dist);

    // 越靠近邊緣越暗
    gray *= 1.0 - vignette * 0.9;

    // =========================
    // 原畫面與灰階混合
    // =========================

    vec3 color = mix(col.rgb, gray, clamp(GrayAmount, 0.0, 1.0));

    // =========================
    // 雜訊 / 顆粒效果
    // =========================

    // 動態噪點
    float noise = hash(gl_FragCoord.xy + vec2(Time * 913.7, Time * 431.9));

    // 將 noise 映射到 -1 ~ 1
    float grain = (noise - 0.5) * 2.0;

    // 限制強度範圍
    float intensity = clamp(NoiseIntensity, 0.0, 1.0);

    // 邊緣額外強化噪點
    float edgeBoost = 0.65 + vignette * 0.6;

    // 添加雜訊
    color += vec3(grain) * intensity * 0.35 * edgeBoost;

    // 額外降低飽和感
    color = mix(color, vec3(luminance * 0.65), intensity * 0.18);

    // =========================
    // 黑畫面淡出
    // =========================

    color = mix(color, vec3(0.0), clamp(Blackout, 0.0, 1.0));

    // =========================
    // 最終輸出
    // =========================

    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}