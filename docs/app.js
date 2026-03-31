const canvas = document.getElementById('graph');
const ctx = canvas.getContext('2d');
const exprInput = document.getElementById('exprInput');
const addBtn = document.getElementById('addBtn');
const functionList = document.getElementById('functionList');
const coordsLabel = document.getElementById('coords');
const statusLabel = document.getElementById('status');
const analysisSelect = document.getElementById('analysisFunction');
const derivativeBtn = document.getElementById('derivativeBtn');
const clearDerivativeBtn = document.getElementById('clearDerivativeBtn');
const integrateBtn = document.getElementById('integrateBtn');
const clearAreaBtn = document.getElementById('clearAreaBtn');
const intStartInput = document.getElementById('intStart');
const intEndInput = document.getElementById('intEnd');
const integralResult = document.getElementById('integralResult');
const slidersContainer = document.getElementById('sliders');
const tMinInput = document.getElementById('tMin');
const tMaxInput = document.getElementById('tMax');
const thetaMinInput = document.getElementById('thetaMin');
const thetaMaxInput = document.getElementById('thetaMax');
const minXInput = document.getElementById('minX');
const maxXInput = document.getElementById('maxX');
const minYInput = document.getElementById('minY');
const maxYInput = document.getElementById('maxY');
const applyViewBtn = document.getElementById('applyView');
const resetViewBtn = document.getElementById('resetView');
const zoomInBtn = document.getElementById('zoomIn');
const zoomOutBtn = document.getElementById('zoomOut');
const autoFitBtn = document.getElementById('autoFit');

const palette = [
  '#ef4444', '#3b82f6', '#22c55e', '#f59e0b', '#a855f7',
  '#06b6d4', '#f97316', '#ec4899', '#14b8a6', '#eab308'
];

if (!Math.log10) {
  Math.log10 = (x) => Math.log(x) / Math.LN10;
}

let functions = [];
let nextFunctionId = 1;
let variableConfig = {};
let variableValues = {};
let view = { minX: -10, maxX: 10, minY: -10, maxY: 10 };
let dragging = false;
let lastMouse = null;
let area = null;

function resizeCanvas() {
  const rect = canvas.getBoundingClientRect();
  canvas.width = rect.width * devicePixelRatio;
  canvas.height = rect.height * devicePixelRatio;
  ctx.setTransform(devicePixelRatio, 0, 0, devicePixelRatio, 0, 0);
  draw();
}

window.addEventListener('resize', resizeCanvas);
resizeCanvas();

function setStatus(text) {
  statusLabel.textContent = text;
  statusLabel.classList.remove('error', 'ok');
  if (text.toLowerCase().startsWith('error')) {
    statusLabel.classList.add('error');
  } else if (
    text.toLowerCase().startsWith('plotted') ||
    text.toLowerCase().startsWith('ready') ||
    text.toLowerCase().startsWith('updated')
  ) {
    statusLabel.classList.add('ok');
  }
}

function toScreen(x, y) {
  const w = canvas.clientWidth;
  const h = canvas.clientHeight;
  const sx = ((x - view.minX) / (view.maxX - view.minX)) * w;
  const sy = h - ((y - view.minY) / (view.maxY - view.minY)) * h;
  return { x: sx, y: sy };
}

function toMath(x, y) {
  const w = canvas.clientWidth;
  const h = canvas.clientHeight;
  const mx = view.minX + (x / w) * (view.maxX - view.minX);
  const my = view.minY + (1 - y / h) * (view.maxY - view.minY);
  return { x: mx, y: my };
}

const FUNCTION_MAP = {
  asin: 'Math.asin',
  acos: 'Math.acos',
  atan: 'Math.atan',
  sin: 'Math.sin',
  cos: 'Math.cos',
  tan: 'Math.tan',
  log10: 'Math.log10',
  log: 'Math.log10',
  ln: 'Math.log',
  exp: 'Math.exp',
  sqrt: 'Math.sqrt',
  abs: 'Math.abs',
  floor: 'Math.floor',
  ceil: 'Math.ceil',
  round: 'Math.round',
  sign: 'Math.sign',
  min: 'Math.min',
  max: 'Math.max',
  ncr: 'helpers.nCr',
  npr: 'helpers.nPr',
  factorial: 'helpers.factorial'
};

const CONSTANT_MAP = {
  pi: 'Math.PI',
  e: 'Math.E'
};

function tokenize(expr) {
  const tokens = [];
  let i = 0;
  while (i < expr.length) {
    const ch = expr[i];
    if (/\s/.test(ch)) {
      i += 1;
      continue;
    }
    if (/[0-9.]/.test(ch)) {
      let num = ch;
      i += 1;
      while (i < expr.length && /[0-9.]/.test(expr[i])) {
        num += expr[i];
        i += 1;
      }
      tokens.push({ type: 'number', value: num });
      continue;
    }
    if (/[a-z_]/.test(ch)) {
      let name = ch;
      i += 1;
      while (i < expr.length && /[a-z0-9_]/.test(expr[i])) {
        name += expr[i];
        i += 1;
      }
      tokens.push({ type: 'ident', value: name });
      continue;
    }
    if ('+-*/^(),'.includes(ch)) {
      tokens.push({ type: 'op', value: ch });
      i += 1;
      continue;
    }
    throw new Error('Invalid character in expression');
  }
  return tokens;
}

function isFunctionToken(token) {
  return token.type === 'ident' && Object.prototype.hasOwnProperty.call(FUNCTION_MAP, token.value);
}

function isVariableToken(token) {
  return token.type === 'ident' && !isFunctionToken(token);
}

