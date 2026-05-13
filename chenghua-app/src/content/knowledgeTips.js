export const knowledgeTips = [
  {
    id: 'marriage-blessing',
    text: '在川南一带，新娘出嫁要撑油纸伞，寓意“伞开百子、子孙满堂”。',
    triggers: ['first_enter', 'save_success'],
  },
  {
    id: 'craft-steps',
    text: '一把传统油纸伞往往要经过七十多道工序，从选竹、削骨到上油、晾晒，每一步都不能省略。',
    triggers: ['first_enter', 'assemble_success'],
  },
  {
    id: 'pattern-cloud',
    text: '伞面上的祥云纹常与祝寿、婚嫁场景相配，象征吉祥如意、福气绵长。',
    triggers: ['pattern_change'],
  },
  {
    id: 'pattern-lotus',
    text: '莲花纹样原本多见于江南纸伞，寓意“荷你同路”，也有守护平安的意思。',
    triggers: ['pattern_change'],
  },
  {
    id: 'heritage-list',
    text: '泸州油纸伞制作技艺已列入国家级非物质文化遗产名录，当地匠人至今仍保留完整的“伞谱”传统。',
    triggers: ['catalog_apply', 'first_enter'],
  },
];

export function getRandomTip(trigger, excludeIds = []) {
  const excluded = new Set(excludeIds);
  const candidates = knowledgeTips.filter((tip) => {
    const matchTrigger = !tip.triggers || tip.triggers.includes(trigger);
    return matchTrigger && !excluded.has(tip.id);
  });

  if (!candidates.length) return null;

  const index = Math.floor(Math.random() * candidates.length);
  return candidates[index];
}

