# 디자인 시스템 규칙 (DESIGN.md)

모든 사이드 프로젝트에 공통으로 적용하는 UI 기준입니다.

이 프로젝트의 기본 디자인 시스템은 **Okerry UI**입니다.
버튼, 입력창, 카드, 레이아웃, 텍스트, 뱃지, 알림 등 공통 UI는 직접 새로 만들지 않고 **Okerry UI의 클래스와 CSS 변수**를 우선 사용합니다.

Okerry UI에 없는 프로젝트 전용 스타일만 별도의 `platform_style.css`에서 추가합니다.

---

## 1. 기본 원칙

이 프로젝트의 모든 디자인은 다음 우선순위를 따릅니다.

```txt
1순위: Okerry UI 클래스 사용
2순위: Okerry UI CSS 변수 사용
3순위: platform_style.css에 프로젝트 전용 클래스 추가
4순위: 임의 스타일 작성 금지
```

즉, 버튼이 필요하면 직접 CSS를 만들지 않고 먼저 `okerry-btn` 계열 클래스를 사용합니다.

```html
<button class="okerry-btn okerry-btn-primary">저장</button>
```

카드가 필요하면 직접 `.card`를 만들지 않고 `okerry-card`를 사용합니다.

```html
<div class="okerry-card">
  ...
</div>
```

입력창이 필요하면 직접 input 스타일을 만들지 않고 `okerry-input`을 사용합니다.

```html
<input class="okerry-input" type="text">
```

---

## 2. CSS 파일 사용 방식

모든 프로젝트는 기본적으로 Okerry UI CDN을 먼저 불러옵니다.

```html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/youngheonchoi/okerry_ui@main/concept/default/v1.0.0/okerry-ui.css">
```

프로젝트 전용 스타일이 필요한 경우, Okerry UI 다음에 `platform_style.css`를 불러옵니다.

순서는 반드시 다음과 같이 유지합니다.

```txt
1. okerry-ui-버전.css
2. 프로젝트 전용 platform_style.css
```

`platform_style.css`는 Okerry UI를 덮어쓰거나 확장하기 위한 파일입니다.

---

## 3. Okerry UI 사용 원칙

공통 UI는 반드시 Okerry UI 클래스를 우선 사용합니다.

### 버튼

버튼은 `okerry-btn` 계열 클래스를 사용합니다.

```html
<button class="okerry-btn okerry-btn-primary">저장</button>
<button class="okerry-btn okerry-btn-secondary">취소</button>
<button class="okerry-btn okerry-btn-danger">삭제</button>
<button class="okerry-btn okerry-btn-ghost">닫기</button>
```

버튼 스타일을 새로 만들지 않습니다.

나쁜 예:

```css
.save-button {
  background: #1a7f37;
  border-radius: 6px;
  padding: 8px 16px;
}
```

좋은 예:

```html
<button class="okerry-btn okerry-btn-primary">저장</button>
```

---

### 입력 필드

입력창, textarea, select는 Okerry UI 입력 클래스를 사용합니다.

```html
<div class="okerry-field">
  <label class="okerry-label">이메일</label>
  <input class="okerry-input" type="email" placeholder="email@example.com">
  <p class="okerry-help-text">가입한 이메일을 입력하세요.</p>
</div>
```

에러 상태는 다음처럼 사용합니다.

```html
<div class="okerry-field">
  <label class="okerry-label">비밀번호</label>
  <input class="okerry-input okerry-input-error" type="password">
  <p class="okerry-error-text">비밀번호를 입력해주세요.</p>
</div>
```

---

### 카드

카드는 `okerry-card` 계열 클래스를 사용합니다.

```html
<div class="okerry-card">
  <div class="okerry-card-header">
    <h2 class="okerry-card-title">프로젝트</h2>
    <p class="okerry-card-description">최근 작업한 프로젝트입니다.</p>
  </div>

  <div class="okerry-card-body">
    카드 내용
  </div>

  <div class="okerry-card-footer">
    <button class="okerry-btn okerry-btn-primary">확인</button>
  </div>
</div>
```

