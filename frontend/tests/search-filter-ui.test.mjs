import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'

const productSource = readFileSync(
  join(process.cwd(), 'frontend/src/views/Product.vue'),
  'utf8'
)
const jobSource = readFileSync(
  join(process.cwd(), 'frontend/src/views/Job.vue'),
  'utf8'
)
const helpSource = readFileSync(
  join(process.cwd(), 'frontend/src/views/Help.vue'),
  'utf8'
)

test('product view exposes combined search filters and reset flow', () => {
  assert.match(productSource, /const filters = ref\(\{/)
  assert.match(productSource, /keyword:\s*''/)
  assert.match(productSource, /minPrice:\s*null/)
  assert.match(productSource, /maxPrice:\s*null/)
  assert.match(productSource, /sort:\s*'latest'/)
  assert.match(productSource, /axios\.get\('\/product\/list',\s*\{\s*params:\s*buildProductQueryParams\(\)/)
  assert.match(productSource, /const resetFilters = \(\) =>/)
  assert.match(productSource, /<el-button @click="resetFilters">/)
  assert.match(productSource, /const productCategories = \[/)
  assert.match(productSource, /label:\s*'(?:书籍|涔︾睄)',\s*value:\s*'books'/)
  assert.match(productSource, /label:\s*'(?:电子产品|鐢靛瓙浜у搧)',\s*value:\s*'electronics'/)
  assert.match(productSource, /label:\s*'(?:生活用品|鐢熸椿鐢ㄥ搧)',\s*value:\s*'daily'/)
  assert.match(productSource, /const getCategoryLabel = \(category\) =>/)
  assert.match(productSource, /<el-tag size="small">{{ getCategoryLabel\(product\.category\) }}<\/el-tag>/)
  assert.match(productSource, /if \(currentUser\.id\) \{\s*params\.userId = currentUser\.id/s)
  assert.match(productSource, /axios\.get\(`\/product\/\$\{product\.id\}`,\s*\{\s*params:\s*\{\s*userId:\s*currentUser\.id/s)
})

test('job view exposes location salary filters and unified list query', () => {
  assert.match(jobSource, /const filters = ref\(\{/)
  assert.match(jobSource, /location:\s*''/)
  assert.match(jobSource, /minSalary:\s*null/)
  assert.match(jobSource, /maxSalary:\s*null/)
  assert.match(jobSource, /sort:\s*'latest'/)
  assert.match(jobSource, /axios\.get\('\/job\/list',\s*\{\s*params:\s*buildJobQueryParams\(\)/)
  assert.match(jobSource, /const resetFilters = \(\) =>/)
  assert.match(jobSource, /if \(currentUser\.id\) \{\s*params\.userId = currentUser\.id/s)
  assert.match(jobSource, /axios\.get\(`\/job\/\$\{job\.id\}`,\s*\{\s*params:\s*\{\s*userId:\s*currentUser\.id/s)
})

test('help view exposes urgency filter and unified list query', () => {
  assert.match(helpSource, /const filters = ref\(\{/)
  assert.match(helpSource, /urgency:\s*''/)
  assert.match(helpSource, /sort:\s*'latest'/)
  assert.match(helpSource, /axios\.get\('\/help\/list',\s*\{\s*params:\s*buildHelpQueryParams\(\)/)
  assert.match(helpSource, /const resetFilters = \(\) =>/)
  assert.match(helpSource, /if \(currentUser\.id\) \{\s*params\.userId = currentUser\.id/s)
  assert.match(helpSource, /axios\.get\(`\/help\/\$\{help\.id\}`,\s*\{\s*params:\s*\{\s*userId:\s*currentUser\.id/s)
})
