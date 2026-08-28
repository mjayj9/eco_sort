# 🌿 에코픽 (EcoPick) - AI 기반 우리 단지 배달 쓰레기 분리배출 도우미

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini%203.5%20Flash-8E75FF?style=flat-square&logo=google&logoColor=white)](https://ai.google.dev/)
[![Competition](https://img.shields.io/badge/2026-대한민국%20청소년%20창업경진대회-FF6F00?style=flat-square)](https://yee.go.kr)

> **"사진 한 장으로 오염도 분석부터 세척 가이드, 단지 랭킹 리워드까지!"**  
> **2026 대한민국 청소년 창업경진대회 출품작**  
> **국립대학법인 서울대학교사범대학부설중학교 창업동아리 `SNUMS_psg`**

---

## 📌 목차 (Table of Contents)
1. [프로젝트 개요 (Overview)](#-프로젝트-개요-overview)
2. [기획 배경 및 해결 과제 (Problem & Solution)](#-기획-배경-및-해결-과제-problem--solution)
3. [핵심 기능 (Key Features)](#-핵심-기능-key-features)
4. [시스템 아키텍처 및 기술 스택 (Architecture & Tech Stack)](#-시스템-아키텍처-및-기술-스택-architecture--tech-stack)
5. [프로젝트 디렉토리 구조 (Project Structure)](#-프로젝트-디렉토리-구조-project-structure)
6. [설치 및 실행 가이드 (Getting Started)](#-설치-및-실행-가이드-getting-started)
7. [팀원 소개 및 역할 (Team & Roles)](#-팀원-소개-및-역할-team--roles)

---

## 🌟 프로젝트 개요 (Overview)

**에코픽(EcoPick)** 은 배달 음식 소비 급증으로 인해 발생하는 복합 재질 및 기름/양념 오염 배달 쓰레기의 올바른 분리배출을 돕는 **AI 멀티모달 비전 기반 친환경 리워드 플랫폼**입니다.

사용자가 배달 용기나 쓰레기를 스마트폰 카메라로 촬영하면, **Google Gemini 3.5 Flash** 모델이 실시간으로 쓰레기의 **재질(PP, PS, PET, 종이 등)과 오염도(0~100%)를 정밀 분석**하여 세척 가이드와 분리배출 등급을 제공합니다. 올바른 실천을 인증하면 **에코 포인트**가 적립되며, 아파트 단지 간 **친환경 실시간 랭킹 경쟁**을 통해 자발적이고 지속 가능한 분리배출 문화를 조성합니다.

---

## 💡 기획 배경 및 해결 과제 (Problem & Solution)

### 🚨 당면한 문제 (Problem)
1. **오염된 배달 용기의 재활용 불가율 증가**: 배달 용기 플라스틱은 양념이나 기름때가 남아있으면 선별장에서 전량 일반 쓰레기(소각/매립)로 폐기됩니다.
2. **복합 재질 분리배출 지식 부족**: 뚜껑(PP), 본체(PS), 비닐 실링, 흡습패드 등 복합 포장재의 분리 방법을 소비자가 정확히 알기 어렵습니다.
3. **지속적인 실천 동기 부재**: 분리배출을 열심히 해도 개인이나 공동체에 돌아오는 실질적인 혜택이 없어 참여율이 저조합니다.

### 💡 에코픽의 해결 솔루션 (Solution)
- **AI 멀티모달 비전 분석**: 사진만 찍으면 AI가 오염도를 퍼센트(%)로 측정하고, "기름때 제거를 위해 베이킹소다나 따뜻한 물로 헹구세요" 등 맞춤 행동 요령을 제시.
- **차등 리워드 시스템**: 오염도와 실천 완성도에 따라 등급(A/B/C/F)을 부여하고 에코 포인트를 즉시 지급.
- **아파트 단지 대항전(ESG 게이미피케이션)**: 우리 단지의 분리배출율, 탄소 저감량, 처리비용 절감액을 실시간 집계하여 주민들의 소속감과 참여율 증대.
- **모바일 쿠폰샵 연계**: 적립된 포인트로 배달의민족 할인쿠폰, 편의점 상품권, 커피 기프티콘 등으로 실시간 교환.

---

## 🚀 핵심 기능 (Key Features)

### 1. 📷 AI 멀티모달 비전 스캐너 (`AiScannerScreen.kt`)
- **실시간 카메라 촬영 & 갤러리 업로드**: 직관적인 카메라 인터페이스 및 사진 즉시 미리보기.
- **Google Gemini 3.5 Flash 심층 분석**:
  - 품목 및 재질 식별 (예: 치킨 박스, 마라탕 플라스틱 용기, 페트병 등)
  - 오염도 정밀 수치화 (0% ~ 100%)
  - 세부 액션 플랜 (라벨 제거, 이물질 세척, 뚜껑 분리 등)
- **실천 등급 산정 및 포인트 즉시 지급**:
  - **A등급 (오염도 10% 이하)**: +100P
  - **B등급 (오염도 11~30%)**: +70P
  - **C등급 (오염도 31~60%)**: +40P
  - **F등급 (오염도 60% 초과)**: 재세척 가이드 안내 및 포인트 미지급

### 2. 🏢 우리 단지 대시보드 & 랭킹 (`DashboardScreen.kt`)
- **단지별 실시간 랭킹**: 래미안 에코팰리스, 자이 더 그린 등 전국 아파트 단지 배출 실적 비교.
- **친환경 환경 지표 시각화**:
  - 누적 탄소 저감량 (kg CO₂ 환산)
  - 우리 단지 쓰레기 처리 비용 절감액 (원)
  - 단지별 분리배출 요일(화/목/일) 실시간 캘린더 안내
- **관리자 전용 공지사항 시스템**: 관리사무소 계정으로 로그인 시 단지 공지사항 작성 및 관리.

### 3. 🎁 에코 포인트샵 (`PointShopScreen.kt`)
- **다양한 모바일 제휴 상품**: GS25 편의점권, 배달의민족 할인쿠폰, 스타벅스 아메리카노, CU 상품권 등.
- **원클릭 교환 & 즉시 바코드 발급**: 오프라인 및 앱에서 즉시 사용할 수 있는 바코드 생성 뷰어 및 유효기간 관리.
- **보유 쿠폰함 및 교환 이력 관리**: 사용 완료 / 사용 전 쿠폰 상태 분류.

### 4. 🔐 계정 및 인증 시스템 (`LoginScreen.kt`, `SettingsScreen.kt`)
- **회원가입 / 로그인 탭 전환**:
  - 이메일/비밀번호 정규식 유효성 검사 및 비밀번호 표시/숨김 토글.
  - 거주 아파트 단지 간편 선택.
  - 서비스 이용약관 및 개인정보 처리방침 전문 확인 및 동의.
- **Google 계정 1-Tap 연동 및 비회원 체험 모드**.
- **알림 설정**: 분리배출일 알림, 포인트 적립 알림, 단지 순위 변동 알림 개별 토글.
- **비밀번호 재설정 & 회원 탈퇴/데이터 초기화**.

---

## 🛠 시스템 아키텍처 및 기술 스택 (Architecture & Tech Stack)

```mermaid
graph TD
    A[사용자 UI (Jetpack Compose / M3)] --> B[ViewModel & GlobalState]
    B --> C[Gemini AI Vision Engine (Gemini 3.5 Flash)]
    B --> D[Local SharedPreferences Storage]
    B --> E[Barcode Generator Service]
    C --> F[Google Generative Language API]
```

| 구분 | 기술 스택 | 설명 |
| :--- | :--- | :--- |
| **OS / Platform** | Android (minSdk 24, targetSdk 34) | 최신 모바일 안드로이드 환경 |
| **Language** | Kotlin 1.9+ | 100% Kotlin 기반 코루틴 및 비동기 처리 |
| **UI Framework** | Jetpack Compose & Material 3 | 선언형 UI, 반응형 레이아웃, 다크/라이트 테마 |
| **AI / Vision** | Google Gemini 3.5 Flash API | 멀티모달 비전 프롬프트 엔지니어링 및 JSON 파싱 |
| **Navigation** | AndroidX Navigation Compose | 컴포저블 기반 화면 라우팅 |
| **State & Storage** | Kotlin StateFlow + SharedPreferences | 반응형 상태 관리 및 영구 로컬 세션 캐싱 |
| **Build System** | Gradle (Kotlin DSL - .gradle.kts) | 의존성 및 빌드 라이프사이클 관리 |

---

## 📂 프로젝트 디렉토리 구조 (Project Structure)

```
app/src/main/java/com/example/
├── MainActivity.kt                  # 앱 엔트리포인트 및 Navigation Graph
├── network/
│   ├── GeminiService.kt             # Google Gemini 3.5 Flash 멀티모달 REST 클라이언트
│   └── GeminiModels.kt              # 비전 분석 요청/응답 DTO 및 JSON 파서
├── repository/
│   └── RecycleRepository.kt         # 분리배출 기록, 리워드 및 데이터 동기화 계층
├── ui/
│   ├── components/
│   │   └── AdBanner.kt              # 공익 광고 및 배너 컴포넌트
│   ├── screens/
│   │   ├── AiScannerScreen.kt       # AI 카메라 촬영, 오염도 분석 및 리워드 수령
│   │   ├── DashboardScreen.kt       # 아파트 랭킹, 탄소 저감 통계, 단지 공지
│   │   ├── PointShopScreen.kt       # 포인트 상점, 바코드 쿠폰 교환 및 쿠폰함
│   │   ├── SettingsScreen.kt        # 프로필, 알림 설정, API 키 설정, 약관
│   │   ├── LoginScreen.kt           # 회원가입, 로그인, 약관 동의 다이얼로그
│   │   ├── ApartmentSelectionScreen.kt # 초기 아파트 단지 등록 화면
│   │   └── MainTabScreen.kt         # 하단 4대 탭 네비게이션 컨테이너
│   └── theme/
│       ├── Color.kt                 # 에코 그린 친환경 테마 컬러 팔레트
│       ├── Theme.kt                 # Material 3 테마 설정
│       └── Type.kt                  # 타이포그래피 스타일 정의
└── util/
    ├── GlobalState.kt               # 유저 정보, 포인트, 분리배출 히스토리 전역 상태
    └── BarcodeUtils.kt              # 모바일 쿠폰용 바코드 비트맵 렌더러
```

---

## 💻 설치 및 실행 가이드 (Getting Started)

### 1. 전제 조건 (Prerequisites)
- Android Studio Hedgehog (2023.1.1) 이상 또는 Google AI Studio Build 환경
- JDK 17 이상
- Android SDK 34 (Android 14)
- (선택) 개별 Google Gemini API Key ([Google AI Studio](https://aistudio.google.com/)에서 무료 발급)

### 2. 프로젝트 클론 및 열기
```bash
git clone https://github.com/mjayj9/EcoPick.git
cd EcoPick
```

### 3. API 키 설정 (Environment Configuration)
`local.properties` 또는 프로젝트 루트의 `.env` 파일에 API 키를 등록하거나, 앱 내 **[설정] > [Gemini AI API 설정]** 메뉴에서 직접 입력할 수 있습니다.
```properties
GEMINI_API_KEY=your_gemini_api_key_here
```

### 4. 빌드 및 테스트 실행
```bash
# 디버그 APK 빌드
gradle assembleDebug

# 유닛 및 로보렉트릭 테스트 실행
gradle :app:testDebugUnitTest
```

---

## 👥 팀원 소개 및 역할 (Team & Roles)

**서울대학교사범대학부설중학교 창업동아리 `SNUMS_psg`**

| 이름 | 역할 | 담당 업무 |
| :---: | :---: | :--- |
| **정민재** | **대표 / 메인 개발** | • 프로젝트 총괄 및 안드로이드 아키텍처 설계<br>• Jetpack Compose UI 및 전역 상태 관리 구현<br>• Google Gemini 멀티모달 비전 API 연동 |
| **채희범** | **코어 개발 / QA** | • AI 분석 결과 JSON 파싱 및 오염도 등급 알고리즘 구현<br>• 포인트샵 바코드 발급 엔진 개발 및 빌드 테스트 |
| **김관호** | **기획 / 기술 문서** | • 비즈니스 모델(BM) 수립 및 2026 청소년 창업경진대회 사업계획서 작성<br>• 서비스 이용약관 및 아파트 단지 리워드 정책 기획 |
| **정서준** | **개발 보조 / 리서치** | • 재질별(PP, PS, PET 등) 분리배출 표준 가이드 DB 리서치<br>• 대시보드 통계 지표(탄소 저감량 환산식) 검증 |

---

## 📄 라이선스 (License)

본 프로젝트는 2026 대한민국 청소년 창업경진대회 출품작으로, 저작권은 **SNUMS_psg 팀**에 있습니다.  
Copyright © 2026 SNUMS_psg (Seoul National University Middle School). All rights reserved.