function insertImplicitMultiplication(tokens) {
  const result = [];
  for (let i = 0; i < tokens.length; i++) {
    const current = tokens[i];
    const next = tokens[i + 1];
    result.push(current);
    if (!next) continue;

    if (isFunctionToken(current) && next.value === '(') {
      continue;
    }

    const currentIsValue = current.type === 'number' || isVariableToken(current) || current.value === ')';
    const nextIsValue =
      next.type === 'number' ||
      isVariableToken(next) ||
      next.value === '(' ||
      isFunctionToken(next);

    if (currentIsValue && nextIsValue) {
      result.push({ type: 'op', value: '*' });
    }
  }
  return result;
}

function normalizeExpression(expr) {
  let s = expr.toLowerCase();
  s = s.replace(/\s+/g, '');
  s = s.replace(/√/g, 'sqrt');
  s = s.replace(/π/g, 'pi');
  s = s.replace(/θ/g, 'theta');

  let tokens = tokenize(s);
  tokens = insertImplicitMultiplication(tokens);

  const variables = new Set();
  const output = tokens.map((token) => {
    if (token.type === 'ident') {
      if (FUNCTION_MAP[token.value]) {
        return FUNCTION_MAP[token.value];
      }
      if (CONSTANT_MAP[token.value]) {
        return CONSTANT_MAP[token.value];
      }
      if (token.value === 'x' || token.value === 'y' || token.value === 't' || token.value === 'theta') {
        return token.value;
      }
      variables.add(token.value);
      return `vars.${token.value}`;
    }
    if (token.type === 'op' && token.value === '^') {
      return '**';
    }
    return token.value;
  });

  return { expression: output.join(''), variables: Array.from(variables) };
}

const HELPERS = {
  factorial: (n) => {
    if (!Number.isFinite(n) || n < 0) return NaN;
    const k = Math.floor(n);
    if (k > 170) return Infinity;
    let result = 1;
    for (let i = 2; i <= k; i++) result *= i;
    return result;
  },
  nCr: (n, r) => {
    if (!Number.isFinite(n) || !Number.isFinite(r)) return NaN;
    const nn = Math.floor(n);
    const rr = Math.floor(r);
    if (rr < 0 || nn < 0 || rr > nn) return NaN;
    return HELPERS.factorial(nn) / (HELPERS.factorial(rr) * HELPERS.factorial(nn - rr));
  },
  nPr: (n, r) => {
    if (!Number.isFinite(n) || !Number.isFinite(r)) return NaN;
    const nn = Math.floor(n);
    const rr = Math.floor(r);
    if (rr < 0 || nn < 0 || rr > nn) return NaN;
    return HELPERS.factorial(nn) / HELPERS.factorial(nn - rr);
  }
};

function compileExpression(expr) {
  const normalized = normalizeExpression(expr);
  // eslint-disable-next-line no-new-func
  const fn = new Function('x', 'y', 't', 'theta', 'vars', 'helpers', `return ${normalized.expression};`);
  const evaluator = (x, y = 0, t = 0, theta = 0) => {
    const vars = variableValues;
    const value = fn(x, y, t, theta, vars, HELPERS);
    if (Number.isNaN(value) || !Number.isFinite(value)) return null;
    return value;
  };
  return { evaluator, variables: normalized.variables };
}

function splitTopLevel(str) {
  let depth = 0;
  for (let i = 0; i < str.length; i++) {
    const ch = str[i];
    if (ch === '(') depth += 1;
    if (ch === ')') depth -= 1;
    if (ch === ',' && depth === 0) {
      return [str.slice(0, i), str.slice(i + 1)];
    }
  }
  return null;
}

function mergeVariables(...lists) {
  const set = new Set();
  lists.flat().forEach((name) => set.add(name));
  return Array.from(set);
}

function passesRestrictions(fn, x, y) {
  if (!fn.restrictions || fn.restrictions.length === 0) return true;
  return fn.restrictions.every((predicate) => predicate(x, y));
}

function extractRestrictions(expr) {
  const restrictions = [];
  let base = expr;
  const matches = [...expr.matchAll(/\{([^}]+)\}/g)];
  matches.forEach((match) => {
    const parts = match[1].split(',');
    parts.forEach((part) => {
      const clause = part.trim();
      if (clause) restrictions.push(clause);
    });
  });
  base = base.replace(/\{[^}]+\}/g, '').trim();
  return { base, restrictions };
}

function compileRestriction(clause) {
  const match = clause.match(/^(x|y)\s*(<=|>=|<|>)\s*(.+)$/i);
  if (!match) return null;
  const axis = match[1].toLowerCase();
  const op = match[2];
  const rhs = match[3];
  const compiled = compileExpression(rhs);
  const predicate = (x, y) => {
    const left = axis === 'x' ? x : y;
    const right = compiled.evaluator(x, y, 0, 0);
    if (right === null) return false;
    if (op === '<') return left < right;
    if (op === '<=') return left <= right;
    if (op === '>') return left > right;
    if (op === '>=') return left >= right;
    return false;
  };
  return { predicate, variables: compiled.variables };
}

