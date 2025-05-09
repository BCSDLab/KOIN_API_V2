# Koin - 한기대 학생들을 위한 단 하나의 서비스

<img src="https://github.com/user-attachments/assets/67226e2d-ae4b-4775-8075-3ec262550373" width="500"/>

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

![Image](https://github.com/user-attachments/assets/0a259345-9ef9-46a5-8979-dd806a4dcdf5)

![Image](https://github.com/user-attachments/assets/0f0fc4a9-4cc3-453c-9180-ca1197055531)

![Image](https://github.com/user-attachments/assets/360d64a7-e9b6-4cf1-b1e4-b14b2fdd7770)

![Image](https://github.com/user-attachments/assets/20013730-9d9a-4721-8280-d228dd578c46)

![Image](https://github.com/user-attachments/assets/f5d843d5-f965-43fb-9b3b-7a0fa155e62f)

![Image](https://github.com/user-attachments/assets/c1dbc16e-caba-45cc-8083-aa7fb108f9db)

![Image](https://github.com/user-attachments/assets/a9b21c01-2473-46bf-92e1-69b44c789641)

![Image](https://github.com/user-attachments/assets/2a50b172-2d94-4c60-a4f3-cf58fd8e80e9)

![Image](https://github.com/user-attachments/assets/0a991cd2-55e5-4ec6-ac72-167b8afb3833)

</details>

# ⚙️ Tech Stack

## 🖥️ Backend
![Image](https://github.com/user-attachments/assets/12e777f1-975a-439b-8701-3efe50f3fb92)

## ☁️ Infra
![Image](https://github.com/user-attachments/assets/ddafef08-6b11-4fa2-af7d-c6693df05c2e)

## 🏗️ Infra Structure
<img width="944" alt="Image" src="https://github.com/user-attachments/assets/7a45ac14-6263-4e78-bf86-fcd86559825e" />

## 📦 Server Instance
<img width="784" alt="Image" src="https://github.com/user-attachments/assets/57f12fc8-ded3-42f4-be88-ac2287d891d2" />

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
