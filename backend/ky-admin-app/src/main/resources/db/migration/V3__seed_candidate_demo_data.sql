SET NAMES utf8mb4;

UPDATE sys_user
SET display_name = '考研考生'
WHERE id = 10001;

INSERT INTO tree_group(user_id, name, description, sort_order)
SELECT 10001, '专业课', '计算机专业课与自定义专业课知识树', 10
WHERE NOT EXISTS (
  SELECT 1 FROM tree_group WHERE user_id = 10001 AND name = '专业课'
);

UPDATE tree_group
SET description = '计算机专业课与自定义专业课知识树',
    sort_order = 10
WHERE user_id = 10001 AND name = '专业课';

INSERT INTO tree_group(user_id, name, description, sort_order)
SELECT 10001, '公共课', '数学、英语、政治等公共课知识树', 20
WHERE NOT EXISTS (
  SELECT 1 FROM tree_group WHERE user_id = 10001 AND name = '公共课'
);

UPDATE tree_group
SET description = '数学、英语、政治等公共课知识树',
    sort_order = 20
WHERE user_id = 10001 AND name = '公共课';

SET @candidate_user_id = 10001;
SET @major_group_id = (SELECT id FROM tree_group WHERE user_id = @candidate_user_id AND name = '专业课' ORDER BY id LIMIT 1);
SET @public_group_id = (SELECT id FROM tree_group WHERE user_id = @candidate_user_id AND name = '公共课' ORDER BY id LIMIT 1);

UPDATE study_tree
SET name = '数据结构',
    group_id = @major_group_id,
    description = '线性结构、树图结构与算法题复盘',
    sort_order = 10
WHERE user_id = @candidate_user_id AND name = '408';

UPDATE study_tree
SET name = '数据结构',
    group_id = @major_group_id,
    description = '线性结构、树图结构与算法题复盘',
    sort_order = 10
WHERE user_id = @candidate_user_id AND name = '专业课默认树';

INSERT INTO study_tree(user_id, group_id, name, description, sort_order)
SELECT @candidate_user_id, @major_group_id, '数据结构', '线性结构、树图结构与算法题复盘', 10
WHERE NOT EXISTS (
  SELECT 1 FROM study_tree WHERE user_id = @candidate_user_id AND name = '数据结构'
);

INSERT INTO study_tree(user_id, group_id, name, description, sort_order)
SELECT @candidate_user_id, @major_group_id, '操作系统', '进程、内存、文件与 I/O 的状态推演', 20
WHERE NOT EXISTS (
  SELECT 1 FROM study_tree WHERE user_id = @candidate_user_id AND name = '操作系统'
);

INSERT INTO study_tree(user_id, group_id, name, description, sort_order)
SELECT @candidate_user_id, @major_group_id, '计算机组成原理', '存储、指令、CPU 与整机结构计算题', 30
WHERE NOT EXISTS (
  SELECT 1 FROM study_tree WHERE user_id = @candidate_user_id AND name = '计算机组成原理'
);

INSERT INTO study_tree(user_id, group_id, name, description, sort_order)
SELECT @candidate_user_id, @major_group_id, '计算机网络', '分层协议、传输控制与地址规划', 40
WHERE NOT EXISTS (
  SELECT 1 FROM study_tree WHERE user_id = @candidate_user_id AND name = '计算机网络'
);

INSERT INTO study_tree(user_id, group_id, name, description, sort_order)
SELECT @candidate_user_id, @public_group_id, '高等数学', '极限、导数、积分、线代与概率的公共课树', 10
WHERE NOT EXISTS (
  SELECT 1 FROM study_tree WHERE user_id = @candidate_user_id AND name = '高等数学'
);

INSERT INTO study_tree(user_id, group_id, name, description, sort_order)
SELECT @candidate_user_id, @public_group_id, '英语', '词汇、阅读、翻译、写作联动复盘', 20
WHERE NOT EXISTS (
  SELECT 1 FROM study_tree WHERE user_id = @candidate_user_id AND name = '英语'
);

INSERT INTO study_tree(user_id, group_id, name, description, sort_order)
SELECT @candidate_user_id, @public_group_id, '政治', '政治理论框架、时政热点与背诵节奏', 30
WHERE NOT EXISTS (
  SELECT 1 FROM study_tree WHERE user_id = @candidate_user_id AND name = '政治'
);

UPDATE study_tree
SET group_id = @public_group_id,
    description = '政治理论框架、时政热点与背诵节奏',
    sort_order = 30
WHERE user_id = @candidate_user_id AND name = '政治';

UPDATE study_tree
SET group_id = @major_group_id,
    description = '线性结构、树图结构与算法题复盘',
    sort_order = 10
WHERE user_id = @candidate_user_id AND name = '数据结构';

UPDATE study_tree
SET group_id = @major_group_id,
    description = '进程、内存、文件与 I/O 的状态推演',
    sort_order = 20
WHERE user_id = @candidate_user_id AND name = '操作系统';

UPDATE study_tree
SET group_id = @major_group_id,
    description = '存储、指令、CPU 与整机结构计算题',
    sort_order = 30
