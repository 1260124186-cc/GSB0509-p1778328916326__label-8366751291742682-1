<template>
  <div class="checkin-container">
    <el-card class="checkin-card" v-loading="loading">
      <div v-if="activity">
        <h2 class="title">{{ activity.title }}</h2>
        <div class="info">
          <p><el-icon><Calendar /></el-icon> {{ formatTime(activity.startTime) }}</p>
          <p v-if="activity.description">{{ activity.description }}</p>
        </div>

        <el-divider />

        <div v-if="activity.status === 'PUBLISHED'">
          <el-form :model="form" @submit.prevent="handleSubmit">
            <el-form-item>
              <el-input 
                v-model="form.name" 
                placeholder="请输入您的姓名" 
                size="large"
                prefix-icon="User"
              />
            </el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              class="submit-btn" 
              @click="handleSubmit"
              :loading="submitting"
            >
              签到打卡
            </el-button>
          </el-form>
        </div>

        <el-result v-else icon="warning" title="无法签到" sub-title="活动未发布或已结束">
        </el-result>
      </div>
      
      <el-skeleton v-else :rows="5" animated />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getActivityByToken, checkin } from '../../api/activity'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { Calendar, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const token = route.params.token

const activity = ref(null)
const loading = ref(true)
const submitting = ref(false)
const form = reactive({ name: '' })

const formatTime = (time) => dayjs(time).format('YYYY年MM月DD日 HH:mm')

onMounted(async () => {
  try {
    activity.value = await getActivityByToken(token)
  } catch (error) {
    // Error handled by interceptor
  } finally {
    loading.value = false
  }
})

const handleSubmit = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('请输入姓名')
    return
  }

  submitting.value = true
  try {
    const res = await checkin(token, { name: form.name })
    router.push({
      path: '/checkin/success',
      query: {
        name: res.name,
        time: res.checkinTime
      }
    })
  } catch (error) {
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.checkin-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.checkin-card {
  width: 100%;
  max-width: 400px;
  border-radius: 12px;
}
.title {
  text-align: center;
  color: #333;
  margin-bottom: 20px;
}
.info {
  color: #666;
  font-size: 14px;
  margin-bottom: 20px;
}
.info p {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 5px 0;
}
.submit-btn {
  width: 100%;
  margin-top: 10px;
}
</style>
