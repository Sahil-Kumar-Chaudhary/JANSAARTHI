import os
import shutil
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import parse_xml
from docx.oxml.ns import nsdecls

def set_cell_background(cell, fill_hex):
    tcPr = cell._element.get_or_add_tcPr()
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
    tcPr.append(shd)

def set_cell_margins(cell, top=60, bottom=60, left=100, right=100):
    tcPr = cell._element.get_or_add_tcPr()
    tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}><w:top w:w="{top}" w:type="dxa"/><w:bottom w:w="{bottom}" w:type="dxa"/><w:left w:w="{left}" w:type="dxa"/><w:right w:w="{right}" w:type="dxa"/></w:tcMar>')
    tcPr.append(tcMar)

def set_table_borders(table, color="D1D5DB", sz="4", val="single"):
    tblPr = table._element.xpath('w:tblPr')
    if tblPr:
        borders = parse_xml(
            f'<w:tblBorders {nsdecls("w")}>'
            f'<w:top w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
            f'<w:bottom w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
            f'<w:insideH w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
            f'<w:insideV w:val="none"/>'
            f'<w:left w:val="none"/>'
            f'<w:right w:val="none"/>'
            f'</w:tblBorders>'
        )
        tblPr[0].append(borders)

def add_header_footer(doc):
    for s in doc.sections:
        s.top_margin = Inches(0.75)
        s.bottom_margin = Inches(0.75)
        s.left_margin = Inches(0.75)
        s.right_margin = Inches(0.75)
        s.page_width = Inches(8.27)
        s.page_height = Inches(11.69)
        
        # Header: clean academic style
        header = s.header
        hp = header.paragraphs[0]
        hp.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        hrun = hp.add_run("JANSAARTHI AI — SIH 2026 (Problem Statement ID: SIH26092)")
        hrun.font.name = "Arial"
        hrun.font.size = Pt(8.5)
        hrun.font.color.rgb = RGBColor(100, 116, 139)
        
        # Footer: clean academic style
        footer = s.footer
        fp = footer.paragraphs[0]
        fp.alignment = WD_ALIGN_PARAGRAPH.CENTER
        frun = fp.add_run("Project Progress Report — JANSAARTHI AI — Internal Academic Evaluation")
        frun.font.name = "Arial"
        frun.font.size = Pt(8.5)
        frun.font.color.rgb = RGBColor(100, 116, 139)

# Clean, simple academic typography
TEXT_COLOR = RGBColor(26, 32, 44)       # Deep charcoal / near black
HEADING_COLOR = RGBColor(15, 41, 66)    # Deep academic navy
SUB_COLOR = RGBColor(71, 85, 105)       # Neutral slate gray
BORDER_COLOR = "CBD5E1"                 # Clean border gray
HEADER_BG = "F1F5F9"                   # Clean light slate header fill
ALT_ROW_BG = "F8FAFC"                  # Clean soft white alt row

def format_heading(doc, text, level=1):
    p = doc.add_paragraph()
    p.paragraph_format.keep_with_next = True
    run = p.add_run(text)
    run.bold = True
    run.font.name = "Arial"
    if level == 1:
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(4)
        run.font.size = Pt(12.5)
        run.font.color.rgb = HEADING_COLOR
    elif level == 2:
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(3)
        run.font.size = Pt(10.5)
        run.font.color.rgb = HEADING_COLOR
    else:
        p.paragraph_format.space_before = Pt(6)
        p.paragraph_format.space_after = Pt(2)
        run.font.size = Pt(9.5)
        run.font.color.rgb = HEADING_COLOR
    return p

def add_p(doc, text, bold_prefix="", space_after=4):
    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.line_spacing = 1.15
    if bold_prefix:
        r_bold = p.add_run(bold_prefix)
        r_bold.bold = True
        r_bold.font.name = "Arial"
        r_bold.font.size = Pt(9.5)
        r_bold.font.color.rgb = HEADING_COLOR
    r_text = p.add_run(text)
    r_text.font.name = "Arial"
    r_text.font.size = Pt(9.5)
    r_text.font.color.rgb = TEXT_COLOR
    return p

def add_bullet(doc, text, bold_prefix="", space_after=2.5):
    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.left_indent = Inches(0.2)
    p.paragraph_format.line_spacing = 1.15
    rb = p.add_run("• ")
    rb.font.name = "Arial"
    rb.font.size = Pt(9)
    rb.font.color.rgb = SUB_COLOR
    if bold_prefix:
        r_bold = p.add_run(bold_prefix)
        r_bold.bold = True
        r_bold.font.name = "Arial"
        r_bold.font.size = Pt(9)
        r_bold.font.color.rgb = HEADING_COLOR
    r_text = p.add_run(text)
    r_text.font.name = "Arial"
    r_text.font.size = Pt(9)
    r_text.font.color.rgb = TEXT_COLOR
    return p

