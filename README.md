# 校园账本 · 月度明细与分类统计

当前工程包含成员 A 的记账表单与本地数据库，以及成员 B 的主页和分类统计。在 Android Studio 中打开包含 `settings.gradle.kts` 的工程目录，待 Gradle Sync 完成，选择 `app`，在 API 30 及以上的设备上运行。

## 成员 B 的交付

| 页面 | 可以完成的操作 |
|---|---|
| 明细 | 上下月切换、查看当月收入/支出/结余、按日期查看账单，打开原有表单新增或编辑。补记其他月份时默认选中该月 1 日，日期可修改。 |
| 统计 | 查看当月支出分类排行。金额按从大到小排列，横条表示各类支出占当月总支出的比例。没有支出时提示“本月暂无支出”。 |
| 预算入口 | 保留与明细、统计一致的所选月份，显示下一阶段的开发状态。成员 C 可将占位页面替换为正式预算页。 |

首页和统计共用 `MainActivity.selectedMonth()`。成员 B 的页面、账单适配器及月汇总在 Kotlin 包 `cn.campus.ledger.overview`。两页调用成员 A 的 `LedgerDao.month(start, end)`，从月初查到下月月初之前。收支均按分累加，统计只汇总支出。编辑返回时页面重新读取数据库，连续切月时按最后一次选择显示结果。

## 成员 B 提交到小组仓库

以已合并成员 A 源码的小组主仓库为起点。在本机克隆小组仓库后，在其根目录打开 PowerShell。将本次交付的 `CampusLedger-member-B.patch` 保存到本地，在提示时输入它的完整路径。

```powershell
git status --short
git switch main
git pull --ff-only origin main
git switch -c feature/monthly-overview
$patchFile = (Read-Host "输入成员 B 补丁文件的完整路径").Trim('"')
git apply --check "$patchFile"
git apply --index "$patchFile"
git diff --cached --stat
.\gradlew.bat :app:assembleDebug
```

开始前确认工作区干净。补丁检查成功后再执行应用命令。如果远端已修改同一处代码且检查失败，可在 Android Studio 中对照完整 B 源码，将相应改动合并到本地功能分支，保留组员已有修改。项目 SDK、Gradle JDK 的设置见下方运行说明。

验收完成后，用成员 B 自己的 GitHub 账号提交和推送。

```powershell
git commit -m "feat: 完成月度收支明细与分类统计"
git push -u origin feature/monthly-overview
```

在 GitHub 创建 PR，选择目标分支 `main`、来源分支 `feature/monthly-overview`。标题可用“第二阶段：月度明细与分类统计”。正文填入关联 Issue 编号、实测金额及结果，邀请成员 A 或 C 审查。合并后，成员 C 从更新后的 main 开发预算页。

## 成员 B 验收用例

1. 选择没有账单的月份，验证明细和统计的空状态，记入 1,200 元生活费、15 元餐饮、45 元学习支出。首页应显示收入 1,200 元、支出 60 元、结余 1,140 元。
2. 切到统计。学习应排第一，占 75%，餐饮占 25%。把餐饮改成 20 元，回到统计，总支出应为 65 元。
3. 把学习支出移到上月。本月支出应为 20 元，上月增加 45 元。在统计、明细、预算入口之间切换，月份选择应一致。
4. 删除剩余的餐饮支出，本月仍应保留生活费收入，统计页显示“本月暂无支出”。在成员 A 的安装上覆盖安装同一签名的 B 版本，原有账单应可按所属月份查看。

## 第一阶段记录

本阶段提供可以独立安装的 Android 应用。用户可以记录收支、修改金额与分类、补记其他日期、填写备注和删除账单。首页按日期倒序列出记录，点一行即可进入编辑。数据保存在手机本地。

## 在 Android Studio 运行

1. 解压项目或克隆仓库后，用 **File → Open** 打开包含 `settings.gradle.kts` 的 CampusLedger 根目录。
2. 等待 Gradle Sync 完成。如果 IDE 提示 SDK 缺失，在 SDK Manager 安装本工程指定的 Android SDK Platform 37。项目包含 Gradle Wrapper，守护进程使用 JDK 17，Java 与 Kotlin 目标级别为 17。
3. 在设备栏选择 Android 11（API 30）及以上的模拟器或手机，选择 **app** 并点 **Run ▶**。命令行构建可在 Windows 的工程根目录运行 `.\gradlew.bat :app:assembleDebug`。

## 成员 A 的交接接口

| 位置 | 职责 |
|---|---|
| `entry/EntryActivity.kt`、`res/layout/activity_entry.xml` | 收入与支出表单、输入校验、日期选择、保存及删除确认 |
| `data/Bill.kt` | 账单编号、类型、金额、日期、分类和备注 |
| `data/LedgerDao.kt`、`data/LedgerDb.kt` | Room 数据库存储、按月查询和后台线程读写 |
| `MainActivity.kt`、`res/layout/activity_main.xml` | 本阶段验收入口和账单列表，后续由 B 接手 |

