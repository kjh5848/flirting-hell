import { useEffect, useRef } from "react";

const introDurationMs = 3600;

type Point3D = {
  pointX: number;
  pointY: number;
  pointZ: number;
};

type ProjectedPoint = {
  screenX: number;
  screenY: number;
  scale: number;
  alpha: number;
};

type Bubble = {
  text: string;
  angle: number;
  depthOffset: number;
  tone: "light" | "dark" | "rose";
};

const bubbles: Bubble[] = [
  { text: "오늘 뭐해?", angle: -1.9, depthOffset: -80, tone: "light" },
  { text: "뭐라고 보내지?", angle: -0.72, depthOffset: -160, tone: "dark" },
  { text: "너무 부담스럽나?", angle: 0.24, depthOffset: -120, tone: "light" },
  { text: "읽씹이면?", angle: 1.28, depthOffset: -130, tone: "rose" },
  { text: "그냥 집에 있어 ㅋㅋ", angle: 2.34, depthOffset: -70, tone: "light" }
];

const stars = Array.from({ length: 72 }, (_, index) => ({
  angle: index * 1.831,
  radius: 90 + ((index * 47) % 420),
  depth: -520 + ((index * 83) % 720),
  size: 0.6 + ((index * 13) % 9) / 10
}));

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max);
}

function easeOutCubic(value: number): number {
  return 1 - Math.pow(1 - value, 3);
}

function easeInOutCubic(value: number): number {
  return value < 0.5 ? 4 * value * value * value : 1 - Math.pow(-2 * value + 2, 3) / 2;
}

function project(point: Point3D, centerX: number, centerY: number, focalLength: number): ProjectedPoint {
  const depth = focalLength / (focalLength + point.pointZ);
  return {
    screenX: centerX + point.pointX * depth,
    screenY: centerY + point.pointY * depth,
    scale: depth,
    alpha: clamp(0.28 + depth * 0.82, 0, 1)
  };
}

function drawRoundedRect(context: CanvasRenderingContext2D, left: number, top: number, width: number, height: number, radius: number): void {
  const right = left + width;
  const bottom = top + height;
  context.beginPath();
  context.moveTo(left + radius, top);
  context.lineTo(right - radius, top);
  context.quadraticCurveTo(right, top, right, top + radius);
  context.lineTo(right, bottom - radius);
  context.quadraticCurveTo(right, bottom, right - radius, bottom);
  context.lineTo(left + radius, bottom);
  context.quadraticCurveTo(left, bottom, left, bottom - radius);
  context.lineTo(left, top + radius);
  context.quadraticCurveTo(left, top, left + radius, top);
  context.closePath();
}

function drawRing(context: CanvasRenderingContext2D, centerX: number, centerY: number, radiusX: number, radiusY: number, rotation: number, alpha: number): void {
  context.save();
  context.translate(centerX, centerY);
  context.rotate(rotation);
  context.globalAlpha = alpha;
  context.strokeStyle = "rgba(255, 228, 230, 0.44)";
  context.lineWidth = 1;
  context.shadowBlur = 28;
  context.shadowColor = "rgba(225, 29, 72, 0.3)";
  context.beginPath();
  context.ellipse(0, 0, radiusX, radiusY, 0, 0, Math.PI * 2);
  context.stroke();
  context.restore();
}

function drawBubble(context: CanvasRenderingContext2D, bubble: Bubble, projected: ProjectedPoint, progress: number): void {
  const scale = clamp(projected.scale, 0.62, 1.48);
  const bubbleWidth = Math.max(74, context.measureText(bubble.text).width + 28) * scale;
  const bubbleHeight = 36 * scale;
  const left = projected.screenX - bubbleWidth / 2;
  const top = projected.screenY - bubbleHeight / 2;
  const converge = easeInOutCubic(clamp((progress - 0.58) / 0.24, 0, 1));
  const alpha = projected.alpha * (1 - converge);

  context.save();
  context.globalAlpha = alpha;
  context.shadowBlur = 24 * scale;
  context.shadowColor = "rgba(0, 0, 0, 0.34)";
  drawRoundedRect(context, left, top, bubbleWidth, bubbleHeight, bubbleHeight / 2);
  context.fillStyle = bubble.tone === "dark" ? "rgba(42, 20, 27, 0.92)" : bubble.tone === "rose" ? "rgba(255, 228, 230, 0.95)" : "rgba(255, 255, 255, 0.92)";
  context.fill();
  context.shadowBlur = 0;
  context.fillStyle = bubble.tone === "dark" ? "#fff8f4" : bubble.tone === "rose" ? "#881337" : "#181114";
  context.font = `${Math.round(13 * scale)}px "Pretendard Variable", "Noto Sans KR", sans-serif`;
  context.textAlign = "center";
  context.textBaseline = "middle";
  context.fillText(bubble.text, projected.screenX, projected.screenY + 1 * scale);
  context.restore();
}

