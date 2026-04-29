# Frontend Auth Navigation Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the Vue front-end so login lands on `/home`, authenticated users can access `/product`, `/job`, `/help`, and `/chat`, and refresh preserves access without changing backend JWT logic.

**Architecture:** Introduce a tiny shared session helper for reading and updating `localStorage` auth state, and a small route-access helper that the Vue router guard can reuse. Keep the existing business pages intact, update the global app shell to react to auth changes, and add explicit navigation entry points on the home page.

**Tech Stack:** Vue 3, Vue Router 4, Vite, Element Plus, Node built-in test runner

---

### Task 1: Add session and route-access helpers with regression tests

**Files:**
- Create: `frontend/src/utils/auth.mjs`
- Create: `frontend/src/router/access.mjs`
- Create: `frontend/tests/auth-navigation.test.mjs`

- [ ] **Step 1: Write the failing test**

```javascript
import test from 'node:test'
import assert from 'node:assert/strict'
import { createMemoryStorage, getStoredSession, hasValidSession, setStoredSession, clearStoredSession } from '../src/utils/auth.mjs'
import { canAccessRoute, PUBLIC_PATHS } from '../src/router/access.mjs'

test('stored session becomes valid after saving token and user', () => {
  const storage = createMemoryStorage()

  assert.equal(hasValidSession(storage), false)

  setStoredSession(storage, { id: 7, username: 'alice' }, 'jwt-token')

  assert.deepEqual(getStoredSession(storage), {
    user: { id: 7, username: 'alice' },
    token: 'jwt-token'
  })
  assert.equal(hasValidSession(storage), true)
})

test('clearing storage removes auth state', () => {
  const storage = createMemoryStorage()
  setStoredSession(storage, { id: 7, username: 'alice' }, 'jwt-token')

  clearStoredSession(storage)

  assert.equal(hasValidSession(storage), false)
  assert.deepEqual(getStoredSession(storage), {
    user: {},
    token: ''
  })
})

test('route access allows public pages and blocks protected pages without auth', () => {
  assert.equal(PUBLIC_PATHS.includes('/login'), true)
  assert.equal(canAccessRoute('/login', false), true)
  assert.equal(canAccessRoute('/register', false), true)
  assert.equal(canAccessRoute('/home', false), false)
  assert.equal(canAccessRoute('/product', false), false)
})

test('route access allows business pages with auth', () => {
  assert.equal(canAccessRoute('/home', true), true)
  assert.equal(canAccessRoute('/product', true), true)
  assert.equal(canAccessRoute('/job', true), true)
  assert.equal(canAccessRoute('/help', true), true)
  assert.equal(canAccessRoute('/chat', true), true)
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node --test frontend/tests/auth-navigation.test.mjs`
Expected: FAIL because `frontend/src/utils/auth.mjs` and `frontend/src/router/access.mjs` do not exist yet

- [ ] **Step 3: Write minimal implementation**

```javascript
// frontend/src/utils/auth.mjs
import { reactive } from 'vue'

const USER_KEY = 'user'
const TOKEN_KEY = 'token'

function safeParseUser(rawUser) {
  if (!rawUser) return {}
  try {
    return JSON.parse(rawUser) || {}
  } catch {
    return {}
  }
}

export function getStoredSession(storage = localStorage) {
  return {
    user: safeParseUser(storage.getItem(USER_KEY)),
    token: storage.getItem(TOKEN_KEY) || ''
  }
}

export function hasValidSession(storage = localStorage) {
  const session = getStoredSession(storage)
  return Boolean(session.user?.id && session.token)
}

export function setStoredSession(storage, user, token) {
  storage.setItem(USER_KEY, JSON.stringify(user))
  storage.setItem(TOKEN_KEY, token)
}

export function clearStoredSession(storage = localStorage) {
  storage.removeItem(USER_KEY)
  storage.removeItem(TOKEN_KEY)
}

export function createMemoryStorage() {
  const values = new Map()
  return {
    getItem(key) {
      return values.has(key) ? values.get(key) : null
    },
    setItem(key, value) {
      values.set(key, String(value))
    },
    removeItem(key) {
      values.delete(key)
    }
  }
}

const sessionState = reactive(getStoredSession())

export function useSessionState() {
  return sessionState
}

export function syncSessionState(storage = localStorage) {
  const next = getStoredSession(storage)
  sessionState.user = next.user
  sessionState.token = next.token
}

export function saveSession(user, token, storage = localStorage) {
  setStoredSession(storage, user, token)
  syncSessionState(storage)
}

export function clearSession(storage = localStorage) {
  clearStoredSession(storage)
  syncSessionState(storage)
}
```