def build_docx():
    doc = docx.Document()
    add_header_footer(doc)
    
    # ----------------------------------------------------
    # COVER / TITLE HEADER (Academic Clean Style)
    # ----------------------------------------------------
    p_inst = doc.add_paragraph()
    p_inst.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p_inst.add_run("SMART INDIA HACKATHON 2026\nPROJECT PROGRESS REPORT")
    r.bold = True
    r.font.name = "Arial"
    r.font.size = Pt(11)
    r.font.color.rgb = SUB_COLOR
    
    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_title.paragraph_format.space_before = Pt(4)
    p_title.paragraph_format.space_after = Pt(2)
    r_title = p_title.add_run("JANSAARTHI AI")
    r_title.bold = True
    r_title.font.name = "Arial"
    r_title.font.size = Pt(22)
    r_title.font.color.rgb = HEADING_COLOR
    
    p_sub = doc.add_paragraph()
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sub.paragraph_format.space_after = Pt(8)
    r_sub = p_sub.add_run("AI-Driven Scheme Matching for Marginalized Entrepreneurs\n\"Aapki Zarurat. Sahi Sarkari Raasta.\"")
    r_sub.italic = True
    r_sub.font.name = "Arial"
    r_sub.font.size = Pt(10)
    r_sub.font.color.rgb = SUB_COLOR
    
    # Metadata Box Table (Clean, Simple Academic Table)
    meta_table = doc.add_table(rows=4, cols=2)
    meta_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(meta_table, color="D1D5DB")
    meta_data = [
        ("Problem Statement ID:", "SIH26092"),
        ("Project Title:", "AI-Driven Scheme Matching for Marginalized Entrepreneurs"),
        ("Project Repository:", "https://github.com/Sahil-Kumar-Chaudhary/JANSAARTHI"),
        ("Evaluation Purpose:", "Internal College Academic Progress, Source Implementation & Technical Milestone Review")
    ]
    for i, (k, v) in enumerate(meta_data):
        row = meta_table.rows[i]
        c0, c1 = row.cells[0], row.cells[1]
        c0.width = Inches(2.0)
        c1.width = Inches(4.7)
        set_cell_background(c0, "F8FAFC")
        set_cell_background(c1, "FFFFFF")
        set_cell_margins(c0, 35, 35, 60, 60)
        set_cell_margins(c1, 35, 35, 60, 60)
        
        p0 = c0.paragraphs[0]
        r0 = p0.add_run(k)
        r0.bold = True
        r0.font.name = "Arial"
        r0.font.size = Pt(8.5)
        r0.font.color.rgb = HEADING_COLOR
        
        p1 = c1.paragraphs[0]
        r1 = p1.add_run(v)
        r1.font.name = "Arial"
        r1.font.size = Pt(8.5)
        r1.font.color.rgb = TEXT_COLOR
    
    doc.add_paragraph().paragraph_format.space_after = Pt(4)
    
    # ----------------------------------------------------
    # SECTION 1: TEAM OVERVIEW & INDIVIDUAL RESPONSIBILITIES
    # ----------------------------------------------------
    format_heading(doc, "1. Project Team Members & Assigned Responsibilities", level=1)
    add_p(doc, "The following 5-member student engineering team is assigned to the JANSAARTHI AI project for Smart India Hackathon 2026. The individual contributions and responsibilities are detailed below:", bold_prefix="Team Roster: ")
    
    table = doc.add_table(rows=6, cols=5)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(table, color="D1D5DB")
    headers = ["Member", "Student Name", "Reg. Number", "Assigned Responsibility", "Status"]
    widths = [Inches(0.7), Inches(1.8), Inches(1.1), Inches(2.3), Inches(0.8)]
    
    for j, h in enumerate(headers):
        cell = table.rows[0].cells[j]
        cell.width = widths[j]
        set_cell_background(cell, "F1F5F9")
        set_cell_margins(cell, 45, 45, 50, 50)
        p = cell.paragraphs[0]
        if j in [0, 2, 4]:
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h)
        r.bold = True
        r.font.name = "Arial"
        r.font.size = Pt(8.5)
        r.font.color.rgb = HEADING_COLOR
        
    members_data = [
        ("Member 1", "Tanya Patel", "12305225", "Backend Development, API Integration, Cloud Deployment and DevOps Support", "Completed"),
        ("Member 2", "Sahil Kumar Chaudhary", "12305507", "Android Application Development, Jetpack Compose UI/UX, Navigation & Auth UI, Profile Management, Backend Integration & Localization", "Completed"),
        ("Member 3", "Sunny", "12312528", "Retrieval-Augmented Generation (RAG) Research and Knowledge Retrieval", "In Progress"),
        ("Member 4", "Gourav Saini", "12306706", "Government Scheme Research and Eligibility Criteria Analysis", "In Progress"),
        ("Member 5", "Disha", "12310869", "Financial Assistance and Citizen Requirement Analysis", "In Progress"),
    ]
    
    for i, m in enumerate(members_data):
        row = table.rows[i+1]
        bg = "FFFFFF" if i % 2 == 0 else "F8FAFC"
        for j, val in enumerate(m):
            cell = row.cells[j]
            cell.width = widths[j]
            set_cell_background(cell, bg)
            set_cell_margins(cell, 35, 35, 50, 50)
            p = cell.paragraphs[0]
            if j in [0, 2, 4]:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            r = p.add_run(val)
            r.font.name = "Arial"
            r.font.size = Pt(8)
            r.font.color.rgb = TEXT_COLOR
            if i in [0, 1] and j in [0, 1]:
                r.bold = True
            elif j == 0:
                r.bold = True
                
    doc.add_paragraph().paragraph_format.space_after = Pt(4)
    
    # ----------------------------------------------------
    # INDIVIDUAL MEMBER CONTRIBUTION SECTIONS (ALL 5 MEMBERS)
    # ----------------------------------------------------
    # MEMBER 1: TANYA PATEL
    format_heading(doc, "Member 1 — Backend Development, System Integration & DevOps", level=2)
    m1_box = doc.add_table(rows=1, cols=1)
    m1_box.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(m1_box, color="CBD5E1")
    m1_cell = m1_box.rows[0].cells[0]
    m1_cell.width = Inches(6.7)
    set_cell_background(m1_cell, "F8FAFC")
    set_cell_margins(m1_cell, 60, 60, 80, 80)
    
    pm1 = m1_cell.paragraphs[0]
    rm1 = pm1.add_run("Student Name: Tanya Patel   |   Registration Number: 12305225   |   Role: Backend & DevOps\n")
    rm1.bold = True
    rm1.font.name = "Arial"
    rm1.font.size = Pt(8.5)
    rm1.font.color.rgb = HEADING_COLOR
    
    p1_resp = m1_cell.add_paragraph()
    p1_resp.paragraph_format.space_after = Pt(2)
    r_lbl1 = p1_resp.add_run("Assigned Responsibility: ")
    r_lbl1.bold = True
    r_lbl1.font.name = "Arial"
    r_lbl1.font.size = Pt(8)
    r_lbl1.font.color.rgb = HEADING_COLOR
    r_val1 = p1_resp.add_run("Backend Development, API Integration, Cloud Deployment and DevOps Support")
    r_val1.font.name = "Arial"
    r_val1.font.size = Pt(8)
    r_val1.font.color.rgb = TEXT_COLOR

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
    p1_wk = m1_cell.add_paragraph()
    p1_wk.paragraph_format.space_after = Pt(1)
    r_wk1 = p1_wk.add_run("Work Completed / Contributions:")
    r_wk1.bold = True
    r_wk1.font.name = "Arial"
    r_wk1.font.size = Pt(8)
    r_wk1.font.color.rgb = HEADING_COLOR
    for item in m1_items:
        p_c = m1_cell.add_paragraph()
        p_c.paragraph_format.space_after = Pt(1.5)
        p_c.paragraph_format.line_spacing = 1.15
        rb = p_c.add_run("• ")
        rb.font.name = "Arial"
        rb.font.size = Pt(8)
        rb.font.color.rgb = SUB_COLOR
        r_text = p_c.add_run(item)
        r_text.font.name = "Arial"
        r_text.font.size = Pt(8)
        r_text.font.color.rgb = TEXT_COLOR
        
    doc.add_paragraph().paragraph_format.space_after = Pt(3)

    # MEMBER 2: SAHIL KUMAR CHAUDHARY
    format_heading(doc, "Member 2 — Android Application Development & Integration", level=2)
    m2_box = doc.add_table(rows=1, cols=1)
    m2_box.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(m2_box, color="CBD5E1")
    m2_cell = m2_box.rows[0].cells[0]
    m2_cell.width = Inches(6.7)
    set_cell_background(m2_cell, "F8FAFC")
    set_cell_margins(m2_cell, 60, 60, 80, 80)
    
    pm2 = m2_cell.paragraphs[0]
    rm2 = pm2.add_run("Student Name: Sahil Kumar Chaudhary   |   Registration Number: 12305507   |   Role: Android Lead\n")
    rm2.bold = True
    rm2.font.name = "Arial"
    rm2.font.size = Pt(8.5)
    rm2.font.color.rgb = HEADING_COLOR
    
    p2_resp = m2_cell.add_paragraph()
    p2_resp.paragraph_format.space_after = Pt(2)
    r_lbl2 = p2_resp.add_run("Assigned Responsibility: ")
    r_lbl2.bold = True
    r_lbl2.font.name = "Arial"
    r_lbl2.font.size = Pt(8)
    r_lbl2.font.color.rgb = HEADING_COLOR
    r_val2 = p2_resp.add_run("Android Application Development, Jetpack Compose UI/UX, Navigation & Auth UI, Profile Management, Backend Integration & Localization")
    r_val2.font.name = "Arial"
    r_val2.font.size = Pt(8)
    r_val2.font.color.rgb = TEXT_COLOR

    m2_contributions = [
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
    p2_wk = m2_cell.add_paragraph()
    p2_wk.paragraph_format.space_after = Pt(1)
    r_wk2 = p2_wk.add_run("Work Completed / Contributions:")
    r_wk2.bold = True
    r_wk2.font.name = "Arial"
    r_wk2.font.size = Pt(8)
    r_wk2.font.color.rgb = HEADING_COLOR
    for pref, desc in m2_contributions:
        p_c = m2_cell.add_paragraph()
        p_c.paragraph_format.space_after = Pt(2)
        p_c.paragraph_format.line_spacing = 1.15
        rb = p_c.add_run("• ")
        rb.font.name = "Arial"
        rb.font.size = Pt(8)
        rb.font.color.rgb = SUB_COLOR
        r_pref = p_c.add_run(pref)
        r_pref.bold = True
        r_pref.font.name = "Arial"
        r_pref.font.size = Pt(8)
        r_pref.font.color.rgb = HEADING_COLOR
        r_desc = p_c.add_run(desc)
        r_desc.font.name = "Arial"
        r_desc.font.size = Pt(8)
        r_desc.font.color.rgb = TEXT_COLOR
        
    doc.add_paragraph().paragraph_format.space_after = Pt(3)

    # MEMBER 3: SUNNY
    format_heading(doc, "Member 3 — RAG Research & Preliminary Development", level=2)
    m3_box = doc.add_table(rows=1, cols=1)
    m3_box.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(m3_box, color="CBD5E1")
    m3_cell = m3_box.rows[0].cells[0]
    m3_cell.width = Inches(6.7)
    set_cell_background(m3_cell, "FFFFFF")
    set_cell_margins(m3_cell, 60, 60, 80, 80)
    
    pm3 = m3_cell.paragraphs[0]
    rm3 = pm3.add_run("Student Name: Sunny   |   Registration Number: 12312528\n")
    rm3.bold = True
    rm3.font.name = "Arial"
    rm3.font.size = Pt(8.5)
    rm3.font.color.rgb = HEADING_COLOR
    
    p3_resp = m3_cell.add_paragraph()
    p3_resp.paragraph_format.space_after = Pt(2)
    r_lbl3 = p3_resp.add_run("Assigned Responsibility: ")
    r_lbl3.bold = True
    r_lbl3.font.name = "Arial"
    r_lbl3.font.size = Pt(8)
    r_lbl3.font.color.rgb = HEADING_COLOR
    r_val3 = p3_resp.add_run("Retrieval-Augmented Generation (RAG) Research and Knowledge Retrieval")
    r_val3.font.name = "Arial"
    r_val3.font.size = Pt(8)
    r_val3.font.color.rgb = TEXT_COLOR

    m3_items = [
        "Studied the working principles and architecture of Retrieval-Augmented Generation (RAG).",
        "Explored the use of government scheme documents as a knowledge source.",
        "Researched how relevant information can be retrieved from scheme documents.",
        "Worked on the initial RAG-based information retrieval component.",
        "Explored how retrieved scheme information can support accurate responses to citizen queries.",
        "Assisted in understanding the role of RAG in JANSAARTHI's scheme assistance system."
    ]
    p3_wk = m3_cell.add_paragraph()
    p3_wk.paragraph_format.space_after = Pt(1)
    r_wk3 = p3_wk.add_run("Work Completed:")
    r_wk3.bold = True
    r_wk3.font.name = "Arial"
    r_wk3.font.size = Pt(8)
    r_wk3.font.color.rgb = HEADING_COLOR
    for item in m3_items:
        p_c = m3_cell.add_paragraph()
        p_c.paragraph_format.space_after = Pt(1.5)
        p_c.paragraph_format.line_spacing = 1.15
        rb = p_c.add_run("• ")
        rb.font.name = "Arial"
        rb.font.size = Pt(8)
        rb.font.color.rgb = SUB_COLOR
        r_text = p_c.add_run(item)
        r_text.font.name = "Arial"
        r_text.font.size = Pt(8)
        r_text.font.color.rgb = TEXT_COLOR
        
    doc.add_paragraph().paragraph_format.space_after = Pt(3)

    # MEMBER 4: GOURAV SAINI
    format_heading(doc, "Member 4 — Government Scheme Research & Eligibility", level=2)
    m4_box = doc.add_table(rows=1, cols=1)
    m4_box.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(m4_box, color="CBD5E1")
    m4_cell = m4_box.rows[0].cells[0]
    m4_cell.width = Inches(6.7)
    set_cell_background(m4_cell, "FFFFFF")
    set_cell_margins(m4_cell, 60, 60, 80, 80)
    
    pm4 = m4_cell.paragraphs[0]
    rm4 = pm4.add_run("Student Name: Gourav Saini   |   Registration Number: 12306706\n")
    rm4.bold = True
    rm4.font.name = "Arial"
    rm4.font.size = Pt(8.5)
    rm4.font.color.rgb = HEADING_COLOR
    
    p4_resp = m4_cell.add_paragraph()
    p4_resp.paragraph_format.space_after = Pt(2)
    r_lbl4 = p4_resp.add_run("Assigned Responsibility: ")
    r_lbl4.bold = True
    r_lbl4.font.name = "Arial"
    r_lbl4.font.size = Pt(8)
    r_lbl4.font.color.rgb = HEADING_COLOR
    r_val4 = p4_resp.add_run("Government Scheme Research and Eligibility Criteria Analysis")
    r_val4.font.name = "Arial"
    r_val4.font.size = Pt(8)
    r_val4.font.color.rgb = TEXT_COLOR

    m4_items = [
        "Researched relevant Central and State Government welfare schemes.",
        "Collected scheme eligibility criteria, benefits and required documents.",
        "Categorized schemes based on income, occupation and social category.",
        "Prepared structured scheme information for the application.",
        "Worked on mapping citizen requirements with suitable government schemes."
    ]
    p4_wk = m4_cell.add_paragraph()
    p4_wk.paragraph_format.space_after = Pt(1)
    r_wk4 = p4_wk.add_run("Work Completed:")
    r_wk4.bold = True
    r_wk4.font.name = "Arial"
    r_wk4.font.size = Pt(8)
    r_wk4.font.color.rgb = HEADING_COLOR
    for item in m4_items:
        p_c = m4_cell.add_paragraph()
        p_c.paragraph_format.space_after = Pt(1.5)
        p_c.paragraph_format.line_spacing = 1.15
        rb = p_c.add_run("• ")
        rb.font.name = "Arial"
        rb.font.size = Pt(8)
        rb.font.color.rgb = SUB_COLOR
        r_text = p_c.add_run(item)
        r_text.font.name = "Arial"
        r_text.font.size = Pt(8)
        r_text.font.color.rgb = TEXT_COLOR
        
    doc.add_paragraph().paragraph_format.space_after = Pt(3)

    # MEMBER 5: DISHA
    format_heading(doc, "Member 5 — Financial Assistance & User Requirements", level=2)
    m5_box = doc.add_table(rows=1, cols=1)
    m5_box.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(m5_box, color="CBD5E1")
    m5_cell = m5_box.rows[0].cells[0]
    m5_cell.width = Inches(6.7)
    set_cell_background(m5_cell, "FFFFFF")
    set_cell_margins(m5_cell, 60, 60, 80, 80)
    
    pm5 = m5_cell.paragraphs[0]
    rm5 = pm5.add_run("Student Name: Disha   |   Registration Number: 12310869\n")
    rm5.bold = True
    rm5.font.name = "Arial"
    rm5.font.size = Pt(8.5)
    rm5.font.color.rgb = HEADING_COLOR
    
    p5_resp = m5_cell.add_paragraph()
    p5_resp.paragraph_format.space_after = Pt(2)
    r_lbl5 = p5_resp.add_run("Assigned Responsibility: ")
    r_lbl5.bold = True
    r_lbl5.font.name = "Arial"
    r_lbl5.font.size = Pt(8)
    r_lbl5.font.color.rgb = HEADING_COLOR
    r_val5 = p5_resp.add_run("Financial Assistance and Citizen Requirement Analysis")
    r_val5.font.name = "Arial"
    r_val5.font.size = Pt(8)
    r_val5.font.color.rgb = TEXT_COLOR

    m5_items = [
        "Researched government financial assistance and loan-related schemes.",
        "Identified common financial requirements of citizens and small businesses.",
        "Prepared information about loan limits, subsidies and financial benefits.",
        "Designed the functional requirements for financial assistance features.",
        "Helped identify relevant information required for the funding journey."
    ]
    p5_wk = m5_cell.add_paragraph()
    p5_wk.paragraph_format.space_after = Pt(1)
    r_wk5 = p5_wk.add_run("Work Completed:")
    r_wk5.bold = True
    r_wk5.font.name = "Arial"
    r_wk5.font.size = Pt(8)
    r_wk5.font.color.rgb = HEADING_COLOR
    for item in m5_items:
        p_c = m5_cell.add_paragraph()
        p_c.paragraph_format.space_after = Pt(1.5)
        p_c.paragraph_format.line_spacing = 1.15
        rb = p_c.add_run("• ")
        rb.font.name = "Arial"
        rb.font.size = Pt(8)
        rb.font.color.rgb = SUB_COLOR
        r_text = p_c.add_run(item)
        r_text.font.name = "Arial"
        r_text.font.size = Pt(8)
        r_text.font.color.rgb = TEXT_COLOR
        
    doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # ----------------------------------------------------
    # SECTION 2: EXECUTIVE SUMMARY
    # ----------------------------------------------------
    format_heading(doc, "2. Executive Summary", level=1)
    add_p(doc, "JANSAARTHI AI is an intelligent civic advisory platform created to address Problem Statement SIH26092 for Smart India Hackathon 2026. The platform bridges the massive informational and procedural gap that separates marginalized Indian micro-entrepreneurs—especially street vendors, rural artisans, women entrepreneurs, and Scheduled Caste / Scheduled Tribe (SC/ST) business owners—from central and state government credit and subsidy schemes.", bold_prefix="Project Overview: ")
    add_p(doc, "While flagship initiatives such as the Pradhan Mantri Mudra Yojana (PMMY), Stand-Up India, and PMEGP have substantial budgetary allocations, access remains hindered by scattered portal websites, complex bureaucratic language, lack of eligibility awareness, and exploitation by predatory intermediaries. JANSAARTHI AI eliminates these barriers by providing a unified, bilingual (Hindi/English), mobile-first application that ingests applicant profiles, applies multi-factor deterministic and semantic eligibility algorithms, computes accurate financial projections (EMIs and subsidies), and guides users directly to official channels and nearby authorized banking partners.", bold_prefix="Core Mission & Solution: ")
    add_p(doc, "At the current project milestone, the full-stack architecture is operational: a modern native Kotlin Android client powered by Jetpack Compose is integrated via TLS REST APIs with an Express.js backend and a MongoDB Atlas cloud cluster. The client-side application features authenticated session management, multi-step onboarding, a personalized home dashboard, dynamic profile editing, a mathematical EMI calculator, partner discovery with map routing, and a dedicated notifications hub. Staging tests and 28 automated unit tests have been successfully verified.", bold_prefix="Current Progress: ")

    # ----------------------------------------------------
    # SECTION 3: INTRODUCTION & PROBLEM STATEMENT
    # ----------------------------------------------------
    format_heading(doc, "3. Introduction and Problem Statement", level=1)
    add_p(doc, "The Micro, Small, and Medium Enterprises (MSME) sector contributes approximately 30% of India's GDP and over 45% of total manufacturing output, serving as the largest employer outside agriculture. Within this ecosystem, micro-enterprises account for over 95% of all MSME establishments.", bold_prefix="Economic Context: ")
    add_p(doc, "Despite their economic importance, marginalized entrepreneurs face systemic structural handicaps when seeking institutional finance:", bold_prefix="Key Challenges Identified: ")
    add_bullet(doc, "Government schemes are distributed across disparate ministry portals (MSME, MoF, MoHUA, MoTA, MoSJE), forcing applicants to navigate confusing, fragmented government websites.", bold_prefix="Information Asymmetry & Fragmentation: ")
    add_bullet(doc, "Guidelines are predominantly published in dense legal English or formal Hindi, creating an impassable barrier for micro-entrepreneurs who lack formal higher education or financial literacy.", bold_prefix="Language & Literacy Barriers: ")
    add_bullet(doc, "Entrepreneurs are unable to evaluate whether their caste category, annual income, or enterprise activity satisfies strict eligibility criteria before undergoing lengthy branch visits.", bold_prefix="Ambiguous Eligibility Criteria: ")
    add_bullet(doc, "Applicants frequently face rejections due to incomplete paperwork or lack of awareness regarding essential documents such as Udyam registration, caste certificates, or project reports.", bold_prefix="Documentation Deficits: ")
    add_bullet(doc, "Information vacuums enable unscrupulous agents and touts who charge exorbitant 'processing fees' from vulnerable citizens without guaranteeing loan approvals.", bold_prefix="Intermediary Exploitation: ")

    # ----------------------------------------------------
    # SECTION 4: PROJECT OBJECTIVES
    # ----------------------------------------------------
    format_heading(doc, "4. Project Objectives", level=1)
    add_p(doc, "The primary objective of JANSAARTHI AI is to engineer a dependable, transparent, AI-augmented mobile platform that empowers grassroots entrepreneurs to independently discover, evaluate, and apply for government financial assistance.", bold_prefix="Primary Goal: ")
    add_bullet(doc, "Consolidate central and state financial schemes into a standardized, machine-readable JSON schema updated continuously.", bold_prefix="1. Unified Scheme Repository: ")
    add_bullet(doc, "Implement a two-tiered recommendation engine combining hard demographic filtering with fuzzy NLP requirement parsing.", bold_prefix="2. Deterministic & Semantic Matching: ")
    add_bullet(doc, "Provide transparent match percentages accompanied by plain-language rationales detailing why a scheme fits the user's specific context.", bold_prefix="3. Explainable AI Matching: ")
    add_bullet(doc, "Design an accessible, low-bandwidth native Android application supporting English and Hindi with full Material 3 contrast standards.", bold_prefix="4. Bilingual & Accessible Design: ")
    add_bullet(doc, "Generate personalized checklists of required identity, income, business, and caste documents prior to formal submission.", bold_prefix="5. Document Readiness Assistant: ")
    add_bullet(doc, "Connect entrepreneurs directly to verified nearby public and commercial bank branches with one-touch calling and route mapping.", bold_prefix="6. Geo-Spatial Banking Navigation: ")

    # ----------------------------------------------------
    # SECTION 5: PROPOSED SOLUTION
    # ----------------------------------------------------
    format_heading(doc, "5. Proposed Solution & Value Proposition", level=1)
    add_p(doc, "JANSAARTHI AI delivers a comprehensive, citizen-centric mobile ecosystem designed to simplify government scheme discovery. Instead of navigating dozens of web portals, entrepreneurs interact with a single intuitive application that translates their profile into personalized scheme recommendations with transparent match scores, financial projections, and local bank touchpoints.", bold_prefix="Solution Framework: ")
    add_bullet(doc, "Native Android application supporting Hindi and English with adaptive Light and Dark themes, clean card layouts, and accessible fonts.", bold_prefix="Bilingual, Mobile-First Interface: ")
    add_bullet(doc, "Transparent match percentage scoring (e.g. 98% Mudra match) that explains exact eligibility factors (caste category, income threshold, location).", bold_prefix="Explainable Match Scoring: ")
    add_bullet(doc, "Live EMI calculator with Indian numbering format (₹) and sliders for loan amount, tenure, and interest rate.", bold_prefix="Mathematical Financial Projections: ")
    add_bullet(doc, "Location-aware discovery of nearby authorized commercial bank branches and District Industries Centres (DIC) with one-touch phone and navigation intents.", bold_prefix="Local Banking Guidance: ")
    add_bullet(doc, "RAG-powered chatbot answering citizen queries using official scheme guidelines without hallucination.", bold_prefix="AI-Driven Vernacular Assistant: ")

    # ----------------------------------------------------
    # SECTION 6: APPLICATION WORKFLOW
    # ----------------------------------------------------
    format_heading(doc, "6. Complete End-to-End Application Workflow", level=1)
    add_p(doc, "The operational workflow models the complete citizen journey from application launch to official scheme application submission. Each phase addresses a specific barrier in the scheme discovery lifecycle:", bold_prefix="Process Walkthrough: ")
    
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
        add_bullet(doc, desc, bold_prefix=pref)

    # ----------------------------------------------------
    # SECTION 7: SYSTEM ARCHITECTURE
    # ----------------------------------------------------
    format_heading(doc, "7. Complete System Architecture", level=1)
    add_p(doc, "JANSAARTHI AI employs a modular 5-tier client-server architecture designed for high availability, security, and low-latency mobile execution across India's diverse cellular networks.", bold_prefix="Architectural Overview: ")
    
    arch_img = "JANSAARTHI_REPORT/images/diagram_a_sys_arch.png"
    if os.path.exists(arch_img):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        doc.add_picture(arch_img, width=Inches(5.8))
        p_cap = doc.add_paragraph()
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_c = p_cap.add_run("Figure 7.1: Five-Tier System Architecture Diagram (Client, Network, Cloud Backend, Database, AI Engine)")
        r_c.font.name = "Arial"
        r_c.font.size = Pt(8)
        r_c.italic = True
        r_c.font.color.rgb = SUB_COLOR

    add_bullet(doc, "Native Android application written in Kotlin and Jetpack Compose. Utilizes Single-Activity architecture (MainActivity.kt), MVVM ViewModels, and StateFlow for reactive, unidirectional UI updates.", bold_prefix="1. Client Presentation Tier: ")
    add_bullet(doc, "Retrofit 2 REST HTTP client with OkHttp interceptors providing automatic JWT Bearer token injection, request logging, and TLS 1.3 encryption.", bold_prefix="2. Network & Security Gateway: ")
    add_bullet(doc, "Node.js and Express.js REST API server hosted on Render cloud platform. Enforces JWT authMiddleware.js, Multer multipart processing, and route rate limiting.", bold_prefix="3. Cloud Application Tier: ")
    add_bullet(doc, "MongoDB Atlas managed cloud cluster. Mongoose ODM enforces strict schema validation, unique indexes on mobile numbers, and encrypted password storage.", bold_prefix="4. Cloud Database Tier: ")
    add_bullet(doc, "Hybrid recommendation engine combining rule-based eligibility verification in recommendationController.js with Python RAG retrieval subprocess (ragController.js) querying ChromaDB vector stores.", bold_prefix="5. AI & Intelligence Tier: ")

    # ----------------------------------------------------
    # SECTION 8: WORKFLOW FLOWCHART
    # ----------------------------------------------------
    format_heading(doc, "8. Application Workflow and Decision-Branch Flowchart", level=1)
    add_p(doc, "The following decision-branch flowchart maps the logical transitions executed by the mobile client and backend services as a user navigates from splash launch to final banking handoff:", bold_prefix="Control Flow Specification: ")

    flow_img = "JANSAARTHI_REPORT/images/diagram_d_flowchart.png"
    if os.path.exists(flow_img):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        doc.add_picture(flow_img, width=Inches(5.2))
        p_cap = doc.add_paragraph()
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_c = p_cap.add_run("Figure 8.1: Decision-Branch Application Flowchart (Auth Checks, Onboarding, Matching Logic, Bank Navigation)")
        r_c.font.name = "Arial"
        r_c.font.size = Pt(8)
        r_c.italic = True
        r_c.font.color.rgb = SUB_COLOR

    add_bullet(doc, "TokenManager checks local SharedPreferences. If valid JWT exists, routes to Home; otherwise routes to Login.", bold_prefix="Branch A (Session Token Check): ")
    add_bullet(doc, "Verifies if social category, annual income, and district are present. If missing, prompts profile completion.", bold_prefix="Branch B (Profile Completion Check): ")
    add_bullet(doc, "Discards schemes whose strict criteria (age, caste, income limit) are violated by the user profile.", bold_prefix="Branch C (Eligibility Filter Check): ")
    add_bullet(doc, "Directs user to either physical bank branch (via Map intent) or official portal web link.", bold_prefix="Branch D (Assistance Channel Selection): ")

    # ----------------------------------------------------
    # SECTION 9: TECHNOLOGY STACK
    # ----------------------------------------------------
    format_heading(doc, "9. Technology Stack & Framework Inventory", level=1)
    add_p(doc, "The technological stack is selected for enterprise reliability, native performance, low memory footprint, and rapid scalability:", bold_prefix="Component Stack: ")

    tech_table = doc.add_table(rows=9, cols=4)
    tech_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(tech_table, color="D1D5DB")
    t_headers = ["Tier", "Technology / Framework", "Version", "Functional Purpose in JANSAARTHI"]
    t_widths = [Inches(1.1), Inches(1.8), Inches(0.9), Inches(2.9)]
    for j, h in enumerate(t_headers):
        c = tech_table.rows[0].cells[j]
        c.width = t_widths[j]
        set_cell_background(c, "F1F5F9")
        set_cell_margins(c, 40, 40, 50, 50)
        p = c.paragraphs[0]
        r = p.add_run(h)
        r.bold = True
        r.font.name = "Arial"
        r.font.size = Pt(8.5)
        r.font.color.rgb = HEADING_COLOR
        
    tech_data = [
        ("Mobile Client", "Kotlin & Jetpack Compose", "2.0 / M3", "Declarative UI, responsive layout constraints, modern state management"),
        ("Architecture", "MVVM & Navigation Compose", "2.8.5", "Lifecycle-aware ViewModels, StateFlow, secure AuthGuard routing"),
        ("Networking", "Retrofit 2 & OkHttp 4", "2.11.0", "Type-safe REST client, JWT interceptor, TLS 1.3 encrypted transport"),
        ("Image Handling", "Coil & Android Bitmap SDK", "2.7.0", "Asynchronous profile photo loading, local compression, and Base64 sync"),
        ("Backend API", "Node.js & Express.js", "20.x / 4.19", "Non-blocking asynchronous REST services, JWT security, Multer uploads"),
        ("Database", "MongoDB Atlas & Mongoose", "8.5.2", "Cloud NoSQL database, JSON document models, connection pooling"),
        ("AI / ML Pipeline", "Python, LangChain & ChromaDB", "3.12 / 0.4.x", "Vector embedding of official scheme PDFs, RAG retrieval engine"),
        ("Cloud Hosting", "Render Cloud Infrastructure", "PaaS", "Continuous deployment from GitHub with automated build verification"),
    ]
    for i, row in enumerate(tech_data):
        r_el = tech_table.rows[i+1]
        bg = "FFFFFF" if i % 2 == 0 else "F8FAFC"
        for j, val in enumerate(row):
            c = r_el.cells[j]
            c.width = t_widths[j]
            set_cell_background(c, bg)
            set_cell_margins(c, 30, 30, 50, 50)
            p = c.paragraphs[0]
            r = p.add_run(val)
            r.font.name = "Arial"
            r.font.size = Pt(8)
            r.font.color.rgb = TEXT_COLOR
            if j == 0:
                r.bold = True

    doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # ----------------------------------------------------
    # SECTION 10: ANDROID APPLICATION IMPLEMENTATION
    # ----------------------------------------------------
    format_heading(doc, "10. Android Application Implementation Details", level=1)
    add_p(doc, "The Android application is built with 100% Kotlin code utilizing modern Jetpack Compose principles without legacy XML views. Key architectural features include:", bold_prefix="Implementation Highlights: ")
    add_bullet(doc, "The MainActivity.kt establishes edge-to-edge window insets (enableEdgeToEdge), sets up the JANSAARTHI theme, and renders the central NavHost graph.", bold_prefix="Single-Activity Architecture: ")
    add_bullet(doc, "Navigation Compose declares routes for splash, login, register, home, schemes, calculator, partners, profile, notifications, and sub-profile editors. Custom AuthGuard composables enforce token verification on all protected routes.", bold_prefix="Navigation & AuthGuard: ")
    add_bullet(doc, "RegisterScreen.kt implements a 4-step progressive onboarding sequence (Introduction -> Basic Details -> Additional Details -> Create Password). A shared RegistrationFormState preserves entered field values across back-and-forth navigation.", bold_prefix="Multi-Step Registration Flow: ")
    add_bullet(doc, "HomeScreen.kt features a personalized greeting header with live user name, profile avatar, AI assistant banner with RAG chip, 2x2 Quick Services grid, and PM Mudra featured scheme card.", bold_prefix="Home Dashboard: ")
    add_bullet(doc, "CalculatorScreen.kt allows micro-entrepreneurs to calculate monthly loan EMIs using the exact banking formula [P x R x (1+R)^N]/[(1+R)^N - 1] with live interactive sliders and Indian currency formatting (₹).", bold_prefix="Financial EMI Calculator: ")
    add_bullet(doc, "PartnersScreen.kt displays nearby authorized public and private banks offering MSME credit. Includes one-touch Android Intent actions for direct telephone calling (Intent.ACTION_DIAL) and Google Maps navigation (Intent.ACTION_VIEW).", bold_prefix="Partner Center Discovery: ")
    add_bullet(doc, "NotificationScreen.kt provides an operational notification hub featuring unread badges, category filter chips (All, Unread, Schemes), mark-as-read toggles, and direct deep-linking into scheme recommendations.", bold_prefix="Notification Hub: ")

    # ----------------------------------------------------
    # SECTION 11: BACKEND IMPLEMENTATION
    # ----------------------------------------------------
    format_heading(doc, "11. Backend Implementation & Cloud Services", level=1)
    add_p(doc, "The backend tier is built on Node.js and Express.js, hosted on Render cloud infrastructure, providing secure, stateless REST APIs:", bold_prefix="Backend Highlights: ")
    add_bullet(doc, "Encapsulated in authRoutes.js and authController.js. Handles user registration, mobile number uniqueness verification, password hashing, and login token issuance.", bold_prefix="Authentication Controller: ")
    add_bullet(doc, "Implemented in profileRoutes.js. Allows authenticated users to query and update demographic details, income ranges, caste category, and upload compressed profile photos via Multer.", bold_prefix="Profile Controller: ")
    add_bullet(doc, "recommendationController.js executes multi-factor scoring against central and state scheme databases based on applicant category, income, and business purpose.", bold_prefix="Recommendation Controller: ")
    add_bullet(doc, "ragController.js bridges the Node.js backend with Python RAG retrieval scripts, enabling natural-language citizen inquiries against official government guidelines.", bold_prefix="RAG Subprocess Controller: ")

    # ----------------------------------------------------
    # SECTION 12: MONGODB DATABASE DESIGN
    # ----------------------------------------------------
    format_heading(doc, "12. MongoDB Database Design & Schema Architecture", level=1)
    add_p(doc, "The database layer is deployed on MongoDB Atlas managed cloud infrastructure. The primary document schema in User.js models citizen identities, demographic attributes, and business parameters.", bold_prefix="Database Architecture: ")

    db_img = "JANSAARTHI_REPORT/images/diagram_c_database.png"
    if os.path.exists(db_img):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        doc.add_picture(db_img, width=Inches(5.8))
        p_cap = doc.add_paragraph()
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_c = p_cap.add_run("Figure 12.1: MongoDB Atlas Database Schema & Entity Relationships")
        r_c.font.name = "Arial"
        r_c.font.size = Pt(8)
        r_c.italic = True
        r_c.font.color.rgb = SUB_COLOR

    add_p(doc, "The users collection includes the following structured field categories:", bold_prefix="Schema Field Breakdown: ")
    add_bullet(doc, "name (String, required), mobile (String, unique, indexed), email (String, sparse index), passwordHash (String, bcrypt hashed).", bold_prefix="Identity & Security: ")
    add_bullet(doc, "age (Number), dob (String), gender (String), state (String), district (String), scCategory (String: General, OBC, SC, ST, EWS).", bold_prefix="Demographic Profile: ")
    add_bullet(doc, "annualFamilyIncome (Number), occupation (String), businessType (String), businessRegistrationStatus (String), purpose (String), requestedAmount (Number), projectCost (Number), educationLevel (String), areaType (String: Urban/Rural).", bold_prefix="Business & Financial Data: ")
    add_bullet(doc, "verificationDocType (String), verificationDocUrl (String), profilePhoto (String: Base64 data).", bold_prefix="Media & Verification: ")
    add_bullet(doc, "language (String, default: 'EN'), theme (String, default: 'SYSTEM'), notificationsEnabled (Boolean, default: true).", bold_prefix="Preferences: ")

    # ----------------------------------------------------
    # SECTION 13: AUTHENTICATION AND SECURITY
    # ----------------------------------------------------
    format_heading(doc, "13. Authentication and Security Architecture", level=1)
    add_p(doc, "Security is engineered into every tier of the JANSAARTHI platform following defense-in-depth principles:", bold_prefix="Security Posture: ")
    add_bullet(doc, "Passwords are never stored in plaintext. They are salted and hashed on the backend using bcrypt with a work factor of 10 rounds prior to database insertion.", bold_prefix="1. Password Hashing: ")
    add_bullet(doc, "Successful authentication issues an HMAC-SHA256 signed JSON Web Token containing the user ID and expiration timestamp, sent via Authorization: Bearer <token> headers.", bold_prefix="2. Stateless JWT Tokens: ")
    add_bullet(doc, "TokenManager.kt stores the JWT token within Android's private SharedPreferences, accessible only by the application's Linux sandbox UID.", bold_prefix="3. Client Token Storage: ")
    add_bullet(doc, "The mobile navigation graph wraps all protected routes in an AuthGuard check. If the session is invalid, the user is rerouted to Login with the entire backstack popped to prevent back-button bypasses.", bold_prefix="4. Compose AuthGuard: ")
    add_bullet(doc, "Strict validation regex patterns enforce 10-digit Indian mobile numbers (^[6-9][0-9]{9}$), password complexity (8+ chars, uppercase, lowercase, numeric), and date formatting.", bold_prefix="5. Input Sanitization & Validation: ")

    # ----------------------------------------------------
    # SECTION 14: SCHEME DISCOVERY & MATCHING
    # ----------------------------------------------------
    format_heading(doc, "14. Scheme Discovery and Matching Approach", level=1)
    add_p(doc, "JANSAARTHI AI implements a multi-stage scheme discovery pipeline that processes applicant data through sequential validation and scoring filters:", bold_prefix="Matching Methodology: ")

    pipe_img = "JANSAARTHI_REPORT/images/diagram_b_pipeline.png"
    if os.path.exists(pipe_img):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        doc.add_picture(pipe_img, width=Inches(5.8))
        p_cap = doc.add_paragraph()
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_c = p_cap.add_run("Figure 14.1: Eight-Step Scheme Discovery, Matching, and RAG Intelligence Pipeline")
        r_c.font.name = "Arial"
        r_c.font.size = Pt(8)
        r_c.italic = True
        r_c.font.color.rgb = SUB_COLOR

    add_bullet(doc, "Ingests 14 verified applicant data points from MongoDB.", bold_prefix="Stage 1 — Profile Ingestion: ")
    add_bullet(doc, "Eliminates programs whose caste, age, income, or gender rules are strictly violated.", bold_prefix="Stage 2 — Hard Demographic Filtering: ")
    add_bullet(doc, "Evaluates whether requested capital matches scheme loan brackets (Shishu, Kishore, Tarun).", bold_prefix="Stage 3 — Business Purpose Matching: ")
    add_bullet(doc, "Computes transparent match percentage based on weighted attribute alignment.", bold_prefix="Stage 4 — Dynamic Scoring: ")
    add_bullet(doc, "Generates human-readable rationales explaining why the scheme was recommended.", bold_prefix="Stage 5 — Explainability Engine: ")

    # ----------------------------------------------------
    # SECTION 15: ELIGIBILITY ASSESSMENT APPROACH
    # ----------------------------------------------------
    format_heading(doc, "15. Multi-Factor Eligibility Assessment Approach", level=1)
    add_p(doc, "The eligibility engine models both binary gate constraints and gradient preference scoring:", bold_prefix="Dual Evaluation Logic: ")
    add_bullet(doc, "Mandatory qualifying criteria where failure results in immediate disqualification (e.g. non-SC/ST applicants disqualified from Stand-Up India unless female; applicants exceeding income caps disqualified from targeted subsidies).", bold_prefix="1. Hard Threshold Rules: ")
    add_bullet(doc, "Additive scoring parameters that elevate recommendations for vulnerable groups (e.g., annual family income under ₹3,00,000 adds 15% priority score; women entrepreneurs receive enhanced ranking for Mudra Tarun).", bold_prefix="2. Soft Weighting Rules: ")

    # ----------------------------------------------------
    # SECTION 16: APPLICATION READINESS & DOCUMENTS
    # ----------------------------------------------------
    format_heading(doc, "16. Application Readiness and Document Management", level=1)
    add_p(doc, "To prevent application rejections caused by missing paperwork, JANSAARTHI AI introduces an automated Document Readiness Assistant:", bold_prefix="Document Verification Framework: ")
    add_bullet(doc, "Aadhaar Card (mandatory for UIDAI biometric authentication), Voter ID, PAN Card.", bold_prefix="Tier 1 — Identity Documents: ")
    add_bullet(doc, "Bank statement for last 6 months, Income Certificate from Tehsildar, ITR acknowledgments (if filed).", bold_prefix="Tier 2 — Financial Documents: ")
    add_bullet(doc, "Udyam Registration Certificate, Shop & Establishment Act license, trade license.", bold_prefix="Tier 3 — Business Enterprise Proof: ")
    add_bullet(doc, "Caste certificate issued by competent revenue authority for SC/ST/OBC applicants.", bold_prefix="Tier 4 — Category Proof: ")

    # ----------------------------------------------------
    # SECTION 17: COMPLETE API AND DATA FLOW
    # ----------------------------------------------------
    format_heading(doc, "17. Complete API and Data Flow Directory", level=1)
    add_p(doc, "The table below documents all active REST API endpoints implemented in the backend service:", bold_prefix="REST Endpoint Specifications: ")

    api_table = doc.add_table(rows=8, cols=6)
    api_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(api_table, color="D1D5DB")
    api_headers = ["Method", "Endpoint URL", "Auth", "Payload / Parameters", "Response Object", "HTTP"]
    api_widths = [Inches(0.7), Inches(1.8), Inches(0.5), Inches(2.0), Inches(1.1), Inches(0.6)]
    for j, h in enumerate(api_headers):
        c = api_table.rows[0].cells[j]
        c.width = api_widths[j]
        set_cell_background(c, "F1F5F9")
        set_cell_margins(c, 35, 35, 40, 40)
        p = c.paragraphs[0]
        if j in [0, 2, 5]:
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h)
        r.bold = True
        r.font.name = "Arial"
        r.font.size = Pt(8)
        r.font.color.rgb = HEADING_COLOR
        
    api_raw = [
        ("POST", "/api/auth/register", "No", "name, mobile, password, state, district, scCategory, ...", "user, token", "201 Created"),
        ("POST", "/api/auth/login", "No", "mobile, password", "user, token", "200 OK"),
        ("GET", "/api/auth/me", "Yes", "None (Bearer JWT in header)", "user profile object", "200 OK"),
        ("POST", "/api/auth/logout", "Yes", "None (invalidates client token)", "{ message: 'Logged out' }", "200 OK"),
        ("GET", "/api/profile", "Yes", "None (Bearer JWT in header)", "comprehensive user profile", "200 OK"),
        ("PUT", "/api/profile", "Yes", "Partial user fields to update", "updated user object", "200 OK"),
        ("POST", "/api/profile/photo", "Yes", "Multipart form: 'photo' file", "{ photoUrl: '...' }", "200 OK"),
    ]
    for i, row in enumerate(api_raw):
        r_el = api_table.rows[i+1]
        bg = "FFFFFF" if i % 2 == 0 else "F8FAFC"
        for j, val in enumerate(row):
            c = r_el.cells[j]
            c.width = api_widths[j]
            set_cell_background(c, bg)
            set_cell_margins(c, 25, 25, 40, 40)
            p = c.paragraphs[0]
            if j in [0, 2, 5]:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            r = p.add_run(val)
            r.font.name = "Arial"
            r.font.size = Pt(7.5)
            r.font.color.rgb = TEXT_COLOR
            if j == 0:
                r.bold = True

    doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # ----------------------------------------------------
    # SECTION 18: SOURCE CODE DIRECTORY
    # ----------------------------------------------------
    format_heading(doc, "18. Source Code and File Structure Inventory", level=1)
    add_p(doc, "The following directory lists 33 verified source code files implemented in the JANSAARTHI repository across Android, Backend, and AI layers:", bold_prefix="Repository Inventory: ")

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
    t_f = doc.add_table(rows=len(files_list)+1, cols=4)
    t_f.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_f, color="D1D5DB")
    fl_headers = ["File Name", "Technology / Layer", "Functional Purpose in JANSAARTHI", "Status"]
    fl_widths = [Inches(1.7), Inches(1.3), Inches(2.8), Inches(0.9)]
    for j, h in enumerate(fl_headers):
        c = t_f.rows[0].cells[j]
        c.width = fl_widths[j]
        set_cell_background(c, "F1F5F9")
        set_cell_margins(c, 30, 30, 40, 40)
        p = c.paragraphs[0]
        if j == 3:
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h)
        r.bold = True
        r.font.name = "Arial"
        r.font.size = Pt(8)
        r.font.color.rgb = HEADING_COLOR
        
    for i, (fn, tech, purp, st) in enumerate(files_list):
        r_el = t_f.rows[i+1]
        bg = "FFFFFF" if i % 2 == 0 else "F8FAFC"
        for j, val in enumerate([fn, tech, purp, st]):
            c = r_el.cells[j]
            c.width = fl_widths[j]
            set_cell_background(c, bg)
            set_cell_margins(c, 20, 20, 40, 40)
            p = c.paragraphs[0]
            if j == 3:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            r = p.add_run(val)
            r.font.name = "Arial"
            r.font.size = Pt(7.5)
            r.font.color.rgb = TEXT_COLOR
            if j == 0:
                r.bold = True

    doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # ----------------------------------------------------
    # SECTION 19: SCREENSHOTS (Clean 2-Column Grid)
    # ----------------------------------------------------
    format_heading(doc, "19. Application Verification Screenshots", level=1)
    add_p(doc, "The following 10 screenshots represent authentic captures from the live Android application executing on a physical Android test device (OPPO CPH2333, ColorOS / Android 13). They confirm full operational readiness of all core user flows:", bold_prefix="Physical Device Execution Proof: ")

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
    s_table = doc.add_table(rows=5, cols=2)
    s_table.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(s_table, color="E2E8F0")
    for idx, (img_name, cap) in enumerate(screenshots):
        row_idx = idx // 2
        col_idx = idx % 2
        cell = s_table.rows[row_idx].cells[col_idx]
        cell.width = Inches(3.3)
        set_cell_margins(cell, 30, 30, 40, 40)
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        img_path = os.path.join("JANSAARTHI_REPORT", "images", img_name)
        if os.path.exists(img_path):
            p.add_run().add_picture(img_path, width=Inches(1.8))
        p_cap = cell.add_paragraph()
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        rc = p_cap.add_run(cap)
        rc.font.name = "Arial"
        rc.font.size = Pt(7.5)
        rc.italic = True
        rc.font.color.rgb = SUB_COLOR

    doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # ----------------------------------------------------
    # SECTION 20: CURRENT IMPLEMENTATION STATUS
    # ----------------------------------------------------
    format_heading(doc, "20. Current Implementation Status Matrix", level=1)
    add_p(doc, "The matrix below provides an honest, verified evaluation of each module within the JANSAARTHI AI platform:", bold_prefix="Module Status Assessment: ")

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
    t_mat = doc.add_table(rows=len(matrix_data)+1, cols=3)
    t_mat.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(t_mat, color="D1D5DB")
    m_headers = ["System Module / Capability", "Current Status", "Technical Verification & Evidence"]
    m_widths = [Inches(1.8), Inches(1.1), Inches(3.8)]
    for j, h in enumerate(m_headers):
        c = t_mat.rows[0].cells[j]
        c.width = m_widths[j]
        set_cell_background(c, "F1F5F9")
        set_cell_margins(c, 30, 30, 40, 40)
        p = c.paragraphs[0]
        if j == 1:
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h)
        r.bold = True
        r.font.name = "Arial"
        r.font.size = Pt(8)
        r.font.color.rgb = HEADING_COLOR
        
    for i, (mod, st, det) in enumerate(matrix_data):
        r_el = t_mat.rows[i+1]
        bg = "FFFFFF" if i % 2 == 0 else "F8FAFC"
        for j, val in enumerate([mod, st, det]):
            c = r_el.cells[j]
            c.width = m_widths[j]
            set_cell_background(c, bg)
            set_cell_margins(c, 25, 25, 40, 40)
            p = c.paragraphs[0]
            if j == 1:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            r = p.add_run(val)
            r.font.name = "Arial"
            r.font.size = Pt(7.5)
            r.font.color.rgb = TEXT_COLOR
            if j == 0:
                r.bold = True

    doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # ----------------------------------------------------
    # SECTION 21: TESTING AND VERIFICATION
    # ----------------------------------------------------
    format_heading(doc, "21. Testing and Verification", level=1)
    add_p(doc, "The JANSAARTHI platform has undergone rigorous multi-tier testing across unit, integration, and physical hardware layers:", bold_prefix="Testing Methodology: ")
    add_bullet(doc, "28 automated unit tests executed via Gradle (testDebugUnitTest) verifying registration form validation (RegistrationFlowUnitTest.kt), markdown rendering (FormattedMessageTextTest.kt), and query construction (RagIntegrationTest.kt). Result: 28/28 Passed (BUILD SUCCESSFUL).", bold_prefix="1. Android Unit Tests: ")
    add_bullet(doc, "All REST routes tested via curl and Postman against staging deployment on Render. Auth tokens, registration payloads, and profile photo multipart streams verified.", bold_prefix="2. Backend Integration: ")
    add_bullet(doc, "Successfully installed and tested app-debug.apk on OPPO CPH2333 (Android 13, ColorOS). Verified touch targets, soft keyboard handling (imePadding), gesture navigation bar insets, and notifications hub stability.", bold_prefix="3. Hardware Testing: ")

    # ----------------------------------------------------
    # SECTION 22: FUTURE DEVELOPMENT ROADMAP
    # ----------------------------------------------------
    format_heading(doc, "22. Future Development Roadmap", level=1)
    add_p(doc, "The future research and product milestones are organized into three sequential development phases:", bold_prefix="Development Timeline: ")
    add_bullet(doc, "An intelligent diagnostic engine that explains exactly why an applicant does not qualify for a specific high-value scheme and suggests concrete remedial actions (e.g. obtaining Udyam registration or credit repair).", bold_prefix="Milestone 1 — AI Eligibility Gap Analyzer: ")
    add_bullet(doc, "Interactive side-by-side comparative views highlighting interest rate differences, subsidy percentages, and collateral rules between competing schemes.", bold_prefix="Milestone 2 — Multi-Scheme Comparison Matrix: ")
    add_bullet(doc, "Sequential milestone tracker allowing citizens to monitor document collection, bank branch appraisal, and disbursement status in real time.", bold_prefix="Milestone 3 — Application Readiness Tracker: ")
    add_bullet(doc, "Automated synchronization with official government open data portals (data.gov.in) to ingest newly announced schemes and revised guidelines.", bold_prefix="Milestone 4 — Real-Time Scheme API Ingestion: ")
    add_bullet(doc, "Integration of Automatic Speech Recognition (ASR) and Text-to-Speech (TTS) in regional languages (Hindi, Bhojpuri, Maithili, Bengali) for non-literate micro-entrepreneurs.", bold_prefix="Milestone 5 — Voice-First Vernacular Dialogue: ")
    add_bullet(doc, "GPS-assisted routing to nearby Common Service Centers (CSCs), District Industries Centres (DIC), and specialized SME bank branches.", bold_prefix="Milestone 6 — Geo-Spatial Banking & CSC Routing: ")

    # ----------------------------------------------------
    # SECTION 23: CONCLUSION
    # ----------------------------------------------------
    format_heading(doc, "23. Academic Conclusion", level=1)
    add_p(doc, "The JANSAARTHI AI project addresses one of the most pressing socio-economic challenges identified in Smart India Hackathon 2026: bridging the persistent information and accessibility chasm that disconnects marginalized Indian entrepreneurs from vital government financial support. At the current evaluation milestone, the project has established a highly robust, secure, and production-grade native Android client integrated with a scalable cloud backend and MongoDB database.", bold_prefix="Milestone Synthesis: ")
    add_p(doc, "With full authentication security, dynamic multi-step onboarding, interactive financial calculators, dedicated notification mechanisms, and live physical device verification successfully concluded, the foundation is fully prepared for final AI matching pipeline refinements and pilot field trials. JANSAARTHI AI demonstrates high technical feasibility, academic rigor, and immense potential to empower millions of grassroots micro-enterprises across the nation.", bold_prefix="Societal Impact: ")

    # Safe Save: save updated document
    main_docx = "JANSAARTHI_REPORT/JANSAARTHI_Professional_Progress_Report_Updated.docx"
    doc.save(main_docx)
    print(f"Successfully generated DOCX: {main_docx}")

    for target in ["JANSAARTHI_REPORT/JANSAARTHI_Professional_Progress_Report.docx",
                   "JANSAARTHI_REPORT/JANSAARTHI_Project_Progress_Report.docx"]:
        try:
            doc.save(target)
            print(f"Successfully updated: {target}")
        except PermissionError:
            print(f"Note: {target} is currently open in Word. It is saved in {main_docx}.")

if __name__ == '__main__':
    build_docx()