function createFunctionFromInput(raw, overrides = {}) {
  const trimmed = raw.trim();
  const { base, restrictions } = extractRestrictions(trimmed);
  const restrictionEntries = restrictions.map(compileRestriction).filter(Boolean);
  const restrictionFns = restrictionEntries.map((entry) => entry.predicate);
  const restrictionVars = mergeVariables(...restrictionEntries.map((entry) => entry.variables));
  const color = overrides.color || palette[functions.length % palette.length];
  const id = overrides.id || nextFunctionId++;
  const meta = overrides.meta || { type: 'base' };

  const inequality = base.match(/^(y)\s*(<=|>=|<|>)\s*(.+)$/i);
  if (inequality) {
    const rhs = inequality[3];
    const compiled = compileExpression(rhs);
    return {
      id,
      expr: trimmed,
      color,
      evaluator: compiled.evaluator,
      variables: mergeVariables(compiled.variables, restrictionVars),
      kind: 'inequality',
      op: inequality[2],
      restrictions: restrictionFns,
      meta
    };
  }

  const equality = base.match(/^([xy]|r)\s*=\s*(.+)$/i);
  if (equality) {
    const lhs = equality[1].toLowerCase();
    const rhs = equality[2];
    if (lhs === 'y') {
      const compiled = compileExpression(rhs);
      return {
        id,
        expr: trimmed,
        color,
        evaluator: compiled.evaluator,
        variables: mergeVariables(compiled.variables, restrictionVars),
        kind: 'function',
        restrictions: restrictionFns,
        meta
      };
    }
    if (lhs === 'x') {
      const compiled = compileExpression(rhs);
      return {
        id,
        expr: trimmed,
        color,
        xEvaluator: compiled.evaluator,
        variables: mergeVariables(compiled.variables, restrictionVars),
        kind: 'vertical',
        restrictions: restrictionFns,
        meta
      };
    }
    if (lhs === 'r') {
      const compiled = compileExpression(rhs);
      return {
        id,
        expr: trimmed,
        color,
        rEvaluator: compiled.evaluator,
        variables: mergeVariables(compiled.variables, restrictionVars),
        kind: 'polar',
        restrictions: restrictionFns,
        meta
      };
    }
  }

  if (base.startsWith('(') && base.endsWith(')')) {
    const inner = base.slice(1, -1);
    const parts = splitTopLevel(inner);
    if (parts) {
      const [xExpr, yExpr] = parts;
      const xCompiled = compileExpression(xExpr);
      const yCompiled = compileExpression(yExpr);
      const vars = mergeVariables(xCompiled.variables, yCompiled.variables, restrictionVars);
      const usesParam = /\\bt\\b|\\btheta\\b|θ/i.test(inner);
      return {
        id,
        expr: trimmed,
        color,
        xEvaluator: xCompiled.evaluator,
        yEvaluator: yCompiled.evaluator,
        variables: vars,
        kind: usesParam ? 'parametric' : 'point',
        restrictions: restrictionFns,
        meta
      };
    }
  }

  const compiled = compileExpression(base);
  return {
    id,
    expr: trimmed,
    color,
    evaluator: compiled.evaluator,
    variables: mergeVariables(compiled.variables, restrictionVars),
    kind: 'function',
    restrictions: restrictionFns,
    meta
  };
}

function addFunction(expr) {
  if (!expr.trim()) return;
  try {
    const fn = createFunctionFromInput(expr, { meta: { type: 'base' } });
    fn.visible = true;
    functions.push(fn);
    exprInput.value = '';
    setStatus('Plotted: ' + expr);
    renderFunctionList();
    draw();
  } catch (err) {
    setStatus('Error: ' + err.message);
  }
}

function renderFunctionList() {
  functionList.innerHTML = '';
  analysisSelect.innerHTML = '';
  const baseFunctions = [];
  functions.forEach((fn, i) => {
    if (fn.meta?.type === 'base' && fn.kind === 'function') {
      baseFunctions.push({ fn, index: i });
    }
  });

  if (baseFunctions.length === 0) {
    const opt = document.createElement('option');
    opt.value = '';
    opt.textContent = 'No functions yet';
    analysisSelect.appendChild(opt);
    analysisSelect.disabled = true;
  } else {
    analysisSelect.disabled = false;
    baseFunctions.forEach((entry) => {
      const opt = document.createElement('option');
      opt.value = entry.index;
      opt.textContent = entry.fn.expr;
      analysisSelect.appendChild(opt);
    });
  }
  functions.forEach((fn, idx) => {
    const item = document.createElement('div');
    item.className = 'function-item';

    const colorBar = document.createElement('div');
    colorBar.className = 'color-bar';
    colorBar.style.background = fn.color;

    const exprField = document.createElement('input');
    exprField.className = 'expr';
    exprField.value = fn.expr;
    exprField.disabled = fn.meta?.type === 'derivative';
    if (fn.meta?.type === 'derivative') {
      exprField.style.opacity = '0.6';
    }
    exprField.addEventListener('keydown', (event) => {
      if (event.key === 'Enter') {
        exprField.blur();
      }
    });
    exprField.addEventListener('blur', () => {
      if (exprField.value.trim() === fn.expr) return;
      try {
        const updated = createFunctionFromInput(exprField.value, {
          id: fn.id,
          color: fn.color,
          meta: fn.meta
        });
        updated.visible = fn.visible;
        functions[idx] = updated;
        if (area && area.fn.id === updated.id) {
          area.fn = updated;
        }
        setStatus('Updated: ' + updated.expr);
        if (updated.kind !== 'function') {
          functions = functions.filter((item) => item.meta?.sourceId !== updated.id);
        } else {
          functions.forEach((other) => {
            if (other.meta?.type === 'derivative' && other.meta?.sourceId === updated.id) {
              other.expr = `d/dx(${updated.expr})`;
              other.evaluator = makeDerivativeEvaluator(updated);
            }
          });
        }
        renderFunctionList();
        draw();
      } catch (err) {
        exprField.value = fn.expr;
        setStatus('Error: ' + err.message);
      }
    });

    const deleteBtn = document.createElement('button');
    deleteBtn.textContent = 'Remove';
    deleteBtn.className = 'delete';
    deleteBtn.addEventListener('click', () => {
      const removed = functions[idx];
      functions = functions.filter((item, i) => {
        if (i === idx) return false;
        if (removed.meta?.type === 'base' && item.meta?.sourceId === removed.id) {
          return false;
        }
        return true;
      });
      if (area && removed === area.fn) {
        area = null;
        integralResult.textContent = 'Integral: —';
      }
      renderFunctionList();
      draw();
    });

    const right = document.createElement('div');
    right.style.display = 'flex';
    right.style.alignItems = 'center';
    right.style.gap = '8px';
    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.checked = fn.visible;
    checkbox.addEventListener('change', () => {
      fn.visible = checkbox.checked;
      draw();
    });
    right.appendChild(checkbox);
    right.appendChild(deleteBtn);

    item.appendChild(colorBar);
    item.appendChild(exprField);
    item.appendChild(right);
    functionList.appendChild(item);
  });

  rebuildVariableConfig();
}

