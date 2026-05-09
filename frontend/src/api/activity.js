import request from './request'

// 管理端接口
export const createActivity = (data) => request.post('/admin/activities', data)
export const getActivities = () => request.get('/admin/activities')
export const getActivity = (id) => request.get(`/admin/activities/${id}`)
export const publishActivity = (id) => request.post(`/admin/activities/${id}/publish`)
export const closeActivity = (id) => request.post(`/admin/activities/${id}/close`)
export const getCheckins = (id) => request.get(`/admin/activities/${id}/checkins`)


// 参与者接口
export const getActivityByToken = (token) => request.get(`/public/activities/${token}`)
export const checkin = (token, data) => request.post(`/public/activities/${token}/checkin`, data)
