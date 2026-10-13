---
name: LexIA Design System
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#444651'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#757682'
  outline-variant: '#c5c5d3'
  surface-tint: '#4059aa'
  primary: '#00236f'
  on-primary: '#ffffff'
  primary-container: '#1e3a8a'
  on-primary-container: '#90a8ff'
  inverse-primary: '#b6c4ff'
  secondary: '#4f5e83'
  on-secondary: '#ffffff'
  secondary-container: '#c4d4ff'
  on-secondary-container: '#4c5b80'
  tertiary: '#442100'
  on-tertiary: '#ffffff'
  tertiary-container: '#653400'
  on-tertiary-container: '#fc922b'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dce1ff'
  primary-fixed-dim: '#b6c4ff'
  on-primary-fixed: '#00164e'
  on-primary-fixed-variant: '#264191'
  secondary-fixed: '#d9e2ff'
  secondary-fixed-dim: '#b6c6f1'
  on-secondary-fixed: '#081a3c'
  on-secondary-fixed-variant: '#37466a'
  tertiary-fixed: '#ffdcc3'
  tertiary-fixed-dim: '#ffb77d'
  on-tertiary-fixed: '#2f1500'
  on-tertiary-fixed-variant: '#6e3900'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
  # Validez normativa (Vigente) — en HTML: bg-success-*, text-success-*
  success-50: '#ecfdf5'
  success-100: '#d1fae5'
  success-200: '#a7f3d0'
  success-400: '#34d399'
  success-500: '#10b981'
  success-600: '#059669'
  success-700: '#047857'
  success-800: '#065f46'
  # Acento de inteligencia / síntesis RAG — en HTML: bg-ai-*, text-ai-*
  ai-100: '#fef3c7'
  ai-200: '#fde68a'
  ai-300: '#fcd34d'
  ai-500: '#f59e0b'
  ai-600: '#d97706'
  ai-800: '#92400e'
typography:
  display-md:
    fontFamily: Inter
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 30px
    letterSpacing: 0em
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 26px
    letterSpacing: 0em
  title-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: 0em
  title-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
    letterSpacing: 0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 26px
    letterSpacing: 0.01em
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 22px
    letterSpacing: 0.01em
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: 0.015em
  label-lg:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-md:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.03em
  label-sm:
    fontFamily: Inter
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 12px
    letterSpacing: 0.04em
rounded:
  # Valores del tailwind.config de las pantallas (2xl/3xl = defaults de Tailwind)
  DEFAULT: 0.25rem
  lg: 0.5rem
  xl: 0.75rem
  2xl: 1rem
  3xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system establishes an institutional, authoritative, and technologically advanced foundation for Android (Jetpack Compose / Material 3), engineered specifically for high-precision Mexican statutory analysis, retrieval-augmented generation (RAG) inspection, and dense legal workflows.

### Aesthetic Foundation
- **Modern Institutionalism:** Blends formal constitutional gravitas with state-of-the-art vector intelligence. Visual noise is minimized to convey stability, absolute accuracy, and reliability.
- **Utilitarian Rigor:** Prioritizes structural density, rapid skimming of articles, clear statutory hierarchies, and immediate verification of cited authorities.
- **Restraint and Focus:** Replaces playful or casual motifs with structured container tiers, calibrated contrast ratios, and deliberate metadata delineation.

### Target Audience & Emotional Response
- **Audience:** Mexican attorneys, corporate legal counsel, magistrates, compliance officers, and judicial researchers requiring split-second verification of legal codes, jurisprudence from the SCJN, and semantic document retrieval.
- **Emotional Response:** Inspires absolute legal certainty, procedural discipline, intellectual clarity, and trust in AI-grounded citations without hallucination anxiety.

## Colors

The color palette is built around sovereign institutional tones paired with dedicated functional indicators for semantic retrieval, statutory validation, and confidence indicators.

> **Source of truth:** the YAML frontmatter above. Every hex in this section mirrors a token defined there and in each screen's `tailwind.config`. Refer to colors by token name, never by raw hex.