function rebuildVariableConfig() {
  const used = new Set();
  functions.forEach((fn) => {
    (fn.variables || []).forEach((name) => used.add(name));
  });

  Object.keys(variableConfig).forEach((name) => {
    if (!used.has(name)) {
      delete variableConfig[name];
    }
  });

  used.forEach((name) => {
    if (!variableConfig[name]) {
      variableConfig[name] = { value: 1, min: -10, max: 10, step: 0.1 };
    }
  });

  syncVariableValues();
  renderSliders();
}

function syncVariableValues() {
  variableValues = {};
  Object.entries(variableConfig).forEach(([name, cfg]) => {
    variableValues[name] = cfg.value;
  });
}

function renderSliders() {
  slidersContainer.innerHTML = '';
  const names = Object.keys(variableConfig);
  if (names.length === 0) {
    const empty = document.createElement('div');
    empty.className = 'hint';
    empty.textContent = 'No sliders yet.';
    slidersContainer.appendChild(empty);
    return;
  }
  names.sort().forEach((name) => {
    const cfg = variableConfig[name];
    const row = document.createElement('div');
    row.className = 'slider-row';

    const label = document.createElement('div');
    label.className = 'var-name';
    label.textContent = name;

    const slider = document.createElement('input');
    slider.type = 'range';
    slider.min = cfg.min;
    slider.max = cfg.max;
    slider.step = cfg.step;
    slider.value = cfg.value;

    const number = document.createElement('input');
    number.type = 'number';
    number.value = cfg.value;
    number.step = cfg.step;

    const updateValue = (value) => {
      const v = Number(value);
      if (!Number.isFinite(v)) return;
      cfg.value = v;
      slider.value = v;
      number.value = v;
      syncVariableValues();
      draw();
    };

    slider.addEventListener('input', (e) => updateValue(e.target.value));
    number.addEventListener('change', (e) => updateValue(e.target.value));

    row.appendChild(label);
    row.appendChild(slider);
    row.appendChild(number);
    slidersContainer.appendChild(row);
  });
}

function drawGrid() {
  const w = canvas.clientWidth;
  const h = canvas.clientHeight;
  ctx.clearRect(0, 0, w, h);

  ctx.fillStyle = '#ffffff';
  ctx.fillRect(0, 0, w, h);

  ctx.lineWidth = 1;
  ctx.strokeStyle = 'rgba(15, 23, 42, 0.08)';

  const xStep = niceStep(view.maxX - view.minX, w, 120);
  const yStep = niceStep(view.maxY - view.minY, h, 120);

  for (let x = Math.ceil(view.minX / xStep) * xStep; x <= view.maxX; x += xStep) {
    const sx = toScreen(x, 0).x;
    ctx.beginPath();
    ctx.moveTo(sx, 0);
    ctx.lineTo(sx, h);
    ctx.stroke();
  }

  for (let y = Math.ceil(view.minY / yStep) * yStep; y <= view.maxY; y += yStep) {
    const sy = toScreen(0, y).y;
    ctx.beginPath();
    ctx.moveTo(0, sy);
    ctx.lineTo(w, sy);
    ctx.stroke();
  }

  ctx.strokeStyle = 'rgba(15, 23, 42, 0.35)';
  const origin = toScreen(0, 0);
  ctx.beginPath();
  ctx.moveTo(origin.x, 0);
  ctx.lineTo(origin.x, h);
  ctx.stroke();
  ctx.beginPath();
  ctx.moveTo(0, origin.y);
  ctx.lineTo(w, origin.y);
  ctx.stroke();

  ctx.fillStyle = 'rgba(15, 23, 42, 0.55)';
  ctx.font = '11px "JetBrains Mono"';

  for (let x = Math.ceil(view.minX / xStep) * xStep; x <= view.maxX; x += xStep) {
    if (Math.abs(x) < 1e-8) continue;
    const sx = toScreen(x, 0).x;
    ctx.fillText(formatNumber(x), sx + 4, origin.y - 4);
  }

  for (let y = Math.ceil(view.minY / yStep) * yStep; y <= view.maxY; y += yStep) {
    if (Math.abs(y) < 1e-8) continue;
    const sy = toScreen(0, y).y;
    ctx.fillText(formatNumber(y), origin.x + 6, sy - 4);
  }
}

function drawFunctions() {
  if (area && functions.includes(area.fn) && area.fn.kind === 'function') {
    drawArea(area.fn, area.a, area.b);
  }

  functions.forEach((fn) => {
    if (!fn.visible) return;
    switch (fn.kind) {
      case 'function':
        drawExplicitFunction(fn);
        break;
      case 'inequality':
        drawInequality(fn);
        break;
      case 'vertical':
        drawVerticalFunction(fn);
        break;
      case 'point':
        drawPoint(fn);
        break;
      case 'parametric':
        drawParametric(fn);
        break;
      case 'polar':
        drawPolar(fn);
        break;
      default:
        drawExplicitFunction(fn);
    }
  });
}

