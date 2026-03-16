# AI Act Compliance Platform — Project Specification

## Project Overview

**Product Name:** AIAudit (working title)
**Domain:** EU AI Act (Regulation EU 2024/1689) compliance SaaS platform
**Target Market:** European SMEs, financial institutions, ICT providers, and their advisors (law firms, audit companies)
**Primary Region:** Baltics (Estonia, Latvia, Lithuania), Nordics (Finland), Poland
**Founder:** Solo full-stack developer, 5 years Spring Boot/Angular experience, also runs DoraAudit.eu (DORA/NIS2 compliance platform)

---

## Regulatory Context

The EU AI Act is the world's first comprehensive AI regulation. Key enforcement dates:

- **February 2025:** Prohibited AI practices banned
- **August 2025:** GPAI model obligations in effect, penalties active
- **2 August 2026:** HIGH-RISK AI system obligations fully enforceable (Annex III), transparency rules (Article 50), national AI sandboxes required — **THIS IS THE PRIMARY DEADLINE WE ARE BUILDING TOWARD**
- **August 2027:** Full scope including product-embedded AI (Annex I / Article 6(1))
- **December 2030:** Legacy public sector and large-scale IT system deadline

Penalties: Up to €35M or 7% of global annual turnover.

The AI Act classifies AI systems into 4 risk tiers:
1. **Unacceptable Risk** — Banned (social scoring, manipulative AI, untargeted facial scraping, emotion recognition in workplace/education)
2. **High Risk** — Strict requirements (Annex III: biometrics, critical infrastructure, education, employment, essential services, law enforcement, migration, justice, democratic processes)
3. **Limited Risk** — Transparency obligations (chatbots, emotion recognition, deepfakes, biometric categorization)
4. **Minimal Risk** — No specific obligations (spam filters, AI in video games)

Key obligations for high-risk AI systems:
- Risk Management System (Article 9)
- Data and Data Governance (Article 10)
- Technical Documentation (Article 11)
- Record-Keeping / Logging (Article 12)
- Transparency and Information to Deployers (Article 13)
- Human Oversight (Article 14)
- Accuracy, Robustness and Cybersecurity (Article 15)
- Quality Management System (Article 17)
- Conformity Assessment (Article 43)
- EU Database Registration (Article 49)
- Fundamental Rights Impact Assessment — FRIA (Article 27, for deployers)
- Post-Market Monitoring (Article 72)
- Serious Incident Reporting (Article 73)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21 + Spring Boot 3.x |
| Frontend | Angular 18+ (standalone components, signals) |
| Database | PostgreSQL 16 |
| Auth | Spring Security + JWT (access + refresh tokens) |
| Containerization | Docker + Docker Compose |
| Reverse Proxy | Caddy |
| Infrastructure | Contabo VPS (production + dev), Ubuntu |
| Email | Resend (transactional) |
| Payments | LemonSqueezy |
| AI/LLM | Anthropic Claude API (document generation, classification assistance) |
| Search/SEO | SSR prerendering for Angular SPA (learned from DoraAudit SEO issues) |

### Architecture Principles

- Monolith-first (no microservices — solo developer, keep it simple)
- Server-side subscription enforcement (learned from DoraAudit)
- RESTful API with clear separation: `/api/v1/` prefix
- Angular lazy-loaded feature modules
- Docker Compose for local dev, single Docker host for production
- Caddy auto-TLS with Let's Encrypt
- Database migrations via Flyway
- All business logic in service layer, thin controllers

---

## Data Model — Core Entities

### Organization
```
organization
├── id (UUID)
├── name
├── registration_code (business registry number)
├── country (ISO 3166-1)
├── sector (NACE code)
├── employee_count_range (MICRO/SMALL/MEDIUM/LARGE)
├── subscription_plan (STARTER/PROFESSIONAL/ENTERPRISE)
├── subscription_status
├── created_at
└── updated_at
```

