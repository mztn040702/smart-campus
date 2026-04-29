import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { join } from 'node:path'

const productSource = readFileSync(
  join(process.cwd(), 'frontend/src/views/Product.vue'),
  'utf8'
)
const profileSource = readFileSync(
  join(process.cwd(), 'frontend/src/views/Profile.vue'),
  'utf8'
)
const adminSource = readFileSync(
  join(process.cwd(), 'frontend/src/views/Admin.vue'),
  'utf8'
)
const viteConfigSource = readFileSync(
  join(process.cwd(), 'frontend/vite.config.js'),
  'utf8'
)

test('product publish flow preserves image field and preference keyword after reset', () => {
  assert.match(productSource, /const preferenceKeyword = publishForm\.value\.category/)
  assert.match(
    productSource,
    /publishForm\.value\s*=\s*\{\s*title:\s*'',\s*description:\s*'',\s*price:\s*0,\s*category:\s*'',\s*images:\s*''\s*\}/
  )
  assert.match(productSource, /keyword:\s*preferenceKeyword/)
})

test('product view exposes upload helpers and image styles', () => {
  assert.match(productSource, /imageUploading,/)
  assert.match(productSource, /resolveImageUrl,/)
  assert.match(productSource, /getProductImage,/)
  assert.match(productSource, /beforeImageUpload,/)
  assert.match(productSource, /uploadProductImage,/)
  assert.match(productSource, /\.upload-preview\s*\{/)
  assert.match(productSource, /\.detail-image\s*\{/)
})

test('profile view wires avatar upload and preview to image endpoint', () => {
  assert.match(profileSource, /:http-request="uploadAvatar"/)
  assert.match(profileSource, /:before-upload="beforeImageUpload"/)
  assert.match(profileSource, /class="avatar-preview"/)
  assert.match(profileSource, /const avatarUploading = ref\(false\)/)
  assert.match(profileSource, /const uploadAvatar = async \(\{ file, onSuccess, onError \}\) =>/)
  assert.match(profileSource, /request\.post\('\/upload\/image', formData/)
})

test('admin product table renders thumbnail images', () => {
  assert.match(adminSource, /class="product-thumb"/)
  assert.match(adminSource, /resolveImageUrl\(row\.images\)/)
  assert.match(adminSource, /function resolveImageUrl\(url\)/)
})

test('vite dev server proxies uploaded images to backend', () => {
  assert.match(viteConfigSource, /['"]\/uploads['"]\s*:/)
  assert.match(viteConfigSource, /target:\s*['"]http:\/\/localhost:8080['"]/)
})
