# Koin - 한기대 학생들을 위한 단 하나의 서비스

![Image](https://github.com/user-attachments/assets/75742ede-7e33-435c-9330-c9b6ab232231)

## 👋 Build Communities, Share Dreams - BCSD
안녕하세요! 한국기술교육대학교의 BCSD, BackEnd 트랙에 오신 걸 환영합니다.

코인은 한국기술교육대학교 학생들을 위하여 제공하는 커뮤니티 플랫폼 서비스에요.

한기대 학생들의 편의를 위한 기능들을 분석하고, 개발하며, 서비스를 제공하고 있어요.

BCSD는 현재 3개의 팀(**Campus, Business, User**)으로 나뉘어 각자의 도메인에 집중하는 프로젝트를 진행하고 있어요.

팀 내에서도 9개의 트랙(**BackEnd / FrontEnd / Android / IOS / Design(UI/UX) / DA / PM / Security / Game**) 등 다양한 트랙의 구성원들로 이루어져 각자의 전문성을 살려 기능을 개발하고 있어요.

BCSD는 기술적인 고민 및 해결 방안에 대해 서로 공유하며, 상호협력을 통한 성장을 도모하고 있어요.

BCSD가 추구하는 방향성에 대한 정보는 아래 블로그를 통해 확인할 수 있어요.

> 📝 [BCSD 블로그](https://blog.bcsdlab.com/introduce)

---

어떤 기능을 제공하는지 직접 확인해보세요.

> 👉 [Koin Web 바로가기](https://koreatech.in/)

> 🤖 [Koin App(Android) 설치하기](https://play.google.com/store/apps/details?id=in.koreatech.koin&hl=ko)

> 🍎 [Koin App(IOS) 설치하기](https://apps.apple.com/bh/app/%EC%BD%94%EC%9D%B8-koreatech-in-%ED%95%9C%EA%B8%B0%EB%8C%80-%EC%BB%A4%EB%AE%A4%EB%8B%88%ED%8B%B0/id1500848622)

# ☀️ 주요 기능
<details>
<summary> Koin의 주요 기능을 열어서 확인해보세요!</summary>

<br/>

![Image](https://github.com/user-attachments/assets/6a4dfe70-9168-4023-871f-2c900d5a2ea0)

![Image](https://github.com/user-attachments/assets/a5b51d8a-c47f-4601-9485-0a26986096fb)

![Image](https://github.com/user-attachments/assets/47893383-ba53-4854-b59a-cc1338e3fee6)

![Image](https://github.com/user-attachments/assets/c361d317-a7d5-4219-91d3-6c84beb820c7)

![Image](https://github.com/user-attachments/assets/4a1d6b6a-b773-479d-abb5-035fa46d1aae)

![Image](https://github.com/user-attachments/assets/1f32589f-d688-406a-9af7-efbe9fc70b04)

![Image](https://github.com/user-attachments/assets/0e0be4a5-7025-463d-a95f-16c855af4db3)

![Image](https://github.com/user-attachments/assets/3b058da3-91ec-4d50-baed-734b29799783)

![Image](https://github.com/user-attachments/assets/dddba80d-21e3-416e-8dff-9f443d14f199)

</details>

# ⚙️ Tech Stack

## 🖥️ Backend
![Image](https://github.com/user-attachments/assets/7b409909-051a-4918-9374-29f654cd8ca7)

## ☁️ Infra
![Image](https://github.com/user-attachments/assets/747c5fab-40d0-4b69-881b-ba7335061389)

## 🏗️ Infra Structure
![Image](https://github.com/user-attachments/assets/5727bb18-242b-4edb-930a-cee08a90f2ad)

## 📦 Server Instance
![Image](https://github.com/user-attachments/assets/5bf5c67a-b008-4652-b45e-e7606803df82)

## 🗂️ Repository Structure
```
src
├── main
│   ├── java
│   │   └── in.koreatech.koin
│   │       ├── _common               # 공통 유틸, 예외, 설정
│   │       ├── admin                 # 관리자 기능
│   │       ├── domain                # 핵심 도메인별 기능 분리
│   │       │   └── function
│   │       │       ├── controller
│   │       │       ├── dto
│   │       │       │   ├── request
│   │       │       │   └── response
│   │       │       ├── enums
│   │       │       ├── exception
│   │       │       ├── model
│   │       │       ├── repository
│   │       │       ├── service
│   │       │       └── utils
│   │       ├── infrastructure        # 외부 시스템 연동 (메일, 슬랙 등)
│   │       ├── socket                # WebSocket 관련 코드
│   │       ├── web                   # Web 관련 설정 코드
│   │       └── KoinApplication       
│   └── resources
│       ├── db.migration              # Flyway 기반 마이그레이션
│       ├── mail                      # 이메일 템플릿
│       ├── static.js                 # 정적 JS 파일
│       ├── application.yml           # 환경 설정
│       └── logback-spring.xml        # 로깅 설정
└── test
    ├── java
    │   └── in.koreatech.koin
    │       ├── acceptance            # 사용자 기능 테스트
    │       ├── admin.acceptance      # 관리자 기능 테스트
    │       ├── config                # 테스트 환경 설정
    │       ├── fixture               # 테스트용 더미 데이터
    │       ├── support               # 테스트 헬퍼 및 유틸
    │       ├── util                  # 공통 테스트 유틸
    │       ├── AcceptanceTest        # 테스트 기본 세팅
    │       └── KoinApplicationTest
    └── resources
```

## 🧑‍🧑‍🧒‍🧒 백엔드 구성원들
BCSD에서 열심히 달리고 있는 구성원들을 소개할게요.
| <img src="https://github.com/user-attachments/assets/7f3ce35a-0b5d-4755-b5c3-2baef4a2677f" width="130" height="130"> | <img src="https://github.com/user-attachments/assets/530d5b55-26a0-4b85-ac65-d0da0e5f895a" width="130" height="130"> | <img src="https://github.com/user-attachments/assets/74c64b62-bf71-44d0-b08b-0f0ff0c58840" width="130" height="130"> | <img src="https://github.com/user-attachments/assets/1210988c-4ed0-4759-949c-b435bb81e803" width="130" height="130"> | <img src="https://github.com/user-attachments/assets/25e3bc7a-0c9e-4fca-97f8-6bbfb45079ee" width="130" height="130"> |
| :----------------------------------------------: | :----------------------------------------------: | :----------------------------------------------: | :----------------------------------------------: | :----------------------------------------------: |
|      [진호](https://github.com/BaeJinho4028)       |      [성현](https://github.com/krSeonghyeon)       |      [준기](https://github.com/dradnats1012)       |      [관규](https://github.com/Soundbar91)       |      [성빈](https://github.com/ImTotem)       |

| <img src="https://github.com/user-attachments/assets/d0ae5872-2ae6-45f8-aae8-fa053fa475e5" width="130" height="130"> | <img src="https://github.com/user-attachments/assets/b3373849-7ed8-46a3-81f2-fdae8e52d82a" width="130" height="130"> | <img src="https://github.com/user-attachments/assets/740ba508-ff2a-48e9-8efe-81635dd310ff" width="130" height="130"> | <img src="https://github.com/user-attachments/assets/b217bbd3-9a6e-4262-abd3-46acb1fd3cbd" width="130" height="130"> | <img src="https://github.com/user-attachments/assets/6068f003-71cc-4119-93bf-97c9a27ed256" width="130" height="130"> |
| :----------------------------------------------: | :----------------------------------------------: | :----------------------------------------------: | :----------------------------------------------: | :----------------------------------------------: |
|      [정빈](https://github.com/duehee)       |      [현식](https://github.com/Choon0414)       |      [다희](https://github.com/daheeParkk)       |      [인화](https://github.com/kih1015)       |      [두현](https://github.com/DHkimgit)      |