### AI System Registry
```
ai_system
├── id (UUID)
├── organization_id (FK)
├── name
├── description
├── purpose (intended use)
├── ai_type (MACHINE_LEARNING/DEEP_LEARNING/NLP/COMPUTER_VISION/EXPERT_SYSTEM/GENERATIVE/OTHER)
├── deployment_status (DEVELOPMENT/TESTING/PRODUCTION/RETIRED)
├── deployment_date
├── provider_name (if third-party)
├── provider_type (INTERNAL/THIRD_PARTY/OPEN_SOURCE)
├── role (PROVIDER/DEPLOYER/IMPORTER/DISTRIBUTOR)
├── risk_classification (UNACCEPTABLE/HIGH/LIMITED/MINIMAL/UNCLASSIFIED)
├── risk_classification_method (AUTO/MANUAL/OVERRIDE)
├── risk_classification_date
├── risk_classification_rationale (text)
├── annex_iii_category (nullable — which of the 8 Annex III areas)
├── uses_personal_data (boolean)
├── uses_biometric_data (boolean)
├── affects_fundamental_rights (boolean)
├── is_safety_component (boolean)
├── gpai_model_id (nullable — link to GPAI model if applicable)
├── compliance_status (NOT_STARTED/IN_PROGRESS/COMPLIANT/NON_COMPLIANT)
├── compliance_score (0-100)
├── next_review_date
├── created_at
├── updated_at
└── created_by (FK user)
```

### Compliance Obligation
```
compliance_obligation
├── id (UUID)
├── ai_system_id (FK)
├── obligation_type (enum — maps to AI Act articles)
│   ├── RISK_MANAGEMENT_SYSTEM (Art. 9)
│   ├── DATA_GOVERNANCE (Art. 10)
│   ├── TECHNICAL_DOCUMENTATION (Art. 11)
│   ├── RECORD_KEEPING (Art. 12)
│   ├── TRANSPARENCY (Art. 13)
│   ├── HUMAN_OVERSIGHT (Art. 14)
│   ├── ACCURACY_ROBUSTNESS_CYBERSECURITY (Art. 15)
│   ├── QUALITY_MANAGEMENT (Art. 17)
│   ├── CONFORMITY_ASSESSMENT (Art. 43)
│   ├── EU_DATABASE_REGISTRATION (Art. 49)
│   ├── FRIA (Art. 27)
│   ├── POST_MARKET_MONITORING (Art. 72)
│   └── INCIDENT_REPORTING (Art. 73)
├── status (NOT_STARTED/IN_PROGRESS/COMPLETED/NOT_APPLICABLE)
├── evidence_description (text)
├── evidence_file_ids (array of document references)
├── assigned_to (FK user, nullable)
├── due_date
├── completed_date (nullable)
├── notes
├── created_at
└── updated_at
```

### Risk Classification Questionnaire
```
classification_question
├── id (UUID)
├── question_text (i18n key)
├── question_group (ANNEX_III_CHECK/PROHIBITED_CHECK/SAFETY_COMPONENT/PRODUCT_REGULATION)
├── answer_type (YES_NO/SINGLE_CHOICE/MULTI_CHOICE/TEXT)
├── options (JSON, nullable)
├── order_index
├── depends_on_question_id (nullable — conditional logic)
├── depends_on_answer (nullable)
├── weight (for scoring)
└── help_text (i18n key)

classification_response
├── id (UUID)
├── ai_system_id (FK)
├── question_id (FK)
├── answer (text/JSON)
├── answered_by (FK user)
└── answered_at
```

### Document Generation
```
generated_document
├── id (UUID)
├── ai_system_id (FK)
├── document_type (FRIA/TECHNICAL_DOC/RISK_MANAGEMENT_PLAN/HUMAN_OVERSIGHT_PROTOCOL/CONFORMITY_DECLARATION/DATA_GOVERNANCE_PLAN)
├── title
├── content (text/HTML — the generated document body)
├── template_version
├── generation_method (AI_ASSISTED/MANUAL/TEMPLATE)
├── status (DRAFT/REVIEW/APPROVED/ARCHIVED)
├── approved_by (FK user, nullable)
├── approved_at (nullable)
├── file_path (nullable — exported PDF/DOCX)
├── created_at
└── updated_at
```