---

### 레이아웃

페이지 구조는 Okerry UI 레이아웃 클래스를 우선 사용합니다.

```html
<main class="okerry-container">
  <section class="okerry-section">
    <div class="okerry-stack">
      ...
    </div>
  </section>
</main>
```

자주 사용하는 레이아웃 클래스:

```txt
okerry-container
okerry-section
okerry-stack
okerry-row
okerry-between
okerry-center
okerry-grid
okerry-grid-2
okerry-grid-3
```

레이아웃 간격은 가능하면 `margin`을 직접 쓰지 않고 `okerry-stack`, `okerry-row`, `okerry-grid`의 `gap` 기반 구조를 사용합니다.

---

### 텍스트

제목, 본문, 보조 텍스트는 Okerry UI 텍스트 클래스를 사용합니다.

```html
<h1 class="okerry-title-2xl">대시보드</h1>
<h2 class="okerry-title-xl">최근 프로젝트</h2>
<p class="okerry-text">본문 내용입니다.</p>
<p class="okerry-text-muted">보조 설명입니다.</p>
<span class="okerry-caption">마지막 수정: 오늘</span>
```

---

### 뱃지

상태 표시에는 Okerry UI 뱃지를 사용합니다.

```html
<span class="okerry-badge okerry-badge-primary">New</span>
<span class="okerry-badge okerry-badge-success">완료</span>
<span class="okerry-badge okerry-badge-warning">대기중</span>
<span class="okerry-badge okerry-badge-danger">실패</span>
```

---

### 알림

안내 메시지, 성공 메시지, 경고 메시지, 오류 메시지는 Okerry UI 알림 클래스를 사용합니다.

```html
<div class="okerry-alert okerry-alert-info">
  새로운 업데이트가 있습니다.
</div>

<div class="okerry-alert okerry-alert-danger">
  오류가 발생했습니다.
</div>
```

---

## 4. 프로젝트 전용 platform_style.css 작성 규칙

Okerry UI에 없는 스타일이 필요한 경우에만 `platform_style.css`를 작성합니다.

예를 들어 특정 프로젝트에만 필요한 hero 영역, dashboard chart wrapper, profile layout 같은 스타일은 `platform_style.css`에 추가할 수 있습니다.

```css
.project-hero {
  padding: var(--okerry-space-2xl) 0;
  background: var(--okerry-color-canvas-subtle);
}

.project-dashboard-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: var(--okerry-space-lg);
}
```

단, `platform_style.css`에서도 다음 규칙을 지켜야 합니다.

* 색상은 Okerry UI CSS 변수를 사용한다.
* 간격은 Okerry UI CSS 변수를 사용한다.
* radius는 Okerry UI CSS 변수를 사용한다.
* shadow는 Okerry UI CSS 변수를 사용한다.
* 버튼, 입력창, 카드 같은 공통 컴포넌트를 다시 만들지 않는다.
* 프로젝트 전용 클래스는 의미가 드러나게 작성한다.
* Bootstrap처럼 일반적인 `.btn`, `.card`, `.container` 클래스명은 사용하지 않는다.

---

## 5. platform_style.css에서 허용되는 것

`platform_style.css`에서는 다음과 같은 프로젝트 전용 스타일만 허용합니다.

### 페이지 전용 레이아웃

```css
.project-page {
  min-height: 100vh;
  background: var(--okerry-color-bg);
}
```

### 특정 화면 전용 섹션

```css
.project-hero {
  padding: var(--okerry-space-2xl) 0;
}
```

### 특정 도메인 UI

```css
.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--okerry-space-md);
  padding: var(--okerry-space-md);
  border: 1px solid var(--okerry-color-border);
  border-radius: var(--okerry-radius-lg);
  background: var(--okerry-color-surface);
}
```

### Okerry UI 조합을 위한 래퍼