function drawEscapeCard(context: CanvasRenderingContext2D, centerX: number, centerY: number, progress: number): void {
  const reveal = easeOutCubic(clamp((progress - 0.58) / 0.28, 0, 1));
  if (reveal <= 0) {
    return;
  }

  const cardScale = 0.72 + reveal * 0.46;
  const cardWidth = Math.min(318, context.canvas.width / window.devicePixelRatio - 54) * cardScale;
  const cardHeight = 130 * cardScale;
  const cardLeft = centerX - cardWidth / 2;
  const cardTop = centerY - cardHeight / 2 + 26 * (1 - reveal);

  context.save();
  context.globalAlpha = reveal;
  context.shadowBlur = 70 * reveal;
  context.shadowColor = "rgba(225, 29, 72, 0.42)";
  drawRoundedRect(context, cardLeft, cardTop, cardWidth, cardHeight, 28 * cardScale);
  context.fillStyle = "rgba(255, 255, 255, 0.96)";
  context.fill();
  context.shadowBlur = 0;
  context.fillStyle = "#e11d48";
  context.font = `${Math.round(11 * cardScale)}px "Pretendard Variable", "Noto Sans KR", sans-serif`;
  context.textAlign = "left";
  context.textBaseline = "top";
  context.fillText("BEST REPLY", cardLeft + 22 * cardScale, cardTop + 20 * cardScale);
  context.fillStyle = "#181114";
  context.font = `900 ${Math.round(27 * cardScale)}px "Pretendard Variable", "Noto Sans KR", sans-serif`;
  context.fillText("지금 보낼 한마디", cardLeft + 22 * cardScale, cardTop + 46 * cardScale);
  context.fillStyle = "#5f4b51";
  context.font = `800 ${Math.round(14 * cardScale)}px "Pretendard Variable", "Noto Sans KR", sans-serif`;
  context.fillText("부담 없이, 자연스럽게.", cardLeft + 22 * cardScale, cardTop + 88 * cardScale);
  context.restore();
}

