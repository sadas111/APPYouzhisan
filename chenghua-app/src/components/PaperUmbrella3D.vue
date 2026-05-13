<template>
  <div ref="hostRef" class="host"></div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import { RoomEnvironment } from 'three/examples/jsm/environments/RoomEnvironment.js';

import { DragGesture } from '@use-gesture/vanilla';
import { buildCanopyCanvas, buildRibShadowCanvas, buildWoodCanvas } from './umbrellaTextures.js';

const PARTS = /** @type {const} */ (['canopy', 'ribs', 'handle']);

const props = defineProps({
  canopyColor: { type: String, required: true },
  ribsColor: { type: String, required: true },
  handleColor: { type: String, required: true },

  ribsCount: { type: Number, default: 24 },
  patternId: { type: String, default: 'none' },
  patternScale: { type: Number, default: 1 },
  customPatternUrl: { type: String, default: '' },
  h5Mode: { type: Boolean, default: false },
  exploded: { type: Boolean, default: false },
});

const emit = defineEmits(['select', 'assembled']);

const hostRef = ref(null);

let scene;
let camera;
let renderer;
let controls;
let rafId;

let canopyMesh;
let ribsMesh;
let ribsGroup;
let handleGroup;

let umbrellaGroup;
let trayGroup;
let onControlsStart;
let onControlsEnd;

let customPatternImage;
let customPatternLoadToken = 0;

let ribShadowMesh;

let pmrem;

// 自动旋转相关变量
let isUserInteracting = false;
let lastInteractionTime = 0;
const AUTO_ROTATE_DELAY = 2000; // 停止交互后2秒开始自动旋转
const AUTO_ROTATE_SPEED = 0.005;

const raycaster = new THREE.Raycaster();
const pointerNdc = new THREE.Vector2();

// 简化并统一的吸附阈值
const SNAP_DISTANCE = 0.8; // 统一吸附距离（松手时触发）
const MAGNET_START = 1.5; // 磁吸开始距离
const MAGNET_END = 0.8; // 磁吸最强距离（自动吸附）
const MAGNET_STRENGTH_MAX = 0.5; // 最大磁吸强度

// 拖拽视觉反馈
const DRAG_SCALE = 1.05; // 拖拽时缩放
const DRAG_EMISSIVE_BOOST = 0.35; // 拖拽时发光强度
const snappedState = {
  canopy: false,
  ribs: false,
  handle: false,
};

const selectedPart = ref('canopy');

const canopyHome = new THREE.Vector3(0, 0.0, 0);
const ribsHome = new THREE.Vector3(0, 0, 0);
const handleHome = new THREE.Vector3(0, 0.08, 0); // 精确定位在伞面底部边缘，正好接住伞骨
const partHomePos = { canopy: canopyHome, ribs: ribsHome, handle: handleHome };

const UMBRELLA_GROUP_NORMAL_ROT = new THREE.Euler(-0.26, 0.55, 0.06);
const UMBRELLA_GROUP_NORMAL_POS = new THREE.Vector3(0.0, 0.15, 0.0);

const partExplodeRot = {
  canopy: new THREE.Euler(0, 0, 0),
  ribs: new THREE.Euler(0, 0, 0),
  handle: new THREE.Euler(0, 0, 0),
};

const trayPos = {
  canopy: new THREE.Vector3(-1.2, 0.75, 0.3),
  ribs: new THREE.Vector3(0.3, 0.55, 0.5),  // 偏移一些，避免和伞柄重叠
  handle: new THREE.Vector3(1.2, 0.25, 0.2),
};

// 拆分模式动画相关
let explodeAnimId = null;
let explodeProgress = 0;
const EXPLODE_ANIM_DURATION = 600; // ms

const animateExplode = (targetProgress, onComplete) => {
  const startProgress = explodeProgress;
  const startTime = performance.now();
  
  const tick = () => {
    const elapsed = performance.now() - startTime;
    const t = Math.min(elapsed / EXPLODE_ANIM_DURATION, 1);
    const eased = 1 - Math.pow(1 - t, 3);
    
    explodeProgress = startProgress + (targetProgress - startProgress) * eased;
    
    // 插值位置和旋转
    for (const p of PARTS) {
      const obj = partObj(p);
      if (!obj) continue;
      
      const home = partHomePos[p];
      const target = trayPos[p];
      const homeRot = new THREE.Euler(0, 0, 0);
      const targetRot = partExplodeRot[p];
      
      // 位置插值
      obj.position.lerpVectors(home, target, explodeProgress);
      
      // 旋转插值
      obj.rotation.x = homeRot.x + (targetRot.x - homeRot.x) * explodeProgress;
      obj.rotation.y = homeRot.y + (targetRot.y - homeRot.y) * explodeProgress;
      obj.rotation.z = homeRot.z + (targetRot.z - homeRot.z) * explodeProgress;
    }
    
    if (t < 1) {
      explodeAnimId = requestAnimationFrame(tick);
    } else {
      explodeAnimId = null;
      onComplete?.();
    }
	
  };
  
  if (explodeAnimId) cancelAnimationFrame(explodeAnimId);
  explodeAnimId = requestAnimationFrame(tick);
};

const canopyMat = new THREE.MeshPhysicalMaterial({
  color: new THREE.Color('#ffffff'),
  roughness: 0.45,
  metalness: 0.0,
  transmission: 0.45,
  thickness: 0.22,
  ior: 1.32,
  transparent: true,
  opacity: 0.88,
  attenuationDistance: 1.2,
  attenuationColor: new THREE.Color(props.canopyColor),
  clearcoat: 0.18,
  clearcoatRoughness: 0.65,
  sheen: 0.22,
  sheenRoughness: 0.6,
  sheenColor: new THREE.Color(0xfff8e1),
  side: THREE.DoubleSide,
});

