<template>
  <div class="activity-list-container">
    <div class="header">
      <h2>活动管理</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        创建活动
      </el-button>
    </div>

    <el-table :data="activities" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="活动标题" min-width="150" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="300">
        <template #default="{ row }">
          {{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="viewDetail(row.id)">详情</el-button>
          
          <el-button 
            v-if="row.status === 'DRAFT'"
            link 
            type="success" 
            @click="handlePublish(row)"
          >
            发布
          </el-button>
          
          <el-button 
            v-if="row.status === 'PUBLISHED'" 
            link 
            type="danger" 
            @click="handleClose(row)"
          >
            关闭
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <create-activity-form 
      v-model="showCreateDialog" 
      @success="fetchActivities" 
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getActivities, publishActivity, closeActivity } from '../../api/activity'
import CreateActivityForm from '../../components/CreateActivityForm.vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const activities = ref([])
const loading = ref(false)
const showCreateDialog = ref(false)

const fetchActivities = async () => {
  loading.value = true
  try {
    activities.value = await getActivities()
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const map = {
    DRAFT: 'info',
    PUBLISHED: 'success',
    CLOSED: 'danger'
  }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    CLOSED: '已关闭'
  }
  return map[status] || status
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const viewDetail = (id) => {
  router.push(`/admin/activity/${id}`)
}

const handlePublish = async (row) => {
  try {
    await publishActivity(row.id)
    ElMessage.success('发布成功')
    fetchActivities()
  } catch (error) {}
}

const handleClose = (row) => {
  ElMessageBox.confirm(
    '确定要关闭该活动吗？关闭后将无法继续签到。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    await closeActivity(row.id)
    ElMessage.success('活动已关闭')
    fetchActivities()
  })
}

onMounted(fetchActivities)
</script>

<style scoped>
.activity-list-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