WHERE user_id = @candidate_user_id AND name = '计算机组成原理';

UPDATE study_tree
SET group_id = @major_group_id,
    description = '分层协议、传输控制与地址规划',
    sort_order = 40
WHERE user_id = @candidate_user_id AND name = '计算机网络';

UPDATE study_tree
SET group_id = @public_group_id,
    description = '极限、导数、积分、线代与概率的公共课树',
    sort_order = 10
WHERE user_id = @candidate_user_id AND name = '高等数学';

UPDATE study_tree
SET group_id = @public_group_id,
    description = '词汇、阅读、翻译、写作联动复盘',
    sort_order = 20
WHERE user_id = @candidate_user_id AND name = '英语';

SET @ds_tree_id = (SELECT id FROM study_tree WHERE user_id = @candidate_user_id AND name = '数据结构' ORDER BY id LIMIT 1);
SET @os_tree_id = (SELECT id FROM study_tree WHERE user_id = @candidate_user_id AND name = '操作系统' ORDER BY id LIMIT 1);
SET @coa_tree_id = (SELECT id FROM study_tree WHERE user_id = @candidate_user_id AND name = '计算机组成原理' ORDER BY id LIMIT 1);
SET @cn_tree_id = (SELECT id FROM study_tree WHERE user_id = @candidate_user_id AND name = '计算机网络' ORDER BY id LIMIT 1);
SET @math_tree_id = (SELECT id FROM study_tree WHERE user_id = @candidate_user_id AND name = '高等数学' ORDER BY id LIMIT 1);
SET @english_tree_id = (SELECT id FROM study_tree WHERE user_id = @candidate_user_id AND name = '英语' ORDER BY id LIMIT 1);
SET @politics_tree_id = (SELECT id FROM study_tree WHERE user_id = @candidate_user_id AND name = '政治' ORDER BY id LIMIT 1);

INSERT INTO syllabus_node(id, parent_id, code, title, label, level_no, sort_order, description)
VALUES
  (42, 40, 'CS-VM', '虚拟存储器', '节', 3, 20, '页式管理、地址变换与局部性原理'),
  (43, 42, 'CS-TLB', 'TLB 与快表', '考点', 4, 10, '快表命中、地址变换与访问开销分析'),
  (23, 20, 'DS-STACK-QUEUE', '栈与队列', '节', 3, 20, '顺序栈、链栈、循环队列与应用'),
  (24, 2, 'DS-GRAPH', '图与遍历', '章', 2, 20, '图的存储表示、遍历与最短路径'),
  (25, 24, 'DS-GRAPH-TRAVERSE', 'DFS 与 BFS', '节', 3, 10, '深度优先、广度优先与应用场景'),
  (26, 25, 'DS-GRAPH-TOPO', '拓扑排序与关键路径', '考点', 4, 10, '有向无环图中的排序与工程调度'),
  (33, 31, 'CN-HANDSHAKE', '三次握手与四次挥手', '考点', 4, 20, '连接建立与连接释放的细节'),
  (34, 4, 'CN-NETWORK', '网络层', '章', 2, 20, 'IP、路由、分片与地址规划')
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  code = VALUES(code),
  title = VALUES(title),
  label = VALUES(label),
  level_no = VALUES(level_no),
  sort_order = VALUES(sort_order),
  description = VALUES(description);