function drawExplicitFunction(fn) {
  const w = canvas.clientWidth;
  const rangeX = view.maxX - view.minX;
  const rangeY = view.maxY - view.minY;
  const step = rangeX / Math.max(900, w * 2);
  const jumpLimit = rangeY * 2.5;
  const clipLimit = rangeY * 6;

  ctx.strokeStyle = fn.color;
  ctx.lineWidth = 2;
  ctx.beginPath();
  let started = false;
  let prevY = null;
  for (let x = view.minX; x <= view.maxX; x += step) {
    const y = fn.evaluator(x, 0, 0, 0);
    if (y === null || !Number.isFinite(y)) {
      started = false;
      prevY = null;
      continue;
    }
    if (!passesRestrictions(fn, x, y)) {
      started = false;
      prevY = null;
      continue;
    }
    if (y > view.maxY + clipLimit || y < view.minY - clipLimit) {
      started = false;
      prevY = null;
      continue;
    }
    if (prevY !== null && Math.abs(y - prevY) > jumpLimit) {
      started = false;
      prevY = null;
      continue;
    }
    const pt = toScreen(x, y);
    if (!started) {
      ctx.moveTo(pt.x, pt.y);
      started = true;
    } else {
      ctx.lineTo(pt.x, pt.y);
    }
    prevY = y;
  }
  ctx.stroke();
}

function drawVerticalFunction(fn) {
  const h = canvas.clientHeight;
  const rangeY = view.maxY - view.minY;
  const rangeX = view.maxX - view.minX;
  const step = rangeY / Math.max(900, h * 2);
  const jumpLimit = rangeX * 2.5;
  const clipLimit = rangeX * 6;

  ctx.strokeStyle = fn.color;
  ctx.lineWidth = 2;
  ctx.beginPath();
  let started = false;
  let prevX = null;
  for (let y = view.minY; y <= view.maxY; y += step) {
    const x = fn.xEvaluator(0, y, 0, 0);
    if (x === null || !Number.isFinite(x)) {
      started = false;
      prevX = null;
      continue;
    }
    if (!passesRestrictions(fn, x, y)) {
      started = false;
      prevX = null;
      continue;
    }
    if (x > view.maxX + clipLimit || x < view.minX - clipLimit) {
      started = false;
      prevX = null;
      continue;
    }
    if (prevX !== null && Math.abs(x - prevX) > jumpLimit) {
      started = false;
      prevX = null;
      continue;
    }
    const pt = toScreen(x, y);
    if (!started) {
      ctx.moveTo(pt.x, pt.y);
      started = true;
    } else {
      ctx.lineTo(pt.x, pt.y);
    }
    prevX = x;
  }
  ctx.stroke();
}

function drawParametric(fn) {
  const tMin = parseFloat(tMinInput.value);
  const tMax = parseFloat(tMaxInput.value);
  if (!Number.isFinite(tMin) || !Number.isFinite(tMax)) return;
  const steps = 1000;
  const step = (tMax - tMin) / steps;
  const jumpLimit = Math.hypot(view.maxX - view.minX, view.maxY - view.minY) * 1.5;

  ctx.strokeStyle = fn.color;
  ctx.lineWidth = 2;
  ctx.beginPath();
  let started = false;
  let prev = null;
  for (let t = tMin; t <= tMax; t += step) {
    const x = fn.xEvaluator(0, 0, t, 0);
    const y = fn.yEvaluator(0, 0, t, 0);
    if (x === null || y === null || !Number.isFinite(x) || !Number.isFinite(y)) {
      started = false;
      prev = null;
      continue;
    }
    if (!passesRestrictions(fn, x, y)) {
      started = false;
      prev = null;
      continue;
    }
    if (prev && Math.hypot(x - prev.x, y - prev.y) > jumpLimit) {
      started = false;
      prev = null;
      continue;
    }
    const pt = toScreen(x, y);
    if (!started) {
      ctx.moveTo(pt.x, pt.y);
      started = true;
    } else {
      ctx.lineTo(pt.x, pt.y);
    }
    prev = { x, y };
  }
  ctx.stroke();
}

function drawPolar(fn) {
  const tMin = parseFloat(thetaMinInput.value);
  const tMax = parseFloat(thetaMaxInput.value);
  if (!Number.isFinite(tMin) || !Number.isFinite(tMax)) return;
  const steps = 1000;
  const step = (tMax - tMin) / steps;
  const jumpLimit = Math.hypot(view.maxX - view.minX, view.maxY - view.minY) * 1.5;

  ctx.strokeStyle = fn.color;
  ctx.lineWidth = 2;
  ctx.beginPath();
  let started = false;
  let prev = null;
  for (let theta = tMin; theta <= tMax; theta += step) {
    const r = fn.rEvaluator(0, 0, 0, theta);
    if (r === null || !Number.isFinite(r)) {
      started = false;
      prev = null;
      continue;
    }
    const x = r * Math.cos(theta);
    const y = r * Math.sin(theta);
    if (!passesRestrictions(fn, x, y)) {
      started = false;
      prev = null;
      continue;
    }
    if (prev && Math.hypot(x - prev.x, y - prev.y) > jumpLimit) {
      started = false;
      prev = null;
      continue;
    }
    const pt = toScreen(x, y);
    if (!started) {
      ctx.moveTo(pt.x, pt.y);
      started = true;
    } else {
      ctx.lineTo(pt.x, pt.y);
    }
    prev = { x, y };
  }
  ctx.stroke();
}

