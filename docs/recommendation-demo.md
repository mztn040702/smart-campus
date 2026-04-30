# Recommendation Demo Data

## Import

Run the demo seed script after the base database is ready:

```bash
mysql -u root -p campus_system < database/demo-recommendation-data.sql
```

## What It Seeds

- `user_behavior` table for dynamic recommendation profiling
- at least 200 `second_hand_product` rows
- at least 150 `job_posting` rows
- at least 150 `help_request` rows
- behavior records for:
  - `student1`: Java backend / Spring Boot / MySQL direction
  - `student2`: exam-prep / graduate entrance exam direction

## Safety Notes

- This script does not modify `database/init.sql`.
- It only clears and rebuilds demo content owned by:
  - `demo_reco_seller`
  - `demo_reco_recruiter`
  - `demo_reco_helper`
- It also resets `user_behavior` records for demo users `student1` and `student2` so repeated imports keep recommendation results stable for demos.

## Suggested Demo Flow

1. Import `database/demo-recommendation-data.sql`.
2. Start the Spring Boot backend.
3. Start the Python semantic recommendation service.
4. Log in as `student1` and open the homepage.
5. Verify recommendations lean toward:
   - Java backend internships
   - Spring Boot content
   - MySQL learning materials
6. Log in as `student2` and verify recommendations lean toward:
   - exam-prep books
   - graduate entrance exam materials
   - 408 / politics / English prep content

## Verification Tips

- Check `/api/recommend/products/{userId}`, `/api/recommend/jobs/{userId}`, `/api/recommend/helps/{userId}` in browser devtools.
- In development mode, the homepage shows `finalScore` and `semanticScore` when the backend returns them.
- Backend logs now print:
  - user ID
  - profile text
  - candidate count
  - AI score count
  - fallback usage
