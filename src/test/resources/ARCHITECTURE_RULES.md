# KOIN API V2 아키텍처 규칙

이 문서는 프로젝트의 아키텍처 규칙을 정의합니다.
모든 규칙은 ArchUnit 테스트를 통해 자동으로 검증됩니다.

---

## 1. 레이어 의존성 규칙

| 규칙 | 설명 |
|------|------|
| Controller → Repository 직접 의존 금지 | Controller는 Service를 통해서만 데이터에 접근해야 한다 |
| Service → Controller 의존 금지 | 하위 레이어는 상위 레이어에 의존하지 않는다 |
| Model(Entity) → Service/Controller 의존 금지 | 도메인 모델은 어떤 레이어에도 의존하지 않는다 |
| `global` → `domain`/`admin` 의존 금지 | 공통 모듈은 비즈니스 모듈에 의존하지 않는다 |
| `common` → `domain`/`admin` 의존 금지 | 공통 모듈은 비즈니스 모듈에 의존하지 않는다 |
| 도메인 간 순환 의존 금지 | 도메인 슬라이스 간 순환 참조를 허용하지 않는다 |

---

## 2. 네이밍 컨벤션 규칙

| 패키지 | 허용 접미사 |
|--------|-----------|
| `*.controller` | `*Controller`, `*Api` |
| `*.repository` | `*Repository`, `*RepositoryImpl` |
| `*.exception` | `*Exception` |

---

## 3. 어노테이션 위치 규칙

| 어노테이션 | 허용 패키지 |
|-----------|-----------|
| `@RestController` | `*.controller` |
| `@Service` | `*.service` |
| `@Entity` | `*.model` |
| `@Configuration` | `global.config`, `infrastructure.*` |

---

## 4. Entity 규칙

| 규칙 | 설명 |
|------|------|
| `BaseEntity` 상속 필수 | 모든 `@Entity` 클래스는 `in.koreatech.koin.common.model.BaseEntity`를 상속해야 한다 |
| `@Setter` 사용 금지 | 엔티티 상태 변경은 비즈니스 메서드를 통해서만 수행한다 |
| `@Data` 사용 금지 | `@Data`는 `@Setter`를 포함하므로 사용하지 않는다 |
| `@Getter` 필수 | 엔티티는 `@Getter`를 선언해야 한다 |
| `@NoArgsConstructor` 필수 | JPA 프록시 생성을 위해 기본 생성자가 필요하다 |

---

## 5. 의존성 주입 규칙

| 규칙 | 설명 |
|------|------|
| `@Autowired` 필드 주입 금지 | 생성자 주입(`@RequiredArgsConstructor`)만 허용한다 |

---

## 6. 예외 규칙

| 규칙 | 설명 |
|------|------|
| `KoinException` 계층 상속 필수 | `*.exception` 패키지의 모든 예외는 `KoinException`을 상속해야 한다 |

---

## 7. Repository 규칙

| 규칙 | 설명 |
|------|------|
| `JpaRepository` 상속 금지 | JPA Repository는 `Repository<T, ID>`를 사용하고 필요한 메서드만 명시적으로 선언한다 |
| `JPAQueryFactory` 사용 위치 제한 | `*RepositoryImpl`, `*QueryRepository`, `QueryDslConfig`에서만 사용 가능하다 |

> Redis Repository는 Spring Data Redis 특성상 `CrudRepository` 사용을 허용한다.

---

## 8. 이벤트 규칙

| 규칙 | 설명 |
|------|------|
| Event 클래스 위치 | `*Event` 클래스는 `..event..` 패키지에 위치해야 한다 |
| EventListener 네이밍 | `*EventListener` 클래스는 이름이 `EventListener`로 끝나야 한다 |