```css
.login-card-wrap {
  max-width: 420px;
  margin: 0 auto;
}
```

---

## 6. style.css에서 금지되는 것

다음과 같은 스타일은 작성하지 않습니다.

### 버튼 재정의 금지

나쁜 예:

```css
.my-button {
  background: #1a7f37;
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
}
```

좋은 예:

```html
<button class="okerry-btn okerry-btn-primary">저장</button>
```

---

### 카드 재정의 금지

나쁜 예:

```css
.custom-card {
  background: white;
  border: 1px solid #d0d7de;
  border-radius: 8px;
  padding: 24px;
}
```

좋은 예:

```html
<div class="okerry-card">
  ...
</div>
```

---

### input 재정의 금지

나쁜 예:

```css
.search-input {
  border: 1px solid #d0d7de;
  border-radius: 6px;
  padding: 8px 12px;
}
```

좋은 예:

```html
<input class="okerry-input" type="search">
```

---

### 임의 색상값 금지

나쁜 예:

```css
.box {
  background: #f6f8fa;
  color: #1f2328;
}
```

좋은 예:

```css
.box {
  background: var(--okerry-color-canvas-subtle);
  color: var(--okerry-color-text);
}
```

---

## 7. CSS 변수 사용 규칙

Okerry UI에서 제공하는 CSS 변수를 우선 사용합니다.

예시:

```css
.custom-section {
  background: var(--okerry-color-bg);
  color: var(--okerry-color-text);
  border: 1px solid var(--okerry-color-border);
  border-radius: var(--okerry-radius-lg);
  padding: var(--okerry-space-lg);
}
```

프로젝트에서 새 색상이나 간격이 필요하더라도 바로 hex 값을 쓰지 않습니다.

먼저 다음 순서로 판단합니다.

```txt
1. Okerry UI 변수로 해결 가능한가?
2. 기존 변수 조합으로 해결 가능한가?
3. 정말 프로젝트 전용 변수가 필요한가?
4. 필요하다면 platform_style.css 상단에 프로젝트 전용 변수로 추가한다.
```

프로젝트 전용 변수가 꼭 필요한 경우:

```css
:root {
  --project-sidebar-width: 260px;
}
```

---

## 8. 다크 모드 규칙

Okerry UI는 라이트 모드와 다크 모드를 지원합니다.

다크 모드는 루트 요소에 다음 속성을 붙여 전환합니다.

```html
<html data-theme="dark">
```

라이트 모드는 기본값입니다.

```html
<html>
```

또는 명시적으로 사용할 수 있습니다.

```html
<html data-theme="light">
```

다크 모드를 직접 색상값으로 구현하지 않습니다.

나쁜 예:

```css
.dark-card {
  background: #0d1117;
  color: #e6edf3;
}
```

좋은 예:

```css
.project-panel {
  background: var(--okerry-color-surface);
  color: var(--okerry-color-text);
}
```

변수만 사용하면 다크 모드에서 자동으로 색상이 바뀌어야 합니다.

---

## 9. HTML / Thymeleaf / JSP 사용 규칙

Okerry UI는 CDN `<link>` 태그로 불러옵니다.

### 일반 HTML

```html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/youngheonchoi/okerry_ui@main/concept/default/v1.0.0/okerry-ui.css">
```

### Thymeleaf

```html
<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/youngheonchoi/okerry_ui@main/concept/default/v1.0.0/okerry-ui.css">
```

---

## 10. AI 작업 규칙

AI는 이 프로젝트에서 다음 규칙을 반드시 따른다.