Kotlin 文件均位于 `app/src/main/java/cn/campus/ledger/`。数据库名为 `campus-ledger.db`，第一版表名为 `bills`。金额字段 `cents` 始终保存正数，单位为分；`income` 表示收支方向。日期字段 `day` 使用 `yyyy-MM-dd`。B 可以调用 `month("2026-11-01", "2026-12-01")` 取得 11 月账单，起始日期包含在内，结束日期不包含在内。DAO 的同步方法应放入 `LedgerDb.run(...)` 的后台任务。编辑已有账单时，用 Intent 启动 `EntryActivity`，在 `EntryActivity.BILL_ID` 中传入 long 类型的账单编号。新增时无需传值。

## 成员 A 如何首次上传到 GitHub

以下流程适用于**新建的空仓库**和**解压后的项目目录**。本阶段交付的是工作文件，不含 Git 历史。三位成员使用各自的 GitHub 账号。

1. **建仓并邀请同学。** A 在 GitHub 创建仓库 `CampusLedger`，创建时留空 README、.gitignore 和许可证。进入 **Settings → Collaborators → Add people**，邀请 B、C。让两位同学接受邀请。
2. **建立主分支。** 在 Android Studio 打开本项目，打开底部 **Terminal**。先运行 `git rev-parse --show-toplevel`。全新解压目录通常会提示当前不在 Git 仓库中；如果显示某个上级仓库路径，应将项目解压到独立目录后再操作。在以下命令中把 `YOUR_ACCOUNT` 换成建仓人的 GitHub 用户名。两条配置命令只设置当前仓库的提交身份；邮箱可用自己 GitHub 账号的 noreply 邮箱。

```powershell
git init -b main
git config user.name "成员A自己的GitHub用户名"
git config user.email "成员A自己在GitHub使用的邮箱"
git add .gitignore .gitattributes
git commit -m "chore: 初始化校园账本仓库"
git remote add origin https://github.com/YOUR_ACCOUNT/CampusLedger.git
git push -u origin main
```

3. **设置审查并领取任务。** 仓库管理员进入 **Settings → Branches**，给 `main` 添加保护规则，启用合并前必须通过 PR，并设置 1 位审批者。GitHub Free 个人私有仓库若没有保护选项，可在仓库中约定同样的人工流程。然后在 **Issues** 新建“实现账单增删改查与 Room 存储”，分配给 A，记下实际 Issue 编号。
4. **在功能分支提交源码。** 在同一个 Terminal 执行以下命令。两次 `git add` 覆盖本阶段全部工程文件。检查暂存清单后提交；`local.properties`、`.idea`、`.gradle`、`build` 和 APK 均已排除。Gradle Wrapper 的 JAR 属于工程文件，需要一并提交。

```powershell
git switch -c feature/ledger-entry
git add app gradle gradlew gradlew.bat
git add build.gradle.kts settings.gradle.kts gradle.properties README.md
git update-index --chmod=+x gradlew
git diff --cached --stat
git commit -m "feat: 完成收支录入编辑删除和本地存储"
git push -u origin feature/ledger-entry
```

5. **创建第一个 PR。** 在仓库页面点击 **Compare & pull request**，选择 `base: main`、`compare: feature/ledger-entry`。标题填“第一阶段：账单录入与 Room 存储”。正文写 `Closes #实际编号`（例如 `Closes #1`），说明改动，再写上本人实际跑过的验收步骤。通过 **Reviewers** 请 B 或 C 审查。
6. **审查、合并与同步。** 审查人可在 Android Studio 的 **Get from VCS** 克隆这个主仓库，切换到 `feature/ledger-entry` 后运行项目。通过验收后在 PR 的 **Files changed → Review changes → Approve** 给出结论。A 合并 PR，再在本机执行 `git switch main` 和 `git pull --ff-only origin main`。后续成员从更新后的 main 开自己的功能分支。

若团队已有含 README 的远端仓库，可克隆该仓库，从它的 main 开 `feature/ledger-entry` 分支，再将本工程的源码与构建文件复制到克隆目录；把本文内容合并到现有 README，保留已有团队文件。随后按第 4 步暂存、第 5 步发起 PR。

## PR 验收用例

- 新增 15.50 元餐饮支出，备注 Lunch。应出现一条负号支出；另记 1,200 元生活费，应出现正号收入。
- 将餐饮支出改为学习支出，改成上月的日期和 20.25 元。退出应用再打开，检查金额、日期、备注及分类。
- 输入空金额、零或三位小数，尝试保存，页面应提示修正。
- 对同一笔账单先取消删除，再确认删除；清单应分别保留、移除相应记录。

协作操作参考 [GitHub 邀请协作者](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/repository-access-and-collaboration/inviting-collaborators-to-a-personal-repository)、[分支保护](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches)、[创建 Pull Request](https://docs.github.com/en/pull-requests/how-tos/create-pull-requests/creating-a-pull-request)。
