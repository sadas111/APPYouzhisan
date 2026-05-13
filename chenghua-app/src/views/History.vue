<template>
  <div class="page-container">
    <!-- <div class="header-fixed">
      <h2 class="ancient-font text-center">伞韵溯源</h2>
    </div> -->
	<van-nav-bar
	  left-arrow 
	  @click-left="$router.back()" 
	  fixed 
	  placeholder
	  :border="false"
	  class="custom-nav"
	>
	  <template #title>
	    <span class="ancient-font nav-title">伞韵溯源</span>
	  </template>
	</van-nav-bar>
    
    <div class="header-banner">
      <img src="/images/1.jpg" alt="伞韵溯源" class="banner-img" />
    </div>
    
    <div class="timeline-container">
      <div class="timeline-line"></div>
      
      <div class="timeline-item" v-for="(item, index) in historyItems" :key="index" :class="{ 'right': index % 2 !== 0 }">
        <div class="timeline-dot"></div>
        <div class="timeline-content">
          <div class="period ancient-font">{{ item.period }}</div>
          <h3 class="title ancient-font">{{ item.title }}</h3>
          <p class="desc">{{ item.content }}</p>
          <div class="image-placeholder" v-if="item.imageUrl || item.image_url">
             <img :src="item.imageUrl || item.image_url" :alt="item.title" class="history-img" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const historyItems = ref([]);
const loading = ref(false);

const fetchHistory = async () => {
  loading.value = true;
  // Use static data matching the user's screenshot
  historyItems.value = [
    {
      period: '',
      title: '一、溯源：千年匠艺的起源与流变',
      content: '油纸伞的历史可追溯至东汉时期，相传由蔡伦弟子左伯改良造纸术后，结合传统制伞工艺创制而成。早期油纸伞以竹为骨、以纸为面，涂桐油防水，因轻便耐用、成本低廉，迅速成为民间出行必备工具。至唐宋时期，油纸伞制作工艺日臻成熟，不仅在民间广泛普及，更成为文人墨客吟咏的对象，如戴望舒笔下的《雨巷》，便是油纸伞文学意象的经典体现。',
      imageUrl: null
    },
    {
      period: '',
      title: '二、文化：藏于伞骨伞面的东方智慧',
      content: '油纸伞的文化内涵深植于中国人的生活哲学与审美情趣：\n符号寓意：“伞” 与 “散” 谐音相反，象征 “团圆聚合”，传统婚礼中 “打红伞” 的习俗，既寓意新人百年好合，也暗含对家族兴旺的期盼；伞面圆形对应 “天圆地方” 的宇宙观，竹骨挺拔象征君子气节，桐油防腐则寄托 “坚韧长久” 的生活愿景。\n工艺美学：一把油纸伞需经 “选竹、制骨、裱纸、上油、绘花” 等80 余道纯手工工序，从竹材砍伐到伞面绘制均依赖匠人经验。伞面图案兼具实用性与艺术性，除常见的花鸟、山水、吉祥纹样外，部分流派还融入地方戏曲、民间故事，使油纸伞成为 “可移动的民间艺术品”。\n跨域影响：油纸伞不仅是中国非遗，更通过丝绸之路、海上贸易传入日本、朝鲜、东南亚等地，成为东亚文化交流的载体。日本的 “和伞”、韩国的 “纸伞” 均借鉴了中国油纸伞的工艺，其伞面设计、使用场景仍保留着东方文化的共通性。',
      imageUrl: null
    },
    {
      period: '',
      title: '三、现状：传统匠艺的传承困境与保护价值',
      content: '随着现代工业的发展，塑料伞、折叠伞以其便捷性逐渐取代油纸伞的日常使用功能，传统油纸伞面临 “匠人老龄化、技艺断层、市场萎缩” 的三重困境：目前全国掌握完整油纸伞制作技艺的匠人不足百人，部分流派因年轻人不愿意学艺而濒临失传。然而，油纸伞的文化价值与工艺价值并未褪色 —— 其 “竹骨纸面、桐油防腐” 的传统工艺蕴含着古人的生态智慧，80 余道手工工序是中国传统手工业的活态见证，伞面的吉祥纹样、绘画艺术则承载着民间文化的集体记忆。保护油纸伞，不仅是守护一项传统技艺，更是传承东方美学、生态智慧与民间文化的重要举措，这也为 “非遗数字化” 提供了核心的保护诉求与价值基础。',
      imageUrl: null
    }
  ];
  loading.value = false;
};

onMounted(() => {
  fetchHistory();
});
</script>

<style scoped>
.page-container {
  padding-bottom: 80px;
  background: transparent;
  min-height: 100vh;
  position: relative;
}

.header-fixed, .header-banner, .timeline-container {
  position: relative;
  z-index: 1;
}

.header-fixed {
  position: sticky;
  top: 0;
  background: rgba(185, 222, 201, 0.4);
  padding: 10px 0;
  z-index: 10;
  box-shadow: 0 2px 10px rgba(74, 106, 111, 0.05);
}
.custom-nav {
  --van-nav-bar-background: rgba(185, 222, 201, 0.4);
  --van-nav-bar-title-text-color: #4A6A6F;
  --van-nav-bar-icon-color: #4A6A6F;
}

.text-center {
  text-align: center;
  color: #4A6A6F;
  margin: 0;
}

.header-banner {
  margin: 15px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(74, 106, 111, 0.1);
}

.banner-img {
  width: 100%;
  height: auto;
  display: block;
}

.timeline-container {
  padding: 20px;
  position: relative;
}

.timeline-line {
  position: absolute;
  left: 30px;
  top: 20px;
  bottom: 20px;
  width: 2px;
  background: var(--color-primary);
  opacity: 0.3;
}

.timeline-item {
  margin-bottom: 30px;
  padding-left: 40px;
  position: relative;
}

.timeline-dot {
  position: absolute;
  left: 24px;
  top: 5px;
  width: 14px;
  height: 14px;
  background: var(--color-secondary);
  border-radius: 50%;
  border: 2px solid #fff;
  z-index: 2;
}

.timeline-content {
  background: rgba(255, 255, 255, 0.8);
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border-left: 3px solid var(--color-secondary);
}

.period {
  font-size: 0.9rem;
  color: var(--color-primary);
  font-weight: bold;
  margin-bottom: 5px;
}

.title {
  margin: 0 0 10px 0;
  font-size: 1.1rem;
  color: #333;
}

.desc {
  font-size: 0.85rem;
  color: #666;
  line-height: 1.6;
  margin: 0;
  text-align: justify;
  white-space: pre-wrap;
}

.image-placeholder {
  margin-top: 10px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.history-img {
  width: 100%;
  height: auto;
  display: block;
}

.loading-more {
  text-align: center;
  padding: 20px 0;
}
</style>
