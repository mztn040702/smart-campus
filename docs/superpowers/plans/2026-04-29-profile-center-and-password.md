# Profile Center And Password Change Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a profile center where authenticated users can view and edit their own profile and change their password using old-password BCrypt verification.

**Architecture:** Extend the existing `User` entity with profile fields, expose JWT-bound `/api/user/profile` and `/api/user/password` endpoints, and add a frontend `/profile` view wired into the existing shell navigation. Password changes remain server-side only and force a frontend logout after success.

**Tech Stack:** Spring Boot, Spring Data JPA, BCrypt, Vue 3, Vue Router, Element Plus, Node test runner, JUnit + MockMvc

---

### Task 1: Backend profile and password tests

**Files:**
- Create: `src/test/java/com/campus/campus_system/UserProfileFeatureTests.java`
- Modify: `src/test/java/com/campus/campus_system/CampusSystemApplicationTests.java`

- [ ] Add failing MockMvc tests for `/api/user/profile` get, profile update without userId in payload, and password change with BCrypt old-password verification.
- [ ] Run targeted Maven tests and confirm they fail because the endpoints or fields do not exist yet.

### Task 2: Backend user profile implementation

**Files:**
- Modify: `src/main/java/com/campus/campus_system/entity/User.java`
- Modify: `src/main/java/com/campus/campus_system/service/UserService.java`
- Modify: `src/main/java/com/campus/campus_system/controller/UserController.java`
- Modify: `database/init.sql`

- [ ] Add profile fields to `User`: `nickname`, `email`, `phone`, `bio`.
- [ ] Add service methods for current-user profile read/update and password change using authenticated user id only.
- [ ] Add controller endpoints `GET /api/user/profile`, `PUT /api/user/profile`, `PUT /api/user/password` using `currentUserId` from JWT request attributes.
- [ ] Ensure profile updates cannot modify `username`, `role`, `status`, or arbitrary user ids.
- [ ] Ensure password change validates old password with BCrypt and saves new password with BCrypt.

### Task 3: Frontend profile center UI

**Files:**
- Create: `frontend/src/views/Profile.vue`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/App.vue`
- Modify: `frontend/src/utils/auth.mjs`
- Modify: `frontend/tests/auth-navigation.test.mjs`

- [ ] Add a protected `/profile` route.
- [ ] Add a profile navigation item visible to authenticated users, including admins.
- [ ] Implement profile form for nickname, email, phone, bio, avatar URL, and read-only account fields.
- [ ] Implement password form with old/new password fields and forced logout after success.
- [ ] Add or update route tests to cover `/profile` access.

### Task 4: Verification

**Files:**
- None

- [ ] Run targeted backend tests for profile/password flows.
- [ ] Run frontend Node route tests.
- [ ] Run full Maven test suite.
- [ ] Run frontend production build.