const ribsMat = new THREE.MeshStandardMaterial({
  color: new THREE.Color(props.ribsColor),
  roughness: 0.6,
  metalness: 0.0,
});

const ribWrapMat = new THREE.MeshStandardMaterial({
  color: new THREE.Color('#b88b5c'),
  roughness: 0.92,
  metalness: 0.0,
});

// 伞柄：高粗糙度的竹木质感，基础色接近实物照片
const handleMat = new THREE.MeshStandardMaterial({
  color: new THREE.Color(props.handleColor || '#e3c79b'),
  roughness: 0.9,
  metalness: 0.0,
});

// capMat / bindingMat removed

const applyWoodTextures = () => {
  const ribCanvas = buildWoodCanvas(1207);
  const ribTex = new THREE.CanvasTexture(ribCanvas);
  ribTex.colorSpace = THREE.SRGBColorSpace;
  ribTex.wrapS = THREE.RepeatWrapping;
  ribTex.wrapT = THREE.RepeatWrapping;
  ribTex.repeat.set(2, 8);
  ribTex.anisotropy = 8;
  if (ribsMat.map && ribsMat.map.dispose) ribsMat.map.dispose();
  ribsMat.map = ribTex;
  ribsMat.needsUpdate = true;

  const hCanvas = buildWoodCanvas(2711);
  const hTex = new THREE.CanvasTexture(hCanvas);
  hTex.colorSpace = THREE.SRGBColorSpace;
  hTex.wrapS = THREE.RepeatWrapping;
  hTex.wrapT = THREE.RepeatWrapping;
  // 让纹理在伞柄上更“顺纹理”，拉长竹节走向
  hTex.repeat.set(1.2, 4);
  hTex.anisotropy = 8;
  if (handleMat.map && handleMat.map.dispose) handleMat.map.dispose();
  handleMat.map = hTex;
  handleMat.needsUpdate = true;
};

const applyPattern = () => {
  const canvas = buildCanopyCanvas(props.patternId, props.canopyColor, props.patternScale, {
    overlayImage: customPatternImage,
  });
  const tex = new THREE.CanvasTexture(canvas);
  tex.colorSpace = THREE.SRGBColorSpace;
  tex.wrapS = THREE.RepeatWrapping;
  tex.wrapT = THREE.RepeatWrapping;
  tex.repeat.set(1, 1);
  tex.anisotropy = 16;
  if (canopyMat.map && canopyMat.map.dispose) canopyMat.map.dispose();
  canopyMat.map = tex;
  canopyMat.needsUpdate = true;
};

const applyRibShadow = () => {
  if (!ribShadowMesh) return;
  const canvas = buildRibShadowCanvas({ ribsCount: props.ribsCount ?? 24 });
  const tex = new THREE.CanvasTexture(canvas);
  tex.colorSpace = THREE.SRGBColorSpace;
  tex.wrapS = THREE.ClampToEdgeWrapping;
  tex.wrapT = THREE.ClampToEdgeWrapping;
  tex.anisotropy = 4;
  if (ribShadowMesh.material.map && ribShadowMesh.material.map.dispose) ribShadowMesh.material.map.dispose();
  ribShadowMesh.material.map = tex;
  ribShadowMesh.material.needsUpdate = true;
};

const resize = () => {
  if (!renderer || !camera || !hostRef.value) return;
  const rect = hostRef.value.getBoundingClientRect();
  const w = Math.max(1, Math.floor(rect.width));
  const h = Math.max(1, Math.floor(rect.height));
  renderer.setSize(w, h, false);
  camera.aspect = w / h;
  camera.updateProjectionMatrix();
};

const setPointerFromEvent = (e) => {
  const rect = renderer.domElement.getBoundingClientRect();
  pointerNdc.x = ((e.clientX - rect.left) / rect.width) * 2 - 1;
  pointerNdc.y = -(((e.clientY - rect.top) / rect.height) * 2 - 1);
};

const markInteraction = (active) => {
  isUserInteracting = active;
  lastInteractionTime = Date.now();
};

const pick = (e) => {
  if (!renderer) return null;
  setPointerFromEvent(e);
  raycaster.setFromCamera(pointerNdc, camera);
  const objs = [canopyMesh, ribsGroup, handleGroup].filter(Boolean);
  const hits = raycaster.intersectObjects(objs, true);
  if (!hits.length) return null;

  let o = hits[0].object;
  while (o) {
    const part = o?.userData?.part;
    if (part === 'canopy' || part === 'ribs' || part === 'handle') return part;
    o = o.parent;
  }
  return null;
};

const setSelected = (part) => {
  if (!part) return;
  selectedPart.value = part;
  emit('select', part);
};

const partObj = (part) => ({ canopy: canopyMesh, ribs: ribsGroup, handle: handleGroup }[part] ?? null);

const getPartWorldPosition = (part, out) => {
  const obj = partObj(part);
  if (!obj) return false;
  obj.getWorldPosition(out);
  return true;
};

