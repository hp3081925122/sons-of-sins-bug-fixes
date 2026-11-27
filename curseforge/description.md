**Sons of Sins Bug Fixes** is a non-invasive bug-fix, performance, and compatibility addon for **Sons of Sins**, resolving server/client crashes, item duplication exploits, severe lag spikes, visual glitches, and datapack/localization issues while preserving original gameplay mechanics.

## What It Fixes

- **Crashes & Dedicated Server Safety**
  - Fixes the 7 grab-execution `stopRiding()` null-pointer server crashes (including compatibility crashes with PlayerRevive and other death-cancelling mods).
  - Isolates all `checkGamemode` client-class references (`Minecraft.getInstance()`) from dedicated servers, guards `PlayerVariables` sync/NBT edge cases, fixes `workQueue` delayed-task leaks/exceptions, wraps game-rule registration inside `enqueueWork`, fixes short organ tooltip and consecutive-underscore dimension name `StringIndexOutOfBoundsException` crashes, and guards advancement lookups and unsafe casts.
  - Fixes invincible utility entities (`BludTrade`, `BludAggressive`, `WistiverJaw`) blocking `/kill` and void damage, and protects `ItemStack.EMPTY` from singleton count corruption.
- **Gameplay & Exploit Fixes**
  - Fixes the `LivingDamageEvent.Pre` armor-bypass bug where pre-armor `getOriginalDamage()` overwrote `setNewDamage()`, restoring full armor, enchantment protection, and resistance damage reduction.
  - Fixes Urn double ash duplication on block break, portable Urn count underflow, baby Cell Division Devourers dropping full Flesh Carcasses, `AbstractArrow` projectile singleton corruption / pickup duping / infinite sky leaks, and portable container `stillValid` / `readShareTag` / `ItemTossEvent` exploits.
  - Prevents Sin Monsters from destroying unbreakable blocks (bedrock, command blocks, containers) and enforces `mobGriefing`; prevents tamed Flesh Creatures from attacking owners, teammates, or players on non-PvP servers; fixes Betrayed Guzzler weapon-stealing item loss and instant durability drain (with an optional toggle); fixes strict NBT `Inventory#contains` checks for renamed or enchanted items; fixes AOE attacks hitting dropped items and item frames; and restores Flesh Hoe repair material and Flesh Tool tooltips.
- **Performance & Rendering Fixes**
  - Eliminates per-tick empty `GUISyncMessage` packet storms (20 packets/sec per open GUI), caches all 24 entity `RenderLayer` models plus Remnant Helmet and Chestplate `bakeLayer()` calls, caches all 10 Flesh Carcass GUI preview entities, and caches holiday `Calendar` checks.
  - Fixes the Remnant Chestplate floating duplicate model blocking first-person vision and restores enchantment glint rendering; adds safe texture existence fallback across all 30 organ render layers for addon compatibility; fixes Ether Ghost color overflow (`> 1570`), Ether Soul particle rendering/rotation, Walking Bed phase-2 crouch hitbox updates and glow layer, and `ViewportEvent` static `provider` memory leaks.
  - Replaces idle 10-tick Urn self-scheduling with event-driven updates, throttles idle Blood block ticks and 64³ Curse entity scans, fixes `AcidBloodstream` per-tick `MobEffectEvent.Remove` storms, stops read-only NBT / `CUSTOM_DATA` pollution, and normalizes oversized 64–256 chunk entity tracking ranges and entity categories.
- **Datapacks, Loot, JEI & Localization**
  - Adds missing `MapColor` values to 22 blocks for Xaero's and vanilla maps, restores `FleshBlock` light occlusion, replaces hardcoded tool checks with standard mining tags, adds missing `enchantable/*`, aquatic entity, storage block, and record tags, cleans legacy `forge:` attributes from `sinful_church.nbt`, and provides standard data-driven entity/chest loot tables with Looting support.
  - Marks custom JEI recipes as special (eliminating 11 `Unknown recipe category` warnings), registers missing `RecipeType` entries, and localizes hardcoded tooltips, action-bar messages, container/JEI titles, and misspelled original keys (`en_us` / `zh_cn` / `fr_fr`).

## Configuration & Requirements

- Most fixes and behavior safeguards can be toggled individually in `sonsofsinsbugfix-common.toml` and `sonsofsinsbugfix-client.toml`.
- Requires **Sons of Sins** (`2.2.1` or newer).

## Source Code

- https://github.com/hp3081925122/sons-of-sins-bug-fixes

---

