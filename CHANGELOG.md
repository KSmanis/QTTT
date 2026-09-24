# Changelog

## [1.1.1](https://github.com/KSmanis/QTTT/compare/v1.1.0...v1.1.1) (2026-09-24)


### Bug Fixes

* clarify narrow and double win results ([#19](https://github.com/KSmanis/QTTT/issues/19)) ([1f3728f](https://github.com/KSmanis/QTTT/commit/1f3728faaff0d51cc4dc2a0341c8e383021d1930))

## [1.1.0](https://github.com/KSmanis/QTTT/compare/v1.0.4...v1.1.0) (2026-09-21)


### Features

* add tutorial ([#16](https://github.com/KSmanis/QTTT/issues/16)) ([f1866d5](https://github.com/KSmanis/QTTT/commit/f1866d558f5e7ca94db3044b3437234d5544514a))


### Bug Fixes

* declare RTL layout support ([02cf095](https://github.com/KSmanis/QTTT/commit/02cf095c2a5af2d3d9a43b90cc25d13b592eed44))
* **deps:** update dependency androidx.appcompat:appcompat to v1.8.0 ([#2](https://github.com/KSmanis/QTTT/issues/2)) ([d75c960](https://github.com/KSmanis/QTTT/commit/d75c960ad1df92743dff10bb31a9320e0d374fbc))
* **deps:** update dependency com.google.android.material:material to v1.14.0 ([#4](https://github.com/KSmanis/QTTT/issues/4)) ([b09c511](https://github.com/KSmanis/QTTT/commit/b09c5119f403464808d0302acd4fe777cb3d43af))
* detach minimax work from activity lifecycle ([a86c030](https://github.com/KSmanis/QTTT/commit/a86c030adf5d392ba8d553241bd2c7c1ce8b6a40))
* disable Android data transfer ([106e513](https://github.com/KSmanis/QTTT/commit/106e51352a3c9de09f1dadc758ca3780c062aee9))
* disable app data backup ([#15](https://github.com/KSmanis/QTTT/issues/15)) ([6ad3750](https://github.com/KSmanis/QTTT/commit/6ad37508300272bde6dfb20663a9e30ed01a67be))
* disable cleartext traffic ([e4436b2](https://github.com/KSmanis/QTTT/commit/e4436b2371c36febe721015fac3cc17e85c92d52))
* expose game cells to accessibility services ([d1ab1e1](https://github.com/KSmanis/QTTT/commit/d1ab1e1a465237dfc8a4d7217f88c229fb8e8897))
* expose only legal board actions ([#14](https://github.com/KSmanis/QTTT/issues/14)) ([4e65760](https://github.com/KSmanis/QTTT/commit/4e65760dff1d15e887e39ec69de814f765d352d3))
* handle board rotation ([c799758](https://github.com/KSmanis/QTTT/commit/c799758a0670a5903cc84d9ab9dc0e7a33f595b8))
* handle nullable thinking state ([b391dbd](https://github.com/KSmanis/QTTT/commit/b391dbd4908025968d593ce3473c0234c4853c22))
* handle optimal difficulty explicitly ([6b049a2](https://github.com/KSmanis/QTTT/commit/6b049a2c6b039f3c384ef4900d0155a9c3f1ca81))
* harden single-player AI lifecycle ([4ae4e85](https://github.com/KSmanis/QTTT/commit/4ae4e858db57a7a19d333a54d69d4117acb4f7fd))
* make navigation bar icons legible ([daaa5aa](https://github.com/KSmanis/QTTT/commit/daaa5aadd2f5145ea0fd634727c0439f2d40d001))
* make status bar icons legible on edge-to-edge devices ([a37beed](https://github.com/KSmanis/QTTT/commit/a37beedb2509f1f4d7c4fefd4083d26bcf79aa3d))
* reset game mark opacity ([#13](https://github.com/KSmanis/QTTT/issues/13)) ([dd89bb3](https://github.com/KSmanis/QTTT/commit/dd89bb34ebe5fe41ee0b4633978a0ff67c13faf6))
* resolve Java formatting lint warnings ([fdd86b6](https://github.com/KSmanis/QTTT/commit/fdd86b6582271191aa20b0177d84967395a083bc))
* reuse text bounds while drawing ([fb4e3cd](https://github.com/KSmanis/QTTT/commit/fb4e3cd58a291d13bc732d6b29c1cf739fdf0ef0))


### Performance Improvements

* avoid substring allocations while drawing ([db6b6ca](https://github.com/KSmanis/QTTT/commit/db6b6ca6a09937b1a619e53498b00c6b47e2e788))


### Reverts

* add `dependency-submission` workflow ([076b613](https://github.com/KSmanis/QTTT/commit/076b613669866ea09d5f0e1a5809ed132d1abe91))
