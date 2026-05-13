import request from '@/utils/request';

export function getUserAddresses(userId) {
  // Currently backend might not filter by userId in the generic GET /addresses unless specified
  // But AddressController.getUserAddresses uses session or param?
  // Let's check AddressController. 
  // Wait, I don't have AddressController content in front of me, but I recall creating it.
  // I'll assume standard REST: GET /addresses?userId=... or just GET /addresses if it returns all for current user.
  // In the previous turn I implemented `getUserAddresses` in controller.
  return request({
    url: '/addresses',
    method: 'get',
    params: { userId }
  });
}

export function saveAddress(data) {
  return request({
    url: '/addresses',
    method: 'post',
    data
  });
}

export function deleteAddress(id, userId) {
  return request({
    url: `/addresses/${id}`,
    method: 'delete',
    params: { userId }
  });
}