INSERT INTO user_syllabus_node(
  id, user_id, tree_id, official_node_id, parent_id, title, label, icon_key,
  level_no, sort_order, status, review_count, plain_understanding, today_feeling,
  custom_node, weak_score
)
VALUES
  (1, 10001, @coa_tree_id, 1, NULL, '计算机组成原理', '科目', 'cpu', 1, 10, 'IN_PROGRESS', 4, '先抓住层次化存储、指令执行流程和总线交互，再去啃细碎计算题。', '今天刷题时 Cache 命中率题还行，但地址映射一换条件就容易算乱。', 0, 32.00),
  (2, 10001, @ds_tree_id, 2, NULL, '数据结构', '科目', 'database', 1, 20, 'IN_PROGRESS', 5, '数据结构最怕只背模板，真正拿分还是靠把结构特性和题型对应起来。', '链表题做得顺，图论部分还没有形成稳定手感。', 0, 28.00),
  (3, 10001, @os_tree_id, 3, NULL, '操作系统', '科目', 'terminal', 1, 30, 'IN_PROGRESS', 6, '操作系统核心是把抽象概念变成状态变化和资源竞争过程来看。', 'PV 题仍然会卡住，尤其是多信号量同时变化的时候。', 0, 41.00),
  (4, 10001, @cn_tree_id, 4, NULL, '计算机网络', '科目', 'network', 1, 40, 'IN_PROGRESS', 4, '网络题的主线是分层职责，抓住每层解决什么问题就不容易串。', 'TCP 细节比 UDP 更熟，但网络层还需要连到画图题一起练。', 0, 26.00),
  (5, 10001, @os_tree_id, 10, 3, '进程与线程', '章', 'workflow', 2, 10, 'IN_PROGRESS', 5, '进程像资源分配单位，线程像执行单位，后面的同步调度都围着这层展开。', '调度算法一做就想混淆周转时间和响应时间。', 0, 44.00),
  (6, 10001, @ds_tree_id, 20, 2, '线性表', '章', 'layers', 2, 20, 'MASTERED', 7, '线性表是很多题型的出发点，顺序与链式两种存储的差别必须条件反射。', '顺序表和链表优缺点已经比较稳，接下来主要补边界条件。', 0, 15.00),
  (7, 10001, @cn_tree_id, 30, 4, '传输层', '章', 'cable', 2, 10, 'IN_PROGRESS', 4, '传输层重在可靠传输机制和流量拥塞控制的因果链。', '一旦题目把滑动窗口和拥塞窗口放一起，我就会犹豫先看哪个。', 0, 33.00),
  (8, 10001, @coa_tree_id, 40, 1, '存储系统', '章', 'memory', 2, 10, 'IN_PROGRESS', 4, '存储系统一定要按层次看速度、容量与成本的交换关系。', 'Cache 和虚拟存储串起来之后清晰多了，但细节计算还没完全稳。', 0, 36.00),
  (9, 10001, @os_tree_id, 11, 5, '同步与互斥', '节', 'shield', 3, 10, 'IN_PROGRESS', 6, '同步是为了顺序约束，互斥是为了保护临界资源，这两个目标必须分开看。', '写 PV 题时最怕遗漏一个 V 操作，结果整个流程都错。', 0, 63.00),
  (10, 10001, @ds_tree_id, 21, 6, '链表', '节', 'branch', 3, 10, 'MASTERED', 8, '链表题只要画清指针变化顺序，很多题其实就是局部重连。', '基础操作很稳，但涉及哨兵结点时偶尔会忘记判空。', 0, 18.00),
  (11, 10001, @cn_tree_id, 31, 7, 'TCP 协议', '节', 'network', 3, 10, 'IN_PROGRESS', 5, 'TCP 是把不可靠网络包装成可靠字节流服务的整套机制。', '窗口、确认、重传一起出现时，需要再多做流程推演题。', 0, 38.00),
  (12, 10001, @coa_tree_id, 41, 8, 'Cache 映射与替换策略', '节', 'circuit', 3, 10, 'IN_PROGRESS', 5, 'Cache 题先识别映射方式，再判断替换策略和命中情况。', '组相联映射的块号拆分还不是特别自然。', 0, 52.00),
  (13, 10001, @os_tree_id, 12, 9, 'PV 操作与信号量', '考点', 'binary', 4, 10, 'IN_PROGRESS', 7, '信号量本质是用整数和原语去约束并发执行次序。', '今天又卡在生产者消费者的变体题上，能看懂题意但列不稳 P 和 V。', 0, 78.00),
  (14, 10001, @ds_tree_id, 22, 10, '链表逆置', '考点', 'route', 4, 10, 'MASTERED', 9, '逆置核心是保存后继、翻转指向、整体推进三步循环。', '手写已经很顺，现在主要提醒自己检查空链表和单结点。', 0, 12.00),
  (15, 10001, @cn_tree_id, 32, 11, 'TCP 拥塞控制', '考点', 'workflow', 4, 10, 'IN_PROGRESS', 6, '拥塞控制要连着慢开始、拥塞避免、快重传、快恢复一起理解。', 'ssthresh 更新规则还是会混，得再整理成一张流程表。', 0, 58.00),
  (16, 10001, @os_tree_id, 13, 9, '死锁', '考点', 'atom', 4, 20, 'IN_PROGRESS', 5, '死锁就是资源分配进入循环等待，要会从条件和处理策略两边分析。', '预防、避免、检测三个思路背得出，但一到题里就容易张冠李戴。', 0, 61.00),
  (101, 10001, @coa_tree_id, NULL, 1, '指令系统', '章', 'code', 2, 20, 'NOT_STARTED', 1, '这一章要先把寻址方式和指令格式理顺，再碰细碎计算题。', '刚开了个头，先建立总表，后面再专项刷选择题。', 1, 22.00),
  (102, 10001, @coa_tree_id, NULL, 1, '总线与 I/O', '章', 'cable', 2, 30, 'NOT_STARTED', 0, '总线和 I/O 更像补全整机视角，需要和中断、DMA 联动理解。', '目前只是看过概念，还没形成题感。', 1, 18.00),
  (103, 10001, @os_tree_id, NULL, 3, '页面置换算法', '章', 'workflow', 2, 20, 'IN_PROGRESS', 3, 'FIFO、LRU、OPT 的差异要落到访问串上去比较。', 'Belady 异常已经理解，但自己算 LRU 还是容易漏最近访问次序。', 1, 57.00),
  (104, 10001, @coa_tree_id, NULL, 8, '局部性原理', '节', 'brain', 3, 30, 'MASTERED', 4, '时间局部性和空间局部性解释了为什么 Cache 与页式存储都有效。', '这个点已经比较通顺，适合拿来串联多个章节。', 1, 16.00),
  (105, 10001, @ds_tree_id, NULL, 2, '栈与队列', '章', 'boxes', 2, 30, 'IN_PROGRESS', 4, '栈和队列往往考基本操作与典型应用场景，比如括号匹配和层次遍历。', '循环队列判满判空的条件仍要刻意复述。', 1, 31.00),
  (106, 10001, @ds_tree_id, NULL, 2, '图与遍历', '章', 'branch', 2, 40, 'NOT_STARTED', 1, '图论部分容易一下子变杂，必须先稳住存储结构和遍历框架。', '还没真正开始做图题，先把邻接矩阵和邻接表复盘一下。', 1, 24.00),
  (107, 10001, @os_tree_id, NULL, 9, '临界资源经典问题', '考点', 'shield', 4, 30, 'IN_PROGRESS', 5, '生产者消费者、读者写者、哲学家进餐其实是在练约束条件的表达。', '一遇到公平性要求，我就会多加或少加一个信号量。', 1, 73.00),
  (108, 10001, @os_tree_id, NULL, 5, '调度算法比较', '节', 'route', 3, 20, 'IN_PROGRESS', 4, '先分清抢占和非抢占，再比较等待时间、响应时间和吞吐量。', '短作业优先和时间片轮转的题算起来还不够快。', 1, 46.00),
  (109, 10001, @cn_tree_id, NULL, 11, '流量控制与可靠传输', '考点', 'network', 4, 20, 'IN_PROGRESS', 5, '可靠传输靠确认、重传、编号，流量控制靠接收方窗口调节。', '累计确认和超时重传放在一个题里时，我会把事件顺序搞混。', 1, 49.00),
  (110, 10001, @cn_tree_id, NULL, 11, '三次握手与四次挥手', '考点', 'workflow', 4, 30, 'MASTERED', 6, '背步骤不够，要知道为什么需要 SYN、ACK、TIME_WAIT 这些状态。', '这块已经挺稳，偶尔只会忘记 TIME_WAIT 的作用。', 1, 19.00),
  (111, 10001, @cn_tree_id, NULL, 4, '网络层', '章', 'route', 2, 20, 'NOT_STARTED', 1, '网络层主要看 IP 地址、路由选择、分片重组和子网划分。', '打算放到下周集中开一个专题，把常见计算题一起做。', 1, 27.00),
  (112, 10001, @cn_tree_id, NULL, 111, '子网划分与 CIDR', '节', 'sigma', 3, 10, 'NOT_STARTED', 0, 'CIDR 题关键是掩码与地址块的换算，做多了会越来越像固定模板。', '还没有真正刷题，先留个坑位。', 1, 29.00),
  (113, 10001, @coa_tree_id, NULL, 102, '中断系统与 DMA', '节', 'circuit', 3, 10, 'NOT_STARTED', 0, '这部分要从 CPU 与外设协同的视角来记，不然很容易碎。', '概念了解了一遍，细节等后面再补。', 1, 21.00),

  (17, 10001, @math_tree_id, NULL, NULL, '高等数学', '模块', 'sigma', 1, 10, 'IN_PROGRESS', 8, '高数是公共课数学的主线，先把极限、导数、积分三条主线拉出来。', '题量一上来就会感觉很长，所以我在练分块复习。', 1, 39.00),
  (201, 10001, @math_tree_id, NULL, NULL, '线性代数', '模块', 'layers', 1, 20, 'IN_PROGRESS', 5, '线代更像结构化推理，矩阵、方程组、特征值三块要互相勾连。', '证明题会慢一些，需要多做书写训练。', 1, 34.00),
  (202, 10001, @math_tree_id, NULL, NULL, '概率论与数理统计', '模块', 'atom', 1, 30, 'NOT_STARTED', 2, '概率部分概念多，后期要把分布、期望、方差统一进一个框架。', '目前还在预热阶段，没有正式开刷。', 1, 25.00),
  (203, 10001, @math_tree_id, NULL, 17, '极限与连续', '专题', 'route', 2, 10, 'IN_PROGRESS', 6, '极限题先判断类型，再选等价无穷小、洛必达或夹逼等工具。', '能想到方法，但计算经常拖慢节奏。', 1, 47.00),
  (204, 10001, @math_tree_id, NULL, 17, '一元函数微分学', '专题', 'workflow', 2, 20, 'IN_PROGRESS', 7, '导数既是计算题也是证明题的入口，定义和几何意义都不能丢。', '综合题里经常先求导再讨论单调性，这个链条还要更熟。', 1, 42.00),
  (205, 10001, @math_tree_id, NULL, 17, '一元函数积分学', '专题', 'sigma', 2, 30, 'IN_PROGRESS', 5, '积分先分不定积分与定积分，再按换元、分部、几何意义来拆。', '定积分换元没问题，但遇到参数积分还是发虚。', 1, 44.00),
  (206, 10001, @math_tree_id, NULL, 17, '常微分方程', '专题', 'branch', 2, 40, 'NOT_STARTED', 1, '这部分套路感很强，先建立方程类型与解法映射。', '目前只看过大纲，还没系统推进。', 1, 23.00),
  (207, 10001, @math_tree_id, NULL, 203, '极限计算', '题型', 'sigma', 3, 10, 'IN_PROGRESS', 8, '极限计算要先化简，再看是否满足套公式的前提。', '一遇到分段函数和含参极限，就会担心自己分类不全。', 1, 64.00),
  (208, 10001, @math_tree_id, NULL, 204, '导数应用', '题型', 'brain', 3, 10, 'IN_PROGRESS', 7, '导数应用常落在单调性、极值、最值和切线问题。', '函数证明题里总想太多，反而不敢先求导。', 1, 53.00),
  (209, 10001, @math_tree_id, NULL, 205, '定积分几何意义', '题型', 'boxes', 3, 10, 'MASTERED', 5, '面积与体积题先画图，积分上下限和被积函数会直观很多。', '画草图之后准确率明显提高，这块逐渐稳定。', 1, 18.00),
  (210, 10001, @math_tree_id, NULL, 205, '二重积分', '题型', 'layers', 3, 20, 'IN_PROGRESS', 4, '二重积分先选积分次序，再处理积分区域。', '换成极坐标时偶尔会漏掉雅可比。', 1, 56.00),
  (211, 10001, @math_tree_id, NULL, 201, '矩阵运算', '专题', 'boxes', 2, 10, 'MASTERED', 6, '矩阵乘法和初等变换是线代所有后续题型的手柄。', '基础计算已经比较稳，接下来把速度再提一提。', 1, 17.00),
  (212, 10001, @math_tree_id, NULL, 201, '线性方程组', '专题', 'cable', 2, 20, 'IN_PROGRESS', 5, '秩、基础解系和增广矩阵之间的关系必须形成同一张图。', '做题时有时能算出结果，但解释不够规范。', 1, 45.00),
  (213, 10001, @math_tree_id, NULL, 201, '特征值与特征向量', '专题', 'atom', 2, 30, 'IN_PROGRESS', 4, '特征值问题看着抽象，其实就是把矩阵作用变成伸缩关系。', '相似对角化的条件记住了，但题目迁移还不够快。', 1, 51.00),
  (214, 10001, @math_tree_id, NULL, 202, '随机变量及其分布', '专题', 'binary', 2, 10, 'NOT_STARTED', 1, '离散型和连续型的定义、分布函数和密度函数要分层记。', '准备等高数节奏稳一些再全面推进。', 1, 28.00),
  (215, 10001, @math_tree_id, NULL, 202, '数字特征', '专题', 'brain', 2, 20, 'NOT_STARTED', 1, '期望、方差、协方差最后要和常见分布的结论打通。', '公式先记了个轮廓，还没下题。', 1, 30.00),
  (216, 10001, @math_tree_id, NULL, 202, '大数定律与中心极限定理', '专题', 'workflow', 2, 30, 'NOT_STARTED', 0, '这部分更适合后期收束时整体理解，不急着一开始深挖。', '先留到暑期强化阶段。', 1, 20.00),

  (301, 10001, @english_tree_id, NULL, NULL, '词汇', '模块', 'book', 1, 10, 'IN_PROGRESS', 9, '英语提分的底盘还是词汇，但不是孤立背，而是带语境复现。', '背新词不难，难的是熟词僻义总在阅读里偷袭我。', 1, 36.00),
  (302, 10001, @english_tree_id, NULL, NULL, '阅读', '模块', 'book', 1, 20, 'IN_PROGRESS', 8, '阅读部分是总分大头，要把长难句、定位、选项干扰一起练。', '错题大多不是看不懂，而是定位后没有抓住题干真正问法。', 1, 48.00),
  (303, 10001, @english_tree_id, NULL, NULL, '写作', '模块', 'code', 1, 30, 'IN_PROGRESS', 4, '写作不能只背模板，要准备自己的句型仓库和主题素材。', '小作文套路初步有了，大作文还缺能自然套用的表达。', 1, 33.00),
  (304, 10001, @english_tree_id, NULL, NULL, '翻译', '模块', 'cable', 1, 40, 'IN_PROGRESS', 3, '翻译最吃句法分析，真正难的是拆结构而不是逐词对应。', '长句里主干抓得慢，导致整句都不敢下笔。', 1, 43.00),
  (305, 10001, @english_tree_id, NULL, NULL, '完形与新题型', '模块', 'branch', 1, 50, 'NOT_STARTED', 2, '这两部分更看语篇感觉和逻辑线索，适合中后期集中突破。', '暂时只做过几篇，先把阅读稳定下来再加量。', 1, 24.00),
  (306, 10001, @english_tree_id, NULL, 301, '词根词缀', '专题', 'layers', 2, 10, 'MASTERED', 6, '词根词缀是扩词效率最高的工具，特别适合串联近义词。', '这一块效果不错，背单词不再完全靠死记。', 1, 15.00),
  (307, 10001, @english_tree_id, NULL, 301, '熟词僻义', '专题', 'brain', 2, 20, 'IN_PROGRESS', 5, '考研英语喜欢拿熟词的非常见义项卡人，必须在真题语境里记。', '今天又被 address 和 subject 的语境义绊住。', 1, 52.00),
  (308, 10001, @english_tree_id, NULL, 302, '长难句拆分', '专题', 'cable', 2, 10, 'IN_PROGRESS', 7, '先找主干，再拆从句和插入语，最后再还原中文逻辑。', '句子一长我就容易不敢下刀，得更果断地划主谓宾。', 1, 62.00),
  (309, 10001, @english_tree_id, NULL, 302, '主旨题与态度题', '专题', 'route', 2, 20, 'IN_PROGRESS', 6, '主旨题要抓全文重心，态度题要盯评价词和转折句。', '一看到选项都像对的，就说明我还没有真正站在作者视角上。', 1, 49.00),
  (310, 10001, @english_tree_id, NULL, 305, '段落匹配', '专题', 'boxes', 2, 10, 'NOT_STARTED', 1, '这类题要做信息定位和同义替换识别，不适合纯凭感觉。', '计划等真题二刷时专项练。', 1, 21.00),
  (311, 10001, @english_tree_id, NULL, 303, '小作文模板', '专题', 'workflow', 2, 10, 'IN_PROGRESS', 4, '书信、通知、邀请这些体裁要先稳住格式和高频句。', '开头和结尾已经背住，但主体段还需要更自然。', 1, 29.00),
  (312, 10001, @english_tree_id, NULL, 303, '大作文结构', '专题', 'layers', 2, 20, 'IN_PROGRESS', 3, '大作文至少要准备图画描述、现象解释、个人观点三段节奏。', '素材还不够个性化，写着写着容易变成空话。', 1, 41.00),
  (313, 10001, @english_tree_id, NULL, 304, '汉译英顺译', '专题', 'route', 2, 10, 'NOT_STARTED', 1, '先顺出逻辑主线，再补时态和修饰成分，比死抠词更有效。', '刚开始接触，还在摸语序感觉。', 1, 26.00),
  (314, 10001, @english_tree_id, NULL, 304, '英译汉断句', '专题', 'cable', 2, 20, 'IN_PROGRESS', 3, '翻译的第一步不是直译，而是敢于断句和重组信息。', '定语从句一多就会翻得发硬，得多看范文处理。', 1, 44.00),
  (315, 10001, @english_tree_id, NULL, 305, '完形逻辑线索', '专题', 'branch', 2, 20, 'NOT_STARTED', 1, '完形更像微型阅读，转折、因果、递进都要敏感。', '还没系统整理过这部分。', 1, 23.00),
  (316, 10001, @english_tree_id, NULL, 302, '阅读错因归类', '专题', 'database', 2, 30, 'IN_PROGRESS', 5, '把错因拆成定位偏差、逻辑误读、词义误判，比只看分数更有用。', '归类后发现自己最容易死在过度脑补上。', 1, 46.00),

  (401, 10001, @politics_tree_id, NULL, NULL, '马原', '模块', 'brain', 1, 10, 'IN_PROGRESS', 6, '马原不是靠硬背孤立句子，而是搭建世界观、方法论和认识论框架。', '选择题能跟住，大题还不能自然展开。', 1, 37.00),
  (402, 10001, @politics_tree_id, NULL, NULL, '史纲', '模块', 'book', 1, 20, 'IN_PROGRESS', 4, '史纲要抓阶段线索和事件意义，避免只剩年份记忆。', '重大转折点能记住，但横向比较还比较弱。', 1, 32.00),
  (403, 10001, @politics_tree_id, NULL, NULL, '毛中特', '模块', 'shield', 1, 30, 'NOT_STARTED', 2, '毛中特后期投入更划算，重点是概念体系和政策表达。', '目前只在听导学课。', 1, 22.00),
  (404, 10001, @politics_tree_id, NULL, NULL, '思修法基', '模块', 'shield', 1, 40, 'NOT_STARTED', 1, '这块概念直白，但需要注意条文表述和案例落点。', '先记了章节框架，题目还没铺开。', 1, 18.00),
  (405, 10001, @politics_tree_id, NULL, NULL, '时政', '模块', 'network', 1, 50, 'IN_PROGRESS', 3, '时政更适合作为持续积累模块，不要等到最后一周突击。', '每天看一点，先把高频会议和主题整理成卡片。', 1, 35.00),
  (406, 10001, @politics_tree_id, NULL, 401, '实践与认识', '专题', 'workflow', 2, 10, 'IN_PROGRESS', 5, '认识来源于实践、又反作用于实践，这条链要能自己说顺。', '大题表述还是偏口语，需要更像标准答案。', 1, 43.00),
  (407, 10001, @politics_tree_id, NULL, 401, '矛盾分析法', '专题', 'branch', 2, 20, 'IN_PROGRESS', 4, '主要矛盾和矛盾主要方面很容易混，必须通过例子去压实。', '选项里一换语序就容易被带走。', 1, 47.00),
  (408, 10001, @politics_tree_id, NULL, 402, '近现代历史主线', '专题', 'route', 2, 10, 'MASTERED', 5, '史纲复习的关键是主线清楚，再去挂载人物事件和意义。', '这一块框架感不错，后续主要补细节。', 1, 20.00),
  (409, 10001, @politics_tree_id, NULL, 402, '新民主主义革命', '专题', 'shield', 2, 20, 'IN_PROGRESS', 4, '革命道路、统一战线和党的建设是高频组合考点。', '一做多选题就怕漏项，需要再细抠教材表述。', 1, 39.00),
  (410, 10001, @politics_tree_id, NULL, 403, '中国式现代化', '专题', 'network', 2, 10, 'NOT_STARTED', 1, '这部分要结合最新表述和核心特征来背。', '计划放到暑期后半段系统收。', 1, 24.00),
  (411, 10001, @politics_tree_id, NULL, 403, '党的建设', '专题', 'shield', 2, 20, 'NOT_STARTED', 1, '党的建设通常和政治表述联系紧，需要原句记忆。', '先占位，等课程推进。', 1, 19.00),
  (412, 10001, @politics_tree_id, NULL, 404, '法律基础', '专题', 'book', 2, 10, 'NOT_STARTED', 1, '法基题常见到案例表达，所以不能只背定义。', '还没正式开始。', 1, 17.00),
  (413, 10001, @politics_tree_id, NULL, 404, '道德与理想信念', '专题', 'circle', 2, 20, 'NOT_STARTED', 1, '这部分更适合用关键词去串，不需要把每句都背死。', '先过了一遍目录。', 1, 16.00),
  (414, 10001, @politics_tree_id, NULL, 405, '当月热点专题', '专题', 'network', 2, 10, 'IN_PROGRESS', 3, '时政热点要按会议、政策、国际事件分桶整理。', '这周准备把经济和科技类热点做成一页总结。', 1, 42.00),
  (415, 10001, @politics_tree_id, NULL, 405, '会议与文件关键词', '专题', 'book', 2, 20, 'IN_PROGRESS', 3, '同一主题在不同文件里的提法很像，适合做对照表。', '最近开始做关键词摘录，感觉终于没那么散了。', 1, 31.00)
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  tree_id = VALUES(tree_id),
  official_node_id = VALUES(official_node_id),
  parent_id = VALUES(parent_id),
  title = VALUES(title),
  label = VALUES(label),
  icon_key = VALUES(icon_key),
  level_no = VALUES(level_no),
  sort_order = VALUES(sort_order),
  status = VALUES(status),
  review_count = VALUES(review_count),
  plain_understanding = VALUES(plain_understanding),
  today_feeling = VALUES(today_feeling),
  custom_node = VALUES(custom_node),
  weak_score = VALUES(weak_score);

