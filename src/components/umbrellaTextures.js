export const clamp = (v, min, max) => Math.max(min, Math.min(max, v));

export const rand = (seed) => {
  let s = seed % 2147483647;
  if (s <= 0) s += 2147483646;
  return () => (s = (s * 16807) % 2147483647) / 2147483647;
};

export const buildCanopyCanvas = (patternId, baseColor, scale, opts = {}) => {
  const { overlayImage, size: rawSize } = opts;
  const size = rawSize ?? 1024;
  const c = document.createElement('canvas');
  c.width = size;
  c.height = size;
  const g = c.getContext('2d');

  // base oil-paper tone
  g.fillStyle = baseColor;
  g.fillRect(0, 0, size, size);

  // paper fibers / speckles
  const r = rand(1337);
  const area = (size * size) / (1024 * 1024);
  const speckCount = Math.max(400, Math.round(4200 * area));
  const fiberCount = Math.max(140, Math.round(1400 * area));
  g.globalAlpha = 0.06;
  for (let i = 0; i < speckCount; i += 1) {
    const x = r() * size;
    const y = r() * size;
    const w = 1 + r() * 2;
    g.fillStyle = r() > 0.5 ? 'rgba(255,255,255,1)' : 'rgba(20,20,20,1)';
    g.fillRect(x, y, w, w);
  }
  g.globalAlpha = 0.07;
  for (let i = 0; i < fiberCount; i += 1) {
    const x = r() * size;
    const y = r() * size;
    const len = 10 + r() * 70;
    const ang = r() * Math.PI * 2;
    g.strokeStyle = r() > 0.5 ? 'rgba(255,255,255,1)' : 'rgba(20,20,20,1)';
    g.lineWidth = 1;
    g.beginPath();
    g.moveTo(x, y);
    g.lineTo(x + Math.cos(ang) * len, y + Math.sin(ang) * len);
    g.stroke();
  }

  // gentle vignette - 优化边缘渐变
  g.globalAlpha = 1;
  const edge = g.createRadialGradient(size * 0.5, size * 0.55, size * 0.2, size * 0.5, size * 0.55, size * 0.80);
  edge.addColorStop(0, 'rgba(255,255,255,0.12)');
  edge.addColorStop(0.6, 'rgba(255,255,255,0.05)');
  edge.addColorStop(1, 'rgba(0,0,0,0.16)');
  g.fillStyle = edge;
  g.fillRect(0, 0, size, size);

  // aged edge (subtle) - 优化边缘老化效果
  g.save();
  g.globalCompositeOperation = 'multiply';
  g.globalAlpha = 0.18;
  const aged = g.createRadialGradient(size * 0.5, size * 0.55, size * 0.65, size * 0.5, size * 0.55, size * 0.88);
  aged.addColorStop(0, 'rgba(255,255,255,1)');
  aged.addColorStop(0.5, 'rgba(245,240,225,1)');
  aged.addColorStop(0.75, 'rgba(220,200,170,1)');
  aged.addColorStop(1, 'rgba(180,160,130,1)');
  g.fillStyle = aged;
  g.fillRect(0, 0, size, size);
  g.restore();

  // center透光（更像照片）- 增强中心高光效果
  g.save();
  g.globalCompositeOperation = 'screen';
  g.globalAlpha = 0.52;
  const glow = g.createRadialGradient(size * 0.45, size * 0.55, size * 0.05, size * 0.45, size * 0.55, size * 0.65);
  glow.addColorStop(0, 'rgba(255,255,255,0.98)');
  glow.addColorStop(0.3, 'rgba(255,255,255,0.35)');
  glow.addColorStop(0.6, 'rgba(255,255,255,0.12)');
  glow.addColorStop(1, 'rgba(255,255,255,0)');
  g.fillStyle = glow;
  g.fillRect(0, 0, size, size);
  g.restore();

  // user uploaded ink pattern overlay - 优化DIY图案显示
  if (overlayImage && overlayImage.complete && overlayImage.naturalWidth > 0) {
    g.save();
    
    // 先增强对比度，使图案更清晰
    g.globalCompositeOperation = 'multiply';
    g.globalAlpha = 0.65;
    const iw = overlayImage.naturalWidth;
    const ih = overlayImage.naturalHeight;
    const s = Math.max(size / iw, size / ih);
    const dw = iw * s;
    const dh = ih * s;
    const dx = (size - dw) * 0.5;
    const dy = (size - dh) * 0.5;
    g.drawImage(overlayImage, dx, dy, dw, dh);
    
    // 添加轻微的高光效果，使图案更有层次
    g.globalCompositeOperation = 'screen';
    g.globalAlpha = 0.15;
    g.drawImage(overlayImage, dx, dy, dw, dh);
    
    g.restore();
  }

  if (patternId === 'none') return c;

  const s = clamp(scale, 0.6, 2.2);
  // 520 是 1024 画布的经验值，按尺寸等比例缩放
  const tile = Math.round((size * 0.508) / s);
  const rr = rand(9001 + Math.round(s * 10));

  const ink = (alpha = 0.18) => {
    g.globalAlpha = alpha;
    g.strokeStyle = 'rgba(18,18,18,0.75)';
    g.fillStyle = 'rgba(18,18,18,0.55)';
    g.lineWidth = 2.2;
  };

  const drawCloud = (cx, cy) => {
    g.save();
    g.translate(cx, cy);
    const scale = 1.1 + rr() * 0.2;
    g.scale(scale, scale);
    
    // 多层祥云，更有层次感
    g.globalAlpha = 0.22;
    g.strokeStyle = 'rgba(74, 106, 111, 0.85)';
    g.fillStyle = 'rgba(255, 255, 255, 0.15)';
    g.lineWidth = 2.5;
    g.lineCap = 'round';
    g.lineJoin = 'round';
    
    // 主云朵
    g.beginPath();
    g.moveTo(-38, 0);
    g.bezierCurveTo(-38, -20, -12, -22, 0, -12);
    g.bezierCurveTo(8, -28, 32, -22, 30, -2);
    g.bezierCurveTo(50, 2, 48, 26, 28, 24);
    g.bezierCurveTo(18, 38, -12, 32, -12, 18);
    g.bezierCurveTo(-30, 22, -42, 12, -38, 0);
    g.closePath();
    g.fill();
    g.stroke();
    
    // 装饰小云朵
    g.globalAlpha = 0.15;
    g.beginPath();
    g.arc(-25, -8, 12, 0, Math.PI * 2);
    g.fill();
    g.stroke();
    
    g.beginPath();
    g.arc(25, 8, 10, 0, Math.PI * 2);
    g.fill();
    g.stroke();
    
    g.restore();
  };

  const drawLotus = (cx, cy) => {
    g.save();
    g.translate(cx, cy);
    const rad = 28 + rr() * 16;
    const rotation = (rr() - 0.5) * 0.3;
    g.rotate(rotation);
    
    // 外层花瓣 - 更优雅的曲线
    g.globalAlpha = 0.28;
    g.strokeStyle = 'rgba(58, 125, 92, 0.9)';
    g.fillStyle = 'rgba(255, 240, 245, 0.35)';
    g.lineWidth = 2.2;
    g.lineCap = 'round';
    
    for (let i = 0; i < 8; i += 1) {
      const a = (i / 8) * Math.PI * 2;
      const petalRad = rad * (0.85 + rr() * 0.25);
      g.save();
      g.rotate(a);
      g.beginPath();
      // 更流畅的椭圆花瓣
      g.ellipse(0, -petalRad * 0.35, petalRad * 0.32, petalRad * 0.75, 0, 0, Math.PI * 2);
      g.fill();
      g.stroke();
      // 花瓣纹理
      g.globalAlpha = 0.12;
      g.lineWidth = 1;
      g.beginPath();
      g.moveTo(0, -petalRad * 0.35);
      g.lineTo(0, -petalRad * 0.8);
      g.stroke();
      g.restore();
    }
    
    // 内层花瓣
    g.globalAlpha = 0.22;
    for (let i = 0; i < 6; i += 1) {
      const a = (i / 6) * Math.PI * 2 + Math.PI / 6;
      const petalRad = rad * 0.5;
      g.save();
      g.rotate(a);
      g.beginPath();
      g.ellipse(0, -petalRad * 0.3, petalRad * 0.25, petalRad * 0.5, 0, 0, Math.PI * 2);
      g.fill();
      g.stroke();
      g.restore();
    }
    
    // 莲花花心 - 更精致的渐变
    g.globalAlpha = 0.45;
    const centerGrad = g.createRadialGradient(0, 0, 0, 0, 0, rad * 0.2);
    centerGrad.addColorStop(0, 'rgba(255, 215, 0, 0.9)');
    centerGrad.addColorStop(1, 'rgba(255, 180, 0, 0.7)');
    g.fillStyle = centerGrad;
    g.beginPath();
    g.arc(0, 0, rad * 0.18, 0, Math.PI * 2);
    g.fill();
    
    // 花心细节
    g.globalAlpha = 0.3;
    g.strokeStyle = 'rgba(255, 140, 0, 0.8)';
    g.lineWidth = 1.5;
    for (let i = 0; i < 8; i += 1) {
      const a = (i / 8) * Math.PI * 2;
      g.beginPath();
      g.moveTo(0, 0);
      g.lineTo(Math.cos(a) * rad * 0.15, Math.sin(a) * rad * 0.15);
      g.stroke();
    }
    
    g.restore();
  };

  const drawPlum = (cx, cy) => {
    g.save();
    g.translate(cx, cy);
    g.rotate((rr() - 0.5) * 0.5);
    
    // 梅枝 - 更自然的曲线和粗细变化
    g.globalAlpha = 0.6;
    g.strokeStyle = 'rgba(90,62,43,0.7)';
    g.lineWidth = 7;
    g.lineCap = 'round';
    g.lineJoin = 'round';
    g.shadowColor = 'rgba(0,0,0,0.15)';
    g.shadowBlur = 8;
    
    // 主枝干
    g.beginPath();
    g.moveTo(-125, 55);
    g.bezierCurveTo(-45, -45, 45, 125, 125, -55);
    g.stroke();
    
    // 侧枝
    g.lineWidth = 4;
    g.beginPath();
    g.moveTo(-60, 10);
    g.quadraticCurveTo(-30, -20, -10, -35);
    g.stroke();
    
    g.beginPath();
    g.moveTo(40, 30);
    g.quadraticCurveTo(60, -10, 80, -25);
    g.stroke();
    
    g.shadowBlur = 0;

    // 梅花 - 更精致的五瓣花
    const blossoms = 6 + Math.floor(rr() * 7);
    for (let i = 0; i < blossoms; i += 1) {
      const x = -100 + rr() * 200;
      const y = -75 + rr() * 150;
      const br = 10 + rr() * 9;
      const rot = rr() * Math.PI * 2;
      
      g.save();
      g.translate(x, y);
      g.rotate(rot);
      
      // 花瓣 - 更柔和的渐变
      g.globalAlpha = 0.82;
      g.shadowColor = 'rgba(196,54,72,0.3)';
      g.shadowBlur = 12;
      
      const petalColor = rr() > 0.5 
        ? 'rgba(196,54,72,0.85)' 
        : 'rgba(236,140,154,0.85)';
      
      for (let p = 0; p < 5; p += 1) {
        const a = (p / 5) * Math.PI * 2;
        g.fillStyle = petalColor;
        g.beginPath();
        // 更优雅的花瓣形状
        g.ellipse(
          Math.cos(a) * br * 0.6, 
          Math.sin(a) * br * 0.6, 
          br * 0.5, 
          br * 0.95, 
          a, 
          0, 
          Math.PI * 2
        );
        g.fill();
      }
      
      g.shadowBlur = 0;
      
      // 花心 - 更精致的细节
      g.globalAlpha = 0.4;
      g.fillStyle = 'rgba(255,255,255,0.9)';
      g.beginPath();
      g.arc(0, 0, br * 0.3, 0, Math.PI * 2);
      g.fill();
      
      // 花蕊
      g.globalAlpha = 0.5;
      g.fillStyle = 'rgba(255,200,0,0.9)';
      g.beginPath();
      g.arc(0, 0, br * 0.15, 0, Math.PI * 2);
      g.fill();
      
      g.restore();
    }
    g.restore();
  };

  const drawKoi = (cx, cy) => {
    g.save();
    g.translate(cx, cy);
    g.rotate(rr() * Math.PI * 2);
    g.globalAlpha = 0.14;
    g.strokeStyle = 'rgba(18,18,18,0.72)';
    g.lineWidth = 2.2;
    g.beginPath();
    g.ellipse(0, 0, 44, 20, 0, 0, Math.PI * 2);
    g.stroke();
    g.globalAlpha = 0.10;
    g.beginPath();
    g.moveTo(36, 0);
    g.lineTo(60, -18);
    g.lineTo(56, 0);
    g.lineTo(60, 18);
    g.closePath();
    g.stroke();
    g.restore();
  };

  const drawLandscape = (cx, cy) => {
    g.save();
    g.translate(cx, cy);
    const rotation = (rr() - 0.5) * 0.2;
    g.rotate(rotation);
    
    // 远山 - 水墨画风格，更有层次
    g.globalAlpha = 0.18;
    g.strokeStyle = 'rgba(18,18,18,0.75)';
    g.fillStyle = 'rgba(18,18,18,0.12)';
    g.lineWidth = 2.5;
    g.lineCap = 'round';
    g.lineJoin = 'round';
    
    // 第一层远山
    g.beginPath();
    g.moveTo(-75, 30);
    g.quadraticCurveTo(-30, -20, 0, 18);
    g.quadraticCurveTo(30, -10, 75, 26);
    g.lineTo(75, 50);
    g.lineTo(-75, 50);
    g.closePath();
    g.fill();
    g.stroke();
    
    // 第二层中景山
    g.globalAlpha = 0.22;
    g.beginPath();
    g.moveTo(-65, -25);
    g.quadraticCurveTo(-20, -42, 25, -20);
    g.quadraticCurveTo(50, -30, 70, -15);
    g.lineTo(70, 10);
    g.lineTo(-65, 10);
    g.closePath();
    g.fill();
    g.stroke();
    
    // 近景 - 添加一些细节
    g.globalAlpha = 0.15;
    g.lineWidth = 2;
    // 小树或石头
    for (let i = 0; i < 3; i++) {
      const x = -50 + i * 50;
      const y = 20 + (rr() - 0.5) * 10;
      g.beginPath();
      g.moveTo(x, y);
      g.lineTo(x - 3, y + 8);
      g.moveTo(x, y);
      g.lineTo(x + 3, y + 8);
      g.stroke();
    }
    
    g.restore();
  };

  const drawPeony = (cx, cy) => {
    g.save();
    g.translate(cx, cy);
    g.rotate(rr() * Math.PI * 2);
    
    // 简化的牡丹花瓣
    const layers = [
      { radius: 45, alpha: 0.15, color: 'rgba(196, 54, 72, 0.8)', petals: 12 },
      { radius: 30, alpha: 0.25, color: 'rgba(236, 140, 154, 0.9)', petals: 8 },
      { radius: 18, alpha: 0.35, color: 'rgba(255, 182, 193, 0.9)', petals: 6 }
    ];
    
    layers.forEach(layer => {
      g.globalAlpha = layer.alpha;
      g.fillStyle = layer.color;
      for (let i = 0; i < layer.petals; i++) {
        const a = (i / layer.petals) * Math.PI * 2;
        const r = layer.radius * (0.9 + rr() * 0.2);
        g.save();
        g.translate(Math.cos(a) * r * 0.3, Math.sin(a) * r * 0.3);
        g.rotate(a);
        g.beginPath();
        g.ellipse(0, 0, r * 0.4, r * 0.8, 0, 0, Math.PI * 2);
        g.fill();
        g.restore();
      }
    });
    
    // 花心
    g.globalAlpha = 0.5;
    g.fillStyle = 'rgba(255, 215, 0, 0.9)';
    g.beginPath();
    g.arc(0, 0, 6, 0, Math.PI * 2);
    g.fill();
    g.restore();
  };

  const drawDragon = (cx, cy) => {
    g.save();
    g.translate(cx, cy);
    g.rotate(rr() * Math.PI * 2);
    g.globalAlpha = 0.18;
    g.strokeStyle = 'rgba(74, 106, 111, 0.9)';
    g.lineWidth = 5;
    g.lineCap = 'round';
    
    // 龙身
    g.beginPath();
    g.moveTo(-80, 0);
    g.bezierCurveTo(-40, -50, 0, 30, 40, -20);
    g.bezierCurveTo(60, -30, 80, 10, 100, -10);
    g.stroke();
    
    // 龙头
    g.globalAlpha = 0.25;
    g.lineWidth = 3;
    g.beginPath();
    g.ellipse(-80, 0, 18, 15, 0, 0, Math.PI * 2);
    g.stroke();
    
    // 龙爪
    g.globalAlpha = 0.15;
    g.lineWidth = 2;
    [-40, 0, 40].forEach(x => {
      const y = Math.sin((x + 80) / 180 * Math.PI * 3) * 25;
      g.beginPath();
      g.moveTo(x, y);
      g.lineTo(x - 6, y + 18);
      g.moveTo(x, y);
      g.lineTo(x + 6, y + 18);
      g.stroke();
    });
    
    g.restore();
  };

  // tile motifs - 只绘制保留的图案
  g.save();
  g.globalCompositeOperation = 'multiply';
  ink(patternId === 'plum' ? 0.65 : 0.22);
  for (let y = tile / 2; y < size + tile; y += tile) {
    for (let x = tile / 2; x < size + tile; x += tile) {
      const jx = (rr() - 0.5) * tile * 0.18;
      const jy = (rr() - 0.5) * tile * 0.18;
      if (patternId === 'cloud') drawCloud(x + jx, y + jy);
      else if (patternId === 'lotus') drawLotus(x + jx, y + jy);
      else if (patternId === 'plum') drawPlum(x + jx, y + jy);
      else if (patternId === 'landscape') drawLandscape(x + jx, y + jy);
    }
  }
  g.restore();

  return c;
};

