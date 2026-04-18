##   Koin - 한기대 학생들의 필수 앱 (KoreaTech IN)

![Image](https://github.com/user-attachments/assets/75742ede-7e33-435c-9330-c9b6ab232231)

> [!IMPORTANT]   
> 한국기술교육대학교의 학식, 주변 식당, 버스, 시간표, 공지사항 등 필수 정보를 제공하는   
> **DAU 1,100명 이상**의 **Java / Spring** 기반 커뮤니티 서비스입니다.   

> 현재 3개의 팀(**Campus, Business, User**)으로 나뉘어,   
> 여러 직무의 팀원들과 각자 전문성을 살려 프로덕트를 개발하고 있습니다.   
> (BackEnd / FrontEnd / Android / iOS / Design / DA / PM / Security)

---

### ⚙️ Tech Stack

#### 🖥️ Backend
![Image](https://github.com/user-attachments/assets/7b409909-051a-4918-9374-29f654cd8ca7)

#### ☁️ Infra
![Image](https://github.com/user-attachments/assets/747c5fab-40d0-4b69-881b-ba7335061389)

#### 🏗️ Infra Structure
![Image](https://github.com/user-attachments/assets/5727bb18-242b-4edb-930a-cee08a90f2ad)

#### 📦 Server Instance
![Image](https://github.com/user-attachments/assets/5bf5c67a-b008-4652-b45e-e7606803df82)

---

### ☀️ 주요 기능
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

---

### 🗂️ Package Structure
```
src
├── main
│   ├── java
│   │   └── in.koreatech.koin
│   │       ├── admin                 # 관리자 기능
│   │       ├── common                # 도메인 간 공용 클래스
│   │       ├── domain                # 핵심 도메인별 기능 분리
│   │       │   ├── User
│   │       │   │   ├── controller
│   │       │   │   ├── dto
│   │       │   │   │   ├── Request.java
│   │       │   │   │   └── Response.java
│   │       │   │   ├── enums
│   │       │   │   ├── model
│   │       │   │   ├── repository
│   │       │   │   ├── service
│   │       │   │   └── utils
│   │       │   └── ...
│   │       ├── global                # 전역 설정, 예외, 필터 등 시스템 구조
│   │       ├── infrastructure        # 외부 시스템 연동 (메일, 슬랙 등)
│   │       ├── socket                # 웹소켓 관련 코드
│   │       └── KoinApplication.java      
│   └── resources
│       ├── db.migration              # Flyway 기반 마이그레이션
│       ├── mail                      # 이메일 템플릿
│       ├── static.js                 # 정적 JS 파일
│       ├── application.yml           # 환경 설정
│       └── logback-spring.xml        # 로깅 설정
└── test
    ├── java
    │   └── in.koreatech.koin
    │       ├── acceptance            # 인수 테스트
    │       ├── config                # 테스트 환경 설정
    │       └── unit                  # 유닛 테스트
    └── resources
```

---

### 🧑‍🧑‍🧒‍🧒 Team Members

| <img src="https://github.com/user-attachments/assets/7f3ce35a-0b5d-4755-b5c3-2baef4a2677f" width="130"> | <img src="https://github.com/user-attachments/assets/1210988c-4ed0-4759-949c-b435bb81e803" width="130"> | <img src="https://github.com/user-attachments/assets/25e3bc7a-0c9e-4fca-97f8-6bbfb45079ee" width="130"> | <img src="https://github.com/user-attachments/assets/b217bbd3-9a6e-4262-abd3-46acb1fd3cbd" width="130"> |
| :--: | :--: | :--: | :--: |
| [진호](https://github.com/BaeJinho4028) | [관규](https://github.com/Soundbar91) | [성빈](https://github.com/ImTotem) | [인화](https://github.com/kih1015) |

| <img src="https://github.com/user-attachments/assets/6068f003-71cc-4119-93bf-97c9a27ed256" width="130"> | <img src="https://github.com/taejinn.png" width="130"> | <img src="https://github.com/dh2906.png" width="130"> | <img src="https://github.com/JanooGwan.png" width="130"> |
| :--: | :--: | :--: | :--: |
| [두현](https://github.com/DHkimgit) | [박태진](https://github.com/taejinn) | [이동훈](https://github.com/dh2906) | [관우](https://github.com/JanooGwan) |
   
   
--- 

> [!TIP]   
> #### BCSD 동아리 정보와 App 설치는 아래에서 확인할 수 있습니다.   
> 📝 [BCSD 블로그](https://blog.bcsdlab.com/introduce)   
> 🤖 [Koin App(Android) 설치하기](https://play.google.com/store/apps/details?id=in.koreatech.koin&hl=ko)   
> 🍎 [Koin App(IOS) 설치하기](https://apps.apple.com/bh/app/%EC%BD%94%EC%9D%B8-koreatech-in-%ED%95%9C%EA%B8%B0%EB%8C%80-%EC%BB%A4%EB%AE%A4%EB%8B%88%ED%8B%B0/id1500848622)   
> 👉 [Koin Web 바로가기](https://koreatech.in/)