const isPartSnapped = (part) => {
  const obj = partObj(part);
  const home = partHomePos[part];
  if (!obj || !home) return false;

  if (part === 'ribs') {
    const snapWorld = getRibsHandleSnapTargetWorld?.();
    if (!snapWorld) return false;
    if (!getPartWorldPosition('ribs', tmpCurrentWorld)) return false;
    return tmpCurrentWorld.distanceTo(snapWorld) < SNAP_DISTANCE;
  }

  if (part === 'handle') {
    const dx = obj.position.x - home.x;
    const dz = obj.position.z - home.z;
    const distXZ = Math.sqrt(dx * dx + dz * dz);
    return distXZ < SNAP_DISTANCE;
  }

  return obj.position.distanceTo(home) < SNAP_DISTANCE;
};

const applySnapResult = (part, targetLocal) => {
  const obj = partObj(part);
  const home = partHomePos[part];
  if (!obj || !home) return;

  if (part === 'ribs') {
    const snapWorld = getRibsHandleSnapTargetWorld?.();
    if (!snapWorld) {
      snappedState[part] = false;
      return;
    }
    if (!getPartWorldPosition('ribs', tmpCurrentWorld)) return;
    if (tmpCurrentWorld.distanceTo(snapWorld) < SNAP_DISTANCE) {
      if (obj.parent) {
        obj.parent.updateMatrixWorld(true);
        tmpSnapLocal.copy(snapWorld);
        obj.parent.worldToLocal(tmpSnapLocal);
        obj.position.copy(tmpSnapLocal);
      } else {
        obj.position.copy(snapWorld);
      }
      snappedState[part] = true;
    } else {
      snappedState[part] = false;
    }
    return;
  }

  if (part === 'handle') {
    const dx = targetLocal.x - home.x;
    const dz = targetLocal.z - home.z;
    const distXZ = Math.sqrt(dx * dx + dz * dz);
    if (distXZ < SNAP_DISTANCE) {
      obj.position.copy(home);
      snappedState[part] = true;
    } else {
      snappedState[part] = false;
    }
    return;
  }

  if (targetLocal.distanceTo(home) < SNAP_DISTANCE) {
    obj.position.copy(home);
    snappedState[part] = true;
  } else {
    snappedState[part] = false;
  }
};

const updateSnappedState = () => {
  let all = true;
  for (const p of PARTS) {
    const snapped = isPartSnapped(p);
    snappedState[p] = snapped;
    if (!snapped) all = false;
  }
  if (all) {
    // 确保拼接成功时位置归位，避免“看起来拼上了但位置有偏差”
    const canopyObj = partObj('canopy');
    const handleObj = partObj('handle');
    const ribsObj = partObj('ribs');

    canopyObj && canopyObj.position.copy(partHomePos.canopy);
    handleObj && handleObj.position.copy(partHomePos.handle);

    if (ribsObj) {
      const snapWorld = getRibsHandleSnapTargetWorld?.();
      if (snapWorld) {
        if (ribsObj.parent) {
          ribsObj.parent.updateMatrixWorld(true);
          tmpSnapLocal.copy(snapWorld);
          ribsObj.parent.worldToLocal(tmpSnapLocal);
          ribsObj.position.copy(tmpSnapLocal);
        } else {
          ribsObj.position.copy(snapWorld);
        }
      } else {
        ribsObj.position.copy(partHomePos.ribs);
      }
    }

    controls && (controls.enabled = true);
    emit('assembled');
  }
};

// ============ 拖拽系统 (基于 @use-gesture/vanilla) ============
let activeDragGesture = null;
let draggingPart = null;
let dragPlane = new THREE.Plane();
let dragPlaneNormal = new THREE.Vector3();
let dragStartWorldPos = new THREE.Vector3();
let dragOffset = new THREE.Vector3();
let dragStartLocal = new THREE.Vector3();
let dragHomeWorld = new THREE.Vector3();
let dragRay = new THREE.Ray();
let dragHitPoint = new THREE.Vector3();

// 拖拽临时变量
const tmpWorldPos = new THREE.Vector3();
const tmpLocalPos = new THREE.Vector3();
const tmpTargetWorld = new THREE.Vector3();
const tmpParentMatrixInv = new THREE.Matrix4();
const tmpCurrentWorld = new THREE.Vector3();
const tmpSnapLocal = new THREE.Vector3();

const setDragVisuals = (obj, isDragging, isNearSnap = false) => {
  if (!obj) return;
  // 缩放效果
  const scale = isDragging ? DRAG_SCALE : 1;
  obj.scale.setScalar(scale);
  // 发光效果
  const part = obj.userData.part;
  if (part && partMat[part]) {
    const mat = partMat[part];
    if (isDragging) {
      mat.emissive.setHex(isNearSnap ? 0x4a9a9f : 0x2d5a5f);
      mat.emissiveIntensity = isNearSnap ? DRAG_EMISSIVE_BOOST + 0.15 : DRAG_EMISSIVE_BOOST;
    } else {
      resetEmissive();
    }
  }
};

// 计算零件的吸附目标世界坐标（始终基于 umbrellaGroup 的 home 位置）
const getSnapTargetWorld = (part) => {
  const home = partHomePos[part];
  if (!home) return null;
  if (!umbrellaGroup) return home.clone();
  umbrellaGroup.updateMatrixWorld(true);
  return home.clone().applyMatrix4(umbrellaGroup.matrixWorld);
};

// 检查零件是否已吸附到位（使用世界坐标比较）
const checkSnapped = (part) => {
  const obj = partObj(part);
  if (!obj) return false;
  
  // 获取零件当前世界位置
  const currentWorld = new THREE.Vector3();
  obj.getWorldPosition(currentWorld);
  
  // 获取吸附目标世界位置
  const targetWorld = getSnapTargetWorld(part);
  if (!targetWorld) return false;
  
  return currentWorld.distanceTo(targetWorld) < SNAP_DISTANCE;
};

