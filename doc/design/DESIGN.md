# 디자인 시스템 규칙 (DESIGN.md)

모든 사이드 프로젝트에 공통으로 적용하는 UI·색상 기준입니다.
GitHub의 정돈된 개발자 도구 톤 + 초록 계열 브랜드 액센트.
라이트/다크 두 모드를 모두 지원합니다.

> **에이전트에게**: 색상·간격·radius는 반드시 `platform_style.css`의 CSS 변수를 사용하세요.
> hex 값을 코드에 직접 박지 말고 `var(--accent)` 처럼 참조합니다.
> 다크 모드는 `<html data-theme="dark">` 로 전환되며, 변수만 바뀌면 전체가 따라옵니다.

---

## 1. 색상 (Color)

### 시맨틱 토큰 (의미 기반 — 모드에 따라 자동 전환)
| 토큰 | 라이트 | 다크 | 용도 |
|---|---|---|---|
| `--canvas` | `#ffffff` | `#0d1117` | 기본 배경 |
| `--canvas-subtle` | `#f6f8fa` | `#161b22` | 섹션/카드 배경 |
| `--canvas-inset` | `#eef1f4` | `#010409` | 움푹 들어간 영역 |
| `--border` | `#d0d7de` | `#30363d` | 기본 테두리 |
| `--border-muted` | `#e4e8ec` | `#21262d` | 약한 구분선 |
| `--fg` | `#1f2328` | `#e6edf3` | 본문 텍스트 |
| `--fg-muted` | `#59636e` | `#9198a1` | 보조 텍스트 |
| `--fg-subtle` | `#818b98` | `#6e7681` | 캡션/플레이스홀더 |
| `--accent` | `#1a7f37` | `#3fb950` | 링크/강조 텍스트·아이콘 |
| `--accent-emphasis` | `#1f883d` | `#238636` | Primary 버튼 배경 |
| `--accent-subtle` | `#dafbe1` | `rgba(46,160,67,.18)` | 액센트 배경/포커스 링 |
| `--danger` | `#cf222e` | `#f85149` | 오류/위험 |

### 브랜드 그린 스케일 (고정 참조값)
`0 #dafbe1 · 1 #aceebb · 2 #6fdd8b · 3 #4ac26b · 4 #2da44e · 5 #1a7f37 · 6 #116329 · 7 #044f1e · 8 #003d16 · 9 #002d11`
→ 라이트 기본 액센트는 **5**, 다크는 **3**.

### 중립 그레이 스케일 (고정 참조값)
`0 #f6f8fa · 1 #eaeef2 · 2 #d0d7de · 3 #afb8c1 · 4 #8c959f · 5 #6e7781 · 6 #57606a · 7 #424a53 · 8 #24292f · 9 #0d1117`

**규칙**
- 채도 높은 색을 새로 만들지 말 것. 위 팔레트 안에서 해결.
- 흰/검은 배경에도 살짝 청회색 톤을 유지 (순수 #000/#fff 단독 사용 지양).

---

## 2. 타이포그래피 (Typography)

- **폰트**: 시스템 산세리프 스택 — `var(--font-sans)`. 코드/값은 `var(--font-mono)`.
- 본문 16px 기준, 제목은 음수 자간으로 밀도를 높입니다.

| 역할 | size / weight / letter-spacing |
|---|---|
| Display | 40 / 700 / -0.025em |
| Heading 1 | 32 / 600 / -0.02em |
| Heading 2 | 24 / 600 / -0.02em |
| Heading 3 | 20 / 600 |
| Body | 16 / 400 |
| Small | 14 / 400 |
| Caption | 12 / 400 |

---

## 3. 간격 & 모서리 (Spacing & Radius)

- **4px 베이스 그리드**: `--space-xs 4` · `sm 8` · `md 16` · `lg 24` · `xl 32`
- 요소 간격은 가능한 한 **flex/grid + `gap`** 으로 처리 (margin 남발 금지).
- **Radius**: `--radius 6px`(버튼/인풋 기본) · `--radius-lg 8px`(카드) · `--radius-pill 2em`(뱃지).

---

## 4. 컴포넌트 (Components)

### 버튼
- 기본 높이 32px, `padding: 5px 16px`, `radius: var(--radius)`, `font-weight: 500`.
- **Primary**: `background: var(--accent-emphasis)`, 텍스트 `#fff`, hover `var(--btn-primary-hover)`.
- **Default**: `background: var(--btn-bg)`, `border: 1px solid var(--btn-border)`, hover `var(--btn-hover)`.
- **Danger**: 평소 텍스트 `var(--danger)`, hover 시 배경 `var(--danger)` + 텍스트 `#fff`.
- 크기: small(`3px 12px`/12) · medium(기본) · large(`9px 20px`/16, radius 8).

### 입력 필드 (Input / Select / Textarea)
- `background: var(--canvas)`, `border: 1px solid var(--border)`, `radius: var(--radius)`, `padding: 8px 12px`.
- **포커스**: `border-color: var(--accent)` + `box-shadow: 0 0 0 3px var(--accent-subtle)`. (그린 외곽 링)

### 뱃지 / 라벨
- pill 형태(`radius: var(--radius-pill)`), 12px / weight 500.
- 상태형(Active): `--accent-subtle` 배경 + `--accent` 테두리·텍스트 + 작은 점.
- 솔리드(New): `--accent-emphasis` 배경 + 흰 텍스트.
- 카운터: `--canvas-inset` 배경 + `--border` 테두리.

### 카드
- `background: var(--canvas)`, `border: 1px solid var(--border)`, `radius: var(--radius-lg)`, `box-shadow: var(--shadow)`.
- 헤더 · 본문 · 푸터 3단 구조 권장. 구분선은 `--border-muted`.

---

## 5. 전환 (Theming)

- 다크 모드: 루트 요소에 `data-theme="dark"` 토글.
- 부드러운 전환을 원하면 배경/색/테두리에 `transition: ... .25s ease`.
- 새 색이 필요하면 hex를 박지 말고 **먼저 토큰을 추가**한 뒤 변수로 참조.

---

## 6. 하지 말 것 (Don't)

- ❌ 토큰 밖의 임의 색·그라데이션 남용
- ❌ hex/px 하드코딩 (변수 우선)
- ❌ 과한 라운드·이모지·불필요한 아이콘/통계 (미니멀 지향)
- ❌ Inter/Roboto 등 흔한 폰트로 교체 (시스템 스택 유지)