function renderFrame(context: CanvasRenderingContext2D, width: number, height: number, elapsedMs: number): void {
  const progress = clamp(elapsedMs / introDurationMs, 0, 1);
  const centerX = width / 2;
  const centerY = height * 0.43;
  const focalLength = 520;
  const cameraPush = easeInOutCubic(progress) * 260;
  const rotation = progress * Math.PI * 2.45;

  context.clearRect(0, 0, width, height);

  const background = context.createLinearGradient(0, 0, 0, height);
  background.addColorStop(0, "#080507");
  background.addColorStop(0.55, "#1b0b12");
  background.addColorStop(1, "#2a141b");
  context.fillStyle = background;
  context.fillRect(0, 0, width, height);

  for (const star of stars) {
    const starDepth = star.depth + cameraPush * 1.8;
    const starPoint = project(
      {
        pointX: Math.cos(star.angle) * star.radius,
        pointY: Math.sin(star.angle) * star.radius * 0.75,
        pointZ: starDepth
      },
      centerX,
      centerY,
      focalLength
    );
    context.globalAlpha = clamp(starPoint.alpha * 0.34, 0, 0.5);
    context.fillStyle = star.angle % 2 > 1 ? "#ffe4e6" : "#ffffff";
    context.beginPath();
    context.arc(starPoint.screenX, starPoint.screenY, star.size * starPoint.scale, 0, Math.PI * 2);
    context.fill();
  }
  context.globalAlpha = 1;

  const glow = context.createRadialGradient(centerX, centerY, 8, centerX, centerY, 220);
  glow.addColorStop(0, `rgba(225, 29, 72, ${0.16 + progress * 0.3})`);
  glow.addColorStop(0.36, "rgba(225, 29, 72, 0.16)");
  glow.addColorStop(1, "rgba(225, 29, 72, 0)");
  context.fillStyle = glow;
  context.fillRect(0, 0, width, height);

  const ringFade = 1 - easeOutCubic(clamp((progress - 0.72) / 0.22, 0, 1));
  drawRing(context, centerX, centerY, 150 + progress * 40, 48 + progress * 8, rotation * 0.32, ringFade * 0.64);
  drawRing(context, centerX, centerY, 112 + progress * 28, 34 + progress * 6, -rotation * 0.42, ringFade * 0.72);
  drawRing(context, centerX, centerY, 78 + progress * 18, 24 + progress * 5, rotation * 0.62, ringFade * 0.9);

  for (const bubble of bubbles) {
    const orbitRadius = Math.min(width * 0.29, 122) - progress * 54;
    const orbitAngle = bubble.angle + rotation;
    const pointDepth = bubble.depthOffset + Math.sin(orbitAngle) * 170 + cameraPush;
    const point = {
      pointX: Math.cos(orbitAngle) * orbitRadius,
      pointY: Math.sin(orbitAngle * 0.82) * orbitRadius * 0.5,
      pointZ: pointDepth
    };
    drawBubble(context, bubble, project(point, centerX, centerY, focalLength), progress);
  }

  const lightProgress = easeOutCubic(clamp((progress - 0.48) / 0.34, 0, 1));
  if (lightProgress > 0) {
    const lightRadius = 18 + lightProgress * 170;
    const light = context.createRadialGradient(centerX, centerY, 0, centerX, centerY, lightRadius);
    light.addColorStop(0, `rgba(225, 29, 72, ${0.82 * (1 - lightProgress * 0.25)})`);
    light.addColorStop(0.22, "rgba(225, 29, 72, 0.42)");
    light.addColorStop(1, "rgba(225, 29, 72, 0)");
    context.fillStyle = light;
    context.fillRect(0, 0, width, height);
  }

  drawEscapeCard(context, centerX, centerY, progress);

  const vignette = context.createRadialGradient(centerX, centerY, Math.min(width, height) * 0.18, centerX, centerY, Math.max(width, height) * 0.72);
  vignette.addColorStop(0, "rgba(0, 0, 0, 0)");
  vignette.addColorStop(1, "rgba(0, 0, 0, 0.68)");
  context.fillStyle = vignette;
  context.fillRect(0, 0, width, height);
}

export function CinematicIntroCanvas() {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) {
      return;
    }

    const context = canvas.getContext("2d");
    if (!context) {
      return;
    }

    const activeCanvas = canvas;
    const activeContext = context;
    let animationFrameId = 0;
    let startedAt = performance.now();

    function resize() {
      const ratio = window.devicePixelRatio || 1;
      const bounds = activeCanvas.getBoundingClientRect();
      activeCanvas.width = Math.max(1, Math.floor(bounds.width * ratio));
      activeCanvas.height = Math.max(1, Math.floor(bounds.height * ratio));
      activeContext.setTransform(ratio, 0, 0, ratio, 0, 0);
    }

    function animate(now: number) {
      const bounds = activeCanvas.getBoundingClientRect();
      renderFrame(activeContext, bounds.width, bounds.height, now - startedAt);
      animationFrameId = window.requestAnimationFrame(animate);
    }

    resize();
    startedAt = performance.now();
    animationFrameId = window.requestAnimationFrame(animate);
    window.addEventListener("resize", resize);

    return () => {
      window.cancelAnimationFrame(animationFrameId);
      window.removeEventListener("resize", resize);
    };
  }, []);

  return <canvas ref={canvasRef} className="cinematic-intro-canvas" aria-hidden="true" />;
}
