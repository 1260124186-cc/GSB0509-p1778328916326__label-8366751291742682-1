<template>
  <div class="success-container">
    <el-card class="success-card">
      <el-result
        icon="success"
        title="签到成功"
        :sub-title="`已完成本次活动签到`"
      >
        <template #extra>
          <div class="detail-info">
            <p><strong>姓名：</strong>{{ name }}</p>
            <p><strong>时间：</strong>{{ formatTime(time) }}</p>
          </div>
          <el-button type="primary" class="mt-20" @click="close">关闭页面</el-button>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'

const route = useRoute()
const name = route.query.name
const time = route.query.time

const formatTime = (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm:ss') : ''

const close = () => {
  // 尝试关闭窗口，或者跳转到空白页
  window.opener = null
  window.open('about:blank', '_self')
  window.close()
  // 如果不能关闭，就显示提示
  document.body.innerHTML = '<div style="text-align:center;margin-top:50px;">您现在可以关闭此窗口了</div>'
}
</script>

<style scoped>
.success-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: #f0f9eb;
}
.success-card {
  width: 100%;
  max-width: 400px;
  border-radius: 12px;
}
.detail-info {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  text-align: left;
  margin-bottom: 20px;
}
.detail-info p {
  margin: 5px 0;
  color: #606266;
}
.mt-20 {
  width: 100%;
}
</style>