export const buildRibShadowCanvas = (opts = {}) => {
  const size = 1024;
  const ribsCount = opts.ribsCount ?? 24;
  const c = document.createElement('canvas');
  c.width = size;
  c.height = size;
  const g = c.getContext('2d');

  g.clearRect(0, 0, size, size);

  // soft center darkening
  const center = g.createRadialGradient(size * 0.48, size * 0.56, 0, size * 0.48, size * 0.56, size * 0.56);
  center.addColorStop(0, 'rgba(0,0,0,0.22)');
  center.addColorStop(0.28, 'rgba(0,0,0,0.10)');
  center.addColorStop(1, 'rgba(0,0,0,0)');
  g.fillStyle = center;
  g.fillRect(0, 0, size, size);

  // rib spokes (lightweight fake shadows)
  g.save();
  g.translate(size * 0.5, size * 0.56);
  g.globalCompositeOperation = 'source-over';
  g.strokeStyle = 'rgba(0,0,0,0.065)';
  g.lineCap = 'round';

  for (let i = 0; i < ribsCount; i += 1) {
    const a = (i / ribsCount) * Math.PI * 2;
    const inner = 42;
    const outer = size * 0.52;
    g.lineWidth = i % 2 === 0 ? 7 : 5;
    g.beginPath();
    g.moveTo(Math.cos(a) * inner, Math.sin(a) * inner);
    g.lineTo(Math.cos(a) * outer, Math.sin(a) * outer);
    g.stroke();
  }
  g.restore();

  // slight edge lift (keep outer region cleaner)
  g.save();
  g.globalCompositeOperation = 'destination-out';
  const lift = g.createRadialGradient(size * 0.5, size * 0.56, size * 0.72, size * 0.5, size * 0.56, size * 0.90);
  lift.addColorStop(0, 'rgba(0,0,0,0)');
  lift.addColorStop(1, 'rgba(0,0,0,0.55)');
  g.fillStyle = lift;
  g.fillRect(0, 0, size, size);
  g.restore();

  return c;
};

