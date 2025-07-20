# interactive-ds-prompt.md
Agent Task: Build Interactive Data-Structure Learning Suite  
Audience: College students (non-experts)  
UI Framework: Java Swing (reuse existing raven.swing components)  
Hosting Window: `DashboardFrame` (created in previous phase)

────────────────────────────────────────
1.  Overview
────────────
Generate **7 self-contained Java demos** packaged as:

• 1 JAR per topic (or 1 big JAR with tabs)  
• Each demo = **visualizer panel + control panel**  
• “Play / Pause / Next Step / Reset” buttons  
• Real-time updates (colors, highlights, counters)  
• Export button → PDF mini-book (iText 7) saved beside JAR

────────────────────────────────────────
2.  Topics & Required Features
──────────────────────────────
| # | Topic | Interactive Elements | PDF Contents |
|---|-------|----------------------|--------------|
| 1 | Graph – Shortest Path (Dijkstra) | • Click to add vertex / edge • Weight editor • Run step-by-step • Table of tentative distances | Algorithm walk-through + complexity O((V+E) log V) |
| 2 | Hashing & Hash Functions | • Live hash-table (chaining & open addressing) • Insert / search / delete • Collision counter • Choose hash fn (division, multiplication, universal) | Load factor, amortized O(1) analysis |
| 3 | Binary Heap (min & max) | • Insert, extract-min, build-heap buttons • Array + tree view • Color-coded swaps | Array vs tree, O(log n) per op |
| 4 | Heapsort | • Build heap animation • Extract step-by-step • Compare vs quicksort counter | Time/space analysis |
| 5 | Binary Search Tree (BST) | • Insert, delete (3 cases), find successor • Pre/In/Post traversal playback | Height vs balance |
| 6 | AVL Tree | • Same ops as BST + rotations (LL, RR, LR, RL) • Balance factor overlay | Rotations proof & O(log n) guarantee |
| 7 | Dynamic Array (ArrayList) | • Append / remove / resize • Resize visualizer (capacity doubling) | Amortized O(1) math |

────────────────────────────────────────
3.  Technical Stack
──────────────────
• Java 17  
• SQLite (already in project) – **optional** to save user sessions  
• Swing (reuse `com.raven.swing.*` styles)  
• iText 7 Core (pdf generation)  
• JGraphT / Prefuse **optional** – prefer lightweight custom painting for clarity

────────────────────────────────────────
4.  Deliverables
───────────────
1. `ds-core/`  
   • `AnimationEngine.java` – timer-based step engine (pause, resume, speed slider)  
   • `PDFExporter.java` – iText wrapper that receives JPanel + text → PDF

2. `ds-modules/` (one package per topic)  
   • `{Topic}Panel.java` – main visualizer extending `JPanel`  
   • `{Topic}Controls.java` – buttons & fields  
   • `{Topic}Algorithm.java` – pure logic (no GUI)

3. `DashboardFrame.java` patch  
   • Add `JTabbedPane` with 7 tabs loading above panels  
   • Menu item “Export PDF for current tab”

4. `docs/`  
   • `DataStructuresGuide.pdf` – aggregated mini-book (auto-generated)

5. `pom.xml` additions  
```xml
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
    <type>pom</type>
</dependency>

────────────────────────────────────────
5.  Visualizer Requirements
──────────────────────────
• Colors: consistent palette (raven green #07A479, white, red for swaps)
• Fonts: same as current project (Sans-Serif 14)
• Accessibility: tool-tips on every button, keyboard shortcuts (Space=next, R=reset)
• Responsive: panel scales with window resize (MigLayout)
Example for Dijkstra:   

    VertexPanel (circle + label)  
    EdgePanel (line + weight label)  
    DistTablePanel (JTable overlay)  
    ControlPanel (speed slider, step button, reset)

────────────────────────────────────────
6.  PDF Auto-Generation Rules
──────────────────────────────
For each topic create:
Title page (topic name, complexity)
2-3 paragraphs of plain-English explanation
Pseudo-code with line numbers
Screenshots of 3 key frames (auto-captured)
Complexity table (best/average/worst)
Tiny QR-code linking to Wikipedia article
────────────────────────────────────────
7.  Acceptance Criteria
──────────────────────
• Run java -jar ds-suite.jar → DashboardFrame opens with 7 tabs.
• Each tab: user can perform at least 5 manual operations and watch animation.
• “Export PDF” produces a 6–10-page file < 1 MB in ./exported/ folder.
• All code compiles with mvn package and zero external native libs.
• Memory footprint < 256 MB for 1 000-element dataset.
• Dark/Light theme toggle reuses existing Main look-and-feel.
────────────────────────────────────────
8.  Quick Test Script
────────────────────
Open “BST” tab → insert 50,30,70,20 → click delete 30 → see case-2 splice.
Switch to “AVL” → insert same keys → watch LL rotation.
Click “Export PDF” → verify rotation steps appear in PDF.
Open “Graph” → create 5-node graph → run Dijkstra → step 7 → distance table matches.
Close app → reopen → selected theme & last speed slider value restored.
────────────────────────────────────────
