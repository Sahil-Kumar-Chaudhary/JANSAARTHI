import matplotlib.pyplot as plt
import matplotlib.patches as patches

def create_architecture_diagram():
    fig, ax = plt.subplots(figsize=(10, 8), dpi=300)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    # Colors
    navy = '#17243A'
    gold = '#C89124'
    light_gold = '#FAF2DE'
    light_blue = '#EAF0F8'
    green = '#16845B'
    gray = '#657080'

    # Title
    ax.text(50, 96, "JANSAARTHI AI — SYSTEM ARCHITECTURE & PIPELINE", 
            fontsize=15, fontweight='bold', color=navy, ha='center', va='center')
    ax.text(50, 92.5, "Smart India Hackathon 2026 | Problem Statement: SIH26092", 
            fontsize=10, color=gray, ha='center', va='center')

    # Left Container: Client & Full-Stack Infrastructure (Implemented)
    ax.add_patch(patches.FancyBboxPatch((4, 8), 43, 80, boxstyle="round,pad=1.5", 
                                        edgecolor=navy, facecolor=light_blue, linewidth=1.8, linestyle='-'))
    ax.text(25.5, 85, "1. FULL-STACK CORE SYSTEM\n[IMPLEMENTED & OPERATIONAL]", 
            fontsize=11, fontweight='bold', color=navy, ha='center', va='center')

    # Client Box
    ax.add_patch(patches.FancyBboxPatch((8, 68), 35, 12, boxstyle="round,pad=1", 
                                        edgecolor=gold, facecolor='#FFFFFF', linewidth=1.5))
    ax.text(25.5, 75, "Android Client Application", fontsize=10, fontweight='bold', color=navy, ha='center')
    ax.text(25.5, 71, "Kotlin | Jetpack Compose | Material 3\nMVVM | StateFlow | Navigation Compose", 
            fontsize=7.5, color=gray, ha='center')

    # Arrow Down
    ax.annotate('', xy=(25.5, 60), xytext=(25.5, 68),
                arrowprops=dict(facecolor=navy, edgecolor=navy, width=1.5, headwidth=6))
    ax.text(28, 64, "Retrofit / HTTPS REST", fontsize=7.5, color=navy, fontweight='semibold')

    # Backend Box
    ax.add_patch(patches.FancyBboxPatch((8, 48), 35, 12, boxstyle="round,pad=1", 
                                        edgecolor=navy, facecolor='#FFFFFF', linewidth=1.5))
    ax.text(25.5, 55, "Node.js & Express REST Backend", fontsize=10, fontweight='bold', color=navy, ha='center')
    ax.text(25.5, 51, "JWT Auth | bcrypt | CORS | Staging Deployment\nRender Cloud Hosting Platform", 
            fontsize=7.5, color=gray, ha='center')

    # Arrow Down
    ax.annotate('', xy=(25.5, 40), xytext=(25.5, 48),
                arrowprops=dict(facecolor=navy, edgecolor=navy, width=1.5, headwidth=6))
    ax.text(28, 44, "Mongoose ODM", fontsize=7.5, color=navy, fontweight='semibold')

    # Database Box
    ax.add_patch(patches.FancyBboxPatch((8, 28), 35, 12, boxstyle="round,pad=1", 
                                        edgecolor=green, facecolor='#FFFFFF', linewidth=1.5))
    ax.text(25.5, 35, "MongoDB Atlas Cloud Database", fontsize=10, fontweight='bold', color=green, ha='center')
    ax.text(25.5, 31, "User Accounts | Personal & Business Profiles\nSecure Hashed Passwords & Schemes Data", 
            fontsize=7.5, color=gray, ha='center')

    # Local Cache / Token Manager
    ax.add_patch(patches.FancyBboxPatch((8, 12), 35, 11, boxstyle="round,pad=1", 
                                        edgecolor=gold, facecolor='#FFFFFF', linewidth=1.2))
    ax.text(25.5, 18.5, "Local Session & Security Layer", fontsize=9, fontweight='bold', color=navy, ha='center')
    ax.text(25.5, 15, "Encrypted TokenManager | AuthGuard Navigation\nBi-directional Profile Synchronization", 
            fontsize=7.5, color=gray, ha='center')

    # Right Container: AI Intelligence & Matching Pipeline
    ax.add_patch(patches.FancyBboxPatch((53, 8), 43, 80, boxstyle="round,pad=1.5", 
                                        edgecolor=gold, facecolor=light_gold, linewidth=1.8, linestyle='--'))
    ax.text(74.5, 85, "2. AI SCHEME MATCHING PIPELINE\n[IMPLEMENTED & PROPOSED STAGES]", 
            fontsize=11, fontweight='bold', color=navy, ha='center', va='center')

    # Pipeline Step 1
    ax.add_patch(patches.FancyBboxPatch((57, 73), 35, 8.5, boxstyle="round,pad=0.8", 
                                        edgecolor=navy, facecolor='#FFFFFF', linewidth=1.2))
    ax.text(74.5, 78, "User Profile & Requirements Input", fontsize=9, fontweight='bold', color=navy, ha='center')
    ax.text(74.5, 75, "Category, Income, Location, Need (Implemented)", fontsize=7.5, color=green, ha='center', fontweight='semibold')

    # Arrow Down
    ax.annotate('', xy=(74.5, 68), xytext=(74.5, 73),
                arrowprops=dict(facecolor=navy, edgecolor=navy, width=1.2, headwidth=5))

    # Pipeline Step 2
    ax.add_patch(patches.FancyBboxPatch((57, 60), 35, 8, boxstyle="round,pad=0.8", 
                                        edgecolor=navy, facecolor='#FFFFFF', linewidth=1.2))
    ax.text(74.5, 65, "NLP & Query Understanding", fontsize=9, fontweight='bold', color=navy, ha='center')
    ax.text(74.5, 62, "Intent Classification & Entity Extraction (Proposed/WIP)", fontsize=7.5, color='#B7791F', ha='center', fontweight='semibold')

    # Arrow Down
    ax.annotate('', xy=(74.5, 55), xytext=(74.5, 60),
                arrowprops=dict(facecolor=navy, edgecolor=navy, width=1.2, headwidth=5))

    # Pipeline Step 3
    ax.add_patch(patches.FancyBboxPatch((57, 47), 35, 8, boxstyle="round,pad=0.8", 
                                        edgecolor=navy, facecolor='#FFFFFF', linewidth=1.2))
    ax.text(74.5, 52, "RAG Knowledge Retrieval Engine", fontsize=9, fontweight='bold', color=navy, ha='center')
    ax.text(74.5, 49, "Official Scheme Rules & Embeddings (Implemented/Staging)", fontsize=7.5, color=green, ha='center', fontweight='semibold')

    # Arrow Down
    ax.annotate('', xy=(74.5, 42), xytext=(74.5, 47),
                arrowprops=dict(facecolor=navy, edgecolor=navy, width=1.2, headwidth=5))

    # Pipeline Step 4
    ax.add_patch(patches.FancyBboxPatch((57, 34), 35, 8, boxstyle="round,pad=0.8", 
                                        edgecolor=navy, facecolor='#FFFFFF', linewidth=1.2))
    ax.text(74.5, 39, "Multi-Factor Eligibility Engine", fontsize=9, fontweight='bold', color=navy, ha='center')
    ax.text(74.5, 36, "Hard Rules + Demographic Filters (Implemented)", fontsize=7.5, color=green, ha='center', fontweight='semibold')

    # Arrow Down
    ax.annotate('', xy=(74.5, 29), xytext=(74.5, 34),
                arrowprops=dict(facecolor=navy, edgecolor=navy, width=1.2, headwidth=5))

    # Pipeline Step 5
    ax.add_patch(patches.FancyBboxPatch((57, 21), 35, 8, boxstyle="round,pad=0.8", 
                                        edgecolor=navy, facecolor='#FFFFFF', linewidth=1.2))
    ax.text(74.5, 26, "Ranked Recommendations & Match %", fontsize=9, fontweight='bold', color=navy, ha='center')
    ax.text(74.5, 23, "Dynamic Mudra, Stand-Up, PMEGP Scoring (Implemented)", fontsize=7.5, color=green, ha='center', fontweight='semibold')

    # Arrow Down
    ax.annotate('', xy=(74.5, 16), xytext=(74.5, 21),
                arrowprops=dict(facecolor=navy, edgecolor=navy, width=1.2, headwidth=5))

    # Pipeline Step 6
    ax.add_patch(patches.FancyBboxPatch((57, 8.5), 35, 7.5, boxstyle="round,pad=0.8", 
                                        edgecolor=gold, facecolor='#FFFFFF', linewidth=1.2))
    ax.text(74.5, 13.5, "Personalized Action Plan & Bank Guidance", fontsize=9, fontweight='bold', color=navy, ha='center')
    ax.text(74.5, 10.5, "Document Checklist & Partner Bank Locator (In Progress)", fontsize=7.5, color='#B7791F', ha='center', fontweight='semibold')

    # Inter-container connection arrow
    ax.annotate('', xy=(53, 54), xytext=(47, 54),
                arrowprops=dict(facecolor=gold, edgecolor=gold, width=2, headwidth=7))
    ax.text(50, 56.5, "Data Sync", fontsize=7.5, color=gold, fontweight='bold', ha='center')

    # Legend at bottom
    ax.text(50, 3, "Legend: [Solid Blue Box] = Implemented System Components  |  [Dashed Gold Box] = AI Pipeline Workflow", 
            fontsize=8.5, color=navy, ha='center', fontweight='semibold')

    plt.tight_layout()
    plt.savefig('JANSAARTHI_REPORT/images/architecture_diagram.png', bbox_inches='tight', dpi=300)
    plt.close()
    print("Saved architecture diagram.")