function drawPoint(fn) {
  const x = fn.xEvaluator(0, 0, 0, 0);
  const y = fn.yEvaluator(0, 0, 0, 0);
  if (x === null || y === null || !Number.isFinite(x) || !Number.isFinite(y)) return;
  if (!passesRestrictions(fn, x, y)) return;
  if (x < view.minX || x > view.maxX || y < view.minY || y > view.maxY) return;
  const pt = toScreen(x, y);
  ctx.fillStyle = fn.color;
  ctx.beginPath();
  ctx.arc(pt.x, pt.y, 4, 0, Math.PI * 2);
  ctx.fill();
}

function drawInequality(fn) {
  const w = canvas.clientWidth;
  const rangeX = view.maxX - view.minX;
  const rangeY = view.maxY - view.minY;
  const step = rangeX / Math.max(900, w * 2);
  const clipLimit = rangeY * 6;

  ctx.beginPath();
  let started = false;
  for (let x = view.minX; x <= view.maxX; x += step) {
    const y = fn.evaluator(x, 0, 0, 0);
    if (y === null || !Number.isFinite(y)) {
      started = false;
      continue;
    }
    if (!passesRestrictions(fn, x, y)) {
      started = false;
      continue;
    }
    if (y > view.maxY + clipLimit || y < view.minY - clipLimit) {
      started = false;
      continue;
    }
    const pt = toScreen(x, y);
    if (!started) {
      ctx.moveTo(pt.x, pt.y);
      started = true;
    } else {
      ctx.lineTo(pt.x, pt.y);
    }
  }

  if (started) {
    if (fn.op === '<' || fn.op === '<=') {
      ctx.lineTo(toScreen(view.maxX, view.minY).x, toScreen(view.maxX, view.minY).y);
      ctx.lineTo(toScreen(view.minX, view.minY).x, toScreen(view.minX, view.minY).y);
    } else {
      ctx.lineTo(toScreen(view.maxX, view.maxY).x, toScreen(view.maxX, view.maxY).y);
      ctx.lineTo(toScreen(view.minX, view.maxY).x, toScreen(view.minX, view.maxY).y);
    }
    ctx.closePath();
    ctx.fillStyle = hexToRgba(fn.color, 0.18);
    ctx.fill();
  }

  drawExplicitFunction(fn);
}

function drawArea(fn, a, b) {
  if (!fn.visible) return;
  const min = Math.min(a, b);
  const max = Math.max(a, b);
  const steps = 600;
  const step = (max - min) / steps;
  const segments = [];
  let current = [];
  for (let i = 0; i <= steps; i++) {
    const x = min + i * step;
    const y = fn.evaluator(x, 0, 0, 0);
    if (y === null || !Number.isFinite(y)) {
      if (current.length > 1) segments.push(current);
      current = [];
      continue;
    }
    if (!passesRestrictions(fn, x, y)) {
      if (current.length > 1) segments.push(current);
      current = [];
      continue;
    }
    current.push({ x, y });
  }
  if (current.length > 1) segments.push(current);

  segments.forEach((seg) => {
    ctx.beginPath();
    seg.forEach((pt, idx) => {
      const screen = toScreen(pt.x, pt.y);
      if (idx === 0) ctx.moveTo(screen.x, screen.y);
      else ctx.lineTo(screen.x, screen.y);
    });
    const last = seg[seg.length - 1];
    const first = seg[0];
    const baseLast = toScreen(last.x, 0);
    const baseFirst = toScreen(first.x, 0);
    ctx.lineTo(baseLast.x, baseLast.y);
    ctx.lineTo(baseFirst.x, baseFirst.y);
    ctx.closePath();
    ctx.fillStyle = hexToRgba(fn.color, 0.2);
    ctx.fill();
  });

  const aLine = toScreen(min, 0).x;
  const bLine = toScreen(max, 0).x;
  ctx.save();
  ctx.strokeStyle = hexToRgba(fn.color, 0.5);
  ctx.setLineDash([6, 6]);
  ctx.beginPath();
  ctx.moveTo(aLine, 0);
  ctx.lineTo(aLine, canvas.clientHeight);
  ctx.moveTo(bLine, 0);
  ctx.lineTo(bLine, canvas.clientHeight);
  ctx.stroke();
  ctx.restore();
}