### Key Tokens & Functional Mapping
- **`primary` (`#00236f`):** Core interactive states, active app-bar accents, selected navigation items, primary CTA fills (`bg-primary`), user chat bubbles, and key statutory headers.
- **`primary-container` (`#1e3a8a`):** Icon tiles, assistant avatar, secondary emphasis fills. Text on it uses `on-primary`.
- **`secondary` (`#4f5e83`):** Supporting labels, category captions, metadata typography.
- **`tertiary` family — Intelligence Gold:** `tertiary-container` (`#653400`) for the leading strip of RAG citation cards; `on-tertiary-container` (`#fc922b`) for similarity scores, citation icons and AI sparks.
- **`ai-*` (`ai-600` = `#d97706`):** Vector/chunk highlights and AI confidence badges in dense inspectors (e.g. `bg-ai-500/10 text-ai-800`).
- **`success-*` (`success-600` = `#059669`, `success-500` = `#10b981`):** Applied exclusively to active legal standing, validity status (*Vigente*), sync indicators and premium verification tags (e.g. `bg-success-500/10 text-success-800`).
- **Neutral Surface Palette:**
  - `surface` / `background`: `#f8f9ff` for the base canvas.
  - `surface-container-lowest`: `#ffffff` for primary content tiles and legal text panes.
  - `surface-container-low`: `#eff4ff` for context bars and grounded citation blocks.
  - `surface-container`: `#e5eeff` for read-only statutory reference blocks, embedding metadata cards, and nested quotes.
  - `surface-container-high` / `-highest`: `#dce9ff` / `#d3e4fe` for chips, pills and tonal buttons.
  - `outline-variant`: `#c5c5d3` for structural dividing borders and container limits.
  - `on-surface`: `#0b1c30` for dense statutory reading.
  - `on-surface-variant`: `#444651` for legal citations, dates, and retrieval timestamps.
  - `outline`: `#757682` for tertiary metadata and unselected control borders.

## Typography

The typographic hierarchy utilizes **Inter** across all roles to maximize micro-legibility in dense statutory text, fractional sub-articles, jurisprudence extracts, and technical similarity score tags.

### Reading & Hierarchy Principles
- **Extended Body Line Heights:** Dense legislative texts require higher breathing room; `body-lg` (26px line height on 16px size) and `body-md` (22px on 14px size) prevent optical fatigue during prolonged reading of constitutional decrees.
- **Tabular Figures & Metrics:** For metadata such as chunk distances, cosine similarities, article counts, and publication dates in the *Diario Oficial de la Federación (DOF)*, numerical data is rendered with OpenType tabular figures (`tnum`).
- **Semantic Badges & Labels:** `label-sm` and `label-md` employ bold weights and positive letter spacing for instant recognition of legal statuses (`VIGENTE`, `ABROGADO`, `REFORMADO`).

## Layout & Spacing

The spatial engine follows a strict 4dp/8dp incremental grid tailored to Android form factors, enforcing structural alignment from mobile viewports to tablet-based split-view legal briefs.

### Grid & Composition Rules
- **Mobile Handsets (<600dp):** Single-column layout with fixed `16dp` (`1rem`) outer margins and vertical rhythms structured around `space-sm` (8dp) and `space-md` (16dp).
- **Tablets & Foldables (≥600dp):** Two-pane adaptive scaffold. Pane A (380dp fixed) holds search prompts, semantic document trees, and embedding filters; Pane B (fluid) renders the comparative statutory viewer and synthesis view. Gutters between panes remain fixed at `24dp` (`1.5rem`).
- **Rhythmic Densities:**
  - `space-xs` (4dp): Gap between status dots and article code labels.
  - `space-sm` (8dp): Padding within chips, inner card badge grouping, and dense item lists.
  - `space-md` (16dp): Standard container padding, list item separations, and input field inner insets.
  - `space-lg` (24dp): Vertical spacing between discrete statutory chapters or jurisprudential theses.
  - `space-xl` (32dp): Major structural section demarcations.

## Elevation & Depth

This design system avoids excessive blur shadows, establishing visual depth via Material 3 tonal surface stacking complemented by razor-thin administrative borders (`outline-variant`).

### Surface Hierarchy
- **Level 0 (Canvas Base):** `surface` (`#f8f9ff`). Houses the primary workspace, background lists, and statutory feeds.
- **Level 1 (Card & Pane Container):** `surface-container-lowest` (`#ffffff`) with optional `1dp` `outline-variant` border. Zero or `shadow-sm` in idle states; conveys clean, tactile paper-like clarity.
- **Level 2 (Active Legal Drawer & Floating Bars):** `surface-container-lowest` (or `surface/90` + backdrop blur for app bars) with ambient tint `rgba(11, 28, 48, 0.06)`, `4dp` blur radius, and `2dp` vertical offset.
- **Level 3 (Modal Citations & Vector Inspector Sheets):** `surface-container-lowest` with ambient tint `rgba(11, 28, 48, 0.12)`, `16dp` blur radius, and `4dp` vertical offset.