INSERT INTO learning_node_tag(id, user_id, user_node_id, name, color)
VALUES
  (1001, 10001, 13, '高频易错', '#ef4444'),
  (1002, 10001, 13, '二刷重点', '#f97316'),
  (1003, 10001, 16, '概念混淆', '#fb7185'),
  (1004, 10001, 12, '公式必背', '#6366f1'),
  (1005, 10001, 14, '手写一遍', '#10b981'),
  (1006, 10001, 15, '真题高频', '#f43f5e'),
  (1007, 10001, 207, '计算易丢分', '#f59e0b'),
  (1008, 10001, 208, '综合题核心', '#8b5cf6'),
  (1009, 10001, 210, '积分区域', '#06b6d4'),
  (1010, 10001, 307, '熟词僻义', '#e11d48'),
  (1011, 10001, 308, '长难句', '#0ea5e9'),
  (1012, 10001, 312, '作文素材', '#14b8a6'),
  (1013, 10001, 316, '错因分析', '#6366f1'),
  (1014, 10001, 406, '大题储备', '#f97316'),
  (1015, 10001, 407, '方法论', '#84cc16'),
  (1016, 10001, 414, '时政热点', '#dc2626'),
  (1017, 10001, 415, '关键词摘录', '#0891b2'),
  (1018, 10001, 104, '知识串联', '#22c55e')
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  user_node_id = VALUES(user_node_id),
  name = VALUES(name),
  color = VALUES(color);

