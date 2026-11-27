# Sons of Sins Bug Fixes

Non-invasive bug-fix, compatibility, and performance optimization addon for **Sons of Sins**, covering both **Minecraft 1.20.1 (Forge)** and **Minecraft 1.21.1 (NeoForge)**.

## Repository Structure

- [`1201-forge-sonsofsinsbugfix/`](1201-forge-sonsofsinsbugfix/) — Minecraft `1.20.1` Forge edition (requires Sons of Sins `2.2.1+`)
- [`1211-neoforge-sonsofsinsbugfix/`](1211-neoforge-sonsofsinsbugfix/) — Minecraft `1.21.1` NeoForge edition (requires Sons of Sins `2.2.1+`)

## Highlights

- **Critical Gameplay & Crash Fixes**: Fixes the 1.21.1 `LivingDamageEvent.Pre` armor-bypass bug (writing pre-armor `getOriginalDamage()` into `setNewDamage()`), 7 grab-execution `stopRiding()` server crashes (including PlayerRevive / death-cancel mod conflicts), Urn double ash duplication & underflow, 1.21.1 baby Cell Division Flesh Carcass duplication, `ItemStack.EMPTY` singleton corruption, dedicated-server `checkGamemode` client-class crashes, 1.21.1 `GameRules.register` concurrent setup safety, portable container `readShareTag` / `stillValid` / `ItemTossEvent` bugs, Sin Monster bedrock/container breaking, Betrayed Guzzler weapon-stealing bugs, and tamed pet teammate/PvP friendly fire.
- **Performance Optimizations**: Eliminates 1.21.1 GUI 20 packets/sec empty sync spam, caches all 24 entity `RenderLayer` models and Remnant Helmet/Chestplate `bakeLayer()` calls, caches all 10 Flesh Carcass GUI preview entities, converts idle Urn 10-tick self-scheduling to event-driven updates, throttles idle Blood block ticks and 64³ Curse entity scans, fixes `workQueue` delayed-task leaks/exceptions, removes `ViewportEvent` static `provider` memory leaks, and stops read-only NBT / `CUSTOM_DATA` pollution.
- **Rendering, Datapacks, Loot & Localization**: Fixes Remnant Chestplate floating duplicate rendering & enchantment glint loss (plus missing 1.21.1 texture), guards 30 organ render layers against missing addon textures and malformed IDs, fixes Ether Ghost color overflow (`> 1570`), restores 1.21.1 Walking Bed phase-2 hitbox shrinking and glow layer, fixes 22 missing `MapColor` values, modular tool compatibility and 1.21.1 Flesh/Shape-Shifting tool enchantability & Flesh Hoe repair material, cleans `forge:` attributes from 1.21.1 `sinful_church.nbt`, eliminates 11 JEI `Unknown recipe category` warnings, adds data-driven entity/chest loot tables with Looting support, and provides `en_us` / `zh_cn` / `fr_fr` TOP localization overrides.

---

# Sons of Sins Bug Fixes（原罪之子 Bug 修复）

面向 **原罪之子（Sons of Sins）** 的非侵入式 Bug 修复、兼容性与性能优化附属模组，同时包含 **Minecraft 1.20.1（Forge）** 与 **Minecraft 1.21.1（NeoForge）** 双版本源码。

## 目录结构

- [`1201-forge-sonsofsinsbugfix/`](1201-forge-sonsofsinsbugfix/) — Minecraft `1.20.1` Forge 版本（要求 Sons of Sins `2.2.1+`）
- [`1211-neoforge-sonsofsinsbugfix/`](1211-neoforge-sonsofsinsbugfix/) — Minecraft `1.21.1` NeoForge 版本（要求 Sons of Sins `2.2.1+`）

## 核心内容

- **严重玩法与崩溃修复**：修复 1.21.1 `LivingDamageEvent.Pre` 传入减伤前伤害导致全套护甲与抗性失效、7 处抓人处决 `stopRiding()` 空指针炸服（兼容 PlayerRevive 等取消死亡模组）、骨灰坛破坏双倍刷灰烬与下溢、1.21.1 细胞分裂幼体无限刷血肉躯壳、`ItemStack.EMPTY` 全局单例污染、专用服务端 `checkGamemode` 客户端类崩溃、1.21.1 游戏规则并发注册安全、便携容器 `readShareTag`/`stillValid`/`ItemTossEvent` 缺陷、原罪怪无视规则拆基岩与吞容器、背叛吞噬者偷武器丢物与瞬耗耐久，以及驯服宠物狩猎模式攻击队友/无视关闭 PvP 问题。
- **双端性能优化**：消除 1.21.1 便携容器界面每秒 20 包空同步风暴，缓存全部 24 个实体渲染层与遗骸头盔/胸甲模型烘焙、缓存血肉躯壳界面全部 10 种预览实体，将骨灰坛 10 tick 永久空转改为事件驱动更新，节流血池空转与 64³ 全实体扫描，修复延迟队列 `workQueue` 泄漏与异常击穿，清理 `ViewportEvent` 静态 `provider` 内存泄漏，消除只读 NBT/`CUSTOM_DATA` 污染。
- **渲染、数据包、战利品与本地化**：修复遗骸胸甲悬浮重影与附魔光泽丢失（并补全 1.21.1 贴图），为 30 个器官渲染层补齐缺失附属贴图透明回退与切分保护，修复以太幽灵血肉进度溢出花色、1.21.1 行走之床半血二阶段伏地碰撞箱卡死与发光层，补齐 22 个方块 `MapColor`、模块化工具挖掘兼容与 1.21.1 血肉/变形工具附魔及血肉锄修复材料，清洗 1.21.1 `sinful_church.nbt` 残留 `forge:` 属性，消除进服 11 条 JEI `Unknown recipe category` 刷屏，补齐支持抢夺附魔的生物/箱子 JSON 战利品表并提供 `en_us` / `zh_cn` / `fr_fr` TOP 本地化覆盖包。
