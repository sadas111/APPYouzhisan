package com.chenghua.common;

import com.chenghua.entity.HistoryArticle;
import com.chenghua.entity.Post;
import com.chenghua.entity.Product;
import com.chenghua.entity.User;
import com.chenghua.repository.HistoryArticleRepository;
import com.chenghua.repository.PostRepository;
import com.chenghua.repository.ProductRepository;
import com.chenghua.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final HistoryArticleRepository historyArticleRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            initUsers();
        }
        if (productRepository.count() == 0) {
            initProducts();
        }
        if (historyArticleRepository.count() == 0) {
            initHistoryArticles();
        }
        if (postRepository.count() == 0) {
            initPosts();
        }
    }

    private void initUsers() {
        userRepository.save(User.builder()
                .username("13333333333")
                .password("123456") // In real app, use BCrypt
                .nickname("非遗传承人")
                .role("USER")
                .avatar("/images/16.jpg")
                .build());
    }

    private void initPosts() {
        for (int i = 1; i <= 6; i++) {
            String extension = (i == 1 || i == 2) ? ".png" : ".jpg";
            postRepository.save(Post.builder()
                    .userId((long) i)
                    .username("伞友" + i)
                    .userAvatar("/images/16.jpg")
                    .content("今天完成了一把" + (i % 2 == 0 ? "水墨" : "彩绘") + "油纸伞，大家觉得怎么样？ #非遗传承")
                    .imageUrl("/images/" + (9 + i) + extension)
                    .likeCount(10 + i * 5)
                    .build());
        }
    }

    private void initProducts() {
        productRepository.save(Product.builder()
                .name("梅兰竹菊·四君子")
                .description("纯手工绘制，桐油防水")
                .price(new BigDecimal("299.00"))
                .category("Classic")
                .stock(100)
                .imageUrl("/images/4.jpg")
                .artisan("毕六福")
                .build());
        
        productRepository.save(Product.builder()
                .name("青花瓷韵")
                .description("蓝白相间，古朴典雅")
                .price(new BigDecimal("268.00"))
                .category("Classic")
                .stock(50)
                .imageUrl("/images/5.jpg")
                .artisan("许玲")
                .build());

        productRepository.save(Product.builder()
                .name("粉黛佳人")
                .description("少女情怀，桃花灼灼")
                .price(new BigDecimal("188.00"))
                .category("Modern")
                .stock(200)
                .imageUrl("/images/6.jpg")
                .artisan("余万伦")
                .build());

        productRepository.save(Product.builder()
                .name("水墨山水")
                .description("大师手笔，意境深远")
                .price(new BigDecimal("399.00"))
                .category("Master")
                .stock(10)
                .imageUrl("/images/7.jpg")
                .artisan("闻士善")
                .build());

        productRepository.save(Product.builder()
                .name("锦绣河山")
                .description("色彩艳丽，喜庆吉祥")
                .price(new BigDecimal("328.00"))
                .category("Classic")
                .stock(80)
                .imageUrl("/images/8.jpg")
                .artisan("张朝全")
                .build());

        productRepository.save(Product.builder()
                .name("素雅清风")
                .description("简约大方，日常百搭")
                .price(new BigDecimal("158.00"))
                .category("Modern")
                .stock(150)
                .imageUrl("/images/9.jpg")
                .artisan("曹正新")
                .build());
    }

    private void initHistoryArticles() {
        historyArticleRepository.save(HistoryArticle.builder()
                .title("一、溯源：千年匠艺的起源与流变")
                .content("油纸伞的历史可追溯至东汉时期，相传由蔡伦弟子左伯改良造纸术後，结合传统制伞工艺创制而成。早期油纸伞以竹为骨、以纸为面，涂桐油防水，因轻便耐用、成本低廉，迅速成为民间出行必备工具。至唐宋时期，油纸伞工艺趋于成熟，不仅成为日常生活用品，更融入礼仪文化 —— 婚嫁时以红色油纸伞遮轿，寓意 “圆满喜庆”“辟邪祈福”；文人雅士则将其作为诗词绘画的载体，伞面绘山水、题墨宝，成为兼具实用与审美价值的文化符号。明清以降，油纸伞制作形成地域流派，四川泸州、浙江余杭、福建连城等地的油纸伞各具特色，其中泸州油纸伞因 “工艺最复杂、寿命最长”，被誉为 “中国油纸伞之乡”，其制作技艺被列入国家级非物质文化遗产名录。")
                .author("非遗研究中心")
                .type("ORIGIN")
                .imageUrl(null)
                .build());

        historyArticleRepository.save(HistoryArticle.builder()
                .title("二、文化：藏于伞骨伞面的东方智慧")
                .content("油纸伞的文化内涵深植于中国人的生活哲学与审美情趣：\n" +
                        "符号寓意：“伞” 与 “散” 谐音相反，象征 “团圆聚合”，传统婚礼中 “打红伞” 的习俗，既寓意新人百年好合，也暗含对家族兴旺的期盼；伞面圆形对应 “天圆地方” 的宇宙观，竹骨挺拔象征君子气节，桐油防腐则寄托 “坚韧长久” 的生活愿景。\n" +
                        "工艺美学：一把油纸伞需经 “选竹、制骨、裱纸、上油、绘花” 等80 余道纯手工工序，从竹材砍伐到伞面绘制均依赖匠人经验。伞面图案兼具实用性与艺术性，除常见的花鸟、山水、吉祥纹样外，部分流派还融入地方戏曲、民间故事，使油纸伞成为 “可移动的民间艺术品”。\n" +
                        "跨域影响：油纸伞不仅是中国非遗，更通过丝绸之路、海上贸易传入日本、朝鲜、东南亚等地，成为东亚文化交流的载体。日本的 “和伞”、韩国的 “纸伞” 均借鉴了中国油纸伞的工艺，其伞面设计、使用场景仍保留着东方文化的共通性。")
                .author("非遗传承人")
                .type("CULTURE")
                .imageUrl(null)
                .build());
        
        historyArticleRepository.save(HistoryArticle.builder()
                .title("三、现状：传统匠艺的传承困境与保护价值")
                .content("随着现代工业的发展，塑料伞、折叠伞以其便捷性逐渐取代油纸伞的日常使用功能，传统油纸伞面临 “匠人老龄化、技艺断层、市场萎缩” 的三重困境：目前全国掌握完整油纸伞制作技艺的匠人不足百人，部分流派因年轻人不愿学艺而濒临失传。然而，油纸伞的文化价值与工艺价值并未褪色 —— 其 “竹骨纸面、桐油防腐” 的传统工艺蕴含着古人的生态智慧，80 余道手工工序是中国传统手工业的活态见证，伞面的吉祥纹样、绘画艺术则承载着民间文化的集体记忆。保护油纸伞，不仅是守护一项传统技艺，更是传承东方美学、生态智慧与民间文化的重要举措，这也为 “非遗数字化” 提供了核心的保护诉求与价值基础。")
                .author("陈华工作室")
                .type("STATUS")
                .imageUrl(null)
                .build());
    }
}