**Sons of Sins Bug Fixes（原罪之子 Bug 修复）** 是面向 **原罪之子（Sons of Sins）** 的非侵入式缺陷修复、性能优化与兼容性附属模组，在不破坏原模组核心玩法的前提下，集中修复崩服、刷物品漏洞、严重掉帧卡顿、渲染穿模以及数据包与本地化问题。

## 修复内容一览

- **崩溃与专用服务端安全**
  - 修复全部 7 处抓人处决过程在致死后直接调用 `stopRiding()` 引发的空指针炸服（解决与 PlayerRevive 等取消死亡模组的严重冲突）。
  - 彻底隔离专用服务端中 `checkGamemode` 对 `Minecraft.getInstance()` 的直接引用，加固玩家变量同步与 NBT 反序列化边界，修复延迟队列 `workQueue` 泄漏与异常击穿、游戏规则并发注册安全、短名器官提示与连续下划线维度名越界崩溃，并补全进度判空与类型强转保护。
  - 修复无敌功能实体（血池交易者、攻击态 Blud、威斯蒂弗颚部）拦截 `/kill` 与虚空伤害的问题，并防止 `ItemStack.EMPTY` 全局单例数量被污染。
- **玩法与复制漏洞修复**
  - 修复 `LivingDamageEvent.Pre` 将护甲减伤前原始伤害写回 `setNewDamage()` 导致全套护甲、保护附魔与抗性失效的严重缺陷。
  - 修复破坏骨灰坛双重吐灰烬刷物品、便携骨灰坛剩 1 份灰烬下溢、细胞分裂幼体死亡刷出满器官血肉躯壳、弹射物静态单例污染/盾反刷弹/永久滞留，以及便携容器 `stillValid` / `readShareTag` / `ItemTossEvent` 漏洞。
  - 阻止原罪怪破坏基岩、命令方块等不可破坏方块与容器并严格遵循 `mobGriefing`；阻止驯服血肉造物攻击主人、队友或关闭 PvP 服务器的玩家；修复背叛吞噬者偷武器丢物与瞬耗耐久（提供配置开关）；将背包严格 NBT 比对改为按物品匹配，避免重命名或附魔道具失效；防止范围技能摧毁掉落物与物品展示框；恢复血肉锄修复材料与血肉工具独立描述。
- **双端性能与渲染修复**
  - 消除便携容器界面每 tick 发送空 `GUISyncMessage` 的每秒 20 包网络风暴，缓存全部 24 个实体渲染层与遗骸头盔/胸甲模型烘焙，缓存血肉躯壳界面全部 10 种预览实体，并缓存节日日历查询。
  - 修复遗骸胸甲悬浮重复模型遮挡第一人称视野并恢复附魔光泽（补全缺失胸甲贴图）；为全部 30 个器官渲染层补齐缺失附属器官贴图的透明安全回退；修复以太幽灵血肉进度溢出花色、以太灵魂粒子图集与旋转卡死、行走之床半血二阶段伏地碰撞箱卡死与发光层，以及 `ViewportEvent` 静态 `provider` 内存泄漏。
  - 将骨灰坛 10 tick 永久空转改为物品变动事件驱动，节流血池空转与 64³ 全实体扫描，消除 `AcidBloodstream` 每 tick `MobEffectEvent.Remove` 事件风暴，杜绝只读 NBT 与 `CUSTOM_DATA` 污染，并修正全部 25 个实体错填的 64~256 区块追踪半径与实体分类。
- **数据包、战利品、JEI 与本地化**
  - 为 22 个缺失地图色的可见方块补全 `MapColor`（修复 Xaero 与原版地图隐形），恢复血肉方块实心遮光，移除硬编码镐子校验并补齐标准挖掘、附魔、水生实体、储存方块与唱片标签，清洗 `sinful_church.nbt` 残留旧版 `forge:` 属性，并补齐支持抢夺附魔与 JEI/JER/LootJS 的标准生物/箱子战利品表。
  - 标记自定义 JEI 配方为特殊配方（消除进服 11 条 `Unknown recipe category` 警告）并补齐缺失的 `RecipeType` 注册，全面本地化硬编码物品提示、服务端动作栏、容器与 JEI 标题并修正原模组拼写错误（内置 `en_us` / `zh_cn` / `fr_fr` TOP 覆盖包）。

## 配置与前置

- 绝大多数修复与行为保护均可在 `sonsofsinsbugfix-common.toml` 与 `sonsofsinsbugfix-client.toml` 中独立开关。
- 前置需安装 **Sons of Sins（原罪之子）** `2.2.1` 及以上版本。

## 源码仓库

- https://github.com/hp3081925122/sons-of-sins-bug-fixes