function hexToRgba(hex, alpha) {
  const value = hex.replace('#', '');
  const r = parseInt(value.substring(0, 2), 16);
  const g = parseInt(value.substring(2, 4), 16);
  const b = parseInt(value.substring(4, 6), 16);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

function draw() {
  drawGrid();
  drawFunctions();
}

function numericDerivative(fn, x) {
  const range = view.maxX - view.minX;
  let h = Math.max(1e-6, Math.abs(x) * 1e-4, range / 10000);
  for (let i = 0; i < 6; i++) {
    const f1 = fn(x + h);
    const f2 = fn(x - h);
    if (f1 !== null && f2 !== null && Number.isFinite(f1) && Number.isFinite(f2)) {
      return (f1 - f2) / (2 * h);
    }
    h *= 0.5;
  }
  return null;
}

function makeDerivativeEvaluator(baseFn) {
  return (x, y = 0, t = 0, theta = 0) =>
    numericDerivative((xVal) => baseFn.evaluator(xVal, y, t, theta), x);
}

function compositeSimpson(fn, a, b, n) {
  if (n % 2 !== 0) n += 1;
  const h = (b - a) / n;
  let sum = 0;
  for (let i = 0; i <= n; i++) {
    const x = a + i * h;
    const y = fn(x);
    if (y === null || !Number.isFinite(y)) return null;
    if (i === 0 || i === n) sum += y;
    else if (i % 2 === 0) sum += 2 * y;
    else sum += 4 * y;
  }
  return (h / 3) * sum;
}

function integrateAdaptive(fn, a, b, maxSlices = 800) {
  const min = Math.min(a, b);
  const max = Math.max(a, b);
  const slices = maxSlices;
  const step = (max - min) / slices;
  let segments = [];
  let current = [];
  for (let i = 0; i <= slices; i++) {
    const x = min + i * step;
    const y = fn(x);
    if (y === null || !Number.isFinite(y)) {
      if (current.length > 2) segments.push(current);
      current = [];
      continue;
    }
    current.push(x);
  }
  if (current.length > 2) segments.push(current);
  if (segments.length === 0) return null;

  let total = 0;
  segments.forEach((segment) => {
    const segStart = segment[0];
    const segEnd = segment[segment.length - 1];
    const n = Math.max(50, Math.floor((segEnd - segStart) / step));
    const res = compositeSimpson(fn, segStart, segEnd, n);
    if (res !== null) total += res;
  });
  return a <= b ? total : -total;
}

function niceStep(range, pixels, targetPx) {
  const approx = range * targetPx / pixels;
  const power = Math.pow(10, Math.floor(Math.log10(approx)));
  return power;
}

function formatNumber(val) {
  if (Math.abs(val) >= 1000 || Math.abs(val) < 0.01) {
    return val.toExponential(1);
  }
  return val.toFixed(1);
}

addBtn.addEventListener('click', () => addFunction(exprInput.value));
exprInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter') {
    addFunction(exprInput.value);
  }
});

document.getElementById('quickButtons').addEventListener('click', (e) => {
  const btn = e.target.closest('button');
  if (!btn) return;
  exprInput.value = btn.dataset.insert;
  exprInput.focus();
});

applyViewBtn.addEventListener('click', () => {
  view = {
    minX: parseFloat(minXInput.value),
    maxX: parseFloat(maxXInput.value),
    minY: parseFloat(minYInput.value),
    maxY: parseFloat(maxYInput.value)
  };
  draw();
});

resetViewBtn.addEventListener('click', () => {
  view = { minX: -10, maxX: 10, minY: -10, maxY: 10 };
  minXInput.value = view.minX;
  maxXInput.value = view.maxX;
  minYInput.value = view.minY;
  maxYInput.value = view.maxY;
  draw();
});

zoomInBtn.addEventListener('click', () => zoom(0.8));
zoomOutBtn.addEventListener('click', () => zoom(1.25));
autoFitBtn.addEventListener('click', () => autoFit());

[tMinInput, tMaxInput, thetaMinInput, thetaMaxInput].forEach((input) => {
  input.addEventListener('change', () => draw());
});

derivativeBtn.addEventListener('click', () => {
  const idx = parseInt(analysisSelect.value, 10);
  if (Number.isNaN(idx) || !functions[idx]) {
    setStatus('Error: Select a function');
    return;
  }
  const base = functions[idx];
  if (base.kind !== 'function') {
    setStatus('Error: Select a y = f(x) function');
    return;
  }
  const evaluator = makeDerivativeEvaluator(base);
  const color = base.color;
  const derived = {
    id: nextFunctionId++,
    expr: `d/dx(${base.expr})`,
    color,
    evaluator,
    visible: true,
    variables: base.variables,
    kind: 'function',
    meta: { type: 'derivative', sourceId: base.id }
  };
  functions.push(derived);
  setStatus(`Plotted derivative of ${base.expr}`);
  renderFunctionList();
  draw();
});

clearDerivativeBtn.addEventListener('click', () => {
  functions = functions.filter((fn) => fn.meta?.type !== 'derivative');
  renderFunctionList();
  draw();
});

integrateBtn.addEventListener('click', () => {
  const idx = parseInt(analysisSelect.value, 10);
  if (Number.isNaN(idx) || !functions[idx]) {
    setStatus('Error: Select a function');
    return;
  }
  const a = parseFloat(intStartInput.value);
  const b = parseFloat(intEndInput.value);
  if (!Number.isFinite(a) || !Number.isFinite(b)) {
    setStatus('Error: Invalid bounds');
    return;
  }
  const fn = functions[idx];
  if (fn.kind !== 'function') {
    setStatus('Error: Select a y = f(x) function');
    return;
  }
  const result = integrateAdaptive((x) => {
    const y = fn.evaluator(x, 0, 0, 0);
    if (y === null) return null;
    if (!passesRestrictions(fn, x, y)) return null;
    return y;
  }, a, b);
  if (result === null || !Number.isFinite(result)) {
    setStatus('Error: Integral failed (discontinuity)');
    return;
  }
  area = { fn, a, b };
  integralResult.textContent = `Integral: ${result.toFixed(6)}`;
  setStatus(`Integral computed for ${fn.expr}`);
  draw();
});

clearAreaBtn.addEventListener('click', () => {
  area = null;
  integralResult.textContent = 'Integral: —';
  draw();
});

function zoom(factor) {
  const cx = (view.minX + view.maxX) / 2;
  const cy = (view.minY + view.maxY) / 2;
  const width = (view.maxX - view.minX) * factor;
  const height = (view.maxY - view.minY) * factor;
  view = {
    minX: cx - width / 2,
    maxX: cx + width / 2,
    minY: cy - height / 2,
    maxY: cy + height / 2
  };
  minXInput.value = view.minX.toFixed(2);
  maxXInput.value = view.maxX.toFixed(2);
  minYInput.value = view.minY.toFixed(2);
  maxYInput.value = view.maxY.toFixed(2);
  draw();
}