### Compliance Alert
```
compliance_alert
├── id (UUID)
├── title
├── description
├── source_url
├── source_type (EU_OFFICIAL/STANDARD_BODY/NATIONAL_AUTHORITY/GUIDELINE)
├── alert_type (NEW_REGULATION/GUIDELINE_UPDATE/STANDARD_PUBLISHED/DEADLINE_REMINDER/ENFORCEMENT_ACTION)
├── severity (INFO/WARNING/CRITICAL)
├── relevant_risk_levels (array: HIGH/LIMITED/etc.)
├── relevant_sectors (array of NACE codes, nullable)
├── relevant_countries (array of ISO codes, nullable)
├── published_date
├── effective_date (nullable)
├── created_at
└── is_active (boolean)
```

### GPAI Model (for General Purpose AI providers)
```
gpai_model
├── id (UUID)
├── organization_id (FK)
├── model_name
├── model_version
├── is_systemic_risk (boolean — based on compute threshold >10^25 FLOPS or Commission designation)
├── training_data_summary_status (DRAFT/PUBLISHED)
├── copyright_policy_status (COMPLIANT/NON_COMPLIANT/IN_PROGRESS)
├── technical_doc_status
├── downstream_provider_info_status
├── created_at
└── updated_at
```

### User & Roles
```
app_user
├── id (UUID)
├── organization_id (FK)
├── email
├── password_hash
├── first_name
├── last_name
├── role (OWNER/ADMIN/COMPLIANCE_MANAGER/VIEWER)
├── language_preference (et/en/fi/lv/lt/pl)
├── is_active
├── last_login_at
├── created_at
└── updated_at
```

---

## Feature Modules

### Module 1: AI System Registry (MVP — Phase 1)
**Priority: CRITICAL — Build first**

Wizard-style flow for registering AI systems:
1. Basic info (name, purpose, type, provider)
2. Deployment context (sector, affected persons, data types)
3. Role determination (are you provider, deployer, importer, distributor?)
4. Auto-classification questions → risk level output
5. Summary + dashboard card

The registry is the foundation — everything else connects to it.

**API Endpoints:**
```
POST   /api/v1/ai-systems                    — Create new AI system
GET    /api/v1/ai-systems                    — List org's AI systems (paginated, filterable)
GET    /api/v1/ai-systems/{id}               — Get AI system details
PUT    /api/v1/ai-systems/{id}               — Update AI system
DELETE /api/v1/ai-systems/{id}               — Soft delete
POST   /api/v1/ai-systems/{id}/classify      — Run/re-run classification
GET    /api/v1/ai-systems/{id}/obligations    — Get obligations for this system
GET    /api/v1/ai-systems/summary             — Dashboard summary stats
```

### Module 2: Risk Classification Engine (MVP — Phase 1)
**Priority: CRITICAL**

Implements the AI Act's risk classification logic:

**Prohibited AI check (Article 5):**
- Social scoring by public authorities?
- Real-time remote biometric identification in public spaces (with exceptions)?
- Subliminal manipulation techniques?
- Exploitation of vulnerabilities of specific groups?
- Untargeted scraping of facial images?
- Emotion recognition in workplace/education?
- Biometric categorization inferring sensitive attributes?
- Predictive policing based solely on profiling?

**High-risk check (Annex III areas):**
1. Biometrics (remote identification, categorization)
2. Critical infrastructure (energy, water, transport, digital)
3. Education and vocational training (admissions, assessment)
4. Employment (recruitment, task allocation, termination)
5. Essential private/public services (credit scoring, insurance, social benefits)
6. Law enforcement (risk assessment, polygraph, evidence analysis)
7. Migration, asylum, border control
8. Administration of justice and democratic processes

**High-risk check (Article 6(1) — product safety):**
- Is the AI system a safety component of a product covered by EU harmonization legislation?
- Does the product require third-party conformity assessment?

**Limited risk check (Article 50):**
- Interacts directly with natural persons (chatbot)?
- Generates synthetic content (deepfakes, AI-generated text/images)?
- Uses emotion recognition or biometric categorization?