// 执行吸附：将零件移动到目标位置并移回 umbrellaGroup
const snapPart = (part) => {
  const obj = partObj(part);
  if (!obj) return false;
  
  // 将零件移回 umbrellaGroup
  if (obj.parent !== umbrellaGroup && umbrellaGroup) {
    umbrellaGroup.attach(obj);
  }
  
  // 设置到 home 位置
  const home = partHomePos[part];
  if (home) {
    obj.position.copy(home);
    obj.rotation.set(0, 0, 0);
  }
  
  snappedState[part] = true;
  return true;
};

// 计算磁吸偏移：根据距离返回吸附强度
const getMagnetOffset = (currentWorld, targetWorld) => {
  const dist = currentWorld.distanceTo(targetWorld);
  if (dist >= MAGNET_START) return null;
  if (dist <= MAGNET_END) return { snapped: true, target: targetWorld };
  // 距离渐变：越近吸附力越强
  const t = (dist - MAGNET_END) / (MAGNET_START - MAGNET_END);
  const strength = (1 - t * t) * MAGNET_STRENGTH_MAX; // 二次缓出
  const offset = new THREE.Vector3().subVectors(targetWorld, currentWorld).multiplyScalar(strength);
  return { snapped: false, offset };
};

// 初始化拖拽
const initDrag = (part, origin) => {
  const obj = partObj(part);
  if (!obj || !renderer || !camera) return null;
  draggingPart = part;
  // 禁用轨道控制器
  if (controls) controls.enabled = false;
  // 将伞体摆正，保持正面朝向
  if (umbrellaGroup) {
    umbrellaGroup.rotation.set(0, 0, 0);
    umbrellaGroup.position.set(0, 0.15, 0);
  }
  // 记录初始状态
  obj.getWorldPosition(dragStartWorldPos);
  dragStartLocal.copy(obj.position);
  dragHomeWorld.copy(partHomePos[part]);
  // 创建拖拽平面（垂直于相机方向，通过物体中心）
  camera.getWorldDirection(dragPlaneNormal).normalize();
  dragPlane.setFromNormalAndCoplanarPoint(dragPlaneNormal, dragStartWorldPos);
  // 计算点击偏移
  setPointerFromEvent({ clientX: origin[0], clientY: origin[1] });
  raycaster.setFromCamera(pointerNdc, camera);
  if (raycaster.ray.intersectPlane(dragPlane, dragHitPoint)) {
    dragOffset.subVectors(dragHitPoint, dragStartWorldPos);
  } else {
    dragOffset.set(0, 0, 0);
  }
  // 视觉反馈
  setDragVisuals(obj, true);
  markInteraction(true);
  return { part, obj };
};

// 更新拖拽位置
const updateDrag = (movement) => {
  if (!draggingPart || !renderer || !camera) return;
  const obj = partObj(draggingPart);
  if (!obj) return;
  // 使用当前鼠标位置计算射线
  const rect = renderer.domElement.getBoundingClientRect();
  const x = movement[0] / rect.width * 2 - 1;
  const y = -(movement[1] / rect.height) * 2 + 1;
  pointerNdc.set(x, y);
  raycaster.setFromCamera(pointerNdc, camera);
  // 射线与拖拽平面相交
  if (!raycaster.ray.intersectPlane(dragPlane, dragHitPoint)) return;
  // 计算目标世界位置
  const targetWorld = new THREE.Vector3().subVectors(dragHitPoint, dragOffset);
  // 边界限制：不能离 home 太远
  const maxDist = 2.0;
  if (targetWorld.distanceTo(dragHomeWorld) > maxDist) {
    targetWorld.sub(dragHomeWorld).setLength(maxDist).add(dragHomeWorld);
  }
  // Y轴限制
  targetWorld.y = Math.max(-0.5, Math.min(2.0, targetWorld.y));
  // 检查磁吸效果
  const snapTarget = getSnapTargetWorld(draggingPart);
  let isNearSnap = false;
  if (snapTarget) {
    const magnet = getMagnetOffset(targetWorld, snapTarget);
    if (magnet?.snapped) {
      // 自动吸附
      targetWorld.copy(snapTarget);
      snapPart(draggingPart);
      setDragVisuals(obj, true, true);
      return { snapped: true };
    } else if (magnet?.offset) {
      // 应用磁吸偏移
      targetWorld.add(magnet.offset);
      isNearSnap = true;
    }
  }
  // 转换为本地坐标并应用
  if (obj.parent) {
    obj.parent.updateMatrixWorld(true);
    tmpParentMatrixInv.copy(obj.parent.matrixWorld).invert();
    tmpLocalPos.copy(targetWorld).applyMatrix4(tmpParentMatrixInv);
  } else {
    tmpLocalPos.copy(targetWorld);
  }
  obj.position.copy(tmpLocalPos);
  // 更新视觉反馈
  const distToSnap = snapTarget ? targetWorld.distanceTo(snapTarget) : Infinity;
  setDragVisuals(obj, true, distToSnap < MAGNET_START || isNearSnap);
  return { snapped: false, distance: distToSnap };
};

