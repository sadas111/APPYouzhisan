import request from '@/utils/request';

export function getTutorials() {
  return request({
    url: '/tutorials',
    method: 'get'
  });
}

export function getTutorial(id) {
  return request({
    url: `/tutorials/${id}`,
    method: 'get'
  });
}

export function getTutorialsByDifficulty(difficulty) {
  return request({
    url: `/tutorials/difficulty/${difficulty}`,
    method: 'get'
  });
}