function autoFit() {
  if (!functions.length) return;
  let minX = Infinity;
  let maxX = -Infinity;
  let minY = Infinity;
  let maxY = -Infinity;

  const sampleExplicit = (fn) => {
    const step = (view.maxX - view.minX) / 400;
    for (let x = view.minX; x <= view.maxX; x += step) {
      const y = fn.evaluator(x, 0, 0, 0);
      if (y === null) continue;
      if (!passesRestrictions(fn, x, y)) continue;
      minX = Math.min(minX, x);
      maxX = Math.max(maxX, x);
      minY = Math.min(minY, y);
      maxY = Math.max(maxY, y);
    }
  };

  const sampleParametric = (fn) => {
    const tMin = parseFloat(tMinInput.value);
    const tMax = parseFloat(tMaxInput.value);
    if (!Number.isFinite(tMin) || !Number.isFinite(tMax)) return;
    const steps = 600;
    const step = (tMax - tMin) / steps;
    for (let t = tMin; t <= tMax; t += step) {
      const x = fn.xEvaluator(0, 0, t, 0);
      const y = fn.yEvaluator(0, 0, t, 0);
      if (x === null || y === null) continue;
      if (!passesRestrictions(fn, x, y)) continue;
      minX = Math.min(minX, x);
      maxX = Math.max(maxX, x);
      minY = Math.min(minY, y);
      maxY = Math.max(maxY, y);
    }
  };

  const samplePolar = (fn) => {
    const tMin = parseFloat(thetaMinInput.value);
    const tMax = parseFloat(thetaMaxInput.value);
    if (!Number.isFinite(tMin) || !Number.isFinite(tMax)) return;
    const steps = 600;
    const step = (tMax - tMin) / steps;
    for (let theta = tMin; theta <= tMax; theta += step) {
      const r = fn.rEvaluator(0, 0, 0, theta);
      if (r === null) continue;
      const x = r * Math.cos(theta);
      const y = r * Math.sin(theta);
      if (!passesRestrictions(fn, x, y)) continue;
      minX = Math.min(minX, x);
      maxX = Math.max(maxX, x);
      minY = Math.min(minY, y);
      maxY = Math.max(maxY, y);
    }
  };

  functions.forEach((fn) => {
    if (!fn.visible) return;
    if (fn.kind === 'function' || fn.kind === 'inequality') {
      sampleExplicit(fn);
    } else if (fn.kind === 'parametric') {
      sampleParametric(fn);
    } else if (fn.kind === 'polar') {
      samplePolar(fn);
    } else if (fn.kind === 'point') {
      const x = fn.xEvaluator(0, 0, 0, 0);
      const y = fn.yEvaluator(0, 0, 0, 0);
      if (x === null || y === null) return;
      if (!passesRestrictions(fn, x, y)) return;
      minX = Math.min(minX, x);
      maxX = Math.max(maxX, x);
      minY = Math.min(minY, y);
      maxY = Math.max(maxY, y);
    } else if (fn.kind === 'vertical') {
      const step = (view.maxY - view.minY) / 400;
      for (let y = view.minY; y <= view.maxY; y += step) {
        const x = fn.xEvaluator(0, y, 0, 0);
        if (x === null) continue;
        if (!passesRestrictions(fn, x, y)) continue;
        minX = Math.min(minX, x);
        maxX = Math.max(maxX, x);
        minY = Math.min(minY, y);
        maxY = Math.max(maxY, y);
      }
    }
  });

  if (!Number.isFinite(minX) || !Number.isFinite(minY)) return;
  const padX = (maxX - minX) * 0.15 + 1;
  const padY = (maxY - minY) * 0.15 + 1;
  view.minX = minX - padX;
  view.maxX = maxX + padX;
  view.minY = minY - padY;
  view.maxY = maxY + padY;
  minXInput.value = view.minX.toFixed(2);
  maxXInput.value = view.maxX.toFixed(2);
  minYInput.value = view.minY.toFixed(2);
  maxYInput.value = view.maxY.toFixed(2);
  draw();
}

canvas.addEventListener('mousedown', (e) => {
  dragging = true;
  lastMouse = { x: e.offsetX, y: e.offsetY };
});

canvas.addEventListener('mouseup', () => {
  dragging = false;
  lastMouse = null;
});

canvas.addEventListener('mouseleave', () => {
  dragging = false;
  lastMouse = null;
});

canvas.addEventListener('mousemove', (e) => {
  const math = toMath(e.offsetX, e.offsetY);
  coordsLabel.textContent = `x: ${math.x.toFixed(2)}, y: ${math.y.toFixed(2)}`;
  if (!dragging || !lastMouse) return;

  const dx = e.offsetX - lastMouse.x;
  const dy = e.offsetY - lastMouse.y;
  const w = canvas.clientWidth;
  const h = canvas.clientHeight;
  const rangeX = view.maxX - view.minX;
  const rangeY = view.maxY - view.minY;

  view.minX -= (dx / w) * rangeX;
  view.maxX -= (dx / w) * rangeX;
  view.minY += (dy / h) * rangeY;
  view.maxY += (dy / h) * rangeY;

  minXInput.value = view.minX.toFixed(2);
  maxXInput.value = view.maxX.toFixed(2);
  minYInput.value = view.minY.toFixed(2);
  maxYInput.value = view.maxY.toFixed(2);

  lastMouse = { x: e.offsetX, y: e.offsetY };
  draw();
});

canvas.addEventListener('wheel', (e) => {
  e.preventDefault();
  const factor = e.deltaY > 0 ? 1.1 : 0.9;
  zoom(factor);
});

setStatus('Ready');
renderFunctionList();
