<template>
  <div class="activity-detail-container" v-loading="loading">
    <div class="page-header">
      <el-button @click="$router.push('/admin')">返回列表</el-button>
      <h2>活动详情</h2>
    </div>

    <div v-if="activity" class="content">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="mb-20">
            <template #header>
              <div class="card-header">
                <span>基本信息</span>
                <el-tag :type="getStatusType(activity.status)">
                  {{ getStatusText(activity.status) }}
                </el-tag>
              </div>
            </template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="标题">{{ activity.title }}</el-descriptions-item>
              <el-descriptions-item label="时间">
                {{ formatTime(activity.startTime) }} ~ {{ formatTime(activity.endTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="说明">{{ activity.description || '无' }}</el-descriptions-item>
              <el-descriptions-item label="签到人数">{{ checkinCount }} 人</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card>
            <template #header>
              <div class="card-header">
                <span>签到名单</span>
              </div>
            </template>
            <el-table :data="checkins" stripe style="width: 100%" height="400">
              <el-table-column type="index" label="序号" width="60" />
              <el-table-column prop="name" label="姓名" />
              <el-table-column prop="checkinTime" label="签到时间">
                <template #default="{ row }">
                  {{ formatTime(row.checkinTime) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card v-if="activity.status === 'PUBLISHED'" class="qrcode-card">
            <template #header>
              <span>签到二维码</span>
            </template>
            <div class="qrcode-wrapper">
              <img :src="qrCodeUrl" v-if="qrCodeUrl" class="qrcode-img" />
              <p class="mt-10">扫码签到</p>
              <el-input v-model="checkinLink" readonly size="small">
                <template #append>
                  <el-button @click="copyLink">复制</el-button>
                </template>
              </el-input>
              <el-alert
                title="扫码提示"
                type="warning"
                :closable="false"
                class="mt-10"
                show-icon
              >
                <template #default>
                  请确保手机和电脑在同一局域网，或项目已发布到公网，否则手机可能无法访问。
                </template>
              </el-alert>
            </div>
          </el-card>
          
          <el-card v-else class="qrcode-card">
            <el-result
              icon="info"
              title="未发布"
              sub-title="发布活动后可生成签到二维码"
            >
              <template #extra>
                <el-button 
                  v-if="activity.status === 'DRAFT'"
                  type="primary" 
                  @click="handlePublish"
                >
                  立即发布
                </el-button>
              </template>
            </el-result>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getActivity, getCheckins, publishActivity } from '../../api/activity'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'
import dayjs from 'dayjs'

const route = useRoute()
const activity = ref(null)
const checkins = ref([])
const checkinCount = ref(0)
const loading = ref(false)
const qrCodeUrl = ref('')

const activityId = route.params.id

const checkinLink = computed(() => {
  if (!activity.value?.checkinToken) return ''
  return `${window.location.origin}/checkin/${activity.value.checkinToken}`
})

const getStatusType = (status) => {
  const map = { DRAFT: 'info', PUBLISHED: 'success', CLOSED: 'danger' }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = { DRAFT: '草稿', PUBLISHED: '已发布', CLOSED: '已关闭' }
  return map[status] || status
}

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm:ss')

const fetchData = async () => {
  loading.value = true
  try {
    const data = await getActivity(activityId)
    activity.value = data
    
    // 如果已发布，生成二维码
    if (data.status === 'PUBLISHED' && data.checkinToken) {
      await generateQRCode()
    }

    // 获取签到名单
    const checkinData = await getCheckins(activityId)
    checkins.value = checkinData.checkins
    checkinCount.value = checkinData.totalCount
  } finally {
    loading.value = false
  }
}

const generateQRCode = async () => {
  try {
    qrCodeUrl.value = await QRCode.toDataURL(checkinLink.value, { width: 250, margin: 2 })
  } catch (err) {
    console.error(err)
  }
}

const handlePublish = async () => {
  try {
    await publishActivity(activityId)
    ElMessage.success('发布成功')
    fetchData()
  } catch (error) {}
}



const copyLink = () => {
  navigator.clipboard.writeText(checkinLink.value).then(() => {
    ElMessage.success('链接已复制')
  })
}

onMounted(fetchData)
</script>

<style scoped>
.activity-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mb-20 {
  margin-bottom: 20px;
}
.qrcode-wrapper {
  text-align: center;
}
.qrcode-img {
  width: 200px;
  height: 200px;
}
.mt-10 {
  margin-top: 10px;
  margin-bottom: 10px;
}
</style>
