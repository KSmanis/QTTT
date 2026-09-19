# Changelog

## [1.0.5](https://github.com/KSmanis/QTTT/compare/v1.0.4...v1.0.5) (2026-09-19)


### Bug Fixes

* declare RTL layout support ([02cf095](https://github.com/KSmanis/QTTT/commit/02cf095c2a5af2d3d9a43b90cc25d13b592eed44))
* **deps:** update dependency androidx.appcompat:appcompat to v1.8.0 ([#2](https://github.com/KSmanis/QTTT/issues/2)) ([d75c960](https://github.com/KSmanis/QTTT/commit/d75c960ad1df92743dff10bb31a9320e0d374fbc))
* **deps:** update dependency com.google.android.material:material to v1.14.0 ([#4](https://github.com/KSmanis/QTTT/issues/4)) ([b09c511](https://github.com/KSmanis/QTTT/commit/b09c5119f403464808d0302acd4fe777cb3d43af))
* detach minimax work from activity lifecycle ([a86c030](https://github.com/KSmanis/QTTT/commit/a86c030adf5d392ba8d553241bd2c7c1ce8b6a40))
* disable app data backup ([#15](https://github.com/KSmanis/QTTT/issues/15)) ([6ad3750](https://github.com/KSmanis/QTTT/commit/6ad37508300272bde6dfb20663a9e30ed01a67be))
* disable cleartext traffic ([e4436b2](https://github.com/KSmanis/QTTT/commit/e4436b2371c36febe721015fac3cc17e85c92d52))
* expose game cells to accessibility services ([d1ab1e1](https://github.com/KSmanis/QTTT/commit/d1ab1e1a465237dfc8a4d7217f88c229fb8e8897))
* expose only legal board actions ([#14](https://github.com/KSmanis/QTTT/issues/14)) ([4e65760](https://github.com/KSmanis/QTTT/commit/4e65760dff1d15e887e39ec69de814f765d352d3))
* handle board rotation ([c799758](https://github.com/KSmanis/QTTT/commit/c799758a0670a5903cc84d9ab9dc0e7a33f595b8))
* handle nullable thinking state ([b391dbd](https://github.com/KSmanis/QTTT/commit/b391dbd4908025968d593ce3473c0234c4853c22))
* handle optimal difficulty explicitly ([6b049a2](https://github.com/KSmanis/QTTT/commit/6b049a2c6b039f3c384ef4900d0155a9c3f1ca81))
* harden single-player AI lifecycle ([4ae4e85](https://github.com/KSmanis/QTTT/commit/4ae4e858db57a7a19d333a54d69d4117acb4f7fd))
* reset game mark opacity ([#13](https://github.com/KSmanis/QTTT/issues/13)) ([dd89bb3](https://github.com/KSmanis/QTTT/commit/dd89bb34ebe5fe41ee0b4633978a0ff67c13faf6))
* resolve Java formatting lint warnings ([fdd86b6](https://github.com/KSmanis/QTTT/commit/fdd86b6582271191aa20b0177d84967395a083bc))
* reuse text bounds while drawing ([fb4e3cd](https://github.com/KSmanis/QTTT/commit/fb4e3cd58a291d13bc732d6b29c1cf739fdf0ef0))


### Performance Improvements

* avoid substring allocations while drawing ([db6b6ca](https://github.com/KSmanis/QTTT/commit/db6b6ca6a09937b1a619e53498b00c6b47e2e788))


### Reverts

* add `dependency-submission` workflow ([076b613](https://github.com/KSmanis/QTTT/commit/076b613669866ea09d5f0e1a5809ed132d1abe91))