// 结束拖拽
const endDrag = () => {
  if (!draggingPart) return;
  const obj = partObj(draggingPart);
  if (obj) {
    // 检查是否需要吸附
    const wasSnapped = checkSnapped(draggingPart);
    if (!wasSnapped) {
      // 检查当前距离，如果在 SNAP_DISTANCE 范围内则吸附
      const target = getSnapTargetWorld(draggingPart);
      const current = new THREE.Vector3();
      obj.getWorldPosition(current);
      if (target && current.distanceTo(target) < SNAP_DISTANCE) {
        snapPart(draggingPart);
      }
    }
    // 恢复视觉
    obj.scale.setScalar(1);
    setDragVisuals(obj, false);
  }
  draggingPart = null;
  // 恢复轨道控制器
  if (controls) controls.enabled = !props.exploded;
  markInteraction(false);
  updateAllSnappedState();
};

// 创建拖拽手势 - 统一处理所有部件
const createDragGesture = (element) => {
  return new DragGesture(element, (state) => {
    const { event, first, last, movement, down } = state;
    
    if (first) {
      // 开始拖拽 - 检测点击的是哪个部件
      if (!props.exploded) return;
      if (!renderer || !camera) return;
      
      // 使用 event 的坐标进行拾取检测
      const rect = renderer.domElement.getBoundingClientRect();
      pointerNdc.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
      pointerNdc.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
      raycaster.setFromCamera(pointerNdc, camera);
      
      const objs = [canopyMesh, ribsGroup, handleGroup].filter(Boolean);
      const hits = raycaster.intersectObjects(objs, true);
      if (!hits.length) return;
      
      let hitObj = hits[0].object;
      let part = null;
      while (hitObj) {
        const p = hitObj?.userData?.part;
        if (p === 'canopy' || p === 'ribs' || p === 'handle') {
          part = p;
          break;
        }
        hitObj = hitObj.parent;
      }
      
      if (!part) return;
      
      setSelected(part);
      initDrag(part, movement);
    } else if (down && draggingPart) {
      // 拖拽中
      updateDrag(movement);
    }
    
    if (last) {
      // 结束拖拽
      endDrag();
    }
  }, {
    preventScroll: true,
    eventOptions: { passive: false },
  });
};

// 更新所有零件的吸附状态，检查是否全部完成
const updateAllSnappedState = () => {
  let allSnapped = true;
  for (const p of PARTS) {
    const snapped = checkSnapped(p);
    snappedState[p] = snapped;
    if (!snapped) allSnapped = false;
  }
  if (allSnapped) {
    // 全部吸附，触发完成
    for (const p of PARTS) {
      snapPart(p);
    }
    if (controls) controls.enabled = true;
    emit('assembled');
  }
};

// ============ 旧的辅助函数（清理简化版） ============

	
const partMat = { canopy: canopyMat, ribs: ribsMat, handle: handleMat };
const glow = { canopy: 0.12, ribs: 0.14, handle: 0.14 };
const resetEmissive = () => {
  for (const p of PARTS) {
    const m = partMat[p];
    if (!m?.emissive) continue;
    const on = selectedPart.value === p;
    m.emissive.setHex(on ? 0x0f2a2a : 0x000000);
    m.emissiveIntensity = on ? glow[p] : 0.0;
  }
};

// 吸附预览效果
const snapPreviewState = { canopy: false, ribs: false, handle: false };

const showSnapPreview = (obj, part, show) => {
  if (snapPreviewState[part] === show) return;
  snapPreviewState[part] = show;
  
  const mat = partMat[part];
  if (!mat || !mat.emissive) return;
  
  if (show) {
    mat.emissive.setHex(0x2d5a5f);
    mat.emissiveIntensity = 0.25;
  } else {
    resetEmissive();
  }
};

const RIBS_TO_HANDLE_OFFSET = new THREE.Vector3(0, -0.08, 0);

const getRibsHandleSnapTargetWorld = () => {
  if (!handleGroup) return null;
  const p = new THREE.Vector3();
  handleGroup.getWorldPosition(p);
  p.add(RIBS_TO_HANDLE_OFFSET);
  return p;
};

const setPartsPosition = (lerpAlpha = 1) => {
  for (const p of PARTS) {
    const obj = partObj(p);
    if (!obj) continue;
    const target = props.exploded ? trayPos[p] : partHomePos[p];
    lerpAlpha >= 1 ? obj.position.copy(target) : obj.position.lerp(target, lerpAlpha);

    if (props.exploded) {
      const r = partExplodeRot[p];
      if (r) obj.rotation.set(r.x, r.y, r.z);
    } else {
      obj.rotation.set(0, 0, 0);
    }
  }
};

const updatePartColors = () => {
  ribsMat.color.set(props.ribsColor);
  handleMat.color.set(props.handleColor);
  canopyMat.color.set('#ffffff');
  if (canopyMat.attenuationColor) canopyMat.attenuationColor.set(props.canopyColor);
};

const tick = () => {
  rafId = requestAnimationFrame(tick);

  if (canopyMesh) resetEmissive();

  // 自动旋转逻辑
  if (umbrellaGroup && !props.exploded && !isUserInteracting && Date.now() - lastInteractionTime > AUTO_ROTATE_DELAY) {
    umbrellaGroup.rotation.y += AUTO_ROTATE_SPEED;
	}

  controls?.update();
  renderer?.render(scene, camera);
};