INSERT INTO learning_node_connection(id, user_id, source_node_id, target_node_id, relation_type, label)
VALUES
  (2001, 10001, 13, 16, 'CAUSES', '同步失误常常会进一步引出死锁分析'),
  (2002, 10001, 12, 104, 'RELATED', 'Cache 与局部性原理必须一起理解'),
  (2003, 10001, 14, 105, 'RELATED', '链表逆置常与栈队列思路对照练习'),
  (2004, 10001, 15, 109, 'SUPPORTS', '拥塞控制建立在可靠传输机制之上'),
  (2005, 10001, 207, 208, 'PREREQUISITE', '极限是导数定义和求导规则的前置基础'),
  (2006, 10001, 209, 210, 'PREREQUISITE', '先稳住一元积分，再过渡到二重积分'),
  (2007, 10001, 308, 314, 'SUPPORTS', '长难句拆分直接支撑翻译断句'),
  (2008, 10001, 309, 316, 'RELATED', '主旨题错因需要回到阅读归类中复盘'),
  (2009, 10001, 406, 407, 'RELATED', '认识论与矛盾分析法经常在同一道题里联动'),
  (2010, 10001, 410, 414, 'RELATED', '政治理论热点需要时政语料补充')
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  source_node_id = VALUES(source_node_id),
  target_node_id = VALUES(target_node_id),
  relation_type = VALUES(relation_type),
  label = VALUES(label);