**Classification output:**
```json
{
  "risk_level": "HIGH",
  "confidence": "HIGH",
  "rationale": "System falls under Annex III, Area 5 (essential services) — used for credit scoring decisions affecting natural persons.",
  "applicable_articles": [9, 10, 11, 12, 13, 14, 15, 16, 17, 27, 43, 49, 72, 73],
  "obligations_count": 14,
  "recommended_actions": [
    "Implement risk management system per Article 9",
    "Prepare technical documentation per Article 11",
    "Conduct Fundamental Rights Impact Assessment per Article 27"
  ],
  "deadline": "2026-08-02"
}
```

### Module 3: Compliance Dashboard (MVP — Phase 1)
**Priority: CRITICAL**

Organization-level view:
- Total AI systems count by risk level (donut chart)
- Overall compliance score (0-100%)
- Upcoming deadlines (timeline)
- Obligations by status (NOT_STARTED / IN_PROGRESS / COMPLETED)
- Recent alerts
- Quick actions

Per AI system view:
- Compliance checklist (Articles 9-17 + relevant others)
- Status per obligation with progress indicator
- Document links
- Assigned team members
- Audit trail / history

### Module 4: Document Generation (Phase 2)
**Priority: HIGH**

AI-assisted document generation using Claude API. For each document type, the system:
1. Collects structured input from user (wizard/form)
2. Combines with AI system registry data
3. Sends to Claude API with specialized prompt + template
4. Returns structured document for user review and editing
5. Exports as PDF/DOCX

**Document types:**
- Fundamental Rights Impact Assessment (FRIA) — Article 27
- Technical Documentation — Article 11 + Annex IV
- Risk Management System description — Article 9
- Human Oversight Protocol — Article 14
- Data Governance Plan — Article 10
- Conformity Declaration — Article 47
- Post-Market Monitoring Plan — Article 72
- Transparency Notice (for limited-risk systems) — Article 50

**Claude API integration:**
```java
// Service: DocumentGenerationService
// Use Claude claude-sonnet-4-20250514 for cost efficiency
// System prompt includes:
//   - AI Act article text (relevant sections)
//   - Template structure
//   - Organization context from registry
//   - Specific AI system details
// Temperature: 0.3 (factual, structured output)
// Max tokens: 4096
// Output: Structured markdown → convert to PDF/DOCX
```

### Module 5: Compliance Alert System (Phase 2)
**Priority: HIGH — key differentiator from DoraAudit experience**

