/**
 * LexIA — configuración compartida de Tailwind (CDN).
 * Fuente de verdad: lexia_design_system/DESIGN.md (frontmatter YAML).
 * Cargar DESPUÉS de https://cdn.tailwindcss.com
 */
tailwind.config = {
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        // Primary
        "primary": "#00236f",
        "on-primary": "#ffffff",
        "primary-container": "#1e3a8a",
        "on-primary-container": "#90a8ff",
        "inverse-primary": "#b6c4ff",
        "primary-fixed": "#dce1ff",
        "primary-fixed-dim": "#b6c4ff",
        "on-primary-fixed": "#00164e",
        "on-primary-fixed-variant": "#264191",
        "surface-tint": "#4059aa",

        // Secondary
        "secondary": "#4f5e83",
        "on-secondary": "#ffffff",
        "secondary-container": "#c4d4ff",
        "on-secondary-container": "#4c5b80",
        "secondary-fixed": "#d9e2ff",
        "secondary-fixed-dim": "#b6c6f1",
        "on-secondary-fixed": "#081a3c",
        "on-secondary-fixed-variant": "#37466a",

        // Tertiary (Intelligence Gold)
        "tertiary": "#442100",
        "on-tertiary": "#ffffff",
        "tertiary-container": "#653400",
        "on-tertiary-container": "#fc922b",
        "tertiary-fixed": "#ffdcc3",
        "tertiary-fixed-dim": "#ffb77d",
        "on-tertiary-fixed": "#2f1500",
        "on-tertiary-fixed-variant": "#6e3900",

        // Error
        "error": "#ba1a1a",
        "on-error": "#ffffff",
        "error-container": "#ffdad6",
        "on-error-container": "#93000a",

        // Surfaces & outlines
        "background": "#f8f9ff",
        "on-background": "#0b1c30",
        "surface": "#f8f9ff",
        "surface-dim": "#cbdbf5",
        "surface-bright": "#f8f9ff",
        "surface-container-lowest": "#ffffff",
        "surface-container-low": "#eff4ff",
        "surface-container": "#e5eeff",
        "surface-container-high": "#dce9ff",
        "surface-container-highest": "#d3e4fe",
        "surface-variant": "#d3e4fe",
        "on-surface": "#0b1c30",
        "on-surface-variant": "#444651",
        "inverse-surface": "#213145",
        "inverse-on-surface": "#eaf1ff",
        "outline": "#757682",
        "outline-variant": "#c5c5d3",

        // Validez normativa (Vigente)
        "success": { "50": "#ecfdf5", "100": "#d1fae5", "200": "#a7f3d0", "400": "#34d399", "500": "#10b981", "600": "#059669", "700": "#047857", "800": "#065f46" },
        // Acento IA / síntesis RAG
        "ai": { "100": "#fef3c7", "200": "#fde68a", "300": "#fcd34d", "500": "#f59e0b", "600": "#d97706", "800": "#92400e" }
      },
      borderRadius: {
        "DEFAULT": "0.25rem",
        "lg": "0.5rem",
        "xl": "0.75rem",
        "full": "9999px"
      },
      spacing: {
        "space-xs": "0.25rem",
        "space-sm": "0.5rem",
        "space-md": "1rem",
        "space-lg": "1.5rem",
        "space-xl": "2rem",
        "margin": "1rem",
        "gutter": "1rem"
      },
      fontFamily: {
        "display-md": ["Inter"],
        "headline-lg": ["Inter"],
        "headline-lg-mobile": ["Inter"],
        "headline-md": ["Inter"],
        "headline-sm": ["Inter"],
        "title-lg": ["Inter"],
        "title-md": ["Inter"],
        "body-lg": ["Inter"],
        "body-md": ["Inter"],
        "body-sm": ["Inter"],
        "label-lg": ["Inter"],
        "label-md": ["Inter"],
        "label-sm": ["Inter"]
      },
      fontSize: {
        "display-md": ["40px", { lineHeight: "48px", letterSpacing: "-0.02em", fontWeight: "700" }],
        "headline-lg": ["32px", { lineHeight: "40px", letterSpacing: "-0.01em", fontWeight: "700" }],
        "headline-lg-mobile": ["26px", { lineHeight: "32px", letterSpacing: "-0.01em", fontWeight: "700" }],
        "headline-md": ["24px", { lineHeight: "30px", letterSpacing: "0em", fontWeight: "600" }],
        "headline-sm": ["20px", { lineHeight: "26px", letterSpacing: "0em", fontWeight: "600" }],
        "title-lg": ["18px", { lineHeight: "24px", letterSpacing: "0em", fontWeight: "600" }],
        "title-md": ["16px", { lineHeight: "22px", letterSpacing: "0.01em", fontWeight: "600" }],
        "body-lg": ["16px", { lineHeight: "26px", letterSpacing: "0.01em", fontWeight: "400" }],
        "body-md": ["14px", { lineHeight: "22px", letterSpacing: "0.01em", fontWeight: "400" }],
        "body-sm": ["12px", { lineHeight: "18px", letterSpacing: "0.015em", fontWeight: "400" }],
        "label-lg": ["13px", { lineHeight: "16px", letterSpacing: "0.02em", fontWeight: "600" }],
        "label-md": ["11px", { lineHeight: "14px", letterSpacing: "0.03em", fontWeight: "600" }],
        "label-sm": ["10px", { lineHeight: "12px", letterSpacing: "0.04em", fontWeight: "700" }]
      }
    }
  }
};