INSERT INTO learning_reflection(id, user_id, user_node_id, content_type, content, agent_answer, created_at)
VALUES
  (3001, 10001, 13, 'FEELING', '今天做 PV 题还是会把同步和互斥混到一起，尤其是多缓冲区模型。', '先把题目中的资源、动作、约束三列出来，再决定哪个信号量负责互斥、哪个负责同步。不要急着直接写 P 和 V。', '2026-06-01 21:30:00'),
  (3002, 10001, 16, 'UNDERSTANDING', '死锁四个必要条件已经能背出来，但题目一绕就不会判断是哪种处理策略。', '做题时先判断它是在问预防、避免、检测还是解除，再去看资源分配图或安全序列，不要一开始就被术语带跑。', '2026-06-02 09:10:00'),
  (3003, 10001, 12, 'PLAN', '这周要把 Cache 映射题每天刷五道，重点练组相联。', '建议把每道题统一成“块号拆分、组号定位、替换判断”三步模板，几天后速度会明显上来。', '2026-06-02 22:05:00'),
  (3004, 10001, 207, 'ERROR_BOOK', '含参极限时漏掉了参数取值范围，结果分类讨论不完整。', '以后先写出参数可能改变函数符号或定义域的位置，宁可慢十秒，也别直接代公式。', '2026-06-03 15:20:00'),
  (3005, 10001, 208, 'FEELING', '导数应用综合题一长就会慌，不知道先证明还是先讨论单调性。', '先把目标拆开。若结论涉及最值或不等式，通常先求导找单调区间，再回到原结论组织证明。', '2026-06-03 22:40:00'),
  (3006, 10001, 308, 'FEELING', '阅读里的长难句总觉得每个词都认识，但整句意思抓不住。', '从主干开始切，不要试图一口吞下整句。先找谓语，再找主语、宾语，最后再把修饰部分一层层挂回去。', '2026-06-04 08:00:00'),
  (3007, 10001, 312, 'PLAN', '准备每周自己写一篇大作文，把素材句固定下来。', '可以先建立三套稳定骨架：现象类、品质类、选择类。先求结构稳，再逐步增加个性表达。', '2026-06-04 20:10:00'),
  (3008, 10001, 316, 'UNDERSTANDING', '最近发现阅读错误很多不是单词不认识，而是自己脑补了作者态度。', '这是很关键的发现。后续每次复盘都标出“证据句”，把判断建立在文本上，能明显减少主观带偏。', '2026-06-04 21:50:00'),
  (3009, 10001, 406, 'FEELING', '马原大题写出来总像口语，感觉不够像答案。', '先背住每个专题的三四个标准句型，比如“实践是认识的来源和发展的动力”，再往里填你自己的理解。', '2026-06-05 07:30:00'),
  (3010, 10001, 414, 'PLAN', '本周把科技创新和宏观经济热点整理成两张 A4。', '很好，这样后续做选择题和大题时都能快速调用材料。建议每个热点都保留“关键词、背景、可能考法”三栏。', '2026-06-05 08:10:00')