Monitors and alerts on:
- New EU AI Office guidelines and codes of practice
- CEN-CENELEC AI standard publications (still being developed)
- National competent authority announcements (per country)
- Enforcement actions and fines (learn from others' mistakes)
- Deadline reminders (customized per org's AI systems)

Implementation:
- Admin-curated alerts (manual initially, like DoraAudit)
- Email digest (weekly/monthly per user preference)
- In-app notification center
- Filterable by risk level, sector, country

### Module 6: GPAI Model Compliance (Phase 2)
**Priority: MEDIUM**

For organizations that provide or fine-tune General Purpose AI models:
- Training data summary documentation (Article 53)
- Copyright policy compliance tracking
- Acceptable use policy template
- Downstream provider information requirements
- Systemic risk assessment (for models >10^25 FLOPS)
- Code of Practice alignment checklist

### Module 7: Multi-Regulation View (Phase 3)
**Priority: MEDIUM — major upsell for existing DoraAudit customers**

Unified dashboard showing compliance across:
- EU AI Act
- DORA (if financial sector)
- NIS2 (if essential/important entity)
- GDPR (data protection overlaps)

Cross-regulation dependency mapping — e.g., AI Act Article 15 (cybersecurity) overlaps with DORA ICT risk management and NIS2 security measures.

---

## Pricing

| Plan | Price | Limits | Features |
|------|-------|--------|----------|
| **Starter** | €49/month | 5 AI systems, 1 user | Registry, classification, basic dashboard, checklist |
| **Professional** | €149/month | 20 AI systems, 5 users | + Document generation, alerts, task assignment, export |
| **Enterprise** | €399/month | Unlimited systems & users | + Multi-regulation view, API access, custom templates, white-label option |

**One-time products:**
- AI Act Readiness Assessment — €299 (automated gap analysis report — lead magnet / first revenue)
- AI Act Training Module — €99 per user (AI literacy requirement, Article 4)

Payment via LemonSqueezy (already integrated for other products).

---

## i18n / Localization

**Languages (priority order):**
1. English (default, international)
2. Estonian (home market)
3. Finnish (Nordic expansion)
4. Latvian (Baltic)
5. Lithuanian (Baltic)
6. Polish (large market, many SMEs)

All user-facing strings via Angular i18n or ngx-translate.
Backend error messages and email templates also i18n.
AI Act article references always link to official EUR-Lex source.

---

## SEO & Marketing Strategy

**Lessons from DoraAudit (avoid same mistakes):**
- Implement SSR prerendering from day 1 (DoraAudit had only homepage indexed)
- Google Search Console setup immediately
- Blog section with static HTML/prerendered pages
- Focus content on long-tail keywords: "AI Act compliance Estonia", "AI Act risk classification tool", "FRIA template EU AI Act"

**Content plan:**
- "AI Act impact on Estonian financial sector" (blog)
- "How to classify your AI systems under the EU AI Act" (blog + free tool)
- "AI Act vs DORA: overlapping obligations" (blog — cross-sell DoraAudit)
- "AI literacy requirements for your team" (blog — upsell training module)

**Free tools (lead generation):**
- Public AI Act Risk Classifier (simplified version, email-gated results)
- AI Act Obligation Checker (enter sector + AI type → see obligations)
- AI Act Deadline Calculator (when do YOUR obligations kick in?)

---

## API Design Patterns

### Authentication
```
POST /api/v1/auth/register        — Organization + admin user registration
POST /api/v1/auth/login           — JWT access token + refresh token
POST /api/v1/auth/refresh         — Refresh access token
POST /api/v1/auth/forgot-password — Send reset email via Resend
POST /api/v1/auth/reset-password  — Reset with token
```

### Standard Response Envelope
```json
{
  "success": true,
  "data": { ... },
  "meta": {
    "page": 1,
    "pageSize": 20,
    "totalElements": 42,
    "totalPages": 3
  },
  "errors": []
}
```

### Error Response
```json
{
  "success": false,
  "data": null,
  "errors": [
    {
      "code": "VALIDATION_ERROR",
      "field": "name",
      "message": "AI system name is required"
    }
  ]
}
```

### Subscription Enforcement (Server-Side)
```java
// Interceptor checks subscription plan limits before processing requests
// e.g., Starter plan: max 5 AI systems
// Returns 403 with upgrade prompt if limit exceeded
// NEVER trust frontend for plan enforcement (lesson from DoraAudit)
```

---

## Development Workflow

### Multi-Agent Cursor Setup
Use the established multi-agent workflow:
- **Architect Agent** — High-level design decisions, data model changes, API contracts
- **Backend Agent** — Spring Boot services, repositories, controllers, migrations
- **Frontend Agent** — Angular components, services, routing, UI
- **QA Agent** — Test writing, edge case identification, security review

### Claude Code Sessions
When using Claude Code for autonomous development:
1. Always reference this document for context
2. Follow existing code patterns from the codebase
3. Create Flyway migration files for any DB changes (V{number}__{description}.sql)
4. Write tests for new service methods
5. Use Angular standalone components with signals
6. Follow existing i18n patterns
7. Server-side subscription checks on all plan-limited endpoints

### Git Conventions
- `main` — production-ready
- `dev` — integration branch
- Feature branches: `feature/ai-system-registry`, `feature/risk-classifier`
- Commit messages: conventional commits (`feat:`, `fix:`, `refactor:`, `docs:`)

---

## Infrastructure

### Production
- Contabo VPS (reuse existing or provision new)
- Docker Compose: postgres + spring-boot-app + caddy
- Caddy config: reverse proxy + auto-TLS
- Backups: daily PostgreSQL pg_dump to external storage

### Domain
- Target: aiaudit.eu or similar (.eu TLD for EU market credibility)
- DNS: Zone.ee (existing provider)
- Email: Resend for transactional, custom domain

---

## Development Phases & Timeline

### Phase 1: MVP (Weeks 1-6) — Launch before August 2026 panic
- [ ] Project scaffolding (Spring Boot + Angular + Docker Compose)
- [ ] Auth system (register, login, JWT, roles)
- [ ] Organization management
- [ ] AI System Registry (CRUD + wizard)
- [ ] Risk Classification Engine (questionnaire + logic)
- [ ] Compliance Dashboard (org-level + per-system)
- [ ] Compliance checklist per AI system
- [ ] Basic subscription management (LemonSqueezy webhook)
- [ ] Landing page + SEO setup
- [ ] Free public Risk Classifier (lead magnet)
- [ ] i18n: English + Estonian

### Phase 2: Differentiators (Weeks 7-12)
- [ ] Document Generation (Claude API integration)
- [ ] FRIA template + generator
- [ ] Technical Documentation template + generator
- [ ] Compliance Alert System (admin-curated + email digest)
- [ ] Task assignment and workflow
- [ ] GPAI model compliance module
- [ ] Export (PDF/DOCX reports)
- [ ] i18n: Finnish + Latvian

### Phase 3: Scale (Months 4-6)
- [ ] Multi-regulation view (AI Act + DORA + GDPR)
- [ ] White-label partner portal
- [ ] API for partners
- [ ] AI literacy training module
- [ ] i18n: Lithuanian + Polish
- [ ] Advanced analytics and reporting

---

## Key Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Product looks like "demo" (DoraAudit feedback) | Loss of credibility | Advisory board member with AI governance expertise. Professional UI design. Customer testimonials early. |
| Enterprise platforms (OneTrust, Vanta) add AI Act support | Competitive pressure | Speed, EU-focus, local languages, lower pricing, niche depth > breadth |
| August 2026 deadline postponed (Digital Omnibus) | Reduced urgency | Backstop dates still exist (Dec 2027). Firms must prepare anyway. Pivot messaging to "be ready before competitors." |
| Solo developer burnout | Slower development | Prioritize ruthlessly. MVP first. Use Claude Code for acceleration. Consider contractor for frontend. |
| Low initial traffic (DoraAudit lesson) | Slow growth | Partner channel first (law firms, auditors). Free tools for organic SEO. Paid LinkedIn ads for specific segments. |

---

## Competitive Landscape

| Competitor | Focus | Weakness (our opportunity) |
|-----------|-------|--------------------------|
| AIComply (ai-comply.app) | EU AI Act for SMEs | Generic, no localization, no multi-regulation |
| OneTrust | Privacy + AI governance | Expensive, enterprise-only, not AI Act-specialized |
| Vanta / Drata / Sprinto | SOC 2, ISO 27001 + AI | GRC-first, AI Act is add-on, not core |
| FairNow | AI governance platform | US-centric, not compliance-workflow focused |
| EU AI Act Compliance Checker (FLI) | Free classification tool | Static questionnaire, not a SaaS platform |
| DIGITAL SME Conformity Tool | Free assessment | No ongoing monitoring, no document generation |

**Our positioning:** Purpose-built AI Act compliance for European SMEs, with local language support, affordable pricing, and multi-regulation view (AI Act + DORA + GDPR) that no competitor offers.

---

## Notes for Claude Code

- This is a greenfield project — scaffold from scratch
- Reuse architectural patterns from DoraAudit where applicable
- The risk classification logic is the core IP — implement it thoroughly with unit tests
- Document generation prompts should be stored as configurable templates, not hardcoded
- All AI Act article references should include article number + title for clarity
- Plan for the classification logic to evolve as CEN-CENELEC standards are published
- The free public classifier should work WITHOUT authentication (separate Angular route, no JWT required)
- Mobile-responsive from day 1 (many compliance officers work on tablets/phones)