### Artificial Intelligence Grounding
RAG citations and retrieved context blocks utilize a flat, non-elevated `surface-container-low` background bordered on the leading edge by a solid `3–6dp` indicator strip (`tertiary-container`, `#653400`), with the similarity score in `on-tertiary-container` (`#fc922b`), instantly separating generative synthesis from original source law without vertical displacement.

## Shapes

The geometry uses a balanced curvature scale (`roundedness: 2`), reflecting precision and solemnity without harsh retro brutalism or frivolous circular motifs.

### Shape Scale Guidelines
- **Badges & tags (`rounded` = `0.25rem` / `4dp`):** Article tag chips, vector similarity tags, validity badges.
- **Small Components (`rounded-lg` = `0.5rem` / `8dp`):** Buttons, text fields, chips, and contextual badges.
- **Context blocks (`rounded-xl` = `0.75rem` / `12dp`):** Context bars, grounded citation cards.
- **Medium Components (`rounded-2xl` = `1rem` / `16dp`):** Statutory article cards, chat bubbles, query synthesis blocks, bottom sheet top corners, and alert dialogs.
- **Large Components (`rounded-3xl` = `1.5rem` / `24dp`):** Primary modal surfaces and full-width legal brief containers.
- **Selection Overlays & Pills (`rounded-full`):** Strictly reserved for floating action indicators and binary validity indicators.

> Note: in this system `rounded-lg` is 8px and `rounded-xl` is 12px (overridden vs. Tailwind defaults).

## Components

Component specifications for implementation in Jetpack Compose following Material Design 3 guidelines:

### Buttons & Actions
- **Primary Action (FilledButton):** `primary` background (`#00236f`), `on-primary` text (`#ffffff`), shape `RoundedCornerShape(8.dp)`. Minimum height `48.dp`. State ripple: `rgba(255, 255, 255, 0.16)`.
- **Secondary Action (OutlinedButton):** Border `1.dp` solid `primary`, text `primary`, background transparent.
- **Jurisprudential CTA (TonalButton):** `surface-container` / `surface-container-highest` background, `primary` text, shape `8.dp`. Used for secondary document actions like "Copiar Criterio" or "Ver Historial DOF".

### Statutory Badges & Chips
- **Article Tag Chip:** Height `24.dp`, shape `4.dp`, `surface-container` background, `1.dp` `outline-variant` border. Typography `label-md` in `on-surface`.
- **Embedding / Vector Score Badge:** Height `20.dp`, shape `4.dp`, `surface-container-highest` (or `ai-500/10`) background, `on-tertiary-container` (or `ai-800`) text. Monospace-formatted decimal displays (e.g., `sim: 0.892`).
- **Validity State Badge (*Vigente*):** Height `22.dp`, shape `4.dp`, `success-500/10` background, `success-800` text, `success-600` icon/dot. Bold uppercase label.

### Statutory Cards & RAG Result Tiles
- **Base Card:** Shape `16.dp`, `surface-container-lowest` background, optional `1.dp` `outline-variant` border, inner padding `16.dp`.
- **RAG Chunk Card:** Highlights retrieved context chunks. Features a left-hand accent strip (`tertiary-container`), `surface-container-low` background, and header container displaying the exact legal citation path (e.g., *CPEUM > Art. 14 > Párrafo II*).

### Input Fields
- **Search & Query Bar (OutlinedTextField):** Shape `8.dp`, `surface-container-lowest` container, `1.dp` `outline-variant` border, focused border `2.dp` `primary`. Text style `body-lg`. Leading search icon in `outline`. Trailing icon indicates semantic vs. literal search toggles.

### Lists & Citations
- **Statute Item Layout:** Two-line or multi-line listing with `12.dp` vertical padding, separated by `1.dp` dividers (`surface-container`). Headline in `title-md` (`on-surface`) with secondary supporting citation in `body-sm` (`on-surface-variant`).

### Interactive Toggles & Radios
- **Checkbox & Radio:** Tinted with `primary` for active selection, `outline` for unselected borders. Used in filtering jurisdictions (Federal, Estatal, Tesis Aisladas, Jurisprudencia).

### Brand Asset Exception
The app icon ([logo](../logo_e_icono_de_aplicacion_lexia/code.html)) keeps its own illustrative gradient palette (`#1e3a8a`, `#d97706`, sky/slate tones) and is not bound to UI tokens. Third-party marks (e.g. Google sign-in) also keep their official colors.