const init = () => {
  scene = new THREE.Scene();

  camera = new THREE.PerspectiveCamera(42, 1, 0.1, 50);
  camera.position.set(0.0, 1.55, 3.25);

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true, preserveDrawingBuffer: true });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
  renderer.outputColorSpace = THREE.SRGBColorSpace;
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.18;

  hostRef.value.appendChild(renderer.domElement);

  renderer.domElement.style.touchAction = 'pan-y';

  renderer.domElement.addEventListener('pointercancel', endDrag);
  renderer.domElement.addEventListener('lostpointercapture', endDrag);

  controls = new OrbitControls(camera, renderer.domElement);
  controls.enablePan = false;
  controls.enableZoom = false;
  controls.enableDamping = true;
  controls.dampingFactor = 0.08;
  controls.target.set(0, 0.35, 0);
  controls.minPolarAngle = 0.45;
  controls.maxPolarAngle = 1.35;
  controls.minAzimuthAngle = -1.25;
  controls.maxAzimuthAngle = 1.25;
  
  // 监听用户交互事件
  onControlsStart = () => markInteraction(true);
  
  onControlsEnd = () => {
    markInteraction(false);
  };
  
  controls.addEventListener('start', onControlsStart);
  controls.addEventListener('end', onControlsEnd);

  pmrem = new THREE.PMREMGenerator(renderer);
  const envMap = pmrem.fromScene(new RoomEnvironment(renderer), 0.04).texture;
  scene.environment = envMap;

  const ambient = new THREE.AmbientLight(0xffffff, 0.42);
  scene.add(ambient);

  const hemi = new THREE.HemisphereLight(0xffffff, 0xe8dcc8, 0.65);
  scene.add(hemi);

  const sun = new THREE.DirectionalLight(0xffe4bd, 1.25);
  sun.position.set(-2.6, 3.0, 2.3);
  scene.add(sun);

  const rim = new THREE.DirectionalLight(0xffffff, 0.95);
  rim.position.set(2.2, 1.8, -2.8);
  scene.add(rim);

  const shadowPlane = new THREE.Mesh(
    new THREE.CircleGeometry(1.65, 64),
    new THREE.MeshBasicMaterial({ color: 0x000000, transparent: true, opacity: 0.08 })
  );
  shadowPlane.rotation.x = -Math.PI / 2;
  shadowPlane.position.y = -0.98;
  scene.add(shadowPlane);

  umbrellaGroup = new THREE.Group();
  umbrellaGroup.rotation.copy(UMBRELLA_GROUP_NORMAL_ROT);
  umbrellaGroup.position.copy(UMBRELLA_GROUP_NORMAL_POS);
  scene.add(umbrellaGroup);

  trayGroup = new THREE.Group();
  trayGroup.position.set(0, 0, 0);
  trayGroup.rotation.set(0, 0, 0);

  // 伞面改为更“平”的造型，贴近古代油纸伞的开伞效果
  // 减小高度，同时保证伞骨汇聚点仍位于顶部中心
  const canopyHeight = 0.4;
  const canopyGeo = new THREE.ConeGeometry(1.55, canopyHeight, 72, 1, true);
  // 顶点保持在 y = 0.95，方便与伞骨/伞柄对齐
  canopyGeo.translate(0, 0.95 - canopyHeight * 0.5, 0);
  canopyMesh = new THREE.Mesh(canopyGeo, canopyMat);
  canopyMesh.rotation.x = 0;
  canopyMesh.userData.part = 'canopy';
  canopyMesh.renderOrder = 2;
  umbrellaGroup.add(canopyMesh);

  const ribShadowMat = new THREE.MeshBasicMaterial({
    color: 0x000000,
    transparent: true,
    opacity: 0.28,
    depthWrite: false,
  });
  ribShadowMesh = new THREE.Mesh(new THREE.CircleGeometry(1.52, 80), ribShadowMat);
  ribShadowMesh.rotation.x = -Math.PI / 2;
  ribShadowMesh.position.set(0, 0.02, 0);
  ribShadowMesh.userData.part = 'canopy';
  canopyMesh.add(ribShadowMesh);

  const ribsCount = props.ribsCount ?? 24;

  // 中心竹柄 + 伞骨榫卯结构（参考实物油纸伞）
  const hubGroup = new THREE.Group();
  hubGroup.position.set(0, 0.0, 0);
  hubGroup.userData.part = 'ribs';

  // 中心粗竹节
  const hubCoreGeo = new THREE.CylinderGeometry(0.09, 0.095, 0.20, 32);
  const hubCore = new THREE.Mesh(hubCoreGeo, handleMat);
  hubCore.position.set(0, 0.95 - 0.10, 0);
  hubGroup.add(hubCore);

  // 上下两圈木环
  const hubRingUpper = new THREE.Mesh(
    new THREE.CylinderGeometry(0.25, 0.25, 0.035, 48),
    handleMat
  );
  hubRingUpper.position.set(0, 0.95, 0);
  hubGroup.add(hubRingUpper);

  const hubRingLower = new THREE.Mesh(
    new THREE.CylinderGeometry(0.24, 0.24, 0.04, 48),
    handleMat
  );
  hubRingLower.position.set(0, 0.90, 0);
  hubGroup.add(hubRingLower);

  // 一圈竖向小竹榫，夹在两环之间
  const toothGeo = new THREE.BoxGeometry(0.03, 0.11, 0.055);
  const hubTeeth = new THREE.InstancedMesh(toothGeo, handleMat, ribsCount);
  const toothMat = new THREE.Matrix4();
  for (let i = 0; i < ribsCount; i += 1) {
    const a = (i / ribsCount) * Math.PI * 2;
    const x = Math.cos(a) * 0.20;
    const z = Math.sin(a) * 0.20;
    toothMat.identity();
    toothMat.makeRotationY(a);
    toothMat.setPosition(x, 0.925, z);
    hubTeeth.setMatrixAt(i, toothMat);
  }
  hubTeeth.instanceMatrix.needsUpdate = true;
  hubGroup.add(hubTeeth);

  // 伞骨全部改为细长的竹片，从上木环外缘向伞面展开（更贴近实物照片）
  // 这里精确计算伞骨长度：让伞骨尖端刚好落在伞面侧边圆圈上，
  // 同时整体略微“沉”到伞面内部，从伞顶外侧看不到伞骨，只在内侧可见
  const canopyRadius = 1.55;
  const canopyApexY = 0.95;
  const canopyRimY = canopyApexY - canopyHeight; // = 0.55
  const ribLen = Math.sqrt(
    canopyRadius * canopyRadius +
      (canopyApexY - canopyRimY) * (canopyApexY - canopyRimY)
  ); // ≈ 1.6
  // 主伞骨：使用扁平竹片（Box），更贴近实物图中的伞骨形态
  const ribGeo = new THREE.BoxGeometry(0.018, ribLen, 0.006);
  ribGeo.translate(0, ribLen * 0.5, 0);
  ribsMesh = new THREE.InstancedMesh(ribGeo, ribsMat, ribsCount);
  ribsMesh.userData.part = 'ribs';

  // 外圈绑线位置仍保留一圈小竹环，增强真实感
  const ribWrapH = 0.06;
  const ribWrapGeo = new THREE.CylinderGeometry(0.022, 0.022, ribWrapH, 16);
  ribWrapGeo.translate(0, ribLen - 0.18, 0);
  const ribWrapMesh = new THREE.InstancedMesh(ribWrapGeo, ribWrapMat, ribsCount);
  ribWrapMesh.userData.part = 'ribs';

  // 内部“挑棍/撑骨”：从下方活动环向外挑起主伞骨，形成三角结构，参考实物伞骨构造
  const stretcherGeo = new THREE.BoxGeometry(0.012, 1.0, 0.02); // 先用单位长度，后面用缩放矩阵控制真实长度
  stretcherGeo.translate(0, 0.5, 0);
  const stretcherMesh = new THREE.InstancedMesh(stretcherGeo, ribsMat, ribsCount);
  stretcherMesh.userData.part = 'ribs';

  ribsGroup = new THREE.Group();
  ribsGroup.userData.part = 'ribs';
  ribsGroup.renderOrder = 1;
  ribsGroup.add(hubGroup);
  ribsGroup.add(ribsMesh);
  ribsGroup.add(ribWrapMesh);
  ribsGroup.add(stretcherMesh);

  const up = new THREE.Vector3(0, 1, 0);
  // 参考照片：伞骨贴着伞面内侧向外铺开，只略微向下倾斜，
  // 在这里我们把伞骨整体往伞面内部“压”一点点，从伞顶外侧看不到伞骨
  const ribsInset = 0.02;
  const hub = new THREE.Vector3(0, canopyApexY - ribsInset, 0);
  const dir = new THREE.Vector3();
  const q = new THREE.Quaternion();
  const scl = new THREE.Vector3(1, 1, 1);
  const m = new THREE.Matrix4();

  // 为内部“挑棍”准备的一些复用向量/矩阵
  const base = new THREE.Vector3();
  const tip = new THREE.Vector3();
  const mid = new THREE.Vector3();
  const spanDir = new THREE.Vector3();
  const q2 = new THREE.Quaternion();
  const scl2 = new THREE.Vector3(1, 1, 1);
  const m2 = new THREE.Matrix4();
  for (let i = 0; i < ribsCount; i += 1) {
    const a = (i / ribsCount) * Math.PI * 2;
    // 目标点：伞面圆边上的一点（x,z 为圆周，y 为伞面边缘高度），同样略微往内压
    const rimTarget = new THREE.Vector3(
      Math.cos(a) * canopyRadius,
      canopyRimY - ribsInset,
      Math.sin(a) * canopyRadius
    );
    // 从伞骨起点（hub）指向伞面边缘的方向
    dir.copy(rimTarget).sub(hub).normalize();
    q.setFromUnitVectors(up, dir);
    m.compose(hub, q, scl);
    ribsMesh.setMatrixAt(i, m);
    ribWrapMesh.setMatrixAt(i, m);

    // 内部挑棍：从下方活动环（略低、略内收一些）到主伞骨中段
    base.set(Math.cos(a) * 0.18, 0.90, Math.sin(a) * 0.18);
    tip.copy(hub).addScaledVector(dir, ribLen * 0.55);
    mid.addVectors(base, tip).multiplyScalar(0.5);
    spanDir.copy(tip).sub(base);
    const spanLen = spanDir.length();
    spanDir.normalize();
    q2.setFromUnitVectors(up, spanDir);
    scl2.set(1, spanLen, 1);
    m2.compose(mid, q2, scl2);
    stretcherMesh.setMatrixAt(i, m2);
  }
  ribsMesh.instanceMatrix.needsUpdate = true;
  ribWrapMesh.instanceMatrix.needsUpdate = true;
  stretcherMesh.instanceMatrix.needsUpdate = true;
  umbrellaGroup.add(ribsGroup);

  handleGroup = new THREE.Group();
  handleGroup.userData.part = 'handle';

  // 伞柄：参照参考图，做成略有收分的整根竹柄，带有多个竹节与底部握柄
  const rodLen = 2.1;
  const rodGeo = new THREE.CylinderGeometry(0.032, 0.036, rodLen, 24);
  const rod = new THREE.Mesh(rodGeo, handleMat);
  // 上端仍与伞骨汇聚点（y ≈ 0.95）对齐
  rod.position.set(0, 0.95 - rodLen * 0.5, 0);
  handleGroup.add(rod);

  // 计算竹柄底端 y，供竹节与尾帽定位
  const rodBottomY = rod.position.y - rodLen * 0.5;

  // 沿伞柄添加数个略鼓起的竹节环，更贴近古风竹柄
  const nodeGeo = new THREE.CylinderGeometry(0.038, 0.04, 0.02, 24);
  const nodeOffsets = [0.25, 0.75, 1.25, 1.75];
  nodeOffsets.forEach((offset) => {
    const node = new THREE.Mesh(nodeGeo, handleMat);
    node.position.set(0, rodBottomY + offset, 0);
    handleGroup.add(node);
  });

  // 尾部握柄：略粗一段 + 尾帽，参考实物木柄造型
  const gripGeo = new THREE.CylinderGeometry(0.045, 0.042, 0.14, 24);
  const grip = new THREE.Mesh(gripGeo, handleMat);
  grip.position.set(0, rodBottomY - 0.09, 0);
  handleGroup.add(grip);

  const endCapGeo = new THREE.CylinderGeometry(0.048, 0.03, 0.06, 24);
  const endCap = new THREE.Mesh(endCapGeo, handleMat);
  endCap.position.set(0, rodBottomY - 0.15, 0);
  handleGroup.add(endCap);

  umbrellaGroup.add(handleGroup);

  canopyMesh.position.copy(canopyHome);
  ribsGroup.position.copy(ribsHome);
  handleGroup.position.copy(handleHome);

  resize();
  applyWoodTextures();
  applyPattern();
  applyRibShadow();
  updatePartColors();
  setPartsPosition(1);

  // 使用 @use-gesture/vanilla 创建拖拽手势
  activeDragGesture = createDragGesture(renderer.domElement);
  
  window.addEventListener('resize', resize);

  tick();
};

