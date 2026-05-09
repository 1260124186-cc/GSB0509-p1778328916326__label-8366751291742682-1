import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
    baseURL: '/api',
    timeout: 5000
})

service.interceptors.response.use(
    response => response.data,
    error => {
        ElMessage.error(error.response?.data?.error || '请求失败')
        return Promise.reject(error)
    }
)

export default service