* 모든 기본 UI는 Okerry UI를 기준으로 작성한다.
* 버튼은 `okerry-btn` 계열 클래스를 사용한다.
* 입력창은 `okerry-input`, `okerry-field`, `okerry-label` 계열 클래스를 사용한다.
* 카드는 `okerry-card` 계열 클래스를 사용한다.
* 레이아웃은 `okerry-container`, `okerry-section`, `okerry-stack`, `okerry-row`, `okerry-grid` 계열 클래스를 우선 사용한다.
* 텍스트는 `okerry-title-*`, `okerry-text`, `okerry-text-muted`, `okerry-caption`을 우선 사용한다.
* 뱃지와 알림은 Okerry UI 클래스를 사용한다.
* Okerry UI에 없는 프로젝트 전용 스타일만 `platform_style.css`에 작성한다.
* `platform_style.css`에서도 색상, 간격, radius, shadow는 Okerry UI 변수를 사용한다.
* hex 색상값을 직접 작성하지 않는다.
* 공통 컴포넌트를 `platform_style.css`에서 다시 만들지 않는다.
* 인라인 스타일을 사용하지 않는다.
* Bootstrap식 일반 클래스명 `.btn`, `.card`, `.container`, `.row`를 새로 정의하지 않는다.
* 필요한 경우 프로젝트 전용 클래스명은 기능이나 화면 의미가 드러나게 작성한다.

---

## 11. 권장 작업 방식

새 화면을 만들 때는 다음 순서로 작업합니다.

```txt
1. Okerry UI CDN을 연결한다.
2. 기본 레이아웃을 okerry-container, okerry-section으로 만든다.
3. 화면 구조를 okerry-stack, okerry-row, okerry-grid로 잡는다.
4. 버튼, 입력창, 카드, 텍스트는 Okerry UI 클래스를 사용한다.
5. 부족한 부분만 platform_style.css에 프로젝트 전용 클래스로 추가한다.
6. platform_style.css에서도 Okerry UI 변수를 사용한다.
```

---

## 12. 예시 화면 구조

```html
<main class="okerry-container">
  <section class="okerry-section">
    <div class="okerry-card">
      <div class="okerry-card-header">
        <h1 class="okerry-card-title">로그인</h1>
        <p class="okerry-card-description">계정 정보를 입력해주세요.</p>
      </div>

      <div class="okerry-card-body">
        <div class="okerry-stack">
          <div class="okerry-field">
            <label class="okerry-label">이메일</label>
            <input class="okerry-input" type="email">
          </div>

          <div class="okerry-field">
            <label class="okerry-label">비밀번호</label>
            <input class="okerry-input" type="password">
          </div>

          <button class="okerry-btn okerry-btn-primary okerry-btn-full">
            로그인
          </button>
        </div>
      </div>
    </div>
  </section>
</main>
```

프로젝트 전용으로 카드 너비만 조정해야 한다면 `platform_style.css`에 다음 정도만 추가합니다.

```css
.login-card {
  max-width: 420px;
  margin: 0 auto;
}
```

그리고 HTML에서는 이렇게 사용합니다.

```html
<div class="okerry-card login-card">
  ...
</div>
```

---

## 13. 하지 말 것

* Okerry UI에 있는 버튼, 카드, input을 다시 만들지 말 것
* hex 색상값을 직접 쓰지 말 것
* 인라인 스타일을 쓰지 말 것
* 페이지마다 버튼 크기, radius, 색상을 다르게 만들지 말 것
* 공통 UI를 프로젝트마다 새로 디자인하지 말 것
* 다크 모드 색상을 개별 클래스에 직접 박지 말 것
* 불필요하게 많은 커스텀 CSS를 만들지 말 것
* `platform_style.css`를 두 번째 디자인 시스템처럼 만들지 말 것

---

## 14. 핵심 요약

이 프로젝트의 디자인 기준은 Okerry UI입니다.

Okerry UI에 있는 것은 그대로 사용합니다.
Okerry UI에 없는 프로젝트 전용 스타일만 `platform_style.css`에 추가합니다.
`platform_style.css`에서도 색상, 간격, radius, shadow는 Okerry UI 변수를 사용합니다.

목표는 AI가 매번 다른 디자인을 만들지 않게 하고, 모든 사이드 프로젝트의 UI를 같은 기준으로 유지하는 것입니다.
