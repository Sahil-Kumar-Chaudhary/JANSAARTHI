import os
import shutil
from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, Image, KeepTogether, PageBreak
)
from reportlab.pdfgen import canvas

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super().showPage()
        super().save()

    def draw_page_decorations(self, page_count):
        self.saveState()
        self.setFont("Helvetica", 8)
        self.setFillColor(colors.HexColor("#64748B"))
        
        # Only draw header/footer on page 2 onwards
        if self._pageNumber > 1:
            # Clean Academic Header
            self.drawRightString(550, 808, "JANSAARTHI AI — SIH 2026 (Problem Statement ID: SIH26092)")
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(45, 802, 550, 802)
            
            # Clean Academic Footer
            self.setStrokeColor(colors.HexColor("#CBD5E1"))
            self.setLineWidth(0.5)
            self.line(45, 42, 550, 42)
            self.drawString(45, 30, "Project Progress Report — JANSAARTHI AI — Internal Academic Evaluation")
            page_text = f"Page {self._pageNumber} of {page_count}"
            self.drawRightString(550, 30, page_text)
            
        self.restoreState()

def build_pdf():
    out_pdf = "JANSAARTHI_REPORT/JANSAARTHI_Professional_Progress_Report.pdf"
    
    # 595.27 x 841.89 pt (A4). Margins: 45 pt -> Usable width = 505.27 pt, Usable height = 750 pt
    doc = SimpleDocTemplate(
        out_pdf,
        pagesize=A4,
        leftMargin=45,
        rightMargin=45,
        topMargin=46,
        bottomMargin=46
    )
    
    styles = getSampleStyleSheet()
    
    # Simple, Academic Neutral Color Palette
    navy = colors.HexColor("#0F2942")        # Academic Deep Navy
    dark_text = colors.HexColor("#1A202C")   # Charcoal Text
    slate_sub = colors.HexColor("#475569")   # Neutral Slate Gray
    border_gray = colors.HexColor("#CBD5E1") # Clean Table Border
    th_bg = colors.HexColor("#F1F5F9")       # Subtle Light Slate Header
    alt_bg = colors.HexColor("#F8FAFC")      # Subtle Clean Row Alternate
    card_bg = colors.HexColor("#F8FAFC")     # Clean Subtle Card Background
    success_green = colors.HexColor("#15803D")
    pending_gray = colors.HexColor("#64748B")
    
    # Simple, Academic Typography Styles
    title_style = ParagraphStyle(
        'DocTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=20,
        leading=24,
        textColor=navy,
        alignment=1,
        spaceAfter=2
    )
    
    subtitle_style = ParagraphStyle(
        'DocSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=10,
        leading=13.5,
        textColor=slate_sub,
        alignment=1,
        spaceAfter=8
    )
    
    super_style = ParagraphStyle(
        'DocSuper',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=9.5,
        leading=12.5,
        textColor=slate_sub,
        alignment=1,
        spaceAfter=2
    )
    
    h1_style = ParagraphStyle(
        'Heading1_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=12,
        leading=15,
        textColor=navy,
        spaceBefore=11,
        spaceAfter=4,
        keepWithNext=True
    )
    
    h2_style = ParagraphStyle(
        'Heading2_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=9.5,
        leading=13,
        textColor=navy,
        spaceBefore=7,
        spaceAfter=3,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'Body_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8.2,
        leading=11.2,
        textColor=dark_text,
        spaceAfter=4
    )
    
    bullet_style = ParagraphStyle(
        'Bullet_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=8,
        leading=11,
        textColor=dark_text,
        leftIndent=12,
        spaceAfter=2.5
    )
    
    caption_style = ParagraphStyle(
        'Caption_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Oblique',
        fontSize=7.2,
        leading=9.5,
        textColor=slate_sub,
        alignment=1,
        spaceBefore=2,
        spaceAfter=4
    )
    
    th_style = ParagraphStyle(
        'TH_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=7.5,
        leading=9.5,
        textColor=navy,
        alignment=0
    )
    
    th_center = ParagraphStyle(
        'TH_Center',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=7.5,
        leading=9.5,
        textColor=navy,
        alignment=1
    )

    td_style = ParagraphStyle(
        'TD_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=7.2,
        leading=9.2,
        textColor=dark_text
    )

    td_bold = ParagraphStyle(
        'TD_Bold',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=7.2,
        leading=9.2,
        textColor=navy
    )

    td_center = ParagraphStyle(
        'TD_Center',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=7.2,
        leading=9.2,
        textColor=dark_text,
        alignment=1
    )

    story = []

    def p(text, bold_prefix="", space_after=4):
        txt = f"<b>{bold_prefix}</b>{text}" if bold_prefix else text
        return Paragraph(txt, ParagraphStyle('B', parent=body_style, spaceAfter=space_after))

    def bullet(text, bold_prefix="", space_after=2.5):
        txt = f"• <b>{bold_prefix}</b>{text}" if bold_prefix else f"• {text}"
        return Paragraph(txt, ParagraphStyle('BL', parent=bullet_style, spaceAfter=space_after))

    # ====================================================
    # COVER / HEADER (Clean Academic Style)
    # ====================================================
    story.append(Paragraph("SMART INDIA HACKATHON 2026", super_style))
    story.append(Paragraph("PROJECT PROGRESS REPORT", super_style))
    story.append(Spacer(1, 2))
    story.append(Paragraph("JANSAARTHI AI", title_style))
    story.append(Paragraph("AI-Driven Scheme Matching for Marginalized Entrepreneurs<br/><i>\"Aapki Zarurat. Sahi Sarkari Raasta.\"</i>", subtitle_style))
    story.append(Spacer(1, 2))

    # Metadata Box Table
    meta_data = [
        [Paragraph("<b>Problem Statement ID:</b>", td_bold), Paragraph("SIH26092", td_style)],
        [Paragraph("<b>Project Title:</b>", td_bold), Paragraph("AI-Driven Scheme Matching for Marginalized Entrepreneurs", td_style)],
        [Paragraph("<b>Project Repository:</b>", td_bold), Paragraph("https://github.com/Sahil-Kumar-Chaudhary/JANSAARTHI", td_style)],
        [Paragraph("<b>Evaluation Purpose:</b>", td_bold), Paragraph("Internal College Academic Progress, Source Implementation & Technical Milestone Review", td_style)]
    ]
    t_meta = Table(meta_data, colWidths=[130, 375])
    t_meta.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,-1), th_bg),
        ('BACKGROUND', (1,0), (1,-1), colors.white),
        ('BOX', (0,0), (-1,-1), 0.75, border_gray),
        ('INNERGRID', (0,0), (-1,-1), 0.5, border_gray),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(t_meta)
    story.append(Spacer(1, 6))

    # ====================================================
    # SECTION 1: TEAM OVERVIEW & INDIVIDUAL RESPONSIBILITIES
    # ====================================================
    story.append(Paragraph("1. Project Team Members & Assigned Responsibilities", h1_style))
    story.append(p("The following 5-member student engineering team is assigned to the JANSAARTHI AI project for Smart India Hackathon 2026. The individual contributions and responsibilities are detailed below:", bold_prefix="Team Roster: "))
    
    headers = [Paragraph("Member", th_center), Paragraph("Student Name", th_style), Paragraph("Reg. Number", th_center), Paragraph("Assigned Responsibility", th_style), Paragraph("Status", th_center)]
    team_data = [headers]
    members_data = [
        ("Member 1", "Tanya Patel", "12305225", "Backend Development, API Integration, Cloud Deployment and DevOps Support", "Completed"),
        ("Member 2", "Sahil Kumar Chaudhary", "12305507", "Android Application Development, Jetpack Compose UI/UX, Navigation & Auth UI, Profile Management, Backend Integration & Localization", "Completed"),
        ("Member 3", "Sunny", "12312528", "Retrieval-Augmented Generation (RAG) Research and Knowledge Retrieval", "In Progress"),
        ("Member 4", "Gourav Saini", "12306706", "Government Scheme Research and Eligibility Criteria Analysis", "In Progress"),
        ("Member 5", "Disha", "12310869", "Financial Assistance and Citizen Requirement Analysis", "In Progress"),
    ]
    for i, m in enumerate(members_data):
        st_color = success_green if m[4] == "Completed" else pending_gray
        team_data.append([
            Paragraph(f"<b>{m[0]}</b>", td_center),
            Paragraph(f"<b>{m[1]}</b>" if i in [0, 1] else m[1], td_bold if i in [0, 1] else td_style),
            Paragraph(m[2], td_center),
            Paragraph(m[3], td_style),
            Paragraph(f"<font color='{st_color.hexval()}'><b>{m[4]}</b></font>", td_center)
        ])
    t_team = Table(team_data, colWidths=[45, 125, 62, 208, 65])
    t_team.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), th_bg),
        ('BOX', (0,0), (-1,-1), 0.75, border_gray),
        ('INNERGRID', (0,0), (-1,-1), 0.5, border_gray),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
        ('LEFTPADDING', (0,0), (-1,-1), 4),
        ('RIGHTPADDING', (0,0), (-1,-1), 4),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, alt_bg])
    ]))
    story.append(t_team)
    story.append(Spacer(1, 5))

    # MEMBER 1: TANYA PATEL
    story.append(Paragraph("Member 1 — Backend Development, System Integration & DevOps", h2_style))
    m1_content = [
        Paragraph("<b>Student Name:</b> Tanya Patel &nbsp;&nbsp;|&nbsp;&nbsp; <b>Registration Number:</b> 12305225 &nbsp;&nbsp;|&nbsp;&nbsp; <b>Role:</b> Backend & DevOps", ParagraphStyle('M1H', parent=body_style, fontSize=7.8, textColor=navy, spaceAfter=2)),
        Paragraph("<b>Assigned Responsibility:</b> Backend Development, API Integration, Cloud Deployment and DevOps Support", ParagraphStyle('M1R', parent=body_style, fontSize=7.2, spaceAfter=2)),
        Paragraph("<b>Work Completed / Contributions:</b>", ParagraphStyle('M1W', parent=body_style, fontSize=7.2, spaceAfter=1))
    ]
    m1_items = [
        "Assisted in understanding and designing the backend architecture for the JANSAARTHI AI platform.",
        "Worked on REST API integration between the application frontend and backend services.",
        "Contributed to backend data management and MongoDB database integration.",
        "Explored secure API communication, authentication workflows and data validation.",
        "Worked on containerization and deployment strategies using Docker.",
        "Explored CI/CD pipeline implementation for automated application build and deployment.",
        "Assisted in identifying deployment issues, debugging and improving application reliability.",
        "Contributed to technical documentation and overall system integration."
    ]
    for it in m1_items:
        m1_content.append(Paragraph(f"• {it}", ParagraphStyle('M1I', parent=bullet_style, fontSize=7, leading=9.2, spaceAfter=1.5)))
    t_m1 = Table([[m1_content]], colWidths=[505])
    t_m1.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), card_bg),
        ('BOX', (0,0), (-1,-1), 0.75, border_gray),
        ('TOPPADDING', (0,0), (-1,-1), 3.5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3.5),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(t_m1)
    story.append(Spacer(1, 4))

    # MEMBER 2: SAHIL KUMAR CHAUDHARY
    story.append(Paragraph("Member 2 — Android Application Development & Integration", h2_style))
    m2_content = [
        Paragraph("<b>Student Name:</b> Sahil Kumar Chaudhary &nbsp;&nbsp;|&nbsp;&nbsp; <b>Registration Number:</b> 12305507 &nbsp;&nbsp;|&nbsp;&nbsp; <b>Role:</b> Android Lead", ParagraphStyle('M2H', parent=body_style, fontSize=7.8, textColor=navy, spaceAfter=2.5))
    ]
    m2_items = [
        ("Android Native Application Architecture: ", "Engineered 100% native Kotlin client using Jetpack Compose, Material 3 design tokens, Single-Activity architecture (MainActivity.kt), and Lifecycle-aware MVVM ViewModels."),
        ("Design System & UI Components: ", "Designed and implemented the complete application UI system with responsive layouts, consistent typography, and custom theme tokens in Color.kt and Theme.kt."),
        ("Multi-Step Registration Engine: ", "Implemented 4-step sequential registration (Introduction, Basic Details, Additional Details, Create Password) in RegisterScreen.kt with persistent state (RegistrationFormState.kt)."),
        ("Navigation Shell & AuthGuard: ", "Configured Navigation Compose with custom AuthGuard composables intercepting unauthenticated access across all private routes."),
        ("Home Dashboard & Quick Services: ", "Engineered personalized greeting header, user profile avatar, AI assistant card with RAG badge, 2x2 Quick Services grid, and PM Mudra recommendation card."),
        ("Profile Management & Image Upload: ", "Built ProfileScreen.kt and ProfileSubScreens.kt with dynamic editing of demographics, category, and business details, alongside ImageUtils.kt for photo compression and Base64 sync."),
        ("Localization & Theming: ", "Implemented bilingual English/Hindi resources (Strings.kt / values-hi) and system-wide Light and Dark MaterialTheme support."),
        ("Dedicated Notification Hub: ", "Created NotificationScreen.kt featuring unread counters, category filter chips (All, Unread, Schemes), mark-as-read toggles, and direct deep-linking into scheme recommendations."),
        ("Backend & Cloud Integration: ", "Connected Retrofit HTTP client with Node.js/Express backend, JWT token management via TokenManager.kt, and staging verification on Render."),
        ("Testing & Physical Device Verification: ", "Authored and executed 28 unit tests (100% passing) and verified edge-to-edge UI responsiveness on physical Android device (OPPO CPH2333, Android 13).")
    ]
    for pref, desc in m2_items:
        m2_content.append(Paragraph(f"• <b>{pref}</b>{desc}", ParagraphStyle('M2B', parent=bullet_style, fontSize=7, leading=9.2, spaceAfter=1.5)))
    
    t_m2 = Table([[m2_content]], colWidths=[505])
    t_m2.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), card_bg),
        ('BOX', (0,0), (-1,-1), 0.75, border_gray),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(t_m2)
    story.append(Spacer(1, 4))

    # MEMBER 3: SUNNY
    story.append(Paragraph("Member 3 — RAG Research & Preliminary Development", h2_style))
    m3_content = [
        Paragraph("<b>Student Name:</b> Sunny &nbsp;&nbsp;|&nbsp;&nbsp; <b>Registration Number:</b> 12312528", ParagraphStyle('M3H', parent=body_style, fontSize=7.8, textColor=navy, spaceAfter=2)),
        Paragraph("<b>Assigned Responsibility:</b> Retrieval-Augmented Generation (RAG) Research and Knowledge Retrieval", ParagraphStyle('M3R', parent=body_style, fontSize=7.2, spaceAfter=2)),
        Paragraph("<b>Work Completed:</b>", ParagraphStyle('M3W', parent=body_style, fontSize=7.2, spaceAfter=1))
    ]
    m3_items = [
        "Studied the working principles and architecture of Retrieval-Augmented Generation (RAG).",
        "Explored the use of government scheme documents as a knowledge source.",
        "Researched how relevant information can be retrieved from scheme documents.",
        "Worked on the initial RAG-based information retrieval component.",
        "Explored how retrieved scheme information can support accurate responses to citizen queries.",
        "Assisted in understanding the role of RAG in JANSAARTHI's scheme assistance system."
    ]
    for it in m3_items:
        m3_content.append(Paragraph(f"• {it}", ParagraphStyle('M3I', parent=bullet_style, fontSize=7, leading=9.2, spaceAfter=1.5)))
    t_m3 = Table([[m3_content]], colWidths=[505])
    t_m3.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.white),
        ('BOX', (0,0), (-1,-1), 0.75, border_gray),
        ('TOPPADDING', (0,0), (-1,-1), 3.5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3.5),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(t_m3)
    story.append(Spacer(1, 4))

    # MEMBER 4: GOURAV SAINI
    story.append(Paragraph("Member 4 — Government Scheme Research & Eligibility", h2_style))
    m4_content = [
        Paragraph("<b>Student Name:</b> Gourav Saini &nbsp;&nbsp;|&nbsp;&nbsp; <b>Registration Number:</b> 12306706", ParagraphStyle('M4H', parent=body_style, fontSize=7.8, textColor=navy, spaceAfter=2)),
        Paragraph("<b>Assigned Responsibility:</b> Government Scheme Research and Eligibility Criteria Analysis", ParagraphStyle('M4R', parent=body_style, fontSize=7.2, spaceAfter=2)),
        Paragraph("<b>Work Completed:</b>", ParagraphStyle('M4W', parent=body_style, fontSize=7.2, spaceAfter=1))
    ]
    m4_items = [
        "Researched relevant Central and State Government welfare schemes.",
        "Collected scheme eligibility criteria, benefits and required documents.",
        "Categorized schemes based on income, occupation and social category.",
        "Prepared structured scheme information for the application.",
        "Worked on mapping citizen requirements with suitable government schemes."
    ]
    for it in m4_items:
        m4_content.append(Paragraph(f"• {it}", ParagraphStyle('M4I', parent=bullet_style, fontSize=7, leading=9.2, spaceAfter=1.5)))
    t_m4 = Table([[m4_content]], colWidths=[505])
    t_m4.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.white),
        ('BOX', (0,0), (-1,-1), 0.75, border_gray),
        ('TOPPADDING', (0,0), (-1,-1), 3.5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3.5),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(t_m4)
    story.append(Spacer(1, 4))

    # MEMBER 5: DISHA
    story.append(Paragraph("Member 5 — Financial Assistance & User Requirements", h2_style))
    m5_content = [
        Paragraph("<b>Student Name:</b> Disha &nbsp;&nbsp;|&nbsp;&nbsp; <b>Registration Number:</b> 12310869", ParagraphStyle('M5H', parent=body_style, fontSize=7.8, textColor=navy, spaceAfter=2)),
        Paragraph("<b>Assigned Responsibility:</b> Financial Assistance and Citizen Requirement Analysis", ParagraphStyle('M5R', parent=body_style, fontSize=7.2, spaceAfter=2)),
        Paragraph("<b>Work Completed:</b>", ParagraphStyle('M5W', parent=body_style, fontSize=7.2, spaceAfter=1))
    ]
    m5_items = [
        "Researched government financial assistance and loan-related schemes.",
        "Identified common financial requirements of citizens and small businesses.",
        "Prepared information about loan limits, subsidies and financial benefits.",
        "Designed the functional requirements for financial assistance features.",
        "Helped identify relevant information required for the funding journey."
    ]
    for it in m5_items:
        m5_content.append(Paragraph(f"• {it}", ParagraphStyle('M5I', parent=bullet_style, fontSize=7, leading=9.2, spaceAfter=1.5)))
    t_m5 = Table([[m5_content]], colWidths=[505])
    t_m5.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.white),
        ('BOX', (0,0), (-1,-1), 0.75, border_gray),
        ('TOPPADDING', (0,0), (-1,-1), 3.5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3.5),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(t_m5)
    story.append(Spacer(1, 6))

    # ====================================================
    # SECTION 2: EXECUTIVE SUMMARY
    # ====================================================
    story.append(Paragraph("2. Executive Summary", h1_style))
    story.append(p("JANSAARTHI AI is an intelligent civic advisory platform created to address Problem Statement SIH26092 for Smart India Hackathon 2026. The platform bridges the massive informational and procedural gap that separates marginalized Indian micro-entrepreneurs—especially street vendors, rural artisans, women entrepreneurs, and Scheduled Caste / Scheduled Tribe (SC/ST) business owners—from central and state government credit and subsidy schemes.", bold_prefix="Project Overview: "))
    story.append(p("While flagship initiatives such as the Pradhan Mantri Mudra Yojana (PMMY), Stand-Up India, and PMEGP have substantial budgetary allocations, access remains hindered by scattered portal websites, complex bureaucratic language, lack of eligibility awareness, and exploitation by predatory intermediaries. JANSAARTHI AI eliminates these barriers by providing a unified, bilingual (Hindi/English), mobile-first application that ingests applicant profiles, applies multi-factor deterministic and semantic eligibility algorithms, computes accurate financial projections (EMIs and subsidies), and guides users directly to official channels and nearby authorized banking partners.", bold_prefix="Core Mission & Solution: "))
    story.append(p("At the current project milestone, the full-stack architecture is operational: a modern native Kotlin Android client powered by Jetpack Compose is integrated via TLS REST APIs with an Express.js backend and a MongoDB Atlas cloud cluster. The client-side application features authenticated session management, multi-step onboarding, a personalized home dashboard, dynamic profile editing, a mathematical EMI calculator, partner discovery with map routing, and a dedicated notifications hub. Staging tests and 28 automated unit tests have been successfully verified.", bold_prefix="Current Progress: "))

    # ====================================================
    # SECTION 3: INTRODUCTION & PROBLEM STATEMENT
    # ====================================================
    story.append(Paragraph("3. Introduction and Problem Statement", h1_style))
    story.append(p("The Micro, Small, and Medium Enterprises (MSME) sector contributes approximately 30% of India's GDP and over 45% of total manufacturing output, serving as the largest employer outside agriculture. Within this ecosystem, micro-enterprises account for over 95% of all MSME establishments.", bold_prefix="Economic Context: "))
    story.append(p("Despite their economic importance, marginalized entrepreneurs face systemic structural handicaps when seeking institutional finance:", bold_prefix="Key Challenges Identified: "))
    story.append(bullet("Government schemes are distributed across disparate ministry portals (MSME, MoF, MoHUA, MoTA, MoSJE), forcing applicants to navigate confusing, fragmented government websites.", bold_prefix="Information Asymmetry & Fragmentation: "))
    story.append(bullet("Guidelines are predominantly published in dense legal English or formal Hindi, creating an impassable barrier for micro-entrepreneurs who lack formal higher education or financial literacy.", bold_prefix="Language & Literacy Barriers: "))
    story.append(bullet("Entrepreneurs are unable to evaluate whether their caste category, annual income, or enterprise activity satisfies strict eligibility criteria before undergoing lengthy branch visits.", bold_prefix="Ambiguous Eligibility Criteria: "))
    story.append(bullet("Applicants frequently face rejections due to incomplete paperwork or lack of awareness regarding essential documents such as Udyam registration, caste certificates, or project reports.", bold_prefix="Documentation Deficits: "))
    story.append(bullet("Information vacuums enable unscrupulous agents and touts who charge exorbitant 'processing fees' from vulnerable citizens without guaranteeing loan approvals.", bold_prefix="Intermediary Exploitation: "))

    # ====================================================
    # SECTION 4: PROJECT OBJECTIVES
    # ====================================================
    story.append(Paragraph("4. Project Objectives", h1_style))
    story.append(p("The primary objective of JANSAARTHI AI is to engineer a dependable, transparent, AI-augmented mobile platform that empowers grassroots entrepreneurs to independently discover, evaluate, and apply for government financial assistance.", bold_prefix="Primary Goal: "))
    story.append(bullet("Consolidate central and state financial schemes into a standardized, machine-readable JSON schema updated continuously.", bold_prefix="1. Unified Scheme Repository: "))
    story.append(bullet("Implement a two-tiered recommendation engine combining hard demographic filtering with fuzzy NLP requirement parsing.", bold_prefix="2. Deterministic & Semantic Matching: "))
    story.append(bullet("Provide transparent match percentages accompanied by plain-language rationales detailing why a scheme fits the user's specific context.", bold_prefix="3. Explainable AI Matching: "))
    story.append(bullet("Design an accessible, low-bandwidth native Android application supporting English and Hindi with full Material 3 contrast standards.", bold_prefix="4. Bilingual & Accessible Design: "))
    story.append(bullet("Generate personalized checklists of required identity, income, business, and caste documents prior to formal submission.", bold_prefix="5. Document Readiness Assistant: "))
    story.append(bullet("Connect entrepreneurs directly to verified nearby public and commercial bank branches with one-touch calling and route mapping.", bold_prefix="6. Geo-Spatial Banking Navigation: "))

    # ====================================================
    # SECTION 5: PROPOSED SOLUTION
    # ====================================================
    story.append(Paragraph("5. Proposed Solution & Value Proposition", h1_style))
    story.append(p("JANSAARTHI AI delivers a comprehensive, citizen-centric mobile ecosystem designed to simplify government scheme discovery. Instead of navigating dozens of web portals, entrepreneurs interact with a single intuitive application that translates their profile into personalized scheme recommendations with transparent match scores, financial projections, and local bank touchpoints.", bold_prefix="Solution Framework: "))
    story.append(bullet("Native Android application supporting Hindi and English with adaptive Light and Dark themes, clean card layouts, and accessible fonts.", bold_prefix="Bilingual, Mobile-First Interface: "))
    story.append(bullet("Transparent match percentage scoring (e.g. 98% Mudra match) that explains exact eligibility factors (caste category, income threshold, location).", bold_prefix="Explainable Match Scoring: "))
    story.append(bullet("Live EMI calculator with Indian numbering format (₹) and sliders for loan amount, tenure, and interest rate.", bold_prefix="Mathematical Financial Projections: "))
    story.append(bullet("Location-aware discovery of nearby authorized commercial bank branches and District Industries Centres (DIC) with one-touch phone and navigation intents.", bold_prefix="Local Banking Guidance: "))
    story.append(bullet("RAG-powered chatbot answering citizen queries using official scheme guidelines without hallucination.", bold_prefix="AI-Driven Vernacular Assistant: "))

    # ====================================================
    # SECTION 6: APPLICATION WORKFLOW
    # ====================================================
    story.append(Paragraph("6. Complete End-to-End Application Workflow", h1_style))
    story.append(p("The operational workflow models the complete citizen journey from application launch to official scheme application submission. Each phase addresses a specific barrier in the scheme discovery lifecycle:", bold_prefix="Process Walkthrough: "))
    
    workflow_steps = [
        ("Step 1: App Launch & Session Check — ", "The user opens JANSAARTHI. The animated splash screen initializes dependencies and checks TokenManager for an active, valid JWT token. If authenticated, the user lands directly on the Home dashboard; otherwise, they are routed to Login/Registration."),
        ("Step 2: Authentication & Multi-Step Onboarding — ", "New users enter their 10-digit mobile number, full name, date of birth, gender, state, district, and social category (SC/ST/OBC/General). Step 2 collects annual family income, occupation, education level, and area type (Urban/Rural). Step 3 establishes password security with real-time complexity validation."),
        ("Step 3: Citizen Profile Management — ", "The citizen's profile is synchronized with MongoDB Atlas. Users can update their personal details, upload a profile photo via gallery/camera (compressed and Base64 encoded), and adjust language/theme preferences."),
        ("Step 4: Requirement Description & Input — ", "The citizen describes their financial or business needs: required capital amount, enterprise purpose (retail shop, machinery purchase, vehicle), and preferred repayment timeline."),
        ("Step 5: Scheme Identification & Catalog Query — ", "The backend queries the centralized scheme database, filtering welfare programs across central ministries (MSME, Finance) and state departments."),
        ("Step 6: Multi-Factor Eligibility Analysis — ", "The rule engine checks hard constraints (caste category eligibility, income caps, age limits, and geographical scope) to eliminate non-matching programs."),
        ("Step 7: Scheme Recommendations & Scoring — ", "Eligible schemes are ranked by dynamic match percentage (e.g., 98% for PM Mudra, 85% for Stand-Up India) with clear subsidy and interest rate summaries."),
        ("Step 8: Scheme Comparison & Benefit Evaluation — ", "The user reviews competing schemes side-by-side, evaluating maximum assistance amounts, interest rate slabs, and government subsidy percentages."),
        ("Step 9: In-Depth Scheme Details & Guidelines — ", "The citizen accesses comprehensive scheme documentation, understanding official terms, moratorium periods, and collateral exemptions."),
        ("Step 10: Document Preparation Checklist — ", "An automated checklist identifies mandatory verification documents (Aadhaar, PAN card, ITR / Income Certificate, Project Report, Caste Certificate) required for formal submission."),
        ("Step 11: Action Plan & Official Bank Guidance — ", "The citizen is guided to nearby authorized commercial banks and District Industries Centres (DIC) with one-touch phone dialer and Google Maps directions, or directed to official portals.")
    ]
    for pref, desc in workflow_steps:
        story.append(bullet(desc, bold_prefix=pref))

    # ====================================================
    # SECTION 7: SYSTEM ARCHITECTURE
    # ====================================================
    story.append(Paragraph("7. Complete System Architecture", h1_style))
    story.append(p("JANSAARTHI AI employs a modular 5-tier client-server architecture designed for high availability, security, and low-latency mobile execution across India's diverse cellular networks.", bold_prefix="Architectural Overview: "))
    
    arch_img = "JANSAARTHI_REPORT/images/diagram_a_sys_arch.png"
    if os.path.exists(arch_img):
        story.append(KeepTogether([
            Image(arch_img, width=480, height=240),
            Paragraph("Figure 7.1: Five-Tier System Architecture Diagram (Client, Network, Cloud Backend, Database, AI Engine)", caption_style)
        ]))

    story.append(bullet("Native Android application written in Kotlin and Jetpack Compose. Utilizes Single-Activity architecture (MainActivity.kt), MVVM ViewModels, and StateFlow for reactive, unidirectional UI updates.", bold_prefix="1. Client Presentation Tier: "))
    story.append(bullet("Retrofit 2 REST HTTP client with OkHttp interceptors providing automatic JWT Bearer token injection, request logging, and TLS 1.3 encryption.", bold_prefix="2. Network & Security Gateway: "))
    story.append(bullet("Node.js and Express.js REST API server hosted on Render cloud platform. Enforces JWT authMiddleware.js, Multer multipart processing, and route rate limiting.", bold_prefix="3. Cloud Application Tier: "))
    story.append(bullet("MongoDB Atlas managed cloud cluster. Mongoose ODM enforces strict schema validation, unique indexes on mobile numbers, and encrypted password storage.", bold_prefix="4. Cloud Database Tier: "))
    story.append(bullet("Hybrid recommendation engine combining rule-based eligibility verification in recommendationController.js with Python RAG retrieval subprocess (ragController.js) querying ChromaDB vector stores.", bold_prefix="5. AI & Intelligence Tier: "))

    # ====================================================
    # SECTION 8: WORKFLOW FLOWCHART
    # ====================================================
    story.append(Paragraph("8. Application Workflow and Decision-Branch Flowchart", h1_style))
    story.append(p("The following decision-branch flowchart maps the logical transitions executed by the mobile client and backend services as a user navigates from splash launch to final banking handoff:", bold_prefix="Control Flow Specification: "))

    flow_img = "JANSAARTHI_REPORT/images/diagram_d_flowchart.png"
    if os.path.exists(flow_img):
        story.append(KeepTogether([
            Image(flow_img, width=440, height=310),
            Paragraph("Figure 8.1: Decision-Branch Application Flowchart (Auth Checks, Onboarding, Matching Logic, Bank Navigation)", caption_style)
        ]))

    story.append(bullet("TokenManager checks local SharedPreferences. If valid JWT exists, routes to Home; otherwise routes to Login.", bold_prefix="Branch A (Session Token Check): "))
    story.append(bullet("Verifies if social category, annual income, and district are present. If missing, prompts profile completion.", bold_prefix="Branch B (Profile Completion Check): "))
    story.append(bullet("Discards schemes whose strict criteria (age, caste, income limit) are violated by the user profile.", bold_prefix="Branch C (Eligibility Filter Check): "))
    story.append(bullet("Directs user to either physical bank branch (via Map intent) or official portal web link.", bold_prefix="Branch D (Assistance Channel Selection): "))

    # ====================================================
    # SECTION 9: TECHNOLOGY STACK
    # ====================================================
    story.append(Paragraph("9. Technology Stack & Framework Inventory", h1_style))
    story.append(p("The technological stack is selected for enterprise reliability, native performance, low memory footprint, and rapid scalability:", bold_prefix="Component Stack: "))

    tech_data = [
        [Paragraph("Tier", th_style), Paragraph("Technology / Framework", th_style), Paragraph("Version", th_style), Paragraph("Functional Purpose in JANSAARTHI", th_style)],
        [Paragraph("Mobile Client", td_bold), Paragraph("Kotlin & Jetpack Compose", td_style), Paragraph("2.0 / M3", td_style), Paragraph("Declarative UI, responsive layout constraints, modern state management", td_style)],
        [Paragraph("Architecture", td_bold), Paragraph("MVVM & Navigation Compose", td_style), Paragraph("2.8.5", td_style), Paragraph("Lifecycle-aware ViewModels, StateFlow, secure AuthGuard routing", td_style)],
        [Paragraph("Networking", td_bold), Paragraph("Retrofit 2 & OkHttp 4", td_style), Paragraph("2.11.0", td_style), Paragraph("Type-safe REST client, JWT interceptor, TLS 1.3 encrypted transport", td_style)],
        [Paragraph("Image Handling", td_bold), Paragraph("Coil & Android Bitmap SDK", td_style), Paragraph("2.7.0", td_style), Paragraph("Asynchronous profile photo loading, local compression, and Base64 sync", td_style)],
        [Paragraph("Backend API", td_bold), Paragraph("Node.js & Express.js", td_style), Paragraph("20.x / 4.19", td_style), Paragraph("Non-blocking asynchronous REST services, JWT security, Multer uploads", td_style)],
        [Paragraph("Database", td_bold), Paragraph("MongoDB Atlas & Mongoose", td_style), Paragraph("8.5.2", td_style), Paragraph("Cloud NoSQL database, JSON document models, connection pooling", td_style)],
        [Paragraph("AI / ML Pipeline", td_bold), Paragraph("Python, LangChain & ChromaDB", td_style), Paragraph("3.12 / 0.4.x", td_style), Paragraph("Vector embedding of official scheme PDFs, RAG retrieval engine", td_style)],
        [Paragraph("Cloud Hosting", td_bold), Paragraph("Render Cloud Infrastructure", td_style), Paragraph("PaaS", td_style), Paragraph("Continuous deployment from GitHub with automated build verification", td_style)],
    ]
    t_tech = Table(tech_data, colWidths=[65, 120, 65, 255])
    t_tech.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), th_bg),
        ('BOX', (0,0), (-1,-1), 0.75, border_gray),
        ('INNERGRID', (0,0), (-1,-1), 0.5, border_gray),
        ('TOPPADDING', (0,0), (-1,-1), 2.5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 2.5),
        ('LEFTPADDING', (0,0), (-1,-1), 4),
        ('RIGHTPADDING', (0,0), (-1,-1), 4),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, alt_bg])
    ]))
    story.append(t_tech)

    # ====================================================
    # SECTION 10: ANDROID APPLICATION IMPLEMENTATION
    # ====================================================
    story.append(Paragraph("10. Android Application Implementation Details", h1_style))
    story.append(p("The Android application is built with 100% Kotlin code utilizing modern Jetpack Compose principles without legacy XML views. Key architectural features include:", bold_prefix="Implementation Highlights: "))
    story.append(bullet("The MainActivity.kt establishes edge-to-edge window insets (enableEdgeToEdge), sets up the JANSAARTHI theme, and renders the central NavHost graph.", bold_prefix="Single-Activity Architecture: "))
    story.append(bullet("Navigation Compose declares routes for splash, login, register, home, schemes, calculator, partners, profile, notifications, and sub-profile editors. Custom AuthGuard composables enforce token verification on all protected routes.", bold_prefix="Navigation & AuthGuard: "))
    story.append(bullet("RegisterScreen.kt implements a 4-step progressive onboarding sequence (Introduction -> Basic Details -> Additional Details -> Create Password). A shared RegistrationFormState preserves entered field values across back-and-forth navigation.", bold_prefix="Multi-Step Registration Flow: "))
    story.append(bullet("HomeScreen.kt features a personalized greeting header with live user name, profile avatar, AI assistant banner with RAG chip, 2x2 Quick Services grid, and PM Mudra featured scheme card.", bold_prefix="Home Dashboard: "))
    story.append(bullet("CalculatorScreen.kt allows micro-entrepreneurs to calculate monthly loan EMIs using the exact banking formula [P x R x (1+R)^N]/[(1+R)^N - 1] with live interactive sliders and Indian currency formatting (₹).", bold_prefix="Financial EMI Calculator: "))
    story.append(bullet("PartnersScreen.kt displays nearby authorized public and private banks offering MSME credit. Includes one-touch Android Intent actions for direct telephone calling (Intent.ACTION_DIAL) and Google Maps navigation (Intent.ACTION_VIEW).", bold_prefix="Partner Center Discovery: "))
    story.append(bullet("NotificationScreen.kt provides an operational notification hub featuring unread badges, category filter chips (All, Unread, Schemes), mark-as-read toggles, and direct deep-linking into scheme recommendations.", bold_prefix="Notification Hub: "))

    # ====================================================
    # SECTION 11: BACKEND IMPLEMENTATION
    # ====================================================
    story.append(Paragraph("11. Backend Implementation & Cloud Services", h1_style))
    story.append(p("The backend tier is built on Node.js and Express.js, hosted on Render cloud infrastructure, providing secure, stateless REST APIs:", bold_prefix="Backend Highlights: "))
    story.append(bullet("Encapsulated in authRoutes.js and authController.js. Handles user registration, mobile number uniqueness verification, password hashing, and login token issuance.", bold_prefix="Authentication Controller: "))
    story.append(bullet("Implemented in profileRoutes.js. Allows authenticated users to query and update demographic details, income ranges, caste category, and upload compressed profile photos via Multer.", bold_prefix="Profile Controller: "))
    story.append(bullet("recommendationController.js executes multi-factor scoring against central and state scheme databases based on applicant category, income, and business purpose.", bold_prefix="Recommendation Controller: "))
    story.append(bullet("ragController.js bridges the Node.js backend with Python RAG retrieval scripts, enabling natural-language citizen inquiries against official government guidelines.", bold_prefix="RAG Subprocess Controller: "))

    # ====================================================
    # SECTION 12: MONGODB DATABASE DESIGN
    # ====================================================
    story.append(Paragraph("12. MongoDB Database Design & Schema Architecture", h1_style))
    story.append(p("The database layer is deployed on MongoDB Atlas managed cloud infrastructure. The primary document schema in User.js models citizen identities, demographic attributes, and business parameters.", bold_prefix="Database Architecture: "))

    db_img = "JANSAARTHI_REPORT/images/diagram_c_database.png"
    if os.path.exists(db_img):
        story.append(KeepTogether([
            Image(db_img, width=480, height=235),
            Paragraph("Figure 12.1: MongoDB Atlas Database Schema & Entity Relationships", caption_style)
        ]))

    story.append(p("The users collection includes the following structured field categories:", bold_prefix="Schema Field Breakdown: "))
    story.append(bullet("name (String, required), mobile (String, unique, indexed), email (String, sparse index), passwordHash (String, bcrypt hashed).", bold_prefix="Identity & Security: "))
    story.append(bullet("age (Number), dob (String), gender (String), state (String), district (String), scCategory (String: General, OBC, SC, ST, EWS).", bold_prefix="Demographic Profile: "))
    story.append(bullet("annualFamilyIncome (Number), occupation (String), businessType (String), businessRegistrationStatus (String), purpose (String), requestedAmount (Number), projectCost (Number), educationLevel (String), areaType (String: Urban/Rural).", bold_prefix="Business & Financial Data: "))
    story.append(bullet("verificationDocType (String), verificationDocUrl (String), profilePhoto (String: Base64 data).", bold_prefix="Media & Verification: "))
    story.append(bullet("language (String, default: 'EN'), theme (String, default: 'SYSTEM'), notificationsEnabled (Boolean, default: true).", bold_prefix="Preferences: "))

    # ====================================================
    # SECTION 13: AUTHENTICATION AND SECURITY
    # ====================================================
    story.append(Paragraph("13. Authentication and Security Architecture", h1_style))
    story.append(p("Security is engineered into every tier of the JANSAARTHI platform following defense-in-depth principles:", bold_prefix="Security Posture: "))
    story.append(bullet("Passwords are never stored in plaintext. They are salted and hashed on the backend using bcrypt with a work factor of 10 rounds prior to database insertion.", bold_prefix="1. Password Hashing: "))
    story.append(bullet("Successful authentication issues an HMAC-SHA256 signed JSON Web Token containing the user ID and expiration timestamp, sent via Authorization: Bearer <token> headers.", bold_prefix="2. Stateless JWT Tokens: "))
    story.append(bullet("TokenManager.kt stores the JWT token within Android's private SharedPreferences, accessible only by the application's Linux sandbox UID.", bold_prefix="3. Client Token Storage: "))
    story.append(bullet("The mobile navigation graph wraps all protected routes in an AuthGuard check. If the session is invalid, the user is rerouted to Login with the entire backstack popped to prevent back-button bypasses.", bold_prefix="4. Compose AuthGuard: "))
    story.append(bullet("Strict validation regex patterns enforce 10-digit Indian mobile numbers (^[6-9][0-9]{9}$), password complexity (8+ chars, uppercase, lowercase, numeric), and date formatting.", bold_prefix="5. Input Sanitization & Validation: "))

    # ====================================================
    # SECTION 14: SCHEME DISCOVERY & MATCHING
    # ====================================================
    story.append(Paragraph("14. Scheme Discovery and Matching Approach", h1_style))
    story.append(p("JANSAARTHI AI implements a multi-stage scheme discovery pipeline that processes applicant data through sequential validation and scoring filters:", bold_prefix="Matching Methodology: "))

    pipe_img = "JANSAARTHI_REPORT/images/diagram_b_pipeline.png"
    if os.path.exists(pipe_img):
        story.append(KeepTogether([
            Image(pipe_img, width=480, height=210),
            Paragraph("Figure 14.1: Eight-Step Scheme Discovery, Matching, and RAG Intelligence Pipeline", caption_style)
        ]))

    story.append(bullet("Ingests 14 verified applicant data points from MongoDB.", bold_prefix="Stage 1 — Profile Ingestion: "))
    story.append(bullet("Eliminates programs whose caste, age, income, or gender rules are strictly violated.", bold_prefix="Stage 2 — Hard Demographic Filtering: "))
    story.append(bullet("Evaluates whether requested capital matches scheme loan brackets (Shishu, Kishore, Tarun).", bold_prefix="Stage 3 — Business Purpose Matching: "))
    story.append(bullet("Computes transparent match percentage based on weighted attribute alignment.", bold_prefix="Stage 4 — Dynamic Scoring: "))
    story.append(bullet("Generates human-readable rationales explaining why the scheme was recommended.", bold_prefix="Stage 5 — Explainability Engine: "))

    # ====================================================
    # SECTION 15: ELIGIBILITY ASSESSMENT APPROACH
    # ====================================================
    story.append(Paragraph("15. Multi-Factor Eligibility Assessment Approach", h1_style))
    story.append(p("The eligibility engine models both binary gate constraints and gradient preference scoring:", bold_prefix="Dual Evaluation Logic: "))
    story.append(bullet("Mandatory qualifying criteria where failure results in immediate disqualification (e.g. non-SC/ST applicants disqualified from Stand-Up India unless female; applicants exceeding income caps disqualified from targeted subsidies).", bold_prefix="1. Hard Threshold Rules: "))
    story.append(bullet("Additive scoring parameters that elevate recommendations for vulnerable groups (e.g., annual family income under ₹3,00,000 adds 15% priority score; women entrepreneurs receive enhanced ranking for Mudra Tarun).", bold_prefix="2. Soft Weighting Rules: "))

    # ====================================================
    # SECTION 16: APPLICATION READINESS & DOCUMENTS
    # ====================================================
    story.append(Paragraph("16. Application Readiness and Document Management", h1_style))
    story.append(p("To prevent application rejections caused by missing paperwork, JANSAARTHI AI introduces an automated Document Readiness Assistant:", bold_prefix="Document Verification Framework: "))
    story.append(bullet("Aadhaar Card (mandatory for UIDAI biometric authentication), Voter ID, PAN Card.", bold_prefix="Tier 1 — Identity Documents: "))
    story.append(bullet("Bank statement for last 6 months, Income Certificate from Tehsildar, ITR acknowledgments (if filed).", bold_prefix="Tier 2 — Financial Documents: "))
    story.append(bullet("Udyam Registration Certificate, Shop & Establishment Act license, trade license.", bold_prefix="Tier 3 — Business Enterprise Proof: "))
    story.append(bullet("Caste certificate issued by competent revenue authority for SC/ST/OBC applicants.", bold_prefix="Tier 4 — Category Proof: "))

    # ====================================================
    # SECTION 17: COMPLETE API AND DATA FLOW
    # ====================================================
    story.append(Paragraph("17. Complete API and Data Flow Directory", h1_style))
    story.append(p("The table below documents all active REST API endpoints implemented in the backend service:", bold_prefix="REST Endpoint Specifications: "))

    api_headers = [Paragraph("Method", th_center), Paragraph("Endpoint URL", th_style), Paragraph("Auth", th_center), Paragraph("Payload / Parameters", th_style), Paragraph("Response Object", th_style), Paragraph("HTTP", th_center)]
    api_table_data = [api_headers]
    api_raw = [
        ("POST", "/api/auth/register", "No", "name, mobile, password, state, district, scCategory, ...", "user, token", "201 Created"),
        ("POST", "/api/auth/login", "No", "mobile, password", "user, token", "200 OK"),
        ("GET", "/api/auth/me", "Yes", "None (Bearer JWT in header)", "user profile object", "200 OK"),
        ("POST", "/api/auth/logout", "Yes", "None (invalidates client token)", "{ message: 'Logged out' }", "200 OK"),
        ("GET", "/api/profile", "Yes", "None (Bearer JWT in header)", "comprehensive user profile", "200 OK"),
        ("PUT", "/api/profile", "Yes", "Partial user fields to update", "updated user object", "200 OK"),
        ("POST", "/api/profile/photo", "Yes", "Multipart form: 'photo' file", "{ photoUrl: '...' }", "200 OK"),
    ]
    for row in api_raw:
        api_table_data.append([
            Paragraph(f"<b>{row[0]}</b>", td_center),
            Paragraph(f"<code>{row[1]}</code>", td_style),
            Paragraph(row[2], td_center),
            Paragraph(row[3], td_style),
            Paragraph(row[4], td_style),
            Paragraph(row[5], td_center)
        ])
    t_api = Table(api_table_data, colWidths=[45, 120, 35, 160, 95, 50])
    t_api.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), th_bg),
        ('BOX', (0,0), (-1,-1), 0.75, border_gray),
        ('INNERGRID', (0,0), (-1,-1), 0.5, border_gray),
        ('TOPPADDING', (0,0), (-1,-1), 2.5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 2.5),
        ('LEFTPADDING', (0,0), (-1,-1), 3),
        ('RIGHTPADDING', (0,0), (-1,-1), 3),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, alt_bg])
    ]))
    story.append(t_api)
    story.append(Spacer(1, 4))

    # ====================================================
    # SECTION 18: SOURCE CODE DIRECTORY
    # ====================================================
    story.append(Paragraph("18. Source Code and File Structure Inventory", h1_style))
    story.append(p("The following directory lists 33 verified source code files implemented in the JANSAARTHI repository across Android, Backend, and AI layers:", bold_prefix="Repository Inventory: "))

    files_headers = [Paragraph("File Name", th_style), Paragraph("Technology / Layer", th_style), Paragraph("Functional Purpose in JANSAARTHI", th_style), Paragraph("Status", th_center)]
    files_table_data = [files_headers]
    files_list = [
        ("MainActivity.kt", "Kotlin / Compose", "Application entry activity; edge-to-edge system insets, theme setup", "Completed"),
        ("App.kt", "Kotlin", "Application singleton; initializes AuthRepository and service locator", "Completed"),
        ("AppBottomNavigation.kt", "Compose UI", "Floating curved bottom navigation bar with gold active states", "Completed"),
        ("NavGraph.kt", "Navigation Compose", "Declares all application routes and AuthGuard protected wrappers", "Completed"),
        ("LoginScreen.kt", "Compose UI", "Mobile and password login interface with validation and register trigger", "Completed"),
        ("RegisterScreen.kt", "Compose UI", "4-step multi-page registration with step progress indicator", "Completed"),
        ("RegistrationFormState.kt", "Compose State", "Reactive state holder preserving registration input across steps", "Completed"),
        ("AuthViewModel.kt", "Kotlin MVVM", "ViewModel managing authentication, registration, and session flows", "Completed"),
        ("HomeScreen.kt", "Compose UI", "Main dashboard with personalized header, AI banner, and service grid", "Completed"),
        ("SchemesScreen.kt", "Compose UI", "Schemes directory with search query filter and horizontal category chips", "Completed"),
        ("CalculatorScreen.kt", "Compose UI", "Mathematical loan EMI calculator with live sliders and currency format", "Completed"),
        ("PartnersScreen.kt", "Compose UI", "Nearby banking partner locator with call dialer and maps routing", "Completed"),
        ("ProfileScreen.kt", "Compose UI", "Profile management, avatar with gold ring, grouped setting cards", "Completed"),
        ("ProfileSubScreens.kt", "Compose UI", "Modular sub-screens for Basic Details, Category/Income, Business/Credit", "Completed"),
        ("NotificationScreen.kt", "Compose UI", "Notification center with unread badges, filter chips, scheme alerts", "Completed"),
        ("SplashScreen.kt", "Compose UI", "Animated logo splash screen with session auto-routing logic", "Completed"),
        ("TokenManager.kt", "Kotlin / Data", "Encrypted SharedPreferences storage for JWT authentication tokens", "Completed"),
        ("RetrofitClient.kt", "OkHttp / Retrofit", "HTTP network client with logging and automatic JWT auth interceptor", "Completed"),
        ("AuthApi.kt", "Retrofit Interface", "Declares registration, login, profile query, and update endpoints", "Completed"),
        ("AuthRepository.kt", "Kotlin Repository", "Coordinates network requests, token caching, and user authentication state", "Completed"),
        ("Color.kt", "Material 3 Theme", "Design tokens for Primary Gold, Accent Gold, Navy, and Ivory Background", "Completed"),
        ("Theme.kt", "Material 3 Theme", "Material 3 dynamic Light and Dark theme implementations", "Completed"),
        ("Strings.kt", "Localization", "Bilingual programmatic strings repository supporting English and Hindi", "Completed"),
        ("ImageUtils.kt", "Bitmap Utils", "Image compression, gallery picker handling, and Base64 encoding", "Completed"),
        ("app.js", "Node.js / Express", "Server entry point, CORS, JSON middleware, and route mounting", "Completed"),
        ("db.js", "Mongoose / Config", "MongoDB Atlas connection manager with Mongoose connection pooling", "Completed"),
        ("User.js", "Mongoose Model", "Document schema for user identity, demographic profile, and business data", "Completed"),
        ("authController.js", "Express Controller", "Handles register, login, profile query, and profile update logic", "Completed"),
        ("authRoutes.js", "Express Router", "Declares POST /api/auth/register, login, me, and logout endpoints", "Completed"),
        ("profileRoutes.js", "Express Router", "Declares GET/PUT /api/profile and Multer photo upload endpoints", "Completed"),
        ("authMiddleware.js", "Express Security", "Verifies Bearer JWT tokens and attaches authenticated user context", "Completed"),
        ("ragController.js", "Express / Python", "Spawns Python RAG retrieval subprocess for citizen queries", "In Progress"),
        ("recommendationController.js", "Express Controller", "Multi-factor scheme eligibility and scoring algorithm handler", "In Progress")
    ]
    for fn, tech, purp, st in files_list:
        st_color = success_green if st == "Completed" else pending_gray
        files_table_data.append([
            Paragraph(f"<b>{fn}</b>", td_style),
            Paragraph(tech, td_style),
            Paragraph(purp, td_style),
            Paragraph(f"<font color='{st_color.hexval()}'><b>{st}</b></font>", td_center)
        ])
    t_files = Table(files_table_data, colWidths=[115, 95, 230, 65], repeatRows=1)
    t_files.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), th_bg),
        ('BOX', (0,0), (-1,-1), 0.75, border_gray),
        ('INNERGRID', (0,0), (-1,-1), 0.5, border_gray),
        ('TOPPADDING', (0,0), (-1,-1), 1.8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 1.8),
        ('LEFTPADDING', (0,0), (-1,-1), 3),
        ('RIGHTPADDING', (0,0), (-1,-1), 3),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, alt_bg])
    ]))
    story.append(t_files)

    # ====================================================
    # SECTION 19: SCREENSHOTS (Clean 2-Column Grid)
    # ====================================================
    story.append(Spacer(1, 4))
    story.append(Paragraph("19. Application Verification Screenshots", h1_style))
    story.append(p("The following 10 screenshots represent authentic captures from the live Android application executing on a physical Android test device (OPPO CPH2333, ColorOS / Android 13). They confirm full operational readiness of all core user flows:", bold_prefix="Physical Device Execution Proof: "))

    screenshots = [
        ("1_splash.png", "Figure 19.1: Splash Screen with Logo & Session Check"),
        ("2_login.png", "Figure 19.2: Login Screen with Input Validation"),
        ("3_registration.png", "Figure 19.3: Multi-Step Registration Form (Step 1)"),
        ("4_home.png", "Figure 19.4: Home Dashboard with Services & Mudra Card"),
        ("5_profile.png", "Figure 19.5: My Profile Screen with Grouped Settings"),
        ("6_schemes.png", "Figure 19.6: Schemes Catalog with Category Chips"),
        ("7_calculator.png", "Figure 19.7: Loan EMI Calculator with Dynamic Sliders"),
        ("8_partners.png", "Figure 19.8: Nearby Banking Partners with Call & Map Intents"),
        ("9_notifications.png", "Figure 19.9: Notification Hub with Scheme Match Badges"),
        ("10_rag_assistant.png", "Figure 19.10: RAG AI Assistant Answering Inquiries")
    ]

    for batch_start in range(0, len(screenshots), 4):
        batch = screenshots[batch_start:batch_start+4]
        rows_cells = []
        for i in range(0, len(batch), 2):
            row_items = []
            for item in batch[i:i+2]:
                img_name, cap = item
                img_path = os.path.join("JANSAARTHI_REPORT", "images", img_name)
                cell_flowables = []
                if os.path.exists(img_path):
                    cell_flowables.append(Image(img_path, width=110, height=220))
                cell_flowables.append(Spacer(1, 2))
                cell_flowables.append(Paragraph(cap, caption_style))
                row_items.append(cell_flowables)
            if len(row_items) == 1:
                row_items.append([])
            rows_cells.append(row_items)
            
        t_batch = Table(rows_cells, colWidths=[245, 245])
        t_batch.setStyle(TableStyle([
            ('ALIGN', (0,0), (-1,-1), 'CENTER'),
            ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
            ('TOPPADDING', (0,0), (-1,-1), 2),
            ('BOTTOMPADDING', (0,0), (-1,-1), 2),
            ('LEFTPADDING', (0,0), (-1,-1), 4),
            ('RIGHTPADDING', (0,0), (-1,-1), 4),
        ]))
        story.append(t_batch)
        if batch_start + 4 < len(screenshots):
            story.append(Spacer(1, 4))

    story.append(Spacer(1, 6))

    # ====================================================
    # SECTION 20: CURRENT IMPLEMENTATION STATUS
    # ====================================================
    story.append(Paragraph("20. Current Implementation Status Matrix", h1_style))
    story.append(p("The matrix below provides an honest, verified evaluation of each module within the JANSAARTHI AI platform:", bold_prefix="Module Status Assessment: "))

    matrix_headers = [Paragraph("System Module / Capability", th_style), Paragraph("Current Status", th_center), Paragraph("Technical Verification & Evidence", th_style)]
    matrix_table_data = [matrix_headers]
    matrix_data = [
        ("Android UI & Design System", "Completed", "Full Jetpack Compose UI across all 5 core screens; Material 3 tokens verified."),
        ("Authentication & Security", "Completed", "JWT auth, bcrypt hashing, AuthGuard navigation, and session persistence verified on device."),
        ("MongoDB Atlas Integration", "Completed", "Cloud database cluster connected with Mongoose connection pooling and User schemas."),
        ("Profile Management", "Completed", "Comprehensive profile view, sub-screen editors, and Base64 photo upload functioning cleanly."),
        ("Government Scheme Database", "Completed", "Structured central schemes catalog (Mudra, Stand-Up, PMEGP, SVANidhi) populated with criteria."),
        ("Financial EMI Calculator", "Completed", "Mathematical banking formula implemented with live sliders and Indian numbering format (₹)."),
        ("Banking Partner Discovery", "Completed", "Nearby partner center discovery with active phone dialer and Google Maps intents."),
        ("Dedicated Notification Center", "Completed", "NotificationsScreen implemented with category filters, unread badges, and deep links."),
        ("Multi-Parameter Scheme Matching", "In Progress", "Hard demographic filters completed; advanced weighting algorithm currently under refinement."),
        ("Rule-Based Eligibility Engine", "In Progress", "Basic demographic eligibility active; complex credit and income bracket rule verification in progress."),
        ("Personalized Document Checklist", "In Progress", "Static document lists operational; dynamic user-specific checklist generator in development."),
        ("Application Readiness Action Plan", "Planned", "Scheduled for Phase 3; will provide sequential milestone tracker for loan applications."),
        ("Multilingual NLP Requirement Engine", "In Progress", "Bilingual UI complete; AI requirement extraction pipeline being integrated with Gemini API."),
        ("RAG AI Scheme Query Assistant", "In Progress", "RAG chat UI completed on Android; staging server connected with context retrieval.")
    ]
    for mod, st, det in matrix_data:
        st_color = success_green if st == "Completed" else pending_gray
        matrix_table_data.append([
            Paragraph(f"<b>{mod}</b>", td_style),
            Paragraph(f"<font color='{st_color.hexval()}'><b>{st}</b></font>", td_center),
            Paragraph(det, td_style)
        ])
    t_mat = Table(matrix_table_data, colWidths=[135, 75, 295])
    t_mat.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), th_bg),
        ('BOX', (0,0), (-1,-1), 0.75, border_gray),
        ('INNERGRID', (0,0), (-1,-1), 0.5, border_gray),
        ('TOPPADDING', (0,0), (-1,-1), 2),
        ('BOTTOMPADDING', (0,0), (-1,-1), 2),
        ('LEFTPADDING', (0,0), (-1,-1), 4),
        ('RIGHTPADDING', (0,0), (-1,-1), 4),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, alt_bg])
    ]))
    story.append(t_mat)
    story.append(Spacer(1, 5))

    # ====================================================
    # SECTION 21: TESTING AND VERIFICATION
    # ====================================================
    story.append(Paragraph("21. Testing and Verification", h1_style))
    story.append(p("The JANSAARTHI platform has undergone rigorous multi-tier testing across unit, integration, and physical hardware layers:", bold_prefix="Testing Methodology: "))
    story.append(bullet("28 automated unit tests executed via Gradle (testDebugUnitTest) verifying registration form validation (RegistrationFlowUnitTest.kt), markdown rendering (FormattedMessageTextTest.kt), and query construction (RagIntegrationTest.kt). Result: 28/28 Passed (BUILD SUCCESSFUL).", bold_prefix="1. Android Unit Tests: "))
    story.append(bullet("All REST routes tested via curl and Postman against staging deployment on Render. Auth tokens, registration payloads, and profile photo multipart streams verified.", bold_prefix="2. Backend Integration: "))
    story.append(bullet("Successfully installed and tested app-debug.apk on OPPO CPH2333 (Android 13, ColorOS). Verified touch targets, soft keyboard handling (imePadding), gesture navigation bar insets, and notifications hub stability.", bold_prefix="3. Hardware Testing: "))

    # ====================================================
    # SECTION 22: FUTURE DEVELOPMENT ROADMAP
    # ====================================================
    story.append(Paragraph("22. Future Development Roadmap", h1_style))
    story.append(p("The future research and product milestones are organized into three sequential development phases:", bold_prefix="Development Timeline: "))
    story.append(bullet("An intelligent diagnostic engine that explains exactly why an applicant does not qualify for a specific high-value scheme and suggests concrete remedial actions (e.g. obtaining Udyam registration or credit repair).", bold_prefix="Milestone 1 — AI Eligibility Gap Analyzer: "))
    story.append(bullet("Interactive side-by-side comparative views highlighting interest rate differences, subsidy percentages, and collateral rules between competing schemes.", bold_prefix="Milestone 2 — Multi-Scheme Comparison Matrix: "))
    story.append(bullet("Sequential milestone tracker allowing citizens to monitor document collection, bank branch appraisal, and disbursement status in real time.", bold_prefix="Milestone 3 — Application Readiness Tracker: "))
    story.append(bullet("Automated synchronization with official government open data portals (data.gov.in) to ingest newly announced schemes and revised guidelines.", bold_prefix="Milestone 4 — Real-Time Scheme API Ingestion: "))
    story.append(bullet("Integration of Automatic Speech Recognition (ASR) and Text-to-Speech (TTS) in regional languages (Hindi, Bhojpuri, Maithili, Bengali) for non-literate micro-entrepreneurs.", bold_prefix="Milestone 5 — Voice-First Vernacular Dialogue: "))
    story.append(bullet("GPS-assisted routing to nearby Common Service Centers (CSCs), District Industries Centres (DIC), and specialized SME bank branches.", bold_prefix="Milestone 6 — Geo-Spatial Banking & CSC Routing: "))

    # ====================================================
    # SECTION 23: CONCLUSION
    # ====================================================
    story.append(Paragraph("23. Academic Conclusion", h1_style))
    story.append(p("The JANSAARTHI AI project addresses one of the most pressing socio-economic challenges identified in Smart India Hackathon 2026: bridging the persistent information and accessibility chasm that disconnects marginalized Indian entrepreneurs from vital government financial support. At the current evaluation milestone, the project has established a highly robust, secure, and production-grade native Android client integrated with a scalable cloud backend and MongoDB database.", bold_prefix="Milestone Synthesis: "))
    story.append(p("With full authentication security, dynamic multi-step onboarding, interactive financial calculators, dedicated notification mechanisms, and live physical device verification successfully concluded, the foundation is fully prepared for final AI matching pipeline refinements and pilot field trials. JANSAARTHI AI demonstrates high technical feasibility, academic rigor, and immense potential to empower millions of grassroots micro-enterprises across the nation.", bold_prefix="Societal Impact: "))

    # Build document with NumberedCanvas
    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"Successfully generated PDF: {out_pdf}")

    # Synchronize copy to JANSAARTHI_Project_Progress_Report.pdf
    alt_pdf = "JANSAARTHI_REPORT/JANSAARTHI_Project_Progress_Report.pdf"
    shutil.copyfile(out_pdf, alt_pdf)
    print(f"Synchronized PDF to: {alt_pdf}")

if __name__ == '__main__':
    build_pdf()
