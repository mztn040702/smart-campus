import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'

const homeSource = readFileSync(
  join(process.cwd(), 'frontend/src/views/Home.vue'),
  'utf8'
)

test('home view explains recommendation mix and only shows scores in development mode', () => {
  assert.match(homeSource, /当前推荐基于用户行为、内容热度和语义相似度综合生成。/)
  assert.match(homeSource, /const isDevelopment = import\.meta\.env\.DEV/)
  assert.match(homeSource, /const showRecommendationScores = \(item\) =>/)
  assert.match(homeSource, /typeof item\.finalScore === 'number'/)
  assert.match(homeSource, /推荐分数：{{ formatScore\(product\.finalScore\) }}/)
  assert.match(homeSource, /语义相似度：{{ formatScore\(product\.semanticScore\) }}/)
})