def create_flowchart_diagram():
    fig, ax = plt.subplots(figsize=(9, 13), dpi=300)
    ax.set_xlim(0, 100)
    ax.set_ylim(0, 100)
    ax.axis('off')

    navy = '#17243A'
    gold = '#C89124'
    green = '#16845B'
    gray = '#4A5568'

    # Title
    ax.text(50, 98, "JANSAARTHI AI — END-TO-END PROCESS WORKFLOW", 
            fontsize=14, fontweight='bold', color=navy, ha='center', va='center')
    ax.text(50, 95.8, "User Journey: From Scheme Discovery to Official Application Submission", 
            fontsize=9.5, color=gray, ha='center', va='center')

    steps = [
        ("Start", "Terminal / User Launches JANSAARTHI Application", green, 91.5, True),
        ("Open JANSAARTHI", "Animated Splash Screen & Saved Session Check", navy, 84.5, False),
        ("Login / Register", "Mobile OTP / Password Auth & Multi-Step Onboarding", navy, 77.5, False),
        ("Create User Profile", "Basic Demographics, Category, Location, Income & Occupation", navy, 70.5, False),
        ("Enter Requirement", "Business Needs, Loan Amount, Educational or Asset Goals", navy, 63.5, False),
        ("Analyze User Information", "Extract Criteria & Compare against Stored Scheme Criteria", navy, 56.5, False),
        ("Find Relevant Schemes", "Query Catalog of Central & State Schemes (Mudra, Stand-Up, etc.)", navy, 49.5, False),
        ("Eligibility Assessment", "Rule-Engine Verification + Multi-Parameter Scoring", gold, 42.5, False),
        ("Display Recommendations", "Ranked Scheme Cards with Match Score %, Subsidies & Benefits", navy, 35.5, False),
        ("View Scheme Details", "In-depth Ministry Guidelines, Interest Rates & Tenures", navy, 28.5, False),
        ("Prepare Required Documents", "Automated Personalized Checklist (Aadhaar, ITR, Project Report)", navy, 21.5, False),
        ("Generate Application Action Plan", "Step-by-step Readiness Timeline & Eligibility Gap Checklist", navy, 14.5, False),
        ("Guide User to Official Channel", "Direct Link to Official Portal / Nearby Nodal Bank Centre", gold, 7.5, False),
        ("End", "Successful Scheme Identification & Empowered Citizen", green, 1.5, True)
    ]

    for i, (title, desc, color, y, is_terminal) in enumerate(steps):
        # Shape
        if is_terminal:
            box = patches.FancyBboxPatch((20, y - 2.2), 60, 4.4, boxstyle="round,pad=1.5", 
                                         edgecolor=color, facecolor=color, linewidth=1.5)
            ax.add_patch(box)
            ax.text(50, y + 0.6, title, fontsize=10, fontweight='bold', color='#FFFFFF', ha='center')
            ax.text(50, y - 1.0, desc, fontsize=7.5, color='#F0FDF4', ha='center')
        else:
            border_col = gold if color == gold else navy
            bg_col = '#FFFDF5' if color == gold else '#FFFFFF'
            box = patches.FancyBboxPatch((15, y - 2.5), 70, 5.0, boxstyle="round,pad=0.8", 
                                         edgecolor=border_col, facecolor=bg_col, linewidth=1.5)
            ax.add_patch(box)
            # Step badge
            ax.add_patch(patches.Circle((20, y), 1.8, facecolor=border_col, edgecolor=border_col))
            ax.text(20, y, str(i), fontsize=8, fontweight='bold', color='#FFFFFF', ha='center', va='center')
            ax.text(52, y + 0.9, title, fontsize=9.5, fontweight='bold', color=navy, ha='center')
            ax.text(52, y - 1.1, desc, fontsize=7.5, color=gray, ha='center')

        # Arrow to next
        if i < len(steps) - 1:
            next_y = steps[i+1][3]
            arrow_start = y - 2.6
            arrow_end = next_y + 2.6
            ax.annotate('', xy=(50, arrow_end), xytext=(50, arrow_start),
                        arrowprops=dict(facecolor=navy, edgecolor=navy, width=1.5, headwidth=5))

    plt.tight_layout()
    plt.savefig('JANSAARTHI_REPORT/images/project_flowchart.png', bbox_inches='tight', dpi=300)
    plt.close()
    print("Saved flowchart diagram.")

if __name__ == '__main__':
    create_architecture_diagram()
    create_flowchart_diagram()
