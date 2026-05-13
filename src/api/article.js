import request from '@/utils/request';

export function getArticles() {
  return request({
    url: '/articles',
    method: 'get'
  });
}

export function getArticle(id) {
  return request({
    url: `/articles/${id}`,
    method: 'get'
  });
}

export function getArticlesByType(type) {
  return request({
    url: `/articles/type/${type}`,
    method: 'get'
  });
}
