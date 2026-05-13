import request from '@/utils/request';

export function createOrder(data) {
  return request({
    url: '/orders',
    method: 'post',
    data
  });
}

export function getUserOrders(userId) {
  return request({
    url: '/orders',
    method: 'get',
    params: { userId }
  });
}