watch(
  () => props.exploded,
  (v) => {
    if (!umbrellaGroup) return;

    if (v) {
      if (trayGroup && trayGroup.parent !== scene) scene.add(trayGroup);

      canopyMesh && trayGroup && trayGroup.attach(canopyMesh);
      ribsGroup && trayGroup && trayGroup.attach(ribsGroup);
      handleGroup && trayGroup && trayGroup.attach(handleGroup);

      animateExplode(1);
    } else {
      animateExplode(0, () => {
        canopyMesh && umbrellaGroup && umbrellaGroup.attach(canopyMesh);
        ribsGroup && umbrellaGroup && umbrellaGroup.attach(ribsGroup);
        handleGroup && umbrellaGroup && umbrellaGroup.attach(handleGroup);
        trayGroup && trayGroup.parent && trayGroup.parent.remove(trayGroup);
      });
    }

    if (!v) {
      snappedState.canopy = false;
      snappedState.ribs = false;
      snappedState.handle = false;
    }
  }
);

onMounted(() => {
  init();
});

onBeforeUnmount(() => {
  cancelAnimationFrame(rafId);
  window.removeEventListener('resize', resize);
  // 清理 @use-gesture 手势
  if (activeDragGesture) {
    activeDragGesture.destroy();
    activeDragGesture = null;
  }
  renderer?.domElement?.style && (renderer.domElement.style.touchAction = '');
  if (controls && onControlsStart && onControlsEnd) {
    controls.removeEventListener('start', onControlsStart);
    controls.removeEventListener('end', onControlsEnd);
  }
  controls?.dispose?.();
  renderer?.dispose?.();
});

