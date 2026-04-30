# Recommendation Behavior Demo Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add dynamic `user_behavior`-driven recommendation profiling, isolated recommendation demo seed data, and lightweight homepage recommendation explainability without breaking existing modules.

**Architecture:** Keep Spring Boot as the recommendation orchestrator. Add a dedicated `user_behavior` persistence model plus a `UserProfileService` that builds profile text from recent weighted behaviors and falls back to `user_preference` when needed. Seed large demo data from a standalone SQL script rather than `init.sql`.

**Tech Stack:** Spring Boot, Spring Data JPA, MySQL SQL scripts, Vue 3, Node source-based UI regression tests, existing Python semantic recommendation service.

---

### Task 1: Add failing backend tests for behavior-first profiling and behavior recording

**Files:**
- Modify: `src/test/java/com/campus/campus_system/RecommendationFeatureTests.java`
- Modify: `src/test/java/com/campus/campus_system/SearchFilterFeatureTests.java`

- [ ] Add a test that creates `user_behavior` rows for a user and proves recommendation ranking prefers behavior-derived profile text over fallback preference-only behavior.
- [ ] Add tests that call product/job/help search and detail APIs with `userId` and verify `user_behavior` rows are recorded for `SEARCH` and `VIEW`.
- [ ] Run targeted tests and confirm they fail because `user_behavior` infrastructure does not yet exist.

### Task 2: Add backend behavior model and profile service

**Files:**
- Create: `src/main/java/com/campus/campus_system/entity/UserBehavior.java`
- Create: `src/main/java/com/campus/campus_system/repository/UserBehaviorRepository.java`
- Create: `src/main/java/com/campus/campus_system/service/UserProfileService.java`
- Modify: `src/main/java/com/campus/campus_system/service/RecommendationService.java`

- [ ] Implement `UserBehavior` JPA entity with `userId`, `category`, `behaviorType`, `targetType`, `targetId`, `keyword`, `contentTitle`, `behaviorTime`, `createTime`.
- [ ] Add repository queries for recent behaviors by user and category.
- [ ] Implement `UserProfileService` to build profile text from recent behaviors first, and fall back to `user_preference` when no behavior exists.
- [ ] Update `RecommendationService` to use `UserProfileService`, preserve current response shape, and add debug logs for user ID, profile text, candidate count, AI result count, and fallback path.

### Task 3: Record search and view behavior in business APIs

**Files:**
- Modify: `src/main/java/com/campus/campus_system/controller/SecondHandProductController.java`
- Modify: `src/main/java/com/campus/campus_system/controller/JobPostingController.java`
- Modify: `src/main/java/com/campus/campus_system/controller/HelpRequestController.java`
- Modify: `src/main/java/com/campus/campus_system/service/RecommendationService.java`

- [ ] Extend list/search/detail endpoints to accept optional `userId` for behavior recording without breaking existing callers.
- [ ] Record `SEARCH` behavior when keyword searches happen and `VIEW` behavior when detail endpoints are opened.
- [ ] Reuse central service methods rather than duplicating persistence logic in each controller.

### Task 4: Add standalone demo SQL and import guidance

**Files:**
- Create: `database/demo-recommendation-data.sql`
- Create: `docs/recommendation-demo.md`

- [ ] Create idempotent SQL that creates `user_behavior` if missing.
- [ ] Seed at least 200 products, 150 jobs, and 150 help requests with realistic themed data and varied heat/freshness fields.
- [ ] Seed behavior rows for at least one Java-backend-oriented user and one exam-prep-oriented user using subqueries by username.
- [ ] Document manual import command and validation steps.

### Task 5: Add homepage recommendation explanation and development-only score visibility

**Files:**
- Modify: `frontend/src/views/Home.vue`
- Create or modify: `frontend/tests/*.mjs`

- [ ] Add explanatory copy near the recommendation heading.
- [ ] In development mode only, display `finalScore` and `semanticScore` when present on returned items.
- [ ] Keep cards functional when the backend only returns original entity fields.
- [ ] Add or update source-based frontend tests to lock the new copy and guarded score rendering behavior.

### Task 6: Verify end-to-end regression surface

**Files:**
- No new files

- [ ] Run targeted Maven tests for recommendation and search/filter features.
- [ ] Run Node source-based frontend regression tests.
- [ ] If needed, syntax-check the Python AI service file to ensure recommendation demo environment remains bootable.