```javascript
// frontend/src/router/access.mjs
export const PUBLIC_PATHS = ['/login', '/register']

export function canAccessRoute(path, isAuthenticated) {
  if (PUBLIC_PATHS.includes(path)) {
    return true
  }
  return isAuthenticated
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `node --test frontend/tests/auth-navigation.test.mjs`
Expected: PASS

### Task 2: Wire router guard and app shell to shared session state

**Files:**
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/App.vue`
- Modify: `frontend/src/views/Login.vue`
- Modify: `frontend/src/utils/axios.js`

- [ ] **Step 1: Write the failing test**

Reuse `frontend/tests/auth-navigation.test.mjs` by extending it with:

```javascript
test('route access helper reflects the router guard policy', () => {
  assert.equal(canAccessRoute('/chat', false), false)
  assert.equal(canAccessRoute('/chat', true), true)
  assert.equal(canAccessRoute('/register', true), true)
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node --test frontend/tests/auth-navigation.test.mjs`
Expected: FAIL until the helper exports are connected and the assertions are added

- [ ] **Step 3: Write minimal implementation**

```javascript
// frontend/src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import Chat from '../views/Chat.vue'
import Product from '../views/Product.vue'
import Job from '../views/Job.vue'
import Help from '../views/Help.vue'
import { hasValidSession } from '../utils/auth.mjs'
import { PUBLIC_PATHS, canAccessRoute } from './access.mjs'

const routes = [
  { path: '/login', name: 'Login', component: Login, meta: { public: true } },
  { path: '/register', name: 'Register', component: Register, meta: { public: true } },
  { path: '/home', name: 'Home', component: Home },
  { path: '/chat', name: 'Chat', component: Chat },
  { path: '/product', name: 'Product', component: Product },
  { path: '/job', name: 'Job', component: Job },
  { path: '/help', name: 'Help', component: Help },
  { path: '/', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const authenticated = hasValidSession()
  if (!canAccessRoute(to.path, authenticated)) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
export { PUBLIC_PATHS }
```

```javascript
// frontend/src/App.vue
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { PUBLIC_PATHS } from './router/access.mjs'
import { clearSession, syncSessionState, useSessionState } from './utils/auth.mjs'

const session = useSessionState()
const isAuthPage = computed(() => PUBLIC_PATHS.includes(route.path))
const isLoggedIn = computed(() => Boolean(session.user?.id && session.token))
const showShell = computed(() => isLoggedIn.value && !isAuthPage.value)
```

```javascript
// frontend/src/views/Login.vue
import { saveSession } from '../utils/auth.mjs'

if (res.code === 0) {
  saveSession(res.data, res.token)
  router.push('/home')
}
```

```javascript
// frontend/src/utils/axios.js
import { clearSession, getStoredSession } from './auth.mjs'

const { token } = getStoredSession()
if (token) {
  config.headers.Authorization = `Bearer ${token}`
}

if (error.response?.status === 401) {
  clearSession()
  if (window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `node --test frontend/tests/auth-navigation.test.mjs`
Expected: PASS

### Task 3: Add clear home-page navigation entry points

**Files:**
- Modify: `frontend/src/views/Home.vue`

- [ ] **Step 1: Write the failing test**

Document the required visible destinations in a simple assertion-driven test:

```javascript
test('business routes remain protected destinations after login', () => {
  const destinations = ['/product', '/job', '/help', '/chat']
  for (const path of destinations) {
    assert.equal(canAccessRoute(path, true), true)
  }
})
```

- [ ] **Step 2: Run test to verify it fails**

Run: `node --test frontend/tests/auth-navigation.test.mjs`
Expected: FAIL until the test is added and the helper import is wired

- [ ] **Step 3: Write minimal implementation**

Update `frontend/src/views/Home.vue` to:

```javascript
const quickLinks = [
  { title: '商品交易', description: '查看和发布二手商品', path: '/product', type: 'danger' },
  { title: '招聘信息', description: '浏览兼职、全职和实习岗位', path: '/job', type: 'primary' },
  { title: '互助大厅', description: '发布求助或接受帮助任务', path: '/help', type: 'success' },
  { title: '聊天消息', description: '联系卖家、招聘方或求助者', path: '/chat', type: 'warning' }
]

const goTo = (path) => {
  router.push(path)
}
```

Render those links above the existing recommendation tabs with Element Plus cards or buttons, keeping the recommendation section intact.

- [ ] **Step 4: Run test to verify it passes**

Run: `node --test frontend/tests/auth-navigation.test.mjs`
Expected: PASS

### Task 4: Verify the front-end build

**Files:**
- Modify: `frontend/package.json` only if a verification script is needed

- [ ] **Step 1: Run the regression test suite**

Run: `node --test frontend/tests/auth-navigation.test.mjs`
Expected: PASS

- [ ] **Step 2: Run the Vite production build**

Run: `npm.cmd run build`
Expected: Vite build completes successfully and emits `dist/`

- [ ] **Step 3: Review changed files**

Run: `git status --short`
Expected: Only the intended front-end files and plan doc are modified or created