export const buildWoodCanvas = (seed = 2025) => {
  const size = 512;
  const c = document.createElement('canvas');
  c.width = size;
  c.height = size;
  const g = c.getContext('2d');

  const r = rand(seed);
  const base = g.createLinearGradient(0, 0, size, 0);
  base.addColorStop(0, '#3a2419');
  base.addColorStop(0.35, '#7a4d2f');
  base.addColorStop(0.7, '#4a2d1d');
  base.addColorStop(1, '#8b5b3b');
  g.fillStyle = base;
  g.fillRect(0, 0, size, size);

  g.globalAlpha = 0.35;
  for (let x = 0; x < size; x += 2) {
    const t = x / size;
    const w = 0.8 + r() * 1.8;
    const a = 0.04 + r() * 0.10;
    g.fillStyle = `rgba(0,0,0,${a})`;
    g.fillRect(x, 0, w, size);
    if (r() > 0.6) {
      g.fillStyle = `rgba(255,255,255,${a * 0.55})`;
      g.fillRect(x + 1, 0, 1, size);
    }
    if (t > 0.18 && t < 0.86 && r() > 0.985) {
      const cx = x + (r() - 0.5) * 10;
      const cy = size * r();
      const rad = 16 + r() * 24;
      g.globalAlpha = 0.22;
      g.strokeStyle = 'rgba(0,0,0,0.35)';
      g.lineWidth = 1.5;
      for (let k = 0; k < 7; k += 1) {
        g.beginPath();
        g.ellipse(cx, cy, rad + k * 4, rad * 0.55 + k * 2.5, 0, 0, Math.PI * 2);
        g.stroke();
      }
      g.globalAlpha = 0.35;
    }
  }

  g.globalAlpha = 0.14;
  for (let i = 0; i < 2600; i += 1) {
    const x = r() * size;
    const y = r() * size;
    const w = 1 + r() * 2;
    g.fillStyle = r() > 0.5 ? 'rgba(255,255,255,1)' : 'rgba(0,0,0,1)';
    g.fillRect(x, y, w, w);
  }

  return c;
};

export const buildPatternThumbDataUrl = (id, baseColor, options = {}) => {
  // 缩略图直接复用伞面生成逻辑，避免两套重复绘制代码
  const outSize = 96;
  const srcSize = options.srcSize ?? 256;
  const s = options.scale ?? 1.15;
  const src = buildCanopyCanvas(id, baseColor, s, { size: srcSize });

  const c = document.createElement('canvas');
  c.width = outSize;
  c.height = outSize;
  const g = c.getContext('2d');
  g.drawImage(src, 0, 0, outSize, outSize);
  return c.toDataURL('image/png');
};