watch(
  () => [props.canopyColor, props.ribsColor, props.handleColor],
  () => {
    updatePartColors();
  }
);

watch(
  () => [props.canopyColor, props.patternId, props.patternScale],
  () => {
    applyPattern();
  }
);

watch(
  () => props.customPatternUrl,
  (url) => {
    const token = (customPatternLoadToken += 1);
    if (!url) {
      customPatternImage = null;
      applyPattern();
      return;
    }

    const img = new Image();
    img.onload = () => {
      if (token !== customPatternLoadToken) return;
      customPatternImage = img;
      applyPattern();
    };
    img.onerror = () => {
      if (token !== customPatternLoadToken) return;
      customPatternImage = null;
      applyPattern();
    };
    img.src = url;
  }
);

const getPngDataUrl = () => {
  if (!renderer) return null;
  const rect = hostRef.value?.getBoundingClientRect?.();
  if (!rect) return renderer.domElement.toDataURL('image/png');

  const w = Math.max(1, Math.floor(rect.width));
  const h = Math.max(1, Math.floor(rect.height));
  const oldPixelRatio = renderer.getPixelRatio();
  const oldSize = new THREE.Vector2();
  renderer.getSize(oldSize);

  const exportPixelRatio = Math.min((window.devicePixelRatio || 1) * 2, 3);
  renderer.setPixelRatio(exportPixelRatio);
  renderer.setSize(w, h, false);
  renderer.render(scene, camera);
  const url = renderer.domElement.toDataURL('image/png');

  renderer.setPixelRatio(oldPixelRatio);
  renderer.setSize(oldSize.x, oldSize.y, false);
  renderer.render(scene, camera);

  return url;
};

defineExpose({ getPngDataUrl, setSelected });
</script>

<style scoped>
.host {
  width: 100%;
  height: 100%;
}

.host :deep(canvas) {
  width: 100%;
  height: 100%;
  display: block;
}
</style>