ON DUPLICATE KEY UPDATE
  user_id = VALUES(user_id),
  user_node_id = VALUES(user_node_id),
  content_type = VALUES(content_type),
  content = VALUES(content),
  agent_answer = VALUES(agent_answer),
  created_at = VALUES(created_at);

INSERT INTO community_post(id, author_id, title, content, status, created_at)
VALUES
  (4001, 2, '专业课暑期复习建议：先把知识树搭起来', '暑期不要急着全面刷题。先用知识树把科目、章节、考点拆开，再把错因和感受挂到叶子节点上，后面复盘会轻松很多。', 'PUBLISHED', '2026-06-01 10:00:00'),
  (4002, 10001, '今天把 PV 操作重新梳理了一遍', '之前总觉得 PV 题很玄，今天尝试先画资源和动作，再写信号量变化，感觉终于有点顺了。', 'PUBLISHED', '2026-06-02 22:00:00'),
  (4003, 2, '英语阅读别只盯单词量', '很多同学背了很多词还是阅读错误率高，核心问题往往是长难句结构分析和选项干扰识别。建议结合错因归类一起练。', 'PUBLISHED', '2026-06-03 12:30:00'),
  (4004, 10001, '政治时政准备开始做周总结', '零散看新闻太容易忘，准备每周输出一页热点总结，按会议、政策、国际事件三类整理。', 'PUBLISHED', '2026-06-05 09:00:00')
ON DUPLICATE KEY UPDATE
  author_id = VALUES(author_id),
  title = VALUES(title),
  content = VALUES(content),
  status = VALUES(status),
  created_at = VALUES(created_at);
